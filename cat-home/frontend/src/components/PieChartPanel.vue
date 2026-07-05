<template>
  <section v-if="items.length" class="vue-pie-chart">
    <h3 v-if="title">{{ title }}</h3>
    <div ref="chartElement" class="echarts-pie" :style="{ height: chartHeight }"></div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'
import { getChartColors, onThemeChange } from '../theme'

interface PieChartData {
  items?: Array<{ number: number; title: string }>
  title?: string
}

interface TooltipParam {
  name?: string
  value?: unknown
}

const visibleLabelPercentThreshold = 0.95

const props = defineProps<{
  chart?: PieChartData | string
  title?: string
}>()

use([CanvasRenderer, LegendComponent, PieChart, TooltipComponent])

const chartElement = ref<HTMLDivElement | null>(null)
let chartInstance: ECharts | null = null
let resizeObserver: ResizeObserver | null = null

const chart = computed(() => parseChart(props.chart))
const title = computed(() => props.title || chart.value?.title || '')
const total = computed(() => items.value.reduce((sum, item) => sum + item.value, 0))
const chartHeight = computed(() => {
  if (items.value.length >= 28) {
    return '660px'
  }
  if (items.value.length >= 20) {
    return '600px'
  }
  return '520px'
})
const items = computed(() => (chart.value?.items || [])
  .map((item) => ({
    name: item.title,
    value: Number(item.number) || 0
  }))
  .filter((item) => item.value > 0))
const chartItems = computed(() => items.value.map((item) => {
  const percent = total.value ? (item.value / total.value) * 100 : 0
  const showLabel = percent >= visibleLabelPercentThreshold

  return {
    ...item,
    label: {
      show: showLabel
    },
    labelLine: {
      show: showLabel
    }
  }
}))

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

watch(items, () => renderChart(), { deep: true })

onThemeChange(() => renderChart())

async function renderChart() {
  await nextTick()
  if (!chartElement.value || !items.value.length) {
    return
  }
  if (!chartInstance) {
    chartInstance = init(chartElement.value)
  }
  chartInstance.setOption(option())
  chartInstance.resize()
}

function option(): EChartsCoreOption {
  const chartColors = getChartColors()
  return {
    color: chartColors.palette,
    legend: {
      bottom: 0,
      itemGap: 12,
      itemHeight: 13,
      itemWidth: 20,
      left: 32,
      right: 32,
      textStyle: {
        color: '#333333',
        fontSize: 14,
        fontWeight: 600
      },
      type: 'plain'
    },
    series: [
      {
        avoidLabelOverlap: true,
        center: ['50%', '34%'],
        data: chartItems.value,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.25)',
            shadowOffsetX: 0
          }
        },
        label: {
          color: '#111827',
          formatter: (params: { name?: string; percent?: number }) => {
            return `${formatLabel(params.name || '')}: ${Number(params.percent || 0).toFixed(1)} %`
          },
          fontSize: 14,
          fontWeight: 700,
          lineHeight: 18
        },
        labelLine: {
          length: 18,
          length2: 8,
          lineStyle: {
            color: '#111111',
            width: 1.5
          }
        },
        minAngle: 2,
        name: title.value || 'share',
        radius: '24%',
        stillShowZeroSum: false,
        type: 'pie'
      }
    ],
    tooltip: {
      formatter: (params: TooltipParam | TooltipParam[]) => {
        const item = Array.isArray(params) ? params[0] : params
        const value = Number(item.value) || 0
        const percent = total.value ? (value / total.value) * 100 : 0

        return `${item.name}<br/>${formatInteger(value)} (${percent.toFixed(1)}%)`
      },
      trigger: 'item'
    }
  }
}

function formatInteger(value: number) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value || 0)
}

function formatLabel(value: string) {
  if (value.length <= 80) {
    return value
  }
  const lastSlash = value.lastIndexOf('/')
  const fileName = lastSlash >= 0 ? value.substring(lastSlash + 1) : ''

  if (fileName && fileName.length < 36) {
    return `${value.substring(0, 42)}.../${fileName}`
  }
  return `${value.substring(0, 72)}...`
}

function parseChart(value?: PieChartData | string) {
  if (!value) {
    return null
  }
  if (typeof value !== 'string') {
    return value
  }
  try {
    return JSON.parse(value) as PieChartData
  } catch (error) {
    return null
  }
}
</script>
