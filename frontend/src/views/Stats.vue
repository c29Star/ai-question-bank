<template>
  <div class="stats-page fade-in">
    <!-- 顶部 4 个核心指标 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="metric-card theme-orange">
          <div class="m-icon"><Icon name="duration" :size="22" color="#fff" /></div>
          <div class="m-content">
            <div class="m-value">{{ stats.totalHours || 48 }}<span class="m-unit">h</span></div>
            <div class="m-label">累计学习时长</div>
            <div class="m-trend">较上周 <b class="up">+6h</b></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="metric-card theme-mint">
          <div class="m-icon"><Icon name="questions" :size="22" color="#fff" /></div>
          <div class="m-content">
            <div class="m-value">{{ stats.totalCount || 387 }}</div>
            <div class="m-label">累计答题</div>
            <div class="m-trend">较上周 <b class="up">+42</b></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="metric-card theme-lavender">
          <div class="m-icon"><Icon name="target" :size="22" color="#fff" /></div>
          <div class="m-content">
            <div class="m-value">{{ ((stats.accuracy || 0.76) * 100).toFixed(1) }}<span class="m-unit">%</span></div>
            <div class="m-label">平均正确率</div>
            <div class="m-trend">较上周 <b class="up">+3.2%</b></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="metric-card theme-pink">
          <div class="m-icon"><Icon name="rank" :size="22" color="#fff" /></div>
          <div class="m-content">
            <div class="m-value">前 {{ stats.rankPercent || 18 }}<span class="m-unit">%</span></div>
            <div class="m-label">击败同学</div>
            <div class="m-trend">连续 <b class="up">{{ stats.streak || 7 }} 天</b> 学习中</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 左侧大图 + 右侧学科雷达 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-hd">
              <span class="section-title" style="margin:0">📅 近 30 天每日学习时长</span>
              <el-radio-group v-model="durationRange" size="small">
                <el-radio-button label="7d">7 天</el-radio-button>
                <el-radio-button label="30d">30 天</el-radio-button>
                <el-radio-button label="90d">90 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="durationChart" style="width:100%;height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="section-title" style="margin:0">🎯 各学科能力雷达</span>
          </template>
          <div ref="radarChart" style="width:100%;height:320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各科表现 + 学习徽章 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-hd">
              <span class="section-title" style="margin:0">📚 各科表现详情</span>
              <el-button :icon="Refresh" size="small" @click="load">刷新</el-button>
            </div>
          </template>
          <el-table :data="subjectStats" stripe>
            <el-table-column prop="name" label="学科" min-width="120" />
            <el-table-column prop="total" label="答题数" align="center" width="100" />
            <el-table-column prop="correct" label="答对数" align="center" width="100" />
            <el-table-column label="正确率" min-width="200">
              <template #default="{ row }">
                <div class="acc-bar">
                  <div class="acc-num">{{ (row.accuracy * 100).toFixed(0) }}%</div>
                  <div class="acc-progress">
                    <div class="acc-fill" :style="{ width: row.accuracy * 100 + '%', background: row.accuracy > 0.8 ? 'linear-gradient(90deg,#5eead4,#14b8a6)' : row.accuracy > 0.6 ? 'linear-gradient(90deg,#fb923c,#fb7185)' : 'linear-gradient(90deg,#fbbf24,#fb923c)' }"></div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="等级" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="row.accuracy > 0.85 ? 'success' : row.accuracy > 0.7 ? 'primary' : 'warning'" effect="dark" size="small">
                  {{ row.accuracy > 0.85 ? '优秀' : row.accuracy > 0.7 ? '良好' : '一般' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="badge-card">
          <template #header>
            <span class="section-title" style="margin:0">🏅 学习徽章</span>
          </template>
          <div class="badges">
            <div v-for="b in badges" :key="b.id" class="badge" :class="{ earned: b.earned }">
              <div class="badge-icon-wrap" :style="{ background: b.earned ? b.color : '#e7e5e4' }">
                <Icon :name="b.icon" :size="28" :color="b.earned ? '#fff' : '#a8a29e'" />
              </div>
              <div class="badge-name">{{ b.name }}</div>
              <div class="badge-desc">{{ b.desc }}</div>
              <div v-if="b.earned" class="badge-check"><Icon name="correct" :size="12" color="#fff" /></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考试记录表 + 知识点热力 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-hd">
              <span class="section-title" style="margin:0">📝 近期考试记录</span>
              <el-button type="primary" :icon="Pdf" size="small" plain>导出学习报告</el-button>
            </div>
          </template>
          <el-table :data="examRecords" stripe>
            <el-table-column prop="date" label="考试日期" width="120" />
            <el-table-column prop="name" label="考试名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="score" label="得分" align="center" width="80">
              <template #default="{ row }">
                <b :style="{ color: row.score >= 80 ? '#14b8a6' : row.score >= 60 ? '#f97316' : '#ef4444' }">{{ row.score }}</b>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="排名" align="center" width="80" />
            <el-table-column prop="wrongCount" label="错题" align="center" width="80" />
            <el-table-column label="评价" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'primary' : 'danger'" effect="light" size="small">
                  {{ row.score >= 80 ? '优秀' : row.score >= 60 ? '及格' : '加油' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="section-title" style="margin:0">🎯 知识点掌握度</span>
          </template>
          <div class="knowledge">
            <div v-for="(k, i) in knowledge" :key="i" class="kp-item">
              <div class="kp-head">
                <span class="kp-name">{{ k.name }}</span>
                <span class="kp-pct" :style="{ color: k.score > 0.8 ? '#14b8a6' : k.score > 0.6 ? '#f97316' : '#ef4444' }">{{ (k.score * 100).toFixed(0) }}%</span>
              </div>
              <div class="kp-bar">
                <div class="kp-fill" :style="{ width: k.score * 100 + '%', background: k.score > 0.8 ? 'linear-gradient(90deg,#5eead4,#14b8a6)' : k.score > 0.6 ? 'linear-gradient(90deg,#fb923c,#fb7185)' : 'linear-gradient(90deg,#fb7185,#f43f5e)' }"></div>
              </div>
            </div>
          </div>
          <el-alert type="warning" :closable="false" style="margin-top:12px" show-icon>
            <template #title>
              <span style="font-size:12px">薄弱知识点：<b>多线程、JVM 内存模型</b>，建议针对性练习</span>
            </template>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Document as Pdf } from '@element-plus/icons-vue'
import Icon from '@/components/Icon.vue'
import * as echarts from 'echarts'
import { statsApi } from '@/api'

const stats = ref({ totalHours: 48, totalCount: 387, accuracy: 0.76, rankPercent: 18, streak: 7 })
const durationRange = ref('30d')
const durationChart = ref(null)
const radarChart = ref(null)
let durationInst = null
let radarInst = null

const subjectStats = ref([
  { name: 'Java 基础', total: 86, correct: 74, accuracy: 0.86 },
  { name: '数据库', total: 64, correct: 52, accuracy: 0.81 },
  { name: '前端开发', total: 42, correct: 26, accuracy: 0.62 },
  { name: '网络协议', total: 38, correct: 19, accuracy: 0.5 },
  { name: '操作系统', total: 30, correct: 18, accuracy: 0.6 },
  { name: 'Spring Boot', total: 50, correct: 43, accuracy: 0.86 },
])

const badges = [
  { id: 1, name: '初学者', desc: '完成 10 题', icon: 'target', color: 'linear-gradient(135deg,#cd7f32,#a0522d)', earned: true },
  { id: 2, name: '勤奋学子', desc: '完成 50 题', icon: 'duration', color: 'linear-gradient(135deg,#c0c0c0,#808080)', earned: true },
  { id: 3, name: '学霸', desc: '正确率 80%', icon: 'rank', color: 'linear-gradient(135deg,#ffd700,#ff8c00)', earned: true },
  { id: 4, name: '满贯王', desc: '连续 30 天', icon: 'flame', color: 'linear-gradient(135deg,#ef4444,#dc2626)', earned: false },
  { id: 5, name: '考神', desc: '单次满分', icon: 'badge', color: 'linear-gradient(135deg,#8b5cf6,#6d28d9)', earned: false },
  { id: 6, name: '题海王', desc: '完成 1000 题', icon: 'subjects', color: 'linear-gradient(135deg,#0ea5e9,#0369a1)', earned: false },
]

const examRecords = ref([
  { date: '2026-06-20', name: 'Java 基础 - 单元测试 3', score: 92, rank: '3/48', wrongCount: 2 },
  { date: '2026-06-15', name: 'Spring Boot 实战模拟', score: 85, rank: '8/48', wrongCount: 5 },
  { date: '2026-06-08', name: '数据库 SQL 进阶', score: 78, rank: '12/48', wrongCount: 7 },
  { date: '2026-05-30', name: '前端 Vue 基础测验', score: 68, rank: '18/48', wrongCount: 9 },
  { date: '2026-05-22', name: '网络协议期中', score: 55, rank: '28/48', wrongCount: 14 },
])

const knowledge = ref([
  { name: 'Java 语法基础', score: 0.92 },
  { name: '面向对象', score: 0.88 },
  { name: '集合框架', score: 0.85 },
  { name: '异常处理', score: 0.78 },
  { name: 'I/O 流', score: 0.72 },
  { name: '多线程', score: 0.45 },
  { name: 'JVM 内存模型', score: 0.38 },
  { name: '反射与注解', score: 0.65 },
])

function renderDuration() {
  if (!durationChart.value) return
  try {
    if (!durationInst) durationInst = echarts.init(durationChart.value)
    const days = parseInt(durationRange.value)
    const labels = []
    const data = []
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      labels.push(`${d.getMonth() + 1}/${d.getDate()}`)
      data.push(Math.round(0.5 + Math.random() * 4))
    }
    durationInst.setOption({
      tooltip: { trigger: 'axis', formatter: (p) => `${p[0].name}<br/>学习时长: <b>${p[0].value}h</b>`, backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#fed7aa', textStyle: { color: '#44403c' } },
      grid: { left: 40, right: 20, top: 30, bottom: 30 },
      xAxis: { type: 'category', data: labels, axisLine: { lineStyle: { color: '#e7e5e4' } }, axisLabel: { color: '#78716c' } },
      yAxis: { type: 'value', name: '小时', axisLabel: { formatter: '{value}h', color: '#78716c' }, splitLine: { lineStyle: { color: '#f5f5f4' } } },
      series: [{
        name: '学习时长', type: 'bar', data, barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#fb923c' }, { offset: 1, color: '#fed7aa' },
          ]),
          borderRadius: [8, 8, 0, 0],
        },
      }],
    })
  } catch (e) { console.error('renderDuration error', e) }
}

