<template>
  <div class="sd-page grading-page">
    <div v-if="loading" class="sd-loading">
      <span class="sd-spinner"></span>
      加载中...
    </div>

    <div v-else-if="!grading" class="sd-card sd-empty">
      <div class="sd-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
      </div>
      <p>批改记录不存在，可能还在批改中</p>
      <span class="empty-tip">AI 批改通常需要一点时间，稍后再来查看</span>
      <button class="sd-btn sd-btn-primary" @click="goBack">返回作业中心</button>
    </div>

    <template v-else>
      <div class="sd-page-head">
        <div>
          <h1 class="sd-title">批改结果</h1>
          <p class="sd-subtitle">由 AI 自动批改生成，仅供参考</p>
        </div>
        <button type="button" class="back-bar" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          返回列表
        </button>
      </div>

      <!-- 得分卡 -->
      <div class="score-hero" :class="scoreTone">
        <div class="hero-glow hero-glow-1"></div>
        <div class="hero-glow hero-glow-2"></div>

        <div class="score-ring">
          <svg viewBox="0 0 120 120" class="ring-svg">
            <circle cx="60" cy="60" r="52" class="ring-bg"/>
            <circle
              cx="60" cy="60" r="52"
              class="ring-fg"
              :stroke-dasharray="ringDash"
              stroke-dashoffset="0"
            />
          </svg>
          <div class="ring-text">
            <span class="score-number">{{ grading.score }}</span>
            <span class="score-unit">分</span>
          </div>
        </div>

        <div class="score-info">
          <p class="score-verdict">{{ verdictText }}</p>
          <p class="score-time">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            批改时间：{{ formatTime(grading.gradeTime) }}
          </p>
          <div class="score-bar">
            <span class="score-bar-fill" :style="{ width: scorePercent + '%' }"></span>
          </div>
          <p class="score-percent">得分率 {{ scorePercent.toFixed(0) }}%</p>
        </div>
      </div>

      <!-- 正确性分析 -->
      <div class="sd-card result-card">
        <h3 class="result-title">
          <span class="result-icon icon-blue">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </span>
          正确性分析
        </h3>
        <div class="content-box box-blue">{{ grading.correctnessAnalysis || '暂无分析内容' }}</div>
      </div>

      <!-- 改进建议 -->
      <div class="sd-card result-card">
        <h3 class="result-title">
          <span class="result-icon icon-amber">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
              <polyline points="17 6 23 6 23 12"/>
            </svg>
          </span>
          改进建议
        </h3>
        <div class="content-box box-amber">{{ grading.improvementSuggestions || '暂无建议' }}</div>
      </div>

      <!-- 知识点 -->
      <div
        v-if="grading.knowledgePoints && grading.knowledgePoints !== '[]'"
        class="sd-card result-card"
      >
        <h3 class="result-title">
          <span class="result-icon icon-purple">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
            </svg>
          </span>
          涉及知识点
          <span class="count-pill">{{ knowledgeList.length }}</span>
        </h3>
        <div class="knowledge-tags">
          <span
            v-for="(kp, index) in knowledgeList"
            :key="index"
            class="knowledge-tag"
          >
            {{ kp }}
          </span>
        </div>
      </div>

      <!-- 总体评语 -->
      <div class="sd-card result-card">
        <h3 class="result-title">
          <span class="result-icon icon-green">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </span>
          总体评语
        </h3>
        <div class="content-box box-green">{{ grading.aiFeedback || '暂无评语' }}</div>
      </div>

      <div class="foot-actions">
        <button class="sd-btn sd-btn-primary" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          返回列表
        </button>
      </div>
    </template>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'HomeworkGrading',
  data() {
    return {
      loading: true,
      grading: null
    }
  },
  computed: {
    knowledgeList() {
      return this.parseKnowledgePoints(this.grading?.knowledgePoints)
    },
    scorePercent() {
      const score = parseFloat(this.grading?.score)
      if (isNaN(score)) return 0
      return Math.max(0, Math.min(100, score))
    },
    ringDash() {
      const circumference = 2 * Math.PI * 52
      const filled = (this.scorePercent / 100) * circumference
      return `${filled.toFixed(1)} ${(circumference - filled).toFixed(1)}`
    },
    scoreTone() {
      const p = this.scorePercent
      if (p >= 85) return 'tone-excellent'
      if (p >= 60) return 'tone-good'
      return 'tone-weak'
    },
    verdictText() {
      const p = this.scorePercent
      if (p >= 90) return '非常出色，继续保持'
      if (p >= 85) return '完成度很高，表现优秀'
      if (p >= 70) return '整体不错，还有提升空间'
      if (p >= 60) return '基本达标，建议再打磨'
      return '需要加强，看看改进建议'
    }
  },
  created() {
    this.loadGrading()
  },
  methods: {
    async loadGrading() {
      const submissionId = this.$route.params.submissionId
      try {
        const response = await api.get(`/homework/grading/${submissionId}`)
        if (response.success) {
          this.grading = response.data
        } else {
          console.log(response.message)
        }
      } catch (error) {
        console.error('加载批改结果失败:', error)
      } finally {
        this.loading = false
      }
    },
    parseKnowledgePoints(kpString) {
      try {
        const parsed = JSON.parse(kpString)
        return Array.isArray(parsed) ? parsed : []
      } catch (e) {
        return []
      }
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN')
    },
    goBack() {
      this.$router.push('/homework')
    }
  }
}
</script>

