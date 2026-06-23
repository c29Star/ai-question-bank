import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { markSilent401Window, markJustLoggedOut } from '@/utils/http'

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
      { path: 'wrong', component: () => import('@/views/WrongBook.vue') },
      { path: 'stats', component: () => import('@/views/Stats.vue') },
      { path: 'history', component: () => import('@/views/History.vue') },
      { path: 'exam-taking/:id', component: () => import('@/views/ExamTaking.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _, next) => {
  const userStore = useUserStore()

  // 兜底：守卫触发时再 hydrate 一次（main.js 已经同步调过，这里防御顺序被改）
  // 这样无论"勾了 7 天免登录 → 重开/刷新"还是"内存登录态已就绪"，store 都能拿到 token
  if (!userStore.token) {
    userStore.hydrateFromStorage()
  }

  // 进入登录页：清理内存中的残留登录态，并尽早打开 401 静默窗口
  // （首屏渲染时残留的旧请求可能赶在 Login.vue onMounted 之前到达 401，
  //    把静默窗口前移到守卫里，确保这些残留 401 不弹"请先登录"）
  if (to.meta.guest) {
    markJustLoggedOut()
    markSilent401Window(4000)
    // store 里有 token 但 sessionStorage + localStorage 都没有 → 真正的关网页场景
    // 强制清掉（理论上不会发生：hydrate 已经从 sessionStorage 恢复了；
    //   这段只是防御"store 莫名其妙有 token 但持久化都没"的脏状态）
    const hasPersisted =
      sessionStorage.getItem('aiqb_token') ||
      localStorage.getItem('aiqb_token')
    if (userStore.isLogin && !hasPersisted) {
      userStore.logout()
    }
    return next()
  }

  // 其他受保护页面：未登录则跳到登录页
  if (!userStore.isLogin) return next('/login')
  next()
})

export default router
