<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <button @click="$router.back()" class="flex items-center text-gray-600 hover:text-primary-600 mb-6">
      <span>← 返回</span>
    </button>

    <div v-if="loading" class="text-center py-20">
      <div
          class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else-if="room" class="flex flex-col">
      <div class="card w-full mb-8">
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
              <div v-if="getRoomImages(room.images).length > 1"
                   class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2 z-10">
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
                  <img :src="img" :alt="`图片${Number(idx) + 1}`" class="w-full h-full object-cover" loading="lazy"/>
                </button>
              </div>
            </div>
            <div v-else class="h-64 md:h-96 bg-gray-200 rounded-lg flex items-center justify-center text-8xl">
              {{ getRoomIcon(room.typeName) }}
            </div>
            <!-- 详细描述 -->
            <div v-if="room.description" class="mt-8 pt-8 border-t">
              <h2 class="text-xl font-semibold mb-4">房间介绍</h2>
              <div class="prose text-gray-600">
                <p>{{ room.description }}</p>
              </div>
            </div>
          </div>

          <!-- 信息区 -->
          <div class="min-w-0">
            <div class="mb-4">
              <h1 class="text-3xl font-bold text-gray-900">{{ room.typeNameDisplay || room.typeName }}</h1>
            </div>

            <p v-if="room.hotelName" class="text-gray-500 mb-1">{{ room.hotelName }}</p>
            <p v-if="room.hotelAddress" class="text-gray-500 mb-4">{{ room.hotelAddress }}</p>

            <p class="text-gray-500 mb-4">房间号: {{ room.roomNo }}</p>

            <div class="flex flex-col gap-2 mb-4">
              <div class="flex items-center gap-2 text-sm text-gray-700">
                <span class="font-medium">房间评分:</span>
                <span class="text-yellow-500 text-base">{{ renderStars(roomAvgScore) }}</span>
                <span class="text-gray-500">{{ roomAvgScore.toFixed(1) }}/5</span>
                <span class="text-gray-400 text-xs">({{ roomReviewCount }} 人评价)</span>
              </div>
              <div class="flex items-center gap-2 text-sm text-gray-700">
                <span class="font-medium">酒店评分:</span>
                <span class="text-yellow-500 text-base">{{ renderStars(hotelAvgScore) }}</span>
                <span class="text-gray-500">{{ hotelAvgScore.toFixed(1) }}/5</span>
                <span class="text-gray-400 text-xs">({{ hotelReviewCount }} 人评价)</span>
              </div>
            </div>

            <div class="text-3xl font-bold text-primary-600 mb-6">
              ¥{{ room.pricePerNight }}
              <span class="text-lg text-gray-500">/天</span>
            </div>

            <!-- 日期选择 -->
            <div class="mb-6 p-4 bg-gray-50 rounded-lg">
              <h3 class="font-semibold mb-3">选择入住日期</h3>
              <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="入住日期"
                  end-placeholder="退房日期"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disablePastDates"
                  @change="handleDateRangeChange"
                  class="w-full room-date-picker"
              />
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
              <!-- <div v-if="room.description" class="flex items-start text-gray-600">
                <span class="w-24 flex-shrink-0">介绍:</span>
                <span>{{ room.description }}</span>
              </div> -->
            </div>
          </div>
        </div>

        <!-- 商品选购和按钮 -->
        <div class="w-full">
          <div v-if="productList.length" class="card w-full flex flex-col mb-6">
            <h2 class="text-xl font-semibold mb-4">选购</h2>
            <div class="relative">
              <div class="product-scroll flex gap-4 overflow-x-auto pb-4"
                   :class="productList.length <= 3 ? 'justify-center' : ''" ref="productScroll">
                <div v-for="p in productList" :key="p.id" class="p-4 border rounded-lg flex-shrink-0 w-48">
                  <div class="flex flex-col gap-2 h-full">
                    <div class="w-full h-24 rounded bg-gray-100 overflow-hidden flex-shrink-0">
                      <img
                          v-if="getProductImages(p.images).length"
                          :src="getProductImages(p.images)[0]"
                          :alt="p.name"
                          class="w-full h-full object-cover"
                      />
                      <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-2xl">📦</div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="font-medium text-sm truncate">{{ p.name }}</p>
                      <p class="text-xs text-gray-500">¥{{ p.price }} / 件</p>
                      <p class="text-xs text-gray-400 mt-1">库存：{{ p.stock }}</p>
                    </div>
                    <el-input-number
                        v-model="productQty[p.id]"
                        :min="0"
                        :max="p.stock"
                        size="small"
                        class="w-full"
                    />
                    <p v-if="p.description" class="text-xs text-gray-500 line-clamp-2">{{ p.description }}</p>
                  </div>
                </div>
              </div>
              <button
                  v-if="productScrollable"
                  class="scroll-btn left"
                  aria-label="向左微移"
                  @click="scrollProducts('left')"
              >
                ←
              </button>
              <button
                  v-if="productScrollable"
                  class="scroll-btn right"
                  aria-label="向右微移"
                  @click="scrollProducts('right')"
              >
                →
              </button>
            </div>
            <div v-if="productScrollable" class="text-center mt-3 text-sm text-gray-500">
              ← 左右滑动查看更多商品 →
            </div>
          </div>

          <div class="flex justify-center">
            <button
                v-if="isAvailable && availabilityChecked"
                @click="handleBooking"
                class="btn-primary px-12 py-3 text-lg"
            >
              立即预订
            </button>
            <button
                v-else-if="availabilityChecked && !isAvailable"
                disabled
                class="px-12 py-3 text-lg bg-gray-200 text-gray-500 rounded-lg cursor-not-allowed"
            >
              该时间段不可预订
            </button>
            <button
                v-else
                disabled
                class="px-12 py-3 text-lg bg-gray-200 text-gray-500 rounded-lg cursor-not-allowed"
            >
              请选择入住日期
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区域 - 独立展示 -->
      <div v-if="reviews.length" class="mt-8">
        <div class="card">
          <h2 class="text-xl font-semibold mb-6">用户评价</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div v-for="review in reviews" :key="review.orderId"
                 class="p-4 bg-gray-50 rounded-lg hover:shadow-md transition-shadow">
              <div class="flex items-start justify-between mb-2">
                <div class="flex items-center gap-1">
                  <span class="text-yellow-500">{{ renderStars(review.score) }}</span>
                  <span class="text-gray-600 text-sm font-medium">{{ review.score }}.0分</span>
                </div>
                <span class="text-xs text-gray-400">{{ formatReviewDate(review.createdAt) }}</span>
              </div>
              <p class="text-sm font-medium text-gray-700 mb-2">{{ review.userName || '匿名用户' }}</p>
              <p v-if="review.content" class="text-sm text-gray-600">{{ review.content }}</p>
              <p v-else class="text-sm text-gray-400 italic">未留评价内容</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <button
      class="fixed bottom-8 right-6 bg-primary-600 text-white px-4 py-3 rounded-full shadow-lg hover:bg-primary-700 transition-colors z-50 flex items-center gap-2"
      @click="openServiceChat"
      title="联系客服"
  >
    <span class="text-lg">💬</span>
    <span class="font-medium">联系客服</span>
  </button>

  <teleport to="body">
    <div v-if="serviceVisible" class="fixed inset-0 z-50 flex items-end justify-end pointer-events-none">
      <div class="absolute inset-0 bg-black/30 pointer-events-auto" @click="closeServiceChat"></div>
      <div
          class="relative w-full sm:w-[420px] h-[520px] sm:h-[600px] bg-white rounded-t-2xl sm:rounded-2xl shadow-2xl m-0 sm:m-6 pointer-events-auto flex flex-col overflow-hidden">
        <div class="flex items-center justify-between px-4 py-3 border-b">
          <div>
            <p class="text-base font-semibold">在线客服</p>
            <p class="text-xs text-gray-500" v-if="staffId">正在连接客服 {{ staffName || ('ID ' + staffId) }}</p>
            <p class="text-xs text-gray-500" v-else>匹配在线客服中...</p>
          </div>
          <button class="text-gray-400 hover:text-gray-600" @click="closeServiceChat">✕</button>
        </div>

        <div class="flex-1 bg-gray-50 px-4 py-3 space-y-3 overflow-y-auto" ref="chatBody">
          <div v-if="chatMessages.length === 0" class="text-center text-gray-400 text-sm mt-10">正在为您接入客服...
          </div>
          <div
              v-for="(msg, idx) in chatMessages"
              :key="idx"
              class="flex"
              :class="msg.from === 'me' ? 'justify-end' : 'justify-start'"
          >
            <div
                class="max-w-[70%] px-3 py-2 rounded-2xl text-sm"
                :class="msg.from === 'me' ? 'bg-primary-600 text-white rounded-br-sm' : 'bg-white border rounded-bl-sm'"
            >
              <div>{{ msg.text }}</div>
              <div class="mt-1 text-[11px] opacity-70 text-right">{{ formatChatTime(msg.ts) }}</div>
            </div>
          </div>
        </div>

        <div class="px-4 py-3 border-t bg-white">
          <div class="flex items-center gap-2">
            <input
                v-model="chatInput"
                @keyup.enter="sendChat"
                type="text"
                placeholder="请输入要咨询的问题"
                class="flex-1 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:outline-none"
                :disabled="!connected"
            />
            <button
                class="px-4 py-2 rounded-lg text-white"
                :class="connected && chatInput.trim() ? 'bg-primary-600 hover:bg-primary-700' : 'bg-gray-300 cursor-not-allowed'"
                @click="sendChat"
                :disabled="!connected || !chatInput.trim()"
            >
              发送
            </button>
          </div>
          <p class="text-xs text-gray-400 mt-2" v-if="!connected">正在连接客服，请稍候...</p>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import {nextTick, onMounted, onUnmounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
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

interface Product {
  id: number
  name: string
  price: number
  stock: number
  description?: string
  images?: string
}

interface Review {
  orderId: number
  hotelId: number
  score: number
  userName?: string
  content?: string | null
  createdAt: string
}

const route = useRoute()
const router = useRouter()
const room = ref<Room | null>(null)
const loading = ref(true)
const currentImageIndex = ref(0)

const productList = ref<Product[]>([])
const productQty = ref<Record<number, number>>({})
const productScroll = ref<HTMLElement | null>(null)
const productScrollable = ref(false)

const updateProductScrollable = () => {
  const el = productScroll.value
  if (!el) {
    productScrollable.value = false
    return
  }
  const scrollWidth = el.scrollWidth
  const clientWidth = el.clientWidth
  const overflow = (scrollWidth - clientWidth) > 2
  productScrollable.value = overflow
}
const onResize = () => updateProductScrollable()
let ro: ResizeObserver | null = null
let mo: MutationObserver | null = null
let bindRetryTimer: number | null = null

const reviews = ref<Review[]>([])

const roomAvgScore = ref(0)
const roomReviewCount = ref(0)
const hotelAvgScore = ref(0)
const hotelReviewCount = ref(0)

const serviceVisible = ref(false)
const staffId = ref<number | null>(null)
const staffName = ref('')
const chatInput = ref('')
const chatMessages = ref<{ from: 'me' | 'staff'; text: string; ts: number }[]>([])
const wsRef = ref<WebSocket | null>(null)
const connected = ref(false)
const chatBody = ref<HTMLElement | null>(null)

// 日期相关
const dateRange = ref<string[]>([])
const checkInDate = ref('')
const checkOutDate = ref('')
const minDate = ref('')
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
  dateRange.value = [checkInDate.value, checkOutDate.value]
}

