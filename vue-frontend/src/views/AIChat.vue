<template>
  <div class="sd-page aichat-page">
    <div class="chat-shell">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-left">
          <span class="ai-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="8" width="18" height="12" rx="3"/>
              <path d="M12 8V4"/>
              <circle cx="12" cy="3" r="1.4"/>
              <circle cx="8.5" cy="14" r="1.2"/>
              <circle cx="15.5" cy="14" r="1.2"/>
              <path d="M9.5 17.5h5"/>
            </svg>
          </span>
          <div class="header-text">
            <h2>AI 助手</h2>
            <p class="header-sub">
              <i class="online-dot"></i>
              随时为你解答学习与生活问题
            </p>
          </div>
        </div>

        <div class="header-right">
          <span class="provider-badge" :title="'当前模型：' + providerName">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
            {{ providerName }}
          </span>
          <button class="clear-btn" @click="clearChat" title="清空对话">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 消息区 -->
      <div class="chat-box" ref="chatBox">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']"
        >
          <div class="message-avatar">
            <svg v-if="msg.role !== 'user'" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="8" width="18" height="12" rx="3"/>
              <path d="M12 8V4"/>
              <circle cx="8.5" cy="14" r="1.2"/>
              <circle cx="15.5" cy="14" r="1.2"/>
            </svg>
            <template v-else>{{ userAvatar }}</template>
          </div>
          <div class="message-body">
            <div class="message-bubble">
              <div class="message-text">{{ msg.content }}</div>
            </div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <div v-if="loading" class="message message-ai">
          <div class="message-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="8" width="18" height="12" rx="3"/>
              <path d="M12 8V4"/>
              <circle cx="8.5" cy="14" r="1.2"/>
              <circle cx="15.5" cy="14" r="1.2"/>
            </svg>
          </div>
          <div class="message-body">
            <div class="message-bubble typing-bubble">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-text">正在思考</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷提问 -->
      <div v-if="messages.length <= 1" class="quick-area">
        <p class="quick-label">试试这样问我</p>
        <div class="quick-list">
          <button
            v-for="q in quickPrompts"
            :key="q"
            class="quick-item"
            :disabled="loading"
            @click="usePrompt(q)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="13 17 18 12 13 7"/>
              <polyline points="6 17 11 12 6 7"/>
            </svg>
            {{ q }}
          </button>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <textarea
          v-model="inputMessage"
          @keyup.enter.ctrl="sendMessage"
          placeholder="输入你的问题，Ctrl + Enter 快速发送"
          rows="3"
          :disabled="loading"
          class="chat-input"
        ></textarea>
        <div class="input-side">
          <button
            class="send-btn"
            :disabled="loading || !inputMessage.trim()"
            @click="sendMessage"
          >
            <span v-if="loading" class="send-spinner"></span>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
            {{ loading ? '生成中' : '发送' }}
          </button>
          <span class="input-hint">Ctrl + Enter</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'AIChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      loading: false,
      providerName: '加载中...',
      quickPrompts: [
        '帮我制定一份 Java 学习计划',
        '解释一下什么是数据库索引',
        '推荐几个提高学习效率的方法',
        '帮我润色一段自我介绍'
      ]
    }
  },
  computed: {
    userAvatar() {
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      const text = (user?.userName || '').trim()
      if (!text) return '我'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    }
  },
  created() {
    this.fetchProvider()
    this.messages.push({ role: 'ai', content: '你好！我是AI助手，有什么可以帮你的？', time: this.getCurrentTime() })
  },
  methods: {
    async sendMessage() {
      if (!this.inputMessage.trim() || this.loading) return
      const userMessage = this.inputMessage.trim()
      this.messages.push({ role: 'user', content: userMessage, time: this.getCurrentTime() })
      this.inputMessage = ''
      this.loading = true
      this.scrollToBottom()
      try {
        const response = await api.post('/ai-chat/chat', null, { params: { message: userMessage } })
        if (response.success) {
          this.messages.push({ role: 'ai', content: response.message, time: this.getCurrentTime() })
        } else {
          this.messages.push({ role: 'ai', content: '抱歉，' + (response.message || '出错了'), time: this.getCurrentTime() })
        }
      } catch (error) {
        this.messages.push({ role: 'ai', content: '对话失败，请稍后重试', time: this.getCurrentTime() })
      } finally { this.loading = false; this.scrollToBottom() }
    },
    usePrompt(text) {
      if (this.loading) return
      this.inputMessage = text
      this.sendMessage()
    },
    clearChat() {
      if (this.loading) return
      this.messages = [
        { role: 'ai', content: '对话已清空，我们重新开始吧！', time: this.getCurrentTime() }
      ]
      this.scrollToBottom()
    },
    getCurrentTime() { return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) },
    scrollToBottom() {
      this.$nextTick(() => { const el = this.$refs.chatBox; if (el) el.scrollTop = el.scrollHeight })
    },
    async fetchProvider() {
      try { const r = await api.get('/ai-chat/provider'); if (r.name) this.providerName = r.name } catch (e) {}
    }
  }
}
</script>

<style scoped>
.aichat-page {
  max-width: 860px;
}

.chat-shell {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 132px);
  min-height: 480px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius-xl);
  box-shadow: var(--sd-shadow);
  overflow: hidden;
}

