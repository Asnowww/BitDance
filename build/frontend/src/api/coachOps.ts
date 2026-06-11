import request from '@/utils/request';

// ========== 通用 ==========

export interface PageResp<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// ========== 媒体上传 ==========

export interface MediaAsset {
  assetId: number;
  fileName: string;
  mimeType: string;
  size: number;
  contentUrl: string;
}

export const uploadMediaAsset = (file: File, bizType?: string) => {
  const form = new FormData();
  form.append('file', file);
  if (bizType) form.append('bizType', bizType);
  return request.post<unknown, MediaAsset>('/h5/media-assets', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const mediaContentUrl = (assetId?: number | null) =>
  assetId ? `${import.meta.env.VITE_API_BASE}/public/media-assets/${assetId}/content` : '';

// ========== 舞室入驻与认领 ==========

export interface StudioClaim {
  id: number;
  studioId: number | null;
  applicantUserId: number;
  claimType: 'owner_claim' | 'operator_claim' | 'new_studio';
  claimStatus: 'pending' | 'approved' | 'rejected' | string;
  businessLicenseAssetId: number | null;
  submittedRemark: string | null;
  reviewedByUserId: number | null;
  reviewedAt: string | null;
  reviewRemark: string | null;
  createdAt: string;
}

export interface SubmitClaimBody {
  studioId?: number;
  claimType: 'owner_claim' | 'operator_claim' | 'new_studio';
  businessLicenseAssetId?: number;
  submittedRemark?: string;
  studioName?: string;
  brandName?: string;
  cityId?: number;
  businessDistrictId?: number;
  address?: string;
  longitude?: number;
  latitude?: number;
  contactPhone?: string;
  intro?: string;
  businessHours?: string;
  coverAssetId?: number;
}

export const submitStudioClaim = (body: SubmitClaimBody) =>
  request.post<unknown, StudioClaim>('/h5/studio-claims', body);

export const submitNewStudioClaim = (body: SubmitClaimBody) =>
  request.post<unknown, StudioClaim>('/h5/studio-claims/new-studio', body);

export const fetchMyStudioClaims = () =>
  request.get<unknown, StudioClaim[]>('/h5/studio-claims/mine');

// ========== 教练资质 ==========

export interface CoachCertification {
  id: number;
  userId: number;
  applicationType: 'independent' | 'studio_affiliated';
  coachType: 'full_time' | 'signed' | 'freelance';
  applicationStatus: 'pending' | 'approved' | 'rejected' | string;
  remark: string | null;
  reviewedByUserId: number | null;
  reviewedAt: string | null;
  reviewRemark: string | null;
  createdAt: string;
}

export const submitCertification = (body: {
  applicationType: 'independent' | 'studio_affiliated';
  coachType: 'full_time' | 'signed' | 'freelance';
  remark?: string;
}) => request.post<unknown, CoachCertification>('/h5/coach/certifications', body);

export const fetchMyCertifications = () =>
  request.get<unknown, CoachCertification[]>('/h5/coach/certifications/mine');

// ========== 教练身份与看板 ==========

export interface CoachMe {
  certified: boolean;
  coachId: number | null;
  displayName: string | null;
  intro: string | null;
  teachingStyle: string | null;
  certificationStatus: string | null;
  homeStudioId: number | null;
  coverAssetId: number | null;
  avgRating: number | null;
  activeStudioIds: number[];
}

export const fetchCoachMe = () => request.get<unknown, CoachMe>('/h5/coach/me');

export const updateCoachProfile = (body: {
  displayName?: string;
  intro?: string;
  teachingStyle?: string;
  availableTimeSlots?: string;
  coverAssetId?: number;
}) => request.put<unknown, CoachMe>('/h5/coach/me/profile', body);

export interface OpsDashboard {
  monthIncome: number;
  monthOrderCount: number;
  checkinCount: number;
  refundCount: number;
  courseBookingCount: number;
  workshopSignupCount: number;
  pendingReviewReplies: number;
  avgRating: number | null;
  monthSessions: number;
  monthWorkshopOrders: number;
  ratingCount: number;
}

export const fetchOpsDashboard = (params?: { role?: string; studioId?: number }) =>
  request.get<unknown, OpsDashboard>('/h5/coach/dashboard', { params });

// ========== 教练邀请(教练侧) ==========

export interface CoachRelation {
  id: number;
  studioId: number;
  coachId: number;
  relationType: 'full_time' | 'signed' | 'independent';
  relationStatus: 'pending' | 'active' | 'inactive' | 'terminated';
  settlementMode: 'ratio' | 'fixed' | null;
  settlementRatio: number | null;
  invitedByUserId: number | null;
  approvedByUserId: number | null;
  effectiveFrom: string | null;
  effectiveTo: string | null;
  createdAt: string;
}

export const fetchMyInvitations = () =>
  request.get<unknown, CoachRelation[]>('/h5/coach/invitations');

export const acceptInvitation = (id: number) =>
  request.post<unknown, CoachRelation>(`/h5/coach/invitations/${id}/accept`);

export const rejectInvitation = (id: number) =>
  request.post<unknown, CoachRelation>(`/h5/coach/invitations/${id}/reject`);

// ========== 教练关系(商家侧) ==========

export const inviteCoach = (body: {
  studioId: number;
  coachId: number;
  relationType: 'full_time' | 'signed' | 'independent';
  settlementMode?: 'ratio' | 'fixed';
  settlementRatio?: number;
  effectiveFrom?: string;
  effectiveTo?: string;
}) => request.post<unknown, CoachRelation>('/merchant/coach-relations', body);

export const updateCoachRelation = (
  id: number,
  body: {
    relationStatus?: 'pending' | 'active' | 'inactive' | 'terminated';
    relationType?: 'full_time' | 'signed' | 'independent';
    settlementRatio?: number;
    effectiveTo?: string;
  }
) => request.put<unknown, CoachRelation>(`/merchant/coach-relations/${id}`, body);

export const fetchStudioCoachRelations = (studioId: number) =>
  request.get<unknown, CoachRelation[]>('/merchant/coach-relations', { params: { studioId } });

// ========== 商家课程 ==========

export interface MerchantCourse {
  id: number;
  studioId: number;
  coachId: number | null;
  danceStyleId: number;
  courseName: string;
  difficultyLevel: string;
  priceAmount: number | null;
  trialEnabled: boolean | null;
  trialPriceAmount: number | null;
  trialCapacity: number | null;
  durationMinutes: number | null;
  intensityLevel: string | null;
  courseType: string | null;
  zeroBasicFriendly: boolean | null;
  description: string | null;
  coverAssetId: number | null;
  status: string;
}

export interface MerchantCourseBody {
  studioId: number;
  coachId?: number;
  danceStyleId: number;
  courseName: string;
  difficultyLevel: string;
  priceAmount?: number;
  trialEnabled?: boolean;
  trialPriceAmount?: number;
  trialCapacity?: number;
  durationMinutes?: number;
  intensityLevel?: string;
  courseType?: string;
  zeroBasicFriendly?: boolean;
  description?: string;
  coverAssetId?: number;
}

export const createMerchantCourse = (body: MerchantCourseBody) =>
  request.post<unknown, MerchantCourse>('/merchant/courses', body);

export const updateMerchantCourse = (id: number, body: MerchantCourseBody) =>
  request.put<unknown, MerchantCourse>(`/merchant/courses/${id}`, body);

export const publishMerchantCourse = (id: number) =>
  request.post<unknown, MerchantCourse>(`/merchant/courses/${id}/publish`);

export const offlineMerchantCourse = (id: number) =>
  request.post<unknown, MerchantCourse>(`/merchant/courses/${id}/offline`);

export const fetchMerchantCourses = (studioId: number, status?: string) =>
  request.get<unknown, MerchantCourse[]>('/merchant/courses', { params: { studioId, status } });

// ========== 商家课表 ==========

export interface ScheduleItem {
  id: number;
  courseId: number;
  studioId: number;
  coachId: number | null;
  courseName?: string;
  coachName?: string;
  classroomName: string | null;
  startAt: string;
  endAt: string;
  capacity: number | null;
  bookedCount?: number | null;
  scheduleStatus?: string;
  status?: string;
}

export interface ScheduleBody {
  courseId: number;
  studioId: number;
  coachId?: number;
  classroomName?: string;
  startAt: string;
  endAt: string;
  capacity?: number;
}

export const createSchedule = (body: ScheduleBody) =>
  request.post<unknown, ScheduleItem>('/merchant/course-schedules', body);

export const updateSchedule = (id: number, body: ScheduleBody) =>
  request.put<unknown, ScheduleItem>(`/merchant/course-schedules/${id}`, body);

export const cancelSchedule = (id: number) =>
  request.post<unknown, ScheduleItem>(`/merchant/course-schedules/${id}/cancel`);

export const fetchWeekSchedules = (studioId: number, from: string, to: string) =>
  request.get<unknown, ScheduleItem[]>('/merchant/course-schedules/week', {
    params: { studioId, from, to }
  });

export const fetchScheduleBookings = (scheduleId: number) =>
  request.get<unknown, CourseOrder[]>(`/merchant/course-schedules/${scheduleId}/bookings`);

// ========== 正式课订单 ==========

export type CourseOrderStatus =
  | 'pending_payment'
  | 'paid'
  | 'refund_requested'
  | 'refunded'
  | 'refund_rejected'
  | 'checked_in'
  | 'completed'
  | 'canceled';

export interface CourseOrder {
  id: number;
  orderNo: string;
  courseId: number;
  courseScheduleId: number;
  studioId: number;
  coachId: number | null;
  userId: number;
  amountPayable: number;
  amountPaid: number;
  orderStatus: CourseOrderStatus | string;
  paymentTxnNo: string | null;
  checkinCode: string | null;
  paidAt: string | null;
  canceledAt: string | null;
  refundRequestedAt: string | null;
  refundedAt: string | null;
  completedAt: string | null;
  createdAt: string;
}

export const createCourseOrder = (body: { courseId: number; courseScheduleId: number }) =>
  request.post<unknown, CourseOrder>('/h5/course-orders', body);

export const payCourseOrder = (id: number) =>
  request.post<unknown, CourseOrder>(`/h5/course-orders/${id}/pay`);

export const cancelCourseOrder = (id: number) =>
  request.post<unknown, CourseOrder>(`/h5/course-orders/${id}/cancel`);

export const requestCourseRefund = (id: number, reason?: string) =>
  request.post<unknown, CourseOrder>(`/h5/course-orders/${id}/refund-request`, { reason });

export const fetchMyCourseOrders = () =>
  request.get<unknown, CourseOrder[]>('/h5/course-orders/mine');

export const fetchMerchantCourseOrders = (studioId: number, status?: string) =>
  request.get<unknown, CourseOrder[]>('/merchant/course-orders', { params: { studioId, status } });

export const checkinCourseOrder = (id: number, code: string) =>
  request.post<unknown, CourseOrder>(`/merchant/course-orders/${id}/checkin`, { code });

export const checkinCourseByCode = (code: string) =>
  request.post<unknown, CourseOrder>('/merchant/course-orders/checkin-by-code', null, {
    params: { code }
  });

export const fetchCourseCheckinHistory = (studioId: number) =>
  request.get<unknown, CourseOrder[]>('/merchant/course-checkins/history', {
    params: { studioId }
  });

export interface CourseRefund {
  id: number;
  courseOrderId: number;
  requesterUserId: number;
  refundReason: string | null;
  requestStatus: 'pending' | 'approved' | 'rejected' | string;
  reviewedByUserId: number | null;
  reviewedAt: string | null;
  reviewRemark: string | null;
  createdAt: string;
}

export const fetchCourseRefunds = (studioId: number, status?: string) =>
  request.get<unknown, CourseRefund[]>('/merchant/course-refunds', {
    params: { studioId, status }
  });

export const approveCourseRefund = (id: number, remark?: string) =>
  request.post<unknown, CourseRefund>(`/merchant/course-refunds/${id}/approve`, { remark });

export const rejectCourseRefund = (id: number, remark?: string) =>
  request.post<unknown, CourseRefund>(`/merchant/course-refunds/${id}/reject`, { remark });

// ========== Workshop(创建侧) ==========

export interface WorkshopSession {
  id: number;
  workshopId: number;
  sessionName: string | null;
  startAt: string;
  endAt: string;
  capacity: number;
  priceAmount: number | null;
  soldCount: number | null;
  checkinCount: number | null;
  sessionStatus: string;
}

export interface MerchantWorkshop {
  id: number;
  studioId: number | null;
  coachId: number | null;
  cityId: number;
  danceStyleId: number | null;
  workshopName: string;
  coverAssetId: number | null;
  intro: string | null;
  address: string;
  locationName: string;
  priceAmount: number;
  minPeople: number | null;
  maxPeople: number | null;
  signupDeadline: string | null;
  publishStatus: string;
  auditStatus: string;
  sessions: WorkshopSession[];
  favored: boolean;
}

export interface CreateWorkshopBody {
  studioId?: number;
  coachId?: number;
  cityId: number;
  danceStyleId?: number;
  workshopName: string;
  coverAssetId?: number;
  intro?: string;
  address: string;
  locationName: string;
  longitude?: number;
  latitude?: number;
  priceAmount: number;
  minPeople?: number;
  maxPeople?: number;
  signupDeadline?: string;
  sourceType?: 'studio' | 'coach';
}

export const createMerchantWorkshop = (body: CreateWorkshopBody) =>
  request.post<unknown, MerchantWorkshop>('/merchant/workshops', body);

export const publishMerchantWorkshop = (id: number) =>
  request.post<unknown, MerchantWorkshop>(`/merchant/workshops/${id}/publish`);

export const offlineMerchantWorkshop = (id: number) =>
  request.post<unknown, MerchantWorkshop>(`/merchant/workshops/${id}/offline`);

export const approveMerchantWorkshop = (id: number) =>
  request.post<unknown, MerchantWorkshop>(`/merchant/workshops/${id}/approve`);

export const rejectMerchantWorkshop = (id: number) =>
  request.post<unknown, MerchantWorkshop>(`/merchant/workshops/${id}/reject`);

export const addWorkshopSession = (body: {
  workshopId: number;
  sessionName?: string;
  startAt: string;
  endAt: string;
  capacity: number;
  priceAmount?: number;
}) => request.post<unknown, WorkshopSession>('/merchant/workshop-sessions', body);

export const fetchMerchantWorkshops = (studioId: number) =>
  request.get<unknown, MerchantWorkshop[]>('/merchant/workshops', { params: { studioId } });

// ========== Workshop 订单/核销(商家侧) ==========

export interface WorkshopOrder {
  id: number;
  orderNo: string;
  workshopId: number;
  workshopSessionId: number;
  userId: number;
  amountPayable: number;
  amountPaid: number;
  orderStatus: string;
  paymentTxnNo: string | null;
  checkinCode: string | null;
  paidAt: string | null;
  canceledAt: string | null;
  refundedAt: string | null;
  createdAt: string;
}

export const fetchMerchantWorkshopOrders = (params: {
  studioId?: number;
  workshopId?: number;
  status?: string;
}) => request.get<unknown, WorkshopOrder[]>('/merchant/workshop-orders', { params });

export const checkinWorkshopOrder = (orderId: number, code: string) =>
  request.post<unknown, WorkshopOrder>(`/merchant/workshop-orders/${orderId}/checkin`, { code });

export const fetchWorkshopCheckinHistory = (studioId: number) =>
  request.get<unknown, WorkshopOrder[]>('/merchant/workshop-checkins/history', {
    params: { studioId }
  });

// ========== 评价回复与申诉 ==========

export interface ReviewReply {
  id: number;
  reviewId: number;
  replierUserId: number;
  replyContent: string;
  isOfficial: boolean | null;
  createdAt: string;
}

export interface PendingReview {
  id: number;
  userId: number;
  targetType: string;
  targetId: number;
  overallScore: number;
  contentText: string;
  isVerified: boolean | null;
  reviewStatus: string;
  isPinned: boolean | null;
  publishedAt: string | null;
}

export const createReviewReply = (body: {
  reviewId: number;
  replyContent: string;
  isOfficial?: boolean;
}) => request.post<unknown, ReviewReply>('/h5/review-replies', body);

export const deleteReviewReply = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/h5/review-replies/${id}`);

export const fetchRepliesByReview = (reviewId: number) =>
  request.get<unknown, ReviewReply[]>('/public/review-replies', { params: { reviewId } });

export const fetchMyReplies = () => request.get<unknown, ReviewReply[]>('/h5/review-replies/mine');

export const fetchPendingReplyReviews = (studioId: number) =>
  request.get<unknown, PendingReview[]>('/merchant/reviews/pending-reply', {
    params: { studioId }
  });

export interface ReviewAppeal {
  id: number;
  reviewId: number;
  appellantUserId: number;
  appealReason: string;
  appealStatus: 'pending' | 'approved' | 'rejected' | string;
  evidenceNote: string | null;
  reviewedByUserId: number | null;
  reviewedAt: string | null;
  reviewRemark: string | null;
  createdAt: string;
}

export const createReviewAppeal = (body: {
  reviewId: number;
  appealReason: string;
  evidenceNote?: string;
}) => request.post<unknown, ReviewAppeal>('/h5/review-appeals', body);

export const fetchMyAppeals = () => request.get<unknown, ReviewAppeal[]>('/h5/review-appeals/mine');

// ========== 平台审核 ==========

export interface WorkshopAdminItem {
  id: number;
  studioId: number | null;
  coachId: number | null;
  cityId: number;
  workshopName: string;
  priceAmount: number;
  signupDeadline: string | null;
  auditStatus: string;
  publishStatus: string;
}

const platform = '/h5/coach/platform';

export const platformFetchStudioClaims = (status = 'pending', page = 1, pageSize = 20) =>
  request.get<unknown, PageResp<StudioClaim>>(`${platform}/studio-claims`, {
    params: { status, page, pageSize }
  });

export const platformHandleStudioClaim = (id: number, action: 'approve' | 'reject', remark?: string) =>
  request.post<unknown, StudioClaim>(`${platform}/studio-claims/${id}/${action}`, { remark });

export const platformFetchCertifications = (status = 'pending', page = 1, pageSize = 20) =>
  request.get<unknown, PageResp<CoachCertification>>(`${platform}/coach-certifications`, {
    params: { status, page, pageSize }
  });

export const platformHandleCertification = (id: number, action: 'approve' | 'reject', remark?: string) =>
  request.post<unknown, CoachCertification>(`${platform}/coach-certifications/${id}/${action}`, {
    remark
  });

export const platformFetchWorkshops = (auditStatus = 'pending', page = 1, pageSize = 20) =>
  request.get<unknown, PageResp<WorkshopAdminItem>>(`${platform}/workshops`, {
    params: { auditStatus, page, pageSize }
  });

export const platformHandleWorkshop = (id: number, action: 'approve' | 'reject') =>
  request.post<unknown, WorkshopAdminItem>(`${platform}/workshops/${id}/${action}`);

export const platformFetchAppeals = (status = 'pending', page = 1, pageSize = 20) =>
  request.get<unknown, PageResp<ReviewAppeal>>(`${platform}/review-appeals`, {
    params: { status, page, pageSize }
  });

export const platformHandleAppeal = (id: number, action: 'approve' | 'reject', remark?: string) =>
  request.post<unknown, ReviewAppeal>(`${platform}/review-appeals/${id}/${action}`, { remark });
