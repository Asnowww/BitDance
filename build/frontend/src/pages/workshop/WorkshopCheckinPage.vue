<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { fetchMyWorkshopOrders, checkinWorkshopOrder, type WorkshopOrder } from '@/api/workshop';

const route = useRoute();
const router = useRouter();
const orderId = computed(() => Number(route.params.id));

const order = ref<WorkshopOrder | null>(null);
const code = ref('');
const submitting = ref(false);

onMounted(async () => {
  const list = await fetchMyWorkshopOrders();
  order.value = list.find((it) => it.id === orderId.value) ?? null;
});

const onSimulateScan = () => {
  if (order.value) {
    code.value = order.value.checkinCode;
    showSuccessToast('已模拟扫码，自动填入签到码');
  }
};

const onSubmit = async () => {
  if (!order.value) return;
  submitting.value = true;
  try {
    const result = await checkinWorkshopOrder(orderId.value, code.value.trim());
    if (result && result.status === 'CHECKED_IN') {
      showSuccessToast('签到成功');
      router.replace('/me/workshop-orders');
    } else {
      showFailToast('签到码错误');
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">扫码签到</span>
    </header>
    <section v-if="order" class="info">
      <div class="info__title">{{ order.workshopTitle }}</div>
      <div class="info__meta">{{ order.sessionDate }} {{ order.sessionTime }}</div>
    </section>
    <section v-else class="empty">订单不存在</section>
    <section class="form">
      <div class="scan">
        <div class="scan__box">
          <div class="scan__line" />
        </div>
        <button class="scan__btn" @click="onSimulateScan">模拟扫描签到码</button>
        <p class="scan__tip">真实环境调用相机扫码或长按 NFC；mock 阶段点击按钮自动填充。</p>
      </div>
      <div class="manual">
        <label class="manual__label">手动输入</label>
        <input v-model="code" class="manual__input" placeholder="BD-XXXXXX" />
      </div>
    </section>
    <footer class="footer">
      <button class="btn" :disabled="!code.trim() || submitting" @click="onSubmit">
        {{ submitting ? '提交中…' : '完成签到' }}
      </button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
}
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.info {
  background: #fff;
  padding: 16px;
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
  &__meta {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
.empty {
  padding: 40px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.form {
  padding: 16px;
}
.scan {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  &__box {
    width: 200px;
    height: 200px;
    margin: 0 auto;
    border: 2px solid var(--bd-primary);
    border-radius: 16px;
    position: relative;
    overflow: hidden;
  }
  &__line {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 2px;
    background: var(--bd-primary);
    box-shadow: 0 0 8px var(--bd-primary);
    animation: scan 2s ease-in-out infinite alternate;
  }
  &__btn {
    margin-top: 16px;
    padding: 8px 16px;
    border: 1px solid var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
    border-radius: 999px;
    font-size: 13px;
    cursor: pointer;
  }
  &__tip {
    margin-top: 12px;
    font-size: 11px;
    color: var(--bd-text-secondary);
    line-height: 1.6;
  }
}
@keyframes scan {
  0% {
    top: 0;
  }
  100% {
    top: 100%;
  }
}
.manual {
  margin-top: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  &__label {
    display: block;
    font-size: 12px;
    color: var(--bd-text-secondary);
    margin-bottom: 6px;
  }
  &__input {
    width: 100%;
    height: 38px;
    padding: 0 12px;
    border: 1px solid var(--bd-border);
    border-radius: 8px;
    font-size: 14px;
    font-family: monospace;
    outline: none;
    &:focus {
      border-color: var(--bd-primary);
    }
  }
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
}
.btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  background: var(--bd-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
</style>
