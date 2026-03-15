<template>
  <div class="cart-page">
    <div class="cart-header">
      <h1>购物车</h1>
      <p v-if="itemCount > 0">共 {{ itemCount }} 件商品</p>
      <p v-else>购物车空空如也</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空购物车 -->
    <div v-else-if="itemCount === 0" class="empty-cart">
      <el-empty description="购物车空空如也">
        <el-button type="primary" @click="goToHome">去逛逛</el-button>
      </el-empty>
    </div>

    <!-- 购物车内容 -->
    <div v-else class="cart-content">
      <!-- 商品列表 -->
      <div class="cart-items">
        <div v-for="item in items" :key="item.id" class="cart-item-container">
          <router-link :to="`/product/${item.productId || item.id}`" class="cart-item-link">
            <div class="cart-item">
              <div class="item-info">
                <div class="item-image">
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </div>
                <div class="item-details">
                  <h3 class="item-title">{{ item.title || '商品' }}</h3>
                  <div class="item-price">¥{{ formatPrice(item.price) }}</div>
                </div>
              </div>

              <div class="item-actions">
                <div class="item-quantity">
                  数量: {{ item.quantity }}
                </div>

                <div class="item-total">
                  ¥{{ formatPrice(item.price * item.quantity) }}
                </div>
              </div>
            </div>
          </router-link>
          <el-button
            type="danger"
            :icon="Delete"
            circle
            size="small"
            @click.stop="removeItem(item.id)"
            class="remove-btn floating-btn"
          />
        </div>
      </div>

      <!-- 购物车汇总 -->
      <div class="cart-summary">
        <div class="summary-content">
          <div class="summary-row">
            <span class="label">商品总数</span>
            <span class="value">{{ itemCount }} 件</span>
          </div>
          <div class="summary-row total">
            <span class="label">总计</span>
            <span class="value total-price">¥{{ formatPrice(totalPrice) }}</span>
          </div>

          <div class="summary-actions">
            <el-button
              type="danger"
              plain
              :icon="Delete"
              @click="clearCart"
              :loading="clearing"
            >
              清空购物车
            </el-button>
            <el-button
              type="primary"
              :icon="ShoppingCart"
              @click="checkout"
              :loading="checkingOut"
              class="checkout-btn"
            >
              结算购物车
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Delete, ShoppingCart } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { storeToRefs } from 'pinia'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// 使用storeToRefs确保响应式
const { items, itemCount, totalPrice } = storeToRefs(cartStore)

// 状态
const loading = ref(true)
const clearing = ref(false)
const checkingOut = ref(false)


// 生命周期钩子
onMounted(async () => {
  await loadCart()
})

// 监听路由变化，当进入购物车页面时重新加载数据
watch(
  () => route.path,
  (newPath, oldPath) => {
    // 只有从其他页面导航到购物车页面时才重新加载
    if (newPath === '/cart' && oldPath !== '/cart') {
      loadCart()
    }
  }
)

// 加载购物车
const loadCart = async () => {
  loading.value = true
  try {
    await cartStore.fetchCart()
  } catch (error) {
    console.error('加载购物车失败:', error)
  } finally {
    loading.value = false
  }
}


// 移除商品
const removeItem = async (itemId) => {
  try {
    await ElMessageBox.confirm('确定要从购物车移除该商品吗？', '确认移除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await cartStore.removeItem(itemId)
    if (result.success) {
      ElMessage.success('商品已移除')
    } else {
      ElMessage.error(result.error || '移除失败')
    }
  } catch {
    // 用户取消
  }
}

// 清空购物车
const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    clearing.value = true
    const result = await cartStore.clearCart()
    if (result.success) {
      ElMessage.success('购物车已清空')
    } else {
      ElMessage.error(result.error || '清空失败')
    }
  } catch {
    // 用户取消
  } finally {
    clearing.value = false
  }
}

// 结算购物车
const checkout = async () => {
  try {
    checkingOut.value = true

    // 这里应该跳转到结算页面或调用结算API
    // 暂时模拟结算成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('结算成功！请继续完成订单')
    // 在实际项目中，这里应该跳转到订单确认页面
  } catch (error) {
    console.error('结算失败:', error)
    ElMessage.error('结算失败，请稍后重试')
  } finally {
    checkingOut.value = false
  }
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') return '0.00'
  const num = typeof price === 'string' ? parseFloat(price) : Number(price)
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

// 返回首页
const goToHome = () => {
  router.push('/')
}
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cart-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.cart-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}

.cart-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.loading-state {
  padding: 40px 0;
}

.empty-cart {
  padding: 80px 0;
  text-align: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-content {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.cart-items {
  flex: 1;
  min-width: 300px;
}

.cart-item-container {
  position: relative;
  margin-bottom: 15px;
}

.cart-item-link {
  text-decoration: none;
  color: inherit;
  display: block;
  cursor: pointer;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.cart-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.item-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.item-image {
  width: 80px;
  height: 80px;
  background: #f5f7fa;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.item-image .el-icon {
  font-size: 32px;
}

.item-details {
  flex: 1;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.cart-item-link:hover .item-title {
  color: #409eff;
}

.item-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-right: 50px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-control .el-button {
  width: 32px;
  height: 32px;
  min-width: auto;
  padding: 0;
}

.quantity {
  width: 40px;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.item-quantity {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  min-width: 80px;
}

.item-total {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
  min-width: 100px;
  text-align: right;
}

.remove-btn {
  color: #f56c6c;
  border-color: #f56c6c;
}

.remove-btn:hover {
  background-color: #f56c6c;
  color: white;
}

.floating-btn {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

.cart-summary {
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 25px;
  position: sticky;
  top: 20px;
}

.summary-content {
  margin-bottom: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row.total {
  border-top: 2px solid #e4e7ed;
  border-bottom: none;
  padding-top: 15px;
  margin-top: 10px;
}

.summary-row .label {
  font-size: 16px;
  color: #666;
}

.summary-row .value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.summary-row.total .value {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.summary-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkout-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-content {
    flex-direction: column;
  }

  .cart-summary {
    width: 100%;
    position: static;
  }

  .cart-item-container {
    position: relative;
  }

  .cart-item {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }

  .item-actions {
    justify-content: space-between;
    margin-right: 40px;
  }

  .floating-btn {
    position: absolute;
    right: 15px;
    top: 15px;
    transform: none;
  }
}
</style>