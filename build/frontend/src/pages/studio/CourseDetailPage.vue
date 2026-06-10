<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import ReviewAggregatePanel from '@/components/review/ReviewAggregatePanel.vue';
import { fetchCoachDetail, fetchCourseDetail, type CoachDetail, type CourseDetail } from '@/api/course';
import { toggleFavorite } from '@/api/favorite';

const route = useRoute();
const router = useRouter();
const courseId = Number(route.params.id) || 1;
const detail = ref<CourseDetail | null>(null);
const coach = ref<CoachDetail | null>(null);
const favored = computed(() => detail.value?.favored ?? false);

const audienceLabels: Record<string, string> = {
  adult: '成人',
  beginner: '零基础',
  teen: '青少年',
  kids: '少儿',
  advanced: '进阶',
  fitness: '塑形减脂'
};

const audiences = computed(() =>
  // M1 课程详情：远端种子数据可能是 {adult,beginner} 这类枚举串，先清洗为用户可读标签。
  (detail.value?.targetAudience || '零基础,想减脂,喜欢成品舞')
    .replace(/[{}"]/g, '')
    .split(/[、,，/]/)
    .map((item) => audienceLabels[item.trim()] ?? item.trim())
    .filter(Boolean)
    .slice(0, 4)
);

const coachMeta = computed(() =>
  [
    coach.value?.teachingStyle,
    coach.value?.avgRating ? `评分 ${coach.value.avgRating.toFixed(1)}` : '',
    detail.value?.durationMinutes ? `${detail.value.durationMinutes}min` : ''
  ].filter(Boolean).join(' · ') || '老师信息待完善'
);

const onBook = () => router.push(`/studio/${detail.value?.studioId ?? 1}/trial?courseId=${courseId}`);
const toggleCourseFavorite = async () => {
  const result = await toggleFavorite('course', courseId);
  if (detail.value) detail.value.favored = result.favored;
  showToast(result.favored ? '已收藏' : '已取消收藏');
};

onMounted(async () => {
  detail.value = await fetchCourseDetail(courseId);
  // M1 课程详情：课程主档返回 coachId 后，再拉取老师主档，保证“老师详情入口”来自后端关系。
  coach.value = await fetchCoachDetail(detail.value.coachId).catch(() => null);
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="课程详情" @share="showToast('课程链接已复制')" />

    <section class="pen-scroll">
      <header class="head">
        <h2 class="head__title">{{ detail?.courseName || '课程详情' }}</h2>
        <p class="head__sub">{{ detail?.difficultyLevel || '-' }} · {{ detail?.intensityLevel || '-' }} · ¥{{ detail?.priceAmount ?? '-' }} · {{ detail?.durationMinutes ?? '-' }}min</p>
      </header>

      <article class="coach" @click="router.push(`/coach/${detail?.coachId ?? 1}`)">
        <span class="coach__avatar" aria-hidden="true" />
        <div class="coach__copy">
          <strong class="coach__name">{{ coach?.displayName || `教练 #${detail?.coachId ?? '-'}` }}</strong>
          <p class="coach__meta">{{ coachMeta }}</p>
        </div>
        <span class="tag">{{ coach?.certificationStatus || '认证待核验' }}</span>
      </article>

      <section class="block">
        <h3 class="block__title">适合人群</h3>
        <div class="chip-row">
          <span v-for="item in audiences" :key="item" class="tag">{{ item }}</span>
        </div>
      </section>

      <section class="block">
        <header class="block__head">
          <h3 class="block__title">评价聚合</h3>
        </header>
        <ReviewAggregatePanel
          target-type="course"
          :target-id="courseId"
          :target-name="detail?.courseName || '课程'"
        />
      </section>
    </section>

    <PenActionBar
      :soft-label="favored ? '已收藏' : '收藏'"
      dark-label="预约 / 报名"
      @soft="toggleCourseFavorite"
      @dark="onBook"
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

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.head {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__title {
    margin: 0;
    font-size: 28px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__sub {
    margin: 0;
    color: $pen-mute;
    font-size: 14px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.coach {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: $pen-soft;
  cursor: pointer;

  &__avatar {
    flex: none;
    width: 56px;
    height: 56px;
    border-radius: 999px;
    background: $pen-ink;
  }

  &__copy {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__name {
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }
}

.block {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__title {
    @include pen-h3-section;
    flex: 1;
  }

  &__count {
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.rows {
  display: flex;
  flex-direction: column;
}

.tag {
  height: 40px;
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
