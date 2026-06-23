import { defineStore } from 'pinia'
import { questionApi, paperApi, examApi, wrongApi } from '@/api'

export const useStatsStore = defineStore('stats', {
  state: () => ({
    questionCount: 0,
    paperCount: 0,
    examCount: 0,
    wrongCount: 0,
    loaded: false,
    loading: false,
  }),
  actions: {
    async refresh() {
      // 单飞：多次调用并发只发一次请求
      if (this.loading) return
      this.loading = true
      try {
        const [q, p, e, w] = await Promise.allSettled([
          questionApi.page({ current: 1, size: 1 }),
          paperApi.page({ current: 1, size: 1 }),
          examApi.page({ current: 1, size: 1 }),
          wrongApi.page({ current: 1, size: 1 }),
        ])
        if (q.status === 'fulfilled') this.questionCount = q.value.data?.total || 0
        if (p.status === 'fulfilled') this.paperCount = p.value.data?.total || 0
        if (e.status === 'fulfilled') this.examCount = e.value.data?.total || 0
        if (w.status === 'fulfilled') this.wrongCount = w.value.data?.total || 0
        this.loaded = true
      } catch (e) { /* */ }
      finally {
        this.loading = false
      }
    },
    reset() {
      this.questionCount = 0
      this.paperCount = 0
      this.examCount = 0
      this.wrongCount = 0
      this.loaded = false
    },
  },
})