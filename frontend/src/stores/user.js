import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const isAdmin = computed(() => userInfo.value?.role === 'admin' || userInfo.value?.role === 'super_admin')

  // Actions
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const isLoggedIn = computed(() => !!token.value)

  return {
    token,
    userInfo,
    isAdmin,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout
  }
})
