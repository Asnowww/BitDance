<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { createCheckin } from '@/api/growth';

const router = useRouter();

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];
const VIS_OPTIONS: Array<{ key: 'public' | 'private' | 'friends'; label: string }> = [
  { key: 'public', label: '公开' },
  { key: 'friends', label: '仅搭子' },
  { key: 'private', label: '仅自己' }
];

const style = ref(STYLES[0]);
const durationMin = ref(60);
const location = ref('');
const feeling = ref('');
const visibility = ref<'public' | 'private' | 'friends'>('public');
const submitting = ref(false);

const canSubmit = computed(() => durationMin.value > 0 && !submitting.value);

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast('请填写时长');
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
    showSuccessToast('打卡成功');
    router.replace('/growth');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">训练打卡</span>
    </header>
    <section class="form">
      <div class="group">
        <div class="group__title">舞种</div>
        <div class="chips">
          <span
            v-for="s in STYLES"
            :key="s"
            class="chip"
            :class="{ active: style === s }"
            @click="style = s"
            >{{ s }}</span
          >
        </div>
      </div>
      <div class="row">
        <span class="row__label">时长（分钟）</span>
        <input v-model.number="durationMin" type="number" min="5" max="600" class="input" />
      </div>
      <div class="row">
        <span class="row__label">地点</span>
        <input v-model="location" class="input" placeholder="舞室、家、街头…" />
      </div>
      <div class="row row--top">
        <span class="row__label">感受</span>
        <textarea v-model="feeling" class="input input--textarea" rows="3" placeholder="今天的状态、收获、不足" />
      </div>
      <div class="group">
        <div class="group__title">可见性</div>
        <div class="chips">
          <span
            v-for="v in VIS_OPTIONS"
            :key="v.key"
            class="chip"
            :class="{ active: visibility === v.key }"
            @click="visibility = v.key"
            >{{ v.label }}</span
          >
        </div>
      </div>
    </section>
    <footer class="footer">
      <button class="btn" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '保存中…' : '完成打卡' }}
      </button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
}
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
.form {
  background: #fff;
  padding: 8px 16px 16px;
}
.group {
  padding: 10px 0;
  &__title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 6px 14px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  &__label {
    width: 84px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
  &--top {
    align-items: flex-start;
    .row__label {
      padding-top: 8px;
    }
  }
}
.input {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fafafa;
  font-size: 14px;
  outline: none;
  &--textarea {
    height: auto;
    padding: 8px 12px;
    resize: none;
    font-family: inherit;
  }
  &:focus {
    background: #fff;
    border-color: var(--bd-primary);
  }
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
}
.btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  background: var(--bd-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
</style>
