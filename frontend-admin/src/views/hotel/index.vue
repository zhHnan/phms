<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="酒店名称">
          <el-input v-model="searchForm.name" placeholder="请输入酒店名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="营业中" :value="1" />
            <el-option label="已停业" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增酒店
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="酒店名称" min-width="150" />
        <el-table-column label="图片" width="150">
          <template #default="{ row }">
            <div v-if="parseImages(row.images).length > 0" class="flex items-center gap-2">
              <el-image 
                :src="parseImages(row.images)[0]" 
                :preview-src-list="parseImages(row.images)"
                :hide-on-click-modal="true"
                :preview-teleported="true"
                fit="cover"
                style="width: 40px; height: 40px; border-radius: 4px; cursor: pointer;"
              />
              <span style="color: #409eff; cursor: pointer;" @click="showImageUrls(row)">
                共{{ parseImages(row.images).length }}张
              </span>
            </div>
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '营业中' : '已停业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="酒店名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">营业中</el-radio>
            <el-radio :value="0">已停业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="酒店图片">
          <el-upload
            v-model:file-list="imageFileList"
            action="#"
            :http-request="handleImageUpload"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            list-type="picture-card"
            :limit="10"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #999; font-size: 12px; margin-top: 8px;">
            最多上传10张图片，支持jpg/png格式，单张不超过10MB
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图片URL列表对话框 -->
    <el-dialog v-model="imageUrlDialogVisible" title="酒店图片" width="700px">
      <el-table :data="currentImageUrls" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="预览图" width="120">
          <template #default="{ row }">
            <el-image 
              :src="row.url" 
              :preview-src-list="currentImageUrls.map(item => item.url)"
              :hide-on-click-modal="true"
              :preview-teleported="true"
              :z-index="9999"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 4px; cursor: pointer;"
            />
          </template>
        </el-table-column>
        <el-table-column label="URL" prop="url">
          <template #default="{ row }">
            <div style="word-break: break-all; font-size: 12px; color: #666;">
              {{ row.url }}
            </div>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="imageUrlDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import request from '@/utils/request'

interface Hotel {
  id: number
  name: string
  address: string
  phone: string
  status: number
  images?: string
  createdAt: string
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Hotel[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const imageFileList = ref<any[]>([])
const imageUrlDialogVisible = ref(false)
const currentImageUrls = ref<Array<{ url: string }>>([])

const searchForm = reactive({
  name: '',
  status: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null as number | null,
  name: '',
  address: '',
  phone: '',
  status: 1,
  images: [] as string[]
})

const dialogTitle = computed(() => formData.id ? '编辑酒店' : '新增酒店')

const formRules = {
  name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

// 解析图片JSON字符串
const parseImages = (images: string | undefined): string[] => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/hotel/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        name: searchForm.name || undefined,
        status: searchForm.status ?? undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取酒店列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Hotel) => {
  const images = parseImages(row.images)
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    address: row.address,
    phone: row.phone,
    status: row.status,
    images: images
  })
  // 设置图片预览列表
  imageFileList.value = images.map((url: string, index: number) => ({
    name: `image-${index}`,
    url: url,
    uid: Date.now() + index
  }))
  dialogVisible.value = true
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
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/upload/single', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    const responseData = { url: res.data.url, fileName: res.data.fileName }
    onSuccess(responseData)
    
    // 手动更新fileList中的URL
    const uploadedFile = imageFileList.value.find(f => f.uid === file.uid)
    if (uploadedFile) {
      uploadedFile.url = res.data.url
      uploadedFile.response = responseData
    }
    
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

// 显示图片URL列表
const showImageUrls = (row: Hotel) => {
  const images = parseImages(row.images)
  currentImageUrls.value = images.map((url: string) => ({ url }))
  imageUrlDialogVisible.value = true
}

const handleDelete = async (row: Hotel) => {
  try {
    await ElMessageBox.confirm('确定要删除该酒店吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/hotel/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    // 收集所有已上传的图片URL
    const imageUrls = imageFileList.value.map(file => file.response?.url || file.url).filter(Boolean)
    
    const submitData = {
      ...formData,
      images: JSON.stringify(imageUrls)
    }
    
    if (formData.id) {
      await request.put('/hotel', submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/hotel', submitData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.address = ''
  formData.phone = ''
  formData.status = 1
  formData.images = []
  imageFileList.value = []
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.search-form {
  :deep(.el-form-item) {
    margin-right: 16px;
  }

  // 搜索区的输入框/下拉框默认宽度较窄，会导致选项文本不显示完整
  :deep(.el-form-item .el-input) {
    width: 200px;
  }

  :deep(.el-form-item .el-select) {
    width: 200px;
  }
}
</style>
