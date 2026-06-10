<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  BookOpen,
  ChevronLeft,
  Music2,
  PencilLine,
  Play,
  Plus,
  Settings,
  User
} from 'lucide-vue-next';
import { fetchMyCommunityPosts, fetchUserPractices, fetchUserReviews } from '@/api/userHome';
import type { UserContentPost, UserPracticePost, UserReviewItem } from '@/api/userHome';
import { fetchFollowers, fetchFollowing } from '@/api/community';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const user = useUserStore();

type ContentTab = 'posts' | 'reviews' | 'practices';

const socials = [
  { platform: '抖音', account: '@urban_lili', state: '公开', icon: Music2, dark: true },
  { platform: '小红书', account: '小李练舞日记', state: '公开', icon: BookOpen, dark: false },
  { platform: 'B站', account: '未绑定', state: '仅自己可见', icon: Play, dark: false }
];

const normalizeTab = (value: unknown): ContentTab =>
  value === 'reviews' || value === 'practices' ? value : 'posts';

const activeTab = ref<ContentTab>(normalizeTab(route.query.tab));
const posts = ref<UserContentPost[]>([]);
const reviews = ref<UserReviewItem[]>([]);
const practices = ref<UserPracticePost[]>([]);
const totals = ref({ posts: 0, reviews: 0, practices: 0 });
const followTotals = ref({ following: 0, followers: 0 });
const loading = ref(false);

const profileName = computed(() => user.profile?.nickname || '小李');
const profileId = computed(() => user.profile?.id || 1);
const stats = computed(() => [
  { value: String(totals.value.posts), label: '动态' },
  { value: String(followTotals.value.following), label: '关注', path: '/community/following?tab=following' },
  { value: String(followTotals.value.followers), label: '粉丝', path: '/community/following?tab=fans' },
  { value: String(totals.value.reviews), label: '评价' }
]);

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
    const [postResp, reviewResp, practiceResp, followingResp, followerResp] = await Promise.all([
      fetchMyCommunityPosts(1, 20),
      fetchUserReviews(profileId.value, 1, 20),
      fetchUserPractices(profileId.value),
      fetchFollowing().catch(() => []),
      fetchFollowers().catch(() => [])
    ]);
    posts.value = postResp.list ?? [];
    reviews.value = reviewResp.list ?? [];
    practices.value = practiceResp ?? [];
    totals.value = {
      posts: postResp.total ?? posts.value.length,
      reviews: reviewResp.total ?? reviews.value.length,
      practices: practices.value.length
    };
    followTotals.value = {
      following: followingResp.length,
      followers: followerResp.length
    };
  } finally {
    loading.value = false;
  }
};

onMounted(loadHomeData);
watch(
  () => route.query.tab,
  (next) => {
    activeTab.value = normalizeTab(next);
  }
);
</script>

