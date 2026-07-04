<template>
  <section v-if="rows.length" class="selector-panel">
    <div
      v-for="(row, rowIndex) in rows"
      :key="rowIndex"
      class="selector-row"
      :class="{ 'group-row': rowIndex > 0 }"
    >
      <a
        v-for="item in row"
        :key="`${item.href}-${item.label}`"
        :class="{ current: item.current }"
        :href="item.href"
        :title="item.title"
      >
        <template v-if="item.current">{{ item.label }}</template>
        <template v-else>[&nbsp;{{ item.label }}&nbsp;]</template>
      </a>
    </div>
  </section>
</template>

<script setup lang="ts">
interface ReportSelectorItem {
  current?: boolean
  href: string
  label: string
  title?: string
}

defineProps<{
  rows: ReportSelectorItem[][]
}>()
</script>
