<template>
  <section class="report-graph-panel">
    <div v-if="loading" class="empty-state">正在加载图表数据...</div>
    <div v-else-if="error" class="empty-state">{{ error }}</div>
    <template v-else-if="graph">
      <div v-if="!graph.historyMode" class="transaction-svg-wrap">
        <svg version="1.1" width="980" height="190" xmlns="http://www.w3.org/2000/svg">
          <g v-for="(content, index) in hourlyGraphs" :key="index" v-html="content"></g>
        </svg>
      </div>
      <div v-else class="line-chart-grid">
        <LineChartPanel :chart="graph.hitTrend" />
        <LineChartPanel :chart="graph.failureTrend" />
      </div>
      <table v-if="graph.distributionDetails.length" class="transaction-table distribution-table">
        <thead>
          <tr>
            <td class="center" colspan="4"><strong>分布统计</strong></td>
          </tr>
          <tr>
            <th class="right">Ip</th>
            <th class="right">Total</th>
            <th class="right">Failure</th>
            <th class="right">Failure%</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in graph.distributionDetails" :key="item.ip">
            <td class="right">{{ item.ip }}</td>
            <td class="right">{{ formatInteger(item.totalCount) }}</td>
            <td class="right">{{ formatInteger(item.failCount) }}</td>
            <td class="right">{{ formatRate(item.failPercent / 100, 4) }}</td>
          </tr>
        </tbody>
      </table>
      <PieChartPanel :chart="graph.distributionChart" title="错误分布" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import LineChartPanel from './LineChartPanel.vue'
import PieChartPanel from './PieChartPanel.vue'

interface EventDistributionDetail {
  failCount: number
  failPercent: number
  ip: string
  totalCount: number
}

interface EventGraph {
  distributionChart: string
  distributionDetails: EventDistributionDetail[]
  failureTrend: string
  graph1: string
  graph2: string
  historyMode: boolean
  hitTrend: string
}

const props = defineProps<{
  error: string
  formatInteger: (value: number) => string
  formatRate: (value: number, digits: number) => string
  graph?: EventGraph
  loading: boolean
}>()

const hourlyGraphs = computed(() => {
  const graph = props.graph

  if (!graph) {
    return []
  }
  return [graph.graph1, graph.graph2].filter(Boolean)
})
</script>
