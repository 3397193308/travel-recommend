<template>
  <div class="publish-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/experiences')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-title">
          <h1>发布旅游体验</h1>
          <p class="header-desc">分享您的旅行见闻</p>
        </div>
      </div>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
        <el-form-item label="地区筛选">
          <el-row :gutter="12" style="width: 100%">
            <el-col :span="12">
              <el-select v-model="selectedProvince" placeholder="选择省份" clearable @change="onProvinceChange" style="width: 100%">
                <el-option v-for="item in provinces" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-col>
            <el-col :span="12">
              <el-select v-model="selectedCity" placeholder="选择城市" clearable :disabled="!selectedProvince" @change="onCityChange" style="width: 100%">
                <el-option v-for="item in cities" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="关联景点" prop="destinationId">
          <el-select
            v-model="form.destinationId"
            filterable
            remote
            reserve-keyword
            placeholder="请选择系统内景点（不可手输）"
            :remote-method="searchDestinations"
            :loading="destinationLoading"
            style="width: 100%"
          >
            <el-option v-for="item in destinationOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="体验标题" prop="title">
          <el-input v-model="form.title" maxlength="20" show-word-limit placeholder="1-20字，如：故宫避坑指南" />
        </el-form-item>

        <el-form-item label="体验正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            maxlength="2000"
            show-word-limit
            placeholder="10-2000字，支持换行分段"
          />
        </el-form-item>

        <el-form-item label="星级评分" prop="star">
          <el-rate v-model="form.star" size="large" />
        </el-form-item>

        <el-form-item label="上传图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleRemove"
            :limit="9"
            accept=".jpg,.jpeg,.png,.webp"
          >
            <el-icon><Plus /></el-icon>
            <div style="margin-top: 8px; font-size: 12px; color: #909399">上传图片</div>
          </el-upload>
          <div class="upload-tip">支持 JPG、PNG、WEBP 格式，最多上传 9 张</div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
              <el-icon><Check /></el-icon>
              提交审核
            </el-button>
            <el-button @click="router.push('/experiences')" size="large">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, ArrowLeft, Check, Close } from '@element-plus/icons-vue'
import { getDestinationList } from '@/api/destination'
import { getLocationsByLevel, getLocationsByParentId } from '@/api/location'
import { publishExperience, uploadExperienceImage } from '@/api/experience'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const destinationLoading = ref(false)
const destinationOptions = ref([])
const uploadFiles = ref([])

const provinces = ref([])
const cities = ref([])
const selectedProvince = ref(null)
const selectedCity = ref(null)
const selectedLocationId = ref(null)

const form = reactive({
  destinationId: null,
  title: '',
  content: '',
  star: 5
})

const rules = {
  destinationId: [{ required: true, message: '必须选择系统内景点', trigger: 'change' }],
  title: [{ required: true, min: 1, max: 20, message: '标题长度需在1-20字', trigger: 'blur' }],
  content: [{ required: true, min: 10, max: 2000, message: '正文长度需在10-2000字', trigger: 'blur' }],
  star: [{ required: true, type: 'number', min: 1, max: 5, message: '评分需在1-5星', trigger: 'change' }]
}

const searchDestinations = async (keyword = '') => {
  destinationLoading.value = true
  try {
    const res = await getDestinationList({
      page: 1,
      pageSize: 20,
      keyword,
      locationId: selectedLocationId.value || null
    })
    destinationOptions.value = res.data.list || []
  } finally {
    destinationLoading.value = false
  }
}

const onProvinceChange = async () => {
  selectedCity.value = null
  selectedLocationId.value = selectedProvince.value || null
  if (!selectedProvince.value) {
    cities.value = []
  } else {
    const res = await getLocationsByParentId(selectedProvince.value)
    cities.value = res.data || []
  }
  form.destinationId = null
  destinationOptions.value = []
  searchDestinations('')
}

const onCityChange = async () => {
  selectedLocationId.value = selectedCity.value || selectedProvince.value || null
  form.destinationId = null
  destinationOptions.value = []
  searchDestinations('')
}

const handleFileChange = (file, files) => {
  uploadFiles.value = files.map((item) => item.raw).filter(Boolean)
}

const handleRemove = (_, files) => {
  uploadFiles.value = files.map((item) => item.raw).filter(Boolean)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const uploadedUrls = []
    for (const file of uploadFiles.value) {
      const res = await uploadExperienceImage(file)
      uploadedUrls.push(res.data.imageUrl)
    }

    await publishExperience({
      ...form,
      imageUrls: uploadedUrls
    })
    ElMessage.success('发布成功，等待管理员审核')
    router.push('/my-experiences')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const res = await getLocationsByLevel(1)
  provinces.value = res.data || []
  await searchDestinations('')
})
</script>

<style scoped>
.publish-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  max-width: 960px;
  margin-left: auto;
  margin-right: auto;
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

.form-card {
  border-radius: 12px;
  border: none;
  max-width: 960px;
  margin: 0 auto;
}

.form-card :deep(.el-card__body) {
  padding: 32px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.form-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .publish-page {
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

  .form-card :deep(.el-card__body) {
    padding: 20px;
  }

  .form-actions {
    width: 100%;
  }

  .form-actions .el-button {
    flex: 1;
  }
}
</style>
