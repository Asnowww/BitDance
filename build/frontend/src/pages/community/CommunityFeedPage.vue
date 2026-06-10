<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ChevronLeft, SquarePen, MapPin, Heart, MessageCircle, Share2, Hash, Users } from 'lucide-vue-next';
import {
  fetchFeed,
  fetchMyFolloweeIds,
  fetchPostDetail,
  sharePost,
  toggleFollow,
  togglePostLike,
  type ContentPost
} from '@/api/community';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const user = useUserStore();
const cats = ['推荐', '关注', '同城', '话题'];
const catFromScope = (value: unknown) => {
  if (value === 'follow') return '关注';
  if (value === 'local') return '同城';
  return '推荐';
};
const activeCat = ref(catFromScope(route.query.scope));
const posts = ref<ContentPost[]>([]);
const loading = ref(false);
const followedIds = ref<Set<number>>(new Set());
const publishedId = computed(() => Number(route.query.published) || null);

const scope = computed(() => (activeCat.value === '关注' ? 'follow' : 'recommend'));
const relativeTime = (value: number) => {
  const diff = Date.now() - value;
  const minutes = Math.max(1, Math.floor(diff / 60000));
  if (minutes < 60) return `${minutes} 分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} 小时前`;
  return `${Math.floor(hours / 24)} 天前`;
};

const loadPosts = async () => {
  loading.value = true;
  try {
    const [data, ids] = await Promise.all([
      fetchFeed({ scope: scope.value, page: 1, pageSize: 20 }),
      fetchMyFolloweeIds().catch(() => Array.from(followedIds.value))
    ]);
    followedIds.value = new Set(ids ?? []);
    let nextPosts = data.list;
    if (publishedId.value) {
      const published = await fetchPostDetail(publishedId.value).catch(() => null);
      if (published) {
        nextPosts = [published, ...nextPosts.filter((item) => item.id !== published.id)];
      }
    }
    posts.value = nextPosts;
  } finally {
    loading.value = false;
  }
};

const switchCat = async (cat: string) => {
  if (cat === '话题') {
    router.push('/community/topics');
    return;
  }
  activeCat.value = cat;
  const nextScope = cat === '关注' ? 'follow' : cat === '同城' ? 'local' : undefined;
  router.replace({ path: '/community', query: nextScope ? { scope: nextScope } : {} });
  await loadPosts();
};

const like = async (post: ContentPost) => {
  const next = await togglePostLike(post.id);
  post.liked = next.liked;
  post.likeCount = next.likeCount;
};

const follow = async (post: ContentPost) => {
  if (isMine(post)) return;
  const next = await toggleFollow(post.authorId);
  const ids = new Set(followedIds.value);
  if (next.following) ids.add(post.authorId);
  else ids.delete(post.authorId);
  followedIds.value = ids;
  if (activeCat.value === '关注' && !next.following) {
    posts.value = posts.value.filter((item) => item.authorId !== post.authorId);
  }
  showToast(next.following ? '已关注' : '已取消关注');
};

const isFollowed = (post: ContentPost) => followedIds.value.has(post.authorId);
const isMine = (post: ContentPost) => post.authorId === (user.profile?.id ?? 1);

const copyLink = async (post: ContentPost) => {
  const url = `${window.location.origin}${window.location.pathname}#/community/post/${post.id}`;
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(url);
  } else {
    const input = document.createElement('input');
    input.value = url;
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
  }
};

const share = async (post: ContentPost) => {
  const next = await sharePost(post.id, 'copy');
  post.shareCount = next.shareCount;
  await copyLink(post);
  showToast('动态链接已复制');
};

