<template>
  <div class="login-wrap">
    <div class="login-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
      <div class="bg-circle c4"></div>
      <div class="bg-circle c5"></div>
      <div class="bg-circle c6"></div>
      <!-- 漂浮花瓣/校园装饰 -->
      <div class="petal p1">🌸</div>
      <div class="petal p2">🌼</div>
      <div class="petal p3">🍃</div>
      <div class="petal p4">🌸</div>
      <div class="petal p5">🌼</div>
      <div class="petal p6">🍀</div>
      <div class="petal p7">📖</div>
      <div class="petal p8">✏️</div>
      <div class="petal p9">🎈</div>
      <div class="petal p10">☀️</div>
    </div>

    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text gradient-text-warm">智学云</span>
        </div>
        <h1 class="brand-title">让每一次练习<br/><span class="gradient-text-sunset">都更聪明</span></h1>
        <p class="brand-subtitle">基于通义千问大模型，为中小学、高校、培训机构提供「智能组卷 · 错题精炼 · 学情洞察」一体化教学平台</p>

        <div class="brand-metrics">
          <div class="bm-item">
            <div class="bm-num gradient-text-warm">50万+</div>
            <div class="bm-label">累计题目</div>
          </div>
          <div class="bm-divider"></div>
          <div class="bm-item">
            <div class="bm-num gradient-text-spring">300+</div>
            <div class="bm-label">合作学校</div>
          </div>
          <div class="bm-divider"></div>
          <div class="bm-item">
            <div class="bm-num gradient-text-mint">98.6%</div>
            <div class="bm-label">教师满意度</div>
          </div>
        </div>

        <ul class="brand-features">
          <li>
            <span class="bf-icon ic-1"><Icon name="ai" :size="20" color="#fff" /></span>
            <div>
              <b>AI 智能组卷</b>
              <span>设定知识点和题型，AI 自动从题库抽取组卷，告别手工拼题</span>
            </div>
          </li>
          <li>
            <span class="bf-icon ic-2"><Icon name="wrong" :size="20" color="#fff" /></span>
            <div>
              <b>错题精炼 · 举一反三</b>
              <span>考试错题自动归档，AI 推荐同类变式题，告别低效重复练习</span>
            </div>
          </li>
          <li>
            <span class="bf-icon ic-3"><Icon name="stats" :size="20" color="#fff" /></span>
            <div>
              <b>学情数据驾驶舱</b>
              <span>多维度学情分析，精准定位薄弱知识点，因材施教</span>
            </div>
          </li>
        </ul>

        <div class="brand-footer">
          © 2026 智学云教育科技（上海）有限公司 · 沪 ICP 备 2026xxxxxx 号
        </div>
      </div>

      <!-- 右侧登录区 -->
      <div class="login-panel">
        <div class="login-card glass-card">
          <h2 class="login-title gradient-text-warm">账号登录</h2>
          <p class="login-tip">使用学校 / 培训机构发放的账号登录</p>

            <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
            <el-form-item label="账号">
              <el-input v-model="form.username" placeholder="工号 / 学号 / 手机号" size="large" :prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" :prefix-icon="Lock" @keyup.enter="handleLogin" />
            </el-form-item>
            <div class="form-row">
              <el-checkbox v-model="form.remember">7 天免登录</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
              {{ loading ? '登录中…' : '登 录' }}
            </el-button>

            <el-divider><span class="divider-text">演示账号一键体验</span></el-divider>
            <div class="quick-accounts">
              <div class="account-chip ac-admin" @click="quickLogin('admin', 'admin123')">
                <el-tag size="small" effect="dark">管理员</el-tag>
                <span>admin</span>
              </div>
              <div class="account-chip ac-teacher" @click="quickLogin('teacher1', 'admin123')">
                <el-tag size="small" effect="dark">教师</el-tag>
                <span>teacher1</span>
              </div>
              <div class="account-chip ac-student" @click="quickLogin('student1', 'admin123')">
                <el-tag size="small" effect="dark">学生</el-tag>
                <span>student1</span>
              </div>
            </div>
            <p class="quick-tip">默认密码统一为 <b>admin123</b></p>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import Icon from '@/components/Icon.vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '', remember: false })

