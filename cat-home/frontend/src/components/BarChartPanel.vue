<template>
  <section v-if="items.length" class="vue-bar-chart">
    <h3>{{ title }}</h3>
    <div ref="chartElement" class="echarts-bar"></div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'
import { getChartColors, onThemeChange } from '../theme'

interface BarChartData {
  categories?: string[]
  seriesName?: string
  title?: string
  values?: number[]
}

interface TooltipParam {
  axisValueLabel?: string
  marker?: string
  seriesName?: string
  value?: unknown
}

const props = defineProps<{
  chart?: BarChartData | string
  title?: string
}>()

use([BarChart, CanvasRenderer, GridComponent, TooltipComponent])

const chartElement = ref<HTMLDivElement | null>(null)
let chartInstance: ECharts | null = null
let resizeObserver: ResizeObserver | null = null

const chart = computed(() => parseChart(props.chart))
const title = computed(() => props.title || chart.value?.title || '')
const categories = computed(() => chart.value?.categories || [])
const items = computed(() => (chart.value?.values || []).map((value) => Number(value) || 0))

onMounted(() => {
  renderChart()
  if (chartElement.value) {
    resizeObserver = new ResizeObserver(() => chartInstance?.resize())
    resizeObserver.observe(chartElement.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chartInstance?.dispose()
  resizeObserver = null
  chartInstance = null
})

watch([categories, items], () => renderChart(), { deep: true })

onThemeChange(() => renderChart())

async function renderChart() {
  await nextTick()
  if (!chartElement.value || !items.value.length) {
    return
  }
  if (!chartInstance) {
    chartInstance = init(chartElement.value)
  }
  chartInstance.setOption(option(), true)
  chartInstance.resize()
}

function option(): EChartsCoreOption {
  const chartColors = getChartColors()
  return {
    color: [chartColors.palette[0]],
    grid: {
      bottom: 54,
      containLabel: true,
      left: 56,
      right: 18,
      top: 28
    },
    series: [
      {
        barMaxWidth: 28,
        data: items.value,
        name: chart.value?.seriesName || 'Value',
        type: 'bar'
      }
    ],
    tooltip: {
      formatter: (params: TooltipParam | TooltipParam[]) => formatTooltip(params),
      trigger: 'axis'
    },
    xAxis: {
      axisLabel: {
        color: chartColors.textColor,
        fontSize: 12,
        interval: 0,
        rotate: 45
      },
      axisLine: {
        lineStyle: {
          color: chartColors.axisColor
        }
      },
      data: categories.value,
      type: 'category'
    },
    yAxis: {
      axisLabel: {
        color: chartColors.textColor,
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: chartColors.splitColor,
          width: 1
        }
      },
      type: 'value'
    }
  }
}

function formatTooltip(params: TooltipParam | TooltipParam[]) {
  const item = Array.isArray(params) ? params[0] : params
  const value = Number(item.value) || 0

  return `${item.axisValueLabel || ''}<br/>${item.marker || ''}${item.seriesName || ''}: ${formatNumber(value)}`
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 }).format(value)
}

function parseChart(value?: BarChartData | string) {
  if (!value) {
    return null
  }
  if (typeof value !== 'string') {
    return value
  }
  try {
    return JSON.parse(value) as BarChartData
  } catch (error) {
    return null
  }
}
</script>
