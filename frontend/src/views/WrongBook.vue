<template>
  <div class="page-container fade-in">
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-radio-group v-model="filter.mastered" @change="load">
          <el-radio-button :label="null">全部 ({{ counts.all }})</el-radio-button>
          <el-radio-button :label="false">待复习 ({{ counts.unmastered }})</el-radio-button>
          <el-radio-button :label="true">已掌握 ({{ counts.mastered }})</el-radio-button>
        </el-radio-group>
        <div class="filter-spacer"></div>
        <el-input v-model="filter.keyword" placeholder="搜索错题" style="width:220px" :prefix-icon="Search" clearable @keyup.enter="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      </div>
    </el-card>

    <!-- 批量操作条（选中后显示） -->
    <transition name="slide-down">
      <div v-if="selected.length > 0" class="batch-bar">
        <span class="batch-info">已选 <b>{{ selected.length }}</b> 题</span>
        <div class="batch-spacer"></div>
        <el-button :icon="Correct" type="success" plain @click="batchMark(true)">标记掌握</el-button>
        <el-button :icon="Close" type="warning" plain @click="batchMark(false)">取消掌握</el-button>
        <el-button :icon="Review" type="primary" @click="startReview(selected.map(s => s.id))">开始复习</el-button>
        <el-button :icon="Refresh" @click="selected = []">取消选择</el-button>
      </div>
    </transition>

    <div v-loading="loading" class="wrong-list">
      <el-empty v-if="rows.length === 0 && !loading" description="还没有错题记录，去考试中心答几道题试试看" />
      <div v-for="row in rows" :key="row.id" class="wrong-card" :class="{ selected: selectedIds.includes(row.id) }">
        <div class="wrong-head">
          <div class="wrong-head-left">
            <el-checkbox :model-value="selectedIds.includes(row.id)" @change="(v) => toggleSelect(row, v)" size="large" />
            <el-tag :type="typeTagType(row.type || 'SINGLE')" effect="dark" size="small">{{ typeLabel(row.type || 'SINGLE') }}</el-tag>
            <span class="wrong-subject">{{ row.subjectName || '未分类' }}</span>
            <span class="wrong-count">错 {{ row.wrongCount || 0 }} 次</span>
          </div>
          <el-dropdown trigger="click" @command="(c) => onRowCmd(c, row)">
            <el-button text :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="analyze"><Icon name="view" :size="14" />&nbsp;查看解析</el-dropdown-item>
                <el-dropdown-item command="master"><Icon :name="row.mastered ? 'close' : 'correct'" :size="14" />&nbsp;{{ row.mastered ? '取消掌握' : '标记掌握' }}</el-dropdown-item>
                <el-dropdown-item command="review" divided><Icon name="review" :size="14" />&nbsp;立即复习</el-dropdown-item>
                <el-dropdown-item command="ai"><Icon name="ai" :size="14" color="#0ea5e9" />&nbsp;AI 推荐同类题</el-dropdown-item>
                <el-dropdown-item command="remove" divided><span style="color:#ef4444"><Icon name="delete" :size="14" color="#ef4444" />&nbsp;从错题本移除</span></el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="wrong-content">{{ row.content }}</div>
        <div v-if="row.options && row.options.length" class="wrong-options">
          <div v-for="(opt, i) in row.options" :key="i" class="opt" :class="{ correct: row.correctAnswer?.includes(String.fromCharCode(65 + i)) }">
            <span class="opt-label">{{ String.fromCharCode(65 + i) }}</span>{{ opt }}
          </div>
        </div>
        <div class="wrong-foot">
          <div class="wrong-ans">
            <Icon name="correct" :size="14" color="#10b981" /> 参考答案：<b>{{ row.correctAnswer || '略' }}</b>
          </div>
          <el-button size="small" :icon="Review" @click="startReview([row.id])">开始复习</el-button>
        </div>
      </div>

      <el-pagination v-if="total > 0" v-model:current-page="filter.current" v-model:page-size="filter.size" :page-sizes="[5, 10, 20]" :total="total" layout="total, sizes, prev, pager, next" style="margin-top:20px; justify-content: center" @current-change="load" @size-change="load" />
    </div>

    <!-- AI 解析弹窗（独立 dialog，从错题列表"解析"操作直接弹出） -->
    <el-dialog v-model="explainDialog.visible" title="🤖 AI 题目解析" width="700px" top="6vh" :teleported="true">
      <div v-if="explainDialog.loading" class="ai-loading">
        <Icon name="loading" :size="32" color="#0ea5e9" />
        <p>通义千问正在解析，约需 3-8 秒...</p>
      </div>
      <div v-else-if="explainDialog.error">
        <el-alert :title="'AI 调用失败：' + explainDialog.error" type="error" :closable="false" />
      </div>
      <div v-else class="ai-content" v-html="formatAI(explainDialog.content)" />
      <template #footer>
        <el-button @click="explainDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- AI 推荐弹窗 -->
    <el-dialog v-model="aiDialog.visible" title="AI 推荐同类变式题" width="820px" top="6vh" :teleported="true">
      <div v-if="aiDialog.loading" class="ai-loading">
        <Icon name="loading" :size="32" color="#0ea5e9" />
        <p>通义千问正在生成，约需 3-8 秒...</p>
      </div>
      <div v-else-if="aiDialog.error" class="ai-error">
        <el-alert :title="'AI 调用失败：' + aiDialog.error" type="error" :closable="false" />
      </div>
      <div v-else class="ai-content">
        <div class="ai-tip">
          <el-icon><MagicStick /></el-icon>
          基于你的错题《{{ aiDialog.sourceRow?.content?.substring(0, 40) }}...》推荐了 {{ aiDialog.list.length }} 道同类题：
        </div>
        <div v-for="(q, i) in aiDialog.list" :key="q.id" class="ai-q-card">
          <div class="ai-q-head">
            <el-tag size="small" effect="dark" :type="typeTagType(q.type)">{{ typeLabel(q.type) }}</el-tag>
            <span class="ai-q-idx">第 {{ i + 1 }} 题</span>
            <span class="ai-q-kp">知识点：{{ q.knowledgePoint || '通用' }}</span>
            <el-rate v-model="q.difficulty" disabled :show-text="false" :max="5" size="small" style="margin-left:auto" />
          </div>
          <div class="ai-q-body">{{ q.content }}</div>
          <div v-if="q.options" class="ai-q-opts">
            <div v-for="(opt, k) in safeParseArr(q.options)" :key="k" class="ai-q-opt">
              <b>{{ String.fromCharCode(65 + k) }}.</b> {{ opt }}
            </div>
          </div>
          <div class="ai-q-foot">
            <span class="ai-q-answer">参考答案：<b>{{ q.answer }}</b></span>
          </div>
          <div v-if="q.explanation" class="ai-q-expl">解析：{{ q.explanation }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiDialog.visible = false">关闭</el-button>
        <el-button v-if="aiDialog.sourceRow" type="primary" :icon="Refresh" @click="aiRecommend(aiDialog.sourceRow)">换一批</el-button>
      </template>
    </el-dialog>

    <!-- 全屏复习模式 -->
    <el-dialog v-model="reviewDialog.visible" :show-close="false" fullscreen :align-center="true" class="review-dialog" :teleported="true">
      <div class="review-mode" v-if="reviewDialog.visible">
        <div class="review-top">
          <div class="review-progress">
            <div class="rp-info">第 <b>{{ reviewDialog.current + 1 }}</b> / {{ reviewDialog.questions.length }} 题</div>
            <div class="rp-bar"><div class="rp-fill" :style="{ width: ((reviewDialog.current + 1) / reviewDialog.questions.length * 100) + '%' }"></div></div>
          </div>
          <el-button :icon="MagicStick" type="primary" plain :loading="explainDialog.loading && explainDialog.qId === currentQ.id" @click="aiExplainToDialog(currentQ)">AI 解析</el-button>
          <el-button :icon="Close" plain @click="exitReview">退出复习</el-button>
        </div>

        <div class="review-body" v-if="currentQ">
          <div class="review-meta">
            <el-tag :type="typeTagType(currentQ.type || 'SINGLE')" effect="dark" size="small">{{ typeLabel(currentQ.type || 'SINGLE') }}</el-tag>
            <span class="review-subject">{{ currentQ.subjectName }}</span>
            <span class="review-tag">错题复习</span>
          </div>
          <h2 class="review-content">{{ currentQ.content }}</h2>

          <div v-if="currentQ.options && currentQ.options.length" class="review-options">
            <div v-for="(opt, i) in currentQ.options" :key="i" class="review-opt" :class="{
              selected: reviewDialog.answers[currentQ.id]?.includes(String.fromCharCode(65 + i)),
              correct: reviewDialog.showAnswer && currentQ.correctAnswer?.includes(String.fromCharCode(65 + i)),
              wrong: reviewDialog.showAnswer && reviewDialog.answers[currentQ.id]?.includes(String.fromCharCode(65 + i)) && !currentQ.correctAnswer?.includes(String.fromCharCode(65 + i)),
            }" @click="!reviewDialog.showAnswer && pickAnswer(currentQ, i)">
              <span class="opt-letter">{{ String.fromCharCode(65 + i) }}</span>
              <span class="opt-text">{{ opt }}</span>
              <Icon v-if="reviewDialog.showAnswer && currentQ.correctAnswer?.includes(String.fromCharCode(65 + i))" name="correct" :size="18" color="#10b981" class="opt-mark" />
            </div>
          </div>

          <el-alert v-if="reviewDialog.showAnswer && currentQ.analysis" type="info" :closable="false" style="margin-top:24px" show-icon>
            <template #title>
              <span><b>解析：</b>{{ currentQ.analysis }}</span>
            </template>
          </el-alert>
        </div>

        <div class="review-foot">
          <el-button :disabled="reviewDialog.current === 0" :icon="ArrowLeft" @click="prevQuestion">上一题</el-button>
          <div class="rf-spacer"></div>
          <el-button v-if="!reviewDialog.showAnswer" type="primary" size="large" :icon="Check" @click="submitAnswer">提交答案</el-button>
          <el-button v-else type="primary" size="large" @click="nextQuestion">
            {{ reviewDialog.current === reviewDialog.questions.length - 1 ? '查看成绩' : '下一题' }}
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 复习成绩单 -->
    <el-dialog v-model="resultDialog.visible" title="🎉 复习完成" width="500px" :show-close="false" :align-center="true" :teleported="true">
      <div class="result-wrap" v-if="resultDialog.result">
        <div class="result-score" :style="{ background: resultDialog.result.accuracy >= 0.8 ? 'linear-gradient(135deg,#10b981,#34d399)' : resultDialog.result.accuracy >= 0.6 ? 'linear-gradient(135deg,#0ea5e9,#06b6d4)' : 'linear-gradient(135deg,#f59e0b,#fbbf24)' }">
          <div class="rs-num">{{ (resultDialog.result.accuracy * 100).toFixed(0) }}<span>%</span></div>
          <div class="rs-label">本次正确率</div>
        </div>
        <div class="result-stats">
          <div class="rs-item"><div class="rsi-num">{{ resultDialog.result.correct }}</div><div class="rsi-label">答对</div></div>
          <div class="rs-item"><div class="rsi-num">{{ resultDialog.result.wrong }}</div><div class="rsi-label">答错</div></div>
          <div class="rs-item"><div class="rsi-num">{{ resultDialog.result.total }}</div><div class="rsi-label">总题数</div></div>
        </div>
        <el-alert v-if="resultDialog.result.accuracy >= 0.8" type="success" :closable="false" show-icon>
          <template #title><b>太棒了！</b>继续保持，错题正在被你征服</template>
        </el-alert>
        <el-alert v-else type="warning" :closable="false" show-icon style="margin-top:12px">
          <template #title>还需努力，建议加入重练计划</template>
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="resultDialog.visible = false">关闭</el-button>
        <el-button type="primary" :icon="Refresh" @click="restartReview">再练一次</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, MoreFilled, Check, Close, ArrowLeft, ArrowRight, MagicStick } from '@element-plus/icons-vue'
