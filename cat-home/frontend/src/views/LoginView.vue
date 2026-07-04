<template>
  <main class="login-page">
    <section class="brand-panel" aria-label="CAT">
      <div class="brand-mark">CAT</div>
      <div class="brand-copy">
        <p class="eyebrow">Central Application Tracking</p>
        <h1>CAT 运维监控平台</h1>
        <p class="summary">
          聚合应用、接口、存储和告警数据，帮助团队快速发现线上问题。
        </p>
      </div>

      <div class="signal-grid">
        <div class="signal-item">
          <span class="signal-value">24h</span>
          <span class="signal-label">实时观测</span>
        </div>
        <div class="signal-item">
          <span class="signal-value">API</span>
          <span class="signal-label">接口追踪</span>
        </div>
        <div class="signal-item">
          <span class="signal-value">SLA</span>
          <span class="signal-label">告警联动</span>
        </div>
      </div>
    </section>

    <section class="login-panel" aria-label="登录">
      <div class="login-card">
        <div class="login-heading">
          <h2>登录</h2>
          <p>使用 CAT 账号进入监控后台</p>
        </div>

        <div v-if="loginError" class="login-error">登录失败，请检查账号和密码。</div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @keyup.enter="submit"
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model.trim="form.username"
              autocomplete="username"
              placeholder="请输入账号"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              autocomplete="current-password"
              placeholder="请输入密码"
              show-password
              type="password"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.remember">保持登录</el-checkbox>
            <!-- <button class="text-button" type="button">忘记密码</button> -->
          </div>

          <el-button class="login-button" type="primary" :loading="loading" @click="submit">
            登录
          </el-button>
          <div class="login-home-link">
            <a :href="homeUrl">返回首页</a>
          </div>
        </el-form>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'

interface LoginForm {
  username: string
  password: string
  remember: boolean
}

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginForm>({
  username: '',
  password: '',
  remember: true
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const currentParams = computed(() => new URLSearchParams(window.location.search))
const contextPath = computed(() => {
  const path = window.location.pathname
  const mvcIndex = path.indexOf('/mvc/')

  if (mvcIndex > 0) {
    return path.substring(0, mvcIndex)
  }
  return '/cat'
})
const loginError = computed(() => Boolean(currentParams.value.get('error')))
const homeUrl = computed(() => `${contextPath.value}/mvc/vue/r`)
const returnUrl = computed(() => {
  const rtnUrl = currentParams.value.get('rtnUrl')

  if (rtnUrl) {
    return rtnUrl
  }

  const currentUrl = `${window.location.pathname}${window.location.search}`

  if (!currentUrl.includes('/s/login')) {
    return currentUrl
  }
  return `${contextPath.value}/mvc/vue/s/config?op=projects`
})

const submit = async () => {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  loading.value = true
  submitLoginForm()
}

function appendField(formElement: HTMLFormElement, name: string, value: string) {
  const input = document.createElement('input')

  input.type = 'hidden'
  input.name = name
  input.value = value
  formElement.appendChild(input)
}

function submitLoginForm() {
  const formElement = document.createElement('form')

  formElement.method = 'post'
  formElement.action = `${contextPath.value}/mvc/s/login`
  appendField(formElement, 'account', form.username)
  appendField(formElement, 'password', form.password)
  appendField(formElement, 'rtnUrl', returnUrl.value)
  document.body.appendChild(formElement)
  formElement.submit()
}
</script>
