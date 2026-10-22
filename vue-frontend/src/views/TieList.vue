<template>
  <div class="sd-page my-ties">
    <!-- 页面头部 -->
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">我的帖子</h1>
        <p class="sd-subtitle">共发布 {{ ties.length }} 篇内容</p>
      </div>
      <div class="head-actions">
        <router-link to="/createtie" class="sd-btn sd-btn-primary">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          发新帖
        </router-link>
        <button @click="loadTies" class="sd-btn">
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
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
        </svg>
      </span>
      <span>暂无帖子，快去发布第一篇吧！</span>
      <router-link to="/createtie" class="sd-btn sd-btn-primary">立即发帖</router-link>
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
          </div>

          <h3 class="tie-title">{{ tie.tieTitle }}</h3>
          <p class="tie-content">{{ tie.tieContent }}</p>

          <div v-if="tie.tieImage" class="tie-images">
            <img :src="tie.tieImage" alt="图片" class="tie-image-thumbnail"
                 @click.stop="viewImage(tie.tieImage, $event)" />
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

    <!-- 底部快捷入口 -->
    <div class="quick-links">
      <router-link to="/allties" class="quick-link">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="7" height="7" rx="1.5"/>
          <rect x="14" y="3" width="7" height="7" rx="1.5"/>
          <rect x="3" y="14" width="7" height="7" rx="1.5"/>
          <rect x="14" y="14" width="7" height="7" rx="1.5"/>
        </svg>
        浏览广场
      </router-link>
      <router-link to="/login" class="quick-link">
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
  name: 'TieList',
  data() {
    return {
      ties: [],
      loading: false
    }
  },
  created() {
    this.loadTies()
  },
  methods: {
    async loadTies() {
      try {
        // 从 Session 获取当前用户
        const userResponse = await api.get('/user/current')
        let userName = ''
        if (userResponse.success && userResponse.data) {
          userName = userResponse.data.userName
        }
        
        if (!userName) {
          this.$router.push('/login')
          return
        }
        
        this.loading = true
        const response = await api.get('/tie/list', {
          params: { userName }
        })
        
        // 查询成功，直接返回帖子列表，过滤掉null值
        this.ties = (response || []).filter(tie => tie !== null && tie !== undefined)
        this.loading = false
      } catch (error) {
        console.error('加载帖子失败:', error)
        this.loading = false
      }
    },
    viewDetail(tie) {
      // 假设 tie 对象中有 tieId 字段
      this.$router.push({
        path: '/tiedetail',
        query: {
          tieId: tie.tieId
        }
      })
    },
    viewImage(img, event) {
      // 阻止冒泡，防止触发点击帖子事件
      event.stopPropagation()
      // 可以后续添加图片放大查看功能
      console.log('查看图片:', img)
    },
    isImageFile(path) {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
    },
    avatarText(name) {
      const text = (name || '').trim()
      if (!text) return '匿'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    formatTime(timeStr) {
      if (!timeStr) return '未知时间'
      // 如果是标准格式直接返回
      if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/.test(timeStr)) {
        return timeStr
      }
      // 尝试解析其他格式
      try {
        const date = new Date(timeStr)
        if (!isNaN(date.getTime())) {
          return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
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

.tie-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tie-image-thumbnail {
  width: 92px;
  height: 92px;
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

/* ===== 底部快捷入口 ===== */
.quick-links {
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px solid var(--sd-border-soft);
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--sd-border-soft);
  color: var(--sd-text-3);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}

.quick-link:hover {
  color: var(--sd-primary-dark);
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
  transform: translateY(-1px);
}

.quick-link svg {
  width: 15px;
  height: 15px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .tie-list {
    grid-template-columns: 1fr;
  }
}
</style>