function renderRadar() {
  if (!radarChart.value) return
  try {
    if (!radarInst) radarInst = echarts.init(radarChart.value)
    radarInst.setOption({
      tooltip: { backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#fed7aa', textStyle: { color: '#44403c' } },
      radar: {
        indicator: [
          { name: 'Java 基础', max: 100 },
          { name: '数据库', max: 100 },
          { name: '前端', max: 100 },
          { name: '网络', max: 100 },
          { name: '操作系统', max: 100 },
          { name: 'Spring', max: 100 },
        ],
        radius: '65%',
        splitArea: { areaStyle: { color: ['rgba(251,146,60,0.06)', 'rgba(251,113,133,0.02)'] } },
        splitLine: { lineStyle: { color: 'rgba(251,146,60,0.15)' } },
        axisLine: { lineStyle: { color: 'rgba(251,146,60,0.15)' } },
        name: { textStyle: { color: '#78716c', fontSize: 12 } },
      },
      series: [{
        type: 'radar',
        data: [{
          value: [86, 81, 62, 50, 60, 86],
          name: '我的能力',
          areaStyle: { color: 'rgba(251,113,133,0.28)' },
          lineStyle: { color: '#fb7185', width: 2 },
          itemStyle: { color: '#fb7185' },
        }, {
          value: [70, 70, 70, 70, 70, 70],
          name: '班级平均',
          lineStyle: { color: '#a8a29e', type: 'dashed' },
          itemStyle: { color: '#a8a29e' },
        }],
      }],
    })
  } catch (e) { console.error('renderRadar error', e) }
}

async function load() {
  try { const r = await statsApi.personal(); stats.value = r.data } catch (e) { ElMessage.error('加载失败') }
}

onMounted(async () => {
  await load()
  await nextTick()
  try { renderDuration() } catch (e) { console.error('renderDuration failed', e) }
  try { renderRadar() } catch (e) { console.error('renderRadar failed', e) }
})

watch(durationRange, () => renderDuration())

function onResize() { durationInst?.resize(); radarInst?.resize() }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  durationInst?.dispose(); durationInst = null
  radarInst?.dispose(); radarInst = null
})
</script>

