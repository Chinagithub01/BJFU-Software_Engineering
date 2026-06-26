<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import GradingModal from '../components/GradingModal.vue'
import { submissionDownloadUrl } from '../utils/api'
import { toastError, toastSuccess } from '../utils/toast'

const PAGE_SIZE = 10

const route = useRoute()
const router = useRouter()
const assignmentId = route.params.id
const courseId = route.query.courseId

const submissions = ref([])
const reviews = ref([])
const loading = ref(false)
const total = ref(0)
const reviewsLoaded = ref(false)

const currentPage = computed(() => {
  const p = parseInt(route.query.page, 10)
  return Number.isFinite(p) && p >= 1 ? p : 1
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))

const pageNumbers = computed(() => {
  const pages = []
  for (let i = 1; i <= totalPages.value; i++) pages.push(i)
  return pages
})

const buildQuery = (page) => {
  const query = {}
  if (courseId) query.courseId = courseId
  if (page > 1) query.page = String(page)
  return query
}

const goToPage = (page) => {
  const target = Math.min(Math.max(1, page), totalPages.value)
  if (target === currentPage.value) return
  router.replace({ path: route.path, query: buildQuery(target) })
}

const fetchReviews = async () => {
  if (reviewsLoaded.value) return
  try {
    const revResp = await fetch(`/api/reviews/assign?assignmentId=${assignmentId}`)
    const revResult = await revResp.json()
    if (revResult.success) {
      reviews.value = revResult.data
      reviewsLoaded.value = true
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const resp = await fetch(
      `/api/submissions?assignmentId=${assignmentId}&page=${currentPage.value}&pageSize=${PAGE_SIZE}`
    )
    const result = await resp.json()
    if (result.success) {
      submissions.value = result.data || []
      total.value = result.total ?? submissions.value.length
      if (total.value > 0 && currentPage.value > totalPages.value) {
        router.replace({ path: route.path, query: buildQuery(totalPages.value) })
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getPeerReviews = (submissionId) => {
  return reviews.value.filter(r => r.submissionId === submissionId)
}

const grading = ref({ visible: false, submission: null, defaultScore: '', defaultComment: '' })

const openGrading = (sub) => {
  grading.value = {
    visible: true,
    submission: sub,
    defaultScore: sub.finalScore || '',
    defaultComment: sub.finalComment || ''
  }
}

const saveFinalScore = async ({ score, comment }) => {
  const sub = grading.value.submission
  if (!sub) return
  try {
    const resp = await fetch('/api/submissions', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ submissionId: sub.submissionId, finalScore: score, finalComment: comment })
    })
    const result = await resp.json()
    if (result.success) {
      toastSuccess(result.message || '保存成功')
      sub.finalScore = score
      sub.finalComment = comment
      grading.value.visible = false
    } else {
      toastError(result.message || '保存失败')
    }
  } catch (e) {
    toastError('操作失败')
  }
}

watch(() => route.query.page, () => {
  fetchSubmissions()
})

onMounted(async () => {
  await fetchReviews()
  await fetchSubmissions()
})
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push(`/course/${courseId || assignmentId}`)">← 返回课程</button>
          <h2 class="page-title">学生提交列表</h2>
          <span v-if="total > 0" class="total-hint">共 {{ total }} 条</span>
        </div>

        <div v-if="loading" class="empty-state">
          <div class="empty-icon">⏳</div>
          <p>加载中...</p>
        </div>
        <div v-else-if="total === 0" class="empty-state">
          <div class="empty-icon">📂</div>
          <p>暂无提交记录</p>
        </div>

        <template v-else>
          <div class="submissions-list">
            <div v-for="sub in submissions" :key="sub.submissionId" class="card submission-card">
              <div class="sub-header">
                <span class="student-id">{{ sub.studentName || '-' }} ({{ sub.studentNo || sub.studentId }})</span>
                <span class="sub-time">{{ sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : '-' }}</span>
                <span :class="['badge', sub.status === 'late' ? 'badge-warning' : 'badge-primary']">{{ sub.status }}</span>
              </div>

              <div v-if="sub.contentText" class="content-block">
                <span class="block-label">文字内容：</span>
                <p class="text-content">{{ sub.contentText }}</p>
              </div>

              <div v-if="sub.fileName" class="content-block">
                <span class="block-label">附件：</span>
                <a :href="submissionDownloadUrl(sub.submissionId)" :download="sub.fileName" class="file-link" target="_blank" rel="noopener">📎 {{ sub.fileName }}</a>
              </div>

              <div class="peer-section">
                <span class="section-title">同伴互评</span>
                <div v-if="getPeerReviews(sub.submissionId).length === 0" class="no-reviews">暂无互评数据</div>
                <div v-else class="peer-list">
                  <div v-for="pr in getPeerReviews(sub.submissionId)" :key="pr.praId" class="peer-item">
                    <div class="peer-meta">
                      <span class="peer-reviewer">{{ pr.reviewerId ? '同学 ' + pr.reviewerId : '匿名' }}</span>
                      <span class="peer-score badge badge-neutral">得分: {{ pr.totalScore || '-' }}</span>
                    </div>
                    <p class="peer-comment">{{ pr.overallComment || '该同学未留下总评。' }}</p>
                  </div>
                </div>
              </div>

              <div class="final-section">
                <div class="final-info">
                  <span class="final-label">最终打分：</span>
                  <span v-if="sub.finalScore" class="final-score">{{ sub.finalScore }} 分</span>
                  <span v-else class="no-final">未打分</span>
                </div>
                <button class="btn btn-primary" @click="openGrading(sub)">
                  {{ sub.finalScore ? '重新打分' : '最终打分' }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="totalPages > 1" class="pagination">
            <button
              class="btn btn-secondary btn-sm"
              :disabled="currentPage <= 1"
              @click="goToPage(currentPage - 1)"
            >上一页</button>
            <div class="page-nums">
              <button
                v-for="p in pageNumbers"
                :key="p"
                :class="['btn', 'btn-sm', 'page-btn', { active: p === currentPage }]"
                @click="goToPage(p)"
              >{{ p }}</button>
            </div>
            <button
              class="btn btn-secondary btn-sm"
              :disabled="currentPage >= totalPages"
              @click="goToPage(currentPage + 1)"
            >下一页</button>
            <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
          </div>
        </template>
      </main>
    </div>
    <GradingModal
      :visible="grading.visible"
      title="最终打分"
      :defaultScore="grading.defaultScore"
      :defaultComment="grading.defaultComment"
      @close="grading.visible = false"
      @submit="saveFinalScore"
    />
  </div>
</template>

<style scoped>
.app-container { display: flex; flex-direction: column; height: 100vh; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.content-area { flex: 1; background-color: var(--bg-app); padding: 2rem 3rem; overflow-y: auto; }

.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }
.total-hint { margin-left: auto; font-size: 0.875rem; color: var(--text-secondary); }

.submissions-list { display: flex; flex-direction: column; gap: 1.5rem; }
.submission-card { padding: 1.5rem; }

.sub-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--border-light);
}
.student-id { font-weight: 600; font-size: 1.125rem; color: var(--text-primary); }
.sub-time { color: var(--text-secondary); font-size: 0.875rem; margin-left: auto; }

.content-block {
  background-color: var(--bg-app);
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
}
.block-label { font-weight: 600; font-size: 0.875rem; color: var(--text-secondary); display: block; margin-bottom: 0.5rem; }
.text-content { white-space: pre-wrap; margin: 0; font-size: 0.875rem; line-height: 1.6; color: var(--text-primary); }
.file-link { color: var(--primary-color); font-weight: 500; font-size: 0.875rem; }
.file-link:hover { text-decoration: underline; }

.peer-section {
  margin-top: 1.5rem;
  padding: 1rem;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}
.section-title { font-weight: 600; font-size: 0.875rem; color: var(--text-secondary); display: block; margin-bottom: 0.75rem; }
.no-reviews { color: var(--text-tertiary); font-size: 0.875rem; }
.peer-list { display: flex; flex-direction: column; gap: 0.75rem; }
.peer-item { background-color: #f9fafb; padding: 0.75rem 1rem; border-radius: var(--radius-sm); }
.peer-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.25rem; }
.peer-reviewer { font-weight: 500; font-size: 0.875rem; }
.peer-score { font-size: 0.75rem; }
.peer-comment { margin: 0; font-size: 0.8125rem; color: var(--text-secondary); }

.final-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding-top: 1.25rem;
  border-top: 1px dashed var(--border-light);
}
.final-info { display: flex; align-items: baseline; gap: 0.5rem; }
.final-label { font-weight: 600; color: var(--text-primary); }
.final-score { color: var(--secondary-color); font-size: 1.5rem; font-weight: 700; }
.no-final { color: var(--text-tertiary); font-style: italic; }

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 2rem;
  padding-bottom: 2rem;
}
.page-nums { display: flex; gap: 0.35rem; }
.page-btn {
  min-width: 2.25rem;
  background: var(--bg-surface);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
}
.page-btn.active {
  background: var(--primary-color);
  color: #fff;
  border-color: var(--primary-color);
}
.page-info { font-size: 0.875rem; color: var(--text-secondary); }

.btn-sm { padding: 0.4rem 0.75rem; font-size: 0.8125rem; }
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
