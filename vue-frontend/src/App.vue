<template>
  <div id="app">
    <nav class="navbar" :class="{ 'navbar-scrolled': scrolled }">
      <div class="navbar-content">
        <router-link :to="isLoggedIn ? '/allties' : '/login'" class="nav-brand">
          <span class="nav-brand-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
            </svg>
          </span>
          <span class="nav-brand-text">校园论坛</span>
        </router-link>

        <div class="nav-links">
          <router-link
            v-for="item in mainNavs"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ 'nav-item-primary': item.primary }"
          >
            {{ item.label }}
            <span v-if="item.badge > 0" class="badge">{{ item.badge > 99 ? '99+' : item.badge }}</span>
          </router-link>
        </div>

        <div class="nav-right">
          <div v-if="serverInfo" class="server-info">
            <span class="nav-dot"></span>
            <span class="server-label">服务器</span>
            <span class="server-address">{{ serverInfo.address }}</span>
          </div>

          <div v-if="isLoggedIn" class="nav-user">
            <button class="nav-user-trigger" @click="menuOpen = !menuOpen">
              <span class="nav-avatar">{{ avatarText }}</span>
              <span class="nav-user-name">{{ currentUser }}</span>
              <span v-if="isStaff" class="admin-badge">管理员</span>
              <svg class="nav-chevron" :class="{ 'nav-chevron-open': menuOpen }" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>

            <transition name="nav-fade">
              <div v-if="menuOpen" class="nav-dropdown">
                <div class="nav-dropdown-header">
                  <span class="nav-avatar nav-avatar-lg">{{ avatarText }}</span>
                  <div class="nav-dropdown-meta">
                    <p class="nav-dropdown-name">{{ currentUser }}</p>
                    <p class="nav-dropdown-role">{{ isStaff ? '管理员账号' : '普通用户' }}</p>
                  </div>
                </div>
                <div class="nav-dropdown-divider"></div>

                <router-link
                  v-for="item in adminNavs"
                  :key="item.path"
                  :to="item.path"
                  class="nav-dropdown-item"
                  @click="closeMenus"
                >
                  <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path v-if="item.icon === 'shield'" d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    <path v-else d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                  </svg>
                  <span>{{ item.label }}</span>
                </router-link>

                <router-link to="/update" class="nav-dropdown-item" @click="closeMenus">
                  <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  <span>修改资料</span>
                </router-link>

                <div class="nav-dropdown-divider"></div>

                <button class="nav-dropdown-item nav-dropdown-danger" @click="handleLogout">
                  <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  <span>退出登录</span>
                </button>
              </div>
            </transition>
          </div>

          <button
            class="nav-hamburger"
            :class="{ 'nav-hamburger-open': mobileOpen }"
            aria-label="切换菜单"
            @click="toggleMobile"
          >
            <span></span><span></span><span></span>
          </button>
        </div>
      </div>

      <transition name="nav-slide">
        <div v-if="mobileOpen" class="nav-mobile-panel">
          <router-link
            v-for="item in mobileNavs"
            :key="item.path"
            :to="item.path"
            class="nav-mobile-item"
            @click="closeMenus"
          >
            {{ item.label }}
            <span v-if="item.badge > 0" class="badge">{{ item.badge > 99 ? '99+' : item.badge }}</span>
          </router-link>
          <router-link v-if="isLoggedIn" to="/update" class="nav-mobile-item" @click="closeMenus">
            修改资料
          </router-link>
          <button v-if="isLoggedIn" class="nav-mobile-item nav-mobile-logout" @click="handleLogout">
            退出登录
          </button>
        </div>
      </transition>

      <div v-if="menuOpen || mobileOpen" class="nav-backdrop" @click="closeMenus"></div>
    </nav>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script>
import api from './utils/api'

