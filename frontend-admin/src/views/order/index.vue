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
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
}

const loading = ref(false)
const tableData = ref<Order[]>([])
const detailVisible = ref(false)
const detailData = ref<Order>({} as Order)

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
    await ElMessageBox.confirm('确定要办理入住吗？', '提示', {
      type: 'warning'
    })
    await request.post(`/order/${row.id}/check-in`)
    ElMessage.success('已办理入住')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('办理入住失败:', error)
    }
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
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
