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
        [&nbsp;{{ item.label }}&nbsp;]
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
