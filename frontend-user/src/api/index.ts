import request from '@/utils/request'

// ==================== 酒店接口 ====================
export interface Hotel {
    id: number
    name: string
    code: string
    address: string
    managerName: string
    phone: string
    status: number
    images?: string  // 酒店图片URL列表（JSON格式）
    createdAt: string
    updatedAt: string
}

export interface Room {
    id: number
    roomNo: string
    typeName: string  // 后端返回的是 typeName
    typeNameDisplay?: string
    pricePerNight: number  // 后端返回的是 pricePerNight
    maxPetNum: number  // 后端返回的是 maxPetNum
    status: number
    description: string
    hotelId: number
    hotelName?: string
    hotelAddress?: string
    features?: string
    images?: string  // 房间图片URL列表（JSON格式）
}

export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 获取酒店列表
export const getHotelList = (): Promise<{ code: number; data: Hotel[] }> => {
    return request.get('/hotel/list')
}

// 获取顶级酒店（按评分排序）
export const getTopHotels = (limit: number = 5): Promise<{ code: number; data: Hotel[] }> => {
    return request.get('/hotel/top', { params: { limit } })
}

// 获取可用房间列表
export const getAvailableRooms = (params: {
    pageNum: number
    pageSize: number
    roomType?: string
    checkInDate?: string
    checkOutDate?: string
    hotelId?: number
    orderBy?: string
    orderDirection?: string
}): Promise<{ code: number; data: PageResult<Room> }> => {
    return request.get('/room/available', { params })
}

// 获取房间详情
export const getRoomDetail = (id: number): Promise<{ code: number; data: Room }> => {
    return request.get(`/room/${id}`)
}
