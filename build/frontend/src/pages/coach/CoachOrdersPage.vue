<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { useOpsStore } from '@/stores/ops';
import {
  fetchMerchantCourseOrders,
  fetchMerchantWorkshopOrders,
  fetchCourseRefunds,
  fetchCourseCheckinHistory,
  fetchWorkshopCheckinHistory,
  checkinCourseOrder,
  checkinWorkshopOrder,
  approveCourseRefund,
  rejectCourseRefund,
  type CourseOrder,
  type WorkshopOrder,
  type CourseRefund
} from '@/api/coachOps';

type Tab = 'course' | 'workshop' | 'refund' | 'history';

const router = useRouter();
const ops = useOpsStore();
const tab = ref<Tab>('course');
const loading = ref(true);
const statusFilter = ref('');

const courseOrders = ref<CourseOrder[]>([]);
const workshopOrders = ref<WorkshopOrder[]>([]);
const refunds = ref<CourseRefund[]>([]);
const courseHistory = ref<CourseOrder[]>([]);
const workshopHistory = ref<WorkshopOrder[]>([]);

const tabs: Array<{ key: Tab; label: string }> = [
  { key: 'course', label: '课程订单' },
  { key: 'workshop', label: 'Workshop 订单' },
  { key: 'refund', label: '退款审核' },
  { key: 'history', label: '核销历史' }
];

const orderStatusMeta: Record<string, { label: string; cls: string }> = {
  pending_payment: { label: '待支付', cls: 'warn' },
  paid: { label: '已支付', cls: 'ok' },
  refund_requested: { label: '退款审核中', cls: 'warn' },
  refunded: { label: '已退款', cls: 'bad' },
  refund_rejected: { label: '退款被拒', cls: '' },
  checked_in: { label: '已核销', cls: 'ink' },
  completed: { label: '已完成', cls: '' },
  canceled: { label: '已取消', cls: '' }
};

const refundStatusMeta: Record<string, { label: string; cls: string }> = {
  pending: { label: '待审核', cls: 'warn' },
  approved: { label: '已退款', cls: 'ok' },
  rejected: { label: '已拒绝', cls: 'bad' }
};

const statusOptions = computed(() => {
  if (tab.value === 'course') {
    return ['', 'pending_payment', 'paid', 'refund_requested', 'checked_in', 'refunded'];
  }
  if (tab.value === 'workshop') return ['', 'pending_payment', 'paid', 'checked_in', 'refunded'];
  return [];
});

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  await ops.refresh();
  const sid = ops.studioId;
  if (!sid) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    if (tab.value === 'course') {
      courseOrders.value = await fetchMerchantCourseOrders(sid, statusFilter.value || undefined);
    } else if (tab.value === 'workshop') {
      workshopOrders.value = await fetchMerchantWorkshopOrders({
        studioId: sid,
        status: statusFilter.value || undefined
      });
    } else if (tab.value === 'refund') {
      refunds.value = await fetchCourseRefunds(sid);
    } else {
      const [c, w] = await Promise.all([
        fetchCourseCheckinHistory(sid),
        fetchWorkshopCheckinHistory(sid).catch(() => [])
      ]);
      courseHistory.value = c;
      workshopHistory.value = w;
    }
  } finally {
    loading.value = false;
  }
};

const switchTab = (t: Tab) => {
  tab.value = t;
  statusFilter.value = '';
  load();
};

const setStatusFilter = (s: string) => {
  statusFilter.value = s;
  load();
};

const checkinCourse = async (o: CourseOrder) => {
  await showConfirmDialog({ title: '核销确认', message: `确认核销课程订单 ${o.orderNo}?` });
  await checkinCourseOrder(o.id, o.checkinCode ?? '');
  showSuccessToast('核销成功');
  load();
};

const checkinWorkshop = async (o: WorkshopOrder) => {
  await showConfirmDialog({ title: '核销确认', message: `确认核销 Workshop 订单 ${o.orderNo}?` });
  await checkinWorkshopOrder(o.id, o.checkinCode ?? '');
  showSuccessToast('核销成功');
  load();
};

const handleRefund = async (r: CourseRefund, action: 'approve' | 'reject') => {
  await showConfirmDialog({
    title: action === 'approve' ? '同意退款' : '拒绝退款',
    message:
      action === 'approve'
        ? `确认同意订单 #${r.courseOrderId} 的退款申请?款项将原路退回。`
        : `确认拒绝订单 #${r.courseOrderId} 的退款申请?`
  });
  if (action === 'approve') await approveCourseRefund(r.id);
  else await rejectCourseRefund(r.id);
  showSuccessToast('已处理');
  load();
};

onMounted(load);
</script>

