<template>
  <el-container class="main-wrap">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-mark">
          <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
            <defs>
              <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#fb923c"/>
                <stop offset="50%" stop-color="#fb7185"/>
                <stop offset="100%" stop-color="#a78bfa"/>
              </linearGradient>
            </defs>
            <rect x="3" y="6" width="20" height="22" rx="3" fill="url(#logoGrad)"/>
            <rect x="9" y="2" width="20" height="22" rx="3" fill="url(#logoGrad)" opacity="0.55"/>
            <path d="M14 10h10M14 14h10M14 18h6" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="25" cy="22" r="3" fill="#fff"/>
            <path d="M25 20.5v3l1.6.8" stroke="#f97316" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
        </div>
        <div class="logo-info">
          <div class="logo-name gradient-text">智学云</div>
          <div class="logo-sub">智能题库平台</div>
        </div>
      </div>

      <el-menu :default-active="activeRoute" class="aside-menu">
        <div class="menu-group">教学运营</div>
        <el-menu-item index="/questions" @click="go('/questions')">
          <Icon name="questions" :size="18" />
          <span>题库中心</span>
          <span class="menu-badge" v-if="counts.questions">{{ counts.questions }}</span>
        </el-menu-item>
        <el-menu-item index="/papers" @click="go('/papers')">
          <Icon name="papers" :size="18" />
          <span>试卷中心</span>
          <span class="menu-badge" v-if="counts.papers">{{ counts.papers }}</span>
        </el-menu-item>
        <el-menu-item index="/exams" @click="go('/exams')">
          <Icon name="exams" :size="18" />
          <span>考试中心</span>
          <span class="menu-badge" v-if="counts.exams">{{ counts.exams }}</span>
        </el-menu-item>

        <div class="menu-group">学习中心</div>
        <el-menu-item index="/wrong" @click="go('/wrong')">
          <Icon name="wrong" :size="18" />
          <span>我的错题</span>
          <span class="menu-badge warn" v-if="counts.wrongs">{{ counts.wrongs }}</span>
        </el-menu-item>
        <el-menu-item index="/stats" @click="go('/stats')">
          <Icon name="stats" :size="18" />
          <span>学情分析</span>
        </el-menu-item>

        <div class="menu-group">数据洞察</div>
        <el-menu-item index="/dashboard" @click="go('/dashboard')">
          <Icon name="dashboard" :size="18" />
          <span>运营看板</span>
        </el-menu-item>
      </el-menu>

      <div class="user-card">
        <el-avatar :size="38" class="avatar">{{ userInitial }}</el-avatar>
        <div class="user-info">
          <div class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</div>
          <div class="user-role">
            <el-tag size="small" :type="roleTagType" effect="dark">{{ roleLabel }}</el-tag>
          </div>
        </div>
        <el-dropdown trigger="click" @command="onCommand">
          <Icon name="more" :size="18" class="more-icon" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <Icon name="logout" :size="14" />&nbsp;退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <span class="topbar-greeting gradient-text">{{ greeting }}</span>
          <span class="topbar-divider"></span>
          <span class="topbar-date">{{ today }}</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useStatsStore } from '@/stores/stats'
import Icon from '@/components/Icon.vue'
import { questionApi, paperApi, examApi, wrongApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const statsStore = useStatsStore()

const activeRoute = computed(() => route.path)

const title = computed(() => {
  const map = {
    '/dashboard': '运营看板', '/questions': '题库中心', '/papers': '试卷中心',
    '/exams': '考试中心', '/wrong': '我的错题', '/stats': '学情分析',
  }
  return map[route.path] || '首页'
})

const roleLabel = computed(() => ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }[userStore.user?.role] || '用户'))
const roleTagType = computed(() => ({ ADMIN: 'danger', TEACHER: 'success', STUDENT: 'warning' }[userStore.user?.role] || 'info'))
const userInitial = computed(() => (userStore.user?.nickname || userStore.user?.username || '?')[0].toUpperCase())

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '🌙 夜深了'
  if (h < 12) return '☀️ 早上好'
  if (h < 14) return '🌤️ 中午好'
  if (h < 18) return '🌅 下午好'
  return '🌙 晚上好'
})

