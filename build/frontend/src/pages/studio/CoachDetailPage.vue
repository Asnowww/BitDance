<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import { fetchCoachDetail, type CoachDetail } from '@/api/course';
import { toggleFavorite } from '@/api/favorite';

const route = useRoute();
const router = useRouter();
const coachId = Number(route.params.id) || 1;
const detail = ref<CoachDetail | null>(null);
const favored = computed(() => detail.value?.favored ?? false);

const stats = [
  { value: '4.9', label: '耐心' },
  { value: '4.8', label: '纠错' },
  { value: '4.9', label: '讲解' }
];

const course = {
  id: 'jazz-basic',
  title: 'Jazz 基础律动',
  meta: '明天 20:00 · Urban Flow',
  tag: '初级',
  price: '¥99 / 节'
};

const onBook = () => router.push(`/course/${course.id}`);
const toggleCoachFavorite = async () => {
  const result = await toggleFavorite('coach', coachId);
  if (detail.value) detail.value.favored = result.favored;
  showToast(result.favored ? '已收藏' : '已取消收藏');
};

onMounted(async () => {
  detail.value = await fetchCoachDetail(coachId);
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="老师详情" @share="showToast('老师主页链接已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <div class="hero__bars" aria-hidden="true">
          <span v-for="i in 6" :key="i" />
        </div>
        <Music class="hero__icon" :size="42" :stroke-width="2" />
        <strong class="hero__title">MIA</strong>
        <p class="hero__meta">{{ detail?.styles.map((item) => `#${item.danceStyleId}`).join(' / ') || '舞种待完善' }}</p>
      </section>

      <section class="body">
        <h2 class="body__title">{{ detail?.displayName || '老师详情' }}</h2>
        <p class="body__sub">{{ detail?.certificationStatus || '待认证' }} · {{ detail?.teachingStyle || '教学风格待完善' }}</p>

        <div class="stats">
          <div v-for="stat in stats" :key="stat.label" class="stat">
            <strong class="stat__value">{{ stat.value }}</strong>
            <span class="stat__label">{{ stat.label }}</span>
          </div>
        </div>

        <header class="section-head">
          <h3>可预约课程</h3>
          <button type="button" class="section-head__more" @click="showToast('查看全部课程')">全部</button>
        </header>

        <article class="course" @click="router.push(`/course/${course.id}`)">
          <div class="course__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="course__body">
            <strong class="course__title">{{ course.title }}</strong>
            <p class="course__meta">{{ course.meta }}</p>
            <span class="tag">{{ course.tag }}</span>
            <span class="course__price">{{ course.price }}</span>
          </div>
        </article>
      </section>
    </section>

    <PenActionBar
      :soft-label="favored ? '已收藏' : '收藏'"
      dark-label="预约课程"
      @soft="toggleCoachFavorite"
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
}

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 216px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__bars {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
    height: 30px;
    margin-bottom: auto;

    span {
      height: 100%;
      background: $pen-charcoal;
    }
  }

  &__icon {
    flex-shrink: 0;
    color: $pen-on-primary;
  }

  &__title {
    margin: 0;
    font-size: 32px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 18px 20px;

  &__title {
    margin: 0;
    font-size: 26px;
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

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  height: 92px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border-radius: 16px;
  background: $pen-soft;

  &__value {
    font-size: 22px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__label {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.section-head {
  display: flex;
  align-items: center;
  gap: 8px;

  h3 {
    @include pen-h3-section;
    flex: 1;
  }

  &__more {
    border: 0;
    background: transparent;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
  }
}

.course {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 124px;
  cursor: pointer;

  &__cover {
    flex: none;
    display: grid;
    place-items: center;
    width: 112px;
    align-self: stretch;
    border-radius: 14px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 4px 0;
  }

  &__title {
    font-size: 16px;
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

  &__price {
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.tag {
  align-self: flex-start;
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