import Icon from '@/components/Icon.vue'
import { wrongApi, aiApi } from '@/api'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const counts = ref({ all: 0, mastered: 0, unmastered: 0 })
const filter = reactive({ keyword: '', mastered: null, current: 1, size: 10 })
const selected = ref([])
const selectedIds = computed(() => selected.value.map(s => s.id))

const aiDialog = reactive({ visible: false, loading: false, content: '', sourceRow: null, list: [], error: '' })
const reviewDialog = reactive({ visible: false, current: 0, questions: [], answers: {}, showAnswer: false })
const resultDialog = reactive({ visible: false, result: null })
const explainDialog = reactive({ visible: false, loading: false, content: '', error: '', qId: null })

const currentQ = computed(() => reviewDialog.questions[reviewDialog.current])

function typeLabel(t) { return { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', FILL: '填空', ESSAY: '简答', QA: '问答', CALC: '计算' }[t] || t }
function typeTagType(t) { return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning', FILL: 'info', ESSAY: 'danger', QA: 'danger', CALC: 'success' }[t] || '' }

async function load() {
  loading.value = true
  try {
    const params = { current: filter.current, size: filter.size }
    if (filter.keyword) params.keyword = filter.keyword
    if (filter.mastered !== null) params.mastered = filter.mastered
    const r = await wrongApi.page(params)
    rows.value = r.data?.records || r.data || []
    total.value = r.data?.total || rows.value.length
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function refreshCounts() {
  try {
    const [all, mastered] = await Promise.allSettled([
      wrongApi.page({ current: 1, size: 1 }),
      wrongApi.page({ current: 1, size: 1, mastered: true }),
    ])
    counts.value.all = all.status === 'fulfilled' ? (all.value.data?.total || all.value.data?.records?.length || 0) : 0
    counts.value.mastered = mastered.status === 'fulfilled' ? (mastered.value.data?.total || mastered.value.data?.records?.length || 0) : 0
    counts.value.unmastered = counts.value.all - counts.value.mastered
  } catch (e) { /* */ }
}

function toggleSelect(row, checked) {
  if (checked) { if (!selected.value.find(s => s.id === row.id)) selected.value.push(row) }
  else { selected.value = selected.value.filter(s => s.id !== row.id) }
}

async function batchMark(mastered) {
  try { await Promise.all(selected.value.map(s => wrongApi.toggleMastered(s.id, mastered))); ElMessage.success(`已${mastered ? '标记掌握' : '取消掌握'} ${selected.value.length} 题`); selected.value = []; load(); refreshCounts() }
  catch (e) { ElMessage.error('操作失败') }
}

async function onRowCmd(cmd, row) {
  if (cmd === 'analyze') aiExplainToDialog({ id: row.questionId, content: row.content, type: row.type })
  else if (cmd === 'master') { try { await wrongApi.toggleMastered(row.id, !row.mastered); row.mastered = !row.mastered; ElMessage.success('已更新'); refreshCounts() } catch (e) { ElMessage.error('操作失败') } }
  else if (cmd === 'review') startReview([row.id])
  else if (cmd === 'ai') aiRecommend(row)
  else if (cmd === 'remove') { try { await wrongApi.toggleMastered(row.id, true); row.mastered = true; ElMessage.success('已移除'); refreshCounts() } catch (e) { /* */ } }
}

async function aiRecommend(row) {
  aiDialog.sourceRow = row
  aiDialog.visible = true
  aiDialog.loading = true
  aiDialog.error = ''
  aiDialog.list = []
  try {
    // 关键：http.js 拦截器已剥掉 resp.data.data 那一层，所以 r 已经是 Result 对象
    // r = { code, message, data: [...] }
    const r = await aiApi.recommend(row.id)
    const list = Array.isArray(r?.data) ? r.data : []
    aiDialog.list = list
    if (list.length === 0) {
      const msg = r?.message
      aiDialog.error = msg && msg !== '操作成功' ? `后端：${msg}` : '未推荐到同类题，可换个错题试试或稍后再来'
    }
  } catch (e) {
    aiDialog.error = e.response?.data?.message || e.message || '未知错误'
  } finally { aiDialog.loading = false }
}

function safeParseArr(s) {
  if (!s) return []
  if (Array.isArray(s)) return s
  try { const p = JSON.parse(s); return Array.isArray(p) ? p : [s] } catch { return [s] }
}

async function aiExplainToDialog(q) {
  if (!q || !q.id) return
  explainDialog.qId = q.id
  explainDialog.visible = true
  explainDialog.loading = true
  explainDialog.error = ''
  explainDialog.content = ''
  try {
    // 关键：http.js 拦截器返回的是 resp.data = Result 对象，
    // 所以 r 已经是 { code, message, data: "解析文本" }
    const r = await aiApi.explain(q.id)
    const text = r?.data
    if (typeof text === 'string' && text.length > 0) {
      explainDialog.content = text
      if (/^\[(错误|异常|\u6a21\u62df)/.test(text)) {
        explainDialog.error = 'AI 服务降级提示：' + text
      }
    } else {
      explainDialog.error = '解析内容为空（AI 服务可能未配置或网络异常）'
    }
  } catch (e) {
    explainDialog.error = e.response?.data?.message || e.message || '未知错误'
  } finally { explainDialog.loading = false }
}

function formatAI(text) {
  if (typeof text !== 'string') return `<pre>${JSON.stringify(text, null, 2)}</pre>`
  // HTML escape
  let html = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 代码块 ```...```
  html = html.replace(/```([\s\S]*?)```/g, (m, code) => `<pre class="ai-code">${code.trim()}</pre>`)
  // 行内代码 `xxx`
  html = html.replace(/`([^`]+?)`/g, '<code>$1</code>')
  // 加粗 **xxx**
  html = html.replace(/\*\*([^*]+?)\*\*/g, '<b>$1</b>')
  // 标题行：以 • 或 1. 2. 或 - 开头的行加 class
  html = html.replace(/^(\s*[•\-\d]+[\.、\)]\s*)(.+)$/gm, '<div class="ai-li"><span class="ai-marker">$1</span><span>$2</span></div>')
  // 段落分隔：双换行 → 段
  html = html.replace(/\n{2,}/g, '<div class="ai-gap"></div>')
  // 单换行 → 换行
  html = html.replace(/\n/g, '<br>')
  return html
}

function startReview(ids) {
  if (ids.length === 0) return ElMessage.warning('请先选择题目')
  reviewDialog.questions = rows.value.filter(r => ids.includes(r.id))
  reviewDialog.current = 0
  reviewDialog.answers = {}
  reviewDialog.showAnswer = false
  reviewDialog.visible = true
  selected.value = []
}

function pickAnswer(q, idx) {
  const letter = String.fromCharCode(65 + idx)
  if (q.type === 'MULTIPLE') {
    const cur = reviewDialog.answers[q.id] || ''
    reviewDialog.answers[q.id] = cur.includes(letter) ? cur.replace(letter, '') : cur + letter
  } else {
    reviewDialog.answers[q.id] = letter
  }
}

function submitAnswer() {
  if (!reviewDialog.answers[currentQ.value.id]) return ElMessage.warning('请先选择答案')
  reviewDialog.showAnswer = true
}

function nextQuestion() {
  if (reviewDialog.current === reviewDialog.questions.length - 1) {
    finishReview()
  } else {
    reviewDialog.current++
    reviewDialog.showAnswer = false
  }
}

function prevQuestion() { if (reviewDialog.current > 0) { reviewDialog.current--; reviewDialog.showAnswer = false } }

function finishReview() {
  let correct = 0
  reviewDialog.questions.forEach(q => {
    if (reviewDialog.answers[q.id] === q.correctAnswer) correct++
  })
  resultDialog.result = { total: reviewDialog.questions.length, correct, wrong: reviewDialog.questions.length - correct, accuracy: correct / reviewDialog.questions.length }
  reviewDialog.visible = false
  resultDialog.visible = true
}

function restartReview() { resultDialog.visible = false; startReview(reviewDialog.questions.map(q => q.id)) }
function exitReview() {
  if (Object.keys(reviewDialog.answers).length > 0) {
    ElMessageBox.confirm('当前复习未完成，确定退出？', '提示', { type: 'warning' }).then(() => { reviewDialog.visible = false }).catch(() => {})
  } else { reviewDialog.visible = false }
}

onMounted(() => { load(); refreshCounts() })
</script>

<style scoped>
.filter-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.filter-card :deep(.el-card__header) {
  background: linear-gradient(90deg, #fff7ed 0%, #fef3e8 50%, #fff1f2 100%);
}
.filter-row { display: flex; gap: 12px; align-items: center; }
.filter-spacer { flex: 1; }

.batch-bar {
  display: flex; align-items: center; gap: 8px;
  background: linear-gradient(90deg, #fef3c7 0%, #fff7ed 50%, #ffe4e6 100%);
  border: 1px solid #fbbf24;
  border-radius: var(--radius-md);
  padding: 12px 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.18);
}
.batch-info {
  background: linear-gradient(135deg, #ea580c 0%, #f97316 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 13px; font-weight: 600;
}
.batch-info b { color: #c2410c; font-size: 15px; -webkit-text-fill-color: #c2410c; }
.batch-spacer { flex: 1; }

.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-10px); }

.wrong-list { display: flex; flex-direction: column; }
.wrong-card {
  position: relative;
  background: #fff;
  border: 1px solid #fde68a;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
}
.wrong-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #fbbf24, #fb923c, #fb7185);
  border-radius: 0 4px 4px 0;
}
.wrong-card::after {
  content: '';
  position: absolute;
  top: -40px; right: -40px;
  width: 120px; height: 120px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.10) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(15px);
  pointer-events: none;
}
.wrong-card:hover {
  box-shadow: 0 8px 20px rgba(251, 146, 60, 0.18);
  transform: translateY(-2px);
  border-color: #fb923c;
}
.wrong-card.selected {
  border-color: #fb7185;
  background: linear-gradient(135deg, #fff1f2 0%, #ffffff 100%);
  box-shadow: 0 8px 20px rgba(251, 113, 133, 0.20);
}

.wrong-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.wrong-head-left { display: flex; align-items: center; gap: 12px; }
.wrong-subject { font-size: 13px; color: #c2410c; font-weight: 500; }
.wrong-count {
  font-size: 12px; font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #fb7185 0%, #f43f5e 100%);
  padding: 2px 10px;
  border-radius: 999px;
  box-shadow: 0 2px 6px rgba(251, 113, 133, 0.30);
}

.wrong-content { font-size: 15px; line-height: 1.7; color: #292524; margin-bottom: 16px; font-weight: 500; }
.wrong-options { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 16px; }
.opt {
  padding: 8px 12px;
  background: linear-gradient(135deg, #fef3c7 0%, #fff7ed 100%);
  border-radius: 8px;
  font-size: 13px;
  display: flex; align-items: center; gap: 8px;
  border: 1px solid #fed7aa;
}
.opt.correct {
  background: linear-gradient(135deg, #ccfbf1 0%, #99f6e4 100%);
  color: #0f766e;
  font-weight: 600;
  border-color: #5eead4;
  box-shadow: 0 2px 6px rgba(20, 184, 166, 0.18);
}
.opt-label {
  display: inline-flex; align-items: center; justify-content: center;
  width: 22px; height: 22px;
  border-radius: 4px;
  background: rgba(0,0,0,0.06);
  color: #64748b;
  font-size: 11px; font-weight: 600;
  flex-shrink: 0;
}
.opt.correct .opt-label { background: linear-gradient(135deg, #14b8a6, #0d9488); color: #fff; }

.wrong-foot {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #fed7aa;
}
.wrong-ans {
  font-size: 13px;
  color: #0d9488;
  display: flex; align-items: center; gap: 6px;
  font-weight: 500;
}
.wrong-ans b {
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.ai-loading { text-align: center; padding: 60px 0; color: #64748b; }
.ai-content { line-height: 1.8; font-size: 14px; color: #334155; max-height: 60vh; overflow-y: auto; padding-right: 4px; }
.ai-content :deep(code) { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; color: #0ea5e9; }
.ai-content :deep(.ai-code) { background: #1c1917; color: #fafaf9; padding: 12px 14px; border-radius: 8px; overflow-x: auto; font-family: 'Cascadia Code', Consolas, monospace; font-size: 13px; line-height: 1.6; margin: 10px 0; white-space: pre; }
.ai-content :deep(.ai-gap) { height: 12px; }
.ai-content :deep(.ai-li) { display: flex; gap: 6px; padding: 4px 0; }
.ai-content :deep(.ai-marker) { color: #ea580c; font-weight: 600; min-width: 24px; flex-shrink: 0; }
.ai-content :deep(b) { color: #ea580c; font-weight: 600; }
.ai-error { color: #dc2626; padding: 16px; background: #fef2f2; border-radius: 8px; }

.ai-tip {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px; margin-bottom: 14px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffe4e6 100%);
  border-radius: 10px; font-size: 13px; color: #9a3412;
}
.ai-q-card {
  background: #fff; border: 1px solid #fed7aa;
  border-radius: 12px; padding: 14px 16px; margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(251, 146, 60, 0.06);
  transition: box-shadow 0.2s;
}
.ai-q-card:hover { box-shadow: 0 4px 16px rgba(251, 146, 60, 0.12); }
.ai-q-head { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.ai-q-idx { font-weight: 600; color: #ea580c; }
.ai-q-kp { font-size: 12px; color: #78716c; }
.ai-q-body { font-size: 15px; color: #1c1917; font-weight: 500; margin-bottom: 10px; line-height: 1.7; }
.ai-q-opts { background: #fff7ed; border-radius: 8px; padding: 8px 12px; margin-bottom: 10px; }
.ai-q-opt { font-size: 13px; color: #44403c; padding: 3px 0; }
.ai-q-foot {
  display: flex; align-items: center; gap: 12px;
  padding-top: 8px; border-top: 1px dashed #fed7aa;
}
.ai-q-answer { font-size: 13px; color: #14b8a6; }
.ai-q-answer b { background: #ccfbf1; padding: 2px 8px; border-radius: 6px; color: #0d9488; }
.ai-q-expl { font-size: 12px; color: #64748b; margin-top: 8px; padding: 6px 10px; background: #f0f9ff; border-radius: 6px; }

/* 复习模式 —— 春日校园氛围 */
:deep(.review-dialog) {
  background:
    linear-gradient(135deg,
      #fff7ed 0%, #fef3c7 18%, #ffe4e6 36%,
      #ede9fe 52%, #e0f2fe 68%, #ccfbf1 84%, #ecfccb 100%);
  background-size: 200% 200%;
  animation: gradient-shift 20s ease infinite;
}
:deep(.review-dialog .el-dialog__header) { display: none; }
:deep(.review-dialog .el-dialog__body) { padding: 0; }

@keyframes gradient-shift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.review-mode { min-height: 100vh; display: flex; flex-direction: column; position: relative; }
.review-top {
  display: flex; align-items: center;
  padding: 16px 32px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(251, 146, 60, 0.15);
  box-shadow: 0 2px 12px rgba(251, 146, 60, 0.06);
}
.review-progress { flex: 1; max-width: 600px; margin: 0 auto; }
.rp-info { text-align: center; font-size: 13px; color: #78716c; margin-bottom: 8px; }
.rp-info b {
  background: linear-gradient(135deg, #ea580c 0%, #f97316 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 16px;
}
.rp-bar { height: 8px; background: #fef3c7; border-radius: 4px; overflow: hidden; }
.rp-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24 0%, #fb923c 50%, #fb7185 100%);
  border-radius: 4px;
  transition: width 0.4s ease;
  box-shadow: 0 2px 6px rgba(251, 146, 60, 0.35);
}

.review-body { flex: 1; max-width: 800px; margin: 0 auto; padding: 48px 32px; width: 100%; }
.review-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.review-subject {
  font-size: 14px;
  background: linear-gradient(135deg, #f97316 0%, #fb7185 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 600;
}
.review-tag {
  font-size: 12px;
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  color: #c2410c;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(251, 146, 60, 0.20);
}
.review-content {
  font-size: 22px; line-height: 1.6;
  color: #1c1917;
  margin: 0 0 32px; font-weight: 600;
  padding: 24px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-md);
  border-left: 4px solid;
  border-image: linear-gradient(180deg, #fbbf24, #fb7185, #a78bfa) 1;
  box-shadow: 0 4px 16px rgba(251, 146, 60, 0.10);
}

.review-options { display: flex; flex-direction: column; gap: 12px; }
.review-opt {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border: 2px solid #fed7aa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.review-opt:hover {
  border-color: #fb923c;
  background: rgba(255, 247, 237, 0.95);
  transform: translateX(4px);
  box-shadow: 0 6px 18px rgba(251, 146, 60, 0.20);
}
.review-opt.selected {
  border-color: #fb923c;
  background: linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%);
  box-shadow: 0 6px 18px rgba(251, 146, 60, 0.25);
}
.review-opt.correct {
  border-color: #14b8a6;
  background: linear-gradient(135deg, #ccfbf1 0%, #99f6e4 100%);
  box-shadow: 0 6px 18px rgba(20, 184, 166, 0.25);
}
.review-opt.wrong {
  border-color: #fb7185;
  background: linear-gradient(135deg, #ffe4e6 0%, #fecdd3 100%);
  box-shadow: 0 6px 18px rgba(251, 113, 133, 0.25);
}
.opt-letter {
  width: 34px; height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  color: #c2410c;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}
.review-opt.selected .opt-letter { background: linear-gradient(135deg, #fb923c 0%, #f97316 100%); color: #fff; }
.review-opt.correct .opt-letter { background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%); color: #fff; }
.review-opt.wrong .opt-letter { background: linear-gradient(135deg, #fb7185 0%, #f43f5e 100%); color: #fff; }
.opt-text { flex: 1; font-size: 15px; color: #292524; font-weight: 500; }
.opt-mark { margin-left: auto; }

.review-foot {
  display: flex; align-items: center;
  padding: 20px 32px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(251, 146, 60, 0.15);
  box-shadow: 0 -2px 12px rgba(251, 146, 60, 0.06);
}
.rf-spacer { flex: 1; }

/* 成绩单 */
.result-wrap { text-align: center; padding: 12px 0; }
.result-score {
  padding: 32px;
  border-radius: var(--radius-lg);
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 12px 36px rgba(251, 113, 133, 0.30);
  position: relative;
  overflow: hidden;
}
.result-score::before {
  content: '';
  position: absolute;
  top: -50%; right: -20%;
  width: 200px; height: 200px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.25) 0%, transparent 70%);
  border-radius: 50%;
}
.rs-num { font-size: 56px; font-weight: 700; line-height: 1; position: relative; }
.rs-num span { font-size: 24px; opacity: 0.8; }
.rs-label { font-size: 14px; opacity: 0.9; margin-top: 8px; position: relative; }
.result-stats { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; margin-bottom: 16px; }
.rs-item {
  padding: 16px;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 100%);
  border-radius: var(--radius-md);
  border: 1px solid rgba(251, 146, 60, 0.15);
}
.rs-item:nth-child(1) .rsi-num { background: linear-gradient(135deg, #14b8a6 0%, #5eead4 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.rs-item:nth-child(2) .rsi-num { background: linear-gradient(135deg, #fb7185 0%, #f43f5e 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.rs-item:nth-child(3) .rsi-num { background: linear-gradient(135deg, #f97316 0%, #fbbf24 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.rsi-num { font-size: 24px; font-weight: 700; color: #1c1917; }
.rsi-label { font-size: 12px; color: #78716c; margin-top: 4px; }
</style>
