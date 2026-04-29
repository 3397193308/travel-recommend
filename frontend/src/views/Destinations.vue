<template>
  <div class="destinations-page">
    <div class="page-header">
      <h1>景点浏览</h1>
      <p>探索精彩景点，发现旅行灵感</p>
    </div>

    <div class="filter-bar">
      <!-- 快捷筛选标签 -->
      <div class="quick-filters">
        <el-tag
          v-for="tag in quickFilterTags"
          :key="tag.value"
          :type="activeQuickFilter === tag.value ? 'primary' : 'info'"
          @click="handleQuickFilter(tag.value)"
        >
          {{ tag.label }}
        </el-tag>
      </div>
      
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="filters.keyword" placeholder="搜索景点名称" clearable @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="selectedProvince" placeholder="选择省份" clearable @change="handleProvinceChange">
            <el-option v-for="p in provinces" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="selectedCity" placeholder="选择城市" clearable :disabled="!selectedProvince" @change="handleCityChange">
            <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.tagId" placeholder="景点标签" clearable>
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px;">
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="selectedRootCategory" placeholder="选择一级分类" clearable @change="handleRootCategoryChange">
            <el-option v-for="category in rootCategories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.categoryId" placeholder="选择二级分类" clearable :disabled="!selectedRootCategory">
            <el-option v-for="category in subCategories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="sort-controls">
            <el-select v-model="filters.sortBy" placeholder="排序方式">
              <el-option label="综合排序" value="view_count" />
              <el-option label="评分最高" value="average_rating" />
              <el-option label="收藏最多" value="collect_count" />
              <el-option label="价格" value="ticket_price" />
            </el-select>
            <el-button
              :icon="filters.sortOrder === 'desc' ? SortDown : SortUp"
              circle
              size="small"
              @click="toggleSortOrder"
            />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="action-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </el-col>
      </el-row>
      
      <!-- 价格和评分范围筛选 -->
      <el-row :gutter="20" style="margin-top: 15px;">
        <el-col :xs="24" :sm="12" :md="12">
          <div class="range-filter">
            <span class="label">价格范围：</span>
            <el-input-number v-model="filters.minPrice" :min="0" placeholder="最低价" size="small" style="width: 100px;" />
            <span class="separator">-</span>
            <el-input-number v-model="filters.maxPrice" :min="0" placeholder="最高价" size="small" style="width: 100px;" />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12">
          <div class="range-filter">
            <span class="label">评分范围：</span>
            <el-slider
              v-model="ratingRange"
              range
              :min="0"
              :max="5"
              :step="0.5"
              :format-tooltip="(val) => val + '分'"
              @change="handleRatingChange"
              style="flex: 1; margin: 0 10px;"
            />
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 筛选结果统计 -->
    <div v-if="!loading" class="results-stats">
      <span class="stats-text">共找到 <strong>{{ pagination.total }}</strong> 个景点</span>
      <span v-if="hasActiveFilters" class="active-filters">
        <el-tag
          v-for="filter in activeFiltersList"
          :key="filter.key"
          closable
          size="small"
          @close="removeFilter(filter.key)"
        >
          {{ filter.label }}
        </el-tag>
      </span>
    </div>

    <div class="destinations-grid" v-loading="loading">
      <el-row :gutter="20">
        <el-col 
          v-for="item in destinations" 
          :key="item.id" 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6"
        >
          <el-card class="destination-card" shadow="hover" @click="goToDetail(item.id)">
            <div class="card-image">
              <img :src="getImageUrl(item)" :alt="item.name" />
              <div class="card-rating">
                <el-rate :model-value="item.averageRating" disabled show-score text-color="#ff9900" />
              </div>
              <div class="collect-badge" v-if="item.isCollected">
                <el-icon><Star /></el-icon>
              </div>
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ item.name }}</h3>
              <p class="card-location">
                <el-icon><Location /></el-icon>
                {{ item.province }} · {{ item.city }}
              </p>
              <p class="card-description">{{ item.description?.substring(0, 50) }}...</p>
              <div class="card-tags">
                <el-tag v-for="tag in item.tags?.slice(0, 2)" :key="tag.id" size="small" type="info">
                  {{ tag.name }}
                </el-tag>
              </div>
              <div class="card-footer">
                <span class="card-price" v-if="item.ticketPrice">
                  ¥{{ item.ticketPrice }}
                </span>
                <span class="card-price free" v-else>免费</span>
                <div class="card-stats">
                  <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                  <span><el-icon><Star /></el-icon> {{ item.collectCount }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && destinations.length === 0" description="暂无景点数据" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Location, View, Star, SortUp, SortDown } from '@element-plus/icons-vue'
import { getDestinationList } from '@/api/destination'
import { getLocationsByLevel, getLocationsByParentId } from '@/api/location'
import { getAllTags } from '@/api/tag'
import { getRootCategories, getCategoriesByParentId } from '@/api/category'
import { getDestinationDefaultImage, normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()

const loading = ref(false)
const destinations = ref([])
const provinces = ref([])

// 获取景点的显示图片
const getImageUrl = (item) => {
  if (item.imageUrl) return normalizeImageUrl(item.imageUrl)
  return getDestinationDefaultImage(item)
}
const cities = ref([])
const tags = ref([])
const rootCategories = ref([])
const subCategories = ref([])
const selectedProvince = ref(null)
const selectedCity = ref(null)
const selectedRootCategory = ref(null)

// 快捷筛选标签
const quickFilterTags = [
  { label: '全部', value: '' },
  { label: '免费', value: 'free' },
  { label: '热门', value: 'hot' },
  { label: '高分', value: 'high_rating' },
  { label: '最近更新', value: 'recent' }
]
const activeQuickFilter = ref('')

// 评分范围
const ratingRange = ref([0, 5])

const filters = reactive({
  keyword: '',
  locationId: null,
  tagId: null,
  categoryId: null,
  sortBy: 'view_count',
  sortOrder: 'desc',
  minPrice: null,
  maxPrice: null,
  minRating: null,
  maxRating: null
})

const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0
})

