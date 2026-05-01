import request from '@/utils/request'

export function getOrderList(params) {
  return request({ url: '/orders', method: 'get', params })
}

export function getOrderDetail(id) {
  return request({ url: `/orders/${id}`, method: 'get' })
}

export function createOrder(data) {
  return request({ url: '/orders', method: 'post', data })
}

export function cancelOrder(id) {
  return request({ url: `/orders/${id}/cancel`, method: 'put' })
}

export function confirmReceipt(id) {
  return request({ url: `/orders/${id}/confirm`, method: 'put' })
}

export function payOrder(id, data) {
  return request({ url: `/orders/${id}/pay`, method: 'post', data })
}

export function shipOrder(id, data) {
  return request({ url: `/orders/${id}/ship`, method: 'put', data })
}

export function getOrderLogistics(id) {
  return request({ url: `/orders/${id}/logistics`, method: 'get' })
}

export function getSoldOrders(params) {
  return request({ url: '/orders/sold', method: 'get', params })
}

export function getBoughtOrders(params) {
  return request({ url: '/orders/bought', method: 'get', params })
}
