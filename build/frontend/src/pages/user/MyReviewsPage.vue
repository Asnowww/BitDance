<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchCoachDetail, fetchCourseDetail } from '@/api/course';
import { fetchMyReviews, type ReviewItem, type ReviewTargetType } from '@/api/review';
import { fetchStudioDetail } from '@/api/studio';

const cats: Array<{ key: 'all' | ReviewTargetType; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'studio', label: '舞室' },
  { key: 'coach', label: '老师' },
  { key: 'course', label: '课程' },
  { key: 'workshop', label: 'Workshop' }
];

const activeCat = ref<'all' | ReviewTargetType>('all');
const loading = ref(false);
const reviews = ref<ReviewItem[]>([]);
const targetNames = reactive<Record<string, string>>({});

const typeLabel: Record<ReviewTargetType, string> = {
  studio: '舞室',
  coach: '老师',
  course: '课程',
  workshop: 'Workshop'
};

const filteredReviews = computed(() =>
  activeCat.value === 'all'
    ? reviews.value
    : reviews.value.filter((item) => item.targetType === activeCat.value)
);

const targetKey = (item: Pick<ReviewItem, 'targetType' | 'targetId'>) => `${item.targetType}:${item.targetId}`;

const fallbackTarget = (item: ReviewItem) => `${typeLabel[item.targetType] ?? item.targetType} #${item.targetId}`;

const targetName = (item: ReviewItem) => targetNames[targetKey(item)] ?? fallbackTarget(item);

const formatDate = (value?: string) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value.slice(0, 10);
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

const dimensionText = (item: ReviewItem) =>
  item.dimensions?.length
    ? item.dimensions.map((dim) => `${dim.name} ${dim.score}`).join(' · ')
    : `综合 ${Number(item.overallScore).toFixed(1)}`;

const resolveTargetName = async (item: ReviewItem) => {
  const key = targetKey(item);
  if (targetNames[key]) return;
  try {
    if (item.targetType === 'studio') {
      targetNames[key] = (await fetchStudioDetail(item.targetId)).name;
      return;
    }
    if (item.targetType === 'coach') {
      targetNames[key] = (await fetchCoachDetail(item.targetId)).displayName;
      return;
    }
    if (item.targetType === 'course') {
      targetNames[key] = (await fetchCourseDetail(item.targetId)).courseName;
      return;
    }
    targetNames[key] = fallbackTarget(item);
  } catch {
    targetNames[key] = fallbackTarget(item);
  }
};

const load = async () => {
  loading.value = true;
  try {
    const data = await fetchMyReviews({ page: 1, pageSize: 20 });
    reviews.value = data.list ?? [];
    void Promise.all(reviews.value.slice(0, 12).map(resolveTargetName));
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的评价" :show-share="false" />

    <section class="pen-scroll">
      <div class="chip-row">
        <button
          v-for="c in cats"
          :key="c.key"
          class="chip"
          :class="activeCat === c.key ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeCat = c.key"
        >
          {{ c.label }}
        </button>
      </div>

      <p v-if="loading" class="empty">正在读取后端评价...</p>
      <p v-else-if="!filteredReviews.length" class="empty">暂无评价记录</p>

      <article v-for="r in filteredReviews" :key="r.id" class="rev">
        <header class="rev__top">
          <strong class="rev__target">{{ targetName(r) }}</strong>
          <span v-if="r.isVerified" class="rev__verified">已验证</span>
          <span class="rev__status">{{ r.reviewStatus }}</span>
        </header>
        <span class="rev__stars">
          <Star
            v-for="i in 5"
            :key="i"
            :size="14"
            :stroke-width="2"
            :fill="i <= Math.round(r.overallScore) ? '#111111' : 'none'"
            :color="i <= Math.round(r.overallScore) ? '#111111' : '#E5E5E5'"
          />
        </span>
        <p class="rev__dims">{{ dimensionText(r) }}</p>
        <p class="rev__content">{{ r.contentText }}</p>
        <span class="rev__date">{{ formatDate(r.publishedAt) }}</span>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.empty {
  margin: 18px 0;
  color: $pen-mute;
  font-size: 14px;
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

  &__top { display: flex; align-items: center; gap: 8px; }
  &__target { flex: 1; min-width: 0; font-size: 15px; font-weight: 900; line-height: $pen-lh; }

  &__verified,
  &__status {
    flex: none;
    height: 26px;
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__verified {
    border: 1px solid $pen-success;
    color: $pen-success;
  }

  &__status {
    background: $pen-canvas;
    color: $pen-mute;
  }

  &__stars { display: inline-flex; gap: 3px; }
  &__dims { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: $pen-lh; }
  &__content { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
  &__date { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
}
</style>
