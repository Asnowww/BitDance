import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { getToken } from '@/utils/request';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/pages/home/HomePage.vue'),
    meta: { tab: 'home', title: 'BitDance' }
  },
  {
    path: '/practice',
    name: 'practice-square',
    component: () => import('@/pages/practice/PracticeSquarePage.vue'),
    meta: { tab: 'practice', title: '约练广场' }
  },
  {
    path: '/publish/checkin',
    name: 'publish-checkin',
    component: () => import('@/pages/publish/PublishCheckinPage.vue'),
    meta: { title: '训练打卡', requiresAuth: true }
  },
  {
    path: '/publish/practice',
    name: 'publish-practice',
    component: () => import('@/pages/publish/PublishPracticePage.vue'),
    meta: { title: '发起约练', requiresAuth: true }
  },
  {
    path: '/publish/review',
    name: 'publish-review',
    component: () => import('@/pages/publish/PublishReviewPage.vue'),
    meta: { title: '写评价', requiresAuth: true }
  },
  {
    path: '/growth',
    name: 'growth',
    component: () => import('@/pages/growth/GrowthPage.vue'),
    meta: { tab: 'growth', title: '成长' }
  },
  {
    path: '/me',
    name: 'me',
    component: () => import('@/pages/user/MePage.vue'),
    meta: { tab: 'me', title: '我的', requiresAuth: true }
  },
  {
    path: '/me/home',
    name: 'my-home',
    component: () => import('@/pages/user/UserCenterPage.vue'),
    meta: { tab: 'me', title: '我的个人主页', requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'public-user-home',
    component: () => import('@/pages/user/PublicUserHomePage.vue'),
    meta: { tab: 'practice', title: '个人主页' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/user/LoginPage.vue'),
    meta: { title: '登录', hideTabBar: true }
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/pages/home/SearchPage.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/studio/:id',
    name: 'studio-detail',
    component: () => import('@/pages/studio/StudioDetailPage.vue'),
    meta: { title: '舞室详情' }
  },
  {
    path: '/studio/compare',
    name: 'studio-compare',
    component: () => import('@/pages/studio/StudioComparePage.vue'),
    meta: { title: '舞室对比' }
  },
  {
    path: '/course/:id',
    name: 'course-detail',
    component: () => import('@/pages/studio/CourseDetailPage.vue'),
    meta: { title: '课程详情' }
  },
  {
    path: '/coach/:id',
    name: 'coach-detail',
    component: () => import('@/pages/studio/CoachDetailPage.vue'),
    meta: { title: '教练详情' }
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('@/pages/user/FavoritesPage.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/studio/:id/trial',
    name: 'trial-booking',
    component: () => import('@/pages/studio/TrialBookingPage.vue'),
    meta: { title: '试听预约', requiresAuth: true }
  },
  {
    path: '/studio/:id/schedule',
    name: 'studio-schedule',
    component: () => import('@/pages/studio/StudioSchedulePage.vue'),
    meta: { title: '周课表' }
  },
  {
    path: '/me/trials',
    name: 'my-trials',
    component: () => import('@/pages/user/MyTrialsPage.vue'),
    meta: { title: '我的试听', requiresAuth: true }
  },
  {
    path: '/studio/:id/reviews',
    name: 'studio-reviews',
    component: () => import('@/pages/studio/StudioReviewsPage.vue'),
    meta: { title: '评价' }
  },
  {
    path: '/me/reviews',
    name: 'my-reviews',
    component: () => import('@/pages/user/MyReviewsPage.vue'),
    meta: { title: '我的评价', requiresAuth: true }
  },
  {
    path: '/practice/:id',
    name: 'practice-detail',
    component: () => import('@/pages/practice/PracticeDetailPage.vue'),
    meta: { title: '约练详情' }
  },
  {
    path: '/me/profile',
    name: 'profile-edit',
    component: () => import('@/pages/user/ProfileEditPage.vue'),
    meta: { title: '资料与偏好', requiresAuth: true }
  },
  {
    path: '/me/privacy',
    name: 'privacy',
    component: () => import('@/pages/user/PrivacyPage.vue'),
    meta: { title: '隐私设置', requiresAuth: true }
  },
  {
    path: '/me/practices',
    name: 'my-practices',
    component: () => import('@/pages/user/MyPracticesPage.vue'),
    meta: { title: '我的约练', requiresAuth: true }
  },
  {
    path: '/me/coach-home',
    name: 'coach-home',
    component: () => import('@/pages/user/CoachHomePage.vue'),
    meta: { title: '教练主页', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'messages',
    component: () => import('@/pages/user/MessagesPage.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/workshops',
    name: 'workshop-list',
    component: () => import('@/pages/workshop/WorkshopListPage.vue'),
    meta: { tab: 'activity', title: '活动' }
  },
  {
    path: '/workshop/:id',
    name: 'workshop-detail',
    component: () => import('@/pages/workshop/WorkshopDetailPage.vue'),
    meta: { title: 'Workshop' }
  },
  {
    path: '/workshop/:id/pay',
    name: 'workshop-payment',
    component: () => import('@/pages/workshop/WorkshopPaymentPage.vue'),
    meta: { title: 'Workshop 报名支付', requiresAuth: true }
  },
  {
    path: '/workshop-checkin/:id',
    name: 'workshop-checkin',
    component: () => import('@/pages/workshop/WorkshopCheckinPage.vue'),
    meta: { title: 'Workshop 签到', requiresAuth: true }
  },
  {
    path: '/me/workshop-orders',
    name: 'my-workshop-orders',
    component: () => import('@/pages/user/MyWorkshopOrdersPage.vue'),
    meta: { title: '我的 Workshop 订单', requiresAuth: true }
  },
  {
    path: '/me/workshop-calendar',
    name: 'workshop-calendar',
    component: () => import('@/pages/user/WorkshopCalendarPage.vue'),
    meta: { title: '活动日历', requiresAuth: true }
  },
  {
    path: '/community',
    name: 'community',
    component: () => import('@/pages/community/CommunityFeedPage.vue'),
    meta: { title: '社区' }
  },
  {
    path: '/community/post/:id',
    name: 'post-detail',
    component: () => import('@/pages/community/PostDetailPage.vue'),
    meta: { title: '动态' }
  },
  {
    path: '/community/publish',
    name: 'publish-post',
    component: () => import('@/pages/community/PublishPostPage.vue'),
    meta: { tab: 'activity', title: '发动态', requiresAuth: true }
  },
  {
    path: '/community/topics',
    name: 'topics',
    component: () => import('@/pages/community/TopicsPage.vue'),
    meta: { title: '话题广场' }
  },
  {
    path: '/community/topic/:name',
    name: 'topic-detail',
    component: () => import('@/pages/community/TopicDetailPage.vue'),
    meta: { title: '话题' }
  },
  {
    path: '/community/following',
    name: 'following',
    component: () => import('@/pages/community/FollowingPage.vue'),
    meta: { title: '关注', requiresAuth: true }
  },
  {
    path: '/community/search',
    name: 'community-search',
    component: () => import('@/pages/community/CommunitySearchPage.vue'),
    meta: { title: '社区搜索' }
  },
  {
    path: '/practice/recommend',
    name: 'practice-recommend',
    component: () => import('@/pages/practice/RecommendPage.vue'),
    meta: { title: '推荐与搭子' }
  },
  {
    path: '/practice/:id/rate',
    name: 'practice-rate',
    component: () => import('@/pages/practice/PracticeRatingPage.vue'),
    meta: { title: '约练评价', requiresAuth: true }
  },
  {
    path: '/me/works',
    name: 'my-works',
    component: () => import('@/pages/growth/WorksPage.vue'),
    meta: { title: '阶段作品', requiresAuth: true }
  },
  {
    path: '/me/works/upload',
    name: 'publish-work',
    component: () => import('@/pages/community/PublishPostPage.vue'),
    meta: { tab: 'growth', title: '上传作品', requiresAuth: true }
  },
  {
    path: '/me/goal',
    name: 'my-goal',
    component: () => import('@/pages/growth/GoalPage.vue'),
    meta: { title: '训练目标', requiresAuth: true }
  },
  {
    path: '/me/course-orders',
    name: 'my-course-orders',
    component: () => import('@/pages/user/MyCourseOrdersPage.vue'),
    meta: { title: '我的课程订单', requiresAuth: true }
  },
  {
    path: '/coach/appeal',
    name: 'coach-appeal',
    component: () => import('@/pages/coach/AppealPage.vue'),
    meta: { title: '评价申诉', requiresAuth: true }
  },
  {
    path: '/coach/studio-claim',
    name: 'studio-claim',
    component: () => import('@/pages/coach/StudioClaimPage.vue'),
    meta: { title: '舞室入驻 / 认领', requiresAuth: true }
  },
  {
    path: '/coach/studio-claim/status',
    name: 'studio-claim-status',
    component: () => import('@/pages/coach/StudioClaimStatusPage.vue'),
    meta: { title: '入驻审核进度', requiresAuth: true }
  },
  {
    path: '/coach/courses',
    name: 'merchant-courses',
    component: () => import('@/pages/coach/MerchantCoursesPage.vue'),
    meta: { title: '课程管理', requiresAuth: true }
  },
  {
    path: '/coach/course-edit/:id?',
    name: 'course-edit',
    component: () => import('@/pages/coach/CourseEditPage.vue'),
    meta: { title: '课程编辑', requiresAuth: true }
  },
  {
    path: '/coach/schedule',
    name: 'merchant-schedule',
    component: () => import('@/pages/coach/MerchantSchedulePage.vue'),
    meta: { title: '周课表', requiresAuth: true }
  },
  {
    path: '/coach/schedule-edit/:id?',
    name: 'schedule-edit',
    component: () => import('@/pages/coach/ScheduleEditPage.vue'),
    meta: { title: '场次编辑', requiresAuth: true }
  },
  {
    path: '/coach/schedule/:id/bookings',
    name: 'schedule-bookings',
    component: () => import('@/pages/coach/ScheduleBookingsPage.vue'),
    meta: { title: '预约名单', requiresAuth: true }
  },
  {
    path: '/coach/checkin',
    name: 'coach-checkin',
    component: () => import('@/pages/coach/CheckinPage.vue'),
    meta: { title: '签到核销', requiresAuth: true }
  },
  {
    path: '/coach/workshops',
    name: 'merchant-workshops',
    component: () => import('@/pages/coach/MerchantWorkshopsPage.vue'),
    meta: { title: 'Workshop 管理', requiresAuth: true }
  },
  {
    path: '/coach/coaches',
    name: 'merchant-coaches',
    component: () => import('@/pages/coach/MerchantCoachesPage.vue'),
    meta: { title: '教练管理', requiresAuth: true }
  },
  {
    path: '/coach/invitations',
    name: 'coach-invitations',
    component: () => import('@/pages/coach/CoachInvitationsPage.vue'),
    meta: { title: '我的合作邀请', requiresAuth: true }
  },
  {
    path: '/coach/certification',
    name: 'coach-certification',
    component: () => import('@/pages/coach/CoachCertificationPage.vue'),
    meta: { title: '教练资质', requiresAuth: true }
  },
  {
    path: '/coach/settlement',
    name: 'coach-settlement',
    component: () => import('@/pages/coach/SettlementPage.vue'),
    meta: { title: '收益统计', requiresAuth: true }
  },
  {
    path: '/coach/platform/reviews',
    name: 'platform-reviews',
    component: () => import('@/pages/coach/PlatformReviewPage.vue'),
    meta: { title: '平台审核中心', requiresAuth: true }
  },
  {
    path: '/coach/workshop-create',
    name: 'coach-workshop-create',
    component: () => import('@/pages/coach/CoachWorkshopCreatePage.vue'),
    meta: { title: '创建 Workshop', requiresAuth: true }
  },
  {
    path: '/coach/orders',
    name: 'coach-orders',
    component: () => import('@/pages/coach/CoachOrdersPage.vue'),
    meta: { title: '学员订单与核销', requiresAuth: true }
  },
  {
    path: '/coach/replies',
    name: 'coach-replies',
    component: () => import('@/pages/coach/ReplyReviewsPage.vue'),
    meta: { title: '评价回复', requiresAuth: true }
  },
  {
    path: '/coach/dashboard',
    name: 'coach-dashboard',
    component: () => import('@/pages/coach/CoachDashboardPage.vue'),
    meta: { title: '经营看板', requiresAuth: true }
  },
  {
    path: '/admin/reports',
    name: 'admin-reports',
    component: () => import('@/pages/admin/ReportTicketsPage.vue'),
    meta: { title: '举报后台', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/pages/common/NotFoundPage.vue'),
    meta: { hideTabBar: true }
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

const hasOpsRole = () => {
  try {
    const profile = JSON.parse(localStorage.getItem('bitdance_profile') ?? 'null');
    const roles: string[] = profile?.roles ?? [];
    return roles.some((r) => ['PLATFORM_ADMIN', 'STUDIO_ADMIN', 'COACH'].includes(r));
  } catch {
    return false;
  }
};

router.beforeEach((to, from) => {
  if (to.meta?.requiresAuth && !getToken()) {
    // 登录态统一守卫：点击“我的”等受保护入口时先去登录页，登录成功后按 redirect 回到原目标页。
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  // 应用入口分流:已登录的运营角色打开 App 时直达管理端(站内点击 tab 回首页不受影响)
  const isAppEntry = from.matched.length === 0;
  if (isAppEntry && to.path === '/home' && getToken() && hasOpsRole()) {
    return { path: '/coach/dashboard' };
  }
  return true;
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title);
  }
});

export default router;
