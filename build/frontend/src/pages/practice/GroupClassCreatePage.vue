<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft } from 'lucide-vue-next';
import { createGroupClassIntent } from '@/api/practice';

const router = useRouter();
const studioId = ref(1);
const danceStyleId = ref(1);
const preferredTimeNote = ref('周末下午 14:00-16:00，可接受 1 小时浮动');
const targetPeopleCount = ref(3);
const submitting = ref(false);

const studios = [
  { id: 1, name: 'Urban Flow' },
  { id: 2, name: 'DanceLab 五道口' },
  { id: 3, name: 'Joy Studio 朝阳' }
];

const styles = [
  { id: 1, name: 'Hiphop' },
  { id: 2, name: 'Jazz' },
  { id: 3, name: 'Breaking' },
  { id: 4, name: 'Locking' },
  { id: 5, name: 'Popping' },
  { id: 6, name: 'K-pop' }
];

const canSubmit = computed(() =>
  studioId.value > 0 &&
  danceStyleId.value > 0 &&
  targetPeopleCount.value >= 2 &&
  targetPeopleCount.value <= 30 &&
  preferredTimeNote.value.trim().length > 0
);

const submit = async () => {
  if (!canSubmit.value || submitting.value) {
    showFailToast('请先补全拼课信息');
    return;
  }
  submitting.value = true;
  try {
    await createGroupClassIntent({
      studioId: studioId.value,
      danceStyleId: danceStyleId.value,
      preferredTimeNote: preferredTimeNote.value.trim(),
      targetPeopleCount: targetPeopleCount.value
    });
    showSuccessToast('拼课已发起');
    router.replace('/practice/group-class');
  } catch {
    showFailToast('发起失败，请确认舞室和舞种数据已存在');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="create-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" />
      </button>
      <div>
        <p>GROUP CLASS</p>
        <h1>发起拼课</h1>
      </div>
    </header>

    <section class="hero">
      <span>人数达标后，系统会通知舞室管理员联系开课。</span>
    </section>

    <section class="form">
      <section class="block">
        <span class="label">选择舞室</span>
        <div class="chips">
          <button v-for="studio in studios" :key="studio.id" class="chip" :class="{ active: studioId === studio.id }" type="button" @click="studioId = studio.id">
            {{ studio.name }}
          </button>
        </div>
        <label class="manual">
          <span>没有在上面？输入舞室 ID</span>
          <input v-model.number="studioId" type="number" min="1" inputmode="numeric" />
        </label>
      </section>

      <section class="block">
        <span class="label">舞种</span>
        <div class="chips">
          <button v-for="style in styles" :key="style.id" class="chip" :class="{ active: danceStyleId === style.id }" type="button" @click="danceStyleId = style.id">
            {{ style.name }}
          </button>
        </div>
      </section>

      <label class="field">
        <span>期望人数</span>
        <input v-model.number="targetPeopleCount" type="number" min="2" max="30" inputmode="numeric" />
      </label>

      <label class="field">
        <span>偏好时间和说明</span>
        <textarea v-model="preferredTimeNote" rows="4" maxlength="100" />
      </label>
    </section>

    <footer class="save-bar">
      <button class="primary-btn" type="button" :disabled="!canSubmit || submitting" @click="submit">
        {{ submitting ? '发布中...' : '发布拼课意向' }}
      </button>
    </footer>
  </main>
</template>

<style scoped lang="scss">
.create-page { min-height: 100vh; max-width: 430px; margin: 0 auto; padding-bottom: 88px; background: #fff; color: #111; }
.topbar { height: 70px; padding: 12px 18px; border-bottom: 1px solid #e5e5e5; display: flex; align-items: center; gap: 12px; box-sizing: border-box; }
.topbar div { flex: 1; min-width: 0; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 22px; line-height: 1.1; font-weight: 950; }
.icon-btn { width: 38px; height: 38px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: grid; place-items: center; }
.hero { margin: 16px 18px 0; padding: 18px; border-radius: 8px; background: #111; color: #fff; }
.hero span { font-size: 20px; line-height: 1.1; font-weight: 950; }
.form { padding: 16px 18px; display: flex; flex-direction: column; gap: 12px; }
.block, .field { padding: 14px; border-radius: 8px; background: #f5f5f5; }
.field, .manual { display: flex; flex-direction: column; gap: 8px; }
.label, .field span, .manual span { color: #707072; font-size: 12px; font-weight: 900; }
.chips { margin-top: 10px; display: flex; flex-wrap: wrap; gap: 8px; }
.chip { height: 36px; padding: 0 14px; border: 0; border-radius: 999px; background: #fff; color: #111; font-size: 13px; font-weight: 900; }
.chip.active { background: #111; color: #fff; }
.manual { margin-top: 12px; }
input, textarea { width: 100%; border: 0; background: transparent; color: #111; font: inherit; font-size: 20px; font-weight: 950; outline: none; box-sizing: border-box; }
textarea { resize: none; font-size: 16px; line-height: 1.45; font-weight: 800; }
.save-bar { position: fixed; left: 50%; bottom: 0; width: 100%; max-width: 430px; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); border-top: 1px solid #e5e5e5; background: #fff; box-sizing: border-box; transform: translateX(-50%); }
.primary-btn { width: 100%; height: 48px; border: 0; border-radius: 999px; background: #111; color: #fff; font-size: 15px; font-weight: 950; }
.primary-btn:disabled { opacity: .42; }
</style>
