<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import RadarChart from '../components/RadarChart.vue'
import AppealModal from '../components/AppealModal.vue'
import { canAppeal, APPEAL_SCORE_THRESHOLD } from '../utils/api'
import { toast, toastError, toastSuccess } from '../utils/toast'

const router = useRouter()
const results = ref([])
const loading = ref(false)
const appealModal = ref({ visible: false, item: null })

const fetchResults = async () => {
  const userStr = sessionStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)
  loading.value = true
  try {
    const response = await fetch(`/api/reviews/assign?studentId=${user.userId}`)
    const result = await response.json()
    if (result.success) results.value = result.data
    else toastError(result.message || '加载失败')
  } catch (e) {
    console.error('获取互评结果失败', e)
    toastError('无法加载互评结果')
  } finally {
    loading.value = false
  }
}

const openAppeal = (item) => {
  if (!canAppeal(item.totalScore)) {
    toast(`互评分数不低于 ${APPEAL_SCORE_THRESHOLD} 分时不可申诉`, 'warning')
    return
  }
  appealModal.value = { visible: true, item }
}

const submitAppeal = async (reason) => {
  const item = appealModal.value.item
  if (!item) return
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  try {
    const resp = await fetch('/api/appeals', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        praId: item.praId,
        submissionId: item.submissionId,
        studentId: user.userId,
        reason,
      }),
    })
    const result = await resp.json()
    if (result.success) {
      toastSuccess(result.message || '申诉已提交')
      appealModal.value.visible = false
    } else {
      toastError(result.message || '申诉失败')
    }
  } catch (e) {
    toastError('提交失败，请检查网络')
  }
}

onMounted(fetchResults)
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push('/')">← 返回工作台</button>
          <h2 class="page-title">我的互评结果</h2>
        </div>

        <div v-if="loading" class="empty-state">
          <div class="empty-icon">⏳</div>
          <p>加载中...</p>
        </div>
        <div v-else-if="results.length === 0" class="empty-state">
          <div class="empty-icon">📊</div>
          <p>暂无收到的互评结果</p>
          <p class="empty-hint">完成互评并等待同伴评分后，结果将显示在这里</p>
        </div>

        <div v-else class="results-list">
          <div v-for="item in results" :key="item.praId" class="card result-card">
            <div class="result-header">
              <h3 class="assignment-title">{{ item.assignmentTitle || '-' }}</h3>
              <span :class="['total-badge', { 'low-score': canAppeal(item.totalScore) }]">
                {{ item.totalScore }} 分
              </span>
            </div>

            <div class="result-body" v-if="item.scores && item.scores.length > 0">
              <div class="radar-wrap">
                <RadarChart
                  :dimensions="item.scores.map(s => ({ label: s.itemName, score: s.score, maxScore: s.maxScore }))"
                  :size="220"
                />
              </div>
              <div class="modern-table-container table-wrapper">
                <table class="modern-table">
                  <thead>
                    <tr>
                      <th>评分项</th>
                      <th>得分</th>
                      <th>满分</th>
                      <th>评语</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in item.scores" :key="s.itemName">
                      <td class="font-medium">{{ s.itemName }}</td>
                      <td class="score-highlight">{{ s.score }}</td>
                      <td class="text-tertiary">{{ s.maxScore }}</td>
                      <td>{{ s.comment || '-' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="overall-section" v-if="item.overallComment">
              <div class="overall-title">总评</div>
              <div class="overall-content">{{ item.overallComment }}</div>
            </div>

            <div class="appeal-bar">
              <button
                class="btn btn-secondary btn-sm discuss-btn"
                @click="router.push('/discussion/' + item.assignmentId)"
              >💬 匿名讨论</button>
              <button
                v-if="canAppeal(item.totalScore)"
                class="btn btn-secondary btn-sm warning-border"
                @click="openAppeal(item)"
              >我要申诉</button>
              <span v-else class="appeal-tip">分数 ≥ {{ APPEAL_SCORE_THRESHOLD }}，无需申诉</span>
            </div>
          </div>
        </div>
      </main>
    </div>

    <AppealModal
      :visible="appealModal.visible"
      :assignment-title="appealModal.item?.assignmentTitle"
      :score="appealModal.item?.totalScore"
      @close="appealModal.visible = false"
      @submit="submitAppeal"
    />
  </div>
</template>

<style scoped>
.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

.results-list { display: flex; flex-direction: column; gap: 1.5rem; padding-bottom: 2rem; }
.result-card { padding: 1.5rem; }

.result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; gap: 1rem; flex-wrap: wrap; }
.assignment-title { margin: 0; font-size: 1.25rem; font-weight: 600; color: var(--text-primary); }
.total-badge {
  background-color: #f0fdf4;
  color: var(--secondary-hover);
  padding: 0.5rem 1.25rem;
  border-radius: var(--radius-full);
  font-size: 1.25rem;
  font-weight: 700;
  border: 1px solid #bbf7d0;
}
.total-badge.low-score {
  background-color: #fffbeb;
  color: #d97706;
  border-color: #fde68a;
}

.result-body { display: flex; gap: 2rem; align-items: flex-start; margin-bottom: 1.5rem; flex-wrap: wrap; }
.radar-wrap { flex-shrink: 0; background-color: var(--bg-app); border-radius: var(--radius-lg); padding: 1rem; }
.table-wrapper { flex: 1; min-width: min(400px, 100%); }

.font-medium { font-weight: 500; }
.score-highlight { color: var(--secondary-hover); font-weight: 600; }
.text-tertiary { color: var(--text-tertiary); }

.overall-section {
  background-color: var(--bg-app);
  padding: 1rem 1.25rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
}
.overall-title { font-weight: 600; font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 0.5rem; }
.overall-content { color: var(--text-primary); line-height: 1.6; font-size: 0.9375rem; }

.appeal-bar { display: flex; justify-content: flex-end; align-items: center; gap: 0.75rem; border-top: 1px dashed var(--border-light); padding-top: 1rem; flex-wrap: wrap; }
.discuss-btn { background: #f0f5ff; color: #2f54eb; border: 1px solid #d6e4ff; }
.warning-border { border-color: #f59e0b; color: #d97706; }
.appeal-tip { font-size: 0.8125rem; color: var(--text-tertiary); }

.btn-sm { padding: 0.4rem 1rem; font-size: 0.875rem; }
.empty-hint { font-size: 0.875rem; margin-top: 0.5rem; color: var(--text-tertiary); }
</style>
