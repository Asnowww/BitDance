<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

const local = ref({ ...user.privacy });

const FIELDS: Array<{ key: keyof typeof local.value; label: string; desc: string }> = [
  { key: 'profile', label: '个人资料', desc: '昵称、头像、简介' },
  { key: 'checkin', label: '训练打卡', desc: '默认可见性' },
  { key: 'practice', label: '约练帖', desc: '默认可见性' },
  { key: 'community', label: '社区动态', desc: '默认可见性' }
];

const OPTIONS = [
  { value: 'public', label: '公开' },
  { value: 'friends', label: '仅搭子' },
  { value: 'private', label: '仅自己' }
];

const onSave = () => {
  user.updatePrivacy(local.value);
  showSuccessToast('已保存');
  router.back();
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">隐私设置</span>
    </header>
    <section class="list">
      <div v-for="f in FIELDS" :key="f.key" class="row">
        <div>
          <div class="row__label">{{ f.label }}</div>
          <div class="row__desc">{{ f.desc }}</div>
        </div>
        <select v-model="local[f.key]" class="select">
          <option v-for="o in OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
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
.list {
  background: #fff;
  margin-top: 8px;
}
.row {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--bd-border);
  &__label {
    font-size: 14px;
  }
  &__desc {
    margin-top: 2px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
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
