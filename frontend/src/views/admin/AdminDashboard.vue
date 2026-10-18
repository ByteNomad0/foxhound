<template>
  <div>
    <h2 class="page-title">数据看板</h2>
    <el-row :gutter="20">
      <el-col v-for="item in cards" :key="item.label" :xs="12" :sm="8" :md="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-num" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '@/api'

const data = ref({})

const cards = computed(() => [
  { label: '用户数', value: data.value.userCount ?? 0, color: '#409eff' },
  { label: '课程数', value: data.value.courseCount ?? 0, color: '#67c23a' },
  { label: '知识点数', value: data.value.knowledgePointCount ?? 0, color: '#e6a23c' },
  { label: '题目数', value: data.value.questionCount ?? 0, color: '#f56c6c' },
  { label: '答题记录', value: data.value.answerCount ?? 0, color: '#7b68ee' },
  { label: 'AI 对话', value: data.value.aiChatCount ?? 0, color: '#909399' }
])

onMounted(async () => {
  const res = await getDashboard()
  data.value = res.data || {}
})
</script>

<style scoped>
.page-title {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 16px 0;
  margin-bottom: 20px;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
}

@media (max-width: 767px) {
  .stat-num {
    font-size: 24px;
  }
}

.stat-label {
  margin-top: 8px;
  color: #909399;
}
</style>
