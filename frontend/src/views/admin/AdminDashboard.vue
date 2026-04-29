<template>
  <div class="admin-page">
    <el-row :gutter="16" class="mb16">
      <el-col :span="6">
        <div class="admin-stat-card admin-stat-blue">
          <div class="label">用户总数</div>
          <div class="value">{{ stats.userCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="admin-stat-card admin-stat-cyan">
          <div class="label">景点总数</div>
          <div class="value">{{ stats.destinationCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="admin-stat-card admin-stat-emerald">
          <div class="label">评论总数</div>
          <div class="value">{{ stats.commentCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="admin-stat-card admin-stat-violet">
          <div class="label">评分总数</div>
          <div class="value">{{ stats.ratingCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="admin-card">
          <template #header>最近注册用户</template>
          <div class="admin-table-wrap">
            <el-table :data="stats.recentUsers || []" size="small" max-height="380">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="admin-card">
          <template #header>最新评论</template>
          <div class="admin-table-wrap">
            <el-table :data="stats.recentComments || []" size="small" max-height="380">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="destinationName" label="景点" width="140" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminDashboardStats } from '@/api/admin'

const stats = ref({})

const fetchStats = async () => {
  const res = await adminDashboardStats()
  stats.value = res.data || {}
}

onMounted(fetchStats)
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}
</style>
