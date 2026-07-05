<template>
  <section class="heartbeat-chart">
    <div ref="chartElement" class="heartbeat-echarts-bar"></div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { BarChart } from 'echarts/charts'
import { GridComponent, TitleComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'
import { getChartColors, onThemeChange } from '../theme'

interface TooltipParam {
  axisValueLabel?: string
  marker?: string
  seriesName?: string
  value?: unknown
}

const props = defineProps<{
  label: string
  title: string
  values: number[]
}>()

use([BarChart, CanvasRenderer, GridComponent, TitleComponent, TooltipComponent])

const chartElement = ref<HTMLDivElement | null>(null)
let chartInstance: ECharts | null = null
let resizeObserver: ResizeObserver | null = null

const categories = computed(() => Array.from({ length: 61 }, (_, index) => String(index)))
const seriesValues = computed(() => {
  const values = props.values.map((value) => Number(value) || 0).slice(0, 60)

  return [...values, null]
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

watch(seriesValues, () => renderChart(), { deep: true })

onThemeChange(() => renderChart())

async function renderChart() {
  await nextTick()
  if (!chartElement.value) {
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
    animation: false,
    color: [chartColors.palette[0]],
    grid: {
      bottom: 34,
      containLabel: true,
      left: 52,
      right: 12,
      top: 28
    },
    series: [
      {
        barCategoryGap: '10%',
        barGap: '0%',
        data: seriesValues.value,
        name: props.title,
        type: 'bar'
      }
    ],
    title: {
      left: 'center',
      text: props.title,
      textStyle: {
        color: '#111827',
        fontSize: 14,
        fontWeight: 700
      },
      top: 0
    },
    tooltip: {
      formatter: (params: TooltipParam | TooltipParam[]) => formatTooltip(params),
      trigger: 'axis'
    },
    xAxis: {
      axisLabel: {
        color: '#111827',
        fontSize: 11,
        hideOverlap: false,
        interval: shouldShowMinuteLabel
      },
      axisLine: {
        lineStyle: {
          color: chartColors.axisColor
        }
      },
      axisTick: {
        alignWithLabel: true,
        interval: showEveryMinuteTick,
        length: 5,
        lineStyle: {
          color: chartColors.axisColor
        },
        show: true
      },
      data: categories.value,
      name: 'Minute',
      nameLocation: 'middle',
      nameGap: 22,
      nameTextStyle: {
        color: '#111827',
        fontSize: 12
      },
      type: 'category'
    },
    yAxis: {
      axisLabel: {
        color: '#111827',
        fontSize: 11
      },
      axisLine: {
        lineStyle: {
          color: chartColors.axisColor
        },
        show: true
      },
      name: props.label || '',
      nameLocation: 'middle',
      nameGap: 34,
      nameTextStyle: {
        color: '#111827',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: chartColors.splitColor,
          type: 'dotted'
        }
      },
      type: 'value'
    }
  }
}

function shouldShowMinuteLabel(index: number) {
  return index % 10 === 0
}

function showEveryMinuteTick(index: number) {
  return index <= 60
}

function formatTooltip(params: TooltipParam | TooltipParam[]) {
  const item = Array.isArray(params) ? params[0] : params
  const rawValue = Number(item.value)

  if (!Number.isFinite(rawValue) || rawValue === 0) {
    return ''
  }

  return `Minute ${item.axisValueLabel || ''}<br/>${item.marker || ''}${props.title}: ${formatNumber(rawValue)}`
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 }).format(value)
}
</script>
