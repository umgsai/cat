<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a :href="applicationUrl">Application</a>
        <a class="is-active" :href="configHomeUrl">Configs</a>
        <a :href="documentUrl">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
      </div>
    </header>

    <div class="cat-body">
      <ConfigSidebar :active-item="activeSidebarItem" :context-path="contextPath" />

      <section class="cat-content">
        <section v-if="isErrorPage" class="permission-panel">
          <div class="permission-icon">
            <ShieldAlert />
          </div>
          <h1>没有权限查看此页面</h1>
          <p>请登录具备配置权限的账号，或联系管理员。</p>
          <p class="permission-contact">jialin.sun@dianping.com,chunan.chen@dianping.com,yong.you@dianping.com</p>
          <div class="permission-actions">
            <a class="config-primary" :href="loginUrl">重新登录</a>
            <a class="config-secondary" :href="configHomeUrl">返回项目配置</a>
          </div>
        </section>

        <section v-else class="config-card">
          <div v-if="loading" class="config-empty">正在加载配置...</div>
          <div v-else-if="loadError" class="config-empty is-error">{{ loadError }}</div>
          <form
            v-else
            class="config-project-form config-xml-form"
            method="post"
            :action="permissionSubmitUrl(currentAction)"
          >
            <input type="hidden" name="vue" value="true">
            <p v-if="isSuccessState(report?.opState)" class="config-state">操作成功</p>
            <p v-else-if="isFailureState(report?.opState)" class="config-state is-error">操作失败</p>
            <table class="config-form-table">
              <tbody>
                <tr>
                  <td>
                    <h2>{{ pageTitle }}</h2>
                  </td>
                </tr>
                <tr>
                  <td>
                    <input type="hidden" name="content" :value="xmlEditorContent">
                    <XmlEditor v-model="xmlEditorContent" />
                  </td>
                </tr>
                <tr>
                  <td class="center">
                    <button class="config-primary" type="submit">提交</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ShieldAlert } from 'lucide-vue-next'

import ConfigSidebar from '../components/ConfigSidebar.vue'
import XmlEditor from '../components/XmlEditor.vue'

interface PermissionReport {
  actionName: string
  contextPath: string
  content: string
  opState: string | null
}

const report = ref<PermissionReport | null>(null)
const xmlEditorContent = ref('')
const loading = ref(false)
const loadError = ref('')

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return report.value?.contextPath || '/cat'
})
const currentParams = computed(() => new URLSearchParams(window.location.search))
const currentAction = computed(() => {
  const action = report.value?.actionName || currentParams.value.get('op') || 'resource'

  return action === 'user' || action === 'error' ? action : 'resource'
})
const isErrorPage = computed(() => currentAction.value === 'error')
const isResourceConfig = computed(() => currentAction.value === 'resource')
const isUserConfig = computed(() => currentAction.value === 'user')
const activeSidebarItem = computed(() => isUserConfig.value ? 'user' : 'resource')
const pageTitle = computed(() => isUserConfig.value ? '用户权限配置' : '资源权限配置')
const configHomeUrl = computed(() => `${contextPath.value}/mvc/vue/s/config?op=projects`)
const applicationUrl = computed(() => `${contextPath.value}/mvc/vue/r/t?op=view&domain=cat&ip=All`)
const documentUrl = computed(() => `${contextPath.value}/mvc/vue/r/home?op=view&docName=index&domain=cat`)
const loginUrl = computed(() => `${contextPath.value}/mvc/vue/s/login?rtnUrl=${encodeURIComponent(permissionUrl(currentAction.value))}`)

onMounted(() => {
  if (!isErrorPage.value) {
    loadPermission()
  }
})

async function loadPermission() {
  loading.value = true
  loadError.value = ''

  try {
    const response = await fetch(dataUrl(), { headers: { Accept: 'application/json' } })

    if (response.redirected && response.url.includes('/mvc/vue/s/login')) {
      window.location.href = response.url
      return
    }
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    if (!isJsonResponse(response)) {
      throw new Error('接口未返回 JSON，请确认当前账号是否已登录并具备权限配置页面权限')
    }
    const data = await response.json() as PermissionReport

    report.value = data
    xmlEditorContent.value = data.content || ''
  } catch (error) {
    loadError.value = `权限配置加载失败: ${error instanceof Error ? error.message : String(error)}`
  } finally {
    loading.value = false
  }
}

function dataUrl() {
  const params = new URLSearchParams()

  params.set('op', 'vueData')
  params.set('vueAction', currentAction.value)
  if (currentParams.value.get('opState')) {
    params.set('opState', currentParams.value.get('opState') || '')
  }
  return `${contextPath.value}/mvc/s/permission?${params.toString()}`
}

function isFailureState(value: string | null | undefined) {
  return value === 'Fail' || value === 'Failure' || value === 'false'
}

function isJsonResponse(response: Response) {
  return (response.headers.get('content-type') || '').toLowerCase().includes('application/json')
}

function isSuccessState(value: string | null | undefined) {
  return value === 'Success' || value === 'true'
}

function permissionSubmitUrl(op: string) {
  return `${contextPath.value}/mvc/s/permission?op=${encodeURIComponent(op)}`
}

function permissionUrl(op: string) {
  return `${contextPath.value}/mvc/vue/s/permission?op=${encodeURIComponent(op)}`
}
</script>
