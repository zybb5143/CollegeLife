<template>
  <div class="sd-page private-chat-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">私信聊天</h1>
        <p class="sd-subtitle">与同学实时沟通，支持商品链接分享</p>
      </div>
      <div class="head-actions">
        <span class="conn-chip" :class="connected ? 'conn-on' : 'conn-off'">
          <i class="conn-dot"></i>
          {{ connected ? '实时连接中' : '连接断开' }}
        </span>
      </div>
    </div>

    <div class="chat-shell">
      <!-- 左侧对话列表 -->
      <aside class="conv-panel" :class="{ 'conv-hidden': mobileChatOpen }">
        <div class="conv-head">
          <h3 class="conv-title">
            消息列表
            <span class="count-pill">{{ conversations.length }}</span>
          </h3>
          <button class="conv-refresh" @click="loadConversations" title="刷新列表">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>

        <div class="conv-search">
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input class="sd-input" v-model="convKeyword" placeholder="搜索联系人..." />
          </div>
        </div>

        <div class="conv-scroll">
          <div v-if="filteredConversations.length === 0" class="conv-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span>{{ convKeyword ? '没有匹配的联系人' : '暂无消息' }}</span>
          </div>

          <div
            v-for="(conv, index) in filteredConversations"
            :key="index"
            class="conv-item"
            :class="{ active: currentTarget === conv.otherUser }"
            @click="selectConversation(conv.otherUser)"
          >
            <span class="conv-avatar">{{ nameInitial(conv.otherUser) }}</span>
            <div class="conv-text">
              <div class="conv-line1">
                <span class="conv-name">{{ conv.otherUser }}</span>
                <span class="conv-time">{{ formatShortTime(conv.sendTime) }}</span>
              </div>
              <div class="conv-line2">
                <span class="conv-preview">{{ truncateText(conv.messageContent, 20) }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧聊天窗口 -->
      <section class="chat-panel" :class="{ 'panel-open': mobileChatOpen }">
        <div v-if="!currentTarget" class="no-chat">
          <span class="no-chat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              <path d="M8 9h8M8 13h5"/>
            </svg>
          </span>
          <p class="no-chat-text">请选择一个对话开始聊天</p>
          <p class="no-chat-sub">也可以从二手市场点击「联系卖家」直接发起会话</p>
        </div>

        <div v-else class="chat-active">
          <div class="chat-header">
            <button class="mobile-back" @click="mobileChatOpen = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="19" y1="12" x2="5" y2="12"/>
                <polyline points="12 19 5 12 12 5"/>
              </svg>
            </button>
            <span class="chat-avatar">{{ nameInitial(currentTarget) }}</span>
            <div class="chat-head-text">
              <h3 class="chat-name">{{ currentTarget }}</h3>
              <p class="chat-status">
                <i class="status-dot" :class="connected ? 'dot-on' : 'dot-off'"></i>
                {{ connected ? '在线' : '连接中...' }}
              </p>
            </div>
            <span class="msg-count">{{ messages.length }} 条消息</span>
          </div>

          <div class="message-list" ref="messageList">
            <div v-if="messages.length === 0" class="msg-empty">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <span>还没有聊天记录，发条消息打个招呼吧</span>
            </div>

            <div
              v-for="msg in messages"
              :key="msg.messageId"
              class="message"
              :class="msg.senderName === currentUser ? 'message-self' : 'message-other'"
            >
              <div class="message-avatar">{{ nameInitial(msg.senderName) }}</div>
              <div class="message-content">
                <div class="message-sender">{{ msg.senderName }}</div>

                <!-- 商品消息 -->
                <div
                  v-if="msg.messageType === 'product' && msg.productId"
                  class="product-card"
                  @click="goToProduct(msg.productId)"
                >
                  <span class="product-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                      <line x1="7" y1="7" x2="7.01" y2="7"/>
                    </svg>
                  </span>
                  <div class="product-info">
                    <div class="product-name">{{ msg.messageContent }}</div>
                    <div class="product-hint">点击查看商品详情</div>
                  </div>
                  <svg class="product-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"/>
                    <polyline points="12 5 19 12 12 19"/>
                  </svg>
                </div>

                <!-- 普通文本消息 -->
                <div v-else class="message-text">{{ msg.messageContent }}</div>

                <div class="message-time">{{ formatTime(msg.sendTime) }}</div>
              </div>
            </div>
          </div>

          <div class="message-input">
            <textarea
              v-model="newMessage"
              placeholder="输入消息，Ctrl + Enter 发送..."
              @keyup.enter.ctrl="sendMessage"
              rows="3"
              class="chat-textarea"
            ></textarea>
            <button class="btn-send" :disabled="!newMessage.trim()" @click="sendMessage">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="22" y1="2" x2="11" y2="13"/>
                <polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
              发送
            </button>
          </div>
        </div>
      </section>
    </div>

    <div class="quick-nav">
      <router-link to="/secondhand" class="nav-pill">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="9" cy="21" r="1"/>
          <circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
        二手市场
      </router-link>
      <router-link to="/allties" class="nav-pill">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        返回首页
      </router-link>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'PrivateChat',
  data() {
    return {
      currentUser: '',
      conversations: [],
      currentTarget: '',
      messages: [],
      newMessage: '',
      wsSocket: null,
      connected: false,
      convKeyword: '',
      mobileChatOpen: false
    }
  },
  computed: {
    filteredConversations() {
      const key = this.convKeyword.trim().toLowerCase()
      if (!key) return this.conversations
      return this.conversations.filter(c => (c.otherUser || '').toLowerCase().includes(key))
    }
  },
  async created() {
    await this.getCurrentUser()
    if (this.currentUser) {
      this.loadConversations()
      this.connectWebSocket()

      // 如果 URL 中有 targetUser 参数，自动选中该用户
      const targetUser = this.$route.query.targetUser
      const productId = this.$route.query.productId
      const productName = this.$route.query.productName

      if (targetUser) {
        // 等待 WebSocket 连接和对话列表加载完成
        setTimeout(async () => {
          await this.selectConversation(targetUser)

          // 如果有商品信息，且是首次进入(没有历史记录)，才发送商品链接
          if (productId && productName && this.messages.length === 0) {
            await this.sendProductMessage(productId, productName)
          }
        }, 500)
      }
    }
  },
  beforeUnmount() {
    if (this.wsSocket) {
      this.wsSocket.close()
    }
  },
  methods: {
    async getCurrentUser() {
      try {
        const response = await api.get('/user/current')
        if (response.success && response.data) {
          this.currentUser = response.data.userName
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    async loadConversations() {
      try {
        const response = await api.get('/message/conversations')
        if (response.success) {
          // 处理对话列表，提取对话对象
          const convMap = new Map()
          response.data.forEach(msg => {
            const otherUser = msg.senderName === this.currentUser ? msg.receiverName : msg.senderName
            if (!convMap.has(otherUser)) {
              convMap.set(otherUser, {
                otherUser,
                messageContent: msg.messageContent,
                sendTime: msg.sendTime
              })
            }
          })
          this.conversations = Array.from(convMap.values())
        }
      } catch (error) {
        console.error('加载对话列表失败:', error)
      }
    },

    async selectConversation(targetUser) {
      this.currentTarget = targetUser
      this.mobileChatOpen = true
      // 先清空旧消息，避免切换会话瞬间显示上一个对话的内容
      this.messages = []
      await this.loadMessages(targetUser)
    },

    async loadMessages(targetUser) {
      try {
        // 通过 WebSocket 请求历史记录
        if (this.wsSocket && this.connected) {
          const requestData = {
            type: 'getHistory',
            targetUser: targetUser
          }
          this.wsSocket.send(JSON.stringify(requestData))
        } else {
          console.warn('WebSocket 未连接')
        }
      } catch (error) {
        console.error('加载消息失败:', error)
      }
    },

    async sendMessage() {
      if (!this.newMessage.trim() || !this.currentTarget) return

      try {
        // 通过 WebSocket 发送消息
        if (this.wsSocket && this.connected) {
          console.log('准备发送消息, readyState:', this.wsSocket.readyState)
          const messageData = {
            type: 'sendMessage',
            receiverName: this.currentTarget,
            messageContent: this.newMessage,
            messageType: 'text'
          }
          console.log('发送消息数据:', messageData)
          this.wsSocket.send(JSON.stringify(messageData))
          console.log('消息已发送')

          // 清空输入框
          this.newMessage = ''

          // 手动更新对话列表 (添加或更新当前对话)
          const now = new Date().toISOString()
          const existingConv = this.conversations.find(c => c.otherUser === this.currentTarget)
          if (existingConv) {
            existingConv.messageContent = messageData.messageContent
            existingConv.sendTime = now
          } else {
            this.conversations.unshift({
              otherUser: this.currentTarget,
              messageContent: messageData.messageContent,
              sendTime: now
            })
          }

          // 立即加载最新消息 (包含刚发送的缓存消息)
          await this.loadMessages(this.currentTarget)
        } else {
          console.error('WebSocket 未连接, wsSocket:', this.wsSocket, 'connected:', this.connected)
          this.$message.error('WebSocket 未连接，无法发送消息')
        }
      } catch (error) {
        console.error('发送失败:', error)
        this.$message.error('发送失败：' + error.message)
      }
    },

    connectWebSocket() {
      // 使用原生 WebSocket 连接
      const wsUrl = `ws://localhost:10010/ws/chat?userName=${this.currentUser}`
      this.wsSocket = new WebSocket(wsUrl)

      this.wsSocket.onopen = () => {
        this.connected = true
        console.log('WebSocket 已连接')
      }

      this.wsSocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('收到 WebSocket 消息:', data)

          // 根据消息类型处理
          if (data.type === 'historyData') {
            // 历史记录数据
            if (data.targetUser === this.currentTarget) {
              this.messages = data.messages
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            }
          } else if (data.type === 'sendSuccess') {
            // 发送成功确认 (可选处理)
            console.log('消息发送成功:', data.message)
          } else if (data.type === 'receiveMessage') {
            // 接收到对方发来的新消息
            this.loadConversations()
            // 如果是当前对话，刷新消息列表
            if (data.senderName === this.currentTarget) {
              this.loadMessages(this.currentTarget)
            }
          } else if (data.type === 'error') {
            // 服务端返回的错误提示
            this.$message.error(data.message || '消息处理失败')
          }
        } catch (e) {
          console.error('解析消息失败:', e)
        }
      }

      this.wsSocket.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this.connected = false
      }

      this.wsSocket.onclose = () => {
        console.log('WebSocket 已关闭')
        this.connected = false
      }
    },

    scrollToBottom() {
      if (this.$refs.messageList) {
        this.$refs.messageList.scrollTop = this.$refs.messageList.scrollHeight
      }
    },

    nameInitial(name) {
      const text = (name || '').trim()
      if (!text) return '?'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },

    truncateText(text, maxLength) {
      if (!text) return ''
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },

    formatTime(timeStr) {
      if (!timeStr) return ''
      try {
        const date = new Date(timeStr)
        return date.toLocaleString('zh-CN', {
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (e) {
        return timeStr
      }
    },

    formatShortTime(timeStr) {
      if (!timeStr) return ''
      try {
        const date = new Date(timeStr)
        const now = new Date()
        const isToday = date.toDateString() === now.toDateString()
        if (isToday) {
          return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        }
        return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
      } catch (e) {
        return ''
      }
    },

    goToProduct(productId) {
      // 跳转到商品详情页
      this.$router.push({
        path: '/secondhand/detail',
        query: { productId }
      })
    },

    async sendProductMessage(productId, productName) {
      // 发送商品链接消息
      if (this.wsSocket && this.connected && this.currentTarget) {
        const messageData = {
          type: 'sendMessage',
          receiverName: this.currentTarget,
          messageContent: productName,
          messageType: 'product',
          productId: parseInt(productId)
        }
        this.wsSocket.send(JSON.stringify(messageData))

        // 刷新对话列表和历史记录
        await this.loadConversations()
        await this.loadMessages(this.currentTarget)
      }
    }
  }
}
</script>

<style scoped>
.private-chat-page {
  max-width: 1140px;
}

.head-actions {
  display: flex;
  gap: 10px;
}

/* ===== 连接状态 ===== */
.conn-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.conn-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}

.conn-on {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.conn-on .conn-dot {
  animation: sd-pulse 2s ease-in-out infinite;
}

.conn-off {
  background: #f8fafc;
  color: var(--sd-text-4);
  border: 1px solid var(--sd-border);
}

/* ===== 主布局 ===== */
.chat-shell {
  display: flex;
  gap: 16px;
  height: calc(100vh - 210px);
  min-height: 520px;
}

/* ===== 左侧对话列表 ===== */
.conv-panel {
  width: 290px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius-lg);
  box-shadow: var(--sd-shadow-sm);
  overflow: hidden;
}

.conv-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 16px 16px 10px;
}

.conv-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--sd-text);
}

