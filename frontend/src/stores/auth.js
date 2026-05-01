import { defineStore } from 'pinia'
import { setToken, removeToken, getToken, setUserInfo, getUserInfo, removeUserInfo } from '@/utils/storage'
import { getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null,
    isLoggedIn: !!getToken()
  }),

  getters: {
    // 获取用户ID
    userId: (state) => state.userInfo?.userId,
    
    // 获取用户名
    username: (state) => state.userInfo?.username || state.userInfo?.mobile || '用户',
    
    // 获取用户头像
    avatar: (state) => state.userInfo?.avatar || '',
    
    // 判断是否已登录
    isAuthenticated: (state) => !!state.token,

    // 判断是否管理员
    isAdmin: (state) => state.userInfo?.role === 'ADMIN'
  },

  actions: {
    /**
     * 设置Token
     * @param {string} token - JWT令牌
     */
    setToken(token) {
      this.token = token
      this.isLoggedIn = true
      setToken(token)
    },

    /**
     * 设置用户信息
     * @param {Object} userInfo - 用户信息
     */
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    },

    /**
     * 初始化用户信息
     */
    async initUserInfo() {
      if (!this.token) return
      
      try {
        const response = await getCurrentUser()
        this.setUserInfo(response.data)
      } catch (error) {
        console.error('获取用户信息失败', error)
        // 如果获取失败，可能是Token过期，清除登录状态
        if (error.response?.status === 401) {
          this.logout()
        }
      }
    },

    /**
     * 登出
     */
    logout() {
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      removeToken()
      removeUserInfo()
    },

    /**
     * 更新用户信息中的特定字段
     * @param {Object} data - 需要更新的字段
     */
    updateUserInfo(data) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...data }
        setUserInfo(this.userInfo)
      }
    }
  }
})
