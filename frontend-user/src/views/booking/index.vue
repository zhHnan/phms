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
            <h3 class="text-xl font-semibold">{{ room?.typeName }}</h3>
            <p class="text-gray-500">房间号: {{ room?.roomNo }}</p>
            <p class="text-primary-600 font-bold">¥{{ room?.pricePerNight }}/天</p>
          </div>
        </div>
      </div>

      <!-- 入住信息 -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">入住信息</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">入住日期 *</label>
            <input 
              type="date" 
              v-model="form.checkInDate" 
              :min="today"
              required
              class="input-field"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">退房日期 *</label>
            <input 
              type="date" 
              v-model="form.checkOutDate" 
              :min="form.checkInDate || today"
              required
              class="input-field"
            />
          </div>
        </div>
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
        
        <div v-if="pets.length > 0" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            选择宠物（最多 {{ room?.maxPetNum || 1 }} 只）
          </label>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div 
              v-for="pet in pets" 
              :key="pet.id"
              @click="togglePet(pet.id)"
              class="p-4 border rounded-lg cursor-pointer transition-all relative"
              :class="selectedPets.includes(pet.id) ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300'"
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
  pricePerNight: number
  maxPetNum: number
  hotelId: number
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
const pets = ref<Pet[]>([])
const selectedPets = ref<number[]>([]) // 选中的宠物ID列表

const today = new Date().toISOString().split('T')[0]

// 获取默认日期（明天和后天）
const getDefaultDates = () => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const dayAfter = new Date()
  dayAfter.setDate(dayAfter.getDate() + 4) // 默认住3天
  
  return {
    checkIn: tomorrow.toISOString().split('T')[0],
    checkOut: dayAfter.toISOString().split('T')[0]
  }
}

const defaultDates = getDefaultDates()

const form = reactive({
  checkInDate: defaultDates.checkIn,
  checkOutDate: defaultDates.checkOut,
  remark: ''
})

const petForm = reactive({
  name: '',
  type: '' as number | '',
  age: null as number | null,
  weight: null as number | null,
  notes: ''
})

// 切换宠物选择
const togglePet = (petId: number) => {
  const index = selectedPets.value.indexOf(petId)
  if (index > -1) {
    // 已选中，取消选择
    selectedPets.value.splice(index, 1)
  } else {
    // 未选中，检查是否超出容量
    if (selectedPets.value.length >= (room.value?.maxPetNum || 1)) {
      showWarning(`该房间最多容纳 ${room.value?.maxPetNum} 只宠物`)
      return
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
  if (!canSubmit.value) {
    if (selectedPets.value.length === 0) {
      showWarning('请至少选择一只宠物')
    } else if (selectedPets.value.length > (room.value?.maxPetNum || 1)) {
      showWarning(`该房间最多容纳 ${room.value?.maxPetNum} 只宠物，请减少选择`)
    } else if (!form.checkInDate || !form.checkOutDate) {
      showWarning('请选择入住和退房日期')
    }
    return
  }
  
  // 校验退房日期必须晚于入住日期
  if (form.checkInDate && form.checkOutDate) {
    const checkIn = new Date(form.checkInDate)
    const checkOut = new Date(form.checkOutDate)
    if (checkOut <= checkIn) {
      showWarning('退房日期必须晚于入住日期')
      return
    }
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
    showSuccess('预订成功！订单号：' + orderData.orderNo)
    router.push('/orders')
  } catch (error: any) {
    showError(error.response?.data?.message || error.message || '预订失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>
