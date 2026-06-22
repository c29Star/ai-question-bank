<template>
  <div class="page">
    <div class="card">
      <div class="toolbar">
        <el-select v-model="query.subjectId" placeholder="科目" clearable style="width: 140px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="query.type" placeholder="题型" clearable style="width: 120px">
          <el-option label="单选" value="SINGLE" />
          <el-option label="多选" value="MULTIPLE" />
          <el-option label="判断" value="JUDGE" />
          <el-option label="简答" value="ESSAY" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="搜索题干/知识点" clearable style="width: 220px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button v-if="userStore.isTeacher" type="success" @click="openCreate">+ 新建题目</el-button>
        <el-button v-if="userStore.isTeacher" @click="openImport">批量导入</el-button>
        <el-button @click="downloadTemplate">下载模板</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="题型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            <el-rate v-model="row.difficulty" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题干" show-overflow-tooltip />
        <el-table-column prop="knowledgePoint" label="知识点" width="120" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="aiExplain(row)">AI 解析</el-button>
            <el-button v-if="userStore.isTeacher" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="userStore.isTeacher" size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 16px; text-align: right"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新建题目'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="科目">
          <el-select v-model="form.subjectId" placeholder="选择科目" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-radio-group v-model="form.type">
            <el-radio value="SINGLE">单选</el-radio>
            <el-radio value="MULTIPLE">多选</el-radio>
            <el-radio value="JUDGE">判断</el-radio>
            <el-radio value="ESSAY">简答</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="form.difficulty" :max="5" />
        </el-form-item>
        <el-form-item label="题干">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="form.type === 'SINGLE' || form.type === 'MULTIPLE'" label="选项（JSON）">
          <el-input v-model="form.options" type="textarea" :rows="3" placeholder='如：["A. xxx", "B. xxx", "C. xxx", "D. xxx"]' />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" placeholder="单选/判断填字母；多选填如 ABD；简答填文本" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.explanation" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoint" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI 解析对话框 -->
    <el-dialog v-model="aiVisible" title="AI 智能解析" width="640px">
      <div v-loading="aiLoading" class="ai-content" v-html="aiText"></div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importVisible" title="批量导入题目" width="500px">
      <el-form>
        <el-form-item label="导入到科目">
          <el-select v-model="importSubjectId" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Excel 文件">
          <el-upload :auto-upload="false" :limit="1" :on-change="onFileChange" :show-file-list="false">
            <el-button>选择文件</el-button>
            <span style="margin-left: 12px; color: #999">{{ importFile?.name || '未选择' }}</span>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionApi, subjectApi } from '@/api'
import { aiApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const TYPE = { SINGLE: ['单选', 'primary'], MULTIPLE: ['多选', 'success'], JUDGE: ['判断', 'warning'], ESSAY: ['简答', 'info'] }
const typeLabel = (t) => TYPE[t]?.[0] || t
const typeColor = (t) => TYPE[t]?.[1] || ''

const list = ref([])
const total = ref(0)
const loading = ref(false)
const subjects = ref([])
const query = reactive({ subjectId: null, type: '', keyword: '', pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ id: null, subjectId: null, type: 'SINGLE', difficulty: 2, content: '', options: '[]', answer: '', explanation: '', knowledgePoint: '' })
const aiVisible = ref(false)
const aiLoading = ref(false)
const aiText = ref('')
const importVisible = ref(false)
const importSubjectId = ref(null)
const importFile = ref(null)
const importing = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await questionApi.page(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function openCreate() {
  form.value = { id: null, subjectId: subjects.value[0]?.id, type: 'SINGLE', difficulty: 2, content: '', options: '[]', answer: '', explanation: '', knowledgePoint: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.subjectId || !form.value.content || !form.value.answer) return ElMessage.warning('请填写完整')
  submitting.value = true
  try {
    if (form.value.id) await questionApi.update(form.value)
    else await questionApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除题目 #${row.id}？`, '提示', { type: 'warning' })
  await questionApi.remove([row.id])
  ElMessage.success('已删除')
  loadData()
}

async function aiExplain(row) {
  aiVisible.value = true
  aiLoading.value = true
  aiText.value = ''
  try {
    const text = await aiApi.explain(row.id)
    aiText.value = text.replace(/\n/g, '<br/>')
  } finally { aiLoading.value = false }
}

function openImport() {
  importSubjectId.value = subjects.value[0]?.id
  importFile.value = null
  importVisible.value = true
}

function onFileChange(file) {
  importFile.value = file.raw
}

async function doImport() {
  if (!importFile.value) return ElMessage.warning('请选择文件')
  importing.value = true
  try {
    const count = await questionApi.importExcel(importSubjectId.value, importFile.value)
    ElMessage.success(`成功导入 ${count} 道题`)
    importVisible.value = false
    loadData()
  } finally { importing.value = false }
}

function downloadTemplate() {
  window.open(questionApi.templateUrl, '_blank')
}

onMounted(async () => {
  subjects.value = await subjectApi.list()
  loadData()
})
</script>
