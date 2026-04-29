<template>
  <div class="search-page">
    <div class="search-header">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索景点名称、城市、地区..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="hot-searches">
        <span class="hot-label">热门搜索：</span>
        <el-tag 
          v-for="tag in hotSearches" 
          :key="tag"
          @click="searchByTag(tag)"
          class="hot-tag"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <div class="filter-section">
      <el-card>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label>省份</label>
              <el-select v-model="filters.province" placeholder="选择省份" clearable>
                <el-option label="全部省份" value="" />
                <el-option label="北京" value="北京" />
                <el-option label="上海" value="上海" />
                <el-option label="广东" value="广东" />
                <el-option label="浙江" value="浙江" />
                <el-option label="江苏" value="江苏" />
                <el-option label="四川" value="四川" />
                <el-option label="云南" value="云南" />
                <el-option label="湖南" value="湖南" />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label>景点类型</label>
              <el-select v-model="filters.category" placeholder="选择类型" clearable>
                <el-option label="全部类型" value="" />
                <el-option label="5A级景区" value="5A级景区" />
                <el-option label="4A级景区" value="4A级景区" />
                <el-option label="历史文化" value="历史文化" />
                <el-option label="自然景观" value="自然景观" />
                <el-option label="主题公园" value="主题公园" />
                <el-option label="博物馆" value="博物馆" />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label>价格范围</label>
              <el-select v-model="filters.priceRange" placeholder="选择价格" clearable>
                <el-option label="全部价格" value="" />
                <el-option label="免费" value="free" />
                <el-option label="0-100元" value="0-100" />
                <el-option label="100-200元" value="100-200" />
                <el-option label="200-500元" value="200-500" />
                <el-option label="500元以上" value="500+" />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label>评分</label>
              <el-select v-model="filters.rating" placeholder="选择评分" clearable>
                <el-option label="全部评分" value="" />
                <el-option label="4.5分以上" value="4.5" />
                <el-option label="4.0分以上" value="4.0" />
                <el-option label="3.5分以上" value="3.5" />
              </el-select>
            </div>
          </el-col>
        </el-row>
        
        <div class="filter-actions">
          <el-button type="primary" @click="handleFilter">
            <el-icon><Filter /></el-icon>
            筛选
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </el-card>
    </div>

    <div class="search-results" v-if="hasSearched">
      <div class="results-header">
        <h2>搜索结果</h2>
        <div class="results-info">
          <span>共找到 <strong>{{ searchResults.length }}</strong> 个景点</span>
          <el-select v-model="sortBy" placeholder="排序方式" style="width: 150px">
            <el-option label="综合排序" value="default" />
            <el-option label="评分最高" value="rating" />
            <el-option label="价格最低" value="price" />
            <el-option label="浏览最多" value="views" />
          </el-select>
        </div>
      </div>

      <el-empty v-if="searchResults.length === 0" description="未找到相关景点" />
      
      <div class="results-grid" v-else>
        <el-row :gutter="20">
          <el-col 
            v-for="item in searchResults" 
            :key="item.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <el-card class="result-card" shadow="hover" @click="goToDetail(item.id)">
              <div class="card-image">
                <img :src="item.image" :alt="item.name" />
                <div class="card-rating">
                  <el-rate v-model="item.rating" disabled show-score />
                </div>
              </div>
              <div class="card-content">
                <h3 class="card-title">{{ item.name }}</h3>
                <p class="card-location">
                  <el-icon><Location /></el-icon>
                  {{ item.province }} · {{ item.city }}
                </p>
                <p class="card-description">{{ item.description }}</p>
                <div class="card-footer">
                  <span class="card-price" v-if="item.price">
                    ¥{{ item.price }}
                  </span>
                  <span class="card-price free" v-else>免费</span>
                  <div class="card-stats">
                    <span><el-icon><View /></el-icon> {{ item.views }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="pagination" v-if="searchResults.length > 0">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[8, 16, 24, 32]"
          :total="searchResults.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <div class="discover-section" v-else>
      <div class="section-header">
        <h2>
          <el-icon><Compass /></el-icon>
          发现精彩
        </h2>
      </div>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="discover-card" shadow="hover" @click="goToCategory('自然景观')">
            <div class="discover-image">
              <img src="https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400" alt="自然景观" />
              <div class="discover-overlay">
                <h3>自然景观</h3>
                <p>山川湖海，壮美河山</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="discover-card" shadow="hover" @click="goToCategory('历史文化')">
            <div class="discover-image">
              <img src="https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=400" alt="历史文化" />
              <div class="discover-overlay">
                <h3>历史文化</h3>
                <p>古韵悠长，文化传承</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="discover-card" shadow="hover" @click="goToCategory('主题公园')">
            <div class="discover-image">
              <img src="https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=400" alt="主题公园" />
              <div class="discover-overlay">
                <h3>主题公园</h3>
                <p>欢乐时光，尽情畅玩</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Filter, RefreshLeft, Location, View, Compass 
} from '@element-plus/icons-vue'

