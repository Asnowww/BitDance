<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const route = useRoute();
const topic = computed(() => decodeURIComponent(String(route.params.name || '零基础打卡挑战')));

const sort = ref<'hot' | 'new'>('hot');

const posts = [
  { id: 'a', name: '小美', time: '2 小时前', text: 'Day 7 打卡，今天练了基础律动，越来越顺了' },
  { id: 'b', name: 'Leo', time: '昨天', text: '第一次完整跳完一支舞，纪念一下！' }
];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="话题" @share="showToast('已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <strong class="hero__title"># {{ topic }}</strong>
        <span class="hero__meta">1.2 万人参与 · 3400 条动态</span>
      </section>

      <div class="inner">
        <div class="chip-row">
          <button class="chip" :class="sort === 'hot' ? 'chip--active' : 'chip--inactive'" type="button" @click="sort = 'hot'">最热</button>
          <button class="chip" :class="sort === 'new' ? 'chip--active' : 'chip--inactive'" type="button" @click="sort = 'new'">最新</button>
        </div>

        <article v-for="p in posts" :key="p.id" class="post">
          <header class="post__head">
            <span class="post__avatar" aria-hidden="true" />
            <div class="post__who">
              <strong class="post__name">{{ p.name }}</strong>
              <span class="post__time">{{ p.time }}</span>
            </div>
          </header>
          <p class="post__text">{{ p.text }}</p>
          <div class="post__media" aria-hidden="true" />
        </article>
      </div>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="showToast('已参与话题')">参与话题</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 14px; }

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 150px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__title { font-size: 28px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.inner { display: flex; flex-direction: column; gap: 14px; padding: 0 18px 20px; }

.chip-row { display: flex; gap: 8px; }
.chip { @include pen-chip; }

.post {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid $pen-hairline;

  &__head { display: flex; align-items: center; gap: 10px; }
  &__avatar { flex: none; width: 36px; height: 36px; border-radius: 999px; background: $pen-ink; }
  &__who { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
  &__name { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
  &__time { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__text { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
  &__media { height: 130px; border-radius: 14px; background: $pen-soft; }
}

.save-bar {
  position: fixed;
  right: 0; bottom: var(--app-tabbar-offset, 0px); left: 0;
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
