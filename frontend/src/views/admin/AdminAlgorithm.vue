<template>
  <div class="admin-page">
  <el-card class="admin-card">
    <template #header>
      <div class="admin-toolbar">
        <div>
          <div class="admin-title">推荐算法权重配置</div>
          <div class="admin-subtitle">可在线调整推荐模块的权重占比，保存后立即生效</div>
        </div>
      </div>
    </template>

    <el-form ref="formRef" :model="form" label-width="220px">
      <div class="admin-grid two">
        <el-card class="group-card">
          <template #header>用户相似度权重</template>
          <el-form-item label="评分相似度权重">
            <el-input-number v-model="form.userSimilarityRatingWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="行为相似度权重">
            <el-input-number v-model="form.userSimilarityBehaviorWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="偏好相似度权重">
            <el-input-number v-model="form.userSimilarityPreferenceWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="属性相似度权重">
            <el-input-number v-model="form.userSimilarityAttributeWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
        </el-card>

        <el-card class="group-card">
          <template #header>景点相似度权重</template>
          <el-form-item label="评分相似度权重">
            <el-input-number v-model="form.destinationSimilarityRatingWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="标签相似度权重">
            <el-input-number v-model="form.destinationSimilarityTagWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="分类相似度权重">
            <el-input-number v-model="form.destinationSimilarityCategoryWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="地点相似度权重">
            <el-input-number v-model="form.destinationSimilarityLocationWeight" :min="0" :max="1" :step="0.05" />
          </el-form-item>
        </el-card>
      </div>

      <el-card class="group-card mt16">
        <template #header>推荐策略占比</template>
        <el-form-item label="协同过滤占比">
          <el-input-number v-model="form.recommendationCollaborativeWeight" :min="0" :max="1" :step="0.05" />
        </el-form-item>
        <el-form-item label="偏好推荐占比">
          <el-input-number v-model="form.recommendationPreferenceWeight" :min="0" :max="1" :step="0.05" />
        </el-form-item>
        <el-form-item label="内容推荐占比">
          <el-input-number v-model="form.recommendationContentWeight" :min="0" :max="1" :step="0.05" />
        </el-form-item>
        <el-form-item label="热门补充占比">
          <el-input-number v-model="form.recommendationHotWeight" :min="0" :max="1" :step="0.05" />
        </el-form-item>
      </el-card>

      <div class="action-row">
        <el-button type="primary" @click="save">保存配置</el-button>
        <el-button @click="loadConfig">重置</el-button>
      </div>
    </el-form>
  </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlgorithmConfig, updateAlgorithmConfig } from '@/api/admin'

const form = reactive({
  id: null,
  userSimilarityRatingWeight: 0.35,
  userSimilarityBehaviorWeight: 0.3,
  userSimilarityPreferenceWeight: 0.25,
  userSimilarityAttributeWeight: 0.1,
  destinationSimilarityRatingWeight: 0.3,
  destinationSimilarityTagWeight: 0.3,
  destinationSimilarityCategoryWeight: 0.2,
  destinationSimilarityLocationWeight: 0.3,
  recommendationCollaborativeWeight: 0.5,
  recommendationPreferenceWeight: 0.25,
  recommendationContentWeight: 0.2,
  recommendationHotWeight: 0.05
})

const loadConfig = async () => {
  const res = await getAlgorithmConfig()
  Object.assign(form, res.data || {})
}

const save = async () => {
  await updateAlgorithmConfig(form)
  ElMessage.success('算法配置已保存')
}

onMounted(loadConfig)
</script>

<style scoped>
.group-card {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.group-card :deep(.el-card__header) {
  font-weight: 600;
  background: #f8fafc;
}

.action-row {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.mt16 {
  margin-top: 16px;
}
</style>
