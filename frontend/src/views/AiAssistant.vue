<template>
  <div>
    <el-alert v-if="!configured" type="warning" :closable="false" show-icon class="config-alert"
      title="AI 助手尚未配置"
      description="请联系管理员在「后台管理 → AI 配置」中填写 API 密钥并启用后使用。" />

    <el-card class="chat-card">
      <div class="chat-header">
        <span class="chat-title">AI 学习助手</span>
        <el-button size="small" type="primary" :loading="analyzing" :disabled="!configured" @click="onWeakPoint">
          薄弱点分析
        </el-button>
      </div>

      <div class="chat-body" ref="chatBody">
        <div v-if="messages.length === 0" class="welcome">
          <p>👋 我是你的 408 学习助手，可以帮你：</p>
          <p>· 解答数据结构、组成原理、操作系统、计算机网络的问题</p>
          <p>· 针对你的薄弱知识点给出复习建议</p>
        </div>
        <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role === 'user' ? 'user' : 'assistant']">
          <div class="bubble">
            <span class="bubble-text">{{ msg.content }}</span><span v-if="isStreamingMsg(i, msg)" class="cursor"></span>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input v-model="input" type="textarea" :rows="3" placeholder="输入你的问题，例如：请解释什么是死锁？"
          :disabled="!configured" @keydown.enter.exact.prevent="onSend" />
        <el-button v-if="streaming" type="danger" plain @click="stopGeneration">停止</el-button>
        <el-button v-else type="primary" :loading="sending" :disabled="!configured || !input.trim()" @click="onSend">
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiConfigStatus, getAiHistory } from '@/api'

const route = useRoute()
const configured = ref(false)
const messages = ref([])
const input = ref('')
const sending = ref(false)
const analyzing = ref(false)
const chatBody = ref()
const activeController = ref(null)

const streaming = computed(() => sending.value || analyzing.value)

const context = {
  courseId: route.query.courseId || undefined,
  knowledgePointId: route.query.knowledgePointId || undefined
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

const isStreamingMsg = (i, msg) =>
  msg.role === 'assistant' && i === messages.value.length - 1 && streaming.value

const stopGeneration = () => {
  if (activeController.value) {
    activeController.value.abort()
  }
}

/**
 * 调用 SSE 流式接口。
 * 协议：每个 data 行是一个 JSON 字符串（一个文本片段）；`[DONE]` 表示结束；`[ERROR]` 后跟一条 JSON 字符串错误信息。
 */
const streamAi = async (url, body, { onChunk, onError, signal } = {}) => {
  const token = localStorage.getItem('token')
  const resp = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(body),
    signal
  })

  if (!resp.ok || !resp.body) {
    let msg = `请求失败(${resp.status})`
    try {
      const data = await resp.json()
      msg = data.message || msg
    } catch (e) {
      // ignore
    }
    if (resp.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      throw new Error('登录已过期')
    }
    throw new Error(msg)
  }

  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let errorFlag = false

  const handleData = (data) => {
    if (data === '[DONE]') return true
    if (data === '[ERROR]') {
      errorFlag = true
      return false
    }
    if (errorFlag) {
      errorFlag = false
      let msg = data
      try {
        msg = JSON.parse(data)
      } catch (e) {
        // ignore
      }
      onError && onError(msg)
      return true
    }
    let chunk = data
    try {
      chunk = JSON.parse(data)
    } catch (e) {
      // 非 JSON 则原样输出
    }
    onChunk && onChunk(chunk)
    return false
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let idx
    while ((idx = buffer.indexOf('\n\n')) >= 0) {
      const rawEvent = buffer.slice(0, idx)
      buffer = buffer.slice(idx + 2)
      let ended = false
      for (const line of rawEvent.split('\n')) {
        if (!line.startsWith('data:')) continue
        if (handleData(line.slice(5).trim())) {
          ended = true
          break
        }
      }
      if (ended) return
    }
  }
}

