<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const route = useRoute();
const router = useRouter();
const studioId = String(route.params.id || 'urban-flow');

const days = [
  { w: '一', d: '27' }, { w: '二', d: '28' }, { w: '三', d: '29' }, { w: '四', d: '30' },
  { w: '五', d: '31' }, { w: '六', d: '1' }, { w: '日', d: '2' }
];
const activeDay = ref('30');

const slots = ['10:00', '14:00', '16:00', '19:30'];
const activeSlot = ref('14:00');

const fields = [
  { label: '姓名', value: '请输入称呼' },
  { label: '手机号', value: '138••••6789' },
  { label: '舞蹈基础', value: '零基础' }
];

const onConfirm = () => {
  showSuccessToast('已提交，等待舞室确认');
  router.push('/me/trials');
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="试听预约" @share="showSuccessToast('链接已复制')" />

    <section class="pen-scroll">
      <div class="studio">
        <div class="studio__cover" aria-hidden="true"><Music :size="26" :stroke-width="2" /></div>
        <div class="studio__copy">
          <strong class="studio__name">Urban Flow 舞室</strong>
          <span class="studio__meta">五道口地铁站 320m · 韩舞强</span>
          <strong class="studio__price">¥79 体验课</strong>
        </div>
      </div>

      <h2 class="block-title">选择日期</h2>
      <div class="week">
        <button
          v-for="d in days"
          :key="d.d"
          class="day"
          :class="{ 'day--on': activeDay === d.d }"
          type="button"
          @click="activeDay = d.d"
        >
          <span class="day__w">{{ d.w }}</span>
          <span class="day__d">{{ d.d }}</span>
        </button>
      </div>

      <h2 class="block-title">选择时段</h2>
      <div class="chip-row">
        <button
          v-for="s in slots"
          :key="s"
          class="chip"
          :class="activeSlot === s ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeSlot = s"
        >
          {{ s }}
        </button>
      </div>

      <h2 class="block-title">报名信息</h2>
      <div class="rows">
        <PenFieldRow v-for="f in fields" :key="f.label" :label="f.label" :value="f.value" />
      </div>

      <div class="remark">备注：想了解的内容、目标舞种…</div>
    </section>

    <PenActionBar
      soft-label="收藏"
      dark-label="确认预约"
      @soft="showSuccessToast('已收藏')"
      @dark="onConfirm"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.studio {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: $pen-soft;

  &__cover {
    flex: none; width: 56px; height: 56px; border-radius: 12px;
    background: $pen-ink; color: $pen-on-primary; display: grid; place-items: center;
  }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__price { font-size: 14px; font-weight: 800; line-height: $pen-lh; }
}

.block-title { @include pen-h3-section; }

.week { display: flex; gap: 8px; }

.day {
  flex: 1;
  height: 60px;
  border: 0;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;

  &__w { font-size: 12px; font-weight: 700; line-height: $pen-lh; color: $pen-mute; }
  &__d { font-size: 15px; font-weight: 900; line-height: $pen-lh; }

  &--on {
    background: $pen-ink;
    .day__w, .day__d { color: $pen-on-primary; }
  }
}

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.rows { display: flex; flex-direction: column; }

.remark {
  min-height: 80px;
  padding: 14px;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-mute;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}
</style>
