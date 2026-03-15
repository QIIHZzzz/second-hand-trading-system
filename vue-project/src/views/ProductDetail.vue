<template>
  <!-- 页面容器 -->
  <div class="product-detail-page">

    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products/list' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 商品详情内容 -->
    <div v-else class="detail-content">
      <!-- 商品图片区域 -->
      <div class="product-gallery">
        <!-- 主图 -->
        <div class="main-image">
          <div v-if="getMainImageUrl(product)" class="image-container">
            <img
              :src="getMainImageUrl(product)"
              :alt="product.title"
              class="main-img"
              @error="handleImageError"
            />
          </div>
          <div v-else class="image-placeholder">
            <el-icon><Picture /></el-icon>
            <span>商品主图</span>
          </div>
        </div>

        <!-- 缩略图（如果有的话） -->
        <div class="thumbnail-list" v-if="product.images && product.images.length > 0">
          <div
            v-for="(imageUrl, index) in product.images"
            :key="index"
            class="thumbnail"
            :class="{ active: activeImageIndex === index }"
            @click="setActiveImage(index)"
          >
            <div v-if="getThumbnailUrl(imageUrl)" class="thumbnail-image">
              <img
                :src="getThumbnailUrl(imageUrl)"
                :alt="`缩略图 ${index + 1}`"
                class="thumb-img"
                @error="handleThumbnailError"
              />
            </div>
            <div v-else class="thumbnail-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </div>
        </div>
        <div v-else class="thumbnail-list">
          <!-- 如果有单张图片 -->
          <div v-if="getFirstImageUrl(product)" class="thumbnail" :class="{ active: activeImageIndex === 0 }" @click="setActiveImage(0)">
            <div class="thumbnail-image">
              <img
                :src="getFirstImageUrl(product)"
                :alt="'商品图片'"
                class="thumb-img"
                @error="handleThumbnailError"
              />
            </div>
          </div>
          <!-- 如果没有图片，显示占位符 -->
          <div v-else v-for="i in 3" :key="i" class="thumbnail" :class="{ active: i === 1 }">
            <div class="thumbnail-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品信息区域 -->
      <div class="product-info">
        <!-- 商品标题和状态 -->
        <div class="product-header">
          <h1 class="product-title">{{ product.title }}</h1>
          <div class="product-status">
            <el-tag type="success" size="small" v-if="product.status === 1">上架中</el-tag>
            <el-tag type="info" size="small" v-else-if="product.status === 2">已售出</el-tag>
            <el-tag type="warning" size="small" v-else>已下架</el-tag>
          </div>
        </div>

        <!-- 价格区域 -->
        <div class="price-section">
          <div class="current-price">¥{{ product.price }}</div>
          <div class="original-price" v-if="product.originalPrice">
            原价：<span>¥{{ product.originalPrice }}</span>
          </div>
        </div>

        <!-- 商品属性表格 -->
        <div class="attributes-section">
          <h3>商品信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商品编号">
              {{ product.id }}
            </el-descriptions-item>
            <el-descriptions-item label="发布时间" v-if="product.createTime">
              {{ formatTime(product.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="卖家信息">
              用户: {{ product.sellerUsername || `用户${product.userId || 5}` }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮组 -->
        <div class="action-buttons">
          <el-button
            type="primary"
            size="large"
            @click="addToCart"
            class="cart-btn"
            :loading="addingToCart"
            :disabled="addingToCart"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ addingToCart ? '添加中...' : '加入购物车' }}
          </el-button>

          <el-button
            type="success"
            size="large"
            @click="buyNow"
            class="buy-btn"
          >
            <el-icon></el-icon>
            立即购买
          </el-button>

        </div>

        <!-- 联系卖家 -->
        <div class="contact-seller">
          <h3>联系卖家</h3>
          <div class="contact-options">
            <el-button
              type="primary"
              plain
              @click="contactSeller()"
              class="contact-btn"
            >
              <el-icon><Phone /></el-icon>
              拨打电话
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品描述区域 -->
    <div v-if="!loading" class="product-description">
      <h2>商品描述</h2>
      <div class="description-content">
        {{ product.description || '暂无描述' }}
      </div>

      <!-- 描述中的额外信息 -->
      <div class="description-extra" v-if="product.description">
        <el-divider />
        <h3>交易说明</h3>
        <ul class="trade-notes">
          <li>商品为个人闲置，非全新</li>
          <li>交易前请仔细检查商品</li>
          <li>售出后非质量问题不退不换</li>
        </ul>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productAPI } from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture,
  ShoppingCart,
  Phone
} from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

