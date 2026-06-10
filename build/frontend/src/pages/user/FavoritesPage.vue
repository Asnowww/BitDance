<script setup lang="ts">
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();

const stats = [
  { value: '126', label: '累计天数' },
  { value: '43h', label: '训练时长' },
  { value: '5', label: '尝试舞种' }
];

const trend = [
  { day: '周一', ratio: 0.6 },
  { day: '周二', ratio: 0.85 },
  { day: '周三', ratio: 0.5 },
  { day: '周四', ratio: 1 },
  { day: '周五', ratio: 0.7 }
];

interface FavoriteCard {
  id: string;
  title: string;
  meta: string;
  tag: string;
  action: string;
  to: string;
}

const favorites: FavoriteCard[] = [
  {
    id: 'urban-flow',
    title: 'Urban Flow 舞室',
    meta: '收藏于 5/24 · 可预约',
    tag: '舞室',
    action: '预约试听',
    to: '/studio/urban-flow'
  },
  {
    id: 'mia-jazz',
    title: 'Mia Jazz 基础课',
    meta: '周三晚 · 可报名',
    tag: '课程',
    action: '立即报名',
    to: '/course/mia-jazz'
  }
];
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="学习数据" @share="showToast('学习数据链接已复制')" />

    <section class="pen-scroll">
      <div class="stats">
        <div v-for="stat in stats" :key="stat.label" class="stat">
          <strong class="stat__value">{{ stat.value }}</strong>
          <span class="stat__label">{{ stat.label }}</span>
        </div>
      </div>

      <section class="trend">
        <h3 class="trend__title">训练趋势</h3>
        <div class="trend__rows">
          <div v-for="item in trend" :key="item.day" class="trend-row">
            <span class="trend-row__day">{{ item.day }}</span>
            <span class="trend-row__bar" :style="{ width: `${item.ratio * 100}%` }" />
          </div>
        </div>
      </section>

      <section class="favorites">
        <header class="favorites__head">
          <h3>收藏管理</h3>
          <span class="favorites__sub">舞室 / 课程 / 老师</span>
        </header>

        <article
          v-for="item in favorites"
          :key="item.id"
          class="fav"
          @click="router.push(item.to)"
        >
          <div class="fav__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="fav__body">
            <strong class="fav__title">{{ item.title }}</strong>
            <p class="fav__meta">{{ item.meta }}</p>
            <span class="tag">{{ item.tag }}</span>
            <button class="fav__action" type="button" @click.stop="router.push(item.to)">
              {{ item.action }}
            </button>
          </div>
        </article>
      </section>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  height: 112px;
}

.stat {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 14px;
  border-radius: 16px;
  background: $pen-soft;

  &__value {
    font-size: 28px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__label {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.trend {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__title {
    @include pen-h3-section;
  }

  &__rows {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.trend-row {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 32px;

  &__day {
    flex: none;
    width: 36px;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__bar {
    height: 10px;
    border-radius: 999px;
    background: $pen-ink;
  }
}

.favorites {
  display: flex;
  flex-direction: column;
  gap: 16px;

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__head h3 {
    @include pen-h3-section;
    flex: 1;
  }

  &__sub {
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.fav {
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

  &__action {
    align-self: flex-start;
    padding: 0;
    border: 0;
    background: transparent;
    color: $pen-success;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.tag {
  align-self: flex-start;
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
</style>
