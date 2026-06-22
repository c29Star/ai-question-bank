<template>
  <div class="page">
    <div class="card">
      <div class="toolbar">
        <el-radio-group v-model="filter" @change="loadList">
          <el-radio-button :value="undefined">全部</el-radio-button>
          <el-radio-button :value="false">未掌握</el-radio-button>
          <el-radio-button :value="true">已掌握</el-radio-button>
        </el-radio-group>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无错题" />
      <div v-else>
        <el-card v-for="w in list" :key="w.id" class="wrong-card" shadow="hover">
          <div class="wrong-header">
            <div>
              <el-tag size="small" :type="typeColor(w.type)">{{ typeLabel(w.type) }}</el-tag>
              <el-tag v-if="w.mastered" size="small" type="success" style="margin-left: 4px">已掌握</el-tag>
              <span class="text-muted" style="margin-left: 8px">错 {{ w.wrongCount }} 次 · {{ w.knowledge_point }}</span>
            </div>
            <div>
              <el-button size="small" @click="aiRecommend(w)">🤖 AI 推题</el-button>
              <el-button v-if="!w.mastered" size="small" type="success" @click="mark(w, true)">标记掌握</el-button>
              <el-button v-else size="small" @click="mark(w, false)">撤销掌握</el-button>
            </div>
          </div>
          <div class="wrong-content">{{ w.content }}</div>
          <div v-if="w.options" class="text-muted">{{ w.options }}</div>
          <div class="text-success">答案：{{ w.answer }}</div>
          <div v-if="w.explanation" class="text-muted">解析：{{ w.explanation }}</div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="recVisible" title="AI 推荐的同类练习" width="700px">
      <div v-for="(q, i) in recommended" :key="q.id" class="rec-item">
        <p><b>[{{ i + 1 }}]</b> {{ q.content }}</p>
        <p class="text-muted">{{ q.options }}</p>
        <p class="text-success">答案：{{ q.answer }}</p>
        <el-divider v-if="i < recommended.length - 1" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { wrongApi, aiApi } from '@/api'

const list = ref([])
const loading = ref(false)
const filter = ref(undefined)
const recommended = ref([])
const recVisible = ref(false)

const TYPE = { SINGLE: ['单选', 'primary'], MULTIPLE: ['多选', 'success'], JUDGE: ['判断', 'warning'], ESSAY: ['简答', 'info'] }
const typeLabel = (t) => TYPE[t]?.[0] || t
const typeColor = (t) => TYPE[t]?.[1] || ''

async function loadList() {
  loading.value = true
  try {
    const params = filter.value === undefined ? {} : { mastered: filter.value }
    list.value = await wrongApi.list(params)
  } finally { loading.value = false }
}

async function mark(w, mastered) {
  await wrongApi.markMastered(w.id, mastered)
  ElMessage.success('已更新')
  loadList()
}

async function aiRecommend(w) {
  ElMessage.info('AI 正在生成同类题…')
  try {
    recommended.value = await aiApi.recommend({ wrongQuestionId: w.id, count: 3 })
    recVisible.value = true
  } catch (e) { /* */ }
}

onMounted(loadList)
</script>

<style scoped>
.wrong-card { margin-bottom: 12px; }
.wrong-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.wrong-content { font-weight: 500; line-height: 1.6; margin: 8px 0; }
.rec-item { line-height: 1.8; }
</style>
