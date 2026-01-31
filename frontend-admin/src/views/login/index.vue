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
  background-color: #f0f2f5;
  background-image: radial-gradient(circle at 50% 50%, rgba(48, 65, 86, 0.05) 0%, transparent 100%);
  position: relative;
}

.login-box {
  width: 400px;
  padding: 40px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(48, 65, 86, 0.1);
  border: 1px solid #e4e7ed;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 12px;
    background-color: rgb(48, 65, 86);
    font-size: 32px;
    color: #ffffff;
    margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(48, 65, 86, 0.2);
  }

  h1 {
    font-size: 24px;
    color: #303133;
    margin: 0 0 8px;
    font-weight: 600;
  }

  p {
    color: #909399;
    font-size: 14px;
    margin: 0;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    &.is-focus {
      box-shadow: 0 0 0 1px rgb(48, 65, 86) inset !important;
    }
  }

  .el-form-item {
    margin-bottom: 24px;
  }

  .login-btn {
    width: 100%;
    height: 44px;
    border-radius: 4px;
    font-weight: 600;
    font-size: 16px;
    background: rgb(48, 65, 86);
    border-color: rgb(48, 65, 86);
    color: #ffffff;
    transition: all 0.3s;

    &:hover {
      background: rgb(60, 80, 105);
      border-color: rgb(60, 80, 105);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(48, 65, 86, 0.3);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// 敏捷卡片浮动样式
.agile-card-float {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 280px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(48, 65, 86, 0.15);
  z-index: 1002;
  padding: 0;
  border: 1px solid #e4e7ed;
  animation: floatIn .3s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.agile-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  border-bottom: 1px solid #f2f6fc;
}

.agile-card-content {
  padding: 16px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;

  p {
    margin: 0 0 8px;
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.close-icon {
  cursor: pointer;
  font-size: 16px;
  color: #909399;
  transition: all 0.2s;
  &:hover {
    color: #f56c6c;
  }
}

.agile-card-toggle {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 48px;
  height: 48px;
  background: rgb(48, 65, 86);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(48, 65, 86, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  font-size: 24px;
  color: #ffffff;
  transition: all 0.3s;

  &:hover {
    background: rgb(60, 80, 105);
    transform: scale(1.05) rotate(5deg);
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
