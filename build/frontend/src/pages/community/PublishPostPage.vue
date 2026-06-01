<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { ChevronLeft, Image, Plus } from 'lucide-vue-next';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';

const router = useRouter();
const content = ref('');

const rows = [
  { label: '# 添加话题', trailing: undefined },
  { label: '关联舞室 / 课程 / 老师', trailing: '推荐' },
  { label: '所在位置', trailing: '五道口' },
  { label: '谁可以看', trailing: '公开' }
];

const onPublish = () => {
  showSuccessToast('已发布');
  router.back();
};
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">发动态</h1>
      <button class="topbar__pub" type="button" @click="onPublish">发布</button>
    </header>

    <section class="pen-scroll">
      <textarea
        v-model="content"
        class="editor"
        rows="4"
        placeholder="分享试听感受 / 课堂记录 / 约练日常…"
      />

      <div class="media">
        <div class="media__cell media__cell--filled" aria-hidden="true" />
        <div class="media__cell media__cell--filled" aria-hidden="true"><Image :size="24" :stroke-width="2" /></div>
        <button class="media__cell media__cell--add" type="button" aria-label="添加图片">
          <Plus :size="26" :stroke-width="2" />
        </button>
      </div>

      <div class="rows">
        <PenSettingRow
          v-for="r in rows"
          :key="r.label"
          :label="r.label"
          :trailing="r.trailing"
        />
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

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.editor {
  width: 100%;
  min-height: 110px;
  border: 0;
  background: transparent;
  color: $pen-ink;
  font-family: $pen-font;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  resize: none;
  outline: none;
  &::placeholder { color: $pen-mute; }
}

.media {
  display: flex;
  gap: 8px;

  &__cell {
    width: 88px;
    height: 88px;
    border-radius: 12px;
    display: grid;
    place-items: center;

    &--filled { background: $pen-ink; color: $pen-on-primary; }
    &--add {
      background: $pen-soft;
      color: $pen-mute;
      border: 1px solid $pen-hairline;
      cursor: pointer;
    }
  }
}

.rows { display: flex; flex-direction: column; }
</style>
