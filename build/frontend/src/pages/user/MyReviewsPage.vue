<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { EyeOff, Image, ShieldCheck, Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchCourseDetail, fetchCoachDetail } from '@/api/course';
import { fetchMyReviews, type ReviewItem, type ReviewTargetType } from '@/api/review';
import { fetchStudioDetail } from '@/api/studio';

const router = useRouter();

const cats: Array<'全部' | '舞室' | '老师' | '课程'> = ['全部', '舞室', '老师', '课程'];
const activeCat = ref<(typeof cats)[number]>('全部');
const reviews = ref<ReviewItem[]>([]);
const loading = ref(false);
const loadError = ref('');
const targetNameMap = ref<Record<string, string>>({});

const typeLabel: Record<ReviewTargetType, string> = {
  studio: '舞室',
  coach: '老师',
  course: '课程'
};

const statusLabel: Record<string, string> = {
  published: '已发布',
  pending: '待审核',
  folded: '已折叠',
  hidden: '已隐藏'
};

const filteredReviews = computed(() => {
  if (activeCat.value === '全部') return reviews.value;
  const targetType = ({ 舞室: 'studio', 老师: 'coach', 课程: 'course' } as const)[activeCat.value];
  return reviews.value.filter((item) => item.targetType === targetType);
});

const statusTone = (status: string) => {
  if (status === 'published') return 'success';
  if (status === 'pending') return 'warning';
  return 'danger';
};

const reviewTargetKey = (targetType: ReviewTargetType, targetId: number) => `${targetType}:${targetId}`;

const reviewTargetName = (review: ReviewItem) =>
  targetNameMap.value[reviewTargetKey(review.targetType, review.targetId)]
  || `${typeLabel[review.targetType]} #${review.targetId}`;

const reviewStatusGuide = (review: ReviewItem) => {
  if (review.reviewStatus === 'pending') return '系统已收到评价，正在核验来源和异常波动。';
  if (review.reviewStatus === 'folded') return '该评价因低权重或异常特征被折叠，复核后可能恢复展示。';
  if (review.reviewStatus === 'hidden') return '该评价当前对外隐藏，请等待平台处理结果。';
  if (review.isVerified) return '已验证来源，展示权重更高。';
  return review.riskLevel > 0 ? '当前以普通权重展示，风险升高时可能进入复核。' : '当前以普通权重展示。';
};

const dimLine = (review: ReviewItem) =>
  (review.dimensions ?? [])
    .slice(0, 4)
    .map((item) => `${item.name.replace(/便利度|程度|质量|清晰度|条件|整体|合理性/g, '')} ${item.score}`)
    .join(' · ') || '维度评分待同步';

const dateLabel = (value: string) =>
  new Date(value).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' });

const openTarget = (review: ReviewItem) => {
  if (review.targetType === 'studio') router.push(`/studio/${review.targetId}?tab=reviews`);
  else router.push(`/${review.targetType}/${review.targetId}`);
};

const loadTargetNames = async (list: ReviewItem[]) => {
  const nextMap: Record<string, string> = {};
  const studioIds = Array.from(new Set(list.filter((item) => item.targetType === 'studio').map((item) => item.targetId)));
  const coachIds = Array.from(new Set(list.filter((item) => item.targetType === 'coach').map((item) => item.targetId)));
  const courseIds = Array.from(new Set(list.filter((item) => item.targetType === 'course').map((item) => item.targetId)));

  const [studios, coaches, courses] = await Promise.all([
    Promise.allSettled(studioIds.map((id) => fetchStudioDetail(id))),
    Promise.allSettled(coachIds.map((id) => fetchCoachDetail(id))),
    Promise.allSettled(courseIds.map((id) => fetchCourseDetail(id)))
  ]);

  studios.forEach((item, index) => {
    if (item.status === 'fulfilled') nextMap[reviewTargetKey('studio', studioIds[index])] = item.value.name;
  });
  coaches.forEach((item, index) => {
    if (item.status === 'fulfilled') nextMap[reviewTargetKey('coach', coachIds[index])] = item.value.displayName;
  });
  courses.forEach((item, index) => {
    if (item.status === 'fulfilled') nextMap[reviewTargetKey('course', courseIds[index])] = item.value.courseName;
  });

  targetNameMap.value = nextMap;
};