<template>
  <main class="profile-page profile-page--self">
    <header class="topbar">
      <button type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="24" />
      </button>
      <h1>我的个人主页</h1>
      <button type="button" aria-label="主页设置" @click="router.push('/me/profile')">
        <Settings :size="23" />
      </button>
    </header>

    <section class="profile-scroll">
      <section class="hero-card">
        <div class="avatar">
          <User :size="36" />
        </div>
        <div class="hero-card__copy">
          <h2>{{ profileName }}</h2>
          <p>@bitdance_lili · 零基础韩舞爱好者</p>
          <div class="chips">
            <span class="chip chip--active">韩舞</span>
            <span class="chip">Jazz</span>
            <span class="chip">北京</span>
          </div>
        </div>
        <button class="edit-btn" type="button" aria-label="编辑资料" @click="router.push('/me/profile')">
          <PencilLine :size="22" />
        </button>
      </section>

      <section class="stats" aria-label="主页数据">
        <button
          v-for="item in stats"
          :key="item.label"
          class="stats__item"
          type="button"
          @click="item.path ? router.push(item.path) : undefined"
        >
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </button>
      </section>

      <section class="section">
        <h2>社交账号</h2>
        <article v-for="item in socials" :key="item.platform" class="social-row">
          <span class="social-row__icon" :class="{ 'social-row__icon--dark': item.dark }">
            <component :is="item.icon" :size="23" />
          </span>
          <span class="social-row__copy">
            <strong>{{ item.platform }}</strong>
            <em>{{ item.account }}</em>
          </span>
          <span class="state" :class="{ 'state--active': item.state === '公开' }">{{ item.state }}</span>
        </article>
        <button class="bind-btn" type="button" @click="router.push('/me/profile')">
          <Plus :size="20" />
          <span>管理绑定</span>
        </button>
      </section>

      <section class="section">
        <h2>他人视角预览</h2>
        <div class="preview-card">
          <strong>仅展示标记为公开的社交账号</strong>
          <div class="preview-card__chips">
            <span>抖音 @urban_lili</span>
            <span>小红书 小李练舞日记</span>
          </div>
          <p>按平台单独控制可见性</p>
        </div>
      </section>

      <nav class="segment" aria-label="主页内容筛选">
        <button class="segment__item" :class="{ 'segment__item--active': activeTab === 'posts' }" type="button" @click="activeTab = 'posts'">
          动态
        </button>
        <button class="segment__item" :class="{ 'segment__item--active': activeTab === 'reviews' }" type="button" @click="activeTab = 'reviews'">
          评价
        </button>
        <button class="segment__item" :class="{ 'segment__item--active': activeTab === 'practices' }" type="button" @click="activeTab = 'practices'">
          约练
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
            <div class="chips">
              <span class="chip">{{ item.visibility === 'private' ? '仅自己' : item.visibility === 'followers' ? '粉丝可见' : '公开' }}</span>
              <span class="chip">{{ item.likeCount ?? 0 }} 赞</span>
              <span class="chip">{{ item.commentCount ?? 0 }} 评论</span>
            </div>
          </article>
          <p v-if="!posts.length" class="empty-state">还没有发布动态</p>
        </template>
        <template v-else-if="activeTab === 'reviews'">
          <article v-for="item in reviews" :key="item.id" class="content-card">
            <h3>{{ reviewTarget(item) }} · {{ reviewScore(item) }}</h3>
            <p>{{ item.contentText }}</p>
            <div class="chips">
              <span class="chip" :class="{ 'chip--active': item.isVerified }">{{ item.isVerified ? '已验证' : '普通评价' }}</span>
            </div>
          </article>
          <p v-if="!reviews.length" class="empty-state">还没有发布评价</p>
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
          <p v-if="!practices.length" class="empty-state">还没有发布约练</p>
        </template>
      </section>
    </section>

    <footer class="save-bar">
      <button type="button" @click="router.push('/me/profile')">保存主页设置</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.profile-page {
  @include pen-page;
  min-height: 100%;
  padding-bottom: calc(76px + var(--app-tabbar-offset, 0px));
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

.profile-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px;
}

.hero-card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 126px;
  padding: 14px;
  border-radius: 18px;
  background: $pen-soft;

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

.edit-btn {
  display: grid;
  flex: none;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  cursor: pointer;
  place-items: center;
}

.chips,
.preview-card__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.chip,
.preview-card__chips span,
.state {
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
.preview-card__chips span,
.state--active {
  border-color: $pen-ink;
  background: $pen-ink;
  color: $pen-on-primary;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  min-height: 58px;
  gap: 8px;

  &__item {
    display: flex;
    min-width: 0;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    border: 0;
    background: transparent;
    color: $pen-ink;
    cursor: pointer;

    strong {
      font-size: 22px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 800;
      line-height: $pen-lh;
    }
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

.bind-btn,
.save-bar button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 0;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 15px;
  font-weight: 900;
  line-height: $pen-lh;
  cursor: pointer;
}

.bind-btn {
  height: 46px;
}

.preview-card {
  min-height: 94px;
  padding: 12px;
  border-radius: 14px;
  background: $pen-soft;

  strong {
    font-size: 13px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    margin: 8px 0 0;
    color: $pen-mute;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.segment {
  display: flex;
  gap: 8px;
}

.segment__item {
  display: inline-flex;
  min-width: 70px;
  min-height: 34px;
  align-items: center;
  justify-content: center;
  padding: 7px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 900;
  line-height: $pen-lh;
  white-space: nowrap;
  cursor: pointer;
}

.segment__item--active {
  border-color: $pen-ink;
  background: $pen-ink;
  color: $pen-on-primary;
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

.save-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 90;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px;
  border-top: 1px solid $pen-hairline;
  background: $pen-canvas;
  box-sizing: border-box;

  button {
    width: 100%;
    height: 48px;
  }
}

@media (max-width: 360px) {
  .hero-card {
    align-items: flex-start;
  }

  .avatar {
    width: 58px;
    height: 58px;
  }

  .hero-card__copy h2 {
    font-size: 24px;
  }
}
</style>
