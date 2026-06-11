<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { fetchMyStudioClaims, type StudioClaim } from '@/api/coachOps';
import { useOpsStore } from '@/stores/ops';

const router = useRouter();
const ops = useOpsStore();
const claims = ref<StudioClaim[]>([]);
const loading = ref(true);

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending: { label: '审核中', cls: 'warn' },
  approved: { label: '已通过', cls: 'ok' },
  rejected: { label: '已拒绝', cls: 'bad' }
};

const typeLabels: Record<string, string> = {
  owner_claim: '认领已有舞室',
  operator_claim: '运营方认领',
  new_studio: '新舞室入驻'
};

const sorted = computed(() =>
  [...claims.value].sort((a, b) => (b.createdAt > a.createdAt ? 1 : -1))
);

const fmt = (t?: string | null) => (t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—');

const load = async () => {
  loading.value = true;
  try {
    claims.value = await fetchMyStudioClaims();
  } finally {
    loading.value = false;
  }
};

const enterWorkspace = async () => {
  await ops.refresh(true);
  ops.setRole('studio_admin');
  router.replace('/coach/dashboard');
};

onMounted(load);
</script>

<template>
  <main class="status-page">
    <PenTopBar title="入驻审核进度" :show-share="false" />

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <EmptyState
        v-else-if="!sorted.length"
        title="还没有入驻申请"
        desc="认领已有舞室或提交新舞室入驻,审核通过后开通商家工作台"
        action-text="去入驻 / 认领"
        @action="router.push('/coach/studio-claim')"
      />

      <article v-for="c in sorted" :key="c.id" class="card">
        <div class="head">
          <h3>{{ typeLabels[c.claimType] ?? c.claimType }}</h3>
          <span class="badge" :class="statusMeta[c.claimStatus]?.cls">
            {{ statusMeta[c.claimStatus]?.label ?? c.claimStatus }}
          </span>
        </div>
        <p class="meta">
          <span v-if="c.studioId">舞室 #{{ c.studioId }}</span>
          <span>提交于 {{ fmt(c.createdAt) }}</span>
          <span v-if="c.reviewedAt">审核于 {{ fmt(c.reviewedAt) }}</span>
        </p>
        <p v-if="c.submittedRemark" class="remark">备注:{{ c.submittedRemark }}</p>
        <p v-if="c.claimStatus === 'rejected' && c.reviewRemark" class="reject-reason">
          拒绝原因:{{ c.reviewRemark }}
        </p>
        <div class="actions">
          <button
            v-if="c.claimStatus === 'rejected'"
            class="primary"
            @click="router.push('/coach/studio-claim')"
          >
            重新提交
          </button>
          <button v-if="c.claimStatus === 'approved'" class="primary" @click="enterWorkspace">
            进入商家工作台
          </button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.status-page {
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
.remark {
  margin: 10px 0 0;
  color: $pen-charcoal;
  font-size: 13px;
  line-height: 1.5;
}
.reject-reason {
  margin: 10px 0 0;
  color: #d30005;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.5;
}
.actions {
  @include ops-actions;
}
</style>
