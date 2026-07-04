import { expect, type Locator, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'
const testDomain = process.env.CAT_TEST_DOMAIN || 'cat-codex-test'
const invalidProjectDomain = '非法 domain'
const validationType = 'CodexValidation'
const validationName = 'CodexInvalidRule'
const modifierKey = process.platform === 'darwin' ? 'Meta+A' : 'Control+A'

interface XmlValidationCase {
  endpoint: 'business' | 'config'
  pageAction: string
  path: string
  title: string
  vueAction: string
}

interface RuleValidationCase {
  action: 'eventRuleUpdate' | 'heartbeatRuleUpdate' | 'transactionRuleUpdate'
  title: string
}

const xmlValidationPages: XmlValidationCase[] = [
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

const ruleValidationPages: RuleValidationCase[] = [
  {
    action: 'transactionRuleUpdate',
    title: 'Transaction告警'
  },
  {
    action: 'eventRuleUpdate',
    title: 'Event告警'
  },
  {
    action: 'heartbeatRuleUpdate',
    title: '心跳告警配置'
  }
]

test.describe.serial('管理后台表单校验测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await cleanupValidationProjects(page)
    await cleanupValidationRules(page)
  })

  test.afterEach(async ({ page }) => {
    await cleanupValidationRules(page)
    await cleanupValidationProjects(page)
  })

  test('项目基本信息提交空项目名时应返回失败且停留在新页面', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=projectAdd')
    await page.getByRole('button', { name: '提交' }).click()
    await page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
      && url.searchParams.get('op') === 'updateSubmit'
      && url.searchParams.get('opState') === 'false')

    await expect(page.locator('.config-card')).toBeVisible()
    expect(new URL(page.url()).pathname).toBe('/cat/mvc/vue/s/config')
    expect(new URL(page.url()).searchParams.get('opState')).toBe('false')
  })

  test('项目基本信息提交非法项目名时应由前端校验拦截', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=projectAdd')

    const domainInput = page.locator('input[name="project.domain"]')

    await domainInput.fill(invalidProjectDomain)
    await expectAlertWithoutNavigation(page, page.getByRole('button', { name: '提交' }), '项目名只能包含半角英文、数字、点和中划线。')

    const project = await queryProject(page, invalidProjectDomain)

    expect(project).toBeNull()
  })

  test('机器分组配置输入非法 IP 时应由前端校验拦截', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=domainGroupConfigUpdate')

    const startUrl = page.url()
    const pendingIpInput = page.locator('input[placeholder="Enter ip ..."]')

    await page.locator('.domain-group-editor input.wide').fill(testDomain)
    await page.getByRole('button', { name: '添加组' }).click()
    await page.locator('input[placeholder="Enter group ..."]').fill('codex-group')
    await pendingIpInput.fill('not-an-ip')
    await expectAlertWithoutNavigation(page, pendingIpInput, 'IP格式不正确：not-an-ip', () => pendingIpInput.blur())

    await expect(page.locator('.tag-chip')).toHaveCount(0)
    expect(page.url()).toBe(startUrl)
  })

  test('业务监控配置缺少 BusinessKey 时应由浏览器 required 校验拦截', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=businessCustomAdd&domain=cat')

    const startUrl = page.url()

    await page.locator('input[name="customConfig.title"]').fill('Codex Validation')
    await page.locator('input[name="customConfig.viewOrder"]').fill('1')
    await page.locator('textarea[name="customConfig.pattern"]').fill('${cat,test,COUNT}')
    await page.getByRole('button', { name: '提交' }).click()
    await page.waitForTimeout(300)

    expect(page.url()).toBe(startUrl)
    await expect(page.locator('input[name="customConfig.id"]')).toHaveJSProperty('validity.valueMissing', true)
  })

  test('异常阈值配置缺少项目名称时应由浏览器 required 校验拦截', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=exceptionThresholdAdd&type=threshold&domain=cat')

    const startUrl = page.url()

    await page.locator('input[name="exceptionLimit.name"]').fill('java.lang.CodexValidationException')
    await page.locator('input[name="exceptionLimit.warning"]').fill('1')
    await page.locator('input[name="exceptionLimit.error"]').fill('2')
    await page.getByRole('button', { name: '提交' }).click()
    await page.waitForTimeout(300)

    expect(page.url()).toBe(startUrl)
    await expect(page.locator('input[name="exceptionLimit.domain"]')).toHaveJSProperty('validity.valueMissing', true)
  })

  for (const pageCase of ruleValidationPages) {
    test(`${pageCase.title} 提交空阈值时应由前端校验拦截`, async ({ page }) => {
      await openRuleValidationPage(page, pageCase.action)
      await fillRuleBase(page, pageCase.action)
      await page.locator('.transaction-rule-editor .rule-condition-row input.small-input').first().fill('1')

      await expectAlertWithoutNavigation(page, ruleSubmitButton(page, pageCase.action), '阈值不能为空！')
    })

    test(`${pageCase.title} 提交非数字阈值时应由前端校验拦截`, async ({ page }) => {
      await openRuleValidationPage(page, pageCase.action)
      await fillRuleBase(page, pageCase.action)
      await page.locator('.transaction-rule-editor .rule-condition-row input.small-input').first().fill('1')
      await page.locator('.transaction-rule-editor .subcondition-row input.small-input').first().fill('not-a-number')

      await expectAlertWithoutNavigation(page, ruleSubmitButton(page, pageCase.action), '阈值必须是数字！')
    })
  }

  for (const pageCase of xmlValidationPages) {
    test(`${pageCase.title} 提交非法 XML 时应失败且不改写原配置`, async ({ page }) => {
      const originalContent = await queryConfigContent(page, pageCase)
      const originalRootName = xmlRootName(originalContent)
      const invalidMarker = `codex-invalid-${pageCase.vueAction}`

      await openConfigPage(page, pageCase.path)
      await setXmlEditorContent(page, `<${invalidMarker}><broken></${invalidMarker}`)
      await Promise.all([
        page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
          && url.searchParams.get('op') === pageCase.pageAction
          && isFailureState(url.searchParams.get('opState'))),
        page.getByRole('button', { name: '提交' }).click()
      ])

      await expect(page.locator('.config-state')).toContainText('操作失败')

      const afterContent = await queryConfigContent(page, pageCase)

      expect(xmlRootName(afterContent)).toBe(originalRootName)
      expect(afterContent).not.toContain(invalidMarker)
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

async function cleanupValidationRules(page: Page) {
  await cleanupRuleRows(page, 'transactionRule', 'transactionRules', 'transactionRuleDelete', 'ruleId')
  await cleanupRuleRows(page, 'eventRule', 'eventRules', 'eventRuleDelete', 'ruleId')
  await cleanupHeartbeatRules(page)
}

async function cleanupValidationProjects(page: Page) {
  await cleanupProject(page, invalidProjectDomain)
}

async function cleanupProject(page: Page, domain: string) {
  const project = await queryProject(page, domain)

  if (!project?.id) {
    return
  }
  await page.goto(`/cat/mvc/s/config?op=projectDelete&projectId=${encodeURIComponent(String(project.id))}&vue=true`)
  await page.waitForLoadState('domcontentloaded')
}

async function queryProject(page: Page, domain: string): Promise<{ id?: number; domain?: string } | null> {
  const response = await page.request.get(`/cat/mvc/s/config?op=vueData&vueAction=projects&domain=${encodeURIComponent(domain)}`, {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok(), `${domain} 项目查询应返回成功状态`).toBeTruthy()

  const data = await response.json() as { project?: { id?: number; domain?: string } | null }

  if (data.project?.domain !== domain) {
    return null
  }
  return data.project
}

async function cleanupRuleRows(
  page: Page,
  vueAction: string,
  dataKey: 'eventRules' | 'transactionRules',
  deleteAction: string,
  idParam: string
) {
  const data = await queryConfig(page, vueAction)
  const rules = (data[dataKey] || []).filter((rule) => rule.domain === testDomain)

  for (const rule of rules) {
    await page.goto(`/cat/mvc/s/config?op=${deleteAction}&${idParam}=${encodeURIComponent(rule.id)}&vue=true`)
    await page.waitForLoadState('domcontentloaded')
  }
}

async function cleanupHeartbeatRules(page: Page) {
  const data = await queryConfig(page, 'heartbeatRuleConfigList')
  const rules = (data.heartbeatRules || []).filter((rule) => rule.id.startsWith(`${testDomain};`))

  for (const rule of rules) {
    await page.goto(`/cat/mvc/s/config?op=heartbeatRulDelete&key=${encodeURIComponent(rule.id)}&vue=true`)
    await page.waitForLoadState('domcontentloaded')
  }
}

async function openRuleValidationPage(page: Page, action: RuleValidationCase['action']) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=${action}&domain=cat`)
}

async function fillRuleBase(page: Page, action: RuleValidationCase['action']) {
  if (action === 'heartbeatRuleUpdate') {
    const metric = await firstSelectOptionValue(page.locator('.transaction-base-row select').first())

    expect(metric, '心跳告警配置需要至少一个可选指标').toBeTruthy()
    await page.locator('.transaction-base-row input').first().fill(testDomain)
    await page.locator('.transaction-base-row select').first().selectOption(metric)
    await page.locator('.heartbeat-metric-row textarea').first().fill(testDomain)
    await page.locator('.heartbeat-metric-row select').first().selectOption(metric)
    return
  }
  await page.locator('.transaction-base-row input').nth(0).fill(testDomain)
  await page.locator('.transaction-base-row input').nth(1).fill(validationType)
  await page.locator('.transaction-base-row input').nth(2).fill(validationName)
  await page.locator('.transaction-base-row select').first().selectOption('count')
}

function ruleSubmitButton(page: Page, action: RuleValidationCase['action']) {
  const editorSelector = action === 'heartbeatRuleUpdate' ? '.heartbeat-rule-editor' : '.transaction-rule-editor'

  return page.locator(editorSelector).getByRole('button', { name: '提交' })
}

async function expectAlertWithoutNavigation(page: Page, trigger: Locator, message: string, action?: () => Promise<void>) {
  const startUrl = page.url()
  const dialogPromise = page.waitForEvent('dialog').then(async (dialog) => {
    const actualMessage = dialog.message()

    await dialog.accept()
    return actualMessage
  })

  if (action) {
    await action()
  } else {
    await trigger.click()
  }

  const actualMessage = await dialogPromise

  expect(actualMessage).toBe(message)
  await page.waitForTimeout(300)
  expect(page.url()).toBe(startUrl)
}

async function openConfigPage(page: Page, path: string) {
  await page.goto(path)
  await page.waitForLoadState('domcontentloaded')
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2').waitFor({ state: 'visible' })
  await expect(page.locator('body')).not.toContainText('加载失败')
}

async function queryConfigContent(page: Page, pageCase: XmlValidationCase) {
  const response = await page.request.get(dataUrl(pageCase), {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok(), `${pageCase.title} vueData 应返回成功状态`).toBeTruthy()

  const data = await response.json() as { content?: string }

  return data.content || ''
}

async function queryConfig(page: Page, vueAction: string): Promise<ConfigData> {
  const response = await page.request.get(`/cat/mvc/s/config?op=vueData&vueAction=${encodeURIComponent(vueAction)}&domain=cat`, {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok(), `${vueAction} vueData 应返回成功状态`).toBeTruthy()
  return await response.json() as ConfigData
}

function dataUrl(pageCase: XmlValidationCase) {
  const params = new URLSearchParams()

  params.set('op', 'vueData')
  params.set('vueAction', pageCase.vueAction)
  params.set('domain', 'cat')
  return `/cat/mvc/s/${pageCase.endpoint}?${params.toString()}`
}

async function setXmlEditorContent(page: Page, content: string) {
  await page.locator('.cm-content').click()
  await page.keyboard.press(modifierKey)
  await page.keyboard.insertText(content)
  await expect(page.locator('input[name="content"]')).toHaveValue(content)
}

function xmlRootName(content: string) {
  const match = content.match(/<([A-Za-z][\w:.-]*)\b/)

  return match?.[1] || ''
}

function isFailureState(opState: string | null) {
  return opState === 'false' || opState === 'Fail' || opState === 'Failure'
}

async function firstSelectOptionValue(select: Locator) {
  return await select.locator('option').first().getAttribute('value') || ''
}

interface ConfigData {
  eventRules?: RuleRow[]
  heartbeatRules?: HeartbeatRuleRow[]
  transactionRules?: RuleRow[]
}

interface RuleRow {
  domain: string
  id: string
}

interface HeartbeatRuleRow {
  id: string
}
