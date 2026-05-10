<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchFeed, type ContentPost } from '@/api/community';

const router = useRouter();
const list = ref<ContentPost[]>([]);
const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const page = ref(1);
const scope = ref<'recommend' | 'follow'>('recommend');

const load = async (reset = false) => {
  if (loading.value) return;
  loading.value = true;
  if (reset) {
    page.value = 1;
    finished.value = false;
  }
  try {
    const data = await fetchFeed({ scope: scope.value, page: page.value, pageSize: 20 });
    if (reset) list.value = data.list;
    else list.value = list.value.concat(data.list);
    if (list.value.length >= data.total || data.list.length === 0) finished.value = true;
    else page.value += 1;
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const onRefresh = () => {
  refreshing.value = true;
  void load(true);
};
const onLoad = () => !finished.value && void load(false);

onMounted(() => void load(true));
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">社区</span>
      <button class="search" @click="router.push('/community/search')">🔍</button>
    </header>
    <nav class="scope">
      <button class="scope__item" :class="{ active: scope === 'recommend' }" @click="scope = 'recommend'; load(true)">推荐</button>
      <button class="scope__item" :class="{ active: scope === 'follow' }" @click="scope = 'follow'; load(true)">关注</button>
      <button class="scope__more" @click="router.push('/community/topics')">话题 →</button>
    </nav>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="到底啦"
        @load="onLoad"
      >
        <section class="grid">
          <article
            v-for="p in list"
            :key="p.id"
            class="card"
            @click="router.push(`/community/post/${p.id}`)"
          >
            <div class="card__cover">
              <span v-if="p.hasVideo" class="card__video">▶</span>
              <span class="card__cover-text">{{ p.style ?? '✨' }}</span>
            </div>
            <div class="card__body">
              <p class="card__text">{{ p.text }}</p>
              <div v-if="p.topics.length" class="card__topics">
                <span v-for="t in p.topics.slice(0, 2)" :key="t" class="topic">#{{ t }}</span>
              </div>
              <div class="card__foot">
                <span class="author">
                  <span class="avatar">{{ p.authorName.charAt(0) }}</span>
                  <span>{{ p.authorName }}</span>
                </span>
                <span class="like">♥ {{ p.likeCount }}</span>
              </div>
            </div>
          </article>
        </section>
      </van-list>
    </van-pull-refresh>
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
.back,
.search {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
}
.scope {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__item {
    border: none;
    background: none;
    font-size: 15px;
    color: var(--bd-text-secondary);
    cursor: pointer;
    &.active {
      color: var(--bd-text);
      font-weight: 700;
    }
  }
  &__more {
    margin-left: auto;
    border: none;
    background: none;
    color: var(--bd-primary);
    font-size: 13px;
    cursor: pointer;
  }
}
.grid {
  column-count: 2;
  column-gap: 8px;
  padding: 8px;
}
.card {
  break-inside: avoid;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  &__cover {
    aspect-ratio: 4 / 5;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    font-weight: 700;
    position: relative;
  }
  &__video {
    position: absolute;
    bottom: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
  }
  &__body {
    padding: 8px 10px 10px;
  }
  &__text {
    margin: 0;
    font-size: 12px;
    line-height: 1.5;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  &__topics {
    margin-top: 6px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
  &__foot {
    margin-top: 6px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.topic {
  font-size: 10px;
  color: var(--bd-primary);
}
.author {
  display: flex;
  align-items: center;
  gap: 4px;
}
.avatar {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.like {
  color: var(--bd-primary);
}
</style>