const today = computed(() => {
  const d = new Date()
  const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${w}`
})

const counts = computed(() => ({
  questions: statsStore.questionCount,
  papers: statsStore.paperCount,
  exams: statsStore.examCount,
  wrongs: statsStore.wrongCount,
}))

function go(path) {
  const cleanQuery = { ...route.query }
  delete cleanQuery._t
  if (Object.keys(cleanQuery).length === 0) {
    router.replace({ path, query: undefined })
  } else {
    router.replace({ path, query: cleanQuery })
  }
  if (route.path !== path) {
    router.push(path).catch(() => {})
  }
}

async function onCommand(cmd) {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
      userStore.logout()
      ElMessage.success('已退出')
      router.push('/login')
    } catch (e) { /* */ }
  }
}

onMounted(async () => {
  await statsStore.refresh()
})
</script>

<style scoped>
.main-wrap { height: 100vh; }

/* ============ 侧边栏：彩色春日渐变 + 装饰光斑 ============ */
.aside {
  position: relative;
  background:
    linear-gradient(180deg,
      #fff7ed 0%, #fef3c7 15%, #ffe4e6 35%,
      #ede9fe 55%, #e0f2fe 75%, #ccfbf1 100%);
  color: var(--gray-800);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(251, 113, 133, 0.10);
  border-right: 1px solid rgba(251, 146, 60, 0.10);
  overflow: hidden;
}
.aside::before {
  content: '';
  position: absolute;
  top: 8%;
  left: -40px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.20) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(30px);
  pointer-events: none;
  animation: drift 12s ease-in-out infinite;
}
.aside::after {
  content: '';
  position: absolute;
  bottom: 12%;
  right: -40px;
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.18) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(35px);
  pointer-events: none;
  animation: drift 15s ease-in-out infinite reverse;
}

.logo {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  position: relative;
  border-bottom: 1px solid rgba(251, 146, 60, 0.12);
}
.logo::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 18px;
  right: 18px;
  height: 2px;
  background: linear-gradient(90deg,
    transparent, #fbbf24, #fb923c, #fb7185,
    #c4b5fd, #7dd3fc, #5eead4, transparent);
  background-size: 200% 100%;
  opacity: 0.8;
  animation: shimmer 5s linear infinite;
}
.logo-mark {
  width: 38px; height: 38px;
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 100%);
  border-radius: 12px;
  filter: drop-shadow(0 4px 10px rgba(251, 146, 60, 0.35));
  animation: float 4s ease-in-out infinite;
}
.logo-mark svg { display: block; }
.logo-name {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: 1px;
}
.logo-sub {
  font-size: 10px;
  color: var(--gray-500);
  line-height: 1.1;
  margin-top: 5px;
  letter-spacing: 1px;
}

.menu-group {
  font-size: 10px;
  color: var(--gray-400);
  padding: 16px 20px 6px;
  letter-spacing: 2px;
  font-weight: 600;
}
.aside-menu {
  flex: 1;
  border: none !important;
  background: transparent !important;
  overflow-y: auto;
}
.aside-menu :deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
}
.aside-menu :deep(.el-menu-item) {
  color: var(--gray-700) !important;
  height: 44px;
  line-height: 44px;
  margin: 4px 10px;
  border-radius: 10px;
  font-weight: 500;
}
.aside-menu :deep(.el-menu-item:hover) {
  background: linear-gradient(90deg, #fff7ed 0%, #ffe4e6 100%) !important;
  color: var(--brand-primary-dark) !important;
  transform: translateX(2px);
}
.aside-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #fb923c 0%, #fb7185 100%) !important;
  color: #fff !important;
  box-shadow: 0 6px 18px rgba(249, 115, 22, 0.35);
  position: relative;
}
.aside-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: var(--gradient-rainbow);
  background-size: 200% 100%;
  border-radius: 0 4px 4px 0;
  animation: shimmer 4s linear infinite;
}
.aside-menu :deep(.el-menu-item.is-active .el-icon),
.aside-menu :deep(.el-menu-item.is-active svg) {
  color: #fff !important;
}

.menu-badge {
  display: inline-block;
  min-width: 20px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: linear-gradient(135deg, #a78bfa, #7dd3fc);
  color: #fff;
  border-radius: 9px;
  font-size: 11px;
  margin-left: auto;
  box-shadow: 0 2px 6px rgba(167, 139, 250, 0.35);
  font-weight: 600;
}
.menu-badge.warn {
  background: linear-gradient(135deg, #fb7185, #f43f5e);
  box-shadow: 0 2px 6px rgba(251, 113, 133, 0.40);
}

.user-card {
  position: relative;
  padding: 14px 16px;
  border-top: 1px solid rgba(251, 146, 60, 0.12);
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(10px);
  z-index: 2;
}
.user-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: var(--gradient-rainbow);
  background-size: 200% 100%;
  animation: shimmer 6s linear infinite;
}
.avatar {
  background: linear-gradient(135deg, #fb923c 0%, #fb7185 50%, #c4b5fd 100%) !important;
  color: #fff !important;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(251, 113, 133, 0.35);
}
.user-info { flex: 1; min-width: 0; }
.user-name {
  font-size: 13px;
  color: var(--gray-800);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-role { margin-top: 4px; }
.more-icon {
  color: var(--gray-400);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s ease;
}
.more-icon:hover {
  color: var(--brand-primary);
  background: var(--brand-peach-soft);
}

/* ============ 顶栏 ============ */
.topbar {
  position: relative;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(251, 146, 60, 0.10);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 60px;
  box-shadow: 0 2px 12px rgba(251, 146, 60, 0.08);
  overflow: hidden;
}
.topbar::before {
  content: '';
  position: absolute;
  top: -20px;
  right: 10%;
  width: 200px;
  height: 60px;
  background: radial-gradient(ellipse, rgba(251, 113, 133, 0.10) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(20px);
  pointer-events: none;
}
.topbar::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 30%;
  width: 220px;
  height: 40px;
  background: radial-gradient(ellipse, rgba(167, 139, 250, 0.10) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(20px);
  pointer-events: none;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.topbar-greeting {
  font-size: 14px;
  font-weight: 600;
}
.topbar-divider {
  width: 1px;
  height: 14px;
  background: linear-gradient(180deg, transparent, #fb923c, transparent);
}
.topbar-date {
  font-size: 12px;
  color: var(--gray-500);
}

.main-content {
  background: transparent;
  padding: 0;
  overflow-y: auto;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.fade-enter-from { opacity: 0; transform: translateY(6px); }
.fade-leave-to   { opacity: 0; transform: translateY(-6px); }
</style>