<template>
  <div class="exam-page">
    <!-- 顶部：考试信息 + 倒计时 -->
    <div class="exam-top">
      <div class="exam-top-left">
        <h2 class="exam-h">{{ examInfo.title }}</h2>
        <div class="exam-meta">
          <span>📚 {{ paper?.title }}</span>
          <span>📝 {{ questions.length }} 题</span>
          <span>💯 {{ paper?.totalScore || 0 }} 分</span>
          <span>⏱️ {{ paper?.duration || 0 }} 分钟</span>
        </div>
      </div>
      <div class="exam-top-right">
        <div class="countdown" :class="{ warn: timeLeft < 60 }">
          <div class="cd-label">剩余时间</div>
          <div class="cd-num">{{ formattedTime }}</div>
        </div>
        <el-button :icon="Check" type="primary" size="large" @click="confirmSubmit">交卷</el-button>
      </div>
    </div>

    <!-- 主体：左题号 + 右题目 -->
    <div class="exam-body">
      <!-- 题号卡片 -->
      <div class="qno-card">
        <div class="qno-title">答题卡</div>
        <div class="qno-grid">
          <div v-for="(q, i) in questions" :key="q.questionId"
               class="qno-item"
               :class="{
                 answered: !!answers[q.questionId],
                 current: currentIdx === i,
               }"
               @click="currentIdx = i">
            {{ i + 1 }}
          </div>
        </div>
        <div class="qno-legend">
          <span><i class="dot answered-dot"></i>已答</span>
          <span><i class="dot current-dot"></i>当前</span>
        </div>
      </div>

      <!-- 题目区 -->
      <div class="q-card" v-if="currentQ">
        <div class="q-head">
          <span class="q-no">第 {{ currentIdx + 1 }} / {{ questions.length }} 题</span>
          <el-tag size="small" :type="typeTagType(currentQ.type)" effect="light">{{ typeLabel(currentQ.type) }}</el-tag>
          <el-rate :model-value="currentQ.difficulty" disabled :show-text="false" :max="5" size="small" />
          <span class="q-score">{{ currentQ.score || 0 }} 分</span>
        </div>
        <div class="q-content">{{ currentQ.content }}</div>

        <!-- 单选/判断 -->
        <el-radio-group v-if="currentQ.type === 'SINGLE' || currentQ.type === 'JUDGE'" v-model="answers[currentQ.questionId]" class="q-opts" @change="(v) => onAnswer(currentQ.questionId, v)">
          <el-radio v-for="(o, i) in currentQ.options" :key="i" :value="String.fromCharCode(65 + i)" class="q-opt">{{ String.fromCharCode(65 + i) }}. {{ o }}</el-radio>
        </el-radio-group>
        <!-- 多选 -->
        <el-checkbox-group v-else-if="currentQ.type === 'MULTIPLE'" v-model="answers[currentQ.questionId]" class="q-opts" @change="(v) => onAnswer(currentQ.questionId, v.join(''))">
          <el-checkbox v-for="(o, i) in currentQ.options" :key="i" :value="String.fromCharCode(65 + i)" class="q-opt">{{ String.fromCharCode(65 + i) }}. {{ o }}</el-checkbox>
        </el-checkbox-group>
        <!-- 填空/简答 -->
        <el-input
          v-else-if="currentQ.type === 'FILL' || currentQ.type === 'ESSAY'"
          v-model="answers[currentQ.questionId]"
          type="textarea"
          :rows="4"
          placeholder="请输入答案"
          @input="(v) => onAnswer(currentQ.questionId, v)"
        />

        <div class="q-foot">
          <el-button :disabled="currentIdx === 0" @click="currentIdx--">上一题</el-button>
          <el-button v-if="currentIdx < questions.length - 1" type="primary" @click="currentIdx++">下一题</el-button>
          <el-button v-else type="success" @click="confirmSubmit">交卷</el-button>
        </div>
      </div>
      <el-empty v-else description="加载中..." />
    </div>

    <!-- 交卷结果 -->
    <el-dialog v-model="resultDialog.visible" title="🎉 考试完成" width="600px" :show-close="false" :align-center="true" :teleported="true">
      <div v-if="resultDialog.result" class="result-wrap">
        <div class="result-score" :style="{ background: resultDialog.result.score / resultDialog.result.totalScore >= 0.6 ? 'linear-gradient(135deg,#10b981,#34d399)' : 'linear-gradient(135deg,#f59e0b,#fbbf24)' }">
          <div class="rs-num">{{ resultDialog.result.score }}<span class="rs-total"> / {{ resultDialog.result.totalScore }}</span></div>
          <div class="rs-label">本次得分</div>
        </div>
        <el-table :data="resultDialog.result.results" max-height="320" stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="question.content" label="题目" min-width="240" show-overflow-tooltip />
          <el-table-column label="你的答案" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.isCorrect ? '#10b981' : '#ef4444' }">{{ row.userAnswer || '未答' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="question.answer" label="正确答案" width="100" align="center" />
          <el-table-column label="结果" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isCorrect ? 'success' : 'danger'" size="small" effect="dark">
                {{ row.isCorrect ? '对' : '错' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="goBack">返回考试中心</el-button>
        <el-button type="primary" @click="goHistory">查看考试记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, ArrowLeft, ArrowRight, VideoPlay } from '@element-plus/icons-vue'
import { examApi } from '@/api'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.id)

