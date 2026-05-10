<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchTopics } from '@/api/community';

const router = useRouter();
const list = ref<Array<{ name: string; count: number; hot: boolean }>>([]);

onMounted(async () => {
  list.value = await fetchTopics();
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">话题广场</span>
    </header>
    <section class="list">
      <article
        v-for="t in list"
        :key="t.name"
        class="item"
        @click="router.push(`/community/topic/${encodeURIComponent(t.name)}`)"
      >
        <span class="item__name">#{{ t.name }}</span>
        <span class="item__count">{{ t.count }} 条</span>
        <span v-if="t.hot" class="item__hot">🔥</span>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
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
.list {
  padding: 8px 12px;
}
.item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  &__name {
    flex: 1;
    font-size: 14px;
    color: var(--bd-primary);
    font-weight: 600;
  }
  &__count {
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
</style>
