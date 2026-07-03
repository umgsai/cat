<template>
  <ReportPageShell
    active-report="Problem"
    :application-url="problemUrl({})"
    :context-path="contextPath"
    :date="currentDate"
    :domain="currentDomain"
    :ip="currentIp"
    :report-type="currentReportType"
  >
    <ReportQueryBar
      v-model:domain-input="domainInput"
      :domain-groups="domainGroups"
      :domain-url="domainUrl"
      :frequent-domains="frequentDomains"
      :mode-switch-text="modeSwitchText"
      :mode-switch-url="modeSwitchUrl"
      :report-end="report?.reportEnd"
      :report-start="report?.reportStart"
      :search-domains="searchDomains"
      :shortcuts="shortcuts"
      :show-domain-panel="showDomainPanel"
      :show-frequent-panel="showFrequentPanel"
      @go-domain="goDomain"
      @select-domain="selectDomain"
      @toggle-domain-panel="showDomainPanel = !showDomainPanel"
      @toggle-frequent-panel="showFrequentPanel = !showFrequentPanel"
    />

    <section v-if="report && report.sample !== 1" class="sample-panel">
      <strong>采样</strong>
      <span>采样比例 {{ formatPercent(report.sample) }}</span>
    </section>

    <ReportSelectorPanel :rows="selectorRows" />

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
      <div class="report-table-wrap">
        <table class="report-table problem-table">
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
                    <span class="problem-type">
                      <span class="problem-type-marker" :class="problemTypeMarkerClass(row.type)"></span>
                      <span>{{ row.type }}</span>
                    </span>
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
      <a :href="problemUrl({ op: 'group' })" @click="loadThreadGroups">Threads Details</a>
    </section>

    <section v-if="threadGroupLoading || threadGroupError || threadTableRows.length" class="transaction-card problem-thread-card">
      <section v-if="threadGroupLoading" class="empty-state">
        正在加载 Threads Details...
      </section>
      <section v-else-if="threadGroupError" class="empty-state">
        {{ threadGroupError }}
      </section>
      <div v-else class="report-table-wrap">
        <table v-if="activeThreadLevelInfo" class="report-table transaction-report-table problem-thread-table">
          <tbody>
            <tr>
              <td title="time\group">Time Group</td>
              <td
                v-for="group in activeThreadLevelInfo.groups"
                :key="group.name"
                :colspan="group.number"
                :title="group.name"
              >
                <a
                  :href="threadDetailUrl(group.name)"
                  @click="loadThreadDetail(group.name, $event)"
                >
                  {{ truncateThreadLabel(group.name) }}
                </a>
              </td>
            </tr>
            <tr>
              <td title="time\thread">Time Thread</td>
              <td v-for="thread in activeThreadLevelInfo.threads" :key="thread">{{ thread }}</td>
            </tr>
            <tr
              v-for="(row, index) in activeThreadLevelInfo.datas"
              :key="`thread-${index}`"
              @click="handleProblemDetailClick"
              v-html="threadRowHtml(row)"
            ></tr>
          </tbody>
        </table>
        <table v-else class="report-table transaction-report-table problem-thread-table">
          <tbody>
            <tr>
              <td title="time\group">Time Group</td>
              <td v-for="group in activeGroupLevelInfo?.groups || []" :key="group" :title="group">
                <a
                  :href="threadDetailUrl(group)"
                  @click="loadThreadDetail(group, $event)"
                >
                  {{ truncateThreadLabel(group) }}
                </a>
              </td>
            </tr>
            <tr
              v-for="(row, index) in activeGroupLevelInfo?.datas || []"
              :key="`group-${index}`"
              @click="handleProblemDetailClick"
              v-html="threadRowHtml(row)"
            ></tr>
          </tbody>
        </table>
      </div>
    </section>

    <div v-if="problemDetailVisible" class="problem-detail-backdrop" @click.self="closeProblemDetail">
      <section class="problem-detail-modal" role="dialog" aria-modal="true" aria-label="Problem Detail">
        <header class="problem-detail-header">
          <strong>Problem Detail</strong>
          <button type="button" aria-label="关闭" @click="closeProblemDetail">×</button>
        </header>
        <section v-if="problemDetailLoading" class="empty-state">
          正在加载 Problem Detail...
        </section>
        <section v-else-if="problemDetailError" class="empty-state">
          {{ problemDetailError }}
        </section>
        <div
          v-else
          class="problem-detail-content"
          @click="handleProblemDetailClick"
          v-html="problemDetailHtml"
        ></div>
      </section>
    </div>
  </ReportPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import ProblemGraphPanel from '../components/ProblemGraphPanel.vue'
import ReportPageShell from '../components/ReportPageShell.vue'
import ReportQueryBar from '../components/ReportQueryBar.vue'
import ReportSelectorPanel from '../components/ReportSelectorPanel.vue'
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

interface ProblemGroupLevelInfo {
  datas: string[]
  groups: string[]
}

