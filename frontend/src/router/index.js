import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'course/:id', name: 'CourseDetail', component: () => import('@/views/CourseDetail.vue') },
      { path: 'knowledge-point/:id', name: 'KnowledgePointDetail', component: () => import('@/views/KnowledgePointDetail.vue') },
      { path: 'practice', name: 'Practice', component: () => import('@/views/Practice.vue'), meta: { requiresAuth: true } },
      { path: 'history', name: 'History', component: () => import('@/views/History.vue'), meta: { requiresAuth: true } },
      { path: 'stats', name: 'Stats', component: () => import('@/views/Stats.vue'), meta: { requiresAuth: true } },
      { path: 'ai', name: 'AiAssistant', component: () => import('@/views/AiAssistant.vue'), meta: { requiresAuth: true } },
      { path: 'interview', name: 'Interview', component: () => import('@/views/Interview.vue') },
      { path: 'interview/problem/:id', name: 'CodingProblem', component: () => import('@/views/CodingProblem.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboard.vue') },
      { path: 'courses', name: 'CourseManage', component: () => import('@/views/admin/CourseManage.vue') },
      { path: 'knowledge-points', name: 'KnowledgePointManage', component: () => import('@/views/admin/KnowledgePointManage.vue') },
      { path: 'questions', name: 'QuestionManage', component: () => import('@/views/admin/QuestionManage.vue') },
      { path: 'coding', name: 'CodingProblemManage', component: () => import('@/views/admin/CodingProblemManage.vue') },
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'ai-config', name: 'AiConfig', component: () => import('@/views/admin/AiConfig.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  if (to.meta.requiresAuth && !store.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !store.isAdmin) {
    return { path: '/' }
  }
  return true
})

export default router
