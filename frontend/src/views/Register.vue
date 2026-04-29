<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo">
          <el-icon size="48" color="#409EFF"><User /></el-icon>
        </div>
        <h1 class="title">用户注册</h1>
        <p class="subtitle">创建账号，开启您的旅行之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
        @keyup.enter="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20个字符）"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址（可选）"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号码（可选）"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20个字符）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-link type="primary" :underline="false" @click="showAgreement">
              用户协议
            </el-link>
            和
            <el-link type="primary" :underline="false" @click="showPrivacy">
              隐私政策
            </el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？
          <el-link type="primary" :underline="false" @click="goToLogin">
            立即登录
          </el-link>
        </div>
      </el-form>
    </div>

    <div class="register-footer">
      <p>旅游推荐系统</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 表单引用
const registerFormRef = ref(null)

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 自定义验证规则：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 自定义验证规则：用户协议
const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

const validateOptionalEmail = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
    return
  }
  callback()
}

const validateOptionalPhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号码'))
    return
  }
  callback()
}

// 注册验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' }
  ],
  email: [
    { validator: validateOptionalEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validateOptionalPhone, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    // 移除确认密码和协议字段，只发送必要数据
    const { confirmPassword, agreement, ...registerData } = registerForm
    
    const res = await register(registerData)
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      // 跳转到登录页面
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}

// 显示用户协议
const showAgreement = () => {
  ElMessage.info('用户协议功能开发中...')
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessage.info('隐私政策功能开发中...')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 460px;
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

.register-header {
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

.register-form {
  margin-top: 20px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  padding-bottom: 8px;
}

.register-button {
  width: 100%;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
  margin-top: 10px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
}

.register-footer {
  margin-top: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
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
  .register-box {
    padding: 30px 20px;
  }
  
  .title {
    font-size: 20px;
  }
}
</style>
