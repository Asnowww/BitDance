<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { showSuccessToast, showToast } from 'vant';
import { CheckCircle2, ChevronLeft, RefreshCcw, ShieldAlert, XCircle } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import {
  closeReportTicket,
  fetchReportTickets,
  processReportTicket,
  rejectReportTicket
} from '@/api/admin';
import type { ReportTicket } from '@/api/admin';

const router = useRouter();
const tabs = [
  { value: 'pending', label: '待处理' },
  { value: 'processing', label: '处理中' },
  { value: 'closed', label: '已关闭' },
  { value: 'rejected', label: '已驳回' }
];

const activeStatus = ref('pending');
const tickets = ref<ReportTicket[]>([]);
const total = ref(0);
const loading = ref(false);
const operatingId = ref<number | null>(null);

const statusLabel = (value: string) => tabs.find((tab) => tab.value === value)?.label ?? value;
const targetLabel = (ticket: ReportTicket) => `${ticket.targetType} #${ticket.targetId}`;
const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '-');

const loadTickets = async () => {
  loading.value = true;
  try {
    const data = await fetchReportTickets(activeStatus.value);
    tickets.value = data.content ?? [];
    total.value = data.totalElements ?? tickets.value.length;
  } finally {
    loading.value = false;
  }
};

const switchStatus = async (status: string) => {
  activeStatus.value = status;
  await loadTickets();
};

const replaceTicket = (next: ReportTicket) => {
  tickets.value = tickets.value.map((item) => (item.id === next.id ? next : item));
};

const operate = async (ticket: ReportTicket, action: 'process' | 'close' | 'reject') => {
  operatingId.value = ticket.id;
  try {
    if (action === 'process') {
      replaceTicket(await processReportTicket(ticket.id));
      showSuccessToast('已受理举报');
      return;
    }
    if (action === 'close') {
      replaceTicket(await closeReportTicket(ticket.id, '已核实违规，关闭并记录处理结果'));
      showSuccessToast('举报已关闭');
      return;
    }
    replaceTicket(await rejectReportTicket(ticket.id, '证据不足，暂不处理'));
    showSuccessToast('举报已驳回');
  } catch (error) {
    showToast('操作失败，请检查账号权限');
  } finally {
    operatingId.value = null;
    await loadTickets();
  }
};

onMounted(loadTickets);
</script>

<template>
  <main class="admin-page">
    <header class="admin-topbar">
      <button type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="22" />
      </button>
      <div>
        <h1>举报后台</h1>
        <p>平台管理员处理社区举报与封禁线索</p>
      </div>
      <button type="button" aria-label="刷新举报" @click="loadTickets">
        <RefreshCcw :size="20" />
      </button>
    </header>

    <section class="admin-scroll">
      <nav class="tabs" aria-label="举报状态">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          :class="{ 'tabs__item--active': activeStatus === tab.value }"
          class="tabs__item"
          @click="switchStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </nav>

      <section class="summary">
        <ShieldAlert :size="22" />
        <span>
          <strong>{{ statusLabel(activeStatus) }}</strong>
          <em>{{ loading ? '读取中...' : `${total} 条记录来自后端` }}</em>
        </span>
      </section>

      <article v-for="ticket in tickets" :key="ticket.id" class="ticket-card">
        <header>
          <span>
            <strong>{{ targetLabel(ticket) }}</strong>
            <em>#{{ ticket.id }} · {{ statusLabel(ticket.reportStatus) }} · {{ formatTime(ticket.createdAt) }}</em>
          </span>
          <b>{{ ticket.reasonCode }}</b>
        </header>
        <p>{{ ticket.reasonDetail || '举报人未补充说明' }}</p>
        <footer>
          <button
            v-if="ticket.reportStatus === 'pending'"
            type="button"
            :disabled="operatingId === ticket.id"
            @click="operate(ticket, 'process')"
          >
            <ShieldAlert :size="16" />
            <span>受理</span>
          </button>
          <button
            v-if="ticket.reportStatus === 'pending' || ticket.reportStatus === 'processing'"
            type="button"
            :disabled="operatingId === ticket.id"
            @click="operate(ticket, 'close')"
          >
            <CheckCircle2 :size="16" />
            <span>关闭</span>
          </button>
          <button
            v-if="ticket.reportStatus === 'pending' || ticket.reportStatus === 'processing'"
            type="button"
            :disabled="operatingId === ticket.id"
            class="ticket-card__danger"
            @click="operate(ticket, 'reject')"
          >
            <XCircle :size="16" />
            <span>驳回</span>
          </button>
          <small v-if="ticket.handleResult">{{ ticket.handleResult }}</small>
        </footer>
      </article>

      <p v-if="!loading && !tickets.length" class="empty-state">当前状态没有举报记录</p>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.admin-page {
  @include pen-page;
  min-height: 100%;
}

.admin-topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 76px;
  padding: 12px 18px;
  border-bottom: 1px solid $pen-hairline;
  background: $pen-canvas;

  button {
    display: grid;
    width: 40px;
    height: 40px;
    flex: none;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    cursor: pointer;
    place-items: center;
  }

  div {
    min-width: 0;
    flex: 1;
  }

  h1,
  p {
    margin: 0;
  }

  h1 {
    font-size: 20px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    margin-top: 3px;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.admin-scroll {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 18px calc(24px + env(safe-area-inset-bottom));
}

.tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;

  &__item {
    min-height: 36px;
    padding: 7px 13px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
    white-space: nowrap;
    cursor: pointer;

    &--active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }
}

.summary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  background: $pen-soft;

  span {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    gap: 3px;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  em {
    color: $pen-mute;
    font-size: 12px;
    font-style: normal;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.ticket-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-canvas;

  header {
    display: flex;
    align-items: flex-start;
    gap: 8px;
  }

  span {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    gap: 4px;
  }

  strong,
  em {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  em {
    color: $pen-mute;
    font-size: 12px;
    font-style: normal;
    font-weight: 800;
    line-height: $pen-lh;
  }

  b {
    flex: none;
    padding: 5px 9px;
    border-radius: 999px;
    background: $pen-soft;
    font-size: 11px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p {
    margin: 0;
    color: $pen-charcoal;
    font-size: 13px;
    font-weight: 800;
    line-height: 1.45;
  }

  footer {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;

    button {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      min-height: 34px;
      padding: 0 12px;
      border: 0;
      border-radius: 999px;
      background: $pen-ink;
      color: $pen-on-primary;
      font-size: 12px;
      font-weight: 900;
      cursor: pointer;

      &:disabled {
        opacity: 0.55;
      }
    }

    small {
      min-width: 0;
      flex: 1;
      color: $pen-success;
      font-size: 12px;
      font-weight: 900;
      line-height: $pen-lh;
    }
  }

  &__danger {
    background: #d30005 !important;
  }
}

.empty-state {
  margin: 0;
  padding: 18px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 900;
  line-height: $pen-lh;
  text-align: center;
}
</style>
