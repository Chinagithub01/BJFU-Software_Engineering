<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { toast, toastError, toastSuccess } from '../utils/toast'

const route = useRoute()
const router = useRouter()
const assignmentId = route.params.id

const messages = ref([])
const newMsg = ref('')
const sending = ref(false)
const userId = ref(1)
const loading = ref(false)

const fetchMessages = async () => {
  loading.value = true
  try {
    const resp = await fetch(
      `/api/discussions?assignmentId=${assignmentId}&studentId=${userId.value}`
    )
    const result = await resp.json()
    if (result.success) messages.value = result.data
    else toastError(result.message || '加载失败')
  } catch (e) {
    console.error(e)
    toastError('无法加载讨论区')
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMsg.value.trim()) return
  sending.value = true
  try {
    const resp = await fetch('/api/discussions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        assignmentId: parseInt(assignmentId),
        studentId: userId.value,
        content: newMsg.value,
      }),
    })
    const result = await resp.json()
    if (result.success) {
      newMsg.value = ''
      toastSuccess('消息已发送')
      await fetchMessages()
      nextTick(() => {
        const el = document.querySelector('.msg-list')
        if (el) el.scrollTop = el.scrollHeight
      })
    } else {
      toastError(result.message)
    }
  } catch (e) {
    toastError('发送失败')
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  userId.value = user.userId || 1
  fetchMessages()
})
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.back()">← 返回</button>
          <h2 class="page-title">匿名讨论区</h2>
          <span class="hint badge badge-neutral">仅本作业提交者可参与</span>
        </div>

        <div v-if="loading" class="empty-state">
          <div class="empty-icon">⏳</div>
          <p>加载中...</p>
        </div>
        <div v-else class="discussion-container">
          <div class="card msg-list">
            <div v-if="messages.length === 0" class="empty-msg">
              <span class="empty-icon">💬</span>
              <p>暂无讨论，发起第一条消息吧</p>
            </div>
            <div
              v-for="m in messages"
              :key="m.discussionId"
              class="msg-item"
              :class="{ 'is-mine': m.isMine }"
            >
              <div class="msg-header">
                <span class="msg-author">{{ m.anonymousName }}{{ m.isMine ? '（我）' : '' }}</span>
                <span class="msg-time">{{ new Date(m.createdAt).toLocaleString() }}</span>
              </div>
              <p class="msg-content">{{ m.content }}</p>
            </div>
          </div>
          <div class="msg-input-bar">
            <input
              v-model="newMsg"
              class="form-input"
              type="text"
              placeholder="输入消息参与讨论…"
              maxlength="500"
              @keyup.enter="sendMessage"
            />
            <button class="btn btn-primary" @click="sendMessage" :disabled="sending || !newMsg.trim()">
              {{ sending ? '发送中' : '发送' }}
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; flex-wrap: wrap; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

.discussion-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  min-height: 400px;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  margin-bottom: 1rem;
  min-height: 360px;
  display: flex;
  flex-direction: column;
}

.msg-item {
  margin-bottom: 1.25rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  background: var(--bg-app);
}
.msg-item.is-mine {
  background: var(--primary-light);
  border-left: 3px solid var(--primary-color);
}

.msg-header { display: flex; align-items: baseline; gap: 1rem; margin-bottom: 0.35rem; flex-wrap: wrap; }
.msg-author { font-weight: 600; color: var(--primary-color); font-size: 0.9375rem; }
.msg-time { font-size: 0.8125rem; color: var(--text-tertiary); }
.msg-content { margin: 0; line-height: 1.6; color: var(--text-primary); font-size: 0.9375rem; white-space: pre-wrap; }

.msg-input-bar { display: flex; gap: 0.75rem; align-items: center; }

.empty-msg { text-align: center; color: var(--text-tertiary); padding: 3rem 0; margin: auto; }
.empty-icon { font-size: 2.5rem; margin-bottom: 0.5rem; opacity: 0.6; display: block; }
.btn-sm { padding: 0.4rem 1rem; font-size: 0.875rem; }
</style>
