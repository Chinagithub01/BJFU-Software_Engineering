import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { getToken } from './utils/auth'
import './assets/main.css'

const nativeFetch = window.fetch.bind(window)
window.fetch = (input, init = {}) => {
  const url = typeof input === 'string' ? input : input.url
  const token = getToken()
  if (token && url.includes('/api/') && !url.includes('/api/submissions/download')) {
    const headers = new Headers(init.headers || {})
    if (!headers.has('Authorization')) {
      headers.set('Authorization', `Bearer ${token}`)
    }
    init = { ...init, headers }
  }
  return nativeFetch(input, init)
}

const app = createApp(App)
app.use(router)
app.mount('#app')
