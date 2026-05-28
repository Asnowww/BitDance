<script setup lang="ts">
import { ChevronLeft, Share2 } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

defineProps<{
  title: string;
  showShare?: boolean;
}>();

const emit = defineEmits<{
  back: [];
  share: [];
}>();

const router = useRouter();

const onBack = () => {
  emit('back');
  router.back();
};
</script>

<template>
  <header class="pen-topbar">
    <button type="button" class="pen-topbar__icon" aria-label="返回" @click="onBack">
      <ChevronLeft :size="20" :stroke-width="2" />
    </button>
    <h1 class="pen-topbar__title">{{ title }}</h1>
    <button
      v-if="showShare !== false"
      type="button"
      class="pen-topbar__icon"
      aria-label="分享"
      @click="emit('share')"
    >
      <Share2 :size="20" :stroke-width="2" />
    </button>
    <span v-else class="pen-topbar__spacer" aria-hidden="true" />
  </header>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__title {
    flex: 1;
    margin: 0;
    font-size: 18px;
    font-weight: 900;
    line-height: 1.25;
    letter-spacing: 0;
  }

  &__icon {
    display: inline-flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    padding: 0;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    cursor: pointer;
  }

  &__spacer {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
  }
}
</style>
