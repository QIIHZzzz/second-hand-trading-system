<template>
  <div class="product-list-page">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="products.length === 0" class="empty-state">
      <el-empty description="暂无商品" />
      <el-button type="primary" @click="goToPublish">去发布商品</el-button>
    </div>

    <!-- 商品网格 -->
    <div v-else class="products-grid">
      <el-row :gutter="20">
        <el-col
          v-for="product in products"
          :key="product.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="product-col"
        >
          <!-- 商品卡片 -->
          <el-card
            class="product-card"
            :body-style="{ padding: '0' }"
            shadow="hover"
            @click="viewProductDetail(product.id)"
          >
            <!-- 商品图片 -->
            <div class="product-image">
              <div v-if="getFirstImageUrl(product)" class="image-container">
                <img
                  :src="getFirstImageUrl(product)"
                  :alt="cleanTitle(product.title)"
                  class="product-img"
                  @error="handleImageError"
                />
              </div>
              <div v-else class="image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
            </div>

            <!-- 商品信息 -->
            <div class="product-info">
              <h3 class="product-title">{{ cleanTitle(product.title) }}</h3>

              <!-- 只有一个价格 -->
              <div class="price-info">
                <span class="current-price">¥{{ product.price }}</span>
              </div>


              <div class="product-actions">
                <el-button type="primary" size="small" @click.stop="addToCart(product)">
                  加入购物车
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  plain
                  @click.stop="viewProductDetail(product.id)"
                >
                  查看详情
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productAPI } from '@/services/api'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

// 响应式数据
const products = ref([])
const loading = ref(true)

// 生命周期钩子
onMounted(async () => {
  await loadProducts()
})

