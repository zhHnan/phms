import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './styles/index.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useUserStore } from '@/stores/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 预先拉取用户信息，确保导航栏显示昵称
const userStore = useUserStore(pinia)
userStore.fetchUserInfo()

app.mount('#app')
