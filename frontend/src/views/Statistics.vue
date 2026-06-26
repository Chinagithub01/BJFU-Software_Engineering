<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { exportUrl } from '../utils/api'
import { toastError } from '../utils/toast'

const route = useRoute()
const router = useRouter()
const assignmentId = route.params.id
const courseId = route.query.courseId

const data = ref(null)
const loading = ref(false)

const maxDistCount = computed(() => {
  if (!data.value?.distribution?.length) return 1
  return Math.max(...data.value.distribution.map((d) => d.count), 1)
})

const maxFinalCount = computed(() => {
  if (!data.value?.finalDistribution?.length) return 1
  return Math.max(...data.value.finalDistribution.map((d) => d.count), 1)
})

const barWidth = (count, max) => `${Math.round((count / max) * 100)}%`

const fetchStats = async () => {
  loading.value = true
  try {
    const resp = await fetch(`/api/statistics?assignmentId=${assignmentId}`)
    const result = await resp.json()
    if (result.success) data.value = result.data
    else toastError(result.message || '获取统计失败')
  } catch (e) {
    console.error('获取统计失败', e)
    toastError('无法连接统计服务，请确认后端已启动')
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push('/course/' + (courseId || assignmentId))">← 返回课程</button>
          <h2 class="page-title">数据统计</h2>
          <a
            :href="exportUrl(assignmentId)"
            class="btn btn-primary btn-sm export-btn"
            download
          >📥 导出 Excel</a>
        </div>

        <div v-if="loading" class="empty-state">
          <div class="empty-icon">⏳</div>
          <p>加载中...</p>
        </div>
        <div v-else-if="!data" class="empty-state">
          <div class="empty-icon">📈</div>
          <p>暂无数据</p>
        </div>

        <template v-else>
          <section class="stat-section card">
            <h3 class="section-title">互评分数分布</h3>
            <div v-if="data.distribution && data.distribution.length > 0" class="chart-table">
              <div class="bar-chart">
                <div v-for="d in data.distribution" :key="d.scoreRange" class="bar-row">
                  <span class="bar-label">{{ d.scoreRange }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ width: barWidth(d.count, maxDistCount) }" />
                  </div>
                  <span class="bar-value">{{ d.count }}</span>
                </div>
              </div>
              <div class="modern-table-container">
                <table class="modern-table">
                  <thead><tr><th>分数段</th><th>人数</th></tr></thead>
                  <tbody>
                    <tr v-for="d in data.distribution" :key="'t-' + d.scoreRange">
                      <td class="font-medium">{{ d.scoreRange }}</td>
                      <td>{{ d.count }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div v-else class="empty-state-small">暂无互评数据</div>
          </section>

          <section class="stat-section card">
            <h3 class="section-title">教师最终评分分布</h3>
            <div v-if="data.finalDistribution && data.finalDistribution.length > 0" class="chart-table">
              <div class="bar-chart">
                <div v-for="d in data.finalDistribution" :key="d.scoreRange" class="bar-row">
                  <span class="bar-label">{{ d.scoreRange }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ width: barWidth(d.count, maxFinalCount) }" />
                  </div>
                  <span class="bar-value">{{ d.count }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-state-small">教师尚未进行最终打分</div>
          </section>

          <section class="stat-section card">
            <h3 class="section-title">互评一致性检测</h3>
            <div class="modern-table-container" v-if="data.anomalies && data.anomalies.length > 0">
              <table class="modern-table">
                <thead><tr><th>被评学生</th><th>评分数</th><th>标准差</th><th>最低分</th><th>最高分</th><th>状态</th></tr></thead>
                <tbody>
                  <tr v-for="a in data.anomalies" :key="a.submitter" :class="{ 'anomaly-row': a.alert === '异常' }">
                    <td class="font-medium">{{ a.submitter }}</td>
                    <td>{{ a.reviewCount }}</td>
                    <td>{{ Number(a.scoreStddev).toFixed(2) }}</td>
                    <td class="text-tertiary">{{ a.minScore }}</td>
                    <td class="score-highlight">{{ a.maxScore }}</td>
                    <td><span :class="['badge', a.alert === '异常' ? 'badge-danger' : 'badge-success']">{{ a.alert }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="empty-state-small">暂无足够互评数据进行一致性分析</div>
          </section>

          <section class="stat-section card">
            <h3 class="section-title">学生成绩单</h3>
            <div class="modern-table-container" v-if="data.studentScores && data.studentScores.length > 0">
              <table class="modern-table">
                <thead><tr><th>姓名</th><th>互评次数</th><th>互评均分</th><th>最低分</th><th>最高分</th><th>最终评分</th></tr></thead>
                <tbody>
                  <tr v-for="s in data.studentScores" :key="s.userId">
                    <td class="font-medium">{{ s.realName }}</td>
                    <td>{{ s.reviewCount }}</td>
                    <td class="score-highlight">{{ Number(s.avgScore).toFixed(1) }}</td>
                    <td class="text-tertiary">{{ Number(s.minScore).toFixed(1) }}</td>
                    <td class="text-tertiary">{{ Number(s.maxScore).toFixed(1) }}</td>
                    <td class="score-highlight">{{ s.finalScore != null ? Number(s.finalScore).toFixed(1) : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="empty-state-small">暂无成绩数据</div>
          </section>
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; flex-wrap: wrap; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); flex: 1; }
.export-btn { margin-left: auto; text-decoration: none; }

.stat-section { margin-bottom: 1.5rem; padding: 1.5rem; }
.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
}

.chart-table {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  align-items: start;
}

@media (max-width: 900px) {
  .chart-table { grid-template-columns: 1fr; }
}

.anomaly-row { background-color: #fef2f2 !important; }
.font-medium { font-weight: 500; }
.score-highlight { color: var(--secondary-hover); font-weight: 600; }
.text-tertiary { color: var(--text-tertiary); }
.btn-sm { padding: 0.4rem 1rem; font-size: 0.875rem; }

.empty-state-small {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
  border: 1px dashed var(--border-light);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
}
</style>
