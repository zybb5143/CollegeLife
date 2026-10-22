<template>
  <div class="sd-page submit-page">
    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      加载中...
    </div>

    <div v-else-if="!question" class="sd-card sd-empty">
      <div class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <p>题目不存在</p>
      <button class="sd-btn sd-btn-primary" @click="goBack">返回作业中心</button>
    </div>

    <template v-else>
      <div class="sd-page-head">
        <div>
          <h1 class="sd-title">作答</h1>
          <p class="sd-subtitle">{{ question.courseName || '未分类课程' }} · 总分 {{ question.totalScore }} 分</p>
        </div>
        <button type="button" class="back-bar" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          返回
        </button>
      </div>

      <!-- 题目信息 -->
      <div class="sd-card question-card">
        <div class="q-head">
          <span class="course-tag">{{ question.courseName || '未分类课程' }}</span>
          <span class="score-tag">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            总分 {{ question.totalScore }}
          </span>
        </div>
        <h3 class="q-title">{{ question.questionTitle }}</h3>
        <div class="q-content">{{ question.questionContent }}</div>
      </div>

      <!-- 作答区 -->
      <form @submit.prevent="handleSubmit" class="sd-card answer-card">
        <div class="editor-bar">
          <div class="editor-left">
            <span class="editor-dots"><i></i><i></i><i></i></span>
            <span class="editor-label">你的答案</span>
          </div>
          <div class="editor-right">
            <div class="lang-selector">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <polyline points="16 18 22 12 16 6"/>
                <polyline points="8 6 2 12 8 18"/>
              </svg>
              <select v-model="answerLanguage" class="language-select" @change="handleLanguageChange">
                <option value="txt">纯文本 (txt)</option>
                <option value="java">Java</option>
                <option value="javascript">JavaScript</option>
                <option value="python">Python</option>
                <option value="cpp">C++</option>
              </select>
            </div>
            <span class="char-count">{{ answerContent.length }} 字符</span>
          </div>
        </div>

        <textarea
          v-model="answerContent"
          rows="15"
          required
          spellcheck="false"
          placeholder="请在此输入你的答案，支持 Tab 键缩进..."
          @keydown.tab.prevent="handleTab"
          class="code-textarea"
        ></textarea>

        <p class="editor-tip">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="16" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
          提交后由 AI 自动批改，可在「我的提交记录」中查看结果
        </p>

        <div class="form-foot">
          <button type="button" class="sd-btn" @click="goBack">返回</button>
          <button type="submit" class="sd-btn sd-btn-primary sd-btn-lg submit-btn" :disabled="submitting">
            <span v-if="submitting" class="btn-spinner"></span>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
            {{ submitting ? '提交中...' : '提交答案' }}
          </button>
        </div>
      </form>
    </template>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'HomeworkSubmit',
  data() {
    return {
      loading: true,
      question: null,
      answerContent: '',
      answerLanguage: 'txt',
      submitting: false,
      currentUser: ''
    }
  },
  created() {
    this.getCurrentUser()
    this.loadQuestion()
  },
  methods: {
    handleTab(e) {
      // 支持 Tab 键缩进
      const textarea = e.target
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const value = textarea.value

      textarea.value = value.substring(0, start) + '  ' + value.substring(end)
      textarea.selectionStart = textarea.selectionEnd = start + 2

      // 触发 v-model 更新
      this.answerContent = textarea.value
    },
    handleLanguageChange() {
      // 语言切换后保留已输入内容，仅作为提交标记
    },
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
    async loadQuestion() {
      const questionId = this.$route.params.questionId
      try {
        const response = await api.get(`/homework/question/${questionId}`)
        if (response.success) {
          this.question = response.data
        } else {
          this.$message.error(response.message || '加载题目失败')
        }
      } catch (error) {
        this.$message.error('加载题目失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    async handleSubmit() {
      if (!this.currentUser) {
        this.$message.warning('请先登录')
        return
      }
      if (this.submitting) return

      this.submitting = true
      try {
        const response = await api.post('/homework/submit', {
          questionId: this.question.questionId,
          userName: this.currentUser,
          answerContent: this.answerContent,
          answerLanguage: this.answerLanguage
        })

        if (response.success) {
          this.$message.success('提交成功！AI正在批改，请稍后查看结果。')
          this.$router.push('/homework')
          return
        }
        this.$message.error(response.message || '提交失败')
        this.submitting = false
      } catch (error) {
        this.$message.error('提交失败：' + error.message)
        this.submitting = false
      }
    },
    goBack() {
      this.$router.push('/homework')
    }
  }
}
</script>

<style scoped>
.submit-page {
  max-width: 900px;
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

/* ===== 题目卡片 ===== */
.question-card {
  position: relative;
  padding: 22px 24px;
  margin-bottom: 18px;
  overflow: hidden;
}

.question-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--sd-gradient);
}

