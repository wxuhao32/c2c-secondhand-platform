import request from '@/utils/request'

export function getUserInfo() {
  return request({ url: '/user/info', method: 'get' })
}

export function updateUserInfo(data) {
  return request({ url: '/user/info', method: 'put', data })
}

export function updatePassword(data) {
  return request({ url: '/user/password', method: 'put', data })
}

export function updateAvatar(data) {
  return request({ url: '/user/avatar', method: 'post', data })
}

export function getAddressList() {
  return request({ url: '/user/address', method: 'get' })
}

export function addAddress(data) {
  return request({ url: '/user/address', method: 'post', data })
}

export function updateAddress(id, data) {
  return request({ url: `/user/address/${id}`, method: 'put', data })
}

export function deleteAddress(id) {
  return request({ url: `/user/address/${id}`, method: 'delete' })
}

export function setDefaultAddress(id) {
  return request({ url: `/user/address/${id}/default`, method: 'put' })
}

export function getFavorites(params) {
  return request({ url: '/user/favorites', method: 'get', params })
}

export function addFavorite(productId) {
  return request({ url: `/user/favorites/${productId}`, method: 'post' })
}

export function removeFavorite(productId) {
  return request({ url: `/user/favorites/${productId}`, method: 'delete' })
}

export function getUserStats() {
  return request({ url: '/user/stats', method: 'get' })
}
