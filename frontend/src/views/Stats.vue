<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="card">
          <h3>📈 成绩趋势</h3>
          <div ref="scoreChart" style="height: 320px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <h3>🎯 错题分布（按知识点）</h3>
          <div ref="wrongChart" style="height: 320px"></div>
        </div>
      </el-col>
    </el-row>

    <div class="card mt-16">
      <h3>📊 学习概览</h3>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="已完成考试">{{ overview.totalExams || 0 }} 次</el-descriptions-item>
        <el-descriptions-item label="平均分">{{ overview.avgScore || 0 }} 分</el-descriptions-item>
        <el-descriptions-item label="错题总数">{{ overview.wrongTotal || 0 }} 道</el-descriptions-item>
        <el-descriptions-item label="已掌握">{{ overview.masteredCount || 0 }} 道</el-descriptions-item>
        <el-descriptions-item label="未掌握">{{ overview.unmasteredCount || 0 }} 道</el-descriptions-item>
        <el-descriptions-item label="掌握率">{{ masterRate }}%</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { statsApi, wrongApi } from '@/api'

const scoreChart = ref(null)
const wrongChart = ref(null)
const overview = ref({})
const scoreTrend = ref([])
const wrongDist = ref([])
let scoreInst = null
let wrongInst = null

const masterRate = computed(() => {
  const m = overview.value.masteredCount || 0
  const u = overview.value.unmasteredCount || 0
  const t = m + u
  return t === 0 ? 0 : Math.round((m / t) * 100)
})

function renderScore() {
  if (!scoreInst) scoreInst = echarts.init(scoreChart.value)
  scoreInst.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: scoreTrend.value.map(s => s.date) },
    yAxis: { type: 'value', max: 100 },
    series: [{
      name: '成绩',
      type: 'line',
      data: scoreTrend.value.map(s => s.score),
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409eff' },
    }],
  })
}

function renderWrong() {
  if (!wrongInst) wrongInst = echarts.init(wrongChart.value)
  wrongInst.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: wrongDist.value,
      label: { formatter: '{b}: {c}' },
    }],
  })
}

onMounted(async () => {
  const stats = await statsApi.personal()
  overview.value = stats.overview
  scoreTrend.value = stats.scoreTrend || []
  wrongDist.value = stats.wrongDist || []
  renderScore()
  renderWrong()
  window.addEventListener('resize', () => { scoreInst?.resize(); wrongInst?.resize() })
})
</script>
