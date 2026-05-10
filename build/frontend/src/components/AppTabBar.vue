<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showActionSheet } from 'vant';

const route = useRoute();
const router = useRouter();
const activeTab = computed(() => (route.meta?.tab as string) ?? '');

const tabs = [
  { key: 'home', label: '首页', icon: '🏠', to: '/home' },
  { key: 'practice', label: '约练', icon: '💃', to: '/practice' },
  { key: 'growth', label: '成长', icon: '📈', to: '/growth' },
  { key: 'me', label: '我的', icon: '👤', to: '/me' }
];

const sheetVisible = ref(false);

const onPublishClick = () => {
  showActionSheet({
    title: '发布',
    description: '选择想发布的内容',
    cancelText: '取消',
    actions: [
      { name: '训练打卡', subname: '记录今天练了多久', callback: () => router.push('/publish/checkin') },
      { name: '发起约练', subname: '找个搭子一起练', callback: () => router.push('/publish/practice') },
      { name: '写评价', subname: '聊聊舞室、老师或课程', callback: () => router.push('/publish/review') }
    ]
  });
  sheetVisible.value = true;
};

const goTab = (to: string) => {
  if (route.path === to) return;
  router.push(to);
};
</script>

<template>
  <nav class="tabbar">
    <button
      v-for="tab in tabs.slice(0, 2)"
      :key="tab.key"
      class="tabbar__item"
      :class="{ active: activeTab === tab.key }"
      @click="goTab(tab.to)"
    >
      <span class="tabbar__icon">{{ tab.icon }}</span>
      <span class="tabbar__label">{{ tab.label }}</span>
    </button>
    <button class="tabbar__publish" aria-label="发布" @click="onPublishClick">
      <span class="tabbar__publish-inner">+</span>
    </button>
    <button
      v-for="tab in tabs.slice(2, 4)"
      :key="tab.key"
      class="tabbar__item"
      :class="{ active: activeTab === tab.key }"
      @click="goTab(tab.to)"
    >
      <span class="tabbar__icon">{{ tab.icon }}</span>
      <span class="tabbar__label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<style lang="scss" scoped>
.tabbar {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 0;
  width: 100%;
  max-width: 480px;
  height: calc(56px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: var(--bd-surface);
  border-top: 1px solid var(--bd-border);
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: center;
  z-index: 100;

  &__item {
    background: none;
    border: none;
    padding: 6px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    color: var(--bd-text-secondary);
    font-size: 11px;
    line-height: 1;
    cursor: pointer;
    transition: color 0.18s;
    &.active {
      color: var(--bd-primary);
    }
  }
  &__icon {
    font-size: 22px;
    line-height: 1;
  }
  &__label {
    font-size: 11px;
  }
  &__publish {
    width: 52px;
    height: 52px;
    border: none;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--bd-primary), var(--bd-primary-dark));
    color: #fff;
    font-size: 28px;
    line-height: 1;
    margin: 0 auto;
    transform: translateY(-14px);
    box-shadow: 0 6px 16px rgba(255, 36, 66, 0.35);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    &-inner {
      transform: translateY(-1px);
    }
    &:active {
      transform: translateY(-12px) scale(0.96);
    }
  }
}
</style>
