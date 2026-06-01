<script setup lang="ts">
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ChevronLeft, ChevronRight, Ticket } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();
const weekdays = ['日', '一', '二', '三', '四', '五', '六'];

interface Cell {
  n: number;
  muted: boolean;
  event: boolean;
  selected: boolean;
}

const cells: Cell[] = [];
[27, 28, 29, 30].forEach((n) => cells.push({ n, muted: true, event: false, selected: false }));
for (let d = 1; d <= 31; d++) {
  cells.push({ n: d, muted: false, event: [14, 22, 31].includes(d), selected: d === 31 });
}
[1, 2, 3, 4, 5, 6, 7].forEach((n) => cells.push({ n, muted: true, event: false, selected: false }));

const weeks: Cell[][] = [];
for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="活动日历" @share="showToast('已复制')" />

    <section class="pen-scroll">
      <div class="month">
        <button class="month__nav" type="button" aria-label="上个月"><ChevronLeft :size="20" :stroke-width="2" /></button>
        <span class="month__label">2026 年 5 月</span>
        <button class="month__nav" type="button" aria-label="下个月"><ChevronRight :size="20" :stroke-width="2" /></button>
      </div>

      <div class="week-head">
        <span v-for="w in weekdays" :key="w">{{ w }}</span>
      </div>

      <div class="grid">
        <div v-for="(row, ri) in weeks" :key="ri" class="grid__row">
          <div v-for="(c, ci) in row" :key="ci" class="cell">
            <span v-if="c.selected" class="cell__sel">{{ c.n }}</span>
            <span v-else class="cell__num" :class="{ 'cell__num--muted': c.muted }">{{ c.n }}</span>
            <span v-if="c.event && !c.selected" class="cell__dot" aria-hidden="true" />
          </div>
        </div>
      </div>

      <h2 class="day-title">5 月 31 日 · 周日</h2>
      <article class="event">
        <div class="event__cover" aria-hidden="true"><Ticket :size="24" :stroke-width="2" /></div>
        <div class="event__copy">
          <strong class="event__name">Locking 大师课</strong>
          <span class="event__meta">14:00 · Joy Studio · 剩 8 位 · ¥199</span>
        </div>
        <button class="event__btn" type="button" @click="router.push('/workshop/locking')">报名</button>
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
  height: 42px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;

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
