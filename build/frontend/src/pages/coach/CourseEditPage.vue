<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { useOpsStore } from '@/stores/ops';
import {
  createMerchantCourse,
  updateMerchantCourse,
  fetchMerchantCourses,
  fetchStudioCoachRelations,
  uploadMediaAsset,
  mediaContentUrl,
  type MerchantCourseBody,
  type CoachRelation
} from '@/api/coachOps';

const route = useRoute();
const router = useRouter();
const ops = useOpsStore();
const courseId = computed(() => (route.params.id ? Number(route.params.id) : null));

const difficulties = [
  { key: 'beginner', label: '入门' },
  { key: 'intermediate', label: '进阶' },
  { key: 'advanced', label: '高级' }
];
const intensities = [
  { key: 'low', label: '低强度' },
  { key: 'medium', label: '中强度' },
  { key: 'high', label: '高强度' }
];
const courseTypes = [
  { key: 'regular', label: '常规课' },
  { key: 'private', label: '私教课' },
  { key: 'choreo', label: '编舞课' }
];

const form = ref({
  courseName: '',
  danceStyleId: '' as string | number,
  coachId: '' as string | number,
  difficultyLevel: 'beginner',
  intensityLevel: 'medium',
  courseType: 'regular',
  priceAmount: '' as string | number,
  durationMinutes: '60' as string | number,
  trialEnabled: false,
  trialPriceAmount: '' as string | number,
  trialCapacity: '' as string | number,
  zeroBasicFriendly: false,
  description: '',
  coverAssetId: null as number | null
});

const coachOptions = ref<CoachRelation[]>([]);
const uploading = ref(false);
const submitting = ref(false);
const loading = ref(true);

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
    const asset = await uploadMediaAsset(file, 'course_cover');
    form.value.coverAssetId = asset.assetId;
  } finally {
    uploading.value = false;
  }
};

const ready = computed(
  () =>
    form.value.courseName.trim() &&
    form.value.danceStyleId !== '' &&
    form.value.difficultyLevel &&
    (!form.value.trialEnabled || form.value.trialCapacity !== '')
);

