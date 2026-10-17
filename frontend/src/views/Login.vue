<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">408 学习平台</h2>
      <p class="sub">登录你的账号</p>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" size="large" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" class="submit" size="large" :loading="loading" @click="onSubmit">登 录</el-button>
      </el-form>
      <div class="foot">
        还没有账号？<router-link to="/register" class="link">立即注册</router-link>
      </div>
      <div class="tip">默认管理员：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await store.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    // 错误信息已由拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-card {
  width: 400px;
  padding: 10px 20px;
}

.title {
  text-align: center;
  color: #303133;
}

.sub {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin: 8px 0 24px;
}

.submit {
  width: 100%;
}

.foot {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}

.link {
  color: #409eff;
}

.tip {
  text-align: center;
  margin-top: 12px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
