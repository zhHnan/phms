<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div class="text-center">
        <span class="text-6xl">🐾</span>
        <h2 class="mt-6 text-3xl font-bold text-gray-900">
          欢迎回来
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          使用手机号登录宠物酒店
        </p>
      </div>

      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div class="space-y-4">
          <div>
            <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">手机号</label>
            <input
              id="phone"
              v-model="phone"
              type="tel"
              required
              maxlength="11"
              class="input-field"
              placeholder="请输入手机号"
            />
          </div>

          <div>
            <label for="code" class="block text-sm font-medium text-gray-700 mb-1">验证码</label>
            <div class="flex space-x-4">
              <input
                id="code"
                v-model="code"
                type="text"
                required
                maxlength="6"
                class="input-field flex-1"
                placeholder="请输入验证码"
              />
              <button
                type="button"
                @click="sendCode"
                :disabled="countdown > 0 || sendingCode"
                class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap transition-colors"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </div>
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full btn-primary py-3 text-lg disabled:opacity-50"
          >
            {{ loading ? '登录中...' : '登录 / 注册' }}
          </button>
        </div>

        <p class="text-center text-sm text-gray-500">
          未注册的手机号将自动创建账号
        </p>
      </form>

      <!-- 测试用提示 -->
      <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 class="text-sm font-medium text-blue-800 mb-2">测试账号</h4>
        <p class="text-xs text-blue-600">手机号: 13800138001</p>
        <p class="text-xs text-blue-600">验证码: 123456 (开发环境)</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showSuccess, showError, showWarning } from '@/utils/message'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const phone = ref('')
const code = ref('')
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

let timer: number | null = null

const sendCode = async () => {
  if (!phone.value || phone.value.length !== 11) {
    showWarning('请输入正确的手机号')
    return
  }

  sendingCode.value = true
  try {
    await userStore.sendCode(phone.value)
    showSuccess('验证码已发送')
    startCountdown()
  } catch (error: any) {
    showError(error.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  timer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (timer) clearInterval(timer)
    }
  }, 1000)
}

const handleLogin = async () => {
  if (!phone.value || !code.value) {
    showWarning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    await userStore.login(phone.value, code.value)
    const redirect = route.query.redirect as string || '/'
    // 提示登录成功
    showSuccess('登录成功')
    router.push(redirect)
  } catch (error: any) {
    showError(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
