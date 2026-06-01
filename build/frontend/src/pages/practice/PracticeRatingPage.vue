<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import StarRating from '@/components/StarRating.vue';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const route = useRoute();
const router = useRouter();
const practiceId = String(route.params.id || '');

const punctuality = ref(5);
const friendliness = ref(5);
const levelMatch = ref(4);
const comment = ref('');
const anonymous = ref(false);

const dims = [
  { label: '守时', model: punctuality },
  { label: '友好度', model: friendliness },
  { label: '水平匹配', model: levelMatch }
];

const onSubmit = () => {
  showSuccessToast('评价已提交');
  router.back();
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="约练评价" :show-share="false" />

    <section class="pen-scroll">
      <div class="target">
        <span class="target__avatar" aria-hidden="true" />
        <div class="target__copy">
          <strong class="target__name">阿 May</strong>
          <p class="target__meta">本次约练：周六 Hiphop 中级</p>
        </div>
      </div>

      <div v-for="d in dims" :key="d.label" class="dim">
        <span class="dim__label">{{ d.label }}</span>
        <StarRating v-model="d.model.value" :size="22" />
      </div>

      <textarea
        v-model="comment"
        class="note"
        rows="3"
        placeholder="补充说明对方的表现，帮助其他舞友参考…"
      />

      <div class="anon">
        <span class="anon__label">匿名评价</span>
        <button class="switch" :class="{ 'switch--on': anonymous }" type="button" @click="anonymous = !anonymous">
          <span class="switch__knob" />
        </button>
      </div>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="onSubmit">提交评价</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.target {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: $pen-soft;

  &__avatar { flex: none; width: 48px; height: 48px; border-radius: 999px; background: $pen-ink; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 16px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
}

.dim {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid $pen-hairline;

  &__label { font-size: 15px; font-weight: 800; line-height: $pen-lh; }

  :deep(.star) { color: $pen-hairline; }
  :deep(.star.active) { color: $pen-ink; }
}

.note {
  width: 100%;
  min-height: 84px;
  padding: 14px;
  border: 0;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  font-family: $pen-font;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  resize: none;
  box-sizing: border-box;
  outline: none;

  &::placeholder { color: $pen-mute; }
}

.anon {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid $pen-hairline;

  &__label { flex: 1; font-size: 15px; font-weight: 800; line-height: $pen-lh; }
}

.switch {
  flex: none;
  width: 46px;
  height: 28px;
  padding: 3px;
  border: 0;
  border-radius: 999px;
  background: $pen-hairline;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  cursor: pointer;

  &__knob { width: 22px; height: 22px; border-radius: 999px; background: $pen-canvas; }
  &--on { background: $pen-ink; justify-content: flex-end; }
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
