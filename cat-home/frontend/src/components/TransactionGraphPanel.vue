<template>
  <section class="report-graph-panel transaction-graph-panel">
    <div v-if="loading" class="empty-state">正在加载图表数据...</div>
    <div v-else-if="error" class="empty-state">{{ error }}</div>
    <template v-else-if="graph">
      <div class="line-chart-grid transaction-chart-grid">
        <BarChartPanel v-if="graph.durationDistribution" :chart="graph.durationDistribution" />
        <LineChartPanel v-if="graph.hitTrend" :chart="graph.hitTrend" smooth />
        <LineChartPanel v-if="graph.responseTrend" :chart="graph.responseTrend" smooth />
        <LineChartPanel v-if="graph.errorTrend" :chart="graph.errorTrend" smooth />
      </div>

      <div v-if="graph.distributionDetails.length" class="report-table-wrap">
        <table class="report-table distribution-table">
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
import BarChartPanel from './BarChartPanel.vue'
import LineChartPanel from './LineChartPanel.vue'
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
  durationDistribution: string
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

defineProps<{
  error: string
  formatDecimal: (value: number, digits: number) => string
  formatInteger: (value: number) => string
  formatRate: (value: number, digits: number) => string
  graph?: TransactionGraph
  loading: boolean
}>()
</script>
