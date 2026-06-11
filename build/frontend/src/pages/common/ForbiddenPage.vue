<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ShieldAlert } from 'lucide-vue-next';

const router = useRouter();
</script>

<template>
  <main class="pen-page forbidden">
    <ShieldAlert class="forbidden__icon" :size="58" :stroke-width="2" />
    <strong class="forbidden__code">403</strong>
    <p class="forbidden__text">&#24403;&#21069;&#36134;&#21495;&#26080;&#26435;&#35775;&#38382;&#35813;&#39029;&#38754;</p>
    <button class="forbidden__btn" type="button" @click="router.replace('/me')">
      &#22238;&#21040;&#25105;&#30340;
    </button>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.forbidden {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  padding: 32px;
  box-sizing: border-box;
  text-align: center;

  &__icon { color: $pen-ink; }

  &__code {
    font-size: 72px;
    font-weight: 900;
    line-height: 1;
    letter-spacing: 0;
  }

  &__text {
    margin: 0;
    color: $pen-mute;
    font-size: 15px;
    font-weight: 700;
    line-height: $pen-lh;
  }

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
