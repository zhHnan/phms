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

    <!-- 热门酒店 -->
    <section class="py-16 bg-gray-100">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl font-bold text-center text-gray-900 mb-12">热门酒店</h2>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-20">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
          <p class="mt-4 text-gray-600">加载中...</p>
        </div>

        <!-- 酒店列表 - 横向滚动 -->
        <div v-else-if="hotels.length > 0" class="relative">
          <div class="overflow-x-auto scrollbar-hide">
            <div class="flex gap-6 pb-4" :class="hotels.length <= 3 ? 'justify-center' : ''">
              <div 
                v-for="hotel in hotels" 
                :key="hotel.id" 
                class="card group cursor-pointer hover:shadow-lg transition-shadow flex-shrink-0"
                :style="{ width: hotels.length <= 3 ? 'calc(33.333% - 1rem)' : '350px', minWidth: '300px' }"
                @click="goToHotelRooms(hotel.id)"
              >
                <!-- 酒店图片或默认图标 -->
                <div class="h-48 rounded-lg mb-4 overflow-hidden relative">
                  <img 
                    v-if="parseImages(hotel.images).length > 0"
                    :src="parseImages(hotel.images)[0]"
                    :alt="hotel.name"
                    class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                  />
                  <div v-else class="w-full h-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-6xl">
                    🏨
                  </div>
                  <!-- 图片数量角标 -->
                  <div 
                    v-if="parseImages(hotel.images).length > 1"
                    class="absolute bottom-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded"
                  >
                    {{ parseImages(hotel.images).length }}张
                  </div>
                </div>
                <h3 class="text-xl font-semibold mb-2">{{ hotel.name }}</h3>
                <div class="space-y-2 mb-4">
                  <p class="text-gray-600 text-sm flex items-center">
                    <span class="mr-2">📍</span>
                    {{ hotel.address }}
                  </p>
                  <p class="text-gray-600 text-sm flex items-center">
                    <span class="mr-2">📞</span>
                    {{ hotel.phone }}
                  </p>
                </div>
                <div class="flex justify-between items-center">
                  <span 
                    class="px-3 py-1 text-sm rounded-full"
                    :class="hotel.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
                  >
                    {{ hotel.status === 1 ? '营业中' : '休息中' }}
                  </span>
                  <span class="text-primary-600 group-hover:translate-x-1 transition-transform">
                    查看房间 →
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 滚动提示 -->
          <div v-if="hotels.length > 3" class="text-center mt-4 text-sm text-gray-500">
            ← 左右滑动查看更多酒店 →
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="text-center py-20">
          <span class="text-6xl">🏨</span>
          <p class="mt-4 text-gray-600">暂无酒店信息</p>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotelList, type Hotel } from '@/api'

const router = useRouter()
const hotels = ref<Hotel[]>([])
const loading = ref(false)

// 解析图片JSON字符串
const parseImages = (images: string | undefined): string[] => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

// 获取酒店列表
const fetchHotels = async () => {
  loading.value = true
  try {
    const res = await getHotelList()
    // 只显示营业中的酒店
    hotels.value = res.data.filter(h => h.status === 1)
  } catch (error) {
    console.error('获取酒店列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到酒店房间列表
const goToHotelRooms = (hotelId: number) => {
  router.push({
    path: '/rooms',
    query: { hotelId: hotelId.toString() }
  })
}

onMounted(() => {
  fetchHotels()
})
</script>
