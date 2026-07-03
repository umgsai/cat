<template>
  <main class="cat-shell">
    <header class="cat-topbar">
      <div class="cat-brand">
        <strong>CAT</strong>
        <span>Central Application Tracking</span>
      </div>
      <nav class="cat-sections" aria-label="主导航">
        <a :href="applicationUrl">Application</a>
        <a :href="legacyUrl('/mvc/vue/s/config?op=projects')">Configs</a>
        <a class="is-active" :href="documentUrl('index')">Documents</a>
      </nav>
      <div class="cat-actions">
        <a class="star-link" href="https://github.com/dianping/cat/" target="_blank" rel="noreferrer">Star</a>
        <span class="user-greeting">欢迎，admin</span>
      </div>
    </header>

    <div class="cat-body">
      <aside class="cat-sidebar document-sidebar" :class="{ 'is-collapsed': collapsed }" aria-label="文档导航">
        <a
          v-for="item in documentNavs"
          :key="item.name"
          class="sidebar-item document-sidebar-item"
          :class="{ 'is-active': docName === item.name }"
          :href="documentUrl(item.name)"
          :title="item.label"
        >
          <component :is="item.icon" class="sidebar-icon" />
          <span>{{ item.label }}</span>
        </a>
        <button
          class="document-sidebar-toggle"
          type="button"
          :title="collapsed ? '展开菜单' : '收起菜单'"
          :aria-label="collapsed ? '展开菜单' : '收起菜单'"
          @click="toggleCollapsed"
        >
          <PanelLeftOpen v-if="collapsed" />
          <PanelLeftClose v-else />
        </button>
      </aside>

      <section class="cat-content">
        <article class="document-card">
          <section v-if="docName === 'release'" class="document-section">
            <h2>版本说明</h2>
            <div class="document-table-wrap">
              <table class="document-table">
                <thead>
                  <tr>
                    <th>版本</th>
                    <th>说明</th>
                    <th>发布时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in releases" :key="item.version">
                    <td>{{ item.version }}</td>
                    <td>{{ item.description }}</td>
                    <td>{{ item.date }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-else-if="docName === 'plugin'" class="document-section">
            <h2>插件扩展</h2>
            <ul class="document-list">
              <li>
                下载
                <a :href="legacyUrl('/mvc/s/plugin/chrome')">Chrome扩展插件</a>
                ，保存到 cat.crx 文件
              </li>
              <li>选择菜单：窗口 --&gt; 扩展程序，或者在 URL 中输入：chrome://extensions</li>
              <li>用鼠标将 cat.crx 文件拖到扩展程序管理页面，开始安装</li>
              <li>
                对于支持 CAT 的页面，浏览器 URL 框中会出现 CAT 小图标，点击查看 CAT Logview
                <div class="plugin-preview">
                  <img :src="assetUrl('/images/cat-chrome.png')" alt="CAT Chrome extension">
                </div>
              </li>
            </ul>
          </section>

          <section v-else class="document-section">
            <h2>CAT总体介绍</h2>
            <p>CAT(Central Application Tracking)是基于Java开发的实时应用监控平台，为美团点评提供了全面的监控服务和决策支持。</p>
            <p>CAT作为美团点评基础监控组件，它已经在中间件框架（MVC框架，RPC框架，数据库框架，缓存框架等）中得到广泛应用，为美团点评各业务线提供系统的性能指标、健康状况、基础告警等。</p>

            <h2>CAT目前现状</h2>
            <ul class="document-list">
              <li>15台CAT物理监控集群</li>
              <li>单台机器15w qps</li>
              <li>2000+ 业务应用（包括部分.net以及Job）</li>
              <li>7000+ 应用服务器</li>
              <li>50TB 消息，~450亿消息（每天）</li>
            </ul>

            <h2>CAT监控大盘</h2>
            <div class="document-actions">
              <a :href="topUrl" target="_blank" rel="noreferrer">系统报错大盘</a>
            </div>

            <h2 class="document-danger">
              更多接入公司，欢迎在
              <a href="https://github.com/dianping/cat/issues/753" target="_blank" rel="noreferrer">登记！</a>
            </h2>
            <div class="logo-grid">
              <a v-for="logo in logos" :key="logo.src" :href="logo.href" target="_blank" rel="noreferrer">
                <img :src="assetUrl(logo.src)" :alt="logo.name">
              </a>
            </div>
          </section>
        </article>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { BookOpen, Home, KeyRound, PanelLeftClose, PanelLeftOpen } from 'lucide-vue-next'

const sidebarCollapsedKey = 'catDocumentSidebarCollapsed'

const documentNavs = [
  { icon: Home, label: '项目首页', name: 'index' },
  { icon: BookOpen, label: '版本说明', name: 'release' },
  { icon: KeyRound, label: '插件扩展', name: 'plugin' }
]

const releases = [
  { date: '2018-10-01', description: '国庆献礼，重大更新：1、增加多语言客户端；2、增加聚合采样，大幅提升性能效率；3、采用二进制协议通信；4、新版消息文件存储', version: '3.0.0' },
  { date: '2014-12-09', description: '1、增加了客户端路由，去除了NullMessage。', version: '1.3.8' },
  { date: '2014-12-09', description: '1、合并单独的Event，Metric等原子消息，减少消息总量。修复了启动时候连接服务端的bug', version: '1.2.8' },
  { date: '2014-12-09', description: '1、修复了消息截断时候，统计时间的bug', version: '1.1.9' },
  { date: '2014-11-21', description: '1、修复了CAT初始化路由出错导致监控信息丢失的bug', version: '1.1.5' },
  { date: '2014-08-02', description: '1、动态配置CAT的路由策略，支持统一项目名调整', version: '1.1.2' },
  { date: '2014-01-02', description: '1、将ABtest的功能从监控中分离', version: '1.0.1' },
  { date: '2013-12-20', description: '1、修复了CAT监控初始化当服务端都出异常的状况，不会自动连接的Bug', version: '1.0.0' },
  { date: '2013-08-06', description: '1、支持java job的监控，优化了cat的API', version: '0.6.2' },
  { date: '2013-06-06', description: '1、cat客户端的消息长度设置了子消息的最大长度（500），多的消息直接丢弃，以防止内存过大的CAT消息内存泄露', version: '0.6.1' },
  { date: '2013-03-26', description: '1、增加业务监控埋点API。2、修复时间戳调整bug。3、修复classpath获取bug。4、修复CatFilter支持Forward请求', version: '0.6.0' },
  { date: '2012-09-06', description: '1、默认禁止心跳线程获取线程锁信息，以降低对业务线程的影响。', version: '0.4.1' },
  { date: '2012-08-20', description: '1、支持开关动态关闭。2、后端存储重构，支持分布式Logview的查看(关联pigeon的call)。', version: '0.4.0' },
  { date: '2012-07-25', description: '1、规范了CAT客户端的日志。2、规范了后台模块的加载顺序。3、统一服务端配置存取。4、新增心跳报表的Http线程', version: '0.3.4' },
  { date: '2012-07-17', description: '1、修改CAT线程为后台Dameon线程。2、减少CAT的日志输出。3、修复了极端情况客户端丢失部分消息。4、支持CAT的延迟加载。5、修复了0.3.2一个getLog的bug', version: '0.3.3' },
  { date: '2012-07-01', description: '1、修复了配置单个服务器时候，服务器重启，客户端断开链接bug。2、修复了CAT不正常加载时候，内存溢出的问题。（此版本有问题，请更新至0.3.3）', version: '0.3.2' },
  { date: '2012-06-25', description: '1、修复CAT在业务testcase的使用，支持业务运行Testcase在Console上看到运行情况。', version: '0.3.1' },
  { date: '2012-06-15', description: '1、修复CAT在Transaction Name的Nullpoint异常。', version: '0.3.0' },
  { date: '2012-05-01', description: '1、心跳消息监控新增oldgc和newgc  2、更新了ThreadLocal的线程模型（修复了一些无头消息和部分错乱消息）', version: '0.2.5' }
]

const logos = [
  { href: 'http://www.dianping.com/', name: 'Dianping', src: '/images/logo/dianping.png' },
  { href: 'https://www.lufax.com/', name: 'Lufax', src: '/images/logo/lufax.png' },
  { href: 'http://www.ly.com/', name: 'LY', src: '/images/logo/ly.png' },
  { href: 'http://www.liepin.com/', name: 'Liepin', src: '/images/logo/liepin.png' },
  { href: 'http://www.qipeipu.com/', name: 'Qipeipu', src: '/images/logo/qipeipu.jpg' },
  { href: 'http://www.shangpin.com/', name: 'Shangpin', src: '/images/logo/shangping.jpg' },
  { href: 'http://www.travelzen.com/flight/', name: 'Travelzen', src: '/images/logo/zhenlv.png' },
  { href: 'http://www.oppo.com/', name: 'Oppo', src: '/images/logo/oppo.png' }
]

const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return '/cat'
})
const currentParams = computed(() => new URLSearchParams(window.location.search))
const docName = computed(() => {
  const value = currentParams.value.get('docName') || 'index'

  return documentNavs.some((item) => item.name === value) ? value : 'index'
})
const currentDomain = computed(() => currentParams.value.get('domain') || 'cat')
const currentDate = computed(() => currentParams.value.get('date') || '')
const currentIp = computed(() => currentParams.value.get('ip') || 'All')
const currentReportType = computed(() => currentParams.value.get('reportType') || 'day')
const collapsed = ref(readCollapsedState())
const applicationUrl = computed(() => {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('domain', currentDomain.value)
  params.set('ip', currentIp.value)
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  return `${contextPath.value}/mvc/vue/r/t?${params.toString()}`
})
const topUrl = computed(() => `${contextPath.value}/mvc/vue/r?op=view&domain=${encodeURIComponent(currentDomain.value)}`)

function assetUrl(path: string) {
  return `${contextPath.value}${path}`
}

function documentUrl(name: string) {
  const params = new URLSearchParams()

  params.set('op', 'view')
  params.set('docName', name)
  if (currentDomain.value) {
    params.set('domain', currentDomain.value)
  }
  if (currentIp.value) {
    params.set('ip', currentIp.value)
  }
  if (currentDate.value) {
    params.set('date', currentDate.value)
  }
  if (currentReportType.value) {
    params.set('reportType', currentReportType.value)
  }
  return `${contextPath.value}/mvc/vue/r/home?${params.toString()}`
}

function legacyUrl(path: string) {
  return `${contextPath.value}${path}`
}

watch(collapsed, (value) => {
  try {
    window.localStorage.setItem(sidebarCollapsedKey, String(value))
  } catch {
    // Ignore storage failures; the toggle still works for the current page.
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
</script>
