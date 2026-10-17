<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">课程管理</h2>
      <el-button type="primary" @click="openCreate">新增课程</el-button>
    </div>

    <el-table :data="courses" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="课程名称" width="180" />
      <el-table-column prop="code" label="代码" width="100" />
      <el-table-column prop="subject" label="学科" width="140" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="代码">
          <el-input v-model="form.code" placeholder="如 DS" />
        </el-form-item>
        <el-form-item label="学科">
          <el-input v-model="form.subject" placeholder="如 数据结构" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="form.cover" placeholder="可选" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminCourses, createCourse, updateCourse, deleteCourse } from '@/api'

const courses = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({ id: null, name: '', code: '', subject: '', description: '', cover: '', sort: 0, status: 1 })
const rules = { name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try {
    const res = await getAdminCourses()
    courses.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, name: '', code: '', subject: '', description: '', cover: '', sort: 0, status: 1 })
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id, name: row.name, code: row.code, subject: row.subject,
    description: row.description, cover: row.cover, sort: row.sort, status: row.status
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateCourse(form.id, form)
    } else {
      await createCourse(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除课程「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
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
</style>
