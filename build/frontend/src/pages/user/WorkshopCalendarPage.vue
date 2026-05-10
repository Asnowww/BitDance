<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchMyWorkshopOrders, type WorkshopOrder } from '@/api/workshop';

const router = useRouter();
const list = ref<WorkshopOrder[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    list.value = await fetchMyWorkshopOrders();
  } finally {
    loading.value = false;
  }
});

const grouped = computed(() => {
  const m = new Map<string, WorkshopOrder[]>();
  list.value
    .filter((it) => it.status === 'PAID' || it.status === 'CHECKED_IN')
    .sort((a, b) => a.sessionDate.localeCompare(b.sessionDate))
    .forEach((it) => {
      const arr = m.get(it.sessionDate) ?? [];
      arr.push(it);
      m.set(it.sessionDate, arr);
    });
  return Array.from(m.entries());
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">活动日历</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!grouped.length" class="empty">还没有已支付的活动</div>
    <section v-for="[date, items] in grouped" :key="date" class="day">
      <h3 class="day__title">{{ date }}</h3>
      <article v-for="it in items" :key="it.id" class="item" @click="router.push(`/workshop/${it.workshopId}`)">
        <div class="item__time">{{ it.sessionTime }}</div>
        <div class="item__body">
          <div class="item__title">{{ it.workshopTitle }}</div>
          <div class="item__sub">¥{{ it.amount }} · 签到码 {{ it.checkinCode }}</div>
        </div>
      </article>
    </section>
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
.day {
  padding: 12px;
  &__title {
    margin: 0 0 8px;
    font-size: 14px;
    color: var(--bd-primary);
  }
}
.item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  &__time {
    width: 96px;
    font-size: 13px;
    color: var(--bd-primary);
    font-weight: 600;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
  }
  &__sub {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
</style>
