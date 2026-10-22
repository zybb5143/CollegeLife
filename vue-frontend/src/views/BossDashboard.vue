<template>
  <div class="sd-page dashboard-page">
    <!-- 管理员欢迎卡 -->
    <div class="admin-hero">
      <div class="hero-glow hero-glow-1"></div>
      <div class="hero-glow hero-glow-2"></div>
      <div class="hero-grid"></div>

      <div class="hero-content">
        <div class="hero-left">
          <span class="hero-avatar">{{ avatarText }}</span>
          <div class="hero-text">
            <p class="hero-welcome">欢迎回来，{{ currentUser?.userName }}</p>
            <p class="hero-desc">这里是站点管理中心，可进行用户、文件与 AI 模型管理</p>
          </div>
        </div>
        <span class="role-badge" :class="currentUser?.role === 'boss' ? 'role-boss' : 'role-admin'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          {{ currentUser?.role === 'boss' ? '超级管理员' : '管理员' }}
        </span>
      </div>

      <div class="hero-stats">
        <div class="hstat">
          <span class="hstat-num">{{ allUsers.length }}</span>
          <span class="hstat-label">注册用户</span>
        </div>
        <span class="hstat-divider"></span>
        <div class="hstat">
          <span class="hstat-num">{{ staffCount }}</span>
          <span class="hstat-label">管理账号</span>
        </div>
        <span class="hstat-divider"></span>
        <div class="hstat">
          <span class="hstat-num">{{ currentAIProvider ? aiProviderName : '—' }}</span>
          <span class="hstat-label">当前 AI 模型</span>
        </div>
      </div>
    </div>

    <!-- 管理操作 -->
    <div class="sd-card panel">
      <h3 class="section-title">
        <span class="title-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.6 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.6a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </span>
        管理操作
      </h3>

      <div class="action-grid">
        <button class="action-card act-danger" @click="showDeleteUserDialog = true">
          <span class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="18" y1="8" x2="23" y2="13"/>
              <line x1="23" y1="8" x2="18" y2="13"/>
            </svg>
          </span>
          <span class="action-text">
            <span class="action-name">移除用户</span>
            <span class="action-desc">删除指定普通用户账号</span>
          </span>
        </button>

        <button class="action-card act-blue" @click="showDeleteAdminDialog = true">
          <span class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              <line x1="9" y1="10" x2="15" y2="16"/>
              <line x1="15" y1="10" x2="9" y2="16"/>
            </svg>
          </span>
          <span class="action-text">
            <span class="action-name">移除管理员</span>
            <span class="action-desc">撤销管理员权限账号</span>
          </span>
        </button>

        <router-link to="/admin/files" class="action-card act-green">
          <span class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </span>
          <span class="action-text">
            <span class="action-name">文件管理</span>
            <span class="action-desc">管理全站上传的文件</span>
          </span>
        </router-link>

        <button class="action-card act-purple" @click="showSwitchAIDialog = true; fetchAIProvider()">
          <span class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="8" width="18" height="12" rx="3"/>
              <path d="M12 8V4"/>
              <circle cx="12" cy="3" r="1.4"/>
              <circle cx="8.5" cy="14" r="1.2"/>
              <circle cx="15.5" cy="14" r="1.2"/>
            </svg>
          </span>
          <span class="action-text">
            <span class="action-name">切换 AI 模型</span>
            <span class="action-desc">DeepSeek / 豆包 之间切换</span>
          </span>
        </button>

        <button class="action-card act-amber" @click="loadAllUsers">
          <span class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </span>
          <span class="action-text">
            <span class="action-name">刷新用户列表</span>
            <span class="action-desc">重新拉取全部注册用户</span>
          </span>
        </button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div v-if="allUsers.length > 0" class="sd-card panel">
      <div class="panel-head">
        <h3 class="section-title">
          <span class="title-icon icon-indigo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </span>
          用户列表
          <span class="count-pill">{{ filteredUsers.length }}</span>
        </h3>
        <div class="sd-input-wrap search-wrap">
          <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input class="sd-input" v-model="userKeyword" placeholder="搜索用户名..." />
        </div>
      </div>

      <div v-if="filteredUsers.length === 0" class="sd-empty small-empty">
        <p>没有匹配的用户</p>
      </div>

      <div v-else class="user-list">
        <div v-for="(user, index) in filteredUsers" :key="index" class="user-item">
          <span class="user-avatar" :class="getRoleBadge(user)">{{ userInitial(user) }}</span>
          <span class="user-name">{{ user }}</span>
          <span :class="['tag', getRoleBadge(user)]">{{ getRoleText(user) }}</span>
          <button
            v-if="user !== currentUser?.userName"
            class="btn-remove-small"
            @click="confirmDeleteUser(user)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
            移除
          </button>
          <span v-else class="self-tag">当前账号</span>
        </div>
      </div>
    </div>

    <!-- 移除用户对话框 -->
    <transition name="dlg-fade">
      <div v-if="showDeleteUserDialog" class="dialog-overlay" @click="showDeleteUserDialog = false">
        <div class="dialog" @click.stop>
          <span class="dialog-icon icon-danger">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="18" y1="8" x2="23" y2="13"/>
              <line x1="23" y1="8" x2="18" y2="13"/>
            </svg>
          </span>
          <h3>移除用户</h3>
          <p class="dialog-desc">该操作不可撤销，请确认用户名无误</p>
          <input
            v-model="deleteUserName"
            type="text"
            placeholder="请输入用户名"
            class="dialog-input"
            @keyup.enter="handleDeleteUser"
          />
          <div class="dialog-actions">
            <button class="btn-cancel" @click="showDeleteUserDialog = false">取消</button>
            <button class="btn-confirm btn-confirm-danger" @click="handleDeleteUser">确认移除</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 移除管理员对话框 -->
    <transition name="dlg-fade">
      <div v-if="showDeleteAdminDialog" class="dialog-overlay" @click="showDeleteAdminDialog = false">
        <div class="dialog" @click.stop>
          <span class="dialog-icon icon-blue">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              <line x1="9" y1="10" x2="15" y2="16"/>
              <line x1="15" y1="10" x2="9" y2="16"/>
            </svg>
          </span>
          <h3>移除管理员</h3>
          <p class="dialog-desc">移除后该账号将失去管理权限</p>
          <input
            v-model="deleteAdminName"
            type="text"
            placeholder="请输入管理员用户名"
            class="dialog-input"
            @keyup.enter="handleDeleteAdmin"
          />
          <div class="dialog-actions">
            <button class="btn-cancel" @click="showDeleteAdminDialog = false">取消</button>
            <button class="btn-confirm" @click="handleDeleteAdmin">确认移除</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 切换AI对话框 -->
    <transition name="dlg-fade">
      <div v-if="showSwitchAIDialog" class="dialog-overlay" @click="showSwitchAIDialog = false">
        <div class="dialog" @click.stop>
          <span class="dialog-icon icon-purple">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="8" width="18" height="12" rx="3"/>
              <path d="M12 8V4"/>
              <circle cx="12" cy="3" r="1.4"/>
              <circle cx="8.5" cy="14" r="1.2"/>
              <circle cx="15.5" cy="14" r="1.2"/>
            </svg>
          </span>
          <h3>切换 AI 模型</h3>
          <p class="dialog-desc">
            当前：<strong>{{ aiProviderName }}</strong>
          </p>
          <div class="provider-options">
            <button
              :class="['provider-btn', selectedProvider === 'deepseek' ? 'active' : '']"
              @click="selectedProvider = 'deepseek'"
              :disabled="switchingAI"
            >
              <span class="provider-name">DeepSeek</span>
              <span class="provider-tag">深度求索</span>
            </button>
            <button
              :class="['provider-btn', selectedProvider === 'doubao' ? 'active' : '']"
              @click="selectedProvider = 'doubao'"
              :disabled="switchingAI"
            >
              <span class="provider-name">豆包</span>
              <span class="provider-tag">字节跳动</span>
            </button>
          </div>
          <div class="dialog-actions">
            <button class="btn-cancel" @click="showSwitchAIDialog = false">取消</button>
            <button
              class="btn-confirm btn-confirm-purple"
              @click="handleSwitchAI"
              :disabled="switchingAI || selectedProvider === currentAIProvider"
            >
              <span v-if="switchingAI" class="dlg-spinner"></span>
              {{ switchingAI ? '切换中...' : '确认切换' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'BossDashboard',
  data() {
    return {
      currentUser: null, showDeleteUserDialog: false, deleteUserName: '',
      showDeleteAdminDialog: false, deleteAdminName: '', allUsers: [],
      showSwitchAIDialog: false, currentAIProvider: '', selectedProvider: '', switchingAI: false,
      userKeyword: ''
    }
  },
  computed: {
    avatarText() {
      const text = (this.currentUser?.userName || '').trim()
      if (!text) return 'A'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    staffCount() {
      return this.allUsers.filter(u => u === 'boss' || u === 'admin').length
    },
    aiProviderName() {
      if (this.currentAIProvider === 'deepseek') return 'DeepSeek'
      if (this.currentAIProvider === 'doubao') return '豆包'
      return this.currentAIProvider || '未知'
    },
    filteredUsers() {
      const key = this.userKeyword.trim().toLowerCase()
      if (!key) return this.allUsers
      return this.allUsers.filter(u => String(u).toLowerCase().includes(key))
    }
  },
  created() { this.loadUserInfo() },
  methods: {
    async loadUserInfo() {
      try {
        const response = await api.get('/user/current')
        if (response.success && response.data) {
          this.currentUser = response.data
          if (this.currentUser.role !== 'boss' && this.currentUser.role !== 'admin') {
            this.$message.warning('无权访问')
            setTimeout(() => this.$router.push('/allties'), 1500)
          }
          this.loadAllUsers()
          this.fetchAIProvider()
        } else {
          this.$message.warning('请先登录')
          setTimeout(() => this.$router.push('/login'), 1500)
        }
      } catch (error) {
        this.$message.warning('请先登录')
        setTimeout(() => this.$router.push('/login'), 1500)
      }
    },
    async handleDeleteUser() {
      if (!this.deleteUserName.trim()) { this.$message.warning('请输入用户名'); return }
      try {
        const r = await api.delete(`/admin/user/${this.deleteUserName}`)
        if (r.success) {
          this.$message.success('删除成功')
          this.showDeleteUserDialog = false
          this.deleteUserName = ''
          this.loadAllUsers()
        } else this.$message.error(r.message || '删除失败')
      } catch (e) {
        if (e?.status === 403) this.$message.error('权限不足：仅超级管理员（boss）可删除用户')
        else this.$message.error(e?.message || '删除失败')
      }
    },
    async handleDeleteAdmin() {
      if (!this.deleteAdminName.trim()) { this.$message.warning('请输入管理员用户名'); return }
      try {
        const r = await api.delete(`/admin/user/${this.deleteAdminName}`)
        if (r.success) {
          this.$message.success('删除成功')
          this.showDeleteAdminDialog = false
          this.deleteAdminName = ''
          this.loadAllUsers()
        } else this.$message.error(r.message || '删除失败')
      } catch (e) {
        if (e?.status === 403) this.$message.error('权限不足：仅超级管理员（boss）可删除用户')
        else this.$message.error(e?.message || '删除失败')
      }
    },
    async loadAllUsers() {
      try {
        const r = await api.get('/admin/dashboard')
        if (r.success && r.users) this.allUsers = r.users
      } catch (e) { console.error('加载失败', e) }
    },
    getRoleBadge(userName) {
      if (userName === 'boss') return 'role-boss'
      if (userName === 'admin') return 'role-admin'
      return 'role-user'
    },
    getRoleText(userName) {
      if (userName === 'boss') return '超级管理员'
      if (userName === 'admin') return '管理员'
      return '用户'
    },
    userInitial(userName) {
      const text = String(userName || '').trim()
      if (!text) return '?'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    confirmDeleteUser(userName) { this.deleteUserName = userName; this.showDeleteUserDialog = true },
    async fetchAIProvider() {
      try {
        const r = await api.get('/ai-chat/provider')
        if (r.provider) { this.currentAIProvider = r.provider; this.selectedProvider = r.provider }
      } catch (e) {}
    },
    async handleSwitchAI() {
      if (!this.selectedProvider || this.selectedProvider === this.currentAIProvider) return
      this.switchingAI = true
      try {
        const r = await api.post('/ai-chat/switch-provider', null, { params: { provider: this.selectedProvider } })
        if (r.success) { this.currentAIProvider = this.selectedProvider; this.$message.success(r.message || '切换成功'); this.showSwitchAIDialog = false }
        else this.$message.error(r.message || '切换失败')
      } catch (e) { this.$message.error('切换失败') }
      finally { this.switchingAI = false }
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  max-width: 940px;
}

/* ===== 管理员主卡 ===== */
.admin-hero {
  position: relative;
  padding: 26px 28px 0;
  margin-bottom: 18px;
  border-radius: var(--sd-radius-xl);
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 45%, #312e81 100%);
  color: #fff;
  overflow: hidden;
  box-shadow: 0 20px 44px rgba(15, 23, 42, 0.34);
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(54px);
  pointer-events: none;
}

.hero-glow-1 {
  width: 280px;
  height: 280px;
  top: -140px;
  right: -50px;
  background: rgba(99, 102, 241, 0.5);
}

.hero-glow-2 {
  width: 220px;
  height: 220px;
  bottom: -120px;
  left: 6%;
  background: rgba(59, 130, 246, 0.34);
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 34px 34px;
  pointer-events: none;
  mask-image: radial-gradient(ellipse at 50% 0%, #000 30%, transparent 78%);
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.hero-avatar {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 17px;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 21px;
  font-weight: 800;
  box-shadow: 0 8px 20px rgba(96, 165, 250, 0.4);
}

.hero-welcome {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.2px;
}

.hero-desc {
  margin: 5px 0 0;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.62);
  line-height: 1.6;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 15px;
  border-radius: 999px;
  font-size: 12.5px;
  font-weight: 700;
  flex-shrink: 0;
}

.role-badge svg {
  width: 14px;
  height: 14px;
}

.role-boss {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #78350f;
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.34);
}

.role-admin {
  background: rgba(96, 165, 250, 0.2);
  color: #bfdbfe;
  border: 1px solid rgba(96, 165, 250, 0.4);
}

.hero-stats {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 10px;
  background: rgba(255, 255, 255, 0.07);
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.hstat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  flex: 1;
  min-width: 0;
}

.hstat-num {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.hstat-label {
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.58);
}

.hstat-divider {
  width: 1px;
  height: 30px;
  background: rgba(255, 255, 255, 0.14);
  flex-shrink: 0;
}

/* ===== 面板通用 ===== */
.panel {
  padding: 22px 24px 24px;
  margin-bottom: 18px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0 0 18px;
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.panel-head .section-title {
  margin-bottom: 0;
}

.title-icon {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: var(--sd-primary-light);
  color: var(--sd-primary);
}

.title-icon.icon-indigo {
  background: #eef2ff;
  color: var(--sd-accent);
}

.title-icon svg {
  width: 16px;
  height: 16px;
}

.count-pill {
  padding: 1px 9px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 12px;
  font-weight: 700;
}

.search-wrap {
  width: 220px;
}

.search-wrap .sd-input {
  height: 38px;
}

/* ===== 操作卡片 ===== */
.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 12px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 16px;
  border-radius: var(--sd-radius);
  border: 1px solid var(--sd-border-soft);
  background: #fcfdff;
  font-family: inherit;
  text-align: left;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.25s ease;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--sd-shadow-md);
}

.action-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.action-icon svg {
  width: 19px;
  height: 19px;
}

.action-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.action-name {
  font-size: 14px;
  font-weight: 650;
  color: var(--sd-text);
}

.action-desc {
  font-size: 11.5px;
  color: var(--sd-text-4);
  line-height: 1.4;
}

.act-danger:hover { border-color: #fecaca; background: #fff8f8; }
.act-danger .action-icon { background: #fef2f2; color: var(--sd-danger); }

.act-blue:hover { border-color: #bfdbfe; background: #f8fbff; }
.act-blue .action-icon { background: var(--sd-primary-light); color: var(--sd-primary); }

.act-green:hover { border-color: #a7f3d0; background: #f6fdfa; }
.act-green .action-icon { background: #ecfdf5; color: #059669; }

.act-purple:hover { border-color: #c7d2fe; background: #fafaff; }
.act-purple .action-icon { background: #eef2ff; color: var(--sd-accent); }

.act-amber:hover { border-color: #fde68a; background: #fffdf6; }
.act-amber .action-icon { background: #fffbeb; color: #d97706; }

/* ===== 用户列表 ===== */
.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 9px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 13px;
  border-radius: var(--sd-radius);
  border: 1px solid var(--sd-border-soft);
  background: #fcfdff;
  transition: all 0.2s ease;
}

.user-item:hover {
  border-color: #dbe6f5;
  background: #fff;
  box-shadow: var(--sd-shadow-sm);
}

.user-avatar {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 12.5px;
  font-weight: 700;
  background: #e2e8f0;
  color: var(--sd-text-2);
}

.user-avatar.role-boss {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #78350f;
}

.user-avatar.role-admin {
  background: var(--sd-gradient);
  color: #fff;
}

.user-name {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag {
  flex-shrink: 0;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.tag.role-boss {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.tag.role-admin {
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  border: 1px solid #bfdbfe;
}

.tag.role-user {
  background: #f8fafc;
  color: var(--sd-text-3);
  border: 1px solid var(--sd-border);
}

.btn-remove-small {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  padding: 5px 11px;
  border-radius: 999px;
  border: 1px solid #fecaca;
  background: #fff;
  color: var(--sd-danger);
  font-size: 11.5px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-remove-small:hover {
  background: var(--sd-danger);
  border-color: var(--sd-danger);
  color: #fff;
}

.btn-remove-small svg {
  width: 12px;
  height: 12px;
}

.self-tag {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--sd-text-4);
}

.small-empty {
  padding: 24px 0 8px;
}

/* ===== 对话框 ===== */
.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
}

.dialog {
  width: 100%;
  max-width: 400px;
  padding: 28px 26px 22px;
  border-radius: var(--sd-radius-xl);
  background: #fff;
  box-shadow: 0 28px 60px rgba(15, 23, 42, 0.3);
  text-align: center;
}

.dialog-icon {
  width: 54px;
  height: 54px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
}

.dialog-icon svg {
  width: 25px;
  height: 25px;
}

.icon-danger { background: #fef2f2; color: var(--sd-danger); }
.icon-blue { background: var(--sd-primary-light); color: var(--sd-primary); }
.icon-purple { background: #eef2ff; color: var(--sd-accent); }

.dialog h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--sd-text);
}

.dialog-desc {
  margin: 7px 0 18px;
  font-size: 12.5px;
  color: var(--sd-text-4);
  line-height: 1.6;
}

.dialog-desc strong {
  color: var(--sd-primary-dark);
  font-weight: 700;
}

.dialog-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  background: var(--sd-bg-input);
  font-size: 14px;
  font-family: inherit;
  color: var(--sd-text);
  outline: none;
  transition: all 0.2s ease;
}

.dialog-input:focus {
  border-color: var(--sd-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.13);
}

.dialog-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  border: 1px solid var(--sd-border);
  background: #fff;
  color: var(--sd-text-3);
}

.btn-cancel:hover {
  background: var(--sd-bg-input);
  color: var(--sd-text-2);
}

.btn-confirm {
  border: none;
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.btn-confirm:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(59, 130, 246, 0.36);
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-confirm-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.28);
}

.btn-confirm-danger:hover:not(:disabled) {
  box-shadow: 0 10px 22px rgba(239, 68, 68, 0.36);
}

.btn-confirm-purple {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.28);
}

.btn-confirm-purple:hover:not(:disabled) {
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.36);
}

.dlg-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

/* AI 模型选择 */
.provider-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 11px;
}

.provider-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 15px 12px;
  border-radius: var(--sd-radius);
  border: 2px solid var(--sd-border);
  background: #fff;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.22s ease;
}

.provider-btn:hover:not(:disabled) {
  border-color: #c7d2fe;
  background: #fbfbff;
}

.provider-btn.active {
  border-color: var(--sd-accent);
  background: #f5f3ff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.14);
}

.provider-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.provider-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--sd-text);
}

.provider-tag {
  font-size: 11.5px;
  color: var(--sd-text-4);
}

/* 对话框动画 */
.dlg-fade-enter-active {
  transition: opacity 0.24s ease;
}

.dlg-fade-enter-active .dialog {
  transition: all 0.3s cubic-bezier(0.22, 1.3, 0.36, 1);
}

.dlg-fade-leave-active {
  transition: opacity 0.18s ease;
}

.dlg-fade-enter-from,
.dlg-fade-leave-to {
  opacity: 0;
}

.dlg-fade-enter-from .dialog {
  opacity: 0;
  transform: translateY(-18px) scale(0.95);
}

/* ===== 响应式 ===== */
@media (max-width: 700px) {
  .admin-hero {
    padding: 22px 18px 0;
  }

  .hero-welcome {
    font-size: 17px;
  }

  .hero-desc {
    display: none;
  }

  .hero-stats {
    gap: 6px;
  }

  .hstat-num {
    font-size: 16px;
  }

  .search-wrap {
    width: 100%;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .user-list {
    grid-template-columns: 1fr;
  }
}
</style>
