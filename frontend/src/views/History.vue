<template>
  <div class="page">
    <div class="card">
      <h3>历史考试记录</h3>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="paper_title" label="试卷" />
        <el-table-column prop="exam_title" label="考试" />
        <el-table-column label="得分" width="100">
          <template #default="{ row }">
            <span :class="scoreClass(row.score, row.total_score)">{{ row.score || '-' }} / {{ row.total_score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'GRADED' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用时" width="100">
          <template #default="{ row }">{{ formatDuration(row.duration_used) }}</template>
        </el-table-column>
        <el-table-column prop="submit_time" label="提交时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { examApi } from '@/api'

const records = ref([])
const loading = ref(false)
const STATUS = { IN_PROGRESS: '进行中', SUBMITTED: '已提交', GRADED: '已批改' }
const statusLabel = (s) => STATUS[s] || s

function scoreClass(s, t) {
  if (!s) return ''
  const p = s / t
  if (p >= 0.9) return 'text-success'
  if (p >= 0.6) return 'text-warn'
  return 'text-danger'
}

function formatDuration(sec) {
  if (!sec) return '-'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}分${s}秒`
}

onMounted(async () => {
  loading.value = true
  try { records.value = await examApi.myRecords() } finally { loading.value = false }
})
</script>

<style scoped>
.text-success { color: #67c23a; font-weight: 600; }
.text-warn { color: #e6a23c; font-weight: 600; }
.text-danger { color: #f56c6c; font-weight: 600; }
</style>
