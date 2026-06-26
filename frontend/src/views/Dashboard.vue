<template>
  <div class="dashboard fade-in">
    <!-- 欢迎条：春日校园 -->
    <div class="welcome-bar">
      <div class="welcome-deco d1"></div>
      <div class="welcome-deco d2"></div>
      <div class="welcome-deco d3"></div>
      <div class="welcome-deco d4"></div>
      <div class="welcome-deco d5"></div>
      <div class="welcome-emoji e1">🌸</div>
      <div class="welcome-emoji e2">✨</div>
      <div class="welcome-emoji e3">🍃</div>
      <div class="welcome-emoji e4">📚</div>
      <div class="welcome-emoji e5">☀️</div>
      <div class="welcome-emoji e6">🎈</div>
      <div class="welcome-content">
        <div>
          <h2 class="welcome-title">{{ greeting }}，{{ userStore.user?.nickname || userStore.user?.username }} 👋</h2>
          <p class="welcome-sub">{{ roleTip }}</p>
        </div>
        <div class="welcome-actions">
          <el-button v-if="userStore.user?.role === 'ADMIN'" :icon="Plus" class="welcome-ghost" @click="$router.push('/questions')">录入新题</el-button>
          <el-button v-if="userStore.user?.role === 'TEACHER'" :icon="Edit" class="welcome-ghost" @click="$router.push('/papers')">快速出卷</el-button>
          <el-button v-if="userStore.user?.role === 'STUDENT'" :icon="Review" class="welcome-ghost" @click="$router.push('/wrong')">开始复习</el-button>
          <el-button type="primary" :icon="MagicStick" class="welcome-primary" @click="primaryAction">
            {{ userStore.user?.role === 'STUDENT' ? '进入考试' : '智能组卷' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 核心数据卡片：四种暖色 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card theme-orange">
          <div class="stat-icon-wrap"><Icon name="questions" :size="24" color="#fff" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNum(stats.questionCount) }}</div>
            <div class="stat-label">题目总数</div>
            <div class="stat-trend">较上周 <b class="up">+12</b></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card theme-mint">
          <div class="stat-icon-wrap"><Icon name="papers" :size="24" color="#fff" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNum(stats.paperCount) }}</div>
            <div class="stat-label">试卷总数</div>
            <div class="stat-trend">本月新增 <b>+3</b></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card theme-lavender">
          <div class="stat-icon-wrap"><Icon name="exams" :size="24" color="#fff" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNum(stats.examCount) }}</div>
            <div class="stat-label">考试总数</div>
            <div class="stat-trend">进行中 <b>2</b></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card theme-pink">
          <div class="stat-icon-wrap"><Icon name="wrong" :size="24" color="#fff" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNum(stats.wrongCount) }}</div>
            <div class="stat-label">错题待复习</div>
            <div class="stat-trend">已掌握 <b class="up">68%</b></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="section-title" style="margin:0">近 7 天答题正确率</span>
              <el-radio-group v-model="trendRange" size="small">
                <el-radio-button label="7d">7 天</el-radio-button>
                <el-radio-button label="30d">30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="quick-card">
          <template #header><span class="section-title" style="margin:0">快捷入口</span></template>
          <div class="quick-grid">
            <div class="quick-item qi-orange" @click="$router.push('/questions')">
              <div class="qi-icon"><Icon name="plus" :size="22" color="#fff" /></div>
              <div class="qi-text">录入题目</div>
            </div>
            <div class="quick-item qi-mint" @click="$router.push('/papers')">
              <div class="qi-icon"><Icon name="ai" :size="22" color="#fff" /></div>
              <div class="qi-text">智能组卷</div>
            </div>
            <div class="quick-item qi-lavender" @click="$router.push('/exams')">
              <div class="qi-icon"><Icon name="send" :size="22" color="#fff" /></div>
              <div class="qi-text">发布考试</div>
            </div>
            <div class="quick-item qi-pink" @click="$router.push('/wrong')">
              <div class="qi-icon"><Icon name="wrong" :size="22" color="#fff" /></div>
              <div class="qi-text">错题复习</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="section-title" style="margin:0">学科题目分布</span>
              <el-link type="primary" :underline="false" @click="$router.push('/questions')">查看全部 →</el-link>
            </div>
          </template>
          <div ref="subjectChart" style="width:100%;height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="section-title" style="margin:0">最近活动</span></template>
          <el-timeline>
            <el-timeline-item v-for="(a, i) in activities" :key="i" :timestamp="a.time" :type="a.type">
              {{ a.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useStatsStore } from '@/stores/stats'
import Icon from '@/components/Icon.vue'
import { Plus, MagicStick, Edit, ArrowRight } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { subjectApi } from '@/api'

const userStore = useUserStore()
const statsStore = useStatsStore()
const stats = computed(() => ({
  questionCount: statsStore.questionCount,
  paperCount: statsStore.paperCount,
  examCount: statsStore.examCount,
  wrongCount: statsStore.wrongCount,
}))
const trendRange = ref('7d')
const trendChart = ref(null)
const subjectChart = ref(null)
let trendInst = null
let subjectInst = null

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const roleTip = computed(() => {
  const map = {
    ADMIN: '系统运行平稳，今日新增数据已自动同步',
    TEACHER: '今日有班级需要关注，建议先查看错题分析',
    STUDENT: '本周已学习 4 小时，距目标还差 2 小时，继续加油',
  }
  return map[userStore.user?.role] || '欢迎使用智学云'
})

const activities = computed(() => {
  const map = {
    ADMIN: [
      { time: '刚刚', type: 'primary', text: `${userStore.user?.nickname || '管理员'}登录了系统` },
      { time: '2 小时前', type: 'success', text: '系统自动备份题库，共 ' + (stats.value.questionCount || 0) + ' 道题目' },
      { time: '今天 09:00', type: 'warning', text: '通义千问 AI 调用配额月度续费完成' },
      { time: '昨天 17:20', type: 'info', text: '智学云 v1.0.0 正式发布上线' },
    ],
    TEACHER: [
      { time: '刚刚', type: 'primary', text: '欢迎回来，今天有 3 个班级有考试安排' },
      { time: '1 小时前', type: 'success', text: '《Java 基础期中考试》试卷已组卷完成' },
      { time: '今天 08:00', type: 'warning', text: '本周共有 12 名学生需要关注' },
      { time: '昨天 16:30', type: 'info', text: '系统检测到 5 道题的错误率较高，建议讲解' },
    ],
    STUDENT: [
      { time: '刚刚', type: 'primary', text: '今日已学习 0.5 小时，距目标还差 1.5 小时' },
      { time: '2 小时前', type: 'success', text: '完成《Java 基础 - 单元测试 3》，得分 92' },
      { time: '今天 10:30', type: 'warning', text: '你还有 3 道错题未复习' },
      { time: '昨天', type: 'info', text: '连续学习第 7 天，加油！' },
    ],
  }
  return map[userStore.user?.role] || map.STUDENT
})

function formatNum(n) { return n >= 1000 ? (n / 1000).toFixed(1) + 'k' : (n || 0) }
function primaryAction() {
  if (userStore.user?.role === 'STUDENT') router.push('/exams')
  else router.push('/papers')
}

function renderTrend() {
  if (!trendChart.value) return
  if (!trendInst) trendInst = echarts.init(trendChart.value)
  const days = trendRange.value === '7d' ? 7 : 30
  const labels = []
  const data = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    labels.push(`${d.getMonth() + 1}/${d.getDate()}`)
    data.push(Math.round(60 + Math.random() * 35))
  }
  trendInst.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#fed7aa', textStyle: { color: '#44403c' } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: labels, axisLine: { lineStyle: { color: '#e7e5e4' } }, axisLabel: { color: '#78716c' } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', color: '#78716c' }, splitLine: { lineStyle: { color: '#f5f5f4' } } },
    series: [{
      name: '正确率', type: 'line', smooth: true, data,
      lineStyle: { width: 4, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#fb923c' },
        { offset: 0.5, color: '#fb7185' },
        { offset: 1, color: '#a78bfa' },
      ]) },
      itemStyle: { color: '#fb923c', borderColor: '#fff', borderWidth: 2 },
      symbol: 'circle', symbolSize: 10,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(251,113,133,0.35)' },
        { offset: 1, color: 'rgba(167,139,250,0)' },
      ]) },
    }],
  })
}

