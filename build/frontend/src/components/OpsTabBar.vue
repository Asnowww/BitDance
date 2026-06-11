<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LayoutGrid, CalendarDays, ScanLine, MessageSquare, UserRound } from 'lucide-vue-next';

interface TabItem {
  key: string;
  label: string;
  icon: typeof LayoutGrid;
  to: string;
  /** 命中该 tab 的路由前缀 */
  match: string[];
}

const route = useRoute();
const router = useRouter();

const tabs: TabItem[] = [
  {
    key: 'workspace',
    label: '工作台',
    icon: LayoutGrid,
    to: '/coach/dashboard',
    match: [
      '/coach/dashboard',
      '/coach/studio-claim',
      '/coach/coaches',
      '/coach/invitations',
      '/coach/certification',
      '/coach/settlement',
      '/coach/platform'
    ]
  },
  {
    key: 'schedule',
    label: '课表',
    icon: CalendarDays,
    to: '/coach/schedule',
    match: ['/coach/schedule', '/coach/courses', '/coach/course-edit', '/coach/schedule-edit']
  },
  {
    key: 'orders',
    label: '订单',
    icon: ScanLine,
    to: '/coach/orders',
    match: ['/coach/orders', '/coach/checkin', '/coach/workshops', '/coach/workshop-create']
  },
  {
    key: 'reviews',
    label: '评价',
    icon: MessageSquare,
    to: '/coach/replies',
    match: ['/coach/replies', '/coach/appeal']
  },
  { key: 'me', label: '我的', icon: UserRound, to: '/coach/me', match: ['/coach/me'] }
];

const activeKey = computed(() => {
  const hit = tabs.find((t) => t.match.some((m) => route.path.startsWith(m)));
  return hit?.key ?? 'workspace';
});

const goTab = (to: string) => {
  if (route.path === to) return;
  router.push(to);
};
</script>

<template>
  <nav class="ops-tabbar" aria-label="运营端底部导航">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="ops-tabbar__item"
      :class="{ active: activeKey === tab.key }"
      type="button"
      @click="goTab(tab.to)"
    >
      <component :is="tab.icon" :size="19" :stroke-width="2.2" />
      <span class="ops-tabbar__label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<style lang="scss" scoped>
.ops-tabbar {
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

.ops-tabbar__item {
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

.ops-tabbar__label {
  font-size: 11px;
  line-height: 1.25;
  font-weight: 700;
  letter-spacing: 0;
}
</style>
