<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900">我的宠物</h1>
      <button @click="showAddModal = true" class="btn-primary">
        + 添加宠物
      </button>
    </div>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else-if="pets.length === 0" class="text-center py-20">
      <span class="text-6xl">🐾</span>
      <p class="mt-4 text-gray-600">还没有添加宠物</p>
      <button @click="showAddModal = true" class="btn-primary mt-4">添加宠物</button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div v-for="pet in pets" :key="pet.id" class="card">
        <div class="flex items-start justify-between">
          <div class="flex items-center space-x-4">
            <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center text-3xl">
              {{ pet.petType === 'cat' ? '🐱' : '🐕' }}
            </div>
            <div>
              <h3 class="text-xl font-semibold">{{ pet.name }}</h3>
              <p class="text-gray-500">{{ pet.breed || '未知品种' }}</p>
            </div>
          </div>
          <div class="flex space-x-2">
            <button @click="editPet(pet)" class="text-primary-600 hover:text-primary-700">编辑</button>
            <button @click="deletePet(pet)" class="text-red-500 hover:text-red-600">删除</button>
          </div>
        </div>
        <div class="mt-4 grid grid-cols-3 gap-4 text-sm">
          <div>
            <p class="text-gray-500">年龄</p>
            <p class="font-medium">{{ pet.age ? `${pet.age}岁` : '未知' }}</p>
          </div>
          <div>
            <p class="text-gray-500">体重</p>
            <p class="font-medium">{{ pet.weight ? `${pet.weight}kg` : '未知' }}</p>
          </div>
          <div>
            <p class="text-gray-500">性别</p>
            <p class="font-medium">{{ pet.gender === 'male' ? '公' : pet.gender === 'female' ? '母' : '未知' }}</p>
          </div>
        </div>
        <div v-if="pet.healthNotes" class="mt-4 p-3 bg-yellow-50 rounded-lg">
          <p class="text-sm text-yellow-800">健康备注: {{ pet.healthNotes }}</p>
        </div>
      </div>
    </div>

    <!-- 添加/编辑宠物弹窗 -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
        <h3 class="text-xl font-semibold mb-4">{{ editingPet ? '编辑宠物' : '添加宠物' }}</h3>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">宠物名称 *</label>
            <input v-model="form.name" required class="input-field" placeholder="请输入宠物名称" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">宠物类型 *</label>
            <select v-model="form.petType" required class="input-field">
              <option value="">请选择</option>
              <option value="cat">猫咪</option>
              <option value="dog">狗狗</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">品种</label>
            <input v-model="form.breed" class="input-field" placeholder="请输入品种" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">年龄</label>
              <input type="number" v-model.number="form.age" min="0" class="input-field" placeholder="岁" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">体重(kg)</label>
              <input type="number" v-model.number="form.weight" min="0" step="0.1" class="input-field" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
            <select v-model="form.gender" class="input-field">
              <option value="">未知</option>
              <option value="male">公</option>
              <option value="female">母</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">健康备注</label>
            <textarea v-model="form.healthNotes" class="input-field" rows="3" placeholder="请填写宠物的健康状况、过敏信息等"></textarea>
          </div>
          <div class="flex justify-end space-x-4 pt-4">
            <button type="button" @click="closeModal" class="btn-secondary">取消</button>
            <button type="submit" :disabled="submitting" class="btn-primary">
              {{ submitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

interface Pet {
  id: number
  name: string
  petType: string
  breed: string
  age: number | null
  weight: number | null
  gender: string
  healthNotes: string
}

const pets = ref<Pet[]>([])
const loading = ref(true)
const submitting = ref(false)
const showAddModal = ref(false)
const editingPet = ref<Pet | null>(null)

const form = reactive({
  name: '',
  petType: '',
  breed: '',
  age: null as number | null,
  weight: null as number | null,
  gender: '',
  healthNotes: ''
})

const fetchPets = async () => {
  loading.value = true
  try {
    const res = await request.get('/pet/my-pets')
    pets.value = res.data
  } catch (error) {
    console.error('获取宠物列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    petType: '',
    breed: '',
    age: null,
    weight: null,
    gender: '',
    healthNotes: ''
  })
}

const closeModal = () => {
  showAddModal.value = false
  editingPet.value = null
  resetForm()
}

const editPet = (pet: Pet) => {
  editingPet.value = pet
  Object.assign(form, {
    name: pet.name,
    petType: pet.petType,
    breed: pet.breed,
    age: pet.age,
    weight: pet.weight,
    gender: pet.gender,
    healthNotes: pet.healthNotes
  })
  showAddModal.value = true
}

const deletePet = async (pet: Pet) => {
  if (!confirm(`确定要删除 ${pet.name} 吗？`)) return
  
  try {
    await request.delete(`/pet/${pet.id}`)
    pets.value = pets.value.filter(p => p.id !== pet.id)
  } catch (error: any) {
    alert(error.message || '删除失败')
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (editingPet.value) {
      await request.put('/pet', { ...form, id: editingPet.value.id })
      const index = pets.value.findIndex(p => p.id === editingPet.value!.id)
      if (index > -1) {
        pets.value[index] = { ...pets.value[index], ...form }
      }
    } else {
      const res = await request.post('/pet', form)
      pets.value.push(res.data)
    }
    closeModal()
  } catch (error: any) {
    alert(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchPets)
</script>
