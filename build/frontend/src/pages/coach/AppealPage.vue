<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { createReviewAppeal, fetchMyAppeals, type ReviewAppeal } from '@/api/coachOps';

const route = useRoute();
const appeals = ref<ReviewAppeal[]>([]);
const loading = ref(true);
const submitting = ref(false);

const reasons = ['与事实不符', '恶意差评', '同行攻击', '泄露隐私', '其他'];

const form = ref({
  reviewId: (route.query.reviewId as string) ?? '',
  reasonType: '与事实不符',
  detail: '',
  evidenceNote: ''
});

const statusMeta: Record<string, { label: string; cls: string }> = {
  pending: { label: '平台审核中', cls: 'warn' },
  approved: { label: '申诉成功', cls: 'ok' },
  rejected: { label: '申诉驳回', cls: 'bad' }
};

const ready = computed(
  () => form.value.reviewId !== '' && (form.value.reasonType + ':' + form.value.detail).length >= 5
);

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const load = async () => {
  loading.value = true;
  try {
    appeals.value = await fetchMyAppeals();
  } finally {
    loading.value = false;
  }
};

const submit = async () => {
  if (!ready.value || submitting.value) return;
  submitting.value = true;
  try {
    await createReviewAppeal({
      reviewId: Number(form.value.reviewId),
      appealReason: `${form.value.reasonType}:${form.value.detail.trim()}`,
      evidenceNote: form.value.evidenceNote.trim() || undefined
    });
    showSuccessToast('申诉已提交,等待平台审核');
    form.value.detail = '';
    form.value.evidenceNote = '';
    load();
  } finally {
    submitting.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="appeal-page">
    <PenTopBar title="评价申诉" :show-share="false" />

    <section class="body form">
      <p class="notice">商家不可直接删除评价,可对违规评价发起申诉,由平台审核处理。</p>

      <p class="form-section">发起申诉</p>
      <div class="field">
        <label>评价 ID <em>*</em></label>
        <input v-model="form.reviewId" type="number" placeholder="从评价回复页进入会自动填入" />
      </div>
      <div class="field">
        <label>申诉理由 <em>*</em></label>
        <div class="seg">
          <button
            v-for="r in reasons"
            :key="r"
            :class="{ active: form.reasonType === r }"
            @click="form.reasonType = r"
          >
            {{ r }}
          </button>
        </div>
      </div>
      <div class="field">
        <label>详细说明 <em>*</em></label>
        <textarea v-model="form.detail" maxlength="1900" placeholder="说明评价与事实不符之处…" />
      </div>
      <div class="field">
        <label>证据说明</label>
        <textarea v-model="form.evidenceNote" maxlength="2000" placeholder="可补充监控、订单、聊天记录等证据说明(选填)" />
      </div>

      <p class="form-section">申诉记录</p>
      <p v-if="loading" class="loading">加载中…</p>
      <EmptyState v-else-if="!appeals.length" title="暂无申诉记录" />
      <article v-for="a in appeals" :key="a.id" class="card">
        <div class="head">
          <h3>评价 #{{ a.reviewId }}</h3>
          <span class="badge" :class="statusMeta[a.appealStatus]?.cls">
            {{ statusMeta[a.appealStatus]?.label ?? a.appealStatus }}
          </span>
        </div>
        <p class="reason">{{ a.appealReason }}</p>
        <p class="meta">
          <span>提交于 {{ fmt(a.createdAt) }}</span>
          <span v-if="a.reviewedAt">处理于 {{ fmt(a.reviewedAt) }}</span>
        </p>
        <p v-if="a.reviewRemark" class="remark">平台备注:{{ a.reviewRemark }}</p>
      </article>
    </section>

    <footer class="submit-bar">
      <button :disabled="submitting || !ready" @click="submit">
        {{ submitting ? '提交中…' : '提交申诉' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.appeal-page {
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

.notice {
  margin: 4px 0 0;
  border-radius: 16px;
  background: $pen-soft;
  padding: 12px 14px;
  color: $pen-charcoal;
  font-size: 12.5px;
  font-weight: 700;
  line-height: 1.5;
}

.reason {
  margin: 10px 0 0;
  color: $pen-charcoal;
  font-size: 13px;
  line-height: 1.5;
}

.remark {
  margin: 8px 0 0;
  color: $pen-mute;
  font-size: 12.5px;
}
</style>
