<template>
  <div class="sd-page product-create-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">发布闲置物品</h1>
        <p class="sd-subtitle">填写越详细，越容易被同学看中</p>
      </div>
      <button type="button" class="back-bar" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        返回
      </button>
    </div>

    <form @submit.prevent="handleCreate" class="sd-card product-form">
      <div class="sd-field">
        <label class="sd-label">商品名称</label>
        <div class="sd-input-wrap">
          <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
          <input type="text" class="sd-input" v-model="form.productName"
                 placeholder="例如：九成新高等数学教材" required />
        </div>
      </div>

      <div class="sd-field">
        <label class="sd-label">商品描述</label>
        <textarea v-model="form.productDescription" class="sd-textarea"
                  placeholder="请详细描述商品的使用情况、购买时间、是否有配件等" rows="5" required></textarea>
        <p class="field-tip">{{ (form.productDescription || '').length }} 字</p>
      </div>

      <div class="form-row">
        <div class="sd-field">
          <label class="sd-label">几成新</label>
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            <select class="sd-input sd-select-native" v-model="form.productCondition" required>
              <option value="" disabled>请选择</option>
              <option value="9 成新">9 成新</option>
              <option value="8 成新">8 成新</option>
              <option value="7 成新">7 成新</option>
              <option value="6 成新">6 成新</option>
              <option value="5 成新及以下">5 成新及以下</option>
            </select>
            <svg class="select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>
        </div>

        <div class="sd-field">
          <label class="sd-label">商品分类</label>
          <div class="sd-input-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7" rx="1.5"/>
              <rect x="14" y="3" width="7" height="7" rx="1.5"/>
              <rect x="3" y="14" width="7" height="7" rx="1.5"/>
              <rect x="14" y="14" width="7" height="7" rx="1.5"/>
            </svg>
            <select class="sd-input sd-select-native" v-model="form.category">
              <option value="">请选择（可选）</option>
              <option value="图书教材">图书教材</option>
              <option value="数码电子">数码电子</option>
              <option value="生活用品">生活用品</option>
              <option value="运动户外">运动户外</option>
              <option value="其他">其他</option>
            </select>
            <svg class="select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="form-row">
        <div class="sd-field">
          <label class="sd-label">售价（元）</label>
          <div class="sd-input-wrap">
            <span class="price-prefix">¥</span>
            <input type="number" step="0.01" min="0" class="sd-input price-input"
                   v-model="form.price" placeholder="0.00" required />
          </div>
        </div>

        <div class="sd-field">
          <label class="sd-label">原价（元，可选）</label>
          <div class="sd-input-wrap">
            <span class="price-prefix">¥</span>
            <input type="number" step="0.01" min="0" class="sd-input price-input"
                   v-model="form.originalPrice" placeholder="0.00" />
          </div>
        </div>
      </div>

      <div class="sd-field">
        <label class="sd-label">商品图片（可多选）</label>
        <label class="file-drop" :class="{ 'file-drop-filled': selectedFileList.length > 0 }">
          <input type="file" class="file-input-hidden" multiple accept="image/*"
                 @change="handleFileChange" />
          <span class="file-drop-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
          </span>
          <span class="file-drop-text">
            <span class="file-drop-name">
              {{ selectedFileList.length > 0 ? `已选择 ${selectedFileList.length} 张图片` : '点击选择商品图片' }}
            </span>
            <span class="file-drop-size">清晰的实拍图能显著提升成交率</span>
          </span>
        </label>
        <div v-if="selectedFileList.length > 0" class="file-chips">
          <span v-for="(file, index) in selectedFileList" :key="index" class="file-chip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            {{ file.name }}
          </span>
        </div>
      </div>

      <transition name="msg-fade">
        <div v-if="message" class="sd-alert" :class="messageClass">{{ message }}</div>
      </transition>

      <div class="form-foot">
        <button type="button" @click="$router.back()" class="sd-btn">取消</button>
        <button type="submit" class="sd-btn sd-btn-primary sd-btn-lg submit-btn" :disabled="submitting">
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
  name: 'CreateProduct',
  data() {
    return {
      form: {
        productName: '',
        productDescription: '',
        productCondition: '',
        category: '',
        price: '',
        originalPrice: ''
      },
      selectedFiles: [],
      message: '',
      messageType: '',
      submitting: false
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'sd-alert-success' : 'sd-alert-error'
    },
    selectedFileList() {
      return Array.from(this.selectedFiles || [])
    }
  },
  methods: {
    handleFileChange(event) {
      this.selectedFiles = event.target.files
    },
    
    async handleCreate() {
      if (this.submitting) return
      this.submitting = true
      try {
        const formData = new FormData()
        formData.append('productName', this.form.productName)
        formData.append('productDescription', this.form.productDescription)
        formData.append('productCondition', this.form.productCondition)
        formData.append('price', this.form.price)
        
        if (this.form.originalPrice) {
          formData.append('originalPrice', this.form.originalPrice)
        }
        if (this.form.category) {
          formData.append('category', this.form.category)
        }
        
        // 添加图片
        for (let i = 0; i < this.selectedFiles.length; i++) {
          formData.append('images', this.selectedFiles[i])
        }
        
        const response = await api.post('/secondhand/create', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.success) {
          this.messageType = 'success'
          this.message = '发布成功！'
          // 保持提交态直到跳转
          setTimeout(() => {
            this.$router.push('/secondhand')
          }, 1000)
          return
        }
        this.messageType = 'error'
        this.message = response.message || '发布失败'
        this.submitting = false
      } catch (error) {
        this.messageType = 'error'
        this.message = error.response?.data?.message || '发布失败，请稍后重试'
        console.error('发布错误:', error)
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.product-create-page {
  max-width: 780px;
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

.product-form {
  padding: 28px 30px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.sd-select-native {
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
  padding-right: 40px;
}

.select-arrow {
  position: absolute;
  right: 13px;
  width: 15px;
  height: 15px;
  color: var(--sd-text-4);
  pointer-events: none;
}

.field-tip {
  margin-top: 7px;
  text-align: right;
  font-size: 12px;
  color: var(--sd-text-4);
}

/* ===== 价格输入 ===== */
.price-prefix {
  position: absolute;
  left: 14px;
  font-size: 15px;
  font-weight: 700;
  color: var(--sd-text-4);
  pointer-events: none;
}

.price-input {
  padding-left: 32px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

/* ===== 图片选择 ===== */
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

.file-drop:hover .file-drop-icon,
.file-drop-filled .file-drop-icon {
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
}

.file-drop-size {
  font-size: 12px;
  color: var(--sd-text-4);
}

.file-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 10px;
}

.file-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 220px;
  padding: 4px 11px;
  background: #fff;
  border: 1px solid var(--sd-border);
  border-radius: 999px;
  font-size: 12px;
  color: var(--sd-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-chip svg {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
  color: var(--sd-primary);
}

/* ===== 提示与底部 ===== */
.sd-alert {
  justify-content: center;
  margin-bottom: 18px;
}

.form-foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
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

.msg-fade-enter-active,
.msg-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.msg-fade-enter-from,
.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .product-form {
    padding: 20px 18px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .form-foot .sd-btn:not(.submit-btn) {
    display: none;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>