const disablePastDates = (date: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

// 日期区间变化时同步入住/退房
const handleDateRangeChange = (range: string[]) => {
  if (!range || range.length !== 2) {
    availabilityChecked.value = false
    return
  }
  let [start, end] = range
  if (start === end) {
    const nextDay = new Date(start)
    nextDay.setDate(nextDay.getDate() + 1)
    end = formatLocalDate(nextDay)
    dateRange.value = [start, end]
  }
  checkInDate.value = start
  checkOutDate.value = end
  checkAvailability()
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
    const items = Object.entries(productQty.value)
        .map(([id, qty]) => ({productId: Number(id), quantity: Number(qty)}))
        .filter(item => item.quantity > 0)
    router.push({
      path: `/booking/${room.value.id}`,
      query: {
        checkInDate: checkInDate.value,
        checkOutDate: checkOutDate.value,
        products: items.length ? encodeURIComponent(JSON.stringify(items)) : undefined
      }
    })
  }
}

const formatChatTime = (ts: number) => {
  const d = new Date(ts)
  const h = `${d.getHours()}`.padStart(2, '0')
  const m = `${d.getMinutes()}`.padStart(2, '0')
  return `${h}:${m}`
}

const scrollChatToBottom = async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

const pushMessage = (from: 'me' | 'staff', text: string, ts?: number) => {
  chatMessages.value.push({from, text, ts: ts ?? Date.now()})
  scrollChatToBottom()
}

