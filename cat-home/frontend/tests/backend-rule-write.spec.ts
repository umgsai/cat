import { expect, type Locator, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'
const testDomain = process.env.CAT_TEST_DOMAIN || 'cat-codex-test'
const transactionType = 'CodexTransaction'
const eventType = 'CodexEvent'
const ruleName = 'CodexName'
const exceptionThresholdName = 'java.lang.CodexWriteException'
const exceptionExcludeName = 'java.lang.CodexExcludeException'

test.describe.serial('管理后台应用告警配置写操作测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await cleanupAlertRules(page)
  })

  test.afterEach(async ({ page }) => {
    await cleanupAlertRules(page)
  })

  test('Transaction告警可以新增、编辑、展示、删除测试规则', async ({ page }) => {
    await submitApplicationRule(page, {
      action: 'transactionRuleUpdate',
      listAction: 'transactionRule',
      monitor: 'count',
      name: ruleName,
      title: '编辑Transaction监控规则',
      type: transactionType
    })

    await assertRuleVisible(page, 'transactionRule', transactionRuleId())
    await updateApplicationRuleThreshold(page, 'transactionRuleUpdate', transactionRuleId(), '2')
    await expectRuleThreshold(page, 'transactionRuleUpdate', transactionRuleId(), 'transactionRuleConfigs', '2')
    await cleanupTransactionRules(page)
    await assertRuleHidden(page, 'transactionRule', transactionRuleId())
  })

  test('Event告警可以新增、编辑、展示、删除测试规则', async ({ page }) => {
    await submitApplicationRule(page, {
      action: 'eventRuleUpdate',
      listAction: 'eventRule',
      monitor: 'count',
      name: ruleName,
      title: '编辑Event监控规则',
      type: eventType
    })

    await assertRuleVisible(page, 'eventRule', eventRuleId())
    await updateApplicationRuleThreshold(page, 'eventRuleUpdate', eventRuleId(), '2')
    await expectRuleThreshold(page, 'eventRuleUpdate', eventRuleId(), 'eventRuleConfigs', '2')
    await cleanupEventRules(page)
    await assertRuleHidden(page, 'eventRule', eventRuleId())
  })

  test('异常告警配置可以新增、编辑、展示、删除异常阈值', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=exceptionThresholdAdd&type=threshold&domain=cat')
    await expect(page.locator('.config-title-cell h2')).toContainText('修改异常阈值配置信息')

    await page.locator('input[name="exceptionLimit.domain"]').fill(testDomain)
    await page.locator('input[name="exceptionLimit.name"]').fill(exceptionThresholdName)
    await page.locator('input[name="exceptionLimit.warning"]').fill('1')
    await page.locator('input[name="exceptionLimit.error"]').fill('2')
    await Promise.all([
      page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
        && url.searchParams.get('op') === 'exception'
        && url.searchParams.get('opState') === 'true'),
      page.getByRole('button', { name: '提交' }).click()
    ])

    await assertExceptionThresholdVisible(page)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=exceptionThresholdUpdate&type=threshold&domain=${encodeURIComponent(testDomain)}`
      + `&exception=${encodeURIComponent(exceptionThresholdName)}`)
    await page.locator('input[name="exceptionLimit.warning"]').fill('3')
    await page.locator('input[name="exceptionLimit.error"]').fill('4')
    await Promise.all([
      page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
        && url.searchParams.get('op') === 'exception'
        && url.searchParams.get('opState') === 'true'),
      page.getByRole('button', { name: '提交' }).click()
    ])
    await expectExceptionThreshold(page, 3, 4)
    await cleanupExceptionThreshold(page)
    await assertExceptionThresholdHidden(page)
  })

  test('异常告警配置可以新增、展示、删除异常过滤', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=exceptionExcludeAdd&type=exclude&domain=cat')
    await expect(page.locator('.config-title-cell h2')).toContainText('修改异常过滤配置信息')

    await page.locator('input[name="exceptionExclude.domain"]').fill(testDomain)
    await page.locator('input[name="exceptionExclude.name"]').fill(exceptionExcludeName)
    await Promise.all([
      page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
        && url.searchParams.get('op') === 'exception'
        && url.searchParams.get('type') === 'exclude'
        && url.searchParams.get('opState') === 'true'),
      page.getByRole('button', { name: '提交' }).click()
    ])

    await assertExceptionExcludeVisible(page)
    await cleanupExceptionExclude(page)
    await assertExceptionExcludeHidden(page)
  })

  test('心跳告警配置可以新增、编辑、展示、删除测试规则', async ({ page }) => {
    await openConfigPage(page, '/cat/mvc/vue/s/config?op=heartbeatRuleUpdate&domain=cat')
    await expect(page.locator('.config-title-cell h2')).toContainText('编辑心跳告警规则')

    const metric = await firstSelectOptionValue(page.locator('.transaction-base-row select').first())

    expect(metric, '心跳告警配置需要至少一个可选指标').toBeTruthy()
    await page.locator('.transaction-base-row input').first().fill(testDomain)
    await page.locator('.transaction-base-row select').first().selectOption(metric)
    await page.locator('.heartbeat-metric-row textarea').first().fill(testDomain)
    await page.locator('.heartbeat-metric-row select').first().selectOption(metric)
    await fillFirstRuleCondition(page)

    await Promise.all([
      page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
        && url.searchParams.get('op') === 'heartbeatRuleConfigList'
        && url.searchParams.get('opState') === 'true'),
      page.locator('.heartbeat-rule-editor').getByRole('button', { name: '提交' }).click()
    ])

    const ruleId = heartbeatRuleId(metric)

    await assertRuleVisible(page, 'heartbeatRuleConfigList', ruleId)
    await updateHeartbeatRuleThreshold(page, ruleId, '2')
    await expectRuleThreshold(page, 'heartbeatRuleUpdate', ruleId, 'heartbeatRuleConfigs', '2')
    await cleanupHeartbeatRules(page)
    await assertRuleHidden(page, 'heartbeatRuleConfigList', ruleId)
  })
})

interface RuleSubmitOptions {
  action: 'eventRuleUpdate' | 'transactionRuleUpdate'
  listAction: 'eventRule' | 'transactionRule'
  monitor: string
  name: string
  title: string
  type: string
}

async function login(page: Page) {
  await page.goto('/cat/mvc/vue/s/config?op=projects&domain=cat')

  if (new URL(page.url()).pathname.endsWith('/cat/mvc/vue/s/login')) {
    await page.getByPlaceholder('请输入账号').fill(adminUsername)
    await page.getByPlaceholder('请输入密码').fill(adminPassword)
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
  }
}

async function openConfigPage(page: Page, path: string) {
  await page.goto(path)
  await page.waitForLoadState('domcontentloaded')
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2').waitFor({ state: 'visible' })
  await expect(page.locator('body')).not.toContainText('加载失败')
}

async function submitApplicationRule(page: Page, options: RuleSubmitOptions) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=${options.action}&domain=cat`)
  await expect(page.locator('.config-title-cell h2')).toContainText(options.title)

  await page.locator('.transaction-base-row input').nth(0).fill(testDomain)
  await page.locator('.transaction-base-row input').nth(1).fill(options.type)
  await page.locator('.transaction-base-row input').nth(2).fill(options.name)
  await page.locator('.transaction-base-row select').first().selectOption(options.monitor)
  await fillFirstRuleCondition(page)

  await Promise.all([
    page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
      && url.searchParams.get('op') === options.listAction
      && url.searchParams.get('opState') === 'true'),
    page.locator('.transaction-rule-editor').getByRole('button', { name: '提交' }).click()
  ])
}

