<template>
  <div class="page-container">
    <el-card shadow="never" class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdmin" label="门店">
          <el-select v-model="searchForm.hotelId" placeholder="全部门店" clearable style="width: 200px;">
            <el-option v-for="h in hotelList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="openAdd" v-if="hasPerm('product:add')">
          新增商品
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <el-image
              v-if="parseImages(row.images).length"
              :src="parseImages(row.images)[0]"
              :preview-src-list="parseImages(row.images)"
              fit="cover"
              style="width: 40px; height: 40px; border-radius: 4px;"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="hotelNames" label="关联门店" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="(n, idx) in row.hotelNames || []" :key="idx" class="mr-1" type="info">{{ n }}</el-tag>
            <span v-if="!row.hotelNames || row.hotelNames.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)" v-if="hasPerm('product:edit')">编辑</el-button>
            <el-button type="warning" link @click="toggleStatus(row)" v-if="hasPerm('product:status')">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="info" link @click="openStock(row)" v-if="hasPerm('product:stock')">库存</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="hasPerm('product:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="formData.id ? '编辑商品' : '新增商品'" width="600px" @close="resetForm">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select
            v-model="formData.category"
            filterable
            allow-create
            default-first-option
            placeholder="如：食品/护理/玩具"
            style="width: 100%"
          >
            <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            v-model:file-list="imageFileList"
            action="#"
            :http-request="handleImageUpload"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            list-type="picture-card"
            :limit="8"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #999; font-size: 12px; margin-top: 8px;">
            最多上传8张图片，支持jpg/png，单张不超过10MB
          </div>
        </el-form-item>
        <el-form-item label="门店" prop="hotelIds" v-if="isAdmin">
          <el-select v-model="formData.hotelIds" multiple placeholder="请选择门店" style="width: 100%">
            <el-option v-for="h in hotelList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockVisible" title="库存调整" width="420px" @close="resetStock">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="商品">
          <span>{{ stockForm.name }}</span>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="stockForm.delta" :min="-99999" :max="99999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleStock">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

interface Product {
  id: number
  name: string
  category: string
  price: number
  stock: number
  status: number
  images?: string
  hotelIds?: number[]
  hotelNames?: string[]
  createdAt?: string
  description?: string
}

interface Hotel {
  id: number
  name: string
}

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const hasPerm = (perm: string) => userStore.hasPermission(perm)

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Product[]>([])
const hotelList = ref<Hotel[]>([])

const searchForm = reactive({
  name: '',
  status: undefined as number | undefined,
  hotelId: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formVisible = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  category: '',
  price: 0,
  stock: 0,
  images: [] as string[],
  description: '',
  status: 1,
  hotelIds: [] as number[]
})

const categoryOptions = ['食品', '护理', '玩具', '清洁', '医疗', '其他']

const imageFileList = ref<any[]>([])

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  hotelIds: [{ required: isAdmin.value, message: '请选择门店', trigger: 'change' }]
}

const stockVisible = ref(false)
const stockForm = reactive({
  id: 0,
  name: '',
  delta: 0
})

const fetchHotels = async () => {
  if (!isAdmin.value) return
  const res = await request.get('/hotel/list')
  hotelList.value = res.data || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/product/page', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        name: searchForm.name || undefined,
        status: searchForm.status,
        hotelId: searchForm.hotelId
      }
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
  searchForm.hotelId = undefined
  fetchData()
}

const openAdd = () => {
  resetForm()
  formVisible.value = true
}

const openEdit = (row: Product) => {
  resetForm()
  formData.id = row.id
  formData.name = row.name
  formData.category = row.category
  formData.price = row.price
  formData.stock = row.stock
  formData.description = row.description || ''
  formData.status = row.status
  formData.hotelIds = row.hotelIds ? [...row.hotelIds] : []
  const images = parseImages(row.images || '')
  formData.images = images
  imageFileList.value = images.map((url: string, index: number) => ({
    name: `image-${index}`,
    url,
    uid: Date.now() + index
  }))
  formVisible.value = true
}

const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.category = ''
  formData.price = 0
  formData.stock = 0
  formData.images = []
  formData.description = ''
  formData.status = 1
  formData.hotelIds = []
  imageFileList.value = []
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = { ...formData } as any
    if (!isAdmin.value) {
      payload.hotelIds = undefined
    }
    payload.images = formData.images.length ? JSON.stringify(formData.images) : ''
    if (formData.id) {
      await request.put('/product', payload)
      ElMessage.success('更新成功')
    } else {
      await request.post('/product', payload)
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: Product) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await request.put(`/product/${row.id}/status`, { status: nextStatus })
  ElMessage.success('状态已更新')
  fetchData()
}

const openStock = (row: Product) => {
  stockForm.id = row.id
  stockForm.name = row.name
  stockForm.delta = 0
  stockVisible.value = true
}

const resetStock = () => {
  stockForm.id = 0
  stockForm.name = ''
  stockForm.delta = 0
}

const handleStock = async () => {
  if (!stockForm.id) return
  submitting.value = true
  try {
    await request.put(`/product/${stockForm.id}/stock`, { delta: stockForm.delta })
    ElMessage.success('库存已调整')
    stockVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: Product) => {
  await ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
  await request.delete(`/product/${row.id}`)
  ElMessage.success('删除成功')
  fetchData()
}

// 解析图片 JSON 字符串
const parseImages = (images?: string): string[] => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

// 图片上传前校验
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

// 自定义图片上传
const handleImageUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  try {
    const form = new FormData()
    form.append('file', file)
    const res = await request.post('/upload/single', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const responseData = { url: res.data.url, fileName: res.data.fileName }
    onSuccess(responseData)

    const uploadedFile = imageFileList.value.find(f => f.uid === file.uid)
    if (uploadedFile) {
      uploadedFile.url = res.data.url
      uploadedFile.response = responseData
    }
    formData.images.push(res.data.url)
    ElMessage.success('图片上传成功')
  } catch (error) {
    onError(error)
    ElMessage.error('图片上传失败')
  }
}

// 删除图片
const handleImageRemove = (file: any) => {
  const url = file.response?.url || file.url
  const index = formData.images.indexOf(url)
  if (index > -1) {
    formData.images.splice(index, 1)
  }
}

onMounted(() => {
  fetchHotels()
  fetchData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
}
</style>