onMounted(loadPosts);
watch(() => route.query.published, loadPosts);
watch(
  () => route.query.scope,
  async (next) => {
    const cat = catFromScope(next);
    if (cat === activeCat.value) return;
    activeCat.value = cat;
    await loadPosts();
  }
);
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <div class="topbar__copy">
        <h1 class="topbar__title">动态广场</h1>
        <p>图文、话题、关注与同城舞友</p>
      </div>
      <button class="topbar__compose" type="button" aria-label="发动态" @click="router.push('/community/publish')">
        <SquarePen :size="20" :stroke-width="2" />
      </button>
    </header>

    <section class="pen-scroll">
      <div v-if="publishedId" class="published-banner">
        <strong>发布成功</strong>
        <span>你的动态已进入社区广场</span>
        <button type="button" @click="router.push(`/community/post/${publishedId}`)">查看</button>
      </div>

      <div class="quick-row" aria-label="社区快捷入口">
        <button type="button" @click="router.push('/community/topics')">
          <Hash :size="18" :stroke-width="2" />
          <span>话题广场</span>
        </button>
        <button type="button" @click="router.push('/community/following')">
          <Users :size="18" :stroke-width="2" />
          <span>关注/粉丝</span>
        </button>
        <button type="button" @click="router.push('/workshops')">
          <MapPin :size="18" :stroke-width="2" />
          <span>Workshop</span>
        </button>
      </div>

      <div class="chip-row">
        <button
          v-for="c in cats"
          :key="c"
          class="chip"
          :class="activeCat === c ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="switchCat(c)"
        >
          {{ c }}
        </button>
      </div>

      <p v-if="loading" class="empty">加载中</p>
      <p v-else-if="posts.length === 0" class="empty">暂无动态</p>
      <article
        v-for="p in posts"
        :key="p.id"
        class="post"
        :class="{ 'post--published': p.id === publishedId }"
        @click="router.push(`/community/post/${p.id}`)"
      >
        <header class="post__head">
          <span class="post__avatar" aria-hidden="true" @click.stop="router.push(`/user/${p.authorId}`)" />
          <div class="post__who" @click.stop="router.push(`/user/${p.authorId}`)">
            <strong class="post__name">{{ p.authorName }}</strong>
            <span class="post__meta">{{ p.location ?? p.style ?? '社区' }} · {{ relativeTime(p.createdAt) }}</span>
          </div>
          <button
            v-if="!isMine(p)"
            class="post__follow"
            :class="{ 'post__follow--on': isFollowed(p) }"
            type="button"
            @click.stop="follow(p)"
          >
            {{ isFollowed(p) ? '已关注' : '关注' }}
          </button>
        </header>
        <p class="post__text">{{ p.text }}</p>
        <div v-if="p.mediaAssets.length" class="post__media">
          <video
            v-if="p.hasVideo && p.mediaAssets[0]"
            :src="p.mediaAssets[0].url"
            muted
            playsinline
            preload="metadata"
          />
          <template v-else>
            <img
              v-for="image in p.mediaAssets.filter((item) => item.mediaType === 'image').slice(0, 4)"
              :key="image.id"
              :src="image.url"
              :alt="image.originalFilename || '动态图片'"
            />
          </template>
        </div>
        <div class="post__anchor">
          <MapPin :size="14" :stroke-width="2" />
          <span>{{ p.topics.map((topic) => `#${topic}`).join(' ') || p.location || '同城动态' }}</span>
        </div>
        <div class="post__actions">
          <button class="act act--button" type="button" @click.stop="like(p)">
            <Heart :size="18" :stroke-width="2" :fill="p.liked ? 'currentColor' : 'none'" />{{ p.likeCount }}
          </button>
          <span class="act"><MessageCircle :size="18" :stroke-width="2" />{{ p.commentCount }}</span>
          <button class="act act--button" type="button" @click.stop="share(p)">
            <Share2 :size="18" :stroke-width="2" />{{ p.shareCount }}
          </button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
  &__title { margin: 0; font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__copy p { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: $pen-lh; }

  &__icon {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }
  &__compose {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    display: grid; place-items: center; cursor: pointer;
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.published-banner {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 2px 10px;
  align-items: center;
  padding: 12px;
  border-radius: 14px;
  background: $pen-ink;
  color: $pen-on-primary;

  strong {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span {
    color: rgba(255, 255, 255, 0.72);
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  button {
    grid-row: 1 / span 2;
    grid-column: 2;
    height: 32px;
    padding: 0 12px;
    border: 0;
    border-radius: 999px;
    background: $pen-on-primary;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    cursor: pointer;
  }
}

.quick-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;

  button {
    min-width: 0;
    min-height: 58px;
    border: 1px solid $pen-hairline;
    border-radius: 14px;
    background: $pen-soft;
    color: $pen-ink;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.empty {
  margin: 20px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.post {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid $pen-hairline;
  cursor: pointer;

  &--published {
    padding: 12px;
    border: 1px solid $pen-ink;
    border-radius: 14px;
  }

  &__head { display: flex; align-items: center; gap: 10px; }
  &__avatar { flex: none; width: 40px; height: 40px; border-radius: 999px; background: $pen-ink; }
  &__who { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
  &__name { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }

  &__follow {
    flex: none;
    height: 32px;
    padding: 6px 12px;
    border: 1px solid $pen-ink;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;

    &--on {
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }

  &__text { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
  &__media {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 4px;
    overflow: hidden;
    min-height: 160px;
    border-radius: 14px;
    background: $pen-soft;

    img,
    video {
      width: 100%;
      height: 100%;
      min-height: 160px;
      object-fit: cover;
    }

    video {
      grid-column: 1 / -1;
    }
  }

  &__anchor {
    align-self: flex-start;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 6px 12px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__actions { display: flex; gap: 20px; }
}

.act {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 600;
  line-height: $pen-lh;

  &--button {
    border: 0;
    padding: 0;
    background: transparent;
    cursor: pointer;
  }
}
</style>
