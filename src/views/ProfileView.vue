<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">个人信息</h2>
      <button
        @click="handleRefresh"
        :disabled="loading"
        class="flex items-center space-x-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50"
      >
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        <span>刷新</span>
      </button>
    </div>

    <div v-if="loading" class="flex justify-center items-center py-12">
      <Loader2 class="w-8 h-8 animate-spin text-blue-500" />
    </div>

    <div v-else class="bg-white rounded-lg shadow-sm p-6 max-w-2xl">
      <div class="flex items-center mb-6">
        <div class="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center">
          <User class="w-10 h-10 text-blue-500" />
        </div>
        <div class="ml-6">
          <h3 class="text-xl font-bold text-gray-800">{{ user?.username }}</h3>
          <span
            :class="user?.role === 'ADMIN' ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-600'"
            class="mt-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
          >
            {{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </span>
        </div>
      </div>

      <div class="space-y-4">
        <div class="flex items-center justify-between py-3 border-b border-gray-200">
          <span class="text-gray-500 flex items-center">
            <User class="w-4 h-4 mr-2" />
            用户ID
          </span>
          <span class="text-gray-800 font-medium">{{ user?.id }}</span>
        </div>

        <div class="flex items-center justify-between py-3 border-b border-gray-200">
          <span class="text-gray-500 flex items-center">
            <Mail class="w-4 h-4 mr-2" />
            邮箱
          </span>
          <span class="text-gray-800">{{ user?.email || '未设置' }}</span>
        </div>

        <div class="flex items-center justify-between py-3 border-b border-gray-200">
          <span class="text-gray-500 flex items-center">
            <Phone class="w-4 h-4 mr-2" />
            手机号
          </span>
          <span class="text-gray-800">{{ user?.phoneNumber }}</span>
        </div>

        <div class="flex items-center justify-between py-3 border-b border-gray-200">
          <span class="text-gray-500 flex items-center">
            <Calendar class="w-4 h-4 mr-2" />
            注册时间
          </span>
          <span class="text-gray-800">{{ formatDateTime(user?.createTime) }}</span>
        </div>

        <div class="flex items-center justify-between py-3">
          <span class="text-gray-500 flex items-center">
            <Shield class="w-4 h-4 mr-2" />
            角色
          </span>
          <span class="text-gray-800">{{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loader2, User, Mail, Phone, Calendar, Shield, RefreshCw } from 'lucide-vue-next'
import { getProfile } from '../services/userService'
import { getUser } from '../utils/auth'

const loading = ref(false)
const profileData = ref(null)

const user = computed(() => profileData.value?.data || getUser())

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

async function fetchProfile() {
  loading.value = true
  
  try {
    const response = await getProfile()
    
    if (response.code === 200) {
      profileData.value = response
    }
  } catch (err) {
    console.error('获取个人信息失败:', err)
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  fetchProfile()
}

onMounted(() => {
  fetchProfile()
})
</script>
