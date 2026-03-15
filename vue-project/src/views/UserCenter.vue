<template>
  <div class="user-center">
    <!-- 顶部导航栏 -->
    <div class="top-navbar">
      <div class="nav-content">
        <h2>个人中心</h2>
      </div>
    </div>

    <div class="main-container">
      <!-- 左侧导航栏 -->
      <div class="sidebar">
        <el-menu
          :default-active="activeTab"
          class="side-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="my-products">
            <el-icon><Goods /></el-icon>
            <span>我的发布</span>
          </el-menu-item>
          <el-menu-item index="my-orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区 -->
      <div class="content">
        <!-- 个人信息 -->
        <div v-if="activeTab === 'profile'" class="tab-content profile-tab">
          <div class="tab-header">
            <h3>个人信息</h3>
            <el-button
              v-if="!isEditing"
              type="primary"
              size="small"
              @click="startEditing"
            >
              编辑信息
            </el-button>
            <div v-else class="edit-actions">
              <el-button type="success" size="small" @click="saveProfile">
                保存
              </el-button>
              <el-button type="info" size="small" @click="cancelEditing">
                取消
              </el-button>
            </div>
          </div>

          <div class="profile-content">
            <!-- 查看模式 -->
            <div v-if="!isEditing" class="view-mode">
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ userInfo.username || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ userInfo.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ userInfo.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">注册时间：</span>
                <span class="value">{{ formatDate(userInfo.createdAt) }}</span>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-else class="edit-mode">
              <el-form
                :model="editForm"
                :rules="formRules"
                ref="profileFormRef"
                label-width="100px"
                class="edit-form"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input
                    v-model="editForm.username"
                    placeholder="请输入用户名"
                    maxlength="20"
                  />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input
                    v-model="editForm.email"
                    placeholder="请输入邮箱"
                    type="email"
                  />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input
                    v-model="editForm.phone"
                    placeholder="请输入手机号"
                    maxlength="11"
                  />
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>

        <!-- 我的发布 -->
        <div v-else-if="activeTab === 'my-products'" class="tab-content products-tab">
          <div class="tab-header">
            <h3>我的发布</h3>
            <el-button type="primary" @click="goToPublish">
              <el-icon><Plus /></el-icon>
              发布新商品
            </el-button>
          </div>

          <div v-if="myProductsLoading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>

          <div v-else-if="myProducts.length === 0" class="empty-state">
            <el-empty description="暂无发布的商品" />
            <el-button type="primary" @click="goToPublish">去发布商品</el-button>
          </div>

          <div v-else class="products-list">
            <el-table :data="myProducts" style="width: 100%">
              <el-table-column prop="title" label="商品标题" width="300" />
              <el-table-column prop="price" label="价格" width="120">
                <template #default="{ row }">
                  ¥{{ row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag
                    :type="row.status === '上架' ? 'success' : 'info'"
                    size="small"
                  >
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="发布时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    size="small"
                    @click="editProduct(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="toggleProductStatus(row)"
                  >
                    {{ row.status === '上架' ? '下架' : '上架' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 我的订单 -->
        <div v-else-if="activeTab === 'my-orders'" class="tab-content orders-tab">
          <div class="tab-header">
            <h3>我的订单</h3>
          </div>

          <div v-if="myOrdersLoading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>

          <div v-else-if="myOrders.length === 0" class="empty-state">
            <el-empty description="暂无订单" />
          </div>

          <div v-else class="orders-list">
            <el-table :data="myOrders" style="width: 100%">
              <el-table-column prop="orderId" label="订单号" width="200" />
              <el-table-column prop="productTitle" label="商品" width="300" />
              <el-table-column prop="price" label="价格" width="120">
                <template #default="{ row }">
                  ¥{{ row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="totalAmount" label="总金额" width="120">
                <template #default="{ row }">
                  ¥{{ row.totalAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag
                    :type="getOrderStatusType(row.status)"
                    size="small"
                  >
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="下单时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    size="small"
                    @click="viewOrderDetail(row)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Goods, Document, Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { productAPI, orderAPI } from '@/services/api'
import { authAPI } from '@/services/auth'

const router = useRouter()
const authStore = useAuthStore()

// 当前激活的标签页
const activeTab = ref('profile')

// 用户信息相关
const userInfo = ref({})
const isEditing = ref(true)
const editForm = reactive({
  username: '',
  email: '',
  phone: ''
})
const profileFormRef = ref()

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 我的发布相关
const myProducts = ref([])
const myProductsLoading = ref(false)

// 我的订单相关
const myOrders = ref([])
const myOrdersLoading = ref(false)

// 初始化加载数据
onMounted(async () => {
  await loadUserInfo()
  await loadMyProducts()
  await loadMyOrders()
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 从store获取用户信息
    const user = authStore.user
    if (user) {
      userInfo.value = {
        username: user.username || '',
        email: user.email || '',
        phone: user.phone || '',
        createdAt: user.createdAt || new Date().toISOString()
      }

      // 初始化编辑表单
      editForm.username = userInfo.value.username
      editForm.email = userInfo.value.email
      editForm.phone = userInfo.value.phone
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 加载我的发布
const loadMyProducts = async () => {
  myProductsLoading.value = true
  try {
    const response = await productAPI.getMyProducts()
    if (response.success) {
      myProducts.value = response.data || []
    } else {
      ElMessage.error(response.error || '获取我的发布失败')
    }
  } catch (error) {
    console.error('加载我的发布失败:', error)
    ElMessage.error('加载我的发布失败')
  } finally {
    myProductsLoading.value = false
  }
}

// 加载我的订单
const loadMyOrders = async () => {
  myOrdersLoading.value = true
  try {
    const response = await orderAPI.getMyOrders()
    if (response.success) {
      myOrders.value = response.data || []
    } else {
      ElMessage.error(response.error || '获取我的订单失败')
    }
  } catch (error) {
    console.error('加载我的订单失败:', error)
    ElMessage.error('加载我的订单失败')
  } finally {
    myOrdersLoading.value = false
  }
}

// 菜单选择
const handleMenuSelect = (index) => {
  activeTab.value = index
}

// 开始编辑个人信息
const startEditing = () => {
  isEditing.value = true
}

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false
  // 恢复原始数据
  editForm.username = userInfo.value.username
  editForm.email = userInfo.value.email
  editForm.phone = userInfo.value.phone
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    // 验证表单
    await profileFormRef.value.validate()

    // 调用API更新用户信息
    const updateData = {
      username: editForm.username,
      email: editForm.email,
      phone: editForm.phone
      // 注意：不传递用户ID，后端应该从token中获取当前用户
    }

    const response = await authStore.updateProfile(updateData)

    if (response.success) {
      // 更新本地用户信息
      userInfo.value.username = editForm.username
      userInfo.value.email = editForm.email
      userInfo.value.phone = editForm.phone

      isEditing.value = false
      ElMessage.success('个人信息更新成功')
    } else {
      ElMessage.error(response.error || '更新个人信息失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新个人信息失败')
  }
}

// 编辑商品
const editProduct = (product) => {
  // 跳转到商品编辑页面
  router.push(`/product/edit/${product.id}`)
}

// 切换商品状态（上架/下架）
const toggleProductStatus = async (product) => {
  try {
    await ElMessageBox.confirm(
      `确定要${product.status === '上架' ? '下架' : '上架'}该商品吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API更新商品状态
    product.status = product.status === '上架' ? '下架' : '上架'

    ElMessage.success(`商品已${product.status === '上架' ? '上架' : '下架'}`)
  } catch {
    // 用户取消
  }
}

// 查看订单详情
const viewOrderDetail = (order) => {
  // TODO: 跳转到订单详情页面
  ElMessage.info(`查看订单 ${order.orderId} 详情`)
}

// 获取订单状态标签类型
const getOrderStatusType = (status) => {
  const statusMap = {
    '待付款': 'warning',
    '待发货': 'info',
    '已发货': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 跳转到发布商品页面
const goToPublish = () => {
  router.push('/products/publish')
}
</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.top-navbar {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
}

.nav-content h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.main-container {
  display: flex;
  min-height: calc(100vh - 60px);
}

.sidebar {
  width: 200px;
  background: white;
  border-right: 1px solid #e4e7ed;
}

.side-menu {
  border-right: none;
}

.side-menu :deep(.el-menu-item) {
  height: 56px;
  line-height: 56px;
  font-weight: 500;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.tab-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.tab-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.profile-content {
  max-width: 600px;
}

.info-item {
  display: flex;
  margin-bottom: 16px;
  font-size: 16px;
}

.info-item .label {
  width: 100px;
  color: #666;
  font-weight: 500;
}

.info-item .value {
  flex: 1;
  color: #333;
}

.edit-form {
  max-width: 500px;
}

.loading-state {
  padding: 40px 0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  border-radius: 8px;
  background: #fafafa;
}

.empty-state .el-button {
  margin-top: 20px;
}

.products-list,
.orders-list {
  margin-top: 20px;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }

  .content {
    padding: 16px;
  }

  .tab-content {
    padding: 16px;
  }

  .info-item {
    flex-direction: column;
  }

  .info-item .label {
    width: auto;
    margin-bottom: 4px;
  }
}
</style>