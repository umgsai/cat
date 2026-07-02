<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="problemUrl({})">Application</a>
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
        active-report="Problem"
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
          <div class="query-actions">
            <button class="domain-toggle" type="button" @click="showDomainPanel = !showDomainPanel">
              {{ showDomainPanel ? '收起' : '全部' }}
            </button>
            <button class="domain-toggle" type="button" @click="showFrequentPanel = !showFrequentPanel">
              {{ showFrequentPanel ? '收起' : '常用' }}
            </button>
            <el-autocomplete
              v-model="domainInput"
              class="domain-input"
              placeholder="input domain for search"
              :fetch-suggestions="searchDomains"
              value-key="value"
              clearable
              @select="selectDomain"
              @keyup.enter="goDomain"
            />
            <button class="domain-go" type="button" @click="goDomain">Go</button>
          </div>
          <div class="time-shortcuts">
            <span>
              【<a class="mode-link" :href="historyModeUrl">切到历史模式</a>】
            </span>
            <span v-for="shortcut in shortcuts" :key="shortcut.label">
              [
              <a :href="shortcut.href">{{ shortcut.label }}</a>
              ]
            </span>
          </div>
        </div>

        <section v-if="showDomainPanel" class="domain-panel">
          <table>
            <tbody>
              <template v-for="department in domainGroups" :key="department.name">
                <tr v-for="(line, index) in department.lines" :key="`${department.name}-${line.name}`">
                  <td v-if="index === 0" class="department-cell" :rowspan="department.lines.length">
                    {{ department.name }}
                  </td>
                  <td class="department-cell">{{ line.name }}</td>
                  <td class="domain-cell">
                    <a v-for="item in line.domains" :key="item" :href="domainUrl(item)">
                      [&nbsp;{{ item }}&nbsp;]
                    </a>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </section>

        <section v-if="showFrequentPanel" class="domain-panel">
          <table>
            <tbody>
              <tr>
                <td class="domain-cell">
                  <a v-for="item in frequentDomains" :key="item" :href="domainUrl(item)">
                    [&nbsp;{{ item }}&nbsp;]
                  </a>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <section v-if="report && report.sample !== 1" class="sample-panel">
          <strong>采样</strong>
          <span>采样比例 {{ formatPercent(report.sample) }}</span>
        </section>

        <section v-if="report" class="selector-panel">
          <div class="selector-row">
            <a :class="{ current: currentIp === 'All' }" :href="problemUrl({ ip: 'All' })">
              [&nbsp;All&nbsp;]
            </a>
            <a
              v-for="ip in report.ips"
              :key="ip"
              :class="{ current: currentIp === ip }"
              :href="problemUrl({ ip })"
              :title="report.ipToHostname[ip] || ip"
            >
              [&nbsp;{{ hostLabel(ip) }}&nbsp;]
            </a>
          </div>
          <div v-if="report.groups.length" class="selector-row group-row">
            <a
              v-for="item in report.groups"
              :key="item"
              :class="{ current: report.group === item }"
              :href="problemUrl({ op: 'groupReport', group: item })"
            >
              [&nbsp;{{ item }}&nbsp;]
            </a>
          </div>
        </section>

        <section v-if="report" class="threshold-panel">
          <label>
            Long-url
            <select v-model="thresholds.urlThreshold">
              <option v-for="item in urlOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </label>
          <label>
            Long-sql
            <select v-model="thresholds.sqlThreshold">
              <option v-for="item in sqlOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </label>
          <label>
            Long-service
            <select v-model="thresholds.serviceThreshold">
              <option v-for="item in serviceOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </label>
          <label>
            Long-cache
            <select v-model="thresholds.cacheThreshold">
              <option v-for="item in cacheOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </label>
          <label>
            Long-call
            <select v-model="thresholds.callThreshold">
              <option v-for="item in callOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </label>
          <button type="button" @click="applyThresholds">查询</button>
        </section>

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 Problem 数据...
        </section>

        <section v-else-if="report" class="transaction-card">
          <div class="transaction-table-wrap">
            <table class="transaction-table problem-table">
              <thead>
                <tr>
                  <th class="left">Type</th>
                  <th class="right">Total</th>
                  <th class="left">Status</th>
                  <th class="right">Count</th>
                  <th class="left">SampleLinks</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="row in report.rows" :key="row.type">
                  <template v-for="(status, index) in row.statuses" :key="`${row.type}-${status.status}`">
                    <tr>
                      <td v-if="index === 0" class="left top-cell" :rowspan="row.statuses.length">
                        <span class="problem-type">{{ row.type }}</span>
                        <br />
                        <a class="show-link" :href="graphUrl(row)" @click="toggleGraph(typeGraphKey(row), graphUrl(row), $event)">
                          {{ graphLinkText(typeGraphKey(row)) }}
                        </a>
                      </td>
                      <td v-if="index === 0" class="right top-cell" :rowspan="row.statuses.length">
                        {{ formatInteger(row.count) }}
                      </td>
                      <td class="left">
                        <a
                          class="show-link"
                          :href="graphUrl(row, status)"
                          @click="toggleGraph(statusGraphKey(row, status), graphUrl(row, status), $event)"
                        >
                          {{ graphLinkText(statusGraphKey(row, status)) }}
                        </a>
                        <span>{{ status.status }}</span>
                      </td>
                      <td class="right">{{ formatInteger(status.count) }}</td>
                      <td class="left sample-links">
                        <a
                          v-for="(link, linkIndex) in status.links"
                          :key="`${row.type}-${status.status}-${linkIndex}`"
                          :href="logViewUrl(link)"
                          target="_blank"
                          rel="noreferrer"
                        >
                          {{ sampleLetter(linkIndex, status.links.length) }}
                        </a>
                      </td>
                    </tr>
                  </template>
                  <tr v-if="isActiveProblemGraph(row)">
                    <td colspan="5">
                      <ProblemGraphPanel
                        :error="graphErrors[activeGraphKey]"
                        :graph="graphCache[activeGraphKey]"
                        :loading="graphLoadingKey === activeGraphKey"
                      />
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="report && currentIp !== 'All'" class="threads-link">
          <a :href="legacyProblemUrl({ op: 'group' })">Threads Details</a>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import ProblemGraphPanel from '../components/ProblemGraphPanel.vue'
