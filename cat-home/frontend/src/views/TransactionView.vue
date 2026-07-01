<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="transactionUrl({})">Application</a>
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
        active-report="Transaction"
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
          <span>采样直接影响的是 Transaction、Event 的总量和 QPS，不影响 Metric、Heartbeat、Exception 等数据。</span>
        </section>

        <section v-if="report" class="selector-panel">
          <div class="selector-row">
            <a :class="{ current: currentIp === 'All' }" :href="transactionUrl({ ip: 'All' })">
              [&nbsp;All&nbsp;]
            </a>
            <a
              v-for="ip in report.ips"
              :key="ip"
              :class="{ current: currentIp === ip }"
              :href="transactionUrl({ ip })"
              :title="report.ipToHostname[ip] || ip"
            >
              [&nbsp;{{ hostLabel(ip) }}&nbsp;]
            </a>
          </div>
        </section>

        <section v-if="report?.groups.length" class="selector-panel">
          <div class="selector-row">
            <a
              v-for="item in report.groups"
              :key="item"
              :class="{ current: report.group === item }"
              :href="transactionUrl({ op: 'groupReport', group: item })"
            >
              [&nbsp;{{ item }}&nbsp;]
            </a>
          </div>
        </section>

        <section v-if="loadError" class="empty-state">
          {{ loadError }}
        </section>

        <section v-else-if="loading" class="empty-state">
          正在加载 Transaction 数据...
        </section>

        <section v-else-if="report" class="transaction-card">
          <div v-if="isNameView" class="transaction-filter">
            <input v-model="queryNameInput" type="text" name="queryname" />
            <button type="button" @click="filterByName">Filter</button>
            <span>支持多个字符串查询，例如 sql|url|task，查询结果为包含任一 sql、url、task 的列。</span>
          </div>

          <div class="transaction-table-wrap">
            <table class="transaction-table">
              <thead>
                <tr v-if="!isNameView">
                  <th class="left"><a :href="sortUrl('type')">Type</a></th>
                  <th class="right"><a :href="sortUrl('total')">Total</a></th>
                  <th class="right"><a :href="sortUrl('failure')">Failure</a></th>
                  <th class="right"><a :href="sortUrl('failurePercent')">Failure%</a></th>
                  <th class="right">Sample Link</th>
                  <th class="right"><a :href="sortUrl('min')">Min</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('max')">Max</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('avg')">Avg</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('95line')">95Line</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('99line')">99.9Line</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('std')">Std</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('total')">QPS</a></th>
                </tr>
                <tr v-else>
                  <th class="left">
                    <a :href="graphUrl()">[:: show ::]</a>
                    <a :href="sortUrl('type')">Name</a>
                  </th>
                  <th class="right"><a :href="sortUrl('total')">Total</a></th>
                  <th class="right"><a :href="sortUrl('failure')">Failure</a></th>
                  <th class="right"><a :href="sortUrl('failurePercent')">Failure%</a></th>
                  <th class="right">Sample Link</th>
                  <th class="right"><a :href="sortUrl('min')">Min</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('max')">Max</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('avg')">Avg</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('95line')">95Line</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('99line')">99.9Line</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('std')">Std</a>(ms)</th>
                  <th class="right"><a :href="sortUrl('total')">QPS</a></th>
                  <th class="right"><a :href="sortUrl('total')">Percent%</a></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in report.rows" :key="`${row.index}-${row.id}`">
                  <td class="left long-text" :class="{ center: row.totalRow }">
                    <template v-if="!isNameView">
                      <a :href="graphUrl(row)">[:: show ::]</a>
                      <a :href="transactionUrl({ type: row.id, ip: currentIp })">{{ row.id }}</a>
                    </template>
                    <template v-else-if="row.totalRow">
                      {{ truncate(row.id) }}
                    </template>
                    <template v-else>
                      <a :href="graphUrl(row)">[:: show ::]</a>
                      <span>{{ truncate(row.id) }}</span>
                    </template>
                  </td>
                  <td class="right">{{ formatInteger(row.totalCount) }}</td>
                  <td class="right">{{ formatInteger(row.failCount) }}</td>
                  <td class="right">{{ formatRate(row.failPercent, 4) }}</td>
                  <td class="right sample-link">
                    <a v-if="row.messageUrl" :href="logViewUrl(row.messageUrl)">Log View</a>
                  </td>
                  <td class="right">{{ formatDecimal(row.min, 1) }}</td>
                  <td class="right">{{ formatDecimal(row.max, 1) }}</td>
                  <td class="right">{{ formatDecimal(row.avg, 1) }}</td>
                  <td class="right">{{ isNameView && row.totalRow ? '-' : formatDecimal(row.line95Value, 1) }}</td>
                  <td class="right">{{ isNameView && row.totalRow ? '-' : formatDecimal(row.line99Value, 1) }}</td>
                  <td class="right">{{ formatDecimal(row.std, 1) }}</td>
                  <td class="right">{{ formatDecimal(row.tps, 1) }}</td>
                  <td v-if="isNameView" class="right">{{ formatRate(row.totalPercent, 2) }}</td>
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

