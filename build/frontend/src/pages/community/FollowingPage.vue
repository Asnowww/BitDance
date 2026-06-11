<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchFollowers, fetchFollowing, toggleFollow, type FollowUser } from '@/api/community';

const route = useRoute();
const router = useRouter();
const tab = ref<'following' | 'fans'>(route.query.tab === 'fans' ? 'fans' : 'following');
const loading = ref(false);

const following = ref<FollowUser[]>([]);
const followers = ref<FollowUser[]>([]);
const visibleUsers = computed(() => (tab.value === 'fans' ? followers.value : following.value));

const load = async () => {
  loading.value = true;
  try {
    const [followingData, followerData] = await Promise.all([fetchFollowing(), fetchFollowers()]);
    following.value = followingData;
    followers.value = followerData;
  } finally {
    loading.value = false;
  }
};

const switchTab = (next: 'following' | 'fans') => {
  tab.value = next;
};

const toggle = async (u: FollowUser) => {
  const next = await toggleFollow(u.id);
  u.followed = next.following;
  u.followerCount = next.followerCount;
  if (tab.value === 'following' && !next.following) {
    following.value = following.value.filter((item) => item.id !== u.id);
  }
  followers.value = followers.value.map((item) => (item.id === u.id ? { ...item, followed: next.following } : item));
};

onMounted(load);
watch(
  () => route.query.tab,
  (next) => {
    tab.value = next === 'fans' ? 'fans' : 'following';
  }
);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="关注" :show-share="false" />

    <section class="pen-scroll">
      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'following' }" type="button" @click="switchTab('following')">关注 {{ following.length }}</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'fans' }" type="button" @click="switchTab('fans')">粉丝 {{ followers.length }}</button>
      </div>

      <p v-if="loading" class="empty">加载中</p>
      <p v-else-if="visibleUsers.length === 0" class="empty">暂无关注关系</p>
      <article v-for="u in visibleUsers" :key="u.id" class="user" @click="router.push(`/user/${u.id}`)">
        <span class="user__avatar" aria-hidden="true" />
        <div class="user__copy">
          <strong class="user__name">{{ u.name }}</strong>
          <span class="user__meta">{{ u.followerCount }} 粉丝 · {{ u.followeeCount }} 关注</span>
        </div>
        <button
          class="user__pill"
          :class="{ 'user__pill--solid': !u.followed }"
          type="button"
          @click.stop="toggle(u)"
        >
          {{ u.followed ? '已关注' : '关注' }}
        </button>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.seg {
  display: flex;
  gap: 8px;
  &__btn {
    flex: 1;
    height: 46px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
    &--on { background: $pen-ink; color: $pen-on-primary; }
  }
}

.empty {
  margin: 20px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__avatar { flex: none; width: 48px; height: 48px; border-radius: 999px; background: $pen-ink; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }

  &__pill {
    flex: none;
    height: 36px;
    padding: 8px 16px;
    border: 1px solid $pen-ink;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;

    &--solid { background: $pen-ink; color: $pen-on-primary; }
  }
}
</style>
