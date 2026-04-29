<template>
  <div class="profile-page">
    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <el-card class="hero-card">
          <div class="hero-main">
            <el-avatar :size="88" :src="profileForm.avatar">
              {{ profileForm.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="hero-meta">
              <h1>{{ profileForm.username || '用户' }}</h1>
              <p>{{ profileForm.email || '未填写邮箱' }}</p>
              <div class="meta-line">
                <span>手机号：{{ profileForm.phone || '未填写' }}</span>
                <span>性别：{{ genderText }}</span>
                <span>地区：{{ locationName }}</span>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="primary" @click="profileDialogVisible = true">编辑资料</el-button>
              <el-button @click="passwordDialogVisible = true">修改密码</el-button>
            </div>
          </div>
        </el-card>

        <el-row :gutter="14" class="overview-row">
          <el-col :xs="12" :sm="6">
            <el-card class="overview-card"><div class="value">{{ userStats.collections || 0 }}</div><div class="label">我的收藏</div></el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card class="overview-card"><div class="value">{{ userStats.ratings || 0 }}</div><div class="label">评分次数</div></el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card class="overview-card"><div class="value">{{ recentViews.length || 0 }}</div><div class="label">最近浏览</div></el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card class="overview-card"><div class="value">{{ userStats.comments || 0 }}</div><div class="label">评论数量</div></el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :xs="24" :lg="16">
            <el-card class="panel-card">
              <template #header>
                <div class="card-header"><el-icon><Clock /></el-icon><span>最近浏览</span></div>
              </template>
              <div v-if="recentViews.length > 0" class="list-block">
                <div v-for="item in recentViews" :key="`${item.id}-${item.viewTime}`" class="record-item" @click="goToDetail(item.id)">
                  <img :src="getImageUrl(item)" :alt="item.name" />
                  <div class="record-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.locationName || '未知地点' }}</p>
                    <span>{{ formatTime(item.viewTime) }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无浏览记录" />
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="8">
            <el-card class="panel-card">
              <template #header>
                <div class="card-header"><el-icon><Compass /></el-icon><span>快捷入口</span></div>
              </template>
              <div class="quick-actions">
                <el-button plain @click="router.push('/preferences')">偏好设置</el-button>
                <el-button plain @click="router.push('/my-experiences')">我的体验</el-button>
                <el-button plain @click="router.push('/collections')">我的收藏</el-button>
                <el-button plain @click="router.push('/experiences/publish')">发布体验</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="panel-card rating-card">
          <template #header>
            <div class="card-header"><el-icon><Star /></el-icon><span>我的评分</span></div>
          </template>
          <div v-if="myRatings.length > 0" class="list-block">
            <div v-for="item in myRatings" :key="`${item.id}-${item.ratingTime}`" class="record-item" @click="goToDetail(item.id)">
              <img :src="getImageUrl(item)" :alt="item.name" />
              <div class="record-info">
                <h4>{{ item.name }}</h4>
                <el-rate :model-value="item.score" disabled show-score />
                <p>{{ item.comment || '暂无评论' }}</p>
                <span>{{ formatTime(item.ratingTime) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无评分记录" />
        </el-card>
      </template>
    </el-skeleton>

    <el-dialog v-model="profileDialogVisible" title="编辑个人资料" width="560px">
      <el-form label-width="90px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleCustomUpload"
            :before-upload="beforeUpload">
            <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div style="margin-top: 8px; color: #909399; font-size: 12px;">建议尺寸 200x200，支持 jpg、png</div>
        </el-form-item>
        <el-form-item label="邮箱"><el-input v-model="profileForm.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="profileForm.phone" /></el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="profileForm.age" :min="1" :max="120" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="profileForm.gender" clearable style="width: 100%">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在地">
          <el-row :gutter="10" style="width: 100%">
            <el-col :span="12">
              <el-select v-model="selectedProvinceId" clearable placeholder="选择省份" style="width: 100%" @change="handleProvinceChange">
                <el-option v-for="loc in provinceOptions" :key="loc.id" :label="loc.name" :value="loc.id" />
              </el-select>
            </el-col>
            <el-col :span="12">
              <el-select v-model="selectedCityId" clearable placeholder="选择城市" :disabled="!selectedProvinceId" style="width: 100%" @change="handleCityChange">
                <el-option v-for="loc in cityOptions" :key="loc.id" :label="loc.name" :value="loc.id" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="520px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Compass, Star, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getMyRatings,
  getProfileOverview,
  getRecentViews,
  getUserInfo,
  updatePassword,
  updateUserInfo
} from '@/api/user'
import { getAllLocations } from '@/api/location'
import request from '@/api/request'
import { getDestinationDefaultImage, normalizeImageUrl } from '@/utils/defaultImages'

const router = useRouter()
const userStore = useUserStore()
const defaultImage = 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=240'
const locations = ref([])
const recentViews = ref([])
const myRatings = ref([])

// 获取景点的显示图片
const getImageUrl = (item) => {
  if (item.imageUrl) return normalizeImageUrl(item.imageUrl)
  return getDestinationDefaultImage(item)
}
const passwordFormRef = ref(null)
const loading = ref(false)
const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const selectedProvinceId = ref(null)
const selectedCityId = ref(null)

const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  avatar: '',
  age: null,
  gender: null,
  locationId: null
})

const userStats = reactive({
  collections: 0,
  ratings: 0,
  comments: 0
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }]
}

const genderText = computed(() => {
  if (profileForm.gender === 1) return '男'
  if (profileForm.gender === 2) return '女'
  return '未设置'
})

const locationName = computed(() => {
  const hit = locations.value.find((item) => item.id === profileForm.locationId)
  return hit?.name || '未设置'
})

const provinceOptions = computed(() => locations.value.filter((item) => item.level === 1))
const cityOptions = computed(() =>
  locations.value.filter((item) => item.level === 2 && item.parentId === selectedProvinceId.value)
)

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString()
}

