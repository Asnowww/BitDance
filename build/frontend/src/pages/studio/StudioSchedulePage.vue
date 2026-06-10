<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import { fetchCoachDetail, fetchCourseDetail, type CoachDetail, type CourseDetail } from '@/api/course';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';

const route = useRoute();
const router = useRouter();
const studioId = Number(route.params.id) || 1;

const view = ref<'day' | 'week'>('day');

const slots = ref<ScheduleSlot[]>([]);
const detail = ref<StudioDetail | null>(null);
const courseMap = ref(new Map<number, CourseDetail>());
const coachMap = ref(new Map<number, CoachDetail>());
const today = new Date();
const days = Array.from({ length: 7 }, (_, index) => {
  const date = new Date(today);
  date.setDate(today.getDate() + index);
  return { w: '日一二三四五六'[date.getDay()], d: String(date.getDate()), date: date.toISOString().slice(0, 10) };
});
const activeDay = ref(days[0].date);
const classes = computed(() =>
  slots.value
    .filter((slot) => view.value === 'week' || slot.startAt.slice(0, 10) === activeDay.value)
    .map((slot) => {
      const start = new Date(slot.startAt);
      const end = new Date(slot.endAt);
      const full = slot.bookedCount >= slot.capacity;
      const course = courseMap.value.get(slot.courseId);
      const coach = coachMap.value.get(slot.coachId);
      return {
        id: slot.id,
        time: start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        dur: `${Math.max(0, Math.round((end.getTime() - start.getTime()) / 60000))}min`,
        title: course?.courseName || `课程 #${slot.courseId}`,
        teacher: `${coach?.displayName || `教练 #${slot.coachId}`} · ${slot.classroomName || '教室待定'}`,
        level: course?.difficultyLevel || slot.status,
        price: full ? '已满员' : '可预约试听',
        full
      };
    })
);

onMounted(async () => {
  const [studioDetail, schedule] = await Promise.all([
    fetchStudioDetail(studioId),
    fetchStudioSchedule(studioId)
  ]);
  detail.value = studioDetail;
  slots.value = schedule;
  // M1 课表页：排期只带 ID，进入页面后补齐课程/教练名称，截图和交互都使用后端关系数据。
  const courseIds = Array.from(new Set(schedule.map((slot) => slot.courseId)));
  const coachIds = Array.from(new Set(schedule.map((slot) => slot.coachId)));
  const [courseResults, coachResults] = await Promise.all([
    Promise.allSettled(courseIds.map((id) => fetchCourseDetail(id))),
    Promise.allSettled(coachIds.map((id) => fetchCoachDetail(id)))
  ]);
  courseMap.value = new Map(
    courseResults
      .filter((item): item is PromiseFulfilledResult<CourseDetail> => item.status === 'fulfilled')
      .map((item) => [item.value.id, item.value])
  );
  coachMap.value = new Map(
    coachResults
      .filter((item): item is PromiseFulfilledResult<CoachDetail> => item.status === 'fulfilled')
      .map((item) => [item.value.id, item.value])
  );
  if (schedule[0]) activeDay.value = schedule[0].startAt.slice(0, 10);
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="周课表" @share="showToast('课表链接已复制')" />

    <section class="pen-scroll">
      <h2 class="studio">{{ detail?.name || `舞室 #${studioId}` }}</h2>

      <div class="toggle">
        <button class="toggle__btn" :class="{ 'toggle__btn--on': view === 'day' }" type="button" @click="view = 'day'">日视图</button>
        <button class="toggle__btn" :class="{ 'toggle__btn--on': view === 'week' }" type="button" @click="view = 'week'">周视图</button>
      </div>

      <div class="week">
        <button
          v-for="d in days"
          :key="d.date"
          class="day"
          :class="{ 'day--on': activeDay === d.date }"
          type="button"
          @click="activeDay = d.date"
        >
          <span class="day__w">{{ d.w }}</span>
          <span class="day__d">{{ d.d }}</span>
        </button>
      </div>

      <h3 class="date-title">{{ activeDay }}</h3>

      <article v-for="c in classes" :key="c.id" class="lesson">
        <div class="lesson__time">
          <strong>{{ c.time }}</strong>
          <span>{{ c.dur }}</span>
        </div>
        <div class="lesson__card">
          <strong class="lesson__title">{{ c.title }}</strong>
          <p class="lesson__teacher">{{ c.teacher }}</p>
          <div class="lesson__foot">
            <span class="chip chip--inactive">{{ c.level }}</span>
            <span class="lesson__price" :class="{ 'lesson__price--full': c.full }">{{ c.price }}</span>
          </div>
        </div>
      </article>
      <p v-if="!classes.length" class="empty-hint">当前日期暂无课程，请切换日视图或查看周视图。</p>
    </section>

    <PenActionBar
      soft-label="导航"
      dark-label="预约试听"
      @soft="showToast('正在打开导航')"
      @dark="router.push(`/studio/${studioId}/trial`)"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.studio { @include pen-h2; }

.toggle {
  display: flex;
  gap: 8px;
  &__btn {
    flex: 1;
    height: 48px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
    &--on { background: $pen-ink; color: $pen-on-primary; }
  }
}

.week { display: flex; gap: 8px; }

.day {
  flex: 1;
  height: 60px;
  border: 0;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;

  &__w { font-size: 12px; font-weight: 700; line-height: $pen-lh; color: $pen-mute; }
  &__d { font-size: 15px; font-weight: 900; line-height: $pen-lh; }

  &--on {
    background: $pen-ink;
    .day__w, .day__d { color: $pen-on-primary; }
  }
}

.date-title { @include pen-h3-section; }

.lesson {
  display: flex;
  gap: 12px;
  align-items: flex-start;

  &__time {
    flex: none;
    width: 56px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    strong { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
    span { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  }

  &__card {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 14px;
    border-radius: 16px;
    background: $pen-soft;
  }

  &__title { font-size: 16px; font-weight: 900; line-height: $pen-lh; }
  &__teacher { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__foot { display: flex; align-items: center; justify-content: space-between; }
  &__price { font-size: 14px; font-weight: 800; line-height: $pen-lh; &--full { color: $pen-mute; } }
}

.chip {
  @include pen-chip;
  height: 32px;
  padding: 6px 12px;
  font-size: 12px;
}

.empty-hint {
  // M1 日/周课表空态：当选中日期无课时，引导用户切换视图而不是留下空白区域。
  margin: 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
