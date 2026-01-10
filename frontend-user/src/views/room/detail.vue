<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <button @click="$router.back()" class="flex items-center text-gray-600 hover:text-primary-600 mb-6">
      <span>← 返回</span>
    </button>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else-if="room" class="card">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- 图片区 - 轮播图 -->
        <div class="relative">
          <div v-if="getRoomImages(room.images).length > 0" class="relative">
            <!-- 主图 -->
            <div class="h-64 md:h-96 bg-gray-200 rounded-lg overflow-hidden relative">
              <!-- 添加过渡效果 -->
              <transition name="fade" mode="out-in">
                <img 
                  :key="currentImageIndex"
                  :src="getRoomImages(room.images)[currentImageIndex]" 
                  :alt="room.typeName"
                  class="w-full h-full object-cover"
                />
              </transition>
              
              <!-- 预加载下一张和上一张图片 -->
              <img 
                v-for="(img, idx) in getRoomImages(room.images)" 
                :key="`preload-${idx}`"
                v-show="false"
                :src="img" 
                alt="预加载"
              />
            </div>
            
            <!-- 切换按钮 -->
            <button 
              v-if="getRoomImages(room.images).length > 1"
              @click.stop="prevImage"
              class="absolute left-2 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white p-3 rounded-full hover:bg-opacity-70 hover:scale-110 transition-all z-10"
            >
              ←
            </button>
            <button 
              v-if="getRoomImages(room.images).length > 1"
              @click.stop="nextImage"
              class="absolute right-2 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white p-3 rounded-full hover:bg-opacity-70 hover:scale-110 transition-all z-10"
            >
              →
            </button>
            
            <!-- 指示器 -->
            <div v-if="getRoomImages(room.images).length > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2 z-10">
              <button 
                v-for="(_, idx) in getRoomImages(room.images)" 
                :key="idx"
                @click.stop="currentImageIndex = Number(idx)"
                class="w-2 h-2 rounded-full transition-all duration-300"
                :class="Number(idx) === currentImageIndex ? 'bg-white w-6' : 'bg-white bg-opacity-50 hover:bg-opacity-80'"
              />
            </div>
            
            <!-- 缩略图 -->
            <div v-if="getRoomImages(room.images).length > 1" class="mt-4 grid grid-cols-5 gap-2">
              <button
                v-for="(img, idx) in getRoomImages(room.images).slice(0, 5)"
                :key="idx"
                @click.stop="currentImageIndex = Number(idx)"
                class="aspect-square rounded overflow-hidden border-2 transition-all duration-200"
                :class="Number(idx) === currentImageIndex ? 'border-primary-600 scale-105' : 'border-transparent hover:border-gray-300'"
              >
                <img :src="img" :alt="`图片${Number(idx) + 1}`" class="w-full h-full object-cover" loading="lazy" />
              </button>
            </div>
          </div>
          <div v-else class="h-64 md:h-96 bg-gray-200 rounded-lg flex items-center justify-center text-8xl">
            {{ getRoomIcon(room.typeName) }}
          </div>
        </div>

        <!-- 信息区 -->
        <div>
          <div class="mb-4">
            <h1 class="text-3xl font-bold text-gray-900">{{ room.typeNameDisplay || room.typeName }}</h1>
          </div>

          <p v-if="room.hotelName" class="text-gray-500 mb-1">{{ room.hotelName }}</p>
          <p v-if="room.hotelAddress" class="text-gray-500 mb-4">{{ room.hotelAddress }}</p>

          <p class="text-gray-500 mb-4">房间号: {{ room.roomNo }}</p>

          <div class="text-3xl font-bold text-primary-600 mb-6">
            ¥{{ room.pricePerNight }}
            <span class="text-lg text-gray-500">/天</span>
          </div>

          <!-- 日期选择 -->
          <div class="mb-6 p-4 bg-gray-50 rounded-lg">
            <h3 class="font-semibold mb-3">选择入住日期</h3>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-600 mb-1">入住日期</label>
                <input 
                  type="date" 
                  v-model="checkInDate" 
                  :min="minDate"
                  @change="handleCheckInDateChange"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-600 mb-1">退房日期</label>
                <input 
                  type="date" 
                  v-model="checkOutDate" 
                  :min="minCheckOutDate"
                  @change="checkAvailability"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
            </div>
            <!-- 可用性提示 -->
            <div v-if="availabilityChecked" class="mt-3 flex items-center gap-2">
              <span 
                v-if="isAvailable"
                class="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium"
              >
                ✓ 该时间段可预订
              </span>
              <span 
                v-else
                class="px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm font-medium"
              >
                ✗ 该时间段不可预订
              </span>
            </div>
          </div>

          <div class="space-y-4 mb-8">
            <div class="flex items-center text-gray-600">
              <span class="w-24">容量:</span>
              <span>{{ room.maxPetNum }}只宠物</span>
            </div>
            <div v-if="getRoomFeatures(room.features).length > 0" class="flex items-start text-gray-600">
              <span class="w-24 flex-shrink-0">设施:</span>
              <div class="flex flex-wrap gap-2">
                <span 
                  v-for="(feature, idx) in getRoomFeatures(room.features)" 
                  :key="idx"
                  class="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-sm"
                >
                  {{ feature }}
                </span>
              </div>
            </div>
            <div v-if="room.description" class="flex items-start text-gray-600">
              <span class="w-24 flex-shrink-0">介绍:</span>
              <span>{{ room.description }}</span>
            </div>
          </div>

          <button 
            v-if="isAvailable && availabilityChecked"
            @click="handleBooking"
            class="btn-primary w-full py-3 text-lg"
          >
            立即预订
          </button>
          <button 
            v-else-if="availabilityChecked && !isAvailable"
            disabled
            class="w-full py-3 text-lg bg-gray-200 text-gray-500 rounded-lg cursor-not-allowed"
          >
            该时间段不可预订
          </button>
          <button 
            v-else
            disabled
            class="w-full py-3 text-lg bg-gray-200 text-gray-500 rounded-lg cursor-not-allowed"
          >
            请选择入住日期
          </button>
        </div>
      </div>

      <!-- 详细描述 -->
      <div v-if="room.description" class="mt-8 pt-8 border-t">
        <h2 class="text-xl font-semibold mb-4">房间介绍</h2>
        <div class="prose text-gray-600">
          <p>{{ room.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

interface Room {
  id: number
  roomNo: string
  typeName: string
  typeNameDisplay?: string
  pricePerNight: number
  maxPetNum: number
  status: number
  description: string
  features?: string
  images?: string
  hotelId?: number
  hotelName?: string
  hotelAddress?: string
}

const route = useRoute()
const router = useRouter()
const room = ref<Room | null>(null)
const loading = ref(true)
const currentImageIndex = ref(0)

// 日期相关
const checkInDate = ref('')
const checkOutDate = ref('')
const minDate = ref('')
const minCheckOutDate = ref('')
const isAvailable = ref(false)
const availabilityChecked = ref(false)

const formatLocalDate = (d: Date) => {
  const y = d.getFullYear()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 初始化默认日期（入住默认今天，退房默认+3天）
const initDefaultDates = () => {
  const today = new Date()
  const defaultCheckIn = new Date(today)
  
  const defaultCheckOut = new Date(defaultCheckIn)
  defaultCheckOut.setDate(defaultCheckIn.getDate() + 3)
  
  minDate.value = formatLocalDate(today)
  checkInDate.value = formatLocalDate(defaultCheckIn)
  checkOutDate.value = formatLocalDate(defaultCheckOut)
  minCheckOutDate.value = formatLocalDate(defaultCheckOut)
}

// 入住日期变化时自动调整退房日期
const handleCheckInDateChange = () => {
  if (checkInDate.value) {
    // 计算最小退房日期（入住日期+1天）
    const checkIn = new Date(checkInDate.value)
    const minCheckOut = new Date(checkIn)
    minCheckOut.setDate(checkIn.getDate() + 1)
    minCheckOutDate.value = formatLocalDate(minCheckOut)
    
    // 自动设置退房日期为入住日期+3天
    const defaultCheckOut = new Date(checkIn)
    defaultCheckOut.setDate(checkIn.getDate() + 3)
    checkOutDate.value = formatLocalDate(defaultCheckOut)
    
    // 检查可用性
    checkAvailability()
  }
}

// 检查房间可用性
const checkAvailability = async () => {
  if (!checkInDate.value || !checkOutDate.value || !room.value) {
    availabilityChecked.value = false
    return
  }
  
  try {
    const res = await request.get('/room/check-availability', {
      params: {
        roomId: room.value.id,
        checkInDate: checkInDate.value,
        checkOutDate: checkOutDate.value
      }
    })
    isAvailable.value = res.data
    availabilityChecked.value = true
  } catch (error) {
    console.error('检查可用性失败:', error)
    isAvailable.value = false
    availabilityChecked.value = true
  }
}

// 预订跳转
const handleBooking = () => {
  if (room.value) {
    router.push({
      path: `/booking/${room.value.id}`,
      query: {
        checkInDate: checkInDate.value,
        checkOutDate: checkOutDate.value
      }
    })
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

// 上一张图片
const prevImage = () => {
  if (!room.value) return
  const images = getRoomImages(room.value.images)
  if (images.length > 1) {
    currentImageIndex.value = (currentImageIndex.value - 1 + images.length) % images.length
    console.log('切换到上一张:', currentImageIndex.value)
  }
}

// 下一张图片
const nextImage = () => {
  if (!room.value) return
  const images = getRoomImages(room.value.images)
  if (images.length > 1) {
    currentImageIndex.value = (currentImageIndex.value + 1) % images.length
    console.log('切换到下一张:', currentImageIndex.value)
  }
}

const fetchRoom = async () => {
  loading.value = true
  try {
    const roomRes = await request.get(`/room/${route.params.id}`)
    room.value = roomRes.data
    console.log('房间数据:', room.value)
    console.log('图片列表:', getRoomImages(room.value?.images))
    
    // 加载完房间后，检查默认日期的可用性
    await checkAvailability()
  } catch (error) {
    console.error('获取房间详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initDefaultDates()
  fetchRoom()
})
</script>
<style scoped>
/* 图片切换淡入淡出动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
}

.fade-leave-to {
  opacity: 0;
}

.fade-enter-to {
  opacity: 1;
}
</style>