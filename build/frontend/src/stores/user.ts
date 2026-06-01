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

  const ROLE_KEY = 'bitdance_active_role';
  const activeRole = ref<'user' | 'coach'>(
    (localStorage.getItem(ROLE_KEY) as 'user' | 'coach') ?? 'user'
  );

  const switchRole = (next: 'user' | 'coach') => {
    activeRole.value = next;
    localStorage.setItem(ROLE_KEY, next);
    if (next === 'coach' && !isCoach.value && profile.value) {
      const roles = [...(profile.value.roles ?? [])];
      if (!roles.includes('coach')) roles.push('coach');
      profile.value = { ...profile.value, roles };
      localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value));
    }
  };

  const updateProfile = (patch: Partial<UserProfile>) => {
    if (!profile.value) return;
    profile.value = { ...profile.value, ...patch };
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value));
  };

  const PREF_KEY = 'bitdance_preferences';
  const preferences = ref<{ styles: string[]; level: string; goal: string }>(
    JSON.parse(localStorage.getItem(PREF_KEY) ?? '{"styles":[],"level":"","goal":""}')
  );
  const updatePreferences = (next: typeof preferences.value) => {
    preferences.value = next;
    localStorage.setItem(PREF_KEY, JSON.stringify(next));
  };

  const PRIVACY_KEY = 'bitdance_privacy';
  const privacy = ref<{ profile: string; checkin: string; practice: string; community: string }>(
    JSON.parse(
      localStorage.getItem(PRIVACY_KEY) ??
        '{"profile":"public","checkin":"public","practice":"public","community":"public"}'
    )
  );
  const updatePrivacy = (next: typeof privacy.value) => {
    privacy.value = next;
    localStorage.setItem(PRIVACY_KEY, JSON.stringify(next));
  };

  const sendSmsCode = async (phone: string) => {
    return request.post<unknown, { sent: boolean; expiresIn: number }>('/auth/sms/send', { phone });
  };

  const applyLogin = (data: { token: string; user: UserProfile }) => {
    token.value = data.token;
    profile.value = data.user;
    setToken(data.token);
    localStorage.setItem(PROFILE_KEY, JSON.stringify(data.user));
    return data.user;
  };

  const login = async (phone: string, code: string) => {
    const data = await request.post<unknown, { token: string; user: UserProfile }>('/auth/login', {
      phone,
      code
    });
    return applyLogin(data);
  };

  const loginWithPassword = async (phone: string, password: string) => {
    const data = await request.post<unknown, { token: string; user: UserProfile }>(
      '/auth/login/password',
      { phone, password }
    );
    return applyLogin(data);
  };

  const logout = () => {
    token.value = '';
    profile.value = null;
    clearToken();
    localStorage.removeItem(PROFILE_KEY);
  };

  return {
    profile,
    token,
    isLogin,
    isCoach,
    activeRole,
    preferences,
    privacy,
    sendSmsCode,
    login,
    loginWithPassword,
    logout,
    switchRole,
    updateProfile,
    updatePreferences,
    updatePrivacy
  };
});
