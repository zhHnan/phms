<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧：角色列表 -->
      <el-col :span="10">
        <el-card shadow="never" class="role-card">
          <template #header>
            <div class="card-header">
              <span>角色列表</span>
              <el-button type="primary" size="small" @click="handleAdd" v-if="userStore.hasPermission('role:add')">
                <el-icon><Plus /></el-icon>新增
              </el-button>
            </div>
          </template>

          <el-table 
            :data="roleList" 
            v-loading="loading" 
            stripe 
            size="small"
            highlight-current-row
            @current-change="handleRoleSelect"
            ref="roleTableRef"
          >
            <el-table-column prop="roleName" label="角色名称" min-width="100">
              <template #default="{ row }">
                <div class="role-name-cell">
                  <span>{{ row.roleName }}</span>
                  <el-tag size="small" type="info">{{ row.roleCode }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click.stop="handleEdit(row)" v-if="userStore.hasPermission('role:edit')">
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  link 
                  size="small"
                  @click.stop="handleDelete(row)"
                  :disabled="isBuiltInRole(row.id)"
                  v-if="userStore.hasPermission('role:delete')"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：权限配置 -->
      <el-col :span="14">
        <el-card shadow="never" class="permission-card">
          <template #header>
            <div class="card-header">
              <span>
                权限配置
                <template v-if="currentRole">
                  - {{ currentRole.roleName }}
                  <el-tag size="small" style="margin-left: 8px;">{{ currentRole.roleCode }}</el-tag>
                </template>
              </span>
              <el-alert 
                v-if="currentRole"
                type="info" 
                :closable="false" 
                show-icon
                style="padding: 4px 8px; margin-left: 12px;"
              >
                修改后需重新登录生效
              </el-alert>
            </div>
          </template>

          <div v-if="!currentRole" class="empty-state">
            <el-empty description="请在左侧选择一个角色" :image-size="100" />
          </div>

          <div v-else v-loading="permLoading" class="permission-content">
            <div v-if="isAdminRole" class="admin-warning">
              <el-alert type="warning" :closable="false" show-icon>
                <template #title>
                  <el-icon><Warning /></el-icon>
                  超级管理员拥有所有权限，不允许修改
                </template>
              </el-alert>
            </div>

            <div v-for="(perms, module) in groupedPermissions" :key="module" class="permission-group">
              <div class="group-header">
                <el-checkbox
                  :model-value="isModuleAllChecked(module)"
                  :indeterminate="isModuleIndeterminate(module)"
                  :disabled="isAdminRole"
                  @change="(val: boolean) => handleModuleCheck(module, val)"
                >
                  {{ getModuleName(module) }}
                </el-checkbox>
              </div>
              <div class="group-content">
                <el-checkbox
                  v-for="perm in perms"
                  :key="perm.id"
                  :model-value="checkedPermIds.includes(perm.id)"
                  :disabled="isAdminRole"
                  @change="(val: boolean) => handlePermCheck(perm.id, val)"
                >
                  {{ perm.permName }}
                </el-checkbox>
              </div>
            </div>

            <div v-if="Object.keys(groupedPermissions).length === 0" class="empty-tip">
              暂无权限数据
            </div>

            <div class="action-bar" v-if="userStore.hasPermission('permission:manage')">
              <el-button 
                type="primary" 
                :loading="permSaving" 
                :disabled="isAdminRole" 
                @click="handleSavePermissions"
              >
                <el-icon><Check /></el-icon>保存权限
              </el-button>
              <el-button @click="handleResetPermissions" :disabled="isAdminRole">
                <el-icon><Refresh /></el-icon>重置
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="角色ID" prop="id" v-if="!isEdit">
          <el-input-number v-model="form.id" :min="10" :max="99" placeholder="请输入角色ID（10-99）" style="width: 100%;" />
          <div class="form-tip">内置角色ID：1=员工，2=店长，9=超管，自定义角色请使用10-99</div>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码（如：AUDITOR）" :disabled="isEdit && isBuiltInRole(form.id)" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules, ElTable } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  sortOrder: number
  status: number
}

interface Permission {
  id: number
  permCode: string
  permName: string
  module: string
  sortOrder: number
}

const loading = ref(false)
const roleList = ref<Role[]>([])
const roleTableRef = ref<InstanceType<typeof ElTable>>()

// 当前选中的角色
const currentRole = ref<Role | null>(null)