const loading = ref(false)
const record = ref(null)
const paper = ref(null)
const questions = ref([])
const currentIdx = ref(0)
const answers = reactive({})
const startTime = ref(Date.now())
const timeLeft = ref(0) // 秒
let timer = null

const examInfo = computed(() => record.value?.exam || { title: '考试作答' })
const currentQ = computed(() => questions.value[currentIdx.value])
const formattedTime = computed(() => {
  const s = Math.max(0, timeLeft.value)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
})

const resultDialog = reactive({ visible: false, result: null })

function typeLabel(t) { return { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', FILL: '填空', ESSAY: '简答', QA: '问答', CALC: '计算' }[t] || t }
function typeTagType(t) { return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning', FILL: 'info', ESSAY: 'danger', QA: 'danger', CALC: 'success' }[t] || '' }

async function start() {
  loading.value = true
  try {
    const r = await examApi.start(examId)
    const data = r.data || {}
    record.value = data.record || {}
    paper.value = data.paper || {}
    questions.value = (data.questions || []).map(q => ({
      questionId: q.questionId,
      score: q.score,
      sortOrder: q.sortOrder,
      ...(q.question || {}),
      // options 是 JSON 字符串，解析为数组
      options: typeof q.question?.options === 'string' ? parseOptions(q.question.options) : (q.question?.options || []),
    }))
    // 回显已保存的答案
    questions.value.forEach(q => {
      if (q.answer) answers[q.questionId] = q.answer
    })
    // 初始化倒计时
    if (paper.value.duration) {
      // 用服务端 record.startTime 计算已用时
      const used = record.value.startTime ? Math.floor((Date.now() - new Date(record.value.startTime).getTime()) / 1000) : 0
      timeLeft.value = Math.max(0, paper.value.duration * 60 - used)
    }
    startTime.value = Date.now()
    startTimer()
  } catch (e) {
    ElMessage.error('开始考试失败：' + (e.response?.data?.message || e.message))
    setTimeout(() => router.push('/exams'), 1500)
  } finally { loading.value = false }
}

function parseOptions(s) {
  if (!s) return []
  try {
    let o = s.trim()
    if (o.startsWith('[')) o = o.substring(1)
    if (o.endsWith(']')) o = o.substring(0, o.length() - 1)
    return o.split(',').map(x => x.trim().replaceAll(/^"|"$/g, '').replaceAll('\\"', '"')).filter(Boolean)
  } catch { return [] }
}

function startTimer() {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('考试时间到，自动交卷')
      doSubmit()
    }
  }, 1000)
}

async function onAnswer(qid, val) {
  if (val === undefined || val === null || val === '') {
    delete answers[qid]
  } else {
    answers[qid] = String(val)
  }
  // 自动保存（防抖）
  clearTimeout(window.__examSaveTimer)
  window.__examSaveTimer = setTimeout(() => {
    examApi.saveAnswer({
      recordId: record.value.id,
      questionId: qid,
      userAnswer: answers[qid] || '',
    }).catch(() => { /* 静默 */ })
  }, 800)
}

async function confirmSubmit() {
  const answered = Object.values(answers).filter(Boolean).length
  try {
    await ElMessageBox.confirm(
      `已答 ${answered} / ${questions.value.length} 题。${answered < questions.value.length ? '还有题目未答，确定交卷？' : '确定交卷？'}`,
      '提示',
      { type: answered < questions.value.length ? 'warning' : 'success' }
    )
  } catch (e) { return }
  doSubmit()
}

async function doSubmit() {
  if (!record.value?.id) return
  try {
    const durationUsed = Math.floor((Date.now() - startTime.value) / 1000)
    const r = await examApi.submit({
      recordId: record.value.id,
      durationUsed,
      answers: questions.value.map(q => ({
        questionId: q.questionId,
        userAnswer: answers[q.questionId] || '',
      })),
    })
    resultDialog.result = r.data
    resultDialog.visible = true
    if (timer) clearInterval(timer)
  } catch (e) {
    ElMessage.error('交卷失败：' + (e.response?.data?.message || e.message))
  }
}

