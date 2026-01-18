<template>
  <div class="support-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>在线客服</span>
          <el-badge :value="waitingUsers.length" :hidden="waitingUsers.length === 0">
            <el-button @click="refreshWaitingList" :icon="Refresh" circle size="small" />
          </el-badge>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧：等待用户列表 -->
        <el-col :span="8">
          <div class="user-list">
            <h3>等待咨询的用户</h3>
            <el-empty v-if="waitingUsers.length === 0" description="暂无用户等待" />
            <div v-else class="user-items">
              <div
                v-for="user in waitingUsers"
                :key="user.userId"
                class="user-item"
                :class="{ active: currentUserId === user.userId }"
                @click="selectUser(user)"
              >
                <el-avatar :size="40">{{ user.userName?.[0] || 'U' }}</el-avatar>
                <div class="user-info">
                  <div class="name">{{ user.userName || '用户' + user.userId }}</div>
                  <div class="meta">
                    房间ID: {{ user.roomId || '未知' }}
                    <el-tag v-if="user.online === false" size="small" type="info" class="ml-2">离线</el-tag>
                  </div>
                </div>
                <el-badge v-if="user.unread > 0" :value="user.unread" />
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧：聊天窗口 -->
        <el-col :span="16">
          <div class="chat-window">
            <div v-if="!currentUserId" class="empty-chat">
              <el-empty description="请从左侧选择用户开始聊天" />
            </div>
            <div v-else class="chat-active">
              <div class="chat-header">
                <span>正在与 {{ currentUserName }} 聊天</span>
                <el-tag v-if="connected" type="success" size="small">已连接</el-tag>
                <el-tag v-else type="info" size="small">连接中...</el-tag>
              </div>

              <div class="chat-messages" ref="chatBody">
                <div v-if="messages.length === 0" class="no-messages">暂无消息</div>
                <div
                  v-for="(msg, idx) in messages"
                  :key="idx"
                  class="message"
                  :class="msg.from === 'me' ? 'message-sent' : 'message-received'"
                >
                  <div class="message-content">
                    <div class="text">{{ msg.text }}</div>
                    <div class="time">{{ formatChatTime(msg.ts) }}</div>
                  </div>
                </div>
              </div>

              <div class="chat-input">
                <el-input
                  v-model="chatInput"
                  @keyup.enter="sendMessage"
                  placeholder="输入消息..."
                  :disabled="!connected"
                />
                <el-button
                  type="primary"
                  @click="sendMessage"
                  :disabled="!connected || !chatInput.trim()"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'

interface WaitingUser {
  userId: number
  userName?: string
  roomId?: number
  unread: number
  online?: boolean
}

interface ChatMessage {
  from: 'me' | 'user'
  text: string
  ts: number
}

const userStore = useUserStore()

const waitingUsers = ref<WaitingUser[]>([])
const currentUserId = ref<number | null>(null)
const currentUserName = ref('')
const messages = ref<ChatMessage[]>([])
const chatInput = ref('')
const wsRef = ref<WebSocket | null>(null)
const connected = ref(false)
const chatBody = ref<HTMLElement | null>(null)
const currentRoom = ref<number | null>(null)
const currentHotel = ref<number | null>(null)

const formatChatTime = (ts: number) => {
  const d = new Date(ts)
  const h = `${d.getHours()}`.padStart(2, '0')
  const m = `${d.getMinutes()}`.padStart(2, '0')
  return `${h}:${m}`
}

const scrollChatToBottom = async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

const pushMessage = (from: 'me' | 'user', text: string, ts?: number) => {
  messages.value.push({ from, text, ts: ts ?? Date.now() })
  scrollChatToBottom()
}

const handleIncomingPayload = (payload: any) => {
  if (payload?.type === 'USER_ASSIGNED') {
    // 可在此处更新当前用户信息（如房间、酒店）
    if (payload.roomId) {
      currentRoom.value = payload.roomId
    }
    if (payload.hotelId) {
      currentHotel.value = payload.hotelId
    }
    const target = waitingUsers.value.find(u => u.userId === currentUserId.value)
    if (target) {
      target.online = true
    }
    return
  }
  if (payload?.type === 'HISTORY') {
    const history = Array.isArray(payload.messages) ? payload.messages : []
    history.forEach((msg: any) => {
      if (!msg) return
      const from = msg.sender === 'staff' ? 'me' : 'user'
      const text = msg.text || msg.message
      if (text) {
        const ts = msg.ts ? Number(msg.ts) : undefined
        pushMessage(from, String(text), ts)
      }
    })
    return
  }
  const text = payload?.text || payload?.message
  if (text) {
    pushMessage('user', String(text))
  }
}

