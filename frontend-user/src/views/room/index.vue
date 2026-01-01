<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">选择房间</h1>

    <!-- 筛选条件 -->
    <div class="card mb-8">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 items-end">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">门店</label>
          <select v-model="filters.hotelId" class="input-field">
            <option value="">全部门店</option>
            <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">
              {{ hotel.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">房间类型</label>
          <select v-model="filters.roomType" class="input-field">
            <option value="">全部类型</option>
            <option value="豪华猫屋">豪华猫屋</option>
            <option value="标准狗舍">标准狗舍</option>
            <option value="豪华狗舍">豪华狗舍</option>
            <option value="猫">所有猫房</option>
            <option value="狗">所有狗房</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">入住日期</label>
          <input type="date" v-model="filters.checkInDate" class="input-field" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">退房日期</label>
          <input type="date" v-model="filters.checkOutDate" class="input-field" />
        </div>
        <div>
          <button @click="handleSearch" class="btn-primary w-full">搜索</button>
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
        <div class="h-48 bg-gray-200 rounded-lg mb-4 flex items-center justify-center text-6xl">
          {{ getRoomIcon(room.typeName) }}
        </div>
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-xl font-semibold">{{ room.typeName }}</h3>
          <span 
            class="px-2 py-1 text-xs rounded-full"
            :class="room.status === 0 ? 'bg-green-100 text-green-800' : 'bg-orange-100 text-orange-800'"
          >
            {{ room.status === 0 ? '可预订' : '已预订' }}
          </span>
        </div>
        <p class="text-gray-500 text-sm mb-2">房间号: {{ room.roomNo }}</p>
        <p v-if="room.hotelName" class="text-gray-500 text-sm mb-2">门店: {{ room.hotelName }}</p>
        
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
            v-if="room.status === 0"
            @click.stop="$router.push(`/booking/${room.id}`)"
            class="btn-primary"
          >
            立即预订
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="flex justify-center mt-8 space-x-2">
      <button 
        @click="page--" 
        :disabled="page === 1"
        class="btn-secondary disabled:opacity-50"
      >
        上一页
      </button>
      <span class="px-4 py-2">{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
      <button 
        @click="page++" 
        :disabled="page >= Math.ceil(total / pageSize)"
        class="btn-secondary disabled:opacity-50"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getAvailableRooms, getHotelList, type Room, type Hotel } from '@/api'
import { showWarning } from '@/utils/message'

const route = useRoute()
const rooms = ref<Room[]>([])
const hotels = ref<Hotel[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(9)
const total = ref(0)

// 获取默认日期（明天和后天）
const getDefaultDates = () => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const dayAfter = new Date()
  dayAfter.setDate(dayAfter.getDate() + 4) // 默认住3天
  
  return {
    checkIn: tomorrow.toISOString().split('T')[0],
    checkOut: dayAfter.toISOString().split('T')[0]
  }
}

const defaultDates = getDefaultDates()

const filters = reactive({
  hotelId: '',
  roomType: '',
  checkInDate: defaultDates.checkIn,
  checkOutDate: defaultDates.checkOut
})

// 从路由参数获取酒店ID
onMounted(() => {
  if (route.query.hotelId) {
    filters.hotelId = route.query.hotelId as string
  }
  fetchHotels()
  fetchRooms()
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

const fetchRooms = async () => {
  loading.value = true
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
    
    if (filters.checkInDate) {
      params.checkInDate = filters.checkInDate
    }
    
    if (filters.checkOutDate) {
      params.checkOutDate = filters.checkOutDate
    }

    const res = await getAvailableRooms(params)
    rooms.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取房间失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 校验日期先后顺序
  if (filters.checkInDate && filters.checkOutDate) {
    const checkIn = new Date(filters.checkInDate)
    const checkOut = new Date(filters.checkOutDate)
    if (checkOut <= checkIn) {
      showWarning('退房日期必须晚于入住日期')
      return
    }
  }
  
  page.value = 1
  fetchRooms()
}

watch(page, fetchRooms)
</script>