// 表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Role>>({
  id: undefined,
  roleName: '',
  roleCode: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  id: [{ required: true, message: '请输入角色ID', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 权限相关
const permLoading = ref(false)
const permSaving = ref(false)
const allPermissions = ref<Permission[]>([])
const checkedPermIds = ref<number[]>([])
const originalPermIds = ref<number[]>([])

// 是否为超管角色
const isAdminRole = computed(() => currentRole.value?.id === 9)

// 模块名称映射
const moduleNameMap: Record<string, string> = {
  dashboard: '工作台',
  hotel: '门店管理',
  staff: '员工管理',
  room: '房态管理',
  order: '订单管理',
  care: '护理日志',
  log: '日志管理',
  role: '角色管理',
  permission: '权限管理'
}

const getModuleName = (module: string) => moduleNameMap[module] || module

// 按模块分组的权限
const groupedPermissions = computed(() => {
  const groups: Record<string, Permission[]> = {}
  for (const perm of allPermissions.value) {
    if (!groups[perm.module]) {
      groups[perm.module] = []
    }
    groups[perm.module].push(perm)
  }
  return groups
})

// 内置角色判断
const isBuiltInRole = (id: number | undefined) => id !== undefined && [1, 2, 9].includes(id)

// 模块全选相关
const isModuleAllChecked = (module: string) => {
  const perms = groupedPermissions.value[module] || []
  return perms.length > 0 && perms.every(p => checkedPermIds.value.includes(p.id))
}

const isModuleIndeterminate = (module: string) => {
  const perms = groupedPermissions.value[module] || []
  const count = perms.filter(p => checkedPermIds.value.includes(p.id)).length
  return count > 0 && count < perms.length
}

const handleModuleCheck = (module: string, checked: boolean) => {
  const permIds = (groupedPermissions.value[module] || []).map(p => p.id)
  if (checked) {
    checkedPermIds.value = [...new Set([...checkedPermIds.value, ...permIds])]
  } else {
    checkedPermIds.value = checkedPermIds.value.filter(id => !permIds.includes(id))
  }
}

const handlePermCheck = (permId: number, checked: boolean) => {
  if (checked) {
    if (!checkedPermIds.value.includes(permId)) {
      checkedPermIds.value.push(permId)
    }
  } else {
    checkedPermIds.value = checkedPermIds.value.filter(id => id !== permId)
  }
}

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    const res = await request.get('/role/list')
    roleList.value = res.data || []
    // 默认选中第一个角色
    if (roleList.value.length > 0 && !currentRole.value) {
      await nextTick()
      roleTableRef.value?.setCurrentRow(roleList.value[0])
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有权限
const fetchAllPermissions = async () => {
  try {
    const res = await request.get('/permission/list')
    allPermissions.value = res.data || []
  } catch (error) {
    console.error('获取权限列表失败:', error)
  }
}

// 获取角色权限
const fetchRolePermissions = async (roleId: number) => {
  permLoading.value = true
  try {
    const res = await request.get(`/permission/role/${roleId}/ids`)
    checkedPermIds.value = res.data || []
    originalPermIds.value = [...checkedPermIds.value]
  } catch (error) {
    console.error('获取角色权限失败:', error)
  } finally {
    permLoading.value = false
  }
}

// 选择角色
const handleRoleSelect = (row: Role | undefined) => {
  if (row) {
    currentRole.value = row
    fetchRolePermissions(row.id)
  }
}

// 新增角色
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: undefined,
    roleName: '',
    roleCode: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row: Role) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/role', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/role', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchRoleList()
    // 如果编辑的是当前选中的角色，更新显示
    if (isEdit.value && currentRole.value?.id === form.id) {
      const updated = roleList.value.find(r => r.id === form.id)
      if (updated) currentRole.value = updated
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 删除角色
const handleDelete = async (row: Role) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', {
      type: 'warning'
    })
    await request.delete(`/role/${row.id}`)
    ElMessage.success('删除成功')
    // 如果删除的是当前选中的角色，清空选择
    if (currentRole.value?.id === row.id) {
      currentRole.value = null
      checkedPermIds.value = []
    }
    fetchRoleList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 保存权限
const handleSavePermissions = async () => {
  if (!currentRole.value) return
  
  permSaving.value = true
  try {
    await request.put(`/permission/role/${currentRole.value.id}`, {
      permIds: checkedPermIds.value
    })
    ElMessage.success('权限配置保存成功')
    originalPermIds.value = [...checkedPermIds.value]
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    permSaving.value = false
  }
}

// 重置权限
const handleResetPermissions = () => {
  checkedPermIds.value = [...originalPermIds.value]
}

onMounted(() => {
  fetchRoleList()
  fetchAllPermissions()
})
</script>

<style lang="scss" scoped>
.role-card, .permission-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;

  :deep(.el-card__body) {
    flex: 1;
    overflow: auto;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .el-tag {
    font-size: 10px;
  }
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.admin-warning {
  margin-bottom: 16px;
}

.permission-content {
  padding-bottom: 20px;
}

.permission-group {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;

  .group-header {
    background: #f5f7fa;
    padding: 10px 16px;
    font-weight: 500;
    border-bottom: 1px solid #ebeef5;
  }

  .group-content {
    padding: 12px 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 12px 24px;
  }
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.action-bar {
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
