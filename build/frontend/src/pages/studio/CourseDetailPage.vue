<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchCourseDetail, type CourseDetail } from '@/api/course';
import { useFavoriteStore } from '@/stores/favorite';

const route = useRoute();
const router = useRouter();
const fav = useFavoriteStore();
const detail = ref<CourseDetail | null>(null);
const loading = ref(true);
const courseId = computed(() => Number(route.params.id));

onMounted(async () => {
  try {
    detail.value = await fetchCourseDetail(courseId.value);
  } finally {
    loading.value = false;
  }
});

const onFav = () => {
  if (!detail.value) return;
  fav.toggle({
    targetType: 'course',
    targetId: detail.value.id,
    title: detail.value.name,
    subtitle: `${detail.value.style} · ¥${detail.value.price}`
  });
};
</script>

<template>
  <div v-if="loading" class="loading">加载中…</div>
  <div v-else-if="detail" class="page">
    <header class="hero">
      <button class="back" @click="router.back()">←</button>
      <div class="hero__title">{{ detail.name }}</div>
      <div class="hero__sub">{{ detail.style }} · {{ detail.difficulty }}</div>
    </header>
    <section class="grid">
      <div class="cell"><span>价格</span><strong>¥{{ detail.price }}</strong></div>
      <div class="cell"><span>时长</span><strong>{{ detail.durationMin }}min</strong></div>
      <div class="cell"><span>强度</span><strong>{{ '🔥'.repeat(detail.intensity) }}</strong></div>
      <div class="cell"><span>频次</span><strong>{{ detail.frequency }}</strong></div>
    </section>
    <section class="block">
      <h3>课程介绍</h3>
      <p>{{ detail.intro }}</p>
    </section>
    <section class="block">
      <h3>授课老师</h3>
      <div class="coach" @click="router.push(`/coach/${detail.coachId}`)">
        <div class="coach__avatar">{{ detail.coachName.charAt(0) }}</div>
        <div class="coach__name">{{ detail.coachName }}</div>
        <span class="coach__more">查看 →</span>
      </div>
    </section>
    <section class="block">
      <h3>所属舞室</h3>
      <div class="row" @click="router.push(`/studio/${detail.studioId}`)">
        <span>🏠 {{ detail.studioName }}</span>
        <span class="row__action">查看 →</span>
      </div>
    </section>
    <footer class="footer">
      <button class="btn btn--ghost" @click="onFav">
        {{ fav.isFav('course', detail.id) ? '♥ 已收藏' : '♡ 收藏' }}
      </button>
      <button class="btn btn--primary" @click="router.push(`/studio/${detail.studioId}/trial?courseId=${detail.id}`)">
        预约试听
      </button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
}
.loading {
  padding: 80px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.hero {
  position: relative;
  padding: 60px 20px 24px;
  background: linear-gradient(135deg, #ffd2da, #ff7799);
  color: #fff;
  &__title {
    font-size: 22px;
    font-weight: 700;
  }
  &__sub {
    margin-top: 6px;
    font-size: 13px;
    opacity: 0.9;
  }
}
.back {
  position: absolute;
  top: calc(12px + env(safe-area-inset-top));
  left: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 18px;
  cursor: pointer;
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1px;
  background: var(--bd-border);
  margin-top: 8px;
}
.cell {
  background: #fff;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  span {
    color: var(--bd-text-secondary);
    font-size: 11px;
  }
  strong {
    color: var(--bd-primary);
  }
}
.block {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 8px;
    font-size: 15px;
  }
  p {
    margin: 0;
    font-size: 13px;
    line-height: 1.6;
  }
}
.coach,
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
}
.coach__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.coach__name {
  flex: 1;
  font-size: 14px;
}
.coach__more,
.row__action {
  font-size: 12px;
  color: var(--bd-primary);
}
.row {
  justify-content: space-between;
}
.footer {
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
</style>
