<script setup lang="ts">
import { useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';

interface PracticeCard {
  title: string;
  place: string;
  meta: string;
  action: string;
  tone: 'dark' | 'light';
  to: string;
}

const router = useRouter();

const scopeChips = ['推荐', '附近', '同舞种', '我的'];
const filterChips = ['Hiphop', '中级', '周末', '3人'];
const practices: PracticeCard[] = [
  {
    title: '周六 Hiphop 中级复习',
    place: '五道口 DanceLab · 15:00',
    meta: '2/4 人 · 发起人已验证',
    action: '我要参加',
    tone: 'dark',
    to: '/practice/recommend'
  },
  {
    title: '韩舞成品舞互拍',
    place: '朝阳 Joy Studio · 今晚',
    meta: '1/3 人 · 接受新手',
    action: '查看详情',
    tone: 'light',
    to: '/practice/kpop-shoot'
  },
  {
    title: 'Urban 基础律动',
    place: '中关村 · 明天 19:30',
    meta: '3/3 人 · 已满员',
    action: '查看详情',
    tone: 'light',
    to: '/practice/urban-basic'
  }
];

const goPractice = (practice: PracticeCard) => {
  router.push(practice.to);
};
</script>

<template>
  <div class="practice-page">
    <header class="practice-topbar">
      <div class="practice-topbar__copy">
        <h1>约练</h1>
        <p>推荐匹配同城舞友</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息提醒" @click="router.push('/messages')">
        <NikeIcon name="bell" :size="20" />
      </button>
    </header>

    <main class="practice-content">
      <button class="search-pill" type="button" @click="router.push('/search')">
        <NikeIcon name="search" :size="18" />
        <span>搜索舞种、地点、发起人</span>
      </button>

      <section class="chip-row" aria-label="约练推荐范围">
        <button
          v-for="(chip, index) in scopeChips"
          :key="chip"
          class="filter-chip"
          :class="{ 'filter-chip--active': index === 0 }"
          type="button"
        >
          {{ chip }}
        </button>
      </section>

      <section class="chip-row" aria-label="约练筛选条件">
        <button v-for="chip in filterChips" :key="chip" class="filter-chip" type="button">
          {{ chip }}
        </button>
      </section>

      <section class="practice-list" aria-label="推荐约练">
        <article
          v-for="practice in practices"
          :key="practice.title"
          class="practice-card"
          :class="`practice-card--${practice.tone}`"
          @click="goPractice(practice)"
        >
          <h2>{{ practice.title }}</h2>
          <p class="practice-card__place">{{ practice.place }}</p>
          <p class="practice-card__meta">{{ practice.meta }}</p>
          <button class="practice-card__action" type="button" @click.stop="goPractice(practice)">
            {{ practice.action }}
          </button>
        </article>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.practice-page {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft-cloud: #f5f5f5;
  --nike-mute: #707072;
  --nike-hairline-soft: #e5e5e5;

  min-height: calc(100vh - 72px - env(safe-area-inset-bottom));
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.practice-topbar {
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

.practice-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: var(--nike-canvas);
}

.search-pill {
  width: 100%;
  height: 44px;
  border: 0;
  border-radius: 24px;
  padding: 0 16px;
  background: var(--nike-soft-cloud);
  color: var(--nike-mute);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-align: left;

  span {
    min-width: 0;
    flex: 1;
    color: var(--nike-mute);
    font-size: 14px;
    line-height: 1.25;
    font-weight: 500;
  }
}

.chip-row {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-chip {
  height: 40px;
  border: 1px solid var(--nike-hairline-soft);
  border-radius: 999px;
  padding: 8px 14px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  font-size: 13px;
  line-height: 1.25;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;

  &--active {
    border-color: var(--nike-ink);
    background: var(--nike-ink);
    color: var(--nike-canvas);
  }
}

.practice-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.practice-card {
  width: 100%;
  min-height: 153px;
  border: 0;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;

  h2,
  p {
    margin: 0;
  }

  h2 {
    font-size: 18px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }

  &__place {
    font-size: 14px;
    line-height: 1.25;
    font-weight: 600;
  }

  &__meta {
    font-size: 12px;
    line-height: 1.25;
    font-weight: 600;
  }

  &__action {
    width: 100%;
    height: 42px;
    border: 0;
    border-radius: 999px;
    padding: 12px 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 15px;
    line-height: 1.25;
    font-weight: 700;
    cursor: pointer;
  }

  &--dark {
    background: var(--nike-ink);
    color: var(--nike-canvas);

    .practice-card__meta {
      color: var(--nike-hairline-soft);
    }

    .practice-card__action {
      background: var(--nike-soft-cloud);
      color: var(--nike-ink);
    }
  }

  &--light {
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);

    .practice-card__place,
    .practice-card__meta {
      color: var(--nike-mute);
    }

    .practice-card__action {
      background: var(--nike-ink);
      color: var(--nike-canvas);
    }
  }
}
</style>
