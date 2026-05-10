<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchFollowing, toggleFollow } from '@/api/community';

const router = useRouter();
const list = ref<Array<{ id: number; name: string; avatar: string; followed: boolean }>>([]);
const loading = ref(true);

const reload = async () => {
  loading.value = true;
  try {
    list.value = await fetchFollowing();
  } finally {
    loading.value = false;
  }
};

const onToggle = async (id: number) => {
  const r = await toggleFollow(id);
  const item = list.value.find((it) => it.id === id);
  if (item) item.followed = r.following;
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">关注 / 推荐用户</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <article v-for="u in list" :key="u.id" class="item">
      <span class="avatar">{{ u.name.charAt(0) }}</span>
      <span class="name">{{ u.name }}</span>
      <button
        class="btn"
        :class="{ followed: u.followed }"
        @click="onToggle(u.id)"
      >
        {{ u.followed ? '已关注' : '+ 关注' }}
      </button>
    </article>
  </div>
</template>

<style lang="scss" scoped>
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
.empty {
  padding: 60px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.name {
  flex: 1;
  font-size: 14px;
}
.btn {
  border: 1px solid var(--bd-primary);
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 12px;
  cursor: pointer;
  &.followed {
    background: #fff;
    color: var(--bd-text-secondary);
    border-color: var(--bd-border);
  }
}
</style>
