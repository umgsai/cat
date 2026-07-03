<template>
  <section v-if="error" class="empty-state">
    {{ error }}
  </section>

  <section v-else-if="loading" class="empty-state">
    {{ loadingText }}
  </section>

  <slot v-else />
</template>

<script setup lang="ts">
defineProps<{
  error: string
  loading: boolean
  loadingText: string
}>()
</script>
