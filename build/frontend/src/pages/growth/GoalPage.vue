<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { fetchGrowthGoal, saveGrowthGoal, type GrowthGoal } from '@/api/growth';

const router = useRouter();

const period = ref<'week' | 'month'>('week');
const targetSessions = ref(3);
const targetMinutes = ref(180);

const computeRange = () => {
  const today = new Date();
  const start = new Date(today);
  const end = new Date(today);
  if (period.value === 'week') {
    const d = (today.getDay() + 6) % 7;
    start.setDate(today.getDate() - d);
    end.setDate(start.getDate() + 6);
  } else {
    start.setDate(1);
    end.setMonth(start.getMonth() + 1);
    end.setDate(0);
  }
  return { startDate: start.toISOString().slice(0, 10), endDate: end.toISOString().slice(0, 10) };
};

const onSave = async () => {
  const { startDate, endDate } = computeRange();
  const goal: GrowthGoal = {
    period: period.value,
    targetSessions: Number(targetSessions.value),
    targetMinutes: Number(targetMinutes.value),
    startDate,
    endDate
  };
  await saveGrowthGoal(goal);
  showSuccessToast('已保存');
  router.back();
};

onMounted(async () => {
  const g = await fetchGrowthGoal();
  if (g) {
    period.value = g.period;
    targetSessions.value = g.targetSessions;
    targetMinutes.value = g.targetMinutes;
  }
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">训练目标</span>
    </header>
    <section class="form">
      <div class="group">
        <div class="group__title">周期</div>
        <div class="chips">
          <span class="chip" :class="{ active: period === 'week' }" @click="period = 'week'">每周</span>
          <span class="chip" :class="{ active: period === 'month' }" @click="period = 'month'">每月</span>
        </div>
      </div>
      <div class="row">
        <span>训练次数</span>
        <input v-model.number="targetSessions" type="number" min="1" max="50" class="input" />
      </div>
      <div class="row">
        <span>训练分钟</span>
        <input v-model.number="targetMinutes" type="number" min="30" max="3000" class="input" />
      </div>
      <p class="tip">目标设定后会显示在成长档案首屏的进度条中。</p>
    </section>
    <footer class="footer">
      <button class="btn" @click="onSave">保存目标</button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
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
.form {
  background: #fff;
  padding: 16px;
}
.group {
  padding: 10px 0;
  &__title {
    font-size: 13px;
    margin-bottom: 8px;
    color: var(--bd-text-secondary);
  }
}
.chips {
  display: flex;
  gap: 8px;
}
.chip {
  padding: 6px 14px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  span {
    width: 84px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
}
.input {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fafafa;
  font-size: 14px;
  outline: none;
}
.tip {
  margin-top: 12px;
  font-size: 11px;
  color: var(--bd-text-secondary);
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
}
.btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  background: var(--bd-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}
</style>
