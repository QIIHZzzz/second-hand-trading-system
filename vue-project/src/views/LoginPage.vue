<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>登录</h1>
        <p>欢迎回到二手交易平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    loading.value = true

    // 调用登录API
    const result = await authStore.login(loginForm)

    if (result.success) {
      ElMessage.success('登录成功！')
      // 跳转到首页
      router.push('/')
    } else {
      // 尝试从响应数据中提取更具体的错误信息
      let errorMessage = result.error || '登录失败'
      if (result.data) {
        const response = result.data
        if (response.password && Array.isArray(response.password)) {
          errorMessage = response.password[0] || '密码错误'
        } else if (response.errors) {
          // 提取第一个错误
          const errorFields = Object.keys(response.errors)
          if (errorFields.length > 0) {
            const firstError = response.errors[errorFields[0]]
            if (Array.isArray(firstError) && firstError.length > 0) {
              errorMessage = firstError[0]
            } else if (typeof firstError === 'string') {
              errorMessage = firstError
            }
          }
        } else if (response.error) {
          errorMessage = response.error
        } else if (response.message) {
          errorMessage = response.message
        }
      }
      ElMessage.error(errorMessage)
    }
  } catch (error) {
    console.error('登录错误:', error)
    // 显示具体的错误信息
    let errorMessage = '登录失败，请检查输入'

    // 优先使用error.message（来自authStore.login抛出的错误）
    if (error.message) {
      errorMessage = error.message
    } else if (error.response?.data) {
      // 如果是HTTP错误响应，尝试提取错误信息
      const errorData = error.response.data
      if (errorData.password && Array.isArray(errorData.password)) {
        errorMessage = errorData.password[0] || '密码错误'
      } else if (errorData.errors) {
        // 提取第一个错误
        const errorFields = Object.keys(errorData.errors)
        if (errorFields.length > 0) {
          const firstError = errorData.errors[errorFields[0]]
          if (Array.isArray(firstError) && firstError.length > 0) {
            errorMessage = firstError[0]
          } else if (typeof firstError === 'string') {
            errorMessage = firstError
          }
        }
      } else if (errorData.error) {
        errorMessage = errorData.error
      } else if (errorData.message) {
        errorMessage = errorData.message
      }
    }
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 72px); /* 减去顶部栏高度 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.login-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.login-form .el-form-item {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.login-footer .el-link {
  margin-left: 8px;
  font-size: 14px;
}
</style>