.count-pill {
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 11.5px;
  font-weight: 700;
}

.conv-refresh {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border: 1px solid var(--sd-border-soft);
  background: #fff;
  color: var(--sd-text-4);
  cursor: pointer;
  transition: all 0.2s ease;
}

.conv-refresh:hover {
  color: var(--sd-primary);
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
}

.conv-refresh svg {
  width: 14px;
  height: 14px;
}

.conv-search {
  padding: 0 16px 12px;
}

.conv-search .sd-input {
  height: 36px;
  font-size: 13px;
}

.conv-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px 12px;
}

.conv-scroll::-webkit-scrollbar {
  width: 6px;
}

.conv-scroll::-webkit-scrollbar-thumb {
  background: #dfe6ef;
  border-radius: 999px;
}

.conv-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 44px 16px;
  color: var(--sd-text-4);
  font-size: 13px;
  text-align: center;
}

.conv-empty svg {
  width: 38px;
  height: 38px;
  opacity: 0.5;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 12px;
  margin-bottom: 4px;
  border-radius: var(--sd-radius);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.conv-item:hover {
  background: #f6f9ff;
  border-color: var(--sd-border-soft);
}

.conv-item.active {
  background: var(--sd-primary-light);
  border-color: #bfdbfe;
}

.conv-avatar {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #93c5fd, #a5b4fc);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}

.conv-item.active .conv-avatar {
  background: var(--sd-gradient);
  box-shadow: var(--sd-shadow-primary);
}

.conv-text {
  flex: 1;
  min-width: 0;
}

.conv-line1 {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 3px;
}

.conv-name {
  font-size: 13.5px;
  font-weight: 650;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  flex-shrink: 0;
  font-size: 10.5px;
  color: var(--sd-text-4);
}

.conv-line2 {
  display: flex;
  align-items: center;
}

.conv-preview {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--sd-text-4);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* ===== 右侧聊天窗 ===== */
.chat-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius-lg);
  box-shadow: var(--sd-shadow-sm);
  overflow: hidden;
}

