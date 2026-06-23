<template>
  <div class="page-container fade-in">
    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span class="section-title" style="margin:0">📋 我的考试记录</span>
          <div>
            <el-button :icon="Refresh" @click="load">刷新</el-button>
            <el-button type="primary" :icon="VideoPlay" @click="$router.push('/exams')">去考试中心</el-button>
          </div>
        </div>
      </template>

      <!-- 概览卡片 -->
      <el-row :gutter="16" class="summary-row" v-if="records.length > 0">
        <el-col :span="6">
          <div class="summary-card">
            <div class="sc-num">{{ records.length }}</div>
            <div class="sc-label">参加考试</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-card theme-mint">
            <div class="sc-num">{{ records.filter(r => r.status === 'GRADED' || r.status === 'SUBMITTED').length }}</div>
            <div class="sc-label">已完成</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-card theme-lavender">
            <div class="sc-num">{{ avgScore }}</div>
            <div class="sc-label">平均分</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-card theme-pink">
            <div class="sc-num">{{ bestScore }}</div>
            <div class="sc-label">最高分</div>
          </div>
        </el-col>
      </el-row>

      <div v-loading="loading" class="record-list">
        <el-empty v-if="!loading && records.length === 0" description="还没有考试记录，去考试中心参加一场考试吧">
          <el-button type="primary" @click="$router.push('/exams')">前往考试中心</el-button>
        </el-empty>
        <div v-for="r in records" :key="r.id" class="record-card" :class="statusClass(r)">
          <div class="record-head">
            <h3 class="record-title">{{ r.examTitle || r.exam_title || '考试' }}</h3>
            <el-tag :type="statusTagType(r.status)" effect="dark" size="small">{{ statusLabel(r.status) }}</el-tag>
          </div>
          <div class="record-body">
            <div class="record-meta">
              <span>📅 {{ formatDate(r.submitTime || r.submit_time || r.startTime || r.start_time) }}</span>
              <span>⏱️ 用时 {{ r.durationUsed || r.duration_used || 0 }} 秒</span>
            </div>
            <div class="record-score" :class="scoreClass(r)">
              <div class="rs-val">{{ r.score || 0 }}</div>
              <div class="rs-total">/ {{ r.totalScore || r.total_score || 0 }}</div>
              <div class="rs-rate">{{ rate(r) }}%</div>
            </div>
          </div>
          <div class="record-foot">
            <el-button size="small" :icon="View" @click="viewDetail(r)">查看答题详情</el-button>
            <el-button v-if="r.status === 'GRADED' || r.status === 'SUBMITTED'" size="small" type="primary" plain :icon="Refresh" @click="reviewAgain(r)">重新挑战</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 答题详情 -->
    <el-dialog v-model="detailDialog.visible" title="答题详情" width="70%" top="5vh" :teleported="true">
      <div v-if="detailDialog.record">
        <el-descriptions :column="4" border style="margin-bottom:16px">
          <el-descriptions-item label="考试">{{ detailDialog.record.examTitle || detailDialog.record.exam_title }}</el-descriptions-item>
          <el-descriptions-item label="得分">
            <b :style="{ color: scoreColor(detailDialog.record) }">
              {{ detailDialog.record.score }} / {{ detailDialog.record.totalScore }}
            </b>
          </el-descriptions-item>
          <el-descriptions-item label="用时">{{ detailDialog.record.durationUsed || 0 }} 秒</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(detailDialog.record.status) }}</el-descriptions-item>
        </el-descriptions>
        <el-alert v-if="detailDialog.record.answers && detailDialog.record.answers.length > 0" :title="`答对 ${detailDialog.record.answers.filter(a => a.isCorrect).length} / ${detailDialog.record.answers.length} 题`" :type="rate(detailDialog.record) >= 60 ? 'success' : 'warning'" :closable="false" show-icon style="margin-bottom:16px" />
        <el-table v-if="detailDialog.record.answers" :data="detailDialog.record.answers" max-height="500" stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="题目" min-width="280">
            <template #default="{ row }">{{ row.question?.content || row.questionId }}</template>
          </el-table-column>
          <el-table-column label="你的答案" width="120" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.isCorrect ? '#10b981' : '#ef4444' }">{{ row.userAnswer || '未答' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="正确答案" width="120" align="center">
            <template #default="{ row }">{{ row.question?.answer || '—' }}</template>
          </el-table-column>
          <el-table-column label="结果" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isCorrect ? 'success' : 'danger'" size="small" effect="dark">
                {{ row.isCorrect ? '对' : '错' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无答题详情" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, VideoPlay, View } from '@element-plus/icons-vue'
import { examApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const records = ref([])
const detailDialog = reactive({ visible: false, record: null })

function statusLabel(s) { return { IN_PROGRESS: '进行中', SUBMITTED: '已交卷', GRADED: '已批改' }[s] || s || '—' }
function statusTagType(s) { return { IN_PROGRESS: 'warning', SUBMITTED: 'primary', GRADED: 'success' }[s] || 'info' }
function statusClass(r) {
  if (r.status === 'IN_PROGRESS') return 'status-pending'
  if (r.status === 'GRADED' && rate(r) >= 60) return 'status-pass'
  if (r.status === 'GRADED' && rate(r) < 60) return 'status-fail'
  return ''
}
function scoreClass(r) { const rt = rate(r); if (rt >= 80) return 'good'; if (rt >= 60) return 'mid'; return 'bad' }
function scoreColor(r) { const rt = rate(r); return rt >= 80 ? '#10b981' : rt >= 60 ? '#f97316' : '#ef4444' }
function rate(r) { return r.totalScore ? Math.round((r.score || 0) / r.totalScore * 100) : 0 }
function formatDate(d) { if (!d) return '—'; return String(d).replace('T', ' ').substring(0, 19) }

const avgScore = computed(() => {
  const done = records.value.filter(r => r.status === 'GRADED' || r.status === 'SUBMITTED')
  if (done.length === 0) return 0
  return Math.round(done.reduce((s, r) => s + rate(r), 0) / done.length)
})
const bestScore = computed(() => {
  const done = records.value.filter(r => r.status === 'GRADED' || r.status === 'SUBMITTED')
  if (done.length === 0) return 0
  return Math.max(...done.map(r => rate(r)))
})

async function load() {
  loading.value = true
  try {
    const r = await examApi.myRecords()
    records.value = r.data || []
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function viewDetail(r) { detailDialog.record = r; detailDialog.visible = true }
function reviewAgain(r) {
  // 跳到考试中心查找对应考试重新开始
  router.push('/exams')
}

onMounted(() => { load() })
</script>

<style scoped>
.table-header { display: flex; justify-content: space-between; align-items: center; }

.summary-row { margin-bottom: 16px; }
.summary-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border-left: 5px solid #fb923c;
}
.summary-card.theme-mint { border-left-color: #14b8a6; }
.summary-card.theme-lavender { border-left-color: #a78bfa; }
.summary-card.theme-pink { border-left-color: #fb7185; }
.sc-num { font-size: 32px; font-weight: 700; line-height: 1.1; background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.summary-card.theme-mint .sc-num { background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.summary-card.theme-lavender .sc-num { background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.summary-card.theme-pink .sc-num { background: linear-gradient(135deg, #be123c 0%, #fb7185 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.sc-label { font-size: 13px; color: var(--gray-600); margin-top: 4px; }

.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-card {
  background: #fff;
  border: 1px solid rgba(251, 146, 60, 0.10);
  border-radius: var(--radius-md);
  padding: 18px 22px;
  transition: all 0.3s ease;
}
.record-card.status-pass { border-left: 5px solid #14b8a6; }
.record-card.status-fail { border-left: 5px solid #ef4444; }
.record-card.status-pending { border-left: 5px solid #fbbf24; background: linear-gradient(135deg, #fff7ed 0%, #ffffff 100%); }
.record-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.record-title { margin: 0; font-size: 15px; font-weight: 600; color: #1c1917; }
.record-body { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.record-meta { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: var(--gray-500); flex: 1; }
.record-score { display: flex; align-items: baseline; gap: 4px; }
.rs-val { font-size: 32px; font-weight: 700; line-height: 1; }
.rs-val.good { background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.rs-val.mid { background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.rs-val.bad { color: #ef4444; }
.rs-total { font-size: 14px; color: var(--gray-500); }
.rs-rate { margin-left: 8px; font-size: 12px; color: var(--gray-500); }
.record-foot { display: flex; gap: 8px; margin-top: 12px; padding-top: 12px; border-top: 1px dashed var(--gray-200); }
</style>
