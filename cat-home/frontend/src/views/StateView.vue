<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="stateUrl({})">Application</a>
        <a :href="legacyUrl('/mvc/vue/s/config?op=projects')">Configs</a>
        <a :href="legacyUrl('/mvc/vue/r/home?op=view&docName=index')">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
        <span class="user-greeting">欢迎，admin</span>
      </div>
    </header>

    <div class="cat-body">
      <ReportSidebar
        active-report="State"
        :context-path="contextPath"
        :date="currentDate"
        :domain="currentDomain"
        :ip="currentIp"
        :report-type="currentReportType"
      />

      <section class="cat-content">
        <div class="query-bar">
          <div class="time-range">
            <span v-if="report">{{ report.reportStart }} to {{ report.reportEnd }}</span>
            <span v-else>Loading...</span>
          </div>
          <div class="time-shortcuts">
            <span>
              [
              <a class="mode-link" :href="modeSwitchUrl">{{ modeSwitchText }}</a>
              ]
            </span>
            <template v-if="isHistoryMode">
              <span v-for="nav in report?.historyNavs || []" :key="nav.title">
                [
                <a :class="{ current: nav.title === currentReportType }" :href="stateUrl({ op: 'history', reportType: nav.title })">
                  {{ nav.title }}
                </a>
                ]
              </span>
              <span>
                [
                <a :href="stateUrl({ op: 'history', step: '-1' })">{{ currentHistoryNav?.last || '-1d' }}</a>
                ]
              </span>
              <span>
                [
                <a :href="stateUrl({ op: 'history', step: '1' })">{{ currentHistoryNav?.next || '+1d' }}</a>
                ]
              </span>
              <span>
                [
                <a :href="historyNowUrl">now</a>
                ]
              </span>
            </template>
            <template v-else>
              <span v-for="nav in report?.navs || []" :key="nav.title">
                [
                <a :href="stateUrl({ step: String(nav.hours) })">{{ nav.title }}</a>
                ]
              </span>
              <span>
                [
                <a :href="hourlyNowUrl">now</a>
                ]
              </span>
            </template>
          </div>
        </div>

        <section v-if="report" class="selector-panel">
          <div class="selector-row">
            <a :class="{ current: currentIp === 'All' }" :href="stateUrl({ ip: 'All' })">
              [&nbsp;All&nbsp;]
            </a>
            <a
              v-for="ip in report.ips"
              :key="ip"
              :class="{ current: currentIp === ip }"
              :href="stateUrl({ ip })"
            >
              [&nbsp;{{ ip }}&nbsp;]
            </a>
          </div>
        </section>

        <section v-if="report && !isHistoryMode" class="state-status" :class="{ 'has-error': Boolean(report.message) }">
          <strong v-if="report.message">出问题CAT的服务端: {{ report.message }}</strong>
          <strong v-else>CAT服务端正常</strong>
        </section>

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 State 数据...
        </section>

        <section v-else-if="report" class="transaction-card">
          <div class="transaction-table-wrap">
            <table class="transaction-table state-summary-table">
              <thead>
                <tr>
                  <th colspan="2" class="left">指标</th>
                  <th class="right">值</th>
                  <th class="left">备注</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="metric in report.metrics" :key="metric.key">
                  <tr>
                    <td class="left">
                      <a :href="graphUrl(metric.key)" @click="toggleGraph(metric.key, graphUrl(metric.key), $event)">
                        {{ graphLinkText(metric.key) }}
                      </a>
                    </td>
                    <td class="left">{{ metric.title }}</td>
                    <td class="right" :class="{ warning: metric.warning }">{{ formatMetric(metric) }}</td>
                    <td class="left">{{ metric.remark }}</td>
                  </tr>
                  <tr v-if="activeGraphKey === metric.key">
                    <td colspan="4">
                      <StateGraphPanel
                        :error="graphErrors[metric.key]"
                        :graph="graphCache[metric.key]"
                        :loading="graphLoadingKey === metric.key"
                      />
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="report?.show" class="transaction-card">
          <div class="transaction-table-wrap">
            <table class="transaction-table state-domain-table">
              <thead>
                <tr>
                  <th class="left"><a :href="sortUrl('domain')">处理项目列表</a></th>
                  <th class="right"><a :href="sortUrl('total')">处理消息总量</a></th>
                  <th class="right"><a :href="sortUrl('loss')">丢失消息总量</a></th>
                  <th class="right"><a :href="sortUrl('size')">压缩前消息大小(GB)</a></th>
                  <th class="right"><a :href="sortUrl('avg')">平均消息大小(KB)</a></th>
                  <th class="right"><a :href="sortUrl('machine')">机器总数</a></th>
                  <th class="left">项目对应机器列表</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="item in report.processDomains" :key="item.name">
                  <tr>
                    <td class="left">{{ item.name }}</td>
                    <td class="right">
                      {{ formatDecimal(item.total, 1) }}<br />
                      <a
                        v-if="!isHistoryMode"
                        :href="graphUrl(`${item.name}:total`)"
                        @click="toggleGraph(`${item.name}:total`, graphUrl(`${item.name}:total`), $event)"
                      >
                        {{ graphLinkText(`${item.name}:total`) }}
                      </a>
                    </td>
                    <td class="right">
                      {{ formatDecimal(item.totalLoss, 1) }}<br />
                      <a
                        v-if="!isHistoryMode"
                        :href="graphUrl(`${item.name}:totalLoss`)"
                        @click="toggleGraph(`${item.name}:totalLoss`, graphUrl(`${item.name}:totalLoss`), $event)"
                      >
                        {{ graphLinkText(`${item.name}:totalLoss`) }}
                      </a>
                    </td>
                    <td class="right">
                      {{ formatDecimal(item.sizeGb, 3) }}<br />
                      <a
                        v-if="!isHistoryMode"
                        :href="graphUrl(`${item.name}:size`)"
                        @click="toggleGraph(`${item.name}:size`, graphUrl(`${item.name}:size`), $event)"
                      >
                        {{ graphLinkText(`${item.name}:size`) }}
                      </a>
                    </td>
                    <td class="right">{{ formatDecimal(item.avgKb, 3) }}</td>
                    <td class="right">{{ item.machineCount }}</td>
                    <td class="left long-text">{{ item.ips.join(', ') }}</td>
                  </tr>
                  <tr v-if="isActiveDomainGraph(item)">
                    <td colspan="7">
                      <StateGraphPanel
                        :error="graphErrors[activeGraphKey]"
                        :graph="graphCache[activeGraphKey]"
                        :loading="graphLoadingKey === activeGraphKey"
                      />
                    </td>
                  </tr>
                </template>
                <tr class="state-total-row">
                  <td></td>
                  <td></td>
                  <td></td>
                  <td></td>
                  <td></td>
                  <td>{{ report.totalSize }}</td>
                  <td></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import ReportSidebar from '../components/ReportSidebar.vue'
