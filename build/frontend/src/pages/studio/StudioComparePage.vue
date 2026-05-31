<script setup lang="ts">
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';

const columns = ['维度', 'Urban', 'BeatLab', 'K-Star'];
const rows: Array<{ label: string; values: string[] }> = [
  { label: '距离', values: ['1.2km', '2.8km', '4.6km'] },
  { label: '价格', values: ['¥79', '¥88', '¥69'] },
  { label: '评分', values: ['4.8', '4.7', '4.6'] },
  { label: '零基础', values: ['强', '中', '强'] },
  { label: '晚课', values: ['多', '少', '中'] },
  { label: '操作', values: ['预约', '收藏', '详情'] }
];

const onShare = () => showToast('已生成对比分享卡');
const onBook = () => showToast('已选择 Urban 作为最优预约');
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="舞室对比" @share="onShare" />

    <section class="pen-body pen-body--compare">
      <h2 class="pen-h2">3 家舞室对比</h2>

      <div class="compare-grid" role="table" aria-label="舞室对比表">
        <div class="compare-grid__row compare-grid__row--head" role="row">
          <div
            v-for="(col, i) in columns"
            :key="col"
            class="compare-cell"
            :class="i === 0 ? 'compare-cell--dim' : 'compare-cell--head'"
            role="columnheader"
          >
            {{ col }}
          </div>
        </div>
        <div v-for="row in rows" :key="row.label" class="compare-grid__row compare-grid__row--body" role="row">
          <div class="compare-cell compare-cell--label" role="rowheader">{{ row.label }}</div>
          <div
            v-for="(val, i) in row.values"
            :key="`${row.label}-${i}`"
            class="compare-cell compare-cell--value"
            :class="{ 'compare-cell--book': val === '预约' }"
            role="cell"
          >
            {{ val }}
          </div>
        </div>
      </div>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="预约最优"
      @soft="showToast('已收藏对比')"
      @dark="onBook"
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

.pen-body--compare {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 14px;
}

.pen-h2 {
  @include pen-h2;
}

.compare-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__row {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 6px;

    &--head {
      height: 72px;
    }

    &--body .compare-cell {
      height: 58px;
      border: 1px solid $pen-hairline;
    }
  }
}

.compare-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  padding: 8px;
  box-sizing: border-box;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
  letter-spacing: 0;
  text-align: center;

  &--dim {
    height: 72px;
    border: none;
    background: $pen-ink;
    color: $pen-on-primary;
    font-weight: 900;
  }

  &--head {
    height: 72px;
    border: none;
    background: $pen-soft;
    color: $pen-ink;
    font-weight: 900;
  }

  &--label {
    background: $pen-soft;
    color: $pen-ink;
  }

  &--value {
    background: $pen-canvas;
    color: $pen-mute;
  }

  &--book {
    color: $pen-success;
  }
}
</style>
