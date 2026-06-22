<template>
  <div class="exam-page" v-if="started">
    <div class="card">
      <div class="exam-header">
        <h2>{{ data.paper.title }}</h2>
        <div class="timer">
          剩余时间：<span :class="{ danger: timeLeft < 60 }">{{ formatTime(timeLeft) }}</span>
        </div>
      </div>
      <el-divider />
      <ol>
        <li v-for="(q, i) in data.questions" :key="q.questionId" class="q-item">
          <div class="q-title">
            <span class="q-type">{{ typeLabel(q.question.type) }}</span>
            <span class="q-score">{{ q.score }} 分</span>
            <span class="q-content">{{ q.question.content }}</span>
          </div>
          <div v-if="q.question.options" class="q-options">
            <div v-for="(opt, idx) in parseOptions(q.question.options)" :key="idx" class="option">
              <el-radio v-if="q.question.type === 'SINGLE' || q.question.type === 'JUDGE'"
                v-model="answers[q.questionId]" :value="String.fromCharCode(65 + idx)"
                @change="saveOne(q.questionId)">
                {{ opt }}
              </el-radio>
              <el-checkbox v-else-if="q.question.type === 'MULTIPLE'"
                v-model="multiAnswers[q.questionId]" :value="String.fromCharCode(65 + idx)"
                @change="saveOne(q.questionId)">
                {{ opt }}
              </el-checkbox>
            </div>
          </div>
          <div v-else-if="q.question.type === 'ESSAY'">
            <el-input v-model="answers[q.questionId]" type="textarea" :rows="3" placeholder="请输入你的答案" @blur="saveOne(q.questionId)" />
          </div>
        </li>
      </ol>
      <div class="footer">
        <el-button type="primary" size="large" :loading="submitting" @click="submit">提交试卷</el-button>
      </div>
    </div>
  </div>
  <div class="page" v-else>
    <el-card><el-icon class="is-loading"><Loading /></el-icon> 加载中…</el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examApi } from '@/api'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)

const started = ref(false)
const data = ref({ paper: {}, questions: [], record: {} })
const answers = reactive({})         // 单选/判断/简答
const multiAnswers = reactive({})     // 多选
const submitting = ref(false)
const timeLeft = ref(0)
let timer = null

const TYPE = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', ESSAY: '简答' }
const typeLabel = (t) => TYPE[t] || t

function formatTime(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function parseOptions(json) {
  try { return typeof json === 'string' ? JSON.parse(json) : json } catch (e) { return [] }
}

async function start() {
  const res = await examApi.start(examId)
  data.value = res
  // 回显已保存的答案
  for (const q of res.questions) {
    const saved = q.question.answer
    if (saved) {
      if (q.question.type === 'MULTIPLE') {
        multiAnswers[q.questionId] = saved.split('')
      } else {
        answers[q.questionId] = saved
      }
    }
  }
  timeLeft.value = res.paper.duration * 60
  started.value = true
  startTimer()
}

function startTimer() {
  timer = setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) {
      clearInterval(timer)
      ElMessage.warning('时间到，自动提交')
      submit()
    }
  }, 1000)
}

async function saveOne(qid) {
  const ans = getAnswer(qid)
  await examApi.saveAnswer({ recordId: data.value.record.id, questionId: qid, userAnswer: ans })
}

function getAnswer(qid) {
  const q = data.value.questions.find(x => x.questionId === qid)
  if (!q) return ''
  if (q.question.type === 'MULTIPLE') {
    return (multiAnswers[qid] || []).sort().join('')
  }
  return answers[qid] || ''
}

async function submit() {
  try {
    await ElMessageBox.confirm('确定提交试卷？提交后不可修改。', '提示', { type: 'warning' })
  } catch (e) { return }
  submitting.value = true
  try {
    const list = data.value.questions.map(q => ({
      questionId: q.questionId,
      userAnswer: getAnswer(q.questionId),
    }))
    const result = await examApi.submit({ recordId: data.value.record.id, answers: list })
    ElMessage.success(`得分 ${result.score} / ${result.totalScore}`)
    if (timer) clearInterval(timer)
    router.push('/history')
  } finally { submitting.value = false }
}

onMounted(start)
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.exam-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
.exam-header { display: flex; justify-content: space-between; align-items: center; }
.exam-header h2 { margin: 0; }
.timer { font-size: 16px; }
.danger { color: #f56c6c; font-weight: 600; }
.q-item { padding: 16px 0; border-bottom: 1px dashed #eee; }
.q-title { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.q-type { background: #409eff; color: #fff; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.q-score { color: #999; font-size: 12px; }
.q-content { flex: 1; }
.q-options { padding-left: 24px; margin-top: 8px; }
.option { padding: 4px 0; }
.footer { text-align: center; margin-top: 24px; }
</style>
