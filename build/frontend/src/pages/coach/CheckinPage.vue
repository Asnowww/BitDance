<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { useOpsStore } from '@/stores/ops';
import {
  checkinCourseByCode,
  checkinWorkshopOrder,
  fetchMerchantWorkshopOrders,
  type CourseOrder
} from '@/api/coachOps';

const ops = useOpsStore();
const mode = ref<'course' | 'workshop'>('course');
const code = ref('');
const submitting = ref(false);
const lastResult = ref<{ ok: boolean; text: string } | null>(null);

const codeValid = computed(() => /^\d{8}$/.test(code.value));

// ---------- 核销 ----------
const doCheckin = async () => {
  if (!codeValid.value || submitting.value) return;
  await showConfirmDialog({
    title: '核销确认',
    message: `确认核销${mode.value === 'course' ? '课程' : 'Workshop'}核销码 ${code.value}?核销后立即失效。`
  });
  submitting.value = true;
  try {
    if (mode.value === 'course') {
      const order: CourseOrder = await checkinCourseByCode(code.value);
      lastResult.value = { ok: true, text: `核销成功:订单 ${order.orderNo}` };
    } else {
      await ops.refresh();
      if (!ops.studioId) throw new Error('未开通商家后台');
      const orders = await fetchMerchantWorkshopOrders({ studioId: ops.studioId, status: 'paid' });
      const hit = orders.find((o) => o.checkinCode === code.value);
      if (!hit) {
        lastResult.value = { ok: false, text: '未找到匹配的已支付 Workshop 订单,请检查核销码是否正确、是否已核销或已退款' };
        showFailToast('核销码无效');
        return;
      }
      await checkinWorkshopOrder(hit.id, code.value);
      lastResult.value = { ok: true, text: `核销成功:订单 ${hit.orderNo}` };
    }
    showSuccessToast('核销成功');
    code.value = '';
  } catch (e: unknown) {
    const msg = (e as { message?: string })?.message ?? '核销失败';
    lastResult.value = { ok: false, text: msg };
  } finally {
    submitting.value = false;
  }
};

// ---------- 扫码(浏览器 BarcodeDetector,扫出 8 位数字码) ----------
const scanning = ref(false);
const videoEl = ref<HTMLVideoElement | null>(null);
let stream: MediaStream | null = null;
let scanTimer: number | null = null;

const stopScan = () => {
  if (scanTimer) {
    clearInterval(scanTimer);
    scanTimer = null;
  }
  stream?.getTracks().forEach((t) => t.stop());
  stream = null;
  scanning.value = false;
};

const startScan = async () => {
  const Detector = (window as unknown as { BarcodeDetector?: new (o: { formats: string[] }) => { detect: (v: HTMLVideoElement) => Promise<Array<{ rawValue: string }>> } }).BarcodeDetector;
  if (!Detector) {
    showFailToast('当前浏览器不支持扫码,请手动输入 8 位核销码');
    return;
  }
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
  } catch {
    showFailToast('无法访问相机,请手动输入核销码');
    return;
  }
  scanning.value = true;
  await new Promise((r) => setTimeout(r, 50));
  if (!videoEl.value) return;
  videoEl.value.srcObject = stream;
  await videoEl.value.play();
  const detector = new Detector({ formats: ['qr_code'] });
  scanTimer = window.setInterval(async () => {
    if (!videoEl.value) return;
    try {
      const codes = await detector.detect(videoEl.value);
      const raw = codes[0]?.rawValue?.trim();
      if (raw) {
        const digits = raw.match(/\d{8}/)?.[0];
        if (digits) {
          code.value = digits;
          stopScan();
          showSuccessToast('已识别核销码');
        }
      }
    } catch {
      /* 单帧识别失败忽略 */
    }
  }, 400);
};

onBeforeUnmount(stopScan);
</script>

<template>
  <main class="checkin-page">
    <PenTopBar title="签到核销" :show-share="false" />

    <section class="body">
      <div class="seg">
        <button :class="{ active: mode === 'course' }" @click="mode = 'course'">课程订单</button>
        <button :class="{ active: mode === 'workshop' }" @click="mode = 'workshop'">
          Workshop 订单
        </button>
      </div>

      <div class="code-card">
        <p class="label">输入 8 位数字核销码</p>
        <input
          v-model="code"
          inputmode="numeric"
          maxlength="8"
          pattern="[0-9]*"
          placeholder="00000000"
          class="code-input"
        />
        <div class="scan-row">
          <button v-if="!scanning" class="scan-btn" @click="startScan">扫二维码</button>
          <button v-else class="scan-btn stop" @click="stopScan">停止扫码</button>
        </div>
        <video v-show="scanning" ref="videoEl" class="scan-video" muted playsinline />
      </div>

      <div v-if="lastResult" class="result" :class="lastResult.ok ? 'ok' : 'bad'">
        {{ lastResult.text }}
      </div>

      <p class="rules">
        核销规则:支付成功后生成核销码;核销后立即失效;活动结束后失效。过早核销、已过期、已核销、退款中订单将被拒绝并提示原因。
      </p>
    </section>

    <footer class="submit-bar">
      <button :disabled="!codeValid || submitting" @click="doCheckin">
        {{ submitting ? '核销中…' : '确认核销' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.checkin-page {
  @include ops-page;
}
.body {
  @include ops-body;
  @include ops-form;
}

.code-card {
  margin-top: 18px;
  border-radius: 24px;
  background: $pen-soft;
  padding: 22px 18px;
  text-align: center;
  .label {
    margin: 0 0 12px;
    color: $pen-mute;
    font-size: 12.5px;
    font-weight: 800;
  }
}

.code-input {
  width: 100%;
  border: 0;
  border-bottom: 2px solid $pen-ink;
  background: transparent;
  padding: 6px 0 10px;
  text-align: center;
  font-size: 36px;
  font-weight: 900;
  letter-spacing: 0.35em;
  color: $pen-ink;
  outline: none;
  box-sizing: border-box;
  &::placeholder {
    color: $pen-hairline-strong;
  }
}

.scan-row {
  margin-top: 16px;
}
.scan-btn {
  height: 40px;
  padding: 0 22px;
  border: 1px solid $pen-ink;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  &.stop {
    background: $pen-ink;
    color: #fff;
  }
}

.scan-video {
  width: 100%;
  margin-top: 14px;
  border-radius: 18px;
  background: #111;
  aspect-ratio: 1;
  object-fit: cover;
}

.result {
  margin-top: 16px;
  border-radius: 18px;
  padding: 14px 16px;
  font-size: 13.5px;
  font-weight: 800;
  line-height: 1.5;
  &.ok {
    background: rgba(0, 125, 72, 0.08);
    color: $pen-success;
  }
  &.bad {
    background: rgba(211, 0, 5, 0.06);
    color: #d30005;
  }
}

.rules {
  margin: 18px 2px 0;
  color: $pen-mute;
  font-size: 12px;
  line-height: 1.6;
}

.submit-bar {
  @include ops-submit-bar;
}
</style>
