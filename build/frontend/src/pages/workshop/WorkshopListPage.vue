<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAppStore } from '@/stores/app';
import { fetchWorkshops, type WorkshopBrief } from '@/api/workshop';

const router = useRouter();
const appStore = useAppStore();
const STYLES = ['全部', 'Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];

const list = ref<WorkshopBrief[]>([]);
const loading = ref(true);
const style = ref('全部');

const reload = async () => {
  loading.value = true;
  try {
    const data = await fetchWorkshops({
      city: appStore.city,
      style: style.value === '全部' ? undefined : style.value,
      page: 1,
      pageSize: 50
    });
    list.value = data.list;
  } finally {
    loading.value = false;
  }
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">Workshop · {{ appStore.city }}</span>
      <button class="cal" @click="router.push('/me/workshop-calendar')">日历</button>
    </header>
    <section class="filter">
      <span
        v-for="s in STYLES"
        :key="s"
        class="chip"
        :class="{ active: style === s }"
        @click="style = s; reload()"
      >
        {{ s }}
      </span>
    </section>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">没有合适的活动</div>
    <article
      v-for="w in list"
      :key="w.id"
      class="card"
      @click="router.push(`/workshop/${w.id}`)"
    >
      <div class="card__cover">
        <span v-if="w.hot" class="hot">🔥 热门</span>
        <span class="cover-label">{{ w.styles[0] }}</span>
      </div>
      <div class="card__body">
        <div class="card__title">{{ w.title }}</div>
        <div class="card__meta">{{ w.startDate }} ~ {{ w.endDate }} · {{ w.area }}</div>
        <div class="card__foot">
          <span class="price">¥{{ w.priceMin }}<span v-if="w.priceMin !== w.priceMax">~{{ w.priceMax }}</span></span>
          <span class="cap">{{ w.taken }}/{{ w.capacity }}</span>
        </div>
      </div>
    </article>
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
.cal {
  border: none;
  background: none;
  color: var(--bd-primary);
  font-size: 13px;
  cursor: pointer;
}
.filter {
  display: flex;
  gap: 8px;
  padding: 12px;
  overflow-x: auto;
  background: #fff;
}
.chip {
  flex-shrink: 0;
  padding: 5px 14px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.empty {
  text-align: center;
  padding: 60px;
  color: var(--bd-text-secondary);
}
.card {
  margin: 8px 12px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  &__cover {
    aspect-ratio: 16 / 9;
    background: linear-gradient(135deg, #ff7799, #ff2442);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    font-weight: 700;
    position: relative;
  }
  &__body {
    padding: 12px;
  }
  &__title {
    font-size: 15px;
    font-weight: 600;
  }
  &__meta {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__foot {
    margin-top: 10px;
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }
}
.hot {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.4);
  color: #ffaa33;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 999px;
}
.cover-label {
  font-size: 22px;
}
.price {
  color: var(--bd-primary);
  font-size: 16px;
  font-weight: 700;
}
.cap {
  font-size: 12px;
  color: var(--bd-text-secondary);
}
</style>
