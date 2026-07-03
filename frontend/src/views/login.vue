<template>
  <div class="login-container">
    <!-- 背景流光与粒子 -->
    <div class="bg-glow bg-glow-1" />
    <div class="bg-glow bg-glow-2" />
    <div class="bg-glow bg-glow-3" />
    <div class="cyber-grid" />

    <div class="login-box">
      <!-- 左侧科技星轨数据中心 - 桌面端显示 -->
      <div class="login-left hidden-sm-and-down">
        <div class="tech-graphic-container">
          <div class="tech-grid" />
          <div class="tech-glow-orb" />
          <div class="tech-glow-orb secondary" />
          <!-- 科技面板装饰十字标 -->
          <div class="tech-marker marker-tl">+</div>
          <div class="tech-marker marker-tr">+</div>
          <div class="tech-marker marker-bl">+</div>
          <div class="tech-marker marker-br">+</div>
          <!-- 数据流状态文字 -->
          <div class="system-status-overlay">
            <div class="status-item"><span class="status-label">SYSTEM:</span> <span class="status-value text-glow-cyan">ACTIVE</span></div>
            <div class="status-item"><span class="status-label">DATA ROOM:</span> <span class="status-value">SECURE</span></div>
            <div class="status-item"><span class="status-label">DEVICES:</span> <span class="status-value text-glow-indigo">ONLINE</span></div>
          </div>
          <div class="orbit orbit-1">
            <div class="orbit-node node-1" />
            <div class="orbit-node node-2" />
          </div>
          <div class="orbit orbit-2">
            <div class="orbit-node node-3" />
          </div>
          <div class="orbit orbit-3">
            <div class="orbit-node node-4" />
            <div class="orbit-node node-5" />
          </div>
          <div class="central-core">
            <div class="core-logo">
              <div class="core-pulse-1" />
              <div class="core-pulse-2" />
              <img :src="logo" class="core-logo-icon">
            </div>
            <div class="core-text">SIWU IoT</div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-right">
        <!-- 移动端顶部返回按钮 -->
        <div v-if="showBackButton" class="mobile-back hidden-md-and-up">
          <el-button icon="el-icon-arrow-left" circle @click="handleBack" />
        </div>

        <div class="login-header">
          <div class="brand-logo-container">
            <img :src="logo" class="brand-logo" alt="肆物联云">
          </div>
          <h2>欢迎登录</h2>
          <p>请选择您的登录方式</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div class="tab-slider-wrapper">
            <div class="tab-slider-bg" :style="{ left: isPasswordLogin ? '50%' : '0%' }" />
            <button
              :class="['tab-button-custom', { 'active': !isPasswordLogin }]"
              :disabled="loading"
              @click="!loading && (isPasswordLogin = false)"
            >
              <i class="el-icon-mobile-phone" />
              <span class="button-text">动态密码登录</span>
            </button>
            <button
              :class="['tab-button-custom', { 'active': isPasswordLogin }]"
              :disabled="loading"
              @click="!loading && (isPasswordLogin = true)"
            >
              <i class="el-icon-user" />
              <span class="button-text">账号密码登录</span>
            </button>
          </div>
        </div>

        <!-- 账号密码登录表单 -->
        <div v-if="isPasswordLogin" class="login-form">
          <!-- 加载遮罩层 -->
          <div v-if="loading" class="loading-overlay">
            <div class="loading-content">
              <i class="el-icon-loading loading-icon" />
              <p class="loading-text">正在登录...</p>
            </div>
          </div>

          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" @keyup.enter.native="handleLogin">
            <el-form-item prop="username" class="form-item">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                class="login-input"
                :disabled="loading"
              />
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <el-input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                prefix-icon="el-icon-lock"
                class="login-input"
                :disabled="loading"
              >
                <i slot="suffix" class="el-icon-view password-icon" @click="showPassword = !showPassword" />
              </el-input>
            </el-form-item>
          </el-form>

          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" :disabled="loading">记住密码</el-checkbox>
            <el-link type="primary" class="forgot-password" :class="{ 'disabled': loading }">忘记密码？</el-link>
          </div>

          <el-button type="primary" class="login-button" :loading="loading" :disabled="loading" @click="handleLogin">
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </div>

        <!-- 扫码登录 -->
        <div v-else class="qrcode-login">
          <!-- 加载遮罩层 -->
          <div v-if="loading" class="loading-overlay">
            <div class="loading-content">
              <i class="el-icon-loading loading-icon" />
              <p class="loading-text">正在验证...</p>
            </div>
          </div>

          <div class="qrcode-wrapper">
            <div class="qrcode-scan-line" />
            <div class="qrcode-placeholder" />
            <div class="corner corner-tl" />
            <div class="corner corner-tr" />
            <div class="corner corner-bl" />
            <div class="corner corner-br" />
          </div>

          <!-- 动态密码输入区域 -->
          <div v-if="showCodeInput" class="dynamic-code-container">
            <p class="dynamic-code-title">请输入微信获取的动态密码</p>
            <div class="dynamic-code-inputs">
              <el-input
                v-for="(code, index) in dynamicCode"
                :key="index"
                ref="dynamicCodeInputs"
                v-model="dynamicCode[index]"
                class="dynamic-code-input"
                maxlength="1"
                :disabled="loading"
                @input="handleCodeInput(index, $event)"
                @keyup.delete.native="handleCodeDelete(index)"
              />
            </div>
            <p v-if="codeError" class="code-hint">{{ codeError }}</p>
            <p v-if="loading" class="loading-hint">
              <i class="el-icon-loading" /> 正在验证动态密码...
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { setToken } from '@/utils/auth'
import { wxLogin } from '@/api/login'

