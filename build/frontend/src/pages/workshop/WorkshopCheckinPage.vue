<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showToast } from 'vant';
import { QrCode, ChevronRight } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();

const payMethods = ['微信支付', '支付宝', '余额抵扣'];
const selectedPay = ref('微信支付');

const onConfirm = () => {
  showSuccessToast('支付成功，报名完成');
  router.push('/me/workshop-orders');
};
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="订单确认" @share="showToast('订单链接已复制')" />

    <section class="pen-scroll">
      <h2 class="title">Workshop 报名</h2>

      <section class="order">
        <strong class="order__name">Locking 大师课</strong>
        <p class="order__meta">5/30 14:00 · 1 人 · Joy Studio</p>
        <strong class="order__price">¥199</strong>
      </section>

      <section class="pay">
        <h3 class="pay__title">支付方式</h3>
        <button
          v-for="method in payMethods"
          :key="method"
          type="button"
          class="pay-row"
          @click="selectedPay = method"
        >
          <span class="pay-row__label">{{ method }}</span>
          <em v-if="selectedPay === method" class="pay-row__state">已选</em>
          <ChevronRight class="pay-row__chevron" :size="18" :stroke-width="2" />
        </button>
      </section>

      <section class="qr">
        <QrCode :size="96" :stroke-width="2" />
        <p class="qr__tip">报名成功后生成活动二维码</p>
      </section>

      <button type="button" class="confirm" @click="onConfirm">确认支付</button>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.title {
  margin: 0;
  font-size: 26px;
  font-weight: 900;
  line-height: $pen-lh;
}

.order {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-soft;

  &__name {
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__price {
    font-size: 28px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}

.pay {
  display: flex;
  flex-direction: column;

  &__title {
    @include pen-h3-section;
    padding: 8px 0;
  }
}

.pay-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 16px 0;
  border: 0;
  border-bottom: 1px solid $pen-hairline;
  background: $pen-canvas;
  cursor: pointer;
  text-align: left;

  &__label {
    flex: 1;
    color: $pen-ink;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__state {
    color: $pen-mute;
    font-size: 14px;
    font-style: normal;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__chevron {
    flex-shrink: 0;
    color: $pen-mute;
  }
}

.qr {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 220px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-ink;
  color: $pen-on-primary;

  &__tip {
    margin: 0;
    font-size: 14px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.confirm {
  height: 48px;
  border: 0;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 15px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;
}
</style>
