<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { useOpsStore } from '@/stores/ops';
import {
  fetchMerchantCourses,
  publishMerchantCourse,
  offlineMerchantCourse,
  type MerchantCourse
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();
const courses = ref<MerchantCourse[]>([]);
const loading = ref(true);
const filter = ref<'all' | 'draft' | 'published' | 'offline'>('all');

const filters = [
  { key: 'all', label: '全部' },
  { key: 'draft', label: '草稿' },
  { key: 'published', label: '已发布' },
  { key: 'offline', label: '已下架' }
] as const;

const statusMeta: Record<string, { label: string; cls: string }> = {
  draft: { label: '草稿', cls: 'warn' },
  published: { label: '已发布', cls: 'ok' },
  offline: { label: '已下架', cls: '' }
};

const list = computed(() =>
  filter.value === 'all' ? courses.value : courses.value.filter((c) => c.status === filter.value)
);

const load = async () => {
  await ops.refresh();
  if (!ops.studioId) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    courses.value = await fetchMerchantCourses(ops.studioId);
  } finally {
    loading.value = false;
  }
};

const publish = async (c: MerchantCourse) => {
  await publishMerchantCourse(c.id);
  showSuccessToast('已发布');
  load();
};

const offline = async (c: MerchantCourse) => {
  await showConfirmDialog({
    title: '下架课程',
    message: `确认下架「${c.courseName}」?下架后用户端不再展示。`
  });
  await offlineMerchantCourse(c.id);
  showSuccessToast('已下架');
  load();
};

onMounted(load);
</script>

<template>
  <main class="courses-page">
    <PenTopBar title="课程管理" :show-share="false" />

    <nav class="chips">
      <button
        v-for="f in filters"
        :key="f.key"
        :class="{ active: filter === f.key }"
        @click="filter = f.key"
      >
        {{ f.label }}
      </button>
    </nav>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <EmptyState
        v-else-if="!ops.studioId"
        title="尚未开通商家后台"
        desc="完成舞室入驻或认领后即可管理课程"
        action-text="去入驻 / 认领"
        @action="router.push('/coach/studio-claim')"
      />

      <EmptyState
        v-else-if="!list.length"
        title="暂无课程"
        desc="创建第一门课程,配置试听与排期"
        action-text="新建课程"
        @action="router.push('/coach/course-edit')"
      />

      <article v-for="c in list" :key="c.id" class="card">
        <div class="head">
          <h3>{{ c.courseName }}</h3>
          <span class="badge" :class="statusMeta[c.status]?.cls">
            {{ statusMeta[c.status]?.label ?? c.status }}
          </span>
        </div>
        <p class="meta">
          <span>¥{{ c.priceAmount ?? '—' }}</span>
          <span>{{ c.durationMinutes ?? '—' }} 分钟</span>
          <span>难度 {{ c.difficultyLevel }}</span>
          <span v-if="c.trialEnabled">可试听 ¥{{ c.trialPriceAmount ?? 0 }} · {{ c.trialCapacity ?? 0 }} 名额</span>
          <span v-if="c.zeroBasicFriendly">零基础友好</span>
        </p>
        <div class="actions">
          <button @click="router.push(`/coach/course-edit/${c.id}`)">编辑</button>
          <button v-if="c.status !== 'published'" class="primary" @click="publish(c)">发布</button>
          <button v-if="c.status === 'published'" class="danger" @click="offline(c)">下架</button>
          <button @click="router.push({ path: '/coach/schedule', query: { courseId: c.id } })">
            排期
          </button>
        </div>
      </article>
    </section>

    <footer v-if="ops.studioId" class="submit-bar">
      <button @click="router.push('/coach/course-edit')">+ 新建课程</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.courses-page {
  @include ops-page;
}
.chips {
  @include ops-chip-row;
}
.body {
  @include ops-body;
}
.loading {
  @include ops-loading;
}
.card {
  @include ops-card;
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
