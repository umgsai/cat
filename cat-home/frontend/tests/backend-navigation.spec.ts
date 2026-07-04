import { expect, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'

interface NavigationCase {
  activeSubItem: string
  groupName: string
  linkName: string
  startPath: string
  title: string
}

interface EntryCase {
  entryName: string
  expectedTitle: string
  returnTitle?: string
  startPath: string
}

const sidebarNavigationCases: NavigationCase[] = [
  {
    activeSubItem: '机器分组配置',
    groupName: '项目配置信息',
    linkName: '机器分组配置',
    startPath: '/cat/mvc/vue/s/config?op=projects&domain=cat',
    title: '机器分组配置'
  },
  {
    activeSubItem: '业务监控配置',
    groupName: '应用监控配置',
    linkName: '业务监控配置',
    startPath: '/cat/mvc/vue/s/config?op=projects&domain=cat',
    title: '业务监控配置'
  },
  {
    activeSubItem: '业务标签配置',
    groupName: '应用监控配置',
    linkName: '业务标签配置',
    startPath: '/cat/mvc/vue/s/config?op=businessList&domain=cat',
    title: '业务标签配置'
  },
  {
    activeSubItem: '心跳报表展示',
    groupName: '应用监控配置',
    linkName: '心跳报表展示',
    startPath: '/cat/mvc/vue/s/config?op=businessList&domain=cat',
    title: '心跳报表展示'
  },
  {
    activeSubItem: 'Transaction告警',
    groupName: '应用告警配置',
    linkName: 'Transaction告警',
    startPath: '/cat/mvc/vue/s/config?op=projects&domain=cat',
    title: 'Transaction告警'
  },
  {
    activeSubItem: 'Event告警',
    groupName: '应用告警配置',
    linkName: 'Event告警',
    startPath: '/cat/mvc/vue/s/config?op=transactionRule&domain=cat',
    title: 'Event告警'
  },
  {
    activeSubItem: '异常告警配置',
    groupName: '应用告警配置',
    linkName: '异常告警配置',
    startPath: '/cat/mvc/vue/s/config?op=transactionRule&domain=cat',
    title: '异常阈值配置'
  },
  {
    activeSubItem: '心跳告警配置',
    groupName: '应用告警配置',
    linkName: '心跳告警配置',
    startPath: '/cat/mvc/vue/s/config?op=transactionRule&domain=cat',
    title: '心跳告警配置'
  },
  {
    activeSubItem: '告警策略',
    groupName: '全局系统配置',
    linkName: '告警策略',
    startPath: '/cat/mvc/vue/s/config?op=projects&domain=cat',
    title: '告警策略配置'
  },
  {
    activeSubItem: '默认告警人',
    groupName: '全局系统配置',
    linkName: '默认告警人',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '默认告警人配置'
  },
  {
    activeSubItem: '告警服务端',
    groupName: '全局系统配置',
    linkName: '告警服务端',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '告警发送服务配置'
  },
  {
    activeSubItem: '服务端配置',
    groupName: '全局系统配置',
    linkName: '服务端配置',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '服务端配置'
  },
  {
    activeSubItem: '消息采样配置',
    groupName: '全局系统配置',
    linkName: '消息采样配置',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '消息采样配置'
  },
  {
    activeSubItem: '客户端路由',
    groupName: '全局系统配置',
    linkName: '客户端路由',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '客户端路由'
  },
  {
    activeSubItem: '资源权限配置',
    groupName: '全局系统配置',
    linkName: '资源权限配置',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '资源权限配置'
  },
  {
    activeSubItem: '用户权限配置',
    groupName: '全局系统配置',
    linkName: '用户权限配置',
    startPath: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '用户权限配置'
  }
]

const entryCases: EntryCase[] = [
  {
    entryName: '添加',
    expectedTitle: '添加项目',
    returnTitle: '项目基本信息',
    startPath: '/cat/mvc/vue/s/config?op=projects&domain=cat'
  },
  {
    entryName: '添加',
    expectedTitle: '机器分组配置',
    returnTitle: '机器分组配置',
    startPath: '/cat/mvc/vue/s/config?op=domainGroupConfigs&domain=cat'
  },
  {
    entryName: '新增',
    expectedTitle: '修改业务监控规则',
    returnTitle: '业务监控配置',
    startPath: '/cat/mvc/vue/s/config?op=businessList&domain=cat'
  },
  {
    entryName: '新增',
    expectedTitle: '编辑Transaction监控规则',
    returnTitle: 'Transaction告警',
    startPath: '/cat/mvc/vue/s/config?op=transactionRule&domain=cat'
  },
  {
    entryName: '新增',
    expectedTitle: '编辑Event监控规则',
    returnTitle: 'Event告警',
    startPath: '/cat/mvc/vue/s/config?op=eventRule&domain=cat'
  },
  {
    entryName: '新增',
    expectedTitle: '编辑心跳告警规则',
    returnTitle: '心跳告警配置',
    startPath: '/cat/mvc/vue/s/config?op=heartbeatRuleConfigList&domain=cat'
  }
]

test.describe('管理后台导航入口测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  for (const pageCase of sidebarNavigationCases) {
    test(`左侧菜单可以进入 ${pageCase.activeSubItem}`, async ({ page }) => {
      await openBackendPage(page, pageCase.startPath)
      await expandSidebarGroup(page, pageCase.groupName)
      await page.locator('.config-sidebar').getByRole('link', { name: pageCase.linkName }).click()
      await assertVueBackendPage(page, pageCase.title)
      await expect(page.locator('.sidebar-subitem.is-active')).toContainText(pageCase.activeSubItem)
      assertNotLegacyPage(page)
    })
  }

  for (const pageCase of entryCases) {
    test(`${pageCase.startPath} 的 ${pageCase.entryName} 入口可以进入并返回`, async ({ page }) => {
      await openBackendPage(page, pageCase.startPath)
      await page.locator('.config-heading a.config-primary').filter({ hasText: pageCase.entryName }).click()
      await assertVueBackendPage(page, pageCase.expectedTitle)
      assertNotLegacyPage(page)

      if (pageCase.returnTitle) {
        await page.locator('.config-heading a.config-primary').filter({ hasText: '返回' }).click()
        await assertVueBackendPage(page, pageCase.returnTitle)
        assertNotLegacyPage(page)
      }
    })
  }

  test('异常告警配置可以在异常阈值和异常过滤之间切换', async ({ page }) => {
    await openBackendPage(page, '/cat/mvc/vue/s/config?op=exception&domain=cat&type=threshold')
    await expect(page.locator('.exception-tabs a.is-active')).toContainText('异常阈值')

    await page.getByRole('link', { name: '异常过滤' }).click()
    await assertVueBackendPage(page, '异常过滤配置')
    await expect(page.locator('.exception-tabs a.is-active')).toContainText('异常过滤')
    assertNotLegacyPage(page)

    await page.getByRole('link', { name: '异常阈值' }).click()
    await assertVueBackendPage(page, '异常阈值配置')
    await expect(page.locator('.exception-tabs a.is-active')).toContainText('异常阈值')
    assertNotLegacyPage(page)
  })
})

