<template>
  <ReportPageShell
    active-report="Event"
    :application-url="eventUrl({})"
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
      <span>采样直接影响的是 Transaction、Event 的总量和 QPS，不影响 Metric、Heartbeat、Exception 等数据。</span>
    </section>

    <ReportSelectorPanel :rows="ipSelectorRows" />

    <ReportSelectorPanel :rows="groupSelectorRows" />

    <ReportLoadState :error="loadError" :loading="loading" loading-text="正在加载 Event 数据...">
      <section v-if="report" class="transaction-card">
        <div class="report-table-wrap">
          <table class="report-table event-table">
            <thead>
              <tr v-if="!isNameView">
                <th class="left"><a :href="sortUrl('type')">Type</a></th>
                <th class="right"><a :href="sortUrl('total')">Total</a></th>
                <th class="right"><a :href="sortUrl('failure')">Failure</a></th>
                <th class="right"><a :href="sortUrl('failurePercent')">Failure%</a></th>
                <th class="right">Sample Link</th>
                <th class="right">QPS</th>
              </tr>
              <tr v-else>
                <th class="left">
                  <a :href="graphUrl()" @click="toggleGraph('type-total', graphUrl(), $event)">
                    {{ graphLinkText('type-total') }}
                  </a>
                  <a :href="sortUrl('type')">Name</a>
                </th>
                <th class="right"><a :href="sortUrl('total')">Total</a></th>
                <th class="right"><a :href="sortUrl('failure')">Failure</a></th>
                <th class="right"><a :href="sortUrl('failurePercent')">Failure%</a></th>
                <th class="center">Sample Link</th>
                <th class="right"><a :href="sortUrl('total')">QPS</a></th>
                <th class="right"><a :href="sortUrl('total')">Percent%</a></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isNameView && activeGraphKey === 'type-total'">
                <td :colspan="isNameView ? 7 : 6">
                  <EventGraphPanel
                    :error="graphErrors['type-total']"
                    :format-integer="formatInteger"
                    :format-rate="formatRate"
                    :graph="graphCache['type-total']"
                    :loading="graphLoadingKey === 'type-total'"
                  />
                </td>
              </tr>
              <template v-for="row in report.rows" :key="`${row.index}-${row.id}`">
                <tr>
                  <td class="left long-text">
                    <template v-if="!isNameView">
                      <a :href="graphUrl(row)" @click="toggleGraph(rowGraphKey(row), graphUrl(row), $event)">
                        {{ graphLinkText(rowGraphKey(row)) }}
                      </a>
                      <a :href="eventUrl({ type: row.id, ip: currentIp })">{{ row.id }}</a>
                    </template>
                    <template v-else>
                      <a
                        v-if="!row.totalRow"
                        :href="graphUrl(row)"
                        @click="toggleGraph(rowGraphKey(row), graphUrl(row), $event)"
                      >
                        {{ graphLinkText(rowGraphKey(row)) }}
                      </a>
                      <span>{{ row.id }}</span>
                    </template>
                  </td>
                  <td class="right">{{ formatInteger(row.totalCount) }}</td>
                  <td class="right">{{ formatInteger(row.failCount) }}</td>
                  <td class="right">{{ formatRate(row.failPercent, 4) }}</td>
                  <td class="center sample-link">
                    <a v-if="row.messageUrl" :href="logViewUrl(row.messageUrl)" target="_blank" rel="noreferrer">
                      Log View
                    </a>
                  </td>
                  <td class="right">{{ formatDecimal(row.tps, 1) }}</td>
                  <td v-if="isNameView" class="right">{{ formatRate(row.totalPercent, 4) }}</td>
                </tr>
                <tr v-if="activeGraphKey === rowGraphKey(row)">
                  <td :colspan="isNameView ? 7 : 6">
                    <EventGraphPanel
                      :error="graphErrors[rowGraphKey(row)]"
                      :format-integer="formatInteger"
                      :format-rate="formatRate"
                      :graph="graphCache[rowGraphKey(row)]"
                      :loading="graphLoadingKey === rowGraphKey(row)"
                    />
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
        <div v-if="isNameView && report.pieChart" class="report-pie-panel">
          <PieChartPanel :chart="report.pieChart" />
        </div>
      </section>
    </ReportLoadState>
  </ReportPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import EventGraphPanel from '../components/EventGraphPanel.vue'