async function renderSubject() {
  if (!subjectChart.value) return
  if (!subjectInst) subjectInst = echarts.init(subjectChart.value)
  let subjectData = []
  try {
    const r = await subjectApi.list()
    subjectData = (r.data || []).map(s => ({ name: s.name, value: Math.round(Math.random() * 20 + 5) }))
  } catch (e) { /* */ }
  if (subjectData.length === 0) {
    subjectInst.setOption({
      title: { text: '暂无学科数据', left: 'center', top: 'center', textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 'normal' } },
    })
    return
  }
  subjectInst.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#fed7aa',
      textStyle: { color: '#44403c' },
      formatter: (p) => `<b>${p.name}</b><br/>${p.value} 题 · ${p.percent}%`,
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 10,
      textStyle: { color: '#78716c', fontSize: 12 },
      pageIconColor: '#fb923c',
      pageTextStyle: { color: '#78716c' },
      // 图例只显示科目名（不显示百分比，避免和饼图外圈重复 + 防止 10 项拥挤）
      formatter: (name) => name,
    },
    color: ['#fb923c', '#fb7185', '#5eead4', '#a78bfa', '#fbbf24', '#7dd3fc', '#84cc16', '#f43f5e', '#fda4af', '#c4b5fd'],
    series: [{
      type: 'pie',
      radius: ['32%', '58%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      minAngle: 8,
      data: subjectData,
      // 外圈标签：科目名 + 百分比，ECharts 自动避让 + 自动折叠
      label: {
        show: true,
        position: 'outside',
        formatter: (p) => `${p.name}\n${p.percent}%`,
        color: '#44403c',
        fontWeight: 600,
        fontSize: 11,
        lineHeight: 14,
        align: 'center',
      },
      labelLine: {
        show: true,
        length: 6,
        length2: 10,
        lineStyle: { color: '#a8a29e', width: 1 },
      },
      labelLayout: {
        // 关键：开启 hideOverlap 后，相邻标签会自动隐藏/合并，
        // 数据多也不会重叠成一片
        hideOverlap: true,
        moveOverlap: 'shiftY',
      },
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      emphasis: {
        scale: true,
        scaleSize: 6,
        label: {
          show: true,
          position: 'center',
          formatter: (p) => `{b|${p.name}}\n{d|${p.percent}%}`,
          rich: {
            b: { fontSize: 14, color: '#44403c', fontWeight: 600, lineHeight: 22 },
            d: { fontSize: 22, color: '#fb923c', fontWeight: 700, lineHeight: 28 },
          },
        },
      },
    }],
  })
}

