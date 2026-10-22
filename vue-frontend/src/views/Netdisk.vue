<template>
  <div class="sd-page netdisk-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">文件共享</h1>
        <p class="sd-subtitle">上传、下载与管理你的校园网盘文件</p>
      </div>
      <div class="head-actions">
        <router-link v-if="isStaff" to="/admin/files" class="sd-btn sd-btn-primary">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          文件管理后台
        </router-link>
        <div v-if="currentUserName" class="user-chip">
          <span class="user-chip-avatar">{{ avatarText }}</span>
          <span class="user-chip-name">{{ currentUserName }}</span>
          <span v-if="isStaff" class="sd-tag sd-tag-pink">管理员</span>
        </div>
      </div>
    </div>

    <!-- 上传区 -->
    <div class="sd-card upload-card">
      <h3 class="section-title">上传文件</h3>
      <label class="file-drop" :class="{ 'file-drop-filled': !!selectedFile }">
        <input type="file" class="file-input-hidden" @change="handleFileSelect" />
        <span class="file-drop-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
        </span>
        <span class="file-drop-text">
          <span class="file-drop-name">{{ selectedFile ? selectedFile.name : '点击选择要上传的文件' }}</span>
          <span class="file-drop-size">{{ selectedFile ? formatSize(selectedFile.size) : '支持任意格式，单个文件上传' }}</span>
        </span>
      </label>
      <div class="upload-foot">
        <button
          v-if="selectedFile"
          type="button"
          class="sd-btn"
          :disabled="uploading"
          @click="clearFile"
        >
          重新选择
        </button>
        <button
          class="sd-btn sd-btn-primary upload-btn"
          :disabled="!selectedFile || uploading"
          @click="uploadFile"
        >
          <span v-if="uploading" class="btn-spinner"></span>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          {{ uploading ? '上传中...' : '开始上传' }}
        </button>
      </div>
    </div>

    <!-- 文件列表 -->
    <div class="sd-card list-card">
      <div class="list-head">
        <h3 class="section-title">
          文件列表
          <span class="count-pill">{{ fileList.length }}</span>
        </h3>
        <div class="list-tools">
          <div class="sd-input-wrap search-wrap">
            <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input type="text" class="sd-input" v-model="keyword" placeholder="搜索文件名..." />
          </div>
          <button class="sd-btn sd-btn-soft icon-btn" @click="loadFileList" title="刷新">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>
      </div>

      <div v-if="filteredList.length === 0" class="sd-empty">
        <div class="sd-empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <p>{{ keyword ? '没有匹配的文件' : '暂无文件' }}</p>
      </div>

      <div v-else class="file-list">
        <div v-for="file in filteredList" :key="file.fileId" class="file-item">
          <span class="file-icon" :class="fileTone(file.fileName)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <template v-if="isImage(file.fileName)">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </template>
              <template v-else-if="isVideo(file.fileName)">
                <polygon points="23 7 16 12 23 17 23 7"/>
                <rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
              </template>
              <template v-else-if="isArchive(file.fileName)">
                <path d="M21 8v13H3V8"/>
                <path d="M1 3h22v5H1z"/>
                <path d="M10 12h4"/>
              </template>
              <template v-else>
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </template>
            </svg>
          </span>

          <div class="file-main">
            <span class="file-name" :title="file.fileName">{{ file.fileName }}</span>
            <span class="file-sub">
              <span class="file-ext">{{ extName(file.fileName) }}</span>
              <span class="dot">·</span>
              {{ formatSize(file.fileSize) }}
              <template v-if="file.userName">
                <span class="dot">·</span>
                {{ file.userName }}
              </template>
            </span>
          </div>

          <div class="file-actions">
            <button class="sd-btn sd-btn-soft act-btn" @click="downloadFile(file)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="7 10 12 15 17 10"/>
                <line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              下载
            </button>
            <button
              v-if="canDelete(file)"
              class="sd-btn sd-btn-danger act-btn"
              @click="deleteFile(file)"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'Netdisk',
  data() {
    return {
      currentUserName: '',
      userRole: '',
      selectedFile: null,
      uploading: false,
      fileList: [],
      keyword: ''
    }
  },
  computed: {
    isStaff() {
      return this.userRole === 'admin' || this.userRole === 'boss'
    },
    avatarText() {
      const text = (this.currentUserName || '').trim()
      if (!text) return 'U'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    },
    filteredList() {
      const key = this.keyword.trim().toLowerCase()
      if (!key) return this.fileList
      return this.fileList.filter(f => (f.fileName || '').toLowerCase().includes(key))
    }
  },
  async created() {
    await this.loadUserInfo()
    this.loadFileList()
  },
  methods: {
    async loadUserInfo() {
      try {
        const user = JSON.parse(localStorage.getItem('user'))
        if (user) {
          this.currentUserName = user.userName
          this.userRole = user.role
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    handleFileSelect(event) {
      this.selectedFile = event.target.files[0]
    },
    clearFile() {
      this.selectedFile = null
    },
    canDelete(file) {
      return this.isStaff || file.userName === this.currentUserName
    },
    extName(name) {
      const idx = (name || '').lastIndexOf('.')
      if (idx < 0 || idx === name.length - 1) return '文件'
      return name.slice(idx + 1).toUpperCase()
    },
    isImage(name) {
      return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(name || '')
    },
    isVideo(name) {
      return /\.(mp4|mov|avi|mkv|flv|wmv|webm)$/i.test(name || '')
    },
    isArchive(name) {
      return /\.(zip|rar|7z|tar|gz)$/i.test(name || '')
    },
    fileTone(name) {
      if (this.isImage(name)) return 'icon-green'
      if (this.isVideo(name)) return 'icon-pink'
      if (this.isArchive(name)) return 'icon-amber'
      return 'icon-blue'
    },
    async uploadFile() {
      if (!this.selectedFile) return
      this.uploading = true
      try {
        const formData = new FormData()
        formData.append('file', this.selectedFile)
        // 后端 UserFileController 映射为 /api/userfile/upload
        const response = await api.post('/userfile/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (response && response.success) {
          this.$message.success('上传成功')
          this.selectedFile = null
          this.loadFileList()
        } else {
          this.$message.error((response && response.message) || '上传失败')
        }
      } catch (error) {
        this.$message.error(error.message || '上传失败')
      } finally {
        this.uploading = false
      }
    },
    async loadFileList() {
      try {
        // 后端返回结构：{ success, data: UserFile[] }
        const response = await api.get('/userfile/list')
        if (response && response.success) {
          this.fileList = response.data || []
        } else {
          this.fileList = []
          this.$message.error((response && response.message) || '加载文件列表失败')
        }
      } catch (error) {
        console.error('加载文件列表失败:', error)
        this.fileList = []
      }
    },
    downloadFile(file) {
      if (!file || !file.filePath) return
      // 后端 DownloadController 映射为 /api/download/download/{filename}，filename 使用 filePath（UUID）
      window.open('/api/download/download/' + encodeURIComponent(file.filePath), '_blank')
    },
    async deleteFile(file) {
      if (!file || !file.fileId) return
      try {
        await this.$confirm(`确认删除文件「${file.fileName}」？`, '删除文件', {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch (e) {
        return
      }
      try {
        // 后端 UserFileController 映射为 DELETE /api/userfile/delete/{fileId}
        const response = await api.delete('/userfile/delete/' + file.fileId)
        if (response && response.success) {
          this.$message.success('删除成功')
          this.loadFileList()
        } else {
          this.$message.error((response && response.message) || '删除失败')
        }
      } catch (error) {
        this.$message.error(error.message || '删除失败')
      }
    },
    formatSize(bytes) {
      if (!bytes) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB']
      let i = 0
      let size = bytes
      while (size >= 1024 && i < units.length - 1) {
        size /= 1024
        i++
      }
      return size.toFixed(1) + ' ' + units[i]
    }
  }
}
</script>

<style scoped>
.netdisk-page {
  max-width: 940px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* ===== 用户标识 ===== */
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px 5px 5px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--sd-border-soft);
  border-radius: 999px;
  box-shadow: var(--sd-shadow-sm);
}

.user-chip-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--sd-gradient);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.user-chip-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--sd-text);
}

/* ===== 卡片标题 ===== */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.count-pill {
  padding: 1px 9px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 12px;
  font-weight: 700;
}

/* ===== 上传区 ===== */
.upload-card {
  padding: 22px 24px;
  margin-bottom: 18px;
}

.file-drop {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 22px;
  margin-top: 14px;
  border: 2px dashed var(--sd-border);
  border-radius: var(--sd-radius);
  background: #fbfcfe;
  cursor: pointer;
  transition: all 0.25s ease;
}

.file-drop:hover {
  border-color: #93c5fd;
  background: var(--sd-primary-light);
}

.file-drop-filled {
  border-color: #6ee7b7;
  background: #f0fdf9;
  border-style: solid;
}

.file-input-hidden {
  display: none;
}

.file-drop-icon {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff;
  color: var(--sd-primary);
  box-shadow: var(--sd-shadow-sm);
}

.file-drop-icon svg {
  width: 22px;
  height: 22px;
}

.file-drop-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.file-drop-name {
  font-size: 14px;
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

.upload-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.upload-btn {
  min-width: 132px;
  justify-content: center;
}

.upload-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: sd-spin 0.7s linear infinite;
}

/* ===== 列表区 ===== */
.list-card {
  padding: 20px 24px 22px;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  padding-bottom: 16px;
  margin-bottom: 6px;
  border-bottom: 1px solid var(--sd-border-soft);
}

.list-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-wrap {
  width: 230px;
}

.search-wrap .sd-input {
  height: 38px;
}

.icon-btn {
  width: 38px;
  height: 38px;
  padding: 0;
  justify-content: center;
}

.icon-btn svg {
  width: 16px;
  height: 16px;
}

.file-list {
  display: flex;
  flex-direction: column;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 10px;
  border-radius: var(--sd-radius);
  border-bottom: 1px solid var(--sd-border-soft);
  transition: background 0.2s ease;
}

.file-item:last-child {
  border-bottom: none;
}

.file-item:hover {
  background: #f8fbff;
}

.file-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
}

.file-icon svg {
  width: 19px;
  height: 19px;
}

.icon-blue { background: var(--sd-primary-light); color: var(--sd-primary); }
.icon-green { background: #ecfdf5; color: #059669; }
.icon-pink { background: #fdf2f8; color: #db2777; }
.icon-amber { background: #fffbeb; color: #d97706; }

.file-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-sub {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: var(--sd-text-4);
}

.file-ext {
  padding: 1px 7px;
  border-radius: 5px;
  background: var(--sd-bg-input);
  border: 1px solid var(--sd-border-soft);
  font-size: 10.5px;
  font-weight: 700;
  color: var(--sd-text-3);
  letter-spacing: 0.4px;
}

.dot {
  opacity: 0.6;
}

.file-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.act-btn {
  padding: 7px 13px;
  font-size: 12.5px;
}

.act-btn svg {
  width: 14px;
  height: 14px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .search-wrap {
    width: 100%;
  }

  .list-tools {
    width: 100%;
  }

  .file-item {
    flex-wrap: wrap;
  }

  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .file-drop {
    padding: 18px 14px;
  }

  .upload-foot {
    flex-direction: column;
  }

  .upload-foot .sd-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
