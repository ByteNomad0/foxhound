<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">知识点管理</h2>
      <div class="toolbar-right">
        <el-select v-model="courseId" placeholder="请选择课程" style="width: 200px" @change="load">
          <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" :disabled="!courseId" @click="openCreate">新增知识点</el-button>
      </div>
    </div>

    <el-table :data="kps" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="层级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.parentId === 0 ? 'primary' : 'info'" size="small">{{ row.parentId === 0 ? '章节' : '知识点' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属章节" width="180">
        <template #default="{ row }">{{ parentTitle(row.parentId) }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识点' : '新增知识点'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="上级章节">
          <el-select v-model="form.parentId" clearable placeholder="无（作为顶层章节）" style="width: 100%">
            <el-option v-for="p in topLevelKps" :key="p.id" :label="p.title" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细讲解">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="知识点详细讲解，支持换行" />
        </el-form-item>
        <el-form-item label="重点">
          <el-input v-model="form.keyPoint" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="难点">
          <el-input v-model="form.difficultPoint" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { getAdminCourses, getAdminKnowledgePoints, createKnowledgePoint, updateKnowledgePoint, deleteKnowledgePoint } from '@/api'

const courses = ref([])
const courseId = ref(null)
const kps = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({ id: null, title: '', parentId: null, content: '', keyPoint: '', difficultPoint: '', sort: 0 })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

const topLevelKps = computed(() => kps.value.filter((k) => (k.parentId === 0 || !k.parentId) && k.id !== form.id))

const parentTitle = (pid) => {
  if (!pid || pid === 0) return '-'
  const p = kps.value.find((k) => k.id === pid)
  return p ? p.title : '-'
}

const load = async () => {
  if (!courseId.value) {
    kps.value = []
    return
  }
  loading.value = true
  try {
    const res = await getAdminKnowledgePoints(courseId.value)
    kps.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, title: '', parentId: null, content: '', keyPoint: '', difficultPoint: '', sort: 0 })
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id, title: row.title, parentId: row.parentId === 0 ? null : row.parentId,
    content: row.content, keyPoint: row.keyPoint, difficultPoint: row.difficultPoint, sort: row.sort
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form, courseId: courseId.value, parentId: form.parentId || 0 }
    if (form.id) {
      await updateKnowledgePoint(form.id, payload)
    } else {
      await createKnowledgePoint(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除知识点「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteKnowledgePoint(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  const res = await getAdminCourses()
  courses.value = res.data || []
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

.toolbar-right {
  display: flex;
  gap: 10px;
}
</style>