const connectWs = (userId: number) => {
  const staffId = userStore.staffId || userStore.userId
  const token = localStorage.getItem('token') || ''
  const base = window.location.origin.replace(/^http/, 'ws')
  const url = `${base}/api/ws/chat?role=staff&staffId=${staffId}&userId=${userId}${token ? `&token=${encodeURIComponent(token)}` : ''}`
  console.log('Admin正在连接 WebSocket:', url)
  console.log('StaffId:', staffId, 'UserId:', userId)
  
  wsRef.value = new WebSocket(url)

  wsRef.value.onopen = () => {
    console.log('Admin WebSocket 连接成功')
    connected.value = true
  }

  wsRef.value.onmessage = (evt) => {
    console.log('Admin收到消息:', evt.data)
    try {
      const payload = JSON.parse(evt.data)
      handleIncomingPayload(payload)
    } catch {
      pushMessage('user', String(evt.data))
    }
  }

  wsRef.value.onclose = (evt) => {
    console.log('Admin WebSocket 关闭:', evt.code, evt.reason)
    connected.value = false
    const target = waitingUsers.value.find(u => u.userId === currentUserId.value)
    if (target) {
      target.online = false
    }
  }

  wsRef.value.onerror = (err) => {
    console.error('Admin WebSocket 错误:', err)
    connected.value = false
  }
}

const selectUser = (user: WaitingUser) => {
  if (currentUserId.value === user.userId) return
  
  // 关闭旧连接
  if (wsRef.value) {
    wsRef.value.close()
    wsRef.value = null
  }
  
  currentUserId.value = user.userId
  currentUserName.value = user.userName || '用户' + user.userId
   currentRoom.value = user.roomId || null
   currentHotel.value = (user as any).hotelId || null
  messages.value = []
  connected.value = false
  user.unread = 0
  
  connectWs(user.userId)
}

const sendMessage = () => {
  const text = chatInput.value.trim()
  if (!text || !wsRef.value || wsRef.value.readyState !== WebSocket.OPEN) return
  
  wsRef.value.send(JSON.stringify({ text }))
  pushMessage('me', text)
  chatInput.value = ''
}

const refreshWaitingList = async () => {
  try {
    console.log('刷新等待用户列表...')
    const res = await request.get('/support/waiting-users')
    console.log('等待用户列表响应:', res)
    if (res.data) {
      const list: WaitingUser[] = res.data.map((u: any) => ({
        userId: u.userId,
        userName: u.userName,
        roomId: u.roomId,
        hotelId: u.hotelId,
        unread: u.unread || 0,
        online: true
      }))

      // 如果当前正在聊天的用户不在新列表中，保留为离线状态
      if (currentUserId.value && !list.some(u => u.userId === currentUserId.value)) {
        list.push({
          userId: currentUserId.value,
          userName: currentUserName.value,
          roomId: currentRoom.value || undefined,
          hotelId: currentHotel.value || undefined,
          unread: 0,
          online: false
        })
      }

      waitingUsers.value = list
      console.log('已加载用户:', waitingUsers.value)
    }
  } catch (error: any) {
    console.error('获取等待用户列表失败:', error)
    ElMessage.error(error.message || '获取等待用户列表失败')
  }
}

onMounted(() => {
  refreshWaitingList()
  // 每10秒自动刷新一次
  const interval = setInterval(refreshWaitingList, 10000)
  
  onUnmounted(() => {
    clearInterval(interval)
    if (wsRef.value) {
      wsRef.value.close()
    }
  })
})

</script>

<style scoped>
.support-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-list {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.user-list h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.user-items {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.user-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-info .name {
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info .meta {
  font-size: 12px;
  color: #909399;
}

.chat-window {
  height: 600px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-active {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.no-messages {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.message {
  display: flex;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
}

.message-sent .message-content {
  background-color: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-received .message-content {
  background-color: white;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
}

.message-content .text {
  margin-bottom: 4px;
}

.message-content .time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
  background-color: white;
}
</style>