function goBack() { router.push('/exams') }
function goHistory() { router.push('/history') }

// 离开提示
onBeforeRouteLeave((to, from, next) => {
  if (resultDialog.visible) return next()
  ElMessageBox.confirm('当前考试进行中，离开后答案可能不保存，确定离开？', '提示', { type: 'warning' })
    .then(() => next())
    .catch(() => next(false))
})

onUnmounted(() => { if (timer) clearInterval(timer) })

onMounted(() => { start() })
</script>

<style scoped>
.exam-page { min-height: 100vh; background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 50%, #fce7f3 100%); padding: 24px; }

.exam-top {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-radius: var(--radius-lg);
  padding: 20px 28px; margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(251, 113, 133, 0.10);
}
.exam-h { margin: 0 0 6px; font-size: 20px; font-weight: 700; color: #1c1917; }
.exam-meta { display: flex; gap: 16px; font-size: 13px; color: var(--gray-600); }
.exam-top-right { display: flex; align-items: center; gap: 16px; }
.countdown { text-align: center; padding: 8px 18px; background: linear-gradient(135deg, #5eead4 0%, #14b8a6 100%); color: #fff; border-radius: 12px; box-shadow: 0 4px 12px rgba(20, 184, 166, 0.30); }
.countdown.warn { background: linear-gradient(135deg, #fb7185 0%, #f43f5e 100%); animation: pulse 1s infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.05); } }
.cd-label { font-size: 11px; opacity: 0.9; }
.cd-num { font-size: 26px; font-weight: 700; line-height: 1.1; font-variant-numeric: tabular-nums; }

.exam-body { display: grid; grid-template-columns: 240px 1fr; gap: 16px; }

.qno-card { background: #fff; border-radius: var(--radius-md); padding: 18px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04); height: fit-content; position: sticky; top: 20px; }
.qno-title { font-size: 15px; font-weight: 600; color: #1c1917; margin-bottom: 12px; }
.qno-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 6px; }
.qno-item {
  aspect-ratio: 1;
  display: flex; align-items: center; justify-content: center;
  background: var(--gray-50); border: 1.5px solid var(--gray-200);
  border-radius: 8px; font-size: 13px; font-weight: 500;
  color: var(--gray-600); cursor: pointer; transition: all 0.2s ease;
}
.qno-item:hover { border-color: #fb923c; }
.qno-item.answered { background: linear-gradient(135deg, #ccfbf1 0%, #99f6e4 100%); border-color: #14b8a6; color: #0d9488; }
.qno-item.current { background: linear-gradient(135deg, #fb923c 0%, #fb7185 100%); border-color: #fb7185; color: #fff; box-shadow: 0 4px 12px rgba(251, 113, 133, 0.40); }
.qno-legend { display: flex; gap: 12px; font-size: 11px; color: var(--gray-500); margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--gray-200); }
.qno-legend .dot { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 4px; vertical-align: middle; }
.answered-dot { background: linear-gradient(135deg, #5eead4, #14b8a6); }
.current-dot { background: linear-gradient(135deg, #fb923c, #fb7185); }

.q-card { background: #fff; border-radius: var(--radius-md); padding: 28px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04); }
.q-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.q-no { font-weight: 600; color: #c2410c; }
.q-score { margin-left: auto; color: #fb7185; font-weight: 600; font-size: 14px; }
.q-content { font-size: 16px; line-height: 1.8; color: #1c1917; margin-bottom: 24px; padding: 16px; background: linear-gradient(135deg, #fff7ed 0%, #fef3e8 100%); border-radius: 10px; border-left: 4px solid #fb923c; }
.q-opts { display: flex; flex-direction: column; gap: 12px; }
.q-opt { padding: 12px 16px; border: 1.5px solid var(--gray-200); border-radius: 10px; transition: all 0.2s ease; width: 100%; }
.q-opt:hover { border-color: #fb923c; background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 100%); }
:deep(.q-opt.is-checked) { border-color: #fb923c; background: linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%); }
:deep(.q-opt .el-radio__label), :deep(.q-opt .el-checkbox__label) { width: 100%; }
.q-foot { display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px; padding-top: 16px; border-top: 1px dashed var(--gray-200); }

.result-wrap { padding: 8px 0; }
.result-score { padding: 32px; border-radius: var(--radius-lg); color: #fff; margin-bottom: 16px; text-align: center; }
.rs-num { font-size: 48px; font-weight: 700; line-height: 1; }
.rs-total { font-size: 22px; opacity: 0.85; }
.rs-label { font-size: 13px; opacity: 0.9; margin-top: 8px; }
</style>
