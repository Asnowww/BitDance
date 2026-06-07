<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { EyeOff, Image, PencilLine, ShieldCheck, Star, ThumbsUp } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import {
  fetchReviews,
  fetchReviewSummary,
  REVIEW_DIMENSIONS,
  type ReviewItem,
  type ReviewSummary,
  type ReviewTargetType
} from '@/api/review';

const props = defineProps<{
  targetType: ReviewTargetType;
  targetId: number;
  targetName?: string;
}>();

type FilterKey = 'all' | 'verified' | 'media' | 'folded';

const router = useRouter();
const activeFilter = ref<FilterKey>('all');
const summary = ref<ReviewSummary | null>(null);
const reviews = ref<ReviewItem[]>([]);
const loading = ref(false);

const dimensions = computed(() => REVIEW_DIMENSIONS[props.targetType]);
const compactLabels = computed(() => {
  if (props.targetType === 'studio') return ['交通', '卫生', '场地', '氛围'];
  if (props.targetType === 'coach') return ['耐心', '纠错', '讲解', '友好'];
  return ['难度', '节奏', '强度', '收获'];
});

const scoreLabel = computed(() => {
  if (props.targetType === 'studio') return '综合评分';
  if (props.targetType === 'coach') return '教学评分';
  return '课程评分';
});

const filters = computed<Array<{ key: FilterKey; label: string; sort?: 'latest' | 'verified' }>>(() => [
  { key: 'all', label: '全部', sort: 'latest' },
  { key: 'verified', label: '已验证', sort: 'verified' },
  { key: 'media', label: '带图', sort: 'latest' },
  { key: 'folded', label: '低分折叠', sort: 'latest' }
]);

const scoreText = computed(() => {
  const score = Number(summary.value?.weightedAvgScore ?? 0);
  return score > 0 ? score.toFixed(1) : '0.0';
});

const verifiedMeta = computed(() => {
  const verified = summary.value?.verifiedCount ?? 0;
  const total = summary.value?.count ?? reviews.value.length;
  return `已验证 ${verified} 条 / 全部 ${total} 条`;
});

const radarPoints = computed(() => {
  const center = 56;
  const maxRadius = 46;
  const points = dimensions.value.map((dim, index) => {
    const raw = Number(summary.value?.dimensionAvg?.[dim.key] ?? fallbackDimensionAvg(dim.key));
    const radius = Math.max(0.08, Math.min(raw / 5, 1)) * maxRadius;
    const angle = -Math.PI / 2 + index * (Math.PI / 2);
    return `${center + Math.cos(angle) * radius},${center + Math.sin(angle) * radius}`;
  });
  return points.join(' ');
});

const visibleReviews = computed(() => {
  if (activeFilter.value === 'folded') {
    return reviews.value.filter((item) => item.reviewStatus === 'folded' || Number(item.riskLevel ?? 0) > 0);
  }
  if (activeFilter.value === 'verified') return reviews.value.filter((item) => item.isVerified);
  if (activeFilter.value === 'media')
    return reviews.value.filter((item) => item.reviewStatus !== 'folded' && (item.mediaAssets?.length ?? 0) > 0);
  return reviews.value.filter((item) => item.reviewStatus !== 'folded');
});

const foldedCount = computed(
  () => reviews.value.filter((item) => item.reviewStatus === 'folded' || Number(item.riskLevel ?? 0) > 0).length
);

const filterSort = () => filters.value.find((item) => item.key === activeFilter.value)?.sort ?? 'latest';

const fallbackDimensionAvg = (key: string) => {
  const values = reviews.value
    .map((item) => item.dimensions?.find((dim) => dim.code === key)?.score)
    .filter((item): item is number => typeof item === 'number');
  if (!values.length) return 0;
  return values.reduce((sum, item) => sum + item, 0) / values.length;
};

const dimensionValue = (key: string) => {
  const value = Number(summary.value?.dimensionAvg?.[key] ?? fallbackDimensionAvg(key));
  return value > 0 ? value : 0;
};

const dimensionShort = (label: string) => label.replace(/便利度|程度|质量|清晰度|条件|整体|合理性/g, '');

const reviewName = (review: ReviewItem) => {
  if (review.isVerified) return '已验证用户';
  if (review.reviewStatus === 'folded') return '系统折叠';
  return '普通用户';
};

const reviewTag = (review: ReviewItem) => {
  if (review.reviewStatus === 'folded' || Number(review.riskLevel ?? 0) > 0) return '低权重 / 风控折叠';
  return review.isVerified ? '高权重评价' : '普通权重评价';
};

const reviewBody = (review: ReviewItem) =>
  review.contentText || (review.reviewStatus === 'folded' ? '该评价因低权重或异常特征已折叠。' : '用户暂未填写文字内容。');

const dimLine = (review: ReviewItem) =>
  dimensions.value
    .slice(0, 3)
    .map((item) => {
      const score = review.dimensions?.find((dim) => dim.code === item.key)?.score ?? 0;
      return `${dimensionShort(item.label)} ${score || '-'}`;
    })
    .join(' · ');

