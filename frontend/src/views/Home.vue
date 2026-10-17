<template>
  <div>
    <div class="banner">
      <h1>408 计算机学科考研学习平台</h1>
      <p>数据结构 · 计算机组成原理 · 操作系统 · 计算机网络</p>
      <el-button type="primary" size="large" @click="$router.push('/practice')">开始刷题</el-button>
    </div>

    <h2 class="section-title">课程列表</h2>
    <el-row :gutter="20">
      <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="course-card" shadow="hover" @click="$router.push(`/course/${course.id}`)">
          <div class="course-icon" :style="{ background: colorOf(course.id) }">
            {{ (course.name || '课')[0] }}
          </div>
          <div class="course-name">{{ course.name }}</div>
          <div class="course-subject">{{ course.subject }}</div>
          <div class="course-desc">{{ course.description }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api'

const courses = ref([])

const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#7b68ee']

const colorOf = (id) => colors[(id - 1) % colors.length]

onMounted(async () => {
  const res = await getCourses()
  courses.value = res.data || []
})
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #409eff 0%, #2f54eb 100%);
  border-radius: 12px;
  padding: 50px 40px;
  color: #fff;
  margin-bottom: 30px;
}

.banner h1 {
  font-size: 28px;
  margin-bottom: 12px;
}

.banner p {
  margin-bottom: 20px;
  opacity: 0.9;
}

.section-title {
  font-size: 20px;
  margin-bottom: 16px;
}

.course-card {
  cursor: pointer;
  margin-bottom: 20px;
  text-align: center;
}

.course-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  margin: 0 auto 12px;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-name {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 4px;
}

.course-subject {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.course-desc {
  font-size: 13px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
