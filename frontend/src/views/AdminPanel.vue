<script setup>
import { ref, computed, onMounted } from 'vue'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { toastError, toastSuccess } from '../utils/toast'

const PAGE_SIZE = 10

const stats = ref({})
const users = ref([])
const courses = ref([])
const activeSection = ref('stats')
const loading = ref(false)
const userPage = ref(1)
const userTotal = ref(0)

const userTotalPages = computed(() => Math.max(1, Math.ceil(userTotal.value / PAGE_SIZE)))

const userPageNumbers = computed(() => {
  const pages = []
  for (let i = 1; i <= userTotalPages.value; i++) pages.push(i)
  return pages
})

const showEditUserModal = ref(false)
const editingUser = ref({ userId: 0, roleId: 4, isActive: 1 })

const fetchStats = async () => {
  const resp = await fetch('/api/admin/stats')
  const result = await resp.json()
  if (result.success) stats.value = result.data
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const resp = await fetch(`/api/admin/users?page=${userPage.value}&pageSize=${PAGE_SIZE}`)
    const result = await resp.json()
    if (result.success) {
      users.value = result.data || []
      userTotal.value = result.total ?? users.value.length
      if (userTotal.value > 0 && userPage.value > userTotalPages.value) {
        userPage.value = userTotalPages.value
        loading.value = false
        return fetchUsers()
      }
    }
  } finally { loading.value = false }
}

const goToUserPage = (page) => {
  const target = Math.min(Math.max(1, page), userTotalPages.value)
  if (target === userPage.value) return
  userPage.value = target
  fetchUsers()
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const resp = await fetch('/api/admin/courses')
    const result = await resp.json()
    if (result.success) courses.value = result.data
  } finally { loading.value = false }
}

const openEditUser = (u) => {
  editingUser.value = {
    userId: u.userId,
    roleId: u.roleName === 'teacher' ? 2 : (u.roleName === 'ta' ? 3 : 4),
    isActive: u.isActive
  }
  showEditUserModal.value = true
}

const submitEditUser = async () => {
  try {
    const resp = await fetch('/api/admin/users', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: editingUser.value.userId,
        roleId: editingUser.value.roleId,
        isActive: editingUser.value.isActive
      })
    })
    const result = await resp.json()
    if (result.success) {
      toastSuccess('用户已更新')
      showEditUserModal.value = false
      fetchUsers()
    } else {
      toastError(result.message || '更新失败')
    }
  } catch (e) {
    toastError('更新失败')
  }
}

const deleteUser = async (id, name) => {
  if (!confirm(`确定删除用户"${name}"？此操作不可撤销。`)) return
  try {
    const resp = await fetch(`/api/admin/users?id=${id}`, { method: 'DELETE' })
    const result = await resp.json()
    if (result.success) {
      toastSuccess('用户已删除')
      fetchUsers()
    } else {
      toastError(result.message || '删除失败')
    }
  } catch (e) {
    toastError('删除失败')
  }
}

const deleteCourse = async (id, name) => {
  if (!confirm(`确定删除课程"${name}"？此操作不可撤销。`)) return
  try {
    const resp = await fetch(`/api/admin/courses?id=${id}`, { method: 'DELETE' })
    const result = await resp.json()
    if (result.success) {
      toastSuccess('课程已删除')
      fetchCourses()
    } else {
      toastError(result.message || '删除失败')
    }
  } catch (e) {
    toastError('删除失败')
  }
}

const switchSection = (s) => {
  activeSection.value = s
  if (s === 'stats') fetchStats()
  else if (s === 'users') {
    userPage.value = 1
    fetchUsers()
  }
  else if (s === 'courses') fetchCourses()
}

