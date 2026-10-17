<template>
  <el-container class="front-layout">
    <el-header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <el-icon size="24"><Reading /></el-icon>
          <span>408 学习平台</span>
        </router-link>
        <el-menu mode="horizontal" :default-active="activeMenu" router class="nav" :ellipsis="false">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/interview">复试机试</el-menu-item>
          <el-menu-item index="/practice">刷题练习</el-menu-item>
          <el-menu-item index="/history">答题历史</el-menu-item>
          <el-menu-item index="/stats">学习统计</el-menu-item>
          <el-menu-item index="/ai">AI 助手</el-menu-item>
        </el-menu>
        <div class="user-area">
          <template v-if="store.isLogin">
            <el-dropdown @command="onCommand">
              <span class="user-trigger">
                <el-avatar :size="30" :src="store.user?.avatar">{{ (store.user?.nickname || store.user?.username || 'U')[0] }}</el-avatar>
                <span class="nickname">{{ store.user?.nickname || store.user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item v-if="store.isAdmin" command="admin">后台管理</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
    <el-footer class="footer">408 计算机科学学习平台 · 仅供学习交流使用</el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/course') || route.path.startsWith('/knowledge-point')) return '/'
  if (route.path.startsWith('/interview')) return '/interview'
  return route.path
})

const onCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    store.logout()
    router.push('/')
  }
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0;
  height: 60px;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  margin-right: 30px;
  white-space: nowrap;
}

.nav {
  flex: 1;
  border-bottom: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.nickname {
  font-size: 14px;
}

.main {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  height: 50px;
  line-height: 50px;
}
</style>
