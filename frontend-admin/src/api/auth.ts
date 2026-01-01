import { request } from '@/utils/request'
import type { Result, StaffLoginDTO, StaffLoginVO } from './types'

/**
 * B端员工登录
 */
export const login = (data: StaffLoginDTO): Promise<Result<StaffLoginVO>> => {
    return request.post('/auth/staff/login', data)
}

/**
 * 登出
 */
export const logout = (): Promise<Result<void>> => {
    return request.post('/auth/logout')
}