export default {
  name: 'App',
  data() {
    return {
      currentUser: '',
      isLoggedIn: false,
      userRole: '',
      forceUpdate: 0,
      unreadCount: 0,
      serverInfo: null,
      scrolled: false,
      menuOpen: false,
      mobileOpen: false,
      notifySocket: null,
      notifySocketUser: ''
    }
  },
  computed: {
    isStaff() {
      return this.userRole === 'admin' || this.userRole === 'boss'
    },
    avatarText() {
      const name = (this.currentUser || '').trim()
      if (!name) return 'U'
      return /[a-zA-Z]/.test(name.charAt(0)) ? name.charAt(0).toUpperCase() : name.charAt(0)
    },
    mainNavs() {
      if (!this.isLoggedIn) {
        return [
          { path: '/login', label: '登录', primary: true },
          { path: '/register', label: '注册' },
          { path: '/update', label: '修改资料' }
        ]
      }
      return [
        { path: '/allties', label: '论坛广场' },
        { path: '/tielist', label: '我的帖子' },
        { path: '/createtie', label: '发帖' },
        { path: '/netdisk', label: '资源库' },
        { path: '/secondhand', label: '二手市场' },
        { path: '/homework', label: '作业中心' },
        { path: '/chat', label: '私信', badge: this.unreadCount },
        { path: '/ai-chat', label: 'AI助手' },
        { path: '/redpacket', label: '抢红包' },
        ...(this.isStaff ? [{ path: '/redpacket/publish', label: '发布红包' }] : [])
      ]
    },
    adminNavs() {
      if (!this.isStaff) return []
      return [
        { path: '/boss-dashboard', label: '管理中心', icon: 'shield' },
        { path: '/admin/files', label: '文件管理', icon: 'folder' },
        { path: '/admin/ai-config', label: 'AI配置', icon: 'folder' }
      ]
    },
    mobileNavs() {
      return [...this.mainNavs, ...this.adminNavs]
    }
  },
  created() {
    this.checkLoginStatus()
    // 监听路由变化，确保登录状态正确更新
    this.$router.afterEach(() => {
      this.checkLoginStatus()
      this.closeMenus()
    })
    // 定时获取未读消息数
    this.refreshUnreadCount()
    setInterval(() => {
      this.refreshUnreadCount()
    }, 30000) // 每 30 秒刷新一次
    // 获取服务器信息
    this.fetchServerInfo()
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll, { passive: true })
    this.handleScroll()
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.handleScroll)
    this.closeNotifySocket()
  },
  methods: {
    handleScroll() {
      this.scrolled = window.scrollY > 8
    },
    closeMenus() {
      this.menuOpen = false
      this.mobileOpen = false
    },
    toggleMobile() {
      this.mobileOpen = !this.mobileOpen
      this.menuOpen = false
    },
    checkLoginStatus() {
      // 从后端 Session 获取当前用户信息
      api.get('/user/current').then(response => {
        if (response.success && response.data) {
          this.currentUser = response.data.userName
          this.userRole = response.data.role || 'user'
          this.isLoggedIn = true
        } else {
          this.currentUser = ''
          this.userRole = ''
          this.isLoggedIn = false
        }
        this.forceUpdate++
        this.syncNotifySocket()
      }).catch(error => {
        console.error('获取用户信息失败:', error)
        this.currentUser = ''
        this.userRole = ''
        this.isLoggedIn = false
        this.forceUpdate++
        this.syncNotifySocket()
      })
    },
    refreshUnreadCount() {
      if (!this.isLoggedIn) return
      api.get('/message/unread/count').then(response => {
        if (response.success) {
          this.unreadCount = response.count || 0
        }
      }).catch(error => {
        console.error('获取未读消息失败:', error)
      })
    },
    handleLogout() {
      this.closeMenus()
      // 调用后端退出登录接口
      api.post('/user/logout', {}, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }).then(() => {
        this.currentUser = ''
        this.isLoggedIn = false
        this.$router.push('/login')
      }).catch(error => {
        console.error('退出登录失败:', error)
        this.currentUser = ''
        this.isLoggedIn = false
        this.$router.push('/login')
      })
    },
    fetchServerInfo() {
      api.get('/user/server-info').then(response => {
        if (response.success && response.data) {
          this.serverInfo = response.data
        }
      }).catch(error => {
        console.error('获取服务器信息失败:', error)
      })
    },
    // 根据登录状态建立/断开全局提醒 WebSocket
    syncNotifySocket() {
      if (this.isLoggedIn && this.currentUser) {
        this.connectNotifySocket()
      } else {
        this.closeNotifySocket()
      }
    },
    connectNotifySocket() {
      // 已是同一用户的连接则复用
      if (this.notifySocket && this.notifySocketUser === this.currentUser) {
        return
      }
      // 用户切换，先断开旧连接
      if (this.notifySocket && this.notifySocketUser !== this.currentUser) {
        this.closeNotifySocket()
      }
      try {
        const wsUrl = `ws://localhost:10010/ws/chat?userName=${this.currentUser}`
        const socket = new WebSocket(wsUrl)
        socket.onmessage = this.handleNotifyMessage
        socket.onclose = () => {
          this.notifySocket = null
          this.notifySocketUser = ''
        }
        this.notifySocket = socket
        this.notifySocketUser = this.currentUser
      } catch (error) {
        console.error('连接提醒 WebSocket 失败:', error)
      }
    },
    closeNotifySocket() {
      if (this.notifySocket) {
        this.notifySocket.onclose = null
        this.notifySocket.close()
        this.notifySocket = null
      }
      this.notifySocketUser = ''
    },
    // 仅处理后端 MQ 推送的提醒通知（type=notification），其余消息忽略
    handleNotifyMessage(event) {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'notification' && data.category === 'privateMessage') {
          this.$notify({
            title: `来自 ${data.sender} 的私信`,
            message: data.content,
            type: 'info',
            position: 'bottom-right',
            duration: 5000
          })
          this.refreshUnreadCount()
        } else if (data.type === 'notification' && data.category === 'orderCancel') {
          // 订单超时被自动取消，后端延迟队列消费者通过 WebSocket 实时推送
          this.$notify({
            title: '订单已自动取消',
            message: data.content,
            type: 'warning',
            position: 'bottom-right',
            duration: 6000
          })
        }
      } catch (error) {
        // 非 JSON 或无需处理的消息，忽略
      }
    }
  }
}
</script>

