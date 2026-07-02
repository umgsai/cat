<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a :href="applicationUrl">Application</a>
        <a class="is-active" :href="configUrl">Configs</a>
        <a :href="documentUrl">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
      </div>
    </header>

    <div class="cat-body">
      <aside class="cat-sidebar config-sidebar" aria-label="配置导航">
        <a class="sidebar-item is-active" :href="configUrl">
          <ShieldAlert class="sidebar-icon" />
          <span>权限提示</span>
        </a>
      </aside>

      <section class="cat-content">
        <section class="permission-panel">
          <div class="permission-icon">
            <ShieldAlert />
          </div>
          <h1>没有权限查看此页面</h1>
          <p>请登录具备配置权限的账号，或联系管理员。</p>
          <p class="permission-contact">jialin.sun@dianping.com,chunan.chen@dianping.com,yong.you@dianping.com</p>
          <div class="permission-actions">
            <a class="config-primary" :href="loginUrl">重新登录</a>
            <a class="config-secondary" :href="configUrl">返回项目配置</a>
          </div>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ShieldAlert } from 'lucide-vue-next'

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return '/cat'
})

const configUrl = computed(() => `${contextPath.value}/mvc/vue/s/config?op=projects`)
const applicationUrl = computed(() => `${contextPath.value}/mvc/vue/r/t?op=view&domain=cat&ip=All`)
const documentUrl = computed(() => `${contextPath.value}/mvc/vue/r/home?op=view&docName=index&domain=cat`)
const loginUrl = computed(() => `${contextPath.value}/mvc/vue/s/login?rtnUrl=${encodeURIComponent(configUrl.value)}`)
</script>
