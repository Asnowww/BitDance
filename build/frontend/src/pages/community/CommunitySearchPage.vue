<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, Search } from 'lucide-vue-next';

const router = useRouter();
const keyword = ref('');

const tabs = ['动态', '话题', '用户', '活动'];
const activeTab = ref('动态');
const sorts = ['热度', '最新', '距离'];
const activeSort = ref('热度');

const recent = ['韩舞', '零基础', 'Urban Flow', '约练搭子'];
const hot = ['# 零基础打卡', '# 韩舞成品舞', 'Locking 大师课'];
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <div class="topbar__field">
        <Search :size="18" :stroke-width="2" />
        <input v-model="keyword" type="text" placeholder="搜索动态、话题、舞友" />
      </div>
      <button class="topbar__cancel" type="button" @click="router.back()">取消</button>
    </header>

    <section class="pen-scroll">
      <div class="chip-row">
        <button
          v-for="t in tabs"
          :key="t"
          class="chip"
          :class="activeTab === t ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeTab = t"
        >
          {{ t }}
        </button>
      </div>

      <div class="chip-row">
        <button
          v-for="s in sorts"
          :key="s"
          class="sortchip"
          :class="{ 'sortchip--on': activeSort === s }"
          type="button"
          @click="activeSort = s"
        >
          {{ s }}
        </button>
      </div>

      <h2 class="block-title">最近搜索</h2>
      <div class="flow">
        <button v-for="r in recent" :key="r" class="tagchip" type="button" @click="keyword = r">{{ r }}</button>
      </div>

      <h2 class="block-title">热门搜索</h2>
      <div class="flow">
        <button v-for="h in hot" :key="h" class="tagchip" type="button" @click="keyword = h">{{ h }}</button>
      </div>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__icon {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }

  &__field {
    flex: 1;
    min-width: 0;
    height: 40px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 14px;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-mute;

    input {
      flex: 1;
      min-width: 0;
      border: 0;
      background: transparent;
      color: $pen-ink;
      font-size: 14px;
      font-weight: 600;
      outline: none;
      &::placeholder { color: $pen-mute; font-weight: 500; }
    }
  }

  &__cancel {
    flex: none;
    border: 0;
    background: transparent;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.sortchip {
  height: 34px;
  padding: 6px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-mute;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;
  cursor: pointer;
  &--on { color: $pen-ink; }
}

.block-title { @include pen-h3-section; }

.flow { display: flex; flex-wrap: wrap; gap: 8px; }

.tagchip {
  height: 36px;
  padding: 8px 14px;
  border: 0;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 600;
  line-height: $pen-lh;
  cursor: pointer;
}
</style>
