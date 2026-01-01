<template>
  <div class="dashboard">
    <!-- 当前酒店提示 -->
    <el-alert 
      v-if="!isAdmin && userStore.hotelName" 
      :title="`当前查看: ${userStore.hotelName}`" 
      type="info" 
      :closable="false"
      show-icon
      style="margin-bottom: 20px;"
    />
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #409eff;">
            <el-icon :size="28"><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ dashboardData.todayOrders }}</p>
            <p class="stat-label">今日预订</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a;">
            <el-icon :size="28"><House /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ dashboardData.checkedInPets }}</p>
            <p class="stat-label">在住宠物</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c;">
            <el-icon :size="28"><Key /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ dashboardData.freeRooms }}</p>
            <p class="stat-label">空闲房间</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c;">
            <el-icon :size="28"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ dashboardData.totalRooms }}</p>
            <p class="stat-label">总房间数</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 收入统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card revenue-card">
          <div class="stat-icon" style="background-color: #ff9800;">
            <el-icon :size="28"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">¥{{ formatMoney(dashboardData.todayRevenue) }}</p>
            <p class="stat-label">今日收入</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card revenue-card">
          <div class="stat-icon" style="background-color: #9c27b0;">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">¥{{ formatMoney(dashboardData.monthRevenue) }}</p>
            <p class="stat-label">本月收入</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card revenue-card">
          <div class="stat-icon" style="background-color: #00bcd4;">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">¥{{ formatMoney(dashboardData.totalRevenue) }}</p>
            <p class="stat-label">总收入</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>房间状态分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>房态概览</span>
          </template>
          <div class="room-status-list">
            <div class="status-item">
              <span class="status-dot" style="background-color: #67c23a;"></span>
              <span class="status-name">空闲</span>
              <span class="status-count">{{ dashboardData.freeRooms }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" style="background-color: #409eff;"></span>
              <span class="status-name">已预订</span>
              <span class="status-count">{{ dashboardData.reservedRooms }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" style="background-color: #e6a23c;"></span>
              <span class="status-name">入住中</span>
              <span class="status-count">{{ dashboardData.occupiedRooms }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" style="background-color: #909399;"></span>
              <span class="status-name">待清洁</span>
              <span class="status-count">{{ dashboardData.cleaningRooms }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" style="background-color: #f56c6c;"></span>
              <span class="status-name">维修中</span>
              <span class="status-count">{{ dashboardData.maintenanceRooms }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick, computed } from 'vue'
import { getDashboardData } from '@/api'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roleType === 9)

const pieChartRef = ref<HTMLElement>()

const dashboardData = reactive({
  todayOrders: 0,
  checkedInPets: 0,
  freeRooms: 0,
  reservedRooms: 0,
  occupiedRooms: 0,
  cleaningRooms: 0,
  maintenanceRooms: 0,
  totalRooms: 0,
  todayRevenue: 0,
  monthRevenue: 0,
  totalRevenue: 0
})

const formatMoney = (value: number) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toFixed(2)
}

const fetchData = async () => {
  try {
    const res = await getDashboardData()
    Object.assign(dashboardData, res.data)
    await nextTick()
    initPieChart()
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  
  const chart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    series: [
      {
        name: '房间状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: dashboardData.freeRooms, name: '空闲', itemStyle: { color: '#67c23a' } },
          { value: dashboardData.reservedRooms, name: '已预订', itemStyle: { color: '#409eff' } },
          { value: dashboardData.occupiedRooms, name: '入住中', itemStyle: { color: '#e6a23c' } },
          { value: dashboardData.cleaningRooms, name: '待清洁', itemStyle: { color: '#909399' } },
          { value: dashboardData.maintenanceRooms, name: '维修中', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }
  
  chart.setOption(option)
  
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-cards {
    margin-bottom: 20px;
  }

  .stat-card {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 20px;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 16px;
    }

    .stat-info {
      flex: 1;
      
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #333;
        line-height: 1;
      }

      .stat-label {
        font-size: 14px;
        color: #999;
        margin-top: 8px;
      }
    }
    
    &.revenue-card {
      .stat-value {
        font-size: 24px;
      }
    }
  }

  .chart-row {
    .chart-container {
      height: 300px;
    }

    .room-status-list {
      padding: 20px;

      .status-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .status-dot {
          width: 12px;
          height: 12px;
          border-radius: 50%;
          margin-right: 12px;
        }

        .status-name {
          flex: 1;
          color: #666;
        }

        .status-count {
          font-size: 18px;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }
}
</style>
