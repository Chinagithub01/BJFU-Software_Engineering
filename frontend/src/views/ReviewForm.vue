<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { toastError, toastSuccess, toastWarning } from '../utils/toast'
import { submissionDownloadUrl } from '../utils/api'
import AttachmentPreview from '../components/AttachmentPreview.vue'

const route = useRoute()
const router = useRouter()
const praId = route.params.praId

const detail = ref(null)
const rubricScores = ref([])
const overallComment = ref('')
const loading = ref(false)
const submitting = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/reviews?praId=${praId}`)
    const result = await response.json()
    if (result.success) {
      detail.value = result.data
      rubricScores.value = (result.data.rubricItems || []).map(item => ({
        rubricItemId: item.itemId,
        itemName: item.itemName,
        maxScore: item.maxScore,
        weight: item.weight,
        description: item.description,
        sortOrder: item.sortOrder,
        score: 0,
        comment: ''
      }))
    } else {
      toastError(result.message || '加载失败')
    }
  } catch (e) {
    console.error('获取互评详情失败', e)
    toastError('请求失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}

const normalizeScore = (item) => {
  const raw = item.score
  if (raw === '' || raw == null || Number.isNaN(Number(raw))) {
    item.score = 0
    return
  }
  item.score = Math.min(item.maxScore, Math.max(0, Math.round(Number(raw))))
}

const submitReview = async () => {
  for (const item of rubricScores.value) {
    normalizeScore(item)
    const score = Number(item.score)
    if (!Number.isInteger(score)) {
      toastWarning(`"${item.itemName}" 的得分须为整数`)
      return
    }
    if (score < 0 || score > item.maxScore) {
      toastWarning(`"${item.itemName}" 的分数须在 0 ~ ${item.maxScore} 之间的整数`)
      return
    }
  }

  submitting.value = true
  try {
    const response = await fetch('/api/reviews', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        praId: parseInt(praId),
        overallComment: overallComment.value,
        totalScore: parseFloat(totalScore()),
        scores: rubricScores.value.map(s => ({
          rubricItemId: s.rubricItemId,
          score: Math.round(Number(s.score)),
          weight: parseFloat(s.weight) || 1,
          comment: s.comment
        }))
      })
    })
    const result = await response.json()
    if (result.success) {
      toastSuccess(result.message || '互评已提交')
      router.push('/reviews')
    } else {
      toastError(result.message || '提交失败')
    }
  } catch (e) {
    console.error('提交互评失败', e)
    toastError('提交失败，请检查网络')
  } finally {
    submitting.value = false
  }
}

const totalScore = () => {
  return rubricScores.value.reduce((sum, item) => sum + (parseFloat(item.score) || 0) * (parseFloat(item.weight) || 1), 0).toFixed(1)
}

const handleLogout = () => {
  sessionStorage.removeItem('user')
  router.push('/login')
}

onMounted(fetchDetail)
</script>

<template>
  <div class="app-container">
    <Header @logout="handleLogout" />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push('/reviews')">← 返回互评列表</button>
          <h2 class="page-title">互评打分</h2>
        </div>

        <div v-if="loading" class="empty-state">
           <div class="empty-icon">⏳</div>
           <p>加载中...</p>
        </div>

        <template v-else-if="detail">
          <!-- 作业信息 -->
          <div class="info-bar">
            <p><strong>作业：</strong>{{ detail.assignmentTitle || '-' }}</p>
            <p><strong>被评者：</strong>匿名互评</p>
            <p v-if="detail.reviewSubmitted" class="already-submitted">⚠ 您已提交过该互评</p>
          </div>

          <!-- 作业内容 -->
          <div v-if="detail.contentText || detail.fileName" class="card content-section">
            <div v-if="detail.contentText" class="text-content">
              <h4>文字内容</h4>
              <p>{{ detail.contentText }}</p>
            </div>
            <div v-if="detail.fileName" class="file-download">
              <h4>附件</h4>
              <a :href="submissionDownloadUrl(detail.submissionId)" :download="detail.fileName" class="download-link" target="_blank" rel="noopener">
                📎 {{ detail.fileName }}
              </a>
            </div>
          </div>

          <AttachmentPreview
            v-if="detail.fileName || detail.contentText"
            :pra-id="praId"
          />

          <!-- Rubric 分项打分 -->
          <div class="rubric-list">
            <div v-for="(item, idx) in rubricScores" :key="item.rubricItemId" class="card rubric-item">
              <div class="rubric-header">
                <span class="rubric-name">
                  {{ idx + 1 }}. {{ item.itemName }}
                  <small>(满分 {{ item.maxScore }}分，权重 {{ item.weight }})</small>
                </span>
              </div>
              <p v-if="item.description" class="rubric-desc">{{ item.description }}</p>
              <div class="rubric-inputs">
                <div class="score-input">
                  <label class="form-label">得分</label>
                  <div class="score-input-wrapper">
                    <input
                      v-model.number="item.score"
                      class="form-input"
                      type="number"
                      :min="0"
                      :max="item.maxScore"
                      step="1"
                      :disabled="detail.reviewSubmitted"
                      @blur="normalizeScore(item)"
                    />
                    <span class="score-hint">/ {{ item.maxScore }}</span>
                  </div>
                </div>
                <div class="comment-input">
                  <label class="form-label">评语</label>
                  <input
                    v-model="item.comment"
                    class="form-input"
                    type="text"
                    placeholder="对此项的评价..."
                    :disabled="detail.reviewSubmitted"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 总评 -->
          <div class="overall-section">
            <label class="form-label">总评（选填）</label>
            <textarea
              v-model="overallComment"
              class="form-input"
              rows="3"
              placeholder="对学生整体表现的评价..."
              :disabled="detail.reviewSubmitted"
            ></textarea>
          </div>

          <!-- 总分 -->
          <div class="total-bar">
            <span class="total-label">总分合计：</span>
            <span class="total-value">{{ totalScore() }} 分</span>
          </div>

          <!-- 提交按钮 -->
          <div class="submit-bar" v-if="!detail.reviewSubmitted">
            <button class="btn btn-secondary" @click="router.push('/reviews')">取消</button>
            <button class="btn btn-primary" @click="submitReview" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交互评' }}
            </button>
          </div>
        </template>

        <div v-else class="empty-state">
           <div class="empty-icon">❌</div>
           <p>未找到互评详情</p>
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

.info-bar { 
  background-color: var(--primary-light); 
  border-left: 4px solid var(--primary-color);
  border-radius: var(--radius-md); 
  padding: 1rem 1.5rem; 
  margin-bottom: 1.5rem; 
}
.info-bar p { margin: 0.25rem 0; color: var(--primary-hover); }
.already-submitted { color: var(--danger-color) !important; font-weight: 600; margin-top: 0.5rem !important; }

.content-section { padding: 1.5rem; margin-bottom: 1.5rem; }
.content-section h4 { margin: 0 0 0.5rem 0; color: var(--text-primary); font-size: 1.125rem; }
.text-content { margin-bottom: 1.5rem; }
.text-content p { color: var(--text-secondary); line-height: 1.6; white-space: pre-wrap; }
.file-download a { color: var(--primary-color); text-decoration: none; font-weight: 500; }
.file-download a:hover { text-decoration: underline; }

.rubric-list { display: flex; flex-direction: column; gap: 1.5rem; margin-bottom: 2rem; }
.rubric-item { padding: 1.5rem; }
.rubric-header { margin-bottom: 0.5rem; }
.rubric-name { font-weight: 600; font-size: 1.125rem; color: var(--text-primary); }
.rubric-name small { color: var(--text-tertiary); font-weight: normal; margin-left: 0.5rem; font-size: 0.875rem; }
.rubric-desc { color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 1rem; }
.rubric-inputs { display: flex; gap: 1.5rem; align-items: flex-start; }
.score-input { flex: 0 0 160px; }
.score-input-wrapper { display: flex; align-items: center; gap: 0.5rem; }
.score-input-wrapper input { width: 80px; text-align: center; }
.score-hint { color: var(--text-tertiary); font-size: 0.875rem; }
.comment-input { flex: 1; }

.overall-section { margin-bottom: 1.5rem; }

.total-bar { 
  background-color: #f0fdf4; /* emerald 50 */
  border: 1px solid #bbf7d0; /* emerald 200 */
  border-radius: var(--radius-lg); 
  padding: 1.5rem 2rem; 
  margin-bottom: 2rem; 
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.total-label { font-size: 1.125rem; color: var(--text-secondary); }
.total-value { font-size: 2rem; font-weight: 700; color: var(--secondary-color); margin-left: 0.75rem; }

.submit-bar { display: flex; justify-content: flex-end; gap: 1rem; padding-bottom: 2rem; }

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
