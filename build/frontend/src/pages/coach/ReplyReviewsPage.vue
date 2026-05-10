<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { fetchReviews, type ReviewItem } from '@/api/review';
import { replyReview } from '@/api/coachOps';

const router = useRouter();
const list = ref<ReviewItem[]>([]);
const drafts = ref<Record<number, string>>({});
const loading = ref(true);

const reload = async () => {
  loading.value = true;
  try {
    // 简化：拉取 coach 维度评价 #1 ~ #5 作为占位
    const merged: ReviewItem[] = [];
    for (let i = 1; i <= 3; i += 1) {
      const data = await fetchReviews({ targetType: 'coach', targetId: i, page: 1, pageSize: 10 });
      merged.push(...data.list);
    }
    list.value = merged;
  } finally {
    loading.value = false;
  }
};

const onReply = async (id: number) => {
  const text = (drafts.value[id] ?? '').trim();
  if (!text) return;
  await replyReview({ reviewId: id, text });
  showSuccessToast('已回复');
  drafts.value[id] = '';
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">评价回复</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">暂无评价可回复</div>
    <article v-for="r in list" :key="r.id" class="item">
      <div class="item__head">
        <span class="item__author">{{ r.authorName }}</span>
        <span class="item__rating">★ {{ r.ratingAvg }}</span>
      </div>
      <p class="item__text">{{ r.text }}</p>
      <textarea v-model="drafts[r.id]" rows="2" class="ta" placeholder="客气、诚恳、就事论事…" />
      <div class="item__foot">
        <button class="btn-ghost" @click="router.push(`/coach/appeal?reviewId=${r.id}`)">申诉</button>
        <button class="btn-primary" :disabled="!(drafts[r.id] ?? '').trim()" @click="onReply(r.id)">回复</button>
      </div>
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
  padding: 60px;
  text-align: center;
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
    font-size: 13px;
  }
  &__author {
    font-weight: 600;
  }
  &__rating {
    color: #ffaa33;
  }
  &__text {
    margin: 8px 0;
    font-size: 13px;
  }
  &__foot {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
.ta {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  font-size: 13px;
  font-family: inherit;
  resize: none;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
  }
}
.btn-ghost {
  border: 1px solid var(--bd-border);
  background: #fff;
  color: var(--bd-text-secondary);
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 12px;
  cursor: pointer;
}
.btn-primary {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 5px 14px;
  font-size: 12px;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
</style>
