<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  color: #fff;

  .error-code {
    font-size: 150px;
    font-weight: bold;
    line-height: 1;
    margin: 0;
    text-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  }

  .error-message {
    font-size: 24px;
    margin: 20px 0 40px;
    opacity: 0.9;
  }
}
</style>
