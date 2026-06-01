<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { QrCode, Ticket } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  fetchMyWorkshopOrders,
  payWorkshopOrder,
  refundWorkshopOrder,
  type OrderStatus,
  type WorkshopOrder
} from '@/api/workshop';

type FilterKey = 'all' | 'pending_payment' | 'paid' | 'completed' | 'closed';

interface FilterItem {
  key: FilterKey;
  label: string;
}

interface OrderRecord {
  id: number;
  workshopId: number;
  sourceId?: number;
  checkinCode?: string;
  title: string;
  meta: string;
  status: string;
  tone: 'ink' | 'success' | 'mute' | 'danger';
  action: string;
  primary: boolean;
  group: FilterKey;
  reviewQuery?: Record<string, string | number>;
}

const router = useRouter();
const filters: FilterItem[] = [
  { key: 'all', label: '全部' },
  { key: 'pending_payment', label: '待支付' },
  { key: 'paid', label: '已报名' },
  { key: 'completed', label: '已完成' },
  { key: 'closed', label: '退款取消' }
];
const activeFilter = ref<FilterKey>('all');
const orders = ref<WorkshopOrder[]>([]);
const loading = ref(false);

const normalizeStatus = (order: WorkshopOrder): OrderStatus =>
  ((order.orderStatus ?? order.status ?? 'pending_payment') as OrderStatus);

const statusGroup = (status: OrderStatus): FilterKey => {
  if (status === 'UNPAID' || status === 'pending_payment') return 'pending_payment';
  if (status === 'PAID' || status === 'CHECKED_IN' || status === 'paid') return 'paid';
  if (status === 'COMPLETED' || status === 'completed') return 'completed';
  return 'closed';
};

const statusLabel = (status: OrderStatus, amount: number) => {
  const price = amount ? ` ¥${amount}` : '';
  if (status === 'UNPAID' || status === 'pending_payment') return `待支付${price}`;
  if (status === 'PAID' || status === 'paid') return '已报名 · 待签到核销';
  if (status === 'CHECKED_IN') return '已签到 · 待完成';
  if (status === 'COMPLETED' || status === 'completed') return '已完成 · 可评价';
  if (status === 'REFUNDED' || status === 'refunded') return '已退款';
  return '已取消';
};

const toneOf = (group: FilterKey): OrderRecord['tone'] => {
  if (group === 'pending_payment') return 'ink';
  if (group === 'paid') return 'success';
  if (group === 'completed') return 'mute';
  return 'danger';
};

const actionOf = (group: FilterKey) => {
  if (group === 'pending_payment') return '去支付';
  if (group === 'paid') return '二维码';
  if (group === 'completed') return '写评价';
  return '查看';
};

const formatDate = (value?: string | number) => {
  if (!value) return '';
  const date = typeof value === 'number' ? new Date(value) : new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(
    date.getMinutes()
  ).padStart(2, '0')}`;
};

const amountOf = (order: WorkshopOrder) =>
  Number(order.amountPayable ?? order.amount ?? order.amountPaid ?? 0);

const toRecord = (order: WorkshopOrder): OrderRecord => {
  const status = normalizeStatus(order);
  const group = statusGroup(status);
  const amount = amountOf(order);
  const sessionId = order.workshopSessionId ?? order.sessionId;
  const title = order.workshopTitle ?? `Workshop #${order.workshopId}`;
  const sessionText =
    order.sessionDate || order.sessionTime
      ? `${order.sessionDate ?? ''} ${order.sessionTime ?? ''}`.trim()
      : `订单 ${order.orderNo ?? order.id} · ${formatDate(order.createdAt)}`;

  return {
    id: order.id,
    workshopId: order.workshopId,
    sourceId: sessionId ?? order.id,
    checkinCode: order.checkinCode,
    title,
    meta: sessionText,
    status: statusLabel(status, amount),
    tone: toneOf(group),
    action: actionOf(group),
    primary: group === 'pending_payment',
    group,
    reviewQuery: {
      targetType: 'course',
      targetId: order.workshopId,
      targetName: title,
      sourceType: 'order',
      sourceRefId: order.id
    }
  };
};

