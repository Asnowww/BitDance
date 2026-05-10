<script setup lang="ts">
defineProps<{ icon?: string; title?: string; desc?: string; actionText?: string }>();
defineEmits<{ (e: 'action'): void }>();
</script>

<template>
  <div class="empty">
    <div class="empty__icon">{{ icon ?? '🌱' }}</div>
    <div class="empty__title">{{ title ?? '暂无内容' }}</div>
    <div v-if="desc" class="empty__desc">{{ desc }}</div>
    <button v-if="actionText" class="empty__btn" @click="$emit('action')">{{ actionText }}</button>
  </div>
</template>

<style lang="scss" scoped>
.empty {
  text-align: center;
  padding: 60px 24px;
  &__icon {
    font-size: 56px;
    line-height: 1;
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
    border: none;
    border-radius: 999px;
    background: var(--bd-primary);
    color: #fff;
    font-size: 13px;
    cursor: pointer;
  }
}
</style>
