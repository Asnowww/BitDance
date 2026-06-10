<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';
import workshopHero from '@/assets/pencil/kMcxs.png';
import { fetchFeed, type ContentPost } from '@/api/community';
import { fetchWorkshops, type WorkshopBrief } from '@/api/workshop';

interface FeedItem {
  title: string;
  meta: string;
  to: string;
}

const router = useRouter();

const chips = ['推荐', '关注', '同城', '话题', 'Workshop'];
const activeChip = ref('推荐');
const workshops = ref<WorkshopBrief[]>([]);
const posts = ref<ContentPost[]>([]);
const loading = ref(false);

const featuredWorkshop = computed(() => workshops.value[0]);
const feed = computed<FeedItem[]>(() => [
  ...workshops.value.slice(0, 3).map((workshop) => ({
    title: workshop.title,
    meta: `${workshop.area} · ${workshop.styles.join('/')} · ¥${workshop.priceMin} · 剩 ${Math.max(
      0,
      workshop.capacity - workshop.taken
    )} 位`,
    to: `/workshop/${workshop.id}`
  })),
  ...posts.value.slice(0, 3).map((post) => ({
    title: `@${post.authorName} 发布了动态`,
    meta: `${post.location ?? post.style ?? '社区'} · ${post.topics.map((t) => `#${t}`).join(' ') || '新内容'}`,
    to: `/community/post/${post.id}`
  }))
]);

const load = async () => {
  loading.value = true;
  try {
    const [workshopData, feedData] = await Promise.all([
      fetchWorkshops({ page: 1, pageSize: 10 }),
      fetchFeed({ scope: activeChip.value === '关注' ? 'follow' : 'recommend', page: 1, pageSize: 10 })
    ]);
    workshops.value = workshopData.list;
    posts.value = feedData.list;
  } finally {
    loading.value = false;
  }
};

const openCommunityFeed = (scope: 'recommend' | 'follow' | 'local' = 'recommend') => {
  const query = scope === 'recommend' ? undefined : { scope };
  router.push({ path: '/community', query });
};

const switchChip = async (chip: string) => {
  if (chip === '关注') {
    openCommunityFeed('follow');
    return;
  }
  if (chip === '同城') {
    openCommunityFeed('local');
    return;
  }
  if (chip === '话题') {
    router.push('/community/topics');
    return;
  }
  if (chip === 'Workshop') {
    activeChip.value = 'Workshop';
    return;
  }
  activeChip.value = chip;
  await load();
};

onMounted(load);
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

      <section class="entry-grid" aria-label="社区快捷入口">
        <button class="entry-card entry-card--primary" type="button" @click="openCommunityFeed()">
          <strong>动态广场</strong>
          <span>图文、视频与同城舞友</span>
        </button>
        <button class="entry-card" type="button" @click="router.push('/community/topics')">
          <strong>话题</strong>
          <span>热门标签聚合</span>
        </button>
        <button class="entry-card" type="button" @click="router.push('/community/following')">
          <strong>关注</strong>
          <span>关注与粉丝</span>
        </button>
      </section>

      <section class="chip-row" aria-label="活动内容筛选">
        <button
          v-for="(chip, index) in chips"
          :key="chip"
          class="filter-chip"
          :class="{ 'filter-chip--active': activeChip === chip || (!activeChip && index === 0) }"
          type="button"
          @click="switchChip(chip)"
        >
          {{ chip }}
        </button>
      </section>

      <button
        class="workshop-hero"
        type="button"
        @click="router.push(featuredWorkshop ? `/workshop/${featuredWorkshop.id}` : '/community')"
      >
        <img :src="workshopHero" alt="Workshop 周末大师课" />
      </button>

      <section class="feed-section" aria-labelledby="local-feed-title">
        <div class="feed-section__head">
          <h2 id="local-feed-title">同城动态</h2>
          <div class="feed-section__actions">
            <button type="button" @click="openCommunityFeed('local')">更多</button>
            <button type="button" @click="router.push('/community/publish')">发布</button>
          </div>
        </div>

        <p v-if="loading" class="feed-empty">加载中</p>
        <p v-else-if="feed.length === 0" class="feed-empty">暂无同城动态</p>
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

.entry-grid {
  display: grid;
  grid-template-columns: 1.25fr 1fr 1fr;
  gap: 8px;
}

.entry-card {
  min-width: 0;
  min-height: 68px;
  border: 1px solid var(--nike-hairline-soft);
  border-radius: 18px;
  padding: 10px 12px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  text-align: left;
  cursor: pointer;

  strong,
  span {
    display: block;
    min-width: 0;
    letter-spacing: 0;
  }

  strong {
    font-size: 14px;
    line-height: 1.25;
    font-weight: 900;
  }

  span {
    color: var(--nike-mute);
    font-size: 11px;
    line-height: 1.25;
    font-weight: 700;
  }

  &--primary {
    border-color: var(--nike-ink);
    background: var(--nike-ink);
    color: var(--nike-canvas);

    span {
      color: rgba(255, 255, 255, 0.72);
    }
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

  }

  &__actions {
    flex: none;
    display: flex;
    align-items: center;
    gap: 12px;

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

.feed-empty {
  margin: 10px 0;
  color: var(--nike-mute);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
}
</style>
