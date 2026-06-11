<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';
import { Camera, ScanLine, X } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { checkinByCoach, fetchCoachOrders, type CoachWorkshopOrderRow } from '@/api/coachOps';

interface BarcodeDetectorLike {
  detect(source: ImageBitmapSource): Promise<Array<{ rawValue?: string }>>;
}

interface BarcodeDetectorCtor {
  new (options?: { formats?: string[] }): BarcodeDetectorLike;
  getSupportedFormats?: () => Promise<string[]>;
}

const cats = ['待核销', '已核销', '已退款'] as const;
const activeCat = ref<(typeof cats)[number]>('待核销');
const orders = ref<CoachWorkshopOrderRow[]>([]);
const loading = ref(false);
const scannerVisible = ref(false);
const scannerLoading = ref(false);
const scannerError = ref('');
const manualOrderId = ref<number | null>(null);
const manualCode = ref('');
const previewRef = ref<HTMLVideoElement | null>(null);
let scannerStream: MediaStream | null = null;
let scannerTimer: number | null = null;
let barcodeDetector: BarcodeDetectorLike | null = null;

const normalizedStatus = (status: string) => status.toUpperCase();
const filteredOrders = computed(() =>
  orders.value.filter((item) => {
    const status = normalizedStatus(item.status);
    if (activeCat.value === '待核销') return status === 'PAID';
    if (activeCat.value === '已核销') return status === 'CHECKED_IN' || status === 'COMPLETED';
    return status === 'REFUNDED' || status === 'CANCELED';
  })
);

const load = async () => {
  loading.value = true;
  try {
    orders.value = await fetchCoachOrders();
  } finally {
    loading.value = false;
  }
};

const stopScanner = () => {
  if (scannerTimer !== null) {
    window.clearTimeout(scannerTimer);
    scannerTimer = null;
  }
  if (scannerStream) {
    scannerStream.getTracks().forEach((track) => track.stop());
    scannerStream = null;
  }
  if (previewRef.value) {
    previewRef.value.srcObject = null;
  }
};

const closeScanner = () => {
  stopScanner();
  scannerVisible.value = false;
  scannerLoading.value = false;
  scannerError.value = '';
  manualOrderId.value = null;
  manualCode.value = '';
};

const finishCheckin = async (orderId: number, code: string) => {
  await checkinByCoach(orderId, code);
  showSuccessToast('核销成功');
  closeScanner();
  await load();
};

const parsePayload = (raw: string) => {
  try {
    const parsed = JSON.parse(raw) as {
      scene?: string;
      orderId?: number;
      code?: string;
    };
    if (parsed.scene !== 'bitdance_workshop_checkin' || !parsed.orderId || !parsed.code) {
      return null;
    }
    return { orderId: Number(parsed.orderId), code: String(parsed.code) };
  } catch {
    return null;
  }
};

const pollScanResult = async () => {
  if (!previewRef.value || !barcodeDetector || !scannerVisible.value) return;
  try {
    const results = await barcodeDetector.detect(previewRef.value);
    const rawValue = results.find((item) => item.rawValue?.trim())?.rawValue?.trim();
    if (rawValue) {
      const payload = parsePayload(rawValue);
      if (!payload) {
        scannerError.value = '二维码内容无效，请使用用户端生成的 Workshop 签到二维码。';
      } else {
        await finishCheckin(payload.orderId, payload.code);
        return;
      }
    }
  } catch {
    scannerError.value = '当前设备暂不支持实时识别，请改用手动录码。';
    stopScanner();
    return;
  }
  scannerTimer = window.setTimeout(() => {
    void pollScanResult();
  }, 450);
};

const openScanner = async () => {
  scannerVisible.value = true;
  scannerLoading.value = true;
  scannerError.value = '';
  manualOrderId.value = null;
  manualCode.value = '';
  await nextTick();
  try {
    const detectorCtor = (window as Window & { BarcodeDetector?: BarcodeDetectorCtor }).BarcodeDetector;
    if (!detectorCtor || !navigator.mediaDevices?.getUserMedia) {
      scannerError.value = '当前浏览器不支持扫码，请改用手动录入签到码。';
      return;
    }
    const formats = detectorCtor.getSupportedFormats ? await detectorCtor.getSupportedFormats() : ['qr_code'];
    if (!formats.includes('qr_code')) {
      scannerError.value = '当前设备不支持二维码识别，请改用手动录码。';
      return;
    }
    barcodeDetector = new detectorCtor({ formats: ['qr_code'] });
    scannerStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: { ideal: 'environment' } },
      audio: false
    });
    if (!previewRef.value) return;
    previewRef.value.srcObject = scannerStream;
    await previewRef.value.play();
    void pollScanResult();
  } catch (error) {
    scannerError.value = error instanceof Error ? error.message : '相机启动失败，请检查权限后重试。';
    stopScanner();
  } finally {
    scannerLoading.value = false;
  }
};

const submitManual = async () => {
  if (!manualOrderId.value || !manualCode.value.trim()) {
    showFailToast('请输入订单号和签到码');
    return;
  }
  await finishCheckin(manualOrderId.value, manualCode.value.trim());
};

const openManual = (order: CoachWorkshopOrderRow) => {
  scannerVisible.value = true;
  scannerLoading.value = false;
  scannerError.value = '';
  manualOrderId.value = order.orderId;
  manualCode.value = order.checkinCode ?? '';
};

