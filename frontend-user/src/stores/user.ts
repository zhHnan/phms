import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

interface UserInfo {
    id: number
    nickname: string
    phone: string
    avatar: string
}

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('user_token') || '')
    const userInfo = ref<UserInfo | null>(null)

    const isLoggedIn = computed(() => !!token.value)

    const setToken = (newToken: string) => {
        token.value = newToken
        localStorage.setItem('user_token', newToken)
    }

    const setUserInfo = (info: UserInfo) => {
        userInfo.value = info
    }

    const login = async (phone: string, code: string) => {
        const res = await request.post('/auth/user/login', { phone, code })
        setToken(res.data.token)
        setUserInfo(res.data.userInfo)
        return res
    }

    const sendCode = async (phone: string) => {
        return await request.post('/auth/user/send-code', { phone })
    }

    const logout = () => {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('user_token')
    }

    const fetchUserInfo = async () => {
        if (!token.value) return
        try {
            const res = await request.get('/auth/user/info')
            setUserInfo(res.data)
        } catch (error) {
            logout()
        }
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        login,
        sendCode,
        logout,
        fetchUserInfo
    }
})