<style>
/* ===== 导航栏容器 ===== */
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  min-height: var(--sd-nav-h);
  padding: 0 24px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: saturate(180%) blur(16px);
  -webkit-backdrop-filter: saturate(180%) blur(16px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  transition: box-shadow 0.25s ease, background 0.25s ease;
}

.navbar-scrolled {
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.08);
}

.navbar-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ===== Logo ===== */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}

.nav-brand-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: var(--sd-gradient);
  box-shadow: var(--sd-shadow-primary);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.nav-brand-icon svg {
  width: 19px;
  height: 19px;
}

.nav-brand:hover .nav-brand-icon {
  transform: translateY(-1px) rotate(-4deg);
  box-shadow: 0 8px 18px rgba(59, 130, 246, 0.45);
}

.nav-brand-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--sd-text);
}

/* ===== 主导航 ===== */
.nav-links {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  /* overflow-x:auto 会使 overflow-y 也变为裁剪，导致选中项的渐变发光(box-shadow)被上下切平；
     用等量负 margin 抵消 padding，既给发光留出垂直空间，又不改变导航高度与布局位置 */
  padding: 14px 0;
  margin: -14px 0;
}

.nav-links::-webkit-scrollbar {
  display: none;
}

.nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 999px;
  color: var(--sd-text-3);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  transition: color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.nav-item:hover {
  color: var(--sd-primary-dark);
  background: var(--sd-primary-light);
}

.nav-item.router-link-active {
  color: #fff;
  font-weight: 600;
  background: var(--sd-gradient);
  box-shadow: var(--sd-shadow-primary);
}

.nav-item-primary {
  color: #fff;
  background: var(--sd-gradient);
  box-shadow: var(--sd-shadow-primary);
}

.nav-item-primary:hover {
  color: #fff;
  background: var(--sd-gradient);
  filter: brightness(1.06);
}

.nav-item .badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 17px;
  height: 17px;
  padding: 0 5px;
  background: var(--sd-danger);
  color: #fff;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.85);
}

.nav-item.router-link-active .badge {
  background: #fff;
  color: #ef4444;
  box-shadow: none;
}

/* ===== 右侧区域 ===== */
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.server-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 11px;
  background: #ecfdf5;
  border: 1px solid #d1fae5;
  color: #059669;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.nav-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--sd-success);
  animation: sd-pulse 1.8s ease-in-out infinite;
}

.server-label {
  opacity: 0.75;
}

.server-address {
  font-family: 'SF Mono', 'Fira Code', Consolas, monospace;
  font-weight: 600;
}

/* ===== 用户菜单 ===== */
.nav-user {
  position: relative;
}

.nav-user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.nav-user-trigger:hover {
  background: #f1f5f9;
  border-color: var(--sd-border-soft);
}

