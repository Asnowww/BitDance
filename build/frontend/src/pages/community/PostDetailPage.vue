<script setup lang="ts">
import { useRoute } from 'vue-router';
import { showToast } from 'vant';
import { MapPin, ChevronRight, Heart } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const route = useRoute();
const postId = String(route.params.id || '1');

const comments = [
  { id: 'k', name: '小 K', text: '看起来好棒，下次一起约！' },
  { id: 'm', name: 'Mia 老师', text: '动作进步很大，继续保持～' }
];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="动态" @share="showToast('已复制链接')" />

    <section class="pen-scroll">
      <header class="author">
        <span class="author__avatar" aria-hidden="true" />
        <div class="author__who">
          <strong class="author__name">小鹿</strong>
          <span class="author__meta">五道口 · 2 小时前</span>
        </div>
        <button class="author__follow" type="button">关注</button>
      </header>

      <p class="text">
        今天试听了 Urban Flow 的韩舞课，老师会拆动作、节奏适合第一次学韩舞的人，零基础也跟得上，强烈推荐给想入门的姐妹！
      </p>

      <div class="media" aria-hidden="true" />

      <button class="anchor" type="button" @click="showToast('打开 Urban Flow 舞室')">
        <MapPin :size="20" :stroke-width="2" />
        <div class="anchor__copy">
          <strong>Urban Flow 舞室</strong>
          <span>韩舞课 · 可预约试听</span>
        </div>
        <ChevronRight class="anchor__chev" :size="18" :stroke-width="2" />
      </button>

      <p class="stats">32 赞 · 8 评论</p>

      <article v-for="c in comments" :key="c.id" class="comment">
        <span class="comment__avatar" aria-hidden="true" />
        <div class="comment__body">
          <strong class="comment__name">{{ c.name }}</strong>
          <p class="comment__text">{{ c.text }}</p>
        </div>
        <Heart class="comment__like" :size="16" :stroke-width="2" />
      </article>
    </section>

    <footer class="comment-bar">
      <div class="comment-bar__input">写评论…</div>
      <button class="comment-bar__like" type="button" aria-label="点赞">
        <Heart :size="20" :stroke-width="2" />
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

.author {
  display: flex;
  align-items: center;
  gap: 10px;

  &__avatar { flex: none; width: 44px; height: 44px; border-radius: 999px; background: $pen-ink; }
  &__who { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
  &__name { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }

  &__follow {
    flex: none; height: 34px; padding: 8px 16px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 13px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}

.text { margin: 0; font-size: 15px; font-weight: 500; line-height: 1.5; }
.media { height: 190px; border-radius: 14px; background: $pen-soft; }

.anchor {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: 0;
  border-radius: 14px;
  background: $pen-soft;
  color: $pen-ink;
  cursor: pointer;
  text-align: left;

  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px;
    strong { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
    span { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  }
  &__chev { flex: none; color: $pen-mute; }
}

.stats { margin: 0; color: $pen-mute; font-size: 13px; font-weight: 700; line-height: $pen-lh; }

.comment {
  display: flex;
  align-items: flex-start;
  gap: 10px;

  &__avatar { flex: none; width: 32px; height: 32px; border-radius: 999px; background: $pen-ink; }
  &__body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 13px; font-weight: 900; line-height: $pen-lh; }
  &__text { margin: 0; font-size: 13px; font-weight: 500; line-height: 1.4; }
  &__like { flex: none; color: $pen-mute; margin-top: 8px; }
}

.comment-bar {
  position: fixed;
  right: 0; bottom: 0; left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  &__input {
    flex: 1;
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-mute;
    font-size: 14px;
    font-weight: 500;
  }

  &__like {
    flex: none; width: 44px; height: 44px;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }
}
</style>
