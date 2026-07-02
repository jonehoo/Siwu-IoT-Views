<template>
  <div class="share-page-container">
    <!-- 校验中加载动画 -->
    <div v-if="verifying" class="loading-container">
      <div class="spinner"></div>
      <p class="loading-text">正在安全验证分享链接...</p>
    </div>

    <!-- 错误信息展示 -->
    <div v-else-if="errorMsg" class="error-container">
      <div class="error-card">
        <i class="el-icon-warning error-icon"></i>
        <h3 class="error-title">无法查看此大屏</h3>
        <p class="error-description">{{ errorMsg }}</p>
      </div>
    </div>

    <!-- 密码输入框 -->
    <div v-else-if="needPassword" class="password-container">
      <div class="password-card">
        <div class="card-header">
          <div class="lock-icon-wrap">
            <i class="el-icon-lock lock-icon"></i>
          </div>
          <h2 class="card-title">输入访问密码</h2>
          <p class="card-subtitle">该大屏已被加密保护，请输入访问密码以继续</p>
        </div>
        
        <div class="card-body">
          <el-input
            v-model="password"
            placeholder="请输入访问密码"
            show-password
            class="password-input"
            @keyup.enter.native="submitPassword"
            :disabled="submitting"
          />
          
          <p v-if="pwdError" class="pwd-error-msg">
            <i class="el-icon-circle-close"></i> {{ pwdError }}
          </p>

          <el-button
            type="primary"
            class="submit-btn"
            @click="submitPassword"
            :loading="submitting"
          >
            确认进入
          </el-button>
        </div>
      </div>
    </div>

    <!-- 验证通过，渲染大屏 -->
    <div v-else-if="verified" class="canvas-container">
      <BigScreenShare :config="shareConfig" />
    </div>
  </div>
</template>

<script>
import BigScreenShare from 'data-room-ui/BigScreenShare/index.vue'
import axios from 'axios'

export default {
  name: 'ShareView',
  components: {
    BigScreenShare
  },
  data() {
    return {
      verifying: true,
      errorMsg: '',
      needPassword: false,
      password: '',
      pwdError: '',
      submitting: false,
      verified: false,
      pageCode: '',
      shareConfig: {
        code: '',
        fitSelector: '.share-page-container',
        fitMode: 'auto'
      }
    }
  },
  computed: {
    shareToken() {
      return this.$route.params.shareToken
    }
  },
  created() {
    this.verifyShareToken()
  },
  methods: {
    // 首次验证
    verifyShareToken() {
      this.verifying = true
      this.errorMsg = ''
      
      const baseURL = process.env.VUE_APP_BASE_API || ''
      axios.get(`${baseURL}/api/public/share/verify/${this.shareToken}`)
        .then(response => {
          const res = response.data
          if (res.code === 200) {
            if (res.data.needPassword) {
              this.needPassword = true
            } else {
              this.applyAccessToken(res.data.shareAccessToken, res.data.pageCode)
            }
          } else {
            this.errorMsg = res.msg || '链接已失效'
          }
        })
        .catch(error => {
          this.errorMsg = '网络错误，无法连接到服务器'
          console.error(error)
        })
        .finally(() => {
          this.verifying = false
        })
    },

    // 提交密码验证
    submitPassword() {
      if (!this.password) {
        this.pwdError = '请输入密码'
        return
      }
      this.pwdError = ''
      this.submitting = true
      
      const baseURL = process.env.VUE_APP_BASE_API || ''
      axios.post(`${baseURL}/api/public/share/verify/${this.shareToken}`, {
        password: this.password
      })
        .then(response => {
          const res = response.data
          if (res.code === 200) {
            this.applyAccessToken(res.data.shareAccessToken, res.data.pageCode)
          } else {
            this.pwdError = res.msg || '密码错误'
          }
        })
        .catch(error => {
          this.pwdError = '校验失败，请重试'
          console.error(error)
        })
        .finally(() => {
          this.submitting = false
        })
    },

    // 写入 Token 并显示看板
    applyAccessToken(token, pageCode) {
      this.pageCode = pageCode
      this.shareConfig.code = pageCode

      // 注册全局配置，使 Dataroom Axios 的所有请求带上临时 Token
      if (!window.BS_CONFIG) {
        window.BS_CONFIG = {}
      }
      if (!window.BS_CONFIG.httpConfigs) {
        window.BS_CONFIG.httpConfigs = {}
      }
      if (!window.BS_CONFIG.httpConfigs.headers) {
        window.BS_CONFIG.httpConfigs.headers = {}
      }
      window.BS_CONFIG.httpConfigs.headers['X-Share-Access-Token'] = token

      this.needPassword = false
      this.verified = true
    }
  }
}
</script>

<style scoped>
/* 整个分享预览页样式 - 炫丽科技感渐变背景 */
.share-page-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background: radial-gradient(circle at 30% 30%, #1a2238 0%, #0d1220 50%, #05070f 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #e2e8f0;
}

/* 校验中的加载页面 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(59, 130, 246, 0.1);
  border-left-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 20px;
  font-size: 16px;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

/* 错误提示卡片 */
.error-container {
  padding: 20px;
  z-index: 10;
}

.error-card {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 440px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
}

.error-icon {
  font-size: 56px;
  color: #ef4444;
  margin-bottom: 20px;
}

.error-title {
  font-size: 22px;
  font-weight: 700;
  color: #f8fafc;
  margin-bottom: 12px;
}

.error-description {
  font-size: 15px;
  color: #94a3b8;
  line-height: 1.6;
}

/* 密码输入卡片 */
.password-container {
  padding: 20px;
  z-index: 10;
}

.password-card {
  background: rgba(30, 41, 59, 0.45);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 40px;
  width: 380px;
  box-shadow: 
    0 30px 60px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.05) inset;
  text-align: center;
  transform: translateY(-20px);
  animation: slideInUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes slideInUp {
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.lock-icon-wrap {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(6, 182, 212, 0.15));
  border: 1px solid rgba(59, 130, 246, 0.25);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.lock-icon {
  font-size: 28px;
  color: #3b82f6;
  background: linear-gradient(135deg, #60a5fa, #22d3ee);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 32px;
  line-height: 1.5;
}

.password-input >>> .el-input__inner {
  height: 50px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #f8fafc;
  font-size: 16px;
  padding: 0 16px;
  transition: all 0.3s ease;
}

.password-input >>> .el-input__inner:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
  background: rgba(15, 23, 42, 0.8);
}

.pwd-error-msg {
  color: #f87171;
  font-size: 13px;
  margin: 10px 0 0;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 4px;
  animation: shake 0.4s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  75% { transform: translateX(6px); }
}

.submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.25);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(59, 130, 246, 0.35);
  opacity: 0.95;
}

.submit-btn:active {
  transform: translateY(0);
}

/* 验证通过后的大屏展示画布 */
.canvas-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  z-index: 1;
}
</style>
