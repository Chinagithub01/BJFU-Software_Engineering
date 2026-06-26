<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'

const router = useRouter()
const reviews = ref([])
const loading = ref(false)
const userId = ref(1)

const fetchReviews = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/reviews/assign?reviewerId=${userId.value}`)
    const result = await response.json()
    if (result.success) {
      reviews.value = result.data
    }
  } catch (e) {
    console.error('获取互评列表失败', e)
  } finally {
    loading.value = false
  }
}

const goReview = (praId) => {
  router.push(`/review/${praId}`)
}

const handleLogout = () => {
  sessionStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  const userStr = sessionStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userId.value = user.userId || 1
  }
  fetchReviews()
})
</script>

<template>
  <div class="app-container">
    <Header @logout="handleLogout" />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push('/')">← 返回</button>
          <h2 class="page-title">我的互评任务</h2>
        </div>

        <div class="tabs-container">
          <div class="tab active">全部互评</div>
        </div>

        <div class="list-container">
          <div v-if="loading" class="empty-state">
             <div class="empty-icon">⏳</div>
             <p>加载中...</p>
          </div>
          <div v-else-if="reviews.length === 0" class="empty-state">
            <div class="empty-icon">📝</div>
            <p>暂无互评任务</p>
          </div>
          <div v-for="item in reviews" :key="item.praId" class="card list-card">
            <div class="card-header">
              <h3 class="assignment-title">{{ item.assignmentTitle || '作业文件' }}</h3>
              <div class="header-badges">
                <span class="badge badge-primary">{{ item.courseName || '-' }}</span>
                <span :class="['badge', item.reviewSubmitted ? 'badge-success' : 'badge-warning']">
                  {{ item.reviewSubmitted ? '已评' : '待评' }}
                </span>
              </div>
            </div>
            <div class="meta-info">
              <span>互评任务 #{{ item.praId }}</span>
              <span v-if="item.totalScore !== null">得分: {{ item.totalScore }}分</span>
            </div>
            <div class="card-actions">
              <button v-if="!item.reviewSubmitted" class="btn btn-primary" @click="goReview(item.praId)">去互评</button>
              <button v-else class="btn btn-secondary" disabled>已提交</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-container { display: flex; flex-direction: column; height: 100vh; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.content-area { flex: 1; background-color: var(--bg-app); overflow-y: auto; padding: 2rem 3rem; }

.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

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
.tab.active {
  background-color: var(--bg-surface);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  font-weight: 600;
}

.list-container { display: flex; flex-direction: column; gap: 1rem; }

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border-base);
  color: var(--text-secondary);
}
.empty-icon { font-size: 2.5rem; margin-bottom: 0.5rem; opacity: 0.6; }

.list-card { padding: 1.5rem; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
.assignment-title { margin: 0; font-size: 1.25rem; font-weight: 600; color: var(--text-primary); }
.header-badges { display: flex; gap: 0.5rem; align-items: center; }

.meta-info { 
  display: flex; 
  gap: 1.5rem; 
  font-size: 0.875rem; 
  color: var(--text-secondary); 
  margin-bottom: 1.5rem;
  padding: 0.75rem;
  background-color: var(--bg-app);
  border-radius: var(--radius-md);
}

.card-actions { 
  border-top: 1px solid var(--border-light); 
  padding-top: 1.25rem; 
  display: flex; 
  justify-content: flex-end; 
}
.btn-sm { padding: 0.4rem 0.75rem; font-size: 0.8125rem; }
</style>
