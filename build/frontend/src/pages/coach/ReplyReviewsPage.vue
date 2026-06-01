<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';
import { Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const cats = ['全部', '待回复', '已回复'];
const activeCat = ref('待回复');

const reviews = [
  { id: '1', name: '小林', stars: 5, content: '老师会拆动作，零基础也能跟上，节奏很舒服。', reply: '' },
  { id: '2', name: 'Kiki', stars: 4, content: '场地干净，晚课多，地铁出来很好找。', reply: '商家回复：谢谢支持，欢迎常来一起跳～' }
];
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="评价回复" :show-share="false" />

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
        <header class="rev__head">
          <span class="rev__avatar" aria-hidden="true" />
          <div class="rev__who">
            <strong class="rev__name">{{ r.name }}</strong>
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
          </div>
        </header>
        <p class="rev__content">{{ r.content }}</p>

        <div v-if="r.reply" class="rev__quote">{{ r.reply }}</div>
        <div v-else class="rev__reply">
          <span class="rev__reply-text">回复学员…</span>
          <button class="rev__send" type="button" @click="showToast('已回复')">发送</button>
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

.rev {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__head { display: flex; align-items: center; gap: 10px; }
  &__avatar { flex: none; width: 36px; height: 36px; border-radius: 999px; background: $pen-ink; }
  &__who { display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
  &__stars { display: inline-flex; gap: 3px; }
  &__content { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }

  &__quote {
    padding: 12px;
    border-radius: 10px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.4;
  }

  &__reply {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 42px;
    padding: 0 8px 0 16px;
    border-radius: 999px;
    background: $pen-canvas;
    border: 1px solid $pen-hairline;
  }
  &__reply-text { flex: 1; color: $pen-mute; font-size: 13px; font-weight: 500; line-height: $pen-lh; }
  &__send {
    flex: none; height: 30px; padding: 6px 14px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 12px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}
</style>
