<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchGrowthStats, type GrowthStats } from '@/api/growth';

const router = useRouter();
const stats = ref<GrowthStats | null>(null);
const loading = ref(true);

const trend = [
  { day: '周一', value: 32 },
  { day: '周二', value: 10 },
  { day: '周三', value: 32 },
  { day: '周四', value: 15 },
  { day: '周五', value: 32 }
];

const favorites = [
  {
    type: '舞室',
    title: 'Urban Flow 舞室',
    meta: '收藏于 5/24 · 可预约',
    action: '预约试听',
    path: '/studio/1'
  },
  {
    type: '课程',
    title: 'Mia Jazz 基础课',
    meta: '周三晚 · 可报名',
    action: '立即报名',
    path: '/course/1'
  }
];

const displayStats = computed(() => {
  const source = stats.value;
  return {
    days: source?.totalDays ?? 126,
    hours: source ? `${Math.round(source.totalMinutes / 60)}h` : '43h',
    styles: source?.styleCount ?? 5
  };
});

onMounted(async () => {
  loading.value = true;
  try {
    stats.value = await fetchGrowthStats();
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="growth-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">BITDANCE DATA</p>
        <h1>学习数据</h1>
      </div>
      <button class="pill-btn" @click="router.push('/publish/checkin')">打卡</button>
    </header>

    <section class="stats-card" :class="{ loading }">
      <div class="stat">
        <strong>{{ displayStats.days }}</strong>
        <span>累计天数</span>
      </div>
      <div class="stat">
        <strong>{{ displayStats.hours }}</strong>
        <span>训练时长</span>
      </div>
      <div class="stat">
        <strong>{{ displayStats.styles }}</strong>
        <span>尝试舞种</span>
      </div>
    </section>

    <section class="trend-card">
      <div class="section-head">
        <h2>训练趋势</h2>
        <span>THIS WEEK</span>
      </div>
      <div class="bars">
        <div v-for="item in trend" :key="item.day" class="bar-item">
          <div class="bar-track">
            <div class="bar-fill" :style="{ height: `${item.value * 2}px` }" />
          </div>
          <span>{{ item.day }}</span>
        </div>
      </div>
    </section>

    <section class="favorites">
      <div class="section-head">
        <div>
          <h2>收藏管理</h2>
          <p>舞室 / 课程 / 老师</p>
        </div>
        <button class="text-btn" @click="router.push('/favorites')">全部</button>
      </div>

      <article v-for="item in favorites" :key="item.title" class="favorite-card">
        <div class="favorite-card__image">
          <span>{{ item.type }}</span>
        </div>
        <div class="favorite-card__body">
          <h3>{{ item.title }}</h3>
          <p>{{ item.meta }}</p>
          <button @click="router.push(item.path)">{{ item.action }}</button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
.growth-page {
  min-height: 100vh;
  padding: 20px 18px 28px;
  background: #fff;
  color: #111;
}

.topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  h1 {
    margin: 0;
    font-size: 34px;
    line-height: 0.95;
    font-weight: 900;
  }
}

.eyebrow {
  margin: 0 0 6px;
  color: #707072;
  font-size: 11px;
  font-weight: 900;
}

.pill-btn,
.text-btn,
.favorite-card button {
  border: 0;
  border-radius: 999px;
  font-weight: 900;
}

.pill-btn {
  height: 40px;
  padding: 0 18px;
  background: #111;
  color: #fff;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  overflow: hidden;
  margin-top: 24px;
  border-radius: 30px;
  background: #111;
  &.loading {
    opacity: 0.75;
  }
}

.stat {
  min-height: 112px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 16px 12px;
  background: #111;
  color: #fff;
  strong {
    font-size: 34px;
    line-height: 1;
    font-weight: 900;
  }
  span {
    margin-top: 8px;
    color: #cacacb;
    font-size: 12px;
    font-weight: 800;
  }
}

.trend-card,
.favorites {
  margin-top: 24px;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 900;
  }
  p,
  span {
    margin: 4px 0 0;
    color: #707072;
    font-size: 12px;
    font-weight: 800;
  }
}

.bars {
  height: 136px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: end;
  gap: 12px;
  padding: 18px 16px 12px;
  border-radius: 28px;
  background: #f5f5f5;
}

.bar-item {
  display: grid;
  justify-items: center;
  gap: 8px;
  span {
    color: #707072;
    font-size: 11px;
    font-weight: 800;
  }
}

.bar-track {
  width: 100%;
  height: 72px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar-fill {
  width: 100%;
  max-width: 34px;
  min-height: 10px;
  border-radius: 999px 999px 0 0;
  background: #111;
}

.text-btn {
  height: 34px;
  padding: 0 14px;
  background: #f5f5f5;
  color: #111;
}

.favorite-card {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 12px;
  min-height: 124px;
  margin-bottom: 12px;
  padding: 8px;
  border-radius: 30px;
  background: #f5f5f5;
  &__image {
    display: flex;
    align-items: flex-end;
    padding: 12px;
    border-radius: 24px;
    background:
      linear-gradient(135deg, rgba(17, 17, 17, 0.08), rgba(17, 17, 17, 0.7)),
      linear-gradient(135deg, #e5e5e5, #9e9ea0);
    span {
      padding: 5px 9px;
      border-radius: 999px;
      background: #fff;
      color: #111;
      font-size: 11px;
      font-weight: 900;
    }
  }
  &__body {
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    h3 {
      margin: 0;
      font-size: 16px;
      line-height: 1.25;
      font-weight: 900;
    }
    p {
      margin: 8px 0 14px;
      color: #707072;
      font-size: 12px;
    }
    button {
      width: fit-content;
      height: 36px;
      padding: 0 16px;
      background: #111;
      color: #fff;
    }
  }
}
</style>
