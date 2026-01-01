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
            } else {
                // 其他错误 - 如果没有token，也应该重定向到登录
                const token = localStorage.getItem('user_token')
                if (!token) {
                    const userStore = useUserStore()
                    userStore.logout()
                    window.location.href = '/login'
                }
            }
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    (error) => {
        console.error('请求错误:', error)

        // 处理网络错误或后端无响应
        if (!error.response) {
            // 网络错误或后端无响应
            console.error('后端服务未启动或网络连接失败')
            const userStore = useUserStore()
            userStore.logout()
            window.location.href = '/login'
            return Promise.reject(error)
        }

        // 处理 HTTP 错误状态码
        const status = error.response.status

        // 401 未授权
        if (status === 401) {
            const userStore = useUserStore()
            userStore.logout()
            window.location.href = '/login'
            return Promise.reject(error)
        }

        // 500 服务器内部错误 - 后端可能未正常启动
        if (status === 500) {
            console.error('后端服务异常')
            const userStore = useUserStore()
            userStore.logout()
            window.location.href = '/login'
            return Promise.reject(error)
        }

        return Promise.reject(error)
    }
)

export default request
