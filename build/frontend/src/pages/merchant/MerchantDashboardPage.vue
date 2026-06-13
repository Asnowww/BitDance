<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import {
  BarChart3, Calendar, ClipboardCheck, MessageSquareReply, RefreshCw,
  TicketCheck, Users
} from 'lucide-vue-next';
import {
  checkinMerchantWorkshopOrder,
  fetchMerchantWorkshopOrders,
  type MerchantWorkshopOrderRow
} from '@/api/merchant';
import {
  fetchReviewReplyQueue,
  createReviewReply,
  fetchReviewReplies,
  type ReviewItem,
  type ReviewReplyDto
} from '@/api/review';
import request from '@/utils/request';

const router = useRouter();

const activeTab = ref<'orders' | 'schedule' | 'replies'>('orders');

const loading = ref(false);
const orders = ref<MerchantWorkshopOrderRow[]>([]);
const selectedOrder = ref<MerchantWorkshopOrderRow | null>(null);
const manualCode = ref('');
const checking = ref(false);

interface ScheduleEntry {
  id: number;
  courseId: number;
  studioId: number;
  coachId: number;
  classroomName: string;
  startAt: string;
  endAt: string;
  capacity: number;
  bookedCount: number;
  status: string;
}

const scheduleLoading = ref(false);
const scheduleItems = ref<ScheduleEntry[]>([]);
const scheduleWeekOffset = ref(0);

const reviewLoading = ref(false);
const reviewQueue = ref<ReviewItem[]>([]);
const replyTexts = ref<Record<number, string>>({});
const replySending = ref<Record<number, boolean>>({});
const repliesMap = ref<Record<number, ReviewReplyDto[]>>({});

const paidOrders = computed(() => orders.value.filter((o) => o.status.toLowerCase() === 'paid'));
const checkedOrders = computed(() =>
  orders.value.filter((o) => ['checked_in', 'completed'].includes(o.status.toLowerCase()))
);
const totalAmount = computed(() =>
  orders.value.reduce((sum, o) => sum + Number(o.amount || 0), 0)
);

const metrics = computed(() => [
  { label: '待核销', value: paidOrders.value.length, icon: TicketCheck },
  { label: '已核销', value: checkedOrders.value.length, icon: ClipboardCheck },
  { label: '订单数', value: orders.value.length, icon: Users },
  { label: '流水', value: `¥${totalAmount.value.toFixed(0)}`, icon: BarChart3 }
]);

const visibleOrders = computed(() => orders.value.slice(0, 8));

const loadOrders = async () => {
  loading.value = true;
  try {
    orders.value = await fetchMerchantWorkshopOrders();
  } finally {
    loading.value = false;
  }
};

const openCheckin = (order: MerchantWorkshopOrderRow) => {
  if (order.status.toLowerCase() !== 'paid') {
    showFailToast('只有已支付订单可以核销');
    return;
  }
  selectedOrder.value = order;
  manualCode.value = order.checkinCode ?? '';
};

const closeCheckin = () => {
  selectedOrder.value = null;
  manualCode.value = '';
  checking.value = false;
};

const submitCheckin = async () => {
  if (!selectedOrder.value) return;
  if (!manualCode.value.trim()) {
    showFailToast('请输入签到码');
    return;
  }
  checking.value = true;
  try {
    await checkinMerchantWorkshopOrder(selectedOrder.value.orderId, manualCode.value.trim());
    showSuccessToast('核销成功');
    closeCheckin();
    await loadOrders();
  } finally {
    checking.value = false;
  }
};

function weekRange(offset: number) {
  const now = new Date();
  const monday = new Date(now);
  monday.setDate(now.getDate() - now.getDay() + 1 + offset * 7);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  return {
    from: monday.toISOString().slice(0, 10),
    to: sunday.toISOString().slice(0, 10),
    label: `${monday.getMonth() + 1}/${monday.getDate()} - ${sunday.getMonth() + 1}/${sunday.getDate()}`
  };
}

const currentWeek = computed(() => weekRange(scheduleWeekOffset.value));