export default {
  data() {
    return {
      logo: require('@/assets/images/肆物联云.svg'),
      // 动态密码相关数据
      dynamicCode: ['', '', '', ''], // 4位动态密码
      showCodeInput: true, // 控制是否显示输入框
      codeError: '', // 错误提示
      gzh: require('../assets/images/gzh.jpg'),
      isPasswordLogin: false,
      showPassword: false,
      showBackButton: false,
      loginForm: {
        username: '',
        password: '',
        remember: false
      },
      loginRules: {
        username: [
          { required: true, trigger: 'blur', message: '请输入您的账号' }
        ],
        password: [
          { required: true, trigger: 'blur', message: '请输入您的密码' }
        ],
        code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
      },
      loading: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  mounted() {
    this.checkScreenSize()
    window.addEventListener('resize', this.checkScreenSize)
  },
  created() {
    this.getCookie()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkScreenSize)
  },
  methods: {
    getCookie() {
      const username = localStorage.getItem('username')
      const remember = localStorage.getItem('remember')
      this.loginForm = {
        username: username || this.loginForm.username,
        password: '', // 出于安全考虑，不存储密码
        remember: remember === 'true'
      }
    },
    async handleLogin() {
      if (this.loading) {
        return
      }

      if (!this.loginForm.username) {
        this.$message.error('请输入用户名')
        return
      }
      if (!this.loginForm.password) {
        this.$message.error('请输入密码')
        return
      }

      this.loading = true

      try {
        if (this.loginForm.remember) {
          localStorage.setItem('username', this.loginForm.username)
          localStorage.setItem('remember', 'true')
        } else {
          localStorage.removeItem('username')
          localStorage.removeItem('remember')
        }

        await this.$store.dispatch('Login', this.loginForm)
        this.$message.success('登录成功')
        this.$router.push({ path: this.redirect || '/' }).catch(() => {
          this.loading = false
        })
      } catch (error) {
        console.error('登录失败:', error)
        this.$message.error(error.message || '登录失败，请重试')
        this.loading = false
      }
    },

    handleBack() {
      if (this.$router.history.current.name) {
        this.$router.go(-1)
      } else {
        this.$router.push('/')
      }
    },
    checkScreenSize() {
      this.showBackButton = window.innerWidth < 768
    },
    handleCodeInput(index, value) {
      if (value && /^\d$/.test(value)) {
        this.dynamicCode[index] = value
        if (index < 3) {
          this.$nextTick(() => {
            const inputs = this.$refs.dynamicCodeInputs
            if (inputs && inputs[index + 1]) {
              inputs[index + 1].focus()
            }
          })
        }
        if (index === 3) {
          this.verifyDynamicCode()
        }
      } else {
        this.dynamicCode[index] = ''
      }
    },

    handleCodeDelete(index) {
      const inputs = this.$refs.dynamicCodeInputs
      if (!inputs) {
        return
      }

      if (this.dynamicCode[index]) {
        this.dynamicCode[index] = ''
        this.codeError = ''
      } else if (index > 0) {
        this.dynamicCode[index - 1] = ''
        this.codeError = ''
        this.$nextTick(() => {
          if (inputs[index - 1]) {
            inputs[index - 1].focus()
          }
        })
      }
    },

    verifyDynamicCode() {
      if (this.loading) {
        return
      }

      const code = this.dynamicCode.join('')
      if (code.length !== 4) {
        return
      }

      this.codeError = ''
      this.loading = true

      wxLogin({ code }).then(res => {
        if (res.code === 200) {
          setToken(res.token)
          this.$message.success('登录成功')
          this.$router.push({ path: this.redirect || '/index' }).catch(() => {
            this.loading = false
          })
        } else {
          this.codeError = res.msg || '动态密码错误，请重新输入'
          this.resetCodeInput()
          this.loading = false
        }
      }).catch(err => {
        this.codeError = err.message || '动态密码错误，请重新输入'
        this.resetCodeInput()
        this.loading = false
      })
    },

    resetCodeInput() {
      this.dynamicCode = ['', '', '', '']
      this.$nextTick(() => {
        const inputs = this.$refs.dynamicCodeInputs
        if (inputs && inputs[0]) {
          inputs[0].focus()
        }
      })
    }
  }
}
</script>

<style scoped>
/* 渐变流光背景 - 沉浸式暗色太空 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: #060913;
}

/* 3D 纵深感科技网格 */
.cyber-grid {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background-image:
    linear-gradient(rgba(0, 240, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.06) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(600px) rotateX(60deg);
  animation: grid-move 25s linear infinite;
  opacity: 0.55;
  z-index: 1;
  pointer-events: none;
}

@keyframes grid-move {
  0% { transform: perspective(600px) rotateX(60deg) translateY(0); }
  100% { transform: perspective(600px) rotateX(60deg) translateY(50px); }
}

/* 装饰性背景星云流光 */
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(130px);
  opacity: 0.35;
  z-index: 0;
  pointer-events: none;
}

.bg-glow-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.22) 0%, transparent 70%);
  top: -150px;
  right: -100px;
  animation: float-glow-1 20s ease-in-out infinite;
}