const loadProfile = async () => {
  loading.value = true
  try {
    const [userRes, overviewRes, viewsRes, ratingsRes, locationsRes] = await Promise.all([
      getUserInfo(),
      getProfileOverview(),
      getRecentViews(10),
      getMyRatings(10),
      getAllLocations()
    ])
    Object.assign(profileForm, userRes.data || {})
    Object.assign(userStats, overviewRes.data || {})
    recentViews.value = viewsRes.data || []
    myRatings.value = ratingsRes.data || []
    locations.value = locationsRes.data || []
    syncLocationSelection()
  } finally {
    loading.value = false
  }
}

const syncLocationSelection = () => {
  if (!profileForm.locationId) {
    selectedProvinceId.value = null
    selectedCityId.value = null
    return
  }

  const current = locations.value.find((item) => item.id === profileForm.locationId)
  if (!current) {
    selectedProvinceId.value = null
    selectedCityId.value = null
    return
  }

  if (current.level === 1) {
    selectedProvinceId.value = current.id
    selectedCityId.value = null
  } else {
    selectedProvinceId.value = current.parentId
    selectedCityId.value = current.id
  }
}

const handleProvinceChange = (value) => {
  selectedCityId.value = null
  profileForm.locationId = value || null
}

const handleCityChange = (value) => {
  profileForm.locationId = value || selectedProvinceId.value || null
}

const saveProfile = async () => {
  await updateUserInfo({
    email: profileForm.email,
    phone: profileForm.phone,
    avatar: profileForm.avatar,
    age: profileForm.age,
    gender: profileForm.gender,
    locationId: profileForm.locationId
  })
  userStore.setUserInfo({ ...userStore.userInfo, ...profileForm })
  ElMessage.success('个人信息已更新')
  profileDialogVisible.value = false
}

const savePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate()
  await updatePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword
  })
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  ElMessage.success('密码修改成功')
  passwordDialogVisible.value = false
}

const goToDetail = (id) => {
  router.push(`/destination/${id}`)
}

// 头像上传相关函数
const beforeUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 5
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    profileForm.avatar = response.data.imageUrl
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error) => {
  const message = error?.message || '上传失败，请稍后重试'
  ElMessage.error(message)
}

const handleCustomUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  const uploadBaseURL = import.meta.env.VITE_UPLOAD_BASE_URL || 'http://localhost:8082'
  try {
    const response = await request({
      baseURL: uploadBaseURL,
      url: '/api/user/upload/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    handleUploadSuccess(response)
    if (typeof options.onSuccess === 'function') {
      options.onSuccess(response)
    }
  } catch (error) {
    handleUploadError(error)
    if (typeof options.onError === 'function') {
      options.onError(error)
    }
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { padding: 24px 20px; }
.hero-card { margin-bottom: 14px; border-radius: 12px; }
.hero-main { display: flex; align-items: center; gap: 20px; }
.hero-meta { flex: 1; }
.hero-meta h1 { margin: 0 0 8px; font-size: 28px; color: #1f2d3d; }
.hero-meta p { margin: 0 0 10px; color: #606266; }
.meta-line { display: flex; gap: 14px; flex-wrap: wrap; color: #909399; font-size: 13px; }
.hero-actions { display: flex; flex-direction: column; gap: 8px; }
.overview-row { margin-bottom: 12px; }
.overview-card { text-align: center; border-radius: 10px; }
.overview-card .value { font-size: 28px; font-weight: 700; color: #303133; }
.overview-card .label { font-size: 13px; color: #909399; margin-top: 6px; }
.panel-card { margin-bottom: 14px; border-radius: 10px; }
.rating-card { margin-top: 6px; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.list-block { display: flex; flex-direction: column; gap: 10px; }
.record-item { display: flex; gap: 12px; padding: 10px; border-radius: 8px; cursor: pointer; transition: background-color 0.2s ease; }
.record-item:hover { background: #f5f7fa; }
.record-item img { width: 90px; height: 72px; object-fit: cover; border-radius: 6px; }
.record-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.record-info h4 { margin: 0; color: #303133; }
.record-info p { margin: 0; color: #606266; font-size: 13px; }
.record-info span { color: #909399; font-size: 12px; }
.quick-actions { display: grid; grid-template-columns: 1fr; gap: 10px; }

/* 头像上传相关样式 */
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  border-radius: 50%;
  width: 100px;
  height: 100px;
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .profile-page { padding: 14px 10px; }
  .hero-main { flex-direction: column; align-items: flex-start; }
  .hero-actions { width: 100%; flex-direction: row; }
  .hero-actions .el-button { flex: 1; }
}
</style>
