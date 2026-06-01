<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';

const route = useRoute();
const router = useRouter();
const studioId = String(route.params.id || 'urban-flow');

const view = ref<'day' | 'week'>('day');

const days = [
  { w: '一', d: '27' }, { w: '二', d: '28' }, { w: '三', d: '29' }, { w: '四', d: '30' },
  { w: '五', d: '31' }, { w: '六', d: '1' }, { w: '日', d: '2' }
];
const activeDay = ref('1');

const classes = [
  { time: '10:00', dur: '60min', title: '早间塑形基础', teacher: 'Mia 老师 · 1 号厅', level: '零基础友好', price: '¥69 试听', full: false },
  { time: '14:00', dur: '90min', title: 'K-pop 入门成品舞', teacher: '小鹿老师 · 2 号厅', level: '初级', price: '¥79 试听', full: false },
  { time: '19:30', dur: '90min', title: 'Hiphop 中级 Groove', teacher: 'Leo 老师 · 3 号厅', level: '中级', price: '已满员', full: true }
];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="周课表" @share="showToast('课表链接已复制')" />

    <section class="pen-scroll">
      <h2 class="studio">Urban Flow 舞室</h2>

      <div class="toggle">
        <button class="toggle__btn" :class="{ 'toggle__btn--on': view === 'day' }" type="button" @click="view = 'day'">日视图</button>
        <button class="toggle__btn" :class="{ 'toggle__btn--on': view === 'week' }" type="button" @click="view = 'week'">周视图</button>
      </div>

      <div class="week">
        <button
          v-for="d in days"
          :key="d.d"
          class="day"
          :class="{ 'day--on': activeDay === d.d }"
          type="button"
          @click="activeDay = d.d"
        >
          <span class="day__w">{{ d.w }}</span>
          <span class="day__d">{{ d.d }}</span>
        </button>
      </div>

      <h3 class="date-title">周六 · 5 月 31 日</h3>

      <article v-for="c in classes" :key="c.time" class="lesson">
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
</style>
