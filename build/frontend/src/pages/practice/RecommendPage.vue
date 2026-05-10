<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchPracticeRecommend } from '@/api/buddy';
import { fetchMyBuddies, type Buddy } from '@/api/buddy';
import type { PracticePost } from '@/api/practice';

const router = useRouter();
const recommended = ref<PracticePost[]>([]);
const buddies = ref<Buddy[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    [recommended.value, buddies.value] = await Promise.all([
      fetchPracticeRecommend(),
      fetchMyBuddies()
    ]);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">推荐与搭子</span>
    </header>
    <section class="block">
      <h3>我的搭子 ({{ buddies.length }})</h3>
      <div v-if="!buddies.length" class="empty">完成一次约练后会自动沉淀搭子</div>
      <div class="buddies">
        <div v-for="b in buddies" :key="b.userId" class="buddy">
          <span class="avatar">{{ b.name.charAt(0) }}</span>
          <div class="buddy__name">{{ b.name }}</div>
          <div class="buddy__sub">{{ b.sharedStyles.join(' · ') || '舞种待补' }}</div>
          <div class="buddy__sub">{{ b.pastSessions }} 次同练</div>
        </div>
      </div>
    </section>
    <section class="block">
      <h3>为你推荐</h3>
      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="!recommended.length" class="empty">没有合适的推荐，去广场看看吧</div>
      <article
        v-for="p in recommended"
        :key="p.id"
        class="card"
        @click="router.push(`/practice/${p.id}`)"
      >
        <div class="card__title">{{ p.title }}</div>
        <div class="card__meta">
          {{ p.style }} · {{ p.level }} · {{ p.date }} {{ p.time }}
        </div>
        <div class="card__loc">📍 {{ p.location }}</div>
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
.block {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 12px;
    font-size: 14px;
  }
}
.empty {
  text-align: center;
  padding: 24px;
  color: var(--bd-text-secondary);
  font-size: 12px;
}
.buddies {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.buddy {
  flex-shrink: 0;
  width: 80px;
  text-align: center;
  &__name {
    margin-top: 6px;
    font-size: 13px;
    font-weight: 600;
  }
  &__sub {
    margin-top: 2px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin: 0 auto;
  background: linear-gradient(135deg, #ffd2da, #ff7799);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card {
  padding: 12px;
  background: #fafafa;
  border-radius: 10px;
  margin-bottom: 8px;
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
  &__loc {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
</style>