onMounted(() => fetchStats())
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <h2 class="page-title">系统管理面板</h2>

        <div class="tabs-container">
          <div :class="['tab', { active: activeSection === 'stats' }]" @click="switchSection('stats')">平台概览</div>
          <div :class="['tab', { active: activeSection === 'users' }]" @click="switchSection('users')">用户管理</div>
          <div :class="['tab', { active: activeSection === 'courses' }]" @click="switchSection('courses')">课程管理</div>
        </div>

        <!-- 平台概览 -->
        <div v-if="activeSection === 'stats'" class="stats-grid">
          <div class="card stat-card"><span class="num">{{ stats.userCount || 0 }}</span><span class="label">总用户</span></div>
          <div class="card stat-card"><span class="num">{{ stats.studentCount || 0 }}</span><span class="label">学生</span></div>
          <div class="card stat-card"><span class="num">{{ stats.teacherCount || 0 }}</span><span class="label">教师</span></div>
          <div class="card stat-card"><span class="num">{{ stats.courseCount || 0 }}</span><span class="label">课程</span></div>
          <div class="card stat-card"><span class="num">{{ stats.assignmentCount || 0 }}</span><span class="label">作业</span></div>
          <div class="card stat-card"><span class="num">{{ stats.submissionCount || 0 }}</span><span class="label">提交</span></div>
          <div class="card stat-card"><span class="num">{{ stats.reviewCount || 0 }}</span><span class="label">互评</span></div>
          <div class="card stat-card warn"><span class="num">{{ stats.pendingAppeals || 0 }}</span><span class="label">待处理申诉</span></div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeSection === 'users'">
          <p v-if="userTotal > 0" class="section-hint">共 {{ userTotal }} 位用户</p>
          <div v-if="loading" class="empty-state">加载中...</div>
          <div v-else-if="userTotal === 0" class="empty-state">暂无用户</div>
          <template v-else>
            <div class="modern-table-container">
              <table class="modern-table">
                <thead><tr><th>ID</th><th>用户名</th><th>姓名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
                <tbody>
                  <tr v-for="u in users" :key="u.userId">
                    <td>{{ u.userId }}</td><td class="font-medium">{{ u.username }}</td><td>{{ u.realName }}</td>
                    <td>
                      <span :class="['badge', u.roleName === 'student' ? 'badge-success' : u.roleName === 'admin' ? 'badge-warning' : 'badge-primary']">
                        {{ u.roleName }}
                      </span>
                    </td>
                    <td>
                      <span :class="['badge', u.isActive === 1 ? 'badge-success' : 'badge-neutral']">{{ u.isActive === 1 ? '启用' : '禁用' }}</span>
                    </td>
                    <td>
                      <button class="btn btn-primary btn-sm" @click="openEditUser(u)">编辑</button>
                      <button class="btn btn-danger btn-sm" style="margin-left: 8px;" @click="deleteUser(u.userId, u.username)">删除</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-if="userTotalPages > 1" class="pagination">
              <button
                class="btn btn-secondary btn-sm"
                :disabled="userPage <= 1"
                @click="goToUserPage(userPage - 1)"
              >上一页</button>
              <div class="page-nums">
                <button
                  v-for="p in userPageNumbers"
                  :key="p"
                  :class="['btn', 'btn-sm', 'page-btn', { active: p === userPage }]"
                  @click="goToUserPage(p)"
                >{{ p }}</button>
              </div>
              <button
                class="btn btn-secondary btn-sm"
                :disabled="userPage >= userTotalPages"
                @click="goToUserPage(userPage + 1)"
              >下一页</button>
              <span class="page-info">第 {{ userPage }} / {{ userTotalPages }} 页</span>
            </div>
          </template>
        </div>

        <!-- 课程管理 -->
        <div v-if="activeSection === 'courses'">
          <div class="modern-table-container" v-if="courses.length > 0">
            <table class="modern-table">
              <thead><tr><th>ID</th><th>课程名</th><th>代码</th><th>教师</th><th>学生数</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="c in courses" :key="c.courseId">
                  <td>{{ c.courseId }}</td><td class="font-medium">{{ c.courseName }}</td><td>{{ c.courseCode }}</td>
                  <td>{{ c.teacherName }}</td><td>{{ c.studentCount }}</td>
                  <td><button class="btn btn-danger btn-sm" @click="deleteCourse(c.courseId, c.courseName)">删除</button></td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="empty-state">加载中...</p>
        </div>
      </main>
    </div>

    <!-- 编辑用户弹窗 -->
    <Teleport to="body">
      <div v-if="showEditUserModal" class="modal-overlay" @click.self="showEditUserModal = false">
        <div class="modal-content">
          <h3 class="modal-title">编辑用户</h3>
          <div class="form-group">
            <label class="form-label">角色身份</label>
            <select v-model="editingUser.roleId" class="form-input">
              <option :value="2">教师 (Teacher)</option>
              <option :value="3">助教 (TA)</option>
              <option :value="4">学生 (Student)</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">账户状态</label>
            <select v-model="editingUser.isActive" class="form-input">
              <option :value="1">启用 (Active)</option>
              <option :value="0">禁用 (Disabled)</option>
            </select>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showEditUserModal = false">取消</button>
            <button class="btn btn-primary" @click="submitEditUser">确认保存</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.app-container { display: flex; flex-direction: column; height: 100vh; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.content-area { flex: 1; background-color: var(--bg-app); overflow-y: auto; padding: 2rem 3rem; }

.page-title { margin: 0 0 2rem 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

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
.tab:hover { color: var(--text-primary); }
.tab.active {
  background-color: var(--bg-surface);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  font-weight: 600;
}

.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
.stat-card { padding: 1.5rem; text-align: center; display: flex; flex-direction: column; justify-content: center; }
.stat-card .num { display: block; font-size: 2.5rem; font-weight: 800; color: var(--primary-color); margin-bottom: 0.25rem; line-height: 1; }
.stat-card .label { color: var(--text-secondary); font-size: 0.875rem; font-weight: 500; }
.stat-card.warn { border-color: #fcd34d; background-color: #fffbeb; }
.stat-card.warn .num { color: #d97706; }
.stat-card.warn .label { color: #b45309; }

.font-medium { font-weight: 500; }
.btn-sm { padding: 0.4rem 0.75rem; font-size: 0.8125rem; }

.section-hint { margin: 0 0 1rem 0; font-size: 0.875rem; color: var(--text-secondary); }
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 1.5rem;
  padding-bottom: 1rem;
}
.page-nums { display: flex; gap: 0.35rem; }
.page-btn {
  min-width: 2.25rem;
  background: var(--bg-surface);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
}
.page-btn.active {
  background: var(--primary-color);
  color: #fff;
  border-color: var(--primary-color);
}
.page-info { font-size: 0.875rem; color: var(--text-secondary); }

.empty-state { text-align: center; color: var(--text-secondary); padding: 3rem; background: var(--bg-surface); border-radius: var(--radius-lg); border: 1px dashed var(--border-base); }
</style>