<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';
import { useAppStore } from '@/stores/app';
import { createPractice } from '@/api/practice';

const router = useRouter();
const appStore = useAppStore();
const submitting = ref(false);

const fields: Array<[string, string]> = [
  ['舞种', 'Hiphop'],
  ['水平要求', '中级'],
  ['时间', '周六 15:00'],
  ['地点', 'Urban Flow 舞室'],
  ['期望人数', '4 人'],
  ['接受新手', '否'],
  ['可见范围', '同城推荐']
];

const onSubmit = async () => {
  if (submitting.value) return;
  submitting.value = true;
  try {
    await createPractice({
      title: 'Hiphop 周末练习',
      style: 'Hiphop',
      level: '中级',
      date: '2026-05-30',
      time: '15:00-17:00',
      city: appStore.city,
      area: '朝阳区',
      location: 'Urban Flow 舞室',
      capacity: 4,
      remark: '复习上周课堂组合，希望守时，可一起拍视频。',
      idempotencyToken: `practice-${Date.now()}`
    });
    showSuccessToast('已发布并推送匹配用户');
    router.replace('/practice');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="发布约练" @share="showToast('约练草稿已生成')" />

    <section class="pen-body pen-body--form">
      <h2 class="pen-h2">创建一次可坚持的练习</h2>

      <PenFieldRow
        v-for="[label, value] in fields"
        :key="label"
        :label="label"
        :value="value"
        @click="showToast(label)"
      />

      <section class="note-block">
        <h3>备注说明</h3>
        <p>复习上周课堂组合，希望守时，可一起拍视频。</p>
      </section>

      <button type="button" class="pen-primary-btn" :disabled="submitting" @click="onSubmit">
        {{ submitting ? '发布中…' : '发布并推送匹配用户' }}
      </button>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-body--form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px 24px;
}

.pen-h2 {
  @include pen-h2;
  margin-bottom: 6px;
}

.note-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 120px;
  padding: 16px;
  background: $pen-soft;
  box-sizing: border-box;

  h3,
  p {
    margin: 0;
    letter-spacing: 0;
  }

  h3 {
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  p {
    color: $pen-mute;
    font-size: 14px;
    font-weight: 500;
    line-height: $pen-lh;
  }
}

.pen-primary-btn {
  @include pen-primary-btn;
  width: 100%;
  margin-top: 6px;
}
</style>
