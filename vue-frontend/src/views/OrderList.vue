<template>
  <div class="sd-page order-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">{{ orderType === 'buy' ? '我买到的商品' : '我卖出的商品' }}</h1>
        <p class="sd-subtitle">共 {{ orders.length }} 条订单记录</p>
      </div>
      <div class="head-actions">
        <button class="sd-btn" @click="loadOrders" :disabled="loading">
          <svg class="btn-icon" :class="{ spinning: loading }" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          刷新列表
        </button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div v-if="orders.length" class="sum-grid">
      <div v-for="sum in summary" :key="sum.label" class="sum-card" :class="sum.tone">
        <span class="sum-num">{{ sum.value }}</span>
        <span class="sum-label">{{ sum.label }}</span>
      </div>
    </div>

    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      加载中...
    </div>

    <div v-else-if="orders.length === 0" class="sd-card sd-empty">
      <div class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="9" cy="21" r="1"/>
          <circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
      </div>
      <p>暂无订单记录</p>
      <button class="sd-btn sd-btn-primary" @click="$router.push('/secondhand')">去二手市场逛逛</button>
    </div>

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.orderId" class="sd-card order-card">
        <div class="order-head">
          <span class="order-no">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            订单号 {{ order.orderNo }}
          </span>
          <span class="order-status" :class="statusClass(order.status)">
            <i class="status-dot"></i>
            {{ getStatusText(order.status) }}
          </span>
        </div>

        <div class="order-body">
          <div class="order-thumb">
            <img v-if="order.productImage" :src="order.productImage" alt="商品图片" />
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
          </div>

          <div class="order-info">
            <h3 class="order-name">{{ order.productName }}</h3>
            <div class="order-meta">
              <span class="meta-chip">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                {{ orderType === 'buy' ? `卖家：${order.sellerName || '—'}` : `买家：${order.buyerName || '—'}` }}
              </span>
              <span class="meta-chip">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                {{ formatTime(order.createTime) }}
              </span>
            </div>
          </div>

          <div class="order-price">¥{{ order.price }}</div>
        </div>

        <div v-if="hasActions(order)" class="order-actions">
          <button
            v-if="orderType === 'buy' && order.status === 'pending_payment'"
            class="sd-btn sd-btn-primary pay-btn"
            @click="payOrder(order.orderId)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
            立即支付
          </button>
          <button
            v-if="orderType === 'buy' && order.status === 'paid'"
            class="sd-btn confirm-btn"
            @click="confirmReceipt(order.orderId)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            确认收货
          </button>
          <button
            v-if="order.status === 'pending_payment'"
            class="sd-btn sd-btn-danger"
            @click="cancelOrder(order.orderId)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
            取消订单
          </button>
        </div>
      </div>
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
  name: 'OrderList',
  data() {
    return {
      orders: [],
      loading: false,
      orderType: 'buy' // 'buy' 或 'sell'
    }
  },
  computed: {
    summary() {
      const count = (s) => this.orders.filter(o => o.status === s).length
      const total = this.orders.reduce((sum, o) => sum + (parseFloat(o.price) || 0), 0)
      return [
        { label: '全部订单', value: this.orders.length, tone: 'sum-blue' },
        { label: '待支付', value: count('pending_payment'), tone: 'sum-amber' },
        { label: '已支付', value: count('paid'), tone: 'sum-indigo' },
        {
          label: this.orderType === 'buy' ? '累计支出' : '累计收入',
          value: `¥${total.toFixed(2)}`,
          tone: 'sum-green'
        }
      ]
    }
  },
  created() {
    // 从路由判断是买入还是卖出订单
    const path = this.$route.path
    if (path.includes('/sell')) {
      this.orderType = 'sell'
    }
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        this.loading = true
        const endpoint = this.orderType === 'buy' ? '/order/my/buy' : '/order/my/sell'
        const response = await api.get(endpoint)

        if (response.success) {
          this.orders = response.data || []
        } else {
          this.$message.error(response.message || '加载失败')
        }
        this.loading = false
      } catch (error) {
        console.error('加载订单失败:', error)
        this.loading = false
        this.$message.error('加载失败：' + (error.response?.data?.message || error.message))
      }
    },

    getStatusText(status) {
      const statusMap = {
        'pending_payment': '待支付',
        'paid': '已支付',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return statusMap[status] || status
    },

    statusClass(status) {
      const classMap = {
        'pending_payment': 'status-amber',
        'paid': 'status-blue',
        'completed': 'status-green',
        'cancelled': 'status-gray'
      }
      return classMap[status] || 'status-gray'
    },

    hasActions(order) {
      if (this.orderType === 'buy' && (order.status === 'pending_payment' || order.status === 'paid')) return true
      return order.status === 'pending_payment'
    },

    async payOrder(orderId) {
      try {
        await this.$confirm('确认支付该订单吗？', '支付订单', {
          confirmButtonText: '确认支付',
          cancelButtonText: '再想想',
          type: 'warning'
        })
      } catch (e) {
        return
      }

      try {
        const response = await api.post(`/order/pay/${orderId}`)
        if (response.success) {
          this.$message.success('支付成功！')
          this.loadOrders()
        } else {
          this.$message.error(response.message || '支付失败')
        }
      } catch (error) {
        this.$message.error('支付失败：' + (error.response?.data?.message || error.message))
      }
    },

    async confirmReceipt(orderId) {
      try {
        await this.$confirm('确认已收到商品吗？', '确认收货', {
          confirmButtonText: '已收到',
          cancelButtonText: '取消',
          type: 'success'
        })
      } catch (e) {
        return
      }

      try {
        const response = await api.post(`/order/confirm/${orderId}`)
        if (response.success) {
          this.$message.success('确认收货成功！')
          this.loadOrders()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        this.$message.error('操作失败：' + (error.response?.data?.message || error.message))
      }
    },

    async cancelOrder(orderId) {
      let reason = ''
      try {
        const { value } = await this.$prompt('请输入取消原因（可选）', '取消订单', {
          confirmButtonText: '确认取消订单',
          cancelButtonText: '返回',
          inputType: 'textarea',
          inputPlaceholder: '例如：与卖家协商后取消',
          inputValidator: () => true
        })
        reason = value || ''
      } catch (e) {
        return
      }

      try {
        const response = await api.post(`/order/cancel/${orderId}`, null, {
          params: { reason: reason || '' }
        })
        if (response.success) {
          this.$message.success('取消成功！')
          this.loadOrders()
        } else {
          this.$message.error(response.message || '取消失败')
        }
      } catch (error) {
        this.$message.error('取消失败：' + (error.response?.data?.message || error.message))
      }
    },

    formatTime(timeStr) {
      if (!timeStr) return '未知时间'
      if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/.test(timeStr)) {
        return timeStr
      }
      try {
        const date = new Date(timeStr)
        if (!isNaN(date.getTime())) {
          return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          })
        }
      } catch (e) {}
      return '无效时间'
    }
  }
}
</script>

