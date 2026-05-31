<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';

const router = useRouter();

const filters = ['全部', '已验证', '带图', '零基础', '差评'];
const activeFilter = ref('已验证');

const reviews = [
  {
    id: 'lin',
    author: '已验证 · 小林',
    content: '老师会拆动作，节奏适合第一次学韩舞的人。',
    score: '零基础友好 5 · 纠错质量 5 · 氛围 4'
  },
  {
    id: 'kiki',
    author: '已验证 · Kiki',
    content: '场地干净，晚课多，地铁出来很好找。',
    score: '零基础友好 5 · 纠错质量 5 · 氛围 4'
  }
];

const formRows = ['评价对象：舞室 / 老师 / 课程', '结构化评分维度', '图文/视频上传', '匿名开关'];

const onPublish = () => {
  showSuccessToast('评价已发布');
  router.back();
};
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="评价系统" @share="showToast('评价链接已复制')" />

    <section class="pen-scroll">
      <section class="score">
        <strong class="score__value">4.8</strong>
        <div class="score__copy">
          <span class="score__title">综合评分</span>
          <span class="score__detail">环境 4.7 · 纠错 4.9 · 零基础友好 4.8</span>
        </div>
      </section>

      <div class="chip-row" aria-label="评价筛选">
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

      <article v-for="review in reviews" :key="review.id" class="review">
        <strong class="review__author">{{ review.author }}</strong>
        <p class="review__content">{{ review.content }}</p>
        <p class="review__score">{{ review.score }}</p>
      </article>

      <section class="form">
        <h3 class="form__title">写评价表单</h3>
        <PenSettingRow
          v-for="row in formRows"
          :key="row"
          :label="row"
          trailing="设置"
          @click="showToast(`设置：${row}`)"
        />
      </section>

      <button type="button" class="publish" @click="onPublish">发布评价</button>
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
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.score {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-ink;
  color: $pen-on-primary;

  &__value {
    font-size: 48px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__copy {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__title {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__detail {
    color: $pen-subtle-text;
    font-size: 12px;
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

.review {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border-radius: 16px;
  background: $pen-soft;

  &__author {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__content {
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    line-height: $pen-lh;
  }

  &__score {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.form {
  display: flex;
  flex-direction: column;

  &__title {
    @include pen-h3-section;
    padding: 8px 0;
  }
}

.publish {
  height: 48px;
  margin-top: 2px;
  border: 0;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 15px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;
}
</style>
