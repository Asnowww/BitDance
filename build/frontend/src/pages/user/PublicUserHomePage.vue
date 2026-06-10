<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  BookOpen,
  ChevronLeft,
  MessageCircle,
  Music2,
  Share2,
  ShieldCheck,
  User,
  UserPlus
} from 'lucide-vue-next';
import { showToast } from 'vant';
import { fetchUserPosts, fetchUserPractices, fetchUserReviews } from '@/api/userHome';
import type { UserContentPost, UserPracticePost, UserReviewItem } from '@/api/userHome';
import { fetchFollowStatus, toggleFollow } from '@/api/community';

const router = useRouter();
const route = useRoute();

type ContentTab = 'posts' | 'reviews' | 'practices';

const publicSocials = [
  { platform: '抖音', account: '@urban_lili', icon: Music2, dark: true },
  { platform: '小红书', account: '小李练舞日记', icon: BookOpen, dark: false }
];

const activeTab = ref<ContentTab>('posts');
const posts = ref<UserContentPost[]>([]);
const reviews = ref<UserReviewItem[]>([]);
const practices = ref<UserPracticePost[]>([]);
const totals = ref({ posts: 0, reviews: 0, practices: 0 });
const following = ref(false);
const followCounts = ref({ followers: 0, following: 0 });
const loading = ref(false);

const userId = computed(() => Number(route.params.id || 1));
const displayName = computed(() => (userId.value === 1 ? '小李' : `用户 ${userId.value}`));

const topicLabel = (topic: string | { name?: string; topicName?: string }) =>
  typeof topic === 'string' ? topic : topic.topicName || topic.name || '话题';

const postText = (item: UserContentPost) => item.text || item.contentText || '这个用户还没有填写动态内容';
const postTopics = (item: UserContentPost) => (item.topics ?? []).map(topicLabel).slice(0, 3);
const reviewScore = (item: UserReviewItem) => Number(item.overallScore || 0).toFixed(1);
const reviewTarget = (item: UserReviewItem) =>
  ({ studio: '舞室评价', coach: '老师评价', course: '课程评价' }[item.targetType] ?? '评价');
const practiceTitle = (item: UserPracticePost) =>
  item.title || item.description || `${item.style || item.skillLevel || '舞蹈'} 约练`;
const practiceMeta = (item: UserPracticePost) => {
  if (item.date || item.time) return [item.date, item.time, item.location].filter(Boolean).join(' · ');
  const date = item.startAt ? item.startAt.slice(0, 10) : '待定时间';
  return [date, item.locationName, item.skillLevel].filter(Boolean).join(' · ');
};

const loadHomeData = async () => {
  loading.value = true;
  try {
    const [postResp, reviewResp, practiceResp, statusResp] = await Promise.all([
      fetchUserPosts(userId.value, 1, 20),
      fetchUserReviews(userId.value, 1, 20),
      fetchUserPractices(userId.value),
      fetchFollowStatus(userId.value).catch(() => null)
    ]);
    posts.value = postResp.list ?? [];
    reviews.value = reviewResp.list ?? [];
    practices.value = practiceResp ?? [];
    totals.value = {
      posts: postResp.total ?? posts.value.length,
      reviews: reviewResp.total ?? reviews.value.length,
      practices: practices.value.length
    };
    following.value = Boolean(statusResp?.following);
    followCounts.value = {
      followers: statusResp?.followerCount ?? 0,
      following: statusResp?.followeeCount ?? 0
    };
  } finally {
    loading.value = false;
  }
};

const onFollow = async () => {
  const next = await toggleFollow(userId.value);
  following.value = next.following;
  followCounts.value = {
    followers: next.followerCount,
    following: followCounts.value.following
  };
  showToast(next.following ? '已关注' : '已取消关注');
};

onMounted(loadHomeData);
watch(userId, loadHomeData);
</script>

