import request from '@/utils/request'

export function getComments(productId, params) {
  return request({ url: `/comments/product/${productId}`, method: 'get', params })
}

export function addComment(data) {
  return request({ url: '/comments', method: 'post', data })
}

export function deleteComment(id) {
  return request({ url: `/comments/${id}`, method: 'delete' })
}

export function getMyComments(params) {
  return request({ url: '/comments/my', method: 'get', params })
}

export function getCommentStats(productId) {
  return request({ url: `/comments/product/${productId}/stats`, method: 'get' })
}
