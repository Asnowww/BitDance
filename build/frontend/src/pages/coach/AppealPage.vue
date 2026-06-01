<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { Star, Plus } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const router = useRouter();

const reasons = ['与事实不符', '恶意差评', '同行攻击', '其他'];
const reason = ref('与事实不符');
const detail = ref('');

const onSubmit = () => {
  showSuccessToast('申诉已提交，等待人工审核');
  router.back();
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="评价申诉" :show-share="false" />

    <section class="pen-scroll">
      <div class="quote">
        <header class="quote__head">
          <span class="quote__avatar" aria-hidden="true" />
          <div class="quote__who">
            <strong class="quote__name">匿名学员</strong>
            <span class="quote__stars">
              <Star
                v-for="i in 5"
                :key="i"
                :size="13"
                :stroke-width="2"
                :fill="i <= 2 ? '#111111' : 'none'"
                :color="i <= 2 ? '#111111' : '#E5E5E5'"
              />
            </span>
          </div>
        </header>
        <p class="quote__content">环境一般，老师迟到了十分钟。</p>
      </div>

      <h2 class="block-title">申诉理由</h2>
      <div class="chip-row">
        <button
          v-for="r in reasons"
          :key="r"
          class="chip"
          :class="reason === r ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="reason = r"
        >
          {{ r }}
        </button>
      </div>

      <h2 class="block-title">补充说明</h2>
      <textarea
        v-model="detail"
        class="note"
        rows="3"
        placeholder="补充说明与证据描述，如签到记录、监控时间…"
      />

      <div class="evidence">
        <button class="evidence__add" type="button" aria-label="上传证据"><Plus :size="26" :stroke-width="2" /></button>
      </div>

      <p class="hint">提交后进入平台人工审核，3 个工作日内反馈</p>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="onSubmit">提交申诉</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.quote {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 12px;
  background: $pen-soft;
  border-left: 3px solid $pen-ink;

  &__head { display: flex; align-items: center; gap: 8px; }
  &__avatar { flex: none; width: 32px; height: 32px; border-radius: 999px; background: $pen-ink; }
  &__who { display: flex; flex-direction: column; gap: 4px; }
  &__name { font-size: 13px; font-weight: 900; line-height: $pen-lh; }
  &__stars { display: inline-flex; gap: 3px; }
  &__content { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }
}

.block-title { @include pen-h3-section; }

.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.note {
  width: 100%;
  min-height: 84px;
  padding: 14px;
  border: 0;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  font-family: $pen-font;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  resize: none;
  box-sizing: border-box;
  outline: none;
  &::placeholder { color: $pen-mute; }
}

.evidence {
  display: flex;
  gap: 8px;

  &__add {
    width: 88px; height: 88px;
    border: 1px solid $pen-hairline; border-radius: 12px;
    background: $pen-soft; color: $pen-mute;
    display: grid; place-items: center; cursor: pointer;
  }
}

.hint { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }

.save-bar {
  position: fixed;
  right: 0; bottom: 0; left: 0;
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
</style>
