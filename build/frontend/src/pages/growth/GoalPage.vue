<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { showToast } from 'vant';
import { CircleCheckBig } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchGrowthGoal, saveGrowthGoal, type GrowthGoal } from '@/api/growth';

const period = ref<'week' | 'month'>('week');
const goal = ref<GrowthGoal | null>(null);
const targetTimes = ref(5);
const targetMinutes = ref(300);
const loading = ref(false);

const range = computed(() => {
  const now = new Date();
  if (period.value === 'week') {
    const day = (now.getDay() + 6) % 7;
    const start = new Date(now);
    start.setDate(now.getDate() - day);
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    return { start, end };
  }
  return { start: new Date(now.getFullYear(), now.getMonth(), 1), end: new Date(now.getFullYear(), now.getMonth() + 1, 0) };
});

const isoDate = (d: Date) => d.toISOString().slice(0, 10);

const currentTimes = computed(() => goal.value?.currentTimes ?? 0);
const currentMinutes = computed(() => goal.value?.currentMinutes ?? 0);
const progress = computed(() => {
  const byTimes = targetTimes.value ? currentTimes.value / targetTimes.value : 0;
  const byMinutes = targetMinutes.value ? currentMinutes.value / targetMinutes.value : 0;
  return Math.min(100, Math.round(Math.max(byTimes, byMinutes) * 100));
});

const periodLabel = computed(() => period.value === 'week' ? '本周' : '本月');
const isDone = computed(() => progress.value >= 100 || goal.value?.goalStatus === 'completed');

const loadGoal = async () => {
  loading.value = true;
  try {
    goal.value = await fetchGrowthGoal();
    if (goal.value) {
      period.value = goal.value.goalPeriod === 'monthly' ? 'month' : 'week';
      targetTimes.value = goal.value.targetTimes ?? goal.value.targetSessions ?? 5;
      targetMinutes.value = goal.value.targetMinutes ?? 300;
    }
  } finally {
    loading.value = false;
  }
};

const saveGoal = async () => {
  const next = await saveGrowthGoal({
    goalPeriod: period.value === 'week' ? 'weekly' : 'monthly',
    targetTimes: Number(targetTimes.value),
    targetMinutes: Number(targetMinutes.value),
    startDate: isoDate(range.value.start),
    endDate: isoDate(range.value.end)
  } as GrowthGoal);
  goal.value = next;
  showToast('训练目标已保存');
};

onMounted(loadGoal);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="训练目标" :show-share="false" />

    <section class="pen-scroll">
      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': period === 'week' }" type="button" @click="period = 'week'">本周</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': period === 'month' }" type="button" @click="period = 'month'">本月</button>
      </div>

      <section class="goal">
        <span class="goal__label">{{ periodLabel }}训练目标</span>
        <strong class="goal__value">{{ currentTimes }} / {{ targetTimes }} 次</strong>
        <div class="goal__track"><span class="goal__fill" :style="{ width: `${progress}%` }" /></div>
        <span class="goal__hint">{{ currentMinutes }} / {{ targetMinutes }} 分钟 · {{ isDone ? '已达成' : `进度 ${progress}%` }}</span>
      </section>

      <section class="editor">
        <label>
          <span>目标次数</span>
          <input v-model.number="targetTimes" type="number" min="1" max="500" />
        </label>
        <label>
          <span>目标分钟</span>
          <input v-model.number="targetMinutes" type="number" min="1" max="10000" />
        </label>
      </section>

      <h2 class="block-title">里程碑</h2>
      <div class="mile">
        <span class="mile__label">目标达成</span>
        <span v-if="isDone" class="mile__done"><CircleCheckBig :size="18" :stroke-width="2" /> 已达成</span>
        <span v-else class="mile__value">{{ progress }}%</span>
      </div>
      <div class="mile">
        <span class="mile__label">目标周期</span>
        <span class="mile__value">{{ isoDate(range.start) }} - {{ isoDate(range.end) }}</span>
      </div>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" :disabled="loading" @click="saveGoal">
        {{ loading ? '加载中' : '保存目标' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }
.seg { display: flex; gap: 8px; }
.seg__btn {
  flex: 1;
  height: 48px;
  border: 0;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-ink;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
.seg__btn--on { background: $pen-ink; color: $pen-on-primary; }
.goal {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-ink;
  color: $pen-on-primary;
}
.goal__label, .goal__hint { color: $pen-subtle-text; font-size: 13px; font-weight: 700; }
.goal__value { font-size: 40px; font-weight: 900; line-height: $pen-lh; }
.goal__track { height: 10px; border-radius: 999px; background: $pen-charcoal; overflow: hidden; }
.goal__fill { display: block; height: 100%; border-radius: 999px; background: $pen-on-primary; }
.editor { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.editor label { display: flex; flex-direction: column; gap: 8px; padding: 14px; border-radius: 16px; background: $pen-soft; }
.editor span { color: $pen-mute; font-size: 12px; font-weight: 800; }
.editor input { width: 100%; border: 0; background: transparent; color: $pen-ink; font-size: 26px; font-weight: 900; outline: none; }
.block-title { @include pen-h3-section; }
.mile { display: flex; align-items: center; gap: 10px; padding: 14px 0; border-bottom: 1px solid $pen-hairline; }
.mile__label { flex: 1; font-size: 15px; font-weight: 800; }
.mile__value { color: $pen-mute; font-size: 13px; font-weight: 700; }
.mile__done { display: inline-flex; align-items: center; gap: 4px; color: $pen-success; font-size: 14px; font-weight: 800; }
.save-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;
}
.save-bar__btn { width: 100%; height: 48px; border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary; font-size: 15px; font-weight: 800; }
</style>
