<template>
  <div class="sd-page checkin-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">每日签到</h1>
        <p class="sd-subtitle">坚持签到，积累你的校园活跃值</p>
      </div>
      <div v-if="currentUserName" class="user-chip">
        <span class="user-chip-avatar">{{ avatarText }}</span>
        <span class="user-chip-name">{{ currentUserName }}</span>
      </div>
    </div>

    <!-- 签到主卡片 -->
    <div class="checkin-hero">
      <div class="hero-glow hero-glow-1"></div>
      <div class="hero-glow hero-glow-2"></div>

      <span class="hero-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
          <path d="M9 16l2 2 4-4"/>
        </svg>
      </span>

      <p class="hero-label">连续签到</p>
      <p class="hero-days">
        {{ continuousDays }}<span class="hero-days-unit">天</span>
      </p>
      <p class="hero-last">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        上次签到：{{ lastCheckinTime || '从未签到' }}
      </p>

      <button
        class="checkin-btn"
        :class="{ 'checkin-btn-done': hasCheckedInToday }"
        :disabled="hasCheckedInToday || loading"
        @click="handleCheckin"
      >
        <span v-if="loading" class="checkin-spinner"></span>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"
             stroke-linecap="round" stroke-linejoin="round">
          <polyline v-if="hasCheckedInToday" points="20 6 9 17 4 12"/>
          <template v-else>
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
          </template>
        </svg>
        {{ hasCheckedInToday ? '今日已签到' : (loading ? '签到中...' : '立即签到') }}
      </button>

      <p class="hero-tip">
        {{ hasCheckedInToday ? '明天记得再来，连续签到奖励更丰富' : '每天仅可签到一次，零点刷新' }}
      </p>
    </div>

    <!-- 签到规则 -->
    <div class="sd-card rules-card">
      <h3 class="rules-title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="16" x2="12" y2="12"/>
          <line x1="12" y1="8" x2="12.01" y2="8"/>
        </svg>
        签到规则
      </h3>
      <ul class="rules-list">
        <li v-for="rule in rules" :key="rule.text" class="rules-item">
          <span class="rules-item-icon" v-html="rule.icon"></span>
          <span>{{ rule.text }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'Checkin',
  data() {
    return {
      currentUserName: '',
      continuousDays: 0,
      lastCheckinTime: '',
      hasCheckedInToday: false,
      loading: false,
      rules: [
        {
          text: '每天可签到一次',
          icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>'
        },
        {
          text: '连续签到天数越多，奖励越丰富',
          icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>'
        },
        {
          text: '每日零点刷新签到状态',
          icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>'
        }
      ]
    }
  },
  computed: {
    avatarText() {
      const text = (this.currentUserName || '').trim()
      if (!text) return 'U'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    }
  },
  async created() {
    await this.loadUserInfo()
    await this.loadCheckinStatus()
  },
  methods: {
    async loadUserInfo() {
      try {
        const user = JSON.parse(localStorage.getItem('user'))
        if (user) {
          this.currentUserName = user.userName
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    async loadCheckinStatus() {
      if (!this.currentUserName) return
      try {
        const response = await api.get('/user/checkin/info')
        if (response.success && response.data) {
          this.continuousDays = response.data.continuousDays || 0
          this.lastCheckinTime = response.data.lastCheckinTime || ''
          this.hasCheckedInToday = response.data.hasCheckedInToday || false
        }
      } catch (error) {
        console.error('加载签到状态失败:', error)
      }
    },
    async handleCheckin() {
      if (!this.currentUserName) {
        this.$message.warning('请先登录')
        return
      }
      this.loading = true
      try {
        const response = await api.post('/user/checkin')
        if (response.success) {
          this.$message.success(response.message || '签到成功！')
          await this.loadCheckinStatus()
        } else {
          this.$message.warning(response.message || '签到失败')
        }
      } catch (error) {
        this.$message.error('签到失败，请重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.checkin-page {
  max-width: 620px;
}

/* ===== 用户标识 ===== */
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px 5px 5px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--sd-border-soft);
  border-radius: 999px;
  box-shadow: var(--sd-shadow-sm);
}

.user-chip-avatar {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.user-chip-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--sd-text);
}

/* ===== 签到主卡 ===== */
.checkin-hero {
  position: relative;
  padding: 40px 30px 34px;
  margin-bottom: 18px;
  border-radius: var(--sd-radius-xl);
  background: linear-gradient(150deg, #2563eb 0%, #4f46e5 55%, #7c3aed 100%);
  color: #fff;
  text-align: center;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(59, 130, 246, 0.32);
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0.5;
  pointer-events: none;
}

.hero-glow-1 {
  width: 220px;
  height: 220px;
  top: -80px;
  right: -50px;
  background: #38bdf8;
}

.hero-glow-2 {
  width: 200px;
  height: 200px;
  bottom: -90px;
  left: -40px;
  background: #c084fc;
}

.hero-icon {
  position: relative;
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
}

.hero-icon svg {
  width: 27px;
  height: 27px;
}

.hero-label {
  position: relative;
  font-size: 13.5px;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.78);
}

.hero-days {
  position: relative;
  margin: 6px 0 4px;
  font-size: 62px;
  font-weight: 800;
  line-height: 1.05;
  letter-spacing: -1px;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 6px 22px rgba(0, 0, 0, 0.18);
}

.hero-days-unit {
  margin-left: 6px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  opacity: 0.85;
}

.hero-last {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 26px;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.8);
}

.hero-last svg {
  width: 13px;
  height: 13px;
}

.checkin-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 186px;
  height: 48px;
  padding: 0 28px;
  border: none;
  border-radius: 999px;
  background: #fff;
  color: var(--sd-primary-dark);
  font-size: 15.5px;
  font-weight: 700;
  font-family: inherit;
  letter-spacing: 0.8px;
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.16);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.checkin-btn svg {
  width: 17px;
  height: 17px;
}

.checkin-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.22);
}

.checkin-btn:active:not(:disabled) {
  transform: translateY(0);
}

.checkin-btn:disabled {
  cursor: not-allowed;
  opacity: 0.82;
}

.checkin-btn-done {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: #fff;
  box-shadow: none;
}

.checkin-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(59, 130, 246, 0.28);
  border-top-color: var(--sd-primary-dark);
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

.hero-tip {
  position: relative;
  margin-top: 16px;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.72);
}

/* ===== 规则卡片 ===== */
.rules-card {
  padding: 22px 24px;
}

.rules-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 15.5px;
  font-weight: 700;
  color: var(--sd-text);
}

.rules-title svg {
  width: 17px;
  height: 17px;
  color: var(--sd-primary);
}

.rules-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rules-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 10px 4px;
  font-size: 13.5px;
  color: var(--sd-text-3);
  border-bottom: 1px dashed var(--sd-border-soft);
}

.rules-item:last-child {
  border-bottom: none;
}

.rules-item-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
}

.rules-item-icon :deep(svg) {
  width: 15px;
  height: 15px;
}

@media (max-width: 480px) {
  .checkin-hero {
    padding: 32px 20px 28px;
  }

  .hero-days {
    font-size: 50px;
  }

  .checkin-btn {
    width: 100%;
  }
}
</style>
