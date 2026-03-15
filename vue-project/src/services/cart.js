import api from './api'

// 购物车API
export const cartAPI = {
  // 获取购物车列表
  getCartItems: () => api.get('/cart'),

  // 添加商品到购物车
  addToCart: (productId, quantity = 1) => api.post('/cart/items', { productId, quantity }),

  // 更新购物车商品数量
  updateCartItem: (itemId, quantity) => api.put(`/cart/items/${itemId}`, { quantity }),

  // 从购物车移除商品
  removeCartItem: (itemId) => api.delete(`/cart/items/${itemId}`),

  // 清空购物车
  clearCart: () => api.delete('/cart'),

  // 获取购物车商品数量
  getCartCount: () => api.get('/cart/count'),

  // 结算购物车（创建订单）
  checkout: (cartData) => api.post('/cart/checkout', cartData)
}