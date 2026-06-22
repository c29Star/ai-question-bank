<template>
  <div class="page">
    <div class="card">
      <h3>可参加的考试</h3>
      <el-table :data="available" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="考试标题" />
        <el-table-column prop="duration" label="时长(分钟)" width="110" />
        <el-table-column label="剩余次数" width="100">
          <template #default="{ row }">{{ row.maxAttempts - row.usedAttempts }} / {{ row.maxAttempts }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.ongoingRecordId" type="warning">进行中</el-tag>
            <el-tag v-else type="success">未开始</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goExam(row)">
              {{ row.ongoingRecordId ? '继续' : '开始' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card mt-16" v-if="userStore.isTeacher">
      <h3>考试管理（教师）</h3>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 新建考试</el-button>
      </div>
      <el-table :data="allExams" v-loading="examLoading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="paperId" label="试卷ID" width="100" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="maxAttempts" label="次数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" @click="publish(row)">发布</el-button>
            <el-button v-if="row.status === 'PUBLISHED' || row.status === 'ONGOING'" size="small" @click="archive(row)">归档</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="createVisible" title="新建考试" width="540px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="试卷">
          <el-select v-model="createForm.paperId" style="width: 100%">
            <el-option v-for="p in papers" :key="p.id" :label="`#${p.id} ${p.title}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="如：Java 期中考试" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="createForm.duration" :min="10" :max="300" />
        </el-form-item>
        <el-form-item label="可考次数">
          <el-input-number v-model="createForm.maxAttempts" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examApi, paperApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const available = ref([])
const allExams = ref([])
const examLoading = ref(false)
const papers = ref([])
const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive({ paperId: null, title: '', duration: 60, maxAttempts: 1 })

const STATUS = { DRAFT: ['草稿', 'info'], PUBLISHED: ['已发布', 'success'], ONGOING: ['进行中', 'warning'], FINISHED: ['已结束', ''], ARCHIVED: ['已归档', 'info'] }
const statusLabel = (s) => STATUS[s]?.[0] || s
const statusType = (s) => STATUS[s]?.[1] || ''

async function loadAvailable() {
  available.value = await examApi.available()
}

async function loadAll() {
  examLoading.value = true
  try {
    const res = await examApi.page({ pageNum: 1, pageSize: 100 })
    allExams.value = res.records || []
  } finally { examLoading.value = false }
}

function goExam(row) {
  router.push(`/exam/${row.id}`)
}

function openCreate() {
  createForm.paperId = papers.value[0]?.id
  createForm.title = ''
  createForm.duration = 60
  createForm.maxAttempts = 1
  createVisible.value = true
}

async function doCreate() {
  if (!createForm.paperId || !createForm.title) return ElMessage.warning('请填写完整')
  creating.value = true
  try {
    await examApi.create(createForm)
    ElMessage.success('已创建（草稿）')
    createVisible.value = false
    loadAll()
  } finally { creating.value = false }
}

async function publish(row) {
  await examApi.publish(row.id)
  ElMessage.success('已发布')
  loadAll()
}

async function archive(row) {
  await examApi.archive(row.id)
  ElMessage.success('已归档')
  loadAll()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除考试「${row.title}」？`, '提示', { type: 'warning' })
  await examApi.remove(row.id)
  ElMessage.success('已删除')
  loadAll()
}

onMounted(async () => {
  papers.value = await paperApi.list()
  loadAvailable()
  if (userStore.isTeacher) loadAll()
})
</script>
