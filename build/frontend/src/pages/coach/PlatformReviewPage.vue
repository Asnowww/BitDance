<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import {
  mediaContentUrl,
  platformFetchStudioClaims,
  platformHandleStudioClaim,
  platformFetchCertifications,
  platformHandleCertification,
  platformFetchWorkshops,
  platformHandleWorkshop,
  platformFetchAppeals,
  platformHandleAppeal,
  type StudioClaim,
  type CoachCertification,
  type WorkshopAdminItem,
  type ReviewAppeal
} from '@/api/coachOps';

type Tab = 'studio' | 'coach' | 'workshop' | 'appeal';

const tab = ref<Tab>('studio');
const statusFilter = ref('pending');
const loading = ref(true);

const claims = ref<StudioClaim[]>([]);
const certs = ref<CoachCertification[]>([]);
const workshops = ref<WorkshopAdminItem[]>([]);
const appeals = ref<ReviewAppeal[]>([]);

const tabs: Array<{ key: Tab; label: string }> = [
  { key: 'studio', label: '舞室入驻' },
  { key: 'coach', label: '教练资质' },
  { key: 'workshop', label: 'Workshop' },
  { key: 'appeal', label: '评价申诉' }
];

const statuses = [
  { key: 'pending', label: '待审核' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '已拒绝' }
];

const claimTypeLabels: Record<string, string> = {
  owner_claim: '认领已有舞室',
  operator_claim: '运营方认领',
  new_studio: '新舞室入驻'
};

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  loading.value = true;
  try {
    const s = statusFilter.value;
    if (tab.value === 'studio') claims.value = (await platformFetchStudioClaims(s)).content ?? [];
    else if (tab.value === 'coach') certs.value = (await platformFetchCertifications(s)).content ?? [];
    else if (tab.value === 'workshop') workshops.value = (await platformFetchWorkshops(s)).content ?? [];
    else appeals.value = (await platformFetchAppeals(s)).content ?? [];
  } finally {
    loading.value = false;
  }
};

const switchTab = (t: Tab) => {
  tab.value = t;
  statusFilter.value = 'pending';
  load();
};

const setStatus = (s: string) => {
  statusFilter.value = s;
  load();
};

// ---------- 审核处理(可填备注) ----------
const pendingAction = ref<{
  kind: Tab;
  id: number;
  action: 'approve' | 'reject';
  title: string;
} | null>(null);
const remark = ref('');
const handling = ref(false);

const openAction = (kind: Tab, id: number, action: 'approve' | 'reject', title: string) => {
  pendingAction.value = { kind, id, action, title };
  remark.value = '';
};