const connectWs = (id: number) => {
  const token = localStorage.getItem('user_token') || ''
  const base = window.location.origin.replace(/^http/, 'ws')
  const roomId = route.params.id ? Number(route.params.id) : ''
  const hotelId = room.value?.hotelId ? Number(room.value.hotelId) : ''
  const url = `${base}/api/ws/chat?staffId=${id}${roomId ? `&roomId=${roomId}` : ''}${hotelId ? `&hotelId=${hotelId}` : ''}${token ? `&token=${encodeURIComponent(token)}` : ''}`
  console.log('正在连接 WebSocket:', url)
  console.log('Token:', token ? '有' : '无')
  console.log('StaffId:', id)
  console.log('RoomId:', roomId)
  wsRef.value = new WebSocket(url)

  wsRef.value.onopen = () => {
    console.log('WebSocket 连接成功')
    connected.value = true
  }

  wsRef.value.onmessage = (evt) => {
    console.log('收到消息:', evt.data)
    try {
      const payload = JSON.parse(evt.data)
      if (payload?.type === 'STAFF_CONNECTED') {
        if (payload.staffName) {
          staffName.value = payload.staffName
        }
        return
      }
      if (payload?.type === 'HISTORY') {
        if (payload.staffName) {
          staffName.value = payload.staffName
        }
        const history = Array.isArray(payload.messages) ? payload.messages : []
        history.forEach((msg: any) => {
          if (!msg) return
          const from = msg.sender === 'user' ? 'me' : 'staff'
          const ts = msg.ts ? Number(msg.ts) : undefined
          pushMessage(from, String(msg.text || msg.message || ''), ts)
        })
        return
      }
      if (payload?.type === 'GREETING') {
        if (payload.staffName) {
          staffName.value = payload.staffName
        }
        const greetText = payload.text || '您好，我是客服，很高兴为您服务。'
        pushMessage('staff', String(greetText))
        return
      }
      const text = payload?.text || payload?.message || evt.data
      pushMessage('staff', String(text))
    } catch {
      pushMessage('staff', String(evt.data))
    }
  }

  wsRef.value.onclose = (evt) => {
    console.log('WebSocket 连接关闭:', evt.code, evt.reason)
    connected.value = false
  }

  wsRef.value.onerror = (err) => {
    console.error('WebSocket 错误:', err)
    connected.value = false
    pushMessage('staff', '连接出错，请刷新重试')
  }
}