.no-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 30px;
  text-align: center;
}

.no-chat-icon {
  width: 78px;
  height: 78px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 26px;
  background: linear-gradient(135deg, #eff6ff, #eef2ff);
  color: var(--sd-primary);
  margin-bottom: 12px;
}

.no-chat-icon svg {
  width: 36px;
  height: 36px;
}

.no-chat-text {
  margin: 0;
  font-size: 15.5px;
  font-weight: 600;
  color: var(--sd-text-2);
}

.no-chat-sub {
  margin: 0;
  font-size: 12.5px;
  color: var(--sd-text-4);
}

.chat-active {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 13px 18px;
  border-bottom: 1px solid var(--sd-border-soft);
  background: linear-gradient(90deg, #fbfdff 0%, #ffffff 100%);
}

.mobile-back {
  display: none;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  border: 1px solid var(--sd-border-soft);
  background: #fff;
  color: var(--sd-text-3);
  cursor: pointer;
}

.mobile-back svg {
  width: 15px;
  height: 15px;
}

.chat-avatar {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--sd-gradient);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  box-shadow: var(--sd-shadow-primary);
}

.chat-head-text {
  flex: 1;
  min-width: 0;
}

.chat-name {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 3px 0 0;
  font-size: 11.5px;
  color: var(--sd-text-4);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot-on {
  background: var(--sd-success);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.16);
  animation: sd-pulse 2s ease-in-out infinite;
}

.dot-off {
  background: var(--sd-text-4);
}

.msg-count {
  flex-shrink: 0;
  font-size: 11.5px;
  color: var(--sd-text-4);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

/* ===== 消息列表 ===== */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background:
    radial-gradient(circle at 15% 10%, rgba(59, 130, 246, 0.045), transparent 40%),
    radial-gradient(circle at 85% 90%, rgba(99, 102, 241, 0.045), transparent 40%),
    #f8fafc;
}

.message-list::-webkit-scrollbar {
  width: 7px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #d7dfeb;
  border-radius: 999px;
}

.msg-empty {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--sd-text-4);
  font-size: 13px;
  text-align: center;
}