.bg-glow-2 {
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.2) 0%, transparent 70%);
  bottom: -200px;
  left: -150px;
  animation: float-glow-2 24s ease-in-out infinite alternate;
}

.bg-glow-3 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.16) 0%, transparent 70%);
  top: 25%;
  left: 15%;
  animation: float-glow-3 18s ease-in-out infinite;
}

@keyframes float-glow-1 {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, 30px) scale(1.1); }
  100% { transform: translate(0, 0) scale(1); }
}

@keyframes float-glow-2 {
  0% { transform: translate(0, 0) scale(1.1); }
  50% { transform: translate(-30px, -40px) scale(0.9); }
  100% { transform: translate(0, 0) scale(1.1); }
}

@keyframes float-glow-3 {
  0% { transform: translate(0, 0) scale(0.9); }
  50% { transform: translate(25px, -25px) scale(1.05); }
  100% { transform: translate(0, 0) scale(0.9); }
}

/* 登录框 - 暗色磨砂玻璃态与渐变高光边框 */
.login-box {
  display: flex;
  width: 100%;
  max-width: 1040px;
  min-height: 620px;
  position: relative;
  z-index: 2;
  background: rgba(10, 18, 36, 0.45);
  backdrop-filter: blur(25px) saturate(180%);
  -webkit-backdrop-filter: blur(25px) saturate(180%);
  border-radius: 32px;
  box-shadow:
    0 30px 80px rgba(0, 0, 0, 0.5),
    0 0 100px rgba(14, 165, 233, 0.12) inset,
    0 0 40px rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
  flex-direction: column;
  animation: slideUp 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧科技图形中心 */
.login-left {
  width: 100%;
  height: 240px;
  position: relative;
  overflow: hidden;
}

.tech-graphic-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(135deg, #090e17 0%, #111a2e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.04);
}

.tech-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.015) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.015) 1px, transparent 1px);
  background-size: 30px 30px;
  background-position: center;
  pointer-events: none;
}

