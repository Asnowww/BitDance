import request from '@/utils/request';

export interface SocialAccount {
  id: number;
  userId: number;
  platform: string;
  accountName: string;
  profileUrl?: string;
  isPublic: boolean;
}

export const fetchMySocialAccounts = () =>
  request.get<unknown, SocialAccount[]>('/h5/social-accounts');

export const updateSocialAccount = (id: number, isPublic: boolean) =>
  request.put<unknown, SocialAccount>(`/h5/social-accounts/${id}`, { isPublic });

export const fetchPublicSocialAccounts = (userId: number) =>
  request.get<unknown, SocialAccount[]>(`/public/users/${userId}/social-accounts`);
