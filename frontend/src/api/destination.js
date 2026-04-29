import request from './request'

export const getDestinationList = (params) => {
  return request({
    url: '/destination/list',
    method: 'get',
    params
  })
}

export const getDestinationDetail = (id) => {
  return request({
    url: `/destination/${id}`,
    method: 'get'
  })
}

export const getHotDestinations = (limit = 10) => {
  return request({
    url: '/destination/hot',
    method: 'get',
    params: { limit }
  })
}

export const collectDestination = (id) => {
  return request({
    url: `/destination/${id}/collect`,
    method: 'post'
  })
}

export const uncollectDestination = (id) => {
  return request({
    url: `/destination/${id}/collect`,
    method: 'delete'
  })
}

export const rateDestination = (data) => {
  return request({
    url: '/destination/rate',
    method: 'post',
    data
  })
}

export const getComments = (id, page = 1, pageSize = 10) => {
  return request({
    url: `/destination/${id}/comments`,
    method: 'get',
    params: { page, pageSize }
  })
}

export const addComment = (data) => {
  return request({
    url: '/destination/comment',
    method: 'post',
    data
  })
}

export const getProvinces = () => {
  return request({
    url: '/destination/provinces',
    method: 'get'
  })
}

export const getCities = (province) => {
  return request({
    url: '/destination/cities',
    method: 'get',
    params: { province }
  })
}

export const getRecommendations = (limit = 10) => {
  return request({
    url: '/destination/recommend',
    method: 'get',
    params: { limit }
  })
}

export const getRecommendedForYou = (limit = 10) => {
  return request({
    url: '/destination/recommend/for-you',
    method: 'get',
    params: { limit }
  })
}
