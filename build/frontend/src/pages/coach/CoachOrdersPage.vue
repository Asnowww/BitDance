<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';
import { ScanLine } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const cats = ['待核销', '已核销', '已退款'];
const activeCat = ref('待核销');

const orders = [
  { id: '1', name: '小李', meta: 'Locking 大师课 · 5/30 14:00', price: '¥199', done: false },
  { id: '2', name: '阿 May', meta: 'K-pop 入门 · 5/28 19:30', price: '¥79', done: false },
  { id: '3', name: 'Leo', meta: 'Hiphop 中级 · 5/25 20:00', price: '¥128', done: true }
];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="学员订单" :show-share="false" />

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

      <article v-for="o in orders" :key="o.id" class="order">
        <span class="order__avatar" aria-hidden="true" />
        <div class="order__copy">
          <strong class="order__name">{{ o.name }}</strong>
          <span class="order__meta">{{ o.meta }}</span>
          <strong class="order__price">{{ o.price }}</strong>
        </div>
        <span v-if="o.done" class="order__done">已核销</span>
        <button v-else class="order__btn" type="button" @click="showToast('已核销')">核销</button>
      </article>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="showToast('打开扫码核销')">
        <ScanLine :size="20" :stroke-width="2" />
        扫码核销
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 14px; padding: 16px 18px; }

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.order {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__avatar { flex: none; width: 44px; height: 44px; border-radius: 999px; background: $pen-ink; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; }
  &__name { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__price { font-size: 13px; font-weight: 800; line-height: $pen-lh; }

  &__done { flex: none; color: $pen-mute; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
  &__btn {
    flex: none; height: 36px; padding: 8px 16px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 13px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}

.save-bar {
  position: fixed;
  right: 0; bottom: 0; left: 0;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  &__btn {
    width: 100%;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}
</style>
