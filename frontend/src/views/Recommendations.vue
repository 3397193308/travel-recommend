<template>
  <div class="recommendations-page">
    <div class="page-header">
      <h1>智能推荐</h1>
      <p>基于您的偏好，为您推荐最适合的景点</p>
    </div>

    <div class="user-preferences">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><TrendCharts /></el-icon>
            <span>您的偏好标签</span>
            <el-button type="primary" link size="small" @click="refreshUserPreferences">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        <div class="tags-container">
          <el-tag 
            v-for="tag in userPreferences" 
            :key="tag" 
            type="success"
            effect="plain"
            size="large"
          >
            {{ tag }}
          </el-tag>
          <el-tag type="info" effect="plain" size="large" @click="goToPreferences">
            <el-icon><Plus /></el-icon>
            添加偏好
          </el-tag>
        </div>
      </el-card>
    </div>

    <div class="recommendation-section">
      <div class="section-header">
        <h2>
          <el-icon><Star /></el-icon>
          为您推荐
        </h2>
        <el-button type="primary" link @click="refreshRecommendations">
          <el-icon><Refresh /></el-icon>
          刷新推荐
        </el-button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="loading-icon"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-6.219-8.56"></path></svg></el-icon>
        <span>加载推荐中...</span>
      </div>
      <div v-else-if="recommendations.length === 0" class="empty-state">
        <el-icon class="empty-icon"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg></el-icon>
        <p>暂无推荐内容</p>
      </div>
      <div v-else class="recommendations-grid">
        <el-row :gutter="20">
          <el-col 
            v-for="item in recommendations" 
            :key="item.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <el-card class="recommendation-card" shadow="hover" @click="goToDetail(item.id)">
              <div class="card-badge">
                <el-tag :type="getBadgeType(item.matchRate)" size="small">
                  匹配度 {{ item.matchRate }}%
                </el-tag>
              </div>
              <div class="card-image">
                <img :src="getImageUrl(item)" :alt="item.name" />
              </div>
              <div class="card-content">
                <h3 class="card-title">{{ item.name }}</h3>
                <p class="card-location">
                  <el-icon><Location /></el-icon>
                  {{ item.province }} · {{ item.city }}
                </p>
                <div class="card-tags">
                  <el-tag 
                    v-for="tag in item.tags" 
                    :key="tag" 
                    size="small"
                    type="info"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div v-if="item.reason" class="card-reason">
                  <el-tag size="small" type="success">{{ item.reason }}</el-tag>
                </div>
                <div class="card-footer">
                  <div class="card-rating">
                    <el-rate v-model="item.rating" disabled show-score />
                  </div>
                  <span class="card-price" v-if="item.price">
                    ¥{{ item.price }}
                  </span>
                  <span class="card-price free" v-else>免费</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="hot-destinations">
      <div class="section-header">
        <h2>
          <el-icon><Trophy /></el-icon>
          热门景点
        </h2>
        <el-button type="primary" link @click="goToDestinations">
          查看更多
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col 
          v-for="item in hotDestinations" 
          :key="item.id" 
          :xs="24" 
          :sm="12" 
          :md="8"
        >
          <el-card class="hot-card" shadow="hover" @click="goToDetail(item.id)">
            <div class="hot-card-content">
              <img :src="getImageUrl(item)" :alt="item.name" />
              <div class="hot-card-info">
                <h3>{{ item.name }}</h3>
                <p>{{ item.description }}</p>
                <div class="hot-card-stats">
                  <span><el-icon><View /></el-icon> {{ item.views }}</span>
                  <span><el-icon><Star /></el-icon> {{ item.rating }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  TrendCharts, Star, Refresh, Plus, Location, 
  Trophy, ArrowRight, View 
} from '@element-plus/icons-vue'
import { getRecommendedForYou, getHotDestinations } from '../api/destination.js'
import { getUserPreferences } from '../api/user.js'
import { getDestinationDefaultImage, normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()

const userPreferences = ref([])
const recommendations = ref([])
const hotDestinations = ref([])
const loading = ref(false)

// 获取景点的显示图片
const getImageUrl = (item) => {
  if (item.image) return normalizeImageUrl(item.image)
  return getDestinationDefaultImage(item)
}

const getBadgeType = (rate) => {
  if (rate >= 90) return 'success'
  if (rate >= 80) return 'warning'
  return 'info'
}

const loadRecommendations = async () => {
  loading.value = true
  try {
    const [recommendResponse, hotResponse] = await Promise.all([
      getRecommendedForYou(20),
      getHotDestinations(10)
    ])
    
    // 处理推荐数据
    if (recommendResponse.code === 200 && recommendResponse.data) {
      recommendations.value = recommendResponse.data.map(item => ({
        ...item,
        rating: item.averageRating || 0,
        price: item.ticketPrice || 0,
        image: item.imageUrl ? item.imageUrl.replace(/,$/, '') : '',
        matchRate: item.matchRate || 50,
        tags: item.tags ? item.tags.map(tag => tag.name) : []
      }))
    } else {
      // 加载失败时使用热门景点作为备选
      if (hotResponse.code === 200 && hotResponse.data) {
        recommendations.value = hotResponse.data.map(item => ({
          ...item,
          rating: item.averageRating || 0,
          price: 0,
          image: item.imageUrl ? item.imageUrl.replace(/,$/, '') : '',
          matchRate: 50,
          tags: []
        }))
      }
    }
    
    // 处理热门景点数据
    if (hotResponse.code === 200 && hotResponse.data) {
      hotDestinations.value = hotResponse.data.map(item => ({
        ...item,
        rating: item.averageRating || 0,
        views: item.viewCount || 0,
        image: item.imageUrl ? item.imageUrl.replace(/,$/, '') : '',
        description: item.description || ''
      }))
    } else {
      // 热门景点加载失败时使用空数组
      hotDestinations.value = []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    // 错误时使用空数据，避免页面显示错误
    recommendations.value = []
    hotDestinations.value = []
  } finally {
    loading.value = false
  }
}

const refreshRecommendations = async () => {
  await loadUserPreferences()
  await loadRecommendations()
  ElMessage.success('推荐已刷新')
}

const refreshUserPreferences = async () => {
  await loadUserPreferences()
  ElMessage.success('偏好标签已更新')
}

const goToPreferences = () => {
  router.push('/preferences')
}

const goToDestinations = () => {
  router.push('/destinations')
}

const goToDetail = (id) => {
  router.push(`/destination/${id}`)
}

const loadUserPreferences = async () => {
  try {
    const response = await getUserPreferences()
    if (response.code === 200 && response.data) {
      userPreferences.value = response.data.map(tag => tag.name)
    }
  } catch (error) {
    console.error('加载偏好标签失败:', error)
  }
}

onMounted(() => {
  loadUserPreferences()
  loadRecommendations()
})
</script>

<style scoped>
.recommendations-page {
  padding: 30px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #909399;
}

.user-preferences {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tags-container .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.tags-container .el-tag:hover {
  transform: translateY(-2px);
}

.recommendation-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendations-grid {
  margin-bottom: 30px;
}

.recommendation-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
  overflow: hidden;
  position: relative;
}

.recommendation-card:hover {
  transform: translateY(-5px);
}

.card-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}

.card-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recommendation-card:hover .card-image img {
  transform: scale(1.05);
}

.card-content {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-location {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-rating :deep(.el-rate__text) {
  font-size: 12px;
}

.card-price {
  font-size: 18px;
  font-weight: 600;
  color: #F56C6C;
}

.card-price.free {
  color: #67C23A;
}

.hot-destinations {
  margin-top: 40px;
}

.hot-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
}

.hot-card:hover {
  transform: translateY(-5px);
}

.hot-card-content {
  display: flex;
  gap: 15px;
}

.hot-card-content img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.hot-card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hot-card-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.hot-card-info p {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.hot-card-stats {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #909399;
}

.hot-card-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin: 20px 0;
}

.loading-icon,
.empty-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
}

.loading-state span,
.empty-state p {
  font-size: 16px;
  color: #606266;
}

.card-reason {
  margin: 8px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .recommendations-page {
    padding: 20px 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .section-header h2 {
    font-size: 18px;
  }
  
  .hot-card-content {
    flex-direction: column;
  }
  
  .hot-card-content img {
    width: 100%;
    height: 180px;
  }
  
  .loading-state,
  .empty-state {
    padding: 40px 10px;
  }
  
  .loading-icon,
  .empty-icon {
    font-size: 32px;
  }
}
</style>
