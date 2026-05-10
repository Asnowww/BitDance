<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchTopicPosts, type ContentPost } from '@/api/community';

const route = useRoute();
const router = useRouter();
const topic = computed(() => decodeURIComponent(route.params.name as string));
const list = ref<ContentPost[]>([]);

onMounted(async () => {
  const data = await fetchTopicPosts(topic.value);
  list.value = data.list;
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">#{{ topic }}</span>
    </header>
    <section class="grid">
      <article
        v-for="p in list"
        :key="p.id"
        class="card"
        @click="router.push(`/community/post/${p.id}`)"
      >
        <div class="card__cover">{{ p.style ?? '✨' }}</div>
        <p class="card__text">{{ p.text }}</p>
        <div class="card__foot">
          <span>{{ p.authorName }}</span>
          <span>♥ {{ p.likeCount }}</span>
        </div>
      </article>
    </section>
    <div v-if="!list.length" class="empty">该话题下暂无动态</div>
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
.grid {
  column-count: 2;
  column-gap: 8px;
  padding: 8px;
}
.card {
  break-inside: avoid;
  margin-bottom: 8px;
  padding: 10px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  &__cover {
    aspect-ratio: 4 / 5;
    border-radius: 8px;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    font-weight: 700;
  }
  &__text {
    margin: 8px 0 4px;
    font-size: 12px;
    line-height: 1.5;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  &__foot {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.empty {
  padding: 60px;
  text-align: center;
  color: var(--bd-text-secondary);
}
</style>
