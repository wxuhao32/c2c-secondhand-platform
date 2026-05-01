/**
 * 存储工具模块
 * 统一管理Token和用户信息的存储
 */

const TOKEN_KEY = 'resale_platform_token'
const USER_INFO_KEY = 'resale_platform_user_info'
const CAPTCHA_KEY = 'resale_platform_captcha_key'

/**
 * 获取Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 * @param {string} token - JWT令牌
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * @param {Object} userInfo - 用户信息对象
 */
export function setUserInfo(userInfo) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取验证码Key
 */
export function getCaptchaKey() {
  return localStorage.getItem(CAPTCHA_KEY)
}

/**
 * 设置验证码Key
 * @param {string} key - 验证码Key
 */
export function setCaptchaKey(key) {
  localStorage.setItem(CAPTCHA_KEY, key)
}

/**
 * 清除所有存储
 */
export function clearAll() {
  localStorage.clear()
}

/**
 * 检查是否已登录
 */
export function isAuthenticated() {
  return !!getToken()
}
