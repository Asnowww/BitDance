<script setup lang="ts">
import { useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';
import workshopHero from '@/assets/pencil/kMcxs.png';

interface FeedItem {
  title: string;
  meta: string;
  to: string;
}

const router = useRouter();

const chips = ['推荐', '关注', '同城', '话题', 'Workshop'];
const feed: FeedItem[] = [
  {
    title: '@Mia 上传了 Jazz 练习片段',
    meta: '关联：Beats 舞室 · #零基础复盘',
    to: '/community/post/mia-jazz'
  },
  {
    title: 'Locking 话题升温',
    meta: '23 条新动态 · 4 个约练邀约',
    to: '/community/topic/Locking'
  }
];
</script>

<template>
  <div class="activity-page">
    <header class="activity-topbar">
      <div class="activity-topbar__copy">
        <h1>社区 / 活动</h1>
        <p>动态、话题与 Workshop</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息提醒" @click="router.push('/messages')">
        <NikeIcon name="bell" :size="20" />
      </button>
    </header>

    <main class="activity-content">
      <button class="search-pill" type="button" @click="router.push('/community/search')">
        <NikeIcon name="search" :size="18" />
        <span>搜索动态、话题、Workshop</span>
      </button>

      <section class="chip-row" aria-label="活动内容筛选">
        <button
          v-for="(chip, index) in chips"
          :key="chip"
          class="filter-chip"
          :class="{ 'filter-chip--active': index === 0 }"
          type="button"
        >
          {{ chip }}
        </button>
      </section>

      <button class="workshop-hero" type="button" @click="router.push('/workshop/featured')">
        <img :src="workshopHero" alt="Workshop 周末大师课" />
      </button>

      <section class="feed-section" aria-labelledby="local-feed-title">
        <div class="feed-section__head">
          <h2 id="local-feed-title">同城动态</h2>
          <button type="button" @click="router.push('/community/publish')">发布</button>
        </div>

        <article
          v-for="item in feed"
          :key="item.title"
          class="feed-card"
          @click="router.push(item.to)"
        >
          <h3>{{ item.title }}</h3>
          <p>{{ item.meta }}</p>
        </article>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.activity-page {
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

.activity-topbar {
  height: 68px;
  padding: 13px 18px;
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
    font-size: 19px;
    line-height: 1.25;
    font-weight: 800;
    letter-spacing: 0;
  }

  p {
    color: var(--nike-mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 600;
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

.activity-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: var(--nike-canvas);
}

.search-pill {
  width: 100%;
  height: 44px;
  border: 1px solid var(--nike-hairline-soft);
  border-radius: 24px;
  padding: 0 14px;
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
    font-weight: 600;
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
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;

  &--active {
    border-color: var(--nike-ink);
    background: var(--nike-ink);
    color: var(--nike-canvas);
  }
}

.workshop-hero {
  width: 100%;
  height: 150px;
  border: 0;
  padding: 0;
  background: var(--nike-ink);
  display: block;
  overflow: hidden;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
  }
}

.feed-section {
  display: flex;
  flex-direction: column;
  gap: 14px;

  &__head {
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

    button {
      border: 0;
      padding: 0;
      background: none;
      color: var(--nike-mute);
      font-size: 13px;
      line-height: 1.25;
      font-weight: 700;
      cursor: pointer;
    }
  }
}

.feed-card {
  width: 100%;
  min-height: 72px;
  border: 0;
  padding: 14px;
  background: var(--nike-soft-cloud);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  cursor: pointer;

  h3,
  p {
    margin: 0;
  }

  h3 {
    color: var(--nike-ink);
    font-size: 16px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }

  p {
    color: var(--nike-mute);
    font-size: 13px;
    line-height: 1.25;
    font-weight: 700;
    letter-spacing: 0;
  }
}
</style>
