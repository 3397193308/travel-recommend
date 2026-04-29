import request from './request'

export const getAllCategories = () => {
  return request({
    url: '/api/categories',
    method: 'get'
  })
}

export const getRootCategories = () => {
  return request({
    url: '/api/categories/root',
    method: 'get'
  })
}

export const getCategoriesByParentId = (parentId) => {
  return request({
    url: `/api/categories/parent/${parentId}`,
    method: 'get'
  })
}

export const getCategoryById = (id) => {
  return request({
    url: `/api/categories/${id}`,
    method: 'get'
  })
}