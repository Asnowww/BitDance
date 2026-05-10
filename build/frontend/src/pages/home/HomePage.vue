<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

interface StudioCard {
  id: number;
  name: string;
  cover: string;
  distanceKm: number;
  ratingAvg: number;
  reviewCount: number;
  topStyles: string[];
}

const list = ref<StudioCard[]>([]);
const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    const data = await request.get<unknown, { list: StudioCard[] }>('/studios/nearby', {
      params: { page: 1, pageSize: 20 }
    });
    list.value = data.list;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="home">
    <header class="home__header">
      <div class="home__brand">BitDance</div>
      <div class="home__sub">找舞室 · 约搭子 · 记成长</div>
    </header>
    <section class="home__list">
      <article v-for="s in list" :key="s.id" class="card">
        <div class="card__cover" />
        <div class="card__body">
          <div class="card__title">{{ s.name }}</div>
          <div class="card__meta">
            <span>{{ s.distanceKm }}km</span>
            <span>·</span>
            <span>★ {{ s.ratingAvg }} ({{ s.reviewCount }})</span>
          </div>
          <div class="card__tags">
            <span v-for="t in s.topStyles" :key="t" class="tag">{{ t }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.home {
  padding: 16px 12px 80px;
  &__header {
    padding: 8px 4px 16px;
  }
  &__brand {
    font-size: 22px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__list {
    column-count: 2;
    column-gap: 8px;
  }
}
.card {
  break-inside: avoid;
  margin-bottom: 8px;
  background: var(--bd-surface);
  border-radius: var(--bd-radius-md);
  overflow: hidden;
  &__cover {
    width: 100%;
    aspect-ratio: 3 / 4;
    background: linear-gradient(135deg, #ffd2da, #ff2442);
  }
  &__body {
    padding: 8px 10px 10px;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
    line-height: 1.3;
  }
  &__meta {
    margin-top: 4px;
    display: flex;
    gap: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__tags {
    margin-top: 6px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}
.tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(255, 36, 66, 0.08);
  color: var(--bd-primary);
}
</style>
