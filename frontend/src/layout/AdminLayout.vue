<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="22"><Setting /></el-icon>
        <span>后台管理</span>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#001529" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/courses">
          <el-icon><Collection /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/knowledge-points">
          <el-icon><Document /></el-icon>
          <span>知识点管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/questions">
          <el-icon><Tickets /></el-icon>
          <span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/coding">
          <el-icon><Cpu /></el-icon>
          <span>算法题管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/ai-config">
          <el-icon><MagicStick /></el-icon>
          <span>AI 配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>
          <el-button text @click="$router.push('/')">
            <el-icon><Back /></el-icon>&nbsp;返回前台
          </el-button>
        </div>
        <div class="right">
          <span class="admin-name">{{ store.user?.nickname || store.user?.username }}</span>
          <el-button text type="danger" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const activeMenu = computed(() => route.path)

const onLogout = () => {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.aside {
  background-color: #001529;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.aside .el-menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-name {
  font-size: 14px;
  color: #606266;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
