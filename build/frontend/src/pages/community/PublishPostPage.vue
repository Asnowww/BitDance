<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { createPost } from '@/api/community';

const router = useRouter();

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const TOPICS = ['零基础打卡', '街舞日常', '试听感受', '舞室探店', 'Workshop 速记'];

const text = ref('');
const style = ref('');
const topics = ref<string[]>([]);
const location = ref('');
const hasVideo = ref(false);
const submitting = ref(false);

const toggleTopic = (t: string) => {
  const i = topics.value.indexOf(t);
  if (i >= 0) topics.value.splice(i, 1);
  else if (topics.value.length < 3) topics.value.push(t);
};

const onSubmit = async () => {
  if (text.value.trim().length < 5) {
    showFailToast('正文至少 5 个字');
    return;
  }
  submitting.value = true;
  try {
    await createPost({
      text: text.value,
      images: [],
      hasVideo: hasVideo.value,
      topics: topics.value,
      style: style.value || undefined,
      location: location.value || undefined,
      idempotencyToken: `post-${Date.now()}`
    });
    showSuccessToast('已发布');
    router.replace('/community');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">发动态</span>
      <button class="post" :disabled="submitting" @click="onSubmit">
        {{ submitting ? '发布中…' : '发布' }}
      </button>
    </header>
    <section class="form">
      <textarea v-model="text" class="text" rows="6" placeholder="今天练舞的感受 / 想找谁交流 / 想推荐什么…" />
      <div class="upload">
        <button class="upload__btn">+ 图片</button>
        <button class="upload__btn" :class="{ active: hasVideo }" @click="hasVideo = !hasVideo">
          {{ hasVideo ? '✓' : '+' }} 视频
        </button>
      </div>
      <div class="group">
        <div class="group__title">舞种</div>
        <div class="chips">
          <span class="chip" :class="{ active: !style }" @click="style = ''">不指定</span>
          <span
            v-for="s in STYLES"
            :key="s"
            class="chip"
            :class="{ active: style === s }"
            @click="style = s"
            >{{ s }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">话题（最多 3 个）</div>
        <div class="chips">
          <span
            v-for="t in TOPICS"
            :key="t"
            class="chip"
            :class="{ active: topics.includes(t) }"
            @click="toggleTopic(t)"
            >#{{ t }}</span
          >
        </div>
      </div>
      <div class="row">
        <span class="row__label">位置</span>
        <input v-model="location" class="input" placeholder="选填，例：海淀区舞星 Studio" />
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 24px;
}
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
.post {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
.form {
  padding: 16px;
  background: #fff;
}
.text {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--bd-border);
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
  }
}
.upload {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  &__btn {
    width: 64px;
    height: 64px;
    border: 1px dashed var(--bd-border);
    background: #fafafa;
    border-radius: 12px;
    color: var(--bd-text-secondary);
    font-size: 12px;
    cursor: pointer;
    &.active {
      border-color: var(--bd-primary);
      background: rgba(255, 36, 66, 0.06);
      color: var(--bd-primary);
    }
  }
}
.group {
  margin-top: 16px;
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
  padding: 5px 12px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.row {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  &__label {
    width: 56px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
}
.input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fafafa;
  font-size: 13px;
  outline: none;
  &:focus {
    background: #fff;
    border-color: var(--bd-primary);
  }
}
</style>