const submit = async () => {
  if (!ops.studioId || !ready.value || submitting.value) return;
  submitting.value = true;
  const f = form.value;
  const body: MerchantCourseBody = {
    studioId: ops.studioId,
    coachId: f.coachId === '' ? undefined : Number(f.coachId),
    danceStyleId: Number(f.danceStyleId),
    courseName: f.courseName.trim(),
    difficultyLevel: f.difficultyLevel,
    priceAmount: f.priceAmount === '' ? undefined : Number(f.priceAmount),
    trialEnabled: f.trialEnabled,
    trialPriceAmount:
      f.trialEnabled && f.trialPriceAmount !== '' ? Number(f.trialPriceAmount) : undefined,
    trialCapacity: f.trialEnabled && f.trialCapacity !== '' ? Number(f.trialCapacity) : undefined,
    durationMinutes: f.durationMinutes === '' ? undefined : Number(f.durationMinutes),
    intensityLevel: f.intensityLevel,
    courseType: f.courseType,
    zeroBasicFriendly: f.zeroBasicFriendly,
    description: f.description.trim() || undefined,
    coverAssetId: f.coverAssetId ?? undefined
  };
  try {
    if (courseId.value) {
      await updateMerchantCourse(courseId.value, body);
      showSuccessToast('已保存');
    } else {
      await createMerchantCourse(body);
      showSuccessToast('已创建,可在列表中发布');
    }
    router.back();
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  await ops.refresh();
  if (!ops.studioId) {
    loading.value = false;
    return;
  }
  try {
    const [courses, relations] = await Promise.all([
      courseId.value ? fetchMerchantCourses(ops.studioId) : Promise.resolve([]),
      fetchStudioCoachRelations(ops.studioId).catch(() => [])
    ]);
    coachOptions.value = relations.filter((r) => r.relationStatus === 'active');
    if (courseId.value) {
      const c = courses.find((x) => x.id === courseId.value);
      if (c) {
        form.value = {
          courseName: c.courseName,
          danceStyleId: c.danceStyleId,
          coachId: c.coachId ?? '',
          difficultyLevel: c.difficultyLevel,
          intensityLevel: c.intensityLevel ?? 'medium',
          courseType: c.courseType ?? 'regular',
          priceAmount: c.priceAmount ?? '',
          durationMinutes: c.durationMinutes ?? '',
          trialEnabled: Boolean(c.trialEnabled),
          trialPriceAmount: c.trialPriceAmount ?? '',
          trialCapacity: c.trialCapacity ?? '',
          zeroBasicFriendly: Boolean(c.zeroBasicFriendly),
          description: c.description ?? '',
          coverAssetId: c.coverAssetId
        };
      }
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="edit-page">
    <PenTopBar :title="courseId ? '编辑课程' : '新建课程'" :show-share="false" />

    <p v-if="loading" class="loading">加载中…</p>

    <section v-else class="body form">
      <p class="form-section">基础信息</p>
      <div class="field">
        <label>课程名称 <em>*</em></label>
        <input v-model="form.courseName" maxlength="50" placeholder="如:Hiphop 基础元素" />
      </div>
      <div class="field-pair">
        <div class="field">
          <label>舞种 ID <em>*</em></label>
          <input v-model="form.danceStyleId" type="number" placeholder="如 1" />
        </div>
        <div class="field">
          <label>授课教练</label>
          <select v-model="form.coachId">
            <option value="">暂不绑定</option>
            <option v-for="r in coachOptions" :key="r.id" :value="r.coachId">
              教练 #{{ r.coachId }}({{ r.relationType === 'full_time' ? '全职' : r.relationType === 'signed' ? '签约' : '自由' }})
            </option>
          </select>
        </div>
      </div>

      <div class="field">
        <label>课程难度 <em>*</em></label>
        <div class="seg">
          <button
            v-for="d in difficulties"
            :key="d.key"
            :class="{ active: form.difficultyLevel === d.key }"
            @click="form.difficultyLevel = d.key"
          >
            {{ d.label }}
          </button>
        </div>
      </div>
      <div class="field">
        <label>强度 / 目标人群</label>
        <div class="seg">
          <button
            v-for="i in intensities"
            :key="i.key"
            :class="{ active: form.intensityLevel === i.key }"
            @click="form.intensityLevel = i.key"
          >
            {{ i.label }}
          </button>
        </div>
      </div>
      <div class="field">
        <label>课程类型</label>
        <div class="seg">
          <button
            v-for="t in courseTypes"
            :key="t.key"
            :class="{ active: form.courseType === t.key }"
            @click="form.courseType = t.key"
          >
            {{ t.label }}
          </button>
        </div>
      </div>

      <div class="field-pair">
        <div class="field">
          <label>价格(元)</label>
          <input v-model="form.priceAmount" type="number" min="0" placeholder="单节价格" />
        </div>
        <div class="field">
          <label>时长(分钟)</label>
          <input v-model="form.durationMinutes" type="number" min="15" placeholder="60" />
        </div>
      </div>

      <div class="switch-row">
        <span>零基础友好</span>
        <van-switch v-model="form.zeroBasicFriendly" size="22" />
      </div>

      <p class="form-section">试听配置</p>
      <div class="switch-row">
        <span>开放试听</span>
        <van-switch v-model="form.trialEnabled" size="22" />
      </div>
      <div v-if="form.trialEnabled" class="field-pair" style="margin-top: 14px">
        <div class="field">
          <label>试听价格(元)</label>
          <input v-model="form.trialPriceAmount" type="number" min="0" placeholder="0 为免费" />
        </div>
        <div class="field">
          <label>试听名额 <em>*</em></label>
          <input v-model="form.trialCapacity" type="number" min="1" placeholder="如 5" />
        </div>
      </div>

      <p class="form-section">封面与介绍</p>
      <div class="upload">
        <label class="upload-tile">
          <img v-if="form.coverAssetId" :src="mediaContentUrl(form.coverAssetId)" alt="封面" />
          <span v-else>{{ uploading ? '上传中…' : '+ 封面' }}</span>
          <input type="file" accept="image/jpeg,image/png,image/webp" hidden @change="onPickCover" />
        </label>
      </div>
      <div class="field" style="margin-top: 14px">
        <label>课程介绍</label>
        <textarea v-model="form.description" maxlength="2000" placeholder="内容编排、适合人群、注意事项…" />
      </div>
    </section>

    <footer class="submit-bar">
      <button :disabled="submitting || uploading || !ready" @click="submit">
        {{ submitting ? '保存中…' : courseId ? '保存修改' : '创建课程' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.edit-page {
  @include ops-page;
}
.body {
  @include ops-body;
}
.form {
  @include ops-form;
}
.loading {
  @include ops-loading;
}
.upload {
  @include ops-upload;
}
.submit-bar {
  @include ops-submit-bar;
}
</style>
