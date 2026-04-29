import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { public: true }
    },

    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'adminDashboard',
          component: () => import('@/views/admin/AdminDashboard.vue')
        },
        {
          path: 'destinations',
          name: 'adminDestinations',
          component: () => import('@/views/admin/AdminDestinations.vue')
        },
        {
          path: 'tags',
          name: 'adminTags',
          component: () => import('@/views/admin/AdminTags.vue')
        },
        {
          path: 'users',
          name: 'adminUsers',
          component: () => import('@/views/admin/AdminUsers.vue')
        },
        {
          path: 'comments',
          name: 'adminComments',
          component: () => import('@/views/admin/AdminComments.vue')
        },
        {
          path: 'experiences',
          name: 'adminExperiences',
          component: () => import('@/views/admin/AdminExperiences.vue')
        },
        {
          path: 'algorithm',
          name: 'adminAlgorithm',
          component: () => import('@/views/admin/AdminAlgorithm.vue')
        }
      ]
    },
    {
      path: '/',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/destinations'
        },
        {
          path: 'destinations',
          name: 'destinations',
          component: () => import('@/views/Destinations.vue')
        },
        {
          path: 'destination/:id',
          name: 'destinationDetail',
          component: () => import('@/views/DestinationDetail.vue')
        },
        {
          path: 'recommendations',
          name: 'recommendations',
          component: () => import('@/views/Recommendations.vue')
        },
        {
          path: 'experiences',
          name: 'experiences',
          component: () => import('@/views/TravelExperience.vue')
        },
        {
          path: 'experiences/publish',
          name: 'experiencePublish',
          component: () => import('@/views/TravelExperiencePublish.vue')
        },
        {
          path: 'my-experiences',
          name: 'myExperiences',
          component: () => import('@/views/MyExperiences.vue')
        },
        {
          path: 'collections',
          name: 'collections',
          component: () => import('@/views/MyCollections.vue')
        },

        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/Profile.vue')
        },
        {
          path: 'preferences',
          name: 'preferences',
          component: () => import('@/views/Preferences.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/login')
    return
  }

  next()
})

export default router
