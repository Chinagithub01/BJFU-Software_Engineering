<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  assignmentTitle: { type: String, default: '' },
  score: { type: [Number, String], default: null },
})
const emit = defineEmits(['close', 'submit'])

const reason = ref('')

watch(
  () => props.visible,
  (v) => {
    if (v) reason.value = ''
  }
)

const submit = () => {
  const text = reason.value.trim()
  if (text.length < 10) return
  emit('submit', text)
}
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content appeal-modal">
      <h3 class="modal-title">提交申诉</h3>
      <p class="appeal-hint">
        作业「{{ assignmentTitle }}」互评均分
        <strong>{{ score }}</strong> 分，低于阈值 {{ 60 }} 分时可申诉。
      </p>
      <div class="form-group">
        <label class="form-label">申诉理由（至少 10 字）</label>
        <textarea
          v-model="reason"
          class="form-input"
          rows="4"
          placeholder="请说明认为评分不公的具体原因…"
        />
        <span class="char-count">{{ reason.trim().length }} / 10+</span>
      </div>
      <div class="modal-actions">
        <button class="btn btn-secondary" type="button" @click="emit('close')">取消</button>
        <button
          class="btn btn-primary"
          type="button"
          :disabled="reason.trim().length < 10"
          @click="submit"
        >
          提交申诉
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.appeal-modal { width: 520px; }
.appeal-hint {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
  line-height: 1.5;
}
.char-count {
  display: block;
  text-align: right;
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-top: 0.25rem;
}
</style>
