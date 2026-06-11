<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { storeToRefs } from 'pinia';
import { useOpsStore } from '@/stores/ops';
import {
  fetchMerchantWorkshops,
  publishMerchantWorkshop,
  offlineMerchantWorkshop,
  approveMerchantWorkshop,
  rejectMerchantWorkshop,
  type MerchantWorkshop
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();
const { activeRole } = storeToRefs(ops);
const workshops = ref<MerchantWorkshop[]>([]);
const loading = ref(true);

const publishMeta: Record<string, { label: string; cls: string }> = {
  draft: { label: '草稿', cls: 'warn' },
  pending_approval: { label: '待舞室审批', cls: 'warn' },
  published: { label: '已发布', cls: 'ok' },
  offline: { label: '已下架', cls: '' },
  rejected: { label: '已驳回', cls: 'bad' }
};

const auditMeta: Record<string, string> = {
  pending: '平台审核中',
  approved: '平台审核通过',
  rejected: '平台审核驳回'
};

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  await ops.refresh();
  if (!ops.studioId) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    workshops.value = await fetchMerchantWorkshops(ops.studioId);
  } finally {
    loading.value = false;
  }
};

const act = async (w: MerchantWorkshop, action: 'publish' | 'offline' | 'approve' | 'reject') => {
  const confirms: Record<string, string> = {
    publish: `确认发布「${w.workshopName}」?发布后用户端可见可报名。`,
    offline: `确认下架「${w.workshopName}」?用户端将不再展示。`,
    approve: `确认通过教练提交的「${w.workshopName}」?通过后将发布。`,
    reject: `确认驳回「${w.workshopName}」?`
  };
  await showConfirmDialog({ title: '操作确认', message: confirms[action] });
  const fns = {
    publish: publishMerchantWorkshop,
    offline: offlineMerchantWorkshop,
    approve: approveMerchantWorkshop,
    reject: rejectMerchantWorkshop
  };
  await fns[action](w.id);
  showSuccessToast('已处理');
  load();
};

onMounted(load);
</script>

<template>
  <main class="ws-page">
    <PenTopBar title="Workshop 管理" :show-share="false" />

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <EmptyState
        v-else-if="!ops.studioId"
        title="暂无可管理的舞室"
        desc="自由教练发布的 Workshop 不绑定舞室,可直接前往创建"
        action-text="创建 Workshop"
        @action="router.push('/coach/workshop-create')"
      />

      <EmptyState
        v-else-if="!workshops.length"
        title="暂无 Workshop"
        desc="创建你的第一个 Workshop"
        action-text="创建 Workshop"
        @action="router.push('/coach/workshop-create')"
      />

      <article v-for="w in workshops" :key="w.id" class="card">
        <div class="head">
          <h3>{{ w.workshopName }}</h3>
          <span class="badge" :class="publishMeta[w.publishStatus]?.cls">
            {{ publishMeta[w.publishStatus]?.label ?? w.publishStatus }}
          </span>
        </div>
        <p class="meta">
          <span>¥{{ w.priceAmount }}</span>
          <span>{{ w.locationName }}</span>
          <span v-if="w.signupDeadline">截止 {{ fmt(w.signupDeadline) }}</span>
          <span v-if="w.auditStatus && auditMeta[w.auditStatus]">{{ auditMeta[w.auditStatus] }}</span>
        </p>
        <p v-if="w.sessions?.length" class="sessions">
          <span v-for="s in w.sessions" :key="s.id">
            {{ fmt(s.startAt) }} · {{ s.soldCount ?? 0 }}/{{ s.capacity }} 人
            <i v-if="s.priceAmount != null">· ¥{{ s.priceAmount }}</i>
          </span>
        </p>
        <div class="actions">
          <template v-if="activeRole === 'studio_admin' && w.publishStatus === 'pending_approval'">
            <button class="primary" @click="act(w, 'approve')">审批通过</button>
            <button class="danger" @click="act(w, 'reject')">驳回</button>
          </template>
          <button
            v-if="['draft', 'rejected', 'offline'].includes(w.publishStatus)"
            class="primary"
            @click="act(w, 'publish')"
          >
            发布
          </button>
          <button
            v-if="w.publishStatus === 'published'"
            class="danger"
            @click="act(w, 'offline')"
          >
            下架
          </button>
        </div>
      </article>
    </section>

    <footer class="submit-bar">
      <button @click="router.push('/coach/workshop-create')">+ 创建 Workshop</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.ws-page {
  @include ops-page;
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
.sessions {
  margin: 10px 0 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12.5px;
  color: $pen-charcoal;
  font-weight: 600;
  i {
    font-style: normal;
  }
}
.submit-bar {
  @include ops-submit-bar;
}
</style>
