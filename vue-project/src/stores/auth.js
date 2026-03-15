import { defineStore } from 'pinia'
import { authAPI } from '@/services/auth'

// 简单的JWT解码工具函数（只解码payload部分）
function decodeJWT(token) {
  if (!token) return null
  try {
    // JWT格式: header.payload.signature
    const parts = token.split('.')
    if (parts.length !== 3) return null

    // Base64解码payload
    const payload = parts[1]
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decodedPayload)
  } catch (error) {
    console.warn('JWT解码失败:', error)
    return null
  }
}

// 从token中提取用户信息
function extractUserFromToken(token) {
  const payload = decodeJWT(token)
  if (!payload) return null

  // 尝试从payload中提取用户信息
  // 常见JWT字段: sub (subject/user id), username, name, email, user_id, etc.
  const userInfo = {
    id: payload.sub || payload.user_id || payload.id || Date.now(),
    username: payload.username || payload.name || payload.preferred_username || `用户${payload.sub || '未知'}`,
    name: payload.name || payload.username || payload.preferred_username || `用户${payload.sub || '未知'}`,
    email: payload.email || ''
  }

  return userInfo
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = localStorage.getItem('token') || null
    const userStr = localStorage.getItem('user')
    let user = null
    if (userStr) {
      try {
        user = JSON.parse(userStr)
      } catch (error) {
        console.error('解析localStorage用户信息失败:', error)
        localStorage.removeItem('user')
      }
    }
    return {
      user: user,
      token: token
    }
  },

  getters: {
    // 检查是否已认证
    isAuthenticated: (state) => !!state.token,
    // 获取用户ID
    userId: (state) => state.user?.id,
    // 获取用户名
    username: (state) => state.user?.username,
    // 获取用户信息
    userInfo: (state) => state.user
  },

  actions: {
    // 用户登录
    async login(credentials) {
      try {
        const response = await authAPI.login(credentials)

        // 提取token，支持多种可能的字段名
        let token = null
        if (response.token) {
          token = response.token
        } else if (response.access_token) {
          token = response.access_token
        } else if (response.accessToken) {
          token = response.accessToken
        } else if (response.data?.token) {
          token = response.data.token
        } else if (response.data?.access_token) {
          token = response.data.access_token
        }

        // 检查响应是否包含错误信息
        // 1. 首先检查明确的success字段（如果存在）
        if (response.success === false) {
          // 如果success为false，即使有其他字段也视为错误
          let errorMessage = '登录失败'
          // 尝试提取错误信息
          if (response.password && Array.isArray(response.password)) {
            errorMessage = response.password[0] || '密码错误'
          } else if (response.errors) {
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
          throw new Error(errorMessage)
        }

        // 2. 检查其他错误格式（即使success字段不存在或为true也要检查）
        let errorMessage = null

        if (response.password && Array.isArray(response.password)) {
          // 格式: {password: ['错误消息1', '错误消息2']}
          errorMessage = response.password[0] || '密码错误'
        } else if (response.errors) {
          // 格式: {errors: {field: ['错误消息'], ...}}
          // 提取第一个错误
          const errorFields = Object.keys(response.errors)
          if (errorFields.length > 0) {
            const firstError = response.errors[errorFields[0]]
            if (Array.isArray(firstError) && firstError.length > 0) {
              errorMessage = firstError[0]
            } else if (typeof firstError === 'string') {
              errorMessage = firstError
            } else {
              errorMessage = '验证错误'
            }
          }
        } else if (response.error) {
          // 格式: {error: '错误消息'}
          errorMessage = response.error
        } else if (response.message && !token) {
          // 格式: {message: '错误消息'} 且没有token（成功响应可能有message但没有token）
          // 如果同时有token和message，可能是成功消息
          if (!token) {
            errorMessage = response.message
          }
        }

        // 如果没有token，也视为错误（除非后端使用其他字段名）
        if (!token && !errorMessage) {
          errorMessage = '登录失败：未收到认证令牌'
        }

        // 如果检测到错误，抛出异常以便进入catch块
        if (errorMessage) {
          throw new Error(errorMessage)
        }

        this.token = token
        localStorage.setItem('token', token)

        // 从响应中提取用户信息，支持多种可能的字段名和嵌套结构
        let userData = null

        // 检查各种可能的字段位置
        if (response.user) {
          userData = response.user
        } else if (response.userInfo) {
          userData = response.userInfo
        } else if (response.data) {
          // 如果响应结构是 { data: { user: ... } }
          if (response.data.user) {
            userData = response.data.user
          } else if (response.data.userInfo) {
            userData = response.data.userInfo
          } else {
            // 如果data本身就是用户信息
            userData = response.data
          }
        } else if (response.username) {
          // 如果响应直接包含用户名字段
          userData = response
        }

        if (userData) {
          // 确保用户信息有必要的字段
          if (!userData.username && credentials.username) {
            userData.username = credentials.username
          }
          if (!userData.id) {
            userData.id = Date.now() // 临时ID
          }

          this.setUser(userData)
        } else {
          console.warn('登录响应中没有找到用户信息，响应结构详情:')
          console.warn('response:', JSON.stringify(response, null, 2))

          // 如果API没有返回用户信息，使用登录凭据创建基本用户对象
          const fallbackUser = {
            id: Date.now(),
            username: credentials.username || '用户',
            name: credentials.username || '用户'
          }
          this.setUser(fallbackUser)
        }

        return { success: true, data: response }
      } catch (error) {
        console.error('登录失败:', error)
        console.error('错误详情:', error.response?.data || error.message)
        return { success: false, error: error.message || '登录失败' }
      }
    },

    // 用户注册
    async register(userData) {
      try {
        const response = await authAPI.register(userData)

        // 提取token，支持多种可能的字段名
        let token = null
        if (response.token) {
          token = response.token
        } else if (response.access_token) {
          token = response.access_token
        } else if (response.accessToken) {
          token = response.accessToken
        } else if (response.data?.token) {
          token = response.data.token
        } else if (response.data?.access_token) {
          token = response.data.access_token
        }

        // 检查响应是否包含错误信息
        // 1. 首先检查明确的success字段（如果存在）
        if (response.success === false) {
          // 如果success为false，即使有其他字段也视为错误
          let errorMessage = '注册失败'
          // 尝试提取错误信息
          if (response.errors) {
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
          throw new Error(errorMessage)
        }

        // 2. 检查其他错误格式（即使success字段不存在或为true也要检查）
        let errorMessage = null

        if (response.errors) {
          // 格式: {errors: {field: ['错误消息'], ...}}
          // 提取第一个错误
          const errorFields = Object.keys(response.errors)
          if (errorFields.length > 0) {
            const firstError = response.errors[errorFields[0]]
            if (Array.isArray(firstError) && firstError.length > 0) {
              errorMessage = firstError[0]
            } else if (typeof firstError === 'string') {
              errorMessage = firstError
            } else {
              errorMessage = '验证错误'
            }
          }
        } else if (response.error) {
          // 格式: {error: '错误消息'}
          errorMessage = response.error
        }

        // 注册成功后不一定需要token，可能用户需要单独登录
        // 所以不强制检查token是否存在

        // 如果检测到错误，抛出异常以便进入catch块
        if (errorMessage) {
          throw new Error(errorMessage)
        }

        // 只有当token存在时才设置token和用户信息
        if (token) {
          this.token = token
          localStorage.setItem('token', token)

          // 从响应中提取用户信息，支持多种可能的字段名和嵌套结构
          let userResponseData = null

          // 检查各种可能的字段位置
          if (response.user) {
            userResponseData = response.user
          } else if (response.userInfo) {
            userResponseData = response.userInfo
          } else if (response.data) {
            // 如果响应结构是 { data: { user: ... } }
            if (response.data.user) {
              userResponseData = response.data.user
            } else if (response.data.userInfo) {
              userResponseData = response.data.userInfo
            } else {
              // 如果data本身就是用户信息
              userResponseData = response.data
            }
          } else if (response.username) {
            // 如果响应直接包含用户名字段
            userResponseData = response
          }

          if (userResponseData) {
            // 确保用户信息有必要的字段
            if (!userResponseData.username && userData.username) {
              userResponseData.username = userData.username
            }
            if (!userResponseData.id) {
              userResponseData.id = Date.now() // 临时ID
            }

            this.setUser(userResponseData)
          } else {
            console.warn('注册响应中没有找到用户信息，响应结构详情:')
            console.warn('response:', JSON.stringify(response, null, 2))

            // 如果API没有返回用户信息，使用注册数据创建基本用户对象
            const fallbackUser = {
              id: Date.now(),
              username: userData.username || '用户',
              name: userData.username || '用户',
              email: userData.email || ''
            }
            this.setUser(fallbackUser)
          }
        }


        return { success: true, data: response }
      } catch (error) {
        console.error('注册失败:', error)
        console.error('错误详情:', error.response?.data || error.message)
        return { success: false, error: error.message || '注册失败' }
      }
    },

    // 初始化认证状态
    async init() {

      if (this.token) {
        // 如果有token，但用户信息为空，尝试从localStorage恢复
        if (!this.user) {
          const userStr = localStorage.getItem('user')
          if (userStr) {
            try {
              const parsedUser = JSON.parse(userStr)
              // 调用setUser确保字段一致性和完整性
              this.setUser(parsedUser)
            } catch (error) {
              console.error('init: 解析localStorage用户信息失败:', error)
              localStorage.removeItem('user')
            }
          } else {
            // 尝试从token中解码用户信息
            try {
              const userFromToken = extractUserFromToken(this.token)
              if (userFromToken) {
                this.setUser(userFromToken)
              } else {
                console.warn('init: 无法从token解码用户信息，token可能不是JWT或格式不正确')
                // 如果无法从token解码，创建默认用户信息
                const defaultUser = {
                  id: Date.now(),
                  username: '用户',
                  name: '用户'
                }
                this.setUser(defaultUser)
              }
            } catch (error) {
              console.error('init: 从token解码用户信息时发生错误:', error)
            }
          }
        } else {
          // 调用setUser确保字段一致性和完整性，并保存到localStorage
          this.setUser(this.user)
        }
      } else {
      }
    },

    // 设置token
    setToken(token) {
      this.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },

    // 清除认证状态
    clearAuth() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    // 设置用户信息
    setUser(user) {
      if (!user) {
        this.user = null
        localStorage.removeItem('user')
        return
      }

      // 确保必要字段有值
      const userData = {
        id: user.id || Date.now(),
        // 优先使用username，如果没有则使用name，再没有则使用id或默认值
        username: user.username || user.name || `用户${user.id || Date.now()}`,
        name: user.name || user.username || `用户${user.id || Date.now()}`,
        email: user.email || '',
        avatar: user.avatar || ''
      }

      // 合并数据，确保所有必要字段都有值
      // userData在后，这样规范化后的字段会覆盖user中的null/undefined/空值
      const finalUser = { ...user, ...userData }
      this.user = finalUser

      localStorage.setItem('user', JSON.stringify(userData))
    },

    // 退出登录
    async logout() {
      try {
        await authAPI.logout()
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        this.clearAuth()
      }
    },

    // 获取当前用户信息（已弃用 - 后端API可能不存在）
    async fetchUser() {
      console.warn('fetchUser已弃用，后端可能没有/auth/me接口，返回缓存的用户信息')
      if (!this.token) return { success: false, error: '未登录' }

      // 返回当前store中的用户信息，不调用API
      if (this.user) {
        return {
          success: true,
          data: { user: this.user },
          message: '使用缓存的用户信息'
        }
      } else {
        return {
          success: false,
          error: '没有缓存的用户信息',
          message: '用户信息为空，请重新登录'
        }
      }
    },

    // 更新用户信息
    async updateProfile(userData) {
      try {
        const response = await authAPI.updateProfile(userData)
        this.setUser({ ...this.user, ...response.user })
        return { success: true, data: response }
      } catch (error) {
        console.error('更新用户信息失败:', error)
        return { success: false, error: error.message || '更新用户信息失败' }
      }
    },

    // 检查是否已登录
    checkAuth() {
      return this.isAuthenticated && this.token
    }
  }
})