async function fillFirstRuleCondition(page: Page) {
  const editor = page.locator('.transaction-rule-editor')

  await editor.locator('.rule-condition-row input.small-input').first().fill('1')
  await editor.locator('.subcondition-row input.small-input').first().fill('1')
}

async function updateApplicationRuleThreshold(page: Page, action: 'eventRuleUpdate' | 'transactionRuleUpdate', ruleId: string, threshold: string) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=${action}&domain=cat&ruleId=${encodeURIComponent(ruleId)}`)
  await page.locator('.transaction-rule-editor .subcondition-row input.small-input').first().fill(threshold)
  await Promise.all([
    page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
      && url.searchParams.get('op') === (action === 'eventRuleUpdate' ? 'eventRule' : 'transactionRule')
      && url.searchParams.get('opState') === 'true'),
    page.locator('.transaction-rule-editor').getByRole('button', { name: '提交' }).click()
  ])
}

async function updateHeartbeatRuleThreshold(page: Page, ruleId: string, threshold: string) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=heartbeatRuleUpdate&domain=cat&key=${encodeURIComponent(ruleId)}`)
  await page.locator('.heartbeat-rule-editor .subcondition-row input.small-input').first().fill(threshold)
  await Promise.all([
    page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/config'
      && url.searchParams.get('op') === 'heartbeatRuleConfigList'
      && url.searchParams.get('opState') === 'true'),
    page.locator('.heartbeat-rule-editor').getByRole('button', { name: '提交' }).click()
  ])
}