import PieChartPanel from '../components/PieChartPanel.vue'
import ReportLoadState from '../components/ReportLoadState.vue'
import ReportPageShell from '../components/ReportPageShell.vue'
import ReportQueryBar from '../components/ReportQueryBar.vue'
import ReportSelectorPanel from '../components/ReportSelectorPanel.vue'
import { getFrequentDomains } from '../utils/domainCookies'
import { formatDecimal, formatInteger, formatPercent, formatRate } from '../utils/reportFormatters'

interface DomainLine {
  name: string
  domains: string[]
}

interface DomainDepartment {
  name: string
  lines: DomainLine[]
}

interface EventRow {
  encodedName: string
  encodedType: string
  failCount: number
  failPercent: number
  id: string
  index: number
  messageUrl: string
  totalCount: number
  totalPercent: number
  totalRow: boolean
  tps: number
}

interface EventReport {
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  domainGroups: DomainDepartment[]
  encodedType: string
  group: string
  groupIps: string[]
  groups: string[]
  historyMode: boolean
  ipAddress: string
  ipToHostname: Record<string, string>
  ips: string[]
  longDate: number
  name: string
  pieChart: string
  reportEnd: string
  reportStart: string
  reportType: string
  rows: EventRow[]
  sample: number
  sortBy: string
  type: string
}

interface EventDistributionDetail {
  failCount: number
  failPercent: number
  ip: string
  totalCount: number
}

interface EventGraph {
  distributionChart: string
  distributionDetails: EventDistributionDetail[]
  failureTrend: string
  graph1: string
  graph2: string
  historyMode: boolean
  hitTrend: string
}

const report = ref<EventReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const domainInput = ref('')
const showDomainPanel = ref(false)
const showFrequentPanel = ref(false)
const activeGraphKey = ref('')
const graphCache = ref<Record<string, EventGraph>>({})
const graphErrors = ref<Record<string, string>>({})
const graphLoadingKey = ref('')
const historyNavs = [
  { label: 'month', last: '-1m', next: '+1m' },
  { label: 'week', last: '-1w', next: '+1w' },
  { label: 'day', last: '-1d', next: '+1d' }
]

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
const currentType = computed(() => report.value?.type || currentParams.value.get('type') || '')
const domainGroups = computed(() => report.value?.domainGroups || [])
const isNameView = computed(() => Boolean(currentType.value))
const isHistoryMode = computed(() => report.value?.historyMode ?? currentParams.value.get('op') === 'history')
const currentHistoryNav = computed(() => historyNavs.find((item) => item.label === currentReportType.value) || historyNavs[2])
const modeSwitchText = computed(() => isHistoryMode.value ? '切到小时模式' : '切到历史模式')
const modeSwitchUrl = computed(() => {
  const params = new URLSearchParams()

  params.set('op', isHistoryMode.value ? 'view' : 'history')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  if (currentType.value) {
    params.set('type', currentType.value)
  }
  return `${contextPath.value}/mvc/vue/r/e?${params.toString()}`
})

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

const ipSelectorRows = computed(() => {
  if (!report.value) {
    return []
  }
  return [[
    { current: currentIp.value === 'All', href: eventUrl({ ip: 'All' }), label: 'All' },
    ...report.value.ips.map((ip) => ({
      current: currentIp.value === ip,
      href: eventUrl({ ip }),
      label: hostLabel(ip),
      title: report.value?.ipToHostname[ip] || ip
    }))
  ]]
})

