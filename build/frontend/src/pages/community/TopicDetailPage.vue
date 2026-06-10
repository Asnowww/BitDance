<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Heart, MessageCircle, Share2 } from 'lucide-vue-next';
import CommunityMediaGallery from '@/components/community/CommunityMediaGallery.vue';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchTopicDetail, fetchTopicPosts, sharePost, togglePostLike, type CommunityTopic, type ContentPost } from '@/api/community';

const route = useRoute();
const router = useRouter();
const topic = computed(() => decodeURIComponent(String(route.params.name || '零基础打卡挑战')));
const sort = ref<'hot' | 'new'>('hot');
const detail = ref<CommunityTopic | null>(null);
const posts = ref<ContentPost[]>([]);
const loading = ref(false);

const relativeTime = (value: number) => {
  const minutes = Math.max(1, Math.floor((Date.now() - value) / 60000));
  if (minutes < 60) return `${minutes} 分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} 小时前`;
  return `${Math.floor(hours / 24)} 天前`;
};

const load = async () => {
  loading.value = true;
  try {
    const [topicInfo, feed] = await Promise.all([
      fetchTopicDetail(topic.value),
      fetchTopicPosts(topic.value, { sort: sort.value, page: 1, pageSize: 30 })
    ]);
    detail.value = topicInfo;
    posts.value = feed.list;
  } finally {
    loading.value = false;
  }
};

const switchSort = async (next: 'hot' | 'new') => {
  sort.value = next;
  await load();
};

const onLike = async (post: ContentPost) => {
  const next = await togglePostLike(post.id);
  post.liked = next.liked;
  post.likeCount = next.likeCount;
};

const copyTopicLink = async () => {
  const url = `${window.location.origin}${window.location.pathname}#/community/topic/${encodeURIComponent(topic.value)}`;
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(url);
  }
  showToast('话题链接已复制');
};

const onShare = async (post: ContentPost) => {
  const next = await sharePost(post.id, 'copy');
  post.shareCount = next.shareCount;
  showToast('动态链接已复制');
};

const joinTopic = () => {
  router.push(`/community/publish?topic=${encodeURIComponent(topic.value)}`);
};

onMounted(load);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="话题" @share="copyTopicLink" />

    <section class="pen-scroll">
      <section class="hero">
        <strong class="hero__title"># {{ topic }}</strong>
        <span class="hero__meta">{{ detail?.count ?? 0 }} 条动态 · {{ detail?.hot ? '热门推荐' : '新话题' }}</span>
      </section>

      <div class="inner">
        <div class="chip-row">
          <button class="chip" :class="sort === 'hot' ? 'chip--active' : 'chip--inactive'" type="button" @click="switchSort('hot')">最热</button>
          <button class="chip" :class="sort === 'new' ? 'chip--active' : 'chip--inactive'" type="button" @click="switchSort('new')">最新</button>
        </div>

        <p v-if="loading" class="empty">加载中</p>
        <p v-else-if="posts.length === 0" class="empty">暂无相关动态</p>
        <article v-for="p in posts" :key="p.id" class="post" @click="router.push(`/community/post/${p.id}`)">
          <header class="post__head">
            <span class="post__avatar" aria-hidden="true" />
            <div class="post__who">
              <strong class="post__name">{{ p.authorName }}</strong>
              <span class="post__time">{{ p.location ?? p.style ?? '社区' }} · {{ relativeTime(p.createdAt) }}</span>
            </div>
          </header>
          <p class="post__text">{{ p.text }}</p>
          <CommunityMediaGallery v-if="p.mediaAssets.length" :assets="p.mediaAssets" />
          <div class="post__actions">
            <button type="button" @click.stop="onLike(p)">
              <Heart :size="17" :stroke-width="2" :fill="p.liked ? 'currentColor' : 'none'" />{{ p.likeCount }}
            </button>
            <span><MessageCircle :size="17" :stroke-width="2" />{{ p.commentCount }}</span>
            <button type="button" @click.stop="onShare(p)">
              <Share2 :size="17" :stroke-width="2" />{{ p.shareCount }}
            </button>
          </div>
        </article>
      </div>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="joinTopic">参与话题</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 14px; }

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 150px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__title { font-size: 28px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.inner { display: flex; flex-direction: column; gap: 14px; padding: 0 18px 20px; }

.chip-row { display: flex; gap: 8px; }
.chip { @include pen-chip; }

.post {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid $pen-hairline;

  &__head { display: flex; align-items: center; gap: 10px; }
  &__avatar { flex: none; width: 36px; height: 36px; border-radius: 999px; background: $pen-ink; }
  &__who { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
  &__name { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
  &__time { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__text { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
  &__actions {
    display: flex;
    gap: 18px;

    button,
    span {
      border: 0;
      background: transparent;
      color: $pen-mute;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      padding: 0;
      font-size: 12px;
      font-weight: 800;
      line-height: $pen-lh;
    }

    button {
      cursor: pointer;
    }
  }
}

.empty {
  margin: 12px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.save-bar {
  position: fixed;
  right: 0; bottom: var(--app-tabbar-offset, 0px); left: 0;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  &__btn {
    width: 100%;
    height: 48px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}
</style>
