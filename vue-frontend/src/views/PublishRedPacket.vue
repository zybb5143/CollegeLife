<template>
  <div class="sd-page redpacket-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">发布红包</h1>
        <p class="sd-subtitle">管理员发布红包，支持 Redis 高性能与 MySQL 持久化两种存储</p>
      </div>
      <router-link to="/redpacket" class="sd-btn sd-btn-soft">去抢红包</router-link>
    </div>

    <!-- 管理员发布红包 -->
    <div v-if="isStaff" class="sd-card publish-card">
      <h3 class="section-title">
        <span class="title-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 5v14M5 12h14"/>
          </svg>
        </span>
        发布红包
        <span class="sd-tag sd-tag-pink">管理员</span>
      </h3>

      <div class="publish-grid">
        <div class="sd-field">
          <label class="sd-label">红包编号</label>
          <input class="sd-input" v-model="publishForm.packetNo" placeholder="例如：RP20240509" />
        </div>

        <div class="sd-field">
          <label class="sd-label">总金额（元）</label>
          <div class="number-row">
            <button type="button" class="step-btn" @click="stepAmount(-10)">−</button>
            <input
              class="sd-input step-input"
              type="number"
              :min="1"
              :max="10000"
              step="0.01"
              v-model.number="publishForm.totalAmount"
            />
            <button type="button" class="step-btn" @click="stepAmount(10)">+</button>
          </div>
        </div>

        <div class="sd-field">
          <label class="sd-label">红包个数（最多 10000）</label>
          <div class="number-row">
            <button type="button" class="step-btn" @click="stepCount(-10)">−</button>
            <input
              class="sd-input step-input"
              type="number"
              :min="1"
              :max="10000"
              v-model.number="publishForm.totalCount"
            />
            <button type="button" class="step-btn" @click="stepCount(10)">+</button>
          </div>
        </div>

        <div class="sd-field">
          <label class="sd-label">存储类型</label>
          <div class="storage-selector storage-light">
            <button
              type="button"
              class="storage-option"
              :class="{ 'storage-active-light': publishForm.storageType === 'redis' }"
              @click="publishForm.storageType = 'redis'"
            >
              <span class="storage-name">Redis</span>
              <span class="storage-desc">高性能</span>
            </button>
            <button
              type="button"
              class="storage-option"
              :class="{ 'storage-active-light': publishForm.storageType === 'mysql' }"
              @click="publishForm.storageType = 'mysql'"
            >
              <span class="storage-name">MySQL</span>
              <span class="storage-desc">持久化</span>
            </button>
          </div>
        </div>
      </div>

      <p v-if="publishSummary" class="publish-summary">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="16" x2="12" y2="12"/>
          <line x1="12" y1="8" x2="12.01" y2="8"/>
        </svg>
        {{ publishSummary }}
      </p>

      <div class="form-foot">
        <button class="sd-btn sd-btn-primary" :disabled="publishing" @click="handlePublish">
          <span v-if="publishing" class="btn-spinner"></span>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"/>
            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          {{ publishing ? '发布中...' : '发布红包' }}
        </button>
      </div>
    </div>

    <!-- 非管理员提示 -->
    <div v-else class="sd-card publish-card">
      <p class="publish-summary">仅管理员（admin / boss）可发布红包。</p>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'PublishRedPacket',
  data() {
    return {
      userRole: '',
      publishForm: {
        packetNo: '',
        totalAmount: 200,
        totalCount: 500,
        storageType: 'redis'
      },
      publishing: false
    }
  },
  computed: {
    isStaff() {
      return this.userRole === 'admin' || this.userRole === 'boss'
    },
    publishSummary() {
      const amount = parseFloat(this.publishForm.totalAmount)
      const count = parseInt(this.publishForm.totalCount)
      if (isNaN(amount) || isNaN(count) || count <= 0) return ''
      return `平均每个红包约 ${(amount / count).toFixed(2)} 元`
    }
  },
  async created() {
    await this.loadUserInfo()
    // 非管理员不允许停留在发布页
    if (!this.isStaff) {
      this.$message.warning('仅管理员可发布红包')
      this.$router.replace('/redpacket')
    }
  },
  methods: {
    async loadUserInfo() {
      try {
        const user = JSON.parse(localStorage.getItem('user'))
        if (user) {
          this.userRole = user.role
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    stepAmount(delta) {
      const next = (parseFloat(this.publishForm.totalAmount) || 0) + delta
      this.publishForm.totalAmount = Math.min(10000, Math.max(1, Number(next.toFixed(2))))
    },
    stepCount(delta) {
      const next = (parseInt(this.publishForm.totalCount) || 0) + delta
      this.publishForm.totalCount = Math.min(10000, Math.max(1, next))
    },
    async handlePublish() {
      if (!this.publishForm.packetNo || this.publishForm.packetNo.trim() === '') {
        this.$message.warning('请输入红包编号')
        return
      }
      if (this.publishForm.totalAmount <= 0) {
        this.$message.warning('请输入有效的金额')
        return
      }
      if (this.publishForm.totalCount > 10000) {
        this.$message.warning('红包个数不能超过10000个')
        return
      }

      this.publishing = true
      try {
        const response = await api.post('/redpacket/publish', null, {
          params: {
            packetNo: this.publishForm.packetNo,
            totalAmount: this.publishForm.totalAmount,
            totalCount: this.publishForm.totalCount,
            storageType: this.publishForm.storageType
          }
        })

        if (response.success) {
          this.$message.success(response.message)
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('发布红包失败:', error)
        this.$message.error('发布红包失败')
      } finally {
        this.publishing = false
      }
    }
  }
}
</script>

<style scoped src="../styles/redpacket.css"></style>
