import request from '@/utils/request';

export interface CourseDetail {
  id: number;
  studioId: number;
  coachId: number;
  danceStyleId: number;
  courseName: string;
  difficultyLevel: string;
  targetAudience: string;
  priceAmount: number;
  durationMinutes: number;
  intensityLevel: string;
  courseType: string;
  zeroBasicFriendly: boolean;
  description: string;
  coverAssetId?: number;
  status: string;
  favored: boolean;
}

export interface CoachDetail {
  id: number;
  userId: number;
  displayName: string;
  intro: string;
  teachingStyle: string;
  availableTimeSlots: string;
  certificationStatus: string;
  homeStudioId?: number;
  coverAssetId?: number;
  avgRating: number;
  styles: Array<{ danceStyleId: number; proficiencyLevel: string }>;
  favored: boolean;
}

export interface CourseCard {
  id: number;
  studioId: number;
  coachId: number;
  danceStyleId: number;
  courseName: string;
  difficultyLevel: string;
  priceAmount: number;
  durationMinutes: number;
  zeroBasicFriendly: boolean;
  coverAssetId?: number;
}

export const fetchCourseDetail = (id: number) =>
  request.get<unknown, CourseDetail>(`/public/courses/${id}`);

export const fetchCoachDetail = (id: number) =>
  request.get<unknown, CoachDetail>(`/public/coaches/${id}`);

export const fetchCoachCourses = (coachId: number) =>
  // M1 教练详情：复用后端公开接口展示该老师真实可预约课程，避免页面继续显示静态课程卡。
  request.get<unknown, CourseCard[]>(`/public/coaches/${coachId}/courses`);