<style scoped>
.order-page {
  max-width: 900px;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.btn-icon {
  width: 15px;
  height: 15px;
}

.btn-icon.spinning {
  animation: sd-spin 0.9s linear infinite;
}

/* ===== 统计概览 ===== */
.sum-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}

.sum-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 12px;
  border-radius: var(--sd-radius);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--sd-border-soft);
  box-shadow: var(--sd-shadow-sm);
  transition: all 0.25s ease;
}

.sum-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--sd-shadow-md);
}

.sum-num {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.sum-label {
  font-size: 12px;
  color: var(--sd-text-4);
  font-weight: 500;
}

.sum-blue .sum-num { color: var(--sd-primary); }
.sum-amber .sum-num { color: #d97706; }
.sum-indigo .sum-num { color: var(--sd-accent); }
.sum-green .sum-num { color: var(--sd-success); }

/* ===== 订单卡片 ===== */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  padding: 0;
  overflow: hidden;
  transition: all 0.25s ease;
}

.order-card:hover {
  box-shadow: var(--sd-shadow-md);
  transform: translateY(-2px);
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px;
  background: linear-gradient(90deg, #f8fafc 0%, #ffffff 100%);
  border-bottom: 1px solid var(--sd-border-soft);
}

.order-no {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: var(--sd-text-3);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-no svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: var(--sd-text-4);
}

/* 状态标签 */
.order-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding: 4px 12px 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-amber {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-blue {
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  border: 1px solid #bfdbfe;
}

.status-green {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.status-gray {
  background: #f8fafc;
  color: var(--sd-text-3);
  border: 1px solid var(--sd-border);
}

/* 主体 */
.order-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
}

.order-thumb {
  width: 74px;
  height: 74px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--sd-radius);
  overflow: hidden;
  background: var(--sd-bg-input);
  border: 1px solid var(--sd-border-soft);
  color: var(--sd-text-4);
}

.order-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-thumb svg {
  width: 28px;
  height: 28px;
  opacity: 0.5;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-name {
  margin: 0 0 8px;
  font-size: 15.5px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--sd-text-3);
}

.meta-chip svg {
  width: 13px;
  height: 13px;
  color: var(--sd-text-4);
}

.order-price {
  flex-shrink: 0;
  font-size: 20px;
  font-weight: 800;
  color: #dc2626;
  letter-spacing: -0.4px;
}

/* 操作区 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 18px;
  border-top: 1px dashed var(--sd-border);
  background: #fcfdff;
}

.pay-btn {
  background: linear-gradient(135deg, #f97316 0%, #ef4444 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 5px 14px rgba(239, 68, 68, 0.24);
}

.pay-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 9px 20px rgba(239, 68, 68, 0.32);
}

.confirm-btn {
  border-color: #a7f3d0;
  color: #047857;
  background: #ecfdf5;
}

.confirm-btn:hover {
  background: #d1fae5;
  border-color: #6ee7b7;
  transform: translateY(-2px);
}

/* ===== 快捷入口 ===== */
.quick-nav {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
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
@media (max-width: 768px) {
  .sum-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .order-body {
    flex-wrap: wrap;
  }

  .order-price {
    width: 100%;
    text-align: right;
  }

  .order-actions {
    flex-direction: column;
  }

  .order-actions .sd-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
