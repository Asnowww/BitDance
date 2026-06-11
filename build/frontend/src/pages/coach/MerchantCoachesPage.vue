<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { useOpsStore } from '@/stores/ops';
import {
  fetchStudioCoachRelations,
  inviteCoach,
  updateCoachRelation,
  type CoachRelation
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();
const relations = ref<CoachRelation[]>([]);
const loading = ref(true);

const typeLabels: Record<string, string> = {
  full_time: '全职教练',
  signed: '签约教练',
  independent: '自由教练'
};

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending: { label: '待教练确认', cls: 'warn' },
  active: { label: '合作中', cls: 'ok' },
  inactive: { label: '已暂停', cls: '' },
  terminated: { label: '已终止', cls: 'bad' }
};

// ---------- 邀请表单 ----------
const showInvite = ref(false);
const invite = ref({
  coachId: '' as string | number,
  relationType: 'signed' as 'full_time' | 'signed' | 'independent',
  settlementRatio: '' as string | number
});
const inviting = ref(false);

const submitInvite = async () => {
  if (!ops.studioId || invite.value.coachId === '' || inviting.value) return;
  inviting.value = true;
  try {
    await inviteCoach({
      studioId: ops.studioId,
      coachId: Number(invite.value.coachId),
      relationType: invite.value.relationType,
      settlementMode: invite.value.settlementRatio === '' ? undefined : 'ratio',
      settlementRatio:
        invite.value.settlementRatio === '' ? undefined : Number(invite.value.settlementRatio) / 100
    });
    showSuccessToast('邀请已发送,等待教练确认');
    showInvite.value = false;
    invite.value = { coachId: '', relationType: 'signed', settlementRatio: '' };
    load();
  } finally {
    inviting.value = false;
  }
};

// ---------- 调整分成 ----------
const editing = ref<CoachRelation | null>(null);
const editRatio = ref<string | number>('');

const openEdit = (r: CoachRelation) => {
  editing.value = r;
  editRatio.value = r.settlementRatio != null ? Math.round(r.settlementRatio * 100) : '';
};

const submitEdit = async () => {
  if (!editing.value || editRatio.value === '') return;
  await updateCoachRelation(editing.value.id, {
    settlementRatio: Number(editRatio.value) / 100
  });
  showSuccessToast('分成已调整,立即生效');
  editing.value = null;
  load();
};

const terminate = async (r: CoachRelation) => {
  await showConfirmDialog({
    title: '终止合作',
    message: `确认终止与教练 #${r.coachId} 的合作?该教练将无法继续管理本舞室课表。`
  });
  await updateCoachRelation(r.id, { relationStatus: 'terminated' });
  showSuccessToast('已终止合作');
  load();
};

const load = async () => {
  await ops.refresh();
  if (!ops.studioId) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    relations.value = await fetchStudioCoachRelations(ops.studioId);
  } finally {
    loading.value = false;
  }
};

const ratioText = (r: CoachRelation) => {
  if (r.relationType === 'full_time') return '收益归舞室';
  if (r.relationType === 'independent') return '收益归教练本人';
  return r.settlementRatio != null ? `教练分成 ${Math.round(r.settlementRatio * 100)}%` : '按协议分成';
};

onMounted(load);
</script>

<template>
  <main class="coaches-page">
    <PenTopBar title="教练管理" :show-share="false" />

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <EmptyState
        v-else-if="!ops.studioId"
        title="尚未开通商家后台"
        desc="完成舞室入驻后即可邀请教练"
        action-text="去入驻 / 认领"
        @action="router.push('/coach/studio-claim')"
      />

      <EmptyState
        v-else-if="!relations.length"
        title="暂无教练"
        desc="邀请教练加入舞室,设置合作类型与分成"
        action-text="邀请教练"
        @action="showInvite = true"
      />

      <article v-for="r in relations" :key="r.id" class="card">
        <div class="head">
          <h3>教练 #{{ r.coachId }}</h3>
          <span class="badge" :class="statusMeta[r.relationStatus]?.cls">
            {{ statusMeta[r.relationStatus]?.label ?? r.relationStatus }}
          </span>
        </div>
        <p class="meta">
          <span>{{ typeLabels[r.relationType] ?? r.relationType }}</span>
          <span>{{ ratioText(r) }}</span>
          <span v-if="r.effectiveFrom">自 {{ r.effectiveFrom }}</span>
        </p>
        <div v-if="['pending', 'active'].includes(r.relationStatus)" class="actions">
          <button v-if="r.relationType === 'signed'" @click="openEdit(r)">调整分成</button>
          <button class="danger" @click="terminate(r)">终止合作</button>
        </div>
      </article>
    </section>

    <footer v-if="ops.studioId" class="submit-bar">
      <button @click="showInvite = true">+ 邀请教练</button>
    </footer>

    <!-- 邀请弹层 -->
    <van-popup v-model:show="showInvite" position="bottom" round>
      <div class="sheet form">
        <h2>邀请教练</h2>
        <div class="field">
          <label>教练 ID <em>*</em></label>
          <input v-model="invite.coachId" type="number" placeholder="输入教练编号" />
          <p class="hint">可在教练详情页查看教练编号</p>
        </div>
        <div class="field">
          <label>合作类型</label>
          <div class="seg">
            <button
              v-for="(label, key) in typeLabels"
              :key="key"
              :class="{ active: invite.relationType === key }"
              @click="invite.relationType = key as 'full_time' | 'signed' | 'independent'"
            >
              {{ label }}
            </button>
          </div>
        </div>
        <div v-if="invite.relationType === 'signed'" class="field">
          <label>教练分成比例(%)</label>
          <input v-model="invite.settlementRatio" type="number" min="0" max="100" placeholder="如 60" />
        </div>
        <p class="hint" style="margin-bottom: 14px">
          全职教练收益归舞室;签约教练按比例分成;自由教练收益归本人。教练接受邀请后生效。
        </p>
        <button class="sheet-submit" :disabled="inviting || invite.coachId === ''" @click="submitInvite">
          {{ inviting ? '发送中…' : '发送邀请' }}
        </button>
      </div>
    </van-popup>

    <!-- 调整分成弹层 -->
    <van-popup :show="Boolean(editing)" position="bottom" round @update:show="editing = null">
      <div class="sheet form">
        <h2>调整分成 · 教练 #{{ editing?.coachId }}</h2>
        <div class="field">
          <label>教练分成比例(%)</label>
          <input v-model="editRatio" type="number" min="0" max="100" />
        </div>
        <p class="hint" style="margin-bottom: 14px">调整立即生效,后续订单按新比例计算。</p>
        <button class="sheet-submit" :disabled="editRatio === ''" @click="submitEdit">确认调整</button>
      </div>
    </van-popup>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.coaches-page {
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
.submit-bar {
  @include ops-submit-bar;
}

.sheet {
  padding: 24px 20px calc(24px + env(safe-area-inset-bottom));
  @include ops-form;
  h2 {
    margin: 0 0 18px;
    font-size: 20px;
    font-weight: 900;
  }
  .sheet-submit {
    @include pen-primary-btn;
    width: 100%;
  }
}
</style>
