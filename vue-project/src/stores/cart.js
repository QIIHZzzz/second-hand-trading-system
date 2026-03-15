import { defineStore } from 'pinia'
import { cartAPI } from '@/services/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    totalPrice: 0,
    itemCount: 0,
    loading: false
  }),

  getters: {
    // 计算购物车总价
    cartTotal: (state) => {
      return state.items.reduce((total, item) => {
        return total + (item.price * item.quantity)
      }, 0)
    },

    // 计算购物车商品总数
    cartCount: (state) => {
      return state.items.reduce((count, item) => count + item.quantity, 0)
    },

    // 检查商品是否在购物车中
    isInCart: (state) => (productId) => {
      return state.items.some(item => item.productId === productId)
    },

    // 获取购物车中某个商品的数量
    getItemQuantity: (state) => (productId) => {
      const item = state.items.find(item => item.productId === productId)
      return item ? item.quantity : 0
    }
  },

  actions: {
    // 获取购物车列表
    async fetchCart() {
      this.loading = true
      try {
        const response = await cartAPI.getCartItems()

        // 确保items是数组
        const items = Array.isArray(response) ? response : (response.items || response.data || [])

        // 处理商品数据，确保字段类型正确
        const processedItems = items.map(item => {

          const processedItem = {
            id: item.id || item._id,
            productId: item.productId || item.product_id || item.id,
            title: item.title || item.name || item.productTitle || '商品',
            price: typeof item.price === 'string' ? parseFloat(item.price) : Number(item.price) || Number(item.priceAtAdd) || 0,
            quantity: Number(item.quantity) || 1,
            image: item.image || item.img || item.productImageUrl || ''
          }
          return processedItem
        })

        this.items = processedItems
        this.updateTotals()

        return { success: true, data: response }
      } catch (error) {
        console.error('获取购物车失败:', error)
        return { success: false, error: error.message || '获取购物车失败' }
      } finally {
        this.loading = false
      }
    },

    // 添加商品到购物车
    async addItem(product, quantity = 1) {
      try {
        const response = await cartAPI.addToCart(product.id, quantity)

        // 为了确保状态一致性，重新获取购物车数据
        await this.fetchCart()
        return { success: true, data: response }
      } catch (error) {
        console.error('添加商品到购物车失败:', error)
        return { success: false, error: error.message || '添加商品到购物车失败' }
      }
    },

    // 更新购物车商品数量
    async updateQuantity(itemId, quantity) {
      if (quantity <= 0) {
        return await this.removeItem(itemId)
      }

      try {
        await cartAPI.updateCartItem(itemId, quantity)
        // 重新获取购物车数据以确保状态一致
        await this.fetchCart()
        return { success: true }
      } catch (error) {
        console.error('更新商品数量失败:', error)
        return { success: false, error: error.message || '更新商品数量失败' }
      }
    },

    // 从购物车移除商品
    async removeItem(itemId) {
      try {
        await cartAPI.removeCartItem(itemId)
        // 重新获取购物车数据以确保状态一致
        await this.fetchCart()
        return { success: true }
      } catch (error) {
        console.error('移除商品失败:', error)
        return { success: false, error: error.message || '移除商品失败' }
      }
    },

    // 清空购物车
    async clearCart() {
      try {
        await cartAPI.clearCart()
        // 重新获取购物车数据以确保状态一致
        await this.fetchCart()
        return { success: true }
      } catch (error) {
        console.error('清空购物车失败:', error)
        return { success: false, error: error.message || '清空购物车失败' }
      }
    },

    // 结算购物车
    async checkout(orderData) {
      try {
        const response = await cartAPI.checkout(orderData)
        // 结算成功后清空购物车
        await this.clearCart()
        return { success: true, data: response }
      } catch (error) {
        console.error('结算失败:', error)
        return { success: false, error: error.message || '结算失败' }
      }
    },

    // 更新购物车统计信息
    updateTotals() {
      this.totalPrice = this.cartTotal
      this.itemCount = this.cartCount
    }
  },

  // 持久化购物车状态到localStorage
  persist: {
    key: 'cart-store',
    storage: localStorage,
    paths: ['items']
  }
})