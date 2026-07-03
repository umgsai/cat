<template>
  <section class="report-graph-panel event-graph-panel">
    <div v-if="loading" class="empty-state">正在加载图表数据...</div>
    <div v-else-if="error" class="empty-state">{{ error }}</div>
    <template v-else-if="graph">
      <div class="line-chart-grid event-chart-grid">
        <LineChartPanel v-if="graph.hitTrend" :chart="graph.hitTrend" smooth />
        <LineChartPanel v-if="graph.failureTrend" :chart="graph.failureTrend" smooth />
      </div>
      <div v-if="graph.distributionDetails.length" class="report-table-wrap">
        <table class="report-table distribution-table">
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
      </div>
      <PieChartPanel :chart="graph.distributionChart" title="错误分布" />
    </template>
  </section>
</template>

<script setup lang="ts">
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

defineProps<{
  error: string
  formatInteger: (value: number) => string
  formatRate: (value: number, digits: number) => string
  graph?: EventGraph
  loading: boolean
}>()
</script>
