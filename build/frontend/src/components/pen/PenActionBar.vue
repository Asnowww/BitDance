<script setup lang="ts">
defineProps<{
  softLabel: string;
  darkLabel: string;
  darkDisabled?: boolean;
}>();

const emit = defineEmits<{
  soft: [];
  dark: [];
}>();
</script>

<template>
  <footer class="pen-action-bar">
    <button type="button" class="pen-action-bar__btn pen-action-bar__btn--soft" @click="emit('soft')">
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
  bottom: 0;
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
