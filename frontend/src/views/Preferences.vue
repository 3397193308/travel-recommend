<template>
  <div class="preferences-page">
    <div class="page-header">
      <h1>偏好设置</h1>
      <p>设置您的旅行偏好，获得更精准的推荐</p>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card class="preferences-card">
          <template #header>
            <div class="card-header">
              <el-icon><Star /></el-icon>
              <span>旅行偏好</span>
            </div>
          </template>

          <div class="preference-section">
            <h3>选择您感兴趣的标签</h3>
            <p class="section-desc">至少选择3个标签，我们将根据您的偏好为您推荐景点</p>
            
            <div class="tags-grid">
              <el-checkbox-group v-model="selectedPreferences">
                <el-checkbox-button 
                  v-for="tag in preferenceTags" 
                  :key="tag.id"
                  :label="tag.id"
                  :disabled="!isSelected(tag.id) && selectedPreferences.length >= 8"
                >
                  <el-icon><component :is="tag.icon" /></el-icon>
                  {{ tag.name }}
                </el-checkbox-button>
              </el-checkbox-group>
            </div>
          </div>

          <el-divider />

          <div class="preference-section">
            <h3>旅游预算</h3>
            <p class="section-desc">设置您的旅游预算范围</p>
            
            <div class="budget-slider">
              <el-slider
                v-model="budgetRange"
                range
                :min="0"
                :max="5000"
                :step="100"
                :format-tooltip="formatBudget"
                @change="handleBudgetChange"
              />
              <div class="budget-display">
                <span>¥{{ budgetRange[0] }}</span>
                <span>-</span>
                <span>¥{{ budgetRange[1] }}</span>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="preference-section">
            <h3>偏好权重</h3>
            <p class="section-desc">调整各项偏好的重要程度</p>
            
            <div class="weight-sliders">
              <div class="weight-item" v-for="(item, index) in weightItems" :key="index">
                <div class="weight-label">
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ item.label }}</span>
                </div>
                <el-rate 
                  v-model="item.value" 
                  :max="10"
                  show-score
                  score-template="{value}"
                />
              </div>
            </div>
          </div>

          <div class="save-actions">
            <el-button type="primary" size="large" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <el-button type="primary" size="large" @click="handleSave">
              <el-icon><Check /></el-icon>
              保存设置
            </el-button>
            <el-button size="large" @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              重置默认
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>设置说明</span>
            </div>
          </template>

          <div class="info-content">
            <div class="info-item">
              <h4>旅行偏好</h4>
              <p>选择您感兴趣的旅行类型，系统将根据您的选择推荐相关景点。</p>
            </div>

            <div class="info-item">
              <h4>旅游预算</h4>
              <p>设置您的预算范围，系统会优先推荐符合您预算的景点。</p>
            </div>

            <div class="info-item">
              <h4>偏好权重</h4>
              <p>调整各项偏好的重要程度，权重越高，推荐时考虑越多。</p>
            </div>

            <el-alert
              title="温馨提示"
              type="info"
              :closable="false"
              show-icon
            >
              您可以随时修改这些设置，推荐结果会实时更新。
            </el-alert>
          </div>
        </el-card>

        <el-card class="preview-card">
          <template #header>
            <div class="card-header">
              <el-icon><View /></el-icon>
              <span>当前偏好预览</span>
            </div>
          </template>

          <div class="preview-content">
            <div class="preview-item">
              <span class="preview-label">已选标签：</span>
              <div class="preview-tags">
                <el-tag 
                  v-for="tag in selectedPreferenceTags" 
                  :key="tag"
                  type="success"
                  size="small"
                >
                  {{ tag }}
                </el-tag>
                <span v-if="selectedPreferences.length === 0" class="empty-text">
                  暂未选择
                </span>
              </div>
            </div>

            <div class="preview-item">
              <span class="preview-label">预算范围：</span>
              <span class="preview-value">¥{{ budgetRange[0] }} - ¥{{ budgetRange[1] }}</span>
            </div>

            <div class="preview-item">
              <span class="preview-label">偏好数量：</span>
              <span class="preview-value">{{ selectedPreferences.length }} / 8</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Star, Check, RefreshLeft, InfoFilled, View, ArrowLeft,
  Location, Camera, Food, Sunny, Trophy, ShoppingBag,
  Compass, Collection, Reading, FirstAidKit
} from '@element-plus/icons-vue'
import { saveUserPreferences, getAllTags, getUserPreferences } from '../api/user.js'
import { useRouter } from 'vue-router'

const router = useRouter()

const selectedPreferences = ref([])
const budgetRange = ref([0, 2000])
const preferenceTags = ref([])
const loading = ref(false)

