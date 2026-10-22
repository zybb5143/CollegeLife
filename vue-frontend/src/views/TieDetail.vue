<template>
  <div class="sd-page detail-page">
    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      <span>加载中...</span>
    </div>

    <div v-else-if="!tie" class="sd-card sd-empty">
      <span class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </span>
      <span>该帖子不存在或已被删除</span>
      <router-link to="/allties" class="sd-btn sd-btn-primary">返回论坛广场</router-link>
    </div>

    <template v-else>
      <button class="back-bar" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        返回列表
      </button>

      <!-- 帖子主体 -->
      <article class="sd-card post-card">
        <h1 class="post-title">{{ tie.tieTitle }}</h1>

        <div class="post-info">
          <span class="post-avatar">{{ avatarText(tie.userName) }}</span>
          <div class="post-author-meta">
            <span class="post-author">{{ tie.userName }}</span>
            <span class="post-time">发表于 {{ formatTime(tie.tieCreateTime) }}</span>
          </div>
          <div class="post-actions">
            <button
              v-if="isLoggedIn && currentUser !== tie.userName"
              @click="goToChat(tie.userName)"
              class="btn-chat"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              对话
            </button>
            <button
              v-if="socialEnabled && isLoggedIn && currentUser !== tie.userName"
              @click="handleAddFriend(tie.userName)"
              :class="['btn-friend', isFriend(tie.userName) ? 'btn-friended' : '']"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"
                   stroke-linecap="round" stroke-linejoin="round">
                <polyline v-if="isFriend(tie.userName)" points="20 6 9 17 4 12"/>
                <template v-else>
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </template>
              </svg>
              {{ isFriend(tie.userName) ? '已关注' : '关注' }}
            </button>
            <button
              v-if="isLoggedIn && currentUser === tie.userName"
              @click="goToEdit"
              class="btn-edit"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 20h9"/>
                <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z"/>
              </svg>
              编辑
            </button>
          </div>
        </div>

        <div class="post-body">{{ tie.tieContent }}</div>

        <div v-if="tie.tieImage" class="tie-images-detail">
          <!-- 判断文件类型：图片预览，其他类型下载 -->
          <template v-if="isImageFile(tie.tieImage)">
            <img
              :src="getSmartPreviewUrl(tie.tieImage)"
              alt="图片"
              class="tie-image-full"
            />
          </template>
          <template v-else>
            <a
              :href="getDownloadUrl(tie.tieImage)"
              class="download-link"
              target="_blank"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="7 10 12 15 17 10"/>
                <line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              <span class="file-name">{{ getFileName(tie.tieImage) }}</span>
              <span class="download-hint">点击下载</span>
            </a>
          </template>
        </div>

        <!-- 帖子点赞 -->
        <div class="post-foot">
          <button v-if="socialEnabled" @click="handleLike(tie.tieId)" :disabled="likeLoading" class="like-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
            </svg>
            <span class="like-num">{{ tieLikes }}</span>
            <span>点赞</span>
          </button>
          <span class="reply-count">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            {{ replies.length }} 条讨论
          </span>
        </div>
      </article>

      <!-- 讨论区 -->
      <section class="sd-card reply-card">
        <h3 class="section-title">
          讨论区
          <span class="count-pill">{{ replies.length }}</span>
        </h3>

        <div v-if="isLoggedIn" class="reply-form">
          <textarea
            v-model="replyForm.replyContent"
            class="sd-textarea"
            placeholder="发表你的评论，友善交流更受欢迎..."
            rows="4"
            :disabled="submitting"
          ></textarea>
          <div class="reply-form-foot">
            <span class="reply-tip">{{ (replyForm.replyContent || '').trim().length }} 字</span>
            <button
              @click="handleReply"
              :disabled="submitting"
              class="sd-btn sd-btn-primary"
            >
              <span v-if="submitting" class="btn-spinner"></span>
              {{ submitting ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>

        <div v-else class="login-hint">
          <span class="login-hint-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </span>
          <p>登录后才能参与讨论</p>
          <button @click="goToLogin" class="sd-btn sd-btn-primary">去登录</button>
        </div>

        <div class="replies-list">
          <div
            v-for="(reply, index) in replies"
            :key="index"
            class="reply-item"
          >
            <span class="reply-avatar">{{ avatarText(reply.userName) }}</span>
            <div class="reply-main">
              <div class="reply-header">
                <span class="reply-user">{{ reply.userName }}</span>
                <button
                  v-if="isLoggedIn && currentUser !== reply.userName"
                  @click="goToChat(reply.userName)"
                  class="btn-chat-small"
                  title="私信 TA"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                       stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                </button>
                <button
                  v-if="socialEnabled && isLoggedIn && currentUser !== reply.userName"
                  @click="handleAddFriend(reply.userName)"
                  :class="['btn-friend-small', isFriend(reply.userName) ? 'btn-friended' : '']"
                  :title="isFriend(reply.userName) ? '已关注' : '关注'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6"
                       stroke-linecap="round" stroke-linejoin="round">
                    <polyline v-if="isFriend(reply.userName)" points="20 6 9 17 4 12"/>
                    <template v-else>
                      <line x1="12" y1="5" x2="12" y2="19"/>
                      <line x1="5" y1="12" x2="19" y2="12"/>
                    </template>
                  </svg>
                </button>
                <span class="reply-time">{{ formatTime(reply.replyCreateTime) }}</span>
              </div>
              <div class="reply-content">{{ reply.replyContent }}</div>
            </div>
          </div>

          <div v-if="replies.length === 0" class="no-replies">
            <span class="sd-empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </span>
            <span>暂无讨论，快来发表第一个评论吧！</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'TieDetail',
  data() {
    return {
      tie: null,
      replies: [],
      loading: false,
      isLoggedIn: false,
      currentUser: '',
      replyForm: {
        replyContent: ''
      },
      submitting: false,
      // 好友/点赞功能后端尚未实现，暂时隐藏入口与请求；后端补齐后置为 true 即可恢复
      socialEnabled: false,
      friendSet: new Set(), // 用Set存储好友，查询更快
      friendLoadFailed: false, // 标记好友列表是否加载失败
      // 点赞相关
      tieLikes: 0,
      likeLoading: false // 防止重复点击
    }
  },
  created() {
    this.checkLoginStatus()
    this.loadTieDetail()
    // 延迟加载好友列表，避免阻塞主流程
    this.$nextTick(() => {
      this.loadFriendList()
    })
  },
  methods: {
    async checkLoginStatus() {
      try {
        const response = await api.get('/user/current')
        if (response.success && response.data) {
          this.currentUser = response.data.userName
          this.isLoggedIn = true
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    async loadFriendList() {
      if (!this.socialEnabled) return
      try {
        const response = await api.get('/friend/list')
        if (response.success && response.data) {
          // 用Set存储，查询时间复杂度O(1)
          this.friendSet = new Set(response.data.map(f => f.friendName))
          this.friendLoadFailed = false
        }
      } catch (error) {
        console.error('加载好友列表失败:', error)
        this.friendLoadFailed = true
        // 兜底策略：加载失败不影响其他功能，只是不显示关注状态
      }
    },
    async batchCheckFriends() {
      if (!this.socialEnabled) return
      // 收集当前页面需要检查的用户名（作者 + 回复者）
      const userNamesToCheck = new Set()
      
      if (this.tie && this.tie.userName !== this.currentUser) {
        userNamesToCheck.add(this.tie.userName)
      }
      
      if (this.replies && this.replies.length > 0) {
        this.replies.forEach(reply => {
          if (reply.userName !== this.currentUser) {
            userNamesToCheck.add(reply.userName)
          }
        })
      }
      
      if (userNamesToCheck.size === 0) {
        return
      }
      
      try {
        const response = await api.post('/friend/batch-check', Array.from(userNamesToCheck))
        if (response.success && response.data) {
          // 将返回的好友添加到Set中
          response.data.forEach(name => this.friendSet.add(name))
        }
      } catch (error) {
        console.error('批量检查好友失败:', error)
        // 静默失败，不影响用户体验
      }
    },
    async loadTieDetail() {
      try {
        const tieId = this.$route.query.tieId
        if (!tieId) {
          this.$router.push('/allties')
          return
        }
        
        this.loading = true
        const [tieResponse, repliesResponse] = await Promise.all([
          api.get('/tie/detail', { params: { tieId } }),
          api.get('/reply/list', { params: { tieId } })
        ])
        
        this.tie = tieResponse
        this.replies = repliesResponse || []
        this.loading = false
        
        // 加载完帖子后，批量检查这些用户是否为好友
        if (this.isLoggedIn && !this.friendLoadFailed) {
          await this.batchCheckFriends()
        }
        
        // 加载帖子点赞数
        await this.loadLikeCounts()
      } catch (error) {
        console.error('加载帖子详情失败:', error)
        this.loading = false
        this.tie = null
      }
    },
    async handleReply() {
      try {
        if (!this.isLoggedIn || !this.currentUser) {
          alert('请先登录后再发表回复')
          this.$router.push('/login')
          return
        }
        
        const tieId = this.$route.query.tieId
        if (!tieId) {
          alert('帖子信息有误，请刷新页面')
          return
        }
        
        if (!this.replyForm.replyContent || !this.replyForm.replyContent.trim()) {
          alert('回复内容不能为空')
          return
        }
        
        this.submitting = true
        
        await api.post('/reply/create', {
          replyContent: this.replyForm.replyContent.trim(),
          userName: this.currentUser,
          tieId: tieId
        }, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        
        this.replyForm.replyContent = ''
        alert('回复成功！')
        await this.loadTieDetail()
        this.submitting = false
      } catch (error) {
        console.error('回复失败:', error)
        this.submitting = false
        const errorMsg = error.message || '请稍后重试'
        alert('回复失败：' + errorMsg)
      }
    },
    goBack() {
      this.$router.back()
    },
    goToEdit() {
      this.$router.push({
        path: '/edittie',
        query: { tieId: this.tie.tieId }
      })
    },
    goToChat(userName) {
      if (!this.isLoggedIn) {
        alert('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push({
        path: '/chat',
        query: { targetUser: userName }
      })
    },
    goToLogin() {
      this.$router.push('/login')
    },
    avatarText(name) {
      const text = (name || '').trim()
      if (!text) return '匿'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    formatTime(timeStr) {
      if (!timeStr) return '未知时间'
      // 如果是标准格式直接返回
      if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/.test(timeStr)) {
        return timeStr
      }
      // 尝试解析其他格式
      try {
        const date = new Date(timeStr)
        if (!isNaN(date.getTime())) {
          return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          })
        }
      } catch (e) {}
      return '无效时间'
    },
    isImageFile(path) {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)
    },
    getFileName(path) {
      if (!path) return ''
      const parts = path.split('/')
      return parts[parts.length - 1]
    },
    getSmartPreviewUrl(path) {
      if (!path) return ''
      const fileName = this.getFileName(path)
      // 走 Nginx 代理，使用相对路径；后端只有 /preview/{filename} 端点，没有 /smart/
      return `/api/download/preview/${fileName}`
    },
    getDownloadUrl(path) {
      if (!path) return ''
      const fileName = this.getFileName(path)
      return `/api/download/download/${fileName}`
    },
    isFriend(userName) {
      // 如果好友列表加载失败，默认返回false，不显示关注状态
      if (this.friendLoadFailed) {
        return false
      }
      return this.friendSet.has(userName)
    },
    async handleAddFriend(friendName) {
      if (this.isFriend(friendName)) {
        return
      }
      
      try {
        const response = await api.post('/friend/add', null, {
          params: { friendName }
        })
        
        if (response.success) {
          this.friendSet.add(friendName) // 添加到Set
          this.friendLoadFailed = false // 重置失败标记
          alert('关注成功！')
        } else {
          alert(response.message || '关注失败')
        }
      } catch (error) {
        console.error('关注失败:', error)
        alert('关注失败：' + (error.response?.data?.message || error.message))
      }
    },
    async loadLikeCounts() {
      if (!this.socialEnabled) return
      try {
        const response = await api.get('/like/counts', {
          params: { tieId: this.tie.tieId }
        })
        if (response.success) {
          this.tieLikes = response.data || 0
        }
      } catch (error) {
        console.error('加载点赞数失败:', error)
      }
    },
    async handleLike(tieId) {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      if (this.likeLoading) {
        return
      }
      
      this.likeLoading = true
      
      try {
        const response = await api.post('/like/action', null, {
          params: { tieId }
        })
        
        if (response.success) {
          this.tieLikes = response.data || 0
        } else {
          alert(response.message || '操作失败')
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
        alert('操作失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.likeLoading = false
      }
    }
  }
}
</script>

<style scoped>
.detail-page {
  max-width: 880px;
}

/* ===== 返回条 ===== */
.back-bar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 15px 7px 11px;
  margin-bottom: 14px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--sd-border-soft);
  border-radius: 999px;
  color: var(--sd-text-3);
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-bar:hover {
  color: var(--sd-primary-dark);
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  transform: translateX(-2px);
}

.back-bar svg {
  width: 15px;
  height: 15px;
}

/* ===== 帖子主体 ===== */
.post-card {
  padding: 28px 30px;
  margin-bottom: 18px;
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.45;
  letter-spacing: 0.3px;
  color: var(--sd-text);
}

.post-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 18px 0 22px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--sd-border-soft);
}

.post-avatar {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  box-shadow: 0 3px 10px rgba(99, 102, 241, 0.32);
}

.post-author-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.post-author {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-time {
  margin-top: 3px;
  font-size: 12px;
  color: var(--sd-text-4);
}

.post-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.btn-friend,
.btn-edit {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border: none;
  border-radius: 999px;
  font-size: 12.5px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-friend svg,
.btn-edit svg {
  width: 13px;
  height: 13px;
}

.btn-friend {
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.btn-friend:hover {
  filter: brightness(1.06);
  transform: translateY(-1px);
}

.btn-friend.btn-friended {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
  box-shadow: none;
  cursor: default;
}

.btn-friend.btn-friended:hover {
  filter: none;
  transform: none;
}

.btn-edit {
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #b45309;
}

.btn-edit:hover {
  background: #fef3c7;
  transform: translateY(-1px);
}

.btn-chat {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 12.5px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-chat svg {
  width: 13px;
  height: 13px;
}

.btn-chat:hover {
  background: var(--sd-primary);
  border-color: var(--sd-primary);
  color: #fff;
  transform: translateY(-1px);
}

.post-body {
  padding: 18px 20px;
  background: #f8fafc;
  border-left: 3px solid var(--sd-primary);
  border-radius: var(--sd-radius);
  color: var(--sd-text-2);
  font-size: 15px;
  line-height: 1.85;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  word-break: break-word;
  min-height: 96px;
}

.tie-images-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.tie-image-full {
  max-width: 100%;
  max-height: 460px;
  object-fit: contain;
  border-radius: var(--sd-radius);
  border: 1px solid var(--sd-border);
  box-shadow: var(--sd-shadow-sm);
}

.download-link {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 11px 20px;
  background: var(--sd-gradient);
  color: #fff;
  border-radius: 999px;
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 500;
  box-shadow: var(--sd-shadow-primary);
  transition: all 0.2s ease;
}

.download-link:hover {
  filter: brightness(1.06);
  transform: translateY(-1px);
}

.download-link svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.file-name {
  font-weight: 600;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.download-hint {
  font-size: 12px;
  opacity: 0.82;
}

.post-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--sd-border-soft);
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 18px;
  border: 1px solid var(--sd-border);
  border-radius: 999px;
  background: #fff;
  color: var(--sd-text-3);
  font-size: 13.5px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.like-btn svg {
  width: 16px;
  height: 16px;
  transition: transform 0.2s ease;
}

.like-btn:hover:not(:disabled) {
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  transform: translateY(-1px);
}

.like-btn:hover:not(:disabled) svg {
  transform: scale(1.14);
}

.like-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.like-num {
  font-variant-numeric: tabular-nums;
}

.reply-count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--sd-text-4);
}

.reply-count svg {
  width: 15px;
  height: 15px;
}

/* ===== 讨论区 ===== */
.reply-card {
  padding: 26px 30px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 17px;
  font-weight: 700;
  color: var(--sd-text);
}

.section-title::before {
  content: '';
  width: 4px;
  height: 17px;
  border-radius: 999px;
  background: var(--sd-gradient);
}

.count-pill {
  padding: 2px 9px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.reply-form {
  margin-bottom: 24px;
}

.reply-form .sd-textarea {
  border-radius: var(--sd-radius);
  background: #f8fafc;
}

.reply-form-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.reply-tip {
  font-size: 12px;
  color: var(--sd-text-4);
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

.login-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 30px 24px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #eff6ff, #eef2ff);
  border: 1px solid #dbeafe;
  border-radius: var(--sd-radius-lg);
  text-align: center;
}

.login-hint-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: var(--sd-primary);
  box-shadow: var(--sd-shadow-sm);
}

.login-hint-icon svg {
  width: 20px;
  height: 20px;
}

.login-hint p {
  color: var(--sd-text-2);
  font-size: 14px;
  font-weight: 500;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 16px 18px;
  background: #fbfcfe;
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  animation: reply-in 0.3s ease-out both;
}

.reply-item:hover {
  border-color: #dbeafe;
  box-shadow: var(--sd-shadow-sm);
  transform: translateX(2px);
}

@keyframes reply-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.reply-avatar {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #93c5fd, #a5b4fc);
  color: #fff;
  font-size: 13.5px;
  font-weight: 600;
}

.reply-main {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 7px;
}

.reply-user {
  color: var(--sd-primary-dark);
  font-size: 13.5px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-friend-small {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: var(--sd-primary-light);
  border: 1px solid #dbeafe;
  border-radius: 50%;
  color: var(--sd-primary-dark);
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-friend-small svg {
  width: 12px;
  height: 12px;
}

.btn-friend-small:hover {
  background: var(--sd-primary);
  border-color: var(--sd-primary);
  color: #fff;
  transform: scale(1.08);
}

.btn-chat-small {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: var(--sd-primary-light);
  border: 1px solid #dbeafe;
  border-radius: 50%;
  color: var(--sd-primary-dark);
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-chat-small svg {
  width: 12px;
  height: 12px;
}

.btn-chat-small:hover {
  background: var(--sd-primary);
  border-color: var(--sd-primary);
  color: #fff;
  transform: scale(1.08);
}

.btn-friend-small.btn-friended {
  background: #ecfdf5;
  border-color: #a7f3d0;
  color: #059669;
  cursor: default;
}

.btn-friend-small.btn-friended:hover {
  background: #ecfdf5;
  border-color: #a7f3d0;
  color: #059669;
  transform: none;
}

.reply-time {
  margin-left: auto;
  color: var(--sd-text-4);
  font-size: 11.5px;
  white-space: nowrap;
}

.reply-content {
  color: var(--sd-text-2);
  line-height: 1.75;
  font-size: 14px;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  word-break: break-word;
}

.no-replies {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--sd-text-4);
  font-size: 14px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .post-card,
  .reply-card {
    padding: 20px 18px;
  }

  .post-title {
    font-size: 20px;
  }

  .post-info {
    flex-wrap: wrap;
  }

  .post-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .post-foot {
    flex-direction: column;
    align-items: flex-start;
  }

  .file-name {
    max-width: 160px;
  }
}
</style>
