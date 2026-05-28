<script setup lang="ts">
import { ChevronRight } from 'lucide-vue-next';

defineProps<{
  label: string;
  value: string;
}>();

const emit = defineEmits<{
  click: [];
}>();
</script>

<template>
  <button type="button" class="pen-field-row" @click="emit('click')">
    <span class="pen-field-row__label">{{ label }}</span>
    <em class="pen-field-row__value">{{ value }}</em>
    <ChevronRight class="pen-field-row__chevron" :size="18" :stroke-width="2" />
  </button>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-field-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 16px 0;
  border: 0;
  border-bottom: 1px solid $pen-hairline;
  background: $pen-canvas;
  cursor: pointer;
  text-align: left;

  &__label {
    flex: 1;
    color: $pen-ink;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  &__value {
    color: $pen-mute;
    font-size: 14px;
    font-style: normal;
    font-weight: 600;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  &__chevron {
    flex-shrink: 0;
    color: $pen-mute;
  }
}
</style>
