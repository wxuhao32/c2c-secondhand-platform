import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false, title: '用户登录' }
  },
  {
    path: '/login-sms',
    name: 'LoginSms',
    component: () => import('@/views/auth/LoginSms.vue'),
    meta: { requiresAuth: false, title: '短信登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false, title: '用户注册' }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/Index.vue'),
    meta: { requiresAuth: true, title: '首页' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/search/SearchResult.vue'),
    meta: { requiresAuth: false, title: '搜索结果' }
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/category/Category.vue'),
    meta: { requiresAuth: false, title: '商品分类' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/ProductDetail.vue'),
    meta: { requiresAuth: false, title: '商品详情' }
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('@/views/product/Publish.vue'),
    meta: { requiresAuth: true, title: '发布闲置' }
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: () => import('@/views/product/MyProducts.vue'),
    meta: { requiresAuth: true, title: '我的发布' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { requiresAuth: true, title: '个人中心' }
  },
  {
    path: '/security',
    name: 'Security',
    component: () => import('@/views/user/Security.vue'),
    meta: { requiresAuth: true, title: '账号安全' }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('@/views/user/Address.vue'),
    meta: { requiresAuth: true, title: '收货地址' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/user/Favorites.vue'),
    meta: { requiresAuth: true, title: '我的收藏' }
  },
  {
    path: '/my-orders',
    name: 'MyOrders',
    component: () => import('@/views/order/MyOrders.vue'),
    meta: { requiresAuth: true, title: '我的订单' }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/OrderDetail.vue'),
    meta: { requiresAuth: true, title: '订单详情' }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/chat/Messages.vue'),
    meta: { requiresAuth: true, title: '消息中心' }
  },
  {
    path: '/chat',
    name: 'ChatRoom',
    component: () => import('@/views/chat/ChatRoom.vue'),
    meta: { requiresAuth: true, title: '聊天' }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/notification/Notifications.vue'),
    meta: { requiresAuth: true, title: '站内消息' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '管理后台' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { requiresAuth: false, title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 闲置好物`
  }

  const authStore = useAuthStore()
  const token = authStore.token || localStorage.getItem('resale_platform_token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
  } else if ((to.name === 'Login' || to.name === 'LoginSms' || to.name === 'Register') && token) {
    next({ name: 'Home' })
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ name: 'Home' })
    ElMessage.error('无权访问管理后台')
  } else {
    next()
  }
})

router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router
