<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();
const cats = ['全部', '待确认', '已确认', '已完成'];
const activeCat = ref('待确认');

const records = [
  { id: '1', title: 'Urban Flow 舞室', meta: 'K-pop 入门班 · 周日 14:00', status: '待舞室确认', tone: 'ink', action: '查看详情' },
  { id: '2', title: 'Beats Lab', meta: 'Jazz 基础 · 5/28 19:30', status: '已确认 · 待上课', tone: 'success', action: '去上课' },
  { id: '3', title: 'Mia Jazz 工作室', meta: '成品舞体验 · 5/20', status: '已完成', tone: 'mute', action: '写评价' }
];
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
            <button class="rec__btn" type="button" @click="showToast(r.action)">{{ r.action }}</button>
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
