<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, PencilLine, Settings, User } from 'lucide-vue-next';
import { showFailToast, showSuccessToast } from 'vant';
import { fetchUserPosts, fetchUserPractices, fetchUserReviews } from '@/api/userHome';
import type { UserContentPost, UserPracticePost, UserReviewItem } from '@/api/userHome';
import { fetchMySocialAccounts, updateSocialAccount, type SocialAccount } from '@/api/social';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

type ContentTab = 'posts' | 'reviews' | 'practices';

const activeTab = ref<ContentTab>('posts');
const posts = ref<UserContentPost[]>([]);
const reviews = ref<UserReviewItem[]>([]);
const practices = ref<UserPracticePost[]>([]);
const socialAccounts = ref<SocialAccount[]>([]);
const totals = ref({ posts: 0, reviews: 0, practices: 0 });
const loading = ref(false);

const profileName = computed(() => user.detail?.nickname || user.profile?.nickname || '未命名用户');
const profileId = computed(() => user.profile?.id || user.detail?.userId || 0);
const primaryPreference = computed(() => {
  const styles = user.detail?.styles ?? [];
  return styles.find((item) => item.isPrimary) ?? styles[0];
});
const profileMeta = computed(() =>
  [
    user.detail?.cityId ? `城市 ${user.detail.cityId}` : null,
    user.detail?.currentLevel || null,
    user.detail?.learningGoal || null
  ]
    .filter(Boolean)
    .join(' · ')
);
const publicSocials = computed(() => socialAccounts.value.filter((item) => item.isPublic));
const stats = computed(() => [
  { value: String(totals.value.posts), label: '动态' },
  { value: String(totals.value.reviews), label: '评价' },
  { value: String(totals.value.practices), label: '约练' },
  { value: String(socialAccounts.value.length), label: '社交' }
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
    const [profileResult, socialResult] = await Promise.allSettled([
      user.refreshProfile(),
      fetchMySocialAccounts()
    ]);
    if (socialResult.status === 'fulfilled') {
      socialAccounts.value = Array.isArray(socialResult.value) ? socialResult.value : [];
    }
    if (profileResult.status === 'rejected') {
      showFailToast('资料刷新失败，已保留本地资料');
    }

    if (!profileId.value) return;
    const [postResp, reviewResp, practiceResp] = await Promise.allSettled([
      fetchUserPosts(profileId.value, 1, 20),
      fetchUserReviews(profileId.value, 1, 20),
      fetchUserPractices(profileId.value)
    ]);

    if (postResp.status === 'fulfilled') {
      posts.value = postResp.value.list ?? [];
      totals.value.posts = postResp.value.total ?? posts.value.length;
    }
    if (reviewResp.status === 'fulfilled') {
      reviews.value = reviewResp.value.list ?? [];
      totals.value.reviews = reviewResp.value.total ?? reviews.value.length;
    }
    if (practiceResp.status === 'fulfilled') {
      practices.value = practiceResp.value ?? [];
      totals.value.practices = practices.value.length;
    }
  } finally {
    loading.value = false;
  }
};

const toggleSocialVisibility = async (account: SocialAccount) => {
  try {
    const updated = await updateSocialAccount(account.id, !account.isPublic);
    socialAccounts.value = socialAccounts.value.map((item) => (item.id === updated.id ? updated : item));
    showSuccessToast(updated.isPublic ? '已公开展示' : '已设为仅自己可见');
  } catch {
    showFailToast('社交账号状态更新失败');
  }
};

onMounted(loadHomeData);
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
          <p>{{ profileMeta || '资料暂未完善' }}</p>
          <div class="chips">
            <span v-if="primaryPreference" class="chip chip--active">主舞种 {{ primaryPreference.danceStyleId }}</span>
            <span v-if="user.detail?.gender" class="chip">{{ user.detail.gender }}</span>
            <span v-if="user.detail?.currentLevel" class="chip">{{ user.detail.currentLevel }}</span>
          </div>
        </div>
        <button class="edit-btn" type="button" aria-label="编辑资料" @click="router.push('/me/profile')">
          <PencilLine :size="22" />
        </button>
      </section>

      <section class="stats" aria-label="主页数据">
        <div v-for="item in stats" :key="item.label" class="stats__item">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </section>

      <section class="section">
        <h2>社交账号</h2>
        <p v-if="!socialAccounts.length" class="empty-state">后端暂无社交账号绑定记录。</p>
        <article v-for="account in socialAccounts" :key="account.id" class="social-row">
          <span class="social-row__copy">
            <strong>{{ account.platform }}</strong>
            <em>{{ account.accountName }}</em>
          </span>
          <button
            class="state"
            :class="{ 'state--active': account.isPublic }"
            type="button"
            @click="toggleSocialVisibility(account)"
          >
            {{ account.isPublic ? '公开' : '仅自己' }}
          </button>
        </article>
      </section>

      <section class="section">
        <h2>他人视角预览</h2>
        <div class="preview-card">
          <strong>公开社交账号：{{ publicSocials.length }} 个</strong>
          <div v-if="publicSocials.length" class="chips">
            <span v-for="account in publicSocials" :key="account.id" class="chip chip--active">
              {{ account.platform }} {{ account.accountName }}
            </span>
          </div>
          <p>公开主页只展示后端返回的公开账号和公开内容。</p>
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
        <p v-if="loading" class="empty-state">加载中...</p>
        <template v-else-if="activeTab === 'posts'">
          <article v-for="item in posts" :key="item.id" class="content-card">
            <h3>最近动态</h3>
            <p>{{ postText(item) }}</p>
            <div v-if="postTopics(item).length" class="chips">
              <span v-for="topic in postTopics(item)" :key="topic" class="chip">{{ topic }}</span>
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

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.chip,
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

.state {
  cursor: pointer;
}

.chip--active,
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
</style>
