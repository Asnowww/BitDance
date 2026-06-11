<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, RouterView } from 'vue-router';
import AppTabBar from './AppTabBar.vue';
import OpsTabBar from './OpsTabBar.vue';

const route = useRoute();
const showTabBar = computed(() => route.meta?.hideTabBar !== true);
// 运营端管理页(meta.ops)使用工作台/课表/订单/评价/我的专属导航
const isOps = computed(() => route.meta?.ops === true);
</script>

<template>
  <div class="layout" :class="{ 'has-tabbar': showTabBar }">
    <main class="layout__main" :class="{ 'has-tabbar': showTabBar }">
      <RouterView v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    <OpsTabBar v-if="showTabBar && isOps" />
    <AppTabBar v-else-if="showTabBar" />
  </div>
</template>

<style lang="scss" scoped>
.layout {
  --app-tabbar-offset: 0px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  &.has-tabbar {
    --app-tabbar-offset: calc(72px + env(safe-area-inset-bottom));
  }

  &__main {
    flex: 1;
    &.has-tabbar {
      padding-bottom: calc(72px + env(safe-area-inset-bottom));
    }
  }
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
