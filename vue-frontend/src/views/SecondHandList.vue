<template>
  <div class="sd-page secondhand-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">二手市场</h1>
        <p class="sd-subtitle">共 {{ products.length }} 件闲置好物在校内流转</p>
      </div>
      <div class="head-actions">
        <router-link to="/secondhand/create" class="sd-btn sd-btn-primary">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          发布闲置
        </router-link>
        <button @click="loadProducts" class="sd-btn">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          刷新列表
        </button>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-nav">
      <router-link to="/secondhand/my" class="quick-nav-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 7h-9"/><path d="M14 17H5"/>
          <circle cx="17" cy="17" r="3"/><circle cx="7" cy="7" r="3"/>
        </svg>
        我的闲置
      </router-link>
      <router-link to="/order/my/buy" class="quick-nav-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
        我买到的
      </router-link>
      <router-link to="/order/my/sell" class="quick-nav-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="1" x2="12" y2="23"/>
          <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
        </svg>
        我卖出的
      </router-link>
      <router-link to="/allties" class="quick-nav-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        返回首页
      </router-link>
    </div>

    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      <span>加载中...</span>
    </div>

    <div v-else-if="products.length === 0" class="sd-card sd-empty">
      <span class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
      </span>
      <span>暂无商品，快去发布您的第一个闲置物品吧！</span>
      <router-link to="/secondhand/create" class="sd-btn sd-btn-primary">发布闲置物品</router-link>
    </div>

    <div v-else class="product-grid">
      <div v-for="product in products" :key="product.productId" class="product-card"
           @click="viewDetail(product)">
        <div class="product-image-wrapper">
          <img v-if="product.productImages" :src="getFirstImage(product.productImages)"
               alt="商品图片" class="product-image" @click.stop />
          <div v-else class="no-image">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <span>暂无图片</span>
          </div>
          <span v-if="product.productCondition" class="condition-badge">
            {{ product.productCondition }}
          </span>
        </div>

        <div class="product-info">
          <h3 class="product-name">{{ product.productName }}</h3>
          <p class="product-desc">{{ truncateText(product.productDescription, 50) || '卖家很懒，没有填写描述' }}</p>

          <div class="product-meta">
            <span class="sd-tag sd-tag-blue">{{ product.category || '未分类' }}</span>
          </div>

          <div class="product-footer">
            <span class="product-price">¥{{ product.price }}</span>
            <span class="product-stats">
              <span class="stat">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                {{ product.viewCount }}
              </span>
              <span class="stat">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                {{ product.wantCount }}
              </span>
            </span>
          </div>

          <div class="product-seller">
            <span class="seller-avatar">{{ avatarText(product.userName) }}</span>
            <span class="seller-name">{{ product.userName }}</span>
            <span class="seller-time">{{ formatTime(product.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'SecondHandList',
  data() {
    return {
      products: [],
      loading: false
    }
  },
  created() {
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      try {
        this.loading = true
        const response = await api.get('/secondhand/list')
        
        if (response.success) {
          this.products = response.data || []
        } else {
          this.$message.error(response.message || '加载失败')
        }
        this.loading = false
      } catch (error) {
        console.error('加载商品失败:', error)
        this.loading = false
        this.$message.error('加载失败：' + (error.response?.data?.message || error.message))
      }
    },
    
    viewDetail(product) {
      this.$router.push({
        path: '/secondhand/detail',
        query: {
          productId: product.productId
        }
      })
    },
    
    getFirstImage(images) {
      if (!images) return ''
      const imageArray = images.split(',')
      return imageArray[0]
    },
    
    truncateText(text, maxLength) {
      if (!text) return ''
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },
    
    avatarText(name) {
      const text = (name || '').trim()
      if (!text) return '卖'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
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
.btn-icon {
  width: 15px;
  height: 15px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* ===== 快捷入口 ===== */
.quick-nav {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.quick-nav-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--sd-border-soft);
  border-radius: 999px;
  color: var(--sd-text-3);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  box-shadow: var(--sd-shadow-sm);
  transition: all 0.2s ease;
}

.quick-nav-item:hover {
  color: var(--sd-primary-dark);
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  transform: translateY(-2px);
  box-shadow: var(--sd-shadow);
}

.quick-nav-item svg {
  width: 15px;
  height: 15px;
}

/* ===== 商品网格 ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 18px;
}

.product-card {
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius-lg);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--sd-shadow-sm);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--sd-shadow-md);
  border-color: #dbeafe;
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: linear-gradient(135deg, #f1f5f9, #e8eef7);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #b6c2d2;
  font-size: 12.5px;
}

.no-image svg {
  width: 32px;
  height: 32px;
}

.condition-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(6px);
  color: var(--sd-text-2);
  font-size: 11.5px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.14);
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 15px 16px 14px;
}

.product-name {
  font-size: 15.5px;
  font-weight: 650;
  color: var(--sd-text);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-desc {
  font-size: 12.5px;
  color: var(--sd-text-4);
  line-height: 1.65;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 2px;
}

.product-price {
  font-size: 21px;
  font-weight: 800;
  color: #ef4444;
  letter-spacing: -0.4px;
  font-variant-numeric: tabular-nums;
}

.product-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--sd-text-4);
  font-variant-numeric: tabular-nums;
}

.stat svg {
  width: 13px;
  height: 13px;
}

.product-seller {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-top: 11px;
  margin-top: 2px;
  border-top: 1px dashed var(--sd-border-soft);
}

.seller-avatar {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #93c5fd, #a5b4fc);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.seller-name {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sd-text-2);
  max-width: 88px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seller-time {
  margin-left: auto;
  font-size: 11.5px;
  color: var(--sd-text-4);
  white-space: nowrap;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }

  .product-info {
    padding: 12px 13px;
  }

  .product-price {
    font-size: 18px;
  }

  .seller-time {
    display: none;
  }
}
</style>
