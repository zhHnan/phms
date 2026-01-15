<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">选择房间</h1>

    <!-- 筛选条件 -->
    <div class="card mb-8">
      <div class="flex flex-wrap gap-4 items-end">
        <div class="w-48">
          <label class="block text-sm font-medium text-gray-700 mb-1">门店</label>
          <select v-model="filters.hotelId" class="input-field">
            <option value="">全部门店</option>
            <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">
              {{ hotel.name }}
            </option>
          </select>
        </div>
        <div class="w-48">
          <label class="block text-sm font-medium text-gray-700 mb-1">房间类型</label>
          <select v-model="filters.roomType" class="input-field">
            <option value="">全部类型</option>
            <option value="cat_standard">猫咪标间</option>
            <option value="cat_deluxe">猫咪豪华间</option>
            <option value="dog_standard">狗狗标间</option>
            <option value="dog_deluxe">狗狗豪华间</option>
            <option value="vip_suite">VIP套间</option>
          </select>
        </div>
        <div class="w-48">
          <label class="block text-sm font-medium text-gray-700 mb-1">排序方式</label>
          <select v-model="filters.orderBy" class="input-field">
            <option value="">默认排序</option>
            <option value="roomScore">房间评分</option>
            <option value="roomCount">房间评价数</option>
            <option value="hotelScore">酒店评分</option>
            <option value="hotelCount">酒店评价数</option>
          </select>
        </div>
        <div class="ml-auto">
          <button @click="handleSearch" class="px-6 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors whitespace-nowrap">
            筛选
          </button>
        </div>
      </div>
    </div>

    <!-- 房间列表 -->
    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
      <p class="mt-4 text-gray-600">加载中...</p>
    </div>

    <div v-else-if="rooms.length === 0" class="text-center py-20">
      <span class="text-6xl">🏠</span>
      <p class="mt-4 text-gray-600">暂无可用房间</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="room in rooms" 
        :key="room.id" 
        class="card hover:shadow-lg transition-shadow cursor-pointer"
        @click="$router.push(`/rooms/${room.id}`)"
      >
        <!-- 图片轮播或默认图标 -->
        <div class="h-48 bg-gray-200 rounded-lg mb-4 overflow-hidden relative">
          <div v-if="getRoomImages(room.images).length > 0" class="w-full h-full">
            <img 
              :src="getRoomImages(room.images)[0]" 
              :alt="room.typeName"
              class="w-full h-full object-cover"
            />
            <div v-if="getRoomImages(room.images).length > 1" 
                 class="absolute bottom-2 right-2 bg-black bg-opacity-60 text-white text-xs px-2 py-1 rounded">
              共{{ getRoomImages(room.images).length }}张
            </div>
          </div>
          <div v-else class="w-full h-full flex items-center justify-center text-6xl">
            {{ getRoomIcon(room.typeName) }}
          </div>
        </div>
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-xl font-semibold">{{ room.typeNameDisplay || room.typeName }}</h3>
        </div>
        <p class="text-gray-500 text-sm mb-2">房间号: {{ room.roomNo }}</p>
        <p v-if="room.hotelName" class="text-gray-500 text-sm mb-2">门店: {{ room.hotelName }}</p>

        <div class="flex flex-col gap-1 text-sm text-gray-700 mb-2">
          <div class="flex items-center gap-2">
            <span class="font-medium">房间评分:</span>
            <span class="text-yellow-500">{{ renderStars(getRoomScore(room.id).roomAvg) }}</span>
            <span class="text-gray-500">{{ getRoomScore(room.id).roomAvg.toFixed(1) }}/5</span>
            <span class="text-gray-400 text-xs">({{ getRoomScore(room.id).roomCount }} 人)</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="font-medium">酒店评分:</span>
            <span class="text-yellow-500">{{ renderStars(getRoomScore(room.id).hotelAvg) }}</span>
            <span class="text-gray-500">{{ getRoomScore(room.id).hotelAvg.toFixed(1) }}/5</span>
            <span class="text-gray-400 text-xs">({{ getRoomScore(room.id).hotelCount }} 人)</span>
          </div>
        </div>
        
        <!-- 设施标签 -->
        <div v-if="getRoomFeatures(room.features).length > 0" class="flex flex-wrap gap-1 mb-2">
          <span 
            v-for="(feature, idx) in getRoomFeatures(room.features)" 
            :key="idx"
            class="px-2 py-1 text-xs bg-blue-50 text-blue-600 rounded"
          >
            {{ feature }}
          </span>
        </div>
        
        <p class="text-gray-600 mb-4 line-clamp-2">{{ room.description || '舒适温馨的宠物房间' }}</p>
        <div class="flex justify-between items-center">
          <span class="text-2xl font-bold text-primary-600">
            ¥{{ room.pricePerNight }}
            <span class="text-sm text-gray-500">/天</span>
          </span>
          <button 
            @click.stop="$router.push(`/rooms/${room.id}`)"
            class="btn-primary"
          >
            查看详情
          </button>
        </div>
      </div>
    </div>

    <!-- 加载更多提示 -->
    <div v-if="loadingMore" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-6 w-6 border-4 border-primary-600 border-t-transparent"></div>
      <p class="mt-2 text-gray-600">加载中...</p>
    </div>

    <!-- 到底提示 -->
    <div v-if="!loading && !loadingMore && rooms.length >= total && total > 0" class="text-center py-8 text-gray-500">
      <p>已经到底了，共 {{ total }} 间房间</p>
    </div>

    <!-- 返回顶部按钮 -->
    <transition name="fade">
      <button
        v-if="showBackToTop"
        @click="scrollToTop"
        class="fixed bottom-8 right-8 bg-primary-600 text-white rounded-full p-4 shadow-lg hover:bg-primary-700 transition-all z-50"
        title="返回顶部"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
      </button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAvailableRooms, getHotelList, type Room, type Hotel } from '@/api'