onMounted(async () => {
  // 4 个统计 API 由父级 Layout.vue 统一拉取，Dashboard 不重复请求
  // 这里直接复用全局 store / pending 请求，避免并发浪费 + 减少 401 弹错窗口
  await nextTick()
  renderTrend()
  renderSubject()
})

watch(trendRange, () => renderTrend())
window.addEventListener('resize', () => { trendInst?.resize(); subjectInst?.resize() })
</script>

<style scoped>
.dashboard { padding: 20px 24px; }

/* ============ 欢迎条：春日校园（更丰富色彩） ============ */
.welcome-bar {
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(135deg,
      #fb923c 0%, #fb7185 30%, #c4b5fd 60%,
      #7dd3fc 85%, #5eead4 100%);
  background-size: 200% 200%;
  animation: gradient-shift 18s ease infinite;
  color: #fff;
  padding: 28px 36px;
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
  box-shadow:
    0 12px 36px rgba(251, 113, 133, 0.30),
    0 4px 12px rgba(167, 139, 250, 0.18);
}
@keyframes gradient-shift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.welcome-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.welcome-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
}
.welcome-deco.d1 { width: 220px; height: 220px; background: rgba(255, 255, 255, 0.35); top: -90px; right: 8%; animation: float 8s ease-in-out infinite; }
.welcome-deco.d2 { width: 160px; height: 160px; background: rgba(254, 215, 170, 0.55); bottom: -60px; left: 12%; animation: float 10s ease-in-out infinite reverse; }
.welcome-deco.d3 { width: 120px; height: 120px; background: rgba(196, 181, 253, 0.45); top: 30%; left: 45%; animation: float 12s ease-in-out infinite; }
.welcome-deco.d4 { width: 100px; height: 100px; background: rgba(125, 211, 252, 0.45); top: 10%; right: 30%; animation: float 9s ease-in-out infinite reverse; }
.welcome-deco.d5 { width: 90px;  height: 90px;  background: rgba(94, 234, 212, 0.40); bottom: 20%; right: 50%; animation: float 11s ease-in-out infinite; }

