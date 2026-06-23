<template>
  <div class="page-container fade-in">
    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span class="section-title" style="margin:0">试卷中心</span>
          <div>
            <el-button :icon="Refresh" @click="load">刷新</el-button>
            <el-button :icon="MagicStick" type="warning" @click="openAuto">AI 智能组卷</el-button>
            <el-button :icon="Plus" type="primary" @click="openCreate">新建空白试卷</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="paper-grid">
        <el-empty v-if="!loading && rows.length === 0" description="还没有试卷，点击『AI 智能组卷』开始" />
        <div v-for="(p, idx) in rows" :key="p.id" class="paper-card" :class="'theme-' + (idx % 4)">
          <div class="paper-head">
            <div class="paper-icon" :class="'pi-' + (idx % 4)"><Icon name="papers" :size="22" color="#fff" /></div>
            <div class="paper-info">
              <h3 class="paper-name">{{ p.title }}</h3>
              <p class="paper-meta">{{ p.subjectName || '通用' }} · {{ p.questionCount || 0 }} 题</p>
            </div>
          </div>
          <div class="paper-stats">
            <div class="ps-item"><div class="ps-num" :class="'num-' + (idx % 4)">{{ p.totalScore || 0 }}</div><div class="ps-label">总分</div></div>
            <div class="ps-item"><div class="ps-num" :class="'num-' + (idx % 4)">{{ p.questionCount || 0 }}</div><div class="ps-label">题数</div></div>
            <div class="ps-item"><div class="ps-num" :class="'num-' + (idx % 4)">{{ p.duration || 0 }}<span class="ps-unit">分</span></div><div class="ps-label">时长</div></div>
          </div>
          <div class="paper-foot">
            <el-button size="small" :icon="View" @click="previewPaper(p)">预览</el-button>
            <el-dropdown trigger="click" @command="(c) => onCmd(c, p)">
              <el-button size="small" :icon="MoreFilled" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="use"><Icon name="send" :size="14" />&nbsp;用于考试</el-dropdown-item>
                  <el-dropdown-item command="copy"><Icon name="doc" :size="14" />&nbsp;复制</el-dropdown-item>
                  <el-dropdown-item command="edit"><Icon name="edit" :size="14" />&nbsp;编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" divided><span style="color:#ef4444"><Icon name="delete" :size="14" color="#ef4444" />&nbsp;删除</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <el-pagination
        v-model:current-page="filter.current"
        v-model:page-size="filter.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:20px; justify-content: flex-end"
        @current-change="load"
        @size-change="load"
      />
    </el-card>

    <!-- 新建空白试卷 -->
    <el-dialog v-model="createDialog.visible" title="新建空白试卷" width="640px" :teleported="true">
      <el-form :model="createDialog.form" label-width="90px">
        <el-form-item label="试卷名称"><el-input v-model="createDialog.form.title" placeholder="如：Java 基础 - 期中模拟" /></el-form-item>
        <el-form-item label="所属学科">
          <el-select v-model="createDialog.form.subjectId" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="createDialog.form.duration" :min="10" :max="300" :step="5" />
          <span style="margin-left:8px;color:#94a3b8">分钟</span>
        </el-form-item>
        <el-form-item label="试卷描述"><el-input v-model="createDialog.form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="onCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- AI 智能组卷 -->
    <el-dialog v-model="autoDialog.visible" title="AI 智能组卷" width="640px" :teleported="true">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        根据学科、题型、题量自动从题库抽取；同卷内不同题型自动去重，确保题目不重复。
      </el-alert>
      <el-form :model="autoDialog.form" label-width="100px">
        <el-form-item label="试卷名称"><el-input v-model="autoDialog.form.title" placeholder="如：Java 基础单元测试" /></el-form-item>
        <el-form-item label="所属学科">
          <el-select v-model="autoDialog.form.subjectId" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单选题">
          <el-input-number v-model="autoDialog.form.singleCount" :min="0" :max="50" />
          <span style="margin-left:8px;color:#94a3b8">题 × 2 分</span>
        </el-form-item>
        <el-form-item label="多选题">
          <el-input-number v-model="autoDialog.form.multipleCount" :min="0" :max="50" />
          <span style="margin-left:8px;color:#94a3b8">题 × 3 分</span>
        </el-form-item>
        <el-form-item label="判断题">
          <el-input-number v-model="autoDialog.form.judgeCount" :min="0" :max="50" />
          <span style="margin-left:8px;color:#94a3b8">题 × 1 分</span>
        </el-form-item>
        <el-form-item label="填空题">
          <el-input-number v-model="autoDialog.form.fillCount" :min="0" :max="50" />
          <span style="margin-left:8px;color:#94a3b8">题 × 4 分</span>
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="autoDialog.form.duration" :min="10" :max="300" :step="5" />
          <span style="margin-left:8px;color:#94a3b8">分钟</span>
        </el-form-item>
        <el-divider>预计总分：<b style="color:#f97316">{{ autoScore }}</b> 分</el-divider>
      </el-form>
      <template #footer>
        <el-button @click="autoDialog.visible = false">取消</el-button>
        <el-button type="primary" :icon="MagicStick" :loading="loading" @click="onAutoGen">开始组卷</el-button>
      </template>
    </el-dialog>

    <!-- 试卷预览 -->
    <el-dialog v-model="previewDialog.visible" :title="previewDialog.title" width="70%" top="5vh" class="paper-preview-dialog" :teleported="true">
      <div v-if="previewDialog.paper" class="preview-wrap">
        <el-descriptions :column="3" border style="margin-bottom:16px">
          <el-descriptions-item label="试卷名称">{{ previewDialog.paper.title }}</el-descriptions-item>
          <el-descriptions-item label="学科">{{ previewDialog.paper.subjectName }}</el-descriptions-item>
          <el-descriptions-item label="总分 / 时长">{{ previewDialog.paper.totalScore }} 分 / {{ previewDialog.paper.duration }} 分钟</el-descriptions-item>
        </el-descriptions>
        <div v-for="(q, i) in previewDialog.questions" :key="q.id" class="preview-q">
          <div class="pq-head">
            <span class="pq-no">第 {{ i + 1 }} 题</span>
            <el-tag size="small" :type="typeTagType(q.type)" effect="light">{{ typeLabel(q.type) }}</el-tag>
            <el-rate :model-value="q.difficulty" disabled :show-text="false" :max="5" size="small" />
            <span class="pq-score">{{ q.score || 0 }} 分</span>
          </div>
          <div class="pq-content">{{ q.content }}</div>
          <div v-if="q.options && q.options.length" class="pq-opts">
            <div v-for="(o, j) in q.options" :key="j" class="pq-opt">{{ String.fromCharCode(65 + j) }}. {{ o }}</div>
          </div>
          <div class="pq-ans" v-if="q.answer"><b>参考答案：</b>{{ q.answer }}</div>
          <div class="pq-exp" v-if="q.explanation"><b>解析：</b>{{ q.explanation }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, MagicStick, View, MoreFilled } from '@element-plus/icons-vue'
