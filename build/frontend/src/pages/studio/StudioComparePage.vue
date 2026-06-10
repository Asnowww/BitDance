<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { CalendarDays, ChevronRight, Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import { fetchNearbyStudios, fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { fetchCourseDetail, type CourseDetail } from '@/api/course';
import { fetchReviewSummary, type ReviewSummary } from '@/api/review';
import { toggleFavorite } from '@/api/favorite';
import { fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';

const router = useRouter();

interface CompareStudio {
  detail: StudioDetail;
  schedule: ScheduleSlot[];
  courses: CourseDetail[];
  summary: ReviewSummary | null;
}

interface CompareCell {
  text: string;
}

interface CompareRow {
  label: string;
  values: CompareCell[];
}

const studios = ref<CompareStudio[]>([]);
const selectedStudioId = ref<number | null>(null);
const columns = computed(() => ['维度', ...studios.value.map((studio) => studio.detail.name)]);
const gridStyle = computed(() => ({
  gridTemplateColumns: `minmax(72px, .85fr) repeat(${Math.max(studios.value.length, 1)}, minmax(0, 1fr))`
}));

const availableCount = (item: CompareStudio) =>
  item.schedule.reduce((sum, slot) => sum + Math.max(Number(slot.capacity || 0) - Number(slot.bookedCount || 0), 0), 0);

const coursePrices = (item: CompareStudio) =>
  item.courses.map((course) => Number(course.priceAmount || 0)).filter(Boolean);

const priceRange = (item: CompareStudio) => {
  const prices = coursePrices(item);
  if (!prices.length) return '待补价格';
  const min = Math.min(...prices);
  const max = Math.max(...prices);
  return min === max ? `¥${min}` : `¥${min}-${max}`;
};

const zeroBasicCount = (item: CompareStudio) =>
  item.courses.filter((course) => course.zeroBasicFriendly || course.targetAudience?.includes('零基础')).length;

const numericDistance = (item: CompareStudio) => {
  const distance = Number(item.detail.distanceKm);
  return Number.isFinite(distance) && distance > 0 ? distance : 8;
};

const rows = computed<CompareRow[]>(() => [
  {
    label: '综合评分',
    values: studios.value.map((studio) =>
      ({ text: studio.summary?.weightedAvgScore ? `${studio.summary.weightedAvgScore.toFixed(1)} / 5` : '暂无评价' })
    )
  },
  { label: '价格区间', values: studios.value.map((studio) => ({ text: priceRange(studio) })) },
  { label: '课表余位', values: studios.value.map((studio) => ({ text: `${availableCount(studio)} 个` })) },
  { label: '零基础课', values: studios.value.map((studio) => ({ text: `${zeroBasicCount(studio)} 门` })) },
  {
    label: '距离/交通',
    values: studios.value.map((studio) =>
      ({ text: `${studio.detail.distanceKm ?? '-'}km · ${studio.detail.transportInfo || '待补交通'}` })
    )
  },
  { label: '地址', values: studios.value.map((studio) => ({ text: studio.detail.address || '-' })) },
  { label: '收藏状态', values: studios.value.map((studio) => ({ text: studio.detail.favored ? '已收藏' : '未收藏' })) },
  { label: '操作', values: studios.value.map(() => ({ text: 'actions' })) }
]);

const selectedStudio = computed(() => studios.value.find((studio) => studio.detail.id === selectedStudioId.value) ?? null);

const selectStudio = (studioId: number) => {
  selectedStudioId.value = studioId;
};

const onShare = () => showToast('已生成对比分享卡');

const openStudioDetail = (id: number) => {
  void router.push(`/studio/${id}`);
};

const bookStudio = (id?: number) => {
  if (!id) return;
  void router.push(`/studio/${id}/trial`);
};

const bookSelected = () => {
  bookStudio(selectedStudio.value?.detail.id);
};

const toggleStudioFavorite = async (item: CompareStudio) => {
  const result = await toggleFavorite('studio', item.detail.id);
  item.detail.favored = result.favored;
  showToast(result.favored ? '已收藏该舞室' : '已取消收藏');
};

const favoriteBest = async () => {
  if (!selectedStudio.value) return;
  await toggleStudioFavorite(selectedStudio.value);
};

const loadCompareStudio = async (id: number): Promise<CompareStudio> => {
  const [detail, schedule, summary] = await Promise.all([
    fetchStudioDetail(id),
    fetchStudioSchedule(id).catch(() => []),
    fetchReviewSummary('studio', id).catch(() => null)
  ]);
  const courseIds = Array.from(new Set(schedule.map((slot) => slot.courseId))).slice(0, 8);
  const courseResults = await Promise.allSettled(courseIds.map((courseId) => fetchCourseDetail(courseId)));
  return {
    detail,
    schedule,
    summary,
    courses: courseResults
      .filter((item): item is PromiseFulfilledResult<CourseDetail> => item.status === 'fulfilled')
      .map((item) => item.value)
  };
};

const resolveCompareIds = async () => {
  const stored = (JSON.parse(sessionStorage.getItem('bitdance_compare_studio_ids') ?? '[]') as number[])
    .map(Number)
    .filter((id) => Number.isFinite(id) && id > 0);
  if (stored.length >= 2) return stored.slice(0, 3);
  return resolveNearbyCompareIds();
};

const resolveNearbyCompareIds = async () => {
  // M1 舞室对比：没有用户选择时从后端附近舞室取前 3 家，避免旧的静态 [1,2,3] 在远端数据库中不存在。
  const nearby = await fetchNearbyStudios({ page: 1, pageSize: 3, distanceKm: 5 });
  return nearby.list.map((studio) => studio.id).slice(0, 3);
};

const loadCompareStudios = async (ids: number[]) => {
  const results = await Promise.allSettled(ids.map(loadCompareStudio));
  return results
    .filter((item): item is PromiseFulfilledResult<CompareStudio> => item.status === 'fulfilled')
    .map((item) => item.value);
};

onMounted(async () => {
  const ids = await resolveCompareIds();
  let loaded = await loadCompareStudios(ids);
  if (loaded.length < 2) {
    // M1 对比兜底：浏览器里残留的旧对比 ID 无效时，回退到当前后端真实附近舞室，避免出现 0 家空表。
    sessionStorage.removeItem('bitdance_compare_studio_ids');
    loaded = await loadCompareStudios(await resolveNearbyCompareIds());
  }
  studios.value = loaded;
  selectedStudioId.value = loaded[0]?.detail.id ?? null;
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="舞室对比" @share="onShare" />

    <section class="pen-body pen-body--compare">
      <header class="compare-head">
        <div>
          <h2 class="pen-h2">{{ studios.length }} 家舞室对比</h2>
          <p class="compare-head__meta">点击任意格子可选中该列，下方收藏和预约会跟着当前选择走。</p>
        </div>
      </header>

      <div class="compare-grid" role="table" aria-label="舞室对比表">
        <div class="compare-grid__row compare-grid__row--head" role="row" :style="gridStyle">
          <div
            v-for="(col, i) in columns"
            :key="col"
            class="compare-cell"
            :class="[
              i === 0 ? 'compare-cell--dim' : 'compare-cell--head',
              i > 0 && studios[i - 1]?.detail.id === selectedStudioId ? 'compare-cell--selected-col' : ''
            ]"
            role="columnheader"
            @click="i > 0 && selectStudio(studios[i - 1].detail.id)"
          >
            <span>{{ col }}</span>
            <em v-if="i > 0 && studios[i - 1]?.detail.id === selectedStudioId">已选中</em>
          </div>
        </div>
        <div v-for="row in rows" :key="row.label" class="compare-grid__row compare-grid__row--body" role="row" :style="gridStyle">
          <div class="compare-cell compare-cell--label" role="rowheader">{{ row.label }}</div>
          <div
            v-for="(val, i) in row.values"
            :key="`${row.label}-${i}`"
            class="compare-cell compare-cell--value"
            :class="{ 'compare-cell--selected-col': studios[i]?.detail.id === selectedStudioId, 'compare-cell--actions': row.label === '操作' }"
            role="cell"
            @click="selectStudio(studios[i].detail.id)"
          >
            <template v-if="row.label === '操作'">
              <!-- M1 对比操作：每列按钮只作用于本列舞室，底部按钮跟随当前选中列。 -->
              <div class="compare-actions">
                <button type="button" class="compare-action" @click="openStudioDetail(studios[i].detail.id)">
                  <ChevronRight :size="14" :stroke-width="2.4" />
                  <span>详情</span>
                </button>
                <button type="button" class="compare-action" @click="toggleStudioFavorite(studios[i])">
                  <Star :size="14" :stroke-width="2.4" :fill="studios[i].detail.favored ? 'currentColor' : 'none'" />
                  <span>{{ studios[i].detail.favored ? '已收藏' : '收藏' }}</span>
                </button>
                <button
                  type="button"
                  class="compare-action compare-action--primary"
                  :class="{ 'compare-action--selected': studios[i]?.detail.id === selectedStudioId }"
                  @click="bookStudio(studios[i].detail.id)"
                >
                  <CalendarDays :size="14" :stroke-width="2.4" />
                  <span>预约</span>
                </button>
              </div>
            </template>
            <span v-else class="compare-cell__text">
              {{ val.text }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="预约"
      :soft-disabled="!selectedStudio"
      :dark-disabled="!selectedStudio"
      @soft="favoriteBest"
      @dark="bookSelected"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;

  &--with-bar {
    padding-bottom: calc(76px + env(safe-area-inset-bottom));
  }
}

.pen-body--compare {
  display: flex;
  flex-direction: column;
  gap: 14px;
  /* M1 对比页：底部有固定操作栏和 App tabbar，内容区留白避免“操作”行被遮住。 */
  padding: 16px 14px calc(120px + env(safe-area-inset-bottom));
}

.pen-h2 {
  @include pen-h2;
}

.compare-head {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__meta {
    margin: 6px 0 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.compare-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__row {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 6px;

    &--head {
      height: 86px;
    }

    &--body .compare-cell {
      min-height: 58px;
      border: 1px solid $pen-hairline;
    }
  }
}

.compare-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  padding: 8px;
  box-sizing: border-box;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
  letter-spacing: 0;
  text-align: center;

  &--dim {
    height: 86px;
    border: none;
    background: $pen-ink;
    color: $pen-on-primary;
    font-weight: 900;
  }

  &--head {
    height: 86px;
    border: none;
    background: $pen-soft;
    color: $pen-ink;
    flex-direction: column;
    gap: 4px;
    font-weight: 900;

    em {
      padding: 3px 8px;
      border-radius: 999px;
      background: $pen-ink;
      color: $pen-on-primary;
      font-size: 10px;
      font-style: normal;
      font-weight: 900;
      line-height: $pen-lh;
    }
  }

  &--label {
    background: $pen-soft;
    color: $pen-ink;
  }

  &--value {
    background: $pen-canvas;
    color: $pen-mute;
  }

  &--selected-col {
    border-color: $pen-ink !important;
    background: #fafafa;
    color: $pen-ink;
  }

  &--actions {
    align-items: stretch;
    padding: 6px;
  }

  &__text {
    overflow-wrap: anywhere;

    &--best {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-height: 28px;
      padding: 4px 10px;
      border-radius: 999px;
      background: $pen-ink;
      color: $pen-on-primary;
      font-size: 11px;
      font-weight: 900;
    }
  }
}

.compare-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.compare-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  min-height: 30px;
  padding: 5px 6px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 11px;
  font-weight: 900;
  line-height: $pen-lh;
  cursor: pointer;

  &--primary {
    background: $pen-soft;
  }

  &--selected {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}
</style>
