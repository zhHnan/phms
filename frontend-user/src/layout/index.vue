<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo -->
          <router-link to="/" class="flex items-center space-x-2">
            <span class="text-2xl">🐾</span>
            <span class="text-xl font-bold text-gray-900">宠物酒店</span>
          </router-link>

          <!-- 导航链接 -->
          <nav class="hidden md:flex items-center space-x-8">
            <router-link 
              to="/" 
              class="text-gray-600 hover:text-primary-600 transition-colors"
              :class="{ 'text-primary-600 font-medium': $route.path === '/' }"
            >首页</router-link>
            <router-link 
              to="/rooms" 
              class="text-gray-600 hover:text-primary-600 transition-colors"
              :class="{ 'text-primary-600 font-medium': $route.path.startsWith('/rooms') }"
            >房间</router-link>
            <template v-if="userStore.isLoggedIn">
              <router-link 
                to="/pets" 
                class="text-gray-600 hover:text-primary-600 transition-colors"
                :class="{ 'text-primary-600 font-medium': $route.path === '/pets' }"
              >我的宠物</router-link>
              <router-link 
                to="/orders" 
                class="text-gray-600 hover:text-primary-600 transition-colors"
                :class="{ 'text-primary-600 font-medium': $route.path.startsWith('/orders') }"
              >我的订单</router-link>
            </template>
          </nav>

          <!-- 用户区域 -->
          <div class="flex items-center space-x-4">
            <template v-if="userStore.isLoggedIn">
              <router-link 
                to="/profile" 
                class="flex items-center space-x-2 text-gray-700 hover:text-primary-600"
              >
                <span class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                  <span class="text-primary-600 text-sm">{{ userStore.userInfo?.nickname?.charAt(0) || '用' }}</span>
                </span>
                <span class="hidden sm:inline">{{ userStore.userInfo?.nickname || '用户' }}</span>
              </router-link>
              <button @click="handleLogout" class="text-gray-500 hover:text-red-500 transition-colors">
                退出
              </button>
            </template>
            <template v-else>
              <router-link to="/login" class="btn-primary">
                登录
              </router-link>
            </template>
          </div>

          <!-- 移动端菜单按钮 -->
          <button 
            @click="mobileMenuOpen = !mobileMenuOpen" 
            class="md:hidden text-gray-600"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="!mobileMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 移动端菜单 -->
        <div v-if="mobileMenuOpen" class="md:hidden py-4 border-t">
          <nav class="flex flex-col space-y-4">
            <router-link to="/" class="text-gray-600" @click="mobileMenuOpen = false">首页</router-link>
            <router-link to="/rooms" class="text-gray-600" @click="mobileMenuOpen = false">房间</router-link>
            <template v-if="userStore.isLoggedIn">
              <router-link to="/pets" class="text-gray-600" @click="mobileMenuOpen = false">我的宠物</router-link>
              <router-link to="/orders" class="text-gray-600" @click="mobileMenuOpen = false">我的订单</router-link>
              <router-link to="/profile" class="text-gray-600" @click="mobileMenuOpen = false">个人中心</router-link>
            </template>
          </nav>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <main>
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="bg-gray-800 text-gray-300 py-12 mt-20">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 class="text-white text-lg font-semibold mb-4">关于我们</h3>
            <p class="text-sm">专业的宠物寄养服务，让您的爱宠享受五星级待遇。</p>
          </div>
          <div>
            <h3 class="text-white text-lg font-semibold mb-4">联系方式</h3>
            <p class="text-sm">电话: 400-123-4567</p>
            <p class="text-sm">邮箱: contact@pethotel.com</p>
          </div>
          <div>
            <h3 class="text-white text-lg font-semibold mb-4">服务时间</h3>
            <p class="text-sm">每日 8:00 - 22:00</p>
            <p class="text-sm">全年无休</p>
          </div>
        </div>
        <div class="mt-8 pt-8 border-t border-gray-700 text-center text-sm">
          <p>© 2026 宠物酒店管理系统 (PHMS). All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showSuccess } from '@/utils/message'

const router = useRouter()
const userStore = useUserStore()
const mobileMenuOpen = ref(false)

const handleLogout = () => {
  userStore.logout()
  // 提示退出登陆
  showSuccess('已退出登录')
  router.push('/')
}
</script>
