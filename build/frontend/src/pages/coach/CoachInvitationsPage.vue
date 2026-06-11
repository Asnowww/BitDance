<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import {
  fetchMyInvitations,
  acceptInvitation,
  rejectInvitation,
  type CoachRelation
} from '@/api/coachOps';

const invitations = ref<CoachRelation[]>([]);
const loading = ref(true);

const typeLabels: Record<string, string> = {
  full_time: '全职教练',
  signed: '签约教练',
  independent: '自由教练'
};

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending: { label: '待确认', cls: 'warn' },
  active: { label: '合作中', cls: 'ok' },
  inactive: { label: '已暂停', cls: '' },
  terminated: { label: '已终止', cls: 'bad' }
};

const ratioText = (r: CoachRelation) => {
  if (r.relationType === 'full_time') return '课程收益归舞室';
  if (r.relationType === 'independent') return '收益归你本人';
  return r.settlementRatio != null
    ? `你的分成比例 ${Math.round(r.settlementRatio * 100)}%`
    : '按协议分成';
};

const load = async () => {
  loading.value = true;
  try {
    invitations.value = await fetchMyInvitations();
  } finally {
    loading.value = false;
  }
};

const accept = async (r: CoachRelation) => {
  await showConfirmDialog({
    title: '接受邀请',
    message: `确认以「${typeLabels[r.relationType]}」身份与舞室 #${r.studioId} 建立合作?${ratioText(r)}。`
  });
  await acceptInvitation(r.id);
  showSuccessToast('已接受,合作生效');
  load();
};

const reject = async (r: CoachRelation) => {
  await showConfirmDialog({ title: '拒绝邀请', message: `确认拒绝舞室 #${r.studioId} 的合作邀请?` });
  await rejectInvitation(r.id);
  showSuccessToast('已拒绝');
  load();
};

onMounted(load);
</script>

<template>
  <main class="inv-page">
    <PenTopBar title="我的合作邀请" :show-share="false" />

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>
      <EmptyState
        v-else-if="!invitations.length"
        title="暂无邀请"
        desc="舞室管理员发出邀请后会出现在这里"
      />

      <article v-for="r in invitations" :key="r.id" class="card">
        <div class="head">
          <h3>舞室 #{{ r.studioId }} 邀请你加入</h3>
          <span class="badge" :class="statusMeta[r.relationStatus]?.cls">
            {{ statusMeta[r.relationStatus]?.label ?? r.relationStatus }}
          </span>
        </div>
        <p class="meta">
          <span>{{ typeLabels[r.relationType] ?? r.relationType }}</span>
          <span>{{ ratioText(r) }}</span>
          <span v-if="r.effectiveFrom">自 {{ r.effectiveFrom }} 起</span>
        </p>
        <div v-if="r.relationStatus === 'pending'" class="actions">
          <button class="primary" @click="accept(r)">接受</button>
          <button class="danger" @click="reject(r)">拒绝</button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.inv-page {
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
</style>
