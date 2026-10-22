<template>
  <div class="sd-page homework-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">作业中心</h1>
        <p class="sd-subtitle">共 {{ questions.length }} 道题目 · 已提交 {{ submissions.length }} 次</p>
      </div>
      <div class="head-actions">
        <button v-if="isAdmin" class="sd-btn sd-btn-primary" @click="togglePublish">
          <svg v-if="!showPublishForm" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
          {{ showPublishForm ? '收起表单' : '发布题目' }}
        </button>
      </div>
    </div>

    <!-- 发布题目表单 -->
    <transition name="form-slide">
      <div v-if="showPublishForm" class="sd-card publish-card">
        <h3 class="section-title">
          <span class="title-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"/>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
          </span>
          发布新题目
        </h3>
        <p class="section-desc">发布后所有同学都能在作业中心看到该题目</p>

        <form @submit.prevent="handlePublish">
          <div class="form-row">
            <div class="sd-field">
              <label class="sd-label">课程名称</label>
              <div class="sd-input-wrap">
                <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
                <input class="sd-input" v-model="newQuestion.courseName" required
                       placeholder="例如：Java程序设计" />
              </div>
            </div>
            <div class="sd-field">
              <label class="sd-label">总分</label>
              <div class="sd-input-wrap">
                <span class="score-prefix">分</span>
                <input class="sd-input score-input" v-model.number="newQuestion.totalScore"
                       type="number" step="0.01" min="0" required />
              </div>
            </div>
          </div>

          <div class="sd-field">
            <label class="sd-label">题目标题</label>
            <div class="sd-input-wrap">
              <svg class="sd-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <input class="sd-input" v-model="newQuestion.questionTitle" required
                     placeholder="请输入题目标题" />
            </div>
          </div>

          <div class="sd-field">
            <label class="sd-label">题目内容</label>
            <textarea class="sd-textarea" v-model="newQuestion.questionContent" required rows="6"
                      placeholder="请详细描述题目要求"></textarea>
          </div>

          <div class="sd-field">
            <label class="sd-label">参考答案（可选）</label>
            <textarea class="sd-textarea" v-model="newQuestion.referenceAnswer" rows="4"
                      placeholder="请输入参考答案"></textarea>
          </div>

          <div class="form-foot">
            <button type="button" class="sd-btn" @click="showPublishForm = false">取消</button>
            <button type="submit" class="sd-btn sd-btn-primary submit-btn" :disabled="publishing">
              <span v-if="publishing" class="btn-spinner"></span>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="22" y1="2" x2="11" y2="13"/>
                <polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
              {{ publishing ? '发布中...' : '确认发布' }}
            </button>
          </div>
        </form>
      </div>
    </transition>

    <!-- 题目列表 -->
    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      加载中...
    </div>

    <div v-else-if="questions.length === 0" class="sd-card sd-empty">
      <div class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
        </svg>
      </div>
      <p>暂无题目</p>
      <button v-if="isAdmin" class="sd-btn sd-btn-primary" @click="showPublishForm = true">
        发布第一道题目
      </button>
    </div>

    <div v-else class="question-grid">
      <div v-for="q in questions" :key="q.questionId" class="sd-card question-card">
        <div class="q-head">
          <span class="course-tag">{{ q.courseName || '未分类课程' }}</span>
          <span class="score-tag">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            {{ q.totalScore }} 分
          </span>
        </div>

        <h3 class="q-title">{{ q.questionTitle }}</h3>
        <p class="q-content">{{ q.questionContent }}</p>

        <div class="q-foot">
          <span class="q-time">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            {{ formatTime(q.createTime) }}
          </span>
          <button class="sd-btn sd-btn-primary answer-btn" @click="goToSubmit(q.questionId)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"/>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
            作答
          </button>
        </div>
      </div>
    </div>

    <!-- 我的提交记录 -->
    <div class="sd-card submission-card">
      <h3 class="section-title">
        <span class="title-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
            <rect x="8" y="2" width="8" height="4" rx="1" ry="1"/>
            <path d="M9 14l2 2 4-4"/>
          </svg>
        </span>
        我的提交记录
        <span class="count-pill">{{ submissions.length }}</span>
      </h3>

      <div v-if="submissions.length === 0" class="sd-empty small-empty">
        <div class="sd-empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
            <rect x="8" y="2" width="8" height="4" rx="1" ry="1"/>
          </svg>
        </div>
        <p>暂无提交记录</p>
      </div>

      <div v-else class="submission-list">
        <div v-for="sub in submissions" :key="sub.submissionId" class="submission-item">
          <span class="sub-icon" :class="'tone-' + (sub.status || 'submitted')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline v-if="sub.status === 'graded'" points="20 6 9 17 4 12"/>
              <circle v-else-if="sub.status === 'grading'" cx="12" cy="12" r="10"/>
              <template v-else>
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </template>
            </svg>
          </span>

          <div class="sub-main">
            <div class="sub-line1">
              <strong class="sub-title">{{ sub.questionTitle }}</strong>
              <span class="status-badge" :class="'status-' + sub.status">
                <i class="status-dot"></i>
                {{ getStatusText(sub.status) }}
              </span>
            </div>
            <span class="sub-time">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              提交于 {{ formatTime(sub.submitTime) }}
            </span>
          </div>

          <button
            v-if="sub.status === 'graded'"
            class="sd-btn sd-btn-soft view-btn"
            @click="viewGrading(sub.submissionId)"
          >
            查看批改
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="5" y1="12" x2="19" y2="12"/>
              <polyline points="12 5 19 12 12 19"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'HomeworkList',
  data() {
    return {
      isAdmin: false,
      currentUser: '',
      showPublishForm: false,
      loading: true,
      publishing: false,
      questions: [],
      submissions: [],
      newQuestion: {
        courseName: '',
        questionTitle: '',
        questionContent: '',
        referenceAnswer: '',
        totalScore: 100
      }
    }
  },
  created() {
    this.initPage()
  },
  methods: {
    async initPage() {
      // 先获取用户信息
      await this.checkAdminStatus()
      // 然后加载数据
      this.loadQuestions()
      this.loadMySubmissions()
    },
    async checkAdminStatus() {
      try {
        const response = await api.get('/user/current')
        if (response.success && response.data) {
          this.currentUser = response.data.userName
          this.isAdmin = response.data.role === 'admin' || response.data.role === 'boss'
          console.log('当前用户:', this.currentUser)
        }
      } catch (error) {
        console.error('检查权限失败:', error)
      }
    },
    async loadQuestions() {
      try {
        const response = await api.get('/homework/questions')
        if (response.success) {
          this.questions = response.data
        }
      } catch (error) {
        console.error('加载题目失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadMySubmissions() {
      console.log('加载提交记录, currentUser:', this.currentUser)
      if (!this.currentUser) {
        console.warn('用户未登录，跳过加载提交记录')
        return
      }
      try {
        const response = await api.get('/homework/my-submissions', { params: { userName: this.currentUser } })
        console.log('提交记录响应:', response)
        if (response.success) {
          this.submissions = response.data
          console.log('提交记录数量:', this.submissions.length)
        }
      } catch (error) {
        console.error('加载提交记录失败:', error)
      }
    },
    togglePublish() {
      this.showPublishForm = !this.showPublishForm
    },
    async handlePublish() {
      if (this.publishing) return
      this.publishing = true
      try {
        const response = await api.post('/homework/publish', {
          ...this.newQuestion,
          adminName: this.currentUser
        })
        if (response.success) {
          this.$message.success('题目发布成功！')
          this.showPublishForm = false
          this.resetForm()
          this.loadQuestions()
        } else {
          this.$message.error(response.message || '发布失败')
        }
      } catch (error) {
        this.$message.error('发布失败：' + error.message)
      } finally {
        this.publishing = false
      }
    },
    resetForm() {
      this.newQuestion = {
        courseName: '',
        questionTitle: '',
        questionContent: '',
        referenceAnswer: '',
        totalScore: 100
      }
    },
    goToSubmit(questionId) {
      this.$router.push(`/homework/submit/${questionId}`)
    },
    viewGrading(submissionId) {
      this.$router.push(`/homework/grading/${submissionId}`)
    },
    getStatusText(status) {
      const map = {
        'submitted': '已提交',
        'grading': '批改中',
        'graded': '已批改'
      }
      return map[status] || status
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.homework-page {
  max-width: 1060px;
}

.head-actions {
  display: flex;
  gap: 10px;
}

/* ===== 通用标题 ===== */
.section-title {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.section-desc {
  margin: 6px 0 0;
  font-size: 12.5px;
  color: var(--sd-text-4);
}

.title-icon {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: var(--sd-primary-light);
  color: var(--sd-primary);
}

.title-icon svg {
  width: 16px;
  height: 16px;
}

.count-pill {
  padding: 1px 9px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 12px;
  font-weight: 700;
}

/* ===== 发布表单 ===== */
.publish-card {
  padding: 22px 24px 24px;
  margin-bottom: 18px;
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 42%);
}

.form-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px;
  gap: 14px;
}

.score-prefix {
  position: absolute;
  right: 14px;
  font-size: 13px;
  color: var(--sd-text-4);
  pointer-events: none;
}

.score-input {
  padding-right: 38px;
}

.form-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--sd-border-soft);
}

.submit-btn {
  min-width: 130px;
  justify-content: center;
}

.submit-btn:disabled {
  opacity: 0.6;
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

.form-slide-enter-active,
.form-slide-leave-active {
  transition: all 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.form-slide-enter-from,
.form-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

/* ===== 题目卡片 ===== */
.question-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.question-card {
  position: relative;
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.28s ease;
}

.question-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--sd-gradient);
  opacity: 0;
  transition: opacity 0.28s ease;
}

.question-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--sd-shadow-md);
}

