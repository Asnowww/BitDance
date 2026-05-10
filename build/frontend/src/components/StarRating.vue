<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  modelValue: number;
  max?: number;
  readonly?: boolean;
  size?: number;
}>();

const emit = defineEmits<{ (e: 'update:modelValue', v: number): void }>();

const max = computed(() => props.max ?? 5);
const stars = computed(() => Array.from({ length: max.value }).map((_, i) => i + 1));
const size = computed(() => props.size ?? 22);

const setValue = (v: number) => {
  if (props.readonly) return;
  emit('update:modelValue', v);
};
</script>

<template>
  <div class="stars" :style="{ fontSize: `${size}px` }">
    <span
      v-for="n in stars"
      :key="n"
      class="star"
      :class="{ active: n <= props.modelValue, readonly: props.readonly }"
      @click="setValue(n)"
    >
      ★
    </span>
  </div>
</template>

<style lang="scss" scoped>
.stars {
  display: inline-flex;
  gap: 2px;
}
.star {
  color: #d8d8d8;
  cursor: pointer;
  &.active {
    color: #ffaa33;
  }
  &.readonly {
    cursor: default;
  }
}
</style>
