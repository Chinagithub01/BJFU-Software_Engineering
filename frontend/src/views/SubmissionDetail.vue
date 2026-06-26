<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { submissionDownloadUrl } from '../utils/api'
import AttachmentPreview from '../components/AttachmentPreview.vue'

const route = useRoute()
const router = useRouter()
const submissionId = route.params.id
const courseId = route.query.courseId

const detail = ref(null)
const loading = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const resp = await fetch(`/api/submissions/detail?submissionId=${submissionId}`)
    const result = await resp.json()
    if (result.success) detail.value = result.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push(`/course/${courseId}`)">← 返回课程</button>
          <h2 class="page-title">作业详情</h2>
        </div>

        <div v-if="loading" class="empty-state">
           <div class="empty-icon">⏳</div>
           <p>加载中...</p>
        </div>
        <div v-else-if="!detail" class="empty-state">
           <div class="empty-icon">❌</div>
           <p>未找到详情</p>
        </div>

        <div v-else class="detail-layout">
          <!-- 左栏：内容 + 互评 + 申诉 -->
          <div class="left-col">
            <div class="info-bar">
              <p><span class="label">作业：</span>{{ detail.assignmentTitle || '-' }}</p>
              <p><span class="label">状态：</span><span :class="['badge', detail.status === 'late' ? 'badge-warning' : 'badge-primary']">{{ detail.status }}</span></p>
              <p v-if="detail.dueDate"><span class="label">截止时间：</span>{{ new Date(detail.dueDate).toLocaleString() }}</p>
            </div>

            <div v-if="detail.contentText" class="card content-card">
              <h4 class="section-title">文字内容</h4>
              <p class="text-content">{{ detail.contentText }}</p>
            </div>

            <div v-if="detail.fileName" class="card content-card">
              <h4 class="section-title">附件</h4>
              <a :href="submissionDownloadUrl(submissionId)" :download="detail.fileName" class="file-link" target="_blank" rel="noopener">📎 {{ detail.fileName }}</a>
            </div>

            <AttachmentPreview
              v-if="detail.fileName || detail.contentText"
              :submission-id="submissionId"
            />

            <div class="card content-card">
              <h4 class="section-title">互评结果</h4>
              <div v-if="detail.peerReviews && detail.peerReviews.length > 0">
                <div v-for="(r, i) in detail.peerReviews" :key="i" class="review-item">
                  <div class="score-chips">
                    <span v-for="s in r.scores" :key="s.itemName" class="badge badge-neutral chip">{{ s.itemName }}: {{ s.score }}/{{ s.maxScore }}</span>
                  </div>
                  <p class="total">总分：<span class="score-highlight">{{ r.totalScore }}分</span></p>
                  <p v-if="r.overallComment" class="comment"><span class="label">总评：</span>{{ r.overallComment }}</p>
                </div>
              </div>
              <div v-else class="no-data">暂无互评数据</div>
            </div>

            <div class="card content-card">
              <h4 class="section-title">申诉记录</h4>
              <div v-if="detail.appeals && detail.appeals.length > 0">
                <div v-for="a in detail.appeals" :key="a.appealId" class="appeal-item" :class="a.status">
                  <p><span class="label">理由：</span>{{ a.reason }}</p>
                  <p><span class="label">状态：</span>
                    <span :class="['badge', a.status === 'accepted' ? 'badge-success' : a.status === 'rejected' ? 'badge-neutral' : 'badge-warning']">
                      {{ a.status === 'accepted' ? '✓ 已同意' : a.status === 'rejected' ? '✗ 已驳回' : '⋯ 处理中' }}
                    </span>
                  </p>
                  <p v-if="a.handlerResponse"><span class="label">回复：</span>{{ a.handlerResponse }}</p>
                  <p v-if="a.adjustedScore"><span class="label">调分：</span><span class="score-highlight">{{ a.adjustedScore }}分</span></p>
                </div>
              </div>
              <div v-else class="no-data">无申诉记录</div>
            </div>
          </div>

          <!-- 右栏：最终评价 + 重新提交 -->
          <div class="right-col">
            <div class="card sticky-top content-card">
              <h4 class="section-title text-center">教师最终评价</h4>
              <div v-if="detail.finalScore" class="final-block">
                <p class="big-score">{{ detail.finalScore }}</p>
                <p class="score-label">最终得分</p>
                <div v-if="detail.finalComment" class="final-comment">
                  <span class="label">教师评语：</span>
                  <p>{{ detail.finalComment }}</p>
                </div>
              </div>
              <div v-else class="no-data mt-4">教师尚未给出最终评价</div>
            </div>

            <div class="card content-card mt-4 notice-card">
              <h4 class="section-title">提交说明</h4>
              <p class="notice-text">每份作业仅允许提交一次。如需修改，请在截止前联系任课教师处理。</p>
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
.content-area { flex: 1; background-color: var(--bg-app); padding: 2rem 3rem; overflow-y: auto; }

