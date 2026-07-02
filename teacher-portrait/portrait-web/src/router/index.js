import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '../utils/token'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '首页仪表盘' }
      },
      // ========== 移动端项目入口页面 ==========
      {
        path: 'mobile/projects',
        name: 'MobileProjects',
        component: () => import('../views/mobile/MobileProjectsView.vue'),
        meta: { title: '项目中心' }
      },
      // ========== 科研项目 ==========
      {
        path: 'project/vertical',
        name: 'VerticalProject',
        component: () => import('../views/project/VerticalProjectView.vue'),
        meta: { title: '纵向项目' }
      },
      {
        path: 'project/horizontal',
        name: 'HorizontalProject',
        component: () => import('../views/project/HorizontalProjectView.vue'),
        meta: { title: '横向项目' }
      },
      // ========== 专利管理 ==========
      {
        path: 'patent',
        name: 'Patent',
        component: () => import('../views/patent/PatentView.vue'),
        meta: { title: '专利管理' }
      },
      // ========== 软著管理 ==========
      {
        path: 'software',
        name: 'Software',
        component: () => import('../views/software/SoftwareView.vue'),
        meta: { title: '软著管理' }
      },
      // ========== 论文管理 ==========
      {
        path: 'paper',
        name: 'Paper',
        component: () => import('../views/paper/PaperView.vue'),
        meta: { title: '论文管理' }
      },
      // ========== 竞赛指导 ==========
      {
        path: 'competition',
        name: 'Competition',
        component: () => import('../views/competition/CompetitionView.vue'),
        meta: { title: '竞赛指导' }
      },
      // ========== 数字画像 ==========
      {
        path: 'portrait/:userId?',
        name: 'Portrait',
        component: () => import('../views/portrait/PortraitView.vue'),
        meta: { title: '数字画像' }
      },
      // ========== 移动端管理入口页面 ==========
      {
        path: 'mobile/admin',
        name: 'MobileAdmin',
        component: () => import('../views/mobile/MobileAdminView.vue'),
        meta: { title: '系统管理', role: 'ADMIN' }
      },
      // ========== 系统管理（管理员） ==========
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('../views/admin/UserManageView.vue'),
        meta: { title: '用户管理', role: 'ADMIN' }
      },
      {
        path: 'admin/score-config',
        name: 'ScoreConfig',
        component: () => import('../views/admin/ScoreConfigView.vue'),
        meta: { title: '权重配置', role: 'ADMIN' }
      },
      {
        path: 'admin/import-export',
        name: 'ImportExport',
        component: () => import('../views/admin/ImportExportView.vue'),
        meta: { title: '导入导出', role: 'ADMIN' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 教师数字画像系统` : '教师数字画像系统'

  if (to.meta.noAuth) {
    next()
    return
  }

  const token = getToken()
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.role) {
    const user = getUser()
    if (!user || user.role !== to.meta.role) {
      next({ path: '/dashboard' })
      return
    }
  }

  next()
})

export default router