// 响应式数据
const product = ref({})
const loading = ref(true)
const addingToCart = ref(false)
const activeImageIndex = ref(0)

// 生命周期钩子
onMounted(async () => {
  await loadProductDetail()
})

// 加载商品详情
const loadProductDetail = async () => {
  loading.value = true
  try {
    const productId = route.params.id
    console.log('加载商品详情，商品ID:', productId)
    const response = await productAPI.getProduct(productId)
    console.log('商品详情API响应:', response)

    if (response.success) {
      product.value = response.data
      // 调试：打印商品图片信息
      console.log('商品图片信息:', {
        images: product.value.images,
        imageCount: product.value.images ? product.value.images.length : 0,
        hasImages: product.value.images && product.value.images.length > 0
      })

      // 模拟一些数据
      if (!product.value.viewCount) {
        product.value.viewCount = Math.floor(Math.random() * 1000) + 100
      }
      if (!product.value.createTime) {
        product.value.createTime = new Date().toISOString()
      }
      // 确保有sellerUsername字段
      if (!product.value.sellerUsername && product.value.userId) {
        product.value.sellerUsername = `用户${product.value.userId}`
      }
    } else {
      console.warn('商品详情API失败，使用模拟数据')
      // 如果API失败，使用模拟数据
      product.value = getMockProduct(productId)
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    product.value = getMockProduct(route.params.id)
  } finally {
    loading.value = false
  }
}

// 加载相关商品

// URL转换工具函数 - 将相对路径转换为绝对URL（与商品列表页保持一致）
const convertImageUrl = (imageUrl) => {
  if (!imageUrl || typeof imageUrl !== 'string') {
    return null
  }

  // 如果已经是完整的URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }

  // 如果是相对路径（以/开头），加上http://localhost:8080前缀
  if (imageUrl.startsWith('/')) {
    return `http://localhost:8080${imageUrl}`
  }

  // 其他格式的URL（非/开头，也不是完整URL），加上基础路径
  if (!imageUrl.includes('://')) {
    return `http://localhost:8080/uploads/${imageUrl}`
  }

  return null
}

// 获取商品的第一张图片URL，处理多种字段格式（与商品列表页保持一致）
const getFirstImageUrl = (product) => {
  if (!product) {
    return null
  }

  // 优先级1: images数组（可能是字符串数组或对象数组）
  if (product.images && Array.isArray(product.images) && product.images.length > 0) {
    // 获取第一个元素
    const firstImage = product.images[0]

    // 处理不同类型：可能是字符串或对象
    if (typeof firstImage === 'string') {
      const result = convertImageUrl(firstImage)
      if (result) return result
    } else if (firstImage && typeof firstImage === 'object') {
      // 如果是对象，尝试常见的属性名
      const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
      for (const field of possibleFields) {
        if (firstImage[field] && typeof firstImage[field] === 'string') {
          const result = convertImageUrl(firstImage[field])
          if (result) return result
        }
      }
    }
  }

  // 优先级2: image字段（字符串）
  if (product.image && typeof product.image === 'string') {
    const result = convertImageUrl(product.image)
    if (result) return result
  }

  // 优先级3: imageUrl字段（字符串）
  if (product.imageUrl && typeof product.imageUrl === 'string') {
    const result = convertImageUrl(product.imageUrl)
    if (result) return result
  }

  // 优先级4: picture字段（字符串）
  if (product.picture && typeof product.picture === 'string') {
    const result = convertImageUrl(product.picture)
    if (result) return result
  }

  // 优先级5: productImages数组（可能是字符串数组或对象数组）
  if (product.productImages && Array.isArray(product.productImages) && product.productImages.length > 0) {
    const firstImage = product.productImages[0]
    if (typeof firstImage === 'string') {
      const result = convertImageUrl(firstImage)
      if (result) return result
    } else if (firstImage && typeof firstImage === 'object') {
      const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
      for (const field of possibleFields) {
        if (firstImage[field] && typeof firstImage[field] === 'string') {
          const result = convertImageUrl(firstImage[field])
          if (result) return result
        }
      }
    }
  }

  return null
}

// 获取主图URL（支持多种图片字段格式和缩略图切换）
const getMainImageUrl = (product) => {
  if (!product) return null

  // 如果有images数组且不为空，使用活动索引的图片
  if (product.images && Array.isArray(product.images) && product.images.length > 0) {
    const imageIndex = activeImageIndex.value
    if (imageIndex >= 0 && imageIndex < product.images.length) {
      const imageItem = product.images[imageIndex]
      // 处理不同类型的图片项：字符串或对象
      if (typeof imageItem === 'string') {
        return convertImageUrl(imageItem)
      } else if (imageItem && typeof imageItem === 'object') {
        const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
        for (const field of possibleFields) {
          if (imageItem[field] && typeof imageItem[field] === 'string') {
            return convertImageUrl(imageItem[field])
          }
        }
      }
    }
    // 默认返回第一张图片
    const firstImage = product.images[0]
    if (typeof firstImage === 'string') {
      return convertImageUrl(firstImage)
    } else if (firstImage && typeof firstImage === 'object') {
      const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
      for (const field of possibleFields) {
        if (firstImage[field] && typeof firstImage[field] === 'string') {
          return convertImageUrl(firstImage[field])
        }
      }
    }
  }

  // 如果没有images数组，使用getFirstImageUrl获取单张图片
  return getFirstImageUrl(product)
}

// 获取缩略图URL
const getThumbnailUrl = (imageUrl) => {
  if (!imageUrl) return null

  // 如果是字符串，直接转换
  if (typeof imageUrl === 'string') {
    return convertImageUrl(imageUrl)
  }

  // 如果是对象，尝试常见的属性名
  if (imageUrl && typeof imageUrl === 'object') {
    const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
    for (const field of possibleFields) {
      if (imageUrl[field] && typeof imageUrl[field] === 'string') {
        return convertImageUrl(imageUrl[field])
      }
    }
  }

  return null
}

// 设置活动图片
const setActiveImage = (index) => {
  activeImageIndex.value = index
}

// 图片加载失败处理（主图）
const handleImageError = (event) => {
  retryImageLoad(event.target)
}

// 缩略图加载失败处理
const handleThumbnailError = (event) => {
  retryImageLoad(event.target)
}

// 通用图片加载失败处理
const retryImageLoad = (imgElement) => {
  const originalSrc = imgElement.src
  console.error('图片加载失败，原始URL:', originalSrc)

  // 直接设置透明图片，避免浏览器显示破碎图标
  imgElement.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48L3N2Zz4='
}

// 格式化时间
const formatTime = (timeString) => {
  const date = new Date(timeString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加入购物车
const addToCart = async () => {
  // 检查登录状态
  if (!authStore.checkAuth()) {
    ElMessage.warning('请先登录后再添加商品到购物车')
    router.push('/login')
    return
  }

  addingToCart.value = true
  try {
    const result = await cartStore.addItem(product.value)
    if (result.success) {
      ElMessage.success(`已添加 "${product.value.title}" 到购物车`)
    } else {
      ElMessage.error(result.error || '添加失败，请稍后重试')
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
    ElMessage.error('添加失败，请稍后重试')
  } finally {
    addingToCart.value = false
  }
}

// 立即购买
const buyNow = async () => {
  try {
    await ElMessageBox.confirm(
      `确认购买 "${product.value.title}" ？`,
      '确认购买',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('购买成功！请尽快联系卖家完成交易')
  } catch {
    // 用户取消
  }
}


// 联系卖家 - 拨打电话
const contactSeller = () => {
  ElMessage.info('拨打电话联系卖家')
  // TODO: 实现拨打电话逻辑
}


// 模拟商品数据
const getMockProduct = (id) => {
  const products = {
    1: {
      id: 1,
      title: 'iPhone 13 Pro 远峰蓝 256GB',
      price: 3999,
      originalPrice: 5999,
      location: '宿舍楼A区',
      description: '毕业出闲置，保护完好无任何划痕。手机为2022年购入，国行正品，全套配件齐全，包括原装充电器、数据线、包装盒。手机功能一切正常，无拆无修，电池健康度98%。因换新手机故出售，支持当面验机。',
      category: 'phone',
      status: 1,
      viewCount: 1234,
      createTime: '2024-01-15T10:30:00Z',
      userId: 5,
      sellerUsername: '张三'
    },
    2: {
      id: 2,
      title: '华为 Mate 50 Pro',
      price: 3599,
      originalPrice: 4999,
      location: '教学楼附近',
      description: '考研结束出手机，功能一切正常。手机为2023年购入，鸿蒙系统流畅，拍照效果优秀。屏幕无划痕，电池耐用。附带原装充电器和保护壳。',
      category: 'phone',
      status: 1,
      viewCount: 856,
      createTime: '2024-01-10T14:20:00Z',
      userId: 6,
      sellerUsername: '李四'
    }
  }
  return products[id] || {
    id,
    title: '商品加载失败',
    price: 0,
    location: '未知地点',
    description: '商品信息加载失败',
    category: 'other',
    status: 0,
    userId: 0,
    sellerUsername: '未知用户'
  }
}
</script>

<style scoped>
.product-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

.loading-state {
  padding: 40px 0;
}

/* 商品详情主要内容区 */
.detail-content {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.product-gallery {
  flex: 1;
  min-width: 300px;
}

.product-info {
  flex: 1;
  min-width: 300px;
}

/* 商品图片区域 */
.main-image {
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  border-radius: 8px;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  min-height: 300px;
}

.main-image .image-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.image-placeholder {
  text-align: center;
  color: #909399;
}

.image-placeholder .el-icon {
  font-size: 80px;
  display: block;
  margin-bottom: 10px;
  color: #c0c4cc;
}

.image-placeholder span {
  font-size: 16px;
  display: block;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.thumbnail {
  width: 60px;
  height: 60px;
  border: 2px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.thumbnail.active {
  border-color: #409eff;
}

.thumbnail-placeholder {
  color: #c0c4cc;
}

.thumbnail-placeholder .el-icon {
  font-size: 24px;
}

/* 商品信息区域 */
.product-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.product-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.price-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.current-price {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 8px;
}

.original-price {
  color: #999;
  font-size: 16px;
}

.original-price span {
  text-decoration: line-through;
}

/* 商品属性表格 */
.attributes-section {
  margin-bottom: 30px;
}

.attributes-section h3 {
  font-size: 18px;
  margin: 0 0 15px 0;
  color: #333;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
}

.cart-btn {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
}

.cart-btn:hover {
  background: linear-gradient(135deg, #66b1ff, #409eff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.buy-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border: none;
}

.buy-btn:hover {
  background: linear-gradient(135deg, #85ce61, #67c23a);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

/* 联系卖家 */
.contact-seller {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.contact-seller h3 {
  font-size: 18px;
  margin: 0 0 15px 0;
  color: #333;
}

.contact-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.contact-btn {
  flex: 1;
  min-width: 100px;
}

/* 商品描述 */
.product-description {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 40px;
}

.product-description h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.description-content {
  line-height: 1.8;
  color: #333;
  font-size: 15px;
  white-space: pre-wrap;
}

.description-extra {
  margin-top: 20px;
}

.description-extra h3 {
  font-size: 16px;
  color: #333;
  margin: 20px 0 10px 0;
}

.trade-notes {
  padding-left: 20px;
  color: #666;
  line-height: 1.6;
}

.trade-notes li {
  margin-bottom: 8px;
}




/* 响应式设计 */
@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
  }

  .action-buttons {
    flex-direction: column;
  }

  .contact-options {
    flex-direction: column;
  }

}
</style>
