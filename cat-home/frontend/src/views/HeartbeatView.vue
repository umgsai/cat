<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="heartbeatUrl({})">Application</a>
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
        active-report="Heartbeat"
        :context-path="contextPath"
        :date="currentDate"
        :domain="currentDomain"
        :ip="currentIp"
        :report-type="currentReportType"
      />

      <section class="cat-content">
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

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 Heartbeat 数据...
        </section>

        <section v-else-if="report" class="heartbeat-graphs">
          <article v-for="group in report.extensionGroups" :key="group.name" class="heartbeat-group">
            <h2>{{ group.name }} Info</h2>
            <div class="heartbeat-chart-grid">
              <HeartbeatBarChartPanel
                v-for="chart in group.charts"
                :key="chart.key"
                :label="chart.label"
                :title="chart.title"
                :values="chart.values"
              />
            </div>
            <div v-if="!group.charts.length" class="heartbeat-svg-wrap">
              <svg version="1.1" width="1200" :height="group.height * 190" xmlns="http://www.w3.org/2000/svg">
                <g v-for="svg in group.svgs" :key="svg.name" v-html="svg.content"></g>
              </svg>
            </div>
          </article>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import HeartbeatBarChartPanel from '../components/HeartbeatBarChartPanel.vue'
import ReportQueryBar from '../components/ReportQueryBar.vue'
import ReportSelectorPanel from '../components/ReportSelectorPanel.vue'
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

interface HeartbeatSvg {
  content: string
  name: string
}

interface HeartbeatChart {
  key: string
  label: string
  title: string
  values: number[]
}

interface HeartbeatExtensionGroup {
  charts: HeartbeatChart[]
  height: number
  name: string
  svgs: HeartbeatSvg[]
}

interface HeartbeatReport {
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  domainGroups: DomainDepartment[]
  extensionGroups: HeartbeatExtensionGroup[]
  extensionType: string
  groups: string[]
  historyMode: boolean
  ipAddress: string
  ipToHostname: Record<string, string>
  ips: string[]
  longDate: number
  realIp: string
  reportEnd: string
  reportStart: string
  reportType: string
  sample: number
  type: string
}

const report = ref<HeartbeatReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const domainInput = ref('')
const showDomainPanel = ref(false)
const showFrequentPanel = ref(false)
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
const domainGroups = computed(() => report.value?.domainGroups || [])
const isHistoryMode = computed(() => report.value?.historyMode ?? currentParams.value.get('op') === 'history')
const currentHistoryNav = computed(() => historyNavs.find((item) => item.label === currentReportType.value) || historyNavs[2])
const modeSwitchText = computed(() => isHistoryMode.value ? '切到小时模式' : '切到历史模式')
const modeSwitchUrl = computed(() => heartbeatUrl({ op: isHistoryMode.value ? 'view' : 'history' }))

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
  return [
    report.value.ips.map((ip) => ({
      current: report.value?.realIp === ip,
      href: heartbeatUrl({ ip }),
      label: hostLabel(ip),
      title: report.value?.ipToHostname[ip] || ip
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
    { current: false, label: '-7d', href: heartbeatUrl({ date, ip, step: '-168', domain }) },
    { current: false, label: '-1d', href: heartbeatUrl({ date, ip, step: '-24', domain }) },
    { current: false, label: '-1h', href: heartbeatUrl({ date, ip, step: '-1', domain }) },
    { current: false, label: '+1h', href: heartbeatUrl({ date, ip, step: '1', domain }) },
    { current: false, label: '+1d', href: heartbeatUrl({ date, ip, step: '24', domain }) },
    { current: false, label: '+7d', href: heartbeatUrl({ date, ip, step: '168', domain }) },
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
    const data = await response.json() as HeartbeatReport

    report.value = data
    domainInput.value = data.domain || ''
  } catch (error) {
    loadError.value = `Heartbeat 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function baseHeartbeatParams() {
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
  return params
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)
  const action = params.get('op') || 'view'

  params.set('op', 'vueData')
  params.set('vueAction', action)
  return `${contextPath.value}/mvc/r/h?${params.toString()}`
}

function domainUrl(domain: string) {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', domain)
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  return `${contextPath.value}/mvc/vue/r/h?${params.toString()}`
}

function formatDecimal(value: number, digits: number) {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: digits,
    minimumFractionDigits: digits
  }).format(value || 0)
}

function formatPercent(value: number) {
  return `${formatDecimal(value * 100, 2)}%`
}

function goDomain() {
  window.location.href = domainUrl(domainInput.value || currentDomain.value)
}

function hourlyNowUrl() {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  return `${contextPath.value}/mvc/vue/r/h?${params.toString()}`
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
  return `${contextPath.value}/mvc/vue/r/h?${params.toString()}`
}

function heartbeatUrl(overrides: Record<string, string | undefined>) {
  const params = baseHeartbeatParams()

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
  return `${contextPath.value}/mvc/vue/r/h?${params.toString()}`
}

function hostLabel(ip: string) {
  const hostname = report.value?.ipToHostname[ip]

  return hostname ? `${ip}(${hostname})` : ip
}

function legacyHeartbeatUrl(overrides: Record<string, string | undefined>) {
  const params = baseHeartbeatParams()

  if (overrides.op !== undefined) {
    params.set('op', overrides.op)
  }
  return `${contextPath.value}/mvc/r/h?${params.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
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
</script>
