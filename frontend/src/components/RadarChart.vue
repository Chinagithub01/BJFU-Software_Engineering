<script setup>
import { computed } from 'vue'

const props = defineProps({
  dimensions: { type: Array, required: true },  // [{label, score, maxScore}]
  size: { type: Number, default: 240 }
})

const cx = computed(() => props.size / 2)
const cy = computed(() => props.size / 2)
// 缩小雷达图半径，为外围文字留出更多空间
const radius = computed(() => props.size * 0.3)

const levels = [0.2, 0.4, 0.6, 0.8, 1.0]

function angle(index, total) {
  return (Math.PI * 2 * index) / total - Math.PI / 2
}

function point(a, r) {
  return { x: cx.value + r * Math.cos(a), y: cy.value + r * Math.sin(a) }
}

const gridPolygons = computed(() => {
  const n = props.dimensions.length
  return levels.map(level => {
    return props.dimensions.map((_, i) => {
      const p = point(angle(i, n), radius.value * level)
      return `${p.x},${p.y}`
    }).join(' ')
  })
})

const dataPolygon = computed(() => {
  const n = props.dimensions.length
  return props.dimensions.map((d, i) => {
    const ratio = Math.min(d.score / d.maxScore, 1)
    const p = point(angle(i, n), radius.value * ratio)
    return `${p.x},${p.y}`
  }).join(' ')
})

const labels = computed(() => {
  const n = props.dimensions.length
  return props.dimensions.map((d, i) => {
    // 增加文字距离雷达外圈的偏移量
    const p = point(angle(i, n), radius.value + 28)
    return { 
      x: p.x, 
      y: p.y, 
      text: d.label.length > 8 ? d.label.substring(0, 8) + '..' : d.label, 
      score: d.score, 
      maxScore: d.maxScore 
    }
  })
})

const axes = computed(() => {
  const n = props.dimensions.length
  return props.dimensions.map((_, i) => {
    const p = point(angle(i, n), radius.value)
    return { x1: cx.value, y1: cy.value, x2: p.x, y2: p.y }
  })
})
</script>

<template>
  <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
    <!-- 网格 -->
    <polygon v-for="(pts, idx) in gridPolygons" :key="'g'+idx"
      :points="pts" fill="none" stroke="#e5e7eb" stroke-width="1" />
    <!-- 轴线 -->
    <line v-for="(a, idx) in axes" :key="'a'+idx"
      :x1="a.x1" :y1="a.y1" :x2="a.x2" :y2="a.y2" stroke="#f3f4f6" stroke-width="1" />
    <!-- 数据 -->
    <polygon :points="dataPolygon" fill="rgba(16, 185, 129, 0.25)" stroke="#10b981" stroke-width="2" />
    <!-- 数据点 -->
    <circle v-for="(d, i) in props.dimensions" :key="'c'+i"
      :cx="point(angle(i, dimensions.length), radius * Math.min(d.score/d.maxScore, 1)).x"
      :cy="point(angle(i, dimensions.length), radius * Math.min(d.score/d.maxScore, 1)).y"
      r="4" fill="#10b981" />
    <!-- 标签 -->
    <text v-for="(l, i) in labels" :key="'t'+i"
      :x="l.x" :y="l.y" text-anchor="middle" font-size="12" fill="#4b5563">{{ l.text }}</text>
    <!-- 分数 -->
    <text v-for="(l, i) in labels" :key="'s'+i"
      :x="l.x" :y="l.y + 16" text-anchor="middle" font-size="11" fill="#10b981"
      font-weight="bold">{{ l.score }}/{{ l.maxScore }}</text>
  </svg>
</template>
