<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operatorName" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="全部模块" clearable>
            <el-option label="员工管理" value="staff" />
            <el-option label="房间管理" value="room" />
            <el-option label="门店管理" value="hotel" />
            <el-option label="订单管理" value="order" />
            <el-option label="宠物管理" value="pet" />
            <el-option label="护理日志" value="care-log" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
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
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationModule" label="模块" width="120">
          <template #default="{ row }">
            {{ getModuleName(row.operationModule) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            {{ getOperationTypeName(row.operationType) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operationIp" label="IP地址" width="140" />
        <el-table-column prop="operationResult" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.operationResult === 0 ? 'success' : 'danger'">
              {{ row.operationResult === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ detailData.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(detailData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ getModuleName(detailData.operationModule) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationTypeName(detailData.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.operationIp }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.operationResult === 0 ? 'success' : 'danger'">
            {{ detailData.operationResult === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detailData.operationUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-scrollbar max-height="150px">
            <pre>{{ formatJson(detailData.operationParam) }}</pre>
          </el-scrollbar>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.failMsg" label="失败原因" :span="2">
          <el-text type="danger">{{ detailData.failMsg }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

interface OperationLog {
  id: number
  operatorType: number
  operatorId: number
  operatorName: string
  operationModule: string
  operationType: string
  operationParam: string
  operationResult: number
  failMsg: string
  operationIp: string
  operationUrl: string
  createdAt: string
}

// 模块名称映射
const getModuleName = (module: string) => {
  const map: Record<string, string> = {
    staff: '员工管理',
    room: '房间管理',
    hotel: '门店管理',
    order: '订单管理',
    pet: '宠物管理',
    'care-log': '护理日志'
  }
  return map[module] || module
}

// 操作类型映射
const getOperationTypeName = (type: string) => {
  const map: Record<string, string> = {
    add: '新增',
    update: '修改',
    delete: '删除',
    login: '登录',
    logout: '登出'
  }
  return map[type] || type
}

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const detailVisible = ref(false)
const detailData = ref<OperationLog>({} as OperationLog)

const searchForm = reactive({
  operatorName: '',
  module: '',
  status: null as number | null,
  dateRange: null as [string, string] | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
}

const formatJson = (str: string) => {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/log/operation/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        operatorName: searchForm.operatorName || undefined,
        module: searchForm.module || undefined,
        status: searchForm.status ?? undefined,
        startDate: searchForm.dateRange?.[0] || undefined,
        endDate: searchForm.dateRange?.[1] || undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.operatorName = ''
  searchForm.module = ''
  searchForm.status = null
  searchForm.dateRange = null
  handleSearch()
}

const handleDetail = (row: OperationLog) => {
  detailData.value = row
  detailVisible.value = true
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

  :deep(.el-form-item .el-date-editor) {
    width: 320px;
  }
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  font-size: 12px;
}
</style>