const loadReviews = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    // M2 风控验收：读取真实 h5 本人评价列表，包含 pending 状态用于展示“待审核”。
    const resp = await fetchMyReviews({ page: 1, pageSize: 50 });
    reviews.value = resp.list ?? [];
    // M2 我的评价：真实对象名要和评价状态一起出现，不能继续只给用户看“课程 #100010”。
    await loadTargetNames(reviews.value);
  } catch {
    reviews.value = [];
    targetNameMap.value = {};
    loadError.value = '评价接口暂不可用，请检查登录态或后端服务';
  } finally {
    loading.value = false;
  }
};

onMounted(loadReviews);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的评价" :show-share="false" />

    <section class="pen-scroll">
      <div class="chip-row">
        <button
          v-for="c in cats"
          :key="c"
          class="chip"
          :class="activeCat === c ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeCat = c"
        >
          {{ c }}
        </button>
      </div>

      <p v-if="loading" class="empty">评价同步中...</p>
      <p v-else-if="loadError" class="empty">{{ loadError }}</p>
      <p v-else-if="!filteredReviews.length" class="empty">暂无该类型评价</p>

      <article v-for="review in filteredReviews" :key="review.id" class="rev" @click="openTarget(review)">
        <header class="rev__top">
          <strong class="rev__target">
            {{ reviewTargetName(review) }}
          </strong>
          <span class="rev__status" :class="`rev__status--${statusTone(review.reviewStatus)}`">
            {{ statusLabel[review.reviewStatus] ?? review.reviewStatus }}
          </span>
        </header>

        <p class="rev__context">{{ typeLabel[review.targetType] }} · {{ dateLabel(review.publishedAt) }}</p>

        <div class="rev__score">
          <span class="rev__stars">
            <Star
              v-for="i in 5"
              :key="i"
              :size="14"
              :stroke-width="2"
              :fill="i <= Math.round(Number(review.overallScore ?? 0)) ? '#111111' : 'none'"
              :color="i <= Math.round(Number(review.overallScore ?? 0)) ? '#111111' : '#E5E5E5'"
            />
          </span>
          <span>{{ Number(review.overallScore ?? 0).toFixed(1) }}</span>
        </div>

        <p class="rev__dims">{{ dimLine(review) }}</p>
        <p class="rev__content">{{ review.contentText || '用户暂未填写评价内容。' }}</p>

        <div class="rev__meta">
          <span v-if="review.isVerified" class="rev__pill rev__pill--success">
            <ShieldCheck :size="12" /> 已验证来源
          </span>
          <span v-else class="rev__pill">
            <EyeOff :size="12" /> 普通权重
          </span>
          <span v-if="review.mediaAssets?.length" class="rev__pill">
            <Image :size="12" /> {{ review.mediaAssets.length }} 个媒体
          </span>
          <span class="rev__pill">权重 {{ Number(review.weightFactor ?? 0).toFixed(2) }}</span>
          <span class="rev__pill">风险 {{ review.riskLevel ?? 0 }}</span>
        </div>

        <p class="rev__explain">{{ reviewStatusGuide(review) }}</p>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.empty {
  margin: 0;
  padding: 18px;
  border-radius: 12px;
  background: $pen-soft;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 800;
  line-height: $pen-lh;
}

.rev {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;
  cursor: pointer;

  &__top,
  &__score,
  &__meta {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__target {
    flex: 1;
    min-width: 0;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__status,
  &__pill {
    flex: none;
    min-height: 26px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__status--success,
  &__pill--success {
    border-color: $pen-success;
    color: $pen-success;
  }

  &__status--warning {
    border-color: #c99700;
    color: #8a6500;
  }

  &__status--danger {
    border-color: #d84c4c;
    color: #b22f2f;
  }

  &__score {
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__stars {
    display: inline-flex;
    gap: 3px;
  }

  &__dims {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__content {
    margin: 0;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.4;
  }

  &__meta {
    flex-wrap: wrap;
  }

  &__context,
  &__explain {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__explain {
    line-height: 1.45;
  }
}
</style>
