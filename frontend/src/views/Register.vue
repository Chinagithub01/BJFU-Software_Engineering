<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { toastError, toastSuccess } from '../utils/toast'

const router = useRouter()
const username = ref('')
const password = ref('')
const role = ref('student')
const submitting = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (submitting.value) return
  errorMsg.value = ''
  if (!username.value || !password.value) {
    errorMsg.value = '请完整填写信息'
    return
  }
  submitting.value = true
  try {
    const response = await fetch('/api/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value, role: role.value })
    })
    const result = await response.json()
    if (result.success) {
      toastSuccess('注册成功，请前往登录')
      router.push('/login')
    } else {
      errorMsg.value = result.message || '注册失败'
      toastError(errorMsg.value)
    }
  } catch (error) {
    errorMsg.value = '请求后端失败'
    toastError(errorMsg.value)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="card auth-card fade-in">
      <div class="auth-header">
        <h2 class="auth-title">注册账号</h2>
        <p class="auth-subtitle">加入课程作业互评平台</p>
      </div>

      <div v-if="errorMsg" class="error-banner" role="alert">{{ errorMsg }}</div>

      <div class="form-group">
        <label class="form-label">用户名</label>
        <input v-model="username" type="text" class="form-input" placeholder="设置用户名" />
      </div>
      <div class="form-group">
        <label class="form-label">密码</label>
        <input v-model="password" type="password" class="form-input" placeholder="设置密码" />
      </div>
      <div class="form-group">
        <label class="form-label">角色身份</label>
        <select v-model="role" class="form-input">
          <option value="student">学生</option>
          <option value="teacher">教师</option>
        </select>
      </div>

      <button class="btn btn-primary w-full mt-4" @click="handleRegister" :disabled="submitting">
        {{ submitting ? '注册中...' : '注 册' }}
      </button>

      <div class="auth-links">
        <a href="#" @click.prevent="router.push('/login')">已有账号？去登录</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--bg-app) 100%);
}
.auth-card {
  padding: 2.5rem 2rem;
  width: 100%;
  max-width: 420px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
}
.auth-header { text-align: center; margin-bottom: 1.75rem; }
.auth-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
}
.auth-subtitle { color: var(--text-secondary); margin-top: 0.5rem; }
.error-banner {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 0.65rem 0.85rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}
.w-full { width: 100%; }
.mt-4 { margin-top: 1rem; }
.auth-links { margin-top: 1.5rem; text-align: center; font-size: 0.875rem; }
</style>
