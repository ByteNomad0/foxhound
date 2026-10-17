<template>
  <div v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card"><div class="stat-num">{{ stats.totalAnswered ?? 0 }}</div><div class="stat-label">累计答题</div></el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card"><div class="stat-num">{{ stats.correctCount ?? 0 }}</div><div class="stat-label">答对题数</div></el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card"><div class="stat-num accent">{{ stats.accuracy ?? 0 }}%</div><div class="stat-label">正确率</div></el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card"><div class="stat-num accent">{{ stats.overallProgress ?? 0 }}%</div><div class="stat-label">学习进度</div></el-card>
      </el-col>
    </el-row>

    <el-card class="block">
      <h3 class="block-title">各课程学习进度</h3>
      <el-table :data="stats.courseProgress || []" border stripe>
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column label="学习进度" min-width="220">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="16" />
          </template>
        </el-table-column>
        <el-table-column label="知识点" width="140">
          <template #default="{ row }">{{ row.studiedKp }} / {{ row.totalKp }}</template>
        </el-table-column>
        <el-table-column label="答题" width="140">
          <template #default="{ row }">{{ row.answeredCount }} 题</template>
        </el-table-column>
        <el-table-column label="正确率" width="120">
          <template #default="{ row }">
            <span :class="['rate', row.accuracy >= 80 ? 'good' : row.accuracy >= 60 ? 'mid' : 'bad']">{{ row.accuracy }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="block">
      <div class="block-header">
        <h3 class="block-title">薄弱知识点</h3>
        <el-button size="small" type="primary" @click="$router.push('/ai')">用 AI 分析</el-button>
      </div>
      <el-empty v-if="weakPoints.length === 0" description="暂无答题数据，先去刷题吧" />
      <el-table v-else :data="weakPoints" border stripe>
        <el-table-column prop="courseName" label="课程" width="160" />
        <el-table-column prop="knowledgePointTitle" label="知识点" min-width="200" />
        <el-table-column prop="total" label="答题数" width="100" />
        <el-table-column prop="correct" label="答对" width="100" />
        <el-table-column label="正确率" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.accuracy" :status="row.accuracy < 60 ? 'exception' : ''" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats, getWeakPoints } from '@/api'

const loading = ref(false)
const stats = ref({})
const weakPoints = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const [s, w] = await Promise.all([getStats(), getWeakPoints()])
    stats.value = s.data || {}
    weakPoints.value = w.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.stat-num {
  font-size: 30px;
  font-weight: 700;
  color: #303133;
}

.stat-num.accent {
  color: #409eff;
}

.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 14px;
}

.block {
  margin-bottom: 20px;
}

.block-title {
  font-size: 17px;
  margin-bottom: 16px;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rate.good {
  color: #67c23a;
  font-weight: 700;
}

.rate.mid {
  color: #e6a23c;
  font-weight: 700;
}

.rate.bad {
  color: #f56c6c;
  font-weight: 700;
}
</style>
