import { expect, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'

interface BackendPageCase {
  activeGroup: string
  activeSubItem: string
  path: string
  title: string
}

const backendPages: BackendPageCase[] = [
  {
    activeGroup: '项目配置信息',
    activeSubItem: '项目基本信息',
    path: '/cat/mvc/vue/s/config?op=projects&domain=cat',
    title: '项目基本信息'
  },
  {
    activeGroup: '项目配置信息',
    activeSubItem: '机器分组配置',
    path: '/cat/mvc/vue/s/config?op=domainGroupConfigs&domain=cat',
    title: '机器分组配置'
  },
  {
    activeGroup: '应用监控配置',
    activeSubItem: '业务监控配置',
    path: '/cat/mvc/vue/s/config?op=businessList&domain=cat',
    title: '业务监控配置'
  },
  {
    activeGroup: '应用监控配置',
    activeSubItem: '业务标签配置',
    path: '/cat/mvc/vue/s/config?op=businessTagConfig&domain=cat',
    title: '业务标签配置'
  },
  {
    activeGroup: '应用监控配置',
    activeSubItem: '心跳报表展示',
    path: '/cat/mvc/vue/s/config?op=displayPolicy&domain=cat',
    title: '心跳报表展示'
  },
  {
    activeGroup: '应用告警配置',
    activeSubItem: 'Transaction告警',
    path: '/cat/mvc/vue/s/config?op=transactionRule&domain=cat',
    title: 'Transaction告警'
  },
  {
    activeGroup: '应用告警配置',
    activeSubItem: 'Event告警',
    path: '/cat/mvc/vue/s/config?op=eventRule&domain=cat',
    title: 'Event告警'
  },
  {
    activeGroup: '应用告警配置',
    activeSubItem: '异常告警配置',
    path: '/cat/mvc/vue/s/config?op=exception&domain=cat',
    title: '异常阈值配置'
  },
  {
    activeGroup: '应用告警配置',
    activeSubItem: '心跳告警配置',
    path: '/cat/mvc/vue/s/config?op=heartbeatRuleConfigList&domain=cat',
    title: '心跳告警配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '告警策略',
    path: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '告警策略配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '默认告警人',
    path: '/cat/mvc/vue/s/config?op=alertDefaultReceivers&domain=cat',
    title: '默认告警人配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '告警服务端',
    path: '/cat/mvc/vue/s/config?op=alertSenderConfigUpdate&domain=cat',
    title: '告警发送服务配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '服务端配置',
    path: '/cat/mvc/vue/s/config?op=serverConfigUpdate&domain=cat',
    title: '服务端配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '消息采样配置',
    path: '/cat/mvc/vue/s/config?op=sampleConfigUpdate&domain=cat',
    title: '消息采样配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '客户端路由',
    path: '/cat/mvc/vue/s/config?op=routerConfigUpdate&domain=cat',
    title: '客户端路由'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '资源权限配置',
    path: '/cat/mvc/vue/s/permission?op=resource',
    title: '资源权限配置'
  },
  {
    activeGroup: '全局系统配置',
    activeSubItem: '用户权限配置',
    path: '/cat/mvc/vue/s/permission?op=user',
    title: '用户权限配置'
  }
]

test.describe('管理后台只读冒烟测试', () => {
  for (const pageCase of backendPages) {
    test(`${pageCase.activeSubItem} 页面可访问并正确加载`, async ({ page }) => {
      const consoleErrors: string[] = []
      const pageErrors: string[] = []
      const failedResponses: string[] = []
      const dataResponses: Array<{ contentType: string; status: number; url: string }> = []

      page.on('console', (message) => {
        if (message.type() === 'error') {
          consoleErrors.push(message.text())
        }
      })
      page.on('pageerror', (error) => {
        pageErrors.push(error.message)
      })
      page.on('response', (response) => {
        const url = response.url()

        if (!url.includes('/cat/mvc/')) {
          return
        }
        if (url.includes('op=vueData')) {
          dataResponses.push({
            contentType: response.headers()['content-type'] || '',
            status: response.status(),
            url
          })
        }
        if (response.status() >= 400) {
          failedResponses.push(`${response.status()} ${url}`)
        }
      })

      await openBackendPage(page, pageCase.path)

      await expect(page.locator('.cat-topbar')).toBeVisible()
      await expect(page.locator('.config-sidebar')).toBeVisible()
      await expect(page.locator('.config-card')).toBeVisible()
      await expect(page.locator('.sidebar-item.is-active')).toContainText(pageCase.activeGroup)
      await expect(page.locator('.sidebar-subitem.is-active')).toContainText(pageCase.activeSubItem)
      await expect(page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2')).toContainText(pageCase.title)
      await expect(page.locator('body')).not.toContainText('加载失败')
      await expect(page.locator('body')).not.toContainText('接口未返回 JSON')

      expect(new URL(page.url()).pathname).toMatch(/^\/cat\/mvc\/vue\/s\/(config|permission)$/)
      expect(dataResponses, '每个后台页面都应该请求 vueData 接口').not.toHaveLength(0)
      for (const response of dataResponses) {
        expect(response.status, `${response.url} 应返回成功状态`).toBeLessThan(400)
        expect(response.contentType.toLowerCase(), `${response.url} 应返回 JSON`).toContain('application/json')
      }
      expect(failedResponses, '后台页面加载过程中不应该出现 4xx/5xx 响应').toEqual([])
      expect(pageErrors, '后台页面不应该出现未捕获 JS 异常').toEqual([])
      expect(consoleErrors, '后台页面不应该打印 console.error').toEqual([])
    })
  }
})

async function openBackendPage(page: Page, path: string) {
  await page.goto(path)
  await page.waitForLoadState('domcontentloaded')

  if (new URL(page.url()).pathname.endsWith('/cat/mvc/vue/s/login')) {
    await login(page)
  }

  await page.waitForURL(/\/cat\/mvc\/vue\/s\/(config|permission)/)
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2').waitFor({ state: 'visible' })
}

async function login(page: Page) {
  await page.getByPlaceholder('请输入账号').fill(adminUsername)
  await page.getByPlaceholder('请输入密码').fill(adminPassword)
  await page.getByRole('button', { name: '登录' }).click()
}
