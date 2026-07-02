<template>
  <StateView v-if="currentView === 'state'" />
  <BusinessView v-else-if="currentView === 'business'" />
  <ConfigView v-else-if="currentView === 'config'" />
  <CrossView v-else-if="currentView === 'cross'" />
  <DocumentsView v-else-if="currentView === 'documents'" />
  <HeartbeatView v-else-if="currentView === 'heartbeat'" />
  <LogView v-else-if="currentView === 'logview'" />
  <PermissionView v-else-if="currentView === 'permission'" />
  <ProblemView v-else-if="currentView === 'problem'" />
  <EventView v-else-if="currentView === 'event'" />
  <TransactionView v-else-if="currentView === 'transaction'" />
  <ReportHomeView v-else-if="currentView === 'report-home'" />
  <LoginView v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'

import BusinessView from './views/BusinessView.vue'
import ConfigView from './views/ConfigView.vue'
import CrossView from './views/CrossView.vue'
import DocumentsView from './views/DocumentsView.vue'
import EventView from './views/EventView.vue'
import HeartbeatView from './views/HeartbeatView.vue'
import LoginView from './views/LoginView.vue'
import LogView from './views/LogView.vue'
import PermissionView from './views/PermissionView.vue'
import ProblemView from './views/ProblemView.vue'
import ReportHomeView from './views/ReportHomeView.vue'
import StateView from './views/StateView.vue'
import TransactionView from './views/TransactionView.vue'

const currentView = computed(() => {
  const path = window.location.pathname

  if (path.endsWith('/mvc/vue/r/state') || path.endsWith('/mvc/vue/r/state/')) {
    return 'state'
  }
  if (path.endsWith('/mvc/vue/r/business') || path.endsWith('/mvc/vue/r/business/')) {
    return 'business'
  }
  if (path.endsWith('/mvc/vue/s/config') || path.endsWith('/mvc/vue/s/config/')) {
    return 'config'
  }
  if (path.endsWith('/mvc/vue/r/cross') || path.endsWith('/mvc/vue/r/cross/')) {
    return 'cross'
  }
  if (path.endsWith('/mvc/vue/r/home') || path.endsWith('/mvc/vue/r/home/')) {
    return 'documents'
  }
  if (path.endsWith('/mvc/vue/r/h') || path.endsWith('/mvc/vue/r/h/')) {
    return 'heartbeat'
  }
  if (path.includes('/mvc/vue/r/m/')) {
    return 'logview'
  }
  if (path.endsWith('/mvc/vue/s/permission') || path.endsWith('/mvc/vue/s/permission/')) {
    return 'permission'
  }
  if (path.endsWith('/mvc/vue/r/p') || path.endsWith('/mvc/vue/r/p/')) {
    return 'problem'
  }
  if (path.endsWith('/mvc/vue/r/e') || path.endsWith('/mvc/vue/r/e/')) {
    return 'event'
  }
  if (path.endsWith('/mvc/vue/r/t') || path.endsWith('/mvc/vue/r/t/')) {
    return 'transaction'
  }
  if (path.endsWith('/mvc/vue/r') || path.endsWith('/mvc/vue/r/')) {
    return 'report-home'
  }
  return 'login'
})
</script>
