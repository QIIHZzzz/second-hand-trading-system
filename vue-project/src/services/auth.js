import api from './api'

// 用户认证API
export const authAPI = {
  // 用户登录
  login: (credentials) => api.post('/auth/login', credentials),

  // 用户注册
  register: (userData) => api.post('/auth/register', userData),

  // 获取当前用户信息
  getCurrentUser: () => api.get('/auth/me'),

  // 退出登录
  logout: () => api.post('/auth/logout'),

  // 更新用户信息
  updateProfile: (userData) => api.put('/auth/profile', userData)
}