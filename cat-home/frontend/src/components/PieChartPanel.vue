<template>
  <section v-if="items.length" class="vue-pie-chart">
    <h3>{{ title }}</h3>
    <div class="pie-summary">
      <span
        v-for="(item, index) in items"
        :key="item.title"
        :style="{ width: `${item.percent}%`, backgroundColor: colors[index % colors.length] }"
      ></span>
    </div>
    <table class="transaction-table mini-chart-table">
      <tbody>
        <tr v-for="(item, index) in items" :key="item.title">
          <td><i :style="{ backgroundColor: colors[index % colors.length] }"></i>{{ item.title }}</td>
          <td class="right">{{ formatInteger(item.number) }}</td>
          <td class="right">{{ formatPercent(item.percent / 100, 1) }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface PieChart {
  items?: Array<{ number: number; title: string }>
  title?: string
}

const props = defineProps<{
  chart?: PieChart | string
  title?: string
}>()

const colors = ['#0f766e', '#175cd3', '#b42318', '#b54708', '#7a5af8', '#067647', '#3538cd']
const chart = computed(() => parseChart(props.chart))
const total = computed(() => Math.max(1, (chart.value?.items || []).reduce((sum, item) => sum + (Number(item.number) || 0), 0)))
const title = computed(() => props.title || chart.value?.title || '分布统计')
const items = computed(() => (chart.value?.items || []).map((item) => ({
  number: Number(item.number) || 0,
  percent: ((Number(item.number) || 0) / total.value) * 100,
  title: item.title
})))

function formatInteger(value: number) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value || 0)
}

function formatPercent(value: number, digits: number) {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: digits,
    minimumFractionDigits: digits,
    style: 'percent'
  }).format(value || 0)
}

function parseChart(value?: PieChart | string) {
  if (!value) {
    return null
  }
  if (typeof value !== 'string') {
    return value
  }
  try {
    return JSON.parse(value) as PieChart
  } catch (error) {
    return null
  }
}
</script>
