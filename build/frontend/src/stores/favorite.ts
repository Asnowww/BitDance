import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { fetchFavorites, toggleFavorite } from '@/api/favorite';
import type { FavoriteTargetType } from '@/api/favorite';

export interface FavoriteItem {
  targetType: FavoriteTargetType;
  targetId: number;
  title: string;
  cover?: string;
  subtitle?: string;
  ts: number;
}

const KEY = 'bitdance_favorites';

const load = (): FavoriteItem[] => {
  try {
    return JSON.parse(localStorage.getItem(KEY) ?? '[]') as FavoriteItem[];
  } catch {
    return [];
  }
};

export const useFavoriteStore = defineStore('favorite', () => {
  const items = ref<FavoriteItem[]>(load());

  const persist = () => localStorage.setItem(KEY, JSON.stringify(items.value));

  const isFav = (type: FavoriteTargetType, id: number) =>
    items.value.some((it) => it.targetType === type && it.targetId === id);

  const toggle = async (item: Omit<FavoriteItem, 'ts'>) => {
    const { favored } = await toggleFavorite(item.targetType, item.targetId);
    const idx = items.value.findIndex(
      (it) => it.targetType === item.targetType && it.targetId === item.targetId
    );
    if (favored && idx < 0) items.value.unshift({ ...item, ts: Date.now() });
    if (!favored && idx >= 0) items.value.splice(idx, 1);
    persist();
  };

  const sync = async () => {
    const remote = await fetchFavorites();
    const previous = new Map(items.value.map((item) => [`${item.targetType}-${item.targetId}`, item]));
    items.value = remote.map((item) => {
      const cached = previous.get(`${item.targetType}-${item.targetId}`);
      return cached ?? {
        targetType: item.targetType,
        targetId: item.targetId,
        title: item.card?.title ?? `${item.targetType} #${item.targetId}`,
        cover: item.card?.coverUrl ?? undefined,
        subtitle: item.card?.subtitle ?? undefined,
        ts: Date.parse(item.createdAt)
      };
    });
    persist();
  };

  const groupedByType = computed(() => {
    const g: Record<FavoriteTargetType, FavoriteItem[]> = {
      studio: [],
      course: [],
      coach: [],
      workshop: [],
      content_post: []
    };
    items.value.forEach((it) => g[it.targetType].push(it));
    return g;
  });

  return { items, isFav, toggle, sync, groupedByType };
});