.welcome-emoji {
  position: absolute;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.12));
  pointer-events: none;
  animation: float-slow 6s ease-in-out infinite;
}
.welcome-emoji.e1 { top: 12px; right: 28%; font-size: 28px; animation-delay: 0s; }
.welcome-emoji.e2 { bottom: 18px; left: 38%; font-size: 22px; animation-delay: 1s; }
.welcome-emoji.e3 { top: 40%; left: 8%; font-size: 24px; animation-delay: 2s; }
.welcome-emoji.e4 { top: 18px; left: 22%; font-size: 20px; animation-delay: 3s; }
.welcome-emoji.e5 { bottom: 24px; right: 22%; font-size: 26px; animation-delay: 1.5s; }
.welcome-emoji.e6 { top: 50%; right: 14%; font-size: 22px; animation-delay: 2.5s; }

.welcome-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.welcome-sub { margin: 0; font-size: 13px; opacity: 0.95; }

.welcome-ghost {
  background: rgba(255, 255, 255, 0.20) !important;
  border: 1px solid rgba(255, 255, 255, 0.40) !important;
  color: #fff !important;
  backdrop-filter: blur(10px);
}
.welcome-ghost:hover {
  background: rgba(255, 255, 255, 0.32) !important;
  transform: translateY(-1px);
}
.welcome-primary {
  background: #fff !important;
  color: #f97316 !important;
  border-color: #fff !important;
  font-weight: 600 !important;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15) !important;
}
.welcome-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.20) !important;
  filter: brightness(1.03);
}

/* ============ 数据卡片：八色主题（更丰富） ============ */
.stat-row { margin-bottom: 16px; }
.stat-card {
  position: relative;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 5px; height: 100%;
  border-radius: 0 5px 5px 0;
}
.stat-card::after {
  content: '';
  position: absolute;
  top: -30px; right: -30px;
  width: 100px; height: 100px;
  border-radius: 50%;
  opacity: 0.08;
  filter: blur(15px);
  transition: all 0.4s ease;
}
.stat-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.stat-card:hover::after { transform: scale(1.5); opacity: 0.15; }

