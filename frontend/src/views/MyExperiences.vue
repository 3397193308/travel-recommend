<template>
  <div class="my-experience-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/experiences')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-title">
          <h1>我的体验</h1>
          <p class="header-desc">管理您发布的旅游体验</p>
        </div>
      </div>
      <el-button type="primary" @click="router.push('/experiences/publish')" size="large">
        <el-icon><Plus /></el-icon>
        发布新体验
      </el-button>
    </div>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-select 
          v-model="query.status" 
          clearable 
          placeholder="审核状态" 
          @change="handleSearch"
          size="large"
          style="width: 160px"
        >
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </div>
    </el-card>

    <div v-loading="loading" class="list-container">
      <el-empty v-if="!loading && list.length === 0" description="暂无数据" />
      
      <div v-else class="experience-list">
        <el-card 
          v-for="item in list" 
          :key="item.id" 
          class="experience-card" 
          shadow="hover"
        >
          <div class="card-header">
            <h3 class="card-title">{{ item.title }}</h3>
            <div class="card-status">
              <el-tag v-if="item.status === 0" type="warning" effect="light">待审核</el-tag>
              <el-tag v-else-if="item.status === 1" type="success" effect="light">已通过</el-tag>
              <el-tag v-else type="danger" effect="light">已驳回</el-tag>
            </div>
          </div>

          <div class="card-meta">
            <div class="meta-left">
              <el-icon class="meta-icon"><Location /></el-icon>
              <span>{{ item.destinationName }}</span>
              <span class="meta-separator">·</span>
              <el-icon class="meta-icon"><Clock /></el-icon>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="card-rating">
              <el-rate :model-value="item.star" disabled show-score />
            </div>
          </div>

          <p class="card-content">{{ item.content }}</p>

          <div v-if="item.images && item.images.length > 0" class="card-images">
            <el-image
              v-for="img in item.images.slice(0, 4)"
              :key="img.id"
              :src="normalizeImageUrl(img.imageUrl)"
              fit="cover"
              :preview-src-list="item.images.map((v) => normalizeImageUrl(v.imageUrl))"
              preview-teleported
              class="card-image"
            />
            <div v-if="item.images.length > 4" class="image-more">
              +{{ item.images.length - 4 }}
            </div>
          </div>

          <div v-if="item.status === 2 && item.rejectReason" class="reject-reason">
            <el-icon class="reject-icon"><WarningFilled /></el-icon>
            <div class="reject-text">
              <span class="reject-label">驳回原因：</span>
              <span class="reject-content">{{ item.rejectReason }}</span>
            </div>
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
import { ArrowLeft, Plus, Location, Clock, WarningFilled } from '@element-plus/icons-vue'
import { myExperiences } from '@/api/experience'
import { normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const list = ref([])

const query = reactive({
  status: null,
  page: 1,
  pageSize: 6
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await myExperiences(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleSizeChange = () => {
  query.page = 1
  fetchList()
}

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
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

onMounted(fetchList)
</script>

<style scoped>
.my-experience-page {
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

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #64748b;
  font-size: 14px;
}

.back-btn:hover {
  color: #3b82f6;
  background: #eff6ff;
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

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.filter-row {
  display: flex;
  align-items: center;
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

.meta-left {
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

.reject-reason {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 16px;
  background: #fef2f2;
  border-radius: 8px;
  border-left: 3px solid #ef4444;
}

.reject-icon {
  color: #ef4444;
  font-size: 18px;
  margin-top: 2px;
  flex-shrink: 0;
}

.reject-text {
  flex: 1;
  font-size: 14px;
}

.reject-label {
  color: #991b1b;
  font-weight: 500;
}

.reject-content {
  color: #7f1d1d;
  margin-left: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .my-experience-page {
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
    gap: 12px;
  }
}
</style>
