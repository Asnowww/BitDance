<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { AlertTriangle, EyeOff, Gavel, Image, Play, ShieldCheck, Star } from 'lucide-vue-next';
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
  if (review.latestAppeal?.appealStatus === 'pending') return '这条评价已进入申诉复核，平台会结合签到、来源和内容信号重新判断展示方式。';
  if (review.latestAppeal?.appealStatus === 'approved') return '申诉成立后，这条评价已进入治理流程；当前状态和展示范围会按处理结果同步更新。';
  if (review.latestAppeal?.appealStatus === 'rejected') return '平台已复核申诉，当前保留原评价状态与展示权重。';
  if (review.reviewStatus === 'pending') return '系统已收到评价，正在核验来源和异常波动。';
  if (review.reviewStatus === 'folded') return '该评价因低权重或异常特征被折叠，复核后可能恢复展示。';
  if (review.reviewStatus === 'hidden') return '该评价当前对外隐藏，请等待平台处理结果。';
  if (review.isVerified) return '已验证来源，展示权重更高。';
  return review.riskLevel > 0 ? '当前以普通权重展示，风险升高时可能进入复核。' : '当前以普通权重展示。';
};

const riskLevelLabel = (review: ReviewItem) => {
  if (review.reviewStatus === 'hidden') return '平台隐藏';
  if (review.reviewStatus === 'folded') return '折叠复核';
  if (review.riskLevel >= 2) return '高风险复核';
  if (review.riskLevel === 1) return '轻度异常';
  return '状态正常';
};

const riskReasonGuide = (review: ReviewItem) => {
  if (review.latestAppeal?.appealStatus === 'approved') {
    return '本条评价因申诉成立进入治理处理，平台会限制公开展示并保留复核记录。';
  }
  if (review.reviewStatus === 'hidden') {
    return '平台已暂时隐藏这条评价，通常是因为申诉成立或人工审核需要先下线处理。';
  }
  if (review.reviewStatus === 'folded') {
    return review.riskLevel >= 2
      ? '系统检测到短时间异常评分波动或来源可信度不足，所以先折叠等待复核。'
      : '当前互动权重偏低，平台先降低展示优先级，后续复核通过后可恢复。';
  }
  if (review.reviewStatus === 'pending') {
    return '提交后会先核验来源、评分波动和内容完整度，审核通过后再进入公开展示。';
  }
  if (review.riskLevel >= 2) {
    return '虽然仍在展示，但系统已经标记高风险信号，后续可能进入人工复核。';
  }
  if (review.riskLevel === 1) {
    return '系统观察到轻微异常波动，当前仍展示，但传播权重会更保守。';
  }
  return review.isVerified ? '来源已核验，当前按较高可信度参与聚合评分。' : '暂未核验来源，当前按普通权重参与聚合评分。';
};

const appealStatusLabel = (review: ReviewItem) => {
  if (!review.latestAppeal) return '';
  if (review.latestAppeal.appealStatus === 'pending') return '申诉处理中';
  if (review.latestAppeal.appealStatus === 'approved') return '申诉成立';
  if (review.latestAppeal.appealStatus === 'rejected') return '申诉未成立';
  return review.latestAppeal.appealStatus;
};

const appealStatusTone = (review: ReviewItem) => {
  if (!review.latestAppeal) return 'neutral';
  if (review.latestAppeal.appealStatus === 'pending') return 'warning';
  if (review.latestAppeal.appealStatus === 'approved') return 'danger';
  return 'success';
};

const dimLine = (review: ReviewItem) =>
  (review.dimensions ?? [])
    .slice(0, 4)
    .map((item) => `${item.name.replace(/便利度|程度|质量|清晰度|条件|整体|合理性/g, '')} ${item.score}`)
    .join(' · ') || '维度评分待同步';

const dateLabel = (value: string) =>
  new Date(value).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' });

const dateTimeLabel = (value?: string) => {
  if (!value) return '';
  return new Date(value).toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

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
    // M2 我的评价：评价列表先展示，真实对象名后台补齐，避免详情接口慢时页面一直显示“评价同步中...”。
    void loadTargetNames(reviews.value).catch(() => {
      targetNameMap.value = {};
    });
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

        <div v-if="review.mediaAssets?.length" class="rev__media">
          <div v-for="(media, index) in review.mediaAssets.slice(0, 3)" :key="`${review.id}-${media.name}-${index}`" class="rev__media-item">
            <img v-if="media.type === 'image'" :src="media.url" :alt="media.name" />
            <div v-else class="rev__video-cover">
              <video :src="media.url" muted playsinline preload="metadata" />
              <span class="rev__video-badge"><Play :size="12" /> 视频</span>
            </div>
          </div>
        </div>

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
          <span class="rev__pill" :class="`rev__pill--${statusTone(review.reviewStatus)}`">
            <AlertTriangle :size="12" /> {{ riskLevelLabel(review) }}
          </span>
          <span v-if="review.latestAppeal" class="rev__pill" :class="`rev__pill--${appealStatusTone(review)}`">
            <Gavel :size="12" /> {{ appealStatusLabel(review) }}
          </span>
        </div>

        <p class="rev__explain">{{ reviewStatusGuide(review) }}</p>
        <p class="rev__risk-guide">{{ riskReasonGuide(review) }}</p>

        <section v-if="review.latestAppeal" class="rev__appeal">
          <strong class="rev__appeal-title">治理进度</strong>
          <p class="rev__appeal-line">
            {{ appealStatusLabel(review) }}
            <span v-if="review.latestAppeal.createdAt"> · 发起于 {{ dateTimeLabel(review.latestAppeal.createdAt) }}</span>
          </p>
          <p v-if="review.latestAppeal.reviewedAt" class="rev__appeal-line">
            平台处理时间 · {{ dateTimeLabel(review.latestAppeal.reviewedAt) }}
          </p>
          <p v-if="review.latestAppeal.reviewRemark" class="rev__appeal-line">
            平台备注 · {{ review.latestAppeal.reviewRemark }}
          </p>
        </section>
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

  &__status--warning,
  &__pill--warning {
    border-color: #c99700;
    color: #8a6500;
  }

  &__status--danger,
  &__pill--danger {
    border-color: #d84c4c;
    color: #b22f2f;
  }

  &__pill--neutral {
    border-color: $pen-hairline;
    color: $pen-mute;
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

  &__media {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  &__media-item {
    overflow: hidden;
    border-radius: 12px;
    background: $pen-canvas;
    aspect-ratio: 1 / 1;

    img,
    video {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__video-cover {
    position: relative;
    width: 100%;
    height: 100%;
  }

  &__video-badge {
    position: absolute;
    right: 8px;
    bottom: 8px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    min-height: 24px;
    padding: 4px 8px;
    border-radius: 999px;
    background: rgb(17 17 17 / 78%);
    color: $pen-on-primary;
    font-size: 10px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__meta {
    flex-wrap: wrap;
  }

  &__context,
  &__explain,
  &__risk-guide,
  &__appeal-line {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__explain {
    line-height: 1.45;
  }

  &__risk-guide {
    line-height: 1.45;
  }

  &__appeal {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 10px 12px;
    border-radius: 12px;
    background: $pen-canvas;
    border: 1px solid $pen-hairline;
  }

  &__appeal-title {
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}
</style>
