import request from '@/utils/request';

export type FavoriteTargetType = 'studio' | 'course' | 'coach' | 'workshop';

export interface FavoriteDto {
  id: number;
  targetType: FavoriteTargetType;
  targetId: number;
  createdAt: string;
}

export const toggleFavorite = (targetType: FavoriteTargetType, targetId: number) =>
  request.post<unknown, { favored: boolean }>('/h5/favorites', { targetType, targetId });

export const fetchFavorites = (targetType?: FavoriteTargetType, options?: { silentError?: boolean }) =>
  request.get<unknown, FavoriteDto[]>('/h5/favorites', {
    params: { targetType },
    // M1 搜索星标：未登录时允许页面降级为列表自带 favored，不用全局 toast 打断搜索结果。
    silentError: options?.silentError
  });

export const checkFavorite = (targetType: FavoriteTargetType, targetId: number) =>
  request.get<unknown, { favored: boolean }>('/h5/favorites/check', {
    params: { targetType, targetId }
  });
