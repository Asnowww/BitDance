<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft, Send } from 'lucide-vue-next';
import { createPractice } from '@/api/practice';
import { useAppStore } from '@/stores/app';

const router = useRouter();
const appStore = useAppStore();
const submitting = ref(false);

const styles = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];
const levels = [
  { key: 'beginner', label: '入门' },
  { key: 'intermediate', label: '中级' },
  { key: 'advanced', label: '进阶' }
];

const style = ref('Hiphop');
const level = ref('intermediate');
const date = ref(new Date(Date.now() + 86400000 * 2).toISOString().slice(0, 10));
const time = ref('15:00-17:00');
const city = ref(appStore.city || '北京');
const area = ref('朝阳区');
const location = ref('Urban Flow 舞室');
const capacity = ref(4);
const remark = ref('复习上周课堂组合，希望守时，可以一起拍视频复盘。');

const canSubmit = computed(() =>
  style.value &&
  level.value &&
  date.value &&
  time.value &&
  city.value.trim() &&
  location.value.trim() &&
  capacity.value >= 2 &&
  !submitting.value
);

const submit = async () => {
  if (!canSubmit.value) {
    showFailToast('请先补全约练信息');
    return;
  }
  submitting.value = true;
  try {
    await createPractice({
      title: `${style.value} ${levels.find((item) => item.key === level.value)?.label || ''}约练`,
      style: style.value,
      level: level.value,
      date: date.value,
      time: time.value,
      city: city.value.trim(),
      area: area.value.trim(),
      location: location.value.trim(),
      capacity: Number(capacity.value),
      remark: remark.value.trim(),
      idempotencyToken: `practice-${Date.now()}`
    });
    showSuccessToast('约练已发布');
    router.replace('/practice');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="publish-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" />
      </button>
      <div>
        <p>PRACTICE POST</p>
        <h1>发布约练</h1>
      </div>
    </header>

    <section class="hero">
      <h2>创建一次真的能约成的练习。</h2>
      <p>信息越清楚，搭子越容易判断是否适合加入。</p>
    </section>

    <section class="form">
      <div class="field">
        <span>舞种</span>
        <div class="chips">
          <button v-for="item in styles" :key="item" class="chip" :class="{ active: style === item }" type="button" @click="style = item">
            {{ item }}
          </button>
        </div>
      </div>

      <div class="field">
        <span>水平要求</span>
        <div class="chips">
          <button v-for="item in levels" :key="item.key" class="chip" :class="{ active: level === item.key }" type="button" @click="level = item.key">
            {{ item.label }}
          </button>
        </div>
      </div>

      <div class="two-col">
        <label class="input-block">
          <span>日期</span>
          <input v-model="date" type="date" />
        </label>
        <label class="input-block">
          <span>时间</span>
          <input v-model="time" placeholder="15:00-17:00" />
        </label>
      </div>

      <div class="two-col">
        <label class="input-block">
          <span>城市</span>
          <input v-model="city" />
        </label>
        <label class="input-block">
          <span>区域</span>
          <input v-model="area" />
        </label>
      </div>

      <label class="input-block">
        <span>地点</span>
        <input v-model="location" />
      </label>

      <label class="input-block">
        <span>人数上限</span>
        <input v-model.number="capacity" type="number" min="2" max="20" />
      </label>

      <label class="input-block">
        <span>备注说明</span>
        <textarea v-model="remark" rows="4" maxlength="160" />
      </label>
    </section>

    <footer class="save-bar">
      <button type="button" :disabled="!canSubmit" @click="submit">
        <Send :size="18" />
        {{ submitting ? '发布中...' : '发布并推送匹配用户' }}
      </button>
    </footer>
  </main>
</template>

<style scoped lang="scss">
.publish-page { min-height: 100vh; max-width: 430px; margin: 0 auto; padding-bottom: calc(88px + env(safe-area-inset-bottom)); background: #fff; color: #111; }
.topbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; border-bottom: 1px solid #e5e5e5; }
.topbar div { flex: 1; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 22px; font-weight: 950; }
.icon-btn { width: 38px; height: 38px; border: 0; border-radius: 999px; background: #f5f5f5; display: grid; place-items: center; }
.hero { margin: 16px 18px; padding: 18px; border-radius: 8px; background: #111; color: #fff; }
.hero h2 { margin: 0; font-size: 29px; line-height: 1.05; font-weight: 950; }
.hero p { margin: 8px 0 0; color: #e5e5e5; font-size: 13px; font-weight: 800; line-height: 1.45; }
.form { display: flex; flex-direction: column; gap: 12px; padding: 0 18px; }
.field, .input-block { display: flex; flex-direction: column; gap: 8px; padding: 14px; border-radius: 8px; background: #f5f5f5; }
.field > span, .input-block span { color: #707072; font-size: 12px; font-weight: 900; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { height: 36px; padding: 0 14px; border: 0; border-radius: 999px; background: #fff; color: #111; font-weight: 900; }
.chip.active { background: #111; color: #fff; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
input, textarea { width: 100%; border: 0; background: transparent; color: #111; font: inherit; font-size: 18px; font-weight: 900; outline: none; box-sizing: border-box; }
textarea { resize: none; font-size: 15px; line-height: 1.45; font-weight: 800; }
.save-bar { position: fixed; left: 50%; bottom: 0; width: 100%; max-width: 430px; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); border-top: 1px solid #e5e5e5; background: #fff; box-sizing: border-box; transform: translateX(-50%); }
.save-bar button { width: 100%; height: 48px; border: 0; border-radius: 999px; background: #111; color: #fff; display: inline-flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; font-weight: 950; }
.save-bar button:disabled { opacity: .42; }
</style>