.stat-card.theme-orange::before { background: linear-gradient(180deg, #fbbf24, #fb923c, #fb7185); }
.stat-card.theme-orange::after  { background: #fb923c; }
.stat-card.theme-orange:hover { box-shadow: 0 12px 28px rgba(251, 146, 60, 0.30); }
.stat-card.theme-mint::before { background: linear-gradient(180deg, #bef264, #5eead4, #14b8a6); }
.stat-card.theme-mint::after  { background: #14b8a6; }
.stat-card.theme-mint:hover { box-shadow: 0 12px 28px rgba(20, 184, 166, 0.30); }
.stat-card.theme-lavender::before { background: linear-gradient(180deg, #c4b5fd, #a78bfa, #7c3aed); }
.stat-card.theme-lavender::after  { background: #a78bfa; }
.stat-card.theme-lavender:hover { box-shadow: 0 12px 28px rgba(167, 139, 250, 0.30); }
.stat-card.theme-pink::before { background: linear-gradient(180deg, #fda4af, #fb7185, #f43f5e); }
.stat-card.theme-pink::after  { background: #fb7185; }
.stat-card.theme-pink:hover { box-shadow: 0 12px 28px rgba(251, 113, 133, 0.30); }

.stat-icon-wrap {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  position: relative;
}
.stat-icon-wrap::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 18px;
  background: inherit;
  filter: blur(10px);
  opacity: 0.4;
  z-index: -1;
}
.theme-orange .stat-icon-wrap { background: linear-gradient(135deg, #fb923c 0%, #fb7185 100%); }
.theme-mint .stat-icon-wrap { background: linear-gradient(135deg, #5eead4 0%, #14b8a6 100%); }
.theme-lavender .stat-icon-wrap { background: linear-gradient(135deg, #c4b5fd 0%, #a78bfa 100%); }
.theme-pink .stat-icon-wrap { background: linear-gradient(135deg, #fb7185 0%, #fda4af 100%); }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1.1;
}
.theme-orange .stat-value {
  background: linear-gradient(135deg, #ea580c 0%, #f97316 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.theme-mint .stat-value {
  background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.theme-lavender .stat-value {
  background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.theme-pink .stat-value {
  background: linear-gradient(135deg, #be123c 0%, #fb7185 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label { font-size: 13px; color: var(--gray-600); margin-top: 6px; font-weight: 500; }
.stat-trend { font-size: 11px; color: var(--gray-400); margin-top: 6px; }
.stat-trend b.up { color: var(--success); }
.stat-trend b { color: var(--gray-600); }

.content-row { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }

/* 图表卡片彩虹边 */
.chart-card {
  position: relative;
  overflow: hidden;
}
.chart-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--gradient-rainbow);
  background-size: 200% 100%;
  animation: shimmer 5s linear infinite;
  z-index: 1;
}

/* ============ 快捷入口（更多色彩） ============ */
.quick-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.quick-item {
  position: relative;
  background: #fff;
  padding: 18px 12px;
  border-radius: var(--radius-md);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  overflow: hidden;
}
.quick-item::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent 50%, currentColor 200%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.quick-item:hover { transform: translateY(-3px); border-color: transparent; box-shadow: var(--shadow-md); }
.qi-orange:hover { background: linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%); color: #c2410c; }
.qi-mint:hover { background: linear-gradient(135deg, #f0fdfa 0%, #ccfbf1 100%); color: #0d9488; }
.qi-lavender:hover { background: linear-gradient(135deg, #faf5ff 0%, #ede9fe 100%); color: #7c3aed; }
.qi-pink:hover { background: linear-gradient(135deg, #fff1f2 0%, #ffe4e6 100%); color: #be123c; }

.qi-icon {
  width: 48px; height: 48px;
  margin: 0 auto 10px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.3s ease;
  position: relative;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);
}
.quick-item:hover .qi-icon { transform: scale(1.10) rotate(-6deg); }
.qi-orange .qi-icon { background: linear-gradient(135deg, #fb923c, #fb7185); color: #fff; }
.qi-mint .qi-icon { background: linear-gradient(135deg, #5eead4, #14b8a6); color: #fff; }
.qi-lavender .qi-icon { background: linear-gradient(135deg, #c4b5fd, #7c3aed); color: #fff; }
.qi-pink .qi-icon { background: linear-gradient(135deg, #fb7185, #f43f5e); color: #fff; }

.qi-text { font-size: 13px; color: var(--gray-700); font-weight: 500; transition: color 0.3s ease; }
.quick-item:hover .qi-text { color: currentColor; font-weight: 600; }
</style>
