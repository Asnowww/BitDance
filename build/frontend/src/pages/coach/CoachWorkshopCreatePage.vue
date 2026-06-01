<script setup lang="ts">
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { ChevronLeft, ImagePlus, Plus, CircleCheckBig } from 'lucide-vue-next';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const router = useRouter();

const fields = [
  { label: '标题', value: '请输入' },
  { label: '舞种', value: 'Locking' },
  { label: '难度', value: '中级' },
  { label: '价格', value: '¥199' },
  { label: '人数上限', value: '20 人' },
  { label: '报名截止', value: '5/29 23:59' }
];

const sessions = ['5/30 14:00', '5/31 19:30'];

const onPublish = () => {
  showSuccessToast('Workshop 已发布');
  router.back();
};
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">创建 Workshop</h1>
      <button class="topbar__pub" type="button" @click="onPublish">发布</button>
    </header>

    <section class="pen-scroll">
      <button class="cover" type="button">
        <ImagePlus :size="30" :stroke-width="2" />
        <span>上传活动封面</span>
      </button>

      <div class="rows">
        <PenFieldRow v-for="f in fields" :key="f.label" :label="f.label" :value="f.value" />
      </div>

      <h2 class="block-title">场次</h2>
      <div class="sessions">
        <span v-for="s in sessions" :key="s" class="session">{{ s }}</span>
        <button class="session-add" type="button" aria-label="添加场次"><Plus :size="18" :stroke-width="2" /></button>
      </div>

      <div class="role">
        <CircleCheckBig :size="16" :stroke-width="2" />
        <span>自由教练 · 可独立发布</span>
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

  &__title { flex: 1; margin: 0; font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__icon {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }
  &__pub {
    flex: none; height: 36px; padding: 8px 16px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 14px; font-weight: 800; line-height: $pen-lh; cursor: pointer;
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.cover {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 140px;
  border: 0;
  border-radius: 14px;
  background: $pen-ink;
  color: $pen-on-primary;
  cursor: pointer;
  span { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.rows { display: flex; flex-direction: column; }
.block-title { @include pen-h3-section; }

.sessions { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }

.session {
  height: 40px;
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.session-add {
  width: 40px; height: 40px;
  border: 1px solid $pen-hairline; border-radius: 999px;
  background: $pen-canvas; color: $pen-ink;
  display: grid; place-items: center; cursor: pointer;
}

.role {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 6px 14px;
  border: 1px solid $pen-success;
  border-radius: 999px;
  color: $pen-success;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
