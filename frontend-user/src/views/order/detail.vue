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
          <div class="col-span-2">
            <p class="text-gray-500 text-sm mb-2">入住宠物</p>
            <div v-if="getPetInfo(order).length > 0" class="flex flex-wrap gap-2">
              <span 
                v-for="pet in getPetInfo(order)" 
                :key="pet.id || pet.name"
                class="px-3 py-1 bg-primary-50 text-primary-700 rounded-full text-sm font-medium"
              >
                {{ getPetIcon(pet.type) }} {{ pet.name }}
              </span>
            </div>
            <p v-else class="font-medium text-gray-400">未知宠物</p>
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
            <span class="text-primary-600">¥{{ order.totalAmount || order.totalPrice }}</span>
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
      <div v-if="order.status === 0" class="flex justify-center space-x-4">
        <button 
          @click="payOrder"
          :disabled="paying"
          class="px-8 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center min-w-[120px]"
        >
          <span v-if="paying" class="inline-block animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent mr-2"></span>
          {{ paying ? '付款中...' : '立即付款' }}
        </button>
        <button 
          @click="cancelOrder"
          :disabled="paying"
          class="px-8 py-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors disabled:opacity-50"
        >
          取消订单
        </button>
      </div>
      <div v-else-if="order.status === 1" class="flex justify-center">
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
import { showError, showConfirm, showSuccess, showInfo } from '@/utils/message'

interface Pet {
  id: number
  name: string
  type: number
}

interface Order {
  id: number
  orderNo: string
  petIds?: string | number[]  // JSON字符串或数组
  petName?: string  // 兼容旧数据
  pets?: Pet[]  // 宠物列表
  roomNo: string
  roomType: string
  roomPrice: number
  checkInDate: string
  checkOutDate: string
  days: number
  totalAmount?: number  // 后端返回的字段
  totalPrice?: number   // 兼容字段
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
const paying = ref(false)

const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '待入住',
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

const getPetIcon = (type: number) => {
  const map: Record<number, string> = {
    1: '🐱',
    2: '🐕',
    3: '🐰'
  }
  return map[type] || '🐾'
}

const getPetInfo = (order: Order): Pet[] => {
  // 如果有 pets 数组，直接返回
  if (order.pets && order.pets.length > 0) {
    return order.pets
  }
  
  // 兼容旧的 petName 字段
  if (order.petName) {
    return [{ id: 0, name: order.petName, type: 0 }]
  }
  
  // 解析 petIds，显示 ID 列表
  if (order.petIds) {
    try {
      const ids = typeof order.petIds === 'string' ? JSON.parse(order.petIds) : order.petIds
      return ids.map((id: number) => ({ 
        id, 
        name: `ID: ${id}`, 
        type: 0 
      }))
    } catch {
      return []
    }
  }
  
  return []
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
  if (!await showConfirm('确定要取消该订单吗？')) return
  
  try {
    await request.post(`/order/${route.params.id}/cancel`)
    if (order.value) {
      order.value.status = 4
    }
  } catch (error: any) {
    showError(error.message || '取消失败')
  }
}

const payOrder = async () => {
  if (!order.value) return
  
  paying.value = true
  try {
    // 模拟支付延迟 2 秒
    showInfo('正在处理支付...')
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 调用支付接口
    await request.post(`/order/${route.params.id}/pay`)
    
    // 更新订单状态
    order.value.status = 1
    showSuccess('支付成功！请在入住当天12:00后办理入住手续')
  } catch (error: any) {
    showError(error.message || '支付失败')
  } finally {
    paying.value = false
  }
}

onMounted(fetchOrder)
</script>
