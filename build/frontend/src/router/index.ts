import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
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
    component: () => import('@/pages/user/UserCenterPage.vue'),
    meta: { tab: 'me', title: '我的', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/user/LoginPage.vue'),
    meta: { title: '登录' }
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
    meta: { title: 'Workshop' }
  },
  {
    path: '/workshop/:id',
    name: 'workshop-detail',
    component: () => import('@/pages/workshop/WorkshopDetailPage.vue'),
    meta: { title: 'Workshop' }
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
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/pages/common/NotFoundPage.vue')
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
  return true;
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title);
  }
});

export default router;
