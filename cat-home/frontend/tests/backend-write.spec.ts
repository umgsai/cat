import { expect, type Page, test } from '@playwright/test'

const adminUsername = process.env.CAT_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.CAT_ADMIN_PASSWORD || 'admin'
const testDomain = process.env.CAT_TEST_DOMAIN || 'cat-codex-test'
const permissionProbeDomain = `${testDomain}-permission-probe`
const testBusinessKey = process.env.CAT_TEST_BUSINESS_KEY || 'codex-test-custom'

test.describe.serial('管理后台写操作测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test.afterEach(async ({ page }) => {
    await cleanupTestBusiness(page)
    await cleanupTestDomainGroup(page)
    await cleanupTestProject(page)
  })

  test('项目基本信息可以新增、展示、删除测试项目', async ({ page }) => {
    await requireWritePermission(page)
    await cleanupTestBusiness(page)
    await cleanupTestDomainGroup(page)
    await cleanupTestProject(page)

    await openConfigPage(page, '/cat/mvc/vue/s/config?op=projectAdd')

    await page.locator('input[name="project.domain"]').fill(testDomain)
    await page.locator('input[name="project.bu"]').fill('codex-bu')
    await page.locator('input[name="project.cmdbProductline"]').fill('codex-product-line')
    await page.locator('input[name="project.owner"]').fill('codex-owner')
    await page.locator('input[name="project.email"]').fill('codex@example.com')
    await page.locator('input[name="project.phone"]').fill('10086')
    await page.getByRole('button', { name: '提交' }).click()

    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
    await expect(page.locator('.config-title-cell h2')).toContainText('项目基本信息')
    await expect(page.locator('body')).toContainText(testDomain)
    await expect(page.locator('input[name="project.bu"]')).toHaveValue('codex-bu')

    await page.locator('input[name="project.owner"]').fill('codex-owner-updated')
    await page.locator('input[name="project.email"]').fill('codex-updated@example.com')
    await page.getByRole('button', { name: '更新' }).click()
    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
    await expect(page.locator('input[name="project.owner"]')).toHaveValue('codex-owner-updated')
    await expect(page.locator('input[name="project.email"]')).toHaveValue('codex-updated@example.com')

    await deleteCurrentProject(page)
    await assertProjectDeleted(page)
  })

  test('机器分组配置可以新增、展示、删除测试分组', async ({ page }) => {
    await requireWritePermission(page)
    await cleanupTestBusiness(page)
    await cleanupTestDomainGroup(page)
    await cleanupTestProject(page)

    await openConfigPage(page, '/cat/mvc/vue/s/config?op=domainGroupConfigUpdate')

    await page.locator('.domain-group-editor input.wide').fill(testDomain)
    await page.getByRole('button', { name: '添加组' }).click()
    await page.locator('input[placeholder="Enter group ..."]').fill('codex-group')
    await page.locator('input[placeholder="Enter ip ..."]').fill('127.0.0.2,127.0.0.3')
    await page.locator('input[placeholder="Enter ip ..."]').blur()
    await page.getByRole('button', { name: '提交' }).click()

    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=domainGroupConfigs&domain=${encodeURIComponent(testDomain)}`)
    await expect(page.locator('.domain-group-table')).toContainText(testDomain)
    await expect(page.locator('.domain-group-table')).toContainText('codex-group')

    await openConfigPage(page, `/cat/mvc/vue/s/config?op=domainGroupConfigUpdate&domain=${encodeURIComponent(testDomain)}`)
    await page.locator('input[placeholder="Enter ip ..."]').fill('127.0.0.4')
    await page.locator('input[placeholder="Enter ip ..."]').blur()
    await page.getByRole('button', { name: '提交' }).click()
    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=domainGroupConfigUpdate&domain=${encodeURIComponent(testDomain)}`)
    await expect(page.locator('.tag-chip')).toContainText(['127.0.0.2', '127.0.0.3', '127.0.0.4'])

    await cleanupTestDomainGroup(page)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=domainGroupConfigs&domain=${encodeURIComponent(testDomain)}`)
    await expect(page.locator('.domain-group-table')).not.toContainText(testDomain)
  })

  test('业务监控配置可以新增、展示、删除自定义监控项', async ({ page }) => {
    await requireWritePermission(page)
    await cleanupTestBusiness(page)
    await cleanupTestDomainGroup(page)
    await cleanupTestProject(page)

    await ensureTestProject(page)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=businessCustomAdd&domain=${encodeURIComponent(testDomain)}`)

    await page.locator('input[name="customConfig.id"]').fill(testBusinessKey)
    await page.locator('input[name="customConfig.title"]').fill('Codex Test Custom')
    await page.locator('input[name="customConfig.viewOrder"]').fill('999')
    await page.locator('textarea[name="customConfig.pattern"]').fill('${cat,test,COUNT}')
    await page.getByRole('button', { name: '提交' }).click()

    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=businessList&domain=${encodeURIComponent(testDomain)}`)
    await expect(page.locator('.business-config-table')).toContainText(testBusinessKey)
    await expect(page.locator('.business-config-table')).toContainText('Codex Test Custom')

    await openConfigPage(page, `/cat/mvc/vue/s/config?op=businessCustomAdd&domain=${encodeURIComponent(testDomain)}&key=${encodeURIComponent(testBusinessKey)}`)
    await page.locator('input[name="customConfig.title"]').fill('Codex Test Custom Updated')
    await page.locator('textarea[name="customConfig.pattern"]').fill('${cat,test,SUM}')
    await page.getByRole('button', { name: '提交' }).click()
    await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=businessList&domain=${encodeURIComponent(testDomain)}`)
    await expect(page.locator('.business-config-table')).toContainText('Codex Test Custom Updated')
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=businessCustomAdd&domain=${encodeURIComponent(testDomain)}&key=${encodeURIComponent(testBusinessKey)}`)
    await expect(page.locator('textarea[name="customConfig.pattern"]')).toHaveValue('${cat,test,SUM}')

    await cleanupTestBusiness(page)
    await openConfigPage(page, `/cat/mvc/vue/s/config?op=businessList&domain=${encodeURIComponent(testDomain)}`)
    await expect(page.locator('.business-config-table')).not.toContainText(testBusinessKey)
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

async function openConfigPage(page: Page, path: string) {
  await page.goto(path)
  await page.waitForLoadState('domcontentloaded')
  await page.locator('.config-card').waitFor({ state: 'visible' })
  await page.locator('.config-heading h1, .config-title-cell h2, .config-xml-form h2').waitFor({ state: 'visible' })
  await expect(page.locator('body')).not.toContainText('加载失败')
}

async function requireWritePermission(page: Page) {
  const hasPermission = await hasConfigWritePermission(page)

  expect(hasPermission, 'admin 账号应该具备管理后台写权限').toBeTruthy()
}

async function hasConfigWritePermission(page: Page) {
  await page.goto(projectSubmitPath(permissionProbeDomain))
  await page.waitForLoadState('domcontentloaded')

  if (new URL(page.url()).pathname.endsWith('/cat/mvc/vue/s/permission')) {
    return false
  }

  await cleanupProject(page, permissionProbeDomain)
  return true
}

async function ensureTestProject(page: Page) {
  await page.goto(projectSubmitPath())
  await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
  await expect(page.locator('body')).toContainText(testDomain)
}

async function deleteCurrentProject(page: Page) {
  page.once('dialog', async (dialog) => {
    await dialog.accept()
  })
  await page.locator('a.config-danger[href*="projectDelete"]').click()
  await page.waitForURL(/\/cat\/mvc\/vue\/s\/config/)
}

async function assertProjectDeleted(page: Page) {
  await openConfigPage(page, `/cat/mvc/vue/s/config?op=projects&domain=${encodeURIComponent(testDomain)}`)

  const bodyText = await page.locator('body').innerText()

  expect(bodyText).not.toContain(`CAT上项目名称\t${testDomain}`)
}

async function cleanupTestProject(page: Page) {
  await cleanupProject(page, testDomain)
}

async function cleanupProject(page: Page, domain: string) {
  const project = await queryProject(page, domain)

  if (!project?.id) {
    return
  }
  await page.goto(`/cat/mvc/s/config?op=projectDelete&projectId=${encodeURIComponent(String(project.id))}&vue=true`)
  await page.waitForLoadState('domcontentloaded')
}

async function cleanupTestDomainGroup(page: Page) {
  await page.goto(`/cat/mvc/s/config?op=domainGroupConfigDelete&domain=${encodeURIComponent(testDomain)}&vue=true`)
  await page.waitForLoadState('domcontentloaded')
}

async function cleanupTestBusiness(page: Page) {
  await page.goto(`/cat/mvc/s/business?op=customDelete&domain=${encodeURIComponent(testDomain)}&key=${encodeURIComponent(testBusinessKey)}&vue=true`)
  await page.waitForLoadState('domcontentloaded')
}

async function queryProject(page: Page, domain = testDomain): Promise<{ id?: number; domain?: string } | null> {
  const response = await page.request.get(`/cat/mvc/s/config?op=vueData&vueAction=projects&domain=${encodeURIComponent(domain)}`, {
    headers: { Accept: 'application/json' }
  })

  expect(response.ok()).toBeTruthy()

  const data = await response.json() as { project?: { id?: number; domain?: string } | null }

  if (data.project?.domain !== domain) {
    return null
  }
  return data.project
}

function projectSubmitPath(domain = testDomain) {
  const params = new URLSearchParams()

  params.set('op', 'updateSubmit')
  params.set('vue', 'true')
  params.set('project.cmdbDomain', 'default')
  params.set('project.level', '1')
  params.set('project.domain', domain)
  params.set('project.bu', 'codex-bu')
  params.set('project.cmdbProductline', 'codex-product-line')
  params.set('project.owner', 'codex-owner')
  params.set('project.email', 'codex@example.com')
  params.set('project.phone', '10086')
  return `/cat/mvc/s/config?${params.toString()}`
}
