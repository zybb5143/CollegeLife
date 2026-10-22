<template>
  <div class="sd-page all-ties">
    <!-- 页面头部 -->
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">论坛广场</h1>
        <p class="sd-subtitle">
          {{ isSearching ? '搜索结果' : '全部内容' }} · 共 {{ ties.length }} 条
        </p>
      </div>
      <div class="head-actions">
        <router-link to="/checkin" class="sd-btn sd-btn-soft">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
            <path d="M9 16l2 2 4-4"/>
          </svg>
          每日签到
        </router-link>
        <router-link v-if="userRole === 'boss' || userRole === 'admin'" to="/boss-dashboard"
                     class="sd-btn sd-btn-primary">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          管理中心
        </router-link>
        <router-link to="/netdisk" class="sd-btn">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
          资源库
        </router-link>
        <button @click="loadAllTies" class="sd-btn" title="刷新列表">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          刷新
        </button>
      </div>
    </div>

    <!-- 后端服务器提示 -->
    <div v-if="serverAddress" class="sd-server-bar">
      <span class="sd-server-dot"></span>
      <span>实际处理请求的后端服务器：</span>
      <span class="sd-mono">{{ serverAddress }}</span>
    </div>

    <!-- 搜索区 -->
    <div class="sd-card search-card">
      <div class="sd-input-wrap search-wrap">
        <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input v-model="searchKeyword" @keyup.enter="handleSmartSearch"
               placeholder="搜索帖子标题、内容或作者，支持智能匹配" class="sd-input search-input" />
      </div>
      <div class="search-actions">
        <button @click="handleSmartSearch" class="sd-btn sd-btn-primary">搜索</button>
        <button v-if="isSearching" @click="loadAllTies" class="sd-btn">重置</button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="ties.length === 0" class="sd-card sd-empty">
      <span class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </span>
      <span>{{ isSearching ? '没有找到匹配的帖子' : '暂无帖子' }}</span>
      <router-link to="/createtie" class="sd-btn sd-btn-primary">
        发布第一篇帖子
      </router-link>
    </div>

    <!-- 帖子列表 -->
    <div v-else class="tie-list">
      <div v-for="(tie, index) in ties" :key="index" class="tie-card" @click="viewDetail(tie)">
        <div v-if="!tie" class="tie-invalid">无效帖子</div>
        <template v-else>
          <div class="tie-top">
            <span class="tie-avatar">{{ avatarText(tie.userName) }}</span>
            <div class="tie-meta">
              <span class="tie-author">{{ tie.userName || '匿名用户' }}</span>
              <span class="tie-time">{{ formatTime(tie.tieCreateTime) }}</span>
            </div>
            <span class="sd-tag" :class="tie.tieType === 'product' ? 'sd-tag-pink' : 'sd-tag-blue'">
              {{ tie.tieType === 'product' ? '商品' : '帖子' }}
            </span>
          </div>

          <h3 class="tie-title">{{ tie.tieTitle }}</h3>

          <div v-if="tie.tieType === 'product' && tie.productId" class="product-card"
               @click.stop="goToProduct(tie.productId)">
            <div class="product-name">{{ tie.tieContent }}</div>
            <div class="product-hint">
              点击查看商品详情
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </div>
          </div>
          <p v-else class="tie-content">{{ tie.tieContent }}</p>

          <div v-if="tie.tieImage && !tie.tieImage.includes('|PENDING')" class="tie-images">
            <img v-if="isImageFile(tie.tieImage)" :src="tie.tieImage" alt="图片"
                 class="tie-image-thumbnail" @click.stop="viewImage(tie.tieImage, $event)" />
            <a v-else :href="getDownloadUrl(tie.tieImage)" class="file-download" target="_blank"
               @click.stop>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="7 10 12 15 17 10"/>
                <line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              下载附件
            </a>
          </div>

          <div class="tie-foot">
            <span class="tie-more">
              查看详情
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </span>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'AllTies',
  data() {
    return { ties: [], loading: false, userRole: '', searchKeyword: '', isSearching: false, serverAddress: '' }
  },
  created() { this.loadAllTies(); this.loadUserRole() },
  methods: {
    async loadAllTies() {
      try {
        this.loading = true; this.isSearching = false
        const response = await api.get('/tie/all')
        // 后端 /tie/all 现返回 { success, port, address, data:[...] }，端口随帖子一起返回
        this.ties = ((response && response.data) || []).filter(t => t !== null && t !== undefined)
        if (response && response.address) this.serverAddress = response.address
        this.loading = false
      } catch (error) { console.error('加载失败:', error); this.loading = false }
    },
    async handleSmartSearch() {
      if (!this.searchKeyword.trim()) return
      try {
        this.loading = true; this.isSearching = true
        const response = await api.get('/tie/smart-search', { params: { keyword: this.searchKeyword } })
        this.ties = (response || []).filter(t => t !== null && t !== undefined)
        this.loading = false
      } catch (error) { console.error('搜索失败:', error); this.loading = false }
    },
    async loadUserRole() {
      try { const user = JSON.parse(localStorage.getItem('user')); if (user) this.userRole = user.role } catch (e) {}
    },
    viewDetail(tie) {
      if (!tie || !tie.tieId) return
      this.$router.push({ path: '/tiedetail', query: { tieId: tie.tieId } })
    },
    viewImage(img, event) { event.stopPropagation() },
    isImageFile(path) {
      if (!path) return false
      return ['jpg','jpeg','png','gif','bmp','webp','svg'].includes(path.split('.').pop().toLowerCase())
    },
    getFileName(path) { return path ? path.split('/').pop() : '' },
    getDownloadUrl(path) { return path ? `/api/download/download/${this.getFileName(path)}` : '' },
    avatarText(name) {
      const text = (name || '').trim()
      if (!text) return '匿'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/.test(timeStr)) return timeStr
      try { const d = new Date(timeStr); if (!isNaN(d.getTime())) return d.toLocaleString('zh-CN') } catch (e) {}
      return timeStr
    },
    goToProduct(productId) { this.$router.push({ path: '/secondhand/detail', query: { productId } }) }
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

