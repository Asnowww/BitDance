<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchCoachDetail, type CoachDetail } from '@/api/course';
import { useFavoriteStore } from '@/stores/favorite';

const route = useRoute();
const router = useRouter();
const fav = useFavoriteStore();
const detail = ref<CoachDetail | null>(null);
const loading = ref(true);
const coachId = computed(() => Number(route.params.id));

onMounted(async () => {
  try {
    detail.value = await fetchCoachDetail(coachId.value);
  } finally {
    loading.value = false;
  }
});

const onFav = () => {
  if (!detail.value) return;
  fav.toggle({
    targetType: 'coach',
    targetId: detail.value.id,
    title: detail.value.name,
    subtitle: `${detail.value.style} · ★${detail.value.ratingAvg}`
  });
};
</script>

<template>
  <div v-if="loading" class="loading">加载中…</div>
  <div v-else-if="detail" class="page">
    <header class="head">
      <button class="back" @click="router.back()">←</button>
      <div class="avatar">{{ detail.name.charAt(0) }}</div>
      <div class="name">{{ detail.name }}</div>
      <div class="meta">
        <span>{{ detail.style }}</span>
        <span>·</span>
        <span>★ {{ detail.ratingAvg }} ({{ detail.reviewCount }})</span>
      </div>
      <div class="studio" @click="router.push(`/studio/${detail.studioId}`)">
        所属：{{ detail.studioName }} →
      </div>
    </header>

    <section class="block">
      <h3>教学风格</h3>
      <p>{{ detail.teachStyle }}</p>
    </section>

    <section class="block">
      <h3>个人介绍</h3>
      <p>{{ detail.intro }}</p>
    </section>

    <section class="block">
      <h3>代表作品</h3>
      <div class="works">
        <div v-for="w in detail.works" :key="w.id" class="works__item">
          <span class="works__type">{{ w.type === 'video' ? '▶' : '🖼' }}</span>
          <span class="works__title">{{ w.title }}</span>
        </div>
      </div>
    </section>

    <section class="block">
      <h3>可上课程</h3>
      <div class="course-list">
        <div
          v-for="c in detail.courses"
          :key="c.id"
          class="course"
          @click="router.push(`/course/${c.id}`)"
        >
          <span>{{ c.name }}</span>
          <span class="course__diff">{{ c.difficulty }}</span>
        </div>
      </div>
    </section>

    <section class="block">
      <h3>可约时段</h3>
      <div class="slots">
        <span v-for="s in detail.availableSlots" :key="`${s.day}-${s.time}`" class="slot">
          {{ s.day }} {{ s.time }}
        </span>
      </div>
    </section>

    <footer class="footer">
      <button class="btn btn--ghost" @click="onFav">
        {{ fav.isFav('coach', detail.id) ? '♥ 已收藏' : '♡ 收藏' }}
      </button>
      <button class="btn btn--primary" @click="router.push(`/studio/${detail.studioId}/trial?coachId=${detail.id}`)">
        预约 TA 的课
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
.head {
  background: linear-gradient(180deg, #ffe2e8, #fff);
  padding: 80px 24px 24px;
  text-align: center;
  position: relative;
}
.back {
  position: absolute;
  top: calc(12px + env(safe-area-inset-top));
  left: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.1);
  font-size: 18px;
  cursor: pointer;
}
.avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.name {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 700;
}
.meta {
  margin-top: 6px;
  display: inline-flex;
  gap: 6px;
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.studio {
  margin-top: 8px;
  font-size: 12px;
  color: var(--bd-primary);
  cursor: pointer;
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
.works {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  &__item {
    aspect-ratio: 1;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    border-radius: 10px;
    color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }
  &__type {
    font-size: 22px;
  }
  &__title {
    font-size: 12px;
  }
}
.course-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.course {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
  &__diff {
    color: var(--bd-text-secondary);
    font-size: 11px;
  }
}
.slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.slot {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(54, 165, 255, 0.1);
  color: #36a5ff;
  font-size: 12px;
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
