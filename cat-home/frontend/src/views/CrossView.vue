<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="crossUrl({})">Application</a>
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
        active-report="Cross"
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
              【<a class="mode-link" :href="historyModeUrl">{{ historyModeText }}</a>】
            </span>
            <span v-for="shortcut in shortcuts" :key="shortcut.label">
              [
              <a :class="{ current: shortcut.active }" :href="shortcut.href">{{ shortcut.label }}</a>
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

        <section class="transaction-card cross-query">
          <span>查询当前这个时间段内，一个方法被哪些应用调用</span>
          <input v-model="methodInput" type="text" />
          <button type="button" @click="queryCrossMethod">Submit</button>
        </section>

        <section v-if="report" class="selector-panel">
          <div class="selector-row">
            <a :class="{ current: currentIp === 'All' }" :href="crossUrl({ ip: 'All' })">
              [&nbsp;All&nbsp;]
            </a>
            <a
              v-for="ip in report.ips"
              :key="ip"
              :class="{ current: currentIp === ip }"
              :href="crossUrl({ ip })"
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
          正在加载 Cross 数据...
        </section>

        <section v-else-if="report" class="transaction-card">
          <div class="transaction-table-wrap">
            <table v-if="isQueryView" class="transaction-table cross-table">
              <thead>
                <tr>
                  <th class="left">类型</th>
                  <th class="left">项目</th>
                  <th class="left">IP</th>
                  <th class="left">方法名</th>
                  <th class="right">Total</th>
                  <th class="right">Failure</th>
                  <th class="right">Failure%</th>
                  <th class="right">Avg(ms)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in report.queryItems" :key="`${item.type}-${item.domain}-${item.ip}-${item.method}`">
                  <td class="left">{{ item.type }}</td>
                  <td class="left">{{ item.domain }}</td>
                  <td class="left">{{ item.ip }}</td>
                  <td class="left long-text">{{ item.method }}</td>
                  <td class="right">{{ formatInteger(item.totalCount) }}</td>
                  <td class="right">{{ formatInteger(item.failureCount) }}</td>
                  <td class="right">{{ formatRate(item.failurePercent, 4) }}</td>
                  <td class="right">{{ formatDecimal(item.avg, 2) }}</td>
                </tr>
              </tbody>
            </table>

            <table v-else class="transaction-table cross-table">
              <tbody>
                <template v-if="report.callProjects.length">
                  <tr>
                    <td class="center section-title" colspan="7"><strong>调用其他 Pigeon 服务</strong></td>
                  </tr>
                  <tr>
                    <th class="left">Type</th>
                    <th class="left"><a :href="sortUrl('callSort', 'name')">RemoteProject</a></th>
                    <th class="right"><a :href="sortUrl('callSort', 'total')">Total</a></th>
                    <th class="right"><a :href="sortUrl('callSort', 'failure')">Failure</a></th>
                    <th class="right"><a :href="sortUrl('callSort', 'failurePercent')">Failure%</a></th>
                    <th class="right"><a :href="sortUrl('callSort', 'avg')">Avg(ms)</a></th>
                    <th class="right">QPS</th>
                  </tr>
                  <tr v-for="item in report.callProjects" :key="`call-${item.projectName}`">
                    <td class="left">{{ item.type }}</td>
                    <td class="left">
                      <a :href="legacyHostUrl(item.projectName)">{{ item.projectName }}</a>
                    </td>
                    <td class="right">{{ formatInteger(item.totalCount) }}</td>
                    <td class="right">{{ formatInteger(item.failureCount) }}</td>
                    <td class="right">{{ formatRate(item.failurePercent, 4) }}</td>
                    <td class="right">{{ formatDecimal(item.avg, 2) }}</td>
                    <td class="right">{{ formatDecimal(item.tps, 2) }}</td>
                  </tr>
                </template>

                <template v-if="report.serviceProjects.length">
                  <tr>
                    <td class="center section-title" colspan="7"><strong>提供 Pigeon 服务 [ 服务端数据 ]</strong></td>
                    <template v-if="hasCallerProjects">
                      <td></td>
                      <td class="center section-title" colspan="7"><strong>提供 Pigeon 服务 [ 客户端数据 ]</strong></td>
                    </template>
                  </tr>
                  <tr>
                    <th class="left">Type</th>
                    <th class="left"><a :href="sortUrl('serviceSort', 'name')">RemoteProject</a></th>
                    <th class="right"><a :href="sortUrl('serviceSort', 'total')">Total</a></th>
                    <th class="right"><a :href="sortUrl('serviceSort', 'failure')">Failure</a></th>
                    <th class="right"><a :href="sortUrl('serviceSort', 'failurePercent')">Failure%</a></th>
                    <th class="right"><a :href="sortUrl('serviceSort', 'avg')">Avg(ms)</a></th>
                    <th class="right">QPS</th>
                    <template v-if="hasCallerProjects">
                      <th></th>
                      <th class="left">Type</th>
                      <th class="left">RemoteProject</th>
                      <th class="right">Total</th>
                      <th class="right">Failure</th>
                      <th class="right">Failure%</th>
                      <th class="right">Avg(ms)</th>
                      <th class="right">QPS</th>
                    </template>
                  </tr>
                  <tr v-for="item in report.serviceProjects" :key="`service-${item.projectName}`">
                    <td class="left">{{ item.type }}</td>
                    <td class="left">
                      <a :href="legacyHostUrl(item.projectName)">{{ item.projectName }}</a>
                    </td>
                    <td class="right">{{ formatInteger(item.totalCount) }}</td>
                    <td class="right">{{ formatInteger(item.failureCount) }}</td>
                    <td class="right">{{ formatRate(item.failurePercent, 4) }}</td>
                    <td class="right">{{ formatDecimal(item.avg, 2) }}</td>
                    <td class="right">{{ formatDecimal(item.tps, 2) }}</td>
                    <template v-if="hasCallerProjects">
                      <td></td>
                      <td class="left">{{ callerProject(item.projectName)?.type || '' }}</td>
                      <td class="left">
                        <a v-if="callerProject(item.projectName)" :href="legacyHostUrl(callerProject(item.projectName)?.projectName || '')">
                          {{ callerProject(item.projectName)?.projectName }}
                        </a>
                      </td>
                      <td class="right">{{ callerProject(item.projectName) ? formatInteger(callerProject(item.projectName)?.totalCount || 0) : '' }}</td>
                      <td class="right">{{ callerProject(item.projectName) ? formatInteger(callerProject(item.projectName)?.failureCount || 0) : '' }}</td>
                      <td class="right">{{ callerProject(item.projectName) ? formatRate(callerProject(item.projectName)?.failurePercent || 0, 4) : '' }}</td>
                      <td class="right">{{ callerProject(item.projectName) ? formatDecimal(callerProject(item.projectName)?.avg || 0, 2) : '' }}</td>
                      <td class="right">{{ callerProject(item.projectName) ? formatDecimal(callerProject(item.projectName)?.tps || 0, 2) : '' }}</td>
                    </template>
                  </tr>
                </template>
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

interface DomainLine {
  name: string
  domains: string[]
}

interface DomainDepartment {
  name: string
  lines: DomainLine[]
}

interface CrossTypeRow {
  avg: number
  failureCount: number
  failurePercent: number
  ip: string
  projectName: string
  totalCount: number
  tps: number
  type: string
}

interface CrossQueryRow {
  avg: number
  domain: string
  failureCount: number
  failurePercent: number
  ip: string
  method: string
  totalCount: number
  tps: number
  type: string
}

interface CrossReport {
  action: string
  callProjects: CrossTypeRow[]
  callSort: string
  callerProjects: Record<string, CrossTypeRow>
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  domainGroups: DomainDepartment[]
  historyMode: boolean
  ipAddress: string
  ipToHostname: Record<string, string>
  ips: string[]
  longDate: number
  method: string
  project: string
  queryItems: CrossQueryRow[]
  queryName: string
  remoteIp: string
  reportEnd: string
  reportStart: string
  reportType: string
  sample: number
  serviceProjects: CrossTypeRow[]
  serviceSort: string
}

const report = ref<CrossReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const domainInput = ref('')
const methodInput = ref('')
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
const currentAction = computed(() => report.value?.action || currentParams.value.get('op') || 'view')
const currentDomain = computed(() => report.value?.domain || currentParams.value.get('domain') || 'cat')
const currentIp = computed(() => report.value?.ipAddress || currentParams.value.get('ip') || 'All')
const currentDate = computed(() => report.value?.date || currentParams.value.get('date') || '')
const currentReportType = computed(() => report.value?.reportType || currentParams.value.get('reportType') || 'day')
const currentMethod = computed(() => report.value?.method || currentParams.value.get('method') || '')
const domainGroups = computed(() => report.value?.domainGroups || [])
const hasCallerProjects = computed(() => Object.keys(report.value?.callerProjects || {}).length > 0)
const isQueryView = computed(() => currentAction.value === 'query')
const isHistoryMode = computed(() => Boolean(report.value?.historyMode))
const historyModeText = computed(() => isHistoryMode.value ? '切到小时模式' : '切到历史模式')
const historyModeUrl = computed(() => {
  if (isHistoryMode.value) {
    return hourlyNowUrl()
  }
  return crossUrl({ op: 'history' })
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
  const cookie = readCookie('CAT_DOMAINS')
  const values = cookie.split('|').map((item) => decodeURIComponent(item).trim()).filter(Boolean)

  if (values.length) {
    return values
  }
  return currentDomain.value ? [currentDomain.value] : []
})

const shortcuts = computed(() => {
  const date = currentDate.value
  const domain = currentDomain.value
  const ip = currentIp.value

  if (isHistoryMode.value) {
    return [
      { active: currentReportType.value === 'day', label: 'day', href: crossUrl({ date, domain, ip, op: 'history', reportType: 'day' }) },
      { active: currentReportType.value === 'week', label: 'week', href: crossUrl({ date, domain, ip, op: 'history', reportType: 'week' }) },
      { active: currentReportType.value === 'month', label: 'month', href: crossUrl({ date, domain, ip, op: 'history', reportType: 'month' }) },
      { active: false, label: 'last', href: crossUrl({ date, domain, ip, op: 'history', step: '-1' }) },
      { active: false, label: 'next', href: crossUrl({ date, domain, ip, op: 'history', step: '1' }) },
      { active: false, label: 'now', href: historyNowUrl() }
    ]
  }
  return [
    { active: false, label: '-7d', href: crossUrl({ date, domain, ip, step: '-168' }) },
    { active: false, label: '-1d', href: crossUrl({ date, domain, ip, step: '-24' }) },
    { active: false, label: '-1h', href: crossUrl({ date, domain, ip, step: '-1' }) },
    { active: false, label: '+1h', href: crossUrl({ date, domain, ip, step: '1' }) },
    { active: false, label: '+1d', href: crossUrl({ date, domain, ip, step: '24' }) },
    { active: false, label: '+7d', href: crossUrl({ date, domain, ip, step: '168' }) },
    { active: false, label: 'now', href: hourlyNowUrl() }
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
    const data = await response.json() as CrossReport

    report.value = data
    domainInput.value = data.domain || ''
    methodInput.value = data.method || ''
  } catch (error) {
    loadError.value = `Cross 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function baseCrossParams() {
  const params = new URLSearchParams()

  params.set('op', currentAction.value)
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
  if (currentMethod.value) {
    params.set('method', currentMethod.value)
  }
  if (report.value?.callSort) {
    params.set('callSort', report.value.callSort)
  }
  if (report.value?.serviceSort) {
    params.set('serviceSort', report.value.serviceSort)
  }
  return params
}

function callerProject(projectName: string) {
  return report.value?.callerProjects[projectName] || null
}

function crossUrl(overrides: Record<string, string | undefined>) {
  const params = baseCrossParams()

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
  return `${contextPath.value}/mvc/vue/r/cross?${params.toString()}`
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)
  const action = params.get('op') || 'view'

  params.set('op', 'vueData')
  params.set('vueAction', action)
  return `${contextPath.value}/mvc/r/cross?${params.toString()}`
}

function domainUrl(domain: string) {
  return crossUrl({ domain, ip: 'All', op: isHistoryMode.value ? 'history' : 'view' })
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

function formatRate(value: number, digits: number) {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: digits,
    minimumFractionDigits: digits,
    style: 'percent'
  }).format(value || 0)
}

function goDomain() {
  window.location.href = domainUrl(domainInput.value || currentDomain.value)
}

function historyNowUrl() {
  const params = new URLSearchParams()

  params.set('op', 'history')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  params.set('reportType', currentReportType.value)
  return `${contextPath.value}/mvc/vue/r/cross?${params.toString()}`
}

function hourlyNowUrl() {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  if (report.value?.callSort) {
    params.set('callSort', report.value.callSort)
  }
  if (report.value?.serviceSort) {
    params.set('serviceSort', report.value.serviceSort)
  }
  return `${contextPath.value}/mvc/vue/r/cross?${params.toString()}`
}

function hostLabel(ip: string) {
  const hostname = report.value?.ipToHostname[ip]

  return hostname ? `${ip}(${hostname})` : ip
}

function legacyHostUrl(projectName: string) {
  const params = new URLSearchParams()

  params.set('op', isHistoryMode.value ? 'historyHost' : 'host')
  params.set('domain', currentDomain.value)
  params.set('date', currentDate.value)
  params.set('ip', currentIp.value)
  params.set('project', projectName)
  params.set('reportType', currentReportType.value)
  return `${contextPath.value}/mvc/r/cross?${params.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function queryCrossMethod() {
  window.location.href = crossUrl({ method: methodInput.value, op: 'query' })
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

function sortUrl(kind: 'callSort' | 'serviceSort', sort: string) {
  return crossUrl({ [kind]: sort })
}
</script>
