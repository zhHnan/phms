<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <button @click="$router.back()" class="flex items-center text-gray-600 hover:text-primary-600 mb-6">
      <span>← 返回订单列表</span>
    </button>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else-if="order" class="space-y-6">
      <!-- 订单状态 -->
      <div class="card">
        <div class="flex justify-between items-center">
          <div>
            <p class="text-sm text-gray-500">订单号: {{ order.orderNo }}</p>
            <h2 class="text-2xl font-bold mt-1">{{ getStatusName(order.status) }}</h2>
          </div>
          <span 
            class="px-4 py-2 rounded-full text-lg"
            :class="getStatusClass(order.status)"
          >
            {{ getStatusEmoji(order.status) }}
          </span>
        </div>
      </div>

      <!-- 房间信息 -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">房间信息</h3>
        <div class="flex items-center space-x-4">
          <div class="w-20 h-20 bg-gray-100 rounded-lg flex items-center justify-center text-4xl">
            {{ getRoomIcon(order.roomType) }}
          </div>
          <div>
            <p class="font-semibold text-lg">{{ getRoomTypeName(order.roomType) }}</p>
            <p class="text-gray-500">房间号: {{ order.roomNo }}</p>
          </div>
        </div>
      </div>

      <!-- 入住信息 -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">入住信息</h3>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-gray-500 text-sm">宠物</p>
            <p class="font-medium">{{ order.petName }}</p>
          </div>
          <div>
            <p class="text-gray-500 text-sm">入住日期</p>
            <p class="font-medium">{{ order.checkInDate }}</p>
          </div>
          <div>
            <p class="text-gray-500 text-sm">退房日期</p>
            <p class="font-medium">{{ order.checkOutDate }}</p>
          </div>
          <div>
            <p class="text-gray-500 text-sm">入住天数</p>
            <p class="font-medium">{{ order.days }}天</p>
          </div>
        </div>
        <div v-if="order.remark" class="mt-4 p-3 bg-gray-50 rounded-lg">
          <p class="text-sm text-gray-600">备注: {{ order.remark }}</p>
        </div>
      </div>

      <!-- 费用信息 -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">费用信息</h3>
        <div class="space-y-2">
          <div class="flex justify-between">
            <span class="text-gray-500">房间单价</span>
            <span>¥{{ order.roomPrice }}/天</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">入住天数</span>
            <span>{{ order.days }}天</span>
          </div>
          <div class="flex justify-between pt-2 border-t font-bold text-lg">
            <span>订单总价</span>
            <span class="text-primary-600">¥{{ order.totalPrice }}</span>
          </div>
        </div>
      </div>

      <!-- 照料记录 -->
      <div v-if="order.status === 2 && careLogs.length > 0" class="card">
        <h3 class="text-lg font-semibold mb-4">照料记录</h3>
        <div class="space-y-4">
          <div v-for="log in careLogs" :key="log.id" class="p-4 bg-gray-50 rounded-lg">
            <div class="flex justify-between items-start mb-2">
              <span 
                class="px-2 py-1 rounded text-sm"
                :class="getLogTypeClass(log.logType)"
              >
                {{ getLogTypeName(log.logType) }}
              </span>
              <span class="text-sm text-gray-500">{{ log.createdAt }}</span>
            </div>
            <p class="text-gray-700">{{ log.content }}</p>
            <p class="text-sm text-gray-500 mt-2">记录人: {{ log.staffName }}</p>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="order.status <= 1" class="flex justify-center">
        <button 
          @click="cancelOrder"
          class="px-8 py-3 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
        >
          取消订单
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

interface Order {
  id: number
  orderNo: string
  petName: string
  roomNo: string
  roomType: string
  roomPrice: number
  checkInDate: string
  checkOutDate: string
  days: number
  totalPrice: number
  status: number
  remark: string
  createdAt: string
}

interface CareLog {
  id: number
  logType: string
  content: string
  staffName: string
  createdAt: string
}

const route = useRoute()
const router = useRouter()

const order = ref<Order | null>(null)
const careLogs = ref<CareLog[]>([])
const loading = ref(true)

const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已入住',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'bg-yellow-100 text-yellow-800',
    1: 'bg-blue-100 text-blue-800',
    2: 'bg-green-100 text-green-800',
    3: 'bg-gray-100 text-gray-800',
    4: 'bg-red-100 text-red-800'
  }
  return map[status] || 'bg-gray-100 text-gray-800'
}

const getStatusEmoji = (status: number) => {
  const map: Record<number, string> = {
    0: '⏳',
    1: '✅',
    2: '🏠',
    3: '🎉',
    4: '❌'
  }
  return map[status] || '❓'
}

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
  if (type?.startsWith('cat')) return '🐱'
  if (type?.startsWith('dog')) return '🐕'
  return '👑'
}

const getLogTypeName = (type: string) => {
  const map: Record<string, string> = {
    feeding: '喂食',
    cleaning: '清洁',
    walking: '遛弯',
    health_check: '健康检查',
    other: '其他'
  }
  return map[type] || type
}

const getLogTypeClass = (type: string) => {
  const map: Record<string, string> = {
    feeding: 'bg-green-100 text-green-800',
    cleaning: 'bg-blue-100 text-blue-800',
    walking: 'bg-yellow-100 text-yellow-800',
    health_check: 'bg-red-100 text-red-800',
    other: 'bg-gray-100 text-gray-800'
  }
  return map[type] || 'bg-gray-100 text-gray-800'
}

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await request.get(`/order/${route.params.id}`)
    order.value = res.data

    // 如果订单在入住中，获取照料记录
    if (res.data.status === 2) {
      const logsRes = await request.get(`/care-log/order/${route.params.id}`)
      careLogs.value = logsRes.data || []
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

const cancelOrder = async () => {
  if (!confirm('确定要取消该订单吗？')) return
  
  try {
    await request.post(`/order/${route.params.id}/cancel`)
    if (order.value) {
      order.value.status = 4
    }
  } catch (error: any) {
    alert(error.message || '取消失败')
  }
}

onMounted(fetchOrder)
</script>
