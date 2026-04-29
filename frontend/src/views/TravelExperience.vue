<template>
  <div class="experience-page">
    <div class="page-header">
      <div class="header-title">
        <h1>旅游体验分享</h1>
        <p class="header-desc">发现精彩旅行，分享真实体验</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="goPublish">
          <el-icon><Plus /></el-icon>
          发布体验
        </el-button>
        <el-button @click="goMine">我的体验</el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input 
            v-model="query.keyword" 
            placeholder="搜索标题/内容" 
            clearable 
            @keyup.enter="handleSearch"
            size="large"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select 
            v-model="selectedProvince" 
            placeholder="选择省份" 
            clearable 
            @change="handleProvinceChange"
            size="large"
            style="width: 100%"
          >
            <el-option v-for="item in provinces" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select 
            v-model="selectedCity" 
            placeholder="选择城市" 
            clearable 
            :disabled="!selectedProvince" 
            @change="handleCityChange"
            size="large"
            style="width: 100%"
          >
            <el-option v-for="item in cities" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" class="filter-buttons">
          <el-button type="primary" @click="handleSearch" size="large">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset" size="large">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <div v-loading="loading" class="list-container">
      <el-empty v-if="!loading && list.length === 0" description="暂无旅游体验" />
      
      <div v-else class="experience-list">
        <el-card 
          v-for="item in list" 
          :key="item.id" 
          class="experience-card" 
          shadow="hover"
          @click="goDetail(item.id)"
        >
          <div class="card-header">
            <h3 class="card-title">{{ item.title }}</h3>
            <div class="card-rating">
              <el-icon class="star-icon"><Star /></el-icon>
              <span class="rating-num">{{ item.star }}.0</span>
            </div>
          </div>

          <div class="card-meta">
            <div class="meta-left">
              <el-icon class="meta-icon"><Location /></el-icon>
              <span>{{ item.destinationName }}</span>
              <span class="meta-separator">·</span>
              <span>{{ item.provinceName }} {{ item.cityName }}</span>
            </div>
            <div class="meta-right">
              <el-icon class="meta-icon"><User /></el-icon>
              <span>{{ item.username }}</span>
              <span class="meta-separator">·</span>
              <el-icon class="meta-icon"><Clock /></el-icon>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>

          <p class="card-content">
            {{ item.content }}
            <span v-if="item.content.length > 100" class="expand-text">查看全文</span>
          </p>

          <div v-if="item.images && item.images.length > 0" class="card-images">
            <el-image
              v-for="img in item.images.slice(0, 4)"
              :key="img.id"
              :src="normalizeImageUrl(img.imageUrl)"
              fit="cover"
              :preview-src-list="item.images.map((v) => normalizeImageUrl(v.imageUrl))"
              preview-teleported
              class="card-image"
              @click.stop
            />
            <div v-if="item.images.length > 4" class="image-more">
              +{{ item.images.length - 4 }}
            </div>
          </div>

          <div class="card-actions" @click.stop>
            <el-button text class="action-btn" @click="handleLike(item)">
              <el-icon><ChatDotRound /></el-icon>
              <span>点赞</span>
            </el-button>
            <el-button text class="action-btn" @click="handleComment(item)">
              <el-icon><ChatLineSquare /></el-icon>
              <span>评论</span>
            </el-button>
            <el-button text class="action-btn" @click="handleCollect(item)">
              <el-icon><Star /></el-icon>
              <span>收藏</span>
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchList"
        @size-change="handleSizeChange"
        prev-text="上一页"
        next-text="下一页"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, RefreshLeft, Location, User, Clock, Star, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'
import { getLocationsByLevel, getLocationsByParentId } from '@/api/location'
import { listExperiences } from '@/api/experience'
import { normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const provinces = ref([])
const cities = ref([])
const selectedProvince = ref(null)
const selectedCity = ref(null)

const query = reactive({
  page: 1,
  pageSize: 6,
  keyword: '',
  locationId: null
})



const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listExperiences(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const fetchProvinces = async () => {
  const res = await getLocationsByLevel(1)
  provinces.value = res.data || []
}

const handleProvinceChange = async () => {
  selectedCity.value = null
  query.locationId = selectedProvince.value || null
  if (!selectedProvince.value) {
    cities.value = []
    return
  }
  const res = await getLocationsByParentId(selectedProvince.value)
  cities.value = res.data || []
}

const handleCityChange = () => {
  query.locationId = selectedCity.value || selectedProvince.value || null
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.page = 1
  query.keyword = ''
  query.locationId = null
  selectedProvince.value = null
  selectedCity.value = null
  cities.value = []
  fetchList()
}

const handleSizeChange = () => {
  query.page = 1
  fetchList()
}

const goPublish = () => {
  router.push('/experiences/publish')
}

const goMine = () => {
  router.push('/my-experiences')
}

const goDetail = (id) => {
  ElMessage.info('查看详情功能开发中')
}

const handleLike = (item) => {
  ElMessage.success('点赞成功')
}

const handleComment = (item) => {
  ElMessage.info('评论功能开发中')
}

const handleCollect = (item) => {
  ElMessage.success('收藏成功')
}

onMounted(async () => {
  await Promise.all([fetchProvinces(), fetchList()])
})
</script>

<style scoped>
.experience-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.header-desc {
  margin: 8px 0 0 0;
  color: #64748b;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.filter-buttons {
  display: flex;
  gap: 12px;
}

.list-container {
  min-height: 400px;
}

.experience-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.experience-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.experience-card:hover {
  transform: translateY(-2px);
}

.experience-card :deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
  flex: 1;
  padding-right: 16px;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  padding: 6px 12px;
  border-radius: 20px;
}

.star-icon {
  color: #fff;
  font-size: 14px;
}

.rating-num {
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.meta-left,
.meta-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  font-size: 14px;
  color: #94a3b8;
}

.meta-separator {
  color: #cbd5e1;
}

.card-content {
  margin: 0 0 16px 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.expand-text {
  color: #3b82f6;
  cursor: pointer;
}

.expand-text:hover {
  text-decoration: underline;
}

.card-images {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.card-image {
  width: 100%;
  height: 100px;
  border-radius: 8px;
  position: relative;
}

.image-more {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  border-radius: 8px;
}

.card-actions {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 6px;
}

.action-btn:hover {
  background: #f1f5f9;
  color: #3b82f6;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .experience-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-title h1 {
    font-size: 22px;
  }

  .card-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .card-images {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
