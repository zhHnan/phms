<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="宠物名称">
          <el-input v-model="searchForm.petName" placeholder="请输入宠物名称" clearable />
        </el-form-item>
        <el-form-item label="日志类型">
          <el-select v-model="searchForm.logType" placeholder="全部类型" clearable>
            <el-option label="喂食" value="feeding" />
            <el-option label="清洁" value="cleaning" />
            <el-option label="遛弯" value="walking" />
            <el-option label="健康检查" value="health_check" />
            <el-option label="其他" value="other" />
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
          <el-icon><Plus /></el-icon>添加照料记录
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="roomNo" label="房间号" width="120" />
        <el-table-column prop="petName" label="宠物" width="120" />
        <el-table-column prop="logType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLogTypeTagType(row.logType)">
              {{ getLogTypeName(row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="照料内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="images" label="图片" width="100">
          <template #default="{ row }">
            <el-button v-if="row.images" type="primary" link @click="handleViewImages(row)">
              查看图片
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="staffName" label="记录人" width="100" />
        <el-table-column prop="createdAt" label="记录时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="订单" prop="orderId">
          <el-select 
            v-model="formData.orderId" 
            placeholder="请选择订单" 
            style="width: 100%;"
            filterable
            remote
            :remote-method="searchOrders"
            :loading="orderLoading"
            @visible-change="onOrderDropdownVisible"
          >
            <el-option 
              v-for="o in orderList" 
              :key="o.id" 
              :label="`${o.orderNo}${o.roomNo ? ' - 房间:' + o.roomNo : ''}${o.petNames ? ' - 宠物:' + o.petNames : ''}`" 
              :value="o.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日志类型" prop="logType">
          <el-select v-model="formData.logType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="喂食" value="feeding" />
            <el-option label="清洁" value="cleaning" />
            <el-option label="遛弯" value="walking" />
            <el-option label="健康检查" value="health_check" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="照料内容" prop="content">
          <el-input 
            v-model="formData.content" 
            type="textarea" 
            placeholder="请输入照料内容" 
            :rows="4" 
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            v-model:file-list="imageFileList"
            action="#"
            :http-request="handleImageUpload"
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

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="imageVisible" title="图片预览" width="600px">
      <div class="image-list">
        <el-image 
          v-for="(img, index) in imageList" 
          :key="index"
          :src="img"
          :preview-src-list="imageList"
          :initial-index="index"
          fit="cover"
          style="width: 150px; height: 150px; margin: 5px;"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

interface CareLog {
  id: number
  orderId: number
  orderNo: string
  roomNo?: string
  petName: string
  logType: string
  content: string
  images: string
  staffId: number
  staffName: string
  createdAt: string
}

interface Order {
  id: number
  orderNo: string
  petNames?: string
  roomNo?: string
}

const loading = ref(false)
const submitLoading = ref(false)
const orderLoading = ref(false)
const tableData = ref<CareLog[]>([])
const orderList = ref<Order[]>([])
const dialogVisible = ref(false)
const imageVisible = ref(false)
const imageList = ref<string[]>([])
const formRef = ref<FormInstance>()
const userStore = useUserStore()

const imageFileList = ref<any[]>([])

const searchForm = reactive({
  orderNo: '',
  petName: '',
  logType: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null as number | null,
  orderId: null as number | null,
  logType: '',
  content: '',
  images: ''
})

const dialogTitle = computed(() => formData.id ? '编辑照料记录' : '添加照料记录')

const formRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  logType: [{ required: true, message: '请选择日志类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入照料内容', trigger: 'blur' }]
}

const getLogTypeName = (type: string) => {
  const map: Record<string, string> = {
    feeding: '喂食',
    cleaning: '清洁',
    walking: '遛弯',
    health_check: '健康检查',
    other: '其他'
  }
  return map[type] || type
}

const getLogTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    feeding: 'success',
    cleaning: 'primary',
    walking: 'warning',
    health_check: 'danger',
    other: 'info'
  }
  return map[type] || ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/care-log/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        orderNo: searchForm.orderNo || undefined,
        petName: searchForm.petName || undefined,
        logType: searchForm.logType || undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取照料日志失败:', error)
  } finally {
    loading.value = false
  }
}

