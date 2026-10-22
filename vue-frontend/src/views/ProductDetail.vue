<template>
  <div class="sd-page product-detail-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">商品详情</h1>
        <p class="sd-subtitle">查看商品完整信息，联系卖家完成交易</p>
      </div>
      <button type="button" class="back-bar" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        返回列表
      </button>
    </div>

    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      加载中...
    </div>

    <template v-else-if="product">
      <!-- 主体：图片 + 信息 -->
      <div class="detail-main">
        <div class="sd-card gallery-card">
          <div v-if="imageList.length" class="gallery">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="gallery-item"
              @click="viewImage(img)"
            >
              <img :src="img" alt="商品图片" />
              <span class="gallery-zoom">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"
                     stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  <line x1="11" y1="8" x2="11" y2="14"/>
                  <line x1="8" y1="11" x2="14" y2="11"/>
                </svg>
              </span>
            </div>
          </div>
          <div v-else class="gallery-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <span>卖家未上传商品图片</span>
          </div>
          <p v-if="imageList.length" class="gallery-tip">点击图片可查看原图</p>
        </div>

        <div class="sd-card info-card">
          <div class="info-tags">
            <span class="sd-tag sd-tag-blue">{{ product.productCondition || '成色未标注' }}</span>
            <span class="sd-tag sd-tag-green">{{ product.category || '未分类' }}</span>
            <span v-if="product.status === 'on_sale'" class="sd-tag sd-tag-amber">在售</span>
            <span v-else class="sd-tag">已下架</span>
          </div>

          <h2 class="info-title">{{ product.productName }}</h2>

          <div class="price-box">
            <span class="price-now">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="price-origin">原价 ¥{{ product.originalPrice }}</span>
            <span v-if="discountText" class="price-discount">{{ discountText }}</span>
          </div>

          <div class="stat-row">
            <div class="stat-cell">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <span class="stat-num">{{ product.viewCount || 0 }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat-cell">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span class="stat-num">{{ product.wantCount || 0 }}</span>
              <span class="stat-label">想要</span>
            </div>
          </div>

          <div class="seller-box">
            <span class="seller-avatar">{{ sellerAvatar }}</span>
            <div class="seller-text">
              <span class="seller-name">{{ product.userName }}</span>
              <span class="seller-time">发布于 {{ formatTime(product.createTime) }}</span>
            </div>
            <span v-if="isOwner" class="sd-tag sd-tag-pink">我发布的</span>
          </div>

          <div class="action-grid">
            <button v-if="!isOwner" class="sd-btn sd-btn-primary action-btn" @click="contactSeller">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              联系卖家
            </button>
            <button
              v-if="!isOwner && product.status === 'on_sale'"
              class="sd-btn buy-btn action-btn"
              :disabled="buying"
              @click="buyNow"
            >
              <span v-if="buying" class="btn-spinner"></span>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <circle cx="9" cy="21" r="1"/>
                <circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
              {{ buying ? '下单中...' : '立即购买' }}
            </button>
            <button v-if="!isOwner" class="sd-btn sd-btn-soft action-btn" @click="wantIt">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              想要
            </button>
            <button class="sd-btn sd-btn-soft action-btn" @click="shareToForum">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              分享到论坛
            </button>
          </div>

          <div v-if="isOwner" class="owner-actions">
            <button class="sd-btn owner-edit" @click="editProduct">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              编辑商品
            </button>
            <button class="sd-btn sd-btn-danger" @click="deleteProduct">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              删除商品
            </button>
          </div>
        </div>
      </div>

      <!-- 描述 -->
      <div class="sd-card desc-card">
        <h3 class="section-title">
          商品描述
        </h3>
        <p class="desc-text">{{ product.productDescription || '卖家没有填写描述' }}</p>
      </div>
    </template>

    <div v-else class="sd-card sd-empty">
      <div class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <p>商品不存在或已被删除</p>
      <button class="sd-btn sd-btn-primary" @click="$router.push('/secondhand')">回到二手市场</button>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'ProductDetail',
  data() {
    return {
      product: null,
      loading: false,
      currentUser: '',
      isOwner: false,
      buying: false
    }
  },
  computed: {
    imageList() {
      if (!this.product?.productImages) return []
      return this.product.productImages.split(',')
    },
    sellerAvatar() {
      const text = (this.product?.userName || '').trim()
      if (!text) return '卖'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    discountText() {
      const price = parseFloat(this.product?.price)
      const origin = parseFloat(this.product?.originalPrice)
      if (isNaN(price) || isNaN(origin) || origin <= 0 || price >= origin) return ''
      const rate = (price / origin) * 10
      if (rate < 0.5) return '超值'
      return `${rate.toFixed(1)} 折`
    }
  },
  async created() {
    await this.getCurrentUser()
    this.loadProduct()
  },
  methods: {
    async getCurrentUser() {
      try {
        const response = await api.get('/user/current')
        if (response.success && response.data) {
          this.currentUser = response.data.userName
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    async loadProduct() {
      try {
        this.loading = true
        const productId = this.$route.query.productId

        const response = await api.get(`/secondhand/detail/${productId}`)
        if (response.success) {
          this.product = response.data
          this.isOwner = this.product.userName === this.currentUser
        } else {
          this.$message.error(response.message || '加载失败')
        }
        this.loading = false
      } catch (error) {
        console.error('加载商品详情失败:', error)
        this.loading = false
        this.$message.error('加载失败：' + (error.response?.data?.message || error.message))
      }
    },

    contactSeller() {
      // 跳转到私信页面，并传递卖家用户名和商品信息
      this.$router.push({
        path: '/chat',
        query: {
          targetUser: this.product.userName,
          productId: this.product.productId,
          productName: this.product.productName
        }
      })
    },

    async buyNow() {
      if (this.buying) return
      try {
        await this.$confirm(`确认购买「${this.product.productName}」吗？`, '确认下单', {
          confirmButtonText: '确认购买',
          cancelButtonText: '再想想',
          type: 'warning'
        })
      } catch (e) {
        return
      }

      this.buying = true
      try {
        const response = await api.post('/order/create', null, {
          params: { productId: this.product.productId }
        })

        if (response.success) {
          this.$message.success('订单创建成功！')
          this.$router.push('/order/my/buy')
          return
        }
        this.$message.error(response.message || '购买失败')
        this.buying = false
      } catch (error) {
        this.$message.error('购买失败：' + (error.response?.data?.message || error.message))
        this.buying = false
      }
    },

    async wantIt() {
      try {
        const response = await api.post(`/secondhand/want/${this.product.productId}`)
        if (response.success) {
          this.$message.success('已加入想要列表')
          this.loadProduct()
        } else {
          this.$message.warning(response.message || '操作失败')
        }
      } catch (error) {
        this.$message.error('操作失败：' + (error.response?.data?.message || error.message))
      }
    },

    editProduct() {
      this.$router.push({
        path: '/secondhand/edit',
        query: { productId: this.product.productId }
      })
    },

    async deleteProduct() {
      try {
        await this.$confirm('确认删除该商品吗？删除后不可恢复', '删除商品', {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch (e) {
        return
      }

      try {
        const response = await api.post(`/secondhand/delete/${this.product.productId}`)
        if (response.success) {
          this.$message.success('删除成功')
          this.$router.back()
        } else {
          this.$message.error(response.message || '删除失败')
        }
      } catch (error) {
        this.$message.error('删除失败：' + (error.response?.data?.message || error.message))
      }
    },

    viewImage(img) {
      window.open(img, '_blank')
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
    },

    shareToForum() {
      // 跳转到发帖页，自动填充商品信息
      this.$router.push({
        path: '/createtie',
        query: {
          productId: this.product.productId,
          productName: this.product.productName,
          autoFill: 'product'
        }
      })
    }
  }
}
</script>

<style scoped>
.product-detail-page {
  max-width: 1060px;
}

/* ===== 返回按钮 ===== */
.back-bar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 15px 7px 11px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--sd-border-soft);
  border-radius: 999px;
  color: var(--sd-text-3);
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-bar:hover {
  color: var(--sd-primary-dark);
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  transform: translateX(-2px);
}

.back-bar svg {
  width: 15px;
  height: 15px;
}

/* ===== 主布局 ===== */
.detail-main {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  gap: 18px;
  margin-bottom: 18px;
  align-items: start;
}

/* ===== 图片区 ===== */
.gallery-card {
  padding: 16px;
}

.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.gallery-item {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: var(--sd-radius);
  overflow: hidden;
  background: var(--sd-bg-input);
  border: 1px solid var(--sd-border-soft);
  cursor: pointer;
}

.gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.gallery-item:hover img {
  transform: scale(1.07);
}

.gallery-zoom {
  position: absolute;
  right: 9px;
  bottom: 9px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.55);
  color: #fff;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.25s ease;
}

.gallery-zoom svg {
  width: 15px;
  height: 15px;
}

.gallery-item:hover .gallery-zoom {
  opacity: 1;
  transform: translateY(0);
}

.gallery-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 220px;
  border-radius: var(--sd-radius);
  border: 1px dashed var(--sd-border);
  background: #fbfcfe;
  color: var(--sd-text-4);
  font-size: 13px;
}

.gallery-empty svg {
  width: 42px;
  height: 42px;
  opacity: 0.55;
}

.gallery-tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--sd-text-4);
  text-align: center;
}

/* ===== 信息区 ===== */
.info-card {
  padding: 22px;
  display: flex;
  flex-direction: column;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 12px;
}

.info-title {
  margin: 0 0 14px;
  font-size: 23px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--sd-text);
  letter-spacing: -0.2px;
}

.price-box {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 14px;
  border-radius: var(--sd-radius);
  background: linear-gradient(135deg, #fff5f5 0%, #fff8f1 100%);
  border: 1px solid #fee2e2;
}

.price-now {
  font-size: 28px;
  font-weight: 800;
  color: #dc2626;
  letter-spacing: -0.5px;
}

.price-origin {
  font-size: 13px;
  color: var(--sd-text-4);
  text-decoration: line-through;
}

.price-discount {
  padding: 2px 9px;
  border-radius: 999px;
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.stat-cell {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 11px 14px;
  border-radius: var(--sd-radius);
  background: var(--sd-bg-input);
  border: 1px solid var(--sd-border-soft);
}

.stat-cell svg {
  width: 16px;
  height: 16px;
  color: var(--sd-text-4);
}

.stat-num {
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.stat-label {
  font-size: 12px;
  color: var(--sd-text-4);
}

.seller-box {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 13px 15px;
  margin-bottom: 18px;
  border-radius: var(--sd-radius);
  background: var(--sd-primary-light);
  border: 1px solid #dbeafe;
}

.seller-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--sd-gradient);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  box-shadow: var(--sd-shadow-primary);
}

.seller-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.seller-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seller-time {
  font-size: 12px;
  color: var(--sd-text-3);
}

/* ===== 操作区 ===== */
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.action-btn {
  width: 100%;
  justify-content: center;
}

.buy-btn {
  background: linear-gradient(135deg, #f97316 0%, #ef4444 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.26);
}

.buy-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #ea580c 0%, #dc2626 100%);
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(239, 68, 68, 0.34);
}

.buy-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

.owner-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px dashed var(--sd-border);
}

.owner-actions .sd-btn {
  width: 100%;
  justify-content: center;
}

.owner-edit {
  border-color: #fde68a;
  color: #b45309;
  background: #fffbeb;
}

.owner-edit:hover {
  background: #fef3c7;
  border-color: #fcd34d;
  transform: translateY(-2px);
}

/* ===== 描述区 ===== */
.desc-card {
  padding: 22px 24px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.desc-text {
  margin: 0;
  font-size: 14.5px;
  line-height: 1.85;
  color: var(--sd-text-2);
  white-space: pre-wrap;
  word-break: break-word;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .detail-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .info-title {
    font-size: 20px;
  }

  .action-grid,
  .stat-row,
  .owner-actions {
    grid-template-columns: 1fr;
  }

  .gallery {
    grid-template-columns: 1fr;
  }
}
</style>
