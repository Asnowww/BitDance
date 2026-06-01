<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ChevronLeft, Search } from 'lucide-vue-next';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const router = useRouter();

const topics = [
  { name: '# 零基础打卡', count: '1.2 万人参与' },
  { name: '# 韩舞成品舞', count: '8900 人参与' },
  { name: '# 约练搭子', count: '5600 人参与' },
  { name: '# Workshop 现场', count: '3200 人参与' }
];

const open = (name: string) => router.push(`/community/topic/${encodeURIComponent(name.replace('# ', ''))}`);
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">话题广场</h1>
      <button class="topbar__icon" type="button" aria-label="搜索" @click="router.push('/community/search')">
        <Search :size="20" :stroke-width="2" />
      </button>
    </header>

    <section class="pen-scroll">
      <section class="hero">
        <span class="hero__tag"># 本周热门</span>
        <strong class="hero__title">零基础打卡挑战</strong>
        <span class="hero__meta">1.2 万人参与 · 3400 条动态</span>
      </section>

      <h2 class="block-title">热门话题</h2>
      <div class="rows">
        <PenFieldRow
          v-for="t in topics"
          :key="t.name"
          :label="t.name"
          :value="t.count"
          @click="open(t.name)"
        />
      </div>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__title { flex: 1; margin: 0; font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__icon {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 6px;
  height: 130px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-ink;
  color: $pen-on-primary;

  &__tag { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
  &__title { font-size: 28px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.block-title { @include pen-h3-section; }
.rows { display: flex; flex-direction: column; }
</style>