<style scoped>
.grading-page {
  max-width: 820px;
}

.empty-tip {
  margin: -6px 0 14px;
  font-size: 12.5px;
  color: var(--sd-text-4);
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

/* ===== 得分主卡 ===== */
.score-hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: 34px;
  padding: 32px 34px;
  margin-bottom: 18px;
  border-radius: var(--sd-radius-xl);
  color: #fff;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(59, 130, 246, 0.3);
}

.tone-excellent {
  background: linear-gradient(140deg, #059669 0%, #10b981 55%, #34d399 100%);
  box-shadow: 0 18px 40px rgba(16, 185, 129, 0.32);
}

.tone-good {
  background: linear-gradient(140deg, #2563eb 0%, #4f46e5 55%, #7c3aed 100%);
}

.tone-weak {
  background: linear-gradient(140deg, #d97706 0%, #ea580c 55%, #ef4444 100%);
  box-shadow: 0 18px 40px rgba(234, 88, 12, 0.3);
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  pointer-events: none;
}

.hero-glow-1 {
  width: 260px;
  height: 260px;
  top: -120px;
  right: -60px;
  background: rgba(255, 255, 255, 0.24);
}

.hero-glow-2 {
  width: 200px;
  height: 200px;
  bottom: -110px;
  left: -40px;
  background: rgba(255, 255, 255, 0.14);
}

.score-ring {
  position: relative;
  width: 148px;
  height: 148px;
  flex-shrink: 0;
  z-index: 1;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.24);
  stroke-width: 9;
}

.ring-fg {
  fill: none;
  stroke: #fff;
  stroke-width: 9;
  stroke-linecap: round;
  transition: stroke-dasharray 0.9s cubic-bezier(0.22, 1, 0.36, 1);
}

.ring-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.score-number {
  font-size: 44px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -1.5px;
}

.score-unit {
  font-size: 16px;
  font-weight: 600;
  opacity: 0.85;
  margin-top: 18px;
}

.score-info {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
}

.score-verdict {
  margin: 0 0 8px;
  font-size: 21px;
  font-weight: 700;
  letter-spacing: -0.2px;
}

.score-time {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.82);
}

.score-time svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.score-bar {
  height: 7px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.24);
  overflow: hidden;
}

.score-bar-fill {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: #fff;
  transition: width 0.9s cubic-bezier(0.22, 1, 0.36, 1);
}

.score-percent {
  margin: 8px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.78);
}

/* ===== 结果分区 ===== */
.result-card {
  padding: 20px 24px 22px;
  margin-bottom: 16px;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: var(--sd-text);
}

.result-icon {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
}

.result-icon svg {
  width: 16px;
  height: 16px;
}

.icon-blue { background: var(--sd-primary-light); color: var(--sd-primary); }
.icon-amber { background: #fffbeb; color: #d97706; }
.icon-purple { background: #eef2ff; color: var(--sd-accent); }
.icon-green { background: #ecfdf5; color: #059669; }

.count-pill {
  padding: 1px 9px;
  border-radius: 999px;
  background: var(--sd-primary-light);
  color: var(--sd-primary-dark);
  font-size: 12px;
  font-weight: 700;
}

.content-box {
  padding: 16px 18px;
  border-radius: var(--sd-radius);
  font-size: 14.2px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
  border-left: 3px solid transparent;
}

.box-blue {
  background: #f8fbff;
  color: var(--sd-text-2);
  border-left-color: var(--sd-primary);
}

.box-amber {
  background: #fffcf5;
  color: #78500c;
  border-left-color: #f59e0b;
}

.box-green {
  background: #f5fdf9;
  color: #14563d;
  border-left-color: var(--sd-success);
}

/* ===== 知识点标签 ===== */
.knowledge-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}

.knowledge-tag {
  padding: 6px 15px;
  border-radius: 999px;
  background: linear-gradient(135deg, #eef2ff 0%, #eff6ff 100%);
  border: 1px solid #c7d2fe;
  color: #4338ca;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.22s ease;
}

.knowledge-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.2);
  border-color: #a5b4fc;
}

/* ===== 底部操作 ===== */
.foot-actions {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .score-hero {
    flex-direction: column;
    text-align: center;
    gap: 22px;
    padding: 28px 22px;
  }

  .score-time {
    justify-content: center;
  }

  .score-info {
    width: 100%;
  }
}
</style>
