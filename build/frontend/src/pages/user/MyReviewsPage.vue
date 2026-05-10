<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import { fetchMyReviews, deleteReview, type ReviewItem } from '@/api/review';

const router = useRouter();
const list = ref<ReviewItem[]>([]);
const loading = ref(true);

const reload = async () => {
  loading.value = true;
  try {
    list.value = await fetchMyReviews();
  } finally {
    loading.value = false;
  }
};

const onEdit = (id: number) => router.push(`/publish/review?editId=${id}`);
const onDelete = async (id: number) => {
  await showConfirmDialog({ title: '删除评价？', message: '删除后无法恢复' }).catch(() => {
    throw new Error('cancel');
  });
  await deleteReview(id);
  showSuccessToast('已删除');
  void reload();
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">我的评价</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">还没有发表评价</div>
    <article v-for="r in list" :key="r.id" class="item">
      <div class="item__head">
        <span class="item__target">[{{ r.targetType }}] #{{ r.targetId }}</span>
        <span class="item__date">{{ new Date(r.createdAt).toLocaleDateString() }}</span>
      </div>
      <p class="item__text">{{ r.text }}</p>
      <footer class="item__foot">
        <button class="btn-ghost" @click="onEdit(r.id)">编辑</button>
        <button class="btn-ghost btn-ghost--danger" @click="onDelete(r.id)">删除</button>
      </footer>
    </article>
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
.empty {
  text-align: center;
  padding: 60px 24px;
  color: var(--bd-text-secondary);
}
.item {
  margin: 8px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  &__head {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
  }
  &__target {
    color: var(--bd-primary);
  }
  &__date {
    color: var(--bd-text-secondary);
  }
  &__text {
    margin: 8px 0;
    font-size: 13px;
    line-height: 1.6;
  }
  &__foot {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
.btn-ghost {
  border: 1px solid var(--bd-border);
  background: #fff;
  color: var(--bd-text-secondary);
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
  &--danger {
    color: var(--bd-primary);
    border-color: var(--bd-primary);
  }
}
</style>
