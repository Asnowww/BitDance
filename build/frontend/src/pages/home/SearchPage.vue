<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music, Search } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';

const router = useRouter();

const filters = ['舞种', '距离', '价格', '时段', '舞室'];
const activeFilter = ref('距离');
const viewMode = ref<'list' | 'map'>('list');

interface SearchResult {
  id: string;
  title: string;
  meta: string;
  tags: string[];
  price: string;
  priceTone: 'ink' | 'success';
  to: string;
}

const results: SearchResult[] = [
  {
    id: 'urban-flow',
    title: 'Urban Flow 舞室',
    meta: '1.2km · 4.8 · 韩舞/Urban',
    tags: ['零基础', '晚课班'],
    price: '¥79-128 / 节',
    priceTone: 'ink',
    to: '/studio/urban-flow'
  },
  {
    id: 'beats-lab',
    title: 'Beats Lab',
    meta: '2.8km · 4.7 · Jazz/Hiphop',
    tags: ['地铁直达', '试听'],
    price: '¥88 起',
    priceTone: 'ink',
    to: '/studio/beats-lab'
  },
  {
    id: 'k-star',
    title: 'K-Star Studio',
    meta: '4.6km · 4.6 · 韩舞成品舞',
    tags: ['热门成品舞'],
    price: '可约试听',
    priceTone: 'success',
    to: '/studio/k-star'
  }
];

const selected = ref<Record<string, boolean>>({});
const toggleSelect = (id: string) => {
  selected.value[id] = !selected.value[id];
};
const compareCount = () => Object.values(selected.value).filter(Boolean).length;

const onCompare = () => {
  if (compareCount() < 2) {
    showToast('请至少选择 2 个舞室进行对比');
    return;
  }
  router.push('/studio/compare');
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="搜索结果" @share="showToast('搜索结果链接已复制')" />

    <section class="pen-scroll">
      <button class="search-field" type="button" @click="router.back()">
        <Search class="search-field__icon" :size="18" :stroke-width="2" />
        <span class="search-field__text">韩舞 零基础 5km 内</span>
      </button>

      <div class="chip-row" aria-label="筛选条件">
        <button
          v-for="filter in filters"
          :key="filter"
          type="button"
          class="chip"
          :class="activeFilter === filter ? 'chip--active' : 'chip--inactive'"
          @click="activeFilter = filter"
        >
          {{ filter }}
        </button>
      </div>

      <div class="toggle">
        <button
          type="button"
          class="toggle__btn"
          :class="{ 'toggle__btn--active': viewMode === 'list' }"
          @click="viewMode = 'list'"
        >
          列表
        </button>
        <button
          type="button"
          class="toggle__btn"
          :class="{ 'toggle__btn--active': viewMode === 'map' }"
          @click="viewMode = 'map'"
        >
          地图
        </button>
      </div>

      <ul class="result-list">
        <li v-for="item in results" :key="item.id" class="result" @click="router.push(item.to)">
          <div class="result__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="result__body">
            <strong class="result__title">{{ item.title }}</strong>
            <p class="result__meta">{{ item.meta }}</p>
            <div class="result__tags">
              <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <span class="result__price" :class="`result__price--${item.priceTone}`">
              {{ item.price }}
            </span>
          </div>
          <button
            type="button"
            class="radio"
            :class="{ 'radio--on': selected[item.id] }"
            :aria-label="`选择 ${item.title}`"
            @click.stop="toggleSelect(item.id)"
          />
        </li>
      </ul>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="加入对比"
      @soft="showToast('已加入收藏')"
      @dark="onCompare"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;

  &--with-bar {
    padding-bottom: calc(76px + env(safe-area-inset-bottom));
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 44px;
  padding: 0 16px;
  border: 0;
  border-radius: 24px;
  background: $pen-soft;
  cursor: pointer;
  text-align: left;

  &__icon {
    flex: none;
    color: $pen-mute;
  }

  &__text {
    flex: 1;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 600;
    line-height: $pen-lh;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.toggle {
  display: flex;
  gap: 8px;

  &__btn {
    flex: 1;
    height: 48px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;

    &--active {
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.result {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 124px;
  cursor: pointer;

  &__cover {
    flex: none;
    display: grid;
    place-items: center;
    width: 112px;
    align-self: stretch;
    border-radius: 14px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 4px 0;
  }

  &__title {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  &__price {
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;

    &--ink {
      color: $pen-ink;
    }

    &--success {
      color: $pen-success;
    }
  }
}

.tag {
  height: 40px;
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.radio {
  flex: none;
  width: 24px;
  height: 24px;
  border: 2px solid $pen-ink;
  border-radius: 999px;
  background: $pen-canvas;
  cursor: pointer;

  &--on {
    background: $pen-ink;
  }
}
</style>