const router = useRouter()

const searchKeyword = ref('')
const hasSearched = ref(false)
const sortBy = ref('default')

const hotSearches = ref([
  '黄山', '西湖', '故宫', '九寨沟', '张家界', '丽江古城'
])

const filters = reactive({
  province: '',
  category: '',
  priceRange: '',
  rating: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 8
})

const searchResults = ref([])

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  hasSearched.value = true
  ElMessage.success(`搜索：${searchKeyword.value}`)
  
  searchResults.value = [
    {
      id: 1,
      name: '黄山风景区',
      description: '五岳归来不看山，黄山归来不看岳',
      province: '安徽',
      city: '黄山',
      image: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400',
      rating: 4.9,
      price: 190,
      views: 8765
    },
    {
      id: 2,
      name: '九寨沟风景区',
      description: '童话世界，人间仙境，五彩斑斓',
      province: '四川',
      city: '阿坝',
      image: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=400',
      rating: 4.9,
      price: 169,
      views: 6543
    }
  ]
}

const searchByTag = (tag) => {
  searchKeyword.value = tag
  handleSearch()
}

const handleFilter = () => {
  hasSearched.value = true
  ElMessage.success('筛选完成')
  
  searchResults.value = [
    {
      id: 1,
      name: '故宫博物院',
      description: '中国明清两代的皇家宫殿，世界文化遗产',
      province: '北京',
      city: '北京',
      image: 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=400',
      rating: 4.8,
      price: 60,
      views: 12580
    }
  ]
}

const resetFilters = () => {
  filters.province = ''
  filters.category = ''
  filters.priceRange = ''
  filters.rating = ''
  hasSearched.value = false
  searchResults.value = []
  ElMessage.info('已重置筛选条件')
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
}

const goToDetail = (id) => {
  ElMessage.info(`查看景点详情（ID: ${id}）`)
}

const goToCategory = (category) => {
  filters.category = category
  handleFilter()
}
</script>

<style scoped>
.search-page {
  padding: 30px 20px;
}

.search-header {
  margin-bottom: 30px;
}

.search-box {
  margin-bottom: 20px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 30px;
  padding: 4px 20px;
}

.search-box :deep(.el-input__inner) {
  font-size: 16px;
}

.hot-searches {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hot-label {
  font-size: 14px;
  color: #909399;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag:hover {
  transform: translateY(-2px);
  color: #409EFF;
}

.filter-section {
  margin-bottom: 30px;
}

.filter-item {
  margin-bottom: 15px;
}

.filter-item label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.filter-item .el-select {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.search-results {
  margin-bottom: 30px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.results-header h2 {
  font-size: 22px;
  color: #303133;
}

.results-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #606266;
}

.results-info strong {
  color: #409EFF;
  font-size: 16px;
}

.results-grid {
  margin-bottom: 30px;
}

.result-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
  overflow: hidden;
}

.result-card:hover {
  transform: translateY(-5px);
}

.card-image {
  position: relative;
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

.result-card:hover .card-image img {
  transform: scale(1.05);
}

.card-rating {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  padding: 4px 8px;
  border-radius: 4px;
}

.card-rating :deep(.el-rate__text) {
  color: #fff;
  font-size: 12px;
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

.card-description {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-price {
  font-size: 18px;
  font-weight: 600;
  color: #F56C6C;
}

.card-price.free {
  color: #67C23A;
}

.card-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.card-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.discover-section {
  margin-top: 40px;
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.discover-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
  overflow: hidden;
}

.discover-card:hover {
  transform: translateY(-5px);
}

.discover-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.discover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.discover-card:hover .discover-image img {
  transform: scale(1.05);
}

.discover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 30px 20px 20px;
  color: #fff;
}

.discover-overlay h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.discover-overlay p {
  font-size: 14px;
  opacity: 0.9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-page {
    padding: 20px 10px;
  }
  
  .results-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .results-info {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
