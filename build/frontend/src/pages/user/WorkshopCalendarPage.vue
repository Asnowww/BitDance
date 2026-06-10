<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ChevronLeft, ChevronRight, Ticket } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchMyWorkshopOrders, type WorkshopOrder } from '@/api/workshop';

const router = useRouter();
const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
const orders = ref<WorkshopOrder[]>([]);
const loading = ref(false);
const today = new Date();
const selectedDay = ref(today.getDate());
const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
const daysInMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();

interface Cell {
  n: number;
  muted: boolean;
  event: boolean;
  selected: boolean;
}

const dateOf = (order: WorkshopOrder) => {
  const date = order.sessionDate ? new Date(order.sessionDate) : new Date(order.createdAt);
  return Number.isNaN(date.getTime()) ? null : date;
};
const activeOrders = computed(() =>
  orders.value.filter((order) => ['PAID', 'CHECKED_IN', 'COMPLETED'].includes(order.status))
);
const selectedEvents = computed(() =>
  activeOrders.value.filter((order) => {
    const date = dateOf(order);
    return date && date.getMonth() === today.getMonth() && date.getDate() === selectedDay.value;
  })
);
const eventDays = computed(() => new Set(activeOrders.value.map((order) => dateOf(order)?.getDate()).filter(Boolean)));

const cells = computed<Cell[]>(() => {
  const out: Cell[] = [];
  const previousMonthDays = new Date(today.getFullYear(), today.getMonth(), 0).getDate();
  for (let i = monthStart.getDay() - 1; i >= 0; i--) {
    out.push({ n: previousMonthDays - i, muted: true, event: false, selected: false });
  }
  for (let d = 1; d <= daysInMonth; d++) {
    out.push({ n: d, muted: false, event: eventDays.value.has(d), selected: d === selectedDay.value });
  }
  while (out.length % 7 !== 0) {
    out.push({ n: out.length % 7, muted: true, event: false, selected: false });
  }
  return out;
});
const weeks = computed(() => {
  const rows: Cell[][] = [];
  for (let i = 0; i < cells.value.length; i += 7) rows.push(cells.value.slice(i, i + 7));
  return rows;
});
const monthLabel = computed(() => `${today.getFullYear()} 年 ${today.getMonth() + 1} 月`);
const dayTitle = computed(() => `${today.getMonth() + 1} 月 ${selectedDay.value} 日`);

const load = async () => {
  loading.value = true;
  try {
    orders.value = await fetchMyWorkshopOrders();
    const firstEvent = activeOrders.value.find((order) => dateOf(order)?.getMonth() === today.getMonth());
    const firstDate = firstEvent ? dateOf(firstEvent) : null;
    if (firstDate) selectedDay.value = firstDate.getDate();
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="活动日历" @share="showToast('已复制')" />

    <section class="pen-scroll">
      <div class="month">
        <button class="month__nav" type="button" aria-label="上个月"><ChevronLeft :size="20" :stroke-width="2" /></button>
        <span class="month__label">{{ monthLabel }}</span>
        <button class="month__nav" type="button" aria-label="下个月"><ChevronRight :size="20" :stroke-width="2" /></button>
      </div>

      <div class="week-head">
        <span v-for="w in weekdays" :key="w">{{ w }}</span>
      </div>

      <div class="grid">
        <div v-for="(row, ri) in weeks" :key="ri" class="grid__row">
          <button
            v-for="(c, ci) in row"
            :key="ci"
            class="cell"
            type="button"
            :disabled="c.muted"
            @click="selectedDay = c.n"
          >
            <span v-if="c.selected" class="cell__sel">{{ c.n }}</span>
            <span v-else class="cell__num" :class="{ 'cell__num--muted': c.muted }">{{ c.n }}</span>
            <span v-if="c.event && !c.selected" class="cell__dot" aria-hidden="true" />
          </button>
        </div>
      </div>

      <h2 class="day-title">{{ dayTitle }}</h2>
      <p v-if="loading" class="empty">活动加载中</p>
      <p v-else-if="selectedEvents.length === 0" class="empty">当天暂无已报名活动</p>
      <article v-for="event in selectedEvents" :key="event.id" class="event">
        <div class="event__cover" aria-hidden="true"><Ticket :size="24" :stroke-width="2" /></div>
        <div class="event__copy">
          <strong class="event__name">{{ event.workshopTitle }}</strong>
          <span class="event__meta">{{ event.sessionTime || '待确认时间' }} · ¥{{ event.amount }} · {{ event.status }}</span>
        </div>
        <button class="event__btn" type="button" @click="router.push(`/workshop/${event.workshopId}`)">查看</button>
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
  gap: 18px;

  &__label { font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__nav { border: 0; background: transparent; color: $pen-ink; display: grid; place-items: center; cursor: pointer; }
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

  &__row { display: grid; grid-template-columns: repeat(7, 1fr); gap: 6px; }
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

  &:disabled { cursor: default; }

  &__num { font-size: 14px; font-weight: 700; line-height: $pen-lh; &--muted { color: $pen-hairline; } }
  &__sel {
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
  &__dot { width: 5px; height: 5px; border-radius: 999px; background: $pen-ink; }
}

.day-title { @include pen-h3-section; font-size: 16px; margin-top: 4px; }

.empty {
  margin: 6px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.event {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__cover {
    flex: none; width: 48px; height: 48px; border-radius: 12px;
    background: $pen-ink; color: $pen-on-primary; display: grid; place-items: center;
  }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__btn {
    flex: none; height: 34px; padding: 8px 16px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 13px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}
</style>
