import request from '@/utils/request';

export interface PrivacySettings {
  profileVisibility: string;
  growthVisibility: string;
  practiceVisibility: string;
  contentVisibility: string;
}

export interface StylePreference {
  danceStyleId: number;
  name: string;
  skillLevel: string;
  isPrimary: boolean;
}

export interface ProfileResponse {
  userId: number;
  nickname: string;
  avatarAssetId: number | null;
  gender: string | null;
  birthday: string | null;
  bio: string | null;
  cityId: number | null;
  currentLevel: string | null;
  learningGoal: string | null;
  roles: string[];
  styles: StylePreference[];
  privacy: PrivacySettings;
}

export interface UpdateProfileRequest {
  nickname?: string;
  avatarAssetId?: number | null;
  gender?: string | null;
  birthday?: string | null;
  bio?: string | null;
  cityId?: number | null;
  currentLevel?: string | null;
  learningGoal?: string | null;
  styles?: StylePreference[];
  privacy?: PrivacySettings;
}

export const fetchProfile = () => request.get<unknown, ProfileResponse>('/h5/profile');

export const updateProfile = (body: UpdateProfileRequest) =>
  request.put<unknown, ProfileResponse>('/h5/profile', body);
