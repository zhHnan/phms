import { request } from '@/utils/request'
import type { Result, PageResult, Hotel, Staff, Room, Order, CareLog, LoginLog, OperationLog, DashboardVO, MessageCenter, MessageReadVO } from './types'

// ==================== 门店管理 ====================
export const getHotelPage = (params: any): Promise<Result<PageResult<Hotel>>> => {
    return request.get('/hotel/page', { params })
}

export const getHotelList = (): Promise<Result<Hotel[]>> => {
    return request.get('/hotel/list')
}

export const addHotel = (data: Partial<Hotel>): Promise<Result<void>> => {
    return request.post('/hotel', data)
}

export const updateHotel = (data: Partial<Hotel>): Promise<Result<void>> => {
    return request.put('/hotel', data)
}

export const deleteHotel = (id: number): Promise<Result<void>> => {
    return request.delete(`/hotel/${id}`)
}

// ==================== 员工管理 ====================
export const getStaffPage = (params: any): Promise<Result<PageResult<Staff>>> => {
    return request.get('/staff/page', { params })
}

export const addStaff = (data: any): Promise<Result<void>> => {
    return request.post('/staff', data)
}

export const updateStaff = (data: any): Promise<Result<void>> => {
    return request.put('/staff', data)
}

export const deleteStaff = (id: number): Promise<Result<void>> => {
    return request.delete(`/staff/${id}`)
}

export const updateStaffStatus = (id: number, status: number): Promise<Result<void>> => {
    return request.put(`/staff/${id}/status/${status}`)
}

// ==================== 房间管理 ====================
export const getRoomPage = (params: any): Promise<Result<PageResult<Room>>> => {
    return request.get('/room/page', { params })
}

export const getRoomList = (hotelId: number): Promise<Result<Room[]>> => {
    return request.get('/room/list', { params: { hotelId } })
}

export const addRoom = (data: Partial<Room>): Promise<Result<void>> => {
    return request.post('/room', data)
}

export const updateRoom = (data: Partial<Room>): Promise<Result<void>> => {
    return request.put('/room', data)
}

export const deleteRoom = (id: number): Promise<Result<void>> => {
    return request.delete(`/room/${id}`)
}

export const updateRoomStatus = (id: number, status: number): Promise<Result<void>> => {
    return request.put(`/room/${id}/status/${status}`)
}

// ==================== 订单管理 ====================
export const getOrderPage = (params: any): Promise<Result<PageResult<Order>>> => {
    return request.get('/order/page', { params })
}

export const checkIn = (id: number): Promise<Result<void>> => {
    return request.post(`/order/${id}/check-in`)
}

export const checkOut = (id: number): Promise<Result<void>> => {
    return request.post(`/order/${id}/check-out`)
}

// ==================== 护理日志 ====================
export const getCareLogPage = (params: any): Promise<Result<PageResult<CareLog>>> => {
    return request.get('/care-log/page', { params })
}

export const addCareLog = (data: Partial<CareLog>): Promise<Result<void>> => {
    return request.post('/care-log', data)
}

// ==================== 日志管理 ====================
export const getLoginLogPage = (params: any): Promise<Result<PageResult<LoginLog>>> => {
    return request.get('/log/login/page', { params })
}

export const getOperationLogPage = (params: any): Promise<Result<PageResult<OperationLog>>> => {
    return request.get('/log/operation/page', { params })
}

// ==================== 仪表盘 ====================
export const getDashboardData = (): Promise<Result<DashboardVO>> => {
    return request.get('/dashboard')
}

// ==================== 消息中心 ====================
export const getMessagePage = (params: any): Promise<Result<PageResult<MessageCenter>>> => {
    return request.get('/message/page', { params })
}

export const markMessageRead = (id: number): Promise<Result<MessageReadVO>> => {
    return request.put(`/message/${id}/read`)
}

export const markAllMessagesRead = (): Promise<Result<number>> => {
    return request.put('/message/read-all')
}
