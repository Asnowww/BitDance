<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import {
  fetchScheduleBookings,
  checkinCourseOrder,
  type CourseOrder
} from '@/api/coachOps';

const route = useRoute();
const scheduleId = Number(route.params.id);
const orders = ref<CourseOrder[]>([]);
const loading = ref(true);

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending_payment: { label: '待支付', cls: 'warn' },
  paid: { label: '已支付', cls: 'ok' },
  refund_requested: { label: '退款审核中', cls: 'warn' },
  refunded: { label: '已退款', cls: 'bad' },
  refund_rejected: { label: '退款被拒', cls: '' },
  checked_in: { label: '已核销', cls: 'ink' },
  completed: { label: '已完成', cls: '' },
  canceled: { label: '已取消', cls: '' }
};

const stats = computed(() => {
  const total = orders.value.length;
  const paid = orders.value.filter((o) => o.orderStatus === 'paid').length;
  const checked = orders.value.filter((o) =>
    ['checked_in', 'completed'].includes(o.orderStatus)
  ).length;
  return { total, paid, checked };
});

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  loading.value = true;
  try {
    orders.value = await fetchScheduleBookings(scheduleId);
  } finally {
    loading.value = false;
  }
};

const checkin = async (o: CourseOrder) => {
  await showConfirmDialog({
    title: '核销确认',
    message: `确认核销订单 ${o.orderNo}?核销后不可撤销。`
  });
  await checkinCourseOrder(o.id, o.checkinCode ?? '');
  showSuccessToast('核销成功');
  load();
};

onMounted(load);
</script>

<template>
  <main class="bookings-page">
    <PenTopBar title="预约名单" :show-share="false" />

    <section class="stats">
      <article><strong>{{ stats.total }}</strong><span>总订单</span></article>
      <article><strong>{{ stats.paid }}</strong><span>已支付待核销</span></article>
      <article><strong>{{ stats.checked }}</strong><span>已核销</span></article>
    </section>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>
      <EmptyState v-else-if="!orders.length" title="暂无预约" desc="该场次还没有学员下单" />

      <article v-for="o in orders" :key="o.id" class="card">
        <div class="head">
          <h3>学员 #{{ o.userId }}</h3>
          <span class="badge" :class="statusMeta[o.orderStatus]?.cls">
            {{ statusMeta[o.orderStatus]?.label ?? o.orderStatus }}
          </span>
        </div>
        <p class="meta">
          <span>单号 {{ o.orderNo }}</span>
          <span>¥{{ o.amountPayable }}</span>
          <span v-if="o.paidAt">支付 {{ fmt(o.paidAt) }}</span>
        </p>
        <div v-if="o.orderStatus === 'paid'" class="actions">
          <button class="primary" @click="checkin(o)">到课核销</button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.bookings-page {
  @include ops-page;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  margin: 14px 18px 0;
  border-radius: 22px;
  overflow: hidden;
  background: $pen-hairline;
  article {
    background: $pen-soft;
    padding: 14px 8px;
    text-align: center;
    strong {
      display: block;
      font-size: 20px;
      font-weight: 900;
    }
    span {
      margin-top: 4px;
      display: block;
      color: $pen-mute;
      font-size: 11px;
      font-weight: 800;
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
</style>
