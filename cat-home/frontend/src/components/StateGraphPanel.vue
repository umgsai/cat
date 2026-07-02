<template>
  <section class="report-graph-panel">
    <div v-if="loading" class="empty-state">正在加载图表数据...</div>
    <div v-else-if="error" class="empty-state">{{ error }}</div>
    <div v-else-if="graph" class="line-chart-grid">
      <LineChartPanel :chart="graph.graph" smooth />
      <PieChartPanel v-if="graph.pieChart" :chart="graph.pieChart" />
    </div>
  </section>
</template>

<script setup lang="ts">
import LineChartPanel from './LineChartPanel.vue'
import PieChartPanel from './PieChartPanel.vue'

interface StateGraph {
  graph: string
  key: string
  pieChart: string
}

defineProps<{
  error: string
  graph?: StateGraph
  loading: boolean
}>()
</script>
