import request from './request'

export const listExperiences = (params) => {
  return request({
    url: '/experience/list',
    method: 'get',
    params
  })
}

export const getExperienceDetail = (id) => {
  return request({
    url: `/experience/${id}`,
    method: 'get'
  })
}

export const publishExperience = (data) => {
  return request({
    url: '/experience',
    method: 'post',
    data
  })
}

export const myExperiences = (params) => {
  return request({
    url: '/experience/my',
    method: 'get',
    params
  })
}

export const uploadExperienceImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/experience/upload-image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
