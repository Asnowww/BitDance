<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useFavoriteStore, type FavoriteTargetType } from '@/stores/favorite';

const router = useRouter();
const fav = useFavoriteStore();

const TYPES: Array<{ key: FavoriteTargetType; label: string }> = [
  { key: 'studio', label: '舞室' },
  { key: 'course', label: '课程' },
  { key: 'coach', label: '教练' },
  { key: 'workshop', label: 'Workshop' }
];

const activeType = ref<FavoriteTargetType>('studio');

const onItemClick = (type: FavoriteTargetType, id: number) => {
  if (type === 'workshop') router.push(`/workshop/${id}`);
  else router.push(`/${type}/${id}`);
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">我的收藏</span>
    </header>
    <nav class="tabs">
      <button
        v-for="t in TYPES"
        :key="t.key"
        class="tab"
        :class="{ active: activeType === t.key }"
        @click="activeType = t.key"
      >
        {{ t.label }} ({{ fav.groupedByType[t.key].length }})
      </button>
    </nav>
    <section class="list">
      <div v-if="fav.groupedByType[activeType].length === 0" class="empty">
        还没有收藏的{{ TYPES.find((x) => x.key === activeType)?.label }}
      </div>
      <article
        v-for="item in fav.groupedByType[activeType]"
        :key="`${item.targetType}-${item.targetId}`"
        class="item"
        @click="onItemClick(item.targetType, item.targetId)"
      >
        <div class="item__cover">{{ item.title.charAt(0) }}</div>
        <div class="item__body">
          <div class="item__title">{{ item.title }}</div>
          <div class="item__sub">{{ item.subtitle }}</div>
        </div>
        <button
          class="item__remove"
          @click.stop="
            fav.toggle({
              targetType: item.targetType,
              targetId: item.targetId,
              title: item.title,
              subtitle: item.subtitle
            })
          "
        >
          取消
        </button>
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
.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  overflow-x: auto;
}
.tab {
  flex: 1;
  border: none;
  background: none;
  padding: 12px 8px;
  font-size: 13px;
  color: var(--bd-text-secondary);
  cursor: pointer;
  white-space: nowrap;
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
  font-size: 13px;
}
.item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  &__cover {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    background: linear-gradient(135deg, #ffd2da, #ff2442);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
  }
  &__body {
    flex: 1;
    min-width: 0;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
  }
  &__sub {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__remove {
    border: 1px solid var(--bd-border);
    background: #fff;
    border-radius: 999px;
    padding: 4px 12px;
    font-size: 12px;
    color: var(--bd-text-secondary);
    cursor: pointer;
  }
}
</style>
