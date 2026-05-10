import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

export type FavoriteTargetType = 'studio' | 'course' | 'coach' | 'workshop';

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

  const toggle = (item: Omit<FavoriteItem, 'ts'>) => {
    const idx = items.value.findIndex(
      (it) => it.targetType === item.targetType && it.targetId === item.targetId
    );
    if (idx >= 0) items.value.splice(idx, 1);
    else items.value.unshift({ ...item, ts: Date.now() });
    persist();
  };

  const groupedByType = computed(() => {
    const g: Record<FavoriteTargetType, FavoriteItem[]> = {
      studio: [],
      course: [],
      coach: [],
      workshop: []
    };
    items.value.forEach((it) => g[it.targetType].push(it));
    return g;
  });

  return { items, isFav, toggle, groupedByType };
});
