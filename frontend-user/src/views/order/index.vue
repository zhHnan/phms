<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">我的订单</h1>

    <!-- 状态筛选 -->
    <div class="flex space-x-4 mb-6 overflow-x-auto">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="currentTab = tab.value"
        class="px-4 py-2 rounded-lg whitespace-nowrap transition-colors"
        :class="currentTab === tab.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else-if="orders.length === 0" class="text-center py-20">
      <span class="text-6xl">📋</span>
      <p class="mt-4 text-gray-600">暂无订单</p>
      <router-link to="/rooms" class="btn-primary inline-block mt-4">去预订</router-link>
    </div>

    <div v-else class="space-y-4">
      <div 
        v-for="order in orders" 
        :key="order.id" 
        class="card cursor-pointer hover:shadow-lg transition-shadow"
        @click="$router.push(`/orders/${order.id}`)"
      >
        <div class="flex justify-between items-start mb-4">
          <div>
            <p class="text-sm text-gray-500">订单号: {{ order.orderNo }}</p>
            <p class="text-sm text-gray-500">{{ order.createdAt }}</p>
          </div>
          <span 
            class="px-3 py-1 rounded-full text-sm"
            :class="getStatusClass(order.status)"
          >
            {{ getStatusName(order.status) }}
          </span>
        </div>

        <div class="flex items-center space-x-4">
          <div class="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center text-3xl">
            {{ getRoomIcon(order.roomType) }}
          </div>
          <div class="flex-1">
            <h3 class="font-semibold">{{ order.petName }}</h3>
            <p class="text-gray-500 text-sm">{{ getRoomTypeName(order.roomType) }} · {{ order.roomNo }}</p>
            <p class="text-gray-500 text-sm">{{ order.checkInDate }} 至 {{ order.checkOutDate }} ({{ order.days }}天)</p>
          </div>
          <div class="text-right">
            <p class="text-xl font-bold text-primary-600">¥{{ order.totalPrice }}</p>
          </div>
        </div>

        <div v-if="order.status <= 1" class="mt-4 pt-4 border-t flex justify-end space-x-4">
          <button 
            @click.stop="cancelOrder(order)" 
            class="text-red-500 hover:text-red-600"
          >
            取消订单
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
import { ref, watch, onMounted } from 'vue'
import request from '@/utils/request'

interface Order {
  id: number
  orderNo: string
  petName: string
  roomNo: string
  roomType: string
  checkInDate: string
  checkOutDate: string
  days: number
  totalPrice: number
  status: number
  createdAt: string
}

const tabs = [
  { label: '全部', value: '' },
  { label: '待确认', value: '0' },
  { label: '已确认', value: '1' },
  { label: '已入住', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' }
]

const orders = ref<Order[]>([])
const loading = ref(true)
const currentTab = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

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

const fetchOrders = async () => {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: pageSize.value
    }
    if (currentTab.value) {
      params.status = currentTab.value
    }
    
    const res = await request.get('/order/my-orders', { params })
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

const cancelOrder = async (order: Order) => {
  if (!confirm('确定要取消该订单吗？')) return
  
  try {
    await request.post(`/order/${order.id}/cancel`)
    order.status = 4
  } catch (error: any) {
    alert(error.message || '取消失败')
  }
}

watch(currentTab, () => {
  page.value = 1
  fetchOrders()
})

watch(page, fetchOrders)

onMounted(fetchOrders)
</script>
