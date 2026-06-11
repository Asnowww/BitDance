<script setup lang="ts">
import { ref } from 'vue';
import { Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const cats = ['全部', '舞室', '老师', '课程'];
const activeCat = ref('全部');

const reviews = [
  { id: '1', target: 'Urban Flow 舞室', verified: true, stars: 5, dims: '交通 5 · 环境 5 · 氛围 4', content: '地铁出来很好找，场地干净。', date: '5/24' },
  { id: '2', target: '小鹿老师', verified: true, stars: 5, dims: '耐心 5 · 纠错 5 · 讲解 4', content: '会拆动作，零基础也跟得上。', date: '5/20' },
  { id: '3', target: 'K-pop 入门班', verified: false, stars: 4, dims: '上手 易 · 节奏 4 · 收获 5', content: '一节课能跟下整段，成就感强。', date: '5/18' }
];
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的评价" :show-share="false" />

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

      <article v-for="r in reviews" :key="r.id" class="rev">
        <header class="rev__top">
          <strong class="rev__target">{{ r.target }}</strong>
          <span v-if="r.verified" class="rev__verified">已验证</span>
        </header>
        <span class="rev__stars">
          <Star
            v-for="i in 5"
            :key="i"
            :size="14"
            :stroke-width="2"
            :fill="i <= r.stars ? '#111111' : 'none'"
            :color="i <= r.stars ? '#111111' : '#E5E5E5'"
          />
        </span>
        <p class="rev__dims">{{ r.dims }}</p>
        <p class="rev__content">{{ r.content }}</p>
        <span class="rev__date">{{ r.date }}</span>
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

.rev {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__top { display: flex; align-items: center; gap: 8px; }
  &__target { flex: 1; min-width: 0; font-size: 15px; font-weight: 900; line-height: $pen-lh; }

  &__verified {
    flex: none;
    height: 26px;
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    border: 1px solid $pen-success;
    border-radius: 999px;
    color: $pen-success;
    font-size: 11px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__stars { display: inline-flex; gap: 3px; }
  &__dims { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: $pen-lh; }
  &__content { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
  &__date { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
}
</style>
