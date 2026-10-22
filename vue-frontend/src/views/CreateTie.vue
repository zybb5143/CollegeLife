<template>
  <div class="sd-page create-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">发布帖子</h1>
        <p class="sd-subtitle">分享你的观点、资料或好物，让校园生活更精彩</p>
      </div>
    </div>

    <form class="sd-card create-card" @submit.prevent="handleCreateTie">
      <div class="sd-field">
        <label class="sd-label">帖子标题</label>
        <div class="sd-input-wrap">
          <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 7V4h16v3"/>
            <path d="M9 20h6"/>
            <path d="M12 4v16"/>
          </svg>
          <input type="text" class="sd-input" v-model="tieForm.tieTitle"
                 placeholder="一句话概括你想说的内容" required />
        </div>
        <p class="field-tip">{{ tieForm.tieTitle.length }} / 60 字</p>
      </div>

      <div class="sd-field">
        <label class="sd-label">正文内容</label>
        <textarea v-model="tieForm.tieContent" class="sd-textarea content-area"
                  placeholder="请输入内容，支持换行排版..." rows="8" required></textarea>
        <p class="field-tip">{{ tieForm.tieContent.length }} 字</p>
      </div>

      <div class="sd-field">
        <label class="sd-label">附件（可选）</label>
        <label class="file-drop" :class="{ 'file-drop-filled': selectedFile }">
          <input type="file" class="file-input-hidden" @change="handleFileChange" />
          <template v-if="selectedFile">
            <span class="file-drop-icon file-drop-icon-filled">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </span>
            <span class="file-drop-text">
              <span class="file-drop-name">{{ selectedFile.name }}</span>
              <span class="file-drop-size">{{ formatSize(selectedFile.size) }} · 点击可重新选择</span>
            </span>
          </template>
          <template v-else>
            <span class="file-drop-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </span>
            <span class="file-drop-text">
              <span class="file-drop-name">点击上传图片或附件</span>
              <span class="file-drop-size">图片将直接预览，其他文件提供下载入口</span>
            </span>
          </template>
        </label>
      </div>

      <div class="sd-field">
        <label class="sd-label">帖子类型</label>
        <div class="type-selector">
          <label class="type-option" :class="{ 'type-option-active': tieForm.tieType === 'academic' }">
            <input type="radio" v-model="tieForm.tieType" value="academic" />
            <span class="type-option-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </span>
            <span class="type-option-text">
              <span class="type-option-title">帖子</span>
              <span class="type-option-desc">分享观点与校园日常</span>
            </span>
          </label>

          <label class="type-option" :class="{ 'type-option-active': tieForm.tieType === 'product' }">
            <input type="radio" v-model="tieForm.tieType" value="product" />
            <span class="type-option-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <circle cx="9" cy="21" r="1"/>
                <circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
            </span>
            <span class="type-option-text">
              <span class="type-option-title">商品</span>
              <span class="type-option-desc">推荐好物，可进入二手市场</span>
            </span>
          </label>
        </div>
      </div>

      <transition name="hint-fade">
        <div v-if="tieForm.tieType === 'product'" class="product-hint">
          <span class="product-hint-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </span>
          <div>
            <p class="product-title">
              商品名称：<span>{{ tieForm.tieTitle.replace('【商品分享】', '') || '待填写' }}</span>
            </p>
            <small>在下方填写推荐理由和使用心得</small>
          </div>
        </div>
      </transition>

      <div class="form-foot">
        <router-link to="/allties" class="sd-btn">取消</router-link>
        <button type="submit" :disabled="submitting" class="sd-btn sd-btn-primary sd-btn-lg submit-btn">
          <span v-if="submitting" class="btn-spinner"></span>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"/>
            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          {{ submitting ? '发布中...' : '立即发布' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'CreateTie',
  data() {
    return { tieForm: { tieTitle:'', tieContent:'', tieType:'academic', tieImage:'' }, selectedFile:null, submitting:false }
  },
  methods: {
    handleFileChange(e) { this.selectedFile = e.target.files[0] || null },
    formatSize(size) {
      if (!size && size !== 0) return ''
      if (size < 1024) return size + ' B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
      return (size / 1024 / 1024).toFixed(2) + ' MB'
    },
    async handleCreateTie() {
      this.submitting = true
      try {
        const fd = new FormData()
        fd.append('tieTitle', this.tieForm.tieTitle)
        fd.append('tieContent', this.tieForm.tieContent)
        fd.append('tieType', this.tieForm.tieType)
        if (this.selectedFile) fd.append('file', this.selectedFile)
        const r = await api.post('/tie/create', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
        if (r.success) { this.$message.success(r.message || '发布成功'); this.$router.push('/allties') }
        else this.$message.error(r.message || '发布失败')
      } catch (e) { this.$message.error('发布失败') }
      finally { this.submitting = false }
    }
  }
}
</script>

<style scoped>
.create-page {
  max-width: 720px;
}

.create-card {
  padding: 28px 30px;
}

.field-tip {
  margin-top: 7px;
  font-size: 12px;
  color: var(--sd-text-4);
  text-align: right;
}

.content-area {
  border-radius: var(--sd-radius);
  min-height: 170px;
}

/* ===== 文件上传区 ===== */
.file-drop {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border: 1.5px dashed #cbd5e1;
  border-radius: var(--sd-radius);
  background: #fbfcfe;
  cursor: pointer;
  transition: all 0.2s ease;
}

.file-drop:hover {
  border-color: var(--sd-primary);
  background: var(--sd-primary-light);
}

.file-drop-filled {
  border-style: solid;
  border-color: #bfdbfe;
  background: var(--sd-primary-light);
}

.file-input-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.file-drop-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  background: #f1f5f9;
  color: var(--sd-text-4);
  transition: all 0.2s ease;
}

.file-drop:hover .file-drop-icon {
  background: #fff;
  color: var(--sd-primary);
}

.file-drop-icon-filled {
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.file-drop-icon svg {
  width: 19px;
  height: 19px;
}

.file-drop-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.file-drop-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-drop-size {
  font-size: 12px;
  color: var(--sd-text-4);
}

/* ===== 类型选择 ===== */
.type-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.type-option {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1.5px solid var(--sd-border);
  border-radius: var(--sd-radius);
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-option:hover {
  border-color: #bfdbfe;
  background: #fbfdff;
}

.type-option input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.type-option-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f1f5f9;
  color: var(--sd-text-3);
  transition: all 0.2s ease;
}

.type-option-icon svg {
  width: 18px;
  height: 18px;
}

.type-option-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.type-option-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--sd-text);
}