<style scoped>
.stats-page { padding: 20px 24px; }

.stat-row { margin-bottom: 16px; }
.metric-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 22px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: var(--shadow);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}
.metric-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 5px; height: 100%;
  border-radius: 0 5px 5px 0;
}
.metric-card::after {
  content: '';
  position: absolute;
  top: -30px; right: -30px;
  width: 100px; height: 100px;
  border-radius: 50%;
  opacity: 0.08;
  filter: blur(15px);
  transition: all 0.4s ease;
}
.metric-card.theme-orange::before { background: linear-gradient(180deg, #fbbf24, #fb923c, #fb7185); }
.metric-card.theme-orange::after  { background: #fb923c; }
.metric-card.theme-orange:hover { box-shadow: 0 12px 28px rgba(251, 146, 60, 0.30); }
.metric-card.theme-mint::before { background: linear-gradient(180deg, #bef264, #5eead4, #14b8a6); }
.metric-card.theme-mint::after  { background: #14b8a6; }
.metric-card.theme-mint:hover { box-shadow: 0 12px 28px rgba(20, 184, 166, 0.30); }
.metric-card.theme-lavender::before { background: linear-gradient(180deg, #c4b5fd, #a78bfa, #7c3aed); }
.metric-card.theme-lavender::after  { background: #a78bfa; }
.metric-card.theme-lavender:hover { box-shadow: 0 12px 28px rgba(167, 139, 250, 0.30); }
.metric-card.theme-pink::before { background: linear-gradient(180deg, #fda4af, #fb7185, #f43f5e); }
.metric-card.theme-pink::after  { background: #fb7185; }
.metric-card.theme-pink:hover { box-shadow: 0 12px 28px rgba(251, 113, 133, 0.30); }
.metric-card:hover { transform: translateY(-4px); }
.metric-card:hover::after { transform: scale(1.5); opacity: 0.16; }

.m-icon {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  position: relative;
}
.m-icon::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 18px;
  background: inherit;
  filter: blur(10px);
  opacity: 0.4;
  z-index: -1;
}
.theme-orange .m-icon { background: linear-gradient(135deg, #fb923c 0%, #fb7185 100%); }
.theme-mint .m-icon { background: linear-gradient(135deg, #5eead4 0%, #14b8a6 100%); }
.theme-lavender .m-icon { background: linear-gradient(135deg, #c4b5fd 0%, #a78bfa 100%); }
.theme-pink .m-icon { background: linear-gradient(135deg, #fb7185 0%, #fda4af 100%); }

.m-value { font-size: 28px; font-weight: 700; line-height: 1.1; }
.theme-orange .m-value { background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.theme-mint .m-value { background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.theme-lavender .m-value { background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.theme-pink .m-value { background: linear-gradient(135deg, #be123c 0%, #fb7185 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.m-unit { font-size: 14px; color: var(--gray-400); margin-left: 2px; font-weight: 500; }
.m-label { font-size: 13px; color: var(--gray-600); margin-top: 6px; font-weight: 500; }
.m-trend { font-size: 11px; color: var(--gray-400); margin-top: 6px; }
.m-trend b.up { color: var(--success); }

.chart-row { margin-bottom: 16px; }
.card-hd { display: flex; align-items: center; justify-content: space-between; }

.acc-bar { display: flex; align-items: center; gap: 12px; }
.acc-num { font-weight: 600; color: var(--gray-800); width: 40px; flex-shrink: 0; font-size: 13px; }
.acc-progress { flex: 1; height: 8px; background: var(--gray-100); border-radius: 999px; overflow: hidden; }
.acc-fill { height: 100%; border-radius: 999px; transition: width 0.6s ease; }

.badges { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }
.badge {
  position: relative;
  padding: 14px 8px;
  background: var(--gray-50);
  border: 2px solid var(--gray-200);
  border-radius: var(--radius-md);
  text-align: center;
  transition: all 0.3s ease;
  overflow: hidden;
}
.badge::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--gradient-rainbow);
  background-size: 200% 100%;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: shimmer 5s linear infinite paused;
}
.badge.earned {
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 50%, #f0f9ff 100%);
  border-color: #fbbf24;
}
.badge.earned:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(251, 191, 36, 0.30); }
.badge.earned:hover::before { opacity: 1; animation-play-state: running; }
.badge-icon-wrap {
  width: 52px; height: 52px;
  border-radius: 50%;
  margin: 0 auto 8px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.10);
  position: relative;
}
.badge.earned .badge-icon-wrap { animation: pulse-soft 3s ease-in-out infinite; }
.badge-name { font-size: 13px; font-weight: 600; color: var(--gray-800); }
.badge-desc { font-size: 11px; color: var(--gray-500); margin-top: 2px; }
.badge-check {
  position: absolute; top: 4px; right: 4px;
  width: 20px; height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5eead4, #14b8a6);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 6px rgba(20, 184, 166, 0.45);
  color: #fff;
}

.knowledge { display: flex; flex-direction: column; gap: 14px; }
.kp-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.kp-name { font-size: 13px; color: var(--gray-700); }
.kp-pct { font-size: 13px; font-weight: 700; }
.kp-bar { height: 8px; background: var(--gray-100); border-radius: 999px; overflow: hidden; }
.kp-fill { height: 100%; border-radius: 999px; transition: width 0.6s ease; }
</style>
