<template>
  <div>
    <el-card v-if="!started" class="start-card">
      <h3 class="start-title">刷题练习</h3>
      <el-form :inline="true" :model="form">
        <el-form-item label="课程">
          <el-select v-model="form.courseId" clearable placeholder="全部课程" style="width: 170px">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.type" clearable placeholder="全部题型" style="width: 130px">
            <el-option label="单选题" value="SINGLE" />
            <el-option label="多选题" value="MULTI" />
            <el-option label="判断题" value="JUDGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" clearable placeholder="全部难度" style="width: 130px">
            <el-option v-for="d in 5" :key="d" :label="`难度 ${d}`" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.count" :min="1" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="start">开始练习</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else class="quiz-card">
      <div class="quiz-header">
        <span class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
        <span class="score" v-if="finished">得分：{{ correctCount }} / {{ questions.length }}</span>
        <el-button size="small" @click="restart">退出</el-button>
      </div>

      <template v-if="!finished">
        <div class="question-meta">
          <el-tag size="small" :type="tagType(current.type)">{{ typeLabel(current.type) }}</el-tag>
          <el-tag v-if="current.courseName" size="small" type="info">{{ current.courseName }}</el-tag>
          <el-tag v-if="current.knowledgePointTitle" size="small" type="info">{{ current.knowledgePointTitle }}</el-tag>
          <el-tag v-if="current.difficulty" size="small" type="warning">难度 {{ current.difficulty }}</el-tag>
        </div>
        <div class="question-content">{{ current.content }}</div>

        <div class="options">
          <div v-for="opt in displayOptions" :key="opt.key" class="option" :class="optionClass(opt.key)" @click="onSelect(opt.key)">
            <span class="opt-key">{{ opt.key }}</span>
            <span class="opt-content">{{ opt.content }}</span>
          </div>
        </div>

        <div class="quiz-actions">
          <el-button v-if="!result" type="primary" :disabled="selected.length === 0" :loading="submitting" @click="submit">
            提交答案
          </el-button>
          <template v-else>
            <el-button v-if="currentIndex < questions.length - 1" type="primary" @click="next">下一题</el-button>
            <el-button v-else type="success" @click="finish">查看成绩</el-button>
          </template>
        </div>

        <el-result v-if="result" :icon="result.correct ? 'success' : 'error'" :title="result.correct ? '回答正确' : '回答错误'">
          <template #sub-title>
            <div class="analysis">
              <p><b>正确答案：</b>{{ (result.correctAnswer || []).join('、') }}</p>
              <p v-if="result.analysis"><b>解析：</b>{{ result.analysis }}</p>
            </div>
          </template>
        </el-result>
      </template>

      <el-result v-else icon="success" title="练习完成" :sub-title="`共 ${questions.length} 题，答对 ${correctCount} 题，正确率 ${accuracy}%`">
        <template #extra>
          <el-button type="primary" @click="restart">再来一组</el-button>
          <el-button @click="$router.push('/history')">查看答题历史</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourses, getPracticeQuestions, submitAnswer } from '@/api'

const route = useRoute()
const courses = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = reactive({ courseId: null, type: null, difficulty: null, count: 10, knowledgePointId: null })

const started = ref(false)
const questions = ref([])
const currentIndex = ref(0)
const selected = ref([])
const result = ref(null)
const finished = ref(false)
const correctCount = ref(0)

const current = computed(() => questions.value[currentIndex.value] || {})
const displayOptions = computed(() => {
  if (current.value.type === 'JUDGE') {
    return [
      { key: 'T', content: '正确' },
      { key: 'F', content: '错误' }
    ]
  }
  return current.value.options || []
})
const accuracy = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round((correctCount.value / questions.value.length) * 100)
})

const typeLabel = (type) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题' }[type] || type)
const tagType = (type) => ({ SINGLE: 'primary', MULTI: 'success', JUDGE: 'warning' }[type] || 'info')

const optionClass = (key) => {
  if (!result.value) {
    return selected.value.includes(key) ? 'selected' : ''
  }
  if ((result.value.correctAnswer || []).includes(key)) return 'correct'
  if (selected.value.includes(key)) return 'wrong'
  return ''
}

const onSelect = (key) => {
  if (result.value) return
  if (current.value.type === 'MULTI') {
    const idx = selected.value.indexOf(key)
    if (idx >= 0) selected.value.splice(idx, 1)
    else selected.value.push(key)
  } else {
    selected.value = [key]
  }
}

const start = async () => {
  loading.value = true
  try {
    const res = await getPracticeQuestions({
      courseId: form.courseId || undefined,
      knowledgePointId: form.knowledgePointId || undefined,
      type: form.type || undefined,
      difficulty: form.difficulty || undefined,
      count: form.count
    })
    questions.value = res.data || []
    if (questions.value.length === 0) {
      ElMessage.warning('没有符合条件的题目')
      return
    }
    started.value = true
    currentIndex.value = 0
    correctCount.value = 0
    finished.value = false
    selected.value = []
    result.value = null
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (selected.value.length === 0) return
  submitting.value = true
  try {
    const res = await submitAnswer({ questionId: current.value.id, answer: selected.value })
    result.value = res.data
    if (res.data.correct) correctCount.value++
  } finally {
    submitting.value = false
  }
}

const next = () => {
  currentIndex.value++
  selected.value = []
  result.value = null
}

const finish = () => {
  finished.value = true
}

const restart = () => {
  started.value = false
  questions.value = []
  finished.value = false
  result.value = null
  selected.value = []
}

onMounted(async () => {
  const res = await getCourses()
  courses.value = res.data || []
  if (route.query.courseId) {
    form.courseId = Number(route.query.courseId)
  }
  if (route.query.knowledgePointId) {
    form.knowledgePointId = Number(route.query.knowledgePointId)
  }
  if (form.courseId || form.knowledgePointId) {
    start()
  }
})
</script>

<style scoped>
.start-title {
  margin-bottom: 20px;
}

.quiz-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.progress {
  font-weight: 600;
}

.score {
  color: #67c23a;
  font-weight: 600;
}

.quiz-header .el-button {
  margin-left: auto;
}

.question-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.question-content {
  font-size: 17px;
  line-height: 1.8;
  margin-bottom: 24px;
  white-space: pre-wrap;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.option {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.option:hover {
  border-color: #409eff;
}

.option.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.option.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.option.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.opt-key {
  font-weight: 700;
  min-width: 22px;
}

.opt-content {
  line-height: 1.6;
}

.quiz-actions {
  margin-bottom: 16px;
}

.analysis {
  text-align: left;
  line-height: 1.8;
  color: #606266;
}
</style>
