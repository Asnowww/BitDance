<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';
import { fetchCheckins, fetchGrowthStats } from '@/api/growth';
import type { CheckinItem, GrowthStats } from '@/api/growth';

const router = useRouter();

const stats = ref<GrowthStats | null>(null);
const checkins = ref<CheckinItem[]>([]);
const loading = ref(false);
const error = ref('');

const emptyStats: GrowthStats = {
  totalSessions: 0,
  totalMinutes: 0,
  totalDays: 0,
  styleCount: 0,
  streakDays: 0,
  lastCheckinAt: null,
  courseCount: 0,
  weekSessions: 0,
  weekMinutes: 0,
  monthSessions: 0,
  monthMinutes: 0
};

const currentStats = computed(() => stats.value ?? emptyStats);

const formatMinutes = (minutes: number) => {
  if (!minutes) return '0min';
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  if (!hours) return `${rest}min`;
  return rest ? `${hours}h ${rest}m` : `${hours}h`;
};

const metricItems = computed(() => [
  { value: String(currentStats.value.totalDays), label: '累计学舞天数' },
  { value: formatMinutes(currentStats.value.totalMinutes), label: '总训练时长' },
  { value: String(currentStats.value.courseCount), label: '已上课程数' },
  { value: String(currentStats.value.styleCount), label: '尝试舞种数' },
  { value: String(currentStats.value.streakDays), label: '连续打卡天数' },
  { value: `${currentStats.value.monthSessions}次`, label: '本月训练量' }
]);

const periodStats = computed(() => [
  {
    label: '本周训练量',
    value: `${currentStats.value.weekSessions}次 · ${formatMinutes(currentStats.value.weekMinutes)}`
  },
  {
    label: '本月训练量',
    value: `${currentStats.value.monthSessions}次 · ${formatMinutes(currentStats.value.monthMinutes)}`
  }
]);

const checkinDate = (item: CheckinItem) => {
  const raw = item.checkinAt ?? item.createdAt;
  return raw ? new Date(raw) : null;
};

const checkinMinutes = (item: CheckinItem) => item.durationMinutes ?? item.durationMin ?? 0;

const heatmap = computed(() => {
  const totals = new Map<string, number>();
  checkins.value.forEach((item) => {
    const date = checkinDate(item);
    if (!date || Number.isNaN(date.getTime())) return;
    const key = date.toDateString();
    totals.set(key, (totals.get(key) ?? 0) + checkinMinutes(item));
  });

  return Array.from({ length: 48 }, (_, index) => {
    const date = new Date();
    date.setDate(date.getDate() - (47 - index));
    const minutes = totals.get(date.toDateString()) ?? 0;
    if (minutes >= 90) return 3;
    if (minutes > 0) return 1;
    return 0;
  });
});

const loadGrowthData = async () => {
  loading.value = true;
  error.value = '';
  try {
    const [nextStats, nextCheckins] = await Promise.all([fetchGrowthStats(), fetchCheckins()]);
    stats.value = nextStats;
    checkins.value = nextCheckins;
  } catch {
    error.value = '成长数据加载失败';
  } finally {
    loading.value = false;
  }
};

onMounted(loadGrowthData);
</script>

