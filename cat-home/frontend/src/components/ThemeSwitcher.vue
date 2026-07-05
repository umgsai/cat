<template>
  <div class="theme-switcher" ref="switcherRef">
    <button class="theme-switcher-trigger" @click="open = !open">
      <span class="theme-switcher-swatch" :style="{ background: currentSwatch }"></span>
      <span class="theme-switcher-label">{{ currentLabel }}</span>
      <svg class="theme-switcher-chevron" :class="{ 'is-open': open }" width="12" height="12" viewBox="0 0 12 12">
        <path d="M3 4.5L6 7.5L9 4.5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>
    <div v-if="open" class="theme-switcher-menu">
      <button
        v-for="theme in themeList"
        :key="theme.id"
        class="theme-switcher-option"
        :class="{ 'is-active': theme.id === currentThemeId }"
        @click="selectTheme(theme.id)"
      >
        <span class="theme-switcher-swatch" :style="{ background: theme.swatch }"></span>
        <span>{{ theme.label }}</span>
        <svg v-if="theme.id === currentThemeId" class="theme-switcher-check" width="14" height="14" viewBox="0 0 14 14">
          <path d="M3 7L6 10L11 4" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getCurrentThemeId, setTheme } from '../theme'
import { themeList } from '../theme/themes'

const open = ref(false)
const currentThemeId = ref(getCurrentThemeId())
const switcherRef = ref<HTMLDivElement | null>(null)

const currentSwatch = computed(() => themeList.find((t) => t.id === currentThemeId.value)?.swatch || '#0f766e')
const currentLabel = computed(() => themeList.find((t) => t.id === currentThemeId.value)?.label || 'Teal')

function selectTheme(themeId: string) {
  setTheme(themeId)
  currentThemeId.value = themeId
  open.value = false
}

function handleClickOutside(event: MouseEvent) {
  if (switcherRef.value && !switcherRef.value.contains(event.target as Node)) {
    open.value = false
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    open.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.theme-switcher {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.theme-switcher-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #d9e1ec;
  border-radius: 6px;
  padding: 5px 10px;
  background: #ffffff;
  color: #475467;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
}

.theme-switcher-trigger:hover {
  border-color: var(--color-primary-border, #9ccfc7);
  background: var(--color-primary-bg-hover, #eef5f4);
  color: var(--color-primary, #0f766e);
}

.theme-switcher-swatch {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 3px;
  flex-shrink: 0;
}

.theme-switcher-chevron {
  transition: transform 0.16s ease;
  flex-shrink: 0;
}

.theme-switcher-chevron.is-open {
  transform: rotate(180deg);
}

.theme-switcher-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  z-index: 30;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 140px;
  border: 1px solid #d9e1ec;
  border-radius: 6px;
  padding: 5px;
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.12);
}

.theme-switcher-option {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  border-radius: 5px;
  padding: 7px 10px;
  background: transparent;
  color: #344054;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
}

.theme-switcher-option:hover {
  background: var(--color-primary-bg-hover, #eef5f4);
  color: var(--color-primary, #0f766e);
}

.theme-switcher-option.is-active {
  background: var(--color-primary-bg-active, #e5f5f2);
  color: var(--color-primary, #0f766e);
  font-weight: 700;
}

.theme-switcher-check {
  margin-left: auto;
  color: var(--color-primary, #0f766e);
}
</style>