async function login(page: Page) {
  await page.goto('/cat/mvc/vue/s/config?op=projects&domain=cat')

  if (new URL(page.url()).pathname.endsWith('/cat/mvc/vue/s/login')) {
    await page.getByPlaceholder('请输入账号').fill(adminUsername)
    await page.getByPlaceholder('请输入密码').fill(adminPassword)
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
  }
}

async function openBackendPage(page: Page, path: string) {
  await page.goto(path)
  await page.waitForLoadState('domcontentloaded')
  await assertVueBackendPage(page)
}

async function expandSidebarGroup(page: Page, groupName: string) {
  const group = page.locator('.config-sidebar details').filter({
    has: page.locator('summary', { hasText: groupName })
  })

  if (await group.evaluate((element) => !(element as HTMLDetailsElement).open)) {
    await group.locator('summary').click()
  }
}

async function assertVueBackendPage(page: Page, title?: string) {
  await page.waitForURL(/\/cat\/mvc\/vue\/s\/(config|permission)/)
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2').waitFor({ state: 'visible' })
  await expect(page.locator('body')).not.toContainText('加载失败')
  await expect(page.locator('body')).not.toContainText('接口未返回 JSON')
  if (title) {
    await expect(page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2')).toContainText(title)
  }
}

function assertNotLegacyPage(page: Page) {
  const url = new URL(page.url())

  expect(url.pathname, `${url.href} 不应跳到旧后台路径`).not.toMatch(/^\/cat\/s\//)
  expect(url.pathname, `${url.href} 应停留在 Vue 后台页面`).toMatch(/^\/cat\/mvc\/vue\/s\/(config|permission)$/)
}