.q-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 13px;
}

.course-tag {
  padding: 4px 13px;
  border-radius: 999px;
  background: var(--sd-gradient);
  color: #fff;
  font-size: 12.5px;
  font-weight: 600;
  box-shadow: var(--sd-shadow-primary);
}

.score-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 700;
  color: #d97706;
}

.score-tag svg {
  width: 14px;
  height: 14px;
}

.q-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.45;
  color: var(--sd-text);
  letter-spacing: -0.2px;
}

.q-content {
  padding: 14px 16px;
  border-radius: var(--sd-radius);
  background: var(--sd-bg-input);
  border: 1px solid var(--sd-border-soft);
  font-size: 14.5px;
  line-height: 1.85;
  color: var(--sd-text-2);
  white-space: pre-wrap;
  word-break: break-word;
}

/* ===== 作答卡片 ===== */
.answer-card {
  padding: 0;
  overflow: hidden;
}

.editor-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  padding: 12px 18px;
  background: linear-gradient(90deg, #1e293b 0%, #312e81 100%);
  color: #e2e8f0;
}

.editor-left {
  display: flex;
  align-items: center;
  gap: 11px;
}

.editor-dots {
  display: flex;
  gap: 5px;
}

.editor-dots i {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.28);
}

.editor-dots i:nth-child(1) { background: #f87171; }
.editor-dots i:nth-child(2) { background: #fbbf24; }
.editor-dots i:nth-child(3) { background: #34d399; }

.editor-label {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.editor-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lang-selector {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.lang-selector > svg {
  width: 14px;
  height: 14px;
  color: #a5b4fc;
  flex-shrink: 0;
}

.language-select {
  padding: 0 4px;
  background: transparent;
  border: none;
  outline: none;
  color: #f1f5f9;
  font-size: 12.5px;
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
}

.language-select option {
  background: #1e293b;
  color: #f1f5f9;
}

.char-count {
  font-size: 12px;
  color: #94a3b8;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.code-textarea {
  display: block;
  width: 100%;
  padding: 20px 22px;
  border: none;
  outline: none;
  resize: vertical;
  min-height: 320px;
  background: #0f172a;
  color: #e2e8f0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, 'Courier New', monospace;
  font-size: 13.8px;
  line-height: 1.85;
  tab-size: 2;
}

.code-textarea::placeholder {
  color: #475569;
}

.code-textarea:focus {
  box-shadow: inset 0 0 0 2px rgba(99, 102, 241, 0.5);
}

.editor-tip {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  padding: 12px 22px;
  background: #f8fafc;
  border-top: 1px solid var(--sd-border-soft);
  font-size: 12.5px;
  color: var(--sd-text-4);
}

.editor-tip svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.form-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px 20px;
}

.submit-btn {
  min-width: 148px;
  justify-content: center;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
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
  .editor-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .editor-right {
    width: 100%;
    justify-content: space-between;
  }

  .form-foot {
    flex-direction: column-reverse;
  }

  .form-foot .sd-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