/* ===== 头部 ===== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 15px 22px;
  border-bottom: 1px solid var(--sd-border-soft);
  background: linear-gradient(90deg, #fbfdff 0%, #ffffff 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.ai-avatar {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.ai-avatar svg {
  width: 22px;
  height: 22px;
}

.header-text h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--sd-text);
  letter-spacing: -0.2px;
}

.header-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--sd-text-4);
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--sd-success);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
  animation: sd-pulse 2s ease-in-out infinite;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-shrink: 0;
}

.provider-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 190px;
  padding: 5px 12px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  border: 1px solid #bfdbfe;
  color: var(--sd-primary-dark);
  font-size: 12px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.provider-badge svg {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
}

.clear-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  border: 1px solid var(--sd-border-soft);
  background: #fff;
  color: var(--sd-text-4);
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  color: var(--sd-danger);
  border-color: #fecaca;
  background: #fef2f2;
}

.clear-btn svg {
  width: 15px;
  height: 15px;
}

/* ===== 消息区 ===== */
.chat-box {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
  background:
    radial-gradient(circle at 12% 18%, rgba(59, 130, 246, 0.05), transparent 42%),
    radial-gradient(circle at 88% 82%, rgba(99, 102, 241, 0.05), transparent 42%),
    #f8fafc;
}

.chat-box::-webkit-scrollbar {
  width: 7px;
}

.chat-box::-webkit-scrollbar-thumb {
  background: #d7dfeb;
  border-radius: 999px;
}

.chat-box::-webkit-scrollbar-thumb:hover {
  background: #c2cede;
}

.message {
  display: flex;
  gap: 11px;
  margin-bottom: 18px;
  animation: msg-in 0.3s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes msg-in {
  from { opacity: 0; transform: translateY(9px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  font-size: 12px;
  font-weight: 700;
}

.message-avatar svg {
  width: 18px;
  height: 18px;
}

.message-ai .message-avatar {
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.message-user .message-avatar {
  background: #e2e8f0;
  color: var(--sd-text-2);
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 74%;
  min-width: 0;
}

.message-user .message-body {
  align-items: flex-end;
}

.message-bubble {
  padding: 11px 15px;
  border-radius: 15px;
  font-size: 14px;
  line-height: 1.75;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
}

.message-ai .message-bubble {
  background: #fff;
  border: 1px solid var(--sd-border-soft);
  color: var(--sd-text-2);
  border-top-left-radius: 5px;
}

.message-user .message-bubble {
  background: var(--sd-gradient);
  color: #fff;
  border-top-right-radius: 5px;
  box-shadow: var(--sd-shadow-primary);
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: var(--sd-text-4);
  padding: 0 3px;
}

/* 思考中 */
.typing-bubble {
  display: flex;
  align-items: center;
  gap: 5px;
}

.typing-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--sd-primary);
  opacity: 0.45;
  animation: typing-bounce 1.25s ease-in-out infinite;
}

.typing-dot:nth-child(2) { animation-delay: 0.16s; }
.typing-dot:nth-child(3) { animation-delay: 0.32s; }

@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-5px); opacity: 1; }
}

.typing-text {
  margin-left: 5px;
  font-size: 12.5px;
  color: var(--sd-text-4);
}

/* ===== 快捷提问 ===== */
.quick-area {
  padding: 12px 22px 14px;
  border-top: 1px solid var(--sd-border-soft);
  background: #fcfdff;
}

.quick-label {
  margin: 0 0 9px;
  font-size: 12px;
  font-weight: 600;
  color: var(--sd-text-4);
}

.quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--sd-border);
  color: var(--sd-text-2);
  font-size: 12.5px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-item:hover:not(:disabled) {
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  transform: translateY(-2px);
}

.quick-item:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.quick-item svg {
  width: 13px;
  height: 13px;
  opacity: 0.7;
}

/* ===== 输入区 ===== */
.input-area {
  display: flex;
  gap: 12px;
  align-items: stretch;
  padding: 14px 22px 16px;
  border-top: 1px solid var(--sd-border-soft);
  background: #fff;
}

.chat-input {
  flex: 1;
  min-width: 0;
  padding: 11px 14px;
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  background: var(--sd-bg-input);
  font-size: 14px;
  line-height: 1.7;
  font-family: inherit;
  color: var(--sd-text);
  resize: none;
  outline: none;
  transition: all 0.22s ease;
}

.chat-input:focus {
  border-color: var(--sd-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.13);
}

.chat-input:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.input-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
}

.send-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 106px;
  padding: 11px 20px;
  border: none;
  border-radius: var(--sd-radius);
  background: var(--sd-gradient);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  box-shadow: var(--sd-shadow-primary);
  transition: all 0.22s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(59, 130, 246, 0.36);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.send-btn svg {
  width: 16px;
  height: 16px;
}

.send-spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

.input-hint {
  font-size: 11px;
  color: var(--sd-text-4);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .chat-shell {
    height: calc(100vh - 110px);
    min-height: 420px;
    border-radius: var(--sd-radius-lg);
  }

  .chat-header {
    padding: 12px 15px;
  }

  .header-text h2 {
    font-size: 15.5px;
  }

  .header-sub {
    display: none;
  }

  .provider-badge {
    max-width: 110px;
  }

  .chat-box,
  .quick-area,
  .input-area {
    padding-left: 14px;
    padding-right: 14px;
  }

  .message-body {
    max-width: 82%;
  }

  .quick-list {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 4px;
  }

  .quick-item {
    white-space: nowrap;
    flex-shrink: 0;
  }
}
</style>