// 计算是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return filters.keyword || filters.locationId || filters.tagId || 
         filters.categoryId || filters.minPrice != null || 
         filters.maxPrice != null || filters.minRating != null || 
         filters.maxRating != null || activeQuickFilter.value
})

// 获取活跃的筛选条件列表
const activeFiltersList = computed(() => {
  const list = []
  
  if (filters.keyword) {
    list.push({ key: 'keyword', label: `搜索: ${filters.keyword}` })
  }
  if (selectedProvince.value) {
    const province = provinces.value.find(p => p.id === selectedProvince.value)
    if (province) {
      list.push({ key: 'province', label: `省份: ${province.name}` })
    }
  }
  if (selectedCity.value) {
    const city = cities.value.find(c => c.id === selectedCity.value)
    if (city) {
      list.push({ key: 'city', label: `城市: ${city.name}` })
    }
  }
  if (filters.tagId) {
    const tag = tags.value.find(t => t.id === filters.tagId)
    if (tag) {
      list.push({ key: 'tag', label: `标签: ${tag.name}` })
    }
  }
  if (filters.categoryId) {
    const category = [...rootCategories.value, ...subCategories.value].find(c => c.id === filters.categoryId)
    if (category) {
      list.push({ key: 'category', label: `分类: ${category.name}` })
    }
  }
  if (filters.minPrice != null || filters.maxPrice != null) {
    const min = filters.minPrice != null ? filters.minPrice : 0
    const max = filters.maxPrice != null ? filters.maxPrice : '不限'
    list.push({ key: 'price', label: `价格: ${min} - ${max}` })
  }
  if (filters.minRating != null || filters.maxRating != null) {
    const min = filters.minRating != null ? filters.minRating : 0
    const max = filters.maxRating != null ? filters.maxRating : 5
    list.push({ key: 'rating', label: `评分: ${min} - ${max}分` })
  }
  if (activeQuickFilter.value) {
    const tag = quickFilterTags.find(t => t.value === activeQuickFilter.value)
    if (tag) {
      list.push({ key: 'quickFilter', label: tag.label })
    }
  }
  
  return list
})

