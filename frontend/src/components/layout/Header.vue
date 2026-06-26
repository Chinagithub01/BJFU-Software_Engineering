<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { toastError, toastSuccess, toastWarning } from '../../utils/toast'
import { clearAuth } from '../../utils/auth'

const router = useRouter()
const showInviteModal = ref(false)
const showDropdown = ref(false)
const inviteCode = ref('')
const nickname = ref('')
const userRole = ref('student')

onMounted(() => {
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  nickname.value = user.nickname || user.username || '用户'
  userRole.value = user.role || 'student'
  document.addEventListener('click', closeDropdown)
})
onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})

const closeDropdown = () => { showDropdown.value = false }

const handleLogout = () => {
  clearAuth()
  router.push('/login')
}

const handleJoinCourse = async () => {
  if (!inviteCode.value) {
    toastWarning('请输入邀请码')
    return
  }

  const userStr = sessionStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  const userId = user ? user.userId : 1

  try {
    const response = await fetch('/api/courses/join', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        courseCode: inviteCode.value,
        userId: userId
      })
    })
    const result = await response.json()
    if (result.success) {
      toastSuccess(result.message || '加入成功')
      showInviteModal.value = false
      inviteCode.value = ''
      window.dispatchEvent(new Event('course-joined'))
    } else {
      toastError(result.message || '加入失败')
    }
  } catch (error) {
    console.error(error)
    toastError('请求失败，请检查后端服务是否启动')
  }
}
</script>

<template>
  <header class="header">
    <div class="header-left">
      <span class="site-title">PeerReview 互评平台</span>
    </div>
    <div class="header-right">
      <a
        v-if="userRole === 'student' || userRole === 'ta'"
        href="#"
        class="invite-link"
        @click.prevent="showInviteModal = true"
      >输入邀请码</a>
      <div class="user-profile" @click.stop="showDropdown = !showDropdown">
        <div class="avatar-small">{{ nickname.charAt(0) }}</div>
        <span class="username">{{ nickname }} ▾</span>
        <div v-if="showDropdown" class="dropdown-menu fade-in" @click.stop>
          <div class="dropdown-item info">
            <strong>{{ nickname }}</strong>
            <small>{{ userRole === 'admin' ? '管理员' : userRole === 'teacher' ? '教师' : userRole === 'ta' ? '助教' : '学生' }}</small>
          </div>
          <div class="dropdown-item" @click="router.push('/profile')">个人资料</div>
          <div class="dropdown-item logout" @click="handleLogout">退出登录</div>
        </div>
      </div>
    </div>
  </header>

  <!-- 输入邀请码弹窗 -->
  <Teleport to="body">
    <div v-if="showInviteModal" class="modal-overlay" @click.self="showInviteModal = false">
      <div class="modal-content">
        <h3 class="modal-title">加入课程</h3>
        <div class="form-group">
          <label class="form-label">邀请码 (课程代码)</label>
          <input v-model="inviteCode" class="form-input" type="text" placeholder="例如：SE-2026" />
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showInviteModal = false">取消</button>
          <button class="btn btn-primary" @click="handleJoinCourse">确认加入</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.header {
  height: 64px;
  background-color: var(--primary-color);
  border-bottom: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  color: #ffffff;
  box-shadow: var(--shadow-md);
  z-index: 10;
}

.header-left .site-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.025em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.invite-link {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.875rem;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.invite-link:hover {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.15);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
}
.user-profile:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.avatar-small {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background-color: #ffffff;
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: 700;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.username {
  font-size: 0.875rem;
  font-weight: 500;
  color: #ffffff;
}

.user-profile { position: relative; }
.dropdown-menu {
  position: absolute; 
  top: 100%; 
  right: 0; 
  margin-top: 8px;
  background: var(--bg-surface); 
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg); 
  border: 1px solid var(--border-light);
  min-width: 200px; 
  z-index: 200;
  overflow: hidden;
  color: var(--text-primary);
}

.dropdown-item { 
  padding: 12px 16px; 
  cursor: pointer; 
  font-size: 0.875rem; 
  color: var(--text-primary); 
}
.dropdown-item:not(.info):hover { 
  background-color: var(--bg-surface-hover); 
}
.dropdown-item.info { 
  cursor: default; 
  border-bottom: 1px solid var(--border-light);
  background-color: #f9fafb;
}
.dropdown-item.info strong {
  display: block;
  font-weight: 600;
}
.dropdown-item.info small { 
  color: var(--text-secondary); 
  display: block; 
  margin-top: 2px;
}
.dropdown-item.logout { 
  color: var(--danger-color); 
  border-top: 1px solid var(--border-light);
}
.dropdown-item.logout:hover {
  background-color: #fef2f2;
}
</style>
