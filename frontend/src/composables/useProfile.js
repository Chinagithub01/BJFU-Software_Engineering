import { ref } from 'vue'
import { toastError, toastSuccess } from '../utils/toast'

export function useProfile(options = {}) {
  const { includeSummary = true } = options

  const form = ref({
    userId: 0,
    username: '',
    nickname: '',
    phone: '',
    email: '',
    school: '',
    className: '',
    studentId: '',
    role: 'student'
  })
  const summary = ref({})
  const loading = ref(false)
  const saving = ref(false)

  const fetchProfile = async () => {
    loading.value = true
    try {
      const user = JSON.parse(sessionStorage.getItem('user') || '{}')
      const qs = includeSummary ? '&includeSummary=true' : ''
      const resp = await fetch(`/api/profile?userId=${user.userId}${qs}`)
      const result = await resp.json()
      if (result.success) {
        Object.assign(form.value, result.data)
        summary.value = result.data.summary || {}
      } else {
        toastError(result.message || '加载失败')
      }
    } catch (e) {
      console.error(e)
      toastError('加载个人资料失败')
    } finally {
      loading.value = false
    }
  }

  const saveProfile = async (payload) => {
    saving.value = true
    try {
      const body = payload || form.value
      const resp = await fetch('/api/profile', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      })
      const result = await resp.json()
      if (result.success) {
        toastSuccess(result.message || '保存成功')
        const user = JSON.parse(sessionStorage.getItem('user') || '{}')
        user.nickname = body.nickname
        sessionStorage.setItem('user', JSON.stringify(user))
      } else {
        toastError(result.message || '保存失败')
      }
      return result.success
    } catch (e) {
      toastError('保存失败')
      return false
    } finally {
      saving.value = false
    }
  }

  return { form, summary, loading, saving, fetchProfile, saveProfile }
}
