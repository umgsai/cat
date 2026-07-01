<template>
  <ProblemView v-if="currentView === 'problem'" />
  <EventView v-else-if="currentView === 'event'" />
  <TransactionView v-else-if="currentView === 'transaction'" />
  <ReportHomeView v-else-if="currentView === 'report-home'" />
  <LoginView v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'

import EventView from './views/EventView.vue'
import LoginView from './views/LoginView.vue'
import ProblemView from './views/ProblemView.vue'
import ReportHomeView from './views/ReportHomeView.vue'
import TransactionView from './views/TransactionView.vue'

const currentView = computed(() => {
  const path = window.location.pathname

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
