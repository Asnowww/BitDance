<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import QRCode from 'qrcode';
import { showToast } from 'vant';
import { Copy, QrCode, ScanLine } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchMyWorkshopOrder, fetchWorkshopDetail, type WorkshopDetail, type WorkshopOrder } from '@/api/workshop';

const route = useRoute();
const router = useRouter();

const orderId = Number(route.params.id) || 0;
const order = ref<WorkshopOrder | null>(null);
const workshop = ref<WorkshopDetail | null>(null);
const loading = ref(false);
const qrCodeUrl = ref('');

const load = async () => {
  loading.value = true;
  try {
    const currentOrder = await fetchMyWorkshopOrder(orderId);
    order.value = currentOrder;
    workshop.value = await fetchWorkshopDetail(currentOrder.workshopId);
  } finally {
    loading.value = false;
  }
};

const session = computed(() =>
  workshop.value?.sessions.find((item) => item.id === (order.value?.workshopSessionId ?? order.value?.sessionId)) ?? null
);
const qrPayload = computed(() => {
  if (!order.value?.checkinCode) return '';
  return JSON.stringify({
    scene: 'bitdance_workshop_checkin',
    orderId: order.value.id,
    workshopId: order.value.workshopId,
    code: order.value.checkinCode
  });
});

const copyCode = async () => {
  if (!order.value?.checkinCode) return;
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(order.value.checkinCode);
  }
  showToast('签到码已复制');
};

const buildQrCode = async () => {
  if (!qrPayload.value) {
    qrCodeUrl.value = '';
    return;
  }
  qrCodeUrl.value = await QRCode.toDataURL(qrPayload.value, {
    margin: 1,
    width: 240,
    color: {
      dark: '#111111',
      light: '#ffffff'
    }
  });
};

onMounted(load);
watch(qrPayload, () => {
  void buildQrCode();
});
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="签到凭证" :show-share="false" />

    <section class="pen-scroll">
      <section class="hero-card">
        <QrCode :size="42" :stroke-width="2" />
        <strong>{{ workshop?.title || 'Workshop 签到' }}</strong>
        <p>{{ session ? `${session.date} ${session.startTime}-${session.endTime}` : '加载中…' }}</p>
      </section>

      <section class="code-card">
        <div class="code-card__qr">
          <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="签到二维码" />
          <ScanLine v-else :size="92" :stroke-width="1.8" />
        </div>
        <strong>{{ order?.checkinCode || '签到码生成中' }}</strong>
        <p>到场后请向工作人员出示二维码，或由工作人员录入你的签到码完成核销。</p>
        <button class="ghost-btn" type="button" :disabled="!order?.checkinCode" @click="copyCode">
          <Copy :size="16" :stroke-width="2" />
          <span>复制签到码</span>
        </button>
      </section>

      <section class="info-card">
        <h2>核销说明</h2>
        <ul>
          <li>用户端只负责展示签到二维码和签到码，不会自己完成签到。</li>
          <li>商家端需扫描二维码或手动录入签到码后，订单才会变为已签到。</li>
          <li>若当前环境是本地联调，可在商家工作台的“学员订单与核销”页测试扫码核销。</li>
        </ul>
      </section>

      <button class="text-btn" type="button" @click="router.push('/me/workshop-orders')">返回我的订单</button>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.hero-card,
.code-card,
.info-card {
  border-radius: 16px;
  padding: 18px;
}

.hero-card {
  background: $pen-ink;
  color: $pen-on-primary;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;

  strong,
  p {
    margin: 0;
  }

  strong {
    font-size: 24px;
    font-weight: 900;
    line-height: 1.1;
  }

  p {
    color: rgba(255, 255, 255, 0.76);
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.code-card {
  background: $pen-soft;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;

  &__qr {
    width: 196px;
    height: 196px;
    border-radius: 20px;
    background: $pen-canvas;
    display: grid;
    place-items: center;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      display: block;
    }
  }

  strong,
  p {
    margin: 0;
  }

  strong {
    font-size: 28px;
    font-weight: 900;
    letter-spacing: 1px;
    line-height: 1;
  }

  p {
    color: $pen-mute;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.45;
  }
}

.info-card {
  background: $pen-canvas;
  border: 1px solid $pen-hairline;

  h2 {
    margin: 0 0 10px;
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  ul {
    margin: 0;
    padding-left: 18px;
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.45;
  }
}

.ghost-btn,
.text-btn {
  border: 0;
  cursor: pointer;
}

.ghost-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 800;
}

.text-btn {
  background: transparent;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
