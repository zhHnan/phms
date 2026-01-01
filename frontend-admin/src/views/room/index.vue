<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="所属酒店" v-if="isAdmin">
          <el-select v-model="searchForm.hotelId" placeholder="全部酒店" clearable style="width: 180px;">
            <el-option 
              v-for="hotel in hotelList" 
              :key="hotel.id" 
              :label="hotel.name" 
              :value="hotel.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="searchForm.roomNo" placeholder="请输入房间号" clearable />
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="searchForm.typeName" placeholder="全部类型" clearable>
            <el-option label="猫咪标间" value="cat_standard" />
            <el-option label="猫咪豪华间" value="cat_deluxe" />
            <el-option label="狗狗标间" value="dog_standard" />
            <el-option label="狗狗豪华间" value="dog_deluxe" />
            <el-option label="VIP套间" value="vip_suite" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="空闲" :value="0" />
            <el-option label="已预订" :value="1" />
            <el-option label="入住中" :value="2" />
            <el-option label="待清洁" :value="3" />
            <el-option label="维修中" :value="4" />
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
          <el-icon><Plus /></el-icon>新增房间
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border>
<!--        <el-table-column prop="id" label="ID" width="80" />-->
        <el-table-column prop="hotelName" label="所属酒店" width="150" v-if="isAdmin" />
        <el-table-column prop="roomNo" label="房间号" width="120" />
        <el-table-column prop="typeName" label="房间类型" width="140">
          <template #default="{ row }">
            {{ getRoomTypeName(row.typeName) }}
          </template>
        </el-table-column>
        <el-table-column prop="pricePerNight" label="日租金(元)" width="120">
          <template #default="{ row }">
            ¥{{ row.pricePerNight }}
          </template>
        </el-table-column>
        <el-table-column prop="maxPetNum" label="容量(只)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="features" label="房间设施" min-width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="feature in parseFeatures(row.features)" 
              :key="feature" 
              size="small" 
              style="margin-right: 4px; margin-bottom: 2px;"
            >
              {{ feature }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-dropdown @command="(cmd: number) => handleChangeStatus(row, cmd)" style="margin-left: 10px;">
              <el-button type="warning" link>
                状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="0">空闲</el-dropdown-item>
                  <el-dropdown-item :command="3">待清洁</el-dropdown-item>
                  <el-dropdown-item :command="4">维修中</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
        <el-form-item label="所属酒店" prop="hotelId" v-if="isAdmin">
          <el-select v-model="formData.hotelId" placeholder="请选择酒店" style="width: 100%;">
            <el-option 
              v-for="hotel in hotelList" 
              :key="hotel.id" 
              :label="hotel.name" 
              :value="hotel.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNo">
          <el-input v-model="formData.roomNo" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="房间类型" prop="typeName">
          <el-select v-model="formData.typeName" placeholder="请选择房间类型" style="width: 100%;">
            <el-option label="猫咪标间" value="cat_standard" />
            <el-option label="猫咪豪华间" value="cat_deluxe" />
            <el-option label="狗狗标间" value="dog_standard" />
            <el-option label="狗狗豪华间" value="dog_deluxe" />
            <el-option label="VIP套间" value="vip_suite" />
          </el-select>
        </el-form-item>
        <el-form-item label="日租金" prop="pricePerNight">
          <el-input-number v-model="formData.pricePerNight" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="容量" prop="maxPetNum">
          <el-input-number v-model="formData.maxPetNum" :min="1" :max="10" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入房间描述" :rows="3" />
        </el-form-item>
        <el-form-item label="房间设施" prop="features">
          <el-select
            v-model="formData.features"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入房间设施"
            style="width: 100%;"
          >
            <el-option label="监控" value="监控" />
            <el-option label="空调" value="空调" />
            <el-option label="猫爬架" value="猫爬架" />
            <el-option label="独立卫生间" value="独立卫生间" />
            <el-option label="自动喂食器" value="自动喂食器" />
            <el-option label="智能温控" value="智能温控" />
            <el-option label="紫外线消毒" value="紫外线消毒" />
            <el-option label="新风系统" value="新风系统" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 是否为超管
const isAdmin = computed(() => userStore.roleType === 9)

interface Hotel {
  id: number
  name: string
}

interface Room {
  id: number
  roomNo: string
  typeName: string
  pricePerNight: number
  maxPetNum: number
  status: number
  description: string
  features: string
  hotelId: number
  hotelName?: string
  createdAt: string
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Room[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const hotelList = ref<Hotel[]>([])

const searchForm = reactive({
  hotelId: null as number | null,
  roomNo: '',
  typeName: '',
  status: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null as number | null,
  hotelId: null as number | null,
  roomNo: '',
  typeName: '',
  pricePerNight: 0,
  maxPetNum: 1,
  description: '',
  features: [] as string[]
})

const dialogTitle = computed(() => formData.id ? '编辑房间' : '新增房间')

const formRules = computed(() => ({
  hotelId: isAdmin.value ? [{ required: true, message: '请选择所属酒店', trigger: 'change' }] : [],
  roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  typeName: [{ required: true, message: '请选择房间类型', trigger: 'change' }],
  pricePerNight: [{ required: true, message: '请输入日租金', trigger: 'blur' }],
  maxPetNum: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}))

// 解析 features JSON 字符串
const parseFeatures = (features: string): string[] => {
  if (!features) return []
  try {
    return JSON.parse(features)
  } catch {
    return []
  }
}

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

const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    0: '空闲',
    1: '已预订',
    2: '入住中',
    3: '待清洁',
    4: '维修中'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status: number) => {
  const map: Record<number, string> = {
    0: 'success',
    1: 'primary',
    2: 'warning',
    3: 'info',
    4: 'danger'
  }
  return map[status] || ''
}

const fetchData = async () => {
  loading.value = true
  try {
    // 超管可以筛选酒店，其他角色只能查看自己酒店
    const hotelIdParam = isAdmin.value ? searchForm.hotelId : userStore.hotelId
    const res = await request.get('/room/page', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        hotelId: hotelIdParam || undefined,
        roomNo: searchForm.roomNo || undefined,
        typeName: searchForm.typeName || undefined,
        status: searchForm.status ?? undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取房间列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.hotelId = null
  searchForm.roomNo = ''
  searchForm.typeName = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Room) => {
  Object.assign(formData, {
    id: row.id,
    hotelId: row.hotelId,
    roomNo: row.roomNo,
    typeName: row.typeName,
    pricePerNight: row.pricePerNight,
    maxPetNum: row.maxPetNum,
    description: row.description,
    features: parseFeatures(row.features)
  })
  dialogVisible.value = true
}

const handleChangeStatus = async (row: Room, status: number) => {
  try {
    await request.put(`/room/${row.id}/status/${status}`)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const handleDelete = async (row: Room) => {
  try {
    await ElMessageBox.confirm('确定要删除该房间吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/room/${row.id}`)
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
    
    // 非超管自动使用当前用户的 hotelId
    const submitData = {
      ...formData,
      hotelId: isAdmin.value ? formData.hotelId : userStore.hotelId,
      features: JSON.stringify(formData.features)
    }
    
    if (formData.id) {
      await request.put('/room', submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/room', submitData)
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
  formData.hotelId = null
  formData.roomNo = ''
  formData.typeName = ''
  formData.pricePerNight = 0
  formData.maxPetNum = 1
  formData.description = ''
  formData.features = []
  formRef.value?.clearValidate()
}

// 获取酒店列表
const fetchHotelList = async () => {
  if (!isAdmin.value) return
  try {
    const res = await request.get('/hotel/list')
    hotelList.value = res.data || []
  } catch (error) {
    console.error('获取酒店列表失败:', error)
  }
}

onMounted(() => {
  fetchHotelList()
  fetchData()
})
</script>

<style lang="scss" scoped>
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