const confirmAction = async () => {
  const p = pendingAction.value;
  if (!p || handling.value) return;
  if (p.action === 'reject') {
    await showConfirmDialog({ title: '确认拒绝', message: `确认拒绝「${p.title}」?` });
  }
  handling.value = true;
  try {
    if (p.kind === 'studio') await platformHandleStudioClaim(p.id, p.action, remark.value || undefined);
    else if (p.kind === 'coach') await platformHandleCertification(p.id, p.action, remark.value || undefined);
    else if (p.kind === 'workshop') await platformHandleWorkshop(p.id, p.action);
    else await platformHandleAppeal(p.id, p.action, remark.value || undefined);
    showSuccessToast('已处理');
    pendingAction.value = null;
    load();
  } finally {
    handling.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="platform-page">
    <PenTopBar title="平台审核中心" :show-share="false" />

    <nav class="chips">
      <button
        v-for="t in tabs"
        :key="t.key"
        :class="{ active: tab === t.key }"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
      </button>
    </nav>

    <nav class="chips sub">
      <button
        v-for="s in statuses"
        :key="s.key"
        :class="{ active: statusFilter === s.key }"
        @click="setStatus(s.key)"
      >
        {{ s.label }}
      </button>
    </nav>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <!-- 舞室入驻 -->
      <template v-else-if="tab === 'studio'">
        <EmptyState v-if="!claims.length" title="暂无记录" />
        <article v-for="c in claims" :key="c.id" class="card">
          <div class="head">
            <h3>{{ claimTypeLabels[c.claimType] ?? c.claimType }} · 申请人 #{{ c.applicantUserId }}</h3>
            <span class="badge" :class="c.claimStatus === 'approved' ? 'ok' : c.claimStatus === 'rejected' ? 'bad' : 'warn'">
              {{ c.claimStatus === 'pending' ? '待审核' : c.claimStatus === 'approved' ? '已通过' : '已拒绝' }}
            </span>
          </div>
          <p class="meta">
            <span v-if="c.studioId">舞室 #{{ c.studioId }}</span>
            <span>{{ fmt(c.createdAt) }}</span>
          </p>
          <p v-if="c.submittedRemark" class="detail">说明:{{ c.submittedRemark }}</p>
          <a
            v-if="c.businessLicenseAssetId"
            class="license-link"
            :href="mediaContentUrl(c.businessLicenseAssetId)"
            target="_blank"
            rel="noopener"
          >
            查看资质材料 ›
          </a>
          <div v-if="c.claimStatus === 'pending'" class="actions">
            <button class="primary" @click="openAction('studio', c.id, 'approve', `申请 #${c.id}`)">
              通过
            </button>
            <button class="danger" @click="openAction('studio', c.id, 'reject', `申请 #${c.id}`)">
              拒绝
            </button>
          </div>
        </article>
      </template>

      <!-- 教练资质 -->
      <template v-else-if="tab === 'coach'">
        <EmptyState v-if="!certs.length" title="暂无记录" />
        <article v-for="c in certs" :key="c.id" class="card">
          <div class="head">
            <h3>
              {{ c.coachType === 'freelance' ? '自由教练' : c.coachType === 'signed' ? '签约教练' : '全职教练' }}资质 · 用户 #{{ c.userId }}
            </h3>
            <span class="badge" :class="c.applicationStatus === 'approved' ? 'ok' : c.applicationStatus === 'rejected' ? 'bad' : 'warn'">
              {{ c.applicationStatus === 'pending' ? '待审核' : c.applicationStatus === 'approved' ? '已通过' : '已拒绝' }}
            </span>
          </div>
          <p class="meta"><span>{{ fmt(c.createdAt) }}</span></p>
          <p v-if="c.remark" class="detail">资质说明:{{ c.remark }}</p>
          <div v-if="c.applicationStatus === 'pending'" class="actions">
            <button class="primary" @click="openAction('coach', c.id, 'approve', `资质 #${c.id}`)">
              通过
            </button>
            <button class="danger" @click="openAction('coach', c.id, 'reject', `资质 #${c.id}`)">
              拒绝
            </button>
          </div>
        </article>
      </template>

      <!-- Workshop -->
      <template v-else-if="tab === 'workshop'">
        <EmptyState v-if="!workshops.length" title="暂无记录" />
        <article v-for="w in workshops" :key="w.id" class="card">
          <div class="head">
            <h3>{{ w.workshopName }}</h3>
            <span class="badge" :class="w.auditStatus === 'approved' ? 'ok' : w.auditStatus === 'rejected' ? 'bad' : 'warn'">
              {{ w.auditStatus === 'pending' ? '待审核' : w.auditStatus === 'approved' ? '已通过' : '已拒绝' }}
            </span>
          </div>
          <p class="meta">
            <span>¥{{ w.priceAmount }}</span>
            <span v-if="w.studioId">舞室 #{{ w.studioId }}</span>
            <span v-if="w.coachId">教练 #{{ w.coachId }}</span>
            <span>发布状态 {{ w.publishStatus }}</span>
          </p>
          <div v-if="w.auditStatus === 'pending'" class="actions">
            <button class="primary" @click="openAction('workshop', w.id, 'approve', w.workshopName)">
              通过
            </button>
            <button class="danger" @click="openAction('workshop', w.id, 'reject', w.workshopName)">
              拒绝
            </button>
          </div>
        </article>
      </template>

      <!-- 评价申诉 -->
      <template v-else>
        <EmptyState v-if="!appeals.length" title="暂无记录" />
        <article v-for="a in appeals" :key="a.id" class="card">
          <div class="head">
            <h3>评价 #{{ a.reviewId }} · 申诉人 #{{ a.appellantUserId }}</h3>
            <span class="badge" :class="a.appealStatus === 'approved' ? 'ok' : a.appealStatus === 'rejected' ? 'bad' : 'warn'">
              {{ a.appealStatus === 'pending' ? '待审核' : a.appealStatus === 'approved' ? '已通过' : '已拒绝' }}
            </span>
          </div>
          <p class="detail">{{ a.appealReason }}</p>
          <p v-if="a.evidenceNote" class="detail muted">证据:{{ a.evidenceNote }}</p>
          <p class="meta"><span>{{ fmt(a.createdAt) }}</span></p>
          <div v-if="a.appealStatus === 'pending'" class="actions">
            <button class="primary" @click="openAction('appeal', a.id, 'approve', `申诉 #${a.id}`)">
              通过申诉
            </button>
            <button class="danger" @click="openAction('appeal', a.id, 'reject', `申诉 #${a.id}`)">
              驳回
            </button>
          </div>
        </article>
      </template>
    </section>

    <!-- 审核备注弹层 -->
    <van-popup
      :show="Boolean(pendingAction)"
      position="bottom"
      round
      @update:show="pendingAction = null"
    >
      <div class="sheet">
        <h2>
          {{ pendingAction?.action === 'approve' ? '通过' : '拒绝' }} · {{ pendingAction?.title }}
        </h2>
        <textarea
          v-if="pendingAction?.kind !== 'workshop'"
          v-model="remark"
          maxlength="1000"
          placeholder="审核备注(选填,拒绝时建议填写原因)"
        />
        <button class="sheet-submit" :disabled="handling" @click="confirmAction">
          {{ handling ? '处理中…' : '确认' }}
        </button>
      </div>
    </van-popup>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.platform-page {
  @include ops-page;
}
.chips {
  @include ops-chip-row;
  &.sub {
    padding-top: 0;
    button {
      height: 34px;
      font-size: 12px;
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

.detail {
  margin: 10px 0 0;
  color: $pen-charcoal;
  font-size: 13px;
  line-height: 1.5;
  &.muted {
    color: $pen-mute;
    font-size: 12.5px;
  }
}

.license-link {
  display: inline-block;
  margin-top: 10px;
  color: $pen-ink;
  font-size: 12.5px;
  font-weight: 800;
  text-decoration: underline;
}

.sheet {
  padding: 24px 20px calc(24px + env(safe-area-inset-bottom));
  h2 {
    margin: 0 0 14px;
    font-size: 18px;
    font-weight: 900;
  }
  textarea {
    width: 100%;
    min-height: 88px;
    border: 1px solid $pen-hairline;
    border-radius: 16px;
    background: $pen-soft;
    padding: 12px 14px;
    font-size: 14px;
    font-family: inherit;
    outline: none;
    box-sizing: border-box;
    resize: vertical;
    &:focus {
      border-color: $pen-ink;
      background: $pen-canvas;
    }
  }
  .sheet-submit {
    @include pen-primary-btn;
    width: 100%;
    margin-top: 14px;
  }
}
</style>
