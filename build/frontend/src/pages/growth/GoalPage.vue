<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { showSuccessToast } from 'vant';
import { CircleCheckBig } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchGrowthGoal, saveGrowthGoal, type GrowthGoal } from '@/api/growth';

const period = ref<'week' | 'month'>('week');
const goal = ref<GrowthGoal | null>(null);
const targetTimes = ref(5);
const targetMinutes = ref(300);
const loading = ref(false);
const saving = ref(false);

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
  return {
    start: new Date(now.getFullYear(), now.getMonth(), 1),
    end: new Date(now.getFullYear(), now.getMonth() + 1, 0)
  };
});

const isoDate = (date: Date) => date.toISOString().slice(0, 10);
const currentTimes = computed(() => goal.value?.currentTimes ?? 0);
const currentMinutes = computed(() => goal.value?.currentMinutes ?? 0);
const periodLabel = computed(() => period.value === 'week' ? '本周' : '本月');
const progress = computed(() => {
  const byTimes = targetTimes.value ? currentTimes.value / targetTimes.value : 0;
  const byMinutes = targetMinutes.value ? currentMinutes.value / targetMinutes.value : 0;
  return Math.min(100, Math.round(Math.max(byTimes, byMinutes) * 100));
});
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
  saving.value = true;
  try {
    goal.value = await saveGrowthGoal({
      goalPeriod: period.value === 'week' ? 'weekly' : 'monthly',
      targetTimes: Number(targetTimes.value),
      targetMinutes: Number(targetMinutes.value),
      startDate: isoDate(range.value.start),
      endDate: isoDate(range.value.end)
    } as GrowthGoal);
    showSuccessToast('训练目标已保存');
  } finally {
    saving.value = false;
  }
};

onMounted(loadGoal);
</script>

<template>
  <main class="goal-page">
    <PenTopBar title="训练目标" :show-share="false" />

    <section class="content">
      <div class="seg">
        <button :class="{ on: period === 'week' }" type="button" @click="period = 'week'">本周</button>
        <button :class="{ on: period === 'month' }" type="button" @click="period = 'month'">本月</button>
      </div>

      <section class="goal">
        <span>{{ periodLabel }}训练目标</span>
        <strong>{{ currentTimes }} / {{ targetTimes }} 次</strong>
        <div class="track"><i :style="{ width: `${progress}%` }" /></div>
        <p>{{ currentMinutes }} / {{ targetMinutes }} 分钟 · {{ isDone ? '已达成' : `进度 ${progress}%` }}</p>
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

      <section class="mile">
        <div>
          <span>目标状态</span>
          <strong v-if="isDone"><CircleCheckBig :size="18" /> 已达成</strong>
          <strong v-else>{{ progress }}%</strong>
        </div>
        <div>
          <span>周期范围</span>
          <strong>{{ isoDate(range.start) }} - {{ isoDate(range.end) }}</strong>
        </div>
      </section>
    </section>

    <footer class="save-bar">
      <button type="button" :disabled="loading || saving" @click="saveGoal">
        {{ loading ? '加载中...' : saving ? '保存中...' : '保存目标' }}
      </button>
    </footer>
  </main>
</template>

<style scoped lang="scss">
.goal-page { min-height: 100vh; max-width: 430px; margin: 0 auto; background: #fff; color: #111; padding-bottom: calc(86px + env(safe-area-inset-bottom)); }
.content { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }
.seg { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.seg button { height: 46px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; font-size: 14px; font-weight: 900; }
.seg .on { background: #111; color: #fff; }
.goal { display: flex; flex-direction: column; gap: 10px; padding: 18px; border-radius: 8px; background: #111; color: #fff; }
.goal span, .goal p { margin: 0; color: #b8b8bb; font-size: 13px; font-weight: 800; }
.goal strong { font-size: 42px; line-height: 1; font-weight: 950; }
.track { height: 10px; border-radius: 999px; background: #39393b; overflow: hidden; }
.track i { display: block; height: 100%; border-radius: inherit; background: #fff; }
.editor { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.editor label { display: flex; flex-direction: column; gap: 8px; padding: 14px; border-radius: 8px; background: #f5f5f5; }
.editor span, .mile span { color: #707072; font-size: 12px; font-weight: 900; }
.editor input { width: 100%; border: 0; background: transparent; color: #111; font-size: 26px; font-weight: 950; outline: none; }
.mile { display: flex; flex-direction: column; gap: 10px; }
.mile div { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding: 14px 0; border-bottom: 1px solid #e5e5e5; }
.mile strong { display: inline-flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 900; }
.save-bar { position: fixed; left: 50%; bottom: 0; width: 100%; max-width: 430px; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); border-top: 1px solid #e5e5e5; background: #fff; box-sizing: border-box; transform: translateX(-50%); }
.save-bar button { width: 100%; height: 48px; border: 0; border-radius: 999px; background: #111; color: #fff; font-size: 15px; font-weight: 950; }
.save-bar button:disabled { opacity: .42; }
</style>
