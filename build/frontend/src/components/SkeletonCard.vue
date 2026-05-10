<script setup lang="ts">
defineProps<{ count?: number; height?: number }>();
</script>

<template>
  <div class="sk-list">
    <div v-for="i in (count ?? 6)" :key="i" class="sk" :style="{ height: `${height ?? 96}px` }" />
  </div>
</template>

<style lang="scss" scoped>
.sk-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
}
.sk {
  background: linear-gradient(90deg, #f3f3f3 0%, #ececec 50%, #f3f3f3 100%);
  background-size: 200% 100%;
  border-radius: 12px;
  animation: shimmer 1.4s infinite linear;
}
@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
