<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="heartbeatUrl({})">Application</a>
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
        active-report="Heartbeat"
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
            <a
              v-for="ip in report.ips"
              :key="ip"
              :class="{ current: report.realIp === ip }"
              :href="heartbeatUrl({ ip })"
              :title="report.ipToHostname[ip] || ip"
            >
              [&nbsp;{{ hostLabel(ip) }}&nbsp;]
            </a>
          </div>
        </section>

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 Heartbeat 数据...
        </section>

        <section v-else-if="report" class="heartbeat-graphs">
          <article v-for="group in report.extensionGroups" :key="group.name" class="heartbeat-group">
            <h2>{{ group.name }} Info</h2>
            <div class="heartbeat-svg-wrap">
              <svg
                version="1.1"
                width="1200"
                :height="group.height * 190"
                xmlns="http://www.w3.org/2000/svg"
              >
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

import ReportSidebar from '../components/ReportSidebar.vue'

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

interface HeartbeatExtensionGroup {
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
  const cookie = readCookie('CAT_DOMAINS')
  const values = cookie.split('|').map((item) => decodeURIComponent(item).trim()).filter(Boolean)

  if (values.length) {
    return values
  }
  return currentDomain.value ? [currentDomain.value] : []
})

const shortcuts = computed(() => {
  const date = currentDate.value
  const ip = currentIp.value
  const domain = currentDomain.value

  return [
    { label: '-7d', href: heartbeatUrl({ date, ip, step: '-168', domain }) },
    { label: '-1d', href: heartbeatUrl({ date, ip, step: '-24', domain }) },
    { label: '-1h', href: heartbeatUrl({ date, ip, step: '-1', domain }) },
    { label: '+1h', href: heartbeatUrl({ date, ip, step: '1', domain }) },
    { label: '+1d', href: heartbeatUrl({ date, ip, step: '24', domain }) },
    { label: '+7d', href: heartbeatUrl({ date, ip, step: '168', domain }) },
    { label: 'now', href: hourlyNowUrl() }
  ]
})

const historyModeUrl = computed(() => legacyHeartbeatUrl({ op: 'history' }))

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

function heartbeatUrl(overrides: Record<string, string | undefined>) {
  const params = baseHeartbeatParams()

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

function readCookie(name: string) {
  const prefix = `${name}=`
  const item = document.cookie.split('; ').find((entry) => entry.startsWith(prefix))

  return item ? item.substring(prefix.length) : ''
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
