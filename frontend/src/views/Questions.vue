<template>
  <div class="page-container fade-in">
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="filter.keyword" placeholder="题干 / 知识点 / 标签" style="width:240px" clearable :prefix-icon="Search" @keyup.enter="load" />
        <el-cascader v-model="filter.subjectId" :options="subjectOptions" :props="{ checkStrictly: true, value: 'id', label: 'name', emitPath: false }" placeholder="学科" style="width:160px" clearable @change="onFilterChange" />
        <el-select v-model="filter.type" placeholder="题型" style="width:130px" clearable @change="onFilterChange">
          <el-option label="单选" value="SINGLE" />
          <el-option label="多选" value="MULTIPLE" />
          <el-option label="判断" value="JUDGE" />
          <el-option label="填空" value="FILL" />
          <el-option label="简答" value="ESSAY" />
          <el-option label="问答" value="QA" />
          <el-option label="计算" value="CALC" />
        </el-select>
        <el-select v-model="filter.difficulty" placeholder="难度" style="width:130px" clearable @change="onFilterChange">
          <el-option v-for="i in 5" :key="i" :label="'★'.repeat(i) + '☆'.repeat(5 - i)" :value="i" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
        <div class="filter-spacer"></div>
        <el-button :icon="Upload" @click="openImportDialog">导入</el-button>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog()">录入新题</el-button>
        <input ref="fileInputRef" type="file" accept=".xlsx,.xls" style="display:none" @change="onFileSelected" />
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-header">
          <div>
            <span class="section-title" style="margin:0">题目列表</span>
            <el-tag type="info" effect="plain" style="margin-left:8px">共 {{ total }} 题</el-tag>
          </div>
          <el-radio-group v-model="viewMode" size="default">
            <el-radio-button label="table"><Icon name="list" :size="16" /></el-radio-button>
            <el-radio-button label="card"><Icon name="grid" :size="16" /></el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table v-if="viewMode === 'table'" :data="rows" stripe v-loading="loading" :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: 600 }">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" effect="light">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subjectName" label="学科" width="110" />
        <el-table-column prop="content" label="题干" min-width="280" show-overflow-tooltip />
        <el-table-column label="难度" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.difficulty" disabled :show-text="false" :max="5" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70" align="center" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(c) => onRowCommand(c, row)">
              <el-button text :icon="MoreFilled" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view"><Icon name="view" :size="14" />&nbsp;预览</el-dropdown-item>
                  <el-dropdown-item command="edit"><Icon name="edit" :size="14" />&nbsp;编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" divided><Icon name="delete" :size="14" color="#ef4444" />&nbsp;<span style="color:#ef4444">删除</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div v-else v-loading="loading" class="card-grid">
        <div v-for="(q, idx) in rows" :key="q.id" class="q-card" :class="'theme-' + (idx % 4)">
          <div class="q-card-head">
            <span class="q-type-badge" :style="{ background: typeTagColor(q.type) }">{{ typeLabel(q.type) }}</span>
            <el-rate v-model="q.difficulty" disabled :show-text="false" :max="5" size="small" />
          </div>
          <div class="q-card-content">{{ q.content }}</div>
          <div class="q-card-meta">
            <span class="meta-subject">{{ q.subjectName }}</span>
            <span class="meta-score">{{ q.score }} 分</span>
          </div>
          <div class="q-card-actions">
            <el-button size="small" @click="openDialog(q)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="preview(q)">预览</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && rows.length === 0" description="暂无题目，点击右上角『录入新题』开始" />
      </div>

      <el-pagination
        v-model:current-page="filter.current"
        v-model:page-size="filter.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px; justify-content: flex-end"
        @current-change="load"
        @size-change="load"
      />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑题目' : '录入新题'" width="100%" :close-on-click-modal="false" class="q-edit-dialog" :teleported="true">
      <el-form :model="dialog.form" label-width="70px" class="q-edit-form">
        <el-row :gutter="16">
          <el-col :span="10"><el-form-item label="学科"><el-select v-model="dialog.form.subjectId" placeholder="请选择" style="width:100%"><el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="题型"><el-select v-model="dialog.form.type" style="width:100%"><el-option label="单选" value="SINGLE" /><el-option label="多选" value="MULTIPLE" /><el-option label="判断" value="JUDGE" /><el-option label="填空" value="FILL" /></el-select></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="分值"><el-input-number v-model="dialog.form.score" :min="1" :max="100" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="难度"><el-rate v-model="dialog.form.difficulty" :max="5" show-text :texts="['入门', '简单', '中等', '困难', '挑战']" /></el-form-item>
        <el-form-item label="题干"><el-input v-model="dialog.form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item v-if="dialog.form.type === 'SINGLE' || dialog.form.type === 'MULTIPLE'" label="选项">
          <div v-for="(opt, idx) in dialog.form.options" :key="idx" style="display:flex;gap:8px;margin-bottom:8px">
            <span class="opt-label" :class="{ correct: dialog.form.correctAnswer?.includes(String.fromCharCode(65 + idx)) }">{{ String.fromCharCode(65 + idx) }}</span>
            <el-input v-model="dialog.form.options[idx]" :placeholder="`选项 ${String.fromCharCode(65 + idx)}`" />
            <el-button plain @click="dialog.form.options.splice(idx, 1)" :disabled="dialog.form.options.length <= 2">删</el-button>
          </div>
          <el-button plain :icon="Plus" @click="dialog.form.options.push('')" :disabled="dialog.form.options.length >= 8">添加选项</el-button>
        </el-form-item>
        <el-form-item label="答案"><el-input v-model="dialog.form.correctAnswer" placeholder="单选 A / 多选 ABC / 判断 T / 填空 文本" /></el-form-item>
        <el-form-item label="解析"><el-input v-model="dialog.form.analysis" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="知识点">
          <el-select v-model="dialog.form.knowledgePoint" multiple filterable allow-create placeholder="按 Enter 添加" style="width:100%">
            <el-option v-for="kp in knowledgePoints" :key="kp" :label="kp" :value="kp" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialog.visible" title="批量导入题目" width="460px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="目标学科" required>
          <el-select v-model="importDialog.subjectId" placeholder="请选择学科" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Download" @click="downloadTemplate">下载导入模板</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="importDialog.loading" @click="triggerFilePick">选择文件并导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialog.visible" title="题目预览" width="100%" class="q-preview-dialog" :teleported="true">
      <div v-if="previewDialog.row" class="preview-wrap">
        <el-tag :type="typeTagType(previewDialog.row.type)" effect="dark" size="small">{{ typeLabel(previewDialog.row.type) }}</el-tag>
        <span style="margin-left:8px;color:#64748b">{{ previewDialog.row.subjectName }} · {{ previewDialog.row.score }} 分</span>
        <h3 class="preview-content">{{ previewDialog.row.content }}</h3>
        <div v-if="previewDialog.row.options && previewDialog.row.options.length" class="preview-options">
          <div v-for="(opt, i) in previewDialog.row.options" :key="i" class="preview-opt" :class="{ correct: previewDialog.row.correctAnswer?.includes(String.fromCharCode(65 + i)) }">
            <span class="opt-label">{{ String.fromCharCode(65 + i) }}</span>{{ opt }}
          </div>
        </div>
        <el-alert v-if="previewDialog.row.correctAnswer" :title="'参考答案：' + previewDialog.row.correctAnswer" type="success" :closable="false" style="margin-top:16px" />
        <el-alert v-if="previewDialog.row.analysis" :title="'解析：' + previewDialog.row.analysis" type="info" :closable="false" style="margin-top:8px" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Upload, Download, MoreFilled } from '@element-plus/icons-vue'
