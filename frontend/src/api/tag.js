import request from './request'

export const getAllTags = () => {
  return request({
    url: '/tag/list',
    method: 'get'
  })
}

export const getTagsByType = (type) => {
  return request({
    url: `/tag/list/${type}`,
    method: 'get'
  })
}
