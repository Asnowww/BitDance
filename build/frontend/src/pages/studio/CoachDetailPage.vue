<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Heart, Music, UserRound } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import ReviewAggregatePanel from '@/components/review/ReviewAggregatePanel.vue';
import { fetchCoachDetail, type CoachDetail } from '@/api/course';
import { toggleFavorite } from '@/api/favorite';

const route = useRoute();
const router = useRouter();
const coachId = computed(() => Number(route.params.id) || 1);
const detail = ref<CoachDetail | null>(null);
const favored = computed(() => detail.value?.favored ?? false);

const styleNames = ['Hip-hop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'K-pop', 'Waacking', 'Urban'];

const styleText = computed(() => {
  const styles = detail.value?.styles ?? [];
  if (!styles.length) return '擅长舞种待完善';
  return styles.map((item) => styleNames[(item.danceStyleId - 1) % styleNames.length]).join(' / ');
});

const course = {
  id: 3,
  title: 'Mira Locking Workshop',
  meta: '明天 20:00 · Urban Flow',
  tag: '初级',
  price: '¥99 / 节'
};

const onBook = () => router.push(`/course/${course.id}`);

const toggleCoachFavorite = async () => {
  const result = await toggleFavorite('coach', coachId.value);
  if (detail.value) detail.value.favored = result.favored;
  showToast(result.favored ? '已收藏' : '已取消收藏');
};

onMounted(async () => {
  detail.value = await fetchCoachDetail(coachId.value);
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="老师详情" @share="showToast('老师主页链接已复制')" />

    <section class="pen-scroll">
      <section class="body">
        <section class="profile-card">
          <div class="profile-card__avatar">
            <UserRound :size="34" :stroke-width="2" />
          </div>
          <div class="profile-card__content">
            <h1>{{ detail?.displayName || 'Mira Chen' }}</h1>
            <p>{{ styleText }} · {{ detail?.teachingStyle || '零基础友好，可预约课程' }}</p>
          </div>
          <button type="button" class="profile-card__fav" @click="toggleCoachFavorite">
            <Heart :size="18" :fill="favored ? 'currentColor' : 'none'" />
          </button>
        </section>

        <section class="intro-card">
          <h2>教学风格</h2>
          <p>{{ detail?.intro || '注重基础律动与节奏感，会把动作拆成小节，适合零基础和进阶练习。' }}</p>
          <span>{{ detail?.availableTimeSlots || '周一 / 周三 19:00-20:30，周六 14:00-16:00' }}</span>
        </section>

        <ReviewAggregatePanel target-type="coach" :target-id="coachId" />

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

.body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 0 18px 20px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 16px;
  border-radius: 22px;
  background: #efece6;

  &__avatar {
    display: grid;
    flex: none;
    place-items: center;
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  &__content {
    min-width: 0;
    flex: 1;
  }

  &__fav {
    display: grid;
    flex: none;
    place-items: center;
    width: 36px;
    height: 36px;
    border: 0;
    border-radius: 50%;
    background: $pen-canvas;
    color: $pen-ink;
    cursor: pointer;
  }

  h1 {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 900;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.45;
  }
}

.intro-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: 18px;
  background: $pen-soft;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p,
  span {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.45;
  }

  span {
    color: $pen-ink;
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
    font-weight: 800;
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
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__price {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}

.tag {
  align-self: flex-start;
  height: 34px;
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
}
</style>