interface ProblemThreadGroup {
  name: string
  number: number
}

interface ProblemThreadLevelInfo {
  datas: string[]
  groups: ProblemThreadGroup[]
  threads: string[]
}

interface ProblemReport {
  action: string
  cacheThreshold: number
  callThreshold: number
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  domainGroups: DomainDepartment[]
  group: string
  groupLevelInfo: ProblemGroupLevelInfo | null
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
  threadLevelInfo: ProblemThreadLevelInfo | null
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
const problemDetailError = ref('')
const problemDetailHtml = ref('')
const problemDetailLoading = ref(false)
const problemDetailVisible = ref(false)
const threadGroupError = ref('')
const threadGroupInfo = ref<ProblemGroupLevelInfo | null>(null)
const threadGroupLoading = ref(false)
const threadLevelInfo = ref<ProblemThreadLevelInfo | null>(null)
const historyNavs = [
  { label: 'month', last: '-1m', next: '+1m' },
  { label: 'week', last: '-1w', next: '+1w' },
  { label: 'day', last: '-1d', next: '+1d' }
]
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
const isHistoryMode = computed(() => report.value?.historyMode ?? isHistoryAction(currentParams.value.get('op')))
const defaultAction = computed(() => isHistoryMode.value ? 'history' : 'view')
const currentHistoryNav = computed(() => historyNavs.find((item) => item.label === currentReportType.value) || historyNavs[2])
const modeSwitchText = computed(() => isHistoryMode.value ? '切到小时模式' : '切到历史模式')
const modeSwitchUrl = computed(() => problemUrl({ op: isHistoryMode.value ? 'view' : 'history' }))
const activeGroupLevelInfo = computed(() => threadGroupInfo.value || report.value?.groupLevelInfo || null)
const activeThreadLevelInfo = computed(() => threadLevelInfo.value || report.value?.threadLevelInfo || null)
const threadTableRows = computed(() => activeThreadLevelInfo.value?.datas || activeGroupLevelInfo.value?.datas || [])

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

const selectorRows = computed(() => {
  if (!report.value) {
    return []
  }
  const rows = [[
    { current: currentIp.value === 'All', href: problemUrl({ ip: 'All' }), label: 'All' },
    ...report.value.ips.map((ip) => ({
      current: currentIp.value === ip,
      href: problemUrl({ ip }),
      label: hostLabel(ip),
      title: report.value?.ipToHostname[ip] || ip
    }))
  ]]

  if (report.value.groups.length) {
    rows.push(report.value.groups.map((item) => ({
      current: report.value?.group === item,
      href: problemUrl({ op: 'groupReport', group: item }),
      label: item
    })))
  }
  return rows
})

const shortcuts = computed(() => {
  const date = currentDate.value
  const ip = currentIp.value
  const domain = currentDomain.value

  if (isHistoryMode.value) {
    const currentNav = currentHistoryNav.value

    return [
      ...historyNavs.map((nav) => ({
        current: nav.label === currentReportType.value,
        href: historyUrl({ date, domain, ip, reportType: nav.label }),
        label: nav.label
      })),
      {
        current: false,
        href: historyUrl({ date, domain, ip, reportType: currentReportType.value, step: '-1' }),
        label: currentNav.last
      },
      {
        current: false,
        href: historyUrl({ date, domain, ip, reportType: currentReportType.value, step: '1' }),
        label: currentNav.next
      },
      {
        current: false,
        href: historyUrl({ domain, ip, reportType: currentReportType.value }),
        label: 'now'
      }
    ]
  }
  return [
    { current: false, label: '-7d', href: problemUrl({ date, ip, step: '-168', domain }) },
    { current: false, label: '-1d', href: problemUrl({ date, ip, step: '-24', domain }) },
    { current: false, label: '-1h', href: problemUrl({ date, ip, step: '-1', domain }) },
    { current: false, label: '+1h', href: problemUrl({ date, ip, step: '1', domain }) },
    { current: false, label: '+1d', href: problemUrl({ date, ip, step: '24', domain }) },
    { current: false, label: '+7d', href: problemUrl({ date, ip, step: '168', domain }) },
    { current: false, label: 'now', href: hourlyNowUrl() }
  ]
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
    const data = await response.json() as ProblemReport

    report.value = data
    threadGroupInfo.value = data.groupLevelInfo || null
    threadLevelInfo.value = data.threadLevelInfo || null
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

function closeProblemDetail() {
  problemDetailVisible.value = false
  problemDetailError.value = ''
  problemDetailHtml.value = ''
}

function baseProblemParams() {
  const params = new URLSearchParams()

  params.set('op', defaultAction.value)
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

  params.set('op', defaultAction.value)
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

function handleProblemDetailClick(event: MouseEvent) {
  const target = event.target as Element | null
  const anchor = target?.closest('a')

  if (!anchor) {
    return
  }
  const href = anchor.getAttribute('href') || ''

  if (!isProblemDetailUrl(href)) {
    return
  }
  event.preventDefault()
  void openProblemDetail(href)
}

function historyUrl(overrides: Record<string, string | undefined>) {
  const params = new URLSearchParams()

  params.set('op', 'history')
  params.set('domain', overrides.domain || currentDomain.value)
  params.set('ip', overrides.ip || currentIp.value)
  params.set('urlThreshold', String(thresholds.urlThreshold))
  params.set('sqlThreshold', String(thresholds.sqlThreshold))
  params.set('serviceThreshold', String(thresholds.serviceThreshold))
  params.set('cacheThreshold', String(thresholds.cacheThreshold))
  params.set('callThreshold', String(thresholds.callThreshold))
  if (overrides.date) {
    params.set('date', overrides.date)
  }
  if (overrides.reportType) {
    params.set('reportType', overrides.reportType)
  }
  if (overrides.step) {
    params.set('step', overrides.step)
  }
  return `${contextPath.value}/mvc/vue/r/p?${params.toString()}`
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

function isProblemDetailUrl(href: string) {
  try {
    const url = new URL(href, window.location.origin)

    return url.pathname.endsWith('/mvc/r/p') && url.searchParams.get('op') === 'detail'
  } catch {
    return false
  }
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

function logViewUrl(messageUrl: string) {
  return `${contextPath.value}/mvc/vue/r/m/${messageUrl}?domain=${encodeURIComponent(currentDomain.value)}`
}

async function loadThreadDetail(group: string, event: MouseEvent) {
  event.preventDefault()
  await loadThreadInfo({ groupName: group, vueAction: 'thread' })
}

async function loadThreadGroups(event: MouseEvent) {
  event.preventDefault()
  await loadThreadInfo({ vueAction: 'group' })
}

async function loadThreadInfo(options: { groupName?: string; vueAction: string }) {
  threadGroupLoading.value = true
  threadGroupError.value = ''

  try {
    const response = await fetch(threadDataUrl(options), { headers: { Accept: 'application/json' } })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const data = await response.json() as ProblemReport

    threadGroupInfo.value = data.groupLevelInfo || null
    threadLevelInfo.value = data.threadLevelInfo || null
  } catch (error) {
    threadGroupError.value = `Threads Details 加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    threadGroupLoading.value = false
  }
}

function normalizeProblemDetailHtml(html: string) {
  return normalizeProblemHtml(html)
}

function normalizeProblemHtml(html: string) {
  return html
    .replace(/\s+onclick="return show\(this\);"/g, '')
    .replace(/href="\/cat\/mvc\/r\/p/g, `href="${contextPath.value}/mvc/r/p`)
    .replace(/href="\/cat\/mvc\/r\/m\//g, `href="${contextPath.value}/mvc/vue/r/m/`)
    .replace(/href="\/cat\/r\/m\//g, `href="${contextPath.value}/mvc/vue/r/m/`)
    .replace(/href="\/mvc\/r\/m\//g, `href="${contextPath.value}/mvc/vue/r/m/`)
    .replace(/<a href="([^"]*\/mvc\/vue\/r\/m\/[^"]*)"/g, '<a href="$1" target="_blank" rel="noreferrer"')
}

function normalizeProblemDetailUrl(href: string) {
  const url = new URL(href, window.location.origin)

  return `${url.pathname}${url.search}`
}

async function openProblemDetail(href: string) {
  problemDetailVisible.value = true
  problemDetailLoading.value = true
  problemDetailError.value = ''

  try {
    const response = await fetch(normalizeProblemDetailUrl(href), { headers: { Accept: 'text/html' } })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    problemDetailHtml.value = normalizeProblemDetailHtml(await response.text())
  } catch (error) {
    problemDetailError.value = `Problem Detail 加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    problemDetailLoading.value = false
  }
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
  if (overrides.groupName !== undefined) {
    params.set('groupName', overrides.groupName)
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

function threadDataUrl(options: { groupName?: string; vueAction: string }) {
  const params = baseProblemParams()

  params.set('op', 'vueData')
  params.set('vueAction', options.vueAction)
  if (options.groupName) {
    params.set('groupName', options.groupName)
  }
  return `${contextPath.value}/mvc/r/p?${params.toString()}`
}

function threadDetailUrl(group: string) {
  return problemUrl({ groupName: group, op: 'thread' })
}

function threadRowHtml(row: string) {
  return normalizeProblemHtml(row)
}

function truncateThreadLabel(value: string) {
  return value && value.length > 20 ? value.substring(0, 20) : value
}

function problemTypeMarkerClass(type: string) {
  return `is-${type.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase()}`
}

function sampleLetter(index: number, total: number) {
  if (index === 0) {
    return 'L'
  }
  return index === total - 1 ? 'g' : 'o'
}

function isHistoryAction(action: string | null) {
  return Boolean(action?.startsWith('history'))
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
