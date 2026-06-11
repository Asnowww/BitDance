<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { CalendarClock, QrCode, Ticket } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  cancelWorkshopOrder,
  fetchMyWorkshopOrders,
  refundWorkshopOrder,
  type WorkshopOrder
} from '@/api/workshop';

type FilterKey = 'all' | 'UNPAID' | 'PAID' | 'CHECKED_IN' | 'REFUNDED' | 'CANCELED';

const router = useRouter();
const active = ref<FilterKey>('all');
const loading = ref(false);
const orders = ref<WorkshopOrder[]>([]);

const filters: Array<{ key: FilterKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'UNPAID', label: '待支付' },
  { key: 'PAID', label: '待签到' },
  { key: 'CHECKED_IN', label: '已签到' },
  { key: 'REFUNDED', label: '已退款' },
  { key: 'CANCELED', label: '已取消' }
];

const filtered = computed(() =>
  orders.value.filter((order) => active.value === 'all' || order.status === active.value)
);

const load = async () => {
  loading.value = true;
  try {
    orders.value = await fetchMyWorkshopOrders();
  } finally {
    loading.value = false;
  }
};

const actionLabel = (order: WorkshopOrder) => {
  if (order.status === 'UNPAID') return '去支付';
  if (order.status === 'PAID') return '签到页';
  if (order.status === 'CHECKED_IN' || order.status === 'COMPLETED') return '写评价';
  return '查看活动';
};

const openPrimary = (order: WorkshopOrder) => {
  if (order.status === 'UNPAID') {
    router.push({ path: `/workshop/${order.workshopId}/pay`, query: { sessionId: String(order.sessionId) } });
    return;
  }
  if (order.status === 'PAID') {
    router.push(`/workshop-checkin/${order.id}`);
    return;
  }
  if (order.status === 'CHECKED_IN' || order.status === 'COMPLETED') {
    router.push({
      path: '/publish/review',
      query: {
        targetType: 'workshop',
        targetId: order.workshopId,
        targetName: order.workshopTitle,
        sourceType: order.status === 'CHECKED_IN' ? 'checkin' : 'order',
        sourceRefId: order.id
      }
    });
    return;
  }
  router.push(`/workshop/${order.workshopId}`);
};

const openSecondary = async (order: WorkshopOrder) => {
  if (order.status === 'UNPAID') {
    await cancelWorkshopOrder(order.id);
    showToast('订单已取消');
    await load();
    return;
  }
  if (order.status === 'PAID') {
    await refundWorkshopOrder(order.id);
    showToast('退款已提交');
    await load();
  }
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的 Workshop" :show-share="false" />

    <section class="pen-scroll">
      <div class="entry">
        <button class="entry__btn" type="button" @click="router.push('/me/workshop-calendar')">
          <CalendarClock :size="18" :stroke-width="2" />
          <span>活动日历</span>
        </button>
      </div>

      <div class="chip-row">
        <button
          v-for="filter in filters"
          :key="filter.key"
          class="chip"
          :class="active === filter.key ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="active = filter.key"
        >
          {{ filter.label }}
        </button>
      </div>

      <p v-if="loading" class="empty">订单加载中</p>
      <p v-else-if="filtered.length === 0" class="empty">暂无对应订单</p>

      <article v-for="order in filtered" :key="order.id" class="order-card">
        <div class="order-card__cover"><Ticket :size="24" :stroke-width="2" /></div>
        <div class="order-card__copy">
          <strong>{{ order.workshopTitle }}</strong>
          <span>{{ order.sessionDate }} {{ order.sessionTime }} · ¥{{ order.amount }}</span>
          <em>{{ order.status }}</em>
        </div>
        <div class="order-card__actions">
          <button
            v-if="order.status === 'PAID'"
            class="icon-btn"
            type="button"
            aria-label="签到页"
            @click="router.push(`/workshop-checkin/${order.id}`)"
          >
            <QrCode :size="16" :stroke-width="2" />
          </button>
          <button
            v-if="['UNPAID', 'PAID'].includes(order.status)"
            class="ghost-btn"
            type="button"
            @click="openSecondary(order)"
          >
            {{ order.status === 'UNPAID' ? '取消' : '退款' }}
          </button>
          <button class="primary-btn" type="button" @click="openPrimary(order)">
            {{ actionLabel(order) }}
          </button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.entry__btn,
.order-card,
.icon-btn,
.ghost-btn,
.primary-btn {
  border: 0;
}

.entry__btn {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  background: $pen-soft;
  color: $pen-ink;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip { @include pen-chip; }

.empty {
  margin: 8px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.order-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__cover {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: $pen-ink;
    color: $pen-on-primary;
    display: grid;
    place-items: center;
    flex: none;
  }

  &__copy {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span, em {
    color: $pen-mute;
    font-size: 12px;
    font-style: normal;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}

.icon-btn,
.ghost-btn,
.primary-btn {
  cursor: pointer;
}

.icon-btn {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  display: grid;
  place-items: center;
}

.ghost-btn,
.primary-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
}

.ghost-btn {
  background: $pen-canvas;
  color: $pen-ink;
}

.primary-btn {
  background: $pen-ink;
  color: $pen-on-primary;
}
</style>
