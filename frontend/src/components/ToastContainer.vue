<script setup>
import { useToastState } from '../utils/toast'

const state = useToastState()

const icon = (type) => {
  const map = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' }
  return map[type] || map.info
}
</script>

<template>
  <div class="toast-host" aria-live="polite">
    <transition-group name="toast" tag="div" class="toast-list">
      <div
        v-for="t in state.toasts"
        :key="t.id"
        :class="['toast-item', `toast-${t.type}`]"
      >
        <span class="toast-icon">{{ icon(t.type) }}</span>
        <span class="toast-msg">{{ t.message }}</span>
      </div>
    </transition-group>
  </div>
</template>

<style scoped>
.toast-host {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
  pointer-events: none;
  max-width: min(420px, calc(100vw - 2rem));
}

.toast-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.toast-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-lg);
  font-size: 0.875rem;
  line-height: 1.4;
  pointer-events: auto;
}

.toast-success { border-left: 4px solid #10b981; }
.toast-error { border-left: 4px solid #ef4444; }
.toast-warning { border-left: 4px solid #f59e0b; }
.toast-info { border-left: 4px solid #3b82f6; }

.toast-icon { font-weight: 700; flex-shrink: 0; }
.toast-msg { color: var(--text-primary); word-break: break-word; }

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(1rem);
}
</style>
