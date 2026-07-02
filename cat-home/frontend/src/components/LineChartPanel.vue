<template>
  <section class="vue-line-chart">
    <h3>{{ chartTitle }}</h3>
    <svg :viewBox="`0 0 ${width} ${height}`" role="img">
      <line :x1="padding.left" :y1="plotBottom" :x2="plotRight" :y2="plotBottom" class="chart-axis" />
      <line :x1="padding.left" :y1="padding.top" :x2="padding.left" :y2="plotBottom" class="chart-axis" />
      <g v-for="tick in yTicks" :key="tick.value">
        <line :x1="padding.left" :y1="tick.y" :x2="plotRight" :y2="tick.y" class="chart-grid" />
        <text :x="padding.left - 8" :y="tick.y + 4" text-anchor="end" class="chart-label">{{ formatTick(tick.value) }}</text>
      </g>
      <polyline
        v-for="(series, index) in chartSeries"
        :key="series.name"
        :points="series.points"
        :stroke="colors[index % colors.length]"
        class="chart-line"
      />
    </svg>
    <div class="chart-legend">
      <span v-for="(series, index) in chartSeries" :key="series.name">
        <i :style="{ backgroundColor: colors[index % colors.length] }"></i>
        {{ series.name }}
      </span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface LineChart {
  datas?: Array<Record<string, number>>
  htmlTitle?: string
  size?: number
  start?: string
  step?: number
  subTitles?: string[]
  title?: string
  valueObjects?: number[][]
  values?: number[][]
}

const props = defineProps<{
  chart?: LineChart | string
  title?: string
}>()

const width = 520
const height = 260
const padding = { bottom: 32, left: 56, right: 18, top: 26 }
const colors = ['#0f766e', '#b42318', '#175cd3', '#b54708', '#7a5af8']
const plotRight = width - padding.right
const plotBottom = height - padding.bottom
const plotWidth = plotRight - padding.left
const plotHeight = plotBottom - padding.top

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
const maxValue = computed(() => Math.max(1, ...rawSeries.value.flatMap((series) => series.values)))
const chartSeries = computed(() => rawSeries.value.map((series) => ({
  name: series.name,
  points: series.values.map((value, index) => `${x(index, series.values.length)},${y(value)}`).join(' ')
})))
const yTicks = computed(() => [0, 0.25, 0.5, 0.75, 1].map((ratio) => {
  const value = maxValue.value * ratio

  return { value, y: y(value) }
}))

function dataMaps(datas?: Array<Record<string, number>>) {
  return (datas || []).map((item) => Object.keys(item).sort().map((key) => Number(item[key]) || 0))
}

function formatTick(value: number) {
  if (value >= 1000) {
    return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value)
  }
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 1 }).format(value)
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

function x(index: number, length: number) {
  if (length <= 1) {
    return padding.left
  }
  return padding.left + (plotWidth * index) / (length - 1)
}

function y(value: number) {
  return plotBottom - (plotHeight * value) / maxValue.value
}
</script>
