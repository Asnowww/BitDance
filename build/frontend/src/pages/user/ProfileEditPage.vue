<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';

const router = useRouter();

const profile = [
  { label: '昵称', value: '小李' },
  { label: '性别', value: '女' },
  { label: '生日', value: '2003-05' },
  { label: '个人简介', value: '零基础韩舞爱好者' }
];

const styles = ['韩舞', 'Jazz', 'Hiphop', 'Urban'];
const levels = ['零基础', '初级', '中级', '高级'];
const goals = ['塑形', '兴趣', '成品舞', '比赛'];

const selStyles = reactive<Record<string, boolean>>({ 韩舞: true, Jazz: true });
const level = ref('零基础');
const goal = ref('成品舞');

const toggleStyle = (s: string) => {
  selStyles[s] = !selStyles[s];
};

const onSave = () => {
  showSuccessToast('资料已保存');
  router.back();
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="资料与偏好" :show-share="false" />

    <section class="pen-scroll">
      <div class="avatar">
        <span class="avatar__img" aria-hidden="true" />
        <button class="avatar__edit" type="button">更换头像</button>
      </div>

      <div class="rows">
        <PenFieldRow
          v-for="f in profile"
          :key="f.label"
          :label="f.label"
          :value="f.value"
        />
      </div>

      <section class="block">
        <h2 class="block__title">舞蹈偏好</h2>

        <p class="block__label">感兴趣舞种</p>
        <div class="chip-row">
          <button
            v-for="s in styles"
            :key="s"
            class="chip"
            :class="selStyles[s] ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="toggleStyle(s)"
          >
            {{ s }}
          </button>
        </div>

        <p class="block__label">当前水平</p>
        <div class="chip-row">
          <button
            v-for="l in levels"
            :key="l"
            class="chip"
            :class="level === l ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="level = l"
          >
            {{ l }}
          </button>
        </div>

        <p class="block__label">学习目标</p>
        <div class="chip-row">
          <button
            v-for="g in goals"
            :key="g"
            class="chip"
            :class="goal === g ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="goal = g"
          >
            {{ g }}
          </button>
        </div>
      </section>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="onSave">保存资料</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;

  &--with-bar {
    padding-bottom: calc(76px + env(safe-area-inset-bottom));
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px;
}

.avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  &__img {
    width: 84px;
    height: 84px;
    border-radius: 999px;
    background: $pen-ink;
  }

  &__edit {
    border: 0;
    background: transparent;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.rows {
  display: flex;
  flex-direction: column;
}

.block {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__title {
    @include pen-h3-section;
    margin-top: 4px;
  }

  &__label {
    margin: 4px 0 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.save-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
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
