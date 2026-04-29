<template>
  <div class="home-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="goToHome">
          <el-icon size="32" color="#409EFF"><Compass /></el-icon>
          <span class="logo-text">旅游推荐系统</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/destinations">
            <el-icon><MapLocation /></el-icon>
            <span>景点浏览</span>
          </el-menu-item>
          <el-menu-item index="/recommendations">
            <el-icon><Star /></el-icon>
            <span>智能推荐</span>
          </el-menu-item>
          <el-menu-item index="/experiences">
            <el-icon><ChatDotRound /></el-icon>
            <span>旅游体验</span>
          </el-menu-item>
          <el-menu-item index="/collections">
            <el-icon><Collection /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
        </el-menu>
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              <el-avatar :size="32" :src="userStore.userInfo.avatar">
                {{ userStore.userInfo.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo.username || '用户' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="preferences">
                  <el-icon><Setting /></el-icon>
                  <span>偏好设置</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Compass, ArrowDown, MapLocation, Star, Collection, ChatDotRound,
  Search, User, Setting, SwitchButton 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref(route.path)

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})

const handleMenuSelect = (index) => {
  router.push(index)
}

const goToHome = () => {
  router.push('/destinations')
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'preferences':
      router.push('/preferences')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 64px;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.nav-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-menu :deep(.el-menu-item:hover) {
  background-color: rgba(64, 158, 255, 0.1);
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #409EFF;
  border-bottom: 2px solid #409EFF;
}

.user-info {
  cursor: pointer;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-name:hover {
  background-color: #f5f7fa;
}

.username {
  font-weight: 500;
}

.main-content {
  flex: 1;
  padding: 0;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }
  
  .logo-text {
    display: none;
  }
  
  .nav-menu {
    margin: 0 10px;
  }
  
  .nav-menu :deep(.el-menu-item) {
    padding: 0 10px;
    font-size: 14px;
  }
  
  .username {
    display: none;
  }
}
</style>