// 兜底：进入登录页时强制清掉残留登录态（防止上次测试账号的 token 自动续上）
// 注意：401 静默窗口已由 router.beforeEach 在导航前打开，这里不必重复
onMounted(() => {
  // store 内存有 token → logout
  if (userStore.token) userStore.logout()
  // 持久化存储（sessionStorage + localStorage）残留 token/user → 也清掉
  // 防止拦截器拿去发请求导致 401 弹错
  try {
    if (sessionStorage.getItem('aiqb_token')) userStore.logout()
  } catch (e) { /* ignore */ }
  try {
    if (localStorage.getItem('aiqb_token')) userStore.logout()
  } catch (e) { /* ignore */ }
})

async function handleLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请填写账号和密码')
  loading.value = true
  try {
    const res = await authApi.login({ username: form.username, password: form.password })
    const data = res.data || res
    if (data && data.token) {
      userStore.setAuth(data.token, data.user, form.remember)
      ElMessage.success(`欢迎回来，${data.user?.nickname || data.user?.username}`)
      router.push('/')
    } else {
      ElMessage.error('登录失败：未返回 token')
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message
    ElMessage.error('登录失败：' + msg)
  } finally {
    loading.value = false
  }
}

function quickLogin(u, p) {
  form.username = u
  form.password = p
  handleLogin()
}
</script>

