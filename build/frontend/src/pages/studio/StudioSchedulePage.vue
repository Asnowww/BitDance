<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';

const route = useRoute();
const router = useRouter();
const studioId = Number(route.params.id);

const slots = ref<ScheduleSlot[]>([]);
const loading = ref(true);
const activeDate = ref('');

onMounted(async () => {
  try {
    slots.value = await fetchStudioSchedule(studioId);
    if (slots.value.length) activeDate.value = slots.value[0].date;
  } finally {
    loading.value = false;
  }
});

const dateList = computed(() => {
  const seen = new Set<string>();
  const out: Array<{ date: string; weekday: string }> = [];
  slots.value.forEach((s) => {
    if (!seen.has(s.date)) {
      seen.add(s.date);
      out.push({ date: s.date, weekday: s.weekday });
    }
  });
  return out;
});

const dailySlots = computed(() => slots.value.filter((s) => s.date === activeDate.value));
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">周课表</span>
    </header>
    <nav class="dates">
      <button
        v-for="d in dateList"
        :key="d.date"
        class="date"
        :class="{ active: d.date === activeDate }"
        @click="activeDate = d.date"
      >
        <span class="date__weekday">{{ d.weekday }}</span>
        <span class="date__num">{{ d.date.slice(8) }}</span>
      </button>
    </nav>
    <section v-if="loading" class="empty">加载中…</section>
    <section v-else-if="!dailySlots.length" class="empty">这天暂无课程</section>
    <section v-else class="slots">
      <article
        v-for="s in dailySlots"
        :key="s.id"
        class="slot"
        @click="router.push(`/course/${s.courseId}`)"
      >
        <div class="slot__time">{{ s.time }}</div>
        <div class="slot__body">
          <div class="slot__name">{{ s.courseName }}</div>
          <div class="slot__meta">
            <span>{{ s.style }}</span>
            <span>·</span>
            <span>{{ s.difficulty }}</span>
            <span>·</span>
            <span>{{ s.coachName }}</span>
          </div>
        </div>
        <div class="slot__cap">{{ s.taken }}/{{ s.capacity }}</div>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 24px;
}
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.dates {
  display: flex;
  gap: 4px;
  padding: 12px 12px 8px;
  overflow-x: auto;
}
.date {
  flex-shrink: 0;
  width: 56px;
  padding: 8px 0;
  border: 1px solid var(--bd-border);
  border-radius: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: var(--bd-text-secondary);
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
  &__num {
    font-size: 16px;
    font-weight: 700;
  }
}
.slots {
  padding: 4px 12px;
}
.slot {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  &__time {
    width: 80px;
    font-size: 13px;
    font-weight: 600;
    color: var(--bd-primary);
  }
  &__body {
    flex: 1;
    min-width: 0;
  }
  &__name {
    font-size: 14px;
    font-weight: 600;
  }
  &__meta {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
    display: flex;
    gap: 4px;
  }
  &__cap {
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
.empty {
  padding: 60px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
</style>