const fetchDestinations = async () => {
  loading.value = true
  try {
    const res = await getDestinationList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filters
    })
    if (res.code === 200) {
      destinations.value = res.data.list
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('获取景点列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchProvinces = async () => {
  try {
    const res = await getLocationsByLevel(1)
    if (res.code === 200) {
      provinces.value = res.data
    }
  } catch (error) {
    console.error('获取省份列表失败:', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getAllTags()
    if (res.code === 200) {
      tags.value = res.data
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

const fetchRootCategories = async () => {
  try {
    const res = await getRootCategories()
    if (res.code === 200) {
      rootCategories.value = res.data
    }
  } catch (error) {
    console.error('获取一级分类失败:', error)
  }
}

const handleProvinceChange = async () => {
  selectedCity.value = null
  filters.locationId = selectedProvince.value
  if (selectedProvince.value) {
    try {
      const res = await getLocationsByParentId(selectedProvince.value)
      if (res.code === 200) {
        cities.value = res.data
      }
    } catch (error) {
      console.error('获取城市列表失败:', error)
    }
  } else {
    cities.value = []
  }
  // 触发搜索
  handleSearch()
}

const handleCityChange = () => {
  filters.locationId = selectedCity.value || selectedProvince.value
  // 触发搜索
  handleSearch()
}

const handleRootCategoryChange = async () => {
  if (selectedRootCategory.value) {
    // 当选择一级分类时，将其 ID 赋值给 filters.categoryId
    filters.categoryId = selectedRootCategory.value
    try {
      const res = await getCategoriesByParentId(selectedRootCategory.value)
      if (res.code === 200) {
        subCategories.value = res.data
      }
    } catch (error) {
      console.error('获取二级分类失败:', error)
    }
  } else {
    filters.categoryId = null
    subCategories.value = []
  }
  // 触发搜索
  handleSearch()
}

// 快捷筛选处理
const handleQuickFilter = (value) => {
  activeQuickFilter.value = value
  
  // 重置价格和评分筛选
  filters.minPrice = null
  filters.maxPrice = null
  filters.minRating = null
  filters.maxRating = null
  ratingRange.value = [0, 5]
  
  // 根据快捷筛选设置条件
  switch (value) {
    case 'free':
      filters.maxPrice = 0
      filters.sortBy = 'view_count'
      filters.sortOrder = 'desc'
      break
    case 'hot':
      filters.sortBy = 'view_count'
      filters.sortOrder = 'desc'
      break
    case 'high_rating':
      filters.minRating = 4
      filters.sortBy = 'average_rating'
      filters.sortOrder = 'desc'
      break
    case 'recent':
      // 最近更新功能需要后端支持，这里暂时只设置排序
      filters.sortBy = 'view_count'
      filters.sortOrder = 'desc'
      break
    default:
      // 全部
      break
  }
  
  handleSearch()
}

// 评分范围变化处理
const handleRatingChange = (value) => {
  if (value[0] === 0) {
    filters.minRating = null
  } else {
    filters.minRating = value[0]
  }
  
  if (value[1] === 5) {
    filters.maxRating = null
  } else {
    filters.maxRating = value[1]
  }
}

// 切换排序方向
const toggleSortOrder = () => {
  filters.sortOrder = filters.sortOrder === 'desc' ? 'asc' : 'desc'
  handleSearch()
}

// 移除单个筛选条件
const removeFilter = (key) => {
  switch (key) {
    case 'keyword':
      filters.keyword = ''
      break
    case 'province':
      selectedProvince.value = null
      selectedCity.value = null
      filters.locationId = null
      cities.value = []
      break
    case 'city':
      selectedCity.value = null
      filters.locationId = selectedProvince.value
      break
    case 'tag':
      filters.tagId = null
      break
    case 'category':
      filters.categoryId = null
      selectedRootCategory.value = null
      subCategories.value = []
      break
    case 'price':
      filters.minPrice = null
      filters.maxPrice = null
      break
    case 'rating':
      filters.minRating = null
      filters.maxRating = null
      ratingRange.value = [0, 5]
      break
    case 'quickFilter':
      activeQuickFilter.value = ''
      break
  }
  handleSearch()
}

const handleSearch = () => {
  pagination.page = 1
  fetchDestinations()
}

const resetFilters = () => {
  filters.keyword = ''
  selectedProvince.value = null
  selectedCity.value = null
  filters.locationId = null
  filters.tagId = null
  selectedRootCategory.value = null
  subCategories.value = []
  filters.categoryId = null
  filters.sortBy = 'view_count'
  filters.sortOrder = 'desc'
  filters.minPrice = null
  filters.maxPrice = null
  filters.minRating = null
  filters.maxRating = null
  ratingRange.value = [0, 5]
  activeQuickFilter.value = ''
  cities.value = []
  pagination.page = 1
  fetchDestinations()
}

const handleSizeChange = () => {
  pagination.page = 1
  fetchDestinations()
}

const handleCurrentChange = () => {
  fetchDestinations()
}

const goToDetail = (id) => {
  router.push(`/destination/${id}`)
}

onMounted(() => {
  fetchDestinations()
  fetchProvinces()
  fetchTags()
  fetchRootCategories()
})
</script>

<style scoped>
.destinations-page {
  padding: 30px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  color: #909399;
}

.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.quick-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.quick-filters .el-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 6px 15px;
  font-size: 14px;
}

.quick-filters .el-tag:hover {
  transform: translateY(-2px);
}

.filter-bar .el-select,
.filter-bar .el-input {
  width: 100%;
}

.sort-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.sort-controls .el-select {
  flex: 1;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.range-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.range-filter .label {
  white-space: nowrap;
  color: #606266;
  font-size: 14px;
}

.range-filter .separator {
  color: #909399;
}

.results-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px 20px;
  background: #f5f7fa;
  border-radius: 6px;
}

.stats-text {
  color: #606266;
  font-size: 14px;
}

.stats-text strong {
  color: #409eff;
  font-size: 18px;
}

.active-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.destinations-grid {
  min-height: 400px;
}

.destination-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.destination-card:hover {
  transform: translateY(-5px);
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-rating {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 5px 10px;
  border-radius: 4px;
}

.collect-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #f56c6c;
  color: #fff;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-content {
  padding: 15px;
}

.card-title {
  font-size: 16px;
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
  gap: 5px;
}

.card-description {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
}

.card-tags {
  margin-bottom: 10px;
}

.card-tags .el-tag {
  margin-right: 5px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.card-price.free {
  color: #67c23a;
}

.card-stats {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 13px;
}

.card-stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
