<template>
  <div>
    <!-- Hero Section -->
    <section class="relative bg-gradient-to-r from-primary-600 to-primary-800 text-white py-20">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center">
          <h1 class="text-4xl md:text-5xl font-bold mb-6">
            给您的爱宠一个温馨的家
          </h1>
          <p class="text-xl text-primary-100 mb-8 max-w-2xl mx-auto">
            专业的宠物寄养服务，24小时悉心照料，让您放心出行
          </p>
          <router-link to="/rooms" class="inline-block bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors">
            立即预订
          </router-link>
        </div>
      </div>
      <div class="absolute bottom-0 left-0 right-0 h-16 bg-gradient-to-t from-gray-50 to-transparent"></div>
    </section>

    <!-- 服务特色 -->
    <section class="py-16">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl font-bold text-center text-gray-900 mb-12">为什么选择我们</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="card text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">🏠</span>
            </div>
            <h3 class="text-xl font-semibold mb-2">舒适环境</h3>
            <p class="text-gray-600">宽敞明亮的房间，恒温恒湿，给宠物最舒适的居住体验</p>
          </div>
          <div class="card text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">👨‍⚕️</span>
            </div>
            <h3 class="text-xl font-semibold mb-2">专业照护</h3>
            <p class="text-gray-600">经验丰富的护理团队，24小时关注宠物健康状况</p>
          </div>
          <div class="card text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">📱</span>
            </div>
            <h3 class="text-xl font-semibold mb-2">实时反馈</h3>
            <p class="text-gray-600">每日照料记录，随时查看爱宠动态，安心又放心</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 房型展示 -->
    <section class="py-16 bg-gray-100">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl font-bold text-center text-gray-900 mb-12">热门房型</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div v-for="room in featuredRooms" :key="room.type" class="card group cursor-pointer" @click="$router.push('/rooms')">
            <div class="h-48 bg-gray-200 rounded-lg mb-4 flex items-center justify-center text-6xl">
              {{ room.icon }}
            </div>
            <h3 class="text-xl font-semibold mb-2">{{ room.name }}</h3>
            <p class="text-gray-600 mb-4">{{ room.description }}</p>
            <div class="flex justify-between items-center">
              <span class="text-2xl font-bold text-primary-600">¥{{ room.price }}<span class="text-sm text-gray-500">/天</span></span>
              <span class="text-primary-600 group-hover:translate-x-1 transition-transform">查看详情 →</span>
            </div>
          </div>
        </div>
        <div class="text-center mt-8">
          <router-link to="/rooms" class="btn-secondary">
            查看全部房型
          </router-link>
        </div>
      </div>
    </section>

    <!-- 预订流程 -->
    <section class="py-16">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl font-bold text-center text-gray-900 mb-12">预订流程</h2>
        <div class="flex flex-col md:flex-row justify-center items-center gap-8">
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-primary-600 text-white rounded-full flex items-center justify-center text-xl font-bold mb-4">1</div>
            <h3 class="font-semibold mb-2">选择房型</h3>
            <p class="text-gray-600 text-center text-sm">根据宠物类型选择合适的房间</p>
          </div>
          <div class="hidden md:block w-24 h-1 bg-primary-200"></div>
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-primary-600 text-white rounded-full flex items-center justify-center text-xl font-bold mb-4">2</div>
            <h3 class="font-semibold mb-2">填写信息</h3>
            <p class="text-gray-600 text-center text-sm">填写入住日期和宠物信息</p>
          </div>
          <div class="hidden md:block w-24 h-1 bg-primary-200"></div>
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-primary-600 text-white rounded-full flex items-center justify-center text-xl font-bold mb-4">3</div>
            <h3 class="font-semibold mb-2">确认预订</h3>
            <p class="text-gray-600 text-center text-sm">确认订单并完成支付</p>
          </div>
          <div class="hidden md:block w-24 h-1 bg-primary-200"></div>
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-primary-600 text-white rounded-full flex items-center justify-center text-xl font-bold mb-4">4</div>
            <h3 class="font-semibold mb-2">送达入住</h3>
            <p class="text-gray-600 text-center text-sm">按时将宠物送到酒店</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const featuredRooms = ref([
  {
    type: 'cat_standard',
    name: '猫咪标间',
    icon: '🐱',
    description: '适合单只猫咪居住，配备猫爬架和玩具',
    price: 98
  },
  {
    type: 'dog_standard',
    name: '狗狗标间',
    icon: '🐕',
    description: '适合中小型犬，室内外活动空间充足',
    price: 128
  },
  {
    type: 'vip_suite',
    name: 'VIP套间',
    icon: '👑',
    description: '尊贵享受，独立活动区域，专属护理',
    price: 298
  }
])
</script>