.tech-glow-orb {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.15) 0%, transparent 70%);
  top: 10%;
  left: 10%;
  animation: float-glow-1 12s infinite alternate;
}

.tech-glow-orb.secondary {
  background: radial-gradient(circle, rgba(99, 102, 241, 0.12) 0%, transparent 70%);
  bottom: 10%;
  right: 10%;
  animation: float-glow-2 15s infinite alternate-reverse;
}

/* 科技面板装饰十字标 */
.tech-marker {
  position: absolute;
  color: rgba(0, 240, 255, 0.25);
  font-family: monospace;
  font-size: 14px;
  user-select: none;
  pointer-events: none;
}
.marker-tl { top: 12px; left: 12px; }
.marker-tr { top: 12px; right: 12px; }
.marker-bl { bottom: 12px; left: 12px; }
.marker-br { bottom: 12px; right: 12px; }

/* 系统数据流文字叠加 */
.system-status-overlay {
  position: absolute;
  top: 24px;
  left: 24px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  line-height: 1.6;
  pointer-events: none;
  z-index: 3;
}
.status-item {
  margin-bottom: 4px;
  letter-spacing: 1px;
}
.status-label {
  color: rgba(255, 255, 255, 0.25);
}
.status-value {
  font-weight: bold;
}
.text-glow-cyan {
  color: #00f0ff;
  text-shadow: 0 0 6px rgba(0, 240, 255, 0.6);
}
.text-glow-indigo {
  color: #818cf8;
  text-shadow: 0 0 6px rgba(129, 140, 248, 0.6);
}

