<template>
  <div>
    <el-page-header @back="$router.back()" class="page-header">
      <template #content>
        <span class="header-title">{{ kp?.title || '知识点详情' }}</span>
      </template>
    </el-page-header>

    <el-card class="content-card">
      <div class="actions">
        <el-button v-if="store.isLogin" type="primary" plain @click="onMarkStudied">标记为已学习</el-button>
        <el-button type="primary" @click="$router.push(`/practice?knowledgePointId=${kp?.id}`)">练习相关题目</el-button>
        <el-button v-if="store.isLogin" @click="$router.push(`/ai?knowledgePointId=${kp?.id}`)">问 AI 助手</el-button>
      </div>

      <h3 class="block-title">详细讲解</h3>
      <div class="content" v-html="renderContent(kp?.content)"></div>

      <h3 class="block-title">重点</h3>
      <div class="content key" v-html="renderContent(kp?.keyPoint)"></div>

      <h3 class="block-title">难点</h3>
      <div class="content difficult" v-html="renderContent(kp?.difficultPoint)"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getKnowledgePoint, markStudied } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const store = useUserStore()
const kp = ref(null)

const renderContent = (text) => {
  if (!text) return '<span class="empty-text">暂无内容</span>'
  return text.replace(/\n/g, '<br/>')
}

const onMarkStudied = async () => {
  await markStudied(route.params.id)
  ElMessage.success('已标记为已学习')
}

onMounted(async () => {
  const res = await getKnowledgePoint(route.params.id)
  kp.value = res.data
})
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
}

.content-card {
  padding: 8px;
}

.actions {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.block-title {
  font-size: 16px;
  margin: 20px 0 10px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.content {
  line-height: 1.9;
  color: #303133;
  font-size: 15px;
  white-space: normal;
}

.content.key {
  color: #b8860b;
}

.content.difficult {
  color: #c0392b;
}

.empty-text {
  color: #c0c4cc;
}
</style>