const groupSelectorRows = computed(() => {
  if (!report.value?.groups.length) {
    return []
  }
  return [
    report.value.groups.map((item) => ({
      current: report.value?.group === item,
      href: eventUrl({ op: 'groupReport', group: item }),
      label: item
    }))
  ]
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
        href: historyUrl({ date, domain, ip, reportType: nav.label, type: currentType.value }),
        label: nav.label
      })),
      {
        current: false,
        href: historyUrl({ date, domain, ip, reportType: currentReportType.value, step: '-1', type: currentType.value }),
        label: currentNav.last
      },
      {
        current: false,
        href: historyUrl({ date, domain, ip, reportType: currentReportType.value, step: '1', type: currentType.value }),
        label: currentNav.next
      },
      {
        current: false,
        href: historyUrl({ domain, ip, reportType: currentReportType.value, type: currentType.value }),
        label: 'now'
      }
    ]
  }
  return [
    { current: false, label: '-7d', href: eventUrl({ date, ip, step: '-168', domain }) },
    { current: false, label: '-1d', href: eventUrl({ date, ip, step: '-24', domain }) },
    { current: false, label: '-1h', href: eventUrl({ date, ip, step: '-1', domain }) },
    { current: false, label: '+1h', href: eventUrl({ date, ip, step: '1', domain }) },
    { current: false, label: '+1d', href: eventUrl({ date, ip, step: '24', domain }) },
    { current: false, label: '+7d', href: eventUrl({ date, ip, step: '168', domain }) },
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
    const data = await response.json() as EventReport

    report.value = data
    domainInput.value = data.domain || ''
  } catch (error) {
    loadError.value = `Event 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)
  const action = params.get('op') || 'view'

  params.set('op', 'vueData')
  params.set('vueAction', action)
  return `${contextPath.value}/mvc/r/e?${params.toString()}`
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
  return `${contextPath.value}/mvc/vue/r/e?${params.toString()}`
}

function eventUrl(overrides: Record<string, string | undefined>) {
  const params = baseEventParams()

  params.set('op', overrides.op ?? 'view')
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
  if (overrides.sort !== undefined) {
    params.set('sort', overrides.sort)
  }
  if (overrides.type !== undefined) {
    if (overrides.type) {
      params.set('type', overrides.type)
    } else {
      params.delete('type')
    }
  }
  return `${contextPath.value}/mvc/vue/r/e?${params.toString()}`
}

function baseEventParams() {
  const params = new URLSearchParams()

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
  if (currentType.value) {
    params.set('type', currentType.value)
  }
  return params
}

function goDomain() {
  window.location.href = domainUrl(domainInput.value || currentDomain.value)
}

function graphUrl(row?: EventRow) {
  const params = baseEventParams()

  params.set('op', report.value?.historyMode ? 'historyGraph' : 'graphs')
  if (currentType.value) {
    params.set('type', currentType.value)
  } else if (row?.id) {
    params.set('type', row.id)
  }
  if (row?.id && isNameView.value && !row.totalRow) {
    params.set('name', row.id)
  }
  return `${contextPath.value}/mvc/r/e?${params.toString()}`
}

function graphDataUrl(link: string) {
  const url = new URL(link, window.location.origin)
  const action = url.searchParams.get('op') || 'graphs'

  url.searchParams.set('op', 'vueGraphData')
  url.searchParams.set('vueAction', action)
  return `${url.pathname}?${url.searchParams.toString()}`
}

function graphLinkText(key: string) {
  return activeGraphKey.value === key ? '[:: hide ::]' : '[:: show ::]'
}

function historyUrl(overrides: Record<string, string | undefined>) {
  const params = new URLSearchParams()

  params.set('op', 'history')
  params.set('domain', overrides.domain || currentDomain.value)
  params.set('ip', overrides.ip || currentIp.value)
  if (overrides.date) {
    params.set('date', overrides.date)
  }
  if (overrides.reportType) {
    params.set('reportType', overrides.reportType)
  }
  if (overrides.step) {
    params.set('step', overrides.step)
  }
  if (overrides.type) {
    params.set('type', overrides.type)
  }
  return `${contextPath.value}/mvc/vue/r/e?${params.toString()}`
}

function hourlyNowUrl() {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  if (currentType.value) {
    params.set('type', currentType.value)
  }
  return `${contextPath.value}/mvc/vue/r/e?${params.toString()}`
}

function hostLabel(ip: string) {
  const hostname = report.value?.ipToHostname[ip]

  return hostname ? `${ip}(${hostname})` : ip
}

function logViewUrl(messageUrl: string) {
  return `${contextPath.value}/mvc/vue/r/m/${messageUrl}?domain=${encodeURIComponent(currentDomain.value)}`
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

function rowGraphKey(row: EventRow) {
  return isNameView.value ? `name-${row.index}-${row.id}` : `type-${row.index}-${row.id}`
}

function sortUrl(sort: string) {
  return eventUrl({ sort })
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
    const graph = await response.json() as EventGraph

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
