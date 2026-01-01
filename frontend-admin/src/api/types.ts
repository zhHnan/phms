// 通用响应结果
export interface Result<T> {
    code: number
    message: string
    data: T
    timestamp: number
}

// 分页结果
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 员工登录请求
export interface StaffLoginDTO {
    email: string
    password: string
}

// 员工登录结果
export interface StaffLoginVO {
    staffId: number
    email: string
    realName: string
    roleType: number
    hotelId: number | null
    hotelName: string | null
    permissions: string[]
    token: string
}

// 门店信息
export interface Hotel {
    id: number
    name: string
    code: string
    address: string
    managerName: string
    phone: string
    status: number
    createdAt: string
    updatedAt: string
}

// 员工信息
export interface Staff {
    id: number
    hotelId: number | null
    email: string
    realName: string
    roleType: number
    status: number
    lastLoginTime: string
    createdAt: string
}

// 房间信息
export interface Room {
    id: number
    hotelId: number
    roomNo: string
    typeName: string
    pricePerNight: number
    maxPetNum: number
    features: string
    status: number
    createdAt: string
}

// 订单信息
export interface Order {
    id: number
    orderNo: string
    hotelId: number
    userId: number
    petId: number
    roomId: number
    checkInDate: string
    checkOutDate: string
    totalAmount: number
    payTime: string | null
    status: number
    createdAt: string
}

// 护理日志
export interface CareLog {
    id: number
    orderId: number
    staffId: number
    careType: number
    content: string
    createdAt: string
}

// 登录日志
export interface LoginLog {
    id: number
    loginType: number
    userId: number | null
    staffId: number | null
    loginWay: number
    loginIp: string
    deviceInfo: string
    loginStatus: number
    failReason: string | null
    createdAt: string
}

// 操作日志
export interface OperationLog {
    id: number
    operatorType: number
    operatorId: number
    operatorName: string
    operationModule: string
    operationType: string
    operationParam: string
    operationResult: number
    failMsg: string | null
    operationIp: string
    operationUrl: string
    createdAt: string
}

// 仪表盘数据
export interface DashboardVO {
    todayOrders: number
    checkedInPets: number
    freeRooms: number
    reservedRooms: number
    occupiedRooms: number
    cleaningRooms: number
    maintenanceRooms: number
    totalRooms: number
}
