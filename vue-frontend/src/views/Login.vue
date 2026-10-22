<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌展示区 -->
      <div class="auth-hero">
        <div class="auth-hero-glow auth-hero-glow-1"></div>
        <div class="auth-hero-glow auth-hero-glow-2"></div>

        <div class="auth-hero-top">
          <span class="auth-hero-logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
            </svg>
          </span>
          <div>
            <p class="auth-hero-name">校园论坛</p>
            <p class="auth-hero-en">CAMPUS FORUM</p>
          </div>
        </div>

        <h2 class="auth-hero-title">
          欢迎回来，<br />继续你的校园生活
        </h2>
        <p class="auth-hero-desc">交流 · 交易 · 学习，一个平台全部搞定。</p>

        <ul class="auth-hero-list">
          <li v-for="item in heroFeatures" :key="item.title" class="auth-hero-item">
            <span class="auth-hero-item-icon" v-html="item.icon"></span>
            <div>
              <p class="auth-hero-item-title">{{ item.title }}</p>
              <p class="auth-hero-item-desc">{{ item.desc }}</p>
            </div>
          </li>
        </ul>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-form-side">
        <div class="auth-form-head">
          <h3>用户登录</h3>
          <p>登录后即可发帖、私信与参与二手交易</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="sd-field">
            <label class="sd-label">用户名</label>
            <div class="sd-input-wrap">
              <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input type="text" class="sd-input" v-model="loginForm.userName"
                     placeholder="请输入用户名" autocomplete="username" required />
            </div>
          </div>

          <div class="sd-field">
            <label class="sd-label">密码</label>
            <div class="sd-input-wrap">
              <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input :type="showPassword ? 'text' : 'password'" class="sd-input sd-input-suffix"
                     v-model="loginForm.userPassword" placeholder="请输入密码"
                     autocomplete="current-password" required />
              <button type="button" class="auth-eye" @click="showPassword = !showPassword"
                      :aria-label="showPassword ? '隐藏密码' : '显示密码'">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <template v-if="showPassword">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                    <path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </template>
                  <template v-else>
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </template>
                </svg>
              </button>
            </div>
          </div>

          <transition name="auth-msg">
            <div v-if="message" class="sd-alert auth-alert" :class="messageClass">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <template v-if="messageType === 'success'">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22 4 12 14.01 9 11.01"/>
                </template>
                <template v-else>
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </template>
              </svg>
              <span>{{ message }}</span>
            </div>
          </transition>

          <button type="submit" class="sd-btn sd-btn-primary sd-btn-lg sd-btn-block auth-submit"
                  :disabled="loading">
            <span v-if="loading" class="auth-btn-spinner"></span>
            <span>{{ loading ? '登录中...' : '登录' }}</span>
            <svg v-if="!loading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="5" y1="12" x2="19" y2="12"/>
              <polyline points="12 5 19 12 12 19"/>
            </svg>
          </button>

          <div class="auth-links">
            <router-link to="/register">没有账号？去注册</router-link>
            <span class="auth-links-divider"></span>
            <router-link to="/update">修改信息</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: { userName: '', userPassword: '' },
      message: '',
      messageType: '',
      loading: false,
      showPassword: false,
      heroFeatures: [
        {
          title: '论坛广场',
          desc: '发帖互动，分享校园新鲜事',
          icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>'
        },
        {
          title: '二手市场',
          desc: '闲置流转，校内交易更安心',
          icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>'
        },
        {
          title: 'AI 助手',
          desc: '随时答疑，学习效率翻倍',
          icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="3"/><circle cx="9" cy="10" r="1.2"/><circle cx="15" cy="10" r="1.2"/><path d="M9 15h6"/></svg>'
        }
      ]
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'sd-alert-success' : 'sd-alert-error'
    }
  },
  methods: {
    async handleLogin() {
      if (this.loading) return
      this.loading = true
      try {
        const params = new URLSearchParams()
        params.append('userName', this.loginForm.userName)
        params.append('userPassword', this.loginForm.userPassword)
        const response = await api.post('/user/login', params, {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })
        if (response.success) {
          this.messageType = 'success'
          this.message = '登录成功，欢迎 ' + this.loginForm.userName
          localStorage.setItem('token', response.token)
          localStorage.setItem('role', response.role)
          localStorage.setItem('user', JSON.stringify(response.data))
          // 保持 loading 态直到跳转，避免出现按钮闪烁
          setTimeout(() => this.$router.push('/allties'), 500)
        } else {
          this.messageType = 'error'
          this.message = response.message || '登录失败'
          this.loading = false
        }
      } catch (error) {
        this.messageType = 'error'
        this.message = error.message || '登录失败'
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - var(--sd-nav-h) - 66px);
  padding: 10px 0;
}

.auth-card {
  display: grid;
  grid-template-columns: 1.02fr 1fr;
  width: 100%;
  max-width: 900px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: var(--sd-radius-xl);
  box-shadow: var(--sd-shadow-lg);
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.auth-hero {
  position: relative;
  padding: 42px 36px;
  background: linear-gradient(150deg, #2563eb 0%, #4f46e5 52%, #7c3aed 100%);
  color: #fff;
  overflow: hidden;
}

.auth-hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(46px);
  opacity: 0.55;
  pointer-events: none;
}

.auth-hero-glow-1 {
  width: 230px;
  height: 230px;
  top: -70px;
  right: -60px;
  background: #38bdf8;
}

.auth-hero-glow-2 {
  width: 200px;
  height: 200px;
  bottom: -80px;
  left: -50px;
  background: #a855f7;
}

.auth-hero-top {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 34px;
}

.auth-hero-logo {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(6px);
}

.auth-hero-logo svg {
  width: 22px;
  height: 22px;
}

.auth-hero-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
}

.auth-hero-en {
  margin-top: 2px;
  font-size: 11px;
  letter-spacing: 2.4px;
  color: rgba(255, 255, 255, 0.68);
}

.auth-hero-title {
  position: relative;
  font-size: 27px;
  font-weight: 700;
  line-height: 1.42;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

.auth-hero-desc {
  position: relative;
  font-size: 13.5px;
  color: rgba(255, 255, 255, 0.78);
  margin-bottom: 32px;
}

.auth-hero-list {
  position: relative;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.auth-hero-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.auth-hero-item-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.auth-hero-item-icon :deep(svg) {
  width: 17px;
  height: 17px;
}

.auth-hero-item-title {
  font-size: 14px;
  font-weight: 600;
}

.auth-hero-item-desc {
  margin-top: 3px;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.72);
}

/* ===== 右侧表单区 ===== */
.auth-form-side {
  padding: 46px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-form-head {
  margin-bottom: 26px;
}

.auth-form-head h3 {
  font-size: 23px;
  font-weight: 700;
  color: var(--sd-text);
  letter-spacing: 0.4px;
}

.auth-form-head p {
  margin-top: 7px;
  font-size: 13px;
  color: var(--sd-text-4);
}

.sd-input-suffix {
  padding-right: 42px;
}

.auth-eye {
  position: absolute;
  right: 8px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 7px;
  color: var(--sd-text-4);
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease;
}

.auth-eye:hover {
  color: var(--sd-primary);
  background: var(--sd-primary-light);
}

.auth-eye svg {
  width: 17px;
  height: 17px;
}

.auth-alert {
  margin-bottom: 18px;
}

.auth-alert svg {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

.auth-submit {
  margin-top: 4px;
  letter-spacing: 1px;
}

.auth-submit svg {
  width: 17px;
  height: 17px;
  transition: transform 0.2s ease;
}

.auth-submit:hover svg {
  transform: translateX(3px);
}

.auth-btn-spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

.auth-links {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  font-size: 13.5px;
}

.auth-links a {
  color: var(--sd-primary-dark);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.auth-links a:hover {
  color: var(--sd-accent);
  text-decoration: underline;
}

.auth-links-divider {
  width: 1px;
  height: 13px;
  background: var(--sd-border);
}

.auth-msg-enter-active,
.auth-msg-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.auth-msg-enter-from,
.auth-msg-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* ===== 响应式 ===== */
@media (max-width: 860px) {
  .auth-card {
    grid-template-columns: 1fr;
    max-width: 440px;
  }

  .auth-hero {
    padding: 30px 28px;
  }

  .auth-hero-top {
    margin-bottom: 22px;
  }

  .auth-hero-title {
    font-size: 22px;
  }

  .auth-hero-desc {
    margin-bottom: 22px;
  }

  .auth-hero-list {
    gap: 12px;
  }

  .auth-form-side {
    padding: 32px 28px;
  }
}

@media (max-width: 480px) {
  .auth-hero-list {
    display: none;
  }

  .auth-form-side {
    padding: 26px 20px;
  }
}
</style>