import Icon from '@/components/Icon.vue'
import { paperApi, subjectApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const subjects = ref([])
const filter = reactive({ subjectId: null, current: 1, size: 10 })
const createDialog = reactive({ visible: false, form: blank() })
const autoDialog = reactive({ visible: false, form: { title: '智能组卷', subjectId: null, singleCount: 5, multipleCount: 2, judgeCount: 2, fillCount: 1, duration: 60 } })
const previewDialog = reactive({ visible: false, title: '试卷预览', paper: null, questions: [] })

function blank() { return { title: '', subjectId: null, duration: 60, description: '' } }
function typeLabel(t) { return { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', FILL: '填空', ESSAY: '简答', QA: '问答', CALC: '计算' }[t] || t }
function typeTagType(t) { return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning', FILL: 'info', ESSAY: 'danger', QA: 'danger', CALC: 'success' }[t] || '' }

const autoScore = computed(() => {
  const f = autoDialog.form
  return (f.singleCount || 0) * 2 + (f.multipleCount || 0) * 3 + (f.judgeCount || 0) * 1 + (f.fillCount || 0) * 4
})

async function load() {
  loading.value = true
  try {
    const params = { current: filter.current, size: filter.size }
    if (filter.subjectId) params.subjectId = filter.subjectId
    const r = await paperApi.page(params)
    rows.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function openCreate() { createDialog.form = blank(); createDialog.visible = true }
function openAuto() { autoDialog.visible = true }

async function onCreate() {
  if (!createDialog.form.title || !createDialog.form.subjectId) return ElMessage.warning('请填写完整')
  loading.value = true
  try { await paperApi.create(createDialog.form); ElMessage.success('创建成功'); createDialog.visible = false; load() }
  catch (e) { ElMessage.error('创建失败：' + (e.response?.data?.message || e.message)) }
  finally { loading.value = false }
}

async function onAutoGen() {
  if (!autoDialog.form.subjectId) return ElMessage.warning('请选择学科')
  if (!autoDialog.form.title) return ElMessage.warning('请输入试卷名')
  const f = autoDialog.form
  const rules = []
  if (f.singleCount > 0) rules.push({ type: 'SINGLE', count: f.singleCount, score: 2, difficulty: null, knowledgePoint: null })
  if (f.multipleCount > 0) rules.push({ type: 'MULTIPLE', count: f.multipleCount, score: 3, difficulty: null, knowledgePoint: null })
  if (f.judgeCount > 0) rules.push({ type: 'JUDGE', count: f.judgeCount, score: 1, difficulty: null, knowledgePoint: null })
  if (f.fillCount > 0) rules.push({ type: 'FILL', count: f.fillCount, score: 4, difficulty: null, knowledgePoint: null })
  if (rules.length === 0) return ElMessage.warning('至少配置一种题型')
  loading.value = true
  try {
    const payload = { title: f.title, description: '智能组卷自动生成', subjectId: f.subjectId, duration: f.duration, rules }
    const r = await paperApi.autoGenerate(payload)
    ElMessage.success(`组卷完成，试卷 ID: ${r.data}`)
    autoDialog.visible = false
    load()
  } catch (e) { ElMessage.error('组卷失败：' + (e.response?.data?.message || e.message)) }
  finally { loading.value = false }
}

async function previewPaper(p) {
  try {
    const r = await paperApi.get(p.id)
    previewDialog.paper = r.data?.paper
    previewDialog.questions = r.data?.questions || []
    previewDialog.title = `预览：${p.title}`
    previewDialog.visible = true
  } catch (e) { ElMessage.error('加载失败') }
}

async function onCmd(cmd, p) {
  if (cmd === 'use') {
    ElMessage.info('请前往『考试中心』选择该试卷安排考试')
    router.push('/exams')
  } else if (cmd === 'copy') {
    try {
      await paperApi.create({ title: p.title + ' - 副本', subjectId: p.subjectId, duration: p.duration, description: p.description })
      ElMessage.success('已复制'); load()
    } catch (e) { ElMessage.error('复制失败') }
  } else if (cmd === 'edit') {
    ElMessage.info('试卷编辑请重新创建并手动配置题目')
  } else if (cmd === 'delete') {
    try { await ElMessageBox.confirm(`确定删除《${p.title}》？`, '提示', { type: 'warning' }) } catch (e) { return }
    try { await paperApi.delete(p.id); ElMessage.success('已删除'); load() } catch (e) { ElMessage.error('删除失败') }
  }
}

onMounted(async () => {
  try { const r = await subjectApi.list(); subjects.value = r.data || [] } catch (e) { /* */ }
  load()
})
</script>

<style scoped>
.table-header { display: flex; justify-content: space-between; align-items: center; }

.paper-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.paper-card {
  position: relative;
  background: #fff;
  border: 1px solid rgba(251, 146, 60, 0.18);
  border-radius: var(--radius-md);
  padding: 22px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.paper-card:hover { box-shadow: 0 12px 32px rgba(251, 113, 133, 0.22); transform: translateY(-4px); border-color: transparent; }
.paper-card.theme-0 { background: linear-gradient(135deg, #fff7ed 0%, #ffffff 50%, #fff1f2 100%); }
.paper-card.theme-1 { background: linear-gradient(135deg, #f0fdfa 0%, #ffffff 50%, #ecfccb 100%); }
.paper-card.theme-2 { background: linear-gradient(135deg, #faf5ff 0%, #ffffff 50%, #f0f9ff 100%); }
.paper-card.theme-3 { background: linear-gradient(135deg, #fff1f2 0%, #ffffff 50%, #fff7ed 100%); }

.paper-head { display: flex; gap: 12px; margin-bottom: 16px; }
.paper-icon { width: 50px; height: 50px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10); }
.paper-icon.pi-0 { background: linear-gradient(135deg, #fb923c 0%, #fb7185 100%); }
.paper-icon.pi-1 { background: linear-gradient(135deg, #5eead4 0%, #84cc16 100%); }
.paper-icon.pi-2 { background: linear-gradient(135deg, #c4b5fd 0%, #7dd3fc 100%); }
.paper-icon.pi-3 { background: linear-gradient(135deg, #fb7185 0%, #fbbf24 100%); }
.paper-name { margin: 0 0 4px; font-size: 16px; font-weight: 600; color: var(--gray-800); }
.paper-meta { margin: 0; font-size: 12px; color: var(--gray-500); }

.paper-stats { display: flex; gap: 12px; padding: 14px 0; border-top: 1px dashed var(--gray-200); border-bottom: 1px dashed var(--gray-200); }
.ps-item { flex: 1; text-align: center; }
.ps-num { font-size: 22px; font-weight: 700; line-height: 1.1; }
.ps-num.num-0 { background: linear-gradient(135deg, #ea580c 0%, #fb7185 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.ps-num.num-1 { background: linear-gradient(135deg, #0d9488 0%, #65a30d 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.ps-num.num-2 { background: linear-gradient(135deg, #7c3aed 0%, #0284c7 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.ps-num.num-3 { background: linear-gradient(135deg, #be123c 0%, #f59e0b 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
.ps-label { font-size: 11px; color: var(--gray-500); margin-top: 4px; }
.ps-unit { font-size: 12px; opacity: 0.6; margin-left: 2px; }
.paper-foot { display: flex; gap: 8px; margin-top: 14px; }

.preview-wrap { padding: 4px; }
.preview-q { padding: 14px 0; border-bottom: 1px dashed var(--gray-200); }
.preview-q:last-child { border-bottom: none; }
.pq-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.pq-no { font-weight: 600; color: #c2410c; }
.pq-score { margin-left: auto; color: #fb7185; font-weight: 600; }
.pq-content { font-size: 15px; line-height: 1.7; color: #1c1917; margin-bottom: 8px; }
.pq-opts { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; margin-bottom: 8px; }
.pq-opt { padding: 6px 12px; background: var(--bg-warm); border-radius: 6px; font-size: 13px; }
.pq-ans, .pq-exp { font-size: 13px; color: var(--gray-700); margin-top: 4px; }
</style>
