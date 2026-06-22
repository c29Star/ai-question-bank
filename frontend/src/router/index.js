import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { guest: true } },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'questions', component: () => import('@/views/Questions.vue') },
      { path: 'papers', component: () => import('@/views/Papers.vue') },
      { path: 'exams', component: () => import('@/views/Exams.vue') },
      { path: 'exam/:examId', component: () => import('@/views/ExamTaking.vue') },
      { path: 'wrong', component: () => import('@/views/WrongBook.vue') },
      { path: 'history', component: () => import('@/views/History.vue') },
      { path: 'stats', component: () => import('@/views/Stats.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _, next) => {
  const userStore = useUserStore()
  if (to.meta.guest && userStore.isLogin) return next('/')
  if (!to.meta.guest && !userStore.isLogin) return next('/login')
  next()
})

export default router
