<template>
  <transition name="fade">
    <div v-if="visible" class="fixed bottom-6 right-6 z-50 w-80 bg-white shadow-lg rounded-lg p-4 border border-gray-200 flex flex-col gap-2 animate__animated animate__fadeInUp">
      <div class="flex justify-between items-center mb-2">
        <span class="font-bold text-lg text-primary-600">测试账号</span>
        <button @click="close" class="text-gray-400 hover:text-gray-600">
          <span aria-hidden="true">×</span>
        </button>
      </div>
      <div class="space-y-1 text-sm">
        <div><span class="font-semibold">超级管理员：</span>admin@phms.com / 123456</div>
        <div><span class="font-semibold">店长：</span>manager1@phms.com / 123456</div>
        <div><span class="font-semibold">普通员工：</span>staff1@phms.com / 123456</div>
      </div>
    </div>
  </transition>
  <button v-if="!visible" @click="open" class="fixed bottom-6 right-6 z-50 bg-primary-600 text-white rounded-full shadow-lg p-3 hover:bg-primary-700 transition-all animate__animated animate__fadeInUp">
    测试账号
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const visible = ref(false)

function open() {
  visible.value = true
}
function close() {
  visible.value = false
}

// 登录后自动弹出
if (window.sessionStorage.getItem('phms-just-logged-in')) {
  visible.value = true
  window.sessionStorage.removeItem('phms-just-logged-in')
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
