<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="businessUrl({})">Application</a>
        <a :href="legacyUrl('/mvc/s/config?op=projects')">Configs</a>
        <a :href="legacyUrl('/mvc/r/home?op=view&docName=index')">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
        <span class="user-greeting">欢迎，admin</span>
      </div>
    </header>

    <div class="cat-body">
      <ReportSidebar
        active-report="Business"
        :context-path="contextPath"
        :domain="currentDomain"
      />

      <section class="cat-content">
        <div class="query-bar">
          <div class="time-range business-ranges">
            <span>时间段</span>
            <span v-for="range in report?.ranges || []" :key="range.duration">
              [
              <a
                :class="{ current: range.duration === report?.timeRange }"
                :href="businessUrl({ endDate: currentEndTime, startDate: '', timeRange: String(range.duration) })"
              >
                {{ range.title }}
              </a>
              ]
            </span>
          </div>
          <div class="time-shortcuts">
            <span v-for="nav in report?.navs || []" :key="nav.title">
              [
              <a :href="businessUrl({ endDate: currentEndTime, startDate: '', step: String(nav.hours), timeRange: String(currentTimeRange) })">
                {{ nav.title }}
              </a>
              ]
            </span>
            <span>
              [
              <a :href="businessUrl({ endDate: '', startDate: '', timeRange: String(currentTimeRange) })">now</a>
              ]
            </span>
          </div>
        </div>

        <section class="transaction-card business-query">
          <span>开始</span>
          <el-date-picker
            v-model="startTimeInput"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            :clearable="false"
            :editable="true"
          />
          <span>结束</span>
          <el-date-picker
            v-model="endTimeInput"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            :clearable="false"
            :editable="true"
          />
          <span class="business-query-label">查询条件</span>
          <el-tooltip content="输入 domain 或者标签，标签以 TAG_ 开头" placement="top">
            <span class="business-help">?</span>
          </el-tooltip>
          <el-autocomplete
            v-model="queryInput"
            class="business-search"
            placeholder="input domain for search"
            :fetch-suggestions="searchItems"
            value-key="value"
            clearable
            @select="selectQuery"
            @keyup.enter="queryBusiness"
          >
            <template #default="{ item }">
              <div class="business-suggestion">
                <span>{{ item.value }}</span>
                <small>{{ item.category }}</small>
              </div>
            </template>
          </el-autocomplete>
          <button class="domain-go" type="button" @click="queryBusiness">Go</button>
        </section>

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 Business 数据...
        </section>

        <section v-else-if="!charts.length" class="empty-state"></section>

        <section v-else class="business-chart-grid">
          <article v-for="chart in charts" :key="chart.id" class="business-chart-card">
            <LineChartPanel allow-html-title :chart="chart" />
          </article>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import LineChartPanel from '../components/LineChartPanel.vue'
import ReportSidebar from '../components/ReportSidebar.vue'

interface BusinessChart {
  datas: Array<Record<string, number>>
  htmlTitle: string
  id: string
  start: string
  step: number
  subTitles: string[]
  title: string
  unit: string
}

interface BusinessReport {
  contextPath: string
  displayDomain: string
  domain: string
  domains: string[]
  endTime: string
  lineCharts: BusinessChart[]
  name: string
  navs: Array<{ hours: number; title: string }>
  ranges: Array<{ duration: number; title: string }>
  startTime: string
  tags: string[]
  timeRange: number
  type: string
}

const report = ref<BusinessReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const queryInput = ref('')
const startTimeInput = ref('')
const endTimeInput = ref('')

const charts = computed(() => report.value?.lineCharts || [])
const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return report.value?.contextPath || '/cat'
})
const currentParams = computed(() => new URLSearchParams(window.location.search))
const currentDomain = computed(() => report.value?.domain || currentParams.value.get('name') || 'cat')
const currentEndTime = computed(() => report.value?.endTime || currentParams.value.get('endDate') || '')
const currentName = computed(() => report.value?.name || currentParams.value.get('name') || 'cat')
const currentStartTime = computed(() => report.value?.startTime || currentParams.value.get('startDate') || '')
const currentTimeRange = computed(() => report.value?.timeRange || Number(currentParams.value.get('timeRange') || 4))
const currentType = computed(() => report.value?.type || currentParams.value.get('type') || 'domain')

const suggestions = computed(() => {
  const items: Array<{ category: string; value: string }> = []

  for (const tag of report.value?.tags || []) {
    items.push({ category: '标签', value: `TAG_${tag}` })
  }
  for (const domain of report.value?.domains || []) {
    items.push({ category: '项目', value: domain })
  }
  return items
})

onMounted(() => {
  loadReport()
})

async function loadReport() {
  loading.value = true
  loadError.value = ''

  try {
    const response = await fetch(dataUrl(), { headers: { Accept: 'application/json' } })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const data = await response.json() as BusinessReport

    report.value = data
    queryInput.value = data.type === 'tag' ? `TAG_${data.name}` : data.name
    startTimeInput.value = data.startTime
    endTimeInput.value = data.endTime
  } catch (error) {
    loadError.value = `Business 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function businessUrl(overrides: Record<string, string | undefined>) {
  const params = new URLSearchParams()
  const endDate = Object.prototype.hasOwnProperty.call(overrides, 'endDate') ? overrides.endDate : currentEndTime.value
  const startDate = Object.prototype.hasOwnProperty.call(overrides, 'startDate') ? overrides.startDate : currentStartTime.value
  const timeRange = Object.prototype.hasOwnProperty.call(overrides, 'timeRange') ? overrides.timeRange : String(currentTimeRange.value)

  params.set('op', 'view')
  params.set('name', overrides.name ?? currentName.value)
  params.set('type', overrides.type ?? currentType.value)
  if (startDate) {
    params.set('startDate', startDate)
  }
  if (endDate) {
    params.set('endDate', endDate)
  }
  if (timeRange) {
    params.set('timeRange', timeRange)
  }
  if (overrides.step) {
    params.set('step', overrides.step)
  }
  return `${contextPath.value}/mvc/vue/r/business?${params.toString()}`
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)

  params.set('op', 'vueData')
  return `${contextPath.value}/mvc/r/business?${params.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function queryBusiness() {
  let queryName = queryInput.value.trim()
  let queryType = 'domain'

  if (queryName.startsWith('TAG_')) {
    queryType = 'tag'
    queryName = queryName.substring(4)
  }
  if (timeRangeTooLarge(startTimeInput.value, endTimeInput.value)) {
    window.alert('选择的时间间隔不要超过两天')
    return
  }
  window.location.href = businessUrl({
    endDate: endTimeInput.value,
    name: queryName || currentName.value,
    startDate: startTimeInput.value,
    type: queryType
  })
}

function searchItems(query: string, callback: (items: Array<{ category: string; value: string }>) => void) {
  const keyword = query.trim().toLowerCase()

  if (!keyword) {
    callback(suggestions.value)
    return
  }
  callback(suggestions.value.filter((item) => item.value.toLowerCase().includes(keyword)))
}

function selectQuery(item: { value: string }) {
  queryInput.value = item.value
  queryBusiness()
}

function timeRangeTooLarge(start: string, end: string) {
  const startDate = new Date(start.replace(/-/g, '/'))
  const endDate = new Date(end.replace(/-/g, '/'))

  return endDate.getTime() - startDate.getTime() > 2 * 24 * 60 * 60 * 1000
}

</script>
