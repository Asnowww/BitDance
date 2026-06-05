<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';

type NikeIconName = 'activity' | 'bell' | 'search' | 'sparkles' | 'user' | 'users';

interface TabItem {
  key: string;
  label: string;
  icon: NikeIconName;
  to: string;
}

const route = useRoute();
const router = useRouter();

const activeTab = computed(() => {
  if (route.meta?.tab) return route.meta.tab as string;
  if (route.path.startsWith('/practice')) return 'practice';
  if (route.path.startsWith('/workshop') || route.path.startsWith('/community')) return 'activity';
  if (route.path.startsWith('/growth') || route.path.startsWith('/me/works') || route.path.startsWith('/me/goal')) {
    return 'growth';
  }
  if (
    route.path.startsWith('/me') ||
    route.path.startsWith('/coach') ||
    route.path.startsWith('/messages') ||
    route.path.startsWith('/favorites')
  ) {
    return 'me';
  }
  return 'home';
});

const tabs: TabItem[] = [
  { key: 'home', label: '发现', icon: 'search', to: '/home' },
  { key: 'practice', label: '约练', icon: 'users', to: '/practice' },
  { key: 'activity', label: '活动', icon: 'sparkles', to: '/workshops' },
  { key: 'growth', label: '成长', icon: 'activity', to: '/growth' },
  { key: 'me', label: '我的', icon: 'user', to: '/me' }
];

const goTab = (to: string) => {
  if (route.path === to) return;
  router.push(to);
};
</script>

<template>
  <nav class="tabbar" aria-label="底部导航">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="tabbar__item"
      :class="{ active: activeTab === tab.key }"
      type="button"
      @click="goTab(tab.to)"
    >
      <NikeIcon :name="tab.icon" :size="19" />
      <span class="tabbar__label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<style lang="scss" scoped>
.tabbar {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 100;
  width: 100%;
  max-width: 480px;
  height: calc(72px + env(safe-area-inset-bottom));
  padding: 8px 14px calc(8px + env(safe-area-inset-bottom));
  border-top: 1px solid #e5e5e5;
  background: #ffffff;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 4px;
  transform: translateX(-50%);
}

.tabbar__item {
  height: 56px;
  border: 0;
  border-radius: 999px;
  background: #ffffff;
  color: #707072;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  cursor: pointer;

  &.active {
    background: #111111;
    color: #ffffff;
  }
}

.tabbar__label {
  font-size: 11px;
  line-height: 1.25;
  font-weight: 700;
  letter-spacing: 0;
}
</style>
