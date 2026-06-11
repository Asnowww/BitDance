<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ChevronLeft, ChevronRight, Clock3, MapPin } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchWorkshopCalendar, type WorkshopCalendarEvent } from '@/api/workshop';

const router = useRouter();
const events = ref<WorkshopCalendarEvent[]>([]);
const loading = ref(false);
const today = new Date();
const currentMonth = ref(new Date(today.getFullYear(), today.getMonth(), 1));
const selectedDay = ref(today.getDate());

const load = async () => {
  loading.value = true;
  try {
    events.value = await fetchWorkshopCalendar();
    const first = events.value[0] ? new Date(events.value[0].startAt) : null;
    if (first && !Number.isNaN(first.getTime())) {
      currentMonth.value = new Date(first.getFullYear(), first.getMonth(), 1);
      selectedDay.value = first.getDate();
    }
  } finally {
    loading.value = false;
  }
};

const monthLabel = computed(() => `${currentMonth.value.getFullYear()} 年 ${currentMonth.value.getMonth() + 1} 月`);
const monthStartWeekday = computed(() => new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), 1).getDay());
const daysInMonth = computed(() => new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 0).getDate());
const monthEvents = computed(() =>
  events.value.filter((event) => {
    const date = new Date(event.startAt);
    return date.getFullYear() === currentMonth.value.getFullYear() && date.getMonth() === currentMonth.value.getMonth();
  })
);
const selectedEvents = computed(() =>
  monthEvents.value.filter((event) => new Date(event.startAt).getDate() === selectedDay.value)
);
const eventDays = computed(() => new Set(monthEvents.value.map((event) => new Date(event.startAt).getDate())));
const weeks = computed(() => {
  const cells: Array<{ n: number; muted: boolean; event: boolean; selected: boolean }> = [];
  const prevMonthLastDay = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), 0).getDate();
  for (let i = monthStartWeekday.value - 1; i >= 0; i -= 1) {
    cells.push({ n: prevMonthLastDay - i, muted: true, event: false, selected: false });
  }
  for (let day = 1; day <= daysInMonth.value; day += 1) {
    cells.push({ n: day, muted: false, event: eventDays.value.has(day), selected: selectedDay.value === day });
  }
  while (cells.length % 7 !== 0) cells.push({ n: 0, muted: true, event: false, selected: false });
  const rows = [];
  for (let i = 0; i < cells.length; i += 7) rows.push(cells.slice(i, i + 7));
  return rows;
});

const shiftMonth = (delta: number) => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + delta, 1);
  selectedDay.value = 1;
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="活动日历" @share="showToast('活动日历链接已复制')" />

    <section class="pen-scroll">
      <div class="month">
        <button class="month__nav" type="button" aria-label="上个月" @click="shiftMonth(-1)">
          <ChevronLeft :size="20" :stroke-width="2" />
        </button>
        <strong>{{ monthLabel }}</strong>
        <button class="month__nav" type="button" aria-label="下个月" @click="shiftMonth(1)">
          <ChevronRight :size="20" :stroke-width="2" />
        </button>
      </div>

      <div class="week-head">
        <span v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day">{{ day }}</span>
      </div>

      <div class="grid">
        <div v-for="(row, rowIndex) in weeks" :key="rowIndex" class="grid__row">
          <button
            v-for="(cell, index) in row"
            :key="`${rowIndex}-${index}`"
            class="cell"
            type="button"
            :disabled="cell.muted || !cell.n"
            @click="selectedDay = cell.n"
          >
            <span v-if="cell.selected" class="cell__selected">{{ cell.n }}</span>
            <span v-else class="cell__num" :class="{ 'cell__num--muted': cell.muted }">{{ cell.n || '' }}</span>
            <span v-if="cell.event && !cell.selected" class="cell__dot" />
          </button>
        </div>
      </div>

      <section class="tip-card">
        <strong>提醒规则</strong>
        <p>支付成功后加入活动日历；开场前 24 小时、1 小时和结束后会定向提醒。</p>
      </section>

      <p v-if="loading" class="empty">活动加载中</p>
      <p v-else-if="selectedEvents.length === 0" class="empty">这一天暂无活动</p>

      <article v-for="event in selectedEvents" :key="event.orderId" class="event">
        <div class="event__copy">
          <strong>{{ event.workshopName }}</strong>
          <span><Clock3 :size="14" :stroke-width="2" />{{ new Date(event.startAt).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }) }}</span>
          <span><MapPin :size="14" :stroke-width="2" />{{ event.locationName }}</span>
          <p>{{ event.reminderTitle }} · {{ event.reminderBody }}</p>
        </div>
        <div class="event__actions">
          <button
            v-if="event.allowCheckin && event.checkinCode"
            class="ghost-btn"
            type="button"
            @click="router.push(`/workshop-checkin/${event.orderId}`)"
          >
            去签到
          </button>
          <button class="primary-btn" type="button" @click="router.push(`/workshop/${event.workshopId}`)">查看</button>
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

.month {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;

  strong {
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__nav {
    border: 0;
    background: transparent;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }
}

.week-head {
  display: grid;
  grid-template-columns: repeat(7, 1fr);

  span {
    text-align: center;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.grid {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__row {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 6px;
  }
}

.cell {
  border: 0;
  background: transparent;
  color: $pen-ink;
  height: 42px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;

  &:disabled {
    cursor: default;
  }

  &__num {
    font-size: 14px;
    font-weight: 700;
    line-height: $pen-lh;

    &--muted {
      color: $pen-hairline;
    }
  }

  &__selected {
    width: 32px;
    height: 32px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    display: grid;
    place-items: center;
    font-size: 14px;
    font-weight: 800;
  }

  &__dot {
    width: 5px;
    height: 5px;
    border-radius: 999px;
    background: $pen-ink;
  }
}

.tip-card,
.event {
  border-radius: 14px;
}

.tip-card {
  padding: 14px;
  background: $pen-soft;

  strong, p {
    margin: 0;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    margin-top: 6px;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.45;
  }
}

.empty {
  margin: 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.event {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  background: $pen-canvas;
  border: 1px solid $pen-hairline;

  &__copy {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span, p {
    margin: 0;
    display: inline-flex;
    align-items: center;
    gap: 5px;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.4;
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.ghost-btn,
.primary-btn {
  height: 34px;
  padding: 0 14px;
  border: 0;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;
}

.ghost-btn {
  background: $pen-soft;
  color: $pen-ink;
}

.primary-btn {
  background: $pen-ink;
  color: $pen-on-primary;
}
</style>
