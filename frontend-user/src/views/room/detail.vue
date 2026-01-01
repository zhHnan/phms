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
        <!-- 图片区 -->
        <div class="h-64 md:h-auto bg-gray-200 rounded-lg flex items-center justify-center text-8xl">
          {{ getRoomIcon(room.roomType) }}
        </div>

        <!-- 信息区 -->
        <div>
          <div class="flex items-start justify-between mb-4">
            <h1 class="text-3xl font-bold text-gray-900">{{ getRoomTypeName(room.roomType) }}</h1>
            <span 
              class="px-3 py-1 rounded-full text-sm"
              :class="room.status === 0 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
            >
              {{ room.status === 0 ? '可预订' : '已占用' }}
            </span>
          </div>

          <p class="text-gray-500 mb-4">房间号: {{ room.roomNo }}</p>

          <div class="text-3xl font-bold text-primary-600 mb-6">
            ¥{{ room.price }}
            <span class="text-lg text-gray-500">/天</span>
          </div>

          <div class="space-y-4 mb-8">
            <div class="flex items-center text-gray-600">
              <span class="w-24">容量:</span>
              <span>{{ room.capacity }}只宠物</span>
            </div>
            <div class="flex items-start text-gray-600">
              <span class="w-24 flex-shrink-0">设施:</span>
              <span>{{ room.description || '舒适的休息区、饮水器、喂食器、玩具' }}</span>
            </div>
          </div>

          <button 
            v-if="room.status === 0"
            @click="$router.push(`/booking/${room.id}`)"
            class="btn-primary w-full py-3 text-lg"
          >
            立即预订
          </button>
          <button 
            v-else
            disabled
            class="w-full py-3 text-lg bg-gray-200 text-gray-500 rounded-lg cursor-not-allowed"
          >
            暂不可预订
          </button>
        </div>
      </div>

      <!-- 详细描述 -->
      <div class="mt-8 pt-8 border-t">
        <h2 class="text-xl font-semibold mb-4">房间介绍</h2>
        <div class="prose text-gray-600">
          <p>{{ room.description || getDefaultDescription(room.roomType) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
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

const route = useRoute()
const room = ref<Room | null>(null)
const loading = ref(true)

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

const getDefaultDescription = (type: string) => {
  const map: Record<string, string> = {
    cat_standard: '专为猫咪设计的温馨房间，配备舒适的猫窝、猫爬架和各种玩具。房间保持恒温恒湿，确保猫咪舒适度过每一天。',
    cat_deluxe: '豪华猫咪套房，超大活动空间，配备高级猫爬架、猫跳台和丰富的互动玩具。独立阳光休息区，让您的猫咪享受惬意时光。',
    dog_standard: '宽敞明亮的狗狗房间，适合中小型犬居住。配备舒适狗窝、食水器具和玩具。每日安排户外活动时间。',
    dog_deluxe: '豪华狗狗套房，超大活动空间，配备高端狗窝和丰富玩具。包含专属户外活动区域，满足狗狗的运动需求。',
    vip_suite: 'VIP尊贵套房，顶级配置的宠物房间。独立空调系统、高端床具、专属活动区域。24小时专人照护，尊享贵宾服务。'
  }
  return map[type] || '舒适温馨的宠物房间，让您的爱宠宾至如归。'
}

const fetchRoom = async () => {
  loading.value = true
  try {
    const res = await request.get(`/room/${route.params.id}`)
    room.value = res.data
  } catch (error) {
    console.error('获取房间详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchRoom)
</script>
