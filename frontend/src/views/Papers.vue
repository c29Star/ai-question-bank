<template>
  <div class="page">
    <div class="card">
      <div class="toolbar">
        <el-select v-model="subjectId" placeholder="科目" clearable style="width: 160px" @change="loadList" />
        <el-button v-if="userStore.isTeacher" type="primary" @click="openCreate">+ 新建试卷</el-button>
        <el-button v-if="userStore.isTeacher" type="success" @click="openAuto">🎲 随机组卷</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="subjectId" label="科目ID" width="100" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">查看</el-button>
            <el-button v-if="userStore.isTeacher" size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" :title="detail?.title" width="700px">
      <div v-if="detail">
        <p><b>总分：</b>{{ detail.paper.totalScore }} 分 &nbsp; <b>时长：</b>{{ detail.paper.duration }} 分钟</p>
        <p><b>题目数：</b>{{ detail.questions.length }}</p>
        <el-divider />
        <ol>
          <li v-for="(q, i) in detail.questions" :key="q.id" style="margin-bottom: 16px">
            <div><b>[{{ typeLabel(q.type) }}] {{ q.content }}</b></div>
            <div v-if="q.options" class="text-muted">{{ q.options }}</div>
            <div class="text-success">答案：{{ q.answer }}</div>
          </li>
        </ol>
      </div>
    </el-dialog>

    <!-- 随机组卷 -->
    <el-dialog v-model="autoVisible" title="随机组卷" width="560px">
      <el-form :model="autoForm" label-width="100px">
        <el-form-item label="试卷标题">
          <el-input v-model="autoForm.title" placeholder="如：Java 期中模拟" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="autoForm.subjectId" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="autoForm.duration" :min="10" :max="300" /> 分钟
        </el-form-item>
        <el-divider>组卷规则</el-divider>
        <div v-for="(r, i) in autoForm.rules" :key="i" class="rule-row">
          <el-select v-model="r.type" style="width: 110px">
            <el-option label="单选" value="SINGLE" />
            <el-option label="多选" value="MULTIPLE" />
            <el-option label="判断" value="JUDGE" />
            <el-option label="简答" value="ESSAY" />
          </el-select>
          <el-input-number v-model="r.count" :min="1" :max="50" style="width: 100px" />
          <span>题 ×</span>
          <el-input-number v-model="r.score" :min="1" :max="20" style="width: 90px" />
          <span>分/题</span>
          <el-button size="small" type="danger" @click="autoForm.rules.splice(i, 1)">删</el-button>
        </div>
        <el-button @click="addRule" size="small" type="primary" plain>+ 添加规则</el-button>
      </el-form>
      <template #footer>
        <el-button @click="autoVisible = false">取消</el-button>
        <el-button type="primary" :loading="autoing" @click="doAuto">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paperApi, subjectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const list = ref([])
const loading = ref(false)
const subjectId = ref(null)
const subjects = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const autoVisible = ref(false)
const autoing = ref(false)
const autoForm = reactive({
  title: '',
  subjectId: null,
  duration: 60,
  rules: [
    { type: 'SINGLE', count: 10, score: 2 },
    { type: 'MULTIPLE', count: 5, score: 4 },
    { type: 'JUDGE', count: 5, score: 2 },
  ],
})

const TYPE = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', ESSAY: '简答' }
const typeLabel = (t) => TYPE[t] || t

async function loadList() {
  loading.value = true
  try {
    const params = subjectId.value ? { subjectId: subjectId.value } : {}
    list.value = await paperApi.list(params)
  } finally { loading.value = false }
}

async function showDetail(row) {
  detail.value = await paperApi.detail(row.id)
  detailVisible.value = true
}

function openCreate() {
  ElMessage.info('请使用"随机组卷"功能快速生成试卷，或在选题器中手动选题（待扩展）')
}

function openAuto() {
  autoForm.title = ''
  autoForm.subjectId = subjects.value[0]?.id
  autoVisible.value = true
}

function addRule() {
  autoForm.rules.push({ type: 'SINGLE', count: 5, score: 2 })
}

async function doAuto() {
  if (!autoForm.title) return ElMessage.warning('请输入标题')
  if (!autoForm.subjectId) return ElMessage.warning('请选择科目')
  if (autoForm.rules.length === 0) return ElMessage.warning('请添加组卷规则')
  autoing.value = true
  try {
    const id = await paperApi.autoGenerate(autoForm)
    ElMessage.success(`组卷成功，ID = ${id}`)
    autoVisible.value = false
    loadList()
  } finally { autoing.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除试卷「${row.title}」？`, '提示', { type: 'warning' })
  await paperApi.remove(row.id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(async () => {
  subjects.value = await subjectApi.list()
  await loadList()
})
</script>

<style scoped>
.rule-row { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
</style>