/* ===== 搜索卡片 ===== */
.search-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  margin-bottom: 18px;
}

.search-wrap {
  flex: 1;
  min-width: 0;
}

.search-input {
  border-radius: 999px;
  padding: 12px 16px 12px 40px;
  background: #f5f7fb;
}

.search-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ===== 帖子列表 ===== */
.tie-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 16px;
}

.tie-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 18px 20px 14px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius-lg);
  box-shadow: var(--sd-shadow-sm);
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.tie-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--sd-gradient);
  opacity: 0;
  transition: opacity 0.22s ease;
}

.tie-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--sd-shadow-md);
  border-color: #dbeafe;
}

.tie-card:hover::before {
  opacity: 1;
}

.tie-invalid {
  padding: 20px 0;
  text-align: center;
  color: var(--sd-text-4);
  font-size: 14px;
}

.tie-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.tie-avatar {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.tie-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.tie-author {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tie-time {
  margin-top: 2px;
  font-size: 11.5px;
  color: var(--sd-text-4);
}

.tie-title {
  font-size: 16px;
  font-weight: 650;
  color: var(--sd-text);
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tie-content {
  color: var(--sd-text-3);
  font-size: 13.5px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-card {
  padding: 13px 15px;
  background: linear-gradient(135deg, #fdf2f8, #fff1f2);
  border: 1px solid #fbcfe8;
  border-radius: var(--sd-radius);
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.product-card:hover {
  box-shadow: 0 4px 14px rgba(190, 24, 93, 0.12);
  transform: translateY(-1px);
}

.product-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #9d174d;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  font-size: 12px;
  color: #be185d;
  opacity: 0.8;
}

.product-hint svg {
  width: 13px;
  height: 13px;
}

.tie-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tie-image-thumbnail {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: var(--sd-radius-sm);
  cursor: pointer;
  border: 1px solid var(--sd-border);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.tie-image-thumbnail:hover {
  transform: scale(1.04);
  box-shadow: var(--sd-shadow);
}

.file-download {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 15px;
  background: #f1f5f9;
  border: 1px solid var(--sd-border);
  color: var(--sd-text-2);
  border-radius: 999px;
  text-decoration: none;
  font-size: 12.5px;
  transition: all 0.2s ease;
}

.file-download:hover {
  background: var(--sd-primary-light);
  border-color: #bfdbfe;
  color: var(--sd-primary-dark);
}

.file-download svg {
  width: 14px;
  height: 14px;
}

.tie-foot {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px dashed var(--sd-border-soft);
  display: flex;
  justify-content: flex-end;
}

.tie-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sd-text-4);
  transition: color 0.2s ease, gap 0.2s ease;
}

.tie-more svg {
  width: 14px;
  height: 14px;
}

.tie-card:hover .tie-more {
  color: var(--sd-primary-dark);
  gap: 7px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .search-card {
    flex-direction: column;
    align-items: stretch;
  }

  .search-actions {
    justify-content: flex-end;
  }

  .tie-list {
    grid-template-columns: 1fr;
  }
}
</style>
