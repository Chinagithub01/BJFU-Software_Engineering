<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { submissionDownloadUrl } from '../utils/api'

const props = defineProps({
  praId: { type: [String, Number], default: null },
  submissionId: { type: [String, Number], default: null }
})

const preview = ref(null)
const loading = ref(false)
const error = ref('')

const effectiveSubmissionId = computed(() => {
  return props.submissionId || preview.value?.submissionId || null
})

const previewUrl = computed(() => {
  if (!preview.value?.fileAvailable || preview.value.previewType !== 'pdf') return ''
  if (!effectiveSubmissionId.value) return ''
  return submissionDownloadUrl(effectiveSubmissionId.value, true)
})

const downloadUrl = computed(() => {
  if (!effectiveSubmissionId.value) return '#'
  return submissionDownloadUrl(effectiveSubmissionId.value)
})

const typeLabel = computed(() => {
  const map = {
    pdf: 'PDF 文档',
    docx: 'Word 文档',
    code: '代码文件',
    text: '文本内容',
    unsupported: '附件',
    empty: '附件'
  }
  return map[preview.value?.previewType] || '附件'
})

const sourceHint = computed(() => {
  if (!preview.value) return ''
  if (preview.value.source === 'database' && !preview.value.fileAvailable) {
    return '附件文件不可用'
  }
  if (preview.value.source === 'file') {
    return '已从附件提取内容'
  }
  return ''
})

const fetchPreview = async () => {
  if (!props.praId && !props.submissionId) return
  loading.value = true
  error.value = ''
  preview.value = null
  try {
    let url
    if (props.praId) {
      url = `/api/reviews?praId=${props.praId}&mode=preview`
    } else {
      url = `/api/submissions/preview?submissionId=${props.submissionId}`
    }
    const resp = await fetch(url)
    const result = await resp.json()
    if (result.success) {
      preview.value = result.data
    } else {
      error.value = result.message || '预览加载失败'
    }
  } catch (e) {
    console.error('附件预览加载失败', e)
    error.value = '预览加载失败，请检查网络'
  } finally {
    loading.value = false
  }
}

onMounted(fetchPreview)
watch(() => [props.praId, props.submissionId], fetchPreview)
</script>

<template>
  <div class="card preview-section">
    <h4 class="preview-title">附件预览</h4>

    <div v-if="loading" class="preview-state">加载预览中...</div>
    <div v-else-if="error" class="preview-state preview-error">{{ error }}</div>
    <template v-else-if="preview">
      <div class="preview-meta">
        <span v-if="preview.fileName" class="file-tag">📎 {{ preview.fileName }}</span>
        <span class="type-tag">{{ typeLabel }}</span>
        <span v-if="sourceHint" class="source-hint">{{ sourceHint }}</span>
      </div>

      <!-- PDF：iframe 原文件预览 -->
      <div v-if="preview.previewType === 'pdf' && preview.fileAvailable" class="pdf-frame-wrap">
        <iframe
          :src="previewUrl"
          class="pdf-frame"
          title="PDF 预览"
        />
      </div>

      <!-- 不支持在线预览 -->
      <div v-if="preview.previewType === 'unsupported'" class="preview-state">
        该格式暂不支持在线预览，请下载附件查看。
        <a
          v-if="effectiveSubmissionId"
          :href="downloadUrl"
          :download="preview.fileName"
          class="download-link"
          target="_blank"
          rel="noopener"
        >下载 {{ preview.fileName }}</a>
      </div>

      <!-- 空内容 -->
      <div v-if="preview.previewType === 'empty' && !preview.content" class="preview-state">
        暂无附件内容可预览。
      </div>

      <!-- 文本 / 代码 / Word 提取 / PDF 文本摘要 -->
      <div
        v-if="preview.content && (preview.previewType !== 'unsupported')"
        class="text-preview-wrap"
      >
        <div v-if="preview.previewType === 'pdf'" class="text-preview-label">文本摘要</div>
        <pre
          :class="['text-preview', preview.previewType === 'code' ? 'code-preview' : '']"
        >{{ preview.content }}</pre>
        <p v-if="preview.truncated" class="truncate-hint">内容过长，仅显示前 512KB</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.preview-section {
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}
.preview-title {
  margin: 0 0 1rem 0;
  color: var(--text-primary);
  font-size: 1.125rem;
}
.preview-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 1rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}
.file-tag { font-weight: 500; color: var(--text-primary); }
.type-tag {
  background: var(--primary-light);
  color: var(--primary-hover);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
}
.source-hint { color: var(--text-tertiary); font-size: 0.8rem; }

.pdf-frame-wrap {
  border: 1px solid var(--border-base);
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 1rem;
  background: #f8fafc;
}
.pdf-frame {
  width: 100%;
  height: 520px;
  border: none;
  display: block;
}

.text-preview-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}
.text-preview-wrap {
  margin-top: 0.25rem;
}
.text-preview {
  margin: 0;
  padding: 1rem 1.25rem;
  background: #f8fafc;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 480px;
  overflow: auto;
  font-size: 0.9rem;
}
.code-preview {
  font-family: ui-monospace, 'Cascadia Code', 'Consolas', monospace;
  font-size: 0.85rem;
  background: #1e293b;
  color: #e2e8f0;
  border-color: #334155;
}
.truncate-hint {
  margin: 0.5rem 0 0;
  font-size: 0.75rem;
  color: var(--text-tertiary);
}
.preview-state {
  color: var(--text-secondary);
  font-size: 0.9rem;
  padding: 0.5rem 0;
}
.preview-error { color: var(--danger-color); }
.download-link {
  display: inline-block;
  margin-left: 0.5rem;
  color: var(--primary-color);
  text-decoration: none;
}
.download-link:hover { text-decoration: underline; }
</style>
