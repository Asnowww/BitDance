<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { fetchMessages, markRead, markAllRead, type MessageItem, type MessageCategory } from '@/api/message';

const router = useRouter();
const list = ref<MessageItem[]>([]);
const loading = ref(true);
const activeCat = ref<MessageCategory | 'all'>('all');

const TABS: Array<{ key: MessageCategory | 'all'; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'system', label: '系统' },
  { key: 'practice', label: '约练' },
  { key: 'review', label: '评价' },
  { key: 'trial', label: '试听' }
];

const reload = async () => {
  loading.value = true;
  try {
    list.value = await fetchMessages();
  } finally {
    loading.value = false;
  }
};

const filtered = computed(() =>
  activeCat.value === 'all' ? list.value : list.value.filter((it) => it.category === activeCat.value)
);

const unreadCount = computed(() => list.value.filter((it) => !it.read).length);

const onItemClick = async (it: MessageItem) => {
  if (!it.read) {
    await markRead(it.id);
    void reload();
  }
};

const onMarkAll = async () => {
  await markAllRead();
  showSuccessToast('已全部标记为已读');
  void reload();
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">消息中心</span>
      <button v-if="unreadCount > 0" class="all" @click="onMarkAll">全部已读</button>
    </header>
    <nav class="tabs">
      <button
        v-for="t in TABS"
        :key="t.key"
        class="tab"
        :class="{ active: activeCat === t.key }"
        @click="activeCat = t.key"
      >
        {{ t.label }}
      </button>
    </nav>
    <section class="list">
      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="!filtered.length" class="empty">暂无消息</div>
      <article v-for="m in filtered" :key="m.id" class="item" :class="{ unread: !m.read }" @click="onItemClick(m)">
        <div class="item__head">
          <span class="item__title">{{ m.title }}</span>
          <span class="item__time">{{ new Date(m.ts).toLocaleString() }}</span>
        </div>
        <p class="item__body">{{ m.body }}</p>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 24px;
}
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    flex: 1;
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.all {
  border: none;
  background: none;
  color: var(--bd-primary);
  font-size: 13px;
  cursor: pointer;
}
.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  overflow-x: auto;
}
.tab {
  flex-shrink: 0;
  border: none;
  background: none;
  padding: 10px 16px;
  font-size: 13px;
  color: var(--bd-text-secondary);
  cursor: pointer;
  &.active {
    color: var(--bd-primary);
    font-weight: 600;
    border-bottom: 2px solid var(--bd-primary);
  }
}
.list {
  padding: 8px 12px;
}
.empty {
  text-align: center;
  padding: 60px 24px;
  color: var(--bd-text-secondary);
}
.item {
  background: #fff;
  border-radius: 12px;
  padding: 12px 14px;
  margin-bottom: 8px;
  position: relative;
  &.unread::before {
    content: '';
    position: absolute;
    top: 14px;
    left: 6px;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--bd-primary);
  }
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
  }
  &__time {
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__body {
    margin: 6px 0 0;
    font-size: 13px;
    color: var(--bd-text);
  }
}
</style>
