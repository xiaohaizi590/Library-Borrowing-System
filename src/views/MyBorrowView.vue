<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">我的借阅</h2>
    </div>

    <div v-if="loading" class="flex justify-center items-center py-12">
      <Loader2 class="w-8 h-8 animate-spin text-blue-500" />
    </div>

    <div v-else-if="records.length === 0" class="text-center py-12">
      <BookOpenCheck class="w-16 h-16 text-gray-300 mx-auto mb-4" />
      <p class="text-gray-500">暂无借阅记录</p>
    </div>

    <div v-else class="bg-white rounded-lg shadow-sm overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">图书信息</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">借阅时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">应还时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">归还时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">续借次数</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="record in records" :key="record.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">{{ record.bookTitle }}</div>
              <div class="text-sm text-gray-500">{{ record.bookAuthor }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDateTime(record.borrowTime) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm" :class="isOverdue(record) ? 'text-red-500 font-medium' : 'text-gray-500'">
              {{ formatDateTime(record.dueTime) }}
              <span v-if="isOverdue(record)" class="ml-2">已逾期</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ record.returnTime ? formatDateTime(record.returnTime) : '-' }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="getStatusClass(record.status)"
                class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
              >
                {{ getStatusText(record.status) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ record.renewCount }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <template v-if="record.status === 'BORROWED'">
                <button
                  @click="handleReturn(record)"
                  :disabled="returnLoading"
                  class="text-green-600 hover:text-green-900 mr-3 px-3 py-1 bg-green-50 rounded hover:bg-green-100 transition-colors"
                >
                  归还
                </button>
                <button
                  @click="handleRenew(record)"
                  :disabled="renewLoading || record.renewCount >= 3"
                  :class="record.renewCount >= 3 ? 'text-gray-400 cursor-not-allowed' : 'text-blue-600 hover:text-blue-900 px-3 py-1 bg-blue-50 rounded hover:bg-blue-100 transition-colors'"
                >
                  续借
                </button>
              </template>
              <span v-else class="text-gray-400">已归还</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="!loading && totalPages > 1" class="flex justify-center mt-8">
      <nav class="flex items-center space-x-2">
        <button
          @click="prevPage"
          :disabled="currentPage === 0"
          class="px-3 py-2 border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        >
          <ChevronLeft class="w-4 h-4" />
        </button>
        <span class="px-3 py-2 text-gray-600">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
        <button
          @click="nextPage"
          :disabled="currentPage >= totalPages - 1"
          class="px-3 py-2 border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        >
          <ChevronRight class="w-4 h-4" />
        </button>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loader2, BookOpenCheck, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { getBorrowRecordsByUser, returnBook, renewBook } from '../services/bookService'
import { getUser } from '../utils/auth'

const loading = ref(false)
const returnLoading = ref(false)
const renewLoading = ref(false)
const records = ref([])
const currentPage = ref(0)
const totalPages = ref(0)

async function fetchRecords(page = 0) {
  loading.value = true
  const user = getUser()
  
  try {
    const response = await getBorrowRecordsByUser(user?.id, page, 10)
    
    if (response.code === 200) {
      records.value = response.data.content
      totalPages.value = response.data.totalPages
      currentPage.value = response.data.number
    }
  } catch (err) {
    console.error('获取借阅记录失败:', err)
  } finally {
    loading.value = false
  }
}

function prevPage() {
  if (currentPage.value > 0) {
    fetchRecords(currentPage.value - 1)
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    fetchRecords(currentPage.value + 1)
  }
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

function isOverdue(record) {
  if (record.status !== 'BORROWED') return false
  return new Date(record.dueTime) < new Date()
}

function getStatusClass(status) {
  switch (status) {
    case 'BORROWED':
      return 'bg-yellow-100 text-yellow-800'
    case 'RETURNED':
      return 'bg-green-100 text-green-800'
    case 'OVERDUE':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

function getStatusText(status) {
  switch (status) {
    case 'BORROWED':
      return '借阅中'
    case 'RETURNED':
      return '已归还'
    case 'OVERDUE':
      return '已逾期'
    default:
      return status
  }
}

async function handleReturn(record) {
  returnLoading.value = true
  
  try {
    const response = await returnBook(record.id)
    
    if (response.code === 200) {
      fetchRecords(currentPage.value)
    }
  } catch (err) {
    console.error('归还失败:', err)
  } finally {
    returnLoading.value = false
  }
}

async function handleRenew(record) {
  if (record.renewCount >= 3) return
  
  renewLoading.value = true
  
  try {
    const response = await renewBook(record.id)
    
    if (response.code === 200) {
      fetchRecords(currentPage.value)
    }
  } catch (err) {
    console.error('续借失败:', err)
  } finally {
    renewLoading.value = false
  }
}

onMounted(() => {
  fetchRecords()
})
</script>
