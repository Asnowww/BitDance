<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, SquarePen, MapPin, Users, Heart, MessageCircle, Share2 } from 'lucide-vue-next';

const router = useRouter();
const cats = ['推荐', '关注', '同城', '话题'];
const activeCat = ref('推荐');

const posts = [
  {
    id: '1', name: '小鹿', meta: '五道口 · 2 小时前',
    text: '今天试听了 Urban Flow 的韩舞课，老师超耐心，零基础也跟得上！',
    anchorIcon: MapPin, anchor: 'Urban Flow 舞室', likes: '32', comments: '8'
  },
  {
    id: '2', name: 'A Jen', meta: '朝阳 · 昨天',
    text: '周六约练打卡，和搭子一起磨 Hiphop routine～',
    anchorIcon: Users, anchor: 'Hiphop 约练', likes: '51', comments: '12'
  }
];
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">社区</h1>
      <button class="topbar__compose" type="button" aria-label="发动态" @click="router.push('/community/publish')">
        <SquarePen :size="20" :stroke-width="2" />
      </button>
    </header>

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

      <article v-for="p in posts" :key="p.id" class="post" @click="router.push(`/community/post/${p.id}`)">
        <header class="post__head">
          <span class="post__avatar" aria-hidden="true" />
          <div class="post__who">
            <strong class="post__name">{{ p.name }}</strong>
            <span class="post__meta">{{ p.meta }}</span>
          </div>
          <button class="post__follow" type="button" @click.stop>关注</button>
        </header>
        <p class="post__text">{{ p.text }}</p>
        <div class="post__media" aria-hidden="true" />
        <div class="post__anchor">
          <component :is="p.anchorIcon" :size="14" :stroke-width="2" />
          <span>{{ p.anchor }}</span>
        </div>
        <div class="post__actions">
          <span class="act"><Heart :size="18" :stroke-width="2" />{{ p.likes }}</span>
          <span class="act"><MessageCircle :size="18" :stroke-width="2" />{{ p.comments }}</span>
          <span class="act"><Share2 :size="18" :stroke-width="2" /></span>
        </div>
      </article>
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
  &__compose {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    display: grid; place-items: center; cursor: pointer;
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.post {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid $pen-hairline;
  cursor: pointer;

  &__head { display: flex; align-items: center; gap: 10px; }
  &__avatar { flex: none; width: 40px; height: 40px; border-radius: 999px; background: $pen-ink; }
  &__who { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
  &__name { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }

  &__follow {
    flex: none;
    height: 32px;
    padding: 6px 12px;
    border: 1px solid $pen-ink;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;
  }

  &__text { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
  &__media { height: 160px; border-radius: 14px; background: $pen-soft; }

  &__anchor {
    align-self: flex-start;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 6px 12px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__actions { display: flex; gap: 20px; }
}

.act {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 600;
  line-height: $pen-lh;
}
</style>
