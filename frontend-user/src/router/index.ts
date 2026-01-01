import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: () => import('@/layout/index.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('@/views/home/index.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'rooms',
                name: 'Rooms',
                component: () => import('@/views/room/index.vue'),
                meta: { title: '房间列表' }
            },
            {
                path: 'rooms/:id',
                name: 'RoomDetail',
                component: () => import('@/views/room/detail.vue'),
                meta: { title: '房间详情' }
            },
            {
                path: 'booking/:roomId',
                name: 'Booking',
                component: () => import('@/views/booking/index.vue'),
                meta: { title: '预订', requiresAuth: true }
            },
            {
                path: 'pets',
                name: 'Pets',
                component: () => import('@/views/pet/index.vue'),
                meta: { title: '我的宠物', requiresAuth: true }
            },
            {
                path: 'orders',
                name: 'Orders',
                component: () => import('@/views/order/index.vue'),
                meta: { title: '我的订单', requiresAuth: true }
            },
            {
                path: 'orders/:id',
                name: 'OrderDetail',
                component: () => import('@/views/order/detail.vue'),
                meta: { title: '订单详情', requiresAuth: true }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/index.vue'),
                meta: { title: '个人中心', requiresAuth: true }
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
        meta: { title: '页面未找到' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        }
        return { top: 0 }
    }
})

router.beforeEach((to, from, next) => {
    document.title = `${to.meta.title || '宠物酒店'} - 宠物酒店预订服务`

    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
    } else {
        next()
    }
})

export default router
