<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { useOpsStore } from '@/stores/ops';
import {
  createSchedule,
  updateSchedule,
  fetchMerchantCourses,
  fetchStudioCoachRelations,
  type MerchantCourse,
  type CoachRelation
} from '@/api/coachOps';

const route = useRoute();
const router = useRouter();
const ops = useOpsStore();
const scheduleId = computed(() => (route.params.id ? Number(route.params.id) : null));

const courses = ref<MerchantCourse[]>([]);
const coaches = ref<CoachRelation[]>([]);
const loading = ref(true);
const submitting = ref(false);

const form = ref({
  courseId: '' as string | number,
  coachId: '' as string | number,
  classroomName: '',
  date: '',
  startTime: '',
  endTime: '',
  capacity: '' as string | number
});

const ready = computed(
  () =>
    form.value.courseId !== '' &&
    form.value.date &&
    form.value.startTime &&
    form.value.endTime &&
    form.value.startTime < form.value.endTime
);

const toIso = (date: string, time: string) => new Date(`${date}T${time}:00`).toISOString();

const submit = async () => {
  if (!ops.studioId || !ready.value || submitting.value) return;
  if (form.value.startTime >= form.value.endTime) {
    showFailToast('结束时间必须晚于开始时间');
    return;
  }
  submitting.value = true;
  const f = form.value;
  const body = {
    courseId: Number(f.courseId),
    studioId: ops.studioId,
    coachId: f.coachId === '' ? undefined : Number(f.coachId),
    classroomName: f.classroomName.trim() || undefined,
    startAt: toIso(f.date, f.startTime),
    endAt: toIso(f.date, f.endTime),
    capacity: f.capacity === '' ? undefined : Number(f.capacity)
  };
  try {
    if (scheduleId.value) {
      await updateSchedule(scheduleId.value, body);
      showSuccessToast('已保存');
    } else {
      await createSchedule(body);
      showSuccessToast('场次已创建');
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
    const [c, r] = await Promise.all([
      fetchMerchantCourses(ops.studioId),
      fetchStudioCoachRelations(ops.studioId).catch(() => [])
    ]);
    courses.value = c.filter((x) => x.status !== 'offline');
    coaches.value = r.filter((x) => x.relationStatus === 'active');
    const presetCourse = route.query.courseId ? Number(route.query.courseId) : null;
    if (presetCourse && c.some((x) => x.id === presetCourse)) {
      form.value.courseId = presetCourse;
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="edit-page">
    <PenTopBar :title="scheduleId ? '编辑场次' : '新增场次'" :show-share="false" />

    <p v-if="loading" class="loading">加载中…</p>

    <section v-else class="body form">
      <p class="form-section">课程与教练</p>
      <div class="field">
        <label>课程 <em>*</em></label>
        <select v-model="form.courseId">
          <option value="" disabled>选择课程</option>
          <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.courseName }}</option>
        </select>
      </div>
      <div class="field">
        <label>授课教练</label>
        <select v-model="form.coachId">
          <option value="">暂不指定</option>
          <option v-for="r in coaches" :key="r.id" :value="r.coachId">教练 #{{ r.coachId }}</option>
        </select>
      </div>
      <div class="field">
        <label>教室</label>
        <input v-model="form.classroomName" maxlength="50" placeholder="如:A 教室" />
      </div>

      <p class="form-section">时间与容量</p>
      <div class="field">
        <label>日期 <em>*</em></label>
        <input v-model="form.date" type="date" />
      </div>
      <div class="field-pair">
        <div class="field">
          <label>开始时间 <em>*</em></label>
          <input v-model="form.startTime" type="time" />
        </div>
        <div class="field">
          <label>结束时间 <em>*</em></label>
          <input v-model="form.endTime" type="time" />
        </div>
      </div>
      <div class="field">
        <label>容量(人)</label>
        <input v-model="form.capacity" type="number" min="1" placeholder="不填为不限" />
      </div>
    </section>

    <footer class="submit-bar">
      <button :disabled="submitting || !ready" @click="submit">
        {{ submitting ? '保存中…' : scheduleId ? '保存修改' : '创建场次' }}
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
.submit-bar {
  @include ops-submit-bar;
}
</style>
