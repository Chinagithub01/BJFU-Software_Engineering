import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../utils/auth'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import CourseDetail from '../views/CourseDetail.vue'
import ReviewList from '../views/ReviewList.vue'
import ReviewForm from '../views/ReviewForm.vue'
import ReviewResults from '../views/ReviewResults.vue'
import Statistics from '../views/Statistics.vue'
import Profile from '../views/Profile.vue'
import SubmissionsView from '../views/SubmissionsView.vue'
import AdminPanel from '../views/AdminPanel.vue'
import Discussion from '../views/Discussion.vue'
import Appeals from '../views/Appeals.vue'
import SubmissionDetail from '../views/SubmissionDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/',
      name: 'Dashboard',
      component: Dashboard
    },
    {
      path: '/course/:id',
      name: 'CourseDetail',
      component: CourseDetail
    },
    {
      path: '/reviews',
      name: 'ReviewList',
      component: ReviewList
    },
    {
      path: '/review/:praId',
      name: 'ReviewForm',
      component: ReviewForm
    },
    {
      path: '/results',
      name: 'ReviewResults',
      component: ReviewResults
    },
    {
      path: '/statistics/:id',
      name: 'Statistics',
      component: Statistics
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile
    },
    {
      path: '/submissions/:id',
      name: 'SubmissionsView',
      component: SubmissionsView
    },
    {
      path: '/appeals',
      name: 'Appeals',
      component: Appeals
    },
    {
      path: '/submission-detail/:id',
      name: 'SubmissionDetail',
      component: SubmissionDetail
    },
    {
      path: '/admin',
      name: 'AdminPanel',
      component: AdminPanel
    },
    {
      path: '/discussion/:id',
      name: 'Discussion',
      component: Discussion
    }
  ]
})

// 路由守卫：登录 + 基础角色
router.beforeEach((to, from, next) => {
  const userStr = sessionStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  if (to.name !== 'Login' && to.name !== 'Register' && !isLoggedIn()) {
    next({ name: 'Login' })
    return
  }

  if (to.path === '/' && user && user.role === 'admin') {
    next({ name: 'AdminPanel' })
    return
  }

  const role = user?.role
  if (to.name === 'AdminPanel' && role !== 'admin') {
    next({ name: 'Dashboard' })
    return
  }
  if (to.name === 'Appeals' && role !== 'teacher' && role !== 'ta' && role !== 'admin') {
    next({ name: 'Dashboard' })
    return
  }

  next()
})

export default router
