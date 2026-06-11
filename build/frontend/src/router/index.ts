import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { getToken, isPasswordRequired } from '@/utils/request';

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
    path: '/practice/group-class',
    name: 'group-class',
    component: () => import('@/pages/practice/GroupClassPage.vue'),
    meta: { tab: 'practice', title: '拼课广场' }
  },
  {
    path: '/practice/group-class/create',
    name: 'group-class-create',
    component: () => import('@/pages/practice/GroupClassCreatePage.vue'),
    meta: { tab: 'practice', title: '发起拼课', requiresAuth: true, hideTabBar: true }
  },
  {
    path: '/publish/checkin',
    name: 'publish-checkin',
    component: () => import('@/pages/publish/PublishCheckinPage.vue'),
    meta: { tab: 'growth', title: '训练打卡', requiresAuth: true, hideTabBar: true }
  },
  {
    path: '/publish/practice',
    name: 'publish-practice',
    component: () => import('@/pages/publish/PublishPracticePage.vue'),
    meta: { tab: 'practice', title: '发起约练', requiresAuth: true, hideTabBar: true }
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
    meta: { tab: 'practice', title: '我的约练', requiresAuth: true }
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
    path: '/activity',
    name: 'activity-hub',
    component: () => import('@/pages/community/ActivityHubPage.vue'),
    meta: { tab: 'activity', title: '社区 / 活动' }
  },
  {
    path: '/workshops',
    name: 'workshop-list',
    component: () => import('@/pages/workshop/WorkshopListPage.vue'),
    meta: { tab: 'activity', title: 'Workshop' }
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
    path: '/community/post/:id/edit',
    name: 'edit-post',
    component: () => import('@/pages/community/PublishPostPage.vue'),
    meta: { title: '编辑动态', requiresAuth: true }
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
    meta: { tab: 'practice', title: '约练评价', requiresAuth: true, hideTabBar: true }
  },
  {
    path: '/practice/:id',
    name: 'practice-detail',
    component: () => import('@/pages/practice/PracticeDetailPage.vue'),
    meta: { tab: 'practice', title: '约练详情', hideTabBar: true }
  },
  {
    path: '/me/works',
    name: 'my-works',
    component: () => import('@/pages/growth/WorksPage.vue'),
    meta: { tab: 'growth', title: '阶段作品', requiresAuth: true, hideTabBar: true }
  },
  {
    path: '/me/works/upload',
    name: 'publish-work',
    component: () => import('@/pages/growth/WorkUploadPage.vue'),
    meta: { tab: 'growth', title: '上传作品', requiresAuth: true, hideTabBar: true }
  },
  {
    path: '/growth/report',
    name: 'growth-report',
    component: () => import('@/pages/growth/GrowthReportPage.vue'),
    meta: { tab: 'growth', title: '成长报告', requiresAuth: true }
  },
  {
    path: '/growth/timeline',
    name: 'growth-timeline',
    component: () => import('@/pages/growth/GrowthTimelinePage.vue'),
    meta: { tab: 'growth', title: '成长时间线', requiresAuth: true }
  },
  {
    path: '/me/goal',
    name: 'my-goal',
    component: () => import('@/pages/growth/GoalPage.vue'),
    meta: { tab: 'growth', title: '训练目标', requiresAuth: true, hideTabBar: true }
  },
  {
    path: '/coach/appeal',
    name: 'coach-appeal',
    component: () => import('@/pages/coach/AppealPage.vue'),
    meta: { title: '评价申诉', requiresAuth: true }
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

router.beforeEach((to) => {
  if (to.meta?.requiresAuth && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (to.meta?.requiresAuth && isPasswordRequired()) {
    return { path: '/login', query: { setupPassword: '1', redirect: to.fullPath } };
  }
  return true;
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title);
  }
});

export default router;
