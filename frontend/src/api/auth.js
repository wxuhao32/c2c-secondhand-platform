import request from '@/utils/request'
import { getCaptchaKey } from '@/utils/storage'

/**
 * 获取图形验证码
 */
export function getCaptcha() {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}

/**
 * 账号密码登录
 * @param {Object} data - 登录参数
 * @param {string} data.account - 账号（用户名/手机/邮箱）
 * @param {string} data.password - 密码
 * @param {string} data.captchaKey - 验证码Key
 * @param {string} data.captchaCode - 验证码
 * @param {boolean} data.rememberMe - 记住我
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 发送短信验证码
 * @param {Object} data - 请求参数
 * @param {string} data.mobile - 手机号
 * @param {string} data.captchaKey - 图形验证码Key
 * @param {string} data.captchaCode - 图形验证码
 */
export function sendSms(data) {
  return request({
    url: '/auth/sendSms',
    method: 'post',
    data
  })
}

/**
 * 短信验证码登录
 * @param {Object} data - 登录参数
 * @param {string} data.mobile - 手机号
 * @param {string} data.smsCode - 短信验证码
 * @param {boolean} data.rememberMe - 记住我
 */
export function loginBySms(data) {
  return request({
    url: '/auth/loginBySms',
    method: 'post',
    data
  })
}

/**
 * 微信OAuth回调
 * @param {string} code - 微信授权码
 * @param {string} state - 状态参数
 */
export function wechatCallback(code, state) {
  return request({
    url: '/auth/wechat/callback',
    method: 'get',
    params: { code, state }
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册参数
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 登出
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/auth/currentUser',
    method: 'get'
  })
}
