<template>
  <div class="ai-chat-page">
    <a-card title="AI Health Assistant" style="max-width: 800px; margin: 0 auto">
      <!-- 对话历史 -->
      <div class="chat-history" ref="chatHistoryRef">
        <div v-if="messages.length === 0" class="empty-hint">
          Ask me anything about Traditional Chinese Medicine...
        </div>
        <div v-for="(msg, index) in messages" :key="index"
             :class="['message', msg.role]">
          <div class="message-content">{{ msg.content }}</div>
        </div>
        <div v-if="streaming" class="message assistant">
          <div class="message-content">{{ streamingText }}<span class="cursor">|</span></div>
        </div>
      </div>

      <!-- Metadata Filtering -->
      <div class="filter-row">
        <a-select v-model:value="docType" placeholder="Doc Type (optional)"
                  allowClear style="width: 160px">
          <a-select-option value="herb">Herb</a-select-option>
          <a-select-option value="treatment">Treatment</a-select-option>
          <a-select-option value="diagnosis">Diagnosis</a-select-option>
          <a-select-option value="formula">Formula</a-select-option>      <!-- 新加 -->
          <a-select-option value="condition">Condition</a-select-option>  <!-- 新加 -->
          <a-select-option value="general">General</a-select-option>
        </a-select>
        <a-input v-model:value="symptomTag" placeholder="Symptom tag (optional)"
                 style="width: 180px" allowClear />
      </div>

      <!-- 输入框 -->
      <div class="input-row">
        <a-textarea
          v-model:value="inputText"
          placeholder="Ask a question..."
          :rows="2"
          :disabled="streaming"
          @keydown.enter.exact.prevent="handleSend"
          style="flex: 1"
        />
        <a-button type="primary" :loading="streaming"
                  :disabled="!inputText.trim()" @click="handleSend">
          Send
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { chatWithRag } from '@/api/ragController'

const inputText = ref('')
const docType = ref<string | undefined>(undefined)
const symptomTag = ref('')
const streaming = ref(false)
const streamingText = ref('')
const chatHistoryRef = ref<HTMLElement>()

const messages = ref<{ role: string; content: string }[]>([])

const scrollToBottom = async () => {
  await nextTick()
  if (chatHistoryRef.value) {
    chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
  }
}

const handleSend = async () => {
  const question = inputText.value.trim()
  if (!question || streaming.value) return

  // 加入用户消息
  messages.value.push({ role: 'user', content: question })
  inputText.value = ''
  streamingText.value = ''
  streaming.value = true
  await scrollToBottom()

  await chatWithRag(
    {
      question,
      docType: docType.value,
      symptomTag: symptomTag.value || undefined,
      history: messages.value.slice(-6), // 最近3轮对话
    },
    (token) => {
      if (token && !streamingText.value.endsWith(' ') && !token.startsWith(' ')) {
        streamingText.value += ' ' + token
      } else {
        streamingText.value += token
      }
      scrollToBottom()
    },
    () => {
      messages.value.push({ role: 'assistant', content: streamingText.value })
      streamingText.value = ''
      streaming.value = false
      scrollToBottom()
    },
  )
}
</script>

<style scoped>
.ai-chat-page {
  padding: 24px;
}
.chat-history {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  margin-bottom: 16px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}
.empty-hint {
  color: #aaa;
  text-align: center;
  margin-top: 100px;
}
.message {
  margin-bottom: 12px;
  display: flex;
}
.message.user {
  justify-content: flex-end;
}
.message.assistant {
  justify-content: flex-start;
}
.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.message.user .message-content {
  background: #1677ff;
  color: white;
}
.message.assistant .message-content {
  background: white;
  border: 1px solid #e8e8e8;
  color: #333;
}
.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.input-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.cursor {
  animation: blink 1s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
