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
          {{ getRoomIcon(room.typeName) }}
        </div>

        <!-- 信息区 -->
        <div>
          <div class="flex items-start justify-between mb-4">
            <h1 class="text-3xl font-bold text-gray-900">{{ room.typeName }}</h1>
            <span 
              class="px-3 py-1 rounded-full text-sm"
              :class="room.status === 0 ? 'bg-green-100 text-green-800' : 'bg-orange-100 text-orange-800'"
            >
              {{ room.status === 0 ? '可预订' : '已预订' }}
            </span>
          </div>

          <p class="text-gray-500 mb-4">房间号: {{ room.roomNo }}</p>

          <div class="text-3xl font-bold text-primary-600 mb-6">
            ¥{{ room.pricePerNight }}
            <span class="text-lg text-gray-500">/天</span>
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
import { useRoute } from 'vue-router'
import request from '@/utils/request'

interface Room {
  id: number
  roomNo: string
  typeName: string
  pricePerNight: number
  maxPetNum: number
  status: number
  description: string
  features?: string
}

const route = useRoute()
const room = ref<Room | null>(null)
const loading = ref(true)

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