<template>
  <main class="public-home">
    <header class="topbar">
      <button type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="24" />
      </button>
      <h1>{{ displayName }}的主页</h1>
      <button type="button" aria-label="分享">
        <Share2 :size="22" />
      </button>
    </header>

    <section class="public-scroll">
      <section class="hero-card">
        <div class="hero-card__main">
          <div class="avatar">
            <User :size="36" />
          </div>
          <div class="hero-card__copy">
            <h2>{{ displayName }}</h2>
            <p>@bitdance_lili · 零基础韩舞爱好者 · 北京海淀</p>
            <div class="chips">
              <span class="chip chip--active">韩舞</span>
              <span class="chip">周末约练</span>
              <span class="chip">Urban</span>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <button class="hero-actions__follow" type="button" @click="onFollow">
            <UserPlus :size="18" />
            <span>{{ following ? '已关注' : '关注' }}</span>
          </button>
          <button class="hero-actions__message" type="button">
            <MessageCircle :size="18" />
            <span>私信</span>
          </button>
        </div>
      </section>

      <section class="stats" aria-label="社交数据">
        <span><strong>{{ totals.posts }}</strong><em>动态</em></span>
        <span><strong>{{ followCounts.followers }}</strong><em>粉丝</em></span>
        <span><strong>{{ followCounts.following }}</strong><em>关注</em></span>
      </section>

      <section class="section">
        <h2>公开社交账号</h2>
        <article v-for="item in publicSocials" :key="item.platform" class="social-row">
          <span class="social-row__icon" :class="{ 'social-row__icon--dark': item.dark }">
            <component :is="item.icon" :size="23" />
          </span>
          <span class="social-row__copy">
            <strong>{{ item.platform }}</strong>
            <em>{{ item.account }}</em>
          </span>
          <span class="state state--active">公开</span>
        </article>
      </section>

      <section class="notice">
        <ShieldCheck :size="20" />
        <span>对方未公开或未绑定的账号不会在此显示</span>
      </section>

      <nav class="segment" aria-label="主页内容筛选">
        <button class="segment__item" :class="{ 'segment__item--active': activeTab === 'posts' }" type="button" @click="activeTab = 'posts'">
          动态 {{ totals.posts }}
        </button>
        <button class="segment__item" :class="{ 'segment__item--active': activeTab === 'reviews' }" type="button" @click="activeTab = 'reviews'">
          评价 {{ totals.reviews }}
        </button>
        <button class="segment__item" :class="{ 'segment__item--active': activeTab === 'practices' }" type="button" @click="activeTab = 'practices'">
          约练 {{ totals.practices }}
        </button>
      </nav>

      <section class="content-list" aria-live="polite">
        <p v-if="loading" class="empty-state">加载中</p>
        <template v-else-if="activeTab === 'posts'">
          <article v-for="item in posts" :key="item.id" class="content-card" @click="router.push(`/community/post/${item.id}`)">
            <h3>最近动态</h3>
            <p>{{ postText(item) }}</p>
            <div v-if="postTopics(item).length" class="chips">
              <span v-for="topic in postTopics(item)" :key="topic" class="chip">{{ topic }}</span>
            </div>
          </article>
          <p v-if="!posts.length" class="empty-state">还没有公开动态</p>
        </template>
        <template v-else-if="activeTab === 'reviews'">
          <article v-for="item in reviews" :key="item.id" class="content-card">
            <h3>{{ reviewTarget(item) }} · {{ reviewScore(item) }}</h3>
            <p>{{ item.contentText }}</p>
            <div class="chips">
              <span class="chip" :class="{ 'chip--active': item.isVerified }">{{ item.isVerified ? '已验证' : '普通评价' }}</span>
            </div>
          </article>
          <p v-if="!reviews.length" class="empty-state">还没有公开评价</p>
        </template>
        <template v-else>
          <article v-for="item in practices" :key="item.id" class="content-card">
            <h3>{{ practiceTitle(item) }}</h3>
            <p>{{ practiceMeta(item) }}</p>
            <div class="chips">
              <span class="chip chip--active">{{ item.postStatus || item.status || 'PUBLISHED' }}</span>
              <span class="chip">{{ item.currentPeopleCount ?? item.takenCount ?? 1 }}/{{ item.expectedPeopleMax ?? item.capacity ?? 4 }} 人</span>
            </div>
          </article>
          <p v-if="!practices.length" class="empty-state">还没有公开约练</p>
        </template>
      </section>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.public-home {
  @include pen-page;
  min-height: 100%;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  border-bottom: 1px solid $pen-hairline;
  background: $pen-canvas;

  h1 {
    flex: 1;
    margin: 0;
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  button {
    display: grid;
    flex: none;
    width: 40px;
    height: 40px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    cursor: pointer;
    place-items: center;
  }
}

.public-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px;
}

.hero-card {
  display: flex;
  min-height: 180px;
  flex-direction: column;
  gap: 14px;
  padding: 14px;
  border-radius: 18px;
  background: $pen-soft;

  &__main {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__copy {
    min-width: 0;
    flex: 1;

    h2,
    p {
      margin: 0;
    }

    h2 {
      font-size: 28px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    p {
      margin-top: 3px;
      color: $pen-mute;
      font-size: 13px;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }
}

.avatar {
  display: grid;
  flex: none;
  width: 66px;
  height: 66px;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  place-items: center;
}

.hero-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;

  button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 42px;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
    cursor: pointer;
  }

  &__follow {
    border: 0;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  &__message {
    border: 1px solid $pen-hairline;
    background: $pen-canvas;
    color: $pen-ink;
  }
}

.section {
  display: flex;
  flex-direction: column;
  gap: 10px;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;

  span {
    min-height: 56px;
    border: 1px solid $pen-hairline;
    border-radius: 14px;
    background: $pen-canvas;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
  }

  strong {
    font-size: 20px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  em {
    color: $pen-mute;
    font-size: 12px;
    font-style: normal;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.chip,
.state,
.segment__item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  padding: 7px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 900;
  line-height: $pen-lh;
  white-space: nowrap;
}

.chip--active,
.state--active,
.segment__item--active {
  border-color: $pen-ink;
  background: $pen-ink;
  color: $pen-on-primary;
}

.social-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 58px;
  padding: 10px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 12px;
  background: $pen-canvas;

  &__icon {
    display: grid;
    flex: none;
    width: 38px;
    height: 38px;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    place-items: center;

    &--dark {
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }

  &__copy {
    min-width: 0;
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 3px;

    strong {
      font-size: 14px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    em {
      overflow: hidden;
      color: $pen-mute;
      font-size: 12px;
      font-style: normal;
      font-weight: 800;
      line-height: $pen-lh;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.notice {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 52px;
  padding: 12px;
  border-radius: 12px;
  background: $pen-soft;
  color: $pen-success;

  span {
    min-width: 0;
    flex: 1;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}

.segment {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.segment__item {
  min-width: 70px;
  border: 1px solid $pen-hairline;
  cursor: pointer;
}

.content-card {
  display: flex;
  min-height: 118px;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-canvas;
  cursor: pointer;

  h3,
  p {
    margin: 0;
  }

  h3 {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    font-size: 14px;
    font-weight: 800;
    line-height: 1.45;
  }
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-state {
  margin: 0;
  padding: 18px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 900;
  line-height: $pen-lh;
  text-align: center;
}
</style>