const fetchOnlineStaff = async () => {
  try {
    console.log('正在获取在线客服...')
    const hotelId = room.value?.hotelId
    const res = await request.get('/support/online-staff', {
      params: { hotelId }
    })
    console.log('客服API响应:', res)
    const id = res.data?.id || res.data?.staffId
    if (id) {
      console.log('匹配到客服ID:', id)
      staffId.value = Number(id)
      connectWs(Number(id))
    } else {
      console.warn('没有在线客服')
      pushMessage('staff', '当前暂无在线客服，请稍后再试。')
    }
  } catch (error: any) {
    console.error('获取在线客服失败:', error)
    console.error('错误详情:', error.message, error.response)
    pushMessage('staff', '获取在线客服失败，请稍后重试。')
  }
}

const openServiceChat = () => {
  serviceVisible.value = true
  chatMessages.value = []
  staffId.value = null
  connected.value = false
  fetchOnlineStaff()
}

const closeServiceChat = () => {
  serviceVisible.value = false
  chatInput.value = ''
  if (wsRef.value) {
    wsRef.value.close()
    wsRef.value = null
  }
}

const sendChat = () => {
  const text = chatInput.value.trim()
  if (!text || !wsRef.value || wsRef.value.readyState !== WebSocket.OPEN) return
  wsRef.value.send(JSON.stringify({text}))
  pushMessage('me', text)
  chatInput.value = ''
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

const getProductImages = (imagesJson: string | undefined) => {
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

const scrollProducts = (dir: 'left' | 'right') => {
  const el = productScroll.value
  if (!el) return
  const step = 240 // 每次微移像素
  const delta = dir === 'left' ? -step : step
  try {
    // 使用浏览器内置平滑滚动
    el.scrollBy({left: delta, behavior: 'smooth'})
  } catch {
    // 兜底：无平滑滚动支持时，简单动画过渡
    const start = el.scrollLeft
    const target = start + delta
    const duration = 220
    const t0 = performance.now()
    const easeOutQuad = (t: number) => 1 - (1 - t) * (1 - t)
    const animate = (now: number) => {
      const p = Math.min(1, (now - t0) / duration)
      const eased = easeOutQuad(p)
      el.scrollLeft = start + (target - start) * eased
      if (p < 1) requestAnimationFrame(animate)
    }
    requestAnimationFrame(animate)
  }
}

const fetchRoom = async () => {
  loading.value = true
  try {
    const roomRes = await request.get(`/room/${route.params.id}`)
    room.value = roomRes.data

    // 拉取酒店与房间评分
    if (room.value?.hotelId) {
      await fetchReviewSummary(room.value.hotelId, room.value.id)
      await fetchReviews(room.value.hotelId)
    }

    // 加载完房间后，检查默认日期的可用性
    await checkAvailability()

    if (room.value?.hotelId) {
      const productRes = await request.get('/product/user/list', {
        params: {hotelId: room.value.hotelId}
      })
      productList.value = productRes.data || []
      const initQty: Record<number, number> = {}
      productList.value.forEach(p => {
        initQty[p.id] = 0
      })
      productQty.value = initQty
      await nextTick()
      updateProductScrollable()
    }
  } catch (error) {
    console.error('获取房间详情失败:', error)
  } finally {
    loading.value = false
  }
}

const renderStars = (score: number) => {
  if (!score || score <= 0) return '☆☆☆☆☆'
  const rounded = Math.min(5, Math.max(0, Math.round(score)))
  return '★★★★★'.slice(0, rounded) + '☆☆☆☆☆'.slice(0, 5 - rounded)
}

const fetchReviewSummary = async (hotelId: number, roomId: number) => {
  try {
    const res = await request.get(`/hotel-review/summary/${hotelId}`)
    const data = res.data || {}
    hotelAvgScore.value = Number(data.hotelAvgScore || 0)
    hotelReviewCount.value = Number(data.hotelReviewCount || 0)

    if (Array.isArray(data.rooms)) {
      const roomStat = data.rooms.find((r: any) => Number(r.roomId) === Number(roomId))
      if (roomStat) {
        roomAvgScore.value = Number(roomStat.avgScore || 0)
        roomReviewCount.value = Number(roomStat.reviewCount || 0)
      } else {
        roomAvgScore.value = 0
        roomReviewCount.value = 0
      }
    } else {
      roomAvgScore.value = 0
      roomReviewCount.value = 0
    }
  } catch (error) {
    console.error('获取评分汇总失败:', error)
  }
}

const fetchReviews = async (hotelId: number) => {
  try {
    const res = await request.get(`/hotel-review/list/${hotelId}`, {
      params: {limit: 12}
    })
    reviews.value = res.data || []
  } catch (error) {
    console.error('获取评论列表失败:', error)
  }
}

const formatReviewDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

watch(productList, async () => {
  await nextTick()
  updateProductScrollable()
})

onMounted(() => {
  initDefaultDates()
  fetchRoom()
  window.addEventListener('resize', onResize)
  // 初始渲染后再测量一次，避免异步渲染误判
  nextTick(() => updateProductScrollable())
  setTimeout(() => updateProductScrollable(), 200)
  // 绑定观察器，确保 DOM 更新或尺寸变化时都会触发检测
  const bindObservers = () => {
    const el = productScroll.value
    if (!el) {
      // 容器尚未出现，稍后重试绑定
      bindRetryTimer = window.setTimeout(bindObservers, 200)
      return
    }
    try {
      if ('ResizeObserver' in window) {
        ro = new ResizeObserver(() => updateProductScrollable())
        ro.observe(el)
      }
    } catch {
    }
    try {
      mo = new MutationObserver(() => updateProductScrollable())
      mo.observe(el, {childList: true, subtree: true})
    } catch {
    }
    // 立即检测一次
    updateProductScrollable()
  }
  bindObservers()
})

onUnmounted(() => {
  if (wsRef.value) {
    wsRef.value.close()
  }
  window.removeEventListener('resize', onResize)
  if (ro) {
    try {
      ro.disconnect()
    } catch {
    }
    ro = null
  }
  if (mo) {
    try {
      mo.disconnect()
    } catch {
    }
    mo = null
  }
  if (bindRetryTimer) {
    clearTimeout(bindRetryTimer)
    bindRetryTimer = null
  }
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

:deep(.room-date-picker) {
  width: 100%;
  max-width: 100%;
}

:deep(.room-date-picker .el-input__wrapper) {
  width: 100%;
}

.product-scroll {
  scroll-snap-type: x mandatory;
  padding-inline: 4px;
  mask-image: linear-gradient(90deg, transparent, #000 24px, #000 calc(100% - 24px), transparent);
  -webkit-mask-image: linear-gradient(90deg, transparent, #000 24px, #000 calc(100% - 24px), transparent);
  /* 隐藏滚动条（跨浏览器） */
  -ms-overflow-style: none; /* IE/Edge */
  scrollbar-width: none; /* Firefox */
  scroll-behavior: smooth; /* 平滑滚动 */
}

.product-scroll > div {
  scroll-snap-align: start;
}

.product-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none; /* Chrome/Safari */
}

.scroll-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  backdrop-filter: blur(2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
  user-select: none;
}

.scroll-btn:hover {
  background: rgba(0, 0, 0, 0.5);
}

.scroll-btn.left {
  left: -8px;
}

.scroll-btn.right {
  right: -8px;
}

.scroll-btn:active {
  transform: translateY(-50%) scale(0.98);
}
</style>