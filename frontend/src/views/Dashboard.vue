<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="6"><div class="stat-card"><div class="label">题库总数</div><div class="value">{{ overview.questions || 0 }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">已完成考试</div><div class="value">{{ overview.exams || 0 }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">错题数</div><div class="value text-warn">{{ overview.wrong || 0 }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">平均分</div><div class="value text-success">{{ overview.avgScore || 0 }}</div></div></el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="12">
        <div class="card">
          <h3>快捷入口</h3>
          <div class="quick-grid">
            <div v-if="userStore.isTeacher" class="quick-item" @click="$router.push('/questions')">
              <el-icon size="32" color="#409eff"><EditPen /></el-icon><span>题库管理</span>
            </div>
            <div v-if="userStore.isTeacher" class="quick-item" @click="$router.push('/papers')">
              <el-icon size="32" color="#67c23a"><Document /></el-icon><span>组卷中心</span>
            </div>
            <div v-if="userStore.isStudent" class="quick-item" @click="$router.push('/exams')">
              <el-icon size="32" color="#e6a23c"><Tickets /></el-icon><span>参加考试</span>
            </div>
            <div class="quick-item" @click="$router.push('/wrong')">
              <el-icon size="32" color="#f56c6c"><Warning /></el-icon><span>错题复习</span>
            </div>
            <div class="quick-item" @click="$router.push('/stats')">
              <el-icon size="32" color="#909399"><DataLine /></el-icon><span>学情分析</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <h3>系统说明</h3>
          <ul class="info">
            <li>📚 题目涵盖单选、多选、判断、简答四种题型</li>
            <li>🎯 支持手动选题与随机组卷两种方式</li>
            <li>🤖 AI 增强：自动生成解析、智能推同类题</li>
            <li>📊 自动判分（客观题）+ 教师批改（主观题）</li>
            <li>📝 错题自动归集，支持 AI 推荐同类练习</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { questionApi, examApi, wrongApi, statsApi } from '@/api'

const userStore = useUserStore()
const overview = ref({})

onMounted(async () => {
  try {
    // 题库总数（取第一页）
    const qPage = await questionApi.page({ pageNum: 1, pageSize: 1 })
    overview.value.questions = qPage.total

    if (userStore.isStudent) {
      const records = await examApi.myRecords()
      overview.value.exams = records.length
      const wrong = await wrongApi.list({})
      overview.value.wrong = wrong.length
      const stats = await statsApi.personal()
      overview.value.avgScore = stats.overview.avgScore
    }
  } catch (e) { /* */ }
})
</script>

<style scoped>
.stat-card { background: #fff; padding: 24px; border-radius: 8px; text-align: center; }
.label { color: #999; font-size: 14px; }
.value { font-size: 32px; font-weight: 600; color: #409eff; margin-top: 8px; }
.text-warn { color: #e6a23c; }
.text-success { color: #67c23a; }
.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 16px; }
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 20px; background: #f5f7fa; border-radius: 8px; cursor: pointer;
  transition: all 0.2s;
}
.quick-item:hover { background: #ecf5ff; transform: translateY(-2px); }
.info { padding-left: 20px; line-height: 2; color: #666; }
</style>
