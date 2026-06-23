import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './styles/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 应用启动时尝试从 localStorage 恢复登录态（仅当勾选了"7 天免登录"才会写入）
try { useUserStore().hydrateFromStorage() } catch (e) { /* ignore */ }

app.mount('#app')
