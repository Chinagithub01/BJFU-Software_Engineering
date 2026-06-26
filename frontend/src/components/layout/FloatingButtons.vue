<script setup>
</script>

<template>
  <div class="floating-buttons">
  </div>
</template>

<style scoped>
.floating-buttons {
  position: fixed;
  right: 2rem;
  bottom: 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  z-index: 100;
}

.fab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--bg-surface);
  border: 1px solid var(--border-light);
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-full);
  cursor: pointer;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
  color: var(--text-primary);
  font-weight: 500;
}

.fab-btn:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  color: var(--primary-color);
  border-color: var(--primary-light);
}

.fab-icon {
  font-size: 1.25rem;
  color: inherit;
}

.fab-text {
  font-size: 0.875rem;
  color: inherit;
}
</style>