const records = computed(() =>
  orders.value
    .map(toRecord)
    .filter((record) => activeFilter.value === 'all' || record.group === activeFilter.value)
);

const loadOrders = async () => {
  loading.value = true;
  try {
    orders.value = await fetchMyWorkshopOrders();
  } finally {
    loading.value = false;
  }
};

const handleAction = async (record: OrderRecord) => {
  if (record.group === 'pending_payment') {
    await payWorkshopOrder(record.id);
    await loadOrders();
    return;
  }
  if (record.group === 'paid') {
    showToast(record.checkinCode ? `签到码：${record.checkinCode}` : '签到码待生成');
    return;
  }
  if (record.group === 'completed') {
    router.push({ path: '/publish/review', query: record.reviewQuery });
    return;
  }
  showToast(record.status);
};

const requestRefund = async (record: OrderRecord) => {
  if (record.group !== 'paid') return;
  await refundWorkshopOrder(record.id);
  await loadOrders();
};

onMounted(loadOrders);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的订单" :show-share="false" />

    <section class="pen-scroll">
      <div class="chip-row" aria-label="订单筛选">
        <button
          v-for="filter in filters"
          :key="filter.key"
          class="chip"
          :class="activeFilter === filter.key ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeFilter = filter.key"
        >
          {{ filter.label }}
        </button>
      </div>

      <p v-if="loading" class="empty">订单加载中</p>
      <p v-else-if="records.length === 0" class="empty">暂无对应订单</p>

      <article v-for="record in records" :key="record.id" class="rec">
        <div class="rec__cover" aria-hidden="true">
          <Ticket :size="26" :stroke-width="2" />
        </div>
        <div class="rec__body">
          <strong class="rec__title">{{ record.title }}</strong>
          <p class="rec__meta">{{ record.meta }}</p>
          <div class="rec__foot">
            <span class="rec__status" :class="`rec__status--${record.tone}`">
              {{ record.status }}
            </span>
            <div class="rec__actions">
              <button
                v-if="record.group === 'paid'"
                class="rec__icon"
                type="button"
                aria-label="查看签到二维码"
                @click="handleAction(record)"
              >
                <QrCode :size="17" :stroke-width="2" />
              </button>
              <button
                v-if="record.group === 'paid'"
                class="rec__btn"
                type="button"
                @click="requestRefund(record)"
              >
                退款
              </button>
              <button
                class="rec__btn"
                :class="{ 'rec__btn--solid': record.primary }"
                type="button"
                @click="handleAction(record)"
              >
                {{ record.action }}
              </button>
            </div>
          </div>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.empty {
  margin: 20px 0;
  color: $pen-mute;
  font-size: 14px;
  font-weight: 700;
  line-height: $pen-lh;
  text-align: center;
}

.rec {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__cover {
    flex: none;
    display: grid;
    width: 88px;
    height: 88px;
    border-radius: 12px;
    background: $pen-soft;
    color: $pen-ink;
    place-items: center;
  }

  &__body {
    min-width: 0;
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 6px;
  }

  &__title {
    overflow: hidden;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__status {
    min-width: 0;
    flex: 1;
    font-size: 13px;
    font-weight: 800;
    line-height: $pen-lh;

    &--ink {
      color: $pen-ink;
    }

    &--success {
      color: $pen-success;
    }

    &--mute {
      color: $pen-mute;
    }

    &--danger {
      color: $pen-mute;
    }
  }

  &__actions {
    flex: none;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__btn,
  &__icon {
    height: 34px;
    border: 1px solid $pen-ink;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;
  }

  &__btn {
    padding: 6px 12px;

    &--solid {
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }

  &__icon {
    display: grid;
    width: 34px;
    padding: 0;
    place-items: center;
  }
}
</style>
