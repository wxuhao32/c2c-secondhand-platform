import request from '@/utils/request'

export function searchProducts(params) {
  return request({ url: '/search', method: 'get', params })
}

export function getHotKeywords() {
  return request({ url: '/search/hot', method: 'get' })
}

export function getSearchSuggestions(keyword) {
  return request({ url: '/search/suggest', method: 'get', params: { keyword } })
}

export function getSearchHistory() {
  return request({ url: '/search/history', method: 'get' })
}

export function clearSearchHistory() {
  return request({ url: '/search/history', method: 'delete' })
}
