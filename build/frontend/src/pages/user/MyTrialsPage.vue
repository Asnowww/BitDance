<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchCourseDetail, type CourseDetail } from '@/api/course';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { cancelTrialBooking, fetchMyTrialBookings, fetchStudioSchedule, type ScheduleSlot, type TrialBooking } from '@/api/trial';

const router = useRouter();
const cats = ['全部', '待确认', '已确认', '已完成'];
const activeCat = ref('待确认');

const bookings = ref<TrialBooking[]>([]);
const courseMap = ref<Record<number, CourseDetail>>({});
const studioMap = ref<Record<number, StudioDetail>>({});
const scheduleMap = ref<Record<number, ScheduleSlot>>({});
const statusText: Record<string, string> = {
  pending: '待舞室确认', confirmed: '已确认 · 待上课', attended: '已完成', arrived: '已完成',
  noshow: '未到场', rejected: '已拒绝', canceled: '已取消'
};

const formatTime = (value?: string) => {
  if (!value) return '时间待舞室确认';
  const date = new Date(value);
  if (Number.isNaN(date.getTime()) || date.getFullYear() <= 1970) return '时间待舞室确认';
  return date.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};

const records = computed(() => bookings.value.map((item) => ({
  id: String(item.id),
  title: studioMap.value[item.studioId]?.name || `舞室 #${item.studioId}`,
  // M1 试听预约：旧数据 createdAt 可能为空，优先展示课表 startAt，再用创建时间兜底，避免 1970 日期。
  meta: `${courseMap.value[item.courseId]?.courseName || `课程 #${item.courseId}`} · ${formatTime(scheduleMap.value[item.courseScheduleId ?? 0]?.startAt || item.createdAt)}`,
  status: statusText[item.bookingStatus] ?? item.bookingStatus,
  tone: item.bookingStatus === 'confirmed' ? 'success' : item.bookingStatus === 'pending' ? 'ink' : 'mute',
  action: item.bookingStatus === 'pending' ? '取消预约' : '查看详情'
})));
const onAction = async (id: string, action: string) => {
  if (action !== '取消预约') return showToast(action);
  await cancelTrialBooking(Number(id));
  await loadBookings();
};

const loadBookings = async () => {
  bookings.value = await fetchMyTrialBookings();
  const courseIds = Array.from(new Set(bookings.value.map((item) => item.courseId)));
  const studioIds = Array.from(new Set(bookings.value.map((item) => item.studioId)));
  const [courseResults, studioResults, scheduleResults] = await Promise.all([
    Promise.allSettled(courseIds.map((id) => fetchCourseDetail(id))),
    Promise.allSettled(studioIds.map((id) => fetchStudioDetail(id))),
    // M1 试听预约：预约 DTO 只有 scheduleId，这里按舞室批量拉课表补出真实上课时间。
    Promise.allSettled(studioIds.map((id) => fetchStudioSchedule(id)))
  ]);
  courseMap.value = Object.fromEntries(
    courseResults
      .filter((item): item is PromiseFulfilledResult<CourseDetail> => item.status === 'fulfilled')
      .map((item) => [item.value.id, item.value])
  );
  studioMap.value = Object.fromEntries(
    studioResults
      .filter((item): item is PromiseFulfilledResult<StudioDetail> => item.status === 'fulfilled')
      .map((item) => [item.value.id, item.value])
  );
  scheduleMap.value = Object.fromEntries(
    scheduleResults
      .filter((item): item is PromiseFulfilledResult<ScheduleSlot[]> => item.status === 'fulfilled')
      .flatMap((item) => item.value)
      .map((item) => [item.id, item])
  );
};

onMounted(loadBookings);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的试听" :show-share="false" />

    <section class="pen-scroll">
      <div class="chip-row">
        <button
          v-for="c in cats"
          :key="c"
          class="chip"
          :class="activeCat === c ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeCat = c"
        >
          {{ c }}
        </button>
      </div>

      <article v-for="r in records" :key="r.id" class="rec">
        <div class="rec__cover" aria-hidden="true"><Music :size="26" :stroke-width="2" /></div>
        <div class="rec__body">
          <strong class="rec__title">{{ r.title }}</strong>
          <p class="rec__meta">{{ r.meta }}</p>
          <div class="rec__foot">
            <span class="rec__status" :class="`rec__status--${r.tone}`">{{ r.status }}</span>
            <button class="rec__btn" type="button" @click="onAction(r.id, r.action)">{{ r.action }}</button>
          </div>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.rec {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__cover {
    flex: none; width: 88px; height: 88px; border-radius: 12px;
    background: $pen-soft; color: $pen-ink; display: grid; place-items: center;
  }
  &__body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
  &__title { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__foot { display: flex; align-items: center; justify-content: space-between; gap: 8px; }

  &__status {
    font-size: 13px; font-weight: 800; line-height: $pen-lh;
    &--ink { color: $pen-ink; }
    &--success { color: $pen-success; }
    &--mute { color: $pen-mute; }
  }

  &__btn {
    flex: none; height: 34px; padding: 6px 14px;
    border: 1px solid $pen-ink; border-radius: 999px;
    background: $pen-canvas; color: $pen-ink;
    font-size: 13px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}
</style>
