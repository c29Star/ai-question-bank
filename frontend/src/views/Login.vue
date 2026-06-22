<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 class="title">AI 智能题库</h2>
      <p class="subtitle">登录以开始使用</p>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item v-if="!isLogin" label="昵称">
          <el-input v-model="form.nickname" placeholder="选填" size="large" />
        </el-form-item>
        <el-form-item v-if="!isLogin" label="角色">
          <el-radio-group v-model="form.role">
            <el-radio value="STUDENT">学生</el-radio>
            <el-radio value="TEACHER">教师</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="handleSubmit">
          {{ isLogin ? '登录' : '注册' }}
        </el-button>
        <div class="toggle">
          <el-link type="primary" @click="toggle">{{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}</el-link>
        </div>
      </el-form>
      <div class="hint">
        <p>默认账号：admin / teacher1 / student1，密码统一为 <b>admin123</b></p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isLogin = ref(true)
const loading = ref(false)
const form = ref({ username: '', password: '', nickname: '', role: 'STUDENT' })

function toggle() {
  isLogin.value = !isLogin.value
}

async function handleSubmit() {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请填写完整')
  loading.value = true
  try {
    if (isLogin.value) {
      const { token, user } = await authApi.login({ username: form.value.username, password: form.value.password })
      userStore.setAuth(token, user)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      await authApi.register(form.value)
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
      form.value.password = ''
    }
  } catch (e) { /* */ } finally { loading.value = false }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card { width: 440px; border-radius: 12px; }
.title { text-align: center; margin: 0 0 4px; }
.subtitle { text-align: center; color: #999; margin: 0 0 24px; }
.toggle { text-align: center; margin-top: 12px; }
.hint { margin-top: 16px; padding: 8px; background: #f5f7fa; border-radius: 4px; }
.hint p { margin: 0; font-size: 12px; color: #999; }
</style>
