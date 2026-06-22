<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">AI 智能题库</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon><span>首页</span>
        </el-menu-item>
        <el-menu-item index="/questions">
          <el-icon><EditPen /></el-icon><span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/papers">
          <el-icon><Document /></el-icon><span>试卷管理</span>
        </el-menu-item>
        <el-menu-item index="/exams">
          <el-icon><Tickets /></el-icon><span>考试中心</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon><span>历史记录</span>
        </el-menu-item>
        <el-menu-item index="/wrong">
          <el-icon><Warning /></el-icon><span>错题本</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><DataLine /></el-icon><span>统计分析</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span style="font-weight: 600;">{{ pageTitle }}</span>
        <el-dropdown @command="onCmd">
          <span class="user">
            <el-icon><User /></el-icon>
            {{ userStore.user?.nickname || userStore.user?.username }}
            <el-tag size="small" :type="roleType" style="margin-left:6px">{{ roleLabel }}</el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const ROLE_MAP = { ADMIN: ['管理员', 'danger'], TEACHER: ['教师', 'warning'], STUDENT: ['学生', 'success'] }
const roleLabel = computed(() => ROLE_MAP[userStore.user?.role]?.[0] || userStore.user?.role)
const roleType = computed(() => ROLE_MAP[userStore.user?.role]?.[1] || '')

const TITLE_MAP = {
  '/dashboard': '首页概览',
  '/questions': '题库管理',
  '/papers': '试卷管理',
  '/exams': '考试中心',
  '/history': '历史记录',
  '/wrong': '我的错题本',
  '/stats': '统计分析',
}
const pageTitle = computed(() => TITLE_MAP[route.path] || 'AI 智能题库系统')

function onCmd(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; color: #fff; }
.logo {
  height: 60px; display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 600; background: #002140; color: #fff;
}
.menu { background: transparent; border: none; }
.menu :deep(.el-menu-item) { color: rgba(255,255,255,0.85); }
.menu :deep(.el-menu-item.is-active) { background: #1890ff; color: #fff; }
.menu :deep(.el-menu-item:hover) { background: #002140; }
.header {
  background: #fff; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid #eee; padding: 0 24px;
}
.user { display: flex; align-items: center; gap: 6px; cursor: pointer; }
</style>
