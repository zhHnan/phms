import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 基础路由
const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录', hidden: true }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('@/layout/index.vue'),
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '工作台', icon: 'Odometer', perm: 'dashboard:view' }
            }
        ]
    },
    {
        path: '/hotel',
        name: 'Hotel',
        component: () => import('@/layout/index.vue'),
        redirect: '/hotel/list',
        meta: { title: '门店管理', icon: 'OfficeBuilding', perm: 'hotel:list' },
        children: [
            {
                path: 'list',
                name: 'HotelList',
                component: () => import('@/views/hotel/index.vue'),
                meta: { title: '门店列表', perm: 'hotel:list' }
            }
        ]
    },
    {
        path: '/staff',
        name: 'Staff',
        component: () => import('@/layout/index.vue'),
        redirect: '/staff/list',
        meta: { title: '员工管理', icon: 'User', perm: 'staff:list' },
        children: [
            {
                path: 'list',
                name: 'StaffList',
                component: () => import('@/views/staff/index.vue'),
                meta: { title: '员工列表', perm: 'staff:list' }
            }
        ]
    },
    {
        path: '/room',
        name: 'Room',
        component: () => import('@/layout/index.vue'),
        redirect: '/room/list',
        meta: { title: '房态管理', icon: 'House', perm: 'room:list' },
        children: [
            {
                path: 'list',
                name: 'RoomList',
                component: () => import('@/views/room/index.vue'),
                meta: { title: '房间列表', perm: 'room:list' }
            }
        ]
    },
    {
        path: '/order',
        name: 'Order',
        component: () => import('@/layout/index.vue'),
        redirect: '/order/list',
        meta: { title: '订单管理', icon: 'Tickets', perm: 'order:list' },
        children: [
            {
                path: 'list',
                name: 'OrderList',
                component: () => import('@/views/order/index.vue'),
                meta: { title: '订单列表', perm: 'order:list' }
            }
        ]
    },
    {
        path: '/product',
        name: 'Product',
        component: () => import('@/layout/index.vue'),
        redirect: '/product/list',
        meta: { title: '商品管理', icon: 'ShoppingCart', perm: 'product:list' },
        children: [
            {
                path: 'list',
                name: 'ProductList',
                component: () => import('@/views/product/index.vue'),
                meta: { title: '商品列表', perm: 'product:list' }
            }
        ]
    },
    {
        path: '/care',
        name: 'Care',
        component: () => import('@/layout/index.vue'),
        redirect: '/care/list',
        meta: { title: '护理日志', icon: 'DocumentCopy', perm: 'care:list' },
        children: [
            {
                path: 'list',
                name: 'CareList',
                component: () => import('@/views/care/index.vue'),
                meta: { title: '日志列表', perm: 'care:list' }
            }
        ]
    },
    {
        path: '/log',
        name: 'Log',
        component: () => import('@/layout/index.vue'),
        redirect: '/log/login',
        meta: { title: '日志管理', icon: 'Document', perm: 'log:login' },
        children: [
            {
                path: 'login',
                name: 'LoginLog',
                component: () => import('@/views/log/login.vue'),
                meta: { title: '登录日志', perm: 'log:login' }
            },
            {
                path: 'operation',
                name: 'OperationLog',
                component: () => import('@/views/log/operation.vue'),
                meta: { title: '操作日志', perm: 'log:operation' }
            }
        ]
    },
    {
        path: '/role',
        name: 'Role',
        component: () => import('@/layout/index.vue'),
        redirect: '/role/list',
        meta: { title: '角色权限', icon: 'UserFilled', perm: 'role:list' },
        children: [
            {
                path: 'list',
                name: 'RoleList',
                component: () => import('@/views/role/index.vue'),
                meta: { title: '角色权限管理', perm: 'role:list' }
            }
        ]
    },
    {
        path: '/message',
        name: 'Message',
        component: () => import('@/layout/index.vue'),
        redirect: '/message/list',
        meta: { title: '消息中心', icon: 'Bell' },
        children: [
            {
                path: 'list',
                name: 'MessageList',
                component: () => import('@/views/message/index.vue'),
                meta: { title: '消息中心' }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
        meta: { title: '404', hidden: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
    const userStore = useUserStore()

    if (to.path === '/login') {
        next()
        return
    }

    if (!userStore.token) {
        next('/login')
        return
    }

    // 权限校验
    const perm = to.meta.perm as string
    if (perm && !userStore.hasPermission(perm)) {
        next('/dashboard')
        return
    }

    next()
})

export default router