import request from '@/utils/request'

const route = useRoute()
const rooms = ref<Room[]>([])
const hotels = ref<Hotel[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const hasMore = ref(true)
const showBackToTop = ref(false)
const roomScoreMap = ref<Record<number, { roomAvg: number; roomCount: number; hotelAvg: number; hotelCount: number }>>({})
const loadedHotelSummary = new Set<number>()

const filters = reactive({
  hotelId: '',
  roomType: '',
  orderBy: ''
})

// 滚动监听
const handleScroll = () => {
  // 显示/隐藏返回顶部按钮
  showBackToTop.value = window.scrollY > 300

  // 检测是否滚动到底部
  const scrollTop = window.scrollY
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  // 距离底部200px时触发加载
  if (scrollTop + windowHeight >= documentHeight - 200) {
    if (!loading.value && !loadingMore.value && hasMore.value) {
      loadMore()
    }
  }
}

// 返回顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 从路由参数获取酒店ID
onMounted(() => {
  if (route.query.hotelId) {
    filters.hotelId = route.query.hotelId as string
  }
  fetchHotels()
  fetchRooms(true)
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  // 移除滚动监听
  window.removeEventListener('scroll', handleScroll)
})

// 获取酒店列表
const fetchHotels = async () => {
  try {
    const res = await getHotelList()
    hotels.value = res.data
  } catch (error) {
    console.error('获取酒店列表失败:', error)
  }
}

// 根据房型名称获取图标
const getRoomIcon = (typeName: string) => {
  const name = typeName.toLowerCase()
  if (name.includes('猫') || name.includes('cat')) return '🐱'
  if (name.includes('狗') || name.includes('dog') || name.includes('犬')) return '🐕'
  return '🏠'
}

// 解析设施标签
const getRoomFeatures = (featuresJson: string | undefined) => {
  if (!featuresJson) return []
  try {
    return JSON.parse(featuresJson)
  } catch (e) {
    return []
  }
}

// 解析房间图片
const getRoomImages = (imagesJson: string | undefined) => {
  if (!imagesJson) return []
  try {
    return JSON.parse(imagesJson)
  } catch (e) {
    return []
  }
}

const renderStars = (score: number) => {
  if (!score || score <= 0) return '☆☆☆☆☆'
  const rounded = Math.min(5, Math.max(0, Math.round(score)))
  return '★★★★★'.slice(0, rounded) + '☆☆☆☆☆'.slice(0, 5 - rounded)
}

const getRoomScore = (roomId: number) => roomScoreMap.value[roomId] || { roomAvg: 0, roomCount: 0, hotelAvg: 0, hotelCount: 0 }

const loadReviewSummaries = async (roomList: Room[]) => {
  const hotelIds = Array.from(new Set(roomList.map(r => r.hotelId).filter(Boolean)))
  for (const hotelId of hotelIds) {
    if (loadedHotelSummary.has(Number(hotelId))) {
      continue
    }
    try {
      const res = await request.get(`/hotel-review/summary/${hotelId}`)
      const data = res.data || {}
      const hotelAvg = Number(data.hotelAvgScore || 0)
      const hotelCount = Number(data.hotelReviewCount || 0)
      if (Array.isArray(data.rooms)) {
        data.rooms.forEach((stat: any) => {
          const roomId = Number(stat.roomId)
          roomScoreMap.value[roomId] = {
            roomAvg: Number(stat.avgScore || 0),
            roomCount: Number(stat.reviewCount || 0),
            hotelAvg,
            hotelCount
          }
        })
      }
      loadedHotelSummary.add(Number(hotelId))
    } catch (error) {
      console.error('获取酒店评分汇总失败:', error)
    }
  }
}

const fetchRooms = async (reset = false) => {
  if (reset) {
    loading.value = true
    page.value = 1
    rooms.value = []
    roomScoreMap.value = {}
    loadedHotelSummary.clear()
    hasMore.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params: any = {
      pageNum: page.value,
      pageSize: pageSize.value
    }
    
    if (filters.hotelId) {
      params.hotelId = Number(filters.hotelId)
    }
    
    if (filters.roomType) {
      params.roomType = filters.roomType
    }

    if (filters.orderBy) {
      params.orderBy = filters.orderBy
      params.orderDirection = 'desc'
    }

    const res = await getAvailableRooms(params)
    
    if (reset) {
      rooms.value = res.data.records
    } else {
      rooms.value = [...rooms.value, ...res.data.records]
    }
    
    total.value = res.data.total
    hasMore.value = rooms.value.length < total.value
    await loadReviewSummaries(res.data.records)
  } catch (error) {
    console.error('获取房间失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (hasMore.value) {
    page.value++
    fetchRooms(false)
  }
}

const handleSearch = () => {
  fetchRooms(true)
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
