<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { createTrialBooking, fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';

const route = useRoute();
const router = useRouter();
const studioId = Number(route.params.id) || 1;
const requestedCourseId = Number(route.query.courseId) || undefined;
const detail = ref<StudioDetail | null>(null);
const schedule = ref<ScheduleSlot[]>([]);

const today = new Date();
const days = Array.from({ length: 7 }, (_, index) => {
  const date = new Date(today);
  date.setDate(today.getDate() + index);
  return { w: '日一二三四五六'[date.getDay()], d: String(date.getDate()), date: date.toISOString().slice(0, 10) };
});
const activeDay = ref(days[0].date);
const availableSlots = computed(() =>
  schedule.value.filter((slot) => slot.startAt.slice(0, 10) === activeDay.value)
);
const slots = computed(() =>
  availableSlots.value.map((slot) =>
    new Date(slot.startAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  )
);
const activeSlot = ref('');
const selectedSlot = computed(() => {
  const selectedIndex = slots.value.indexOf(activeSlot.value);
  return availableSlots.value[selectedIndex] ?? availableSlots.value[0] ?? null;
});
const studioName = computed(() => detail.value?.name || '正在读取舞室信息');
const studioMeta = computed(() => {
  if (!detail.value) return '正在读取后端舞室地址与交通信息';
  return [detail.value.address, detail.value.transportInfo].filter(Boolean).join(' · ') || '舞室信息待完善';
});
const studioAvailability = computed(() => {
  if (!schedule.value.length) return '暂无可预约课表';
  if (!availableSlots.value.length) return '当天暂无可选时段';
  return `${availableSlots.value.length} 个可选时段`;
});

const fields = [
  { label: '姓名', value: '请输入称呼' },
  { label: '手机号', value: '138••••6789' },
  { label: '舞蹈基础', value: '零基础' }
];

const onConfirm = async () => {
  const selected = selectedSlot.value;
  if (!selected) {
    showFailToast('当前舞室暂无可预约时段');
    return;
  }
  await createTrialBooking({
    courseId: requestedCourseId ?? selected.courseId,
    courseScheduleId: selected.id,
    contactPhone: '13800000789',
    bookingNote: '试听预约'
  });
  showSuccessToast('已提交，等待舞室确认');
  router.push('/me/trials');
};

onMounted(async () => {
  [detail.value, schedule.value] = await Promise.all([
    fetchStudioDetail(studioId),
    fetchStudioSchedule(studioId)
  ]);
  activeSlot.value = slots.value[0] ?? '';
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="试听预约" @share="showSuccessToast('链接已复制')" />

    <section class="pen-scroll">
      <div class="studio">
        <div class="studio__cover" aria-hidden="true"><Music :size="26" :stroke-width="2" /></div>
        <div class="studio__copy">
          <strong class="studio__name">{{ studioName }}</strong>
          <span class="studio__meta">{{ studioMeta }}</span>
          <strong class="studio__price">{{ studioAvailability }}</strong>
        </div>
      </div>

      <h2 class="block-title">选择日期</h2>
      <div class="week">
        <button
          v-for="d in days"
          :key="d.date"
          class="day"
          :class="{ 'day--on': activeDay === d.date }"
          type="button"
          @click="activeDay = d.date; activeSlot = slots[0] ?? ''"
        >
          <span class="day__w">{{ d.w }}</span>
          <span class="day__d">{{ d.d }}</span>
        </button>
      </div>

      <h2 class="block-title">选择时段</h2>
      <div class="chip-row">
        <button
          v-for="s in slots"
          :key="s"
          class="chip"
          :class="activeSlot === s ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeSlot = s"
        >
          {{ s }}
        </button>
        <p v-if="!slots.length" class="empty-state">当天暂无可预约时段，请切换日期或稍后再试。</p>
      </div>

      <h2 class="block-title">报名信息</h2>
      <div class="rows">
        <PenFieldRow v-for="f in fields" :key="f.label" :label="f.label" :value="f.value" />
      </div>

      <div class="remark">备注：想了解的内容、目标舞种…</div>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="确认预约"
      :dark-disabled="!selectedSlot"
      @soft="showSuccessToast('已收藏')"
      @dark="onConfirm"
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

.studio {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: $pen-soft;

  &__cover {
    flex: none; width: 56px; height: 56px; border-radius: 12px;
    background: $pen-ink; color: $pen-on-primary; display: grid; place-items: center;
  }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__price { font-size: 14px; font-weight: 800; line-height: $pen-lh; }
}

.block-title { @include pen-h3-section; }

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

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.empty-state {
  width: 100%;
  margin: 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.rows { display: flex; flex-direction: column; }

.remark {
  min-height: 80px;
  padding: 14px;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-mute;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}
</style>
