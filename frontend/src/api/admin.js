import request from './request'

export const adminDashboardStats = () => {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

export const adminListDestinations = (params) => {
  return request({
    url: '/admin/destinations',
    method: 'get',
    params
  })
}

export const adminSaveDestination = (data) => {
  return request({
    url: '/admin/destinations',
    method: 'post',
    data
  })
}

export const adminUpdateDestinationStatus = (id, status) => {
  return request({
    url: `/admin/destinations/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

export const adminBatchUpdateDestinationStatus = (ids, status) => {
  return request({
    url: '/admin/destinations/batch-status',
    method: 'patch',
    data: { ids, status }
  })
}

export const adminDeleteDestination = (id) => {
  return request({
    url: `/admin/destinations/${id}`,
    method: 'delete'
  })
}

export const adminListTags = (params) => {
  return request({
    url: '/admin/tags',
    method: 'get',
    params
  })
}

export const adminSaveTag = (data) => {
  return request({
    url: '/admin/tags',
    method: 'post',
    data
  })
}

export const adminUpdateTagStatus = (id, status) => {
  return request({
    url: `/admin/tags/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

export const adminBatchUpdateTagStatus = (ids, status) => {
  return request({
    url: '/admin/tags/batch-status',
    method: 'patch',
    data: { ids, status }
  })
}

export const adminListUsers = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export const adminUpdateUserStatus = (id, status) => {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

export const adminBatchUpdateUserStatus = (ids, status) => {
  return request({
    url: '/admin/users/batch-status',
    method: 'patch',
    data: { ids, status }
  })
}

export const adminListComments = (params) => {
  return request({
    url: '/admin/comments',
    method: 'get',
    params
  })
}

export const adminUpdateCommentStatus = (id, status) => {
  return request({
    url: `/admin/comments/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

export const adminBatchUpdateCommentStatus = (ids, status) => {
  return request({
    url: '/admin/comments/batch-status',
    method: 'patch',
    data: { ids, status }
  })
}

export const getAlgorithmConfig = () => {
  return request({
    url: '/admin/algorithm/config',
    method: 'get'
  })
}

export const updateAlgorithmConfig = (data) => {
  return request({
    url: '/admin/algorithm/config',
    method: 'put',
    data
  })
}

export const adminListExperiences = (params) => {
  return request({
    url: '/admin/experiences',
    method: 'get',
    params
  })
}

export const adminGetExperienceDetail = (id) => {
  return request({
    url: `/admin/experiences/${id}`,
    method: 'get'
  })
}

export const adminAuditExperience = (id, data) => {
  return request({
    url: `/admin/experiences/${id}/audit`,
    method: 'patch',
    data
  })
}
