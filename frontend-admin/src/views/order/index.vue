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
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="待入住" :value="1" />
            <el-option label="已入住" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
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
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="下单人" width="120" />
        <el-table-column prop="petNames" label="宠物" width="150">
          <template #default="{ row }">
            {{ row.petNames || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="totalAmount" label="总价(元)" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount || row.totalPrice || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewScore" label="用户评分" width="120">
          <template #default="{ row }">
            <span v-if="row.reviewScore">
              <span class="text-warning">{{ '★'.repeat(row.reviewScore) }}</span>

            </span>
            <span v-else class="text-gray-500">未评价</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status === 1" 
              type="warning" 
              link 
              @click="handleCheckIn(row)"
            >
              办理入住
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              type="info" 
              link 
              @click="handleCheckOut(row)"
            >
              办理退房
            </el-button>
            <el-button 
              v-if="row.status <= 1" 
              type="danger" 
              link 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detailData.status)">
            {{ getStatusName(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单人" :span="2">{{ detailData.userName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="手机号" :span="2">{{ detailData.userPhone || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="宠物" :span="2">{{ detailData.petNames || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detailData.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ detailData.roomType }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ detailData.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="退房日期">{{ detailData.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住天数">{{ detailData.days }}天</el-descriptions-item>
        <el-descriptions-item label="订单总价">¥{{ detailData.totalAmount || detailData.totalPrice || 0 }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(detailData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="用户评分" :span="2">
          <template #default>
            <div v-if="detailData.reviewScore" class="flex items-center gap-3">
              <span class="text-warning">{{ '★'.repeat(detailData.reviewScore) }}</span>
              <!-- <span class="text-gray-500">/5</span> -->
              <span v-if="detailData.reviewCreatedAt" style="margin-left: 20px;" class="text-gray-500">{{ formatDateTime(detailData.reviewCreatedAt) }}</span>
            </div>
            <span v-else class="text-gray-500">未评价</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="用户评价" :span="2">
          <span v-if="detailData.reviewContent">{{ detailData.reviewContent }}</span>
          <span v-else class="text-gray-500">未填写</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 办理入住弹窗 -->
    <el-dialog v-model="checkInVisible" title="办理入住" width="780px" @close="resetCheckInForm">
      <el-steps :active="checkInStep" finish-status="success" align-center class="checkin-steps">
        <el-step title="选择宠物" />
        <el-step title="登记护理信息" />
        <el-step title="完成" />
      </el-steps>

      <!-- 步骤1：选择宠物 -->
      <div v-if="checkInStep === 0" class="mt-5">
        <div class="step-header">
          <div class="text-gray-600">该订单涉及的宠物信息：</div>
          <el-tag type="info" v-if="selectedPets.length">已选 {{ selectedPets.length }} 只</el-tag>
        </div>
        <el-table
          :data="petList"
          stripe
          border
          max-height="400px"
          @selection-change="handlePetSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="宠物名称" width="120" />
          <el-table-column label="宠物类型" width="100">
            <template #default="{ row }">
              {{ getPetTypeName(row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="weight" label="体重(kg)" width="100" />
          <el-table-column label="健康备注" min-width="200">
            <template #default="{ row }">
              {{ row.notes || '无' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 步骤2：登记护理信息 -->
      <div v-if="checkInStep === 1" class="mt-5">
        <el-form :model="careLogForm" label-width="120px">
          <el-form-item label="已选宠物">
            <div class="selected-pets">
              <el-tag
                v-for="pet in selectedPets"
                :key="pet.id"
                class="mr-2"
              >
                {{ pet.name }}（{{ getPetTypeName(pet.type) }}）
              </el-tag>
              <span v-if="selectedPets.length === 0" class="text-gray-400">未选择</span>
            </div>
          </el-form-item>
          <el-form-item label="备注">
            <el-input 
              v-model="careLogForm.content"
              type="textarea" 
              placeholder="可选：其他备注信息"
              rows="2"
            />
          </el-form-item>
          <el-form-item label="宠物照片">
            <el-upload
              v-model:file-list="imageFileList"
              action="#"
              list-type="picture-card"
              :http-request="handleImageUpload"
              :before-upload="beforeImageUpload"
              :on-remove="handleImageRemove"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：完成 -->
      <div v-if="checkInStep === 2" class="mt-5">
        <el-result icon="success" title="入住办理成功" sub-title="护理日志已记录">
          <template #extra>
            <el-card shadow="never" class="result-card">
              <div class="result-row">
                <span class="label">入住宠物</span>
                <div class="value">
                  <el-tag v-for="pet in selectedPets" :key="pet.id" class="mr-2">
                    {{ pet.name }}（{{ getPetTypeName(pet.type) }}）
                  </el-tag>
                </div>
              </div>
              <div class="result-row">
                <span class="label">护理类型</span>
                <span class="value">入住登记</span>
              </div>
              <div class="result-row">
                <span class="label">备注</span>
                <span class="value">{{ careLogForm.content || '无' }}</span>
              </div>
              <div class="result-row" v-if="imageFileList.length">
                <span class="label">宠物照片</span>
                <div class="value image-preview">
                  <el-image
                    v-for="file in imageFileList"
                    :key="file.uid || file.url"
                    :src="file.url || file.response?.url"
                    :preview-src-list="imagePreviewUrls"
                    fit="cover"
                    class="preview-item"
                  />
                </div>
              </div>
            </el-card>
          </template>
        </el-result>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkInVisible = false">关闭</el-button>
          <el-button 
            v-if="checkInStep > 0" 
            @click="checkInStep--"
          >
            上一步
          </el-button>
          <el-button
            v-if="checkInStep < 2"
            type="primary"
            :loading="checkInLoading"
            @click="nextCheckInStep"
          >
            {{ checkInStep === 0 ? '下一步' : '确认入住' }}
          </el-button>
          <el-button 
            v-if="checkInStep === 2"
            type="success" 
            @click="completeCheckIn"
          >
            完成
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Order {
  id: number
  orderNo: string
  petIds?: string
  petNames?: string
  roomId: number
  roomNo: string
  roomType: string
  userId: number
  userName?: string
  userPhone?: string
  checkInDate: string
  checkOutDate: string
  days: number
  totalAmount?: number
  totalPrice?: number
  status: number
  remark: string
  createdAt: string
  reviewScore?: number
  reviewContent?: string
  reviewCreatedAt?: string
}

const loading = ref(false)
const tableData = ref<Order[]>([])
const detailVisible = ref(false)
const detailData = ref<Order>({} as Order)
const checkInVisible = ref(false)
const checkInStep = ref(0)
const checkInLoading = ref(false)
const petList = ref<any[]>([])
const selectedPets = ref<any[]>([])
const currentOrderId = ref<number>(0)
const imageFileList = ref<any[]>([])
const imagePreviewUrls = ref<string[]>([])

const careLogForm = reactive({
  petIds: [] as number[],
  petNames: '',
  careType: 6,
  content: '',
  notes: '',
  images: ''
})

const searchForm = reactive({
  orderNo: '',
  petName: '',
  status: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '待入住',
    2: '已入住',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status: number) => {
  const map: Record<number, string> = {
    0: 'info',
    1: 'primary',
    2: 'warning',
    3: 'success',
    4: 'danger'
  }
  return map[status] || ''
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 图片上传相关方法
const beforeImageUpload = (file: any) => {
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
    
    // 更新本地列表显示真实URL
    const uploadedFile = imageFileList.value.find(f => f.uid === file.uid)
    if (uploadedFile) {
      uploadedFile.url = res.data.url
    }
    
    updateCareLogImages()
    ElMessage.success('图片上传成功')
  } catch (error) {
    onError(error)
    ElMessage.error('图片上传失败')
  }
}

const handleImageRemove = (_file: any, fileList: any[]) => {
  imageFileList.value = fileList
  updateCareLogImages()
}

const updateCareLogImages = () => {
  careLogForm.images = imageFileList.value
    .map(f => f.url || f.response?.url)
    .filter(url => url)
    .join(',')
  imagePreviewUrls.value = imageFileList.value
    .map(f => f.url || f.response?.url)
    .filter(url => url)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/order/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        orderNo: searchForm.orderNo || undefined,
        petName: searchForm.petName || undefined,
        status: searchForm.status ?? undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.petName = ''
  searchForm.status = null
  handleSearch()
}

const handleDetail = (row: Order) => {
  detailData.value = row
  detailVisible.value = true
}

const handleCheckIn = async (row: Order) => {
  try {
    currentOrderId.value = row.id
    // 获取宠物详情
    const response = await request.get(`/order/${row.id}/pets`)
    petList.value = response.data
    if (!petList.value.length) {
      ElMessage.error('该订单没有关联宠物')
      return
    }
    selectedPets.value = []
    imageFileList.value = []
    imagePreviewUrls.value = []
    careLogForm.petIds = []
    careLogForm.petNames = ''
    careLogForm.images = ''
    
    checkInStep.value = 0
    checkInVisible.value = true
  } catch (error) {
    console.error('获取宠物信息失败:', error)
    ElMessage.error('获取宠物信息失败')
  }
}

const handleCheckOut = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要办理退房吗？', '提示', {
      type: 'warning'
    })
    await request.post(`/order/${row.id}/check-out`)
    ElMessage.success('已办理退房')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('办理退房失败:', error)
    }
  }
}

const handlePetSelectionChange = (rows: any[]) => {
  selectedPets.value = rows
}

const nextCheckInStep = async () => {
  if (checkInStep.value === 0) {
    if (!selectedPets.value.length) {
      ElMessage.error('请先选择宠物')
      return
    }
    careLogForm.petIds = selectedPets.value.map(pet => pet.id)
    careLogForm.petNames = selectedPets.value.map(pet => pet.name).join('、')
    checkInStep.value = 1
  } else if (checkInStep.value === 1) {
    // if (!careLogForm.careType) {
    //   ElMessage.error('请选择护理类型')
    //   return
    // }
    // if (!careLogForm.content.trim()) {
    //   ElMessage.error('请输入护理详情')
    //   return
    // }
    
    // 保存护理日志并办理入住
    try {
      checkInLoading.value = true
      await request.post(`/order/${currentOrderId.value}/check-in`, {
        petIds: careLogForm.petIds,
        petNames: careLogForm.petNames,
        careType: careLogForm.careType,
        content: careLogForm.content,
        images: careLogForm.images
      })
      checkInStep.value = 2
    } catch (error) {
      console.error('办理入住失败:', error)
      ElMessage.error('办理入住失败')
    } finally {
      checkInLoading.value = false
    }
  }
}

const completeCheckIn = () => {
  checkInVisible.value = false
  fetchData()
  ElMessage.success('入住办理完成')
}

const resetCheckInForm = () => {
  checkInStep.value = 0
  careLogForm.petIds = []
  careLogForm.petNames = ''
  careLogForm.careType = 6
  careLogForm.content = ''
  careLogForm.images = ''
  imageFileList.value = []
  imagePreviewUrls.value = []
  petList.value = []
  selectedPets.value = []
}

const getPetTypeName = (type: number) => {
  const typeMap = { 1: '猫咪', 2: '狗狗', 3: '异宠' }
  return typeMap[type as keyof typeof typeMap] || '未知'
}

const handleCancel = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    })
    await request.post(`/order/${row.id}/cancel`)
    ElMessage.success('订单已取消')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
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

.checkin-steps {
  margin-top: 8px;
}

.step-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.selected-pets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.result-card {
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.result-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
}

.result-row .label {
  width: 80px;
  color: #64748b;
}

.result-row .value {
  color: #1f2937;
  flex: 1;
}

.image-preview {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-item {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}
</style>

<style lang="scss" scoped>
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