<template>
  <div class="growth-page">
    <header class="growth-topbar">
      <div class="growth-topbar__copy">
        <h1>成长</h1>
        <p>持续练习沉淀为档案</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息提醒" @click="router.push('/messages')">
        <NikeIcon name="bell" :size="20" />
      </button>
    </header>

    <main class="growth-content">
      <section class="stats-card" aria-label="成长数据">
        <div class="stats-card__head">
          <div>
            <h2>成长数据</h2>
            <p>实时汇总打卡、课程与舞种</p>
          </div>
          <span>{{ loading ? 'SYNC' : 'LIVE' }}</span>
        </div>

        <div v-if="error" class="stats-card__error">
          <span>{{ error }}</span>
          <button type="button" @click="loadGrowthData">重试</button>
        </div>

        <div v-else class="stats-grid" :class="{ 'stats-grid--loading': loading }">
          <div v-for="item in metricItems" :key="item.label" class="stats-grid__item">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>

        <div class="period-row" :class="{ 'period-row--loading': loading }">
          <div v-for="item in periodStats" :key="item.label" class="period-row__item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </section>

      <section class="goal-card" aria-label="周目标" role="button" tabindex="0" @click="router.push('/me/goal')" @keyup.enter="router.push('/me/goal')">
        <div class="section-head">
          <h2>周目标</h2>
          <span>{{ currentStats.weekSessions }}/5 次</span>
        </div>
        <div class="goal-card__bar">
          <span :style="{ width: `${Math.min(100, (currentStats.weekSessions / 5) * 100)}%` }" />
        </div>
      </section>

      <section class="heatmap-section" aria-labelledby="heatmap-title">
        <h2 id="heatmap-title">日历热力图</h2>
        <div class="heatmap" aria-hidden="true">
          <span
            v-for="(level, index) in heatmap"
            :key="index"
            class="heatmap__cell"
            :class="`heatmap__cell--${level}`"
          />
        </div>
      </section>

      <section class="growth-actions" aria-label="成长快捷操作">
        <button class="growth-actions__primary" type="button" @click="router.push('/publish/checkin')">
          今日打卡
        </button>
        <button class="growth-actions__secondary" type="button" @click="router.push('/me/works/upload')">
          上传作品
        </button>
        <button class="growth-actions__secondary" type="button" @click="router.push('/me/works')">
          阶段作品
        </button>
        <button class="growth-actions__secondary" type="button" @click="router.push('/growth/timeline')">
          成长时间线
        </button>
        <button class="growth-actions__secondary" type="button" @click="router.push('/me/goal')">
          训练目标
        </button>
        <button class="growth-actions__secondary" type="button" @click="router.push('/growth/report')">
          成长报告
        </button>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.growth-page {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft-cloud: #f5f5f5;
  --nike-mute: #707072;
  --nike-stone: #9e9ea0;
  --nike-charcoal: #39393b;
  --nike-hairline-soft: #e5e5e5;
  --nike-success: #00894d;
  --nike-success-soft: #acd9be;

  min-height: calc(100vh - 72px - env(safe-area-inset-bottom));
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.growth-topbar {
  height: 68px;
  padding: 14px 18px;
  background: var(--nike-canvas);
  border-bottom: 1px solid var(--nike-hairline-soft);
  display: flex;
  align-items: center;
  gap: 12px;

  &__copy {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  h1,
  p {
    margin: 0;
  }

  h1 {
    font-size: 18px;
    line-height: 1.25;
    font-weight: 800;
    letter-spacing: 0;
  }

  p {
    color: var(--nike-mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 500;
    letter-spacing: 0;
  }
}

.icon-button {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  display: grid;
  place-items: center;
  flex: none;
  cursor: pointer;
}

.growth-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.stats-card {
  padding: 14px;
  background: var(--nike-ink);
  color: var(--nike-canvas);
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__head {
    display: flex;
    align-items: flex-start;
    gap: 8px;

    div {
      min-width: 0;
      flex: 1;
    }

    h2,
    p {
      margin: 0;
    }

    h2 {
      font-size: 24px;
      line-height: 1.2;
      font-weight: 900;
      letter-spacing: 0;
    }

    p {
      margin-top: 2px;
      color: var(--nike-stone);
      font-size: 11px;
      line-height: 1.2;
      font-weight: 600;
      letter-spacing: 0;
    }

    > span {
      padding: 5px 10px;
      border-radius: 999px;
      background: var(--nike-canvas);
      color: var(--nike-ink);
      font-size: 10px;
      line-height: 1.2;
      font-weight: 800;
      letter-spacing: 0;
    }
  }

  &__error {
    min-height: 76px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    color: var(--nike-stone);
    font-size: 13px;
    line-height: 1.25;
    font-weight: 700;

    button {
      flex: none;
      height: 36px;
      padding: 0 16px;
      border: 0;
      border-radius: 999px;
      background: var(--nike-canvas);
      color: var(--nike-ink);
      font-size: 13px;
      line-height: 1.25;
      font-weight: 800;
      cursor: pointer;
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 9px 8px;

  &--loading {
    opacity: 0.45;
  }

  &__item {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 1px;

    strong,
    span {
      display: block;
      overflow-wrap: anywhere;
      letter-spacing: 0;
    }

    strong {
      font-size: 22px;
      line-height: 1.2;
      font-weight: 900;
    }

    span {
      color: var(--nike-hairline-soft);
      font-size: 10px;
      line-height: 1.2;
      font-weight: 700;
    }
  }
}

.period-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;

  &--loading {
    opacity: 0.45;
  }

  &__item {
    min-width: 0;
    padding: 8px 10px;
    background: var(--nike-charcoal);
    display: flex;
    flex-direction: column;
    gap: 1px;

    span,
    strong {
      display: block;
      overflow-wrap: anywhere;
      letter-spacing: 0;
    }

    span {
      color: var(--nike-stone);
      font-size: 10px;
      line-height: 1.2;
      font-weight: 700;
    }

    strong {
      color: var(--nike-canvas);
      font-size: 17px;
      line-height: 1.2;
      font-weight: 900;
    }
  }
}

.goal-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;

  &__bar {
    width: 100%;
    height: 10px;
    border-radius: 999px;
    background: var(--nike-soft-cloud);
    overflow: hidden;

    span {
      display: block;
      height: 100%;
      border-radius: 999px;
      background: var(--nike-ink);
      transition: width 180ms ease;
    }
  }
}

.goal-card:focus-visible {
  outline: 2px solid var(--nike-ink);
  outline-offset: 4px;
}

.section-head {
  height: 25px;
  display: flex;
  align-items: center;
  gap: 16px;

  h2 {
    margin: 0;
    min-width: 0;
    flex: 1;
    font-size: 20px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }

  span {
    color: var(--nike-mute);
    font-size: 13px;
    line-height: 1.25;
    font-weight: 700;
    letter-spacing: 0;
  }
}

.heatmap-section {
  display: flex;
  flex-direction: column;
  gap: 8px;

  h2 {
    margin: 0;
    font-size: 20px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }
}

.heatmap {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  grid-auto-rows: 24px;
  gap: 8px 6px;

  &__cell {
    display: block;
    min-width: 0;
    background: var(--nike-soft-cloud);

    &--1 {
      background: var(--nike-success-soft);
    }

    &--3 {
      background: var(--nike-success);
    }
  }
}

.growth-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;

  button {
    height: 48px;
    border: 0;
    border-radius: 999px;
    font-size: 15px;
    line-height: 1.25;
    font-weight: 800;
    letter-spacing: 0;
    cursor: pointer;
  }

  &__primary {
    background: var(--nike-ink);
    color: var(--nike-canvas);
  }

  &__secondary {
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
  }
}
</style>