import ReportSidebar from '../components/ReportSidebar.vue'
import { getFrequentDomains } from '../utils/domainCookies'

interface DomainLine {
  name: string
  domains: string[]
}

interface DomainDepartment {
  name: string
  lines: DomainLine[]
}

interface ProblemStatusRow {
  count: number
  encodedStatus: string
  links: string[]
  status: string
}

interface ProblemTypeRow {
  count: number
  index: number
  statuses: ProblemStatusRow[]
  type: string
}

interface ProblemReport {
  cacheThreshold: number
  callThreshold: number
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  domainGroups: DomainDepartment[]
  group: string
  groupIps: string[]
  groups: string[]
  historyMode: boolean
  ipAddress: string
  ipToHostname: Record<string, string>
  ips: string[]
  longDate: number
  reportEnd: string
  reportStart: string
  reportType: string
  rows: ProblemTypeRow[]
  sample: number
  serviceThreshold: number
  sqlThreshold: number
  status: string
  type: string
  urlThreshold: number
}

interface ProblemGraph {
  distributionChart: string
  errorsTrend: string
  historyMode: boolean
}

const report = ref<ProblemReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const domainInput = ref('')
const showDomainPanel = ref(false)
const showFrequentPanel = ref(false)
const activeGraphKey = ref('')
const graphCache = ref<Record<string, ProblemGraph>>({})
const graphErrors = ref<Record<string, string>>({})
const graphLoadingKey = ref('')
const thresholds = reactive({
  cacheThreshold: 10,
  callThreshold: 50,
  serviceThreshold: 50,
  sqlThreshold: 100,
  urlThreshold: 1000
})

