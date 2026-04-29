<template>
  <div class="search-discover-page">
    <div class="page-header">
      <h1>搜索发现</h1>
      <p>发现更多精彩景点</p>
    </div>

    <div class="search-container">
      <el-card class="search-card">
        <div class="search-form">
          <el-input
            v-model="searchQuery"
            placeholder="请输入景点名称或关键词"
            prefix-icon="Search"
            @keyup.enter="search"
          >
            <template #append>
              <el-button type="primary" @click="search">搜索</el-button>
            </template>
          </el-input>

          <div class="filter-options">
            <el-select
              v-model="selectedProvince"
              placeholder="选择省份"
              clearable
              @change="search"
            >
              <el-option
                v-for="province in provinces"
                :key="province.id"
                :label="province.name"
                :value="province.id"
              />
            </el-select>

            <el-select
              v-model="selectedCity"
              placeholder="选择城市"
              clearable
              @change="search"
            >
              <el-option
                v-for="city in cities"
                :key="city.id"
                :label="city.name"
                :value="city.id"
              />
            </el-select>

            <el-select
              v-model="selectedTag"
              placeholder="选择标签"
              clearable
              @change="search"
            >
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>

            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              @change="search"
            >
              <el-option label="综合排序" value="default" />
              <el-option label="评分最高" value="rating" />
              <el-option label="价格最低" value="price_asc" />
              <el-option label="价格最高" value="price_desc" />
              <el-option label="最新添加" value="newest" />
            </el-select>
          </div>
        </div>
      </el-card>

      <div class="results-container">
        <el-card v-if="loading" class="loading-card">
          <el-skeleton :rows="6" animated />
        </el-card>

        <div v-else-if="destinations.length === 0" class="empty-results">
          <el-empty description="未找到相关景点" />
        </div>

        <div v-else class="destinations-grid">
          <el-card
            v-for="item in destinations"
            :key="item.id"
            class="destination-card"
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
                  :type="item.isCollected ? 'warning' : 'default'"
                  size="small"
                  circle
                  @click="toggleCollection(item.id, !item.isCollected)"
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

        <div v-if="!loading && destinations.length > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, Search } from '@element-plus/icons-vue'
import { getAllTags, addCollection, removeCollection, checkCollection } from '../api/user.js'
import { getDestinationList } from '../api/destination.js'
import { getLocationsByLevel, getLocationsByParentId } from '../api/location.js'
import { useRouter } from 'vue-router'
import { getDestinationDefaultImage, normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()
const searchQuery = ref('')
const selectedProvince = ref('')
const selectedCity = ref('')
const selectedTag = ref('')
const sortBy = ref('default')
const destinations = ref([])
const tags = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(9)
const total = ref(0)

// 获取景点的显示图片
const getImageUrl = (item) => {
  if (item.imageUrl) return normalizeImageUrl(item.imageUrl)
  return getDestinationDefaultImage(item)
}

// 省份和城市数据
const provinces = ref([])
const cities = ref([])

// 加载省份数据
const loadProvinces = async () => {
  try {
    const response = await getLocationsByLevel(1)
    if (response.code === 200 && response.data) {
      provinces.value = response.data
    }
  } catch (error) {
    console.error('加载省份数据失败:', error)
  }
}

// 加载城市数据
const loadCities = async (provinceId) => {
  try {
    if (provinceId) {
      const response = await getLocationsByParentId(provinceId)
      if (response.code === 200 && response.data) {
        cities.value = response.data
      }
    } else {
      cities.value = []
    }
  } catch (error) {
    console.error('加载城市数据失败:', error)
  }
}

// 监听省份变化，加载对应城市
watch(selectedProvince, (newProvinceId) => {
  loadCities(newProvinceId)
  selectedCity.value = ''
})

const loadTags = async () => {
  try {
    const response = await getAllTags()
    if (response.code === 200 && response.data) {
      tags.value = response.data
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const search = async () => {
  try {
    loading.value = true
    
    // 构建搜索参数
    const params = {
      keyword: searchQuery.value,
      locationId: selectedCity.value || selectedProvince.value,
      tagId: selectedTag.value,
      sortBy: sortBy.value,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 调用真实的API
    const response = await getDestinationList(params)
    if (response.code === 200 && response.data) {
      destinations.value = (response.data.list || []).map(dest => ({
        ...dest,
        // 确保图片字段存在，使用默认图片作为后备
        imageUrl: dest.imageUrl || dest.image_url || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
      }))
      total.value = response.data.total || 0
      
      // 检查每个景点的收藏状态
      for (const destination of destinations.value) {
        try {
          const checkResponse = await checkCollection(destination.id)
          if (checkResponse.code === 200) {
            destination.isCollected = checkResponse.data
          }
        } catch (error) {
          console.error('检查收藏状态失败:', error)
          destination.isCollected = false
        }
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const toggleCollection = async (destinationId, isCollected) => {
  try {
    if (isCollected) {
      const response = await addCollection(destinationId)
      if (response.code === 200) {
        ElMessage.success('收藏成功')
        // 更新收藏状态
        const destination = destinations.value.find(item => item.id === destinationId)
        if (destination) {
          destination.isCollected = true
        }
      } else {
        ElMessage.error('收藏失败：' + (response.message || '未知错误'))
      }
    } else {
      const response = await removeCollection(destinationId)
      if (response.code === 200) {
        ElMessage.success('取消收藏成功')
        // 更新收藏状态
        const destination = destinations.value.find(item => item.id === destinationId)
        if (destination) {
          destination.isCollected = false
        }
      } else {
        ElMessage.error('取消收藏失败：' + (response.message || '未知错误'))
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const viewDetails = (destinationId) => {
  router.push(`/destination/${destinationId}`)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  search()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  search()
}

onMounted(() => {
  loadTags()
  loadProvinces()
  search()
})
</script>

<style scoped>
.search-discover-page {
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

.search-container {
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-options {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-options .el-select {
  flex: 1;
  min-width: 150px;
}

.results-container {
  margin-top: 30px;
}

.loading-card {
  margin-bottom: 20px;
}

.empty-results {
  text-align: center;
  padding: 60px 0;
}

.destinations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.destination-card {
  transition: all 0.3s ease;
}

.destination-card:hover {
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

.destination-card:hover .card-image img {
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

.pagination {
  margin-top: 30px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-discover-page {
    padding: 20px 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .filter-options {
    flex-direction: column;
  }
  
  .filter-options .el-select {
    width: 100%;
  }
  
  .destinations-grid {
    grid-template-columns: 1fr;
  }
}
</style>
