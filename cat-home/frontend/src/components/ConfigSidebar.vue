<template>
  <aside class="cat-sidebar config-sidebar" :class="{ 'is-collapsed': collapsed }" aria-label="配置导航">
    <details class="sidebar-group" :open="collapsed || isProjectSection">
      <summary class="sidebar-item" :class="{ 'is-active': isProjectSection }" title="项目配置信息">
        <Settings class="sidebar-icon" />
        <span>项目配置信息</span>
        <ChevronDown class="sidebar-chevron" />
      </summary>
      <nav class="sidebar-submenu" aria-label="项目配置信息">
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'projects' }" :href="configUrl('projects')">
          项目基本信息
        </a>
        <a
          class="sidebar-subitem"
          :class="{ 'is-active': activeItem === 'domainGroupConfigs' }"
          :href="configUrl('domainGroupConfigs')"
        >
          机器分组配置
        </a>
      </nav>
    </details>

    <details class="sidebar-group" :open="collapsed || isBusinessSection">
      <summary class="sidebar-item" :class="{ 'is-active': isBusinessSection }" title="应用监控配置">
        <Cloud class="sidebar-icon" />
        <span>应用监控配置</span>
        <ChevronDown class="sidebar-chevron" />
      </summary>
      <nav class="sidebar-submenu" aria-label="应用监控配置">
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'businessList' }" :href="configUrl('businessList')">
          业务监控配置
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'businessTagConfig' }" :href="configUrl('businessTagConfig')">
          业务标签配置
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'displayPolicy' }" :href="configUrl('displayPolicy')">
          心跳报表展示
        </a>
      </nav>
    </details>

    <details class="sidebar-group" :open="collapsed || isAlertSection">
      <summary class="sidebar-item" :class="{ 'is-active': isAlertSection }" title="应用告警配置">
        <Zap class="sidebar-icon" />
        <span>应用告警配置</span>
        <ChevronDown class="sidebar-chevron" />
      </summary>
      <nav class="sidebar-submenu" aria-label="应用告警配置">
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'transactionRule' }" :href="configUrl('transactionRule')">
          Transaction告警
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'eventRule' }" :href="configUrl('eventRule')">
          Event告警
        </a>
        <a
          class="sidebar-subitem"
          :class="{ 'is-active': activeItem === 'exception' }"
          :href="configUrl('exception', { type: exceptionTab })"
        >
          异常告警配置
        </a>
        <a
          class="sidebar-subitem"
          :class="{ 'is-active': activeItem === 'heartbeatRuleConfigList' }"
          :href="configUrl('heartbeatRuleConfigList')"
        >
          心跳告警配置
        </a>
      </nav>
    </details>

    <details class="sidebar-group" :open="collapsed || isSystemSection">
      <summary class="sidebar-item" :class="{ 'is-active': isSystemSection }" title="全局系统配置">
        <Cog class="sidebar-icon" />
        <span>全局系统配置</span>
        <ChevronDown class="sidebar-chevron" />
      </summary>
      <nav class="sidebar-submenu" aria-label="全局系统配置">
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'alertPolicy' }" :href="configUrl('alertPolicy')">告警策略</a>
        <a
          class="sidebar-subitem"
          :class="{ 'is-active': activeItem === 'alertDefaultReceivers' }"
          :href="configUrl('alertDefaultReceivers')"
        >
          默认告警人
        </a>
        <a
          class="sidebar-subitem"
          :class="{ 'is-active': activeItem === 'alertSenderConfigUpdate' }"
          :href="configUrl('alertSenderConfigUpdate')"
        >
          告警服务端
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'serverConfigUpdate' }" :href="configUrl('serverConfigUpdate')">
          服务端配置
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'sampleConfigUpdate' }" :href="configUrl('sampleConfigUpdate')">
          消息采样配置
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'routerConfigUpdate' }" :href="configUrl('routerConfigUpdate')">
          客户端路由
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'resource' }" :href="permissionUrl('resource')">
          资源权限配置
        </a>
        <a class="sidebar-subitem" :class="{ 'is-active': activeItem === 'user' }" :href="permissionUrl('user')">
          用户权限配置
        </a>
      </nav>
    </details>

    <button
      class="config-sidebar-toggle"
      type="button"
      :title="collapsed ? '展开菜单' : '收起菜单'"
      :aria-label="collapsed ? '展开菜单' : '收起菜单'"
      @click="toggleCollapsed"
    >
      <PanelLeftOpen v-if="collapsed" />
      <PanelLeftClose v-else />
    </button>
  </aside>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ChevronDown, Cloud, Cog, PanelLeftClose, PanelLeftOpen, Settings, Zap } from 'lucide-vue-next'

const sidebarCollapsedKey = 'catConfigSidebarCollapsed'

const props = withDefaults(defineProps<{
  activeItem: string
  contextPath: string
  currentDomain?: string
  exceptionTab?: string
}>(), {
  currentDomain: '',
  exceptionTab: 'threshold'
})

const projectItems = ['projects', 'domainGroupConfigs']
const businessItems = ['businessList', 'businessTagConfig', 'displayPolicy']
const alertItems = ['transactionRule', 'eventRule', 'exception', 'heartbeatRuleConfigList']
const systemItems = ['alertPolicy', 'alertDefaultReceivers', 'alertSenderConfigUpdate', 'serverConfigUpdate',
  'sampleConfigUpdate', 'routerConfigUpdate', 'resource', 'user']

const isProjectSection = computed(() => projectItems.includes(props.activeItem))
const isBusinessSection = computed(() => businessItems.includes(props.activeItem))
const isAlertSection = computed(() => alertItems.includes(props.activeItem))
const isSystemSection = computed(() => systemItems.includes(props.activeItem))
const collapsed = ref(readCollapsedState())

watch(collapsed, (value) => {
  try {
    window.localStorage.setItem(sidebarCollapsedKey, String(value))
  } catch {
    // Ignore storage failures; the toggle should still work for the current page.
  }
})

function readCollapsedState() {
  try {
    return window.localStorage.getItem(sidebarCollapsedKey) === 'true'
  } catch {
    return false
  }
}

function toggleCollapsed() {
  collapsed.value = !collapsed.value
}

function configUrl(op: string, overrides: Record<string, string | undefined> = {}) {
  const params = new URLSearchParams()

  params.set('op', op)
  if (props.currentDomain) {
    params.set('domain', props.currentDomain)
  }
  for (const [key, value] of Object.entries(overrides)) {
    if (value) {
      params.set(key, value)
    }
  }
  return `${props.contextPath}/mvc/vue/s/config?${params.toString()}`
}

function permissionUrl(op: string) {
  return `${props.contextPath}/mvc/vue/s/permission?op=${encodeURIComponent(op)}`
}
</script>
