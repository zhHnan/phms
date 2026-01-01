import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout } from '@/api/auth'
import type { StaffLoginDTO, StaffLoginVO } from '@/api/types'

export const useUserStore = defineStore('user', () => {
    const token = ref<string>(localStorage.getItem('token') || '')
    const staffId = ref<number>(0)
    const email = ref<string>('')
    const realName = ref<string>('')
    const roleType = ref<number>(0)
    const hotelId = ref<number | null>(null)
    const hotelName = ref<string>('')
    const permissions = ref<string[]>([])

    // 初始化用户信息
    const initUserInfo = () => {
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
            const info = JSON.parse(userInfo)
            staffId.value = info.staffId
            email.value = info.email
            realName.value = info.realName
            roleType.value = info.roleType
            hotelId.value = info.hotelId
            hotelName.value = info.hotelName
            permissions.value = info.permissions || []
        }
    }

    // 登录
    const doLogin = async (dto: StaffLoginDTO): Promise<StaffLoginVO> => {
        const res = await login(dto)
        const data = res.data

        // 保存Token
        token.value = data.token
        localStorage.setItem('token', data.token)

        // 保存用户信息
        staffId.value = data.staffId
        email.value = data.email
        realName.value = data.realName
        roleType.value = data.roleType
        hotelId.value = data.hotelId
        hotelName.value = data.hotelName || ''
        permissions.value = data.permissions || []

        localStorage.setItem('userInfo', JSON.stringify({
            staffId: data.staffId,
            email: data.email,
            realName: data.realName,
            roleType: data.roleType,
            hotelId: data.hotelId,
            hotelName: data.hotelName,
            permissions: data.permissions
        }))

        return data
    }

    // 登出
    const doLogout = async () => {
        try {
            await logout()
        } finally {
            clearUserInfo()
        }
    }

    // 清除用户信息
    const clearUserInfo = () => {
        token.value = ''
        staffId.value = 0
        email.value = ''
        realName.value = ''
        roleType.value = 0
        hotelId.value = null
        hotelName.value = ''
        permissions.value = []
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    // 判断是否有权限
    const hasPermission = (perm: string): boolean => {
        if (permissions.value.includes('*')) return true
        return permissions.value.includes(perm)
    }

    // 是否为超管
    const isAdmin = computed(() => roleType.value === 9)

    // 是否为店长
    const isManager = computed(() => roleType.value === 2)

    // 初始化
    initUserInfo()

    return {
        token,
        staffId,
        email,
        realName,
        roleType,
        hotelId,
        hotelName,
        permissions,
        isAdmin,
        isManager,
        doLogin,
        doLogout,
        hasPermission,
        clearUserInfo
    }
})
