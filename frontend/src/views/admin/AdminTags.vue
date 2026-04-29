<template>
  <div class="admin-page">
  <el-card class="admin-card">
    <template #header>
      <div class="admin-toolbar">
        <div class="admin-toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索标签" clearable style="width: 220px" />
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button type="success" plain :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">批量启用</el-button>
          <el-button type="warning" plain :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">批量禁用</el-button>
        </div>
        <el-button type="primary" @click="openCreate">新增标签</el-button>
      </div>
    </template>
    <div class="admin-table-wrap">
    <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
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

  <el-dialog v-model="visible" :title="form.id ? '编辑标签' : '新增标签'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminBatchUpdateTagStatus, adminListTags, adminSaveTag, adminUpdateTagStatus } from '@/api/admin'

const loading = ref(false)
const visible = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])
const formRef = ref()

const query = reactive({
  keyword: '',
  page: 1,
  pageSize: 10
})

const emptyForm = () => ({
  id: null,
  name: '',
  description: '',
  sortOrder: 0,
  status: 1
})
const form = reactive(emptyForm())
const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await adminListTags(query)
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

const openCreate = () => {
  Object.assign(form, emptyForm())
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, { ...row })
  visible.value = true
}

const save = async () => {
  await formRef.value.validate()
  await adminSaveTag(form)
  ElMessage.success('保存成功')
  visible.value = false
  fetchList()
}

const toggleStatus = async (row) => {
  await adminUpdateTagStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态更新成功')
  fetchList()
}

const batchUpdateStatus = async (status) => {
  await adminBatchUpdateTagStatus(selectedIds.value, status)
  ElMessage.success('批量更新成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped></style>