.question-card:hover::before {
  opacity: 1;
}

.q-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.course-tag {
  padding: 3px 11px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 12px;
  font-weight: 600;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.score-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  font-size: 12.5px;
  font-weight: 700;
  color: #d97706;
}

.score-tag svg {
  width: 13px;
  height: 13px;
}

.q-title {
  margin: 0 0 9px;
  font-size: 16.5px;
  font-weight: 700;
  line-height: 1.45;
  color: var(--sd-text);
}

.q-content {
  margin: 0 0 16px;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--sd-text-3);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.q-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid var(--sd-border-soft);
}

.q-time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--sd-text-4);
  min-width: 0;
}

.q-time svg {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
}

.answer-btn {
  flex-shrink: 0;
  padding: 7px 16px;
  font-size: 13px;
}

.answer-btn svg {
  width: 14px;
  height: 14px;
}

/* ===== 提交记录 ===== */
.submission-card {
  padding: 22px 24px;
}

.small-empty {
  padding: 26px 0 10px;
}

.submission-list {
  display: flex;
  flex-direction: column;
  margin-top: 14px;
}

.submission-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 14px 10px;
  border-radius: var(--sd-radius);
  border-bottom: 1px solid var(--sd-border-soft);
  transition: background 0.2s ease;
}

.submission-item:last-child {
  border-bottom: none;
}

.submission-item:hover {
  background: #f8fbff;
}

.sub-icon {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
}

.sub-icon svg {
  width: 18px;
  height: 18px;
}

.tone-submitted { background: var(--sd-primary-light); color: var(--sd-primary); }
.tone-grading { background: #fffbeb; color: #d97706; }
.tone-graded { background: #ecfdf5; color: #059669; }

.sub-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.sub-line1 {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 9px;
}

.sub-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--sd-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  padding: 2px 10px 2px 8px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 600;
}

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}

.status-submitted {
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  border: 1px solid #bfdbfe;
}

.status-grading {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-graded {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.sub-time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--sd-text-4);
}

.sub-time svg {
  width: 12.5px;
  height: 12.5px;
}

.view-btn {
  flex-shrink: 0;
  padding: 7px 14px;
  font-size: 12.5px;
}

.view-btn svg {
  width: 14px;
  height: 14px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .question-grid {
    grid-template-columns: 1fr;
  }

  .submission-item {
    flex-wrap: wrap;
  }

  .view-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
