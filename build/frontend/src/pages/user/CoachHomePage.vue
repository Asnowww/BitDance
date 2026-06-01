<script setup lang="ts">
import { showToast } from 'vant';
import { Play, Plus } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const fields = [
  { label: '个人介绍', value: '已填写' },
  { label: '教学风格', value: '细致 · 有耐心' },
  { label: '擅长舞种', value: '韩舞 / Jazz' },
  { label: '认证资质', value: '已认证' }
];

const slots = ['周一 19:30', '周三 19:30', '周六 全天'];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="我的教练主页" :show-share="false" />

    <section class="pen-scroll">
      <section class="hero">
        <div class="hero__bars" aria-hidden="true"><span v-for="i in 6" :key="i" /></div>
        <strong class="hero__title">MIA</strong>
        <p class="hero__meta">教练主页 · 学员端可见</p>
      </section>

      <section class="inner">
        <div class="rows">
          <PenFieldRow v-for="f in fields" :key="f.label" :label="f.label" :value="f.value" />
        </div>

        <h2 class="block-title">作品展示</h2>
        <div class="media">
          <div class="media__cell" aria-hidden="true"><Play :size="24" :stroke-width="2" /></div>
          <div class="media__cell" aria-hidden="true"><Play :size="24" :stroke-width="2" /></div>
          <button class="media__add" type="button" aria-label="添加作品"><Plus :size="26" :stroke-width="2" /></button>
        </div>

        <h2 class="block-title">可约时段</h2>
        <div class="slots">
          <span v-for="s in slots" :key="s" class="slot">{{ s }}</span>
          <button class="slot-add" type="button" aria-label="添加时段"><Plus :size="18" :stroke-width="2" /></button>
        </div>
      </section>
    </section>

    <PenActionBar
      soft-label="预览主页"
      dark-label="保存"
      @soft="showToast('预览主页')"
      @dark="showToast('已保存')"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; }

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 160px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__bars {
    display: grid; grid-template-columns: repeat(6, 1fr); gap: 8px; height: 30px; margin-bottom: auto;
    span { height: 100%; background: $pen-charcoal; }
  }
  &__title { margin: 0; font-size: 30px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.inner { display: flex; flex-direction: column; gap: 14px; padding: 0 18px 20px; }
.rows { display: flex; flex-direction: column; }
.block-title { @include pen-h3-section; }

.media {
  display: flex;
  gap: 8px;

  &__cell, &__add {
    flex: 1;
    height: 96px;
    border-radius: 12px;
    display: grid;
    place-items: center;
  }
  &__cell { background: $pen-ink; color: $pen-on-primary; }
  &__add { background: $pen-soft; color: $pen-mute; border: 1px solid $pen-hairline; cursor: pointer; }
}

.slots { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }

.slot {
  height: 40px;
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.slot-add {
  width: 40px; height: 40px;
  border: 1px solid $pen-hairline; border-radius: 999px;
  background: $pen-canvas; color: $pen-ink;
  display: grid; place-items: center; cursor: pointer;
}
</style>