onMounted(load);
onBeforeUnmount(stopScanner);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="学员订单" :show-share="false" />

    <section class="pen-scroll">
      <div class="chip-row">
        <button
          v-for="c in cats"
          :key="c"
          class="chip"
          :class="activeCat === c ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="activeCat = c"
        >
          {{ c }}
        </button>
      </div>

      <p v-if="loading" class="empty">订单加载中</p>
      <p v-else-if="filteredOrders.length === 0" class="empty">当前没有对应订单</p>

      <article v-for="o in filteredOrders" :key="o.orderId" class="order">
        <span class="order__avatar" aria-hidden="true" />
        <div class="order__copy">
          <strong class="order__name">{{ o.buyerName }}</strong>
          <span class="order__meta">{{ o.workshopTitle }} · {{ o.sessionDate }} {{ o.sessionTime }}</span>
          <strong class="order__price">¥{{ o.amount }}</strong>
        </div>
        <span v-if="normalizedStatus(o.status) === 'CHECKED_IN' || normalizedStatus(o.status) === 'COMPLETED'" class="order__done">已核销</span>
        <button v-else-if="normalizedStatus(o.status) === 'PAID'" class="order__btn" type="button" @click="openManual(o)">录码核销</button>
        <span v-else class="order__done">{{ o.status }}</span>
      </article>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="openScanner">
        <ScanLine :size="20" :stroke-width="2" />
        扫码核销
      </button>
    </footer>

    <div v-if="scannerVisible" class="scanner" @click.self="closeScanner">
      <div class="scanner__panel">
        <button class="scanner__close" type="button" aria-label="关闭核销" @click="closeScanner">
          <X :size="18" :stroke-width="2" />
        </button>
        <div class="scanner__head">
          <strong>Workshop 核销</strong>
          <p>扫码读取学员签到二维码，或手动录入订单号与签到码。</p>
        </div>
        <div class="scanner__preview">
          <video ref="previewRef" autoplay muted playsinline />
          <div class="scanner__frame" />
          <p v-if="scannerLoading" class="scanner__hint">正在打开相机…</p>
          <p v-else-if="scannerError" class="scanner__hint scanner__hint--error">{{ scannerError }}</p>
          <p v-else class="scanner__hint">请将学员端二维码放入取景框中央</p>
        </div>
        <label class="scanner__manual">
          <span>订单号</span>
          <input v-model.number="manualOrderId" type="number" min="1" placeholder="请输入订单号" />
        </label>
        <label class="scanner__manual">
          <span>签到码</span>
          <input v-model.trim="manualCode" type="text" maxlength="24" placeholder="请输入签到码" />
        </label>
        <button class="save-bar__btn" type="button" @click="submitManual">
          <Camera :size="18" :stroke-width="2" />
          提交核销
        </button>
      </div>
    </div>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 14px; padding: 16px 18px; }
.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.empty {
  margin: 8px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.order {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__avatar { flex: none; width: 44px; height: 44px; border-radius: 999px; background: $pen-ink; }
  &__copy { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; }
  &__name { font-size: 15px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
  &__price { font-size: 13px; font-weight: 800; line-height: $pen-lh; }

  &__done { flex: none; color: $pen-mute; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
  &__btn {
    flex: none; height: 36px; padding: 8px 16px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 13px; font-weight: 700; line-height: $pen-lh; cursor: pointer;
  }
}

.save-bar {
  position: fixed;
  right: 0; bottom: var(--app-tabbar-offset, 0px); left: 0;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  &__btn {
    width: 100%;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.scanner {
  position: fixed;
  inset: 0;
  z-index: 40;
  padding: 24px 18px;
  background: rgba(17, 17, 17, 0.76);
  display: flex;
  align-items: center;
  justify-content: center;

  &__panel {
    position: relative;
    width: min(100%, 420px);
    border-radius: 20px;
    padding: 18px;
    background: $pen-canvas;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  &__close {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 32px;
    height: 32px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }

  &__head {
    padding-right: 36px;

    strong,
    p {
      margin: 0;
    }

    strong {
      font-size: 20px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    p {
      margin-top: 6px;
      color: $pen-mute;
      font-size: 12px;
      font-weight: 600;
      line-height: 1.45;
    }
  }

  &__preview {
    position: relative;
    aspect-ratio: 1;
    border-radius: 18px;
    overflow: hidden;
    background: $pen-ink;

    video {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__frame {
    position: absolute;
    inset: 16%;
    border: 2px solid rgba(255, 255, 255, 0.92);
    border-radius: 18px;
    box-shadow: 0 0 0 999px rgba(17, 17, 17, 0.2);
    pointer-events: none;
  }

  &__hint {
    position: absolute;
    left: 50%;
    bottom: 16px;
    transform: translateX(-50%);
    margin: 0;
    padding: 8px 12px;
    border-radius: 999px;
    background: rgba(17, 17, 17, 0.72);
    color: $pen-on-primary;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
    white-space: nowrap;

    &--error {
      white-space: normal;
      max-width: calc(100% - 24px);
      text-align: center;
    }
  }

  &__manual {
    display: flex;
    flex-direction: column;
    gap: 8px;

    span {
      font-size: 13px;
      font-weight: 800;
      line-height: $pen-lh;
    }

    input {
      height: 44px;
      padding: 0 14px;
      border: 1px solid $pen-hairline;
      border-radius: 12px;
      background: $pen-soft;
      color: $pen-ink;
      font-size: 14px;
      font-weight: 700;
      outline: none;
    }
  }
}
</style>
