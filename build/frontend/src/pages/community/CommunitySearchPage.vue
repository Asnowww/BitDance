<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { searchContent, type ContentPost } from '@/api/community';

const router = useRouter();
const q = ref('');
const list = ref<ContentPost[]>([]);
const searched = ref(false);

const onSearch = async () => {
  if (!q.value.trim()) return;
  const data = await searchContent(q.value.trim());
  list.value = data.list;
  searched.value = true;
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <input
        v-model="q"
        class="input"
        placeholder="搜动态、舞种、用户、话题"
        @keyup.enter="onSearch"
      />
      <button class="btn" @click="onSearch">搜索</button>
    </header>
    <div v-if="searched && !list.length" class="empty">没有匹配结果</div>
    <article
      v-for="p in list"
      :key="p.id"
      class="item"
      @click="router.push(`/community/post/${p.id}`)"
    >
      <div class="item__cover">{{ p.style ?? '✨' }}</div>
      <div class="item__body">
        <div class="item__text">{{ p.text }}</div>
        <div class="item__meta">{{ p.authorName }} · ♥ {{ p.likeCount }}</div>
      </div>
    </article>
  </div>
</template>

<style lang="scss" scoped>
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: none;
  border-radius: 999px;
  background: #f4f4f4;
  font-size: 13px;
  outline: none;
}
.btn {
  border: none;
  background: none;
  color: var(--bd-primary);
  font-size: 13px;
  cursor: pointer;
}
.empty {
  padding: 60px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.item {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  cursor: pointer;
  &__cover {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
  }
  &__text {
    font-size: 13px;
    line-height: 1.5;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  &__meta {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
</style>
