<template>
  <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">个人中心</h1>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else class="space-y-6">
      <!-- 用户信息 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">基本信息</h2>
        <div class="flex items-center space-x-6">
          <div class="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center">
            <span class="text-3xl text-primary-600">{{ userStore.userInfo?.nickname?.charAt(0) || '用' }}</span>
          </div>
          <div>
            <h3 class="text-xl font-semibold">{{ userStore.userInfo?.nickname || '用户' }}</h3>
            <p class="text-gray-500">{{ userStore.userInfo?.phone }}</p>
          </div>
        </div>
      </div>

      <!-- 编辑信息 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">修改昵称</h2>
        <form @submit.prevent="handleUpdateNickname" class="flex space-x-4">
          <input
            v-model="nickname"
            class="input-field flex-1"
            placeholder="请输入新昵称"
          />
          <button type="submit" :disabled="updating" class="btn-primary">
            {{ updating ? '保存中...' : '保存' }}
          </button>
        </form>
      </div>

      <!-- 快捷入口 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">快捷入口</h2>
        <div class="grid grid-cols-2 gap-4">
          <router-link to="/pets" class="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors text-center">
            <span class="text-2xl">🐾</span>
            <p class="mt-2 font-medium">我的宠物</p>
          </router-link>
          <router-link to="/orders" class="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors text-center">
            <span class="text-2xl">📋</span>
            <p class="mt-2 font-medium">我的订单</p>
          </router-link>
        </div>
      </div>

      <!-- 退出登录 -->
      <button 
        @click="handleLogout"
        class="w-full py-3 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors font-medium"
      >
        退出登录
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/message'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const nickname = ref('')

const handleUpdateNickname = async () => {
  if (!nickname.value.trim()) {
    showWarning('请输入昵称')
    return
  }
  
  updating.value = true
  try {
    await request.put('/auth/user/profile', { nickname: nickname.value })
    if (userStore.userInfo) {
      userStore.userInfo.nickname = nickname.value
    }
    showSuccess('修改成功')
    nickname.value = ''
  } catch (error: any) {
    showError(error.message || '修改失败')
  } finally {
    updating.value = false
  }
}

const handleLogout = async () => {
  if (await showConfirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

onMounted(() => {
  nickname.value = userStore.userInfo?.nickname || ''
})
</script>
