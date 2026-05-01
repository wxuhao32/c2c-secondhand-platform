import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './storage'
import router from '@/router'

let serverErrorShown = false
let serverErrorTimer = null

const showServerErrorOnce = (msg) => {
  if (serverErrorShown) return
  serverErrorShown = true
  ElMessage({
    message: msg,
    type: 'error',
    duration: 3000,
    onClose: () => {
      serverErrorShown = false
    }
  })
  clearTimeout(serverErrorTimer)
  serverErrorTimer = setTimeout(() => {
    serverErrorShown = false
  }, 5000)
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
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

    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  (error) => {
    if (error.config?._silent) {
      return Promise.reject(error)
    }

    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 400:
          ElMessage.error('请求参数错误')
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
        case 500:
        case 502:
        case 503:
        case 504:
          showServerErrorOnce('服务暂时不可用，请稍后再试')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      showServerErrorOnce('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default service
