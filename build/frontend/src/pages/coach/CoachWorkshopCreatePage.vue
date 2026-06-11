<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { storeToRefs } from 'pinia';
import { useOpsStore } from '@/stores/ops';
import {
  createMerchantWorkshop,
  addWorkshopSession,
  publishMerchantWorkshop,
  uploadMediaAsset,
  mediaContentUrl,
  type CreateWorkshopBody
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();
const { activeRole, studioId, coachMe } = storeToRefs(ops);

/** 自由教练(无舞室绑定)可不绑定舞室独立发布 */
const isFreelance = computed(
  () => activeRole.value === 'coach' && (coachMe.value?.activeStudioIds?.length ?? 0) === 0
);

const form = ref({
  workshopName: '',
  cityId: '' as string | number,
  danceStyleId: '' as string | number,
  locationName: '',
  address: '',
  priceAmount: '' as string | number,
  minPeople: '' as string | number,
  maxPeople: '' as string | number,
  signupDeadline: '',
  intro: '',
  coverAssetId: null as number | null
});

interface SessionDraft {
  sessionName: string;
  date: string;
  startTime: string;
  endTime: string;
  capacity: string | number;
  priceAmount: string | number;
}

const sessions = ref<SessionDraft[]>([
  { sessionName: '', date: '', startTime: '', endTime: '', capacity: '', priceAmount: '' }
]);

const addSessionDraft = () =>
  sessions.value.push({
    sessionName: '',
    date: '',
    startTime: '',
    endTime: '',
    capacity: '',
    priceAmount: ''
  });

const removeSessionDraft = (i: number) => sessions.value.splice(i, 1);

const uploading = ref(false);
const submitting = ref(false);

const onPickCover = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  (e.target as HTMLInputElement).value = '';
  if (!file) return;
  if (!file.type.startsWith('image/')) {
    showFailToast('封面只支持图片');
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    showFailToast('图片不能超过 5MB');
    return;
  }
  uploading.value = true;
  try {
    const asset = await uploadMediaAsset(file, 'workshop_cover');
    form.value.coverAssetId = asset.assetId;
  } finally {
    uploading.value = false;
  }
};

const validSessions = computed(() =>
  sessions.value.filter((s) => s.date && s.startTime && s.endTime && s.capacity !== '')
);

const ready = computed(
  () =>
    form.value.workshopName.trim() &&
    form.value.cityId !== '' &&
    form.value.locationName.trim() &&
    form.value.address.trim() &&
    form.value.priceAmount !== '' &&
    validSessions.value.length > 0
);

const flowHint = computed(() => {
  if (activeRole.value === 'studio_admin') return '舞室管理员创建后将直接发布';
  if (isFreelance.value) return '自由教练资质审核通过后可独立发布,无需绑定舞室';
  return '全职/签约教练提交后,需舞室管理员审批通过才会发布';
});

const toIso = (date: string, time: string) => new Date(`${date}T${time}:00`).toISOString();

