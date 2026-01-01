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
            {{ getRoomIcon(room?.roomType || '') }}
          </div>
          <div>
            <h3 class="text-xl font-semibold">{{ getRoomTypeName(room?.roomType || '') }}</h3>
            <p class="text-gray-500">房间号: {{ room?.roomNo }}</p>
            <p class="text-primary-600 font-bold">¥{{ room?.price }}/天</p>
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
        <h2 class="text-lg font-semibold mb-4">宠物信息</h2>
        
        <div v-if="pets.length > 0" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">选择已有宠物</label>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div 
              v-for="pet in pets" 
              :key="pet.id"
              @click="form.petId = pet.id"
              class="p-4 border rounded-lg cursor-pointer transition-all"
              :class="form.petId === pet.id ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300'"
            >
              <div class="text-2xl mb-2">{{ pet.petType === 'cat' ? '🐱' : '🐕' }}</div>
              <p class="font-medium">{{ pet.name }}</p>
              <p class="text-sm text-gray-500">{{ pet.breed }}</p>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 bg-gray-50 rounded-lg">
          <p class="text-gray-600 mb-4">您还没有添加宠物信息</p>
          <button type="button" @click="showAddPet = true" class="btn-primary">
            添加宠物
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
            <select v-model="petForm.petType" required class="input-field">
              <option value="">请选择</option>
              <option value="cat">猫咪</option>
              <option value="dog">狗狗</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">品种</label>
            <input v-model="petForm.breed" class="input-field" placeholder="请输入品种" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">年龄</label>
              <input type="number" v-model.number="petForm.age" min="0" class="input-field" placeholder="岁" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">体重(kg)</label>
              <input type="number" v-model.number="petForm.weight" min="0" step="0.1" class="input-field" />
            </div>
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

interface Room {
  id: number
  roomNo: string
  roomType: string
  price: number
}

interface Pet {
  id: number
  name: string
  petType: string
  breed: string
}

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const addingPet = ref(false)
const showAddPet = ref(false)

const room = ref<Room | null>(null)
const pets = ref<Pet[]>([])

const today = new Date().toISOString().split('T')[0]

const form = reactive({
  petId: null as number | null,
  checkInDate: '',
  checkOutDate: '',
  remark: ''
})

const petForm = reactive({
  name: '',
  petType: '',
  breed: '',
  age: null as number | null,
  weight: null as number | null
})

const days = computed(() => {
  if (!form.checkInDate || !form.checkOutDate) return 0
  const start = new Date(form.checkInDate)
  const end = new Date(form.checkOutDate)
  const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

const totalPrice = computed(() => {
  return (room.value?.price || 0) * days.value
})

const canSubmit = computed(() => {
  return form.petId && form.checkInDate && form.checkOutDate && days.value > 0
})

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

const fetchData = async () => {
  loading.value = true
  try {
    const [roomRes, petsRes] = await Promise.all([
      request.get(`/room/${route.params.roomId}`),
      request.get('/pet/my-pets')
    ])
    room.value = roomRes.data
    pets.value = petsRes.data
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddPet = async () => {
  addingPet.value = true
  try {
    const res = await request.post('/pet', petForm)
    pets.value.push(res.data)
    form.petId = res.data.id
    showAddPet.value = false
    // 重置表单
    Object.assign(petForm, { name: '', petType: '', breed: '', age: null, weight: null })
  } catch (error: any) {
    alert(error.message || '添加宠物失败')
  } finally {
    addingPet.value = false
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value) return
  
  submitting.value = true
  try {
    await request.post('/order', {
      roomId: room.value?.id,
      petId: form.petId,
      checkInDate: form.checkInDate,
      checkOutDate: form.checkOutDate,
      remark: form.remark
    })
    alert('预订成功！')
    router.push('/orders')
  } catch (error: any) {
    alert(error.message || '预订失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>
