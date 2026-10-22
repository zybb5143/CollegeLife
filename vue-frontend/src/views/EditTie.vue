<template>
  <div class="sd-page edit-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">编辑帖子</h1>
        <p class="sd-subtitle">修改标题与正文后点击保存即可生效</p>
      </div>
      <button class="back-bar" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        返回详情
      </button>
    </div>

    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      <span>加载中...</span>
    </div>

    <div v-else-if="!tie" class="sd-card sd-empty">
      <span class="sd-empty-icon empty-icon-danger">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </span>
      <span class="empty-text-danger">帖子不存在或您没有权限编辑</span>
      <router-link to="/allties" class="sd-btn sd-btn-primary">返回论坛广场</router-link>
    </div>

    <form v-else class="sd-card edit-card" @submit.prevent="handleSubmit">
      <div class="sd-field">
        <label class="sd-label">标题</label>
        <div class="sd-input-wrap">
          <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 7V4h16v3"/>
            <path d="M9 20h6"/>
            <path d="M12 4v16"/>
          </svg>
          <input type="text" class="sd-input" v-model="formData.tieTitle"
                 placeholder="请输入标题" maxlength="200" />
        </div>
        <p class="char-count">{{ (formData.tieTitle || '').length }} / 200</p>
      </div>

      <div class="sd-field">
        <label class="sd-label">正文内容</label>
        <textarea v-model="formData.tieContent" class="sd-textarea content-area"
                  placeholder="请输入内容" rows="12"></textarea>
        <p class="char-count">{{ (formData.tieContent || '').length }} 字</p>
      </div>

      <div class="edit-foot">
        <button type="button" @click="goBack" class="sd-btn">取消</button>
        <button type="submit" :disabled="submitting" class="sd-btn sd-btn-primary sd-btn-lg submit-btn">
          <span v-if="submitting" class="btn-spinner"></span>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/>
            <polyline points="7 3 7 8 15 8"/>
          </svg>
          {{ submitting ? '保存中...' : '保存修改' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'EditTie',
  data() {
    return {
      tie: null,
      loading: false,
      submitting: false,
      formData: {
        tieId: null,
        tieTitle: '',
        tieContent: ''
      }
    }
  },
  created() {
    this.loadTieDetail()
  },
  methods: {
    async loadTieDetail() {
      try {
        const tieId = this.$route.query.tieId
        if (!tieId) {
          this.$router.push('/allties')
          return
        }
        
        this.loading = true
        const response = await api.get('/tie/detail', { params: { tieId } })
        
        // 检查登录状态
        const userResponse = await api.get('/user/current')
        if (!userResponse.success || !userResponse.data) {
          this.$message.warning('请先登录')
          this.$router.push('/login')
          return
        }
        
        const currentUser = userResponse.data.userName
        
        // 检查是否为作者
        if (response.userName !== currentUser) {
          this.$message.error('您没有权限编辑此帖子')
          this.$router.back()
          return
        }
        
        this.tie = response
        this.formData.tieId = tieId
        this.formData.tieTitle = response.tieTitle
        this.formData.tieContent = response.tieContent
        this.loading = false
      } catch (error) {
        console.error('加载失败:', error)
        this.loading = false
        this.$message.error('加载失败：' + (error.message || '请稍后重试'))
      }
    },
    
    async handleSubmit() {
      try {
        if (!this.formData.tieTitle || !this.formData.tieTitle.trim()) {
          this.$message.warning('标题不能为空')
          return
        }
        
        if (!this.formData.tieContent || !this.formData.tieContent.trim()) {
          this.$message.warning('内容不能为空')
          return
        }
        
        this.submitting = true
        
        const response = await api.post('/tie/edit', {
          tieId: this.formData.tieId,
          tieTitle: this.formData.tieTitle.trim(),
          tieContent: this.formData.tieContent.trim()
        })
        
        if (response.success) {
          this.$message.success('修改成功！')
          this.$router.push({
            path: '/tiedetail',
            query: { tieId: this.formData.tieId }
          })
        } else {
          this.$message.error(response.message || '修改失败')
        }
        
        this.submitting = false
      } catch (error) {
        console.error('修改失败:', error)
        this.submitting = false
        this.$message.error('修改失败：' + (error.response?.data?.message || error.message))
      }
    },
    
    goBack() {
      if (this.tie) {
        this.$router.push({
          path: '/tiedetail',
          query: { tieId: this.tie.tieId }
        })
      } else {
        this.$router.back()
      }
    }
  }
}
</script>

<style scoped>
.edit-page {
  max-width: 800px;
}

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

.edit-card {
  padding: 28px 30px;
}

.content-area {
  border-radius: var(--sd-radius);
  min-height: 260px;
}

.char-count {
  margin-top: 7px;
  text-align: right;
  font-size: 12px;
  color: var(--sd-text-4);
  font-variant-numeric: tabular-nums;
}

.empty-icon-danger {
  background: #fef2f2;
  color: #f87171;
}

.empty-text-danger {
  color: #dc2626;
  font-weight: 500;
}

.edit-foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
  margin-top: 4px;
  border-top: 1px solid var(--sd-border-soft);
}

.submit-btn {
  min-width: 138px;
  letter-spacing: 0.6px;
}

.submit-btn svg {
  width: 16px;
  height: 16px;
}

.btn-spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

@media (max-width: 640px) {
  .edit-card {
    padding: 20px 18px;
  }

  .edit-foot .sd-btn:not(.submit-btn) {
    display: none;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>
