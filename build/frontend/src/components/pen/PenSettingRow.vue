<script setup lang="ts">
import { ChevronRight } from 'lucide-vue-next';

defineProps<{
  label: string;
  trailing?: string;
}>();

const emit = defineEmits<{
  click: [];
}>();
</script>

<template>
  <button type="button" class="pen-setting-row" @click="emit('click')">
    <span class="pen-setting-row__label">{{ label }}</span>
    <em v-if="trailing !== undefined" class="pen-setting-row__trail">{{ trailing }}</em>
    <slot v-else name="value" />
    <ChevronRight class="pen-setting-row__chevron" :size="18" :stroke-width="2" />
  </button>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-setting-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 16px 0;
  border: 0;
  border-bottom: 1px solid $pen-hairline;
  background: $pen-canvas;
  color: $pen-ink;
  cursor: pointer;
  text-align: left;

  &__label {
    flex: 1;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  &__trail {
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
