<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Bell, Search, MapPin, Sparkles, CalendarDays, Star, Ticket } from 'lucide-vue-next';

const router = useRouter();

const quickEntries = [
  { icon: MapPin, label: '附近', to: '/search' },
  { icon: Sparkles, label: '零基础', to: '/search' },
  { icon: CalendarDays, label: '今日课', to: '/search' },
  { icon: Star, label: '热门老师', to: '/search' },
  { icon: Ticket, label: 'Workshop', to: '/workshops' }
];

interface RecommendCard {
  id: string;
  title: string;
  meta: string;
  action: string;
  to: string;
}

const recommends: RecommendCard[] = [
  { id: 'urban-flow', title: 'Urban Flow 舞室', meta: '1.2km · 4.8 · 韩舞强', action: '试听', to: '/studio/1' },
  { id: 'beatlab', title: 'BeatLab 新手课', meta: '今晚 19:30 · ¥79/节', action: '试听', to: '/course/beatlab-newbie' }
];

const heroImage =
  'https://images.unsplash.com/photo-1667384447307-9ae9cd6ff1d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w4NDM0ODN8MHwxfHJhbmRvbXx8fHx8fHx8fDE3Nzk3ODEzMzZ8&ixlib=rb-4.1.0&q=80&w=1080';
</script>

<template>
  <div class="home">
    <header class="home__header">
      <div class="home__copy">
        <h1>北京 · 海淀</h1>
        <p>找舞室、课程、老师</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息提醒" @click="router.push('/messages')">
        <Bell :size="20" :stroke-width="2" />
      </button>
    </header>

    <main class="home__content">
      <button class="search-pill" type="button" @click="router.push('/search')">
        <Search :size="18" :stroke-width="2" />
        <span>搜索舞室、课程、老师、舞种</span>
      </button>

      <section
        class="hero"
        :style="{ backgroundImage: `url(${heroImage})` }"
        @click="router.push('/search')"
      >
        <div class="hero__overlay">
          <strong class="hero__title">FIND<br />YOUR<br />STUDIO</strong>
          <p class="hero__sub">附近零基础友好课程</p>
        </div>
      </section>

      <section class="quick" aria-label="快捷入口">
        <button
          v-for="entry in quickEntries"
          :key="entry.label"
          class="quick__item"
          type="button"
          @click="router.push(entry.to)"
        >
          <component :is="entry.icon" :size="20" :stroke-width="2" />
          <span>{{ entry.label }}</span>
        </button>
      </section>

      <section class="recommend">
        <header class="recommend__head">
          <h2>为你推荐</h2>
          <button class="recommend__more" type="button" @click="router.push('/search')">全部</button>
        </header>
        <div class="recommend__grid">
          <article
            v-for="card in recommends"
            :key="card.id"
            class="rec-card"
            @click="router.push(card.to)"
          >
            <div class="rec-card__cover" aria-hidden="true" />
            <div class="rec-card__row">
              <span class="rec-card__title">{{ card.title }}</span>
              <button class="rec-card__pill" type="button" @click.stop="router.push(card.to)">
                {{ card.action }}
              </button>
            </div>
            <p class="rec-card__meta">{{ card.meta }}</p>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.home {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft-cloud: #f5f5f5;
  --nike-mute: #707072;
  --nike-hairline-soft: #e5e5e5;

  min-height: 100%;
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.home__header {
  height: 68px;
  padding: 14px 18px;
  background: var(--nike-canvas);
  border-bottom: 1px solid var(--nike-hairline-soft);
  display: flex;
  align-items: center;
  gap: 12px;
}

.home__copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;

  h1,
  p {
    margin: 0;
  }

  h1 {
    font-size: 18px;
    line-height: 1.25;
    font-weight: 800;
  }

  p {
    color: var(--nike-mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 500;
  }
}

.icon-button {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  display: grid;
  place-items: center;
  flex: none;
  cursor: pointer;
}

.home__content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.search-pill {
  width: 100%;
  height: 44px;
  border: 0;
  border-radius: 24px;
  padding: 0 16px;
  background: var(--nike-soft-cloud);
  color: var(--nike-mute);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-align: left;

  span {
    min-width: 0;
    flex: 1;
    color: var(--nike-mute);
    font-size: 14px;
    line-height: 1.25;
    font-weight: 500;
  }
}

.hero {
  height: 184px;
  border-radius: 0;
  background-color: var(--nike-ink);
  background-size: cover;
  background-position: center;
  overflow: hidden;
  cursor: pointer;

  &__overlay {
    height: 100%;
    padding: 18px;
    background: rgba(17, 17, 17, 0.2);
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    gap: 10px;
  }

  &__title {
    color: #fff;
    font-size: 34px;
    font-weight: 900;
    line-height: 1.25;
    letter-spacing: 0;
  }

  &__sub {
    margin: 0;
    color: #fff;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.25;
  }
}

.quick {
  display: flex;
  align-items: stretch;
  gap: 8px;

  &__item {
    flex: 1;
    min-width: 0;
    height: 82px;
    border: 0;
    border-radius: 16px;
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    cursor: pointer;

    span {
      font-size: 11px;
      font-weight: 700;
      line-height: 1.25;
    }
  }
}

.recommend {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__head h2 {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 800;
    line-height: 1.25;
  }

  &__more {
    border: 0;
    background: transparent;
    color: var(--nike-mute);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
}

.rec-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;

  &__cover {
    height: 112px;
    border-radius: 14px;
    background: var(--nike-soft-cloud);
  }

  &__row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__title {
    flex: 1;
    min-width: 0;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.25;
  }

  &__pill {
    flex: none;
    height: 40px;
    padding: 8px 14px;
    border: 1px solid var(--nike-hairline-soft);
    border-radius: 999px;
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
    font-size: 13px;
    font-weight: 700;
    line-height: 1.25;
    cursor: pointer;
  }

  &__meta {
    margin: 0;
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 500;
    line-height: 1.25;
  }
}
</style>
