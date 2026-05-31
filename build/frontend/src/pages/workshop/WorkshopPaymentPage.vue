<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';

const route = useRoute();
const router = useRouter();

const workshopId = computed(() => Number(route.params.id || 1));
const payment = ref<'wechat' | 'alipay' | 'balance'>('wechat');
const submitting = ref(false);

const methods = [
  { key: 'wechat', title: '微信支付', desc: '推荐使用，支付后自动生成二维码' },
  { key: 'alipay', title: '支付宝', desc: '使用支付宝完成活动报名' },
  { key: 'balance', title: '余额抵扣', desc: '使用账户余额抵扣本次报名' }
] as const;

const onPay = () => {
  submitting.value = true;
  window.setTimeout(() => {
    submitting.value = false;
    showSuccessToast('报名成功，活动二维码已生成');
    router.push('/me/workshop-orders');
  }, 500);
};
</script>

<template>
  <main class="payment-page">
    <header class="topbar">
      <button class="icon-btn" aria-label="返回" @click="router.back()">‹</button>
      <div>
        <p class="eyebrow">WORKSHOP</p>
        <h1>订单确认</h1>
      </div>
      <span class="order-id">#{{ workshopId }}</span>
    </header>

    <section class="order-card">
      <div class="order-card__media">
        <span>LOCKING</span>
      </div>
      <div class="order-card__body">
        <p class="section-label">Workshop 报名</p>
        <h2>Locking 大师课</h2>
        <p class="muted">5/30 14:00 · 1 人 · Joy Studio</p>
        <div class="price">¥199</div>
      </div>
    </section>

    <section class="panel">
      <div class="panel__head">
        <h2>支付方式</h2>
        <span>3 OPTIONS</span>
      </div>
      <button
        v-for="item in methods"
        :key="item.key"
        class="pay-row"
        :class="{ active: payment === item.key }"
        @click="payment = item.key"
      >
        <span class="pay-row__mark">{{ payment === item.key ? '✓' : '' }}</span>
        <span class="pay-row__text">
          <strong>{{ item.title }}</strong>
          <em>{{ item.desc }}</em>
        </span>
        <span v-if="payment === item.key" class="selected">已选</span>
      </button>
    </section>

    <section class="qr-note">
      <div class="qr-note__icon">QR</div>
      <div>
        <strong>报名成功后生成活动二维码</strong>
        <p>到场时出示二维码即可完成签到核销。</p>
      </div>
    </section>

    <footer class="sticky-action">
      <button class="primary-btn" :disabled="submitting" @click="onPay">
        {{ submitting ? '支付中' : '确认支付' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
.payment-page {
  min-height: 100vh;
  padding: 18px 18px 96px;
  background: #fff;
  color: #111;
}

.topbar {
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 12px;
  h1 {
    margin: 0;
    font-size: 28px;
    line-height: 1;
    font-weight: 900;
  }
}

.eyebrow,
.section-label {
  margin: 0 0 4px;
  font-size: 11px;
  font-weight: 800;
  color: #707072;
  letter-spacing: 0;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: #f5f5f5;
  color: #111;
  font-size: 30px;
  line-height: 1;
}

.order-id {
  padding: 8px 12px;
  border-radius: 999px;
  background: #111;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.order-card {
  margin-top: 24px;
  overflow: hidden;
  border-radius: 28px;
  background: #f5f5f5;
  &__media {
    height: 220px;
    display: flex;
    align-items: flex-end;
    padding: 22px;
    background:
      linear-gradient(135deg, rgba(17, 17, 17, 0.05), rgba(17, 17, 17, 0.62)),
      radial-gradient(circle at 72% 18%, #cacacb 0 18%, transparent 19%),
      linear-gradient(135deg, #e5e5e5, #9e9ea0);
    span {
      color: #fff;
      font-size: 44px;
      line-height: 0.9;
      font-weight: 900;
    }
  }
  &__body {
    padding: 18px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-top: 0;
    border-radius: 0 0 28px 28px;
    h2 {
      margin: 0;
      font-size: 24px;
      line-height: 1.15;
      font-weight: 900;
    }
  }
}

.muted {
  margin: 8px 0 0;
  color: #707072;
  font-size: 13px;
}

.price {
  margin-top: 18px;
  font-size: 32px;
  font-weight: 900;
}

.panel {
  margin-top: 24px;
  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 900;
    }
    span {
      color: #707072;
      font-size: 11px;
      font-weight: 800;
    }
  }
}

.pay-row {
  width: 100%;
  min-height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 24px;
  background: #f5f5f5;
  color: #111;
  text-align: left;
  &.active {
    background: #111;
    color: #fff;
    border-color: #111;
    .pay-row__text em {
      color: #cacacb;
    }
  }
  &__mark {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    display: grid;
    place-items: center;
    flex: 0 0 auto;
    background: #fff;
    color: #111;
    font-weight: 900;
  }
  &__text {
    flex: 1;
    display: grid;
    gap: 3px;
    strong {
      font-size: 15px;
    }
    em {
      color: #707072;
      font-size: 12px;
      font-style: normal;
    }
  }
}

.selected {
  font-size: 12px;
  font-weight: 800;
}

.qr-note {
  display: flex;
  gap: 12px;
  margin-top: 22px;
  padding: 16px;
  border-radius: 24px;
  background: #f5f5f5;
  p {
    margin: 4px 0 0;
    color: #707072;
    font-size: 12px;
  }
  &__icon {
    width: 46px;
    height: 46px;
    border-radius: 16px;
    display: grid;
    place-items: center;
    background: #111;
    color: #fff;
    font-size: 13px;
    font-weight: 900;
  }
}

.sticky-action {
  position: fixed;
  left: 50%;
  bottom: 0;
  width: 100%;
  max-width: 480px;
  transform: translateX(-50%);
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.94);
  border-top: 1px solid #e5e5e5;
}

.primary-btn {
  width: 100%;
  height: 56px;
  border: 0;
  border-radius: 999px;
  background: #111;
  color: #fff;
  font-size: 17px;
  font-weight: 900;
  &:disabled {
    opacity: 0.65;
  }
}
</style>