interface DomainLine {
  name: string
  domains: string[]
}

interface DomainDepartment {
  name: string
  lines: DomainLine[]
}

interface TransactionRow {
  avg: number
  encodedName: string
  encodedType: string
  failCount: number
  failPercent: number
  id: string
  index: number
  line95Value: number
  line99Value: number
  max: number
  messageUrl: string
  min: number
  std: number
  totalCount: number
  totalPercent: number
  totalRow: boolean
  tps: number
}

interface TransactionReport {
  contextPath: string
  date: string
  displayDomain: string
  domain: string
  domainGroups: DomainDepartment[]
  encodedQueryName: string
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
  queryName: string
  reportEnd: string
  reportStart: string
  reportType: string
  rows: TransactionRow[]
  sample: number
  sortBy: string
  type: string
}

const report = ref<TransactionReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const domainInput = ref('')
const queryNameInput = ref('')
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
const currentType = computed(() => report.value?.type || currentParams.value.get('type') || '')
const currentQueryName = computed(() => report.value?.queryName || currentParams.value.get('queryname') || '')
const domainGroups = computed(() => report.value?.domainGroups || [])
const isNameView = computed(() => Boolean(currentType.value))

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
    { label: '-7d', href: transactionUrl({ date, ip, step: '-168', domain }) },
    { label: '-1d', href: transactionUrl({ date, ip, step: '-24', domain }) },
    { label: '-1h', href: transactionUrl({ date, ip, step: '-1', domain }) },
    { label: '+1h', href: transactionUrl({ date, ip, step: '1', domain }) },
    { label: '+1d', href: transactionUrl({ date, ip, step: '24', domain }) },
    { label: '+7d', href: transactionUrl({ date, ip, step: '168', domain }) },
    { label: 'now', href: transactionUrl({ domain, ip }) }
  ]
})

const historyModeUrl = computed(() => transactionUrl({ op: 'history', domain: currentDomain.value, ip: currentIp.value }))

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
    const data = await response.json() as TransactionReport

    report.value = data
    domainInput.value = data.domain || ''
    queryNameInput.value = data.queryName || ''
  } catch (error) {
    loadError.value = `Transaction 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)
  const action = params.get('op') || 'view'

  params.set('op', 'vueData')
  params.set('vueAction', action)
  return `${contextPath.value}/mvc/r/t?${params.toString()}`
}

function filterByName() {
  window.location.href = transactionUrl({ queryname: queryNameInput.value })
}

function goDomain() {
  window.location.href = domainUrl(domainInput.value || currentDomain.value)
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
  return `${contextPath.value}/mvc/vue/r/t?${params.toString()}`
}

function graphUrl(row?: TransactionRow) {
  const params = baseTransactionParams()

  params.set('op', 'graphs')
  if (currentType.value) {
    params.set('type', currentType.value)
  } else if (row?.id) {
    params.set('type', row.id)
  }
  if (row?.id && isNameView.value && !row.totalRow) {
    params.set('name', row.id)
  }
  return `${contextPath.value}/mvc/r/t?${params.toString()}`
}

function hostLabel(ip: string) {
  const hostname = report.value?.ipToHostname[ip]

  return hostname ? `${ip}(${hostname})` : ip
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function logViewUrl(messageUrl: string) {
  return `${contextPath.value}/mvc/r/m/${messageUrl}?domain=${encodeURIComponent(currentDomain.value)}`
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

function sortUrl(sort: string) {
  return transactionUrl({ sort })
}

function transactionUrl(overrides: Record<string, string | undefined>) {
  const params = baseTransactionParams()

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
  if (overrides.queryname !== undefined) {
    params.set('queryname', overrides.queryname)
  }
  return `${contextPath.value}/mvc/vue/r/t?${params.toString()}`
}

function baseTransactionParams() {
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
  if (currentQueryName.value) {
    params.set('queryname', currentQueryName.value)
  }
  return params
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

function truncate(value: string) {
  return value && value.length > 120 ? value.substring(0, 120) : value
}
</script>
