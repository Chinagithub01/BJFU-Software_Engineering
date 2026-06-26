<script setup>
import { ref, watch } from 'vue'
import { toastWarning } from '../utils/toast'

const props = defineProps({
  visible: Boolean,
  title: { type: String, default: '打分' },
  defaultScore: { type: [Number, String], default: '' },
  defaultComment: { type: String, default: '' }
})

const emit = defineEmits(['close', 'submit'])
const score = ref('')
const comment = ref('')

watch(() => props.visible, (v) => {
  if (v) {
    score.value = props.defaultScore || ''
    comment.value = props.defaultComment || ''
  }
})

const handleSubmit = () => {
  if (!score.value) return toastWarning('请输入分数')
  emit('submit', { score: parseFloat(score.value), comment: comment.value })
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-content">
        <h3 class="modal-title">{{ title }}</h3>
        <div class="form-group">
          <label class="form-label">分数（0-100）</label>
          <input v-model="score" class="form-input" type="number" min="0" max="100" step="0.5" placeholder="输入分数" />
        </div>
        <div class="form-group">
          <label class="form-label">评语</label>
          <textarea v-model="comment" class="form-input" rows="3" placeholder="输入评语（可选）"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="emit('close')">取消</button>
          <button class="btn btn-primary" @click="handleSubmit">确认</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 模态框的基础样式已经在 main.css 中定义：
   .modal-overlay, .modal-content, .modal-title, .modal-actions 
   所以这里可以非常精简，甚至不需要任何本地样式 */
</style>
