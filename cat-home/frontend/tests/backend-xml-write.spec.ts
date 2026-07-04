import { expect, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'

interface XmlConfigCase {
  endpoint: 'business' | 'config'
  pageAction: string
  path: string
  title: string
  vueAction: string
}

const xmlConfigPages: XmlConfigCase[] = [
  {
    endpoint: 'business',
    pageAction: 'businessTagConfig',
    path: '/cat/mvc/vue/s/config?op=businessTagConfig&domain=cat',
    title: '业务标签配置',
    vueAction: 'tagConfig'
  },
  {
    endpoint: 'config',
    pageAction: 'displayPolicy',
    path: '/cat/mvc/vue/s/config?op=displayPolicy&domain=cat',
    title: '心跳报表展示',
    vueAction: 'displayPolicy'
  },
  {
    endpoint: 'config',
    pageAction: 'alertPolicy',
    path: '/cat/mvc/vue/s/config?op=alertPolicy&domain=cat',
    title: '告警策略配置',
    vueAction: 'alertPolicy'
  },
  {
    endpoint: 'config',
    pageAction: 'alertDefaultReceivers',
    path: '/cat/mvc/vue/s/config?op=alertDefaultReceivers&domain=cat',
    title: '默认告警人配置',
    vueAction: 'alertDefaultReceivers'
  },
  {
    endpoint: 'config',
    pageAction: 'alertSenderConfigUpdate',
    path: '/cat/mvc/vue/s/config?op=alertSenderConfigUpdate&domain=cat',
    title: '告警发送服务配置',
    vueAction: 'alertSenderConfigUpdate'
  },
  {
    endpoint: 'config',
    pageAction: 'serverConfigUpdate',
    path: '/cat/mvc/vue/s/config?op=serverConfigUpdate&domain=cat',
    title: '服务端配置',
    vueAction: 'serverConfigUpdate'
  },
  {
    endpoint: 'config',
    pageAction: 'sampleConfigUpdate',
    path: '/cat/mvc/vue/s/config?op=sampleConfigUpdate&domain=cat',
    title: '消息采样配置',
    vueAction: 'sampleConfigUpdate'
  },
  {
    endpoint: 'config',
    pageAction: 'routerConfigUpdate',
    path: '/cat/mvc/vue/s/config?op=routerConfigUpdate&domain=cat',
    title: '客户端路由',
    vueAction: 'routerConfigUpdate'
  }
]

test.describe.serial('管理后台 XML 配置写操作测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  for (const pageCase of xmlConfigPages) {
    test(`${pageCase.title} 可以提交当前 XML 内容并回到新页面`, async ({ page }) => {
      const failedResponses: string[] = []
      const navigatedPaths: string[] = []

      page.on('response', (response) => {
        const url = response.url()

        if (url.includes('/cat/mvc/') && response.status() >= 400) {
          failedResponses.push(`${response.status()} ${url}`)
        }
      })
      page.on('framenavigated', (frame) => {
        if (frame === page.mainFrame()) {
          navigatedPaths.push(new URL(frame.url()).pathname)
        }
      })

      await openXmlConfigPage(page, pageCase)

      const beforeData = await queryXmlConfig(page, pageCase)
      const beforeHiddenContent = await page.locator('input[name="content"]').inputValue()
      const beforeRootName = xmlRootName(beforeHiddenContent)

      expect(beforeData.content, `${pageCase.title} vueData 应返回当前 XML 内容`).toBeTruthy()
      expect(beforeHiddenContent, `${pageCase.title} 隐藏 content 字段不应为空`).toBeTruthy()
      expect(beforeRootName, `${pageCase.title} 应包含 XML 根节点`).toBeTruthy()

      const form = page.locator('form.config-xml-form')
      const submit = form.locator('button[type="submit"]').filter({ hasText: '提交' }).first()

      await Promise.all([
        page.waitForURL((url) => {
          return url.pathname === '/cat/mvc/vue/s/config'
            && url.searchParams.get('op') === pageCase.pageAction
            && url.searchParams.has('opState')
        }),
        submit.click()
      ])
      await page.locator('.config-card').waitFor({ state: 'visible' })
      await expect(page.locator('.config-xml-form h2')).toContainText(pageCase.title)
      await expect(page.locator('.config-state')).toContainText('操作成功')
      await expect(page.locator('body')).not.toContainText('加载失败')

      const currentUrl = new URL(page.url())

      expect(currentUrl.pathname).toBe('/cat/mvc/vue/s/config')
      expect(currentUrl.searchParams.get('op')).toBe(pageCase.pageAction)
      expect(currentUrl.searchParams.get('opState')).toMatch(/^(true|Success)$/)
      expect(navigatedPaths, `${pageCase.title} 提交后不应跳到旧页面`).not.toContain('/cat/s/config')
      expect(navigatedPaths, `${pageCase.title} 提交后不应跳到权限错误页`).not.toContain('/cat/mvc/vue/s/permission')
      expect(failedResponses, `${pageCase.title} 提交过程中不应出现 4xx/5xx 响应`).toEqual([])

      const afterData = await queryXmlConfig(page, pageCase)
      const afterRootName = xmlRootName(afterData.content)

      expect(afterData.content, `${pageCase.title} 提交后 XML 内容不应为空`).toBeTruthy()
      expect(afterRootName, `${pageCase.title} 提交后 XML 根节点应保持一致`).toBe(beforeRootName)
    })
  }
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

async function openXmlConfigPage(page: Page, pageCase: XmlConfigCase) {
  await page.goto(pageCase.path)
  await page.waitForLoadState('domcontentloaded')
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await expect(page.locator('.config-xml-form h2')).toContainText(pageCase.title)
  await expect(page.locator('body')).not.toContainText('加载失败')
}

async function queryXmlConfig(page: Page, pageCase: XmlConfigCase) {
  const response = await page.request.get(dataUrl(pageCase), {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok(), `${pageCase.title} vueData 应返回成功状态`).toBeTruthy()

  const data = await response.json() as { content?: string }

  return {
    content: data.content || ''
  }
}

function dataUrl(pageCase: XmlConfigCase) {
  const endpoint = pageCase.endpoint === 'business' ? 'business' : 'config'
  const params = new URLSearchParams()

  params.set('op', 'vueData')
  params.set('vueAction', pageCase.vueAction)
  params.set('domain', 'cat')
  return `/cat/mvc/s/${endpoint}?${params.toString()}`
}

function xmlRootName(content: string) {
  const match = content.match(/<([A-Za-z][\w:.-]*)\b/)

  return match?.[1] || ''
}
