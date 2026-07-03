<template>
  <section class="report-graph-panel problem-graph-panel">
    <div v-if="loading" class="empty-state">正在加载图表数据...</div>
    <div v-else-if="error" class="empty-state">{{ error }}</div>
    <div v-else-if="graph" class="line-chart-grid problem-chart-grid">
      <LineChartPanel :chart="graph.errorsTrend" title="数量" smooth />
      <PieChartPanel :chart="graph.distributionChart" title="分布" />
    </div>
  </section>
</template>

<script setup lang="ts">
import LineChartPanel from './LineChartPanel.vue'
import PieChartPanel from './PieChartPanel.vue'

interface ProblemGraph {
  distributionChart: string
  errorsTrend: string
  historyMode: boolean
}

defineProps<{
  error: string
  graph?: ProblemGraph
  loading: boolean
}>()
</script>
