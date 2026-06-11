<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ChevronLeft, ExternalLink, MessageCircle, Share2, ShieldCheck, User, UserPlus } from 'lucide-vue-next';
import { showToast } from 'vant';
import { fetchPublicSocialAccounts } from '@/api/social';
import type { SocialAccount } from '@/api/social';
import {
  fetchPublicUserProfile,
  fetchUserPosts,
  fetchUserPractices,
  fetchUserReviews,
  type PublicUserProfile,
  type UserContentPost,
  type UserPracticePost,
  type UserReviewItem
} from '@/api/userHome';
import { fetchFollowStatus, toggleFollow } from '@/api/community';

const router = useRouter();
const route = useRoute();

type ContentTab = 'posts' | 'reviews' | 'practices';

const activeTab = ref<ContentTab>('posts');
const profile = ref<PublicUserProfile>();
const posts = ref<UserContentPost[]>([]);
const reviews = ref<UserReviewItem[]>([]);
const practices = ref<UserPracticePost[]>([]);
const socials = ref<SocialAccount[]>([]);
const totals = ref({ posts: 0, reviews: 0, practices: 0 });
const following = ref(false);
const followCounts = ref({ followers: 0, following: 0 });
const loading = ref(false);

const userId = computed(() => Number(route.params.id || 0));
const access = computed(() => profile.value?.access);
const profileVisible = computed(() => Boolean(access.value?.profileVisible));
const contentVisible = computed(() => Boolean(access.value?.contentVisible));
const practiceVisible = computed(() => Boolean(access.value?.practiceVisible));
const displayName = computed(() =>
  profileVisible.value && profile.value?.nickname ? profile.value.nickname : `用户 ${userId.value || '-'}`
);
const styleTags = computed(() =>
  (profile.value?.styles ?? [])
    .map((style) => style.name || style.skillLevel)
    .filter(Boolean)
    .slice(0, 4) as string[]
);

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

const resetLists = () => {
  posts.value = [];
  reviews.value = [];
  practices.value = [];
  socials.value = [];
  totals.value = { posts: 0, reviews: 0, practices: 0 };
};

const loadHomeData = async () => {
  if (!userId.value) return;
  loading.value = true;
  resetLists();
  try {
    const profileResp = await fetchPublicUserProfile(userId.value);
    profile.value = profileResp;

    if (profileResp.access.profileVisible) {
      socials.value = await fetchPublicSocialAccounts(userId.value).catch(() => []);
    }

    const tasks: Promise<void>[] = [];
    if (profileResp.access.contentVisible) {
      tasks.push(
        fetchUserPosts(userId.value, 1, 5).then((resp) => {
          posts.value = resp.list ?? [];
          totals.value.posts = resp.total ?? posts.value.length;
        }),
        fetchUserReviews(userId.value, 1, 5).then((resp) => {
          reviews.value = resp.list ?? [];
          totals.value.reviews = resp.total ?? reviews.value.length;
        })
      );
    }
    if (profileResp.access.practiceVisible) {
      tasks.push(
        fetchUserPractices(userId.value).then((resp) => {
          practices.value = resp ?? [];
          totals.value.practices = practices.value.length;
        })
      );
    }
    const statusResp = await fetchFollowStatus(userId.value).catch(() => null);
    following.value = Boolean(statusResp?.following);
    followCounts.value = {
      followers: statusResp?.followerCount ?? 0,
      following: statusResp?.followeeCount ?? 0
    };
    await Promise.allSettled(tasks);
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
            <p v-if="profileVisible">
              {{ profile?.bio || profile?.learningGoal || '公开主页只展示对方允许公开的内容和社交账号。' }}
            </p>
            <p v-else>对方未公开个人资料，只显示必要的用户编号。</p>
            <div class="chips">
              <span class="chip" :class="{ 'chip--active': profileVisible }">
                {{ profileVisible ? '资料可见' : '资料未公开' }}
              </span>
              <span v-if="contentVisible" class="chip">动态 {{ totals.posts }}</span>
              <span v-if="contentVisible" class="chip">评价 {{ totals.reviews }}</span>
              <span v-if="practiceVisible" class="chip">约练 {{ totals.practices }}</span>
              <span v-for="tag in styleTags" :key="tag" class="chip">{{ tag }}</span>
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
        <article v-for="item in socials" :key="item.id" class="social-card">
          <span>
            <strong>{{ item.platform }}</strong>
            <em>{{ item.accountName }}</em>
          </span>
          <a v-if="item.profileUrl" :href="item.profileUrl" target="_blank" rel="noreferrer" aria-label="打开社交账号">
            <ExternalLink :size="18" />
          </a>
        </article>
        <p v-if="!socials.length" class="empty-state">
          {{ profileVisible ? '对方暂未公开社交账号' : '对方未公开个人资料，社交账号不可见' }}
        </p>
      </section>

      <section class="notice">
        <ShieldCheck :size="20" />
        <span>这里的数据由后端按对方隐私设置裁剪：资料、动态评价、约练可以分别设置可见范围。</span>
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
        <p v-if="loading" class="empty-state">加载中...</p>
        <template v-else-if="activeTab === 'posts'">
          <p v-if="!contentVisible" class="empty-state">对方未公开动态</p>
          <article
            v-for="item in posts"
            v-else
            :key="item.id"
            class="content-card"
            @click="router.push(`/community/post/${item.id}`)"
          >
            <h3>最近动态</h3>
            <p>{{ postText(item) }}</p>
            <div v-if="postTopics(item).length" class="chips">
              <span v-for="topic in postTopics(item)" :key="topic" class="chip">{{ topic }}</span>
            </div>
          </article>
          <p v-if="contentVisible && !posts.length" class="empty-state">还没有公开动态</p>
        </template>
        <template v-else-if="activeTab === 'reviews'">
          <p v-if="!contentVisible" class="empty-state">对方未公开评价</p>
          <article v-for="item in reviews" v-else :key="item.id" class="content-card">
            <h3>{{ reviewTarget(item) }} · {{ reviewScore(item) }}</h3>
            <p>{{ item.contentText }}</p>
            <div class="chips">
              <span class="chip" :class="{ 'chip--active': item.isVerified }">{{ item.isVerified ? '已验证' : '普通评价' }}</span>
            </div>
          </article>
          <p v-if="contentVisible && !reviews.length" class="empty-state">还没有公开评价</p>
        </template>
        <template v-else>
          <p v-if="!practiceVisible" class="empty-state">对方未公开约练</p>
          <article v-for="item in practices" v-else :key="item.id" class="content-card">
            <h3>{{ practiceTitle(item) }}</h3>
            <p>{{ practiceMeta(item) }}</p>
            <div class="chips">
              <span class="chip chip--active">{{ item.postStatus || item.status || 'PUBLISHED' }}</span>
              <span class="chip">{{ item.currentPeopleCount ?? item.takenCount ?? 1 }}/{{ item.expectedPeopleMax ?? item.capacity ?? 4 }} 人</span>
            </div>
          </article>
          <p v-if="practiceVisible && !practices.length" class="empty-state">还没有公开约练</p>
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

.social-card {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 58px;
  padding: 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-canvas;

  span {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    gap: 3px;
  }

  strong,
  em {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  em {
    color: $pen-mute;
    font-size: 13px;
    font-style: normal;
    font-weight: 800;
    line-height: $pen-lh;
  }

  a {
    display: grid;
    width: 36px;
    height: 36px;
    flex: none;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    place-items: center;
  }
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.chip,
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
.segment__item--active {
  border-color: $pen-ink;
  background: $pen-ink;
  color: $pen-on-primary;
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

.content-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
