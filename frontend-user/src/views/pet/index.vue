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
              {{ pet.type === 1 ? '🐱' : pet.type === 2 ? '🐕' : '🐰' }}
            </div>
            <div>
              <h3 class="text-xl font-semibold">{{ pet.name }}</h3>
              <p class="text-gray-500">{{ pet.type === 1 ? '猫咪' : pet.type === 2 ? '狗狗' : '异宠' }}</p>
            </div>
          </div>
          <div class="flex space-x-2">
            <button @click="editPet(pet)" class="text-primary-600 hover:text-primary-700">编辑</button>
            <button @click="deletePet(pet)" class="text-red-500 hover:text-red-600">删除</button>
          </div>
        </div>
        <div class="mt-4 grid grid-cols-2 gap-4 text-sm">
          <div>
            <p class="text-gray-500">年龄</p>
            <p class="font-medium">{{ pet.age ? `${pet.age}岁` : '未知' }}</p>
          </div>
          <div>
            <p class="text-gray-500">体重</p>
            <p class="font-medium">{{ pet.weight ? `${pet.weight}kg` : '未知' }}</p>
          </div>
        </div>
        <div v-if="pet.notes" class="mt-4 p-3 bg-yellow-50 rounded-lg">
          <p class="text-sm text-yellow-800">备注: {{ pet.notes }}</p>
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
            <select v-model.number="form.type" required class="input-field">
              <option value="">请选择</option>
              <option :value="1">猫咪 🐱</option>
              <option :value="2">狗狗 🐕</option>
              <option :value="3">异宠 🐰</option>
            </select>
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
            <label class="block text-sm font-medium text-gray-700 mb-1">健康/性格备注</label>
            <textarea v-model="form.notes" class="input-field" rows="3" placeholder="如：疫苗情况、过敏源、性格特点等"></textarea>
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
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/message'

interface Pet {
  id: number
  name: string
  type: number
  age: number | null
  weight: number | null
  notes: string
}

const pets = ref<Pet[]>([])
const loading = ref(true)
const submitting = ref(false)
const showAddModal = ref(false)
const editingPet = ref<Pet | null>(null)

const form = reactive({
  name: '',
  type: '' as number | '',
  age: null as number | null,
  weight: null as number | null,
  notes: ''
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
    type: '',
    age: null,
    weight: null,
    notes: ''
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
    type: pet.type,
    age: pet.age,
    weight: pet.weight,
    notes: pet.notes
  })
  showAddModal.value = true
}

const deletePet = async (pet: Pet) => {
  const confirmed = await showConfirm(`确定要删除 ${pet.name} 吗？`, '删除宠物')
  if (!confirmed) return
  
  try {
    await request.delete(`/pet/${pet.id}`)
    pets.value = pets.value.filter(p => p.id !== pet.id)
    showSuccess('删除成功')
  } catch (error: any) {
    showError(error.message || '删除失败')
  }
}

const handleSubmit = async () => {
  if (!form.name || !form.type) {
    showWarning('请填写宠物名称和类型')
    return
  }

  submitting.value = true
  try {
    const petData = {
      ...form,
      type: Number(form.type)
    }
    
    if (editingPet.value) {
      await request.put('/pet', { ...petData, id: editingPet.value.id })
      await fetchPets()  // 重新获取列表
      showSuccess('修改成功')
    } else {
      await request.post('/pet', petData)
      await fetchPets()  // 重新获取列表
      showSuccess('添加成功')
    }
    closeModal()
  } catch (error: any) {
    showError(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchPets)
</script>
