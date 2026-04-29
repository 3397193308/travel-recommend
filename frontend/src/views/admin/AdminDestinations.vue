<template>
  <div class="admin-page">
  <el-card class="admin-card">
    <template #header>
      <div class="admin-toolbar">
        <div class="admin-toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索景点名" clearable style="width: 220px" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button type="success" plain :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">批量上架</el-button>
          <el-button type="warning" plain :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">批量下架</el-button>
        </div>
        <el-button type="primary" @click="openCreate">新增景点</el-button>
      </div>
    </template>
    <div class="admin-table-wrap">
    <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="locationName" label="地点" width="140" />
      <el-table-column prop="ticketPrice" label="票价" width="100" />
      <el-table-column prop="averageRating" label="评分" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
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

  <el-dialog v-model="visible" :title="form.id ? '编辑景点' : '新增景点'" width="800px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入景点名称" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入景点描述" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="省份">
            <el-select v-model="selectedProvince" filterable clearable style="width: 100%" placeholder="请选择省份" @change="handleProvinceChange">
              <el-option v-for="item in provinces" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="城市">
            <el-select v-model="form.locationId" filterable clearable style="width: 100%" placeholder="请选择城市" :disabled="!selectedProvince">
              <el-option v-for="item in cities" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="具体地址">
            <el-input v-model="form.address" placeholder="请输入具体地址" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="主图">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload">
              <img v-if="form.imageUrl" :src="form.imageUrl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div style="margin-top: 8px; color: #909399; font-size: 12px;">建议尺寸 800x600，支持 jpg、png</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="票价">
            <el-input-number v-model="form.ticketPrice" :min="0" :precision="2" style="width: 100%" placeholder="请输入票价" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="一级分类">
            <el-select v-model="selectedCategory1" filterable clearable style="width: 100%" placeholder="请选择一级分类" @change="handleCategory1Change">
              <el-option v-for="item in categories1" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="二级分类">
            <el-select v-model="selectedCategory2" multiple filterable clearable style="width: 100%" placeholder="请选择二级分类（可多选）" :disabled="!selectedCategory1">
              <el-option v-for="item in categories2" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="标签">
            <el-select v-model="form.tagIds" multiple clearable filterable style="width: 100%" placeholder="请选择标签">
              <el-option v-for="item in tags" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  adminBatchUpdateDestinationStatus,
  adminDeleteDestination,
  adminListDestinations,
  adminSaveDestination,
  adminUpdateDestinationStatus
} from '@/api/admin'
import { getAllLocations } from '@/api/location'
import { getAllTags } from '@/api/tag'
import { getRootCategories, getCategoriesByParentId } from '@/api/category'

const userStore = useUserStore()
const loading = ref(false)
const visible = ref(false)
const list = ref([])
const total = ref(0)
const locations = ref([])
const tags = ref([])
const categories1 = ref([])
const categories2 = ref([])
const selectedIds = ref([])
const selectedProvince = ref(null)
const selectedCategory1 = ref(null)
const selectedCategory2 = ref(null)
const formRef = ref()

const uploadUrl = 'http://localhost:8082/api/admin/upload/destination'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const provinces = computed(() => {
  return locations.value.filter(l => l.parentId === 0 || !l.parentId)
})

const cities = computed(() => {
  if (!selectedProvince.value) return []
  return locations.value.filter(l => l.parentId === selectedProvince.value)
})

const query = reactive({
  keyword: '',
  status: null,
  page: 1,
  pageSize: 10
})

const emptyForm = () => ({
  id: null,
  name: '',
  description: '',
  locationId: null,
  address: '',
  imageUrl: '',
  imageUrls: '',
  ticketPrice: 0,
  tagIds: [],
  categoryIds: [],
  status: 1
})

const form = reactive(emptyForm())
const rules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入景点描述', trigger: 'blur' }],
  locationId: [{ required: true, message: '请选择城市', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await adminListDestinations(query)
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

const fetchOptions = async () => {
  const [locRes, tagRes, catRes] = await Promise.all([
    getAllLocations(),
    getAllTags(),
    getRootCategories()
  ])
  locations.value = locRes.data || []
  tags.value = tagRes.data || []
  categories1.value = catRes.data || []
}

const handleProvinceChange = () => {
  form.locationId = null
}

const handleCategory1Change = async () => {
  selectedCategory2.value = []
  if (selectedCategory1.value) {
    try {
      const res = await getCategoriesByParentId(selectedCategory1.value)
      categories2.value = res.data || []
    } catch (e) {
      categories2.value = []
    }
  } else {
    categories2.value = []
  }
}

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
    form.imageUrl = response.data.imageUrl
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const openCreate = () => {
  Object.assign(form, emptyForm())
  selectedProvince.value = null
  selectedCategory1.value = null
  selectedCategory2.value = []
  categories2.value = []
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    ...emptyForm(),
    ...row,
    ticketPrice: row.ticketPrice ?? 0,
    tagIds: row.tagIds || [],
    categoryIds: row.categoryIds || []
  })
  
  // 设置省份城市
  if (form.locationId) {
    const loc = locations.value.find(l => l.id === form.locationId)
    if (loc) {
      selectedProvince.value = loc.parentId
    }
  } else {
    selectedProvince.value = null
  }
  
  // 重置分类选择
  selectedCategory1.value = null
  selectedCategory2.value = []
  categories2.value = []
  
  // 设置分类 - 简化处理，先加载所有二级分类再匹配
  if (form.categoryIds && form.categoryIds.length > 0) {
    const rootCatIds = categories1.value.map(c => c.id)
    const selectedRootCats = form.categoryIds.filter(id => rootCatIds.includes(id))
    const selectedSubCats = form.categoryIds.filter(id => !rootCatIds.includes(id))
    
    if (selectedRootCats.length > 0) {
      selectedCategory1.value = selectedRootCats[0]
      // 先加载二级分类选项
      handleCategory1Change().then(() => {
        // 然后设置已选中的二级分类
        selectedCategory2.value = selectedSubCats
      })
    }
  }
  
  visible.value = true
}

const save = async () => {
  await formRef.value.validate()
  
  // 构建 categoryIds 数组
  const categoryIds = []
  if (selectedCategory1.value) categoryIds.push(selectedCategory1.value)
  if (selectedCategory2.value && selectedCategory2.value.length > 0) {
    categoryIds.push(...selectedCategory2.value)
  }
  
  await adminSaveDestination({
    ...form,
    ticketPrice: form.ticketPrice === '' ? null : Number(form.ticketPrice),
    categoryIds
  })
  ElMessage.success('保存成功')
  visible.value = false
  fetchList()
}

const batchUpdateStatus = async (status) => {
  await adminBatchUpdateDestinationStatus(selectedIds.value, status)
  ElMessage.success('批量更新成功')
  fetchList()
}

const toggleStatus = async (row) => {
  await adminUpdateDestinationStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态更新成功')
  fetchList()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该景点？', '提示', { type: 'warning' })
  await adminDeleteDestination(id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchOptions()])
})
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 200px;
  height: 150px;
  display: block;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 150px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
