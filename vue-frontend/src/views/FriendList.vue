<template>
  <div class="sd-page friend-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">我的好友</h1>
        <p class="sd-subtitle">已关注 {{ friends.length }} 位同学</p>
      </div>
      <router-link to="/allties" class="sd-btn">
        <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        返回首页
      </router-link>
    </div>

    <!-- 添加好友 -->
    <div class="sd-card add-card">
      <div class="sd-input-wrap add-wrap">
        <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        <input type="text" v-model="newFriendName" placeholder="输入好友用户名，回车或点击添加"
               class="sd-input add-input" @keyup.enter="handleAddFriend" />
      </div>
      <button @click="handleAddFriend" class="sd-btn sd-btn-primary add-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        添加好友
      </button>
    </div>

    <transition name="msg-fade">
      <div v-if="message" class="sd-alert friend-msg" :class="msgClass">{{ message }}</div>
    </transition>

    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      <span>加载中...</span>
    </div>

    <div v-else-if="friends.length === 0" class="sd-card sd-empty">
      <span class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
      </span>
      <span>还没有关注任何人，去论坛广场看看吧</span>
    </div>

    <div v-else class="friend-list">
      <div v-for="friend in friends" :key="friend.id" class="friend-item">
        <span class="friend-avatar">{{ avatarText(friend.friendName) }}</span>
        <div class="friend-info">
          <span class="friend-name">{{ friend.friendName }}</span>
          <span class="friend-time">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            关注于 {{ formatTime(friend.createTime) }}
          </span>
        </div>
        <button @click="handleRemoveFriend(friend.friendName)" class="btn-remove">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="8.5" cy="7" r="4"/>
            <line x1="18" y1="8" x2="23" y2="13"/>
            <line x1="23" y1="8" x2="18" y2="13"/>
          </svg>
          取消关注
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'FriendList',
  data() { return { friends:[], newFriendName:'', loading:false, message:'', messageType:'' } },
  computed: {
    msgClass() {
      return this.messageType === 'success' ? 'sd-alert-success' : 'sd-alert-error'
    }
  },
  created() { this.loadFriends() },
  methods: {
    async loadFriends() {
      this.loading = true
      try { const r = await api.get('/friend/list'); if(r.success) this.friends = r.data||[] } catch(e) {}
      finally { this.loading = false }
    },
    async handleAddFriend() {
      if (!this.newFriendName.trim()) { this.showMsg('请输入好友用户名','error'); return }
      try {
        const r = await api.post('/friend/add', null, { params: { friendName: this.newFriendName.trim() } })
        if(r.success){ this.showMsg('添加成功','success'); this.newFriendName=''; this.loadFriends() }
        else this.showMsg(r.message||'添加失败','error')
      } catch(e) { this.showMsg('添加失败','error') }
    },
    async handleRemoveFriend(name) {
      try {
        await this.$confirm(`确定取消关注 ${name} 吗？`, '取消关注', {
          confirmButtonText: '确定取消',
          cancelButtonText: '再想想',
          type: 'warning'
        })
      } catch (e) {
        return // 用户放弃操作
      }
      try {
        const r = await api.post('/friend/remove', null, { params: { friendName: name } })
        if(r.success){ this.showMsg('取消成功','success'); this.loadFriends() }
        else this.showMsg(r.message||'操作失败','error')
      } catch(e) { this.showMsg('操作失败','error') }
    },
    avatarText(name) {
      const text = (name || '').trim()
      if (!text) return '友'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    showMsg(msg, type) { this.message = msg; this.messageType = type; setTimeout(() => this.message = '', 3000) },
    formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '' }
  }
}
</script>

<style scoped>
.friend-page {
  max-width: 760px;
}

.btn-icon {
  width: 15px;
  height: 15px;
}

/* ===== 添加好友 ===== */
.add-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  margin-bottom: 16px;
}

.add-wrap {
  flex: 1;
  min-width: 0;
}

.add-input {
  border-radius: 999px;
  padding: 11px 16px 11px 40px;
  background: #f5f7fb;
}

.add-btn {
  flex-shrink: 0;
}

.add-btn svg {
  width: 15px;
  height: 15px;
}

.friend-msg {
  margin-bottom: 16px;
  justify-content: center;
}

.msg-fade-enter-active,
.msg-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.msg-fade-enter-from,
.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ===== 好友列表 ===== */
.friend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius);
  box-shadow: var(--sd-shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.friend-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--sd-shadow);
  border-color: #dbeafe;
}

.friend-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 2px 9px rgba(99, 102, 241, 0.32);
}

.friend-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.friend-name {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-time {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--sd-text-4);
}

.friend-time svg {
  width: 12px;
  height: 12px;
}

.btn-remove {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  padding: 7px 13px;
  background: #fff;
  color: #ef4444;
  border: 1px solid #fecaca;
  border-radius: 999px;
  font-size: 12.5px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-remove svg {
  width: 14px;
  height: 14px;
}

.btn-remove:hover {
  background: #fef2f2;
  border-color: #fca5a5;
  transform: translateY(-1px);
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .add-card {
    flex-direction: column;
    align-items: stretch;
  }

  .friend-item {
    flex-wrap: wrap;
  }

  .btn-remove {
    width: 100%;
    justify-content: center;
  }
}
</style>