const load = async () => {
  if (!props.targetId) return;
  loading.value = true;
  try {
    const [summaryResp, reviewResp] = await Promise.all([
      fetchReviewSummary(props.targetType, props.targetId),
      fetchReviews({
        targetType: props.targetType,
        targetId: props.targetId,
        sort: filterSort(),
        status: activeFilter.value === 'folded' ? 'folded' : 'published',
        page: 1,
        pageSize: 20
      })
    ]);
    summary.value = summaryResp;
    reviews.value = reviewResp.list ?? [];
  } finally {
    loading.value = false;
  }
};

const setFilter = async (key: FilterKey) => {
  activeFilter.value = key;
  await load();
};

const openPublishReview = () => {
  router.push({
    path: '/publish/review',
    query: {
      targetType: props.targetType,
      targetId: String(props.targetId),
      targetName: props.targetName || `${scoreLabel.value} #${props.targetId}`
    }
  });
};

const openAppeal = (review: ReviewItem) => {
  // M2 商家/教练申诉：从真实评价卡带入 reviewId 和摘要，进入后端 review_appeal 提交流。
  router.push({
    path: '/coach/appeal',
    query: {
      reviewId: String(review.id),
      score: String(review.overallScore ?? 0),
      author: reviewName(review),
      content: reviewBody(review).slice(0, 160)
    }
  });
};

watch(() => [props.targetType, props.targetId], load);
onMounted(load);
</script>

