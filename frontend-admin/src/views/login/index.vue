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
      <div class="login-tips">
        <p>测试账号：</p>
        <p>超级管理员：admin@phms.com / 123456</p>
        <p>店长：manager1@phms.com / 123456</p>
        <p>普通员工：staff1@phms.com / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

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
  margin-top: 20px;
  padding: 14px 16px;
  background-color: rgba(241, 245, 249, 0.1);
  border-radius: 10px;
  font-size: 12px;
  color: #475569;
  border: 1px solid rgba(208, 215, 224, 0.1);
  backdrop-filter: blur(2px);

  p {
    margin: 4px 0;
  }
}
</style>
