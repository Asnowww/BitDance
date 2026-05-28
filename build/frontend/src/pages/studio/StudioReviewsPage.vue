<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';

const router = useRouter();
const activeFilter = ref('已验证');

const filters = ['全部', '已验证', '带图', '零基础', '差评'];
const reviews = [
  {
    author: '已验证 · 小林',
    text: '老师会拆动作，节奏适合第一次学韩舞的人。',
    meta: '零基础友好 5 · 纠错质量 5 · 氛围 4'
  },
  {
    author: '已验证 · Kiki',
    text: '场地干净，晚课多，地铁出来很好找。',
    meta: '零基础友好 5 · 纠错质量 5 · 氛围 4'
  }
];
const formRows = ['评价对象：舞室 / 老师 / 课程', '结构化评分维度', '图文/视频上传', '匿名开关'];

const onShare = () => showToast('已复制评价页链接');
const onPublish = () => {
  showToast('评价发布流程已准备');
  router.push('/publish/review');
};
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="评价系统" @share="onShare" />

    <section class="pen-body">
      <section class="score-card">
        <strong class="score-card__num">4.8</strong>
        <div class="score-card__meta">
          <h2>综合评分</h2>
          <p>环境 4.7 · 纠错 4.9 · 零基础友好 4.8</p>
        </div>
      </section>

      <nav class="chips" aria-label="评价筛选">
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
      </nav>

      <article v-for="review in reviews" :key="review.author" class="review-card">
        <h3>{{ review.author }}</h3>
        <p>{{ review.text }}</p>
        <span>{{ review.meta }}</span>
      </article>

      <section class="form-block">
        <h2 class="form-block__title">写评价表单</h2>
        <PenSettingRow
          v-for="row in formRows"
          :key="row"
          :label="row"
          trailing="设置"
          @click="showToast(row)"
        />
        <button type="button" class="pen-primary-btn" @click="onPublish">发布评价</button>
      </section>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px 24px;
}

.score-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;

  &__num {
    font-size: 48px;
    font-weight: 900;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  &__meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;

    h2 {
      margin: 0;
      font-size: 16px;
      font-weight: 900;
      line-height: $pen-lh;
      letter-spacing: 0;
    }

    p {
      margin: 0;
      color: $pen-subtle-text;
      font-size: 12px;
      font-weight: 600;
      line-height: $pen-lh;
      letter-spacing: 0;
    }
  }
}

.chips {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  overflow-x: auto;
}

.chip {
  @include pen-chip;
}

.review-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  background: $pen-soft;

  h3,
  p,
  span {
    margin: 0;
    letter-spacing: 0;
  }

  h3 {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    font-size: 14px;
    font-weight: 500;
    line-height: $pen-lh;
  }

  span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.form-block {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__title {
    @include pen-h3-section;
  }
}

.pen-primary-btn {
  @include pen-primary-btn;
  width: 100%;
  margin-top: 6px;
}
</style>