const appendAssistant = () => {
  messages.value.push({ role: 'assistant', content: '' })
  return messages.value[messages.value.length - 1]
}

const onSend = async () => {
  const text = input.value.trim()
  if (!text) return
  input.value = ''
  messages.value.push({ role: 'user', content: text })
  const assistantMsg = appendAssistant()
  sending.value = true
  activeController.value = new AbortController()
  await scrollToBottom()
  try {
    await streamAi('/api/ai/chat', { message: text, ...context }, {
      signal: activeController.value.signal,
      onChunk: (chunk) => {
        assistantMsg.content += chunk
        scrollToBottom()
      },
      onError: (msg) => {
        assistantMsg.content = assistantMsg.content || `请求失败：${msg}`
        ElMessage.error(msg)
      }
    })
  } catch (e) {
    if (e.name === 'AbortError') {
      assistantMsg.content = assistantMsg.content
        ? `${assistantMsg.content}\n（已停止生成）`
        : '（已停止生成）'
    } else {
      assistantMsg.content = `请求失败：${e.message}`
    }
  } finally {
    sending.value = false
    activeController.value = null
    await scrollToBottom()
  }
}

const onWeakPoint = async () => {
  analyzing.value = true
  activeController.value = new AbortController()
  const assistantMsg = appendAssistant()
  await scrollToBottom()
  try {
    await streamAi('/api/ai/weak-point-analysis', {}, {
      signal: activeController.value.signal,
      onChunk: (chunk) => {
        assistantMsg.content += chunk
        scrollToBottom()
      },
      onError: (msg) => {
        assistantMsg.content = assistantMsg.content || `请求失败：${msg}`
        ElMessage.error(msg)
      }
    })
  } catch (e) {
    if (e.name === 'AbortError') {
      assistantMsg.content = assistantMsg.content
        ? `${assistantMsg.content}\n（已停止生成）`
        : '（已停止生成）'
    } else {
      assistantMsg.content = `请求失败：${e.message}`
    }
  } finally {
    analyzing.value = false
    activeController.value = null
    await scrollToBottom()
  }
}

const loadHistory = async () => {
  try {
    const res = await getAiHistory()
    const list = res.data || []
    messages.value = list.map((r) => ({ role: r.role, content: r.content }))
    await scrollToBottom()
  } catch (e) {
    // 历史加载失败不影响正常使用
  }
}

onMounted(async () => {
  try {
    const res = await aiConfigStatus()
    configured.value = res.data === true
  } catch (e) {
    configured.value = false
  }
  await loadHistory()
})
</script>

<style scoped>
.config-alert {
  margin-bottom: 16px;
}

.chat-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 160px);
  min-height: 400px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.chat-title {
  font-size: 17px;
  font-weight: 600;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
}

.welcome {
  color: #909399;
  line-height: 2;
  text-align: center;
  margin-top: 40px;
}

.message {
  display: flex;
  margin-bottom: 14px;
}

.message.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 10px;
  line-height: 1.7;
  word-break: break-word;
}

.bubble-text {
  white-space: pre-wrap;
}

.message.user .bubble {
  background: #409eff;
  color: #fff;
}

.message.assistant .bubble {
  background: #f0f2f5;
  color: #303133;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  vertical-align: -2px;
  background: #409eff;
  animation: blink 1s steps(1) infinite;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  50.01%,
  100% {
    opacity: 0;
  }
}

.chat-input {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  flex-wrap: wrap;
}

.chat-input :deep(.el-textarea) {
  flex: 1;
  min-width: 200px;
}

.chat-input .el-button {
  height: 56px;
  padding: 0 24px;
}

@media (max-width: 767px) {
  .chat-card {
    height: calc(100vh - 140px);
  }

  .chat-input :deep(.el-textarea) {
    flex: 1 1 100%;
  }

  .chat-input .el-button {
    height: 44px;
    padding: 0 16px;
  }
}
</style>