// 从API加载所有标签
const loadTags = async () => {
  try {
    const response = await getAllTags()
    if (response.code === 200 && response.data) {
      // 为每个标签分配一个图标（实际项目中应该从API获取）
      const iconMap = {
        '自然景观': 'Sunny',
        '历史文化': 'Collection',
        '亲子游': 'Location',
        '美食之旅': 'Food',
        '休闲度假': 'Compass',
        '冒险探险': 'Trophy',
        '摄影采风': 'Camera',
        '购物娱乐': 'ShoppingBag',
        '康养保健': 'FirstAidKit',
        '研学旅行': 'Reading'
      }
      
      preferenceTags.value = response.data.map(tag => ({
        ...tag,
        icon: iconMap[tag.name] || 'Location'
      }))
    }
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败，请稍后重试')
  }
}

// 从API加载用户偏好
const loadUserPreferences = async () => {
  try {
    const response = await getUserPreferences()
    if (response.code === 200 && response.data) {
      // 提取标签ID
      selectedPreferences.value = response.data.map(tag => tag.id)
      
      // 提取预算信息（如果有）
      if (response.data.length > 0) {
        const firstTag = response.data[0]
        if (firstTag.budget_min !== undefined && firstTag.budget_max !== undefined) {
          budgetRange.value = [firstTag.budget_min, firstTag.budget_max]
        }
      }
      
      // 更新权重项
      updateWeightItems(response.data)
    }
  } catch (error) {
    console.error('加载用户偏好失败:', error)
    ElMessage.error('加载用户偏好失败，请稍后重试')
  }
}

const weightItems = ref([])

// 根据选择的标签生成权重项
const updateWeightItems = (userPreferences = []) => {
  const selectedTags = selectedPreferences.value.map(id => {
    return preferenceTags.value.find(tag => tag.id === id)
  }).filter(Boolean)
  
  weightItems.value = selectedTags.map(tag => {
    // 查找用户偏好中的权重值
    const userPref = userPreferences.find(pref => pref.id === tag.id)
    const weight = userPref && userPref.weight ? userPref.weight : 5 // 默认权重
    
    return {
      label: tag.name,
      value: weight,
      icon: tag.icon
    }
  })
}

// 监听选择的标签变化，更新权重项
import { watch } from 'vue'
watch(selectedPreferences, () => updateWeightItems(), { deep: true, immediate: true })

const selectedPreferenceTags = computed(() => {
  return selectedPreferences.value.map(id => {
    const tag = preferenceTags.value.find(t => t.id === id)
    return tag ? tag.name : ''
  }).filter(name => name)
})

const isSelected = (id) => {
  return selectedPreferences.value.includes(id)
}

const formatBudget = (value) => {
  return `¥${value}`
}

const handleBudgetChange = (value) => {
  console.log('预算范围变更:', value)
}

const handleSave = async () => {
  if (selectedPreferences.value.length < 3) {
    ElMessage.warning('请至少选择3个偏好标签')
    return
  }
  
  try {
    const response = await saveUserPreferences({
      tags: selectedPreferences.value,
      budgetMin: budgetRange.value[0],
      budgetMax: budgetRange.value[1],
      weights: weightItems.value.reduce((acc, item) => {
        acc[item.label] = item.value
        return acc
      }, {})
    })
    
    if (response.code === 200) {
      ElMessage.success('偏好设置已保存')
    } else {
      ElMessage.error('保存失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('保存偏好设置失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

const handleReset = () => {
  selectedPreferences.value = [1, 2, 3]
  budgetRange.value = [0, 2000]
  updateWeightItems()
  
  ElMessage.info('已重置为默认设置')
}

const goBack = () => {
  router.back()
}

// 组件挂载时加载标签和用户偏好
onMounted(async () => {
  loading.value = true
  try {
    await loadTags()
    await loadUserPreferences()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.preferences-page {
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

.preferences-card,
.info-card,
.preview-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.preference-section {
  margin-bottom: 30px;
}

.preference-section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tags-grid :deep(.el-checkbox-button) {
  margin: 0;
}

.tags-grid :deep(.el-checkbox-button__inner) {
  padding: 10px 20px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.tags-grid :deep(.el-checkbox-button__inner:hover) {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.tags-grid :deep(.el-checkbox-button.is-checked .el-checkbox-button__inner) {
  color: #fff;
  background-color: #409EFF;
  border-color: #409EFF;
}

.budget-slider {
  padding: 20px 0;
}

.budget-display {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  font-size: 20px;
  font-weight: 600;
  color: #409EFF;
}

.weight-sliders {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.weight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.weight-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #303133;
}

.weight-label .el-icon {
  color: #409EFF;
}

.save-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item h4 {
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
}

.info-item p {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.preview-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.preview-label {
  font-size: 14px;
  color: #909399;
  min-width: 80px;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.empty-text {
  color: #C0C4CC;
  font-size: 13px;
}

.preview-value {
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .preferences-page {
    padding: 20px 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .tags-grid {
    gap: 8px;
  }
  
  .tags-grid :deep(.el-checkbox-button__inner) {
    padding: 8px 15px;
    font-size: 13px;
  }
  
  .save-actions {
    flex-direction: column;
  }
  
  .save-actions .el-button {
    width: 100%;
  }
}
</style>