/* 运行星轨 - 发光 */
.orbit {
  position: absolute;
  border: 1px dashed rgba(0, 240, 255, 0.12);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.orbit-1 {
  width: 240px;
  height: 240px;
  animation: rotate-clockwise 25s linear infinite;
}

.orbit-2 {
  width: 340px;
  height: 340px;
  animation: rotate-counter-clockwise 35s linear infinite;
}

.orbit-3 {
  width: 440px;
  height: 440px;
  animation: rotate-clockwise 45s linear infinite;
}

/* 节点样式 - 荧光发光 */
.orbit-node {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.node-1 { top: 0; left: 50%; transform: translateX(-50%); background: #00f0ff; box-shadow: 0 0 15px #00f0ff, 0 0 25px rgba(0, 240, 255, 0.6); }
.node-2 { bottom: 0; left: 50%; transform: translateX(-50%); background: #38bdf8; box-shadow: 0 0 12px #38bdf8, 0 0 20px rgba(56, 189, 248, 0.5); }
.node-3 { left: 0; top: 50%; transform: translateY(-50%); background: #a855f7; box-shadow: 0 0 15px #a855f7, 0 0 25px rgba(168, 85, 247, 0.6); }
.node-4 { right: 10%; top: 25%; background: #06b6d4; box-shadow: 0 0 12px #06b6d4, 0 0 20px rgba(6, 182, 212, 0.5); }
.node-5 { left: 10%; bottom: 25%; background: #10b981; box-shadow: 0 0 15px #10b981, 0 0 25px rgba(16, 185, 129, 0.6); }

@keyframes rotate-clockwise {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes rotate-counter-clockwise {
  from { transform: rotate(0deg); }
  to { transform: rotate(-360deg); }
}

/* 核心云端节点 - 反应堆旋转交互 */
.central-core {
  position: relative;
  width: 220px;
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
}

.core-logo {
  position: relative;
  width: 170px;
  height: 170px;
  background: rgba(8, 14, 27, 0.95);
  border: 2px solid rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 0 35px rgba(0, 240, 255, 0.5),
    0 0 15px rgba(0, 240, 255, 0.3) inset;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.core-logo:hover {
  transform: scale(1.06) rotate(180deg);
  border-color: #a855f7;
  box-shadow:
    0 0 45px rgba(168, 85, 247, 0.65),
    0 0 15px rgba(168, 85, 247, 0.4) inset;
}

.core-logo-icon {
  width: 110px;
  height: 110px;
  object-fit: contain;
  filter: brightness(0) invert(1) drop-shadow(0 0 6px rgba(0, 240, 255, 0.8));
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  transform: rotate(0deg);
}

.core-logo:hover .core-logo-icon {
  transform: rotate(-180deg);
  filter: brightness(0) invert(1) drop-shadow(0 0 6px rgba(168, 85, 247, 0.8));
}

.core-pulse-1, .core-pulse-2 {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(0, 240, 255, 0.35);
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  pointer-events: none;
}

.core-pulse-1 {
  animation: corePulse 3s cubic-bezier(0.25, 0.8, 0.25, 1) infinite;
}

.core-pulse-2 {
  animation: corePulse 3s cubic-bezier(0.25, 0.8, 0.25, 1) infinite;
  animation-delay: 1.5s;
}

@keyframes corePulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.2); opacity: 0; }
}

.core-text {
  margin-top: 14px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.6);
  text-transform: uppercase;
}

.core-pulse-1, .core-pulse-2 {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(0, 240, 255, 0.35);
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  pointer-events: none;
}

.core-pulse-1 {
  animation: corePulse 3s cubic-bezier(0.25, 0.8, 0.25, 1) infinite;
}

.core-pulse-2 {
  animation: corePulse 3s cubic-bezier(0.25, 0.8, 0.25, 1) infinite;
  animation-delay: 1.5s;
}

@keyframes corePulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.2); opacity: 0; }
}

.core-text {
  margin-top: 10px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.6);
  text-transform: uppercase;
}

/* 右侧登录区域 */
.login-right {
  width: 100%;
  padding: 32px;
  display: flex;
  flex-direction: column;
  background: rgba(8, 14, 27, 0.35);
  position: relative;
  border-left: 1px solid rgba(255, 255, 255, 0.02);
}

/* 移动端返回按钮 */
.mobile-back {
  margin-bottom: 16px;
}

.mobile-back .el-button {
  padding: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #00f0ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.mobile-back .el-button:hover {
  transform: translateX(-3px);
  box-shadow: 0 4px 12px rgba(0, 240, 255, 0.25);
  background: rgba(255, 255, 255, 0.08);
}

/* 登录头部 - 渐变高光标题 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
  animation: fadeInDown 0.6s ease-out 0.2s both;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.brand-logo {
  height: 110px;
  object-fit: contain;
  filter: brightness(0) invert(1) drop-shadow(0 4px 8px rgba(255, 255, 255, 0.15));
  transition: all 0.3s ease;
}

.brand-logo:hover {
  transform: scale(1.02);
  filter: brightness(0) invert(1) drop-shadow(0 6px 12px rgba(0, 240, 255, 0.35));
}

.login-header h2 {
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #cbd5e1 50%, #38bdf8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.login-header p {
  color: #94a3b8;
  font-size: 15px;
  font-weight: 400;
}

/* 登录选项卡样式 - 胶囊滑动切换 */
.login-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  animation: fadeInDown 0.6s ease-out 0.3s both;
  z-index: 2;
}

.tab-slider-wrapper {
  position: relative;
  display: flex;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  padding: 5px;
  border-radius: 16px;
  width: 100%;
}

.tab-slider-bg {
  position: absolute;
  top: 5px;
  bottom: 5px;
  width: calc(50% - 5px);
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(0, 240, 255, 0.25);
  box-shadow:
    0 4px 15px rgba(0, 240, 255, 0.15),
    inset 0 1px 1px rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  transition: left 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.tab-button-custom {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  z-index: 2;
  transition: color 0.3s ease;
  border-radius: 12px;
  outline: none;
}

.tab-button-custom i {
  font-size: 16px;
}

.tab-button-custom:hover:not(.active):not(:disabled) {
  color: #f1f5f9;
}

.tab-button-custom.active {
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.4);
}

.tab-button-custom:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 登录表单样式 */
.login-form {
  width: 100%;
  margin-top: 8px;
  min-height: 380px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  animation: fadeInDown 0.6s ease-out 0.4s both;
  position: relative;
}

