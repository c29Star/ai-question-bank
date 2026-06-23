import { defineStore } from 'pinia'
import { markSilent401Window, markJustLoggedOut } from '@/utils/http'

const TOKEN_KEY = 'aiqb_token'
const USER_KEY = 'aiqb_user'

// 双层存储：
//   - sessionStorage：会话级，**登录成功后默认写入**。浏览器 tab 还开着
//     （含刷新）就一直存在。关掉 tab 后自动失效。
//   - localStorage：跨会话级，仅当勾选"7 天免登录"（remember=true）才写入。
//     关掉整个浏览器后再打开仍然有效。
//
// 读取优先级：localStorage > sessionStorage（记住的优先级更高）。

export const useUserStore = defineStore('user', {
  state: () => ({
    // 启动时不主动读任何存储，由 hydrateFromStorage() 显式恢复
    token: '',
    user: null,
  }),
  getters: {
    isLogin: (s) => !!s.token,
    isAdmin: (s) => s.user?.role === 'ADMIN',
    isTeacher: (s) => s.user?.role === 'TEACHER' || s.user?.role === 'ADMIN',
    isStudent: (s) => s.user?.role === 'STUDENT',
  },
  actions: {
    /**
     * 设置登录态
     * @param token JWT
     * @param user 用户对象
     * @param remember 是否"7 天免登录"。true → sessionStorage + localStorage 都写（关浏览器也不掉登录）；
     *                  false/未传 → 只写 sessionStorage（关掉整个浏览器后失效，但刷新保留）
     */
    setAuth(token, user, remember = false) {
      this.token = token
      this.user = user
      // 总是写 sessionStorage：保证刷新页面不丢登录态
      try {
        sessionStorage.setItem(TOKEN_KEY, token)
        sessionStorage.setItem(USER_KEY, JSON.stringify(user))
      } catch (e) { /* sessionStorage 不可用时静默 */ }
      // 仅在 remember=true 时额外写 localStorage
      if (remember) {
        try {
          localStorage.setItem(TOKEN_KEY, token)
          localStorage.setItem(USER_KEY, JSON.stringify(user))
        } catch (e) { /* */ }
      } else {
        // 确保没有上一轮 remember=true 留下的 localStorage 残留
        try {
          localStorage.removeItem(TOKEN_KEY)
          localStorage.removeItem(USER_KEY)
        } catch (e) { /* */ }
      }
    },
    logout() {
      // 主动打开"静默窗口"——已经飞出去的请求会因为旧 token 失效返回 401，
      // 这些 401 是预期的，不再弹"请先登录"提示轰炸登录页
      markJustLoggedOut()        // 同步置位，立刻生效
      markSilent401Window(3000)  // 兜底，3 秒内残留 401 全部静默
      this.token = ''
      this.user = null
      // 主动退出：同时清掉 sessionStorage 和 localStorage
      try {
        sessionStorage.removeItem(TOKEN_KEY)
        sessionStorage.removeItem(USER_KEY)
      } catch (e) { /* */ }
      try {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
      } catch (e) { /* */ }
    },
    /**
     * 启动时尝试从存储恢复登录态
     * 优先级：localStorage（remember=true 留下的） > sessionStorage（会话级）
     *
     * 注意：这里只做"恢复"，不做"清残留"——残留清理由 router 守卫和 Login.vue 处理。
     * 如果一个 tab 里有"上一轮 sessionStorage 的 token 但用户已经关了 tab 再开"，浏览器会
     * 自动清掉 sessionStorage，所以这里读不到就是干净的。
     */
    hydrateFromStorage() {
      let t = null, u = null
      try {
        // 优先 localStorage（remember=true 留下的，跨浏览器会话）
        t = localStorage.getItem(TOKEN_KEY)
        u = localStorage.getItem(USER_KEY)
        // 如果 localStorage 没有，回退到 sessionStorage（会话级，刷新保留）
        if (!t || !u) {
          t = sessionStorage.getItem(TOKEN_KEY)
          u = sessionStorage.getItem(USER_KEY)
        }
      } catch (e) { /* */ }
      if (t && u) {
        try {
          this.token = t
          this.user = JSON.parse(u)
        } catch (e) {
          this.logout()
        }
      }
    },
  },
})
