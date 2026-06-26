<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { toastError, toastSuccess } from '../utils/toast'
const router = useRouter()
const userRole = ref('student')
const activeTab = ref('我学的课')

onMounted(() => {
  const userStr = sessionStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userRole.value = user.role
    if (user.role === 'admin') {
      activeTab.value = '平台管理'
    } else if (user.role === 'teacher') {
      activeTab.value = '我教的课'
    } else {
      activeTab.value = '我学的课'
    }
  }
  fetchCourses()
  window.addEventListener('course-joined', fetchCourses)
})
onUnmounted(() => {
  window.removeEventListener('course-joined', fetchCourses)
})

const showAddModal = ref(false)
const newCourse = ref({
  courseName: '',
  courseCode: '',
  semester: '',
  description: ''
})
const courses = ref([])
const loading = ref(false)
const submitting = ref(false)

const handleLogout = () => {
  sessionStorage.removeItem('user')
  router.push('/login')
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const userStr = sessionStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    if (!user) return

    const response = await fetch(`/api/courses?userId=${user.userId}&role=${user.role}`)
    const result = await response.json()
    if (result.success) {
      courses.value = result.data
    }
  } catch (e) {
    console.error('获取课程列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleAddCourse = async () => {
  if (!newCourse.value.courseName.trim()) return toastError('请填写课程名称')
  if (!newCourse.value.courseCode.trim()) return toastError('请填写课程代码')
  if (!newCourse.value.semester.trim()) return toastError('请填写学期')
  if (submitting.value) return
  submitting.value = true
  try {
    const userStr = sessionStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const response = await fetch('/api/courses', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        courseName: newCourse.value.courseName,
        courseCode: newCourse.value.courseCode,
        semester: newCourse.value.semester,
        description: newCourse.value.description,
        teacherId: user ? user.userId : 1
      })
    })
    const result = await response.json()
    if (result.success) {
      toastSuccess('课程创建成功')
      showAddModal.value = false
      newCourse.value = { courseName: '', courseCode: '', semester: '', description: '' }
      await fetchCourses()
    } else {
      toastError(result.message || '添加失败')
    }
  } catch (e) {
    console.error('添加课程失败', e)
    toastError('请求后端失败，请检查 Tomcat 是否启动')
  } finally {
    submitting.value = false
  }
}

</script>

<template>
  <div class="app-container">
    <Header @logout="handleLogout" />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <!-- 页面标题区域 -->
        <div class="page-header">
          <h1 class="page-title">课程控制台</h1>
          <p class="page-subtitle">管理您的所有课程和进度</p>
        </div>

        <!-- 顶部标签栏 (Tabs) -->
        <div class="tabs-container">
          <div v-if="userRole === 'admin'" class="tab active">
            平台管理 <div class="indicator"></div>
          </div>
          <div v-if="userRole === 'student' || userRole === 'ta'"
            :class="['tab', { active: activeTab === '我学的课' }]"
            @click="activeTab = '我学的课'"
          >
            我学的课
          </div>
          <div v-if="userRole === 'teacher'"
            :class="['tab', { active: activeTab === '我教的课' }]"
            @click="activeTab = '我教的课'"
          >
            我教的课
          </div>
        </div>

        <div v-if="userRole === 'teacher' && activeTab === '我教的课'" class="action-bar">
          <div class="action-left">
            <button class="btn btn-primary" @click.stop="showAddModal = true">
              <span style="margin-right: 6px;">+</span> 添加课程
            </button>
          </div>
        </div>

        <!-- 课程卡片网格 (Course Grid) -->
        <div class="course-grid">
          <div v-for="course in courses" :key="course.courseId" class="card course-card card-hoverable" @click="router.push('/course/' + course.courseId)">
            <div class="cover-image" :style="course.courseId % 2 === 1 ? 'background: linear-gradient(135deg, var(--primary-light), var(--primary-color))' : 'background: linear-gradient(135deg, #d1fae5, var(--secondary-color))'">
              <div class="course-code-badge">{{ course.courseCode }}</div>
            </div>
            <div class="card-content">
              <h3 class="course-title">{{ course.courseName }}</h3>
              <div class="course-meta">
                <span class="meta-item">📅 {{ course.semester }}</span>
              </div>
              <p class="course-desc">{{ course.description || '暂无详细描述...' }}</p>
            </div>
          </div>
          <!-- 空状态提示 -->
          <div v-if="courses.length === 0" class="empty-state">
            <div class="empty-icon">📚</div>
            <p>暂无课程数据</p>
            <span v-if="userRole === 'teacher' && activeTab === '我教的课'">请点击上方“+ 添加课程”按钮创建</span>
            <span v-else-if="userRole === 'student' || userRole === 'ta'">请点击右上角“输入邀请码”加入课程</span>
          </div>
        </div>
      </main>
    </div>
    <!-- 添加课程的弹窗 -->
    <Teleport to="body">
      <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
        <div class="modal-content">
          <h3 class="modal-title">添加新课程</h3>
          <div class="form-group">
            <label class="form-label">课程名称</label>
            <input v-model="newCourse.courseName" class="form-input" type="text" placeholder="例如：软件工程" />
          </div>
          <div class="form-group">
            <label class="form-label">课程代码</label>
            <input v-model="newCourse.courseCode" class="form-input" type="text" placeholder="例如：SE-2026-02（全局唯一，不可重复）" />
          </div>
          <div class="form-group">
            <label class="form-label">学期</label>
            <input v-model="newCourse.semester" class="form-input" type="text" placeholder="例如：2026春" />
          </div>
          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="newCourse.description" class="form-input" rows="3" placeholder="课程简介..."></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
            <button class="btn btn-primary" @click="handleAddCourse" :disabled="submitting">
              {{ submitting ? '添加中...' : '确认添加' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.main-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.content-area {
  flex: 1;
  background-color: var(--bg-app);
  overflow-y: auto;
  padding: 2rem 3rem;
}

.page-header {
  margin-bottom: 2rem;
}
.page-title {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}
.page-subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
}

/* Tabs 样式 - 现代胶囊设计 */
.tabs-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background-color: var(--border-light);
  padding: 0.25rem;
  border-radius: var(--radius-lg);
  width: fit-content;
}
.tab {
  padding: 0.5rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}
.tab:hover {
  color: var(--text-primary);
}
.tab.active {
  background-color: var(--bg-surface);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  font-weight: 600;
}

/* 操作工具栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

/* 课程网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  padding-bottom: 3rem;
}

.course-card {
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.cover-image {
  height: 160px;
  position: relative;
  padding: 1rem;
}

.course-code-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-primary);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.card-content {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.course-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.meta-item {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-top: auto;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border-base);
  color: var(--text-secondary);
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  font-size: 1.125rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.empty-state span {
  font-size: 0.875rem;
}
</style>