.form-item {
  margin-bottom: 24px;
}

/* Element UI 输入框重构 - 极客暗黑风 */
.login-input >>> .el-input__inner {
  height: 54px;
  line-height: 54px;
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 14px;
  padding: 0 20px 0 52px;
  font-size: 15px;
  color: #f8fafc;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) inset;
}

.login-input >>> .el-input__inner::placeholder {
  color: #64748b;
}

.login-input >>> .el-input__inner:hover {
  border-color: rgba(0, 240, 255, 0.35) !important;
  background: rgba(255, 255, 255, 0.05) !important;
}

.login-input >>> .el-input__inner:focus {
  border-color: #00f0ff !important;
  background: rgba(255, 255, 255, 0.06) !important;
  box-shadow:
    0 0 15px rgba(0, 240, 255, 0.2),
    0 8px 16px rgba(0, 0, 0, 0.3);
  transform: translateY(-1px);
}

.login-input >>> .el-input__prefix {
  left: 18px;
  display: flex;
  align-items: center;
}

.login-input >>> .el-input__prefix i {
  color: #64748b;
  font-size: 18px;
  transition: color 0.3s ease;
}

.login-input:focus-within >>> .el-input__prefix i {
  color: #00f0ff;
  text-shadow: 0 0 6px rgba(0, 240, 255, 0.5);
}

.password-icon {
  cursor: pointer;
  color: #64748b;
  font-size: 18px;
  transition: all 0.3s ease;
  padding: 4px;
}

.password-icon:hover {
  color: #00f0ff;
  transform: scale(1.1);
}

/* 表单选项样式 - 科技青色改造 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 24px 0;
}

.form-options >>> .el-checkbox__label {
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
}

.form-options >>> .el-checkbox__inner {
  background-color: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.15);
  border-radius: 4px;
}

.form-options >>> .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #00f0ff;
  border-color: #00f0ff;
}

.form-options >>> .el-checkbox__input.is-focus .el-checkbox__inner {
  border-color: #00f0ff;
}

.forgot-password {
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
}

.forgot-password:hover {
  color: #00f0ff;
  text-shadow: 0 0 6px rgba(0, 240, 255, 0.4);
}

.forgot-password.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

/* 登录按钮样式 - 渐变极光扫光 */
.login-button {
  width: 100%;
  height: 54px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #4f46e5 0%, #00f0ff 100%) !important;
  border: none !important;
  box-shadow:
    0 8px 24px rgba(79, 70, 229, 0.3),
    0 2px 6px rgba(0, 240, 255, 0.2);
  color: #ffffff !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: none;
}

.login-button:hover::before {
  left: 100%;
  transition: left 0.8s ease-in-out;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 28px rgba(79, 70, 229, 0.45),
    0 0 20px rgba(0, 240, 255, 0.45);
}

.login-button:active {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 二维码登录样式 */
.qrcode-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 28px;
  padding: 12px 0;
  min-height: 380px;
  animation: fadeInDown 0.6s ease-out 0.4s both;
  position: relative;
}

