<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import { fetchCourseDetail, type CourseDetail } from '@/api/course';
import { toggleFavorite } from '@/api/favorite';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { createTrialBooking, fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const studioId = Number(route.params.id) || 1;
const requestedCourseId = Number(route.query.courseId) || undefined;
const requestedScheduleId = Number(route.query.courseScheduleId) || undefined;
const detail = ref<StudioDetail | null>(null);
const schedule = ref<ScheduleSlot[]>([]);
const selectedCourse = ref<CourseDetail | null>(null);
const contactName = ref('');
const contactPhone = ref('');
const danceLevel = ref(user.preferences.level || '零基础');
const bookingNote = ref('');

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
const activeScheduleId = ref<number | undefined>(requestedScheduleId);
const selectedSlot = computed(
  () => availableSlots.value.find((slot) => slot.id === activeScheduleId.value) ?? availableSlots.value[0] ?? schedule.value[0]
);

const studioTitle = computed(() => detail.value?.name || `舞室 #${studioId}`);
const studioMeta = computed(() =>
  [detail.value?.address, detail.value?.transportInfo].filter(Boolean).join(' · ') || '地址与交通信息待完善'
);
const courseTitle = computed(() => selectedCourse.value?.courseName || (selectedSlot.value ? `课程 #${selectedSlot.value.courseId}` : '待选课程'));
const priceText = computed(() =>
  selectedCourse.value?.priceAmount ? `¥${selectedCourse.value.priceAmount} 体验课` : '价格待确认'
);

const formatTime = (iso: string) =>
  new Date(iso).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false });

const setActiveDay = (date: string) => {
  activeDay.value = date;
  activeScheduleId.value = availableSlots.value[0]?.id;
};

const favoriteStudio = async () => {
  const result = await toggleFavorite('studio', studioId);
  showSuccessToast(result.favored ? '已收藏' : '已取消收藏');
};

const onConfirm = async () => {
  const selected = selectedSlot.value;
  if (!selected) return;
  if (!contactPhone.value.trim()) {
    showFailToast('请填写联系电话');
    return;
  }
  await createTrialBooking({
    courseId: requestedCourseId ?? selected.courseId,
    courseScheduleId: selected.id,
    contactPhone: contactPhone.value.trim(),
    // M1 预约流程：把报名人、基础和备注写入 bookingNote，后端暂无独立字段时仍可追踪。
    bookingNote: [contactName.value.trim(), danceLevel.value.trim(), bookingNote.value.trim()].filter(Boolean).join(' / ')
  });
  showSuccessToast('已提交，等待舞室确认');
  router.push('/me/trials');
};

onMounted(async () => {
  [detail.value, schedule.value] = await Promise.all([
    fetchStudioDetail(studioId),
    fetchStudioSchedule(studioId)
  ]);
  contactName.value = user.profile?.nickname || '';
  contactPhone.value = user.profile?.phone || '';
  if (!activeScheduleId.value) activeScheduleId.value = availableSlots.value[0]?.id ?? schedule.value[0]?.id;
});

watch(
  selectedSlot,
  async (slot) => {
    selectedCourse.value = slot ? await fetchCourseDetail(slot.courseId).catch(() => null) : null;
  },
  { immediate: true }
);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="试听预约" @share="showSuccessToast('链接已复制')" />

    <section class="pen-scroll">
      <div class="studio">
        <div class="studio__cover" aria-hidden="true"><Music :size="26" :stroke-width="2" /></div>
        <div class="studio__copy">
          <strong class="studio__name">{{ studioTitle }}</strong>
          <span class="studio__meta">{{ studioMeta }}</span>
          <strong class="studio__price">{{ courseTitle }} · {{ priceText }}</strong>
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
          @click="setActiveDay(d.date)"
        >
          <span class="day__w">{{ d.w }}</span>
          <span class="day__d">{{ d.d }}</span>
        </button>
      </div>

      <h2 class="block-title">选择时段</h2>
      <div class="chip-row">
        <button
          v-for="slot in availableSlots"
          :key="slot.id"
          class="chip"
          :class="activeScheduleId === slot.id ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeScheduleId = slot.id"
        >
          {{ formatTime(slot.startAt) }} · 余 {{ Math.max(slot.capacity - slot.bookedCount, 0) }}
        </button>
      </div>
      <p v-if="!availableSlots.length" class="empty-hint">当天暂无可预约场次，请切换日期。</p>

      <h2 class="block-title">报名信息</h2>
      <div class="rows">
        <label class="form-row">
          <span>姓名</span>
          <input v-model="contactName" type="text" placeholder="请输入称呼" />
        </label>
        <label class="form-row">
          <span>手机号</span>
          <input v-model="contactPhone" type="tel" placeholder="请输入联系电话" />
        </label>
        <label class="form-row">
          <span>舞蹈基础</span>
          <input v-model="danceLevel" type="text" placeholder="例如：零基础 / Jazz 初级" />
        </label>
      </div>

      <label class="remark">
        <span>备注</span>
        <textarea v-model="bookingNote" rows="3" placeholder="想了解的内容、目标舞种…" />
      </label>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="确认预约"
      @soft="favoriteStudio"
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

.empty-hint {
  margin: 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.rows {
  display: flex;
  flex-direction: column;
  border-top: 1px solid $pen-hairline;
}

.form-row {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  align-items: center;
  min-height: 54px;
  border-bottom: 1px solid $pen-hairline;
  font-size: 14px;
  font-weight: 800;
  line-height: $pen-lh;

  span {
    color: $pen-ink;
  }

  input {
    width: 100%;
    min-width: 0;
    border: 0;
    outline: 0;
    background: transparent;
    color: $pen-ink;
    font: inherit;
    text-align: right;
  }
}

.remark {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 80px;
  padding: 14px;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-mute;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;

  span {
    color: $pen-ink;
    font-weight: 900;
  }

  textarea {
    width: 100%;
    min-width: 0;
    border: 0;
    outline: 0;
    resize: vertical;
    background: transparent;
    color: $pen-ink;
    font: inherit;
  }
}
</style>
