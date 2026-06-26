<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import GradingModal from '../components/GradingModal.vue'
import { toastError, toastSuccess } from '../utils/toast'

const appeals = ref([])
const loading = ref(false)
const userId = ref(1)

const grading = ref({ visible: false, appeal: null, action: '' })

const fetchAppeals = async () => {
  loading.value = true
  try {
    const resp = await fetch(`/api/appeals?teacherId=${userId.value}`)
    const result = await resp.json()
    if (result.success) appeals.value = result.data
  } catch (e) {
    console.error(e)
  } finally { loading.value = false }
}

const openGrading = (appeal, action) => {
  grading.value = {
    visible: true,
    appeal,
    action,
    defaultScore: appeal.originalScore ?? '',
    defaultComment: ''
  }
}

const handleAppealSubmit = async ({ score, comment }) => {
  const { appeal, action } = grading.value
  if (!appeal) return
  try {
    const resp = await fetch('/api/appeals', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        appealId: appeal.appealId,
        status: action,
        handlerId: userId.value,
        handlerResponse: comment || '',
        adjustedScore: action === 'accepted' ? score : null
      })
    })
    const result = await resp.json()
    if (result.success) {
      toastSuccess(result.message || '处理完成')
      grading.value.visible = false
      fetchAppeals()
    } else {
      toastError(result.message || '操作失败')
    }
  } catch (e) { toastError('操作失败') }
}

onMounted(() => {
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  userId.value = user.userId || 1
  fetchAppeals()
})
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <h2 class="page-title">申诉管理</h2>
        </div>

        <div v-if="loading" class="empty-state">
           <div class="empty-icon">⏳</div>
           <p>加载中...</p>
        </div>
        <div v-else-if="appeals.length === 0" class="empty-state">
           <div class="empty-icon">📋</div>
           <p>暂无待处理申诉</p>
        </div>

        <div class="appeal-list" v-else>
          <div v-for="a in appeals" :key="a.appealId" class="card appeal-card" :class="a.status">
            <div class="appeal-header">
              <span class="student-name">{{ a.studentName || '学生' + a.studentId }}</span>
              <span class="badge badge-neutral">{{ a.courseName }}</span>
              <span class="badge badge-neutral">{{ a.assignmentTitle }}</span>
              <span class="orig-score">原互评分数: {{ a.originalScore }}分</span>
              <span :class="['badge', a.status === 'pending' ? 'badge-warning' : a.status === 'accepted' ? 'badge-success' : 'badge-neutral']">
                {{ a.status === 'pending' ? '待处理' : a.status === 'accepted' ? '已同意' : '已驳回' }}
              </span>
            </div>
            
            <div class="appeal-content">
              <p class="reason"><span class="label">申诉理由：</span>{{ a.reason }}</p>
            </div>

            <div v-if="a.status !== 'pending'" class="result-block">
              <p><span class="label">处理意见：</span>{{ a.handlerResponse || '无说明' }}</p>
              <p v-if="a.adjustedScore"><span class="label">调整后分数：</span><span class="score-highlight">{{ a.adjustedScore }}分</span></p>
            </div>
            
            <div v-if="a.status === 'pending'" class="actions">
              <button class="btn btn-secondary btn-sm" @click="openGrading(a, 'rejected')">驳回申诉</button>
              <button class="btn btn-primary btn-sm" style="background-color: var(--secondary-color);" @click="openGrading(a, 'accepted')">同意并调分</button>
            </div>
          </div>
        </div>
      </main>
    </div>
    <GradingModal :visible="grading.visible" :title="grading.action === 'accepted' ? '同意申诉并调分' : '驳回申诉'" :defaultScore="grading.defaultScore" :defaultComment="grading.defaultComment" @close="grading.visible = false" @submit="handleAppealSubmit" />
  </div>
</template>

<style scoped>
.app-container { display: flex; flex-direction: column; height: 100vh; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.content-area { flex: 1; background-color: var(--bg-app); padding: 2rem 3rem; overflow-y: auto; }

.header-action { margin-bottom: 2rem; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

.appeal-list { display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 3rem; }
.appeal-card { padding: 1.5rem; transition: all var(--transition-base); }
.appeal-card.pending { border-left: 4px solid #f59e0b; } /* amber 500 */
.appeal-card.accepted { border-left: 4px solid var(--secondary-color); opacity: 0.9; }
.appeal-card.rejected { border-left: 4px solid var(--text-tertiary); opacity: 0.8; }

.appeal-header { display: flex; gap: 1rem; align-items: center; margin-bottom: 1.25rem; flex-wrap: wrap; }
.student-name { font-weight: 600; font-size: 1.125rem; color: var(--text-primary); }
.orig-score { color: var(--text-secondary); font-size: 0.875rem; margin-left: auto; }

.appeal-content { margin-bottom: 1.25rem; }
.reason { margin: 0; font-size: 0.9375rem; line-height: 1.6; color: var(--text-primary); }
.label { font-weight: 600; color: var(--text-secondary); }

.result-block { 
  background-color: #f9fafb; 
  padding: 1rem; 
  border-radius: var(--radius-md); 
  font-size: 0.875rem; 
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.result-block p { margin: 0; }
.score-highlight { color: var(--secondary-color); font-weight: 700; font-size: 1.125rem; }

.actions { display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px dashed var(--border-light); padding-top: 1rem; }
.btn-sm { padding: 0.4rem 1rem; font-size: 0.875rem; }

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border-base);
  color: var(--text-secondary);
}
.empty-icon { font-size: 2.5rem; margin-bottom: 0.5rem; opacity: 0.6; }
</style>
