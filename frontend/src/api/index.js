import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Handle 401 globally
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

// Auth
export const login = (email, password) =>
  api.post('/auth/login', { email, password })

export const registerStudent = (data) => api.post('/register/student', data)
export const registerInstructor = (data) => api.post('/register/instructor', data)
export const validateToken = (token) => api.get('/register/validate-token', { params: { token } })

// Me
export const getMe = () => api.get('/me')
export const updateStudent = (id, data) => api.put(`/students/${id}`, data)
export const updateInstructor = (id, data) => api.put(`/instructors/${id}`, data)

// Sections
export const getSections = (params) => api.get('/sections', { params })
export const createSection = (data) => api.post('/sections', data)
export const getSection = (id) => api.get(`/sections/${id}`)
export const updateSection = (id, data) => api.put(`/sections/${id}`, data)
export const getActiveWeeks = (sectionId) => api.get(`/sections/${sectionId}/active-weeks`)
export const updateActiveWeeks = (sectionId, data) => api.put(`/sections/${sectionId}/active-weeks`, data)
export const inviteStudents = (sectionId, data) => api.post(`/sections/${sectionId}/invite-students`, data)
export const inviteInstructors = (sectionId, data) => api.post(`/sections/${sectionId}/invite-instructors`, data)

// Teams
export const getTeams = (params) => api.get('/teams', { params })
export const createTeam = (data) => api.post('/teams', data)
export const getTeam = (id) => api.get(`/teams/${id}`)
export const updateTeam = (id, data) => api.put(`/teams/${id}`, data)
export const deleteTeam = (id) => api.delete(`/teams/${id}`)
export const assignStudents = (teamId, studentIds) => api.post(`/teams/${teamId}/students`, { studentIds })
export const removeStudentFromTeam = (teamId, studentId) => api.delete(`/teams/${teamId}/students/${studentId}`)
export const assignInstructors = (teamId, instructorIds) => api.post(`/teams/${teamId}/instructors`, { instructorIds })
export const removeInstructorFromTeam = (teamId, instructorId) => api.delete(`/teams/${teamId}/instructors/${instructorId}`)

// Students
export const getStudents = (params) => api.get('/students', { params })
export const getStudent = (id) => api.get(`/students/${id}`)
export const deleteStudent = (id) => api.delete(`/students/${id}`)

// Instructors
export const getInstructors = (params) => api.get('/instructors', { params })
export const getInstructor = (id) => api.get(`/instructors/${id}`)
export const deactivateInstructor = (id) => api.put(`/instructors/${id}/deactivate`)
export const reactivateInstructor = (id) => api.put(`/instructors/${id}/reactivate`)

// Rubrics
export const getRubrics = () => api.get('/rubrics')
export const getRubric = (id) => api.get(`/rubrics/${id}`)
export const createRubric = (data) => api.post('/rubrics', data)

// WARs
export const getStudentWARs = (studentId) => api.get(`/students/${studentId}/wars`)
export const getWAR = (studentId, weekId) => api.get(`/students/${studentId}/wars/${weekId}`)
export const addActivity = (studentId, weekId, data) => api.post(`/students/${studentId}/wars/${weekId}/activities`, data)
export const updateActivity = (id, data) => api.put(`/activities/${id}`, data)
export const deleteActivity = (id) => api.delete(`/activities/${id}`)
export const getTeamWARReport = (teamId, weekId) => api.get(`/teams/${teamId}/war-report`, { params: { weekId } })
export const getStudentWARReport = (studentId, weekIds) => api.get(`/students/${studentId}/war-report`, { params: { weekIds } })

// Peer Evaluations
export const submitPeerEvaluation = (evaluatorId, data) => api.post(`/students/${evaluatorId}/peer-evaluations`, data)
export const getStudentPeerReport = (studentId, weekId) => api.get(`/students/${studentId}/peer-evaluation-report`, { params: { weekId } })
export const getSectionPeerReport = (sectionId, weekId) => api.get(`/sections/${sectionId}/peer-evaluation-report`, { params: { weekId } })
export const getStudentPeerPeriodReport = (studentId, weekIds) => api.get(`/students/${studentId}/peer-evaluation-report/period`, { params: { weekIds } })
export const getSubmittedEvaluations = (evaluatorId, weekId) => api.get(`/students/${evaluatorId}/submitted-evaluations`, { params: { weekId } })

export default api
