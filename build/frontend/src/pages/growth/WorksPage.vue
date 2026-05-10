<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showConfirmDialog } from 'vant';
import {
  fetchGrowthWorks,
  createGrowthWork,
  deleteGrowthWork,
  type GrowthWork
} from '@/api/growth';

const router = useRouter();
const list = ref<GrowthWork[]>([]);
const showForm = ref(false);

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];

const form = ref({
  type: 'image' as 'image' | 'video',
  title: '',
  description: '',
  style: '',
  visibility: 'public' as 'public' | 'private' | 'friends'
});

const reload = async () => {
  list.value = await fetchGrowthWorks();
};

const onAdd = async () => {
  if (!form.value.title.trim()) return;
  await createGrowthWork({
    type: form.value.type,
    title: form.value.title,
    description: form.value.description,
    style: form.value.style || undefined,
    visibility: form.value.visibility
  });
  showSuccessToast('已上传');
  showForm.value = false;
  form.value = { type: 'image', title: '', description: '', style: '', visibility: 'public' };
  void reload();
};

const onDelete = async (id: number) => {
  await showConfirmDialog({ title: '删除作品？' }).catch(() => {
    throw new Error('cancel');
  });
  await deleteGrowthWork(id);
  void reload();
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">阶段作品</span>
      <button class="add" @click="showForm = true">+ 新增</button>
    </header>
    <div v-if="!list.length" class="empty">还没有作品，记录每个阶段的进步吧</div>
    <section class="grid">
      <article v-for="w in list" :key="w.id" class="card">
        <div class="card__cover">{{ w.type === 'video' ? '▶' : '🖼' }}</div>
        <div class="card__body">
          <div class="card__title">{{ w.title }}</div>
          <div class="card__desc">{{ w.description }}</div>
          <div class="card__meta">
            <span v-if="w.style">{{ w.style }}</span>
            <span class="vis">{{ { public: '公开', friends: '搭子', private: '私密' }[w.visibility] }}</span>
          </div>
        </div>
        <button class="card__del" @click="onDelete(w.id)">删除</button>
      </article>
    </section>
    <van-popup v-model:show="showForm" position="bottom" round :style="{ height: '70%' }">
      <div class="form">
        <h3>新增阶段作品</h3>
        <div class="row">
          <span>类型</span>
          <select v-model="form.type" class="select">
            <option value="image">图片</option>
            <option value="video">视频</option>
          </select>
        </div>
        <div class="row">
          <span>标题</span>
          <input v-model="form.title" class="input" placeholder="例：第一支完整 Routine" />
        </div>
        <div class="row row--top">
          <span>简介</span>
          <textarea v-model="form.description" rows="3" class="input ta" />
        </div>
        <div class="row">
          <span>舞种</span>
          <select v-model="form.style" class="select">
            <option value="">不指定</option>
            <option v-for="s in STYLES" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
        <div class="row">
          <span>可见性</span>
          <select v-model="form.visibility" class="select">
            <option value="public">公开</option>
            <option value="friends">仅搭子</option>
            <option value="private">仅自己</option>
          </select>
        </div>
        <button class="btn" @click="onAdd">提交</button>
      </div>
    </van-popup>
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
    flex: 1;
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
.add {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 12px;
  cursor: pointer;
}
.empty {
  padding: 60px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.grid {
  padding: 12px;
}
.card {
  display: flex;
  gap: 10px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 8px;
  &__cover {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
  }
  &__body {
    flex: 1;
    min-width: 0;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
  }
  &__desc {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__meta {
    margin-top: 6px;
    display: flex;
    gap: 6px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__del {
    border: 1px solid var(--bd-border);
    background: #fff;
    color: var(--bd-text-secondary);
    border-radius: 999px;
    padding: 4px 10px;
    font-size: 11px;
    cursor: pointer;
    align-self: flex-start;
  }
}
.vis {
  background: #fafafa;
  padding: 1px 6px;
  border-radius: 6px;
}
.form {
  padding: 20px 16px calc(20px + env(safe-area-inset-bottom));
  h3 {
    margin: 0 0 16px;
    font-size: 16px;
  }
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  span {
    width: 56px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
  &--top {
    align-items: flex-start;
    span {
      padding-top: 8px;
    }
  }
}
.input {
  flex: 1;
  height: 36px;
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
.ta {
  height: auto;
  padding: 8px 12px;
  resize: none;
  font-family: inherit;
}
.select {
  flex: 1;
  height: 36px;
  padding: 0 10px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fafafa;
  font-size: 14px;
}
.btn {
  margin-top: 16px;
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 999px;
  background: var(--bd-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
</style>
