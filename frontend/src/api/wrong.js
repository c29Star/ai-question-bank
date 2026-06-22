import http from '@/utils/http'

export const wrongApi = {
  list: (params) => http.get('/wrong-questions', { params }),
  markMastered: (id, mastered) => http.put(`/wrong-questions/${id}/mastered`, null, { params: { mastered } }),
  stats: () => http.get('/wrong-questions/stats'),
}

export const statsApi = {
  personal: () => http.get('/stats/personal'),
}

export const aiApi = {
  explain: (questionId) => http.post(`/ai/explain/${questionId}`),
  recommend: (params) => http.get('/ai/recommend', { params }),
}
