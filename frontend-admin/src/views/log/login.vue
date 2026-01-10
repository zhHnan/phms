<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名/邮箱" clearable />
        </el-form-item>
        <el-form-item label="登录IP">
          <el-input v-model="searchForm.loginIp" placeholder="请输入登录IP" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
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
        <el-table-column prop="userName" label="用户" min-width="150" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.userType === 'staff' ? 'primary' : 'success'">
              {{ row.userType === 'staff' ? '员工' : '客户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginIp" label="登录IP" width="140" />
        <el-table-column prop="userAgent" label="浏览器" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="消息" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="登录时间" width="180" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

interface LoginLog {
  id: number
  userId: number
  userName: string
  userType: string
  loginIp: string
  userAgent: string
  status: number
  message: string
  createdAt: string
}

const loading = ref(false)
const tableData = ref<LoginLog[]>([])

const searchForm = reactive({
  userName: '',
  loginIp: '',
  status: null as number | null,
  dateRange: null as [string, string] | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/log/login/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        userName: searchForm.userName || undefined,
        loginIp: searchForm.loginIp || undefined,
        status: searchForm.status ?? undefined,
        startDate: searchForm.dateRange?.[0] || undefined,
        endDate: searchForm.dateRange?.[1] || undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取登录日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.loginIp = ''
  searchForm.status = null
  searchForm.dateRange = null
  handleSearch()
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
</style>
