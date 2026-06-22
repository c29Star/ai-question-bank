import http from '@/utils/http'

export const questionApi = {
  page: (params) => http.get('/questions', { params }),
  get: (id) => http.get(`/questions/${id}`),
  create: (data) => http.post('/questions', data),
  update: (data) => http.put('/questions', data),
  remove: (ids) => http.delete('/questions', { data: ids }),
  randomPick: (params) => http.get('/questions/random', { params }),
  importExcel: (subjectId, file) => {
    const form = new FormData()
    form.append('file', file)
    return http.post(`/questions/import/${subjectId}`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  templateUrl: '/api/questions/template',
  exportUrl: (subjectId) => `/api/questions/export${subjectId ? `?subjectId=${subjectId}` : ''}`,
}

export const subjectApi = {
  list: () => http.get('/subjects'),
}
