<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon size="48" color="#409EFF"><Compass /></el-icon>
        </div>
        <h1 class="title">旅游推荐系统</h1>
        <p class="subtitle">发现美好旅程，开启精彩人生</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs" stretch>
        <el-tab-pane label="用户登录" name="user">
          <el-form
            ref="userFormRef"
            :model="userForm"
            :rules="userRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="userForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="userForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="primary" :underline="false" @click="handleForgotPassword">
                  忘记密码？
                </el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>

            <div class="register-link">
              还没有账号？
              <el-link type="primary" :underline="false" @click="goToRegister">
                立即注册
              </el-link>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="管理员登录" name="admin">
          <el-form
            ref="adminFormRef"
            :model="adminForm"
            :rules="adminRules"
            class="login-form"
            @keyup.enter="handleAdminLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="adminForm.username"
                placeholder="请输入管理员账号"
                size="large"
                :prefix-icon="UserFilled"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="adminForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="danger"
                size="large"
                class="login-button"
                :loading="adminLoading"
                @click="handleAdminLogin"
              >
                管理员登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="login-footer">
      <p>旅游推荐系统</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Compass } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login, adminLogin } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 当前选中的标签页
const activeTab = ref('user')

// 加载状态
const loading = ref(false)
const adminLoading = ref(false)

// 记住我
const rememberMe = ref(false)

// 表单引用
const userFormRef = ref(null)
const adminFormRef = ref(null)

// 用户登录表单
const userForm = reactive({
  username: '',
  password: ''
})

// 管理员登录表单
const adminForm = reactive({
  username: '',
  password: ''
})

// 用户登录验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ]
}

// 管理员登录验证规则
const adminRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 用户登录
const handleLogin = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    loading.value = true
    
    const res = await login(userForm)
    
    if (res.code === 200 && res.data) {
      // 保存token和用户信息
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      
      ElMessage.success('登录成功')
      
      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 管理员登录
const handleAdminLogin = async () => {
  if (!adminFormRef.value) return
  
  try {
    await adminFormRef.value.validate()
    adminLoading.value = true
    
    const res = await adminLogin(adminForm)
    
    // 保存token和用户信息
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.adminInfo)
    
    ElMessage.success('管理员登录成功')
    
    // 跳转到管理后台
    router.push('/admin/dashboard')
  } catch (error) {
    console.error('管理员登录失败:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    adminLoading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 忘记密码
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #e0e7ff 0%, #f0f4ff 100%);
}

/* 山脉底纹 - 固定在底部区域 */
.login-container::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 35vh;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='600' height='200' viewBox='0 0 600 200'%3E%3Cpath fill='%23667eea' fill-opacity='0.06' d='M0,120 Q75,80 150,100 T300,90 T450,110 T600,100 L600,200 L0,200 Z'/%3E%3Cpath fill='%23764ba2' fill-opacity='0.04' d='M0,150 Q100,120 200,140 T400,130 T600,140 L600,200 L0,200 Z'/%3E%3C/svg%3E");
  background-repeat: repeat-x;
  background-size: 600px 200px;
  background-position: bottom;
  pointer-events: none;
  z-index: 1;
}

/* 云朵底纹 - 固定在上部区域 */
.login-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 40vh;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='800' height='300' viewBox='0 0 800 300'%3E%3Cellipse cx='150' cy='100' rx='80' ry='40' fill='%23764ba2' fill-opacity='0.04'/%3E%3Cellipse cx='200' cy='90' rx='60' ry='35' fill='%23764ba2' fill-opacity='0.04'/%3E%3Cellipse cx='100' cy='110' rx='50' ry='28' fill='%23764ba2' fill-opacity='0.04'/%3E%3Cellipse cx='500' cy='160' rx='100' ry='50' fill='%23667eea' fill-opacity='0.03'/%3E%3Cellipse cx='570' cy='150' rx='70' ry='40' fill='%23667eea' fill-opacity='0.03'/%3E%3Cellipse cx='430' cy='170' rx='60' ry='32' fill='%23667eea' fill-opacity='0.03'/%3E%3C/svg%3E");
  background-repeat: repeat-x;
  background-size: 800px 300px;
  background-position: top;
  animation: floatClouds 120s linear infinite;
  pointer-events: none;
  z-index: 2;
}

@keyframes floatClouds {
  0% {
    background-position: 0 top;
  }
  100% {
    background-position: 800px top;
  }
}

.login-box {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 10;
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  margin-bottom: 16px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
}

.login-form {
  margin-top: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-button {
  width: 100%;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
}

.login-footer {
  margin-top: 30px;
  text-align: center;
  color: #64748b;
  font-size: 13px;
  position: relative;
  z-index: 10;
}

/* 输入框样式优化 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 4px 11px;
}

:deep(.el-input__inner) {
  height: 42px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .title {
    font-size: 20px;
  }
}
</style>
