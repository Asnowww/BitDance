import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import request from '@/utils/request';
import {
  fetchCoachMe,
  fetchMyStudioClaims,
  type CoachMe,
  type StudioClaim
} from '@/api/coachOps';

export type OpsRole = 'studio_admin' | 'coach' | 'platform';

interface MeSummary {
  id: number;
  phone: string;
  nickname: string;
  avatar: string;
  roles: string[];
}

const ROLE_KEY = 'bitdance_ops_role';
const STUDIO_KEY = 'bitdance_ops_studio';

/**
 * 运营端上下文:当前工作台角色、可管理的舞室、教练身份。
 * 角色资格来自后端 /h5/me 的角色绑定,工作台切换仅在有资格的角色之间进行。
 */
export const useOpsStore = defineStore('ops', () => {
  const me = ref<MeSummary | null>(null);
  const coachMe = ref<CoachMe | null>(null);
  const myClaims = ref<StudioClaim[]>([]);
  const loaded = ref(false);
  const loading = ref(false);

  const activeRole = ref<OpsRole>((localStorage.getItem(ROLE_KEY) as OpsRole) || 'coach');
  const studioId = ref<number | null>(
    localStorage.getItem(STUDIO_KEY) ? Number(localStorage.getItem(STUDIO_KEY)) : null
  );

  const isStudioAdmin = computed(() => me.value?.roles?.includes('STUDIO_ADMIN') ?? false);
  const isPlatformAdmin = computed(() => me.value?.roles?.includes('PLATFORM_ADMIN') ?? false);
  const isCertifiedCoach = computed(() => coachMe.value?.certified ?? false);

  /** 已审核通过的舞室(认领/入驻成功) */
  const approvedStudioIds = computed(() => {
    const ids = myClaims.value
      .filter((c) => c.claimStatus === 'approved' && c.studioId)
      .map((c) => c.studioId as number);
    return [...new Set(ids)];
  });

  const availableRoles = computed<OpsRole[]>(() => {
    const roles: OpsRole[] = [];
    if (isStudioAdmin.value) roles.push('studio_admin');
    roles.push('coach');
    if (isPlatformAdmin.value) roles.push('platform');
    return roles;
  });

  const setRole = (role: OpsRole) => {
    activeRole.value = role;
    localStorage.setItem(ROLE_KEY, role);
  };

  const setStudio = (id: number | null) => {
    studioId.value = id;
    if (id) localStorage.setItem(STUDIO_KEY, String(id));
    else localStorage.removeItem(STUDIO_KEY);
  };

  /** 拉取运营身份;失败时各项保持空值,页面按未开通处理 */
  const refresh = async (force = false) => {
    if (loading.value || (loaded.value && !force)) return;
    loading.value = true;
    try {
      const [meRes, coachRes, claimRes] = await Promise.allSettled([
        request.get<unknown, MeSummary>('/h5/me'),
        fetchCoachMe(),
        fetchMyStudioClaims()
      ]);
      if (meRes.status === 'fulfilled') me.value = meRes.value;
      if (coachRes.status === 'fulfilled') coachMe.value = coachRes.value;
      if (claimRes.status === 'fulfilled') myClaims.value = claimRes.value;

      // 角色资格校验与默认值
      if (!availableRoles.value.includes(activeRole.value)) {
        setRole(isStudioAdmin.value ? 'studio_admin' : 'coach');
      }
      if (approvedStudioIds.value.length > 0) {
        if (!studioId.value || !approvedStudioIds.value.includes(studioId.value)) {
          setStudio(approvedStudioIds.value[0]);
        }
      }
      loaded.value = true;
    } finally {
      loading.value = false;
    }
  };

  return {
    me,
    coachMe,
    myClaims,
    loaded,
    loading,
    activeRole,
    studioId,
    isStudioAdmin,
    isPlatformAdmin,
    isCertifiedCoach,
    approvedStudioIds,
    availableRoles,
    setRole,
    setStudio,
    refresh
  };
});
