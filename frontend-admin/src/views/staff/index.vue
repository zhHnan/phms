<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form class="search-form-inner" :inline="true" :model="searchForm">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleType" placeholder="全部角色" clearable>
            <el-option label="平台管理员" :value="9" />
            <el-option label="店长" :value="2" />
            <el-option label="员工" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
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
          <el-icon><Plus /></el-icon>新增员工
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="hotelName" label="所属酒店" min-width="150" />
        <el-table-column prop="roleType" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.roleType)">
              {{ getRoleName(row.roleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
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
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item v-if="!formData.id" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="所属酒店" prop="hotelId">
          <el-select v-model="formData.hotelId" placeholder="请选择酒店" style="width: 100%;">
            <el-option v-for="h in hotelList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleType">
          <el-select v-model="formData.roleType" placeholder="请选择角色" style="width: 100%;">
            <el-option label="平台管理员" :value="9" />
            <el-option label="店长" :value="2" />
            <el-option label="员工" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">离职</el-radio>
          </el-radio-group>
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

interface Staff {
  id: number
  realName: string
  email: string
  phone: string
  hotelId: number
  hotelName: string
  roleType: number
  status: number
  createdAt: string
}

interface Hotel {
  id: number
  name: string
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Staff[]>([])
const hotelList = ref<Hotel[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  realName: '',
  roleType: null as number | null,
  status: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null as number | null,
  realName: '',
  email: '',
  password: '',
  phone: '',
  hotelId: null as number | null,
  roleType: 1,
  status: 1
})

const dialogTitle = computed(() => formData.id ? '编辑员工' : '新增员工')

const formRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  hotelId: [{ required: true, message: '请选择酒店', trigger: 'change' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleName = (roleType: number) => {
  const map: Record<number, string> = {
    9: '平台管理员',
    2: '店长',
    1: '员工'
  }
  return map[roleType] || '未知'
}

const getRoleTagType = (roleType: number) => {
  const map: Record<number, string> = {
    9: 'danger',
    2: 'warning',
    1: ''
  }
  return map[roleType] || ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/staff/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        realName: searchForm.realName || undefined,
        roleType: searchForm.roleType ?? undefined,
        status: searchForm.status ?? undefined
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取员工列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchHotels = async () => {
  try {
    const res = await request.get('/hotel/list')
    hotelList.value = res.data
  } catch (error) {
    console.error('获取酒店列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.realName = ''
  searchForm.roleType = null
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Staff) => {
  Object.assign(formData, {
    id: row.id,
    realName: row.realName,
    email: row.email,
    password: '',
    phone: row.phone,
    hotelId: row.hotelId,
    roleType: row.roleType,
    status: row.status
  })
  dialogVisible.value = true
}

const handleResetPwd = async (row: Staff) => {
  try {
    await ElMessageBox.confirm('确定要重置该员工的密码吗？密码将重置为123456', '提示', {
      type: 'warning'
    })
    await request.post(`/staff/${row.id}/reset-password`)
    ElMessage.success('密码已重置为123456')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
    }
  }
}

const handleDelete = async (row: Staff) => {
  try {
    await ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/staff/${row.id}`)
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
    
    if (formData.id) {
      await request.put('/staff', formData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/staff', formData)
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
  formData.realName = ''
  formData.email = ''
  formData.password = ''
  formData.phone = ''
  formData.hotelId = null
  formData.roleType = 1
  formData.status = 1
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchData()
  fetchHotels()
})
</script>

<style lang="scss" scoped>
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

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
