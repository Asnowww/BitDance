<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];
const LEVELS = ['零基础', '入门', '初级', '进阶', '高阶'];
const GOALS = ['强身健体', '考级 / 比赛', '社交 / 兴趣', '舞台表演', '专业进阶'];

const nickname = ref(user.profile?.nickname ?? '');
const styles = ref<string[]>([...user.preferences.styles]);
const level = ref(user.preferences.level || LEVELS[1]);
const goal = ref(user.preferences.goal || GOALS[2]);

const toggleStyle = (s: string) => {
  const i = styles.value.indexOf(s);
  if (i >= 0) styles.value.splice(i, 1);
  else styles.value.push(s);
};

const onSave = () => {
  user.updateProfile({ nickname: nickname.value });
  user.updatePreferences({ styles: styles.value, level: level.value, goal: goal.value });
  showSuccessToast('已保存');
  router.back();
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">资料与偏好</span>
    </header>
    <section class="form">
      <div class="row">
        <span class="row__label">昵称</span>
        <input v-model="nickname" class="input" maxlength="20" placeholder="给自己一个有趣的舞名" />
      </div>
      <div class="group">
        <div class="group__title">喜欢的舞种（多选）</div>
        <div class="chips">
          <span
            v-for="s in STYLES"
            :key="s"
            class="chip"
            :class="{ active: styles.includes(s) }"
            @click="toggleStyle(s)"
            >{{ s }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">当前水平</div>
        <div class="chips">
          <span
            v-for="l in LEVELS"
            :key="l"
            class="chip"
            :class="{ active: level === l }"
            @click="level = l"
            >{{ l }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">学习目标</div>
        <div class="chips">
          <span
            v-for="g in GOALS"
            :key="g"
            class="chip"
            :class="{ active: goal === g }"
            @click="goal = g"
            >{{ g }}</span
          >
        </div>
      </div>
    </section>
    <footer class="footer">
      <button class="btn" @click="onSave">保存</button>
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
.form {
  background: #fff;
  padding: 8px 16px 16px;
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  &__label {
    width: 56px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
}
.input {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fafafa;
  font-size: 14px;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
    background: #fff;
  }
}
.group {
  padding: 10px 0;
  &__title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 6px 14px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
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
}
</style>
