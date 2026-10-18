<template>
  <div>
    <el-page-header @back="$router.back()" class="page-header">
      <template #content>
        <span class="header-title">{{ course?.name || '课程详情' }}</span>
      </template>
    </el-page-header>

    <el-card class="info-card">
      <div class="course-subject">{{ course?.subject }}</div>
      <p class="course-desc">{{ course?.description }}</p>
      <el-button type="primary" @click="$router.push(`/practice?courseId=${course?.id}`)">练习本课程题目</el-button>
    </el-card>

    <h3 class="section-title">知识点</h3>
    <el-empty v-if="chapters.length === 0" description="暂无知识点，请联系管理员添加" />
    <el-collapse v-else v-model="activeChapters">
      <el-collapse-item v-for="ch in chapters" :key="ch.id" :name="ch.id">
        <template #title>
          <span class="chapter-title">{{ ch.title }}</span>
        </template>
        <div v-if="ch.children && ch.children.length" class="children">
          <div v-for="child in ch.children" :key="child.id" class="child-item" @click="$router.push(`/knowledge-point/${child.id}`)">
            <el-icon><Reading /></el-icon>
            <span>{{ child.title }}</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
        <el-empty v-else description="该章节下暂无知识点" :image-size="50" />
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCourse, getKnowledgePoints } from '@/api'

const route = useRoute()
const course = ref(null)
const kps = ref([])
const activeChapters = ref([])

const chapters = computed(() => {
  const roots = kps.value.filter((k) => !k.parentId || k.parentId === 0)
  return roots.map((r) => ({ ...r, children: kps.value.filter((k) => k.parentId === r.id) }))
})

onMounted(async () => {
  const courseRes = await getCourse(route.params.id)
  course.value = courseRes.data
  const kpRes = await getKnowledgePoints(route.params.id)
  kps.value = kpRes.data || []
  activeChapters.value = chapters.value.map((c) => c.id)
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

.info-card {
  margin-bottom: 24px;
}

.course-subject {
  font-size: 13px;
  color: #409eff;
  margin-bottom: 8px;
}

.course-desc {
  color: #606266;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 12px;
}

.chapter-title {
  font-size: 16px;
  font-weight: 600;
}

.children {
  padding: 0 8px;
}

.child-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.child-item:hover {
  background: #f0f7ff;
  color: #409eff;
}

.child-item span {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow {
  margin-left: auto;
  color: #c0c4cc;
}
</style>
