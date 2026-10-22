<template>
  <div class="sd-page ai-config-page">
    <div class="sd-page-head">
      <div>
        <h1 class="sd-title">AI 配置管理</h1>
        <p class="sd-subtitle">切换服务商、分别填写豆包 / DeepSeek 的 API Key 与模型，保存后自动生效</p>
      </div>
    </div>

    <div class="sd-card config-card">
      <!-- 服务商选择 -->
      <div class="field">
        <label class="label">当前 AI 服务商</label>
        <div class="provider-switch">
          <button
            type="button"
            class="provider-btn"
            :class="{ active: form.provider === 'doubao' }"
            @click="form.provider = 'doubao'"
          >豆包</button>
          <button
            type="button"
            class="provider-btn"
            :class="{ active: form.provider === 'deepseek' }"
            @click="form.provider = 'deepseek'"
          >DeepSeek</button>
        </div>
      </div>

      <!-- 豆包 -->
      <div class="group">
        <div class="group-title">豆包</div>
        <div class="field">
          <label class="label">API Key</label>
          <input v-model="form.doubaoApiKey" class="input" type="text" placeholder="doubao.api.key" />
        </div>
        <div class="field">
          <label class="label">模型</label>
          <input v-model="form.doubaoModel" class="input" type="text" placeholder="例如 doubao-lite-32k" />
        </div>
      </div>

      <!-- DeepSeek -->
      <div class="group">
        <div class="group-title">DeepSeek</div>
        <div class="field">
          <label class="label">API Key</label>
          <input v-model="form.deepseekApiKey" class="input" type="text" placeholder="deepseek.api.key" />
        </div>
        <div class="field">
          <label class="label">模型</label>
          <input v-model="form.deepseekModel" class="input" type="text" placeholder="例如 deepseek-chat" />
        </div>
      </div>

      <div class="actions">
        <button class="sd-btn" @click="load" :disabled="loading">刷新</button>
        <button class="sd-btn sd-btn-primary" @click="save" :disabled="loading">保存</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../utils/api'

export default {
  name: 'AiConfigManagement',
  data() {
    return {
      loading: false,
      form: {
        provider: 'deepseek',
        doubaoApiKey: '',
        doubaoModel: '',
        deepseekApiKey: '',
        deepseekModel: ''
      }
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      this.loading = true
      try {
        const r = await api.get('/admin/ai-config/values')
        if (r.success) {
          this.form.provider = r.provider || 'deepseek'
          this.form.doubaoApiKey = r.doubaoApiKey || ''
          this.form.doubaoModel = r.doubaoModel || ''
          this.form.deepseekApiKey = r.deepseekApiKey || ''
          this.form.deepseekModel = r.deepseekModel || ''
        } else {
          this.$message.error('加载失败')
        }
      } catch (e) {
        this.$message.error('无法连接 Nacos')
      } finally {
        this.loading = false
      }
    },
    async save() {
      this.loading = true
      try {
        const r = await api.post('/admin/ai-config/values', this.form)
        r.success ? this.$message.success(r.message) : this.$message.error(r.message)
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.ai-config-page { max-width: 720px; }
.config-card { padding: 24px; }

.field { margin-bottom: 16px; }
.label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--sd-text-secondary, #475569);
  margin-bottom: 6px;
}
.input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  font-size: 14px;
  color: var(--sd-text);
  outline: none;
  box-sizing: border-box;
}
.input:focus {
  border-color: var(--sd-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.group {
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  padding: 16px;
  margin-bottom: 18px;
  background: #f8fafc;
}
.group-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--sd-text);
}

.provider-switch { display: flex; gap: 12px; }
.provider-btn {
  flex: 1;
  padding: 12px;
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  background: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.provider-btn.active {
  background: var(--sd-gradient);
  color: #fff;
  border-color: transparent;
  box-shadow: var(--sd-shadow-primary);
}

.actions { display: flex; gap: 12px; margin-top: 8px; }
.sd-btn {
  padding: 10px 24px;
  border: 1px solid var(--sd-border);
  border-radius: var(--sd-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
}
.sd-btn-primary {
  background: var(--sd-gradient);
  color: #fff;
  border: none;
  box-shadow: var(--sd-shadow-primary);
}
.sd-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
