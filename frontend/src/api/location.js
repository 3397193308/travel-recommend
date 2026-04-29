import request from './request'

export const getAllLocations = () => {
  return request({
    url: '/api/locations',
    method: 'get'
  })
}

export const getLocationById = (id) => {
  return request({
    url: `/api/locations/${id}`,
    method: 'get'
  })
}

export const getLocationsByParentId = (parentId) => {
  return request({
    url: `/api/locations/parent/${parentId}`,
    method: 'get'
  })
}

export const getLocationsByLevel = (level) => {
  return request({
    url: `/api/locations/level/${level}`,
    method: 'get'
  })
}