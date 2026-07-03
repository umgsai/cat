<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a class="is-active" :href="applicationUrl">Application</a>
        <a :href="configsUrl">Configs</a>
        <a :href="documentsUrl">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
        <span class="user-greeting">欢迎，admin</span>
      </div>
    </header>

    <div class="cat-body">
      <ReportSidebar
        :active-report="activeReport"
        :context-path="contextPath"
        :date="date"
        :domain="domain"
        :ip="ip"
        :report-type="reportType"
      />

      <section class="cat-content">
        <slot />
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import ReportSidebar from './ReportSidebar.vue'

const props = defineProps<{
  activeReport: string
  applicationUrl: string
  contextPath: string
  date?: string
  domain?: string
  ip?: string
  reportType?: string
}>()

const configsUrl = computed(() => `${props.contextPath}/mvc/vue/s/config?op=projects`)
const documentsUrl = computed(() => `${props.contextPath}/mvc/vue/r/home?op=view&docName=index`)
</script>
