<script setup lang="ts">
import { reactive } from 'vue';
import { showToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const visibility = [
  { label: '个人资料', value: '公开' },
  { label: '训练打卡', value: '仅好友' },
  { label: '约练动态', value: '公开' },
  { label: '社区动态', value: '仅好友' }
];

const toggles = reactive<Record<string, boolean>>({
  异常登录提醒: true,
  陌生人私信: false,
  附近的人可见: true
});

const account = [
  { label: '绑定手机', value: '138••••6789' },
  { label: '登录设备', value: '2 台' }
];
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="隐私设置" :show-share="false" />

    <section class="pen-scroll">
      <h2 class="block-title">内容可见范围</h2>
      <PenFieldRow
        v-for="v in visibility"
        :key="v.label"
        :label="v.label"
        :value="v.value"
        @click="showToast(`${v.label}：${v.value}`)"
      />

      <h2 class="block-title">账号安全</h2>
      <div
        v-for="(on, key) in toggles"
        :key="key"
        class="toggle-row"
      >
        <span class="toggle-row__label">{{ key }}</span>
        <button
          class="switch"
          :class="{ 'switch--on': on }"
          type="button"
          :aria-pressed="on"
          @click="toggles[key] = !on"
        >
          <span class="switch__knob" />
        </button>
      </div>
      <PenFieldRow
        v-for="a in account"
        :key="a.label"
        :label="a.label"
        :value="a.value"
        @click="showToast(a.label)"
      />
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
  gap: 4px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.block-title {
  @include pen-h3-section;
  margin: 12px 0 4px;
}

.toggle-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 0;
  border-bottom: 1px solid $pen-hairline;

  &__label {
    flex: 1;
    color: $pen-ink;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.switch {
  flex: none;
  width: 46px;
  height: 28px;
  padding: 3px;
  border: 0;
  border-radius: 999px;
  background: $pen-hairline;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  cursor: pointer;
  transition: background 0.15s;

  &__knob {
    width: 22px;
    height: 22px;
    border-radius: 999px;
    background: $pen-canvas;
  }

  &--on {
    background: $pen-ink;
    justify-content: flex-end;
  }
}
</style>
