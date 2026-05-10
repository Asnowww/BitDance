<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAppStore } from '@/stores/app';
import { fetchPractices, type PracticePost } from '@/api/practice';

const router = useRouter();
const appStore = useAppStore();

const STYLES = ['全部', 'Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];
const LEVELS = ['全部', '零基础', '入门', '初级', '进阶', '高阶'];

const list = ref<PracticePost[]>([]);
const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const page = ref(1);
const style = ref('全部');
const level = ref('全部');
const scope = ref<'nearby' | 'city'>('city');

const load = async (reset = false) => {
  if (loading.value) return;
  loading.value = true;
  if (reset) {
    page.value = 1;
    finished.value = false;
  }
  try {
    const data = await fetchPractices({
      city: appStore.city,
      style: style.value === '全部' ? undefined : style.value,
      level: level.value === '全部' ? undefined : level.value,
      scope: scope.value,
      page: page.value,
      pageSize: 20
    });
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

const STATUS_LABEL: Record<string, string> = {
  PUBLISHED: '招募中',
  MATCHED: '人满',
  CONFIRMED: '已确认',
  COMPLETED: '已完成',
  CANCELED: '已取消',
  EXPIRED: '已过期'
};
</script>

<template>
  <div class="page">
    <header class="head">
      <div class="head__title">约练广场 · {{ appStore.city }}</div>
      <div class="head__sub">和懂你的人一起练</div>
    </header>
    <nav class="scope">
      <button class="scope__item" :class="{ active: scope === 'city' }" @click="scope = 'city'; load(true)">同城</button>
      <button class="scope__item" :class="{ active: scope === 'nearby' }" @click="scope = 'nearby'; load(true)">附近</button>
      <button class="scope__more" @click="router.push('/practice/recommend')">推荐与搭子 →</button>
    </nav>
    <section class="filter">
      <div class="filter__row">
        <span class="filter__label">舞种</span>
        <div class="chips">
          <span
            v-for="s in STYLES"
            :key="s"
            class="chip"
            :class="{ active: style === s }"
            @click="style = s; load(true)"
            >{{ s }}</span
          >
        </div>
      </div>
      <div class="filter__row">
        <span class="filter__label">水平</span>
        <div class="chips">
          <span
            v-for="l in LEVELS"
            :key="l"
            class="chip"
            :class="{ active: level === l }"
            @click="level = l; load(true)"
            >{{ l }}</span
          >
        </div>
      </div>
    </section>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="到底啦"
        @load="onLoad"
      >
        <article
          v-for="p in list"
          :key="p.id"
          class="card"
          @click="router.push(`/practice/${p.id}`)"
        >
          <div class="card__head">
            <span class="card__title">{{ p.title }}</span>
            <span class="status" :data-s="p.status">{{ STATUS_LABEL[p.status] }}</span>
          </div>
          <div class="card__meta">
            <span>{{ p.style }}</span>
            <span>·</span>
            <span>{{ p.level }}</span>
            <span>·</span>
            <span>{{ p.date }} {{ p.time }}</span>
          </div>
          <div class="card__loc">📍 {{ p.location }}</div>
          <div class="card__foot">
            <div class="author">
              <span class="avatar">{{ p.authorName.charAt(0) }}</span>
              <span>{{ p.authorName }}</span>
            </div>
            <span class="cap">{{ p.takenCount }}/{{ p.capacity }}</span>
          </div>
        </article>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 16px;
}
.head {
  padding: 16px 16px 8px;
  &__title {
    font-size: 20px;
    font-weight: 700;
  }
  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
.scope {
  display: flex;
  gap: 8px;
  padding: 4px 16px 8px;
}
.scope__item {
  border: 1px solid var(--bd-border);
  background: #fff;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.scope__more {
  margin-left: auto;
  border: none;
  background: none;
  color: var(--bd-primary);
  font-size: 13px;
  cursor: pointer;
}
.filter {
  background: #fff;
  padding: 8px 16px;
  &__row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 6px 0;
  }
  &__label {
    width: 36px;
    font-size: 12px;
    color: var(--bd-text-secondary);
    flex-shrink: 0;
  }
}
.chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.chip {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #fafafa;
  color: var(--bd-text);
  cursor: pointer;
  &.active {
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.card {
  margin: 8px 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  &__title {
    font-size: 15px;
    font-weight: 600;
  }
  &__meta {
    margin-top: 6px;
    display: flex;
    gap: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__loc {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__foot {
    margin-top: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
.author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cap {
  font-size: 12px;
  color: var(--bd-primary);
  font-weight: 600;
}
.status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 170, 51, 0.15);
  color: #c87a00;
  &[data-s='MATCHED'],
  &[data-s='CONFIRMED'] {
    background: rgba(54, 165, 255, 0.12);
    color: #36a5ff;
  }
  &[data-s='COMPLETED'] {
    background: rgba(0, 168, 84, 0.12);
    color: #00a854;
  }
  &[data-s='CANCELED'],
  &[data-s='EXPIRED'] {
    background: #f3f3f3;
    color: var(--bd-text-secondary);
  }
}
</style>
