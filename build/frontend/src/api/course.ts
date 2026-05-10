import request from '@/utils/request';

export interface CourseDetail {
  id: number;
  name: string;
  studioId: number;
  studioName: string;
  style: string;
  difficulty: string;
  audience: string;
  durationMin: number;
  intensity: number;
  price: number;
  frequency: string;
  intro: string;
  coverDesc: string;
  coachId: number;
  coachName: string;
}

export interface CoachDetail {
  id: number;
  name: string;
  studioId: number;
  studioName: string;
  style: string;
  teachStyle: string;
  intro: string;
  ratingAvg: number;
  reviewCount: number;
  works: Array<{ id: number; type: 'image' | 'video'; title: string }>;
  courses: Array<{ id: number; name: string; difficulty: string }>;
  availableSlots: Array<{ day: string; time: string }>;
}

export const fetchCourseDetail = (id: number) =>
  request.get<unknown, CourseDetail>(`/courses/${id}`);

export const fetchCoachDetail = (id: number) =>
  request.get<unknown, CoachDetail>(`/coaches/${id}`);
