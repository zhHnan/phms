<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form" v-if="!isStaff">
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
    <el-card shadow="never" v-if="!isStaff">
      <div class="table-toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增员工
        </el-button>
        <el-button @click="openPlan">
          <el-icon><Calendar /></el-icon>排班总览
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button type="success" link @click="openSchedule(row)">排班</el-button>
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

    <!-- 我的排班（员工可见） -->
    <el-card shadow="never" v-else>
      <div class="table-toolbar" style="display:flex;align-items:center;gap:12px;">
        <span class="font-medium">我的在岗状态</span>
        <el-select v-model="myStatus" placeholder="请选择" style="width: 140px;" @change="updateMyStatus">
          <el-option label="在岗" :value="1" />
          <el-option label="离岗" :value="2" />
          <el-option label="忙碌" :value="3" />
          <el-option label="离线" :value="4" />
        </el-select>
      </div>
      <el-table :data="myScheduleList" v-loading="scheduleLoading" stripe border>
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="shiftType" label="班次" width="120">
          <template #default="{ row }">
            {{ getShiftName(row.shiftType) }}
          </template>
        </el-table-column>
        <el-table-column label="时间段" min-width="160">
          <template #default="{ row }">
            {{ formatShiftTime(row) }}
          </template>
        </el-table-column>
      </el-table>
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

    <!-- 排班弹窗 -->
    <el-dialog v-model="scheduleVisible" :title="scheduleTitle" width="720px">
      <div class="mb-3 text-gray-600">员工：{{ scheduleStaff?.realName }}（{{ getRoleName(scheduleStaff?.roleType || 1) }}）</div>
      <el-table :data="scheduleList" v-loading="scheduleLoading" stripe border @selection-change="handleScheduleSelection" height="300">
        <el-table-column v-if="canManageSchedule" type="selection" width="50" />
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="shiftType" label="班次" width="120">
          <template #default="{ row }">
            {{ getShiftName(row.shiftType) }}
          </template>
        </el-table-column>
        <el-table-column label="时间段" min-width="160">
          <template #default="{ row }">
            {{ formatShiftTime(row) }}
          </template>
        </el-table-column>
        <el-table-column v-if="canManageSchedule" label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editSchedule(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteSchedule(row)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column v-else label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editSchedule(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4" v-if="canManageSchedule">
        <div class="flex items-center justify-between mb-4">
          <h4 class="text-base font-medium">{{ scheduleForm.id ? '编辑排班' : '新增排班' }}</h4>
          <el-button v-if="scheduleForm.id" size="small" @click="resetScheduleForm">取消编辑</el-button>
        </div>
        <el-form :model="scheduleForm" label-width="100px">
          <el-form-item label="日期" v-if="scheduleForm.id">
            <el-date-picker
              v-model="scheduleForm.workDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              :disabled-date="disableScheduleDate"
            />
          </el-form-item>
          <el-form-item label="日期" v-else>
            <el-date-picker
              v-model="scheduleForm.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :disabled-date="disableScheduleDate"
            />
          </el-form-item>
          <el-form-item label="班次">
            <el-select v-model="scheduleForm.shiftType" placeholder="请选择" @change="applyShiftDefaults">
              <el-option label="早班" :value="1" />
              <el-option label="中班" :value="2" />
              <el-option label="晚班" :value="3" />
              <el-option label="全天班" :value="4" />
              <el-option label="休息" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间段" v-if="scheduleForm.shiftType !== 5">
            <el-time-picker v-model="scheduleForm.startTime" value-format="HH:mm:ss" placeholder="开始时间" />
            <span style="margin: 0 8px;">-</span>
            <el-time-picker v-model="scheduleForm.endTime" value-format="HH:mm:ss" placeholder="结束时间" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="scheduleVisible = false">关闭</el-button>
        <el-button v-if="canManageSchedule" type="danger" :disabled="!selectedScheduleIds.length" @click="batchDeleteSchedule">
          批量删除
        </el-button>
        <el-button type="primary" :loading="scheduleSaving" @click="saveSchedule">保存</el-button>
      </template>
    </el-dialog>

    <!-- 排班总览弹窗 -->
    <el-dialog v-model="planVisible" title="排班总览" width="860px">
      <div class="plan-toolbar mb-4">
        <div class="date-controls">
          <el-button
            type="default"
            size="small"
            icon="ArrowLeft"
            @click="navigateDate(-1)"
            :disabled="isPrevDisabled"
          />
          <el-date-picker
            v-model="planDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            :disabled-date="disablePlanDate"
            @change="fetchPlan"
            style="width: 160px;"
          />
          <el-button
            type="default"
            size="small"
            icon="ArrowRight"
            @click="navigateDate(1)"
            :disabled="isNextDisabled"
          />
        </div>
        <span class="hint text-gray-500 text-sm">仅支持查看明天起 7 天排班信息</span>
      </div>
      <el-table :data="planList" v-loading="planLoading" stripe border>
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="staffName" label="员工" width="140" />
        <el-table-column prop="roleType" label="角色" width="120">
          <template #default="{ row }">
            {{ getRoleName(row.roleType || 1) }}
          </template>
        </el-table-column>
        <el-table-column prop="shiftType" label="班次" width="120">
          <template #default="{ row }">
            {{ getShiftName(row.shiftType) }}
          </template>
        </el-table-column>
        <el-table-column label="时间段" min-width="160">
          <template #default="{ row }">
            {{ formatShiftTime(row) }}
          </template>
        </el-table-column>
        <el-table-column v-if="canManageSchedule" label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="deletePlanRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="planVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

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
const userStore = useUserStore()

const scheduleVisible = ref(false)
const scheduleLoading = ref(false)
const scheduleSaving = ref(false)
const scheduleList = ref<any[]>([])
const myScheduleList = ref<any[]>([])
const scheduleStaff = ref<Staff | null>(null)
const myStatus = ref<number | null>(null)
const selectedScheduleIds = ref<number[]>([])

const planVisible = ref(false)
const planLoading = ref(false)
const planList = ref<any[]>([])
const planDate = ref('')

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
const scheduleTitle = computed(() => scheduleForm.id ? '编辑排班' : '新增排班')
const canManageSchedule = computed(() => userStore.isAdmin || userStore.isManager)
const isStaff = computed(() => !userStore.isAdmin && !userStore.isManager)

const scheduleForm = reactive({
  id: null as number | null,
  staffId: null as number | null,
  workDate: '',
  dateRange: [] as string[],
  shiftType: 1,
  startTime: '09:00:00',
  endTime: '18:00:00'
})

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

const getShiftName = (type: number) => {
  const map: Record<number, string> = {
    1: '早班',
    2: '中班',
    3: '晚班',
    4: '全天班',
    5: '休息'
  }
  return map[type] || '未知'
}

const formatShiftTime = (row: any) => {
  if (row.shiftType === 5) return '休息'
  return `${row.startTime || '--'} - ${row.endTime || '--'}`
}

const applyShiftDefaults = () => {
  switch (scheduleForm.shiftType) {
    case 1:
      scheduleForm.startTime = '09:00:00'
      scheduleForm.endTime = '18:00:00'
      break
    case 2:
      scheduleForm.startTime = '12:00:00'
      scheduleForm.endTime = '21:00:00'
      break
    case 3:
      scheduleForm.startTime = '17:00:00'
      scheduleForm.endTime = '23:59:00'
      break
    case 4:
      scheduleForm.startTime = '00:00:00'
      scheduleForm.endTime = '23:59:00'
      break
    case 5:
      scheduleForm.startTime = ''
      scheduleForm.endTime = ''
      break
  }
}

const formatDate = (date: Date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const disablePlanDate = (date: Date) => {
  const min = new Date()
  min.setHours(0, 0, 0, 0)
  min.setDate(min.getDate() + 1)
  const max = new Date(min)
  max.setDate(max.getDate() + 6)
  return date.getTime() < min.getTime() || date.getTime() > max.getTime()
}

// 添加计算属性判断按钮是否禁用
const isPrevDisabled = computed(() => {
  if (!planDate.value) return true
  const currentDate = new Date(planDate.value)
  const minDate = new Date()
  minDate.setHours(0, 0, 0, 0)
  minDate.setDate(minDate.getDate() + 1)
  return currentDate <= minDate
})

const isNextDisabled = computed(() => {
  if (!planDate.value) return true
  const currentDate = new Date(planDate.value)
  const maxDate = new Date()
  maxDate.setHours(0, 0, 0, 0)
  maxDate.setDate(maxDate.getDate() + 1)
  maxDate.setDate(maxDate.getDate() + 6) // 加上7天范围
  return currentDate >= maxDate
})

// 添加日期导航方法
const navigateDate = (direction: number) => {
  if (!planDate.value) return
  const date = new Date(planDate.value)
  date.setDate(date.getDate() + direction)

  // 检查新日期是否在允许范围内
  const minDate = new Date()
  minDate.setHours(0, 0, 0, 0)
  minDate.setDate(minDate.getDate() + 1)
  const maxDate = new Date(minDate)
  maxDate.setDate(maxDate.getDate() + 6)

  if (date >= minDate && date <= maxDate) {
    planDate.value = formatDate(date)
    fetchPlan()
  }
}

const disableScheduleDate = (date: Date) => {
  const min = new Date()
  min.setHours(0, 0, 0, 0)
  min.setDate(min.getDate() + 1)
  return date.getTime() < min.getTime()
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

const openPlan = async () => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  planDate.value = formatDate(tomorrow)
  planVisible.value = true
  await fetchPlan()
}

const fetchPlan = async () => {
  if (!planDate.value) return
  planLoading.value = true
  try {
    const res = await request.get('/staff-schedule/plan', {
      params: {
        from: planDate.value,
        to: planDate.value
      }
    })
    planList.value = res.data
  } catch (error) {
    console.error('获取排班总览失败:', error)
  } finally {
    planLoading.value = false
  }
}

const deletePlanRow = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', { type: 'warning' })
    await request.delete(`/staff-schedule/${row.id}`)
    await fetchPlan()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除排班失败:', error)
    }
  }
}

