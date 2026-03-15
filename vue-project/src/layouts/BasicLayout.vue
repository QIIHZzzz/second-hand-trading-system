<template>
  <el-container class="basic-layout">
    <!-- 左侧导航栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>二手交易平台</h2>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeIndex"
        class="side-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="2">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/products/list"> 商品列表 </el-menu-item>
          <el-menu-item index="/products/publish"> 发布商品 </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/cart">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车</span>
        </el-menu-item>

        <el-menu-item index="/about">
          <el-icon><InfoFilled /></el-icon>
          <span>关于我们</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-content">
          <!-- 搜索区域 -->
          <div class="search-area">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品名称..."
              size="large"
              style="width: 440px"
            >
              <template #append>
                <el-button :icon="Search" />
              </template>
            </el-input>


            <el-button type="primary" size="large" :icon="Search">
              搜索
            </el-button>
          </div>

          <!-- 右侧用户操作 -->
          <div class="user-actions">
            <template v-if="authStore.isAuthenticated">
              <el-dropdown>
                <span class="user-info">
                  <span class="username">{{ authStore.user?.username }}</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                    <el-dropdown-item @click="handleLogout" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" :icon="UserFilled" @click="handleLogin">
                登录/注册
              </el-button>
            </template>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区 -->
      <el-main class="content">
        <!-- 这里将显示路由对应的页面 -->
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, Goods, InfoFilled, Search, UserFilled, ShoppingCart } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeIndex = ref(route.path)
const searchKeyword = ref('')


const handleLogin = () => {
  // 跳转到登录页面
  router.push('/login')
}

const handleLogout = async () => {
  await authStore.logout()
  // 退出后刷新页面以确保状态同步
  window.location.reload()
}

const handleProfile = () => {
  // 跳转到个人中心页面
  router.push('/user-center')
}

</script>

<style scoped>
.basic-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  border-right: 1px solid #e6e6e6;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background-color: #263445;
  border-bottom: 1px solid #404854;
}

.logo h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.side-menu {
  border-right: none;
}

.side-menu :deep(.el-sub-menu__title) {
  font-weight: 600;
}

.side-menu :deep(.el-menu-item) {
  font-weight: 500;
}

.header {
  padding: 0;
  height: 72px;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.search-area {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.content {
  padding: 24px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.cart-badge {
  margin-left: 8px;
}
</style>
