<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchGrowthStats, fetchGrowthTimeline, type GrowthStats, type TimelineItem } from '@/api/growth';

const router = useRouter();
const stats = ref<GrowthStats | null>(null);
const timeline = ref<TimelineItem[]>([]);
const loading = ref(true);

const reload = async () => {
  loading.value = true;
  try {
    const [s, tl] = await Promise.all([fetchGrowthStats(), fetchGrowthTimeline()]);
    stats.value = s;
    timeline.value = tl;
  } finally {
    loading.value = false;
  }
};

onMounted(reload);

const TYPE_ICON: Record<string, string> = {
  checkin: '🔥',
  trial: '🎟',
  practice: '🤝',
  review: '✍️'
};
</script>

<template>
  <div class="page">
    <header class="head">
      <div class="head__title">成长档案</div>
      <div class="head__sub">把每一次进步都记下来</div>
    </header>

    <section v-if="stats" class="stats">
      <div class="card">
        <div class="card__num">{{ stats.totalDays }}</div>
        <div class="card__label">学舞天数</div>
      </div>
      <div class="card">
        <div class="card__num">{{ Math.round(stats.totalMinutes / 60) }}h</div>
        <div class="card__label">累计时长</div>
      </div>
      <div class="card">
        <div class="card__num">{{ stats.totalSessions }}</div>
        <div class="card__label">训练次数</div>
      </div>
      <div class="card">
        <div class="card__num">{{ stats.styleCount }}</div>
        <div class="card__label">舞种数</div>
      </div>
      <div class="card card--wide">
        <div class="card__num">🔥 {{ stats.streakDays }} 天</div>
        <div class="card__label">连续打卡</div>
      </div>
      <div class="card card--wide">
        <div class="card__num">{{ stats.goalProgress }}%</div>
        <div class="card__label">月度目标进度</div>
        <div class="bar"><div class="bar__fill" :style="{ width: `${stats.goalProgress}%` }" /></div>
      </div>
    </section>

    <section class="actions">
      <button class="action" @click="router.push('/publish/checkin')">
        <span class="action__icon">🔥</span>
        <span>立即打卡</span>
      </button>
      <button class="action" @click="router.push('/me/works')">
        <span class="action__icon">🎬</span>
        <span>阶段作品</span>
      </button>
      <button class="action" @click="router.push('/me/goal')">
        <span class="action__icon">🎯</span>
        <span>训练目标</span>
      </button>
      <button class="action" @click="router.push('/favorites')">
        <span class="action__icon">⭐</span>
        <span>我的收藏</span>
      </button>
    </section>

    <section class="timeline">
      <h3>成长时间线</h3>
      <div v-if="!timeline.length" class="empty">暂无记录，去打个卡试试</div>
      <article v-for="t in timeline" :key="t.id" class="tl">
        <div class="tl__icon">{{ TYPE_ICON[t.type] }}</div>
        <div class="tl__body">
          <div class="tl__title">{{ t.title }}</div>
          <div v-if="t.subtitle" class="tl__sub">{{ t.subtitle }}</div>
          <div class="tl__time">{{ new Date(t.ts).toLocaleString() }}</div>
        </div>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 16px;
}
.head {
  padding: 16px;
  &__title {
    font-size: 22px;
    font-weight: 700;
  }
  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
.stats {
  padding: 0 12px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  &--wide {
    grid-column: span 2;
  }
  &__num {
    font-size: 22px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__label {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.bar {
  margin-top: 10px;
  height: 6px;
  background: #f3f3f3;
  border-radius: 3px;
  overflow: hidden;
  &__fill {
    height: 100%;
    background: linear-gradient(90deg, #ff7799, #ff2442);
  }
}
.actions {
  margin: 12px 12px 8px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.action {
  background: #fff;
  border: none;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  cursor: pointer;
  &__icon {
    font-size: 18px;
  }
}
.timeline {
  padding: 12px;
  h3 {
    margin: 0 0 8px;
    padding: 0 4px;
    font-size: 15px;
  }
}
.empty {
  text-align: center;
  padding: 40px;
  color: var(--bd-text-secondary);
  font-size: 13px;
}
.tl {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 8px;
  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 36, 66, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;
  }
  &__body {
    flex: 1;
    min-width: 0;
  }
  &__title {
    font-size: 13px;
    font-weight: 600;
  }
  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__time {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
</style>
