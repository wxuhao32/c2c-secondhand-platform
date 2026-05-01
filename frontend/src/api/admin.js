import request from '@/utils/request'

export function getDashboard() {
  return request({ url: '/admin/dashboard', method: 'get' })
}

export function getUserList(params) {
  return request({ url: '/admin/users', method: 'get', params })
}

export function updateUserStatus(id, status) {
  return request({ url: `/admin/users/${id}/status`, method: 'put', data: { status } })
}

export function updateUserRole(id, role) {
  return request({ url: `/admin/users/${id}/role`, method: 'put', data: { role } })
}

export function getGoodsList(params) {
  return request({ url: '/admin/goods', method: 'get', params })
}

export function updateGoodsStatus(id, status) {
  return request({ url: `/admin/goods/${id}/status`, method: 'put', data: { status } })
}

export function deleteGoods(id) {
  return request({ url: `/admin/goods/${id}`, method: 'delete' })
}

export function getCommentList() {
  return request({ url: '/admin/comments', method: 'get' })
}

export function deleteComment(id) {
  return request({ url: `/admin/comments/${id}`, method: 'delete' })
}

export function getOrderList() {
  return request({ url: '/admin/orders', method: 'get' })
}

export function sendNotification(data) {
  return request({ url: '/admin/notify', method: 'post', data })
}

export function getRecentSmsCodes() {
  return request({ url: '/auth/sms-codes', method: 'get' })
}
