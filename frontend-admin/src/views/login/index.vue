<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <span class="logo">🐾</span>
        <h1>宠物之家管理平台</h1>
        <p>后台登录</p>
      </div>
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <!-- 敏捷卡片浮动按钮（已移至容器外部，避免被输入框覆盖） -->
    </div>
    <transition name="fade">
        <div v-if="showCard" class="agile-card-float">
          <div class="agile-card-header">
            <span>测试账号</span>
            <el-icon class="close-icon" @click="showCard = false"><Close /></el-icon>
          </div>
          <div class="agile-card-content">
            <p>超级管理员：admin@phms.com / 123456</p>
            <p>店长：manager1@phms.com / 123456</p>
            <p>普通员工：staff1@phms.com / 123456</p>
          </div>
        </div>
      </transition>
    <transition name="fade">
      <div v-if="!showCard" class="agile-card-toggle" @click="showCard = true">
        <el-icon><InfoFilled /></el-icon>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

import { Close, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const showCard = ref(true)

const loginForm = reactive({
  email: 'admin@phms.com',
  password: '123456'
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    await userStore.doLogin(loginForm)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.3), rgba(224, 231, 255, 0.35)),
    url('https://images.unsplash.com/photo-1507146426996-ef05306b995a?q=80&w=1600&auto=format&fit=crop') center/cover no-repeat;
  position: relative;
}

.login-box {
  width: 420px;
  padding: 44px 40px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(30, 41, 59, 0.12);
  backdrop-filter: blur(2px);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  .logo {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: linear-gradient(135deg, #e0e7ff, #dbeafe);
    font-size: 34px;
  }

  h1 {
    font-size: 26px;
    color: #0f172a;
    margin: 10px 0 5px;
    font-weight: 700;
  }

  p {
    color: #475569;
    font-size: 14px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  .login-btn {
    width: 100%;
    height: 44px;
    border-radius: 10px;
    font-weight: 600;
    background: linear-gradient(135deg,rgba(38, 52, 69, 0.7), rgba(43, 60, 82, 0.7));
    border: 1px solid rgba(255, 255, 255, 0.35);
    backdrop-filter: blur(2px);
  }
}

.login-tips {
  // ...原有内容已移除，仅保留占位，防止未闭合错误
}

// 敏捷卡片浮动样式
.agile-card-float {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 260px;
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(30,41,59,0.13);
  z-index: 1002;
  padding: 0 0 10px 0;
  border: 1px solid #e0e7ef;
  animation: floatIn .3s;
}
.agile-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px 0 16px;
  font-weight: 600;
  color: #334155;
  font-size: 15px;
}
.agile-card-content {
  padding: 8px 16px 0 16px;
  font-size: 13px;
  color: #475569;
}
.close-icon {
  cursor: pointer;
  font-size: 18px;
  color: #64748b;
  transition: color 0.2s;
}
.close-icon:hover {
  color: #f87171;
}
.agile-card-toggle {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #e0e7ff, #dbeafe);
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(30,41,59,0.13);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  font-size: 26px;
  color: #475569;
  border: 1px solid #e0e7ef;
  transition: background 0.2s;
}
.agile-card-toggle:hover {
  background: linear-gradient(135deg, #c7d2fe, #e0e7ff);
}
@keyframes floatIn {
  from { transform: translateY(40px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
