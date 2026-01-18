<template>
  <div class="page-container">
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-row">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleReadAll" :disabled="loading">
            <el-icon><Check /></el-icon>
            一键已读
          </el-button>
          <el-button @click="fetchData" :disabled="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        @row-click="handleRowClick"
        row-class-name="message-row"
      >
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
        <el-table-column prop="isRead" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'info' : 'warning'">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.isRead !== 1" type="primary" link @click.stop="handleRead(row)">
              标记已读
            </el-button>
            <span v-else class="muted">—</span>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { MessageCenter } from '@/api/types'
import { getMessagePage, markMessageRead, markAllMessagesRead } from '@/api'

const loading = ref(false)
const tableData = ref<MessageCenter[]>([])

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMessagePage({
      pageNum: pagination.value.page,
      pageSize: pagination.value.size
    })
    tableData.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleRead = async (row: MessageCenter) => {
  if (!row?.id) return
  const res = await markMessageRead(row.id)
  row.isRead = res.data?.isRead ?? 1
  row.readTime = res.data?.readTime ?? new Date().toISOString()
}

const handleRowClick = async (row: MessageCenter) => {
  if (row.isRead === 1) return
  await handleRead(row)
}

const handleReadAll = async () => {
  const res = await markAllMessagesRead()
  ElMessage.success(`已读 ${res.data || 0} 条`)
  await fetchData()
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return dateStr
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 16px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.muted {
  color: #999;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.message-row) {
  cursor: pointer;
}
</style>