async function assertRuleVisible(page: Page, action: string, ruleId: string) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=${action}&domain=cat`)
  await expectRulePresence(page, action, ruleId, true)
}

async function assertRuleHidden(page: Page, action: string, ruleId: string) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=${action}&domain=cat`)
  await expectRulePresence(page, action, ruleId, false)
}

async function assertExceptionThresholdVisible(page: Page) {
  await openConfigPage(page, '/cat/mvc/vue/s/config?op=exception&type=threshold&domain=cat')
  await expect(page.locator('.exception-table')).toContainText(testDomain)
  await expect(page.locator('.exception-table')).toContainText(exceptionThresholdName)
}

async function expectExceptionThreshold(page: Page, warning: number, error: number) {
  const data = await queryConfig(page, 'exception', { type: 'threshold' })
  const item = (data.exceptionLimits || []).find((limit) => limit.domain === testDomain
    && limit.name === exceptionThresholdName)

  expect(item, `${testDomain}:${exceptionThresholdName} 应该存在`).toBeTruthy()
  expect(item?.warning).toBe(warning)
  expect(item?.error).toBe(error)
}

async function assertExceptionThresholdHidden(page: Page) {
  await openConfigPage(page, '/cat/mvc/vue/s/config?op=exception&type=threshold&domain=cat')
  await expect(page.locator('.exception-table')).not.toContainText(exceptionThresholdName)
}

async function assertExceptionExcludeVisible(page: Page) {
  await openConfigPage(page, '/cat/mvc/vue/s/config?op=exception&type=exclude&domain=cat')
  await expect(page.locator('.exception-exclude-table')).toContainText(testDomain)
  await expect(page.locator('.exception-exclude-table')).toContainText(exceptionExcludeName)
}

async function assertExceptionExcludeHidden(page: Page) {
  await openConfigPage(page, '/cat/mvc/vue/s/config?op=exception&type=exclude&domain=cat')
  await expect(page.locator('.exception-exclude-table')).not.toContainText(exceptionExcludeName)
}

async function cleanupAlertRules(page: Page) {
  await cleanupTransactionRules(page)
  await cleanupEventRules(page)
  await cleanupHeartbeatRules(page)
  await cleanupExceptionThreshold(page)
  await cleanupExceptionExclude(page)
}

async function cleanupTransactionRules(page: Page) {
  await cleanupRuleRows(page, 'transactionRule', 'transactionRules', 'transactionRuleDelete', 'ruleId')
}

async function cleanupEventRules(page: Page) {
  await cleanupRuleRows(page, 'eventRule', 'eventRules', 'eventRuleDelete', 'ruleId')
}

