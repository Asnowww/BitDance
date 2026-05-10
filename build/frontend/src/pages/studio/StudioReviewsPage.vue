<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import StarRating from '@/components/StarRating.vue';
import { fetchReviews, REVIEW_DIMENSIONS, type ReviewItem, type ReviewTargetType } from '@/api/review';

const route = useRoute();
const router = useRouter();
const targetType = (route.query.targetType as ReviewTargetType) || 'studio';
const targetId = Number(route.params.id);

const list = ref<ReviewItem[]>([]);
const summary = ref<{ ratingAvg: number; reviewCount: number; dimensionAvg: Record<string, number> }>({
  ratingAvg: 0,
  reviewCount: 0,
  dimensionAvg: {}
});
const loading = ref(true);
const sort = ref<'latest' | 'helpful' | 'verified'>('latest');

const dims = computed(() => REVIEW_DIMENSIONS[targetType] ?? []);

const reload = async () => {
  loading.value = true;
  try {
    const data = await fetchReviews({ targetType, targetId, sort: sort.value, page: 1, pageSize: 50 });
    list.value = data.list;
    summary.value = data.summary;
  } finally {
    loading.value = false;
  }
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">评价 ({{ summary.reviewCount }})</span>
      <button class="write" @click="router.push(`/publish/review?targetType=${targetType}&targetId=${targetId}`)">
        写评价
      </button>
    </header>
    <section class="summary">
      <div class="summary__avg">
        <div class="summary__num">{{ summary.ratingAvg }}</div>
        <StarRating :model-value="Math.round(summary.ratingAvg)" readonly :size="16" />
        <div class="summary__count">{{ summary.reviewCount }} 条评价</div>
      </div>
      <div class="summary__dims">
        <div v-for="d in dims" :key="d.key" class="dim">
          <span class="dim__label">{{ d.label }}</span>
          <div class="dim__bar">
            <div class="dim__bar-fill" :style="{ width: `${(summary.dimensionAvg[d.key] ?? 0) * 20}%` }" />
          </div>
          <span class="dim__num">{{ summary.dimensionAvg[d.key] ?? '-' }}</span>
        </div>
      </div>
    </section>
    <nav class="sort">
      <button class="sort__item" :class="{ active: sort === 'latest' }" @click="sort = 'latest'; reload()">最新</button>
      <button class="sort__item" :class="{ active: sort === 'helpful' }" @click="sort = 'helpful'; reload()">最有帮助</button>
      <button class="sort__item" :class="{ active: sort === 'verified' }" @click="sort = 'verified'; reload()">已验证优先</button>
    </nav>
    <section class="list">
      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="!list.length" class="empty">暂无评价</div>
      <article v-for="r in list" :key="r.id" class="item">
        <div class="item__head">
          <div class="avatar">{{ r.authorName.charAt(0) }}</div>
          <div class="item__author">
            <div class="item__name">
              {{ r.authorName }}
              <span v-if="r.isVerified" class="verified">✓ {{ r.verifiedSourceType }}</span>
            </div>
            <div class="item__date">{{ new Date(r.createdAt).toLocaleDateString() }}</div>
          </div>
          <StarRating :model-value="Math.round(r.ratingAvg)" readonly :size="14" />
        </div>
        <p class="item__text">{{ r.text }}</p>
        <div class="item__foot">
          <span class="helpful">👍 {{ r.helpfulCount }}</span>
        </div>
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
.write {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
}
.summary {
  background: #fff;
  padding: 16px;
  display: flex;
  gap: 20px;
  &__avg {
    text-align: center;
    width: 96px;
  }
  &__num {
    font-size: 32px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__count {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__dims {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
}
.dim {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  &__label {
    width: 72px;
    color: var(--bd-text-secondary);
  }
  &__bar {
    flex: 1;
    height: 6px;
    background: #f3f3f3;
    border-radius: 3px;
    overflow: hidden;
  }
  &__bar-fill {
    height: 100%;
    background: var(--bd-primary);
  }
  &__num {
    width: 32px;
    text-align: right;
    color: var(--bd-text);
    font-weight: 600;
  }
}
.sort {
  display: flex;
  gap: 8px;
  padding: 12px 16px 4px;
  &__item {
    padding: 5px 12px;
    border: 1px solid var(--bd-border);
    background: #fff;
    border-radius: 999px;
    font-size: 12px;
    color: var(--bd-text-secondary);
    cursor: pointer;
    &.active {
      border-color: var(--bd-primary);
      background: rgba(255, 36, 66, 0.06);
      color: var(--bd-primary);
    }
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
  padding: 12px;
  margin-bottom: 8px;
  &__head {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  &__author {
    flex: 1;
  }
  &__name {
    font-size: 13px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 6px;
  }
  &__date {
    margin-top: 2px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__text {
    margin: 8px 0 4px;
    font-size: 13px;
    line-height: 1.6;
  }
  &__foot {
    margin-top: 6px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd2da, #ff7799);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.verified {
  font-size: 10px;
  color: #00a854;
  background: rgba(0, 168, 84, 0.1);
  padding: 2px 6px;
  border-radius: 6px;
}
.helpful {
  font-size: 11px;
  color: var(--bd-text-secondary);
}
</style>
