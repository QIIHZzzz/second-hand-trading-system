import axios from 'axios'

const API_BASE = '/api'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理响应
api.interceptors.response.use(
  (response) => {
    // 根据您的API结构，返回data字段
    return response.data
  },
  (error) => {
    console.error('API请求错误:', error)

    // 处理401未授权错误
    if (error.response?.status === 401) {
      // 清除过期的token
      localStorage.removeItem('token')

      // 如果是浏览器环境，可以重定向到登录页
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        // 使用setTimeout避免在拦截器中直接导航
        setTimeout(() => {
          window.location.href = '/login?expired=true'
        }, 0)
      }
    }

    return Promise.reject(error)
  }
)

// 商品API
export const productAPI = {
  // 获取所有商品
  getAllProducts: () => api.get('/products'),

  // 获取商品详情
  getProduct: (id) => api.get(`/products/${id}`),

  // 创建商品
  createProduct: (productData) => api.post('/product', productData),

  // 更新商品
  updateProduct: (id, productData) => api.put(`/products/${id}`, productData),

  // 删除商品
  deleteProduct: (id) => api.delete(`/products/${id}`),

  // 按分类获取商品
  getProductsByCategory: (category) => api.get(`/products/category/${category}`),

  // 获取我的商品
  getMyProducts: () => api.get('/products/my'),

  // 点赞商品
  likeProduct: (id) => api.post(`/products/${id}/like`),

  // 下架商品
  disableProduct: (id) => api.post(`/products/${id}/disable`),

  // 上架商品
  enableProduct: (id) => api.post(`/products/${id}/enable`),

  // 切换商品状态
  toggleProductStatus: (id) => api.post(`/products/${id}/toggle-status`)
}

// 订单API
export const orderAPI = {
  // 获取我的订单
  getMyOrders: () => api.get('/orders/my'),

  // 获取订单详情
  getOrder: (id) => api.get(`/orders/${id}`),

  // 创建订单
  createOrder: (orderData) => api.post('/orders', orderData),

  // 取消订单
  cancelOrder: (id) => api.post(`/orders/${id}/cancel`)
}

export default api
