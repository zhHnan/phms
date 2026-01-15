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
        <h2 class="text-lg font-semibold mb-4">修改信息</h2>
        <form @submit.prevent="handleUpdateProfile" class="space-y-4">
          <div>
            <div class="text-sm text-gray-600 mb-1">昵称</div>
            <input v-model="form.nickname" class="input-field" placeholder="请输入昵称" />
          </div>

          <div>
            <div class="text-sm text-gray-600 mb-1">头像</div>
            <div class="flex items-center gap-4">
              <div class="w-14 h-14 bg-primary-100 rounded-full flex items-center justify-center overflow-hidden">
                <img v-if="form.avatar" :src="form.avatar" class="w-14 h-14 object-cover" />
                <span v-else class="text-xl text-primary-600">{{ (userStore.userInfo?.nickname?.charAt(0) || '用') }}</span>
              </div>
              <div class="flex-1 space-y-2">
                <input v-model="form.avatar" class="input-field" placeholder="头像URL（可选）" />
                <div class="flex items-center gap-3">
                  <input
                    ref="fileInputRef"
                    type="file"
                    accept="image/*"
                    class="hidden"
                    @change="handleAvatarFileChange"
                  />
                  <button type="button" class="btn-secondary" :disabled="uploading" @click="triggerPickAvatar">
                    {{ uploading ? '上传中...' : '上传头像' }}
                  </button>
                  <div class="text-xs text-gray-500">支持jpg/png，单张≤10MB</div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex gap-4">
            <button type="submit" :disabled="updating" class="btn-primary">
              {{ updating ? '保存中...' : '保存' }}
            </button>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/message'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

const form = reactive({
  nickname: '',
  avatar: ''
})

const triggerPickAvatar = () => {
  fileInputRef.value?.click()
}

const handleAvatarFileChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 清理 input，允许重复选择同一文件触发 change
  input.value = ''

  if (file.size > 10 * 1024 * 1024) {
    showWarning('图片不能超过10MB')
    return
  }
  if (!file.type.startsWith('image/')) {
    showWarning('只支持图片文件')
    return
  }

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await request.post('/upload/single', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.avatar = res.data.url
    showSuccess('头像上传成功')
  } catch (error: any) {
    showError(error.message || '头像上传失败')
  } finally {
    uploading.value = false
  }
}

const handleUpdateProfile = async () => {
  const nicknameTrimmed = form.nickname.trim()
  const avatarTrimmed = form.avatar.trim()

  if (!nicknameTrimmed) {
    showWarning('请输入昵称')
    return
  }
  if (avatarTrimmed && !/^https?:\/\//i.test(avatarTrimmed)) {
    showWarning('头像URL格式不正确')
    return
  }

  updating.value = true
  try {
    await request.put('/auth/user/profile', {
      nickname: nicknameTrimmed,
      avatar: avatarTrimmed || undefined
    })

    if (userStore.userInfo) {
      userStore.userInfo.nickname = nicknameTrimmed
      userStore.userInfo.avatar = avatarTrimmed
    }
    showSuccess('修改成功')
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
  ;(async () => {
    loading.value = true
    try {
      if (!userStore.userInfo) {
        await userStore.fetchUserInfo()
      }
      form.nickname = userStore.userInfo?.nickname || ''
      form.avatar = userStore.userInfo?.avatar || ''
    } finally {
      loading.value = false
    }
  })()
})
</script>
