<template>
  <section class="transaction-graph-panel">
    <div v-if="loading" class="empty-state">正在加载图表数据...</div>
    <div v-else-if="error" class="empty-state">{{ error }}</div>
    <template v-else-if="graph">
      <div v-if="!graph.historyMode" class="transaction-svg-wrap">
        <svg version="1.1" width="980" height="380" xmlns="http://www.w3.org/2000/svg">
          <g v-for="(content, index) in hourlyGraphs" :key="index" v-html="content"></g>
        </svg>
      </div>

      <div v-if="graph.distributionDetails.length" class="transaction-table-wrap">
        <table class="transaction-table distribution-table">
          <thead>
            <tr>
              <td class="center" colspan="8"><strong>分布统计</strong></td>
            </tr>
            <tr>
              <th class="right">Ip</th>
              <th class="right">Total</th>
              <th class="right">Failure</th>
              <th class="right">Failure%</th>
              <th class="right">Min(ms)</th>
              <th class="right">Max(ms)</th>
              <th class="right">Avg(ms)</th>
              <th class="right">Std(ms)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in graph.distributionDetails" :key="item.ip">
              <td class="right">{{ item.ip }}</td>
              <td class="right">{{ formatInteger(item.totalCount) }}</td>
              <td class="right">{{ formatInteger(item.failCount) }}</td>
              <td class="right">{{ formatRate(item.failPercent, 4) }}</td>
              <td class="right">{{ formatDecimal(item.min, 1) }}</td>
              <td class="right">{{ formatDecimal(item.max, 1) }}</td>
              <td class="right">{{ formatDecimal(item.avg, 1) }}</td>
              <td class="right">{{ formatDecimal(item.std, 1) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <PieChartPanel :chart="graph.distributionChart" title="分布统计" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import PieChartPanel from './PieChartPanel.vue'

interface TransactionDistributionDetail {
  avg: number
  failCount: number
  failPercent: number
  ip: string
  max: number
  min: number
  std: number
  totalCount: number
}

interface TransactionGraph {
  distributionChart: string
  distributionDetails: TransactionDistributionDetail[]
  errorTrend: string
  graph1: string
  graph2: string
  graph3: string
  graph4: string
  hitTrend: string
  historyMode: boolean
  responseTrend: string
}

const props = defineProps<{
  error: string
  formatDecimal: (value: number, digits: number) => string
  formatInteger: (value: number) => string
  formatRate: (value: number, digits: number) => string
  graph?: TransactionGraph
  loading: boolean
}>()

const hourlyGraphs = computed(() => {
  const graph = props.graph

  if (!graph) {
    return []
  }
  return [graph.graph1, graph.graph2, graph.graph3, graph.graph4].filter(Boolean)
})
</script>
