<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { showConfirmDialog, showSuccessToast } from 'vant';
import QRCode from 'qrcode';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import {
  fetchMyCourseOrders,
  payCourseOrder,
  cancelCourseOrder,
  requestCourseRefund,
  type CourseOrder
} from '@/api/coachOps';

const orders = ref<CourseOrder[]>([]);
const loading = ref(true);
const filter = ref<'all' | 'pending_payment' | 'paid' | 'done'>('all');

const filters = [
  { key: 'all', label: '全部' },
  { key: 'pending_payment', label: '待支付' },
  { key: 'paid', label: '待上课' },
  { key: 'done', label: '已完成/退款' }
] as const;

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending_payment: { label: '待支付', cls: 'warn' },
  paid: { label: '已支付 · 待上课', cls: 'ok' },
  refund_requested: { label: '退款审核中', cls: 'warn' },
  refunded: { label: '已退款', cls: 'bad' },
  refund_rejected: { label: '退款被拒', cls: '' },
  checked_in: { label: '已核销', cls: 'ink' },
  completed: { label: '已完成', cls: '' },
  canceled: { label: '已取消', cls: '' }
};

const list = computed(() => {
  if (filter.value === 'all') return orders.value;
  if (filter.value === 'done') {
    return orders.value.filter((o) =>
      ['refunded', 'checked_in', 'completed', 'canceled', 'refund_rejected'].includes(o.orderStatus)
    );
  }
  return orders.value.filter((o) => o.orderStatus === filter.value);
});

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  loading.value = true;
  try {
    orders.value = await fetchMyCourseOrders();
  } finally {
    loading.value = false;
  }
};

// ---------- 操作 ----------
const acting = ref(false);

const pay = async (o: CourseOrder) => {
  if (acting.value) return;
  await showConfirmDialog({
    title: '模拟支付',
    message: `支付 ¥${o.amountPayable}?当前为演示环境,点击确认即完成支付。`
  });
  acting.value = true;
  try {
    await payCourseOrder(o.id);
    showSuccessToast('支付成功,已生成核销码');
    load();
  } finally {
    acting.value = false;
  }
};

const cancel = async (o: CourseOrder) => {
  await showConfirmDialog({ title: '取消订单', message: '确认取消该待支付订单?' });
  await cancelCourseOrder(o.id);
  showSuccessToast('已取消');
  load();
};

const refundReason = ref('');
const refundTarget = ref<CourseOrder | null>(null);

const submitRefund = async () => {
  if (!refundTarget.value) return;
  await requestCourseRefund(refundTarget.value.id, refundReason.value.trim() || undefined);
  showSuccessToast('退款申请已提交,等待商家审核');
  refundTarget.value = null;
  refundReason.value = '';
  load();
};

// ---------- 核销码二维码 ----------
const codeTarget = ref<CourseOrder | null>(null);
const qrDataUrl = ref('');

watch(codeTarget, async (o) => {
  qrDataUrl.value = '';
  if (o?.checkinCode) {
    qrDataUrl.value = await QRCode.toDataURL(o.checkinCode, { margin: 1, width: 240 });
  }
});

onMounted(load);
</script>

<template>
  <main class="my-orders-page">
    <PenTopBar title="我的课程订单" :show-share="false" />

    <nav class="chips">
      <button
        v-for="f in filters"
        :key="f.key"
        :class="{ active: filter === f.key }"
        @click="filter = f.key"
      >
        {{ f.label }}
      </button>
    </nav>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>
      <EmptyState v-else-if="!list.length" title="暂无课程订单" desc="去课程详情页报名正式课" />

      <article v-for="o in list" :key="o.id" class="card">
        <div class="head">
          <h3>课程 #{{ o.courseId }} · 场次 #{{ o.courseScheduleId }}</h3>
          <span class="badge" :class="statusMeta[o.orderStatus]?.cls">
            {{ statusMeta[o.orderStatus]?.label ?? o.orderStatus }}
          </span>
        </div>
        <p class="meta">
          <span>{{ o.orderNo }}</span>
          <span>¥{{ o.amountPayable }}</span>
          <span>下单 {{ fmt(o.createdAt) }}</span>
        </p>
        <div class="actions">
          <template v-if="o.orderStatus === 'pending_payment'">
            <button class="primary" :disabled="acting" @click="pay(o)">模拟支付</button>
            <button @click="cancel(o)">取消订单</button>
          </template>
          <template v-else-if="o.orderStatus === 'paid'">
            <button class="primary" @click="codeTarget = o">出示核销码</button>
            <button class="danger" @click="refundTarget = o">申请退款</button>
          </template>
          <button
            v-else-if="o.orderStatus === 'refund_rejected'"
            class="danger"
            @click="refundTarget = o"
          >
            再次申请退款
          </button>
        </div>
      </article>
    </section>

    <!-- 核销码弹层 -->
    <van-popup :show="Boolean(codeTarget)" position="bottom" round @update:show="codeTarget = null">
      <div class="sheet code-sheet">
        <h2>到店核销码</h2>
        <img v-if="qrDataUrl" :src="qrDataUrl" alt="核销二维码" class="qr" />
        <strong class="code-text">{{ codeTarget?.checkinCode }}</strong>
        <p class="code-hint">向商家出示二维码或报出 8 位数字码,核销后立即失效</p>
      </div>
    </van-popup>

    <!-- 退款弹层 -->
    <van-popup :show="Boolean(refundTarget)" position="bottom" round @update:show="refundTarget = null">
      <div class="sheet">
        <h2>申请退款</h2>
        <p class="refund-info">订单 {{ refundTarget?.orderNo }} · ¥{{ refundTarget?.amountPayable }}</p>
        <textarea v-model="refundReason" maxlength="2000" placeholder="退款原因(选填)" />
        <button class="sheet-submit" @click="submitRefund">提交退款申请</button>
      </div>
    </van-popup>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.my-orders-page {
  @include ops-page;
}
.chips {
  @include ops-chip-row;
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

.sheet {
  padding: 24px 20px calc(24px + env(safe-area-inset-bottom));
  h2 {
    margin: 0 0 14px;
    font-size: 20px;
    font-weight: 900;
  }
  textarea {
    width: 100%;
    min-height: 88px;
    border: 1px solid $pen-hairline;
    border-radius: 16px;
    background: $pen-soft;
    padding: 12px 14px;
    font-size: 14px;
    font-family: inherit;
    outline: none;
    box-sizing: border-box;
    resize: vertical;
    &:focus {
      border-color: $pen-ink;
      background: $pen-canvas;
    }
  }
  .sheet-submit {
    @include pen-primary-btn;
    width: 100%;
    margin-top: 14px;
  }
}

.code-sheet {
  text-align: center;
  .qr {
    width: 200px;
    height: 200px;
    margin: 6px auto 4px;
    display: block;
  }
  .code-text {
    display: block;
    font-size: 30px;
    font-weight: 900;
    letter-spacing: 0.25em;
    margin-top: 6px;
  }
  .code-hint {
    margin: 10px 0 0;
    color: $pen-mute;
    font-size: 12px;
  }
}

.refund-info {
  margin: 0 0 12px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
}
</style>
