// 分类名称到默认图片的映射
export const categoryDefaultImages = {
  '自然风光': '/images/nature.jpg',
  '历史文化': '/images/history.jpg',
  '城市观光': '/images/city.jpg',
  '主题乐园': '/images/theme-park.jpg',
  '宗教圣地': '/images/religious.jpg',
  '古镇村落': '/images/ancient-village.jpg',
  '海滨岛屿': '/images/beach.jpg',
  '艺术文创': '/images/art.jpg',
  '美食天地': '/images/food.jpg',
  '户外探险': '/images/adventure.jpg'
}

// 通用默认图片
export const defaultImage = '/images/nature.jpg'

// 后端基础URL（通过代理访问）
const BACKEND_BASE = ''

// 规范化图片URL
export function normalizeImageUrl(url) {
  if (!url) return ''
  
  // 如果是完整的URL（以 http 或 https 开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径（以 / 开头），添加后端基础路径
  if (url.startsWith('/')) {
    return BACKEND_BASE + url
  }
  
  // 其他情况，直接返回
  return url
}

// 根据景点的分类获取默认图片
export function getDestinationDefaultImage(destination) {
  if (!destination) return defaultImage
  
  // 优先查找一级分类
  if (destination.categories && destination.categories.length > 0) {
    for (const category of destination.categories) {
      if (categoryDefaultImages[category.name]) {
        return categoryDefaultImages[category.name]
      }
    }
  }
  
  // 如果没有匹配的分类，返回通用默认图
  return defaultImage
}
