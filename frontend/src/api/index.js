import request from '@/utils/request'

// ---------- 认证 ----------
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)

// ---------- 用户 ----------
export const getMe = () => request.get('/user/me')
export const updateProfile = (data) => request.put('/user/profile', data)
export const changePassword = (data) => request.put('/user/password', data)

// ---------- 课程 ----------
export const getCourses = () => request.get('/courses')
export const getCourse = (id) => request.get(`/courses/${id}`)

// ---------- 知识点 ----------
export const getKnowledgePoints = (courseId) => request.get('/knowledge-points', { params: { courseId } })
export const getKnowledgePoint = (id) => request.get(`/knowledge-points/${id}`)
export const markStudied = (id) => request.post(`/knowledge-points/${id}/studied`)

// ---------- 题库 / 答题 ----------
export const getPracticeQuestions = (params) => request.get('/questions/practice', { params })
export const getPracticeQuestion = (id) => request.get(`/questions/${id}`)
export const getDailyQuestion = () => request.get('/questions/daily')
export const submitAnswer = (data) => request.post('/answers/submit', data)
export const getHistory = (params) => request.get('/answers/history', { params })

// ---------- 学习统计 ----------
export const getStats = () => request.get('/study/stats')
export const getWeakPoints = () => request.get('/study/weak-points')
export const getStudiedIds = () => request.get('/study/studied-ids')
export const getStudyCalendar = (year, month) => request.get('/study/calendar', { params: { year, month } })

// ---------- AI ----------
export const aiConfigStatus = () => request.get('/ai/config-status')
// 注意：AI 聊天与薄弱点分析为 SSE 流式接口，由 AiAssistant.vue 内使用 fetch 消费，不走 axios 拦截器

// ---------- 后台管理 ----------
export const getDashboard = () => request.get('/admin/dashboard')
export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const updateUserRole = (id, role) => request.put(`/admin/users/${id}/role`, null, { params: { role } })
export const deleteUser = (id) => request.delete(`/admin/users/${id}`)

export const getAdminCourses = () => request.get('/admin/courses')
export const createCourse = (data) => request.post('/admin/courses', data)
export const updateCourse = (id, data) => request.put(`/admin/courses/${id}`, data)
export const deleteCourse = (id) => request.delete(`/admin/courses/${id}`)

export const getAdminKnowledgePoints = (courseId) => request.get('/admin/knowledge-points', { params: { courseId } })
export const createKnowledgePoint = (data) => request.post('/admin/knowledge-points', data)
export const updateKnowledgePoint = (id, data) => request.put(`/admin/knowledge-points/${id}`, data)
export const deleteKnowledgePoint = (id) => request.delete(`/admin/knowledge-points/${id}`)

export const getAdminQuestions = (params) => request.get('/admin/questions', { params })
export const getAdminQuestion = (id) => request.get(`/admin/questions/${id}`)
export const createQuestion = (data) => request.post('/admin/questions', data)
export const updateQuestion = (id, data) => request.put(`/admin/questions/${id}`, data)
export const deleteQuestion = (id) => request.delete(`/admin/questions/${id}`)

export const getAiConfig = () => request.get('/admin/ai-config')
export const saveAiConfig = (data) => request.put('/admin/ai-config', data)

// ---------- 复试机试 ----------
export const getCodingProblems = () => request.get('/coding/problems')
export const getCodingProblem = (id) => request.get(`/coding/problems/${id}`)
export const submitCode = (data) => request.post('/coding/submit', data)
export const getCodingSubmissions = (params) => request.get('/coding/submissions', { params })

// ---------- 后台：算法题管理 ----------
export const getAdminCodingProblems = () => request.get('/admin/coding/problems')
export const createCodingProblem = (data) => request.post('/admin/coding/problems', data)
export const updateCodingProblem = (id, data) => request.put(`/admin/coding/problems/${id}`, data)
export const deleteCodingProblem = (id) => request.delete(`/admin/coding/problems/${id}`)
export const getAdminCodingTestCases = (problemId) => request.get('/admin/coding/test-cases', { params: { problemId } })
export const createCodingTestCase = (data) => request.post('/admin/coding/test-cases', data)
export const updateCodingTestCase = (id, data) => request.put(`/admin/coding/test-cases/${id}`, data)
export const deleteCodingTestCase = (id) => request.delete(`/admin/coding/test-cases/${id}`)
