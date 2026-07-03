<template>
  <div class="query-bar">
    <div class="time-range">
      <span v-if="reportStart && reportEnd">{{ reportStart }} to {{ reportEnd }}</span>
      <span v-else>Loading...</span>
    </div>
    <div class="query-actions">
      <button class="domain-toggle" type="button" @click="$emit('toggleDomainPanel')">
        {{ showDomainPanel ? '收起' : '全部' }}
      </button>
      <button class="domain-toggle" type="button" @click="$emit('toggleFrequentPanel')">
        {{ showFrequentPanel ? '收起' : '常用' }}
      </button>
      <el-autocomplete
        v-model="domainValue"
        class="domain-input"
        placeholder="input domain for search"
        :fetch-suggestions="searchDomains"
        value-key="value"
        clearable
        @select="$emit('selectDomain', $event)"
        @keyup.enter="$emit('goDomain')"
      />
      <button class="domain-go" type="button" @click="$emit('goDomain')">Go</button>
    </div>
    <div class="time-shortcuts">
      <span>
        【<a class="mode-link" :href="modeSwitchUrl">{{ modeSwitchText }}</a>】
      </span>
      <span v-for="shortcut in shortcuts" :key="shortcut.label">
        [
        <a :class="{ current: shortcut.current || shortcut.active }" :href="shortcut.href">{{ shortcut.label }}</a>
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
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface DomainLine {
  domains: string[]
  name: string
}

interface DomainGroup {
  lines: DomainLine[]
  name: string
}

interface DomainSuggestion {
  category?: string
  label?: string
  value: string
}

interface Shortcut {
  active?: boolean
  current?: boolean
  href: string
  label: string
}

const props = defineProps<{
  domainGroups: DomainGroup[]
  domainInput: string
  domainUrl: (domain: string) => string
  frequentDomains: string[]
  modeSwitchText: string
  modeSwitchUrl: string
  reportEnd?: string
  reportStart?: string
  searchDomains: (query: string, callback: (suggestions: DomainSuggestion[]) => void) => void
  shortcuts: Shortcut[]
  showDomainPanel: boolean
  showFrequentPanel: boolean
}>()

const emit = defineEmits<{
  goDomain: []
  selectDomain: [suggestion: DomainSuggestion]
  toggleDomainPanel: []
  toggleFrequentPanel: []
  'update:domainInput': [value: string]
}>()

const domainValue = computed({
  get: () => props.domainInput,
  set: (value: string) => emit('update:domainInput', value)
})
</script>
