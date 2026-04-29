<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px" class="admin-aside">
        <div class="brand">管理后台</div>
        <el-menu
          :default-active="activePath"
          router
          class="admin-menu"
          background-color="#ffffff"
          text-color="#475569"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/destinations">
            <el-icon><MapLocation /></el-icon>
            <span>景点管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tags">
            <el-icon><CollectionTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/experiences">
            <el-icon><Tickets /></el-icon>
            <span>分享管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/algorithm">
            <el-icon><Operation /></el-icon>
            <span>算法配置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <div class="page-title">{{ pageTitle }}</div>
            <div class="admin-subtitle">欢迎，{{ userStore.userInfo.realName || userStore.userInfo.username || '管理员' }}</div>
          </div>
          <div>
            <el-button type="danger" plain @click="logout">退出</el-button>
          </div>
        </el-header>
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, CollectionTag, DataAnalysis, MapLocation, Operation, Tickets, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activePath = computed(() => route.path)
const titleMap = {
  '/admin/dashboard': '仪表盘',
  '/admin/destinations': '景点管理',
  '/admin/tags': '标签管理',
  '/admin/users': '用户管理',
  '/admin/comments': '评论管理',
  '/admin/experiences': '分享管理',
  '/admin/algorithm': '算法配置'
}
const pageTitle = computed(() => titleMap[route.path] || '管理后台')

const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-aside {
  background: #ffffff;
  color: #1e293b;
  border-right: 1px solid #f1f5f9;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
}

.brand {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #1e293b;
  border-bottom: 1px solid #f1f5f9;
  background: #f9fafb;
}

.admin-menu {
  border-right: none;
}

.admin-menu :deep(.el-menu-item) {
  color: #475569;
  margin: 8px 10px;
  border-radius: 8px;
  height: 44px;
  transition: all 0.3s ease;
}

.admin-menu :deep(.el-menu-item:hover) {
  color: #1e293b;
  background: #f1f5f9;
}

.admin-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.admin-header {
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  backdrop-filter: blur(6px);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.admin-main {
  background: #f3f6fb;
  min-height: calc(100vh - 60px);
}
</style>
