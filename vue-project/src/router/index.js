import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import ProductList from '@/views/ProductList.vue'
import ProductDetail from '@/views/ProductDetail.vue'
import AboutPage from '@/views/AboutPage.vue'
import PublishProduct from '@/views/PublishProduct.vue'
import LoginPage from '@/views/LoginPage.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import UserCenter from '@/views/UserCenter.vue'

const routes = [
  {
    path: '/',
    component: BasicLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: ProductList,
      },
      {
        path: 'products/list',
        name: 'ProductList',
        component: ProductList,
      },
      {
        path: 'products/publish',
        name: 'PublishProduct',
        component: PublishProduct,
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: ProductDetail,
      },
      {
        path: 'about',
        name: 'About',
        component: AboutPage,
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/CartPage.vue'),
      },
      {
        path: 'user-center',
        name: 'UserCenter',
        component: UserCenter,
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterPage,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 需要认证的路由
const authRequiredRoutes = ['PublishProduct', 'Cart', 'UserCenter']

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 在守卫内部导入store以确保pinia已注册
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()

  const requiresAuth = authRequiredRoutes.includes(to.name)

  if (requiresAuth && !authStore.isAuthenticated) {
    // 需要认证但未登录，重定向到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && authStore.isAuthenticated) {
    // 已登录用户访问登录/注册页，重定向到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
