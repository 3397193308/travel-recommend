<template>
  <div class="admin-page">
  <el-card class="admin-card">
    <template #header>
      <div class="admin-toolbar">
        <div class="admin-toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索评论/用户名/景点" clearable style="width: 240px" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
            <el-option label="显示" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button type="success" plain :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">批量显示</el-button>
          <el-button type="warning" plain :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">批量隐藏</el-button>
        </div>
      </div>
    </template>
    <div class="admin-table-wrap">
    <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="destinationName" label="景点" width="160" show-overflow-tooltip />
      <el-table-column prop="content" label="评论内容" min-width="280" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
    <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="fetchList"
      />
  </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminBatchUpdateCommentStatus, adminListComments, adminUpdateCommentStatus } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const query = reactive({
  keyword: '',
  status: null,
  page: 1,
  pageSize: 10
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await adminListComments(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map((row) => row.id)
}

const handleSizeChange = () => {
  query.page = 1
  fetchList()
}

const toggleStatus = async (row) => {
  await adminUpdateCommentStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态更新成功')
  fetchList()
}

const batchUpdateStatus = async (status) => {
  await adminBatchUpdateCommentStatus(selectedIds.value, status)
  ElMessage.success('批量更新成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped></style>
