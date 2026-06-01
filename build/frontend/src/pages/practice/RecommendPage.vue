<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const tab = ref<'recommend' | 'mine'>('recommend');
const filters = ['同舞种', '同水平', '同城', '时间匹配'];
const activeFilter = ref('同舞种');

interface MatchCard {
  id: string;
  name: string;
  meta: string;
  pct: string;
  tags: string[];
  dark: boolean;
}

const matches: MatchCard[] = [
  { id: 'jen', name: '阿 Jen', meta: 'Hiphop · 中级 · 1.2km', pct: '92%', tags: ['周末', '成品舞'], dark: true },
  { id: 'k', name: '小 K', meta: 'Jazz · 中级 · 朝阳 3km', pct: '88%', tags: ['晚课', '拍舞'], dark: false },
  { id: 'leo', name: 'Leo', meta: 'Urban · 中级 · 海淀', pct: '85%', tags: ['周日', 'Battle'], dark: false }
];
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="推荐与搭子" @share="showToast('已复制')" />

    <section class="pen-scroll">
      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'recommend' }" type="button" @click="tab = 'recommend'">推荐搭子</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'mine' }" type="button" @click="tab = 'mine'">我的搭子</button>
      </div>

      <div class="chip-row">
        <button
          v-for="f in filters"
          :key="f"
          class="chip"
          :class="activeFilter === f ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeFilter = f"
        >
          {{ f }}
        </button>
      </div>

      <article
        v-for="m in matches"
        :key="m.id"
        class="card"
        :class="m.dark ? 'card--dark' : 'card--light'"
      >
        <header class="card__head">
          <span class="card__avatar" aria-hidden="true" />
          <div class="card__copy">
            <strong class="card__name">{{ m.name }}</strong>
            <p class="card__meta">{{ m.meta }}</p>
          </div>
          <div class="card__match">
            <strong class="card__pct">{{ m.pct }}</strong>
            <span class="card__matchlabel">匹配度</span>
          </div>
        </header>
        <div class="card__tags">
          <span v-for="t in m.tags" :key="t" class="minichip">{{ t }}</span>
        </div>
        <button class="card__btn" type="button" @click="showToast('已打招呼')">打招呼</button>
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

.seg {
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

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  border-radius: 16px;

  &__head { display: flex; align-items: center; gap: 12px; }
  &__avatar { flex: none; width: 44px; height: 44px; border-radius: 999px; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 16px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__match { display: flex; flex-direction: column; align-items: flex-end; gap: 2px; }
  &__pct { font-size: 22px; font-weight: 900; line-height: $pen-lh; }
  &__matchlabel { font-size: 11px; font-weight: 700; line-height: $pen-lh; }
  &__tags { display: flex; flex-wrap: wrap; gap: 8px; }

  &__btn {
    width: 100%;
    height: 42px;
    border: 0;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }

  &--dark {
    background: $pen-ink;
    color: $pen-on-primary;
    .card__avatar { background: $pen-charcoal; }
    .card__meta, .card__matchlabel { color: $pen-subtle-text; }
    .minichip { background: $pen-charcoal; color: $pen-on-primary; border: 0; }
    .card__btn { background: $pen-soft; color: $pen-ink; }
  }

  &--light {
    background: $pen-soft;
    color: $pen-ink;
    .card__avatar { background: $pen-ink; }
    .card__meta, .card__matchlabel { color: $pen-mute; }
    .minichip { background: $pen-canvas; color: $pen-ink; border: 1px solid $pen-hairline; }
    .card__btn { background: $pen-ink; color: $pen-on-primary; }
  }
}

.minichip {
  height: 32px;
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
