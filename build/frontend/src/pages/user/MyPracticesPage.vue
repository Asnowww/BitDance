<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchPractices, type PracticePost } from '@/api/practice';

const router = useRouter();
const list = ref<PracticePost[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const data = await fetchPractices({ page: 1, pageSize: 100 });
    list.value = data.list.filter((it) => it.authorId === 999);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">我的约练</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">还没发布过约练</div>
    <article v-for="p in list" :key="p.id" class="item" @click="router.push(`/practice/${p.id}`)">
      <div class="item__title">{{ p.title }}</div>
      <div class="item__meta">{{ p.date }} {{ p.time }} · {{ p.takenCount }}/{{ p.capacity }} · {{ p.status }}</div>
    </article>
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
.empty {
  padding: 60px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.item {
  margin: 8px 12px;
  padding: 12px 14px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  &__title {
    font-size: 14px;
    font-weight: 600;
  }
  &__meta {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
</style>
