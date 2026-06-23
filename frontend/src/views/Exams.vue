<template>
  <div class="page-container fade-in">
    <!-- 角色切换：教师看考试安排，学生看可参加考试 -->
    <el-tabs v-model="tab" type="card" class="exams-tabs">
      <el-tab-pane v-if="isStudent" label="可参加的考试" name="available">
        <el-card shadow="never" class="filter-card">
          <div class="filter-row">
            <span class="hint">👋 这里是你当前可以参加的考试。点击「开始考试」直接作答。</span>
            <div class="filter-spacer"></div>
            <el-button :icon="Refresh" @click="loadAvailable">刷新</el-button>
            <el-button type="primary" :icon="Clock" @click="$router.push('/history')">查看考试记录</el-button>
          </div>
        </el-card>
        <div v-loading="loading" class="exam-list">
          <el-empty v-if="!loading && availableList.length === 0" description="暂无可以参加的考试" />
          <div v-for="e in availableList" :key="e.id" class="exam-card status-published">
            <div class="exam-status"><Icon name="send" :size="14" /> 进行中</div>
            <div class="exam-main">
              <h3 class="exam-title">{{ e.title }}</h3>
              <div class="exam-meta">
                <span><Icon name="clock" :size="14" /> 限时 {{ e.duration }} 分钟</span>
                <span><Icon name="doc" :size="14" /> 已答 {{ e.usedAttempts || 0 }} / {{ e.maxAttempts }} 次</span>
                <span v-if="e.endTime"><Icon name="calendar" :size="14" /> 截止 {{ e.endTime }}</span>
              </div>
            </div>
            <div class="exam-actions">
              <el-button v-if="e.ongoingRecordId" type="warning" size="default" :icon="EditPen" @click="continueExam(e)">继续作答</el-button>
              <el-button v-else type="primary" size="default" :icon="VideoPlay" @click="startExam(e)">开始考试</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="!isStudent" label="考试安排" name="manage">
        <el-card shadow="never" class="filter-card">
          <div class="filter-row">
            <el-radio-group v-model="filter.status" @change="load">
              <el-radio-button :label="undefined">全部</el-radio-button>
              <el-radio-button label="DRAFT">草稿</el-radio-button>
              <el-radio-button label="PUBLISHED">已发布</el-radio-button>
              <el-radio-button label="ONGOING">进行中</el-radio-button>
              <el-radio-button label="ARCHIVED">已归档</el-radio-button>
            </el-radio-group>
            <div class="filter-spacer"></div>
            <el-button :icon="Refresh" @click="load">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="openCreate">安排新考试</el-button>
          </div>
        </el-card>

        <div v-loading="loading" class="exam-list">
          <el-empty v-if="!loading && rows.length === 0" description="还没有考试安排，点击『安排新考试』开始" />
          <div v-for="e in rows" :key="e.id" class="exam-card" :class="'status-' + (e.status || 'DRAFT').toLowerCase()">
            <div class="exam-status" :class="statusClass(e.status)">
              <Icon :name="statusIconName(e.status)" :size="14" />
              {{ statusLabel(e.status) }}
            </div>
            <div class="exam-main">
              <h3 class="exam-title">{{ e.title }}</h3>
              <div class="exam-meta">
                <span><Icon name="doc" :size="14" /> {{ e.paperName || '未绑定试卷' }}</span>
                <span><Icon name="clock" :size="14" /> {{ e.duration }} 分钟</span>
                <span v-if="e.startTime"><Icon name="calendar" :size="14" /> {{ e.startTime }} ~ {{ e.endTime || '不限' }}</span>
                <span><Icon name="refresh" :size="14" /> 最多 {{ e.maxAttempts }} 次</span>
              </div>
            </div>
            <div class="exam-actions">
              <el-button v-if="e.status === 'DRAFT'" type="success" size="small" :icon="Promotion" @click="publish(e)">发布</el-button>
              <el-button v-if="e.status === 'PUBLISHED' || e.status === 'ONGOING'" type="warning" size="small" plain @click="archive(e)">归档</el-button>
              <el-button v-if="e.status === 'DRAFT'" size="small" :icon="EditPen" @click="openEdit(e)">编辑</el-button>
              <el-button size="small" :icon="Delete" type="danger" plain @click="remove(e)">删除</el-button>
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
      </el-tab-pane>
    </el-tabs>

    <!-- 安排新考试 / 编辑考试 -->
    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑考试' : '安排新考试'" width="640px" :teleported="true">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="考试名称" required>
          <el-input v-model="dialog.form.title" placeholder="如：Java 基础期中考试" />
        </el-form-item>
        <el-form-item label="关联试卷" required>
          <el-select v-model="dialog.form.paperId" style="width:100%" placeholder="请选择试卷" filterable>
            <el-option v-for="p in papers" :key="p.id" :label="`${p.title}（${p.totalScore}分/${p.questionCount}题）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开考时间">
          <el-date-picker v-model="dialog.form.startTime" type="datetime" placeholder="可选，开放时间" style="width:100%" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="dialog.form.endTime" type="datetime" placeholder="可选，截止时间" style="width:100%" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="dialog.form.duration" :min="10" :max="300" :step="5" />
          <span style="margin-left:8px;color:#94a3b8">分钟</span>
        </el-form-item>
        <el-form-item label="参考次数">
          <el-input-number v-model="dialog.form.maxAttempts" :min="1" :max="10" />
          <span style="margin-left:8px;color:#94a3b8">次（每名学生最多参考次数）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Refresh, Promotion, EditPen, Delete,
  VideoPlay, Clock, Calendar, Document,
} from '@element-plus/icons-vue'
import Icon from '@/components/Icon.vue'
import { examApi, paperApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isStudent = computed(() => userStore.user?.role === 'STUDENT')
const tab = ref(isStudent.value ? 'available' : 'manage')

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const availableList = ref([])
const papers = ref([])
const filter = reactive({ status: undefined, current: 1, size: 10 })
const dialog = reactive({ visible: false, form: blank() })

function blank() { return { id: null, title: '', paperId: null, startTime: null, endTime: null, duration: 60, maxAttempts: 1 } }
function statusLabel(s) { return { DRAFT: '草稿', PUBLISHED: '已发布', ONGOING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档' }[s] || s || '草稿' }
function statusClass(s) { return { DRAFT: 'st-draft', PUBLISHED: 'st-active', ONGOING: 'st-active', FINISHED: 'st-archived', ARCHIVED: 'st-archived' }[s] || 'st-draft' }
function statusIconName(s) { return { DRAFT: 'edit', PUBLISHED: 'send', ONGOING: 'send', FINISHED: 'doc', ARCHIVED: 'doc' }[s] || 'doc' }

async function load() {
  if (isStudent.value) return loadAvailable()
  loading.value = true
  try {
    const params = { current: filter.current, size: filter.size }
    if (filter.status) params.status = filter.status
    const r = await examApi.page(params)
    rows.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function loadAvailable() {
  loading.value = true
  try {
    const r = await examApi.available()
    availableList.value = r.data || []
  } catch (e) { ElMessage.error('加载失败：' + (e.response?.data?.message || e.message)) }
  finally { loading.value = false }
}

async function loadPapers() {
  try {
    const r = await paperApi.page({ current: 1, size: 200 })
    papers.value = r.data?.records || []
  } catch (e) { /* */ }
}

function openCreate() { dialog.form = blank(); dialog.visible = true }
function openEdit(e) {
  dialog.form = { id: e.id, title: e.title, paperId: e.paperId, startTime: e.startTime, endTime: e.endTime, duration: e.duration, maxAttempts: e.maxAttempts }
  dialog.visible = true
}

async function onSave() {
  if (!dialog.form.title || !dialog.form.paperId) return ElMessage.warning('请填写完整')
  if (dialog.form.startTime && dialog.form.endTime && new Date(dialog.form.endTime) <= new Date(dialog.form.startTime)) {
    return ElMessage.warning('截止时间必须晚于开考时间')
  }
  loading.value = true
  try {
    if (dialog.form.id) {
      await examApi.update(dialog.form)
      ElMessage.success('已更新')
    } else {
      await examApi.create(dialog.form)
      ElMessage.success('已创建')
    }
    dialog.visible = false
    load()
  } catch (e) { ElMessage.error('操作失败：' + (e.response?.data?.message || e.message)) }
  finally { loading.value = false }
}

async function publish(e) {
  try { await ElMessageBox.confirm(`确定发布《${e.title}》？发布后学生可以参加`, '提示', { type: 'warning' }) } catch (err) { return }
  try { await examApi.publish(e.id); ElMessage.success('已发布'); load() } catch (err) { ElMessage.error('发布失败') }
}
async function archive(e) {
  try { await ElMessageBox.confirm(`确定归档《${e.title}》？`, '提示', { type: 'warning' }) } catch (err) { return }
  try { await examApi.archive(e.id); ElMessage.success('已归档'); load() } catch (err) { ElMessage.error('归档失败') }
}
async function remove(e) {
  try { await ElMessageBox.confirm(`确定删除《${e.title}》？此操作不可恢复`, '提示', { type: 'warning' }) } catch (err) { return }
  try { await examApi.delete(e.id); ElMessage.success('已删除'); load() } catch (err) { ElMessage.error('删除失败') }
}

async function startExam(e) {
  try { await ElMessageBox.confirm(`确定开始《${e.title}》？考试时长 ${e.duration} 分钟`, '提示', { type: 'warning' }) } catch (err) { return }
  router.push(`/exam-taking/${e.id}`)
}
function continueExam(e) {
  router.push(`/exam-taking/${e.id}`)
}

onMounted(async () => {
  await loadPapers()
  load()
})
</script>

<style scoped>
.exams-tabs { background: transparent; }
.exams-tabs :deep(.el-tabs__nav) { background: transparent; }

.filter-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.filter-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.filter-spacer { flex: 1; }
.hint { color: var(--gray-600); font-size: 13px; }

.exam-list { display: flex; flex-direction: column; gap: 12px; }
.exam-card {
  display: flex; align-items: center; gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border: 1px solid rgba(251, 146, 60, 0.10);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}
.exam-card:hover { box-shadow: 0 8px 24px rgba(251, 113, 133, 0.14); transform: translateX(2px); }
.exam-card.status-draft { border-left: 5px solid #fb923c; }
.exam-card.status-published, .exam-card.status-ongoing { border-left: 5px solid #14b8a6; }
.exam-card.status-archived, .exam-card.status-finished { border-left: 5px solid #a78bfa; }

.exam-status { display: flex; align-items: center; gap: 4px; padding: 6px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; flex-shrink: 0; }
.exam-status.st-draft { background: linear-gradient(135deg, #fef3c7, #fed7aa); color: #c2410c; }
.exam-status.st-active { background: linear-gradient(135deg, #ccfbf1, #99f6e4); color: #0d9488; }
.exam-status.st-archived { background: linear-gradient(135deg, #ede9fe, #c4b5fd); color: #7c3aed; }

.exam-main { flex: 1; min-width: 0; }
.exam-title { margin: 0 0 6px; font-size: 15px; font-weight: 600; color: var(--gray-800); }
.exam-meta { display: flex; gap: 16px; font-size: 12px; color: var(--gray-500); flex-wrap: wrap; }
.exam-meta span { display: flex; align-items: center; gap: 4px; }
.exam-actions { display: flex; gap: 6px; flex-shrink: 0; align-items: center; }
</style>
