<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchCoachDashboard, type CoachDashboard } from '@/api/coachOps';

const router = useRouter();
const data = ref<CoachDashboard | null>(null);

onMounted(async () => {
  data.value = await fetchCoachDashboard();
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">经营数据看板</span>
    </header>
    <section v-if="data" class="grid">
      <div class="card"><div class="card__num">{{ data.monthSessions }}</div><div class="card__label">本月授课次数</div></div>
      <div class="card"><div class="card__num">{{ data.monthStudents }}</div><div class="card__label">本月学员数</div></div>
      <div class="card"><div class="card__num">¥{{ data.monthIncome }}</div><div class="card__label">本月收益</div></div>
      <div class="card"><div class="card__num">{{ data.pendingReplies }}</div><div class="card__label">待回复评价</div></div>
      <div class="card card--wide">
        <div class="card__num">{{ data.ratingAvg }} <span class="card__sub">/ 5（{{ data.ratingCount }} 条）</span></div>
        <div class="card__label">学员评分</div>
      </div>
      <div class="card card--wide">
        <div class="card__num">{{ data.conversionRate }}%</div>
        <div class="card__label">下单转化率</div>
      </div>
    </section>
    <section class="actions">
      <button class="row" @click="router.push('/coach/orders')">学员订单与核销 →</button>
      <button class="row" @click="router.push('/coach/replies')">评价回复 →</button>
      <button class="row" @click="router.push('/coach/workshop-create')">创建 Workshop →</button>
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
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  padding: 12px;
}
.card {
  background: #fff;
  padding: 14px;
  border-radius: 12px;
  &--wide {
    grid-column: span 2;
  }
  &__num {
    font-size: 22px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__sub {
    font-size: 12px;
    color: var(--bd-text-secondary);
    font-weight: 400;
  }
  &__label {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.actions {
  margin-top: 8px;
  background: #fff;
}
.row {
  width: 100%;
  border: none;
  background: none;
  padding: 14px 16px;
  text-align: left;
  font-size: 14px;
  border-bottom: 1px solid var(--bd-border);
  cursor: pointer;
  &:last-child {
    border-bottom: none;
  }
}
</style>