import StateGraphPanel from '../components/StateGraphPanel.vue'

interface StateMetric {
  key: string
  remark: string
  title: string
  value: number
  warning: boolean
}

interface StateProcessDomain {
  avgKb: number
  ips: string[]
  machineCount: number
  name: string
  sizeGb: number
  total: number
  totalLoss: number
}

interface StateReport {
  action: string
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  historyMode: boolean
  historyNavs: Array<{ last: string; next: string; title: string }>
  ipAddress: string
  ips: string[]
  longDate: number
  message: string
  metrics: StateMetric[]
  navs: Array<{ hours: number; title: string }>
  processDomains: StateProcessDomain[]
  reportEnd: string
  reportStart: string
  reportType: string
  show: boolean
  sort: string
  totalSize: number
}

interface StateGraph {
  graph: string
  key: string
  pieChart: string
}

const report = ref<StateReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const activeGraphKey = ref('')
const graphCache = ref<Record<string, StateGraph>>({})
const graphErrors = ref<Record<string, string>>({})
const graphLoadingKey = ref('')

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return report.value?.contextPath || '/cat'
})

const currentParams = computed(() => new URLSearchParams(window.location.search))
const currentAction = computed(() => report.value?.action || currentParams.value.get('op') || 'view')
const currentDate = computed(() => report.value?.date || currentParams.value.get('date') || '')
const currentDomain = computed(() => report.value?.domain || currentParams.value.get('domain') || 'cat')
const currentIp = computed(() => report.value?.ipAddress || currentParams.value.get('ip') || 'All')
const currentReportType = computed(() => report.value?.reportType || currentParams.value.get('reportType') || 'day')
const currentShow = computed(() => String(report.value?.show ?? (currentParams.value.get('show') !== 'false')))
const isHistoryMode = computed(() => Boolean(report.value?.historyMode))
const currentHistoryNav = computed(() => (report.value?.historyNavs || []).find((item) => item.title === currentReportType.value))
const modeSwitchText = computed(() => isHistoryMode.value ? '切到小时模式' : '切到历史模式')
const modeSwitchUrl = computed(() => isHistoryMode.value ? hourlyNowUrl.value : stateUrl({ op: 'history' }))
const historyNowUrl = computed(() => {
  const params = new URLSearchParams()

  params.set('op', 'history')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  params.set('reportType', currentReportType.value)
  params.set('show', currentShow.value)
  return `${contextPath.value}/mvc/vue/r/state?${params.toString()}`
})
const hourlyNowUrl = computed(() => {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  params.set('show', currentShow.value)
  return `${contextPath.value}/mvc/vue/r/state?${params.toString()}`
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
    report.value = await response.json() as StateReport
  } catch (error) {
    loadError.value = `State 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function baseStateParams() {
  const params = new URLSearchParams()

  params.set('op', currentAction.value)
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  params.set('show', currentShow.value)
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  return params
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)
  const action = params.get('op') || 'view'

  params.set('op', 'vueData')
  params.set('vueAction', action)
  return `${contextPath.value}/mvc/r/state?${params.toString()}`
}

function formatDecimal(value: number, digits: number) {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: digits,
    minimumFractionDigits: digits
  }).format(value || 0)
}

function formatInteger(value: number) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value || 0)
}

function formatMetric(metric: StateMetric) {
  if (metric.key === 'size') {
    return formatDecimal(metric.value, 3)
  }
  if (metric.key === 'delayAvg') {
    return formatDecimal(metric.value, 1)
  }
  if (metric.key === 'total' || metric.key === 'totalLoss' || metric.key === 'dumpLoss' || metric.key === 'blockLoss') {
    return formatDecimal(metric.value, 1)
  }
  return formatInteger(metric.value)
}

function graphUrl(key: string) {
  const params = new URLSearchParams()

  params.set('op', isHistoryMode.value ? 'historyGraph' : 'graph')
  params.set('ip', currentIp.value)
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  params.set('key', key)
  return `${contextPath.value}/mvc/r/state?${params.toString()}`
}

function graphDataUrl(link: string) {
  const url = new URL(link, window.location.origin)
  const action = url.searchParams.get('op') || 'graph'

  url.searchParams.set('op', 'vueGraphData')
  url.searchParams.set('vueAction', action)
  return `${url.pathname}?${url.searchParams.toString()}`
}

function graphLinkText(key: string) {
  return activeGraphKey.value === key ? '[:: hide ::]' : '[:: show ::]'
}

function isActiveDomainGraph(item: StateProcessDomain) {
  return [`${item.name}:total`, `${item.name}:totalLoss`, `${item.name}:size`].includes(activeGraphKey.value)
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function sortUrl(sort: string) {
  return stateUrl({ show: 'true', sort })
}

function stateUrl(overrides: Record<string, string | undefined>) {
  const params = baseStateParams()

  for (const [key, value] of Object.entries(overrides)) {
    if (value === undefined) {
      params.delete(key)
    } else {
      params.set(key, value)
    }
  }
  if (!params.get('op')) {
    params.set('op', 'view')
  }
  return `${contextPath.value}/mvc/vue/r/state?${params.toString()}`
}

async function toggleGraph(key: string, link: string, event: MouseEvent) {
  if (event.ctrlKey || event.metaKey) {
    return
  }
  event.preventDefault()

  if (activeGraphKey.value === key) {
    activeGraphKey.value = ''
    return
  }
  activeGraphKey.value = key
  if (graphCache.value[key]) {
    return
  }

  graphLoadingKey.value = key
  graphErrors.value = { ...graphErrors.value, [key]: '' }

  try {
    const response = await fetch(graphDataUrl(link), { headers: { Accept: 'application/json' } })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const graph = await response.json() as StateGraph

    graphCache.value = { ...graphCache.value, [key]: graph }
  } catch (error) {
    graphErrors.value = {
      ...graphErrors.value,
      [key]: `图表数据加载失败: ${error instanceof Error ? error.message : String(error)}`
    }
  } finally {
    graphLoadingKey.value = ''
  }
}
</script>