const urlOptions = [
  { label: '0.5 Sec', value: 500 },
  { label: '1.0 Sec', value: 1000 },
  { label: '1.5 Sec', value: 1500 },
  { label: '2.0 Sec', value: 2000 },
  { label: '3.0 Sec', value: 3000 },
  { label: '5.0 Sec', value: 5000 }
]
const sqlOptions = [
  { label: '100 ms', value: 100 },
  { label: '500 ms', value: 500 },
  { label: '1000 ms', value: 1000 },
  { label: '3000 ms', value: 3000 },
  { label: '5000 ms', value: 5000 }
]
const serviceOptions = [
  { label: '50 ms', value: 50 },
  { label: '100 ms', value: 100 },
  { label: '500 ms', value: 500 },
  { label: '1000 ms', value: 1000 },
  { label: '3000 ms', value: 3000 },
  { label: '5000 ms', value: 5000 }
]
const cacheOptions = [
  { label: '10 ms', value: 10 },
  { label: '50 ms', value: 50 },
  { label: '100 ms', value: 100 },
  { label: '500 ms', value: 500 }
]
const callOptions = serviceOptions

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return report.value?.contextPath || '/cat'
})

const currentParams = computed(() => new URLSearchParams(window.location.search))
const currentDomain = computed(() => report.value?.domain || currentParams.value.get('domain') || 'cat')
const currentIp = computed(() => report.value?.ipAddress || currentParams.value.get('ip') || 'All')
const currentDate = computed(() => report.value?.date || currentParams.value.get('date') || '')
const currentReportType = computed(() => report.value?.reportType || currentParams.value.get('reportType') || 'day')
const domainGroups = computed(() => report.value?.domainGroups || [])

const domainSuggestions = computed(() => {
  const suggestions: Array<{ label: string; value: string; category: string }> = []

  for (const department of domainGroups.value) {
    for (const line of department.lines) {
      for (const domain of line.domains) {
        suggestions.push({ label: domain, value: domain, category: line.name })
      }
    }
  }
  return suggestions
})

const frequentDomains = computed(() => {
  return getFrequentDomains(currentDomain.value)
})

const shortcuts = computed(() => {
  const date = currentDate.value
  const ip = currentIp.value
  const domain = currentDomain.value

  return [
    { label: '-7d', href: problemUrl({ date, ip, step: '-168', domain }) },
    { label: '-1d', href: problemUrl({ date, ip, step: '-24', domain }) },
    { label: '-1h', href: problemUrl({ date, ip, step: '-1', domain }) },
    { label: '+1h', href: problemUrl({ date, ip, step: '1', domain }) },
    { label: '+1d', href: problemUrl({ date, ip, step: '24', domain }) },
    { label: '+7d', href: problemUrl({ date, ip, step: '168', domain }) },
    { label: 'now', href: hourlyNowUrl() }
  ]
})

