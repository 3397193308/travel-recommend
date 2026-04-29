<template>
  <div class="my-collections-page">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p>查看和管理您收藏的景点</p>
    </div>

    <div class="collections-container">
      <el-card v-if="loading" class="loading-card">
        <el-skeleton :rows="6" animated />
      </el-card>

      <div v-else-if="collections.length === 0" class="empty-collections">
        <el-empty description="您还没有收藏任何景点" />
        <el-button type="primary" @click="goToDiscover">去发现景点</el-button>
      </div>

      <div v-else class="collections-grid">
        <el-card
          v-for="item in collections"
          :key="item.id"
          class="collection-card"
          :body-style="{ padding: '0' }"
        >
          <div class="card-image">
            <el-image
              :src="getImageUrl(item)"
              fit="cover"
              :alt="item.name"
            />
            <div class="card-actions">
              <el-button
                type="danger"
                size="small"
                circle
                @click="handleRemoveCollection(item.id)"
              >
                <el-icon><Star /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="card-content">
            <h3 class="destination-name">{{ item.name }}</h3>
            <div class="destination-info">
              <span class="location">{{ item.province }} {{ item.city }}</span>
              <span class="rating">
                <el-icon><Star /></el-icon>
                {{ item.averageRating || 0 }}
              </span>
            </div>
            <p class="destination-description">{{ item.description }}</p>
            <div class="card-footer">
              <span class="price">¥{{ item.ticketPrice || 0 }}</span>
              <el-button type="primary" size="small" @click="viewDetails(item.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { getUserCollections, removeCollection } from '../api/user.js'
import { useRouter } from 'vue-router'
import { getDestinationDefaultImage, normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()
const collections = ref([])
const loading = ref(true)

// 获取景点的显示图片
const getImageUrl = (item) => {
  if (item.imageUrl) return normalizeImageUrl(item.imageUrl)
  return getDestinationDefaultImage(item)
}

const loadCollections = async () => {
  try {
    loading.value = true
    const response = await getUserCollections()
    if (response.code === 200 && response.data) {
      collections.value = response.data
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleRemoveCollection = async (destinationId) => {
  try {
    const response = await removeCollection(destinationId)
    if (response.code === 200) {
      ElMessage.success('取消收藏成功')
      // 重新加载收藏列表
      await loadCollections()
    } else {
      ElMessage.error('取消收藏失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败，请稍后重试')
  }
}

const viewDetails = (destinationId) => {
  router.push(`/destination/${destinationId}`)
}

const goToDiscover = () => {
  router.push('/destinations')
}

onMounted(() => {
  loadCollections()
})
</script>

<style scoped>
.my-collections-page {
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

.collections-container {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-card {
  margin-bottom: 20px;
}

.empty-collections {
  text-align: center;
  padding: 60px 0;
}

.collections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.collection-card {
  transition: all 0.3s ease;
}

.collection-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.collection-card:hover .card-image img {
  transform: scale(1.05);
}

.card-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
}

.card-content {
  padding: 15px;
}

.destination-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.destination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.location {
  display: flex;
  align-items: center;
}

.rating {
  display: flex;
  align-items: center;
  color: #E6A23C;
}

.destination-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #F56C6C;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-collections-page {
    padding: 20px 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .collections-grid {
    grid-template-columns: 1fr;
  }
}
</style>
