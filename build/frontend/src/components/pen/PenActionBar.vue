<script setup lang="ts">
// 底部操作栏允许页面显式声明“当前还不能点”，避免出现看起来可用、实际上不该触发的伪动作。
defineProps<{
  softLabel: string;
  darkLabel: string;
  softDisabled?: boolean;
  darkDisabled?: boolean;
  hideSoft?: boolean;
}>();

const emit = defineEmits<{
  soft: [];
  dark: [];
}>();
</script>

<template>
  <footer class="pen-action-bar">
    <button
      v-if="!hideSoft"
      type="button"
      class="pen-action-bar__btn pen-action-bar__btn--soft"
      :disabled="softDisabled"
      @click="emit('soft')"
    >
      {{ softLabel }}
    </button>
    <button
      type="button"
      class="pen-action-bar__btn pen-action-bar__btn--dark"
      :disabled="darkDisabled"
      @click="emit('dark')"
    >
      <slot name="dark">{{ darkLabel }}</slot>
    </button>
  </footer>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-action-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 10;
  display: flex;
  gap: 10px;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  &__btn {
    flex: 1;
    height: 48px;
    padding: 12px 22px;
    border: 0;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
    letter-spacing: 0;
    cursor: pointer;

    &:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    &--soft {
      background: $pen-soft;
      color: $pen-ink;
    }

    &--dark {
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }
}
</style>
