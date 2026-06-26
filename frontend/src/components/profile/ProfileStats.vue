<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  }
})
</script>

<template>
  <div class="stats-grid">
    <div
      v-for="item in items"
      :key="item.label"
      :class="['card', 'stat-card', { warn: item.warn }]"
    >
      <span class="num">{{ item.value ?? 0 }}</span>
      <span class="label">{{ item.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1.25rem;
  margin-bottom: 2rem;
}
.stat-card {
  padding: 1.5rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.stat-card .num {
  display: block;
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 0.25rem;
  line-height: 1;
}
.stat-card .label {
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
}
.stat-card.warn {
  border-color: #fcd34d;
  background-color: #fffbeb;
}
.stat-card.warn .num {
  color: #d97706;
}
.stat-card.warn .label {
  color: #b45309;
}
</style>