<style scoped>
/* ============ 春日校园背景（更丰富的色彩光斑） ============ */
.login-wrap {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg,
      #fff7ed 0%, #fef3c7 18%, #ffe4e6 36%,
      #ede9fe 52%, #e0f2fe 68%, #ccfbf1 84%, #ecfccb 100%);
  background-size: 200% 200%;
  animation: gradient-shift 20s ease infinite;
  overflow: hidden;
}
.login-bg { position: absolute; inset: 0; pointer-events: none; }

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.55;
  animation: float 12s ease-in-out infinite;
}
.bg-circle.c1 { width: 460px; height: 460px; background: #fb923c; top: -120px; left: -120px; }
.bg-circle.c2 { width: 520px; height: 520px; background: #fb7185; bottom: -160px; right: -140px; opacity: 0.45; animation-delay: 2s; }
.bg-circle.c3 { width: 360px; height: 360px; background: #fde68a; top: 40%; left: 38%; opacity: 0.45; animation-delay: 4s; }
.bg-circle.c4 { width: 320px; height: 320px; background: #c4b5fd; top: 8%; right: 18%; opacity: 0.40; animation-delay: 6s; }
.bg-circle.c5 { width: 280px; height: 280px; background: #7dd3fc; top: 55%; left: 5%; opacity: 0.38; animation-delay: 3s; }
.bg-circle.c6 { width: 240px; height: 240px; background: #5eead4; top: 30%; right: 35%; opacity: 0.35; animation-delay: 5s; }

/* 飘落花瓣/校园元素 */
.petal {
  position: absolute;
  font-size: 24px;
  opacity: 0.78;
  animation: float 8s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(251, 146, 60, 0.25));
}
.petal.p1 { top: 8%;  left: 16%; animation-delay: 0s;    }
.petal.p2 { top: 70%; left: 6%;  animation-delay: 1.5s; font-size: 20px; }
.petal.p3 { top: 18%; right: 10%; animation-delay: 3s;  font-size: 22px; }
.petal.p4 { top: 78%; right: 18%; animation-delay: 4.5s; font-size: 18px; }
.petal.p5 { top: 48%; right: 32%; animation-delay: 6s;   font-size: 22px; }
.petal.p6 { top: 60%; left: 25%;  animation-delay: 2s;   font-size: 20px; }
.petal.p7 { top: 32%; left: 45%;  animation-delay: 5.5s; font-size: 26px; }
.petal.p8 { top: 85%; right: 40%; animation-delay: 7s;   font-size: 18px; }
.petal.p9 { top: 12%; right: 38%; animation-delay: 3.5s; font-size: 22px; }
.petal.p10{ top: 88%; left: 40%;  animation-delay: 1s;   font-size: 24px; }

@keyframes gradient-shift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* ============ 主容器 ============ */
.login-container {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 60px;
  max-width: 1200px;
  width: 90%;
  padding: 40px 0;
}

/* ============ 左侧品牌区 ============ */
.login-brand {
  color: var(--gray-800);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.logo-icon {
  font-size: 36px;
  filter: drop-shadow(0 4px 12px rgba(251, 146, 60, 0.4));
  animation: float 4s ease-in-out infinite;
}
.logo-text {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-title {
  font-size: 40px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0 0 16px;
  color: var(--gray-800);
}
.brand-subtitle {
  font-size: 15px;
  color: var(--gray-600);
  line-height: 1.7;
  margin: 0 0 32px;
}

.brand-metrics {
  display: flex;
  align-items: center;
  gap: 28px;
  margin: 0 0 32px;
  padding: 22px 24px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 24px rgba(251, 146, 60, 0.10);
}
.bm-item { flex: 1; }
.bm-num {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
}
.bm-label {
  font-size: 12px;
  color: var(--gray-500);
  margin-top: 6px;
}
.bm-divider {
  width: 1px;
  height: 36px;
  background: linear-gradient(180deg, transparent, #fb923c, transparent);
  opacity: 0.4;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0 0 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.brand-features li {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}
.brand-features li:hover {
  background: rgba(255, 255, 255, 0.85);
  transform: translateX(4px);
  box-shadow: 0 6px 18px rgba(251, 146, 60, 0.12);
}
.bf-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.10);
}
.bf-icon.ic-1 { background: linear-gradient(135deg, #fb923c 0%, #fb7185 100%); }
.bf-icon.ic-2 { background: linear-gradient(135deg, #fbbf24 0%, #fb923c 100%); }
.bf-icon.ic-3 { background: linear-gradient(135deg, #c4b5fd 0%, #a78bfa 100%); }
.brand-features li b {
  display: block;
  font-size: 15px;
  color: var(--gray-800);
  margin-bottom: 4px;
  font-weight: 600;
}
.brand-features li span {
  font-size: 13px;
  color: var(--gray-600);
  line-height: 1.6;
}

.brand-footer {
  font-size: 12px;
  color: var(--gray-500);
  margin-top: auto;
  padding-top: 30px;
}

/* ============ 右侧登录卡 ============ */
.login-panel { display: flex; align-items: center; }
.login-card {
  width: 100%;
  padding: 36px 36px 32px;
  border-radius: var(--radius-xl);
  box-shadow:
    0 20px 60px rgba(251, 113, 133, 0.22),
    0 6px 18px rgba(167, 139, 250, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}
.login-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 4px;
  background: linear-gradient(90deg,
    #fbbf24, #fb923c, #fb7185, #c4b5fd, #7dd3fc, #5eead4, #84cc16);
  background-size: 200% 100%;
  animation: shimmer 6s linear infinite;
}
.login-card::after {
  content: '';
  position: absolute;
  bottom: -50px; right: -50px;
  width: 180px; height: 180px;
  background: radial-gradient(circle, rgba(251, 113, 133, 0.10) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(30px);
  pointer-events: none;
}
.login-title {
  text-align: center;
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
}
.login-tip {
  text-align: center;
  color: var(--gray-500);
  font-size: 13px;
  margin: 0 0 28px;
}
.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
}

.divider-text {
  font-size: 12px;
  color: var(--gray-500);
  background: var(--gray-50);
  padding: 0 12px;
}

.quick-accounts {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 4px;
}
.account-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-size: 13px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--gray-200);
}
.account-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(251, 146, 60, 0.20);
}
.account-chip.ac-admin { border-color: rgba(244, 63, 94, 0.30); }
.account-chip.ac-admin :deep(.el-tag) { background: linear-gradient(135deg, #fb7185, #e11d48) !important; }
.account-chip.ac-teacher { border-color: rgba(20, 184, 166, 0.30); }
.account-chip.ac-teacher :deep(.el-tag) { background: linear-gradient(135deg, #5eead4, #14b8a6) !important; }
.account-chip.ac-student { border-color: rgba(251, 146, 60, 0.30); }
.account-chip.ac-student :deep(.el-tag) { background: linear-gradient(135deg, #fbbf24, #f97316) !important; }

.quick-tip {
  text-align: center;
  font-size: 12px;
  color: var(--gray-500);
  margin: 14px 0 0;
}
.quick-tip b {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
}

/* ============ 工具样式 ============ */
.gradient-text-warm {
  background: linear-gradient(90deg, #fb923c 0%, #fb7185 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}
.gradient-text-sunset {
  background: linear-gradient(90deg, #fbbf24 0%, #fb7185 50%, #a78bfa 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}
.gradient-text-spring {
  background: linear-gradient(90deg, #fb923c 0%, #fb7185 50%, #c4b5fd 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}
.gradient-text-mint {
  background: linear-gradient(90deg, #5eead4 0%, #14b8a6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

@media (max-width: 900px) {
  .login-container { grid-template-columns: 1fr; gap: 30px; }
  .login-brand { text-align: center; }
  .brand-features { display: none; }
  .brand-title { font-size: 28px; }
}
</style>