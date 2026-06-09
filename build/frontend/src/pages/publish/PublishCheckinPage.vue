<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft, Image, Save } from 'lucide-vue-next';
import { createCheckin } from '@/api/growth';

const router = useRouter();

const STYLES = [
  { id: 1, name: 'Hiphop' },
  { id: 2, name: 'Jazz' },
  { id: 4, name: 'Locking' },
  { id: 5, name: 'Popping' }
];
const TRAINING_TYPES = ['课程', '自练', '私教'];
const VIS_OPTIONS: Array<{ key: 'public' | 'friends' | 'private'; label: string }> = [
  { key: 'public', label: '公开' },
  { key: 'friends', label: '仅好友' },
  { key: 'private', label: '仅自己' }
];

const danceStyleId = ref(1);
const style = ref('Hiphop');
const durationMin = ref(90);
const location = ref('Urban Flow');
const trainingType = ref('自练');
const feeling = ref('今天完成了一轮基础练习，节奏和重心更稳定。');
const visibility = ref<'public' | 'private' | 'friends'>('public');
const submitting = ref(false);

const canSubmit = computed(() => durationMin.value > 0 && Boolean(style.value) && !submitting.value);

const pickStyle = (item: { id: number; name: string }) => {
  danceStyleId.value = item.id;
  style.value = item.name;
};

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast('请先补全训练信息');
    return;
  }
  submitting.value = true;
  try {
    await createCheckin({
      danceStyleId: danceStyleId.value,
      style: style.value,
      durationMin: Number(durationMin.value),
      location: location.value,
      feeling: `${trainingType.value} · ${feeling.value}`,
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
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" />
      </button>
      <div>
        <p>TRAINING LOG</p>
        <h1>训练打卡</h1>
      </div>
    </header>

    <section class="hero">
      <p>记录今天的练习</p>
      <strong>KEEP MOVING</strong>
    </section>

    <section class="form">
      <div class="field">
        <span>舞种</span>
        <div class="chips">
          <button v-for="item in STYLES" :key="item.id" class="chip" :class="{ active: danceStyleId === item.id }" type="button" @click="pickStyle(item)">
            {{ item.name }}
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
          <button v-for="item in TRAINING_TYPES" :key="item" class="chip" :class="{ active: trainingType === item }" type="button" @click="trainingType = item">
            {{ item }}
          </button>
        </div>
      </div>

      <div class="field">
        <span>可见范围</span>
        <div class="chips">
          <button v-for="item in VIS_OPTIONS" :key="item.key" class="chip" :class="{ active: visibility === item.key }" type="button" @click="visibility = item.key">
            {{ item.label }}
          </button>
        </div>
      </div>

      <label class="note">
        <span>感受记录</span>
        <textarea v-model="feeling" rows="4" />
      </label>

      <button class="media-link" type="button" @click="router.push('/me/works/upload')">
        <Image :size="18" />
        有练习图片/视频？去上传阶段作品
      </button>
    </section>

    <footer class="sticky-action">
      <button class="primary-btn" type="button" :disabled="!canSubmit" @click="onSubmit">
        <Save :size="18" />
        {{ submitting ? '保存中...' : '保存打卡' }}
      </button>
    </footer>
  </main>
</template>

<style scoped lang="scss">
.checkin-page { min-height: 100vh; max-width: 430px; margin: 0 auto; padding-bottom: calc(88px + env(safe-area-inset-bottom)); background: #fff; color: #111; }
.topbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; border-bottom: 1px solid #e5e5e5; }
.topbar div { flex: 1; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 22px; font-weight: 950; }
.icon-btn { width: 38px; height: 38px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: grid; place-items: center; }
.hero { margin: 16px 18px; min-height: 150px; padding: 18px; border-radius: 8px; background: #111; color: #fff; display: flex; flex-direction: column; justify-content: flex-end; }
.hero p { margin: 0 0 4px; color: #b8b8bb; font-size: 13px; font-weight: 800; }
.hero strong { font-size: 34px; line-height: 1; font-weight: 950; }
.form { display: flex; flex-direction: column; gap: 12px; padding: 0 18px; }
.field, .input-block, .note { display: flex; flex-direction: column; gap: 8px; padding: 14px; border-radius: 8px; background: #f5f5f5; }
.field > span, .input-block span, .note span { color: #707072; font-size: 12px; font-weight: 900; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { height: 36px; padding: 0 14px; border: 0; border-radius: 999px; background: #fff; color: #111; font-weight: 900; }
.chip.active { background: #111; color: #fff; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
input, textarea { width: 100%; border: 0; background: transparent; color: #111; font: inherit; font-size: 20px; font-weight: 950; outline: none; box-sizing: border-box; }
.input-block em { color: #707072; font-size: 12px; font-style: normal; font-weight: 800; }
textarea { resize: none; font-size: 15px; line-height: 1.45; font-weight: 800; }
.media-link { height: 44px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: inline-flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px; font-weight: 900; }
.sticky-action { position: fixed; left: 50%; bottom: 0; width: 100%; max-width: 430px; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); background: #fff; border-top: 1px solid #e5e5e5; box-sizing: border-box; transform: translateX(-50%); }
.primary-btn { width: 100%; height: 48px; border: 0; border-radius: 999px; background: #111; color: #fff; display: inline-flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; font-weight: 950; }
.primary-btn:disabled { opacity: .42; }
</style>