import Icon from '@/components/Icon.vue'
import { questionApi, subjectApi } from '@/api'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const subjects = ref([])
const subjectOptions = ref([])
const viewMode = ref('table')
const knowledgePoints = ['Java 基础', '面向对象', '集合框架', '多线程', 'JVM', 'Spring Boot', '数据库', '网络协议']
const filter = reactive({ keyword: '', subjectId: null, type: '', difficulty: null, current: 1, size: 10 })
const dialog = reactive({ visible: false, form: blank() })
const previewDialog = reactive({ visible: false, row: null })
const importDialog = reactive({ visible: false, subjectId: null, loading: false })
const fileInputRef = ref(null)

function blank() { return { id: null, subjectId: null, type: 'SINGLE', difficulty: 3, score: 5, content: '', options: ['', '', '', ''], correctAnswer: '', analysis: '', knowledgePoint: [] } }
function typeLabel(t) { return { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', FILL: '填空', ESSAY: '简答', QA: '问答', CALC: '计算' }[t] || t }
function typeTagType(t) { return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning', FILL: 'info', ESSAY: 'danger', QA: 'danger', CALC: 'success' }[t] || '' }
function typeTagColor(t) { return { SINGLE: 'linear-gradient(135deg, #fb923c, #fb7185)', MULTIPLE: 'linear-gradient(135deg, #5eead4, #14b8a6)', JUDGE: 'linear-gradient(135deg, #fbbf24, #f59e0b)', FILL: 'linear-gradient(135deg, #c4b5fd, #a78bfa)', ESSAY: 'linear-gradient(135deg, #fb7185, #be123c)', QA: 'linear-gradient(135deg, #fb7185, #a78bfa)', CALC: 'linear-gradient(135deg, #5eead4, #0d9488)' }[t] || '' }

async function load() {
  loading.value = true
  try {
    const params = { current: filter.current, size: filter.size }
    if (filter.keyword) params.keyword = filter.keyword
    if (filter.subjectId) params.subjectId = filter.subjectId
    if (filter.type) params.type = filter.type
    if (filter.difficulty !== null && filter.difficulty !== undefined && filter.difficulty !== '') {
      params.difficulty = Number(filter.difficulty)
    }
    // dev 调试：把请求参数打到控制台，方便排查筛选问题
    if (import.meta.env.DEV) console.log('[Questions] load params =', params)
    const r = await questionApi.page(params)
    rows.value = r.data?.records || r.data || []
    total.value = r.data?.total || rows.value.length
  } catch (e) { ElMessage.error('题目加载失败') }
  finally { loading.value = false }
}

function reset() { Object.assign(filter, { keyword: '', subjectId: null, type: '', difficulty: null, current: 1 }); load() }

function onFilterChange() {
  filter.current = 1
  load()
}

function openDialog(row) {
  dialog.form = row ? JSON.parse(JSON.stringify(row)) : blank()
  if (!dialog.form.options || dialog.form.options.length === 0) dialog.form.options = ['', '', '', '']
  dialog.visible = true
}

function preview(row) { previewDialog.row = row; previewDialog.visible = true }

async function onRowCommand(cmd, row) {
  if (cmd === 'view') preview(row)
  else if (cmd === 'edit') openDialog(row)
  else if (cmd === 'delete') {
    try { await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' }) } catch (e) { return }
    try { await questionApi.delete(row.id); ElMessage.success('已删除'); load() } catch (e) { ElMessage.error('删除失败') }
  }
}

async function onSave() {
  if (!dialog.form.subjectId || !dialog.form.content) return ElMessage.warning('请填写完整')
  // 转换字段：options 数组 → JSON 字符串；knowledgePoint 数组 → 字符串
  const payload = JSON.parse(JSON.stringify(dialog.form))
  if (Array.isArray(payload.options)) {
    payload.options = JSON.stringify(payload.options)
  }
  if (Array.isArray(payload.knowledgePoint)) {
    payload.knowledgePoint = payload.knowledgePoint.join(',')
  }
  loading.value = true
  try {
    if (dialog.form.id) await questionApi.update(payload)
    else await questionApi.create(payload)
    ElMessage.success(dialog.form.id ? '题目已更新' : '题目已录入')
    dialog.visible = false
    load()
  } catch (e) { ElMessage.error('保存失败：' + (e.response?.data?.message || e.message)) }
  finally { loading.value = false }
}

onMounted(async () => {
  try { const r = await subjectApi.list(); subjects.value = r.data || []; subjectOptions.value = subjects.value.map(s => ({ id: s.id, name: s.name })) } catch (e) { /* */ }
  load()
})

function openImportDialog() {
  importDialog.subjectId = filter.subjectId || (subjects.value[0]?.id ?? null)
  importDialog.visible = true
}

function triggerFilePick() {
  if (!importDialog.subjectId) return ElMessage.warning('请先选择学科')
  fileInputRef.value.value = ''
  fileInputRef.value.click()
}

async function onFileSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  importDialog.loading = true
  try {
    const r = await questionApi.import(importDialog.subjectId, file)
    const count = r.data ?? 0
    ElMessage.success(`导入成功，共导入 ${count} 道题`)
    importDialog.visible = false
    load()
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '导入失败，请检查文件格式')
  } finally {
    importDialog.loading = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

async function downloadTemplate() {
  try {
    const blob = await questionApi.template()
    saveBlob(blob, '题目导入模板.xlsx')
    ElMessage.success('模板已下载')
  } catch (e) {
    ElMessage.error('模板下载失败')
  }
}

async function handleExport() {
  try {
    const blob = await questionApi.export({ subjectId: filter.subjectId })
    saveBlob(blob, `题目导出_${dateStamp()}.xlsx`)
    ElMessage.success('已导出')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function dateStamp() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}_${p(d.getHours())}${p(d.getMinutes())}`
}
</script>

<style scoped>
.filter-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.filter-card :deep(.el-card__body) { padding: 14px 20px; }
.filter-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.filter-spacer { flex: 1; }

.table-card { border-radius: var(--radius-md); }
.table-card :deep(.el-card__header) {
  background: linear-gradient(90deg, #fff7ed 0%, #fef3e8 50%, #fff1f2 100%);
}
.table-header { display: flex; align-items: center; justify-content: space-between; }

.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.q-card {
  background: #fff;
  border: 1px solid rgba(251, 146, 60, 0.12);
  border-radius: var(--radius-md);
  padding: 18px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}
.q-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, #fbbf24, #fb923c, #fb7185, #a78bfa);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.q-card::after {
  content: '';
  position: absolute;
  top: -40px; right: -40px;
  width: 120px; height: 120px;
  border-radius: 50%;
  opacity: 0.06;
  filter: blur(20px);
  transition: all 0.4s ease;
  pointer-events: none;
}
.q-card:hover { box-shadow: var(--shadow-md); transform: translateY(-3px); border-color: rgba(251, 146, 60, 0.30); }
.q-card:hover::before { opacity: 1; }
.q-card:hover::after { transform: scale(1.4); opacity: 0.12; }

/* 四种主题色卡片背景 */
.q-card.theme-0::after { background: #fb923c; }
.q-card.theme-1::after { background: #5eead4; }
.q-card.theme-2::after { background: #a78bfa; }
.q-card.theme-3::after { background: #fb7185; }
.q-card.theme-0 { background: linear-gradient(180deg, #fff7ed 0%, #ffffff 40%); }
.q-card.theme-1 { background: linear-gradient(180deg, #f0fdfa 0%, #ffffff 40%); }
.q-card.theme-2 { background: linear-gradient(180deg, #faf5ff 0%, #ffffff 40%); }
.q-card.theme-3 { background: linear-gradient(180deg, #fff1f2 0%, #ffffff 40%); }

.q-card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.q-type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.10);
}
.q-card-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--gray-800);
  min-height: 48px;
  max-height: 72px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.q-card-meta { display: flex; justify-content: space-between; margin-top: 14px; font-size: 12px; }
.meta-subject { color: var(--brand-primary-dark); font-weight: 500; }
.meta-score { color: var(--brand-pink-dark); font-weight: 600; }
.q-card-actions { display: flex; gap: 8px; margin-top: 14px; padding-top: 14px; border-top: 1px dashed var(--gray-200); }

.opt-label { display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 6px; background: var(--gray-100); color: var(--gray-600); font-size: 12px; font-weight: 600; flex-shrink: 0; }
.opt-label.correct { background: linear-gradient(135deg, #ccfbf1, #99f6e4); color: #0d9488; }

.preview-wrap { padding: 8px 0; }
.preview-content { font-size: 16px; line-height: 1.8; color: var(--gray-800); margin: 16px 0; }
.preview-options { display: flex; flex-direction: column; gap: 8px; }
.preview-opt { padding: 12px 16px; background: var(--bg-warm); border-radius: var(--radius); display: flex; align-items: center; gap: 10px; transition: all 0.2s ease; }
.preview-opt.correct { background: linear-gradient(135deg, #d1fae5, #ccfbf1); color: #0d9488; font-weight: 500; box-shadow: 0 2px 8px rgba(20, 184, 166, 0.15); }

/* 录入/编辑弹窗：全屏 modal，body 内部滚动 */
:deep(.q-edit-dialog .el-dialog) {
  margin: 0 !important;
  width: 100% !important;
  max-width: 100% !important;
  height: 100vh !important;
  max-height: 100vh !important;
  border-radius: 0 !important;
  display: flex;
  flex-direction: column;
}
:deep(.q-edit-dialog .el-dialog__header) { flex-shrink: 0; }
:deep(.q-edit-dialog .el-dialog__body) {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 20px 40px;
  min-height: 0;
}
:deep(.q-edit-dialog .el-dialog__footer) { flex-shrink: 0; }
.q-edit-form { padding-right: 4px; max-width: 1000px; margin: 0 auto; }

/* 预览弹窗：全屏 modal，body 内部滚动 */
:deep(.q-preview-dialog .el-dialog) {
  margin: 0 !important;
  width: 100% !important;
  max-width: 100% !important;
  height: 100vh !important;
  max-height: 100vh !important;
  border-radius: 0 !important;
  display: flex;
  flex-direction: column;
}
:deep(.q-preview-dialog .el-dialog__header) { flex-shrink: 0; }
:deep(.q-preview-dialog .el-dialog__body) {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 20px 40px;
  min-height: 0;
}
.preview-wrap { padding: 0; max-width: 900px; margin: 0 auto; }
</style>
