<template>
  <div class="destination-detail" v-loading="loading">
    <div v-if="destination" class="detail-content">
      <div class="detail-header">
        <el-button @click="goBack" text>
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>

      <div class="main-info">
        <div class="image-section">
          <el-carousel height="400px" :autoplay="false">
            <el-carousel-item v-for="(img, index) in allImages" :key="index">
              <img :src="img" :alt="destination.name" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="info-section">
          <h1 class="title">{{ destination.name }}</h1>
          
          <div class="tags">
            <el-tag v-for="tag in destination.tags" :key="tag.id" type="primary" effect="plain">
              {{ tag.name }}
            </el-tag>
          </div>
          
          <div class="categories" v-if="destination.categories && destination.categories.length > 0">
            <el-tag v-for="category in destination.categories" :key="category.id" type="info" effect="plain">
              {{ category.name }}
            </el-tag>
          </div>

          <div class="rating-section">
            <el-rate v-model="destination.averageRating" disabled show-score text-color="#ff9900" />
            <span class="rating-count">{{ destination.ratingCount }} 人评价</span>
          </div>

          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ destination.province }} {{ destination.city }} {{ destination.address }}</span>
          </div>

          <div class="info-item">
            <el-icon><Ticket /></el-icon>
            <span class="price" v-if="destination.ticketPrice">¥{{ destination.ticketPrice }}</span>
            <span class="price free" v-else>免费</span>
          </div>

          <div class="stats">
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ destination.viewCount }} 浏览</span>
            </div>
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ destination.collectCount }} 收藏</span>
            </div>
          </div>

          <div class="action-buttons">
            <el-button 
              :type="destination.isCollected ? 'danger' : 'primary'" 
              size="large"
              @click="handleCollect"
            >
              <el-icon><Star /></el-icon>
              {{ destination.isCollected ? '取消收藏' : '收藏景点' }}
            </el-button>
          </div>

          <div class="user-rating" v-if="userStore.token">
            <span>我的评分：</span>
            <el-rate 
              v-model="userScore" 
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              @change="handleRate"
            />
          </div>
        </div>
      </div>

      <div class="description-section">
        <h2>景点介绍</h2>
        <p>{{ destination.description }}</p>
      </div>

      <div class="experience-section">
        <div class="experience-header">
          <h2>相关旅游体验</h2>
          <div>
            <el-button text type="primary" @click="goToExperienceCommunity">去体验社区</el-button>
            <el-button type="primary" plain @click="goToPublishExperience">发布体验</el-button>
          </div>
        </div>
        <div v-if="experienceList.length === 0">
          <el-empty description="该景点暂无体验分享" />
        </div>
        <div v-else class="experience-list">
          <div v-for="item in experienceList" :key="item.id" class="experience-item">
            <div class="experience-top">
              <strong>{{ item.title }}</strong>
              <el-rate :model-value="item.star" disabled />
            </div>
            <div class="experience-meta">
              <span>{{ item.username }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
            <p class="experience-content">{{ item.content }}</p>
            <div v-if="item.images && item.images.length > 0" class="experience-images">
              <el-image
                v-for="img in item.images.slice(0, 3)"
                :key="img.id"
                :src="normalizeImageUrl(img.imageUrl)"
                fit="cover"
                :preview-src-list="item.images.map((v) => normalizeImageUrl(v.imageUrl))"
                preview-teleported
              />
            </div>
          </div>
        </div>
      </div>

      <div class="comments-section">
        <h2>用户评论 ({{ commentsTotal }})</h2>
        
        <div class="comment-form" v-if="userStore.token">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            maxlength="500"
            show-word-limit
          />
          <el-button type="primary" @click="submitComment" :disabled="!commentContent.trim()">
            发表评论
          </el-button>
        </div>
        <div v-else class="login-tip">
          <el-alert title="登录后才能发表评论" type="info" :closable="false" show-icon />
        </div>

        <div class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="40">
                {{ comment.username?.charAt(0).toUpperCase() }}
              </el-avatar>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="username">{{ comment.username }}</span>
                <span class="time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
            </div>
          </div>
          <el-empty v-if="comments.length === 0" description="暂无评论" />
        </div>

        <div class="pagination" v-if="commentsTotal > commentPageSize">
          <el-pagination
            v-model:current-page="commentPage"
            :page-size="commentPageSize"
            :total="commentsTotal"
            layout="prev, pager, next"
            @current-change="fetchComments"
          />
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="景点不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Location, Ticket, View, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getDestinationDetail, collectDestination, uncollectDestination, rateDestination, getComments, addComment } from '@/api/destination'
import { listExperiences } from '@/api/experience'
import { getDestinationDefaultImage, normalizeImageUrl } from '@/utils/defaultImages'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const destination = ref(null)
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentsTotal = ref(0)
const commentContent = ref('')
const userScore = ref(0)
const experienceList = ref([])

const allImages = computed(() => {
  if (!destination.value) return [getDestinationDefaultImage(null)]
  const images = []
  if (destination.value.imageUrl) {
    images.push(normalizeImageUrl(destination.value.imageUrl))
  }
  if (destination.value.imageUrls && destination.value.imageUrls.length > 0) {
    images.push(...destination.value.imageUrls.map(url => normalizeImageUrl(url)))
  }
  return images.length > 0 ? images : [getDestinationDefaultImage(destination.value)]
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getDestinationDetail(route.params.id)
    if (res.code === 200) {
      destination.value = res.data
      userScore.value = res.data.userScore || 0
    } else {
      ElMessage.error(res.message || '获取景点详情失败')
    }
  } catch (error) {
    console.error('获取景点详情失败:', error)
    ElMessage.error('获取景点详情失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id, commentPage.value, commentPageSize.value)
    if (res.code === 200) {
      comments.value = res.data.list
      commentsTotal.value = res.data.total
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const fetchExperiences = async () => {
  try {
    const res = await listExperiences({
      destinationId: route.params.id,
      page: 1,
      pageSize: 5
    })
    if (res.code === 200) {
      experienceList.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取旅游体验失败:', error)
  }
}

const handleCollect = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (destination.value.isCollected) {
      const res = await uncollectDestination(destination.value.id)
      if (res.code === 200) {
        destination.value.isCollected = false
        destination.value.collectCount--
        ElMessage.success('取消收藏成功')
      }
    } else {
      const res = await collectDestination(destination.value.id)
      if (res.code === 200) {
        destination.value.isCollected = true
        destination.value.collectCount++
        ElMessage.success('收藏成功')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleRate = async (score) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await rateDestination({
      destinationId: destination.value.id,
      score: score
    })
    if (res.code === 200) {
      ElMessage.success('评分成功')
      fetchDetail()
    }
  } catch (error) {
    console.error('评分失败:', error)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  try {
    const res = await addComment({
      destinationId: destination.value.id,
      content: commentContent.value
    })
    if (res.code === 200) {
      ElMessage.success('评论发表成功')
      commentContent.value = ''
      commentPage.value = 1
      fetchComments()
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const goBack = () => {
  router.push('/destinations')
}

const goToExperienceCommunity = () => {
  router.push('/experiences')
}

const goToPublishExperience = () => {
  router.push('/experiences/publish')
}

onMounted(() => {
  fetchDetail()
  fetchComments()
  fetchExperiences()
})
</script>

<style scoped>
.destination-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 20px;
}

.main-info {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.image-section {
  flex: 1;
  max-width: 600px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.info-section {
  flex: 1;
}

.title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 15px;
}

.tags {
  margin-bottom: 15px;
  display: block;
  clear: both;
}

.tags .el-tag {
  margin-right: 8px;
}

.categories {
  margin-bottom: 15px;
  display: block;
  clear: both;
}

.categories .el-tag {
  margin-right: 8px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.rating-count {
  color: #909399;
  font-size: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  color: #606266;
  font-size: 15px;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.price.free {
  color: #67c23a;
}

.stats {
  display: flex;
  gap: 30px;
  margin: 20px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
}

.action-buttons {
  margin: 20px 0;
}

.user-rating {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.description-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.experience-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.experience-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.experience-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.experience-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.experience-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.experience-meta {
  margin: 8px 0;
  color: #909399;
  font-size: 13px;
  display: flex;
  gap: 12px;
}

.experience-content {
  white-space: pre-wrap;
  color: #606266;
}

.experience-images {
  display: flex;
  gap: 8px;
}

.experience-images .el-image {
  width: 90px;
  height: 70px;
  border-radius: 4px;
}

.description-section h2 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.description-section p {
  color: #606266;
  line-height: 1.8;
  text-indent: 2em;
}

.comments-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.comments-section h2 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 25px;
}

.comment-form .el-textarea {
  margin-bottom: 10px;
}

.login-tip {
  margin-bottom: 20px;
}

.comments-list {
  min-height: 200px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 13px;
}

.comment-text {
  color: #606266;
  line-height: 1.6;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .main-info {
    flex-direction: column;
  }
  
  .image-section {
    max-width: 100%;
  }
}
</style>
