<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="reportUrl('/mvc/r/t')">Application</a>
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
        active-report="Dashboard"
        :context-path="contextPath"
        :date="currentDate"
        :domain="currentDomain"
        :ip="currentIp"
        :report-type="currentReportType"
      />

      <section class="cat-content">
        <div class="query-bar">
          <div class="time-range">
            <span v-if="dashboard">{{ dashboard.reportStart }} to {{ dashboard.reportEnd }}</span>
            <span v-else>Loading...</span>
          </div>
          <div class="time-shortcuts">
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
                    <a v-for="item in line.domains" :key="item" :href="vueTopUrl({ domain: item })">
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
                  <a v-for="item in frequentDomains" :key="item" :href="vueTopUrl({ domain: item })">
                    [&nbsp;{{ item }}&nbsp;]
                  </a>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <nav class="minute-pagination" aria-label="分钟选择">
          <a
            v-for="item in dashboard?.minutes || []"
            :key="item"
            :class="{ 'is-active': item === dashboard?.minute, 'is-disabled': item > (dashboard?.maxMinute || 0) }"
            :href="item > (dashboard?.maxMinute || 0) ? undefined : vueTopUrl({ minute: String(item) })"
          >
            {{ pad(item) }}
          </a>
        </nav>

        <section class="dashboard-status" :class="{ 'has-error': Boolean(dashboard?.message) }">
          <div class="status-icon">
            <TriangleAlert v-if="dashboard?.message" />
            <CircleCheck v-else />
          </div>
          <div>
            <p class="status-kicker">Dashboard</p>
            <h1 v-if="dashboard?.message">出问题CAT的服务端: {{ dashboard.message }}</h1>
            <h1 v-else>CAT服务端正常</h1>
          </div>
        </section>

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 Dashboard 数据...
        </section>

        <section v-else-if="!topResults.length" class="empty-state"></section>

        <section v-else class="top-result-grid">
          <table v-for="group in topResults" :key="group.minute" class="top-result-table">
            <thead>
              <tr>
                <th colspan="2">{{ group.minute }}</th>
              </tr>
              <tr>
                <th>系统</th>
                <th>个</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in group.items" :key="`${group.minute}-${item.domain}`" :style="parseStyle(item.style)">
                <td>
                  <el-tooltip
                    :content="item.errorInfo || item.domain"
                    raw-content
                    placement="bottom-start"
                    :show-after="200"
                  >
                    <a :href="problemUrl(item.domain)" :style="parseStyle(item.linkStyle)">
                      {{ item.shortDomain || item.domain }}
                    </a>
                  </el-tooltip>
                </td>
                <td class="number-cell">{{ Math.round(item.value) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CircleCheck, TriangleAlert } from 'lucide-vue-next'

import ReportSidebar from '../components/ReportSidebar.vue'

interface DomainLine {
  name: string
  domains: string[]
}

interface DomainDepartment {
  name: string
  lines: DomainLine[]
}

interface TopResultItem {
  domain: string
  shortDomain: string
  value: number
  errorInfo: string
  style: string
  linkStyle: string
}

interface TopResultGroup {
  minute: string
  items: TopResultItem[]
}

interface TopDashboard {
  contextPath: string
  domain: string
  ipAddress: string
  date: string
  minute: number
  maxMinute: number
  minutes: number[]
  minuteCount: number
  topCount: number
  fullScreen: boolean
  refresh: boolean
  frequency: number
  reportStart: string
  reportEnd: string
  message: string
  sample: number
  domainGroups: DomainDepartment[]
  topResults: TopResultGroup[]
}

const dashboard = ref<TopDashboard | null>(null)
const domainInput = ref('')
const loading = ref(false)
const loadError = ref('')
const showDomainPanel = ref(false)
const showFrequentPanel = ref(false)

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return dashboard.value?.contextPath || '/cat'
})

const currentParams = computed(() => new URLSearchParams(window.location.search))
const currentDomain = computed(() => dashboard.value?.domain || currentParams.value.get('domain') || '')
const currentIp = computed(() => dashboard.value?.ipAddress || currentParams.value.get('ip') || 'All')
const currentDate = computed(() => dashboard.value?.date || currentParams.value.get('date') || '')
const currentReportType = computed(() => currentParams.value.get('reportType') || 'day')
const domainGroups = computed(() => dashboard.value?.domainGroups || [])
const topResults = computed(() => dashboard.value?.topResults || [])

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
    { label: '-7d', href: vueTopUrl({ date, ip, step: '-168', domain }) },
    { label: '-1d', href: vueTopUrl({ date, ip, step: '-24', domain }) },
    { label: '-1h', href: vueTopUrl({ date, ip, step: '-1', domain }) },
    { label: '+1h', href: vueTopUrl({ date, ip, step: '1', domain }) },
    { label: '+1d', href: vueTopUrl({ date, ip, step: '24', domain }) },
    { label: '+7d', href: vueTopUrl({ date, ip, step: '168', domain }) },
    { label: 'now', href: vueTopUrl({ domain }) }
  ]
})

onMounted(() => {
  loadDashboard()
})

async function loadDashboard() {
  loading.value = true
  loadError.value = ''

  try {
    const response = await fetch(dataUrl(), { headers: { Accept: 'application/json' } })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    const data = await response.json() as TopDashboard

    dashboard.value = data
    domainInput.value = data.domain || ''
  } catch (error) {
    loadError.value = `Dashboard 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)

  params.set('op', 'vueData')
  return `${contextPath.value}/mvc/r/top?${params.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function reportUrl(path: string) {
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
  return `${contextPath.value}${path}?${params.toString()}`
}

function vueTopUrl(overrides: Record<string, string | undefined>) {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', overrides.domain ?? currentDomain.value)
  if (overrides.date ?? currentDate.value) {
    params.set('date', overrides.date ?? currentDate.value)
  }
  if (overrides.ip ?? currentIp.value) {
    params.set('ip', overrides.ip ?? currentIp.value)
  }
  if (overrides.minute) {
    params.set('minute', overrides.minute)
  }
  if (overrides.step) {
    params.set('step', overrides.step)
  }
  return `${contextPath.value}/mvc/vue/r?${params.toString()}`
}

function problemUrl(domain: string) {
  const params = new URLSearchParams()

  params.set('domain', domain)
  params.set('date', currentDate.value)
  return `${contextPath.value}/mvc/r/p?${params.toString()}`
}

function goDomain() {
  window.location.href = vueTopUrl({ domain: domainInput.value })
}

function selectDomain(item: { value: string }) {
  domainInput.value = item.value
  goDomain()
}

function searchDomains(query: string, callback: (items: Array<{ label: string; value: string; category: string }>) => void) {
  const keyword = query.trim().toLowerCase()

  if (!keyword) {
    callback(domainSuggestions.value)
    return
  }
  callback(domainSuggestions.value.filter((item) => item.value.toLowerCase().includes(keyword)))
}

function readCookie(name: string) {
  const prefix = `${name}=`
  const item = document.cookie.split('; ').find((entry) => entry.startsWith(prefix))

  return item ? item.substring(prefix.length) : ''
}

function parseStyle(style: string) {
  const result: Record<string, string> = {}

  for (const rule of style.split(';')) {
    const [key, value] = rule.split(':')

    if (key && value) {
      result[key.trim()] = value.trim()
    }
  }
  return result
}

function pad(value: number) {
  return String(value).padStart(2, '0')
}
</script>
