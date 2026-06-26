<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { toast, toastError, toastSuccess } from '../utils/toast'
import { setAuth } from '../utils/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const submitting = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (submitting.value) return
  errorMsg.value = ''
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  submitting.value = true
  try {
    const response = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value }),
    })
    const result = await response.json()
    if (result.success) {
      setAuth(result.data.token, result.data.user)
      toastSuccess('登录成功')
      router.push('/')
    } else {
      errorMsg.value = result.message || '登录失败'
    }
  } catch (error) {
    errorMsg.value = '无法连接服务器，请确认 Tomcat 已启动'
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
        <div class="brand-mark">PR</div>
        <h2 class="auth-title">PeerReview</h2>
        <p class="auth-subtitle">课程作业互评平台</p>
      </div>

      <div v-if="errorMsg" class="error-banner" role="alert">{{ errorMsg }}</div>

      <div class="form-group">
        <label class="form-label">用户名</label>
        <input v-model="username" type="text" class="form-input" placeholder="请输入用户名" autocomplete="username" />
      </div>
      <div class="form-group">
        <label class="form-label">密码</label>
        <input
          v-model="password"
          type="password"
          class="form-input"
          placeholder="请输入密码"
          autocomplete="current-password"
          @keyup.enter="handleLogin"
        />
      </div>

      <button class="btn btn-primary w-full mt-4" @click="handleLogin" :disabled="submitting">
        {{ submitting ? '登录中...' : '登 录' }}
      </button>

      <div class="auth-links">
        <a href="#" @click.prevent="router.push('/register')">没有账号？去注册</a>
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
.brand-mark {
  width: 3rem;
  height: 3rem;
  margin: 0 auto 0.75rem;
  border-radius: var(--radius-lg);
  background: var(--primary-color);
  color: white;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}
.auth-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.025em;
}
.auth-subtitle { color: var(--text-secondary); margin-top: 0.35rem; font-size: 0.875rem; }
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
