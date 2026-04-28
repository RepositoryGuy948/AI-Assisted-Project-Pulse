import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  { path: '/register/student', component: () => import('@/views/RegisterStudentView.vue'), meta: { public: true } },
  { path: '/register/instructor', component: () => import('@/views/RegisterInstructorView.vue'), meta: { public: true } },

  // Admin
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/sections' },
      { path: 'sections', component: () => import('@/views/admin/SectionsView.vue') },
      { path: 'sections/:id', component: () => import('@/views/admin/SectionDetailView.vue') },
      { path: 'sections/:id/active-weeks', component: () => import('@/views/admin/ActiveWeeksView.vue') },
      { path: 'teams', component: () => import('@/views/admin/TeamsView.vue') },
      { path: 'teams/:id', component: () => import('@/views/admin/TeamDetailView.vue') },
      { path: 'students', component: () => import('@/views/admin/StudentsView.vue') },
      { path: 'students/:id', component: () => import('@/views/admin/StudentDetailView.vue') },
      { path: 'instructors', component: () => import('@/views/admin/InstructorsView.vue') },
      { path: 'instructors/:id', component: () => import('@/views/admin/InstructorDetailView.vue') },
      { path: 'rubrics', component: () => import('@/views/admin/RubricsView.vue') },
      { path: 'rubrics/new', component: () => import('@/views/admin/RubricFormView.vue') },
      { path: 'rubrics/:id/edit', component: () => import('@/views/admin/RubricFormView.vue') },
      { path: 'invite/students', component: () => import('@/views/admin/InviteStudents.vue') },
      { path: 'invite/instructors', component: () => import('@/views/admin/InviteInstructors.vue') },
    ],
  },

  // Student
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { roles: ['STUDENT'] },
    children: [
      { path: '', redirect: '/student/dashboard' },
      { path: 'dashboard', component: () => import('@/views/student/DashboardView.vue') },
      { path: 'account', component: () => import('@/views/student/AccountView.vue') },
      { path: 'war/:weekId?', component: () => import('@/views/student/WARView.vue') },
      { path: 'peer-evaluation', component: () => import('@/views/student/PeerEvaluationView.vue') },
      { path: 'my-report', component: () => import('@/views/student/MyReportView.vue') },
      { path: 'team-war-report', component: () => import('@/views/student/TeamWARReportView.vue') },
    ],
  },

  // Instructor
  {
    path: '/instructor',
    component: () => import('@/layouts/InstructorLayout.vue'),
    meta: { roles: ['INSTRUCTOR'] },
    children: [
      { path: '', redirect: '/instructor/dashboard' },
      { path: 'dashboard', component: () => import('@/views/instructor/DashboardView.vue') },
      { path: 'teams', component: () => import('@/views/instructor/TeamsView.vue') },
      { path: 'teams/:id', component: () => import('@/views/instructor/TeamDetailView.vue') },
      { path: 'students', component: () => import('@/views/instructor/StudentsView.vue') },
      { path: 'students/:id', component: () => import('@/views/instructor/StudentDetailView.vue') },
      { path: 'reports/peer-evaluation/section', component: () => import('@/views/instructor/PeerEvalSectionReport.vue') },
      { path: 'reports/peer-evaluation/student/:id', component: () => import('@/views/instructor/PeerEvalStudentReport.vue') },
      { path: 'reports/war/team/:id', component: () => import('@/views/instructor/WARTeamReport.vue') },
      { path: 'reports/war/student/:id', component: () => import('@/views/instructor/WARStudentReport.vue') },
    ],
  },

  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  if (to.meta.public) return next()

  // No token or no user object → go to login
  if (!auth.isAuthenticated || !auth.user) {
    auth.logout()
    return next('/')
  }

  const requiredRoles = to.meta.roles
  if (requiredRoles && !requiredRoles.includes(auth.user.role)) {
    if (auth.isAdmin) return next('/admin')
    if (auth.isInstructor) return next('/instructor')
    if (auth.isStudent) return next('/student')
    return next('/')
  }

  next()
})

export default router
