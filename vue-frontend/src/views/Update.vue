<template>
  <div class="sd-page update-page">
    <div class="sd-card update-card">
      <div class="update-head">
        <span class="update-avatar">{{ avatarText }}</span>
        <div class="update-head-text">
          <h1>修改资料</h1>
          <p>更新账号信息，保存后需要重新登录</p>
        </div>
      </div>

      <form @submit.prevent="handleUpdate">
        <div class="sd-field">
          <label class="sd-label">用户名</label>
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input type="text" class="sd-input" v-model="updateForm.userName"
                   placeholder="请输入用户名" required />
          </div>
        </div>

        <div class="sd-field">
          <label class="sd-label">新密码</label>
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input :type="showPassword ? 'text' : 'password'" class="sd-input sd-input-suffix"
                   v-model="updateForm.userPassword" placeholder="请输入新密码" required />
            <button type="button" class="update-eye" @click="showPassword = !showPassword"
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

        <div class="sd-field">
          <label class="sd-label">邮箱</label>
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            <input type="email" class="sd-input" v-model="updateForm.userEmail"
                   placeholder="请输入邮箱" required />
          </div>
        </div>

        <div class="sd-field">
          <label class="sd-label">性别</label>
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="8" r="4"/>
              <path d="M4 21c0-4 3.6-6 8-6s8 2 8 6"/>
            </svg>
            <select class="sd-input sd-select-native" v-model="updateForm.userGender" required>
              <option value="" disabled>请选择性别</option>
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
            <svg class="update-select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>
        </div>

        <transition name="update-msg">
          <div v-if="message" class="sd-alert update-alert" :class="messageClass">
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

        <button type="submit" class="sd-btn sd-btn-primary sd-btn-lg sd-btn-block update-submit"
                :disabled="loading">
          <span v-if="loading" class="update-spinner"></span>
          <span>{{ loading ? '保存中...' : '保存修改' }}</span>
        </button>

        <div class="update-links">
          <router-link to="/login">返回登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'Update',
  data() {
    return {
      updateForm: {
        userName: '',
        userPassword: '',
        userEmail: '',
        userGender: ''
      },
      message: '',
      messageType: '',
      loading: false,
      showPassword: false
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'sd-alert-success' : 'sd-alert-error'
    },
    avatarText() {
      const text = (this.updateForm.userName || '').trim()
      if (!text) return 'U'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    }
  },
  methods: {
    async handleUpdate() {
      if (this.loading) return
      this.loading = true
      try {
        // 使用 URLSearchParams 发送表单数据
        const params = new URLSearchParams()
        params.append('userName', this.updateForm.userName)
        params.append('userPassword', this.updateForm.userPassword)
        params.append('userEmail', this.updateForm.userEmail)
        params.append('userGender', this.updateForm.userGender)
        
        const response = await api.post('/user/update', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        
        // 修改成功，返回成功消息
        this.messageType = 'success'
        this.message = response || '修改成功！'
        // 修改成功后跳转到登录页（保持 loading 态直到跳转）
        setTimeout(() => {
          this.$router.push('/login')
        }, 1500)
      } catch (error) {
        this.messageType = 'error'
        this.message = error.message || '修改失败，请稍后重试'
        console.error('修改错误:', error)
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.update-page {
  max-width: 520px;
  padding-top: 12px;
}

.update-card {
  padding: 30px 32px;
}

.update-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 22px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--sd-border-soft);
}

.update-avatar {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.34);
}

.update-head-text h1 {
  font-size: 20px;
  font-weight: 700;
  color: var(--sd-text);
  letter-spacing: 0.3px;
}

.update-head-text p {
  margin-top: 5px;
  font-size: 12.5px;
  color: var(--sd-text-4);
}

.sd-input-suffix {
  padding-right: 42px;
}

.sd-select-native {
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
  padding-right: 40px;
}

.update-select-arrow {
  position: absolute;
  right: 13px;
  width: 15px;
  height: 15px;
  color: var(--sd-text-4);
  pointer-events: none;
}

.update-eye {
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

.update-eye:hover {
  color: var(--sd-primary);
  background: var(--sd-primary-light);
}

.update-eye svg {
  width: 17px;
  height: 17px;
}

.update-alert {
  margin-bottom: 18px;
}

.update-alert svg {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

.update-submit {
  letter-spacing: 1px;
}

.update-spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

.update-links {
  margin-top: 20px;
  text-align: center;
}

.update-links a {
  color: var(--sd-primary-dark);
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.update-links a:hover {
  color: var(--sd-accent);
  text-decoration: underline;
}

.update-msg-enter-active,
.update-msg-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.update-msg-enter-from,
.update-msg-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

@media (max-width: 540px) {
  .update-card {
    padding: 24px 20px;
  }
}
</style>