const openSchedule = async (row: Staff) => {
  scheduleStaff.value = row
  scheduleForm.id = null
  scheduleForm.staffId = row.id
  scheduleForm.workDate = ''
  scheduleForm.dateRange = []
  selectedScheduleIds.value = []
  scheduleForm.shiftType = 1
  applyShiftDefaults()
  scheduleVisible.value = true
  await loadSchedule(row.id)
}

const handleScheduleSelection = (rows: any[]) => {
  selectedScheduleIds.value = rows.map(row => row.id).filter((id: number) => id)
}

const loadSchedule = async (staffId: number) => {
  scheduleLoading.value = true
  try {
    const res = await request.get('/staff-schedule/list', { params: { staffId } })
    scheduleList.value = res.data
  } catch (error) {
    console.error('获取排班失败:', error)
  } finally {
    scheduleLoading.value = false
  }
}

const saveSchedule = async () => {
  if (!scheduleForm.staffId) {
    ElMessage.warning('请选择员工')
    return
  }
  if (scheduleForm.id) {
    if (!scheduleForm.workDate) {
      ElMessage.warning('请填写排班日期')
      return
    }
  } else if (!scheduleForm.dateRange || scheduleForm.dateRange.length !== 2) {
    ElMessage.warning('请选择排班日期范围')
    return
  }
  scheduleSaving.value = true
  try {
    const payload: any = {
      staffId: scheduleForm.staffId,
      shiftType: scheduleForm.shiftType,
      startTime: scheduleForm.shiftType === 5 ? null : scheduleForm.startTime,
      endTime: scheduleForm.shiftType === 5 ? null : scheduleForm.endTime
    }
    if (scheduleForm.id) {
      payload.workDate = scheduleForm.workDate
    } else {
      payload.startDate = scheduleForm.dateRange[0]
      payload.endDate = scheduleForm.dateRange[1]
    }
    let res: any
    if (scheduleForm.id) {
      res = await request.put(`/staff-schedule/${scheduleForm.id}`, payload)
    } else {
      res = await request.post('/staff-schedule', payload)
    }
    await loadSchedule(scheduleForm.staffId)
    ElMessage.success(res?.message || '保存成功')
  } catch (error) {
    console.error('保存排班失败:', error)
  } finally {
    scheduleSaving.value = false
  }
}

