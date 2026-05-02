import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './storage'
import router from '@/router'

let serverErrorShown = false
let serverErrorTimer = null

const showServerErrorOnce = (msg, duration = 3000) => {
  if (serverErrorShown) return
  serverErrorShown = true
  ElMessage({
    message: msg,
    type: 'error',
    duration: duration,
    onClose: () => {
      serverErrorShown = false
    }
  })
  clearTimeout(serverErrorTimer)
  serverErrorTimer = setTimeout(() => {
    serverErrorShown = false
  }, 5000)
}

const ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '无权访问',
  404: '请求的资源不存在',
  429: '请求过于频繁，请稍后再试',
  500: '服务器内部错误',
  502: '服务暂时不可用',
  503: '服务暂时不可用',
  504: '请求超时，请稍后再试'
}

const BUSINESS_ERROR_GUIDES = {
  2001: { message: '账号不存在', action: '请检查账号是否正确，或注册新账号' },
  2002: { message: '密码错误', action: '请检查密码是否正确，或尝试找回密码' },
  2003: { message: '账户已被锁定', action: '请联系客服解锁' },
  2004: { message: '账户已被禁用', action: '请联系客服处理' },
  2005: { message: '验证码错误', action: '请重新输入验证码' },
  2006: { message: '验证码已过期', action: '请点击验证码图片刷新' },
  2007: { message: '请先获取验证码', action: '请点击验证码图片获取' },
  2015: { message: '登录失败次数过多', action: '请15分钟后再试' }
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === 200 || res.code === 201) {
      return res
    }

    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(res.message || '未授权'))
    }

    if (res.code === 403) {
      ElMessage.error('无权访问')
      return Promise.reject(new Error(res.message || '无权访问'))
    }

    if (res.code === 404) {
      ElMessage.error('资源不存在')
      return Promise.reject(new Error(res.message || '资源不存在'))
    }

    const businessGuide = BUSINESS_ERROR_GUIDES[res.code]
    if (businessGuide) {
      const fullMessage = `${businessGuide.message}，${businessGuide.action}`
      ElMessage.error(fullMessage)
      return Promise.reject(new Error(businessGuide.message))
    }

    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  (error) => {
    const config = error.config

    if (error.response) {
      const status = error.response.status
      const data = error.response.data

      if ([502, 503, 504].includes(status) && config && (!config._retryCount || config._retryCount < 2)) {
        config._retryCount = (config._retryCount || 0) + 1
        return new Promise(resolve => {
          setTimeout(() => resolve(service(config)), 3000 * config._retryCount)
        })
      }

      if (data && data.code && BUSINESS_ERROR_GUIDES[data.code]) {
        const guide = BUSINESS_ERROR_GUIDES[data.code]
        showServerErrorOnce(`${guide.message}，${guide.action}`)
        return Promise.reject(error)
      }

      const errorMessage = data?.message || ERROR_MESSAGES[status] || '请求失败'
      
      switch (status) {
        case 400:
          ElMessage.error(errorMessage)
          break
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 429:
          showServerErrorOnce('请求过于频繁，请稍后再试', 5000)
          break
        case 500:
        case 502:
        case 503:
        case 504:
          showServerErrorOnce(errorMessage + '，请稍后再试')
          break
        default:
          ElMessage.error(errorMessage)
      }
    } else if (error.code === 'ECONNABORTED') {
      showServerErrorOnce('请求超时，请检查网络后重试')
    } else if (error.request) {
      showServerErrorOnce('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default service
