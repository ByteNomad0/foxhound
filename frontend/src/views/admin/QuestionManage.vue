<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">题库管理</h2>
      <el-button type="primary" @click="openCreate">新增题目</el-button>
    </div>

    <el-form :inline="true" class="filters">
      <el-form-item label="课程">
        <el-select v-model="query.courseId" clearable placeholder="全部课程" style="width: 170px" @change="load(1)">
          <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="题型">
        <el-select v-model="query.type" clearable placeholder="全部题型" style="width: 130px" @change="load(1)">
          <el-option label="单选题" value="SINGLE" />
          <el-option label="多选题" value="MULTI" />
          <el-option label="判断题" value="JUDGE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="搜索题干" clearable style="width: 220px" @keyup.enter="load(1)" @clear="load(1)" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="questions" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="题型" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="tagType(row.type)">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="题干" min-width="260" show-overflow-tooltip />
      <el-table-column prop="courseName" label="课程" width="130" show-overflow-tooltip />
      <el-table-column prop="knowledgePointTitle" label="知识点" width="140" show-overflow-tooltip />
      <el-table-column label="答案" width="90">
        <template #default="{ row }">{{ (row.answer || []).join('、') }}</template>
      </el-table-column>
      <el-table-column label="难度" width="70">
        <template #default="{ row }">{{ row.difficulty }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" background layout="total, prev, pager, next" :total="total"
      :page-size="query.size" :current-page="query.page" @current-change="load" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="720px" top="5vh">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="选择课程" style="width: 100%" @change="onCourseChange">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-select v-model="form.knowledgePointId" clearable placeholder="可选，关联知识点" style="width: 100%">
            <el-option v-for="k in kpOptions" :key="k.id" :label="k.title" :value="k.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio-button label="SINGLE">单选题</el-radio-button>
            <el-radio-button label="MULTI">多选题</el-radio-button>
            <el-radio-button label="JUDGE">判断题</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题干" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item v-if="form.type !== 'JUDGE'" label="选项">
          <div class="options-editor">
            <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
              <span class="opt-key">{{ opt.key }}</span>
              <el-input v-model="opt.content" placeholder="选项内容" />
              <el-button link type="danger" @click="form.options.splice(idx, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="addOption">+ 添加选项</el-button>
          </div>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-radio-group v-if="form.type === 'SINGLE'" v-model="answerSingle">
            <el-radio v-for="opt in form.options" :key="opt.key" :label="opt.key">{{ opt.key }}</el-radio>
          </el-radio-group>
          <el-checkbox-group v-else-if="form.type === 'MULTI'" v-model="form.answer">
            <el-checkbox v-for="opt in form.options" :key="opt.key" :label="opt.key">{{ opt.key }}</el-checkbox>
          </el-checkbox-group>
          <el-radio-group v-else v-model="answerSingle">
            <el-radio label="T">正确</el-radio>
            <el-radio label="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="form.difficulty" :max="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminCourses, getAdminKnowledgePoints, getAdminQuestions, getAdminQuestion,
  createQuestion, updateQuestion, deleteQuestion
} from '@/api'

const courses = ref([])
const kpOptions = ref([])
const questions = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ page: 1, size: 10, courseId: null, type: null, keyword: '' })

const form = reactive({
  id: null, courseId: null, knowledgePointId: null, type: 'SINGLE',
  content: '', options: [], answer: [], analysis: '', difficulty: 1
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{
    validator: (rule, value, callback) => {
      if (!value || value.length === 0) callback(new Error('请设置正确答案'))
      else callback()
    },
    trigger: 'change'
  }]
}

const typeLabel = (type) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题' }[type] || type)
const tagType = (type) => ({ SINGLE: 'primary', MULTI: 'success', JUDGE: 'warning' }[type] || 'info')

const answerSingle = computed({
  get: () => form.answer[0] || '',
  set: (v) => { form.answer = v ? [v] : [] }
})

const addOption = () => {
  const key = String.fromCharCode(65 + form.options.length)
  form.options.push({ key, content: '' })
}

const onTypeChange = (type) => {
  if (type === 'JUDGE') {
    form.answer = ['T']
  } else {
    if (form.options.length === 0) {
      form.options.push({ key: 'A', content: '' }, { key: 'B', content: '' })
    }
    form.answer = []
  }
}

const onCourseChange = async (courseId) => {
  form.knowledgePointId = null
  if (!courseId) {
    kpOptions.value = []
    return
  }
  const res = await getAdminKnowledgePoints(courseId)
  kpOptions.value = res.data || []
}

const load = async (page) => {
  if (page) query.page = page
  loading.value = true
  try {
    const res = await getAdminQuestions({
      page: query.page, size: query.size,
      courseId: query.courseId || undefined,
      type: query.type || undefined,
      keyword: query.keyword || undefined
    })
    questions.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null, courseId: null, knowledgePointId: null, type: 'SINGLE',
    content: '', options: [], answer: [], analysis: '', difficulty: 1
  })
  form.options.push({ key: 'A', content: '' }, { key: 'B', content: '' })
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (row) => {
  const res = await getAdminQuestion(row.id)
  const q = res.data
  Object.assign(form, {
    id: q.id, courseId: q.courseId, knowledgePointId: q.knowledgePointId, type: q.type,
    content: q.content, options: q.options ? [...q.options] : [],
    answer: q.answer ? [...q.answer] : [], analysis: q.analysis || '', difficulty: q.difficulty || 1
  })
  await onCourseChange(q.courseId)
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  if (form.type !== 'JUDGE') {
    const emptyOption = form.options.some((o) => !o.content.trim())
    if (emptyOption) {
      ElMessage.warning('请填写所有选项内容')
      return
    }
  }
  saving.value = true
  try {
    const payload = { ...form, options: form.type === 'JUDGE' ? [] : form.options }
    if (form.id) {
      await updateQuestion(form.id, payload)
    } else {
      await createQuestion(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load(query.page)
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除该题目吗？`, '提示', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  load(query.page)
}

onMounted(async () => {
  const res = await getAdminCourses()
  courses.value = res.data || []
  load(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
}

.filters {
  margin-bottom: 4px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.options-editor {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.opt-key {
  font-weight: 700;
  min-width: 24px;
}
</style>