const submit = async () => {
  if (!ready.value || submitting.value) return;
  if (!isFreelance.value && !studioId.value && activeRole.value === 'studio_admin') {
    showFailToast('请先完成舞室入驻');
    return;
  }
  submitting.value = true;
  const f = form.value;
  try {
    const body: CreateWorkshopBody = {
      studioId: isFreelance.value ? undefined : (studioId.value ?? undefined),
      cityId: Number(f.cityId),
      danceStyleId: f.danceStyleId === '' ? undefined : Number(f.danceStyleId),
      workshopName: f.workshopName.trim(),
      coverAssetId: f.coverAssetId ?? undefined,
      intro: f.intro.trim() || undefined,
      address: f.address.trim(),
      locationName: f.locationName.trim(),
      priceAmount: Number(f.priceAmount),
      minPeople: f.minPeople === '' ? undefined : Number(f.minPeople),
      maxPeople: f.maxPeople === '' ? undefined : Number(f.maxPeople),
      signupDeadline: f.signupDeadline ? new Date(f.signupDeadline).toISOString() : undefined,
      sourceType: activeRole.value === 'studio_admin' ? 'studio' : 'coach'
    };
    const ws = await createMerchantWorkshop(body);

    for (const s of validSessions.value) {
      await addWorkshopSession({
        workshopId: ws.id,
        sessionName: s.sessionName.trim() || undefined,
        startAt: toIso(s.date, s.startTime),
        endAt: toIso(s.date, s.endTime),
        capacity: Number(s.capacity),
        priceAmount: s.priceAmount === '' ? undefined : Number(s.priceAmount)
      });
    }

    if (activeRole.value === 'studio_admin' || isFreelance.value) {
      try {
        await publishMerchantWorkshop(ws.id);
        showSuccessToast('已创建并发布');
      } catch {
        showSuccessToast('已创建,发布需进一步审核');
      }
    } else {
      showSuccessToast('已提交,等待舞室管理员审批');
    }
    router.replace('/coach/workshops');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => ops.refresh());
</script>

<template>
  <main class="create-page">
    <PenTopBar title="创建 Workshop" :show-share="false" />

    <section class="body form">
      <p class="flow-hint">{{ flowHint }}</p>

      <p class="form-section">基础信息</p>
      <div class="field">
        <label>Workshop 名称 <em>*</em></label>
        <input v-model="form.workshopName" maxlength="100" placeholder="如:Locking 大师课" />
      </div>
      <div class="field-pair">
        <div class="field">
          <label>城市 ID <em>*</em></label>
          <input v-model="form.cityId" type="number" placeholder="如 1" />
        </div>
        <div class="field">
          <label>舞种 ID</label>
          <input v-model="form.danceStyleId" type="number" placeholder="选填" />
        </div>
      </div>
      <div class="field">
        <label>场地名称 <em>*</em></label>
        <input v-model="form.locationName" maxlength="100" placeholder="如:XX 舞蹈工作室 A 厅" />
      </div>
      <div class="field">
        <label>详细地址 <em>*</em></label>
        <input v-model="form.address" maxlength="200" placeholder="街道、门牌号" />
      </div>
      <div class="field-pair">
        <div class="field">
          <label>基准价格(元)<em>*</em></label>
          <input v-model="form.priceAmount" type="number" min="0" placeholder="场次未单独定价时使用" />
        </div>
        <div class="field">
          <label>报名截止</label>
          <input v-model="form.signupDeadline" type="datetime-local" />
        </div>
      </div>
      <div class="field-pair">
        <div class="field">
          <label>最少成团(人)</label>
          <input v-model="form.minPeople" type="number" min="1" placeholder="选填" />
        </div>
        <div class="field">
          <label>人数上限</label>
          <input v-model="form.maxPeople" type="number" min="1" placeholder="选填" />
        </div>
      </div>

      <p class="form-section">场次(每场可单独定价与限容)</p>
      <div v-for="(s, i) in sessions" :key="i" class="session-card">
        <div class="session-head">
          <strong>场次 {{ i + 1 }}</strong>
          <button v-if="sessions.length > 1" class="remove" @click="removeSessionDraft(i)">
            删除
          </button>
        </div>
        <div class="field">
          <label>场次名称</label>
          <input v-model="s.sessionName" maxlength="100" placeholder="如:Day 1 基础" />
        </div>
        <div class="field">
          <label>日期 <em>*</em></label>
          <input v-model="s.date" type="date" />
        </div>
        <div class="field-pair">
          <div class="field">
            <label>开始 <em>*</em></label>
            <input v-model="s.startTime" type="time" />
          </div>
          <div class="field">
            <label>结束 <em>*</em></label>
            <input v-model="s.endTime" type="time" />
          </div>
        </div>
        <div class="field-pair">
          <div class="field">
            <label>容量(人)<em>*</em></label>
            <input v-model="s.capacity" type="number" min="1" placeholder="如 20" />
          </div>
          <div class="field">
            <label>场次价格(元)</label>
            <input v-model="s.priceAmount" type="number" min="0" placeholder="留空用基准价" />
          </div>
        </div>
      </div>
      <button class="add-session" @click="addSessionDraft">+ 添加场次</button>

      <p class="form-section">封面与介绍</p>
      <div class="upload">
        <label class="upload-tile">
          <img v-if="form.coverAssetId" :src="mediaContentUrl(form.coverAssetId)" alt="封面" />
          <span v-else>{{ uploading ? '上传中…' : '+ 封面' }}</span>
          <input type="file" accept="image/jpeg,image/png,image/webp" hidden @change="onPickCover" />
        </label>
      </div>
      <div class="field" style="margin-top: 14px">
        <label>活动介绍</label>
        <textarea v-model="form.intro" maxlength="5000" placeholder="师资、内容编排、注意事项…" />
      </div>
    </section>

    <footer class="submit-bar">
      <button :disabled="submitting || uploading || !ready" @click="submit">
        {{ submitting ? '提交中…' : activeRole === 'studio_admin' || isFreelance ? '创建并发布' : '提交舞室审批' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.create-page {
  @include ops-page;
}
.body {
  @include ops-body;
}
.form {
  @include ops-form;
}
.upload {
  @include ops-upload;
}
.submit-bar {
  @include ops-submit-bar;
}

.flow-hint {
  margin: 4px 0 0;
  border-radius: 16px;
  background: $pen-soft;
  padding: 12px 14px;
  color: $pen-charcoal;
  font-size: 12.5px;
  font-weight: 700;
  line-height: 1.5;
}

.session-card {
  border: 1px solid $pen-hairline;
  border-radius: 20px;
  padding: 14px 14px 2px;
  margin-bottom: 12px;
}

.session-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  strong {
    font-size: 14px;
    font-weight: 900;
  }
  .remove {
    border: 0;
    background: none;
    color: #d30005;
    font-size: 12.5px;
    font-weight: 800;
    cursor: pointer;
  }
}

.add-session {
  width: 100%;
  height: 44px;
  border: 1px dashed $pen-hairline-strong;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}
</style>
