<script setup lang="ts">
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Sparkles, Play, Award } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();

const works = [
  { date: '5/24', day: '周六', icon: Play, title: 'K-pop 成品舞 v2', note: '节奏稳了，手位更干净' },
  { date: '5/10', day: '周五', icon: Play, title: 'Locking 基础 routine', note: '第一次完整跳下来' },
  { date: '4/28', day: '周日', icon: Award, title: '入门 30 天打卡里程碑', note: '坚持满一个月' }
];
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="阶段作品" @share="showToast('已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <div class="hero__bars" aria-hidden="true"><span v-for="i in 6" :key="i" /></div>
        <Sparkles class="hero__icon" :size="40" :stroke-width="2" />
        <strong class="hero__title">MY PROGRESS</strong>
        <p class="hero__meta">12 个作品 · 3 个里程碑</p>
      </section>

      <section class="timeline">
        <article v-for="w in works" :key="w.date" class="entry">
          <div class="entry__date">
            <strong>{{ w.date }}</strong>
            <span>{{ w.day }}</span>
          </div>
          <div class="entry__card">
            <div class="entry__media" aria-hidden="true">
              <component :is="w.icon" :size="30" :stroke-width="2" />
            </div>
            <strong class="entry__title">{{ w.title }}</strong>
            <p class="entry__note">{{ w.note }}</p>
          </div>
        </article>
      </section>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="showToast('上传新作品')">上传新作品</button>
    </footer>
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
  height: 180px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__bars {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
    height: 30px;
    margin-bottom: auto;
    span { height: 100%; background: $pen-charcoal; }
  }
  &__icon { flex-shrink: 0; color: $pen-on-primary; }
  &__title { margin: 0; font-size: 30px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.timeline { display: flex; flex-direction: column; gap: 16px; padding: 0 18px 20px; }

.entry {
  display: flex;
  gap: 12px;
  align-items: flex-start;

  &__date {
    flex: none;
    width: 44px;
    display: flex;
    flex-direction: column;
    gap: 2px;
    strong { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
    span { color: $pen-mute; font-size: 11px; font-weight: 600; line-height: $pen-lh; }
  }

  &__card { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; }

  &__media {
    height: 116px;
    border-radius: 14px;
    background: $pen-ink;
    color: $pen-on-primary;
    display: grid;
    place-items: center;
  }

  &__title { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__note { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
}

.save-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
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
