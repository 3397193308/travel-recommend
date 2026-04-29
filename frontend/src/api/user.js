import request from './request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

// 修改密码
export const updatePassword = (data) => {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 管理员登录
export const adminLogin = (data) => {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

// 获取用户偏好标签
export const getUserPreferences = () => {
  return request({
    url: '/user/preferences',
    method: 'get'
  })
}

// 保存用户偏好
export const saveUserPreferences = (data) => {
  return request({
    url: '/user/preferences',
    method: 'post',
    data
  })
}

// 获取所有标签
export const getAllTags = () => {
  return request({
    url: '/tag/list',
    method: 'get'
  })
}

// 收藏相关API
// 添加收藏
export const addCollection = (destinationId) => {
  return request({
    url: '/user/collections',
    method: 'post',
    data: { destinationId }
  })
}

// 移除收藏
export const removeCollection = (destinationId) => {
  return request({
    url: `/user/collections/${destinationId}`,
    method: 'delete'
  })
}

// 获取用户收藏列表
export const getUserCollections = () => {
  return request({
    url: '/user/collections',
    method: 'get'
  })
}

// 检查是否收藏
export const checkCollection = (destinationId) => {
  return request({
    url: `/user/collections/check/${destinationId}`,
    method: 'get'
  })
}

// 用户中心概览
export const getProfileOverview = () => {
  return request({
    url: '/user/profile/overview',
    method: 'get'
  })
}

// 最近浏览
export const getRecentViews = (limit = 10) => {
  return request({
    url: '/user/profile/recent-views',
    method: 'get',
    params: { limit }
  })
}

// 我的评分
export const getMyRatings = (limit = 10) => {
  return request({
    url: '/user/profile/ratings',
    method: 'get',
    params: { limit }
  })
}
