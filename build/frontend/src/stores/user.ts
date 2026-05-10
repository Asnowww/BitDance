import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import request, { setToken, clearToken, getToken } from '@/utils/request';

export interface UserProfile {
  id: number;
  phone: string;
  nickname: string;
  avatar: string;
  roles: string[];
}

const PROFILE_KEY = 'bitdance_profile';

const loadProfile = (): UserProfile | null => {
  const raw = localStorage.getItem(PROFILE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as UserProfile;
  } catch {
    return null;
  }
};

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(loadProfile());
  const token = ref<string>(getToken());

  const isLogin = computed(() => Boolean(token.value && profile.value));
  const isCoach = computed(() => profile.value?.roles?.includes('coach') ?? false);

  const sendSmsCode = async (phone: string) => {
    return request.post<unknown, { sent: boolean; expiresIn: number }>('/auth/sms/send', { phone });
  };

  const login = async (phone: string, code: string) => {
    const data = await request.post<unknown, { token: string; user: UserProfile }>('/auth/login', {
      phone,
      code
    });
    token.value = data.token;
    profile.value = data.user;
    setToken(data.token);
    localStorage.setItem(PROFILE_KEY, JSON.stringify(data.user));
    return data.user;
  };

  const logout = () => {
    token.value = '';
    profile.value = null;
    clearToken();
    localStorage.removeItem(PROFILE_KEY);
  };

  return { profile, token, isLogin, isCoach, sendSmsCode, login, logout };
});