async function cleanupHeartbeatRules(page: Page) {
  const data = await queryConfig(page, 'heartbeatRuleConfigList')
  const rules = (data.heartbeatRules || []).filter((rule) => rule.id.startsWith(`${testDomain};`))

  for (const rule of rules) {
    await page.goto(`/cat/mvc/s/config?op=heartbeatRulDelete&key=${encodeURIComponent(rule.id)}&vue=true`)
    await page.waitForLoadState('domcontentloaded')
  }
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

async function cleanupExceptionThreshold(page: Page) {
  await page.goto(`/cat/mvc/s/config?op=exceptionThresholdDelete&domain=${encodeURIComponent(testDomain)}`
    + `&exception=${encodeURIComponent(exceptionThresholdName)}&type=threshold&vue=true`)
  await page.waitForLoadState('domcontentloaded')
}

async function cleanupExceptionExclude(page: Page) {
  await page.goto(`/cat/mvc/s/config?op=exceptionExcludeDelete&domain=${encodeURIComponent(testDomain)}`
    + `&exception=${encodeURIComponent(exceptionExcludeName)}&type=exclude&vue=true`)
  await page.waitForLoadState('domcontentloaded')
}

async function queryConfig(page: Page, vueAction: string, extraParams: Record<string, string> = {}): Promise<ConfigData> {
  const params = new URLSearchParams()

  params.set('op', 'vueData')
  params.set('vueAction', vueAction)
  params.set('domain', 'cat')
  for (const [key, value] of Object.entries(extraParams)) {
    params.set(key, value)
  }
  const response = await page.request.get(`/cat/mvc/s/config?${params.toString()}`, {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok(), `${vueAction} vueData 应返回成功状态`).toBeTruthy()
  return await response.json() as ConfigData
}

async function expectRuleThreshold(
  page: Page,
  action: 'eventRuleUpdate' | 'heartbeatRuleUpdate' | 'transactionRuleUpdate',
  ruleId: string,
  configKey: 'eventRuleConfigs' | 'heartbeatRuleConfigs' | 'transactionRuleConfigs',
  threshold: string
) {
  const data = await queryConfig(page, action, ruleActionParams(action, ruleId))
  const configsText = data[configKey] || ''
  const configs = JSON.parse(configsText) as RuleConfigData[]
  const actualThreshold = configs[0]?.conditions?.[0]?.['sub-conditions']?.[0]?.text

  expect(actualThreshold, `${ruleId} 阈值应已更新`).toBe(threshold)
}

function ruleActionParams(action: 'eventRuleUpdate' | 'heartbeatRuleUpdate' | 'transactionRuleUpdate', ruleId: string) {
  if (action === 'heartbeatRuleUpdate') {
    return { key: ruleId }
  }
  return { ruleId }
}

async function expectRulePresence(page: Page, action: string, ruleId: string, expected: boolean) {
  const data = await queryConfig(page, action)
  const rules = action === 'eventRule'
    ? data.eventRules || []
    : action === 'heartbeatRuleConfigList'
      ? data.heartbeatRules || []
      : data.transactionRules || []
  const exists = rules.some((rule) => rule.id === ruleId)

  expect(exists, `${ruleId} ${expected ? '应该存在' : '应该已删除'}`).toBe(expected)
}

async function firstSelectOptionValue(select: Locator) {
  return await select.locator('option').first().getAttribute('value') || ''
}

function transactionRuleId() {
  return `${testDomain};${transactionType};${ruleName};count`
}

function eventRuleId() {
  return `${testDomain};${eventType};${ruleName};count`
}

function heartbeatRuleId(metric: string) {
  return `${testDomain};${metric}`
}

interface ConfigData {
  eventRuleConfigs?: string
  eventRules?: RuleRow[]
  exceptionLimits?: ExceptionLimitRow[]
  heartbeatRuleConfigs?: string
  heartbeatRules?: HeartbeatRuleRow[]
  transactionRuleConfigs?: string
  transactionRules?: RuleRow[]
}

interface ExceptionLimitRow {
  domain: string
  error: number
  name: string
  warning: number
}

interface RuleRow {
  domain: string
  id: string
}

interface HeartbeatRuleRow {
  id: string
}

interface RuleConfigData {
  conditions: Array<{
    'sub-conditions': Array<{
      text: string
      type: string
    }>
  }>
}
