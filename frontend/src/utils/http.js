import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const http = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

http.interceptors.request.use((config) => {
  // 优先从 Pinia store 读 token；store 未就绪时回退到 sessionStorage（会话级），
  // 再回退到 localStorage（7 天免登录），但**只信任"token 和 user 同时存在"**的记录——
  // 避免带陈旧/被清掉一半的 token 打后端触发 401 弹错。
  //
  // 注：拦截器是请求触发时才执行，此时 Pinia 早已就绪；ESM 静态 import 在
  // 模块加载时被 hoist，不会出现"拦截器在 Pinia 安装前执行"的问题。
  let token = ''
  try {
    const store = useUserStore()
    if (store && store.token) {
      token = store.token
    } else {
      // store 没拿到 token 时，回退到持久化存储
      let t = sessionStorage.getItem('aiqb_token')
      let u = sessionStorage.getItem('aiqb_user')
      if (!t || !u) {
        t = localStorage.getItem('aiqb_token')
        u = localStorage.getItem('aiqb_user')
      }
      if (t && u) token = t
    }
  } catch (e) {
    try {
      let t = sessionStorage.getItem('aiqb_token')
      let u = sessionStorage.getItem('aiqb_user')
      if (!t || !u) {
        t = localStorage.getItem('aiqb_token')
        u = localStorage.getItem('aiqb_user')
      }
      if (t && u) token = t
    } catch (_) { /* */ }
  }
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 静默窗口：标记一个时间窗，期间收到的 401 一律不弹错
// 用于"刚调用 logout()"或"刚跳到 /login"的瞬态场景——
// 已经飞出去的请求会因为旧 token 失效返回 401，这些 401 是预期的
let silent401Until = 0
// 主动登出标志：调用 logout() 时同步置 true，下一次响应处理后清掉
let justLoggedOut = false

export function markSilent401Window(ms = 3000) {
  silent401Until = Date.now() + ms
}

export function markJustLoggedOut() {
  justLoggedOut = true
  // 4 秒后自动清除，避免永远静默
  setTimeout(() => { justLoggedOut = false }, 4000)
}

function isInSilentWindow() {
  return Date.now() < silent401Until
}

http.interceptors.response.use(
  (resp) => resp.data,
  (err) => {
    const status = err.response?.status
    const detail = err.response?.data?.message || err.response?.data?.detail || err.message
    if (status === 401) {
      // 401 时再动态 import store 和 router（这时 Pinia 已就绪）
      import('@/stores/user').then(({ useUserStore }) => {
        try { useUserStore().logout() } catch (e) { /* */ }
      }).catch(() => {})
      import('@/router').then(({ default: router }) => {
        // 只有当前不在登录页才跳转，避免重复 push 触发路由报错
        if (router.currentRoute.value.path !== '/login') {
          try { router.push('/login') } catch (e) { /* */ }
        }
      }).catch(() => {})

      // 静默条件（满足任一即不弹"请先登录"）：
      //   1. 主动登出标志（logout() 同步置位，4 秒后清）
      //   2. 静默窗口内（logout 后 3 秒内残留 401）
      //   3. 当前已在 /login（用户已经在登录页，401 是预期）
      const path = typeof window !== 'undefined' ? window.location.pathname : ''
      const isLoginPage = path === '/login' || path.startsWith('/login')
      if (!justLoggedOut && !isInSilentWindow() && !isLoginPage) {
        ElMessage.error('请先登录')
      }
    } else {
      ElMessage.error(typeof detail === 'string' ? detail : '请求失败')
    }
    return Promise.reject(err)
  }
)

export default http
