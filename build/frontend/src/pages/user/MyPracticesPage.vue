<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Users } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();
const tab = ref<'mine' | 'joined'>('mine');

const records = [
  { id: '1', title: '周六 Hiphop 中级局', meta: '五道口 DanceLab · 周六 14:00', status: '报名中 2/4 人', tone: 'ink', action: '管理' },
  { id: '2', title: '韩舞成品舞互拍', meta: '朝阳 Joy Studio · 今晚 19:00', status: '已满员 3/3', tone: 'mute', action: '查看' },
  { id: '3', title: 'Urban 基础律动复习', meta: '中关村 · 5/20 已结束', status: '待双向互评', tone: 'success', action: '去互评' }
];

const onAction = (r: (typeof records)[number]) => {
  if (r.action === '去互评') router.push(`/practice/${r.id}/rate`);
  else showToast(r.action);
};
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的约练" :show-share="false" />

    <section class="pen-scroll">
      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'mine' }" type="button" @click="tab = 'mine'">我发起的</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'joined' }" type="button" @click="tab = 'joined'">我参加的</button>
      </div>

      <article v-for="r in records" :key="r.id" class="rec">
        <div class="rec__cover" aria-hidden="true"><Users :size="26" :stroke-width="2" /></div>
        <div class="rec__body">
          <strong class="rec__title">{{ r.title }}</strong>
          <p class="rec__meta">{{ r.meta }}</p>
          <div class="rec__foot">
            <span class="rec__status" :class="`rec__status--${r.tone}`">{{ r.status }}</span>
            <button class="rec__btn" type="button" @click="onAction(r)">{{ r.action }}</button>
          </div>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.seg {
  display: flex;
  gap: 8px;
  &__btn {
    flex: 1;
    height: 46px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
    &--on { background: $pen-ink; color: $pen-on-primary; }
  }
}

.rec {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__cover {
    flex: none; width: 88px; height: 88px; border-radius: 12px;
    background: $pen-soft; color: $pen-ink; display: grid; place-items: center;
  }
  &__body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
  &__title { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__foot { display: flex; align-items: center; justify-content: space-between; gap: 8px; }

  &__status {
    font-size: 13px; font-weight: 800; line-height: $pen-lh;
    &--ink { color: $pen-ink; }
    &--success { color: $pen-success; }
    &--mute { color: $pen-mute; }
  }

  &__btn {
    flex: none; height: 34px; padding: 6px 14px;
    border: 1px solid $pen-ink; border-radius: 999px;
    background: $pen-canvas; color: $pen-ink;
    font-size: 13px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}
</style>
