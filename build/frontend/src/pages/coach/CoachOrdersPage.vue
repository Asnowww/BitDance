<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast, showDialog } from 'vant';
import { fetchCoachOrders, checkinByCoach, type CoachWorkshopOrderRow } from '@/api/coachOps';

const router = useRouter();
const list = ref<CoachWorkshopOrderRow[]>([]);
const loading = ref(true);

const reload = async () => {
  loading.value = true;
  try {
    list.value = await fetchCoachOrders();
  } finally {
    loading.value = false;
  }
};

const onCheckin = async (it: CoachWorkshopOrderRow) => {
  const result = await showDialog({
    title: '核销签到',
    message: `请输入学员出示的签到码（演示：${it.checkinCode}）`,
    showCancelButton: true
  }).catch(() => null);
  if (!result) return;
  const r = await checkinByCoach(it.orderId, it.checkinCode);
  if (r.ok) {
    showSuccessToast('签到成功');
    void reload();
  } else {
    showFailToast('签到失败：状态或签到码不匹配');
  }
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">学员订单与核销</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">还没有学员订单</div>
    <article v-for="it in list" :key="it.orderId" class="item">
      <div class="item__head">
        <span>{{ it.workshopTitle }}</span>
        <span class="status">{{ it.status }}</span>
      </div>
      <div class="item__body">
        <div>{{ it.buyerName }} · {{ it.sessionDate }} {{ it.sessionTime }} · ¥{{ it.amount }}</div>
        <div class="item__code">签到码 {{ it.checkinCode || '—' }}</div>
      </div>
      <button v-if="it.status === 'PAID'" class="btn" @click="onCheckin(it)">核销签到</button>
    </article>
  </div>
</template>

<style lang="scss" scoped>
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
    font-weight: 600;
    font-size: 14px;
  }
  &__body {
    margin-top: 8px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__code {
    margin-top: 4px;
    font-family: monospace;
  }
}
.status {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(54, 165, 255, 0.12);
  color: #36a5ff;
}
.btn {
  margin-top: 10px;
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12px;
  cursor: pointer;
}
</style>
