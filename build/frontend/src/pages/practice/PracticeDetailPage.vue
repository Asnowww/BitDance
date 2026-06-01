<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Users } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const route = useRoute();
const router = useRouter();
const practiceId = String(route.params.id || 'hiphop-mid');

const stats = [
  { value: '2/4', label: '报名人数' },
  { value: '中级', label: '水平要求' },
  { value: '90min', label: '单次时长' }
];

const fields = [
  { label: '地点', value: '五道口 DanceLab' },
  { label: '时间', value: '周六 14:00–16:00' },
  { label: '费用', value: 'AA 场地费 ¥30' },
  { label: '水平要求', value: '中级及以上' }
];

const onJoin = () => {
  showToast('已报名，等待发起人确认');
  router.push(`/practice/${practiceId}/rate`);
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="约练详情" @share="showToast('约练链接已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <div class="hero__bars" aria-hidden="true"><span v-for="i in 6" :key="i" /></div>
        <Users class="hero__icon" :size="42" :stroke-width="2" />
        <strong class="hero__title">HIPHOP<br />中级局</strong>
        <p class="hero__meta">五道口 DanceLab · 周六 14:00–16:00</p>
      </section>

      <section class="body">
        <div class="host">
          <span class="host__avatar" aria-hidden="true" />
          <div class="host__copy">
            <strong class="host__name">阿 May 发起</strong>
            <p class="host__meta">中级 · 学舞 2 年</p>
          </div>
          <span class="tag">已验证</span>
        </div>

        <div class="stats">
          <div v-for="s in stats" :key="s.label" class="stat">
            <strong class="stat__value">{{ s.value }}</strong>
            <span class="stat__label">{{ s.label }}</span>
          </div>
        </div>

        <div class="rows">
          <PenFieldRow v-for="f in fields" :key="f.label" :label="f.label" :value="f.value" />
        </div>

        <p class="note">有效期至 6/7 · 满员后自动关闭，发起人可提前取消</p>
      </section>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="我要参加"
      @soft="showToast('已收藏')"
      @dark="onJoin"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 200px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__bars {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
    height: 30px;
    margin-bottom: auto;
    span { height: 100%; background: $pen-charcoal; }
  }

  &__icon { flex-shrink: 0; color: $pen-on-primary; }
  &__title { margin: 0; font-size: 32px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 18px 20px;
}

.host {
  display: flex;
  align-items: center;
  gap: 12px;

  &__avatar { flex: none; width: 48px; height: 48px; border-radius: 999px; background: $pen-ink; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 16px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
}

.tag {
  flex: none;
  height: 32px;
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  height: 80px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border-radius: 16px;
  background: $pen-soft;

  &__value { font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__label { color: $pen-mute; font-size: 12px; font-weight: 700; line-height: $pen-lh; }
}

.rows { display: flex; flex-direction: column; }

.note {
  margin: 0;
  color: $pen-mute;
  font-size: 12px;
  font-weight: 600;
  line-height: $pen-lh;
}
</style>
