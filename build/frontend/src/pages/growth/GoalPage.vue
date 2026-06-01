<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';
import { CircleCheckBig } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const period = ref<'week' | 'month'>('week');

const milestones = [
  { label: '连续打卡 7 天', done: true, value: '已达成' },
  { label: '累计训练 20 小时', done: false, value: '16 / 20' },
  { label: '尝试 5 个舞种', done: false, value: '4 / 5' }
];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="训练目标" :show-share="false" />

    <section class="pen-scroll">
      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': period === 'week' }" type="button" @click="period = 'week'">本周</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': period === 'month' }" type="button" @click="period = 'month'">本月</button>
      </div>

      <section class="goal">
        <span class="goal__label">本周训练目标</span>
        <strong class="goal__value">4 / 5 次</strong>
        <div class="goal__track"><span class="goal__fill" /></div>
        <span class="goal__hint">再练 1 次即可达成本周目标</span>
      </section>

      <h2 class="block-title">里程碑</h2>
      <div v-for="m in milestones" :key="m.label" class="mile">
        <span class="mile__label">{{ m.label }}</span>
        <span v-if="m.done" class="mile__done">
          <CircleCheckBig :size="18" :stroke-width="2" />
          {{ m.value }}
        </span>
        <span v-else class="mile__value">{{ m.value }}</span>
      </div>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="showToast('编辑目标')">编辑目标</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

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

.goal {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-ink;
  color: $pen-on-primary;

  &__label { color: $pen-subtle-text; font-size: 14px; font-weight: 700; line-height: $pen-lh; }
  &__value { font-size: 40px; font-weight: 900; line-height: $pen-lh; }
  &__track { height: 10px; border-radius: 999px; background: $pen-charcoal; overflow: hidden; }
  &__fill { display: block; width: 80%; height: 100%; border-radius: 999px; background: $pen-on-primary; }
  &__hint { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.block-title { @include pen-h3-section; }

.mile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid $pen-hairline;

  &__label { flex: 1; font-size: 15px; font-weight: 800; line-height: $pen-lh; }
  &__value { color: $pen-mute; font-size: 14px; font-weight: 700; line-height: $pen-lh; }
  &__done {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: $pen-success;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.save-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
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
