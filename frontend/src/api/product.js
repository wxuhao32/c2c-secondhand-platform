import request from '@/utils/request'

export function getProductList(params) {
  return request({ url: '/products', method: 'get', params })
}

export function getProductDetail(id) {
  return request({ url: `/products/${id}`, method: 'get' })
}

export function publishProduct(data) {
  return request({ url: '/products', method: 'post', data })
}

export function updateProduct(id, data) {
  return request({ url: `/products/${id}`, method: 'put', data })
}

export function deleteProduct(id) {
  return request({ url: `/products/${id}`, method: 'delete' })
}

export function getMyProducts(params) {
  return request({ url: '/products/my', method: 'get', params })
}

export function updateProductStatus(id, status) {
  return request({ url: `/products/${id}/status`, method: 'put', data: { status } })
}

export function getCategories() {
  return request({ url: '/products/categories', method: 'get' })
}

export function getProductsByCategory(categoryId, params) {
  return request({ url: `/products/category/${categoryId}`, method: 'get', params })
}

export function uploadProductImage(data) {
  return request({ url: '/products/upload', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}
