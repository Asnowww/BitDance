<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { useOpsStore } from '@/stores/ops';
import { fetchOpsDashboard, type OpsDashboard } from '@/api/coachOps';

const ops = useOpsStore();
const { activeRole, studioId } = storeToRefs(ops);
const data = ref<OpsDashboard | null>(null);
const loading = ref(true);

const money = (v?: number | null) => (v == null ? '—' : `¥${Number(v).toFixed(2)}`);

const attributionRules = computed(() => {
  if (activeRole.value === 'studio_admin') {
    return [
      { who: '舞室管理员 / 全职教练课程', rule: '收益归舞室' },
      { who: '签约教练', rule: '按合作协议比例分成' },
      { who: '自由教练 Workshop', rule: '收益归教练本人,不计入舞室' }
    ];
  }
  return [
    { who: '全职教练', rule: '课程与 Workshop 收益归舞室' },
    { who: '签约教练', rule: '按协议比例计入你的应归属收入' },
    { who: '自由教练', rule: 'Workshop 收益 100% 归你本人' }
  ];
});

onMounted(async () => {
  await ops.refresh();
  try {
    data.value = await fetchOpsDashboard({
      role: activeRole.value,
      studioId: activeRole.value === 'studio_admin' ? (studioId.value ?? undefined) : undefined
    });
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="settle-page">
    <PenTopBar title="收益统计" :show-share="false" />

    <section class="hero">
      <p>本月应归属收入(正式课 + Workshop)</p>
      <strong>{{ loading ? '…' : money(data?.monthIncome) }}</strong>
      <small>按归属规则计算 · 不含待支付与已退款订单</small>
    </section>

    <section class="grid">
      <article>
        <strong>{{ data?.monthOrderCount ?? '—' }}</strong>
        <span>本月订单</span>
      </article>
      <article>
        <strong>{{ data?.checkinCount ?? '—' }}</strong>
        <span>已核销</span>
      </article>
      <article>
        <strong>{{ data?.refundCount ?? '—' }}</strong>
        <span>退款</span>
      </article>
      <article>
        <strong>{{ data?.courseBookingCount ?? '—' }}</strong>
        <span>课程预约</span>
      </article>
      <article>
        <strong>{{ data?.workshopSignupCount ?? '—' }}</strong>
        <span>Workshop 报名</span>
      </article>
      <article>
        <strong>{{ data?.avgRating != null ? Number(data.avgRating).toFixed(1) : '—' }}</strong>
        <span>平均评分</span>
      </article>
    </section>

    <section class="rules">
      <h2>归属规则</h2>
      <div v-for="r in attributionRules" :key="r.who" class="rule-row">
        <strong>{{ r.who }}</strong>
        <span>{{ r.rule }}</span>
      </div>
      <p class="note">
        当前阶段仅展示收益统计,不生成结算单、不支持提现。结算与提现能力将在后续版本开放。
      </p>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.settle-page {
  @include ops-page;
}

.hero {
  margin: 16px 18px 0;
  border-radius: 28px;
  background: $pen-ink;
  color: #fff;
  padding: 26px 22px;
  p {
    margin: 0 0 10px;
    color: $pen-hairline-strong;
    font-size: 12px;
    font-weight: 800;
  }
  strong {
    display: block;
    font-size: 40px;
    font-weight: 900;
    line-height: 1;
  }
  small {
    display: block;
    margin-top: 10px;
    color: #9e9ea0;
    font-size: 11px;
    font-weight: 700;
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  margin: 14px 18px 0;
  border-radius: 22px;
  overflow: hidden;
  background: $pen-hairline;
  article {
    background: $pen-soft;
    padding: 16px 8px;
    text-align: center;
    strong {
      display: block;
      font-size: 18px;
      font-weight: 900;
    }
    span {
      margin-top: 4px;
      display: block;
      color: $pen-mute;
      font-size: 11px;
      font-weight: 800;
    }
  }
}

.rules {
  margin: 26px 18px 0;
  h2 {
    @include pen-h3-section;
    margin-bottom: 12px;
  }
}

.rule-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid $pen-hairline;
  strong {
    font-size: 13.5px;
    font-weight: 900;
  }
  span {
    color: $pen-mute;
    font-size: 12.5px;
    font-weight: 600;
    text-align: right;
  }
}

.note {
  margin: 14px 0 0;
  color: $pen-mute;
  font-size: 12px;
  line-height: 1.6;
}
</style>
