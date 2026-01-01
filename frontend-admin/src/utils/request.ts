import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
service.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = token
        }
        return config
    },
    (error) => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data

        // 成功响应
        if (res.code === 200) {
            return res
        }

        // 未授权
        if (res.code === 401) {
            ElMessage.error('登录已过期，请重新登录')
            const userStore = useUserStore()
            userStore.clearUserInfo()
            router.push('/login')
            return Promise.reject(new Error(res.message || '未授权'))
        }

        // 其他错误 - 如果没有token，也应该重定向到登录
        const token = localStorage.getItem('token')
        if (!token) {
            ElMessage.error('请先登录')
            const userStore = useUserStore()
            userStore.clearUserInfo()
            router.push('/login')
            return Promise.reject(new Error(res.message || '请求失败'))
        }

        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    (error) => {
        console.error('响应错误:', error)

        // 处理网络错误或后端无响应
        if (!error.response) {
            // 网络错误或后端无响应
            ElMessage.error('后端服务未启动或网络连接失败，请稍后重试')
            const userStore = useUserStore()
            userStore.clearUserInfo()
            router.push('/login')
            return Promise.reject(error)
        }

        // 处理 HTTP 错误状态码
        const status = error.response.status

        // 401 未授权
        if (status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            const userStore = useUserStore()
            userStore.clearUserInfo()
            router.push('/login')
            return Promise.reject(error)
        }

        // 500 服务器内部错误 - 后端可能未正常启动
        if (status === 500) {
            ElMessage.error('后端服务异常，请稍后重试')
            const userStore = useUserStore()
            userStore.clearUserInfo()
            router.push('/login')
            return Promise.reject(error)
        }

        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

// 封装请求方法
export const request = {
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
        return service.get(url, config)
    },
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
        return service.post(url, data, config)
    },
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
        return service.put(url, data, config)
    },
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
        return service.delete(url, config)
    }
}

export default service