.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

.detail-layout { display: flex; gap: 2rem; padding-bottom: 3rem; align-items: flex-start; }
.left-col { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 1.5rem; }
.right-col { width: 340px; flex-shrink: 0; }

.info-bar { 
  background-color: var(--primary-light); 
  border-left: 4px solid var(--primary-color);
  border-radius: var(--radius-md); 
  padding: 1rem 1.5rem; 
}
.info-bar p { margin: 0.5rem 0; color: var(--text-primary); }
.label { font-weight: 600; color: var(--text-secondary); margin-right: 0.5rem; }

.content-card { padding: 1.5rem; margin-bottom: 0; }
.section-title { margin: 0 0 1rem 0; color: var(--text-primary); font-size: 1.125rem; font-weight: 600; border-bottom: 1px solid var(--border-light); padding-bottom: 0.75rem; }

.text-content { white-space: pre-wrap; color: var(--text-primary); line-height: 1.6; margin: 0; }
.file-link { color: var(--primary-color); font-weight: 500; font-size: 0.9375rem; }
.file-link:hover { text-decoration: underline; }

.review-item { border-bottom: 1px dashed var(--border-light); padding: 1rem 0; }
.review-item:last-child { border-bottom: none; padding-bottom: 0; }
.score-chips { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 0.75rem; }
.chip { font-weight: normal; }
.total { margin: 0 0 0.5rem 0; }
.score-highlight { font-weight: 700; color: var(--secondary-color); font-size: 1.125rem; }
.comment { margin: 0; color: var(--text-primary); font-size: 0.9375rem; background-color: var(--bg-app); padding: 0.75rem; border-radius: var(--radius-md); }

.appeal-item { padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; background-color: var(--bg-app); font-size: 0.9375rem; display: flex; flex-direction: column; gap: 0.5rem; }
.appeal-item.accepted { border-left: 3px solid var(--secondary-color); }
.appeal-item.rejected { border-left: 3px solid var(--text-tertiary); }
.appeal-item.pending { border-left: 3px solid #f59e0b; }
.appeal-item p { margin: 0; }

.sticky-top { position: sticky; top: 2rem; }
.text-center { text-align: center; }
.final-block { text-align: center; padding-top: 1rem; }
.big-score { font-size: 4rem; font-weight: 800; color: var(--secondary-color); margin: 0; line-height: 1; }
.score-label { color: var(--text-secondary); margin: 0.5rem 0 1.5rem; font-size: 0.875rem; }
.final-comment { background: var(--bg-app); padding: 1rem; border-radius: var(--radius-md); color: var(--text-primary); text-align: left; font-size: 0.9375rem; }
.final-comment p { margin: 0.5rem 0 0; line-height: 1.5; }

.notice-card { background: #fffbeb; border: 1px solid #fde68a; }
.notice-text { margin: 0; font-size: 0.875rem; color: #92400e; line-height: 1.5; }
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
.no-data { color: var(--text-tertiary); text-align: center; padding: 1rem; font-size: 0.9375rem; }
</style>
