<template>
  <section v-if="chartSeries.length" class="vue-line-chart">
    <h3 v-if="allowHtmlTitle" v-html="chartTitle"></h3>
    <h3 v-else>{{ chartTitle }}</h3>
    <div ref="chartElement" class="echarts-line"></div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { LineChart as EchartsLineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'

interface LineChart {
  datas?: Array<Record<string, number>>
  htmlTitle?: string
  maxYlabel?: number
  minTickInterval?: number
  minYlable?: number
  size?: number
  start?: string
  step?: number
  subTitles?: string[]
  title?: string
  unit?: string
  valueObjects?: number[][]
  values?: number[][]
}

interface TooltipParam {
  axisValueLabel?: string
  marker?: string
  seriesName?: string
  value?: unknown
}

const props = defineProps<{
  allowHtmlTitle?: boolean
  chart?: LineChart | string
  smooth?: boolean
  title?: string
}>()

use([CanvasRenderer, EchartsLineChart, GridComponent, LegendComponent, TooltipComponent])

const chartElement = ref<HTMLDivElement | null>(null)
let chartInstance: ECharts | null = null
let resizeObserver: ResizeObserver | null = null

const chart = computed(() => parseChart(props.chart))
const chartTitle = computed(() => props.title || chart.value?.htmlTitle || chart.value?.title || '')
const rawSeries = computed(() => {
  const source = chart.value

  if (!source) {
    return []
  }
  const values = source.values?.length ? source.values : source.valueObjects?.length ? source.valueObjects : dataMaps(source.datas)

  return (values || []).map((items, index) => ({
    name: source.subTitles?.[index] || `Series ${index + 1}`,
    values: items.map((item) => Number(item) || 0)
  })).filter((series) => series.values.length)
})
const chartSeries = computed(() => rawSeries.value.map((series) => ({
  data: series.values,
  name: series.name,
  showSymbol: false,
  smooth: props.smooth || false,
  type: 'line'
})))
const categories = computed(() => buildCategories(chart.value, maxSeriesLength.value))
const maxSeriesLength = computed(() => Math.max(0, ...rawSeries.value.map((series) => series.values.length)))
const xLabelInterval = computed(() => {
  const size = categories.value.length

  if (size <= 8) {
    return 0
  }
  return Math.max(0, Math.ceil(size / 4) - 1)
})

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

watch([chartSeries, categories], () => renderChart(), { deep: true })

async function renderChart() {
  await nextTick()
  if (!chartElement.value || !chartSeries.value.length) {
    return
  }
  if (!chartInstance) {
    chartInstance = init(chartElement.value)
  }
  chartInstance.setOption(option(), true)
  chartInstance.resize()
}

function option(): EChartsCoreOption {
  return {
    color: ['#7cb5ec', '#434348', '#90ed7d', '#f7a35c', '#8085e9', '#f15c80'],
    grid: {
      bottom: 78,
      containLabel: true,
      left: 56,
      right: 18,
      top: 28
    },
    legend: {
      bottom: 10,
      itemHeight: 3,
      itemWidth: 28,
      textStyle: {
        color: '#444',
        fontSize: 13,
        fontWeight: 700
      }
    },
    series: chartSeries.value,
    tooltip: {
      formatter: (params: TooltipParam | TooltipParam[]) => formatTooltip(params),
      trigger: 'axis'
    },
    xAxis: {
      axisLabel: {
        color: '#666',
        fontSize: 12,
        interval: xLabelInterval.value
      },
      axisLine: {
        lineStyle: {
          color: '#ccd6e0'
        }
      },
      axisTick: {
        alignWithLabel: true
      },
      boundaryGap: false,
      data: categories.value,
      type: 'category'
    },
    yAxis: {
      axisLabel: {
        color: '#666',
        fontSize: 12
      },
      axisLine: {
        show: false
      },
      min: chart.value?.minYlable ?? 0,
      minInterval: chart.value?.minTickInterval || undefined,
      name: chart.value?.unit ? 'Values' : '',
      nameLocation: 'middle',
      nameGap: 42,
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#d7d7d7',
          width: 1
        }
      },
      type: 'value'
    }
  }
}

function buildCategories(source: LineChart | null, size: number) {
  const dataKeys = firstDataKeys(source?.datas)

  if (dataKeys.length) {
    return dataKeys.map((item) => formatDateLabel(new Date(item), source?.step || 0))
  }
  const start = parseStart(source?.start)
  const step = source?.step || 0

  if (start && step > 0) {
    return Array.from({ length: source?.size || size }, (_, index) => formatDateLabel(new Date(start.getTime() + step * index), step))
  }
  return Array.from({ length: size }, (_, index) => String(index + 1))
}

function dataMaps(datas?: Array<Record<string, number>>) {
  return (datas || []).map((item) => Object.keys(item)
    .sort((first, second) => Number(first) - Number(second))
    .map((key) => Number(item[key]) || 0))
}

function firstDataKeys(datas?: Array<Record<string, number>>) {
  const first = datas?.[0]

  if (!first) {
    return []
  }
  return Object.keys(first).map((key) => Number(key)).filter((key) => Number.isFinite(key)).sort((left, right) => left - right)
}

function formatDateLabel(date: Date, step: number) {
  if (step >= 24 * 60 * 60 * 1000) {
    return `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
  }
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function formatTooltip(params: TooltipParam | TooltipParam[]) {
  const items = Array.isArray(params) ? params : [params]
  const title = items[0]?.axisValueLabel || ''
  const lines = items.map((item) => `${item.marker || ''}${item.seriesName || ''}: ${formatNumber(Number(item.value) || 0)}`)

  return [title, ...lines].filter(Boolean).join('<br/>')
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 }).format(value)
}

function pad(value: number) {
  return String(value).padStart(2, '0')
}

function parseChart(value?: LineChart | string) {
  if (!value) {
    return null
  }
  if (typeof value !== 'string') {
    return value
  }
  try {
    return JSON.parse(value) as LineChart
  } catch (error) {
    return null
  }
}

function parseStart(value?: string) {
  if (!value) {
    return null
  }
  const match = value.match(/^(\d{4})\/(\d{2})\/(\d{2}) (\d{2}):(\d{2})$/)

  if (!match) {
    return null
  }
  return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]), Number(match[4]), Number(match[5]))
}
</script>
