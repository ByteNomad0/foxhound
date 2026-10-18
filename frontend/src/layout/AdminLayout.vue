<template>
  <el-container class="admin-layout">
    <el-aside v-if="!isNarrow" width="220px" class="aside">
      <div class="logo">
        <el-icon size="22"><Setting /></el-icon>
        <span>后台管理</span>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#001529" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item v-for="m in menuItems" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button v-if="isNarrow" text class="menu-btn" @click="drawer = true">
            <el-icon size="20"><Menu /></el-icon>
          </el-button>
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

    <el-drawer v-model="drawer" direction="ltr" size="240px" :with-header="false" class="admin-drawer">
      <div class="drawer-menu">
        <div class="logo">
          <el-icon size="22"><Setting /></el-icon>
          <span>后台管理</span>
        </div>
        <el-menu :default-active="activeMenu" router background-color="#001529" text-color="#bfcbd9" active-text-color="#409eff" @select="drawer = false">
          <el-menu-item v-for="m in menuItems" :key="m.path" :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <span>{{ m.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useIsNarrow } from '@/composables/useMediaQuery'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const isNarrow = useIsNarrow()
const drawer = ref(false)

const menuItems = [
  { path: '/admin', icon: 'DataAnalysis', title: '数据看板' },
  { path: '/admin/courses', icon: 'Collection', title: '课程管理' },
  { path: '/admin/knowledge-points', icon: 'Document', title: '知识点管理' },
  { path: '/admin/questions', icon: 'Tickets', title: '题库管理' },
  { path: '/admin/coding', icon: 'Cpu', title: '算法题管理' },
  { path: '/admin/users', icon: 'User', title: '用户管理' },
  { path: '/admin/ai-config', icon: 'MagicStick', title: 'AI 配置' }
]

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

.aside .el-menu,
.drawer-menu .el-menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.menu-btn {
  padding: 8px;
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

/* 抽屉版侧栏 */
.admin-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-menu {
  background: #001529;
  min-height: 100%;
}

@media (max-width: 899px) {
  .main {
    padding: 12px;
  }
}
</style>