const loadSchedule = async () => {
  scheduleLoading.value = true;
  try {
    const { from, to } = currentWeek.value;
    const resp = await request.get<unknown, ScheduleEntry[]>(
      `/public/studios/1/schedules`,
      { params: { from, to } }
    );
    scheduleItems.value = Array.isArray(resp) ? resp : [];
  } catch {
    scheduleItems.value = [];
  } finally {
    scheduleLoading.value = false;
  }
};

const prevWeek = () => { scheduleWeekOffset.value--; loadSchedule(); };
const nextWeek = () => { scheduleWeekOffset.value++; loadSchedule(); };

function fmtTime(iso: string) {
  if (!iso) return '--';
  const d = new Date(iso);
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}

function fmtDate(iso: string) {
  if (!iso) return '--';
  const d = new Date(iso);
  const days = ['日', '一', '二', '三', '四', '五', '六'];
  return `周${days[d.getDay()]} ${d.getMonth() + 1}/${d.getDate()}`;
}

const loadReviews = async () => {
  reviewLoading.value = true;
  try {
    const resp = await fetchReviewReplyQueue({ page: 1, pageSize: 20 });
    reviewQueue.value = resp.list ?? [];
    for (const r of reviewQueue.value) {
      try {
        repliesMap.value[r.id] = await fetchReviewReplies(r.id);
      } catch {
        repliesMap.value[r.id] = [];
      }
    }
  } catch {
    reviewQueue.value = [];
  } finally {
    reviewLoading.value = false;
  }
};

const sendReply = async (reviewId: number) => {
  const text = (replyTexts.value[reviewId] ?? '').trim();
  if (!text) { showFailToast('请输入回复内容'); return; }
  replySending.value[reviewId] = true;
  try {
    const reply = await createReviewReply({ reviewId, replyContent: text, isOfficial: true });
    if (!repliesMap.value[reviewId]) repliesMap.value[reviewId] = [];
    repliesMap.value[reviewId].push(reply);
    replyTexts.value[reviewId] = '';
    showSuccessToast('回复成功');
  } finally {
    replySending.value[reviewId] = false;
  }
};

const switchTab = (tab: 'orders' | 'schedule' | 'replies') => {
  activeTab.value = tab;
  if (tab === 'schedule' && scheduleItems.value.length === 0) loadSchedule();
  if (tab === 'replies' && reviewQueue.value.length === 0) loadReviews();
};

onMounted(loadOrders);
</script>

