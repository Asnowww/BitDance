<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Compass } from 'lucide-vue-next';

const router = useRouter();
</script>

<template>
  <main class="pen-page nf">
    <Compass class="nf__icon" :size="56" :stroke-width="2" />
    <strong class="nf__code">404</strong>
    <p class="nf__text">这里还没有舞池</p>
    <button class="nf__btn" type="button" @click="router.replace('/home')">回到发现页</button>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.nf {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  padding: 32px;
  box-sizing: border-box;

  &__icon { color: $pen-ink; }
  &__code { font-size: 72px; font-weight: 900; line-height: 1; letter-spacing: 0; }
  &__text { margin: 0; color: $pen-mute; font-size: 15px; font-weight: 600; line-height: $pen-lh; }

  &__btn {
    height: 48px;
    padding: 12px 28px;
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