<template>
  <section class="review-aggregate">
    <section class="review-summary">
      <div class="review-summary__score">
        <strong>{{ scoreText }}</strong>
        <span>{{ scoreLabel }}</span>
        <small>{{ verifiedMeta }}</small>
      </div>

      <div class="radar" aria-label="评分维度雷达图">
        <span class="radar__label radar__label--top">{{ compactLabels[0] }}</span>
        <span class="radar__label radar__label--right">{{ compactLabels[1] }}</span>
        <span class="radar__label radar__label--bottom">{{ compactLabels[2] }}</span>
        <span class="radar__label radar__label--left">{{ compactLabels[3] }}</span>
        <svg class="radar__svg" viewBox="0 0 112 112" role="img">
          <polygon points="56,10 102,56 56,102 10,56" class="radar__grid" />
          <polygon points="56,25 87,56 56,87 25,56" class="radar__grid" />
          <polygon points="56,40 72,56 56,72 40,56" class="radar__grid" />
          <line x1="10" y1="56" x2="102" y2="56" class="radar__axis" />
          <line x1="56" y1="10" x2="56" y2="102" class="radar__axis" />
          <polygon :points="radarPoints" class="radar__score" />
        </svg>
      </div>
    </section>

    <div class="dimension-bars">
      <div v-for="item in dimensions" :key="item.key" class="dimension-row">
        <span>{{ item.label }}</span>
        <i>
          <b :style="{ width: `${Math.min((dimensionValue(item.key) / 5) * 100, 100)}%` }" />
        </i>
        <strong>{{ dimensionValue(item.key).toFixed(1) }}</strong>
      </div>
    </div>

    <nav class="review-filters" aria-label="评价筛选">
      <button
        v-for="filter in filters"
        :key="filter.key"
        type="button"
        class="chip"
        :class="activeFilter === filter.key ? 'chip--active' : 'chip--inactive'"
        @click="setFilter(filter.key)"
      >
        <Image v-if="filter.key === 'media'" :size="14" />
        <EyeOff v-else-if="filter.key === 'folded'" :size="14" />
        <ShieldCheck v-else-if="filter.key === 'verified'" :size="14" />
        <span>{{ filter.label }}</span>
      </button>
    </nav>

    <button type="button" class="review-compose" @click="openPublishReview">
      <span class="review-compose__icon"><PencilLine :size="16" /></span>
      <span>写评价</span>
      <small>分享体验，帮助后来的舞者判断课程和场地</small>
    </button>

    <section class="review-list" :aria-busy="loading">
      <article v-if="activeFilter === 'folded' && !visibleReviews.length" class="folded-card">
        <EyeOff :size="18" />
        <div>
          <strong>暂无可展开的折叠评价</strong>
          <p>后端公开评价接口当前默认只返回已发布评价；低权重、异常波动评价会由风控折叠后进入复核。</p>
        </div>
      </article>

      <article v-for="review in visibleReviews.slice(0, 3)" :key="review.id" class="review-card">
        <header class="review-card__head">
          <div class="review-card__avatar">
            <EyeOff v-if="review.reviewStatus === 'folded'" :size="16" />
            <ShieldCheck v-else :size="16" />
          </div>
          <div class="review-card__meta">
            <strong>{{ reviewName(review) }}</strong>
            <span>{{ reviewTag(review) }}</span>
          </div>
          <div class="review-card__score">
            <Star :size="13" />
            <span>{{ Number(review.overallScore ?? 0).toFixed(1) }}</span>
          </div>
        </header>
        <p>{{ reviewBody(review) }}</p>
        <div v-if="review.mediaAssets?.length" class="review-card__media">
          <div v-for="(media, index) in review.mediaAssets.slice(0, 3)" :key="`${media.name}-${index}`">
            <img v-if="media.type === 'image'" :src="media.url" :alt="media.name" />
            <video v-else :src="media.url" muted playsinline preload="metadata" />
          </div>
        </div>
        <footer>
          <span>{{ dimLine(review) }}</span>
          <button type="button" class="review-card__appeal" @click="openAppeal(review)">申诉</button>
          <span class="review-card__helpful"><ThumbsUp :size="12" /> {{ review.helpfulCount ?? 0 }}</span>
        </footer>
      </article>

      <article v-if="activeFilter !== 'folded' && foldedCount" class="folded-card folded-card--compact">
        <EyeOff :size="18" />
        <div>
          <strong>{{ foldedCount }} 条低权重评价已折叠</strong>
          <p>可切换“低分折叠”查看风控原因。</p>
        </div>
      </article>
    </section>
  </section>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.review-aggregate {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-summary,
.review-list article {
  border: 1px solid $pen-hairline;
  border-radius: 18px;
  background: $pen-canvas;
}

.review-summary {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 168px;
  gap: 8px;
  padding: 16px;

  &__score {
    display: flex;
    min-width: 0;
    flex-direction: column;
    justify-content: center;
    gap: 5px;

    strong {
      font-size: 44px;
      font-weight: 900;
      line-height: 1;
      letter-spacing: 0;
    }

    span {
      font-size: 14px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    small {
      color: $pen-mute;
      font-size: 11px;
      font-weight: 700;
      line-height: $pen-lh;
    }
  }
}

.radar {
  position: relative;
  width: 168px;
  height: 152px;
  color: $pen-mute;
  font-size: 11px;
  font-weight: 800;

  &__svg {
    position: absolute;
    top: 24px;
    left: 28px;
    width: 112px;
    height: 112px;
  }

  &__grid {
    fill: transparent;
    stroke: #d8d2ca;
    stroke-width: 1;
  }

  &__axis {
    stroke: #e1dcd4;
    stroke-width: 1;
  }

  &__score {
    fill: rgba(17, 17, 17, 0.13);
    stroke: $pen-ink;
    stroke-width: 3;
    stroke-linejoin: round;
  }

  &__label {
    position: absolute;
    white-space: nowrap;

    &--top {
      top: 4px;
      left: 50%;
      transform: translateX(-50%);
    }

    &--right {
      top: 74px;
      right: 0;
    }

    &--bottom {
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
    }

    &--left {
      top: 74px;
      left: 0;
    }
  }
}

.dimension-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 4px;
}

.dimension-row {
  display: grid;
  grid-template-columns: 74px minmax(0, 1fr) 34px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;

  span {
    color: $pen-mute;
  }

  i {
    height: 6px;
    overflow: hidden;
    border-radius: 999px;
    background: #ece8e1;
  }

  b {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: $pen-ink;
  }
}

.review-filters {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 2px 0;
}

.chip {
  @include pen-chip;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
}

.review-compose {
  display: grid;
  grid-template-columns: 34px auto minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 50px;
  padding: 10px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 16px;
  background: $pen-canvas;
  color: $pen-ink;
  text-align: left;
  cursor: pointer;

  &__icon {
    display: grid;
    place-items: center;
    width: 34px;
    height: 34px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  > span:not(.review-compose__icon) {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
    white-space: nowrap;
  }

  small {
    min-width: 0;
    color: $pen-mute;
    font-size: 11px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-card,
.folded-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
}

.review-card {
  &__head {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__avatar {
    display: grid;
    flex: none;
    place-items: center;
    width: 36px;
    height: 36px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  &__meta {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    gap: 2px;

    strong {
      font-size: 14px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      color: $pen-success;
      font-size: 11px;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }

  &__score {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    height: 28px;
    padding: 0 9px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 12px;
    font-weight: 900;
  }

  p {
    margin: 0;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.45;
  }

  &__media {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 6px;

    div {
      height: 68px;
      overflow: hidden;
      border-radius: 8px;
      background: $pen-soft;
    }

    img,
    video {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    color: $pen-mute;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__helpful {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    white-space: nowrap;
  }

  &__appeal {
    // M2 申诉入口：保持为低干扰文本按钮，不抢占普通用户阅读评价的主视觉。
    flex: none;
    border: 0;
    background: transparent;
    color: $pen-ink;
    font: inherit;
    cursor: pointer;
  }
}

.folded-card {
  flex-direction: row;
  align-items: flex-start;
  background: #f2f0ec;
  color: $pen-mute;

  strong {
    display: block;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    margin: 3px 0 0;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.4;
  }
}

@media (max-width: 360px) {
  .review-summary {
    grid-template-columns: 1fr;
  }

  .radar {
    justify-self: center;
  }
}
</style>