<template>
  <main class="orders-page">
    <PenTopBar title="订单中心" :show-share="false" />

    <nav class="chips">
      <button
        v-for="t in tabs"
        :key="t.key"
        :class="{ active: tab === t.key }"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
      </button>
    </nav>

    <nav v-if="statusOptions.length" class="chips sub">
      <button
        v-for="s in statusOptions"
        :key="s"
        :class="{ active: statusFilter === s }"
        @click="setStatusFilter(s)"
      >
        {{ s === '' ? '全部状态' : orderStatusMeta[s]?.label ?? s }}
      </button>
    </nav>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <EmptyState
        v-else-if="!ops.studioId"
        title="尚未开通商家后台"
        desc="完成舞室入驻或认领后查看订单"
        action-text="去入驻 / 认领"
        @action="router.push('/coach/studio-claim')"
      />

      <!-- 课程订单 -->
      <template v-else-if="tab === 'course'">
        <EmptyState v-if="!courseOrders.length" title="暂无课程订单" />
        <article v-for="o in courseOrders" :key="o.id" class="card">
          <div class="head">
            <h3>课程订单 · 学员 #{{ o.userId }}</h3>
            <span class="badge" :class="orderStatusMeta[o.orderStatus]?.cls">
              {{ orderStatusMeta[o.orderStatus]?.label ?? o.orderStatus }}
            </span>
          </div>
          <p class="meta">
            <span>{{ o.orderNo }}</span>
            <span>¥{{ o.amountPayable }}</span>
            <span>课程 #{{ o.courseId }} / 场次 #{{ o.courseScheduleId }}</span>
            <span>下单 {{ fmt(o.createdAt) }}</span>
          </p>
          <div v-if="o.orderStatus === 'paid'" class="actions">
            <button class="primary" @click="checkinCourse(o)">核销</button>
          </div>
        </article>
      </template>

      <!-- Workshop 订单 -->
      <template v-else-if="tab === 'workshop'">
        <EmptyState v-if="!workshopOrders.length" title="暂无 Workshop 订单" />
        <article v-for="o in workshopOrders" :key="o.id" class="card">
          <div class="head">
            <h3>Workshop 订单 · 学员 #{{ o.userId }}</h3>
            <span class="badge" :class="orderStatusMeta[o.orderStatus]?.cls">
              {{ orderStatusMeta[o.orderStatus]?.label ?? o.orderStatus }}
            </span>
          </div>
          <p class="meta">
            <span>{{ o.orderNo }}</span>
            <span>¥{{ o.amountPayable }}</span>
            <span>Workshop #{{ o.workshopId }}</span>
            <span>下单 {{ fmt(o.createdAt) }}</span>
          </p>
          <div v-if="o.orderStatus === 'paid'" class="actions">
            <button class="primary" @click="checkinWorkshop(o)">核销</button>
          </div>
        </article>
      </template>

      <!-- 退款审核 -->
      <template v-else-if="tab === 'refund'">
        <EmptyState v-if="!refunds.length" title="暂无退款申请" />
        <article v-for="r in refunds" :key="r.id" class="card">
          <div class="head">
            <h3>退款申请 · 订单 #{{ r.courseOrderId }}</h3>
            <span class="badge" :class="refundStatusMeta[r.requestStatus]?.cls">
              {{ refundStatusMeta[r.requestStatus]?.label ?? r.requestStatus }}
            </span>
          </div>
          <p class="meta">
            <span>申请人 #{{ r.requesterUserId }}</span>
            <span>{{ fmt(r.createdAt) }}</span>
          </p>
          <p v-if="r.refundReason" class="reason">理由:{{ r.refundReason }}</p>
          <p v-if="r.reviewRemark" class="reason">处理备注:{{ r.reviewRemark }}</p>
          <div v-if="r.requestStatus === 'pending'" class="actions">
            <button class="primary" @click="handleRefund(r, 'approve')">同意退款</button>
            <button class="danger" @click="handleRefund(r, 'reject')">拒绝</button>
          </div>
        </article>
      </template>

      <!-- 核销历史 -->
      <template v-else>
        <EmptyState
          v-if="!courseHistory.length && !workshopHistory.length"
          title="暂无核销记录"
        />
        <article v-for="o in courseHistory" :key="`c${o.id}`" class="card">
          <div class="head">
            <h3>课程核销 · 学员 #{{ o.userId }}</h3>
            <span class="badge ink">已核销</span>
          </div>
          <p class="meta">
            <span>{{ o.orderNo }}</span>
            <span>¥{{ o.amountPaid }}</span>
            <span>完成 {{ fmt(o.completedAt ?? o.paidAt) }}</span>
          </p>
        </article>
        <article v-for="o in workshopHistory" :key="`w${o.id}`" class="card">
          <div class="head">
            <h3>Workshop 核销 · 学员 #{{ o.userId }}</h3>
            <span class="badge ink">已核销</span>
          </div>
          <p class="meta">
            <span>{{ o.orderNo }}</span>
            <span>¥{{ o.amountPaid }}</span>
          </p>
        </article>
      </template>
    </section>

    <footer v-if="ops.studioId" class="submit-bar">
      <button @click="router.push('/coach/checkin')">输入核销码 / 扫码核销</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.orders-page {
  @include ops-page;
}
.chips {
  @include ops-chip-row;
  &.sub {
    padding-top: 0;
    button {
      height: 34px;
      font-size: 12px;
    }
  }
}
.body {
  @include ops-body;
}
.loading {
  @include ops-loading;
}
.card {
  @include ops-card;
}
.head {
  @include ops-card-head;
}
.badge {
  @include ops-badge;
}
.meta {
  @include ops-meta;
}
.actions {
  @include ops-actions;
}
.reason {
  margin: 10px 0 0;
  color: $pen-charcoal;
  font-size: 13px;
  line-height: 1.5;
}
.submit-bar {
  @include ops-submit-bar;
}
</style>
