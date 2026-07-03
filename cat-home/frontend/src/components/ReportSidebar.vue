<template>
  <aside class="cat-sidebar report-sidebar" :class="{ 'is-collapsed': collapsed }" aria-label="报表导航">
    <a
      v-for="item in reportMenus"
      :key="item.name"
      class="sidebar-item"
      :class="{ 'is-active': item.name === activeReport }"
      :href="item.href()"
      :title="item.name"
    >
      <component :is="item.icon" class="sidebar-icon" />
      <span>{{ item.name }}</span>
    </a>
    <button
      class="report-sidebar-toggle"
      type="button"
      :title="collapsed ? '展开菜单' : '收起菜单'"
      :aria-label="collapsed ? '展开菜单' : '收起菜单'"
      @click="toggleCollapsed"
    >
      <PanelLeftOpen v-if="collapsed" />
      <PanelLeftClose v-else />
    </button>
  </aside>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  Activity,
  Clock3,
  Flag,
  GitBranch,
  HeartPulse,
  LayoutDashboard,
  ListTree,
  PanelLeftClose,
  PanelLeftOpen,
  RadioTower
} from 'lucide-vue-next'

const sidebarCollapsedKey = 'catReportSidebarCollapsed'

const props = defineProps<{
  activeReport: string
  contextPath: string
  date?: string
  domain?: string
  ip?: string
  reportType?: string
}>()

const currentDomain = computed(() => props.domain || 'cat')
const currentIp = computed(() => props.ip || 'All')
const currentDate = computed(() => props.date || '')
const currentReportType = computed(() => props.reportType || 'day')
const collapsed = ref(readCollapsedState())

const reportMenus = [
  { name: 'Dashboard', href: () => vueReportUrl('/mvc/vue/r'), icon: LayoutDashboard },
  { name: 'Transaction', href: () => vueReportUrl('/mvc/vue/r/t'), icon: Clock3 },
  { name: 'Event', href: () => vueReportUrl('/mvc/vue/r/e'), icon: Flag },
  { name: 'Problem', href: () => vueReportUrl('/mvc/vue/r/p'), icon: Activity },
  { name: 'Heartbeat', href: () => vueReportUrl('/mvc/vue/r/h'), icon: HeartPulse },
  { name: 'Cross', href: () => vueReportUrl('/mvc/vue/r/cross'), icon: GitBranch },
  { name: 'Business', href: () => businessUrl(), icon: ListTree },
  { name: 'State', href: () => vueReportUrl('/mvc/vue/r/state'), icon: RadioTower }
]

function baseParams() {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  if (currentIp.value) {
    params.set('ip', currentIp.value)
  }
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  return params
}

function businessUrl() {
  const params = new URLSearchParams()

  params.set('name', currentDomain.value)
  params.set('type', 'domain')
  return `${props.contextPath}/mvc/vue/r/business?${params.toString()}`
}

function legacyReportUrl(path: string) {
  return `${props.contextPath}${path}?${baseParams().toString()}`
}

function vueReportUrl(path: string) {
  return `${props.contextPath}${path}?${baseParams().toString()}`
}

watch(collapsed, (value) => {
  try {
    window.localStorage.setItem(sidebarCollapsedKey, String(value))
  } catch {
    // Ignore storage failures; the toggle still works for the current page.
  }
})

function readCollapsedState() {
  try {
    return window.localStorage.getItem(sidebarCollapsedKey) === 'true'
  } catch {
    return false
  }
}

function toggleCollapsed() {
  collapsed.value = !collapsed.value
}
</script>
