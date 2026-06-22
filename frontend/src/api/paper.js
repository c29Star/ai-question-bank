import http from '@/utils/http'

export const paperApi = {
  list: (params) => http.get('/papers', { params }),
  detail: (id) => http.get(`/papers/${id}`),
  create: (data) => http.post('/papers', data),
  update: (data) => http.put('/papers', data),
  remove: (id) => http.delete(`/papers/${id}`),
  autoGenerate: (data) => http.post('/papers/auto-generate', data),
}

export const examApi = {
  page: (params) => http.get('/exams', { params }),
  create: (data) => http.post('/exams', data),
  update: (data) => http.put('/exams', data),
  remove: (id) => http.delete(`/exams/${id}`),
  publish: (id) => http.put(`/exams/${id}/publish`),
  archive: (id) => http.put(`/exams/${id}/archive`),
  available: () => http.get('/exams/available'),
  start: (id) => http.post(`/exams/${id}/start`),
  saveAnswer: (params) => http.post('/exams/save-answer', null, { params }),
  submit: (data) => http.post('/exams/submit', data),
  myRecords: () => http.get('/exams/my-records'),
}
