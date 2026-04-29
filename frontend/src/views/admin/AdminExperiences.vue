<template>
  <div class="admin-page">
    <el-card class="admin-card">
      <template #header>
        <div class="admin-toolbar">
          <div class="admin-toolbar-left">
            <el-input v-model="query.keyword" placeholder="搜索标题/内容/用户名/景点" clearable style="width: 260px" />
            <el-select v-model="query.status" placeholder="审核状态" clearable style="width: 160px">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchList">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="destinationName" label="景点" width="160" show-overflow-tooltip />
        <el-table-column label="评分" width="120">
          <template #default="{ row }">
            <el-rate :model-value="row.star" disabled />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">查看</el-button>
            <el-button v-if="row.status !== 1" link type="success" @click="audit(row, 1)">通过</el-button>
            <el-button v-if="row.status !== 2" link type="danger" @click="openReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="体验详情" width="720px">
      <div v-if="detail">
        <h3>{{ detail.title }}</h3>
        <div class="meta">
          <span>用户：{{ detail.username }}</span>
          <span>景点：{{ detail.destinationName }}</span>
          <span>评分：{{ detail.star }}星</span>
        </div>
        <p class="content">{{ detail.content }}</p>
        <div v-if="detail.images && detail.images.length > 0" class="images">
          <el-image
            v-for="img in detail.images"
            :key="img.id"
            :src="normalizeImageUrl(img.imageUrl)"
            fit="cover"
            :preview-src-list="detail.images.map((v) => normalizeImageUrl(v.imageUrl))"
            preview-teleported
          />
        </div>
        <div v-if="detail.status === 2 && detail.rejectReason" class="reject">
          驳回原因：{{ detail.rejectReason }}
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回原因" width="500px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" maxlength="255" show-word-limit />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAuditExperience, adminGetExperienceDetail, adminListExperiences } from '@/api/admin'
import { normalizeImageUrl } from '@/utils/defaultImages'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)
const rejectVisible = ref(false)
const rejectReason = ref('')
const pendingRejectId = ref(null)

const query = reactive({
  keyword: '',
  status: null,
  page: 1,
  pageSize: 10
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await adminListExperiences(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  const res = await adminGetExperienceDetail(row.id)
  detail.value = res.data
  detailVisible.value = true
}

const audit = async (row, status) => {
  await adminAuditExperience(row.id, {
    status
  })
  ElMessage.success('审核成功')
  fetchList()
}

const openReject = (row) => {
  pendingRejectId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  await adminAuditExperience(pendingRejectId.value, {
    status: 2,
    rejectReason: rejectReason.value
  })
  ElMessage.success('已驳回')
  rejectVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.admin-page { padding: 4px; }
.meta { color: #909399; display: flex; gap: 12px; margin: 8px 0; flex-wrap: wrap; }
.content { white-space: pre-wrap; }
.images { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.images .el-image { width: 120px; height: 90px; border-radius: 6px; }
.reject { margin-top: 10px; padding: 8px; color: #f56c6c; background: #fff2f0; border-radius: 6px; }
</style>
