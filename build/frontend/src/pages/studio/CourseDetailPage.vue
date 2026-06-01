<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';
import { fetchCourseDetail, type CourseDetail } from '@/api/course';
import { toggleFavorite } from '@/api/favorite';

const route = useRoute();
const router = useRouter();
const courseId = Number(route.params.id) || 1;
const detail = ref<CourseDetail | null>(null);
const favored = computed(() => detail.value?.favored ?? false);

const audiences = ['零基础', '想减脂', '喜欢成品舞'];
const structuredReviews = [
  { label: '上手难度', value: '低' },
  { label: '节奏合理性', value: '4.8' },
  { label: '练习强度', value: '中' },
  { label: '实际收获', value: '4.9' }
];

const onBook = () => router.push(`/studio/${detail.value?.studioId ?? 1}/trial?courseId=${courseId}`);
const toggleCourseFavorite = async () => {
  const result = await toggleFavorite('course', courseId);
  if (detail.value) detail.value.favored = result.favored;
  showToast(result.favored ? '已收藏' : '已取消收藏');
};

onMounted(async () => {
  detail.value = await fetchCourseDetail(courseId);
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
          <strong class="coach__name">小鹿老师</strong>
          <p class="coach__meta">擅长韩舞 / Jazz · 6年教学</p>
        </div>
        <span class="tag">认证</span>
      </article>

      <section class="block">
        <h3 class="block__title">适合人群</h3>
        <div class="chip-row">
          <span v-for="item in audiences" :key="item" class="tag">{{ item }}</span>
        </div>
      </section>

      <section class="block">
        <header class="block__head">
          <h3 class="block__title">结构化评价</h3>
          <span class="block__count">128 条</span>
        </header>
        <div class="rows">
          <PenFieldRow
            v-for="row in structuredReviews"
            :key="row.label"
            :label="row.label"
            :value="row.value"
            @click="showToast(`${row.label}：${row.value}`)"
          />
        </div>
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
