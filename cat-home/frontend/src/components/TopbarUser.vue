<template>
  <span v-if="displayName" class="user-greeting">
    <span class="user-greeting-label">欢迎</span>
    <span class="user-greeting-name">{{ displayName }}</span>
  </span>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

const LOGIN_TOKEN_COOKIE = 'ct'
const displayName = ref('')

onMounted(() => {
  displayName.value = readLoginName()
})

function readLoginName() {
  const token = readCookie(LOGIN_TOKEN_COOKIE)

  if (!token) {
    return ''
  }

  const [realName] = token.split('|')

  return decodeCookieValue(stripQuotes(realName.trim()))
}

function readCookie(name: string) {
  if (typeof document === 'undefined') {
    return ''
  }

  const prefix = `${name}=`
  const cookie = document.cookie
    .split(/;\s*/)
    .find((item) => item.startsWith(prefix))

  return cookie ? cookie.substring(prefix.length) : ''
}

function stripQuotes(value: string) {
  if (value.length >= 2 && value.startsWith('"') && value.endsWith('"')) {
    return value.substring(1, value.length - 1)
  }
  if (value.startsWith('"')) {
    return value.substring(1)
  }
  return value
}

function decodeCookieValue(value: string) {
  try {
    return decodeURIComponent(value).trim()
  } catch {
    return value.trim()
  }
}
</script>