const historyModeUrl = computed(() => legacyProblemUrl({ op: 'history' }))

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
    const data = await response.json() as ProblemReport

    report.value = data
    domainInput.value = data.domain || ''
    thresholds.cacheThreshold = data.cacheThreshold
    thresholds.callThreshold = data.callThreshold
    thresholds.serviceThreshold = data.serviceThreshold
    thresholds.sqlThreshold = data.sqlThreshold
    thresholds.urlThreshold = data.urlThreshold
  } catch (error) {
    loadError.value = `Problem 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function applyThresholds() {
  window.location.href = problemUrl({
    cacheThreshold: String(thresholds.cacheThreshold),
    callThreshold: String(thresholds.callThreshold),
    serviceThreshold: String(thresholds.serviceThreshold),
    sqlThreshold: String(thresholds.sqlThreshold),
    urlThreshold: String(thresholds.urlThreshold)
  })
}

function baseProblemParams() {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentIp.value) {
    params.set('ip', currentIp.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  params.set('urlThreshold', String(thresholds.urlThreshold))
  params.set('sqlThreshold', String(thresholds.sqlThreshold))
  params.set('serviceThreshold', String(thresholds.serviceThreshold))
  params.set('cacheThreshold', String(thresholds.cacheThreshold))
  params.set('callThreshold', String(thresholds.callThreshold))
  return params
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)
  const action = params.get('op') || 'view'

  params.set('op', 'vueData')
  params.set('vueAction', action)
  return `${contextPath.value}/mvc/r/p?${params.toString()}`
}

function domainUrl(domain: string) {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', domain)
  params.set('ip', 'All')
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  return `${contextPath.value}/mvc/vue/r/p?${params.toString()}`
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

function formatPercent(value: number) {
  return `${formatDecimal(value * 100, 2)}%`
}

function goDomain() {
  window.location.href = domainUrl(domainInput.value || currentDomain.value)
}

function graphUrl(row: ProblemTypeRow, status?: ProblemStatusRow) {
  return legacyProblemUrl({
    op: report.value?.historyMode ? 'historyGraph' : 'hourlyGraph',
    status: status?.status,
    type: row.type
  })
}

function graphDataUrl(link: string) {
  const url = new URL(link, window.location.origin)
  const action = url.searchParams.get('op') || 'hourlyGraph'

  url.searchParams.set('op', 'vueGraphData')
  url.searchParams.set('vueAction', action)
  return `${url.pathname}?${url.searchParams.toString()}`
}

function graphLinkText(key: string) {
  return activeGraphKey.value === key ? '[:: hide ::]' : '[:: show ::]'
}

function hourlyNowUrl() {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  params.set('urlThreshold', String(thresholds.urlThreshold))
  params.set('sqlThreshold', String(thresholds.sqlThreshold))
  params.set('serviceThreshold', String(thresholds.serviceThreshold))
  params.set('cacheThreshold', String(thresholds.cacheThreshold))
  params.set('callThreshold', String(thresholds.callThreshold))
  return `${contextPath.value}/mvc/vue/r/p?${params.toString()}`
}

function isActiveProblemGraph(row: ProblemTypeRow) {
  return activeGraphKey.value === typeGraphKey(row) || activeGraphKey.value.startsWith(`status-${row.type}-`)
}

function hostLabel(ip: string) {
  const hostname = report.value?.ipToHostname[ip]

  return hostname ? `${ip}(${hostname})` : ip
}

function legacyProblemUrl(overrides: Record<string, string | undefined>) {
  const params = baseProblemParams()

  if (overrides.op !== undefined) {
    params.set('op', overrides.op)
  }
  if (overrides.type !== undefined) {
    params.set('type', overrides.type)
  }
  if (overrides.status !== undefined) {
    params.set('status', overrides.status)
  }
  return `${contextPath.value}/mvc/r/p?${params.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function logViewUrl(messageUrl: string) {
  return `${contextPath.value}/mvc/vue/r/m/${messageUrl}?domain=${encodeURIComponent(currentDomain.value)}`
}

function problemUrl(overrides: Record<string, string | undefined>) {
  const params = baseProblemParams()

  if (overrides.op !== undefined) {
    params.set('op', overrides.op)
  }
  if (overrides.domain !== undefined) {
    params.set('domain', overrides.domain)
  }
  if (overrides.date !== undefined) {
    params.set('date', overrides.date)
  }
  if (overrides.ip !== undefined) {
    params.set('ip', overrides.ip)
  }
  if (overrides.step !== undefined) {
    params.set('step', overrides.step)
  }
  if (overrides.group !== undefined) {
    params.set('group', overrides.group)
  }
  if (overrides.urlThreshold !== undefined) {
    params.set('urlThreshold', overrides.urlThreshold)
  }
  if (overrides.sqlThreshold !== undefined) {
    params.set('sqlThreshold', overrides.sqlThreshold)
  }
  if (overrides.serviceThreshold !== undefined) {
    params.set('serviceThreshold', overrides.serviceThreshold)
  }
  if (overrides.cacheThreshold !== undefined) {
    params.set('cacheThreshold', overrides.cacheThreshold)
  }
  if (overrides.callThreshold !== undefined) {
    params.set('callThreshold', overrides.callThreshold)
  }
  return `${contextPath.value}/mvc/vue/r/p?${params.toString()}`
}

function sampleLetter(index: number, total: number) {
  if (index === 0) {
    return 'L'
  }
  return index === total - 1 ? 'g' : 'o'
}

function searchDomains(query: string, callback: (items: Array<{ label: string; value: string; category: string }>) => void) {
  const keyword = query.trim().toLowerCase()

  if (!keyword) {
    callback(domainSuggestions.value)
    return
  }
  callback(domainSuggestions.value.filter((item) => item.value.toLowerCase().includes(keyword)))
}

function selectDomain(item: { value: string }) {
  domainInput.value = item.value
  goDomain()
}

function statusGraphKey(row: ProblemTypeRow, status: ProblemStatusRow) {
  return `status-${row.type}-${status.status}`
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
    const graph = await response.json() as ProblemGraph

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

function typeGraphKey(row: ProblemTypeRow) {
  return `type-${row.type}`
}
</script>
