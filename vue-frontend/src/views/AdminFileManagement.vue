<template>
  <div class="sd-page admin-file-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">文件管理</h1>
        <p class="sd-subtitle">全站用户上传文件的集中管理与维护</p>
      </div>
      <div v-if="currentUserName" class="admin-chip">
        <span class="admin-chip-avatar">{{ avatarText }}</span>
        <span class="admin-chip-name">{{ currentUserName }}</span>
        <span class="admin-chip-tag">管理员</span>
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-icon stat-icon-blue">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
        </span>
        <div class="stat-text">
          <p class="stat-value">{{ totalFiles }}</p>
          <p class="stat-label">总文件数</p>
        </div>
      </div>

      <div class="stat-card">
        <span class="stat-icon stat-icon-purple">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </span>
        <div class="stat-text">
          <p class="stat-value">{{ totalUsers }}</p>
          <p class="stat-label">总用户数</p>
        </div>
      </div>

      <div class="stat-card">
        <span class="stat-icon stat-icon-green">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <ellipse cx="12" cy="5" rx="9" ry="3"/>
            <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/>
            <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
          </svg>
        </span>
        <div class="stat-text">
          <p class="stat-value">{{ formatFileSize(totalSize) }}</p>
          <p class="stat-label">占用空间</p>
        </div>
      </div>
    </div>

    <!-- 搜索 -->
    <div class="sd-card search-card">
      <el-input v-model="searchKeyword" placeholder="搜索文件名..." clearable @input="handleSearch">
        <template #prefix>
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </template>
      </el-input>
      <button class="sd-btn" @click="loadAllFiles">
        <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <polyline points="23 4 23 10 17 10"/>
          <polyline points="1 20 1 14 7 14"/>
          <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
        </svg>
        重新加载
      </button>
    </div>

    <!-- 文件列表 -->
    <div class="sd-card file-section">
      <h3 class="section-title">
        全部文件
        <span class="count-pill">{{ fileList.length }}</span>
      </h3>
      <el-table :data="fileList" v-loading="loading" class="file-table">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="userName" label="所属用户" width="120" />
        <el-table-column prop="fileSize" label="大小" width="100" :formatter="formatSize" />
        <el-table-column prop="fileType" label="类型" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleDownload(scope.row)">下载</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'AdminFileManagement',
  data() {
    return { fileList:[], loading:false, currentUserName:'', userRole:'', searchKeyword:'', totalFiles:0, totalUsers:0, totalSize:0 }
  },
  computed: {
    avatarText() {
      const text = (this.currentUserName || '').trim()
      if (!text) return 'A'
      return /[a-zA-Z]/.test(text.charAt(0)) ? text.charAt(0).toUpperCase() : text.charAt(0)
    }
  },
  created() { this.loadUserInfo() },
  methods: {
    async loadUserInfo() {
      try {
        const r = await api.get('/user/current')
        if (r.success && r.data) {
          this.currentUserName = r.data.userName; this.userRole = r.data.role || 'user'
          if (this.userRole !== 'admin' && this.userRole !== 'boss') {
            this.$message.error('需要管理员权限'); setTimeout(() => this.$router.push('/login'), 1500); return
          }
          this.loadAllFiles()
        } else { this.$message.error('请先登录'); setTimeout(() => this.$router.push('/login'), 1500) }
      } catch (e) { this.$message.error('请先登录'); setTimeout(() => this.$router.push('/login'), 1500) }
    },
    async loadAllFiles() {
      this.loading = true
      try {
        const r = await api.get('/userfile/list/all')
        if (r.success) {
          this.fileList = r.data || []; this.totalFiles = this.fileList.length
          this.totalUsers = new Set(this.fileList.map(f => f.userName)).size
          this.totalSize = this.fileList.reduce((s, f) => s + (f.fileSize||0), 0)
        } else this.$message.error(r.message||'加载失败')
      } catch (e) { this.$message.error('加载失败') }
      finally { this.loading = false }
    },
    handleSearch() { if(!this.searchKeyword) this.loadAllFiles() },
    handleDownload(row) { window.open(`/api/download/download/${row.filePath}`, '_blank') },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定删除 "${row.fileName}"？`,'提示',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'})
        const r = await api.delete(`/userfile/delete/${row.fileId}`)
        if(r.success){this.$message.success('删除成功');this.loadAllFiles()}else this.$message.error(r.message||'删除失败')
      } catch(e) { if(e!=='cancel') this.$message.error('删除失败') }
    },
    formatFileSize(b) { if(!b||b===0)return'0 B'; const k=1024,s=['B','KB','MB','GB']; const i=Math.floor(Math.log(b)/Math.log(k)); return Math.round(b/Math.pow(k,i)*100)/100+' '+s[i] },
    formatSize(row) { return this.formatFileSize(row.fileSize) }
  }
}
</script>

<style scoped>
.btn-icon {
  width: 15px;
  height: 15px;
}

/* ===== 管理员标识 ===== */
.admin-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px 5px 5px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--sd-border-soft);
  border-radius: 999px;
  box-shadow: var(--sd-shadow-sm);
}

.admin-chip-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  color: #fff;
  font-size: 12.5px;
  font-weight: 600;
}

.admin-chip-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--sd-text);
}

.admin-chip-tag {
  padding: 2px 8px;
  background: #dbeafe;
  color: #1e40af;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

/* ===== 统计卡片 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--sd-border-soft);
  border-radius: var(--sd-radius-lg);
  box-shadow: var(--sd-shadow-sm);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--sd-shadow);
}

.stat-icon {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
}

.stat-icon svg {
  width: 21px;
  height: 21px;
}

.stat-icon-blue {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #2563eb;
}

.stat-icon-purple {
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  color: #7c3aed;
}

.stat-icon-green {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #059669;
}

.stat-value {
  font-size: 22px;
  font-weight: 750;
  color: var(--sd-text);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  margin-top: 3px;
  font-size: 12.5px;
  color: var(--sd-text-4);
}

/* ===== 搜索区 ===== */
.search-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.search-card :deep(.el-input) {
  flex: 1;
}

.search-card :deep(.el-input__wrapper) {
  border-radius: 999px;
  padding: 4px 16px;
  background: #f5f7fb;
  box-shadow: none;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.search-card :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: var(--sd-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.search-icon {
  width: 16px;
  height: 16px;
  color: var(--sd-text-4);
}

/* ===== 文件表格 ===== */
.file-section {
  padding: 22px 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  border-radius: 999px;
  background: var(--sd-gradient);
}

.count-pill {
  padding: 2px 9px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.file-table :deep(th.el-table__cell) {
  background: #f8fafc;
  color: var(--sd-text-2);
  font-weight: 600;
  font-size: 13px;
}

.file-table :deep(td.el-table__cell) {
  font-size: 13px;
  color: var(--sd-text-2);
}

.file-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: var(--sd-primary-light) !important;
}

.file-table :deep(.el-button) {
  border-radius: 999px;
}

@media (max-width: 640px) {
  .search-card {
    flex-direction: column;
    align-items: stretch;
  }

  .file-section {
    padding: 18px 14px;
  }

  .admin-chip-name {
    display: none;
  }
}
</style>
