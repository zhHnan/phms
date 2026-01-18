<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <button @click="$router.back()" class="flex items-center text-gray-600 hover:text-primary-600 mb-6">
      <span>← 返回订单列表</span>
    </button>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>
    <div v-else class="space-y-6">
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
            <p class="font-semibold text-lg">{{ order.roomTypeDisplay || getRoomTypeName(order.roomType) }}</p>
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
          <div v-if="order.items && order.items.length" class="pt-2 border-t">
            <div class="text-gray-500 mb-2">商品明细</div>
            <div v-for="(item, idx) in order.items" :key="idx" class="flex justify-between text-sm">
              <span>{{ item.productName }} x{{ item.quantity }}</span>
              <span>¥{{ item.subtotal }}</span>
            </div>
          </div>
          <div class="flex justify-between pt-2 border-t font-bold text-lg">
            <span>订单总价</span>
            <span class="text-primary-600">¥{{ order.totalAmount || order.totalPrice }}</span>
          </div>
        </div>
      </div>

      <!-- 酒店评价（仅已完成订单） -->
      <div v-if="order.status === 3" class="card">
        <h3 class="text-lg font-semibold mb-4">酒店评价</h3>

        <div v-if="review" class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="text-gray-700">
              <span class="font-medium">评分：</span>
              <span class="text-yellow-500">
                <span v-for="n in 5" :key="n">{{ n <= review.score ? '★' : '☆' }}</span>
              </span>
              <span class="ml-2 text-sm text-gray-500">({{ review.score }}/5)</span>
            </div>
            <div class="text-sm text-gray-500">{{ formatDateTime(review.createdAt) }}</div>
          </div>
          <div v-if="review.content" class="p-3 bg-gray-50 rounded-lg text-gray-700">
            {{ review.content }}
          </div>
          <div v-else class="text-sm text-gray-500">未填写文字评价</div>
        </div>

        <div v-else class="space-y-4">
          <div>
            <p class="text-sm text-gray-600 mb-2">满意度（5分为满分）</p>
            <div class="flex items-center gap-2">
              <button
                v-for="n in 5"
                :key="n"
                type="button"
                class="text-2xl"
                :class="n <= reviewForm.score ? 'text-yellow-500' : 'text-gray-300'"
                @click="reviewForm.score = n"
                aria-label="score"
              >
                ★
              </button>
              <span class="text-sm text-gray-500 ml-2">{{ reviewForm.score }}/5</span>
            </div>
          </div>
          <div>
            <p class="text-sm text-gray-600 mb-2">评价内容（可选）</p>
            <textarea
              v-model="reviewForm.content"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200"
              rows="3"
              placeholder="说说你的入住体验吧"
            ></textarea>
          </div>
          <div>
            <button
              type="button"
              class="btn-primary"
              :disabled="submittingReview"
              @click="submitReview"
            >
              {{ submittingReview ? '提交中...' : '提交评价' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 照料记录 -->
      <div v-if="order.status === 2 && careLogs.length > 0" class="card">
        <h3 class="text-lg font-semibold mb-4">照料记录</h3>
        <div class="overflow-x-auto pb-2">
          <div class="flex gap-4 min-w-full">
            <div v-for="(log, idx) in careLogs" :key="log.id" class="relative">
              <div class="w-72 p-4 bg-gray-50 rounded-xl shadow-sm flex flex-col gap-2">
                <div class="flex items-center justify-between">
                  <span 
                    class="px-2 py-1 rounded text-sm"
                    :class="getCareTypeClass(log.careType)"
                  >
                    {{ getcareTypeName(log.careType) }}
                  </span>
                  <span class="text-xs text-gray-500">{{ formatDateTime(log.createdAt) }}</span>
                </div>
                <p class="text-gray-700 leading-relaxed">{{ log.content }}</p>
                <div v-if="getLogImages(log).length" class="grid grid-cols-2 gap-2">
                  <img
                    v-for="(img, i) in getLogImages(log)"
                    :key="i"
                    :src="img"
                    class="w-full h-24 object-cover rounded-lg border border-gray-200"
                    alt="护理图片"
                  />
                </div>
                <p class="text-xs text-gray-500">记录人: {{ log.staffName }}</p>
              </div>
              <div v-if="idx < careLogs.length - 1" class="absolute top-1/2 -right-2 h-px w-4 bg-gray-300"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="order.status === 0" class="flex justify-center space-x-4">
        <button 
          @click="openCashier"
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

    <!-- 收银台弹窗 -->
    <teleport to="body">
      <div v-if="cashierVisible" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="closeCashier"></div>
        <div class="relative bg-white rounded-2xl shadow-xl w-[360px] p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold">收银台</h3>
            <button class="text-gray-400 hover:text-gray-600" @click="closeCashier">✕</button>
          </div>
          <div class="text-sm text-gray-500">订单号：{{ order?.orderNo }}</div>
          <div class="mt-4 flex flex-col items-center gap-3">
            <div class="w-48 h-48 rounded-xl border border-gray-200 flex items-center justify-center bg-gray-50">
              <img v-if="qrCodeUrl" :src="qrCodeUrl" class="w-44 h-44" alt="支付二维码" />
              <div v-else class="text-gray-400">生成中...</div>
            </div>
            <div class="text-sm text-gray-600">
              请使用手机扫码完成支付
            </div>
            <div v-if="scanStatus === 'waiting'" class="text-xs text-gray-400">等待扫码回调...</div>
            <div v-else class="text-xs text-green-600">支付成功</div>
          </div>
          <div class="mt-6 flex gap-3">
            <button
              class="flex-1 px-4 py-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50"
              @click="closeCashier"
              :disabled="paying"
            >
              取消
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { showError, showConfirm, showSuccess } from '@/utils/message'
import { formatDateTime } from '@/utils/datetime'

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
  roomTypeDisplay?: string
  roomPrice: number
  checkInDate: string
  checkOutDate: string
  days: number
  totalAmount?: number  // 后端返回的字段
  totalPrice?: number   // 兼容字段
  status: number
  remark: string
  createdAt: string
  items?: Array<{
    productId: number
    productName: string
    price: number
    quantity: number
    subtotal: number
  }>
}

interface CareLog {
  id: number
  careType: number
  content: string
  staffName: string
  createdAt: string
  images?: string | string[]
}

interface HotelReview {
  orderId: number
  hotelId: number
  score: number
  content?: string | null
  createdAt: string
}

const route = useRoute()

const order = ref<Order>({} as Order)
const careLogs = ref<CareLog[]>([])
const loading = ref(true)
const paying = ref(false)
const cashierVisible = ref(false)
const scanStatus = ref<'waiting' | 'success'>('waiting')
const payTimer = ref<number | null>(null)

const qrCodeUrl = computed(() => {
  if (!order.value?.id) return ''
  const callbackUrl = `${window.location.origin}/api/order/${order.value.id}/pay-scan`
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(callbackUrl)}`
})

const review = ref<HotelReview | null>(null)
const submittingReview = ref(false)
const reviewForm = reactive({
  score: 5,
  content: ''
})

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

const getcareTypeName = (type: number | string) => {
  const numberMap: Record<number, string> = {
    1: '喂食',
    2: '遛弯',
    3: '清洁',
    4: '体检',
    5: '其他',
    6: '入住登记'
  }
  const stringMap: Record<string, string> = {
    feeding: '喂食',
    walking: '遛弯',
    cleaning: '清洁',
    health_check: '体检',
    other: '其他',
    check_in: '入住登记',
    checkin: '入住登记'
  }
  if (typeof type === 'number') {
    return numberMap[type] || String(type)
  }
  return stringMap[type] || String(type ?? '')
}

const getCareTypeClass = (type: number | string) => {
  const numberMap: Record<number, string> = {
    1: 'bg-green-100 text-green-600',
    2: 'bg-yellow-100 text-yellow-600',
    3: 'bg-blue-100 text-blue-600',
    4: 'bg-red-100 text-red-600',
    5: 'bg-gray-100 text-gray-600',
    6: 'bg-indigo-100 text-indigo-600'
  }
  const stringMap: Record<string, string> = {
    feeding: 'bg-green-100 text-green-600',
    walking: 'bg-yellow-100 text-yellow-600',
    cleaning: 'bg-blue-100 text-blue-600',
    health_check: 'bg-red-100 text-red-600',
    other: 'bg-gray-100 text-gray-600',
    check_in: 'bg-indigo-100 text-indigo-600',
    checkin: 'bg-indigo-100 text-indigo-600'
  }
  if (typeof type === 'number') {
    return numberMap[type] || 'bg-gray-100 text-gray-600'
  }
  return stringMap[type] || 'bg-gray-100 text-gray-600'
}

const getLogImages = (log: CareLog): string[] => {
  if (!log || !log.images) return []
  if (Array.isArray(log.images)) return log.images.filter(Boolean)
  return log.images
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
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
      return (ids as Array<number | string>).map((petId, idx: number) => ({
        id: typeof petId === 'number' ? petId : idx,
        name: `宠物${idx + 1}`,
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
      const logs = logsRes.data || []
      careLogs.value = logs.sort((a: CareLog, b: CareLog) => {
        const timeA = new Date(a.createdAt).getTime()
        const timeB = new Date(b.createdAt).getTime()
        return timeA - timeB
      })
    }

    // 如果订单已完成，尝试获取评价
    if (res.data.status === 3) {
      const reviewRes = await request.get(`/hotel-review/order/${route.params.id}`)
      review.value = reviewRes.data || null
    } else {
      review.value = null
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  if (!order.value) return
  if (reviewForm.score < 1 || reviewForm.score > 5) {
    showError('请先选择评分')
    return
  }

  submittingReview.value = true
  try {
    await request.post('/hotel-review', {
      orderId: order.value.id,
      score: reviewForm.score,
      content: reviewForm.content?.trim() || undefined
    })
    showSuccess('评价提交成功')
    // 重新拉取评价
    const reviewRes = await request.get(`/hotel-review/order/${order.value.id}`)
    review.value = reviewRes.data || null
  } catch (error: any) {
    showError(error.message || '提交失败')
  } finally {
    submittingReview.value = false
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

const openCashier = () => {
  paying.value = false
  scanStatus.value = 'waiting'
  cashierVisible.value = true
  if (payTimer.value) window.clearTimeout(payTimer.value)
  payTimer.value = window.setTimeout(async () => {
    if (!order.value) return
    try {
      await request.get(`/order/${route.params.id}/pay-scan`)
      order.value.status = 1
      scanStatus.value = 'success'
      showSuccess('支付成功！请在入住当天12:00后办理入住手续')
      closeCashier()
    } catch (error: any) {
      showError(error.message || '支付失败')
      scanStatus.value = 'waiting'
    }
  }, 10000)
}

const closeCashier = () => {
  if (payTimer.value) window.clearTimeout(payTimer.value)
  payTimer.value = null
  paying.value = false
  cashierVisible.value = false
  scanStatus.value = 'waiting'
}

onMounted(fetchOrder)
</script>
