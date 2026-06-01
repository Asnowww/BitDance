<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, Search, User, Heart } from 'lucide-vue-next';

const router = useRouter();

const scopes = ['推荐', '附近', '同舞种', '我的'];
const activeScope = ref('推荐');
const filters = ['Hiphop', '中级', '周末', '3人'];
const activeFilters = reactive<Record<string, boolean>>({});

interface PracticeCard {
  id: string;
  cover: string;
  coverH: number;
  tag: string;
  title: string;
  place: string;
  time: string;
  joined: number;
  capacity: number;
  host: string;
}

const cards: PracticeCard[] = [
  { id: 'hiphop-mid', cover: 'https://images.unsplash.com/photo-1667384447307-9ae9cd6ff1d8?w=640&q=80&auto=format&fit=crop', coverH: 150, tag: 'Hiphop', title: '周六 Hiphop 中级复习', place: '五道口 DanceLab', time: '15:00', joined: 2, capacity: 4, host: '阿 May' },
  { id: 'kpop-shoot', cover: 'https://images.unsplash.com/photo-1761882628233-1e23102da76d?w=640&q=80&auto=format&fit=crop', coverH: 120, tag: '韩舞', title: '韩舞成品舞互拍', place: '朝阳 Joy', time: '今晚', joined: 1, capacity: 3, host: 'Leo' },
  { id: 'urban-basic', cover: 'https://images.unsplash.com/photo-1547153760-18fc86324498?w=640&q=80&auto=format&fit=crop', coverH: 110, tag: 'Urban', title: 'Urban 基础律动', place: '中关村', time: '明天 19:30', joined: 3, capacity: 3, host: 'Kiki' },
  { id: 'locking-battle', cover: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=640&q=80&auto=format&fit=crop', coverH: 160, tag: 'Locking', title: '周日 Locking battle', place: '望京 SPACE', time: '14:00', joined: 0, capacity: 4, host: 'Mia' },
  { id: 'jazz-night', cover: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=640&q=80&auto=format&fit=crop', coverH: 130, tag: 'Jazz', title: '工作日晚 Jazz 慢练', place: '国贸 Studio M', time: '周三 20:00', joined: 2, capacity: 5, host: 'Coco' },
  { id: 'breaking-jam', cover: 'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=640&q=80&auto=format&fit=crop', coverH: 120, tag: 'Breaking', title: 'Breaking 地板 jam', place: '五棵松', time: '周六 16:00', joined: 4, capacity: 6, host: 'Ray' }
];

const toggleFilter = (f: string) => {
  activeFilters[f] = !activeFilters[f];
};
const dots = (n: number) => Math.min(n, 5);
const goDetail = (id: string) => router.push(`/practice/${id}`);
</script>

<template>
  <div class="square">
    <header class="square__top">
      <div class="square__copy">
        <h1>约练广场</h1>
        <p>找同城舞友一起练</p>
      </div>
      <button class="icon-btn" type="button" aria-label="消息" @click="router.push('/messages')">
        <Bell :size="20" :stroke-width="2" />
      </button>
    </header>

    <main class="square__content">
      <button class="search" type="button" @click="router.push('/search')">
        <Search :size="18" :stroke-width="2" />
        <span>搜索舞种、地点、发起人</span>
      </button>

      <div class="chips">
        <button
          v-for="s in scopes"
          :key="s"
          class="chip"
          :class="{ 'chip--active': activeScope === s }"
          type="button"
          @click="activeScope = s"
        >
          {{ s }}
        </button>
      </div>
      <div class="chips">
        <button
          v-for="f in filters"
          :key="f"
          class="chip"
          :class="{ 'chip--active': activeFilters[f] }"
          type="button"
          @click="toggleFilter(f)"
        >
          {{ f }}
        </button>
      </div>

      <section class="masonry">
        <article v-for="c in cards" :key="c.id" class="card" @click="goDetail(c.id)">
          <div
            class="card__cover"
            :style="{ backgroundImage: `url(${c.cover})`, height: `${c.coverH}px` }"
          >
            <span class="card__tag">{{ c.tag }}</span>
          </div>
          <div class="card__body">
            <h3 class="card__title">{{ c.title }}</h3>
            <p class="card__meta">{{ c.place }} · {{ c.time }}</p>
            <div class="card__avatars">
              <span
                v-for="n in dots(c.capacity)"
                :key="n"
                class="dot"
                :class="{ 'dot--filled': n <= c.joined }"
              >
                <User v-if="n <= c.joined" :size="13" :stroke-width="2" />
              </span>
              <span class="card__count">{{ c.joined }}/{{ c.capacity }} 人</span>
            </div>
            <div class="card__foot">
              <span class="card__host-avatar" aria-hidden="true" />
              <span class="card__host">{{ c.host }} 发起</span>
              <Heart class="card__like" :size="16" :stroke-width="2" />
            </div>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.square {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft: #f5f5f5;
  --nike-mute: #707072;
  --nike-charcoal: #39393b;
  --nike-hairline: #e5e5e5;
  --nike-hairline-strong: #cacacb;

  min-height: 100%;
  background: var(--nike-soft);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

.square__top {
  height: 68px;
  padding: 14px 18px;
  background: var(--nike-canvas);
  border-bottom: 1px solid var(--nike-hairline);
  display: flex;
  align-items: center;
  gap: 12px;
}

.square__copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;

  h1,
  p {
    margin: 0;
  }
  h1 {
    font-size: 18px;
    font-weight: 900;
    line-height: 1.25;
  }
  p {
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 500;
    line-height: 1.25;
  }
}

.icon-btn {
  width: 40px;
  height: 40px;
  flex: none;
  border: 0;
  border-radius: 999px;
  background: var(--nike-soft);
  color: var(--nike-ink);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.square__content {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search {
  width: 100%;
  height: 44px;
  border: 1px solid var(--nike-hairline);
  border-radius: 24px;
  padding: 0 16px;
  background: var(--nike-canvas);
  color: var(--nike-mute);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-align: left;

  span {
    flex: 1;
    font-size: 14px;
    font-weight: 500;
    color: var(--nike-mute);
  }
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  height: 34px;
  padding: 6px 14px;
  border: 1px solid var(--nike-hairline);
  border-radius: 999px;
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
  cursor: pointer;

  &--active {
    border-color: var(--nike-ink);
    background: var(--nike-ink);
    color: var(--nike-canvas);
  }
}

.masonry {
  column-count: 2;
  column-gap: 10px;
}

.card {
  break-inside: avoid;
  margin-bottom: 10px;
  background: var(--nike-canvas);
  border: 1px solid var(--nike-hairline);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;

  &__cover {
    background-color: var(--nike-charcoal);
    background-size: cover;
    background-position: center;
    padding: 10px;
  }

  &__tag {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 4px 10px;
    border-radius: 999px;
    background: var(--nike-canvas);
    color: var(--nike-ink);
    font-size: 11px;
    font-weight: 700;
    line-height: 1.25;
  }

  &__body {
    padding: 10px 12px 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__title {
    margin: 0;
    font-size: 15px;
    font-weight: 800;
    line-height: 1.3;
  }

  &__meta {
    margin: 0;
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 500;
    line-height: 1.25;
  }

  &__avatars {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__count {
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 700;
    line-height: 1.25;
  }

  &__foot {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__host-avatar {
    width: 20px;
    height: 20px;
    border-radius: 999px;
    background: var(--nike-charcoal);
    flex: none;
  }

  &__host {
    flex: 1;
    min-width: 0;
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 600;
    line-height: 1.25;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  &__like {
    flex: none;
    color: var(--nike-mute);
  }
}

.dot {
  width: 24px;
  height: 24px;
  flex: none;
  border-radius: 999px;
  background: var(--nike-canvas);
  border: 1px solid var(--nike-hairline-strong);
  display: grid;
  place-items: center;

  &--filled {
    background: var(--nike-ink);
    border-color: var(--nike-ink);
    color: var(--nike-canvas);
  }
}
</style>
