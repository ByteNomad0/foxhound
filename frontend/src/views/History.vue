<template>
  <div>
    <el-card>
      <h3 class="title">答题历史</h3>
      <el-form :inline="true">
        <el-form-item label="课程">
          <el-select v-model="query.courseId" clearable placeholder="全部课程" style="width: 170px" @change="load(1)">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="query.correct" clearable placeholder="全部" style="width: 120px" @change="load(1)">
            <el-option label="正确" :value="1" />
            <el-option label="错误" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="records" v-loading="loading" border stripe>
        <el-table-column label="题目" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">{{ row.questionContent }}</template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" width="130" />
        <el-table-column v-if="!isMobile" prop="knowledgePointTitle" label="知识点" width="150" show-overflow-tooltip />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">{{ typeLabel(row.questionType) }}</template>
        </el-table-column>
        <el-table-column label="我的答案" width="120">
          <template #default="{ row }">{{ (row.userAnswer || []).join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isCorrect === 1 ? 'success' : 'danger'">{{ row.isCorrect === 1 ? '正确' : '错误' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="submittedAt" label="时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" background layout="total, prev, pager, next" :total="total"
        :page-size="query.size" :current-page="query.page" @current-change="load" />
    </el-card>

    <el-dialog v-model="detailVisible" title="答题详情" width="min(640px, 92%)">
      <div v-if="detail">
        <div class="detail-question">{{ detail.questionContent }}</div>
        <div class="detail-options">
          <div v-for="opt in detail.options || []" :key="opt.key"
               :class="['detail-option', (detail.correctAnswer || []).includes(opt.key) ? 'correct' : '', (detail.userAnswer || []).includes(opt.key) && !(detail.correctAnswer || []).includes(opt.key) ? 'wrong' : '']">
            <span class="opt-key">{{ opt.key }}.</span>
            <span>{{ opt.content }}</span>
          </div>
        </div>
        <p class="detail-line"><b>你的答案：</b>{{ (detail.userAnswer || []).join('、') || '未作答' }}</p>
        <p class="detail-line"><b>正确答案：</b>{{ (detail.correctAnswer || []).join('、') }}</p>
        <p class="detail-line" v-if="detail.analysis"><b>解析：</b>{{ detail.analysis }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getHistory, getCourses } from '@/api'
import { useIsMobile } from '@/composables/useMediaQuery'

const courses = ref([])
const isMobile = useIsMobile()
const loading = ref(false)
const records = ref([])
const total = ref(0)

const query = reactive({ page: 1, size: 10, courseId: null, correct: null })

const detailVisible = ref(false)
const detail = ref(null)

const typeLabel = (type) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题' }[type] || type)

const load = async (page) => {
  if (page) query.page = page
  loading.value = true
  try {
    const res = await getHistory({
      page: query.page,
      size: query.size,
      courseId: query.courseId || undefined,
      correct: query.correct ?? undefined
    })
    records.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const openDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

onMounted(async () => {
  const res = await getCourses()
  courses.value = res.data || []
  load(1)
})
</script>

<style scoped>
.title {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.detail-question {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 16px;
}

.detail-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-option {
  padding: 8px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.detail-option.correct {
  background: #f0f9eb;
  border-color: #67c23a;
}

.detail-option.wrong {
  background: #fef0f0;
  border-color: #f56c6c;
}

.opt-key {
  font-weight: 700;
  margin-right: 6px;
}

.detail-line {
  margin-bottom: 8px;
  line-height: 1.7;
}

@media (max-width: 767px) {
  :deep(.el-form--inline .el-form-item) {
    display: block;
    margin-right: 0;
  }
  :deep(.el-form--inline .el-select) {
    width: 100% !important;
  }
}
</style>
