<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const nickname = ref('')
const userRole = ref('student')

const roleLabels = {
  student: '学生',
  teacher: '教师',
  ta: '助教',
  admin: '管理员',
}

const allMenuItems = [
  { id: 'home', icon: '🏠', label: '课程工作台', path: '/' },
  { id: 'reviews', icon: '📝', label: '互评任务', path: '/reviews', roles: ['student'] },
  { id: 'results', icon: '📊', label: '互评结果', path: '/results', roles: ['student'] },
  { id: 'appeals', icon: '📋', label: '申诉管理', path: '/appeals', roles: ['teacher', 'ta'] },
  { id: 'profile', icon: '👤', label: '个人资料', path: '/profile' },
  { id: 'admin', icon: '⚙️', label: '系统管理', path: '/admin', roles: ['admin'] },
]

const menuItems = ref([])

const roleLabel = computed(() => roleLabels[userRole.value] || '用户')

const isActive = (item) => {
  if (item.path === '/') return route.path === '/'
  return route.path.startsWith(item.path)
}

onMounted(() => {
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  nickname.value = user.nickname || user.username || '用户'
  userRole.value = user.role || 'student'

  let items = allMenuItems.filter(
    (m) => !m.roles || m.roles.includes(userRole.value)
  )

  if (userRole.value === 'admin') {
    items = items.filter((m) => m.id !== 'home')
    const adminMenu = items.find((m) => m.id === 'admin')
    if (adminMenu) {
      adminMenu.label = '管理首页'
      adminMenu.icon = '🏠'
    }
  }

  menuItems.value = items
})
</script>

<template>
  <aside class="sidebar">
    <div class="user-info">
      <div class="avatar-large">{{ nickname.charAt(0) }}</div>
      <div class="username-large">{{ nickname }}</div>
      <span class="role-badge">{{ roleLabel }}</span>
    </div>

    <ul class="menu-list">
      <li
        v-for="item in menuItems"
        :key="item.id + item.path"
        :class="['menu-item', { active: isActive(item) }]"
        @click="router.push(item.path)"
      >
        <span class="menu-icon">{{ item.icon }}</span>
        <span class="menu-label">{{ item.label }}</span>
      </li>
    </ul>

    <div class="sidebar-foot">
      <span class="foot-text">PeerReview</span>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 260px;
  background-color: var(--sidebar-bg);
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-light);
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 0 20px;
}

.avatar-large {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background-color: var(--primary-light);
  color: var(--primary-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 10px;
  box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2);
  border: 2px solid #ffffff;
}

.username-large {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.role-badge {
  margin-top: 0.35rem;
  font-size: 0.75rem;
  padding: 0.15rem 0.6rem;
  border-radius: var(--radius-full);
  background: var(--primary-light);
  color: var(--primary-hover);
  font-weight: 500;
}

.menu-list {
  flex: 1;
  padding: 12px 16px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 6px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
  font-weight: 500;
}

.menu-item:hover {
  background-color: var(--primary-light);
  color: var(--primary-hover);
}

.menu-item.active {
  background-color: var(--primary-color);
  color: #ffffff;
  font-weight: 600;
  box-shadow: var(--shadow-md);
}

.menu-icon {
  margin-right: 12px;
  font-size: 1.25rem;
}

.sidebar-foot {
  padding: 12px 16px 20px;
  text-align: center;
}

.foot-text {
  font-size: 0.7rem;
  color: var(--text-tertiary);
  letter-spacing: 0.05em;
}
</style>
