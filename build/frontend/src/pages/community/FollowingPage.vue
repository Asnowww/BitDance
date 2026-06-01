<script setup lang="ts">
import { reactive, ref } from 'vue';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const tab = ref<'following' | 'fans'>('following');

const users = reactive([
  { id: '1', name: '小鹿老师', meta: 'Jazz · 认证教练', state: '已关注', followed: true },
  { id: '2', name: 'A Jen', meta: 'Hiphop · 中级 · 同城', state: '互相关注', followed: true },
  { id: '3', name: 'Leo', meta: 'Urban · 中级', state: '已关注', followed: true },
  { id: '4', name: '韩舞研习社', meta: '话题社区 · 8900 成员', state: '关注', followed: false }
]);

const toggle = (u: (typeof users)[number]) => {
  u.followed = !u.followed;
  u.state = u.followed ? '已关注' : '关注';
};
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="关注" :show-share="false" />

    <section class="pen-scroll">
      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'following' }" type="button" @click="tab = 'following'">关注 86</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'fans' }" type="button" @click="tab = 'fans'">粉丝 124</button>
      </div>

      <article v-for="u in users" :key="u.id" class="user">
        <span class="user__avatar" aria-hidden="true" />
        <div class="user__copy">
          <strong class="user__name">{{ u.name }}</strong>
          <span class="user__meta">{{ u.meta }}</span>
        </div>
        <button
          class="user__pill"
          :class="{ 'user__pill--solid': !u.followed }"
          type="button"
          @click="toggle(u)"
        >
          {{ u.state }}
        </button>
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

.seg {
  display: flex;
  gap: 8px;
  &__btn {
    flex: 1;
    height: 46px;
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

.user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__avatar { flex: none; width: 48px; height: 48px; border-radius: 999px; background: $pen-ink; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }

  &__pill {
    flex: none;
    height: 36px;
    padding: 8px 16px;
    border: 1px solid $pen-ink;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;

    &--solid { background: $pen-ink; color: $pen-on-primary; }
  }
}
</style>
