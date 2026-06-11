<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { useOpsStore } from '@/stores/ops';
import {
  fetchWeekSchedules,
  fetchMerchantCourses,
  cancelSchedule,
  type ScheduleItem,
  type MerchantCourse
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();

const weekOffset = ref(0);
const schedules = ref<ScheduleItem[]>([]);
const courses = ref<MerchantCourse[]>([]);
const loading = ref(true);
const selectedDay = ref(0);

const startOfWeek = computed(() => {
  const now = new Date();
  const day = (now.getDay() + 6) % 7; // 周一为 0
  const monday = new Date(now);
  monday.setHours(0, 0, 0, 0);
  monday.setDate(now.getDate() - day + weekOffset.value * 7);
  return monday;
});

const days = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date(startOfWeek.value);
    d.setDate(d.getDate() + i);
    return d;
  })
);

const dayNames = ['一', '二', '三', '四', '五', '六', '日'];

const courseName = (id: number) =>
  courses.value.find((c) => c.id === id)?.courseName ?? `课程 #${id}`;

const daySchedules = computed(() => {
  const d = days.value[selectedDay.value];
  const next = new Date(d);
  next.setDate(next.getDate() + 1);
  return schedules.value
    .filter((s) => {
      const t = new Date(s.startAt);
      return t >= d && t < next;
    })
    .sort((a, b) => a.startAt.localeCompare(b.startAt));
});

const countOf = (i: number) => {
  const d = days.value[i];
  const next = new Date(d);
  next.setDate(next.getDate() + 1);
  return schedules.value.filter((s) => {
    const t = new Date(s.startAt);
    return t >= d && t < next;
  }).length;
};

const fmtTime = (t: string) =>
  new Date(t).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false });

const weekLabel = computed(() => {
  const from = days.value[0];
  const to = days.value[6];
  const f = (d: Date) => `${d.getMonth() + 1}.${d.getDate()}`;
  return `${f(from)} - ${f(to)}`;
});

const load = async () => {
  await ops.refresh();
  if (!ops.studioId) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const from = startOfWeek.value.toISOString();
    const end = new Date(startOfWeek.value);
    end.setDate(end.getDate() + 7);
    const [s, c] = await Promise.all([
      fetchWeekSchedules(ops.studioId, from, end.toISOString()),
      courses.value.length ? Promise.resolve(courses.value) : fetchMerchantCourses(ops.studioId)
    ]);
    schedules.value = s;
    courses.value = c;
  } finally {
    loading.value = false;
  }
};

const shiftWeek = (delta: number) => {
  weekOffset.value += delta;
  load();
};

const onCancel = async (s: ScheduleItem) => {
  await showConfirmDialog({
    title: '取消场次',
    message: `确认取消 ${fmtTime(s.startAt)} 的「${courseName(s.courseId)}」?已预约学员需另行处理。`
  });
  await cancelSchedule(s.id);
  showSuccessToast('已取消');
  load();
};

onMounted(load);
</script>

<template>
  <main class="schedule-page">
    <PenTopBar title="周课表" :show-share="false" />

    <div class="week-nav">
      <button @click="shiftWeek(-1)">‹ 上周</button>
      <strong>{{ weekLabel }}</strong>
      <button @click="shiftWeek(1)">下周 ›</button>
    </div>

    <nav class="day-strip">
      <button
        v-for="(d, i) in days"
        :key="i"
        :class="{ active: selectedDay === i }"
        @click="selectedDay = i"
      >
        <small>周{{ dayNames[i] }}</small>
        <strong>{{ d.getDate() }}</strong>
        <i v-if="countOf(i)">{{ countOf(i) }}</i>
      </button>
    </nav>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <EmptyState
        v-else-if="!ops.studioId"
        title="尚未开通商家后台"
        desc="完成舞室入驻后即可排课"
        action-text="去入驻 / 认领"
        @action="router.push('/coach/studio-claim')"
      />

      <EmptyState
        v-else-if="!daySchedules.length"
        title="当天暂无排期"
        desc="为这一天添加课程场次"
        action-text="新增场次"
        @action="router.push('/coach/schedule-edit')"
      />

      <article v-for="s in daySchedules" :key="s.id" class="card" :class="{ canceled: s.status === 'canceled' }">
        <div class="head">
          <h3>{{ courseName(s.courseId) }}</h3>
          <span class="badge" :class="s.status === 'canceled' ? 'bad' : 'ok'">
            {{ s.status === 'canceled' ? '已取消' : '正常' }}
          </span>
        </div>
        <p class="meta">
          <span>{{ fmtTime(s.startAt) }} - {{ fmtTime(s.endAt) }}</span>
          <span v-if="s.classroomName">{{ s.classroomName }}</span>
          <span v-if="s.coachId">教练 #{{ s.coachId }}</span>
          <span>{{ s.bookedCount ?? 0 }}/{{ s.capacity ?? '∞' }} 已约</span>
        </p>
        <div class="actions">
          <button @click="router.push(`/coach/schedule/${s.id}/bookings`)">预约名单</button>
          <template v-if="s.status !== 'canceled'">
            <button @click="router.push(`/coach/schedule-edit/${s.id}`)">编辑</button>
            <button class="danger" @click="onCancel(s)">取消场次</button>
          </template>
        </div>
      </article>
    </section>

    <footer v-if="ops.studioId" class="submit-bar">
      <button @click="router.push('/coach/schedule-edit')">+ 新增场次</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.schedule-page {
  @include ops-page;
}

.week-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px 4px;
  strong {
    font-size: 15px;
    font-weight: 900;
  }
  button {
    border: 0;
    background: none;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;
  }
}

.day-strip {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  padding: 10px 18px;
  button {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: 8px 0 10px;
    border: 1px solid $pen-hairline;
    border-radius: 16px;
    background: $pen-canvas;
    cursor: pointer;
    small {
      font-size: 10px;
      font-weight: 800;
      color: $pen-mute;
    }
    strong {
      font-size: 16px;
      font-weight: 900;
      color: $pen-ink;
    }
    i {
      position: absolute;
      top: 4px;
      right: 6px;
      font-style: normal;
      font-size: 9px;
      font-weight: 900;
      color: #fff;
      background: $pen-ink;
      border-radius: 999px;
      min-width: 14px;
      height: 14px;
      display: grid;
      place-items: center;
      padding: 0 3px;
    }
    &.active {
      border-color: $pen-ink;
      background: $pen-ink;
      small,
      strong {
        color: #fff;
      }
      i {
        background: #fff;
        color: $pen-ink;
      }
    }
  }
}

.body {
  @include ops-body;
}
.loading {
  @include ops-loading;
}
.card {
  @include ops-card;
  &.canceled {
    opacity: 0.55;
  }
}
.head {
  @include ops-card-head;
}
.badge {
  @include ops-badge;
}
.meta {
  @include ops-meta;
}
.actions {
  @include ops-actions;
}
.submit-bar {
  @include ops-submit-bar;
}
</style>