.type-option-desc {
  font-size: 12px;
  color: var(--sd-text-4);
}

.type-option-active {
  border-color: var(--sd-primary);
  background: var(--sd-primary-light);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.type-option-active .type-option-icon {
  background: var(--sd-gradient);
  color: #fff;
  box-shadow: var(--sd-shadow-primary);
}

.type-option-active .type-option-title {
  color: var(--sd-primary-dark);
}

/* ===== 商品提示 ===== */
.product-hint {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 14px 16px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #fdf2f8, #fff1f2);
  border: 1px solid #fbcfe8;
  border-radius: var(--sd-radius);
}

.product-hint-icon {
  width: 20px;
  flex-shrink: 0;
  color: #db2777;
  margin-top: 1px;
}

.product-hint-icon svg {
  width: 18px;
  height: 18px;
}

.product-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #9d174d;
  line-height: 1.5;
}

.product-title span {
  color: #be185d;
  font-weight: 700;
}

.product-hint small {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #be185d;
  opacity: 0.85;
}

.hint-fade-enter-active,
.hint-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.hint-fade-enter-from,
.hint-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ===== 底部操作 ===== */
.form-foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
  margin-top: 4px;
  border-top: 1px solid var(--sd-border-soft);
}

.submit-btn {
  min-width: 132px;
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

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .create-card {
    padding: 20px 18px;
  }

  .type-selector {
    grid-template-columns: 1fr;
  }

  .form-foot .sd-btn:not(.submit-btn) {
    display: none;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>
