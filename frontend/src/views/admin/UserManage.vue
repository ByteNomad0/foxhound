<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">用户管理</h2>
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width: 240px" @keyup.enter="load(1)" @clear="load(1)">
        <template #append><el-button @click="load(1)">搜索</el-button></template>
      </el-input>
    </div>

    <el-table :data="users" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column v-if="!isMobile" prop="nickname" label="昵称" width="140" />
      <el-table-column v-if="!isMobile" prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role === 'ADMIN' ? '管理员' : '用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!isMobile" prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.username !== 'admin'" link :type="row.role === 'ADMIN' ? 'warning' : 'primary'" @click="onToggleRole(row)">
            {{ row.role === 'ADMIN' ? '设为用户' : '设为管理员' }}
          </el-button>
          <el-button v-if="row.username !== 'admin'" link :type="row.status === 1 ? 'danger' : 'success'" @click="onToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button v-if="row.username !== 'admin'" link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" background layout="total, prev, pager, next" :total="total"
      :page-size="size" :current-page="page" @current-change="load" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserRole, updateUserStatus, deleteUser } from '@/api'
import { useIsMobile } from '@/composables/useMediaQuery'

const users = ref([])
const isMobile = useIsMobile()
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const loading = ref(false)

const load = async (p) => {
  if (p) page.value = p
  loading.value = true
  try {
    const res = await getUsers({ page: page.value, size, keyword: keyword.value || undefined })
    users.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const onToggleRole = async (row) => {
  const role = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  await updateUserRole(row.id, role)
  ElMessage.success('已更新角色')
  load()
}

const onToggleStatus = async (row) => {
  const status = row.status === 1 ? 0 : 1
  await updateUserStatus(row.id, status)
  ElMessage.success('已更新状态')
  load()
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.page-title {
  margin: 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
