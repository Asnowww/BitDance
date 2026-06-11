<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  submitCertification,
  fetchMyCertifications,
  type CoachCertification
} from '@/api/coachOps';
import { useOpsStore } from '@/stores/ops';

const ops = useOpsStore();
const certs = ref<CoachCertification[]>([]);
const loading = ref(true);
const submitting = ref(false);

const form = ref({
  applicationType: 'independent' as 'independent' | 'studio_affiliated',
  coachType: 'freelance' as 'full_time' | 'signed' | 'freelance',
  remark: ''
});

const coachTypes = [
  { key: 'freelance', label: '自由教练', desc: '独立发布 Workshop,收益归本人' },
  { key: 'signed', label: '签约教练', desc: '与舞室签约,按协议分成' },
  { key: 'full_time', label: '全职教练', desc: '舞室全职,收益归舞室' }
] as const;

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending: { label: '审核中', cls: 'warn' },
  approved: { label: '已通过', cls: 'ok' },
  rejected: { label: '已拒绝', cls: 'bad' }
};

const hasPending = computed(() => certs.value.some((c) => c.applicationStatus === 'pending'));
const hasApproved = computed(() => certs.value.some((c) => c.applicationStatus === 'approved'));

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  loading.value = true;
  try {
    certs.value = await fetchMyCertifications();
  } finally {
    loading.value = false;
  }
};

const submit = async () => {
  if (submitting.value) return;
  submitting.value = true;
  try {
    await submitCertification({
      applicationType: form.value.coachType === 'freelance' ? 'independent' : 'studio_affiliated',
      coachType: form.value.coachType,
      remark: form.value.remark.trim() || undefined
    });
    showSuccessToast('已提交,等待平台审核');
    form.value.remark = '';
    await load();
    await ops.refresh(true);
  } finally {
    submitting.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="cert-page">
    <PenTopBar title="教练资质" :show-share="false" />

    <section class="body form">
      <p v-if="loading" class="loading">加载中…</p>

      <template v-else>
        <div v-if="hasApproved" class="approved-banner">
          已获得教练身份,可在工作台管理课程与 Workshop。
        </div>

        <template v-if="certs.length">
          <p class="form-section">申请记录</p>
          <article v-for="c in certs" :key="c.id" class="card">
            <div class="head">
              <h3>
                {{ c.coachType === 'freelance' ? '自由教练' : c.coachType === 'signed' ? '签约教练' : '全职教练' }}资质
              </h3>
              <span class="badge" :class="statusMeta[c.applicationStatus]?.cls">
                {{ statusMeta[c.applicationStatus]?.label ?? c.applicationStatus }}
              </span>
            </div>
            <p class="meta">
              <span>提交于 {{ fmt(c.createdAt) }}</span>
              <span v-if="c.reviewedAt">审核于 {{ fmt(c.reviewedAt) }}</span>
            </p>
            <p v-if="c.applicationStatus === 'rejected' && c.reviewRemark" class="reject">
              拒绝原因:{{ c.reviewRemark }}
            </p>
          </article>
        </template>

        <template v-if="!hasPending">
          <p class="form-section">{{ certs.length ? '重新提交' : '提交资质申请' }}</p>
          <div class="field">
            <label>教练类型</label>
            <div class="type-list">
              <button
                v-for="t in coachTypes"
                :key="t.key"
                :class="{ active: form.coachType === t.key }"
                @click="form.coachType = t.key"
              >
                <strong>{{ t.label }}</strong>
                <small>{{ t.desc }}</small>
              </button>
            </div>
          </div>
          <div class="field">
            <label>资质说明</label>
            <textarea
              v-model="form.remark"
              maxlength="2000"
              placeholder="舞龄、擅长舞种、教学经历、获奖经历、相关证书说明…"
            />
          </div>
          <p class="hint-text">
            全职/签约教练也可由舞室管理员直接邀请绑定;自由教练资质通过后即可独立发布 Workshop。
          </p>
        </template>
        <p v-else class="hint-text">已有审核中的申请,请等待平台处理。</p>
      </template>
    </section>

    <footer v-if="!loading && !hasPending" class="submit-bar">
      <button :disabled="submitting" @click="submit">
        {{ submitting ? '提交中…' : '提交平台审核' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.cert-page {
  @include ops-page;
}
.body {
  @include ops-body;
  @include ops-form;
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
.submit-bar {
  @include ops-submit-bar;
}

.approved-banner {
  margin-top: 4px;
  border-radius: 18px;
  background: rgba(0, 125, 72, 0.08);
  color: $pen-success;
  padding: 14px 16px;
  font-size: 13.5px;
  font-weight: 800;
  line-height: 1.5;
}

.reject {
  margin: 10px 0 0;
  color: #d30005;
  font-size: 13px;
  font-weight: 700;
}

.type-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  button {
    display: flex;
    flex-direction: column;
    gap: 3px;
    border: 1px solid $pen-hairline;
    border-radius: 18px;
    background: $pen-canvas;
    padding: 13px 16px;
    text-align: left;
    cursor: pointer;
    strong {
      font-size: 14.5px;
      font-weight: 900;
      color: $pen-ink;
    }
    small {
      font-size: 12px;
      color: $pen-mute;
    }
    &.active {
      border-color: $pen-ink;
      background: $pen-soft;
    }
  }
}

.hint-text {
  margin: 4px 2px 0;
  color: $pen-mute;
  font-size: 12px;
  line-height: 1.6;
}
</style>
