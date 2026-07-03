<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="transactionUrl">Application</a>
        <a :href="legacyUrl('/mvc/vue/s/config?op=projects')">Configs</a>
        <a :href="legacyUrl('/mvc/vue/r/home?op=view&docName=index')">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
        <TopbarUser />
      </div>
    </header>

    <div class="cat-body">
      <ReportSidebar
        active-report=""
        :context-path="contextPath"
        date=""
        :domain="currentDomain"
        ip="All"
        report-type="day"
      />

      <section class="cat-content">
        <section class="logview-card">
          <div class="logview-title">
            <span>Log View</span>
            <span class="logview-message">{{ displayMessageId }}</span>
          </div>

          <div class="logview-actions">
            <template v-if="report?.waterfall">
              <a :href="modeUrl(false)">Text</a>
              <span class="logview-current">Graph</span>
            </template>
            <template v-else>
              <span class="logview-current">Text</span>
              <a :href="modeUrl(true)">Graph</a>
            </template>
          </div>

          <section v-if="loadError" class="empty-state">
            {{ loadError }}
          </section>

          <section v-else-if="loading" class="empty-state">
            正在加载 Log View 数据...
          </section>

          <section v-else-if="!report?.logView" class="empty-state">
            Sorry, the message is not there. It could be missing or archived.
          </section>

          <section v-else class="logview" v-html="report.logView"></section>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import ReportSidebar from '../components/ReportSidebar.vue'
import TopbarUser from '../components/TopbarUser.vue'

interface LogviewReport {
  contextPath: string
  domain: string
  logView: string
  messageId: string
  waterfall: boolean
}

type LogViewShowHandler = (anchor: HTMLAnchorElement, id: string) => boolean
type LogViewPopupHandler = (id: string) => void

declare global {
  interface Window {
    popup?: LogViewPopupHandler
    show?: LogViewShowHandler
  }
}

const report = ref<LogviewReport | null>(null)
const loading = ref(false)
const loadError = ref('')
const previousShow = window.show
const previousPopup = window.popup

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
const encodedMessageId = computed(() => {
  const path = window.location.pathname
  const prefix = '/mvc/vue/r/m/'
  const index = path.indexOf(prefix)

  if (index < 0) {
    return ''
  }
  return path.substring(index + prefix.length).replace(/\/$/, '')
})
const displayMessageId = computed(() => report.value?.messageId || decodePathSegment(encodedMessageId.value))
const transactionUrl = computed(() => `${contextPath.value}/mvc/vue/r/t?op=view&domain=${encodeURIComponent(currentDomain.value)}&ip=All`)

onMounted(() => {
  installLogViewHandlers()
  loadReport()
})

onBeforeUnmount(() => {
  window.show = previousShow
  window.popup = previousPopup
})

async function loadReport() {
  loading.value = true
  loadError.value = ''

  try {
    const response = await fetch(dataUrl(), { headers: { Accept: 'application/json' } })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    report.value = await response.json() as LogviewReport
  } catch (error) {
    loadError.value = `Log View 数据加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function dataUrl() {
  const params = new URLSearchParams(window.location.search)

  params.set('op', 'vueData')
  return `${contextPath.value}/mvc/r/m/${encodedMessageId.value}?${params.toString()}`
}

function decodePathSegment(value: string) {
  try {
    return decodeURIComponent(value)
  } catch (error) {
    return value
  }
}

function installLogViewHandlers() {
  window.show = (anchor: HTMLAnchorElement, id: string) => {
    const cell = document.getElementById(id)

    if (!cell) {
      return false
    }
    if (!anchor.dataset.originalHtml) {
      anchor.dataset.originalHtml = anchor.innerHTML
    }
    if (anchor.innerHTML !== '[:: hide ::]') {
      anchor.innerHTML = '[:: hide ::]'
      fetch(fragmentUrl(anchor.href), { headers: { Accept: 'text/html' } })
        .then((response) => {
          if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
          }
          return response.text()
        })
        .then((html) => {
          cell.innerHTML = rewriteLogViewHtml(html)
        })
        .catch((error) => {
          cell.innerHTML = `<div class="error">${escapeHtml(error instanceof Error ? error.message : String(error))}</div>`
        })
      cell.style.display = 'block'
    } else {
      anchor.innerHTML = anchor.dataset.originalHtml || anchor.innerHTML
      cell.style.display = 'none'
    }
    return false
  }

  window.popup = (id: string) => {
    const url = `${contextPath.value}/mvc/vue/r/m/${encodeURIComponent(id)}${window.location.search}`

    window.open(url, id)
  }
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function fragmentUrl(link: string) {
  const url = new URL(link, window.location.origin)

  url.pathname = url.pathname.replace('/mvc/vue/r/m/', '/mvc/r/m/')
  url.searchParams.set('header', 'no')
  url.searchParams.set('waterfall', 'false')
  url.searchParams.set('map', 'true')
  return `${url.pathname}?${url.searchParams.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

function modeUrl(waterfall: boolean) {
  const params = new URLSearchParams(window.location.search)

  params.set('domain', currentDomain.value)
  params.set('waterfall', String(waterfall))
  params.delete('op')
  return `${contextPath.value}/mvc/vue/r/m/${encodedMessageId.value}?${params.toString()}`
}

function rewriteLogViewHtml(html: string) {
  const vuePrefix = `${contextPath.value}/mvc/vue/r/m/`

  return html
    .replaceAll(`${contextPath.value}/mvc/r/m/`, vuePrefix)
    .replaceAll(`${contextPath.value}/r/m/`, vuePrefix)
    .replaceAll('/cat/mvc/r/m/', vuePrefix)
    .replaceAll('/cat/r/m/', vuePrefix)
}
</script>

<style>
.logview-card {
  overflow-x: auto;
  border: 1px solid #d9e1ec;
  border-radius: 8px;
  background: #ffffff;
}

.logview-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #e4e9f0;
  padding: 10px 12px;
  color: #0f766e;
  font-size: 15px;
  font-weight: 700;
}

.logview-message {
  color: #667085;
  font-weight: 500;
  word-break: break-all;
}

.logview-actions {
  display: flex;
  gap: 18px;
  padding: 8px 12px;
  color: #0f766e;
  font-size: 14px;
}

.logview-current {
  color: #b42318;
  font-weight: 700;
}

.logview {
  width: auto;
  padding: 0 12px 12px;
  color: #172033;
}

.logview .nav {
  font-size: small;
  white-space: nowrap;
}

.logview table {
  border-collapse: collapse;
}

.logview tr.odd td {
  background-color: #eef2f7;
  font-size: small;
  vertical-align: top;
  white-space: nowrap;
}

.logview tr.even td {
  background-color: #ffffff;
  font-size: small;
  vertical-align: top;
  white-space: nowrap;
}

.logview tr.link td {
  font-size: small;
  vertical-align: top;
  white-space: nowrap;
}

.logview .warn {
  color: #b54708;
}

.logview .error {
  color: #b42318;
}

.logview .header {
  color: #ffffff;
  font-size: small;
  white-space: nowrap;
}
</style>
