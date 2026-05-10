<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">教练主页</span>
    </header>
    <section class="hero">
      <div class="avatar">{{ (user.profile?.nickname ?? '?').charAt(0) }}</div>
      <div class="name">{{ user.profile?.nickname }}</div>
      <div class="sub">教练 · 工作台</div>
    </section>
    <section class="grid">
      <div class="cell">
        <div class="cell__num">0</div>
        <div class="cell__label">本月授课</div>
      </div>
      <div class="cell">
        <div class="cell__num">0</div>
        <div class="cell__label">学员评分</div>
      </div>
      <div class="cell">
        <div class="cell__num">0</div>
        <div class="cell__label">待回复评价</div>
      </div>
      <div class="cell">
        <div class="cell__num">0</div>
        <div class="cell__label">本月收益</div>
      </div>
    </section>
    <section class="list">
      <div class="row">📋 个人资料编辑（待实现）</div>
      <div class="row">📅 我的课程与排期（待实现）</div>
      <div class="row">🎤 Workshop 创建（阶段 C）</div>
      <div class="row">💬 评价回复与申诉（阶段 C）</div>
    </section>
    <p class="tip">教练侧完整能力为阶段 C 内容，本页提供基础工作台框架。</p>
  </div>
</template>

<style lang="scss" scoped>
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.hero {
  background: linear-gradient(180deg, #ffe2e8, #fff);
  text-align: center;
  padding: 32px 16px 20px;
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  margin: 0 auto;
  background: var(--bd-primary);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.name {
  margin-top: 12px;
  font-size: 20px;
  font-weight: 700;
}
.sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1px;
  background: var(--bd-border);
  margin-top: 8px;
}
.cell {
  background: #fff;
  padding: 14px;
  text-align: center;
  &__num {
    font-size: 22px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__label {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.list {
  margin-top: 8px;
  background: #fff;
}
.row {
  padding: 14px 16px;
  font-size: 14px;
  color: var(--bd-text-secondary);
  border-bottom: 1px solid var(--bd-border);
}
.tip {
  padding: 16px;
  font-size: 12px;
  color: var(--bd-text-secondary);
  text-align: center;
}
</style>
