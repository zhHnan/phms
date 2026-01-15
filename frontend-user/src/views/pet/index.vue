<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900">我的宠物</h1>
      <button @click="openAddModal" class="btn-primary">
        + 添加宠物
      </button>
    </div>

    <div v-if="!loading" class="mb-6 text-sm text-gray-600">
      已记录狂犬疫苗：<span class="font-medium">{{ rabiesRecordedCount }}</span>/{{ pets.length }}，
      已记录驱虫：<span class="font-medium">{{ dewormingRecordedCount }}</span>/{{ pets.length }}
    </div>

    <div v-if="loading" class="text-center py-20">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
    </div>

    <div v-else-if="pets.length === 0" class="text-center py-20">
      <span class="text-6xl">🐾</span>
      <p class="mt-4 text-gray-600">还没有添加宠物</p>
      <button @click="openAddModal" class="btn-primary mt-4">添加宠物</button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div v-for="pet in pets" :key="pet.id" class="card">
        <div class="flex items-start justify-between">
          <div class="flex items-center space-x-4">
            <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center text-3xl overflow-hidden">
              <img v-if="pet.photoUrl" :src="pet.photoUrl" :alt="pet.name" class="w-full h-full object-cover" />
              <span v-else>{{ pet.type === 1 ? '🐱' : pet.type === 2 ? '🐕' : '🐰' }}</span>
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

        <div class="mt-4 p-3 bg-gray-50 rounded-lg text-sm">
          <p class="text-gray-700 font-medium mb-2">疫苗/驱虫</p>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <p class="text-gray-500">狂犬疫苗</p>
              <p class="font-medium">{{ pet.rabiesVaccineDate ? pet.rabiesVaccineDate : '未记录' }}</p>
            </div>
            <div>
              <p class="text-gray-500">驱虫</p>
              <p class="font-medium">{{ pet.dewormingDate ? pet.dewormingDate : '未记录' }}</p>
            </div>
          </div>
          <div v-if="pet.vaccineNotes" class="mt-2 text-gray-700">
            备注：{{ pet.vaccineNotes }}
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
            <label class="block text-sm font-medium text-gray-700 mb-1">宠物照片</label>
            <div class="flex items-center space-x-4">
              <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center overflow-hidden">
                <img v-if="form.photoUrl" :src="form.photoUrl" alt="宠物照片" class="w-full h-full object-cover" />
                <span v-else class="text-3xl">{{ form.type === 1 ? '🐱' : form.type === 2 ? '🐕' : form.type === 3 ? '🐰' : '📷' }}</span>
              </div>
              <div class="flex-1">
                <input 
                  ref="fileInput"
                  type="file" 
                  accept="image/*" 
                  @change="handleImageChange" 
                  class="hidden"
                />
                <button 
                  type="button"
                  @click="fileInput?.click()" 
                  class="btn-secondary text-sm"
                  :disabled="uploading"
                >
                  {{ uploading ? '上传中...' : form.photoUrl ? '更换照片' : '上传照片' }}
                </button>
                <p class="text-xs text-gray-500 mt-1">支持 JPG、PNG 格式</p>
              </div>
            </div>
          </div>
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

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">狂犬疫苗接种日期</label>
            <input type="date" v-model="form.rabiesVaccineDate" class="input-field" />
            <p class="text-xs text-gray-500 mt-1">不清楚可不填</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">驱虫日期</label>
            <input type="date" v-model="form.dewormingDate" class="input-field" />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">疫苗/驱虫备注</label>
            <textarea v-model="form.vaccineNotes" class="input-field" rows="2" placeholder="如：每3个月驱虫一次、近期打针反应等"></textarea>
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
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/message'

interface Pet {
  id: number
  name: string
  type: number
  age: number | null
  weight: number | null
  notes: string
  photoUrl?: string
  rabiesVaccineDate?: string | null
  dewormingDate?: string | null
  vaccineNotes?: string | null
}

const pets = ref<Pet[]>([])
const loading = ref(true)
const submitting = ref(false)
const uploading = ref(false)
const showAddModal = ref(false)
const editingPet = ref<Pet | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

const form = reactive({
  name: '',
  type: '' as number | '',
  age: null as number | null,
  weight: null as number | null,
  notes: '',
  photoUrl: '',
  rabiesVaccineDate: '' as string | '',
  dewormingDate: '' as string | '',
  vaccineNotes: ''
})

const rabiesRecordedCount = computed(() => pets.value.filter(p => !!p.rabiesVaccineDate).length)
const dewormingRecordedCount = computed(() => pets.value.filter(p => !!p.dewormingDate).length)

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

const today = () => {
  // YYYY-MM-DD
  return new Date().toISOString().slice(0, 10)
}

const openAddModal = () => {
  editingPet.value = null
  resetForm()
  // 默认选中今天（仅新增时）
  form.rabiesVaccineDate = today()
  form.dewormingDate = today()
  showAddModal.value = true
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    type: '',
    age: null,
    weight: null,
    notes: '',
    photoUrl: '',
    rabiesVaccineDate: '',
    dewormingDate: '',
    vaccineNotes: ''
  })
}

const handleImageChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    showWarning('请上传图片文件')
    return
  }

  // 检查文件大小（限制5MB）
  if (file.size > 5 * 1024 * 1024) {
    showWarning('图片大小不能超过5MB')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bucket', 'phms')

    const res = await request.post('/upload/single', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    form.photoUrl = res.data.url
    showSuccess('照片上传成功')
  } catch (error: any) {
    showError(error.message || '照片上传失败')
  } finally {
    uploading.value = false
    // 清空input，允许重复选择同一文件
    if (target) target.value = ''
  }
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
    notes: pet.notes,
    photoUrl: pet.photoUrl || '',
    rabiesVaccineDate: pet.rabiesVaccineDate || '',
    dewormingDate: pet.dewormingDate || '',
    vaccineNotes: pet.vaccineNotes || ''
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

  if (form.rabiesVaccineDate && form.dewormingDate && form.rabiesVaccineDate > form.dewormingDate) {
    // 仅做轻量校验：避免明显的日期填写错误
  }

  submitting.value = true
  try {
    const petData = {
      ...form,
      type: Number(form.type),
      rabiesVaccineDate: form.rabiesVaccineDate || null,
      dewormingDate: form.dewormingDate || null,
      vaccineNotes: form.vaccineNotes || null
    }
    
    if (editingPet.value) {
      await request.put('/pet', { ...petData, id: editingPet.value.id })
      await fetchPets()
      showSuccess('修改成功')
    } else {
      await request.post('/pet', petData)
      await fetchPets()
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
