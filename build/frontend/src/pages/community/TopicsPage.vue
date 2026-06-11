<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ChevronLeft, Plus, Search, X } from 'lucide-vue-next';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';
import { createTopic, fetchTopics, type CommunityTopic } from '@/api/community';

const router = useRouter();
const topics = ref<CommunityTopic[]>([]);
const keyword = ref('');
const createOpen = ref(false);
const topicName = ref('');
const topicDescription = ref('');
const loading = ref(false);
const creating = ref(false);

const heroTopic = computed(() => topics.value[0]);
const formatCount = (count: number) => `${count} 条动态`;

const loadTopics = async () => {
  loading.value = true;
  try {
    topics.value = await fetchTopics({ scope: 'hot', keyword: keyword.value.trim() || undefined, limit: 30 });
  } finally {
    loading.value = false;
  }
};

const open = (name: string) => router.push(`/community/topic/${encodeURIComponent(name.replace(/^#\s*/, ''))}`);

const openCreate = () => {
  createOpen.value = true;
};

const closeCreate = () => {
  createOpen.value = false;
};

const submitTopic = async () => {
  const name = topicName.value.trim().replace(/^#+/, '').trim();
  if (!name) {
    showToast('请输入话题名称');
    return;
  }
  creating.value = true;
  try {
    const topic = await createTopic({ name, description: topicDescription.value.trim() || undefined });
    topicName.value = '';
    topicDescription.value = '';
    createOpen.value = false;
    await loadTopics();
    showToast('话题已创建');
    open(topic.name);
  } finally {
    creating.value = false;
  }
};

onMounted(loadTopics);
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">话题广场</h1>
      <button class="topbar__icon" type="button" aria-label="搜索" @click="router.push('/community/search')">
        <Search :size="20" :stroke-width="2" />
      </button>
    </header>

    <section class="pen-scroll">
      <section v-if="heroTopic" class="hero" @click="open(heroTopic.name)">
        <span class="hero__tag"># 本周热门</span>
        <strong class="hero__title">{{ heroTopic.name }}</strong>
        <span class="hero__meta">{{ formatCount(heroTopic.count) }} · {{ heroTopic.hot ? '热门推荐' : '正在升温' }}</span>
      </section>

      <div class="search-row">
        <input v-model="keyword" type="text" placeholder="搜索或创建话题" @keyup.enter="loadTopics" />
        <button type="button" aria-label="搜索话题" @click="loadTopics">
          <Search :size="18" :stroke-width="2" />
        </button>
        <button class="search-row__dark" type="button" aria-label="创建话题" @click="openCreate">
          <Plus :size="18" :stroke-width="2.2" />
        </button>
      </div>

      <div class="section-head">
        <h2 class="block-title">热门话题</h2>
        <button type="button" @click="openCreate">创建</button>
      </div>
      <div class="rows">
        <p v-if="loading" class="empty">加载中</p>
        <p v-else-if="topics.length === 0" class="empty">暂无话题</p>
        <PenFieldRow
          v-for="t in topics"
          :key="t.name"
          :label="`# ${t.name}`"
          :value="formatCount(t.count)"
          @click="open(t.name)"
        />
      </div>
    </section>

    <div v-if="createOpen" class="topic-layer" role="dialog" aria-modal="true" aria-label="创建话题">
      <button class="topic-layer__backdrop" type="button" aria-label="关闭创建话题" @click="closeCreate" />
      <section class="topic-sheet">
        <header class="topic-sheet__head">
          <h2>创建话题</h2>
          <button type="button" aria-label="关闭" @click="closeCreate">
            <X :size="16" :stroke-width="2" />
          </button>
        </header>
        <input v-model="topicName" class="topic-input" type="text" maxlength="100" placeholder="话题名称，例如 Kpop打卡" />
        <textarea
          v-model="topicDescription"
          class="topic-note"
          maxlength="500"
          rows="3"
          placeholder="补充一句话介绍，让舞友知道适合发布什么内容。"
        />
        <button class="topic-submit" type="button" :disabled="creating" @click="submitTopic">
          {{ creating ? '创建中' : '创建并进入话题' }}
        </button>
      </section>
    </div>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__title { flex: 1; margin: 0; font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__icon {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }
}

.pen-scroll { display: flex; flex-direction: column; gap: 16px; padding: 16px 18px; }

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 6px;
  height: 130px;
  padding: 18px;
  border-radius: 16px;
  background: $pen-ink;
  color: $pen-on-primary;
  cursor: pointer;

  &__tag { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
  &__title { font-size: 28px; font-weight: 900; line-height: $pen-lh; }
  &__meta { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 42px 42px;
  gap: 8px;

  input {
    min-width: 0;
    height: 42px;
    padding: 0 14px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 700;
    outline: none;
  }

  button {
    width: 42px;
    height: 42px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }

  &__dark {
    background: $pen-ink !important;
    color: $pen-on-primary !important;
  }
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  button {
    height: 32px;
    padding: 0 12px;
    border: 1px solid $pen-ink;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    cursor: pointer;
  }
}

.block-title { @include pen-h3-section; }
.rows { display: flex; flex-direction: column; }
.empty {
  margin: 12px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.topic-layer {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.topic-layer__backdrop {
  position: absolute;
  inset: 0;
  border: 0;
  background: rgba(17, 17, 17, 0.28);
}

.topic-sheet {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: 18px 18px calc(18px + env(safe-area-inset-bottom));
  border-radius: 24px 24px 0 0;
  background: $pen-canvas;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    button {
      width: 34px;
      height: 34px;
      border: 0;
      border-radius: 999px;
      background: $pen-soft;
      color: $pen-ink;
      display: grid;
      place-items: center;
      cursor: pointer;
    }
  }
}

.topic-input,
.topic-note {
  width: 100%;
  box-sizing: border-box;
  border: 0;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  font-size: 14px;
  font-weight: 700;
  outline: none;
}

.topic-input { height: 46px; padding: 0 14px; }
.topic-note { resize: none; padding: 12px 14px; line-height: 1.45; }

.topic-submit {
  height: 48px;
  border: 0;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 15px;
  font-weight: 900;
  cursor: pointer;

  &:disabled {
    opacity: 0.6;
    cursor: default;
  }
}
</style>
