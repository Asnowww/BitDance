<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import {
  fetchMyWorkshopOrders,
  cancelWorkshopOrder,
  refundWorkshopOrder,
  type WorkshopOrder,
  type OrderStatus
} from '@/api/workshop';

const router = useRouter();
const list = ref<WorkshopOrder[]>([]);
const loading = ref(true);

const STATUS_LABEL: Record<OrderStatus, string> = {
  UNPAID: '待支付',
  PAID: '已支付',
  CHECKED_IN: '已签到',
  COMPLETED: '已完成',
  CANCELED: '已取消',
  REFUNDED: '已退款'
};

const reload = async () => {
  loading.value = true;
  try {
    list.value = await fetchMyWorkshopOrders();
  } finally {
    loading.value = false;
  }
};

const onCancel = async (it: WorkshopOrder) => {
  await showConfirmDialog({ title: '取消订单？', message: '未支付订单可直接取消' }).catch(() => {
    throw new Error('cancel');
  });
  await cancelWorkshopOrder(it.id);
  showSuccessToast('已取消');
  void reload();
};

const onRefund = async (it: WorkshopOrder) => {
  await showConfirmDialog({ title: '申请退款？', message: 'mock 阶段退款立即生效' }).catch(() => {
    throw new Error('cancel');
  });
  await refundWorkshopOrder(it.id);
  showSuccessToast('已退款');
  void reload();
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">我的 Workshop 订单</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">还没有报名 Workshop</div>
    <article v-for="it in list" :key="it.id" class="item">
      <div class="item__head">
        <span class="item__title" @click="router.push(`/workshop/${it.workshopId}`)">{{ it.workshopTitle }}</span>
        <span class="status" :data-s="it.status">{{ STATUS_LABEL[it.status] }}</span>
      </div>
      <div class="item__meta">{{ it.sessionDate }} {{ it.sessionTime }} · ¥{{ it.amount }}</div>
      <div v-if="it.checkinCode" class="item__code">签到码：{{ it.checkinCode }}</div>
      <footer class="item__foot">
        <button v-if="it.status === 'UNPAID'" class="btn-ghost" @click="onCancel(it)">取消</button>
        <button v-if="it.status === 'PAID'" class="btn-ghost" @click="onRefund(it)">申请退款</button>
        <button v-if="it.status === 'PAID'" class="btn-primary" @click="router.push(`/workshop-checkin/${it.id}`)">
          扫码签到
        </button>
      </footer>
    </article>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 24px;
}
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.empty {
  padding: 60px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.item {
  margin: 8px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
  }
  &__meta {
    margin-top: 6px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__code {
    margin-top: 8px;
    padding: 6px 10px;
    background: rgba(255, 170, 51, 0.1);
    color: #c87a00;
    border-radius: 6px;
    font-size: 12px;
    font-family: monospace;
  }
  &__foot {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
.status {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 170, 51, 0.15);
  color: #c87a00;
  &[data-s='PAID'] {
    background: rgba(54, 165, 255, 0.12);
    color: #36a5ff;
  }
  &[data-s='CHECKED_IN'],
  &[data-s='COMPLETED'] {
    background: rgba(0, 168, 84, 0.12);
    color: #00a854;
  }
  &[data-s='CANCELED'],
  &[data-s='REFUNDED'] {
    background: #f3f3f3;
    color: var(--bd-text-secondary);
  }
}
.btn-ghost {
  border: 1px solid var(--bd-border);
  background: #fff;
  color: var(--bd-text-secondary);
  border-radius: 999px;
  padding: 5px 14px;
  font-size: 12px;
  cursor: pointer;
}
.btn-primary {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 5px 14px;
  font-size: 12px;
  cursor: pointer;
}
</style>
