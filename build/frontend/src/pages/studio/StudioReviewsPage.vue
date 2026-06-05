<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

onMounted(() => {
  router.replace({
    path: `/studio/${Number(route.params.id) || 1}`,
    query: { tab: 'reviews' }
  });
});
</script>

<template>
  <main class="review-redirect" aria-live="polite">
    正在打开新版评价聚合页...
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.review-redirect {
  @include pen-page;
  display: grid;
  place-items: center;
  padding: 24px;
  color: $pen-mute;
  font-size: 14px;
  font-weight: 800;
}
</style>
