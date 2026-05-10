<script setup lang="ts">
defineProps<{ title?: string; desc?: string; retryText?: string }>();
defineEmits<{ (e: 'retry'): void }>();
</script>

<template>
  <div class="err">
    <div class="err__icon">⚠️</div>
    <div class="err__title">{{ title ?? '加载失败' }}</div>
    <div v-if="desc" class="err__desc">{{ desc }}</div>
    <button class="err__btn" @click="$emit('retry')">{{ retryText ?? '重试' }}</button>
  </div>
</template>

<style lang="scss" scoped>
.err {
  text-align: center;
  padding: 60px 24px;
  &__icon {
    font-size: 56px;
  }
  &__title {
    margin-top: 16px;
    font-size: 15px;
    font-weight: 600;
  }
  &__desc {
    margin-top: 6px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__btn {
    margin-top: 16px;
    height: 36px;
    padding: 0 20px;
    border: 1px solid var(--bd-primary);
    border-radius: 999px;
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
    font-size: 13px;
    cursor: pointer;
  }
}
</style>
