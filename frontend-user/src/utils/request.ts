import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
    baseURL: '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('user_token')
        if (token) {
            config.headers['Authorization'] = token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code !== 200) {
            // 业务错误
            if (res.code === 401) {
                const userStore = useUserStore()
                userStore.logout()
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    (error) => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

export default request
