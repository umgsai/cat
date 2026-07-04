<template>
  <section v-if="items.length" class="vue-pie-chart">
    <h3 v-if="title">{{ title }}</h3>
    <div ref="chartElement" class="echarts-pie"></div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'

interface PieChartData {
  items?: Array<{ number: number; title: string }>
  title?: string
}

interface TooltipParam {
  name?: string
  value?: unknown
}

const visibleLabelPercentThreshold = 0.75

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
  return {
    color: [
      '#7cb5ec',
      '#434348',
      '#90ed7d',
      '#f7a35c',
      '#8085e9',
      '#f15c80',
      '#e4d354',
      '#2b908f',
      '#f45b5b',
      '#91e8e1'
    ],
    legend: {
      bottom: 0,
      itemGap: 10,
      itemHeight: 12,
      itemWidth: 18,
      left: 20,
      right: 20,
      type: 'plain'
    },
    series: [
      {
        avoidLabelOverlap: true,
        center: ['50%', '43%'],
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
            return `${wrapLabel(params.name || '')}: ${Number(params.percent || 0).toFixed(2)} %`
          },
          fontSize: 13,
          fontWeight: 700,
          lineHeight: 17
        },
        labelLine: {
          length: 16,
          length2: 10,
          lineStyle: {
            color: '#2f2f2f',
            width: 1.2
          }
        },
        minAngle: 2,
        name: title.value || 'share',
        radius: '36%',
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

function wrapLabel(value: string) {
  const maxLineLength = 42
  const lines: string[] = []
  let currentLine = ''

  for (const segment of value.split('.')) {
    const nextPart = currentLine ? `.${segment}` : segment

    if (currentLine && currentLine.length + nextPart.length > maxLineLength) {
      lines.push(currentLine)
      currentLine = segment
    } else {
      currentLine += nextPart
    }
  }
  if (currentLine) {
    lines.push(currentLine)
  }
  return lines.length ? lines.join('\n') : value
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
