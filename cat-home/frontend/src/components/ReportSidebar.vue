<template>
  <aside class="cat-sidebar" aria-label="报表导航">
    <a
      v-for="item in reportMenus"
      :key="item.name"
      class="sidebar-item"
      :class="{ 'is-active': item.name === activeReport }"
      :href="item.href()"
    >
      <component :is="item.icon" class="sidebar-icon" />
      <span>{{ item.name }}</span>
    </a>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Activity,
  Clock3,
  Flag,
  GitBranch,
  HeartPulse,
  LayoutDashboard,
  ListTree,
  RadioTower
} from 'lucide-vue-next'

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

const reportMenus = [
  { name: 'Dashboard', href: () => vueReportUrl('/mvc/vue/r'), icon: LayoutDashboard },
  { name: 'Transaction', href: () => vueReportUrl('/mvc/vue/r/t'), icon: Clock3 },
  { name: 'Event', href: () => vueReportUrl('/mvc/vue/r/e'), icon: Flag },
  { name: 'Problem', href: () => vueReportUrl('/mvc/vue/r/p'), icon: Activity },
  { name: 'Heartbeat', href: () => legacyReportUrl('/mvc/r/h'), icon: HeartPulse },
  { name: 'Cross', href: () => legacyReportUrl('/mvc/r/cross'), icon: GitBranch },
  { name: 'Business', href: () => businessUrl(), icon: ListTree },
  { name: 'State', href: () => legacyReportUrl('/mvc/r/state'), icon: RadioTower }
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
  return `${props.contextPath}/mvc/r/business?${params.toString()}`
}

function legacyReportUrl(path: string) {
  return `${props.contextPath}${path}?${baseParams().toString()}`
}

function vueReportUrl(path: string) {
  return `${props.contextPath}${path}?${baseParams().toString()}`
}
</script>