.msg-empty svg {
  width: 40px;
  height: 40px;
  opacity: 0.45;
}

.message {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  animation: msg-slide 0.28s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes msg-slide {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 700;
  background: #e2e8f0;
  color: var(--sd-text-2);
}

.message-self .message-avatar {
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.message-content {
  max-width: 62%;
  min-width: 0;
}

.message-sender {
  margin-bottom: 4px;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sd-text-4);
}

.message-self .message-sender {
  text-align: right;
}

.message-text {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-other .message-text {
  background: #fff;
  border: 1px solid var(--sd-border-soft);
  color: var(--sd-text-2);
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
}

.message-self .message-text {
  background: var(--sd-gradient);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: var(--sd-shadow-primary);
}

.message-time {
  margin-top: 4px;
  font-size: 10.5px;
  color: var(--sd-text-4);
  padding: 0 3px;
}

.message-self .message-time {
  text-align: right;
}

/* 商品消息卡片 */
.product-card {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 13px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #bfdbfe;
  cursor: pointer;
  transition: all 0.22s ease;
}

.product-card:hover {
  border-color: var(--sd-primary);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.18);
  transform: translateY(-2px);
}

.product-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--sd-primary-light);
  color: var(--sd-primary);
}

.product-icon svg {
  width: 17px;
  height: 17px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 13.5px;
  font-weight: 650;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-hint {
  margin-top: 2px;
  font-size: 11px;
  color: var(--sd-text-4);
}

.product-arrow {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
  color: var(--sd-text-4);
  transition: transform 0.22s ease;
}

.product-card:hover .product-arrow {
  color: var(--sd-primary);
  transform: translateX(3px);
}

/* ===== 输入区 ===== */
.message-input {
  display: flex;
  gap: 11px;
  align-items: flex-end;
  padding: 13px 18px 15px;
  border-top: 1px solid var(--sd-border-soft);
  background: #fff;
}

.chat-textarea {
  flex: 1;
  min-width: 0;
  padding: 10px 14px;
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  background: var(--sd-bg-input);
  font-size: 14px;
  line-height: 1.65;
  font-family: inherit;
  color: var(--sd-text);
  resize: none;
  outline: none;
  transition: all 0.22s ease;
}

.chat-textarea:focus {
  border-color: var(--sd-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.13);
}

.btn-send {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 42px;
  padding: 0 22px;
  flex-shrink: 0;
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

.btn-send:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(59, 130, 246, 0.36);
}

.btn-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-send svg {
  width: 16px;
  height: 16px;
}

/* ===== 快捷入口 ===== */
.quick-nav {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.nav-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--sd-border-soft);
  color: var(--sd-text-3);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.22s ease;
  box-shadow: var(--sd-shadow-sm);
}

.nav-pill:hover {
  color: var(--sd-primary-dark);
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  transform: translateY(-2px);
}

.nav-pill svg {
  width: 15px;
  height: 15px;
}

/* ===== 响应式 ===== */
@media (max-width: 860px) {
  .chat-shell {
    height: calc(100vh - 190px);
    min-height: 460px;
    position: relative;
  }

  .conv-panel {
    width: 100%;
  }

  .conv-panel.conv-hidden {
    display: none;
  }

  .chat-panel {
    display: none;
  }

  .chat-panel.panel-open {
    display: flex;
  }

  .mobile-back {
    display: flex;
  }

  .message-content {
    max-width: 76%;
  }
}
</style>