<template>
  <main class="merchant-page">
    <header class="hero">
      <button class="back" type="button" aria-label="返回" @click="router.back()">‹</button>
      <span>STUDIO ADMIN</span>
      <h1>舞室管理员工作台</h1>
      <p>管理订单、课表与评价回复</p>
    </header>

    <section class="metrics" aria-label="舞室经营数据">
      <article v-for="item in metrics" :key="item.label" class="metric">
        <component :is="item.icon" :size="19" :stroke-width="2.2" />
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </article>
    </section>

    <nav class="tabs">
      <button
        :class="{ active: activeTab === 'orders' }" type="button"
        @click="switchTab('orders')"
      >
        <TicketCheck :size="16" :stroke-width="2.2" />
        订单
      </button>
      <button
        :class="{ active: activeTab === 'schedule' }" type="button"
        @click="switchTab('schedule')"
      >
        <Calendar :size="16" :stroke-width="2.2" />
        课表
      </button>
      <button
        :class="{ active: activeTab === 'replies' }" type="button"
        @click="switchTab('replies')"
      >
        <MessageSquareReply :size="16" :stroke-width="2.2" />
        评价回复
      </button>
    </nav>

    <!-- Orders Tab -->
    <section v-if="activeTab === 'orders'" class="panel">
      <div class="panel__head">
        <div>
          <h2>Workshop 订单</h2>
          <p>{{ loading ? '正在读取后端数据' : `来自后端 ${orders.length} 条订单` }}</p>
        </div>
        <button type="button" aria-label="刷新订单" @click="loadOrders">
          <RefreshCw :size="17" :stroke-width="2.3" />
        </button>
      </div>
      <p v-if="!loading && visibleOrders.length === 0" class="empty">当前舞室暂无订单</p>
      <article v-for="order in visibleOrders" :key="order.orderId" class="order">
        <div class="order__main">
          <strong>{{ order.workshopTitle }}</strong>
          <span>{{ order.buyerName }} · {{ order.sessionDate }} {{ order.sessionTime }}</span>
          <em>订单 #{{ order.orderId }} · {{ order.status }} · ¥{{ Number(order.amount).toFixed(2) }}</em>
        </div>
        <button
          v-if="order.status.toLowerCase() === 'paid'"
          type="button" class="order__action"
          @click="openCheckin(order)"
        >
          核销
        </button>
        <span v-else class="order__status">{{ order.status }}</span>
      </article>
    </section>

    <!-- Schedule Tab -->
    <section v-if="activeTab === 'schedule'" class="panel">
      <div class="panel__head">
        <div>
          <h2>课表</h2>
          <p>{{ scheduleLoading ? '加载中...' : `${currentWeek.label} · ${scheduleItems.length} 节课` }}</p>
        </div>
        <button type="button" aria-label="刷新课表" @click="loadSchedule">
          <RefreshCw :size="17" :stroke-width="2.3" />
        </button>
      </div>
      <div class="week-nav">
        <button type="button" @click="prevWeek">← 上一周</button>
        <span>{{ currentWeek.label }}</span>
        <button type="button" @click="nextWeek">下一周 →</button>
      </div>
      <p v-if="!scheduleLoading && scheduleItems.length === 0" class="empty">本周暂无排课</p>
      <article v-for="s in scheduleItems" :key="s.id" class="sched">
        <div class="sched__time">
          <strong>{{ fmtDate(s.startAt) }}</strong>
          <span>{{ fmtTime(s.startAt) }} – {{ fmtTime(s.endAt) }}</span>
        </div>
        <div class="sched__info">
          <strong>{{ s.classroomName || `课程 #${s.courseId}` }}</strong>
          <span>已报 {{ s.bookedCount }}/{{ s.capacity }} · {{ s.status }}</span>
        </div>
      </article>
    </section>

    <!-- Review Replies Tab -->
    <section v-if="activeTab === 'replies'" class="panel">
      <div class="panel__head">
        <div>
          <h2>评价回复管理</h2>
          <p>{{ reviewLoading ? '加载中...' : `${reviewQueue.length} 条待处理` }}</p>
        </div>
        <button type="button" aria-label="刷新" @click="loadReviews">
          <RefreshCw :size="17" :stroke-width="2.3" />
        </button>
      </div>
      <p v-if="!reviewLoading && reviewQueue.length === 0" class="empty">暂无待回复评价</p>
      <article v-for="rv in reviewQueue" :key="rv.id" class="review-card">
        <div class="review-card__head">
          <strong>{{ rv.targetType }} #{{ rv.targetId }}</strong>
          <span>★{{ rv.overallScore }} · {{ rv.reviewStatus }}</span>
        </div>
        <p class="review-card__text">{{ rv.contentText }}</p>
        <div v-if="(repliesMap[rv.id] || []).length > 0" class="review-card__replies">
          <div v-for="rp in repliesMap[rv.id]" :key="rp.id" class="reply-bubble">
            <span v-if="rp.isOfficial" class="official-badge">官方</span>
            {{ rp.replyContent }}
          </div>
        </div>
        <div class="review-card__input">
          <input
            v-model="replyTexts[rv.id]"
            type="text"
            placeholder="输入回复..."
            @keyup.enter="sendReply(rv.id)"
          />
          <button
            type="button"
            :disabled="replySending[rv.id]"
            @click="sendReply(rv.id)"
          >
            {{ replySending[rv.id] ? '...' : '回复' }}
          </button>
        </div>
      </article>
    </section>

    <!-- Checkin Sheet -->
    <Teleport to="body">
      <Transition name="sheet-fade">
        <button
          v-if="selectedOrder" class="sheet-mask" type="button"
          aria-label="关闭核销面板" @click="closeCheckin"
        />
      </Transition>
      <Transition name="sheet-slide">
        <aside v-if="selectedOrder" class="sheet" role="dialog" aria-modal="true" aria-label="订单核销">
          <div class="sheet__handle" />
          <h2>核销订单 #{{ selectedOrder.orderId }}</h2>
          <p>{{ selectedOrder.workshopTitle }} · {{ selectedOrder.buyerName }}</p>
          <label>
            <span>签到码</span>
            <input v-model.trim="manualCode" type="text" autocomplete="off" />
          </label>
          <div class="sheet__actions">
            <button type="button" class="sheet__cancel" @click="closeCheckin">取消</button>
            <button type="button" class="sheet__confirm" :disabled="checking" @click="submitCheckin">
              {{ checking ? '核销中' : '确认核销' }}
            </button>
          </div>
        </aside>
      </Transition>
    </Teleport>
  </main>
