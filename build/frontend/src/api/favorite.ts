import request from '@/utils/request';

export type FavoriteTargetType = 'studio' | 'course' | 'coach' | 'workshop' | 'content_post';

export interface FavoriteCardDto {
  title: string;
  subtitle: string;
  coverUrl?: string | null;
  path: string;
  actionText: string;
}

export interface FavoriteDto {
  id: number;
  targetType: FavoriteTargetType;
  targetId: number;
  createdAt: string;
  card?: FavoriteCardDto | null;
}

export const toggleFavorite = (targetType: FavoriteTargetType, targetId: number) =>
  request.post<unknown, { favored: boolean }>('/h5/favorites', { targetType, targetId });

export const fetchFavorites = (targetType?: FavoriteTargetType) =>
  request.get<unknown, FavoriteDto[]>('/h5/favorites', { params: { targetType } });

export const checkFavorite = (targetType: FavoriteTargetType, targetId: number) =>
  request.get<unknown, { favored: boolean }>('/h5/favorites/check', {
    params: { targetType, targetId }
  });