/* 加载遮罩层 - 毛玻璃暗黑 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(8, 14, 27, 0.7);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.35s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-icon {
  font-size: 44px;
  color: #00f0ff;
  animation: rotate 1s linear infinite;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
  font-weight: 500;
  color: #00f0ff;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
}

/* 二维码区域 */
.qrcode-wrapper {
  position: relative;
  width: 220px;
  height: 220px;
  padding: 10px;
  background: rgba(10, 18, 36, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  box-shadow:
    0 6px 20px rgba(0, 0, 0, 0.3),
    0 0 15px rgba(0, 240, 255, 0.1) inset;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-placeholder {
  width: 198px;
  height: 198px;
  border-radius: 12px;
  background-image: url('../assets/images/gzh.jpg');
  background-size: cover;
  background-position: center;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5) inset;
  transition: all 0.4s ease;
  overflow: hidden;
}

.qrcode-wrapper:hover .qrcode-placeholder {
  transform: scale(1.03);
}

/* 激光扫描线 */
.qrcode-scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(to right, transparent, rgba(0, 240, 255, 0.8), #00f0ff, rgba(0, 240, 255, 0.8), transparent);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.8);
  animation: scan 3s linear infinite;
  z-index: 3;
  pointer-events: none;
}

@keyframes scan {
  0% { top: 5%; }
  50% { top: 92%; }
  100% { top: 5%; }
}

/* 四个角边框装饰 */
.corner {
  position: absolute;
  width: 16px;
  height: 16px;
  border-color: #00f0ff;
  border-style: solid;
  pointer-events: none;
  filter: drop-shadow(0 0 4px rgba(0, 240, 255, 0.5));
}

.corner-tl {
  top: 8px;
  left: 8px;
  border-width: 3px 0 0 3px;
  border-top-left-radius: 6px;
}

.corner-tr {
  top: 8px;
  right: 8px;
  border-width: 3px 3px 0 0;
  border-top-right-radius: 6px;
}

.corner-bl {
  bottom: 8px;
  left: 8px;
  border-width: 0 0 3px 3px;
  border-bottom-left-radius: 6px;
}

.corner-br {
  bottom: 8px;
  right: 8px;
  border-width: 0 3px 3px 0;
  border-bottom-right-radius: 6px;
}

/* 动态密码输入区域 */
.dynamic-code-container {
  margin-top: 10px;
  text-align: center;
  width: 100%;
}

.dynamic-code-title {
  color: #94a3b8;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 20px;
  letter-spacing: 1px;
}

.dynamic-code-inputs {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-bottom: 12px;
}

.dynamic-code-input {
  width: 56px;
  height: 56px;
}

.dynamic-code-input >>> .el-input__inner {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03) !important;
  color: #00f0ff !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.2);
}

.dynamic-code-input >>> .el-input__inner:focus {
  border-color: #00f0ff !important;
  background: rgba(255, 255, 255, 0.05) !important;
  box-shadow:
    0 0 15px rgba(0, 240, 255, 0.35),
    0 4px 12px rgba(0, 0, 0, 0.3);
  transform: scale(1.05);
}

.code-hint {
  color: #f87171;
  font-size: 13px;
  font-weight: 500;
  height: 24px;
  margin-top: 8px;
  transition: all 0.3s ease;
  animation: shake 0.5s ease;
}

.loading-hint {
  color: #00f0ff;
  font-size: 14px;
  font-weight: 500;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-hint i {
  animation: rotate 1s linear infinite;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  75% { transform: translateX(8px); }
}

/* 响应式设计 */
@media (min-width: 768px) {
  .login-box {
    flex-direction: row;
    min-height: 640px;
    border-radius: 32px;
  }

  .login-left {
    width: 50%;
    height: auto;
  }

  .login-right {
    width: 50%;
    padding: 48px;
    background: rgba(8, 14, 27, 0.35);
  }

  .login-header h2 {
    font-size: 28px;
  }
}

@media (max-width: 767px) {
  .login-container {
    padding: 12px;
  }

  .login-box {
    min-height: auto;
    border-radius: 24px;
  }

  .login-right {
    padding: 32px 24px;
    background: rgba(8, 14, 27, 0.35);
  }

  .login-header h2 {
    font-size: 24px;
  }

  .tab-button-custom {
    padding: 10px 12px;
    font-size: 13px;
  }

  .button-text {
    display: none;
  }

  .qrcode-wrapper {
    width: 200px;
    height: 200px;
  }

  .qrcode-placeholder {
    width: 178px;
    height: 178px;
  }

  .dynamic-code-input {
    width: 52px;
    height: 52px;
  }

  .dynamic-code-input >>> .el-input__inner {
    height: 52px;
    line-height: 52px;
    font-size: 22px;
  }
}

.hidden-sm-and-down {
  display: none;
}

@media (min-width: 768px) {
  .hidden-sm-and-down {
    display: block;
  }

  .hidden-md-and-up {
    display: none;
  }
}

/* 滚动条美化 */
.login-right::-webkit-scrollbar {
  width: 6px;
}

.login-right::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.login-right::-webkit-scrollbar-thumb {
  background: rgba(0, 240, 255, 0.2);
  border-radius: 3px;
}

.login-right::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 240, 255, 0.4);
}
</style>