</template>

<style lang="scss" scoped>
.merchant-page {
  min-height: 100vh;
  padding: 18px 18px 32px;
  background: #fff;
  color: #111;
  box-sizing: border-box;
}

.hero {
  position: relative;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 18px;
  background: #111;
  color: #fff;
  box-sizing: border-box;

  span, h1, p { margin: 0; letter-spacing: 0; }
  span { color: #d8d8d8; font-size: 11px; line-height: 1.2; font-weight: 900; }
  h1 { margin-top: 8px; max-width: 260px; font-size: 32px; line-height: 1.02; font-weight: 900; }
  p { margin-top: 8px; color: #d8d8d8; font-size: 13px; line-height: 1.35; font-weight: 700; }
}

.back {
  position: absolute; top: 14px; left: 14px;
  width: 40px; height: 40px; border: 0; border-radius: 999px;
  background: #fff; color: #111; font-size: 32px; line-height: 1; cursor: pointer;
}

.metrics {
  display: grid; grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px; margin-top: 14px; background: #111;
}
.metric {
  min-height: 86px; padding: 10px 8px; background: #f5f5f5;
  display: flex; flex-direction: column; justify-content: center; gap: 6px; box-sizing: border-box;
  strong { font-size: 22px; line-height: 1; font-weight: 900; }
  span { color: #707072; font-size: 11px; line-height: 1.2; font-weight: 800; }
}

.tabs {
  display: flex; gap: 0; margin-top: 14px;
  border-bottom: 2px solid #e5e5e5;
  button {
    flex: 1; display: inline-flex; align-items: center; justify-content: center; gap: 6px;
    height: 46px; border: 0; border-bottom: 2px solid transparent;
    margin-bottom: -2px; background: transparent; color: #707072;
    font-size: 14px; font-weight: 800; cursor: pointer;
    &.active { color: #111; border-bottom-color: #111; }
  }
}

.panel { margin-top: 18px; }
.panel__head {
  display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
  div { min-width: 0; flex: 1; }
  h2, p { margin: 0; letter-spacing: 0; }
  h2 { font-size: 22px; line-height: 1.2; font-weight: 900; }
  p { margin-top: 3px; color: #707072; font-size: 12px; line-height: 1.3; font-weight: 700; }
  button {
    width: 40px; height: 40px; border: 0; border-radius: 999px;
    background: #f5f5f5; color: #111; display: grid; place-items: center; cursor: pointer;
  }
}

.empty { margin: 18px 0; color: #707072; font-size: 13px; line-height: 1.45; font-weight: 700; }

.order {
  min-height: 74px; padding: 12px 0; border-bottom: 1px solid #e5e5e5;
  display: flex; align-items: center; gap: 12px;
}
.order__main {
  min-width: 0; flex: 1; display: flex; flex-direction: column; gap: 4px;
  strong, span, em { min-width: 0; overflow-wrap: anywhere; letter-spacing: 0; }
  strong { font-size: 15px; line-height: 1.25; font-weight: 900; }
  span, em { color: #707072; font-size: 12px; line-height: 1.35; font-style: normal; font-weight: 700; }
}
.order__action, .order__status {
  flex: none; min-width: 62px; min-height: 36px; padding: 8px 13px;
  border: 0; border-radius: 999px; font-size: 13px; line-height: 1.2; font-weight: 900; box-sizing: border-box;
}
.order__action { background: #111; color: #fff; cursor: pointer; }
.order__status { display: grid; place-items: center; background: #f5f5f5; color: #707072; }

/* Schedule */
.week-nav {
  display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 12px;
  button {
    height: 34px; padding: 0 14px; border: 1px solid #e5e5e5; border-radius: 999px;
    background: #fff; color: #111; font-size: 13px; font-weight: 800; cursor: pointer;
  }
  span { font-size: 14px; font-weight: 900; }
}
.sched {
  display: flex; gap: 14px; padding: 12px 0; border-bottom: 1px solid #e5e5e5;
  &__time {
    flex: none; width: 90px; display: flex; flex-direction: column; gap: 2px;
    strong { font-size: 13px; font-weight: 900; }
    span { color: #707072; font-size: 12px; font-weight: 700; }
  }
  &__info {
    flex: 1; display: flex; flex-direction: column; gap: 4px;
    strong { font-size: 15px; font-weight: 900; }
    span { color: #707072; font-size: 12px; font-weight: 700; }
  }
}

/* Review replies */
.review-card {
  padding: 14px 0; border-bottom: 1px solid #e5e5e5;
  &__head {
    display: flex; justify-content: space-between; align-items: center;
    strong { font-size: 14px; font-weight: 900; }
    span { color: #707072; font-size: 12px; font-weight: 700; }
  }
  &__text { margin: 8px 0; font-size: 14px; line-height: 1.5; font-weight: 600; color: #333; }
  &__replies { margin: 8px 0; display: flex; flex-direction: column; gap: 6px; }
  &__input {
    display: flex; gap: 8px; margin-top: 8px;
    input {
      flex: 1; height: 38px; padding: 0 12px; border: 1px solid #e5e5e5; border-radius: 8px;
      background: #f5f5f5; color: #111; font: inherit; font-size: 14px; font-weight: 600; outline: none; box-sizing: border-box;
    }
    button {
      flex: none; height: 38px; padding: 0 16px; border: 0; border-radius: 8px;
      background: #111; color: #fff; font-size: 13px; font-weight: 900; cursor: pointer;
      &:disabled { opacity: 0.5; cursor: progress; }
    }
  }
}
.reply-bubble {
  padding: 8px 12px; border-radius: 10px; background: #f0f0f0;
  font-size: 13px; line-height: 1.45; font-weight: 600;
}
.official-badge {
  display: inline-block; padding: 2px 6px; margin-right: 6px; border-radius: 4px;
  background: #111; color: #fff; font-size: 10px; font-weight: 900;
}

/* Sheet */
.sheet-mask {
  position: fixed; inset: 0; z-index: 120; border: 0;
  background: rgb(17 17 17 / 42%); backdrop-filter: blur(5px);
}
.sheet {
  position: fixed; right: 0; bottom: 0; left: 0; z-index: 130;
  width: 100%; max-width: 480px; margin: 0 auto;
  padding: 10px 18px calc(18px + env(safe-area-inset-bottom));
  background: #fff; box-shadow: 0 -4px 18px rgb(0 0 0 / 12%); box-sizing: border-box;
  h2, p { margin: 0; letter-spacing: 0; }
  h2 { margin-top: 12px; font-size: 24px; line-height: 1.2; font-weight: 900; }
  p { margin-top: 8px; color: #707072; font-size: 13px; line-height: 1.45; font-weight: 700; }
  label { display: flex; flex-direction: column; gap: 8px; margin-top: 18px; }
  label span { font-size: 13px; line-height: 1.2; font-weight: 900; }
  input {
    height: 46px; padding: 0 14px; border: 1px solid #e5e5e5; background: #f5f5f5;
    color: #111; font-size: 15px; line-height: 1.2; font-weight: 800; outline: none; box-sizing: border-box;
  }
}
.sheet__handle { width: 46px; height: 5px; margin: 0 auto 8px; border-radius: 999px; background: #e5e5e5; }
.sheet__actions { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-top: 18px; }
.sheet__cancel, .sheet__confirm {
  min-height: 50px; border: 0; border-radius: 999px; font-size: 14px; line-height: 1.2; font-weight: 900; cursor: pointer;
}
.sheet__cancel { background: #f5f5f5; color: #111; }
.sheet__confirm { background: #111; color: #fff; &:disabled { opacity: 0.55; cursor: progress; } }

.sheet-fade-enter-active, .sheet-fade-leave-active, .sheet-slide-enter-active, .sheet-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.sheet-fade-enter-from, .sheet-fade-leave-to { opacity: 0; }
.sheet-slide-enter-from, .sheet-slide-leave-to { opacity: 0; transform: translateY(24px); }

@media (max-width: 360px) {
  .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
