<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { useFavoriteStore } from '@/stores/favorite';

const route = useRoute();
const router = useRouter();
const fav = useFavoriteStore();

const detail = ref<StudioDetail | null>(null);
const loading = ref(true);

const studioId = computed(() => Number(route.params.id));

onMounted(async () => {
  try {
    detail.value = await fetchStudioDetail(studioId.value);
  } finally {
    loading.value = false;
  }
});

const onToggleFav = () => {
  if (!detail.value) return;
  fav.toggle({
    targetType: 'studio',
    targetId: detail.value.id,
    title: detail.value.name,
    subtitle: `${detail.value.area} · ★${detail.value.ratingAvg}`
  });
};

const onNavigate = () => showToast('地图导航占位（接入腾讯地图后可调用）');
</script>

<template>
  <div v-if="loading" class="page-stub">加载中…</div>
  <div v-else-if="detail" class="detail">
    <div class="hero">
      <div class="hero__cover">
        <span class="hero__title">{{ detail.name }}</span>
      </div>
      <button class="hero__back" @click="router.back()">←</button>
      <button class="hero__fav" @click="onToggleFav">
        {{ fav.isFav('studio', detail.id) ? '♥ 已收藏' : '♡ 收藏' }}
      </button>
    </div>
    <section class="info">
      <div class="info__name">{{ detail.name }}</div>
      <div class="info__meta">
        <span class="rating">★ {{ detail.ratingAvg }}</span>
        <span>{{ detail.reviewCount }} 条评价</span>
        <span>·</span>
        <span>{{ detail.area }}</span>
        <span>·</span>
        <span>{{ detail.distanceKm }}km</span>
      </div>
      <div class="info__tags">
        <span v-for="t in detail.topStyles" :key="t" class="tag">{{ t }}</span>
        <span v-if="detail.beginnerFriendly" class="tag tag--accent">零基础友好</span>
      </div>
    </section>

    <section class="block">
      <h3 class="block__title">舞室简介</h3>
      <p class="block__text">{{ detail.intro }}</p>
    </section>

    <section class="block">
      <h3 class="block__title">营业信息</h3>
      <div class="row" @click="onNavigate">
        <span class="row__label">📍</span>
        <span class="row__value">{{ detail.address }}</span>
        <span class="row__action">导航</span>
      </div>
      <div class="row">
        <span class="row__label">🕐</span>
        <span class="row__value">{{ detail.openHours }}</span>
      </div>
    </section>

    <section class="block">
      <div class="block__head">
        <h3 class="block__title">主打课程</h3>
        <button class="block__more" @click="router.push(`/studio/${detail.id}/schedule`)">
          周课表 →
        </button>
      </div>
      <div class="courses">
        <article
          v-for="c in detail.courses"
          :key="c.id"
          class="course"
          @click="router.push(`/course/${c.id}`)"
        >
          <div class="course__name">{{ c.name }}</div>
          <div class="course__meta">{{ c.style }} · {{ c.difficulty }}</div>
          <div class="course__price">¥{{ c.price }}</div>
        </article>
      </div>
    </section>

    <footer class="detail-footer">
      <button class="btn btn--ghost" @click="router.push(`/studio/${detail.id}/reviews`)">
        看评价
      </button>
      <button class="btn btn--primary" @click="router.push(`/studio/${detail.id}/trial`)">
        试听预约
      </button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.detail {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
}
.hero {
  position: relative;
  height: 240px;
  &__cover {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #ff7799, #ff2442);
    display: flex;
    align-items: flex-end;
    padding: 24px;
    color: #fff;
  }
  &__title {
    font-size: 22px;
    font-weight: 700;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
  }
  &__back,
  &__fav {
    position: absolute;
    top: calc(12px + env(safe-area-inset-top));
    background: rgba(0, 0, 0, 0.35);
    color: #fff;
    border: none;
    height: 32px;
    border-radius: 16px;
    padding: 0 12px;
    font-size: 14px;
    cursor: pointer;
    backdrop-filter: blur(6px);
  }
  &__back {
    left: 12px;
    width: 32px;
    padding: 0;
  }
  &__fav {
    right: 12px;
  }
}
.info {
  padding: 16px;
  background: var(--bd-surface);
  &__name {
    font-size: 18px;
    font-weight: 700;
  }
  &__meta {
    margin-top: 6px;
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__tags {
    margin-top: 10px;
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}
.rating {
  color: #ffaa33;
  font-weight: 600;
}
.tag {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 8px;
  background: rgba(255, 36, 66, 0.08);
  color: var(--bd-primary);
  &--accent {
    background: rgba(54, 165, 255, 0.1);
    color: #36a5ff;
  }
}
.block {
  margin-top: 8px;
  padding: 16px;
  background: var(--bd-surface);
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  &__title {
    margin: 0 0 8px;
    font-size: 15px;
    font-weight: 600;
  }
  &__text {
    margin: 0;
    font-size: 13px;
    line-height: 1.6;
    color: var(--bd-text);
  }
  &__more {
    border: none;
    background: none;
    font-size: 12px;
    color: var(--bd-primary);
    cursor: pointer;
  }
}
.row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 13px;
  &__label {
    width: 22px;
    text-align: center;
  }
  &__value {
    flex: 1;
    color: var(--bd-text);
  }
  &__action {
    color: var(--bd-primary);
    font-size: 12px;
  }
}
.courses {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.course {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  &__name {
    flex: 1;
    font-size: 14px;
    font-weight: 600;
  }
  &__meta {
    font-size: 11px;
    color: var(--bd-text-secondary);
    margin-right: 8px;
  }
  &__price {
    color: var(--bd-primary);
    font-weight: 700;
    font-size: 14px;
  }
}
.detail-footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-top: 1px solid var(--bd-border);
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  display: flex;
  gap: 10px;
  z-index: 50;
}
.btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  &--primary {
    background: var(--bd-primary);
    color: #fff;
  }
  &--ghost {
    background: rgba(255, 36, 66, 0.08);
    color: var(--bd-primary);
  }
}
.page-stub {
  padding: 80px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
</style>
