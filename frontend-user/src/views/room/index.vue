<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">选择房间</h1>

    <!-- 筛选条件 -->
    <div class="card mb-8">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">宠物类型</label>
          <select v-model="filters.petType" class="input-field">
            <option value="">全部</option>
            <option value="cat">猫咪</option>
            <option value="dog">狗狗</option>
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
        <div class="flex items-end">
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
          {{ getRoomIcon(room.roomType) }}
        </div>
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-xl font-semibold">{{ getRoomTypeName(room.roomType) }}</h3>
          <span 
            class="px-2 py-1 text-xs rounded-full"
            :class="room.status === 0 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
          >
            {{ room.status === 0 ? '可预订' : '已占用' }}
          </span>
        </div>
        <p class="text-gray-500 text-sm mb-2">房间号: {{ room.roomNo }}</p>
        <p class="text-gray-600 mb-4">{{ room.description || '舒适温馨的宠物房间' }}</p>
        <div class="flex justify-between items-center">
          <span class="text-2xl font-bold text-primary-600">
            ¥{{ room.price }}
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
import request from '@/utils/request'

interface Room {
  id: number
  roomNo: string
  roomType: string
  price: number
  capacity: number
  status: number
  description: string
}

const rooms = ref<Room[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(9)
const total = ref(0)

const filters = reactive({
  petType: '',
  checkInDate: '',
  checkOutDate: ''
})

const getRoomTypeName = (type: string) => {
  const map: Record<string, string> = {
    cat_standard: '猫咪标间',
    cat_deluxe: '猫咪豪华间',
    dog_standard: '狗狗标间',
    dog_deluxe: '狗狗豪华间',
    vip_suite: 'VIP套间'
  }
  return map[type] || type
}

const getRoomIcon = (type: string) => {
  if (type.startsWith('cat')) return '🐱'
  if (type.startsWith('dog')) return '🐕'
  return '👑'
}

const fetchRooms = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: page.value,
      pageSize: pageSize.value
    }
    
    if (filters.petType) {
      params.roomType = filters.petType
    }
    
    if (filters.checkInDate) {
      params.checkInDate = filters.checkInDate
    }
    
    if (filters.checkOutDate) {
      params.checkOutDate = filters.checkOutDate
    }

    const res = await request.get('/room/available', { params })
    rooms.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取房间失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchRooms()
}

watch(page, fetchRooms)

onMounted(fetchRooms)
</script>
