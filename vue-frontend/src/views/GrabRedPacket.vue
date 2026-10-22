<template>
  <div class="sd-page redpacket-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">抢红包</h1>
        <p class="sd-subtitle">输入红包编号即可参与，手气最佳等着你</p>
      </div>
      <div v-if="grabForm.userName" class="user-chip">
        <span class="user-chip-avatar">{{ avatarText }}</span>
        <span class="user-chip-name">{{ grabForm.userName }}</span>
      </div>
    </div>

    <!-- 抢红包主区 -->
    <div class="grab-hero">
      <div class="hero-glow hero-glow-1"></div>
      <div class="hero-glow hero-glow-2"></div>
      <span class="hero-deco hero-deco-1"></span>
      <span class="hero-deco hero-deco-2"></span>

      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <rect x="2.5" y="6" width="19" height="15" rx="2.5"/>
          <path d="M2.5 10.5h19"/>
          <path d="M12 6v4.5"/>
          <circle cx="12" cy="14.5" r="2.6"/>
          <path d="M8 3.2l1.6 2.8M16 3.2l-1.6 2.8"/>
        </svg>
      </div>
      <h2 class="hero-title">抢红包</h2>
      <p class="hero-sub">支持 Redis 高性能与 MySQL 持久化两种存储</p>

      <div class="hero-form">
        <div class="hero-field">
          <label class="hero-label">红包编号</label>
          <div class="hero-input-wrap">
            <svg class="hero-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="16" rx="2"/>
              <path d="M7 9h4M7 13h10"/>
            </svg>
            <input
              class="hero-input"
              v-model="grabForm.packetNo"
              placeholder="输入红包编号，例如 RP20240509"
              @blur="loadPacketDetail"
              @keyup.enter="handleGrab"
            />
          </div>
        </div>

        <div class="hero-field">
          <label class="hero-label">用户名</label>
          <div class="hero-input-wrap">
            <svg class="hero-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input class="hero-input" v-model="grabForm.userName" placeholder="输入您的用户名" />
          </div>
        </div>

        <div class="hero-field">
          <label class="hero-label">存储类型</label>
          <div class="storage-selector">
            <button
              type="button"
              class="storage-option"
              :class="{ 'storage-active': grabForm.storageType === 'redis' }"
              @click="grabForm.storageType = 'redis'"
            >
              <span class="storage-name">Redis</span>
              <span class="storage-desc">高性能</span>
            </button>
            <button
              type="button"
              class="storage-option"
              :class="{ 'storage-active': grabForm.storageType === 'mysql' }"
              @click="grabForm.storageType = 'mysql'"
            >
              <span class="storage-name">MySQL</span>
              <span class="storage-desc">持久化</span>
            </button>
          </div>
        </div>

        <button class="grab-btn" :disabled="grabbing" @click="handleGrab">
          <span v-if="grabbing" class="grab-spinner"></span>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
          </svg>
          {{ grabbing ? '正在抢...' : '立即抢红包' }}
        </button>
      </div>

      <!-- 红包详情 -->
      <div v-if="packetDetail" class="packet-detail">
        <div class="detail-cell">
          <span class="detail-num">{{ packetDetail.remainCount }}<i>/{{ packetDetail.totalCount }}</i></span>
          <span class="detail-label">剩余个数</span>
        </div>
        <span class="detail-divider"></span>
        <div class="detail-cell">
          <span class="detail-num">{{ packetDetail.remainAmount }}<i>元</i></span>
          <span class="detail-label">剩余金额</span>
        </div>
        <span class="detail-divider"></span>
        <div class="detail-cell">
          <span class="detail-num">{{ packetDetail.totalAmount }}<i>元</i></span>
          <span class="detail-label">红包总额</span>
        </div>
      </div>
    </div>

    <!-- 抢红包结果 -->
    <transition name="result-pop">
      <div v-if="grabResult" class="result-card" :class="{ success: grabResult.success }">
        <span class="result-icon">
          <svg v-if="grabResult.success" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </span>
        <h3 class="result-title">{{ grabResult.success ? '恭喜抢到红包！' : '很遗憾' }}</h3>
        <p class="result-msg">{{ grabResult.message }}</p>
        <p v-if="grabResult.amount" class="result-amount">
          <span class="amount-symbol">¥</span>{{ grabResult.amount }}
        </p>
        <button class="result-close" @click="grabResult = null">知道了</button>
      </div>
    </transition>

    <!-- 抢红包记录 -->
    <div class="sd-card records-card">
      <div class="records-head">
        <h3 class="section-title">
          <span class="title-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </span>
          我的抢红包记录
          <span class="count-pill">{{ records.length }}</span>
        </h3>
        <button class="sd-btn sd-btn-soft icon-btn" @click="loadRecords" title="刷新记录">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
        </button>
      </div>

      <el-table :data="records" class="records-table" empty-text="还没有抢到红包">
        <el-table-column prop="packetNo" label="红包编号" min-width="140" />
        <el-table-column prop="userName" label="用户名" min-width="110" />
        <el-table-column prop="amount" label="抢到金额" width="120">
          <template #default="scope">
            <span class="amount-cell">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="grabTime" label="时间" min-width="170" />
      </el-table>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'GrabRedPacket',
  data() {
    return {
      grabForm: {
        packetNo: '',
        userName: '',
        storageType: 'redis'
      },
      grabbing: false,
      grabResult: null,
      records: [],
      packetDetail: null
    }
  },
  computed: {
    avatarText() {
      const text = (this.grabForm.userName || '').trim()
      if (!text) return 'U'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    }
  },
  async created() {
    await this.loadUserInfo()
    // 页面加载时自动获取抢红包记录
    if (this.grabForm.userName) {
      this.loadRecords()
    }
  },
  methods: {
    async loadUserInfo() {
      try {
        const user = JSON.parse(localStorage.getItem('user'))
        if (user) {
          this.grabForm.userName = user.userName
          console.log('用户信息加载成功:', this.grabForm.userName)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    async handleGrab() {
      if (!this.grabForm.packetNo) {
        this.$message.warning('请输入红包编号')
        return
      }
      if (!this.grabForm.userName) {
        this.$message.warning('请输入用户名')
        return
      }

      this.grabbing = true
      this.grabResult = null
      try {
        const response = await api.post('/redpacket/grab', null, {
          params: {
            packetNo: this.grabForm.packetNo,
            userName: this.grabForm.userName,
            storageType: this.grabForm.storageType
          }
        })

        this.grabResult = response
        if (response.success) {
          this.$message.success(response.message)
          this.loadRecords()
          this.loadPacketDetail()
        } else {
          this.$message.warning(response.message)
        }
      } catch (error) {
        console.error('抢红包失败:', error)
        this.$message.error('抢红包失败')
      } finally {
        this.grabbing = false
      }
    },
    async loadRecords() {
      if (!this.grabForm.userName) {
        console.warn('用户名为空，无法加载记录')
        return
      }

      try {
        console.log('正在加载用户记录:', this.grabForm.userName)
        // 统一从 MySQL 查询抢红包记录
        const response = await api.get('/redpacket/records', {
          params: { userName: this.grabForm.userName }
        })
        console.log('加载到的记录:', response)
        this.records = response || []
      } catch (error) {
        console.error('加载记录失败:', error)
      }
    },
    async loadPacketDetail() {
      if (!this.grabForm.packetNo) return

      try {
        const response = await api.get('/redpacket/detail/' + this.grabForm.packetNo, {
          params: { storageType: this.grabForm.storageType }
        })
        if (response.success && response.data) {
          const data = response.data
          // 处理 Redis 返回的数据结构（Map<Object, Object>）
          this.packetDetail = {
            totalCount: parseInt(data.totalCount || data['totalCount'] || 0),
            remainCount: parseInt(data.remainCount || data['remainCount'] || 0),
            totalAmount: data.totalAmount || data['totalAmount'] || '0',
            remainAmount: data.remainAmount || data['remainAmount'] || '0'
          }
        } else {
          this.packetDetail = null
          this.$message.warning('红包不存在')
        }
      } catch (error) {
        console.error('加载红包详情失败:', error)
        this.packetDetail = null
      }
    }
  }
}
</script>

<style scoped src="../styles/redpacket.css"></style>
