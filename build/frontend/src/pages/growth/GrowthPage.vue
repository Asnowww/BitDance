<script setup lang="ts">
import { useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';

const router = useRouter();

const weeklyStats = [
  { value: '126', label: '学舞天' },
  { value: '43h', label: '训练' },
  { value: '18', label: '课程' }
];

const heatmap = [
  3, 0, 0, 1, 3, 0, 1, 0, 3, 1, 0, 0,
  0, 0, 1, 3, 0, 1, 0, 3, 1, 0, 0, 3,
  0, 1, 3, 0, 1, 0, 3, 1, 0, 0, 3, 0,
  1, 3, 0, 1, 0, 3, 1, 0, 0, 3, 0, 0
];
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
      <section class="week-card" aria-label="本周成长统计">
        <h2>THIS WEEK</h2>
        <div class="week-card__stats">
          <div v-for="stat in weeklyStats" :key="stat.label" class="week-card__stat">
            <strong>{{ stat.value }}</strong>
            <span>{{ stat.label }}</span>
          </div>
        </div>
      </section>

      <section class="goal-card" aria-label="周目标">
        <div class="section-head">
          <h2>周目标</h2>
          <span>3/5 次</span>
        </div>
        <div class="goal-card__bar" />
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
        <button class="growth-actions__secondary" type="button" @click="router.push('/me/works')">
          上传作品
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

.week-card {
  height: 137px;
  padding: 18px;
  background: var(--nike-ink);
  color: var(--nike-canvas);
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  h2 {
    margin: 0;
    font-size: 28px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }

  &__stats {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  &__stat {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    strong,
    span {
      display: block;
    }

    strong {
      font-size: 26px;
      line-height: 1.25;
      font-weight: 900;
      letter-spacing: 0;
    }

    span {
      font-size: 12px;
      line-height: 1.25;
      font-weight: 700;
    }
  }
}

.goal-card {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__bar {
    width: 100%;
    height: 10px;
    border-radius: 999px;
    background: var(--nike-soft-cloud);
  }
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
  grid-template-columns: repeat(12, 24px);
  grid-auto-rows: 24px;
  gap: 8px 6px;

  &__cell {
    width: 24px;
    height: 24px;
    display: block;
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