const searchOrders = async (query: string) => {
  if (!query) {
    // 非超管：无关键词时加载当前门店入住订单
    if (!userStore.isAdmin) {
      await loadCheckedInOrders()
    }
    return
  }
  orderLoading.value = true
  try {
    const res = await request.get('/order/page', {
      params: {
        page: 1,
        size: 20,
        orderNo: query,
        hotelId: userStore.isAdmin ? undefined : (userStore.hotelId ?? undefined),
        status: 2, // 只搜索已入住的订单
        petName: undefined
      }
    })
    orderList.value = res.data.records
  } catch (error) {
    console.error('搜索订单失败:', error)
  } finally {
    orderLoading.value = false
  }
}

const loadCheckedInOrders = async () => {
  if (userStore.isAdmin) return
  if (!userStore.hotelId) return
  orderLoading.value = true
  try {
    const res = await request.get('/order/page', {
      params: {
        page: 1,
        size: 50,
        hotelId: userStore.hotelId,
        status: 2
      }
    })
    orderList.value = res.data.records
  } catch (error) {
    console.error('加载入住订单失败:', error)
  } finally {
    orderLoading.value = false
  }
}

const onOrderDropdownVisible = async (visible: boolean) => {
  if (!visible) return
  if (userStore.isAdmin) return
  if (orderList.value.length > 0) return
  await loadCheckedInOrders()
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.petName = ''
  searchForm.logType = ''
  handleSearch()
}

const handleAdd = () => {
  resetForm()
  // 店长/员工：打开弹窗前预加载当前门店入住订单
  if (!userStore.isAdmin) {
    loadCheckedInOrders()
  }
  dialogVisible.value = true
}

const handleEdit = (row: CareLog) => {
  Object.assign(formData, {
    id: row.id,
    orderId: row.orderId,
    logType: row.logType,
    content: row.content,
    images: row.images
  })
  // 加载当前订单到选项中（展示用）
  orderList.value = [{ id: row.orderId, orderNo: row.orderNo, roomNo: row.roomNo, petNames: row.petName }]

  const urls = (row.images || '').split(',').map(s => s.trim()).filter(Boolean)
  imageFileList.value = urls.map((url: string, index: number) => ({
    name: `image-${index}`,
    url,
    uid: Date.now() + index
  }))
  dialogVisible.value = true
}

const handleViewImages = (row: CareLog) => {
  imageList.value = (row.images || '').split(',').map(s => s.trim()).filter(Boolean)
  imageVisible.value = true
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
    const fd = new FormData()
    fd.append('file', file)
    const res = await request.post('/upload/single', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    const responseData = { url: res.data.url, fileName: res.data.fileName }
    onSuccess(responseData)

    const uploadedFile = imageFileList.value.find((f) => f.uid === file.uid)
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

const handleDelete = async (row: CareLog) => {
  try {
    await ElMessageBox.confirm('确定要删除该照料记录吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/care-log/${row.id}`)
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

    const imageUrls = imageFileList.value
      .map((file) => file.response?.url || file.url)
      .filter(Boolean)

    const submitData = {
      orderId: formData.orderId,
      logType: formData.logType,
      content: formData.content,
      images: imageUrls.join(',')
    }
    
    if (formData.id) {
      await request.put(`/care-log/${formData.id}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/care-log', submitData)
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
  formData.orderId = null
  formData.logType = ''
  formData.content = ''
  formData.images = ''
  orderList.value = []
  imageFileList.value = []
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.search-form {
  :deep(.el-form--inline) {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }

  :deep(.el-form--inline .el-form-item) {
    margin-right: 16px;
    margin-bottom: 12px;
  }

  :deep(.el-form-item .el-input),
  :deep(.el-form-item .el-select) {
    width: 220px;
  }
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>

<style lang="scss" scoped>
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