.nav-avatar {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.35);
}

.nav-avatar-lg {
  width: 38px;
  height: 38px;
  font-size: 15px;
}

.nav-user-name {
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sd-text);
}

.admin-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #dbeafe;
  color: var(--sd-primary-dark);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.nav-chevron {
  width: 14px;
  height: 14px;
  color: var(--sd-text-4);
  transition: transform 0.25s ease;
}

.nav-chevron-open {
  transform: rotate(180deg);
}

.nav-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 216px;
  padding: 6px;
  background: #fff;
  border: 1px solid var(--sd-border-soft);
  border-radius: 14px;
  box-shadow: var(--sd-shadow-lg);
  z-index: 20;
}

.nav-dropdown-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px 10px;
}

.nav-dropdown-meta {
  min-width: 0;
}

.nav-dropdown-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-dropdown-role {
  margin-top: 2px;
  font-size: 12px;
  color: var(--sd-text-4);
}

.nav-dropdown-divider {
  height: 1px;
  margin: 5px 6px;
  background: #f1f5f9;
}

.nav-dropdown-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  background: transparent;
  border: none;
  border-radius: 9px;
  color: var(--sd-text-2);
  font-size: 13.5px;
  font-weight: 500;
  font-family: inherit;
  text-decoration: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}

.nav-dropdown-item:hover {
  background: #f1f5f9;
  color: var(--sd-primary-dark);
}

.nav-dropdown-item.router-link-active {
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-weight: 600;
}

.nav-dropdown-danger:hover {
  background: #fef2f2;
  color: #dc2626;
}

.nav-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  opacity: 0.85;
}

/* ===== 汉堡按钮 ===== */
.nav-hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  width: 36px;
  height: 36px;
  padding: 0 8px;
  background: transparent;
  border: 1px solid var(--sd-border);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.nav-hamburger:hover {
  background: #f1f5f9;
}

.nav-hamburger span {
  display: block;
  height: 2px;
  width: 100%;
  border-radius: 2px;
  background: var(--sd-text-2);
  transition: transform 0.28s ease, opacity 0.2s ease;
}

.nav-hamburger-open span:nth-child(1) {
  transform: translateY(6px) rotate(45deg);
}

.nav-hamburger-open span:nth-child(2) {
  opacity: 0;
}

.nav-hamburger-open span:nth-child(3) {
  transform: translateY(-6px) rotate(-45deg);
}

/* ===== 移动端面板 ===== */
.nav-mobile-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: calc(100vh - var(--sd-nav-h));
  overflow-y: auto;
  padding: 10px 16px 16px;
  background: #fff;
  border-bottom: 1px solid var(--sd-border);
  box-shadow: var(--sd-shadow-md);
  z-index: 15;
}

.nav-mobile-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 14px;
  border-radius: 10px;
  color: var(--sd-text-2);
  font-size: 14.5px;
  font-weight: 500;
  text-decoration: none;
  background: transparent;
  border: none;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}

.nav-mobile-item:hover {
  background: #f1f5f9;
  color: var(--sd-primary-dark);
}

.nav-mobile-item.router-link-active {
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-weight: 600;
}

.nav-mobile-item .badge {
  display: inline-flex;
  align-items: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: var(--sd-danger);
  color: #fff;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.nav-mobile-logout {
  margin-top: 6px;
  color: #dc2626;
  background: #fef2f2;
}

.nav-backdrop {
  position: fixed;
  top: var(--sd-nav-h);
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.22);
  z-index: 5;
}

/* ===== 过渡动画 ===== */
.nav-fade-enter-active,
.nav-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.nav-fade-enter-from,
.nav-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}

.nav-slide-enter-active,
.nav-slide-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.nav-slide-enter-from,
.nav-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ===== 内容区 ===== */
.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 26px 20px 40px;
}

/* ===== 响应式 ===== */
@media (max-width: 1280px) {
  .nav-user-name {
    display: none;
  }

  .server-info {
    display: none;
  }
}

@media (max-width: 1100px) {
  .nav-links {
    display: none;
  }

  .server-info {
    display: none;
  }

  .nav-hamburger {
    display: flex;
  }

  .navbar {
    padding: 0 16px;
  }
}

@media (max-width: 480px) {
  .admin-badge {
    display: none;
  }

  .nav-brand-text {
    font-size: 16px;
  }
}
</style>
