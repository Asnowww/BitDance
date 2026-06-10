import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import {
  fetchProfile,
  updateProfile as updateProfileApi,
  type PrivacySettings,
  type ProfileResponse,
  type StylePreference,
  type UpdateProfileRequest
} from '@/api/profile';
import request, { clearToken, getToken, setToken } from '@/utils/request';

export interface UserProfile {
  id: number;
  phone: string;
  nickname: string;
  avatar: string | null;
  roles: string[];
}

const PROFILE_KEY = 'bitdance_profile';
const ROLE_KEY = 'bitdance_active_role';

const normalizeUser = (user: UserProfile): UserProfile => ({
  ...user,
  roles: (user.roles ?? []).map((role) => role.toUpperCase())
});

const loadProfile = (): UserProfile | null => {
  const raw = localStorage.getItem(PROFILE_KEY);
  if (!raw) return null;
  try {
    return normalizeUser(JSON.parse(raw) as UserProfile);
  } catch {
    return null;
  }
};

const defaultPrivacy: PrivacySettings = {
  profileVisibility: 'public',
  growthVisibility: 'public',
  practiceVisibility: 'public',
  contentVisibility: 'public'
};

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(loadProfile());
  const detail = ref<ProfileResponse | null>(null);
  const token = ref<string>(getToken());

  const isLogin = computed(() => Boolean(token.value && profile.value));
  const roleSet = computed(() => new Set((profile.value?.roles ?? []).map((role) => role.toUpperCase())));
  const isCoach = computed(() => roleSet.value.has('COACH'));
  const isStudioAdmin = computed(() => roleSet.value.has('STUDIO_ADMIN'));
  const isPlatformAdmin = computed(() => roleSet.value.has('PLATFORM_ADMIN'));

  const activeRole = ref<'user' | 'coach'>(
    (localStorage.getItem(ROLE_KEY) as 'user' | 'coach') ?? 'user'
  );

  const updateProfile = (patch: Partial<UserProfile>) => {
    if (!profile.value) return;
    profile.value = normalizeUser({ ...profile.value, ...patch });
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value));
  };

  const switchRole = (next: 'user' | 'coach') => {
    if (next === 'coach' && !isCoach.value) return false;
    activeRole.value = next;
    localStorage.setItem(ROLE_KEY, next);
    return true;
  };

  const privacy = computed(() => detail.value?.privacy ?? defaultPrivacy);
  const preferences = computed(() => ({
    styles: detail.value?.styles?.map((style) => style.name) ?? [],
    level: detail.value?.currentLevel ?? '',
    goal: detail.value?.learningGoal ?? ''
  }));

  const sendSmsCode = async (phone: string) => {
    return request.post<unknown, { sent: boolean; expiresIn: number }>('/auth/sms/send', { phone });
  };

  const applyLogin = (data: { token: string; user: UserProfile }) => {
    token.value = data.token;
    profile.value = normalizeUser(data.user);
    setToken(data.token);
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value));
    return profile.value;
  };

  const refreshProfile = async () => {
    if (!token.value) return null;
    const data = await fetchProfile();
    detail.value = data;
    if (profile.value) {
      updateProfile({
        id: data.userId,
        nickname: data.nickname || profile.value.nickname,
        avatar: data.avatarAssetId == null ? profile.value.avatar : String(data.avatarAssetId)
      });
    }
    return data;
  };

  const saveProfileDetail = async (body: UpdateProfileRequest) => {
    const data = await updateProfileApi(body);
    detail.value = data;
    if (profile.value) {
      updateProfile({
        id: data.userId,
        nickname: data.nickname || profile.value.nickname,
        avatar: data.avatarAssetId == null ? profile.value.avatar : String(data.avatarAssetId)
      });
    }
    return data;
  };

  const updatePreferences = async (next: { styles: StylePreference[]; level: string; goal: string }) =>
    saveProfileDetail({
      ...(detail.value ?? {}),
      currentLevel: next.level,
      learningGoal: next.goal,
      styles: next.styles
    });

  const updatePrivacy = async (next: PrivacySettings) =>
    saveProfileDetail({
      ...(detail.value ?? {}),
      privacy: next
    });

  const login = async (phone: string, code: string) => {
    const data = await request.post<unknown, { token: string; user: UserProfile }>('/auth/login', {
      phone,
      code
    });
    applyLogin(data);
    await refreshProfile();
    return profile.value;
  };

  const loginWithPassword = async (phone: string, password: string) => {
    const data = await request.post<unknown, { token: string; user: UserProfile }>(
      '/auth/login/password',
      { phone, password }
    );
    applyLogin(data);
    await refreshProfile();
    return profile.value;
  };

  const loginWithWechat = async (code = 'dev_mock_coach') => {
    const data = await request.post<unknown, { token: string; user: UserProfile }>(
      '/auth/login/wechat',
      { code }
    );
    applyLogin(data);
    await refreshProfile();
    return profile.value;
  };

  const logout = () => {
    token.value = '';
    profile.value = null;
    detail.value = null;
    clearToken();
    localStorage.removeItem(PROFILE_KEY);
  };

  return {
    profile,
    detail,
    token,
    isLogin,
    isCoach,
    isStudioAdmin,
    isPlatformAdmin,
    activeRole,
    preferences,
    privacy,
    sendSmsCode,
    login,
    loginWithPassword,
    loginWithWechat,
    refreshProfile,
    saveProfileDetail,
    logout,
    switchRole,
    updateProfile,
    updatePreferences,
    updatePrivacy
  };
});
