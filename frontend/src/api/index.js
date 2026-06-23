import http from '@/utils/http'

export const questionApi = {
  page: (params) => http.get('/questions', { params }),
  get: (id) => http.get(`/questions/${id}`),
  create: (data) => http.post('/questions', data),
  update: (data) => http.put('/questions', data),
  delete: (id) => http.delete('/questions', { params: { id } }),
  random: (params) => http.get('/questions/random', { params }),
  template: () => http.get('/questions/template', { responseType: 'blob' }),
  import: (subjectId, file) => {
    const fd = new FormData()
    fd.append('file', file)
    return http.post(`/questions/import/${subjectId}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  export: (params) => http.get('/questions/export', { params, responseType: 'blob' }),
}

export const subjectApi = {
  list: () => http.get('/subjects'),
}

export const paperApi = {
  page: (params) => http.get('/papers', { params }),
  get: (id) => http.get(`/papers/${id}`),
  create: (data) => http.post('/papers', data),
  update: (data) => http.put('/papers', data),
  delete: (id) => http.delete(`/papers/${id}`),
  autoGenerate: (data) => http.post('/papers/auto-generate', data),
}

export const examApi = {
  page: (params) => http.get('/exams', { params }),
  create: (data) => http.post('/exams', data),
  update: (data) => http.put('/exams', data),
  delete: (id) => http.delete(`/exams/${id}`),
  publish: (id) => http.put(`/exams/${id}/publish`),
  archive: (id) => http.put(`/exams/${id}/archive`),
  start: (id) => http.post(`/exams/${id}/start`),
  saveAnswer: (data) => http.post('/exams/save-answer', data),
  submit: (data) => http.post('/exams/submit', data),
  myRecords: () => http.get('/exams/my-records'),
  available: () => http.get('/exams/available'),
}

export const wrongApi = {
  page: (params) => http.get('/wrong-questions', { params }),
  toggleMastered: (id, mastered) => http.put(`/wrong-questions/${id}/mastered`, null, { params: { mastered } }),
  stats: () => http.get('/wrong-questions/stats'),
}

export const statsApi = {
  personal: () => http.get('/stats/personal'),
}

export const aiApi = {
  explain: (questionId) => http.post(`/ai/explain/${questionId}`),
  // wrongQuestionId 是 wrong_questions 表的主键（不是 questions.id），由后端 @RequestParam 强校验
  recommend: (wrongQuestionId, count = 3) => http.get('/ai/recommend', { params: { wrongQuestionId, count } }),
}
