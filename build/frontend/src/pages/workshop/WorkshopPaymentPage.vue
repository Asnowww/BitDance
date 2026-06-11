<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { CheckCircle2, Circle, WalletCards } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  createWorkshopOrder,
  fetchWorkshopDetail,
  payWorkshopOrder,
  type WorkshopDetail,
  type WorkshopOrder
} from '@/api/workshop';

const route = useRoute();
const router = useRouter();

const workshopId = Number(route.params.id) || 1;
const selectedSessionId = computed(() => Number(route.query.sessionId) || null);
const payment = ref<'wechat' | 'alipay' | 'balance'>('wechat');
const workshop = ref<WorkshopDetail | null>(null);
const submitting = ref(false);
const loading = ref(false);

const methods = [
  { key: 'wechat', title: '微信支付' },
  { key: 'alipay', title: '支付宝' },
  { key: 'balance', title: '余额支付' }
] as const;

const session = computed(() =>
  workshop.value?.sessions.find((item) => item.id === selectedSessionId.value) ?? null
);
const amount = computed(() => session.value?.price ?? workshop.value?.priceMin ?? 0);
const isWorkshopEnded = computed(() => Boolean(workshop.value?.ended));
const isSignupClosed = computed(() => Boolean(workshop.value?.signupClosed));
const isSessionEnded = computed(() => Boolean(session.value?.ended));
const canPay = computed(
  () => Boolean(session.value) && !loading.value && !submitting.value && !isWorkshopEnded.value && !isSignupClosed.value && !isSessionEnded.value
);

const load = async () => {
  loading.value = true;
  try {
    const detail = await fetchWorkshopDetail(workshopId);
    workshop.value = detail;
    if (!selectedSessionId.value || !session.value) {
      showFailToast('请先从详情页选择场次');
      router.replace(`/workshop/${workshopId}`);
      return;
    }
    if (isWorkshopEnded.value || isSessionEnded.value) {
      showFailToast('活动已结束');
      router.replace(`/workshop/${workshopId}`);
      return;
    }
    if (isSignupClosed.value) {
      showFailToast('报名已截止');
      router.replace(`/workshop/${workshopId}`);
    }
  } finally {
    loading.value = false;
  }
};

const onPay = async () => {
  if (!selectedSessionId.value || !canPay.value) return;
  submitting.value = true;
  try {
    const order = await createWorkshopOrder({
      workshopId,
      sessionId: selectedSessionId.value,
      idempotencyToken: `workshop-${workshopId}-${selectedSessionId.value}-${Date.now()}`
    });
    let paidOrder: WorkshopOrder = order;
    if (order.status === 'UNPAID') {
      paidOrder = await payWorkshopOrder(order.id);
    }
    showSuccessToast('支付成功，签到码已生成');
    router.replace(`/workshop-checkin/${paidOrder.id}`);
  } catch (error) {
    const err = error as {
      response?: { data?: { message?: string } };
      message?: string;
    };
    showFailToast(err?.response?.data?.message || err?.message || '支付失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="订单确认" :show-share="false" />

    <section class="pen-scroll">
      <section class="order-card">
        <div class="order-card__cover">
          <WalletCards :size="34" :stroke-width="2" />
        </div>
        <div class="order-card__body">
          <p class="eyebrow">WORKSHOP 报名支付</p>
          <h1>{{ workshop?.title || '加载中…' }}</h1>
          <p class="muted">
            {{ session ? `${session.date} ${session.startTime}-${session.endTime}` : '请选择场次' }}
            · {{ workshop?.studioName || workshop?.area }}
          </p>
          <div class="price">¥{{ amount }}</div>
        </div>
      </section>

      <section class="panel">
        <div class="panel__head">
          <h2>支付方式</h2>
        </div>
        <button
          v-for="item in methods"
          :key="item.key"
          class="method"
          :class="{ 'method--active': payment === item.key }"
          type="button"
          @click="payment = item.key"
        >
          <component :is="payment === item.key ? CheckCircle2 : Circle" :size="18" :stroke-width="2" />
          <div class="method__copy">
            <strong>{{ item.title }}</strong>
          </div>
        </button>
      </section>
    </section>

    <footer class="pay-bar">
      <div class="pay-bar__copy">
        <strong>实付 ¥{{ amount }}</strong>
        <span>
          {{
            !session
              ? '等待选择场次'
              : isWorkshopEnded || isSessionEnded
                ? '活动已结束'
                : isSignupClosed
                  ? '报名已截止'
                  : `剩余 ${Math.max(0, session.capacity - session.taken)} 位`
          }}
        </span>
      </div>
      <button class="pay-bar__btn" type="button" :disabled="!canPay" @click="onPay">
        {{ submitting ? '支付中…' : '去支付' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;

  &--with-bar {
    padding-bottom: calc(86px + env(safe-area-inset-bottom));
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.order-card,
.panel {
  border-radius: 16px;
  overflow: hidden;
}

.order-card {
  background: $pen-soft;

  &__cover {
    height: 180px;
    background: $pen-ink;
    color: $pen-on-primary;
    display: grid;
    place-items: center;
  }

  &__body {
    padding: 16px;
    background: $pen-canvas;
  }
}

.eyebrow,
.muted,
.price,
.panel__head h2,
.panel__head span {
  margin: 0;
}

.eyebrow {
  color: $pen-mute;
  font-size: 11px;
  font-weight: 800;
  line-height: $pen-lh;
}

h1 {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.12;
}

.muted {
  margin-top: 8px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.45;
}

.price {
  margin-top: 16px;
  font-size: 30px;
  font-weight: 900;
  line-height: 1;
}

.panel {
  padding: 16px;
  border: 1px solid $pen-hairline;
  background: $pen-canvas;

  &--soft {
    background: $pen-soft;
  }

  &__head {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 10px;

    h2 {
      font-size: 18px;
      font-weight: 900;
      line-height: $pen-lh;
    }
  }
}

.method {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border: 0;
  border-top: 1px solid $pen-hairline;
  background: transparent;
  color: $pen-ink;
  text-align: left;
  cursor: pointer;

  &:first-of-type {
    border-top: 0;
  }

  &--active {
    color: $pen-ink;
  }

  &__copy {
    display: flex;
    flex-direction: column;
    min-width: 0;
    flex: 1;
  }

  strong {
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.pay-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid $pen-hairline;
  background: $pen-canvas;
  display: flex;
  align-items: center;
  gap: 12px;
  box-sizing: border-box;

  &__copy {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  strong {
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__btn {
    height: 46px;
    padding: 0 18px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}
</style>
