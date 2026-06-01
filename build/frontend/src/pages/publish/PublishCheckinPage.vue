<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { createCheckin } from '@/api/growth';

const router = useRouter();

const STYLES = ['Jazz', 'Hiphop', 'Locking', 'Urban'];
const TRAINING_TYPES = ['课程', '自练', '私教'];
const VIS_OPTIONS: Array<{ key: 'public' | 'friends' | 'private'; label: string }> = [
  { key: 'public', label: '公开到社区' },
  { key: 'friends', label: '仅好友' },
  { key: 'private', label: '仅自己' }
];

const style = ref('Jazz');
const durationMin = ref(90);
const location = ref('Urban Flow');
const trainingType = ref('课程');
const feeling = ref('今天开始能跟上副歌部分，转身还需要多练。');
const visibility = ref<'public' | 'private' | 'friends'>('public');
const submitting = ref(false);

const canSubmit = computed(() => durationMin.value > 0 && Boolean(style.value) && !submitting.value);

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast('请先补全训练信息');
    return;
  }
  submitting.value = true;
  try {
    await createCheckin({
      style: style.value,
      durationMin: Number(durationMin.value),
      location: location.value,
      feeling: feeling.value,
      visibility: visibility.value,
      idempotencyToken: `checkin-${Date.now()}`
    });
    showSuccessToast('打卡已保存');
    router.replace('/growth');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="checkin-page">
    <header class="topbar">
      <button class="icon-btn" aria-label="返回" @click="router.back()">‹</button>
      <div>
        <p class="eyebrow">TRAINING LOG</p>
        <h1>训练打卡</h1>
      </div>
    </header>

    <section class="hero">
      <p>记录今天的练习</p>
      <strong>KEEP MOVING</strong>
    </section>

    <section class="form-card">
      <div class="field">
        <span>舞种</span>
        <div class="chips">
          <button
            v-for="item in STYLES"
            :key="item"
            class="chip"
            :class="{ active: style === item }"
            @click="style = item"
          >
            {{ item }}
          </button>
        </div>
      </div>

      <div class="two-col">
        <label class="input-block">
          <span>时长</span>
          <input v-model.number="durationMin" type="number" min="5" />
          <em>分钟</em>
        </label>
        <label class="input-block">
          <span>地点</span>
          <input v-model="location" />
        </label>
      </div>

      <div class="field">
        <span>练习类型</span>
        <div class="chips">
          <button
            v-for="item in TRAINING_TYPES"
            :key="item"
            class="chip"
            :class="{ active: trainingType === item }"
            @click="trainingType = item"
          >
            {{ item }}
          </button>
        </div>
      </div>

      <div class="field">
        <span>可见范围</span>
        <div class="chips">
          <button
            v-for="item in VIS_OPTIONS"
            :key="item.key"
            class="chip"
            :class="{ active: visibility === item.key }"
            @click="visibility = item.key"
          >
            {{ item.label }}
          </button>
        </div>
      </div>

      <label class="note">
        <span>感受记录</span>
        <textarea v-model="feeling" rows="4" />
      </label>

      <button class="upload" type="button">
        <span>＋</span>
        上传图片 / 视频
      </button>
    </section>

    <footer class="sticky-action">
      <button class="primary-btn" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '保存中' : '保存打卡' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
.checkin-page {
  min-height: 100vh;
  padding: 18px 18px 96px;
  background: #fff;
  color: #111;
}

.topbar {
  display: grid;
  grid-template-columns: 44px 1fr;
  align-items: center;
  gap: 12px;
  h1 {
    margin: 0;
    font-size: 30px;
    line-height: 1;
    font-weight: 900;
  }
}

.eyebrow {
  margin: 0 0 4px;
  color: #707072;
  font-size: 11px;
  font-weight: 900;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: #f5f5f5;
  color: #111;
  font-size: 30px;
}

.hero {
  margin: 24px 0 18px;
  padding: 20px;
  border-radius: 30px;
  background: #111;
  color: #fff;
  p {
    margin: 0 0 10px;
    color: #cacacb;
    font-size: 14px;
    font-weight: 700;
  }
  strong {
    display: block;
    width: 210px;
    font-size: 48px;
    line-height: 0.9;
    font-weight: 900;
  }
}

.form-card {
  display: grid;
  gap: 16px;
}

.field,
.note,
.input-block {
  display: grid;
  gap: 10px;
  span {
    color: #707072;
    font-size: 12px;
    font-weight: 900;
  }
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  min-height: 40px;
  border: 0;
  border-radius: 999px;
  padding: 0 16px;
  background: #f5f5f5;
  color: #111;
  font-size: 14px;
  font-weight: 800;
  &.active {
    background: #111;
    color: #fff;
  }
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.input-block {
  position: relative;
  min-height: 94px;
  padding: 14px;
  border-radius: 24px;
  background: #f5f5f5;
  input {
    width: 100%;
    min-width: 0;
    border: 0;
    outline: none;
    background: transparent;
    color: #111;
    font-size: 22px;
    font-weight: 900;
  }
  em {
    position: absolute;
    right: 14px;
    bottom: 15px;
    color: #707072;
    font-size: 12px;
    font-style: normal;
    font-weight: 800;
  }
}

.note {
  textarea {
    width: 100%;
    border: 0;
    border-radius: 24px;
    outline: none;
    resize: none;
    padding: 16px;
    background: #f5f5f5;
    color: #111;
    font-family: inherit;
    font-size: 15px;
    line-height: 1.55;
  }
}

.upload {
  width: 100%;
  height: 100px;
  border: 1px dashed #cacacb;
  border-radius: 28px;
  background: #fff;
  color: #111;
  font-size: 15px;
  font-weight: 900;
  span {
    display: block;
    margin-bottom: 8px;
    font-size: 28px;
    line-height: 1;
  }
}

.sticky-action {
  position: fixed;
  left: 50%;
  bottom: var(--app-tabbar-offset, 0px);
  width: 100%;
  max-width: 480px;
  transform: translateX(-50%);
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.94);
  border-top: 1px solid #e5e5e5;
}

.primary-btn {
  width: 100%;
  height: 56px;
  border: 0;
  border-radius: 999px;
  background: #111;
  color: #fff;
  font-size: 17px;
  font-weight: 900;
  &:disabled {
    opacity: 0.55;
  }
}
</style>
