<template>
  <el-container class="front-layout">
    <el-header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <el-icon size="24"><Reading /></el-icon>
          <span>408 学习平台</span>
        </router-link>

        <el-menu v-if="!isMobile" mode="horizontal" :default-active="activeMenu" router class="nav" :ellipsis="false">
          <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">{{ item.title }}</el-menu-item>
        </el-menu>

        <div v-if="!isMobile" class="user-area">
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

        <el-button v-if="isMobile" class="menu-btn" text @click="drawer = true">
          <el-icon size="24"><Menu /></el-icon>
        </el-button>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>
    <el-footer class="footer">408 计算机科学学习平台 · 仅供学习交流使用</el-footer>

    <el-drawer v-model="drawer" direction="rtl" size="72%" :with-header="false" class="mobile-drawer">
      <div class="drawer-body">
        <div class="drawer-user">
          <template v-if="store.isLogin">
            <el-avatar :size="44" :src="store.user?.avatar">{{ (store.user?.nickname || store.user?.username || 'U')[0] }}</el-avatar>
            <div class="drawer-user-info">
              <div class="drawer-nickname">{{ store.user?.nickname || store.user?.username }}</div>
              <div class="drawer-role">{{ store.isAdmin ? '管理员' : '学员' }}</div>
            </div>
          </template>
          <template v-else>
            <el-button type="primary" @click="go('/register')">注册</el-button>
            <el-button @click="go('/login')">登录</el-button>
          </template>
        </div>

        <div class="drawer-nav">
          <div v-for="item in navItems" :key="item.path" class="drawer-item" :class="{ active: item.path === activeMenu }" @click="go(item.path)">
            {{ item.title }}
          </div>
        </div>

        <div v-if="store.isLogin" class="drawer-actions">
          <el-button text @click="go('/profile')">个人资料</el-button>
          <el-button v-if="store.isAdmin" text @click="go('/admin')">后台管理</el-button>
          <el-button text type="danger" @click="onLogout">退出登录</el-button>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useIsMobile } from '@/composables/useMediaQuery'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const isMobile = useIsMobile()
const drawer = ref(false)

const navItems = [
  { path: '/', title: '首页' },
  { path: '/interview', title: '复试机试' },
  { path: '/practice', title: '刷题练习' },
  { path: '/history', title: '答题历史' },
  { path: '/stats', title: '学习统计' },
  { path: '/ai', title: 'AI 助手' }
]

const activeMenu = computed(() => {
  if (route.path.startsWith('/course') || route.path.startsWith('/knowledge-point')) return '/'
  if (route.path.startsWith('/interview')) return '/interview'
  return route.path
})

const go = (path) => {
  drawer.value = false
  router.push(path)
}

const onLogout = async () => {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  store.logout()
  drawer.value = false
  router.push('/')
}

const onCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    await onLogout()
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

.menu-btn {
  margin-left: auto;
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

/* 移动端抽屉 */
.mobile-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px;
  background: linear-gradient(135deg, #409eff 0%, #2f54eb 100%);
  color: #fff;
}

.drawer-user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.drawer-nickname {
  font-size: 16px;
  font-weight: 600;
}

.drawer-role {
  font-size: 12px;
  opacity: 0.85;
}

.drawer-nav {
  flex: 1;
  padding: 12px 0;
}

.drawer-item {
  padding: 14px 24px;
  font-size: 15px;
  color: #303133;
  cursor: pointer;
}

.drawer-item.active {
  color: #409eff;
  background: #ecf5ff;
  font-weight: 600;
}

.drawer-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 20px 24px;
  border-top: 1px solid #f0f2f5;
}

.drawer-actions .el-button {
  justify-content: flex-start;
  margin-left: 0;
}

@media (max-width: 767px) {
  .header-inner {
    justify-content: space-between;
    padding: 0 12px;
  }

  .logo {
    margin-right: 0;
  }

  .main {
    padding: 12px;
  }
}
</style>
