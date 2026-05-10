<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { useAppStore } from '@/stores/app';
import { createPractice } from '@/api/practice';

const router = useRouter();
const appStore = useAppStore();

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const LEVELS = ['零基础', '入门', '初级', '进阶', '高阶'];
const AREAS = ['海淀区', '朝阳区', '东城区', '西城区', '丰台区', '通州区'];

const today = new Date();
const dateOptions = Array.from({ length: 14 }).map((_, i) => {
  const d = new Date(today);
  d.setDate(today.getDate() + i);
  return d.toISOString().slice(0, 10);
});
const TIME_OPTIONS = ['10:00-12:00', '14:00-16:00', '17:00-19:00', '19:00-21:00', '20:00-22:00'];

const title = ref('');
const style = ref(STYLES[0]);
const level = ref(LEVELS[1]);
const date = ref(dateOptions[0]);
const time = ref(TIME_OPTIONS[3]);
const area = ref(AREAS[0]);
const location = ref('');
const capacity = ref(4);
const remark = ref('');
const submitting = ref(false);

const canSubmit = computed(() => location.value.trim().length > 0 && !submitting.value);

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast('请填写集合地点');
    return;
  }
  submitting.value = true;
  try {
    await createPractice({
      title: title.value || `${style.value} 找搭子`,
      style: style.value,
      level: level.value,
      date: date.value,
      time: time.value,
      city: appStore.city,
      area: area.value,
      location: location.value,
      capacity: capacity.value,
      remark: remark.value || undefined,
      idempotencyToken: `practice-${Date.now()}`
    });
    showSuccessToast('已发布约练');
    router.replace('/practice');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">发起约练</span>
    </header>
    <section class="form">
      <div class="row">
        <span class="row__label">标题</span>
        <input v-model="title" class="input" placeholder="自动按舞种生成，可自定义" />
      </div>
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
      <div class="group">
        <div class="group__title">水平要求</div>
        <div class="chips">
          <span
            v-for="l in LEVELS"
            :key="l"
            class="chip"
            :class="{ active: level === l }"
            @click="level = l"
            >{{ l }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">日期</div>
        <div class="chips chips--scroll">
          <span
            v-for="d in dateOptions"
            :key="d"
            class="chip"
            :class="{ active: date === d }"
            @click="date = d"
            >{{ d.slice(5) }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">时段</div>
        <div class="chips">
          <span
            v-for="t in TIME_OPTIONS"
            :key="t"
            class="chip"
            :class="{ active: time === t }"
            @click="time = t"
            >{{ t }}</span
          >
        </div>
      </div>
      <div class="row">
        <span class="row__label">区域</span>
        <select v-model="area" class="input">
          <option v-for="a in AREAS" :key="a" :value="a">{{ a }}</option>
        </select>
      </div>
      <div class="row">
        <span class="row__label">集合地</span>
        <input v-model="location" class="input" placeholder="例：海淀区舞星 Studio 3" />
      </div>
      <div class="row">
        <span class="row__label">人数</span>
        <input v-model.number="capacity" class="input" type="number" min="2" max="20" />
      </div>
      <div class="row row--top">
        <span class="row__label">备注</span>
        <textarea v-model="remark" class="input input--textarea" rows="3" placeholder="水平、装备、暗号…" />
      </div>
    </section>
    <footer class="footer">
      <button class="btn" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '发布中…' : '发布约练' }}
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
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  &__label {
    width: 56px;
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
  &--scroll {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 4px;
  }
}
.chip {
  flex-shrink: 0;
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