const editSchedule = (row: any) => {
  scheduleForm.id = row.id
  scheduleForm.staffId = row.staffId
  scheduleForm.workDate = row.workDate
  scheduleForm.dateRange = []
  scheduleForm.shiftType = row.shiftType
  scheduleForm.startTime = row.startTime || ''
  scheduleForm.endTime = row.endTime || ''
}

const resetScheduleForm = () => {
  scheduleForm.id = null
  scheduleForm.workDate = ''
  scheduleForm.dateRange = []
  scheduleForm.shiftType = 1
  applyShiftDefaults()
}

const deleteSchedule = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', { type: 'warning' })
    await request.delete(`/staff-schedule/${row.id}`)
    if (scheduleForm.staffId) {
      await loadSchedule(scheduleForm.staffId)
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除排班失败:', error)
    }
  }
}

const batchDeleteSchedule = async () => {
  if (!selectedScheduleIds.value.length || !scheduleForm.staffId) return
  try {
    await ElMessageBox.confirm('确定要批量删除选中的排班吗？', '提示', { type: 'warning' })
    await request.post('/staff-schedule/batch-delete', {
      staffId: scheduleForm.staffId,
      ids: selectedScheduleIds.value
    })
    selectedScheduleIds.value = []
    await loadSchedule(scheduleForm.staffId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

const loadMySchedule = async () => {
  scheduleLoading.value = true
  try {
    const res = await request.get('/staff-schedule/list')
    myScheduleList.value = res.data
  } catch (error) {
    console.error('获取我的排班失败:', error)
  } finally {
    scheduleLoading.value = false
  }
}

const loadMyStatus = async () => {
  try {
    const res = await request.get('/staff-status/my')
    myStatus.value = res.data?.status ?? 4
  } catch (error) {
    console.error('获取在岗状态失败:', error)
  }
}

const updateMyStatus = async (status: number) => {
  try {
    await request.put('/staff-status/my', null, { params: { status } })
    ElMessage.success('状态已更新')
  } catch (error) {
    console.error('更新状态失败:', error)
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
  if (!isStaff.value) {
    fetchData()
    fetchHotels()
  } else {
    loadMySchedule()
    loadMyStatus()
  }
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

.plan-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: nowrap;
}

.plan-toolbar .date-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.plan-toolbar .hint {
  white-space: nowrap;
  flex: 0 0 auto;
}
</style>