// 方法
const loadProducts = async () => {
  loading.value = true
  try {
    const response = await productAPI.getAllProducts()
    console.log('商品列表API响应原始数据:', JSON.parse(JSON.stringify(response)))

    if (response.success) {
      products.value = response.data || []

      // 调试：打印API返回的商品数量和结构
      console.log(`API返回${products.value.length}个商品，数据结构分析:`)
      if (products.value.length > 0) {
        // 打印第一个商品的完整结构（作为示例）
        const sampleProduct = products.value[0]
        console.log('第一个商品完整结构:', JSON.parse(JSON.stringify(sampleProduct)))

        // 打印所有商品的图片信息
        console.log('商品图片信息详细分析:')
        products.value.forEach((product, index) => {
          console.log(`商品 ${index + 1} [ID: ${product.id}]:`)
          console.log(`  标题: "${product.title}"`)
          console.log(`  价格: ${product.price}`)
          console.log(`  image字段:`, product.image)
          console.log(`  images字段:`, product.images)
          console.log(`  imageUrl字段:`, product.imageUrl)
          console.log(`  picture字段:`, product.picture)
          console.log(`  productImages字段:`, product.productImages)

          // 调用getFirstImageUrl来测试图片URL提取
          const imageUrl = getFirstImageUrl(product)
          console.log(`  getFirstImageUrl结果:`, imageUrl)
        })
      }
    } else {
      // 如果API失败，使用模拟数据
      console.warn('商品列表API失败，使用模拟数据')
      products.value = getMockProducts()
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    // 使用模拟数据
    products.value = getMockProducts()
  } finally {
    loading.value = false
  }
}

const viewProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const addToCart = async (product) => {
  // 检查登录状态
  if (!authStore.checkAuth()) {
    ElMessage.warning('请先登录后再添加商品到购物车')
    router.push('/login')
    return
  }

  try {
    const result = await cartStore.addItem(product)
    if (result.success) {
      ElMessage.success(`已添加 "${cleanTitle(product.title)}" 到购物车`)
    } else {
      ElMessage.error(result.error || '添加失败，请稍后重试')
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
    ElMessage.error('添加失败，请稍后重试')
  }
}

const goToPublish = () => {
  router.push('/products/publish')
}

// 获取商品的第一张图片URL，处理多种字段格式和相对路径转换
const getFirstImageUrl = (product) => {
  if (!product) {
    return null
  }

  // 定义一个辅助函数来处理单个图片URL
  const processImageUrl = (imageUrl) => {
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

  // 优先级1: images数组（可能是字符串数组或对象数组）
  if (product.images && Array.isArray(product.images) && product.images.length > 0) {
    // 获取第一个元素
    const firstImage = product.images[0]

    // 处理不同类型：可能是字符串或对象
    if (typeof firstImage === 'string') {
      const result = processImageUrl(firstImage)
      if (result) return result
    } else if (firstImage && typeof firstImage === 'object') {
      // 如果是对象，尝试常见的属性名
      const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
      for (const field of possibleFields) {
        if (firstImage[field] && typeof firstImage[field] === 'string') {
          const result = processImageUrl(firstImage[field])
          if (result) return result
        }
      }
    }
  }

  // 优先级2: image字段（字符串）
  if (product.image && typeof product.image === 'string') {
    const result = processImageUrl(product.image)
    if (result) return result
  }

  // 优先级3: imageUrl字段（字符串）
  if (product.imageUrl && typeof product.imageUrl === 'string') {
    const result = processImageUrl(product.imageUrl)
    if (result) return result
  }

  // 优先级4: picture字段（字符串）
  if (product.picture && typeof product.picture === 'string') {
    const result = processImageUrl(product.picture)
    if (result) return result
  }

  // 优先级5: productImages数组（可能是字符串数组或对象数组）
  if (product.productImages && Array.isArray(product.productImages) && product.productImages.length > 0) {
    const firstImage = product.productImages[0]
    if (typeof firstImage === 'string') {
      const result = processImageUrl(firstImage)
      if (result) return result
    } else if (firstImage && typeof firstImage === 'object') {
      const possibleFields = ['url', 'imageUrl', 'src', 'path', 'file', 'image']
      for (const field of possibleFields) {
        if (firstImage[field] && typeof firstImage[field] === 'string') {
          const result = processImageUrl(firstImage[field])
          if (result) return result
        }
      }
    }
  }

  return null
}

// 清理商品标题，移除末尾的时间戳（格式为 -数字）
const cleanTitle = (title) => {
  if (!title || typeof title !== 'string') {
    return title || ''
  }

  // 匹配末尾的 -数字 格式（包括 -数字 和 -数字.数字）
  const timestampPattern = /\s*-\s*\d+(\.\d+)?\s*$/

  if (timestampPattern.test(title)) {
    const cleanedTitle = title.replace(timestampPattern, '').trim()
    console.log(`清理标题: "${title}" -> "${cleanedTitle}"`)
    return cleanedTitle
  }

  return title
}

// 图片加载失败处理
const handleImageError = (event) => {
  const imgElement = event.target
  const originalSrc = imgElement.src
  console.error('图片加载失败，原始URL:', originalSrc)

  // 直接设置透明图片，避免浏览器显示破碎图标
  imgElement.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48L3N2Zz4='
}

// 模拟数据 - 测试多种图片字段格式和标题清理
const getMockProducts = () => {
  return [
    {
      id: 1,
      title: 'iPhone 13 Pro 远峰蓝 256GB-1772788224711', // 带时间戳的标题
      price: 3999,
      description: '毕业出闲置，保护完好无任何划痕',
      // 测试 images 数组（字符串数组）
      images: [
        'https://picsum.photos/300/200?random=1',
        'https://picsum.photos/300/200?random=2'
      ],
      image: null,
      imageUrl: null,
      picture: null
    },
    {
      id: 2,
      title: '华为 Mate 50 Pro-1772787977304', // 带时间戳的标题
      price: 3599,
      description: '考研结束出手机，功能一切正常',
      // 测试 image 字段（单张图片）
      image: 'https://picsum.photos/300/200?random=3',
      images: [],
      imageUrl: null,
      picture: null
    },
    {
      id: 3,
      title: 'MacBook Pro 2023 M2芯片', // 没有时间戳
      price: 8999,
      description: '因换新电脑出，适合编程设计',
      // 测试 imageUrl 字段
      imageUrl: 'https://picsum.photos/300/200?random=4',
      image: null,
      images: [],
      picture: null
    },
    {
      id: 4,
      title: '戴尔笔记本电脑-1772790000000', // 带时间戳的标题
      price: 3299,
      description: 'i7处理器，16G内存，适合办公学习',
      // 测试 picture 字段
      picture: 'https://picsum.photos/300/200?random=5',
      image: null,
      images: [],
      imageUrl: null
    },
    {
      id: 5,
      title: '考研英语真题集', // 没有时间戳
      price: 39,
      description: '近十年真题详解，包含答案解析',
      // 测试 images 数组（对象数组）
      images: [
        { url: 'https://picsum.photos/300/200?random=6', alt: '真题封面' },
        { src: 'https://picsum.photos/300/200?random=7', title: '内页示例' }
      ],
      image: null,
      imageUrl: null,
      picture: null
    },
    {
      id: 6,
      title: 'Java编程思想-1772788888888.12345', // 带小数时间戳的标题
      price: 69,
      description: '经典编程书籍，适合计算机专业学生',
      // 测试 productImages 字段（对象数组）
      productImages: [
        { imageUrl: 'https://picsum.photos/300/200?random=8', description: '封面' },
        { url: 'https://picsum.photos/300/200?random=9', description: '目录' }
      ],
      image: null,
      images: [],
      imageUrl: null,
      picture: null
    },
    {
      id: 7,
      title: '测试商品 - 没有图片字段',
      price: 100,
      description: '这个商品没有任何图片字段，用于测试默认情况',
      // 没有图片字段
    },
    {
      id: 8,
      title: '测试商品 - 相对路径图片',
      price: 200,
      description: '测试相对路径图片URL',
      // 测试相对路径
      image: '/uploads/test-image.jpg',
      images: [],
      imageUrl: null,
      picture: null
    }
  ]
}
</script>

<style scoped>
.product-list-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.loading-state {
  padding: 40px 0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  background: white;
  border-radius: 8px;
  margin-top: 20px;
}

.empty-state .el-button {
  margin-top: 20px;
}

.products-grid {
  margin-top: 20px;
}

.product-col {
  margin-bottom: 20px;
}

.product-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15) !important;
}

.product-image {
  position: relative;
  height: 180px;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.image-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px 4px 0 0;
}

.image-placeholder {
  text-align: center;
  color: #909399;
}

.image-placeholder .el-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
  color: #c0c4cc;
}

.image-placeholder span {
  font-size: 14px;
  display: block;
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  line-clamp: 2;      
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 44px;
}

.price-info {
  margin-bottom: 12px;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.product-meta {
  margin: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.meta-item .el-icon {
  font-size: 14px;
  color: #909399;
}

.product-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.product-actions .el-button {
  flex: 1;
  font-size: 12px;
  padding: 8px 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-col {
    margin-bottom: 16px;
  }
}
</style>
