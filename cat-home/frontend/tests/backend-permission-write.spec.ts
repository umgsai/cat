import { expect, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'
const testResourcePath = '/codex/permission-test'
const testResourceXml = `  <resource path="${testResourcePath}" op="write" role="9"/>`
const editedTestResourceXml = `  <resource path="${testResourcePath}" op="read" role="8"/>`
const testUserId = 'codex-permission-test'
const testUserXml = `  <user id="${testUserId}" role="9"/>`
const editedTestUserXml = `  <user id="${testUserId}" role="8"/>`
const modifierKey = process.platform === 'darwin' ? 'Meta+A' : 'Control+A'

let originalResourceConfig = ''
let originalUserConfig = ''

test.describe.serial('管理后台权限配置写操作测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test.afterEach(async ({ page }) => {
    await restorePermissionConfig(page, 'resource', originalResourceConfig)
    await restorePermissionConfig(page, 'user', originalUserConfig)
    originalResourceConfig = ''
    originalUserConfig = ''
  })

  test('资源权限配置可以提交、编辑测试资源并恢复原配置', async ({ page }) => {
    await openPermissionPage(page, 'resource')

    originalResourceConfig = await currentPermissionContent(page)
    const updatedContent = insertBeforeClosingTag(removeTestResource(originalResourceConfig), 'resource-config', testResourceXml)

    await setXmlEditorContent(page, updatedContent)
    await submitPermissionForm(page, 'resource')

    await expect(page.locator('.config-state')).toContainText('操作成功')
    await expect(page.locator('.config-xml-form h2')).toContainText('资源权限配置')

    const savedContent = await queryPermissionContent(page, 'resource')

    expectResourceConfig(savedContent, 'write', '9')

    await openPermissionPage(page, 'resource')
    await setXmlEditorContent(page, insertBeforeClosingTag(removeTestResource(savedContent), 'resource-config', editedTestResourceXml))
    await submitPermissionForm(page, 'resource')

    const editedContent = await queryPermissionContent(page, 'resource')

    expectResourceConfig(editedContent, 'read', '8')

    await restorePermissionConfig(page, 'resource', originalResourceConfig)

    const restoredContent = await queryPermissionContent(page, 'resource')

    expect(restoredContent).not.toContain(testResourcePath)
    originalResourceConfig = ''
  })

  test('用户权限配置可以提交、编辑测试用户并恢复原配置', async ({ page }) => {
    await openPermissionPage(page, 'user')

    originalUserConfig = await currentPermissionContent(page)
    const updatedContent = insertBeforeClosingTag(removeTestUser(originalUserConfig), 'user-config', testUserXml)

    await setXmlEditorContent(page, updatedContent)
    await submitPermissionForm(page, 'user')

    await expect(page.locator('.config-state')).toContainText('操作成功')
    await expect(page.locator('.config-xml-form h2')).toContainText('用户权限配置')

    const savedContent = await queryPermissionContent(page, 'user')

    expectUserConfig(savedContent, '9')

    await openPermissionPage(page, 'user')
    await setXmlEditorContent(page, insertBeforeClosingTag(removeTestUser(savedContent), 'user-config', editedTestUserXml))
    await submitPermissionForm(page, 'user')

    const editedContent = await queryPermissionContent(page, 'user')

    expectUserConfig(editedContent, '8')

    await restorePermissionConfig(page, 'user', originalUserConfig)

    const restoredContent = await queryPermissionContent(page, 'user')

    expect(restoredContent).not.toContain(testUserId)
    originalUserConfig = ''
  })
})

async function login(page: Page) {
  await page.goto('/cat/mvc/vue/s/permission?op=resource')

  if (new URL(page.url()).pathname.endsWith('/cat/mvc/vue/s/login')) {
    await page.getByPlaceholder('请输入账号').fill(adminUsername)
    await page.getByPlaceholder('请输入密码').fill(adminPassword)
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForURL(/\/cat\/mvc\/vue\/s\/permission/)
  }
}

async function openPermissionPage(page: Page, action: 'resource' | 'user') {
  await page.goto(`/cat/mvc/vue/s/permission?op=${action}`)
  await page.waitForLoadState('domcontentloaded')
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await page.locator('.config-xml-form h2').waitFor({ state: 'visible' })
  await expect(page.locator('body')).not.toContainText('加载失败')
  await expect(page.locator('body')).not.toContainText('没有权限查看此页面')
}

async function currentPermissionContent(page: Page) {
  const content = await page.locator('input[name="content"]').inputValue()

  expect(content, '权限配置页面隐藏 content 字段不应为空').toBeTruthy()
  return content
}

async function setXmlEditorContent(page: Page, content: string) {
  const normalizedContent = normalizeLineEndings(content)

  await page.locator('.cm-content').click()
  await page.keyboard.press(modifierKey)
  await page.keyboard.insertText(normalizedContent)
  await expect(page.locator('input[name="content"]')).toHaveValue(normalizedContent)
}

async function submitPermissionForm(page: Page, action: 'resource' | 'user') {
  await Promise.all([
    page.waitForURL((url) => url.pathname === '/cat/mvc/vue/s/permission'
      && url.searchParams.get('op') === action
      && url.searchParams.get('opState') === 'Success'),
    page.getByRole('button', { name: '提交' }).click()
  ])
}

async function restorePermissionConfig(page: Page, action: 'resource' | 'user', content: string) {
  if (!content) {
    return
  }
  await openPermissionPage(page, action)
  await setXmlEditorContent(page, content)
  await submitPermissionForm(page, action)
}

async function queryPermissionContent(page: Page, action: 'resource' | 'user') {
  const response = await page.request.get(`/cat/mvc/s/permission?op=vueData&vueAction=${action}`, {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok(), `${action} 权限配置 vueData 应返回成功状态`).toBeTruthy()

  const data = await response.json() as { content?: string }

  return data.content || ''
}

function insertBeforeClosingTag(content: string, tagName: string, insertion: string) {
  const closingTag = `</${tagName}>`
  const cleanContent = content.trimEnd()
  const closingIndex = cleanContent.lastIndexOf(closingTag)

  expect(closingIndex, `${tagName} XML 应包含结束标签`).toBeGreaterThan(-1)
  return `${cleanContent.slice(0, closingIndex)}${insertion}\n${cleanContent.slice(closingIndex)}`
}

function removeTestResource(content: string) {
  return content.replace(new RegExp(`\\s*<resource\\s+path="${escapeRegExp(testResourcePath)}"[^>]*/>`, 'g'), '')
}

function removeTestUser(content: string) {
  return content.replace(new RegExp(`\\s*<user\\s+id="${escapeRegExp(testUserId)}"[^>]*/>`, 'g'), '')
}

function expectResourceConfig(content: string, op: string, role: string) {
  const match = content.match(new RegExp(`<resource\\s+[^>]*path="${escapeRegExp(testResourcePath)}"[^>]*/>`))

  expect(match?.[0], `${testResourcePath} 资源权限应存在`).toBeTruthy()
  expect(match?.[0]).toContain(`op="${op}"`)
  expect(match?.[0]).toContain(`role="${role}"`)
}

function expectUserConfig(content: string, role: string) {
  const match = content.match(new RegExp(`<user\\s+[^>]*id="${escapeRegExp(testUserId)}"[^>]*/>`))

  expect(match?.[0], `${testUserId} 用户权限应存在`).toBeTruthy()
  expect(match?.[0]).toContain(`role="${role}"`)
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function normalizeLineEndings(content: string) {
  return content.replace(/\r\n/g, '\n')
}
