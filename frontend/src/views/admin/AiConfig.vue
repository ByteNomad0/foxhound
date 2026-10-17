<template>
  <div>
    <h2 class="page-title">AI 助手配置</h2>

    <el-alert type="info" :closable="false" show-icon class="tip"
      title="兼容 OpenAI 接口的大模型均可接入"
      description="支持 DeepSeek、通义千问（兼容模式）、OpenAI、Ollama 等。保存后普通用户即可在「AI 助手」中使用知识点答疑与薄弱点分析。" />

    <el-card>
      <el-form :model="form" label-width="120px" class="config-form">
        <el-form-item label="提供商">
          <el-select v-model="form.provider" style="width: 220px">
            <el-option label="OpenAI 兼容" value="openai" />
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
          </el-select>
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input v-model="form.baseUrl" placeholder="如 https://api.deepseek.com/v1" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="填写你的密钥；留空或保持脱敏值则不修改" />
        </el-form-item>
        <el-form-item label="模型名">
          <el-input v-model="form.model" placeholder="如 deepseek-chat / qwen-plus / gpt-4o-mini" />
        </el-form-item>
        <el-form-item label="系统提示词">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="4"
            placeholder="可选，留空使用默认提示词" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiConfig, saveAiConfig } from '@/api'

const saving = ref(false)
const form = reactive({ provider: 'openai', baseUrl: '', apiKey: '', model: '', systemPrompt: '', enabled: 0 })

const onSave = async () => {
  saving.value = true
  try {
    await saveAiConfig(form)
    ElMessage.success('配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const res = await getAiConfig()
  if (res.data) {
    Object.assign(form, {
      provider: res.data.provider || 'openai',
      baseUrl: res.data.baseUrl || '',
      apiKey: res.data.apiKey || '',
      model: res.data.model || '',
      systemPrompt: res.data.systemPrompt || '',
      enabled: res.data.enabled ?? 0
    })
  }
})
</script>

<style scoped>
.page-title {
  margin-bottom: 16px;
}

.tip {
  margin-bottom: 16px;
}

.config-form {
  max-width: 640px;
}
</style>
