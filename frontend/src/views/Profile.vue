<template>
  <div>
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card class="block">
          <h3 class="block-title">个人资料</h3>
          <el-form :model="profile" label-width="80px">
            <el-form-item label="用户名">
              <el-input :model-value="profile.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profile.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profile.email" />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="profile.avatar" placeholder="可填图片链接" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="onSaveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="block">
          <h3 class="block-title">修改密码</h3>
          <el-form :model="pwd" :rules="pwdRules" ref="pwdRef" label-width="80px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwd.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwd.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPwd" @click="onChangePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMe, updateProfile, changePassword } from '@/api'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const savingProfile = ref(false)
const savingPwd = ref(false)
const pwdRef = ref()

const profile = reactive({ username: '', nickname: '', email: '', avatar: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== pwd.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6-20 之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const onSaveProfile = async () => {
  savingProfile.value = true
  try {
    await updateProfile({ nickname: profile.nickname, email: profile.email, avatar: profile.avatar })
    await store.fetchMe()
    ElMessage.success('保存成功')
  } finally {
    savingProfile.value = false
  }
}

const onChangePwd = async () => {
  await pwdRef.value.validate()
  savingPwd.value = true
  try {
    await changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码修改成功')
    pwd.oldPassword = ''
    pwd.newPassword = ''
    pwd.confirmPassword = ''
  } finally {
    savingPwd.value = false
  }
}

onMounted(async () => {
  const res = await getMe()
  Object.assign(profile, {
    username: res.data.username,
    nickname: res.data.nickname,
    email: res.data.email,
    avatar: res.data.avatar
  })
})
</script>

<style scoped>
.block {
  margin-bottom: 20px;
}

.block-title {
  font-size: 17px;
  margin-bottom: 20px;
}
</style>
