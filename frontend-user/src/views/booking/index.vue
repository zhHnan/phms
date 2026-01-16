<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">预订房间</h1>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <form v-else @submit.prevent="handleSubmit" class="space-y-6">
      <!-- 房间信息 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">房间信息</h2>
        <div class="flex items-center space-x-4">
          <div class="w-20 h-20 bg-gray-200 rounded-lg flex items-center justify-center text-4xl">
            {{ getRoomIcon(room?.typeName || '') }}
          </div>
          <div>
            <h3 class="text-xl font-semibold">{{ roomTypeDisplay }}</h3>
            <p v-if="room?.hotelName" class="text-gray-500">{{ room.hotelName }}</p>
            <p v-if="room?.hotelAddress" class="text-gray-500">{{ room.hotelAddress }}</p>
            <p class="text-gray-500">房间号: {{ room?.roomNo }}</p>
            <p class="text-primary-600 font-bold">¥{{ room?.pricePerNight }}/天</p>
          </div>
        </div>
      </div>

      <!-- 入住信息 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">入住信息</h2>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住日期"
          end-placeholder="退房日期"
          value-format="YYYY-MM-DD"
          :disabled-date="disablePastDates"
          @change="handleDateRangeChange"
          class="w-full"
        />
        <div v-if="days > 0" class="mt-4 p-4 bg-primary-50 rounded-lg">
          <p class="text-primary-800">
            入住 <strong>{{ days }}</strong> 天，预计费用: 
            <strong class="text-xl">¥{{ totalPrice }}</strong>
          </p>
        </div>
      </div>

      <!-- 宠物信息 -->
      <div class="card">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">
            宠物信息 
            <span class="text-sm text-gray-500 font-normal ml-2">
              (已选 {{ selectedPets.length }}/{{ room?.maxPetNum || 0 }})
            </span>
          </h2>
        </div>
        
        <!-- 房间类型提示 -->
        <div v-if="!isVIPRoom" class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg text-blue-700 text-sm">
          ℹ️ {{ getRoomTypeRestriction() }}
        </div>
        <div v-else class="mb-4 p-3 bg-purple-50 border border-purple-200 rounded-lg text-purple-700 text-sm">
          ✨ 仅VIP间允许不同宠物一起居住
        </div>
        
        <div v-if="pets.length > 0" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            选择宠物（最多 {{ room?.maxPetNum || 1 }} 只）
          </label>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div 
              v-for="pet in pets" 
              :key="pet.id"
              @click="togglePet(pet.id, pet.type)"
              class="p-4 border rounded-lg cursor-pointer transition-all relative"
              :class="[
                { 'opacity-50 cursor-not-allowed': !isPetTypeAllowed(pet.type) },
                selectedPets.includes(pet.id) ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300'
              ]"
            >
              <!-- 选中标记 -->
              <div v-if="selectedPets.includes(pet.id)" class="absolute top-2 right-2 w-6 h-6 bg-primary-600 rounded-full flex items-center justify-center text-white text-xs">
                ✓
              </div>
              <div class="text-2xl mb-2">{{ getPetTypeIcon(pet.type) }}</div>
              <p class="font-medium">{{ pet.name }}</p>
              <p class="text-sm text-gray-500">{{ getPetTypeName(pet.type) }}</p>
              <p v-if="pet.weight" class="text-xs text-gray-400">{{ pet.weight }}kg</p>
            </div>
          </div>
          
          <!-- 容量警告 -->
          <div v-if="selectedPets.length > (room?.maxPetNum || 1)" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
            ⚠️ 已超出房间最大容量（{{ room?.maxPetNum }} 只），请减少宠物数量
          </div>
          
          <!-- 宠物类型不兼容警告 -->
          <div v-if="!isVIPRoom && selectedPets.length > 1 && !areSelectedPetsCompatible" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
            ⚠️ 该房间不允许不同类型的宠物一起居住，请选择相同类型的宠物或预订VIP套间
          </div>
        </div>

        <div v-else class="text-center py-8 bg-gray-50 rounded-lg">
          <p class="text-gray-600 mb-4">您还没有添加宠物信息，无法预订</p>
          <button type="button" @click="showAddPet = true" class="btn-primary">
            立即添加宠物
          </button>
        </div>

        <button 
          v-if="pets.length > 0"
          type="button" 
          @click="showAddPet = true" 
          class="text-primary-600 hover:text-primary-700 text-sm mt-4"
        >
          + 添加新宠物
        </button>
      </div>

      <!-- 备注 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">备注信息</h2>
        <textarea 
          v-model="form.remark"
          rows="3"
          class="input-field"
          placeholder="请填写宠物的特殊需求、饮食习惯、健康状况等"
        ></textarea>
      </div>

      <!-- 提交 -->
      <div class="flex justify-end space-x-4">
        <button type="button" @click="$router.back()" class="btn-secondary">
          取消
        </button>
        <button 
          type="submit" 
          :disabled="!canSubmit || submitting"
          class="btn-primary disabled:opacity-50"
        >
          {{ submitting ? '提交中...' : '确认预订' }}
        </button>
      </div>
    </form>

    <!-- 添加宠物弹窗 -->
    <div v-if="showAddPet" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h3 class="text-xl font-semibold mb-4">添加宠物</h3>
        <form @submit.prevent="handleAddPet" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">宠物名称 *</label>
            <input v-model="petForm.name" required class="input-field" placeholder="请输入宠物名称" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">宠物类型 *</label>
            <select v-model.number="petForm.type" required class="input-field">
              <option value="">请选择</option>
              <option :value="1">猫咪 🐱</option>
              <option :value="2">狗狗 🐕</option>
              <option :value="3">异宠 🐰</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">年龄(岁)</label>
            <input type="number" v-model.number="petForm.age" min="0" max="30" class="input-field" placeholder="选填" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">体重(kg)</label>
            <input type="number" v-model.number="petForm.weight" min="0" step="0.1" class="input-field" placeholder="选填" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">健康/性格备注</label>
            <textarea v-model="petForm.notes" class="input-field" rows="3" placeholder="如：疫苗情况、过敏源、性格特点等"></textarea>
          </div>
          <div class="flex justify-end space-x-4 pt-4">
            <button type="button" @click="showAddPet = false" class="btn-secondary">取消</button>
            <button type="submit" :disabled="addingPet" class="btn-primary">
              {{ addingPet ? '添加中...' : '确认添加' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { showSuccess, showError, showWarning } from '@/utils/message'

interface Room {
  id: number
  roomNo: string
  typeName: string
  typeNameDisplay?: string
  pricePerNight: number
  maxPetNum: number
  hotelId: number
  hotelName?: string
  hotelAddress?: string
}

interface Pet {
  id: number
  name: string
  type: number  // 1=猫 2=狗 3=异宠
  weight: number
  notes: string
}

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const addingPet = ref(false)
const showAddPet = ref(false)

const room = ref<Room | null>(null)
const roomTypeDisplay = computed(() => room.value?.typeNameDisplay || room.value?.typeName || '')
const pets = ref<Pet[]>([])
const selectedPets = ref<number[]>([]) // 选中的宠物ID列表

const formatLocalDate = (d: Date) => {
  const y = d.getFullYear()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${y}-${m}-${day}`
}

const today = formatLocalDate(new Date())

const dateRange = ref<string[]>([])

// 获取默认日期（优先从URL参数获取，否则使用明天和后天）
const getDefaultDates = () => {
  // 先尝试从URL参数获取
  const checkInFromQuery = route.query.checkInDate as string
  const checkOutFromQuery = route.query.checkOutDate as string
  
  if (checkInFromQuery && checkOutFromQuery) {
    return {
      checkIn: checkInFromQuery,
      checkOut: checkOutFromQuery
    }
  }
  
  // 没有URL参数时使用默认值（入住默认今天，退房默认+3天）
  const todayDate = new Date()
  const defaultCheckOut = new Date(todayDate)
  defaultCheckOut.setDate(todayDate.getDate() + 3)
  
  return {
    checkIn: formatLocalDate(todayDate),
    checkOut: formatLocalDate(defaultCheckOut)
  }
}

const defaultDates = getDefaultDates()

const form = reactive({
  checkInDate: defaultDates.checkIn,
  checkOutDate: defaultDates.checkOut,
  remark: ''
})

dateRange.value = [form.checkInDate, form.checkOutDate]

const disablePastDates = (date: Date) => {
  const t = new Date()
  t.setHours(0, 0, 0, 0)
  return date < t
}

const handleDateRangeChange = (range: string[]) => {
  if (!range || range.length !== 2) return
  let [start, end] = range
  if (start === end) {
    const nextDay = new Date(start)
    nextDay.setDate(nextDay.getDate() + 1)
    end = formatLocalDate(nextDay)
    dateRange.value = [start, end]
  }
  form.checkInDate = start
  form.checkOutDate = end
}

// 解析日期字符串为 Date（忽略时区偏移导致的跨天问题）
const parseDate = (dateStr: string) => {
  // dateStr 形如 "2024-01-10"
  const [y, m, d] = dateStr.split('-').map(Number)
  return new Date(y, m - 1, d)
}

const petForm = reactive({
  name: '',
  type: '' as number | '',
  age: null as number | null,
  weight: null as number | null,
  notes: ''
})

// 判断是否是VIP房间
const isVIPRoom = computed(() => {
  const typeName = room.value?.typeName?.toLowerCase() || ''
  return typeName.includes('vip') || typeName.includes('套间')
})

// 判断宠物类型是否允许
const isPetTypeAllowed = (petType: number) => {
  // VIP房间允许所有类型
  if (isVIPRoom.value) return true
  
  const roomTypeName = room.value?.typeName?.toLowerCase() || ''
  
  // 猫咪房间只允许猫
  if (roomTypeName.includes('猫') || roomTypeName.includes('cat')) {
    return petType === 1
  }
  
  // 狗狗房间只允许狗
  if (roomTypeName.includes('狗') || roomTypeName.includes('dog') || roomTypeName.includes('犬')) {
    return petType === 2
  }
  
  // 其他房间类型默认允许
  return true
}

// 获取房间类型限制说明
const getRoomTypeRestriction = () => {
  const roomTypeName = room.value?.typeName?.toLowerCase() || ''
  
  if (roomTypeName.includes('猫') || roomTypeName.includes('cat')) {
    return '该房间仅限猫咪入住'
  }
  
  if (roomTypeName.includes('狗') || roomTypeName.includes('dog') || roomTypeName.includes('犬')) {
    return '该房间仅限狗狗入住'
  }
  
  return ''
}

// 检查已选宠物是否类型兼容
const areSelectedPetsCompatible = computed(() => {
  if (selectedPets.value.length <= 1) return true
  if (isVIPRoom.value) return true
  
  const selectedPetTypes = selectedPets.value.map(petId => {
    const pet = pets.value.find(p => p.id === petId)
    return pet?.type
  }).filter(Boolean)
  
  // 检查是否所有宠物类型都相同
  return new Set(selectedPetTypes).size === 1
})

// 切换宠物选择
const togglePet = (petId: number, petType: number) => {
  const index = selectedPets.value.indexOf(petId)
  if (index > -1) {
    // 已选中，取消选择
    selectedPets.value.splice(index, 1)
  } else {
    // 检查宠物类型是否允许
    if (!isPetTypeAllowed(petType)) {
      showWarning(`该房间不允许该类型的宠物入住，请选择其他宠物或预订VIP套间`)
      return
    }
    
    // 检查是否超出容量
    if (selectedPets.value.length >= (room.value?.maxPetNum || 1)) {
      showWarning(`该房间最多容纳 ${room.value?.maxPetNum} 只宠物`)
      return
    }
    
    // 非VIP房间，检查宠物类型是否兼容
    if (!isVIPRoom.value && selectedPets.value.length > 0) {
      const firstSelectedPet = pets.value.find(p => p.id === selectedPets.value[0])
      if (firstSelectedPet && firstSelectedPet.type !== petType) {
        showWarning('该房间不允许不同类型的宠物一起居住，仅VIP间允许混搭')
        return
      }
    }
    
    selectedPets.value.push(petId)
  }
}

// 获取宠物类型图标
const getPetTypeIcon = (type: number) => {
  const icons: Record<number, string> = {
    1: '🐱', // 猫
    2: '🐕', // 狗
    3: '🐰'  // 异宠
  }
  return icons[type] || '🐾'
}

// 获取宠物类型名称
const getPetTypeName = (type: number) => {
  const names: Record<number, string> = {
    1: '猫咪',
    2: '狗狗',
    3: '异宠'
  }
  return names[type] || '未知'
}

const days = computed(() => {
  if (!form.checkInDate || !form.checkOutDate) return 0
  const start = new Date(form.checkInDate)
  const end = new Date(form.checkOutDate)
  const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

const totalPrice = computed(() => {
  return (room.value?.pricePerNight || 0) * days.value
})

const canSubmit = computed(() => {
  return selectedPets.value.length > 0 && 
         selectedPets.value.length <= (room.value?.maxPetNum || 1) &&
         areSelectedPetsCompatible.value &&
         form.checkInDate && 
         form.checkOutDate && 
         days.value > 0
})

// 根据房型名称获取图标
const getRoomIcon = (typeName: string) => {
  const name = typeName.toLowerCase()
  if (name.includes('猫') || name.includes('cat')) return '🐱'
  if (name.includes('狗') || name.includes('dog') || name.includes('犬')) return '🐕'
  return '🏠'
}

const fetchData = async () => {
  loading.value = true
  try {
    const [roomRes, petsRes] = await Promise.all([
      request.get(`/room/${route.params.roomId}`),
      request.get('/pet/my-pets')
    ])
    room.value = roomRes.data || roomRes
    pets.value = petsRes.data || petsRes
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddPet = async () => {
  if (!petForm.name || !petForm.type) {
    showWarning('请填写宠物名称和类型')
    return
  }
  
  // 确保 type 是有效的数字
  const typeNum = Number(petForm.type)
  if (!typeNum || typeNum < 1 || typeNum > 3) {
    showWarning('请选择有效的宠物类型')
    return
  }
  
  addingPet.value = true
  try {
    // 确保 type 是数字类型
    const petData = {
      name: petForm.name,
      type: typeNum,
      age: petForm.age || null,
      weight: petForm.weight || null,
      notes: petForm.notes || ''
    }
    console.log('提交宠物数据:', petData)
    await request.post('/pet', petData)
    // 重新获取宠物列表
    const petsRes = await request.get('/pet/my-pets')
    pets.value = petsRes.data || petsRes
    showAddPet.value = false
    // 重置表单
    Object.assign(petForm, { name: '', type: '', age: null, weight: null, notes: '' })
    showSuccess('添加宠物成功！')
  } catch (error: any) {
    showError(error.response?.data?.message || error.message || '添加宠物失败')
  } finally {
    addingPet.value = false
  }
}

const handleSubmit = async () => {
  // 基础必填
  if (!form.checkInDate || !form.checkOutDate) {
    showWarning('请选择入住和退房日期')
    return
  }

  const todayDate = parseDate(today)
  const checkIn = parseDate(form.checkInDate)
  const checkOut = parseDate(form.checkOutDate)

  // 不允许选择今天之前的日期
  if (checkIn < todayDate) {
    showWarning('入住日期不能早于今天')
    return
  }

  // 退房必须晚于入住
  if (checkOut <= checkIn) {
    showWarning('退房日期必须晚于入住日期')
    return
  }

  // 校验宠物数量与类型
  if (selectedPets.value.length === 0) {
    showWarning('请至少选择一只宠物')
    return
  }
  if (selectedPets.value.length > (room.value?.maxPetNum || 1)) {
    showWarning(`该房间最多容纳 ${room.value?.maxPetNum} 只宠物，请减少选择`)
    return
  }
  if (!areSelectedPetsCompatible.value) {
    showWarning('该房间不允许不同类型的宠物一起居住，仅VIP间允许混搭')
    return
  }
  
  submitting.value = true
  try {
    const res = await request.post('/order', {
      hotelId: room.value?.hotelId,
      roomId: room.value?.id,
      petIds: selectedPets.value,
      checkInDate: form.checkInDate,
      checkOutDate: form.checkOutDate,
      remark: form.remark
    })
    const orderData = res.data || res
    showSuccess(`预订成功！订单号：${orderData.orderNo}\n请在1分钟内完成支付，否则订单将被自动取消`)
    router.push('/orders')
  } catch (error: any) {
    showError(error.response?.data?.message || error.message || '预订失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>
