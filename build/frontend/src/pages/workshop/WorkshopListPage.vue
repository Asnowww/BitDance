<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { CalendarClock, ChevronRight, MapPin, Ticket } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchWorkshops, type WorkshopBrief } from '@/api/workshop';

const router = useRouter();
const styles = ['全部', 'Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'] as const;

const activeStyle = ref<(typeof styles)[number]>('全部');
const workshops = ref<WorkshopBrief[]>([]);
const loading = ref(false);

const load = async () => {
  loading.value = true;
  try {
    const data = await fetchWorkshops({
      page: 1,
      pageSize: 20,
      style: activeStyle.value === '全部' ? undefined : activeStyle.value
    });
    workshops.value = data.list;
  } finally {
    loading.value = false;
  }
};

const featured = computed(() => workshops.value[0] ?? null);
const remaining = (item: WorkshopBrief) => Math.max(0, item.capacity - item.taken);

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="Workshop" :show-share="false" />

    <section class="pen-scroll">
      <div class="entry-row">
        <button class="entry-card" type="button" @click="router.push('/me/workshop-orders')">
          <Ticket :size="18" :stroke-width="2" />
          <span>我的订单</span>
        </button>
        <button class="entry-card" type="button" @click="router.push('/me/workshop-calendar')">
          <CalendarClock :size="18" :stroke-width="2" />
          <span>活动日历</span>
        </button>
      </div>

      <section v-if="featured" class="featured" @click="router.push(`/workshop/${featured.id}`)">
        <div class="featured__copy">
          <p class="featured__eyebrow">WORKSHOP LIST</p>
          <h1>{{ featured.title }}</h1>
          <span>{{ featured.startDate }} · {{ featured.area }}</span>
          <strong>¥{{ featured.priceMin }} 起 · 剩 {{ remaining(featured) }} 位</strong>
        </div>
        <ChevronRight :size="20" :stroke-width="2" />
      </section>

      <div class="chip-row">
        <button
          v-for="style in styles"
          :key="style"
          class="chip"
          :class="activeStyle === style ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="
            activeStyle = style;
            load();
          "
        >
          {{ style }}
        </button>
      </div>

      <p v-if="loading" class="empty">Workshop 加载中</p>
      <p v-else-if="workshops.length === 0" class="empty">当前没有可报名的 Workshop</p>

      <article
        v-for="item in workshops"
        :key="item.id"
        class="workshop-card"
        @click="router.push(`/workshop/${item.id}`)"
      >
        <div class="workshop-card__copy">
          <strong>{{ item.title }}</strong>
          <span>{{ item.startDate }} - {{ item.endDate || item.startDate }}</span>
          <p>
            <MapPin :size="14" :stroke-width="2" />
            {{ item.area }} · {{ item.styles.join('/') }} · 导师 {{ item.coachName }}
          </p>
        </div>
        <div class="workshop-card__side">
          <strong>¥{{ item.priceMin }}</strong>
          <span>剩 {{ remaining(item) }} 位</span>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.entry-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.entry-card {
  min-height: 54px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-soft;
  color: $pen-ink;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;
}

.featured {
  border-radius: 18px;
  padding: 16px;
  background: $pen-ink;
  color: $pen-on-primary;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  cursor: pointer;

  &__copy {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__eyebrow,
  h1,
  span,
  strong {
    margin: 0;
  }

  &__eyebrow {
    color: rgba(255, 255, 255, 0.68);
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  h1 {
    font-size: 24px;
    font-weight: 900;
    line-height: 1.15;
  }

  span {
    color: rgba(255, 255, 255, 0.76);
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  strong {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip { @include pen-chip; }

.empty {
  margin: 8px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.workshop-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 16px;
  border-radius: 16px;
  background: $pen-soft;
  cursor: pointer;

  &__copy {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;

    strong,
    span,
    p {
      margin: 0;
    }

    strong {
      font-size: 17px;
      font-weight: 900;
      line-height: 1.2;
    }

    span,
    p {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 700;
      line-height: 1.45;
    }

    p {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
    }
  }

  &__side {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;

    strong {
      font-size: 18px;
      font-weight: 900;
      line-height: 1;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 800;
      line-height: $pen-lh;
      white-space: nowrap;
    }
  }
}
</style>
