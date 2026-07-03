<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 左侧图片区域 - 桌面端显示 -->
      <div class="login-left hidden-sm-and-down">
        <img src="https://ai-public.mastergo.com/ai/img_res/2a65a44cbb22796876b76618f862b2ef.jpg" class="login-image"
          alt="登录背景" />
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-right">
        <!-- 移动端顶部返回按钮 -->
        <div class="mobile-back hidden-md-and-up" v-if="showBackButton">
          <el-button icon="el-icon-arrow-left" circle @click="handleBack"></el-button>
        </div>

        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请选择您的登录方式</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div class="tab-buttons">
            <el-button :class="['tab-button', { 'active': !isPasswordLogin, 'disabled': loading }]" 
              @click="!loading && (isPasswordLogin = false)"
              :disabled="loading">
              <i class="el-icon-mobile-phone"></i>
              <span class="button-text">动态密码登录</span>
            </el-button>
            <el-button :class="['tab-button', { 'active': isPasswordLogin, 'disabled': loading }]" 
              @click="!loading && (isPasswordLogin = true)"
              :disabled="loading">
              <i class="el-icon-user"></i>
              <span class="button-text">账号密码登录</span>
            </el-button>

          </div>
        </div>

        <!-- 账号密码登录表单 -->
        <div v-if="isPasswordLogin" class="login-form">
          <!-- 加载遮罩层 -->
          <div v-if="loading" class="loading-overlay">
            <div class="loading-content">
              <i class="el-icon-loading loading-icon"></i>
              <p class="loading-text">正在登录...</p>
            </div>
          </div>

          <el-form :model="loginForm" :rules="loginRules" ref="loginForm" @keyup.enter.native="handleLogin">

            <el-form-item prop="username" class="form-item">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user"
                class="login-input" :disabled="loading">
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <el-input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码"
                prefix-icon="el-icon-lock" class="login-input" :disabled="loading">
                <i slot="suffix" class="el-icon-view password-icon" @click="showPassword = !showPassword">
                </i>
              </el-input>
            </el-form-item>
          </el-form>

          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" :disabled="loading">记住密码</el-checkbox>
            <el-link type="primary" class="forgot-password" :class="{ 'disabled': loading }">忘记密码？</el-link>
          </div>

          <el-button type="primary" class="login-button" @click="handleLogin" :loading="loading" :disabled="loading">
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </el-button>

          <!-- <div class="register-link">
            <span>还没有账号？</span>
            <el-link type="primary">立即注册</el-link>
          </div> -->
        </div>

        <!-- 扫码登录 -->
        <div v-else class="qrcode-login">
          <!-- 加载遮罩层 -->
          <div v-if="loading" class="loading-overlay">
            <div class="loading-content">
              <i class="el-icon-loading loading-icon"></i>
              <p class="loading-text">正在验证...</p>
            </div>
          </div>

          <div class="qrcode-placeholder">
            <!-- <i class="el-icon-picture-outline"></i> -->
            <!-- <img class="img-logo" :src="gzh"> -->
          </div>
          <!-- <p>请使用微信扫码登录</p> -->
          <!-- 动态密码输入区域 -->
          <div class="dynamic-code-container" v-if="showCodeInput">
            <p class="dynamic-code-title">请输入微信获取的动态密码</p>
            <div class="dynamic-code-inputs">
              <el-input v-for="(code, index) in dynamicCode" :key="index" v-model="dynamicCode[index]"
                ref="dynamicCodeInputs" class="dynamic-code-input" maxlength="1" 
                @input="handleCodeInput(index, $event)"
                @keyup.delete.native="handleCodeDelete(index)"
                :disabled="loading"></el-input>
            </div>
            <p class="code-hint" v-if="codeError">{{ codeError }}</p>
            <p v-if="loading" class="loading-hint">
              <i class="el-icon-loading"></i> 正在验证动态密码...
            </p>
          </div>
        </div>

        <!-- 社交媒体登录 - 桌面端显示 -->
        <!-- <div class="social-login hidden-sm-and-down">
          <div class="divider">
            <div class="divider-line"></div>
            <span class="divider-text">其他登录方式</span>
            <div class="divider-line"></div>
          </div>

          <div class="social-buttons">
            <el-button circle class="social-button">
              <i class="el-icon-chat-dot-round"></i>
            </el-button>
            <el-button circle class="social-button">
              <i class="el-icon-chat-line-round"></i>
            </el-button>
            <el-button circle class="social-button">
              <i class="el-icon-share"></i>
            </el-button>
          </div>
        </div> -->
      </div>
    </div>
  </div>
</template>

<script>
import { getToken, setToken } from '@/utils/auth'
import { wxLogin } from '@/api/login'
export default {
  data() {
    return {
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
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  mounted() {
    this.checkScreenSize();
    window.addEventListener('resize', this.checkScreenSize);
  },
  created() {
    this.getCookie();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkScreenSize);
  },
  methods: {
    getCookie() {
      const username = localStorage.getItem("username");
      const remember = localStorage.getItem('remember');
      this.loginForm = {
        username: username || this.loginForm.username,
        password: '', // 出于安全考虑，不存储密码
        remember: remember === 'true'
      };
    },
    async handleLogin() {
      // 如果正在登录中，直接返回，防止重复提交
      if (this.loading) {
        return;
      }

      // 添加基础验证
      if (!this.loginForm.username) {
        this.$message.error('请输入用户名');
        return;
      }
      if (!this.loginForm.password) {
        this.$message.error('请输入密码');
        return;
      }

      // 显示加载状态
      this.loading = true;

      try {
        // 记住密码处理
        if (this.loginForm.remember) {
          // 建议只存储用户名，不要存储密码
          localStorage.setItem("username", this.loginForm.username);
          localStorage.setItem("remember", "true");
        } else {
          localStorage.removeItem("username");
          localStorage.removeItem("remember");
        }

        // 调用登录API
        await this.$store.dispatch("Login", this.loginForm);
        this.$message.success('登录成功');
        // 登录成功后保持loading状态，直到页面跳转
        // 不重置loading，防止在跳转过程中按钮被再次点击
        this.$router.push({ path: this.redirect || "/" }).catch(() => {
          // 如果跳转失败，才重置loading状态
          this.loading = false;
        });
      } catch (error) {
        // 错误处理
        console.error('登录失败:', error);
        this.$message.error(error.message || '登录失败，请重试');
        this.loading = false;
      }
    },

    handleBack() {
      // 返回上一页
      if (this.$router.history.current.name) {
        this.$router.go(-1);
      } else {
        this.$router.push('/');
      }
    },
    checkScreenSize() {
      this.showBackButton = window.innerWidth < 768;
    },
    // 动态密码输入处理
    handleCodeInput(index, value) {
      // 只允许数字
      if (value && /^\d$/.test(value)) {
        this.dynamicCode[index] = value;
        // 自动聚焦下一个输入框
        if (index < 3) {
          this.$nextTick(() => {
            const inputs = this.$refs.dynamicCodeInputs;
            if (inputs && inputs[index + 1]) {
              inputs[index + 1].focus();
            }
          });
        }
        // 输入完最后一位自动验证
        if (index === 3) {
          this.verifyDynamicCode();
        }
      } else {
        this.dynamicCode[index] = '';
      }
    },

    handleCodeDelete(index) {
      const inputs = this.$refs.dynamicCodeInputs;
      if (!inputs) {
        return;
      }

      // 当前框有内容时只清空当前框
      if (this.dynamicCode[index]) {
        this.dynamicCode[index] = '';
        this.codeError = ''; // 清空错误提示
      }
      // 当前框为空且不是第一个框时，回退到上一个
      else if (index > 0) {
        this.dynamicCode[index - 1] = '';
        this.codeError = ''; // 清空错误提示
        this.$nextTick(() => {
          if (inputs[index - 1]) {
            inputs[index - 1].focus();
          }
        });
      }
    },

    // 验证动态密码
    verifyDynamicCode() {
      // 如果正在验证中，直接返回，防止重复提交
      if (this.loading) {
        return;
      }

      const code = this.dynamicCode.join('');
      if (code.length !== 4) {
        return;
      }

      this.codeError = ''; // 清空之前的错误提示
      this.loading = true;

      // 调用验证API
      wxLogin({ code }).then(res => {
        if (res.code === 200) {
          setToken(res.token);
          this.$message.success('登录成功');
          // 登录成功后保持loading状态，直到页面跳转
          // 不重置loading，防止在跳转过程中按钮被再次点击
          this.$router.push({ path: this.redirect || '/index' }).catch(() => {
            // 如果跳转失败，才重置loading状态
            this.loading = false;
          });
        } else {
          this.codeError = res.msg || '动态密码错误，请重新输入';
          this.resetCodeInput();
          this.loading = false;
        }
      }).catch(err => {
        this.codeError = err.message || '动态密码错误，请重新输入';
        this.resetCodeInput();
        this.loading = false;
      });
    },

    // 重置动态密码输入框
    resetCodeInput() {
      this.dynamicCode = ['', '', '', ''];
      this.$nextTick(() => {
        const inputs = this.$refs.dynamicCodeInputs;
        if (inputs && inputs[0]) {
          inputs[0].focus();
        }
      });
    },
  }
}
</script>
<style scoped>
/* 现代化基础样式 - 渐变背景 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
  /* 浅色系渐变背景 */
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 25%, #f8fafc 50%, #f1f5f9 75%, #e0e7ff 100%);
  background-size: 400% 400%;
  animation: gradientShift 20s ease infinite;
}

/* 动态背景动画 */
@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 装饰性背景元素 - 浅色系 */
.login-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(147, 197, 253, 0.15);
  top: -250px;
  right: -250px;
  animation: float 20s ease-in-out infinite;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(165, 180, 252, 0.12);
  bottom: -200px;
  left: -200px;
  animation: float 15s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(30px, 30px) rotate(180deg); }
}

/* 登录框 - 毛玻璃效果 */
.login-box {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  position: relative;
  z-index: 1;
  /* 毛玻璃效果 - 浅色系 */
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 24px;
  box-shadow: 
    0 20px 60px rgba(59, 130, 246, 0.08),
    0 8px 24px rgba(59, 130, 246, 0.05),
    0 0 0 1px rgba(255, 255, 255, 0.8) inset;
  overflow: hidden;
  flex-direction: column;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧图片区域 */
.login-left {
  width: 100%;
  height: 200px;
  position: relative;
  overflow: hidden;
}

.login-left::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(59, 130, 246, 0.05), transparent);
}

.login-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.login-box:hover .login-image {
  transform: scale(1.05);
}

/* 右侧登录区域 */
.login-right {
  width: 100%;
  padding: 32px;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.5);
}

/* 移动端返回按钮 */
.mobile-back {
  margin-bottom: 16px;
}

.mobile-back .el-button {
  padding: 8px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.mobile-back .el-button:hover {
  transform: translateX(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 登录头部 */
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

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.login-header p {
  color: #6b7280;
  font-size: 15px;
  font-weight: 400;
}

/* 登录选项卡样式 - 现代化设计 */
.login-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  animation: fadeInDown 0.6s ease-out 0.3s both;
}

.tab-buttons {
  display: flex;
  gap: 8px;
  background: rgba(59, 130, 246, 0.08);
  padding: 6px;
  border-radius: 12px;
}

.tab-button {
  border: none;
  background: transparent;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-button i {
  font-size: 16px;
  margin-right: 6px;
}

.tab-button:hover {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
}

.tab-button.active {
  color: #3b82f6;
  background: #fff;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.tab-button.active::after {
  display: none;
}

.tab-button.disabled,
.tab-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.button-text {
  font-weight: 500;
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
  margin-bottom: 20px;
}

.login-input>>>.el-input__inner {
  height: 52px;
  line-height: 52px;
  background: rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(59, 130, 246, 0.15);
  border-radius: 12px;
  padding: 0 50px;
  font-size: 15px;
  color: #1f2937;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-input>>>.el-input__inner::placeholder {
  color: #9ca3af;
}

.login-input>>>.el-input__inner:hover {
  border-color: rgba(59, 130, 246, 0.4);
  background: rgba(255, 255, 255, 0.95);
}

.login-input>>>.el-input__inner:focus {
  border-color: #3b82f6;
  background: #fff;
  box-shadow: 
    0 0 0 4px rgba(59, 130, 246, 0.1),
    0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-1px);
}

.login-input>>>.el-input__prefix {
  left: 16px;
}

.login-input>>>.el-input__prefix i {
  color: #3b82f6;
  font-size: 18px;
}

.login-input>>>.el-input__suffix {
  right: 16px;
}

.password-icon {
  cursor: pointer;
  color: #9ca3af;
  font-size: 20px;
  transition: all 0.3s ease;
  padding: 4px;
}

.password-icon:hover {
  color: #3b82f6;
  transform: scale(1.1);
}

/* 表单选项样式 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24px 0;
}

.form-options>>>.el-checkbox__label {
  color: #6b7280;
  font-size: 14px;
}

.form-options>>>.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #3b82f6;
  border-color: #3b82f6;
}

.forgot-password {
  color: #3b82f6;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
}

.forgot-password:hover {
  color: #06b6d4;
  text-decoration: underline;
}

/* 登录按钮样式 - 现代化渐变 */
.login-button {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  border: none;
  box-shadow: 
    0 4px 14px rgba(59, 130, 246, 0.4),
    0 2px 4px rgba(59, 130, 246, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 6px 20px rgba(59, 130, 246, 0.5),
    0 4px 8px rgba(59, 130, 246, 0.3);
}

.login-button:active {
  transform: translateY(0);
}

.login-button.is-loading {
  opacity: 0.8;
}

/* 二维码登录样式 */
.qrcode-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 28px;
  padding: 20px 0;
  min-height: 380px;
  animation: fadeInDown 0.6s ease-out 0.4s both;
  position: relative;
}

/* 加载遮罩层 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-icon {
  font-size: 40px;
  color: #3b82f6;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 16px;
  font-weight: 500;
  color: #4b5563;
  margin: 0;
}

.qrcode-placeholder {
  width: 200px;
  height: 200px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(59, 130, 246, 0.2);
  background-image: url('../assets/images/gzh.jpg');
  background-size: cover;
  background-position: center;
  box-shadow: 
    0 8px 24px rgba(59, 130, 246, 0.2),
    0 0 0 8px rgba(255, 255, 255, 0.5) inset;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.qrcode-placeholder::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(6, 182, 212, 0.1));
  opacity: 0;
  transition: opacity 0.4s ease;
}

.qrcode-placeholder:hover {
  transform: scale(1.05);
  box-shadow: 
    0 12px 32px rgba(59, 130, 246, 0.3),
    0 0 0 8px rgba(255, 255, 255, 0.6) inset;
}

.qrcode-placeholder:hover::before {
  opacity: 1;
}

/* 动态密码输入区域 */
.dynamic-code-container {
  margin-top: 20px;
  text-align: center;
  width: 100%;
}

.dynamic-code-title {
  color: #4b5563;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 20px;
  letter-spacing: 0.3px;
}

.dynamic-code-inputs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.dynamic-code-input {
  width: 56px;
  height: 56px;
}

.dynamic-code-input>>>.el-input__inner {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  border: 2px solid rgba(59, 130, 246, 0.2);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  color: #3b82f6;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dynamic-code-input>>>.el-input__inner:focus {
  border-color: #3b82f6;
  background: #fff;
  box-shadow: 
    0 0 0 4px rgba(59, 130, 246, 0.15),
    0 4px 12px rgba(59, 130, 246, 0.2);
  transform: scale(1.05);
}

.code-hint {
  color: #ef4444;
  font-size: 13px;
  font-weight: 500;
  height: 24px;
  margin-top: 8px;
  transition: all 0.3s ease;
  animation: shake 0.5s ease;
}

.loading-hint {
  color: #3b82f6;
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

.forgot-password.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
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
    min-height: 620px;
    border-radius: 28px;
  }

  .login-left {
    width: 50%;
    height: auto;
  }

  .login-right {
    width: 50%;
    padding: 48px;
    background: rgba(255, 255, 255, 0.7);
  }

  .login-header h2 {
    font-size: 32px;
  }

  .login-header p {
    font-size: 16px;
  }

  .tab-buttons {
    gap: 12px;
    padding: 8px;
  }

  .tab-button {
    padding: 12px 24px;
  }
}

@media (max-width: 767px) {
  .login-container {
    padding: 12px;
  }

  .login-box {
    min-height: auto;
    border-radius: 20px;
  }

  .login-left {
    height: 180px;
  }

  .login-right {
    padding: 24px;
  }

  .login-header h2 {
    font-size: 24px;
  }

  .tab-button {
    padding: 8px 16px;
    font-size: 13px;
  }

  .button-text {
    display: none;
  }

  .qrcode-placeholder {
    width: 180px;
    height: 180px;
  }

  .dynamic-code-input {
    width: 52px;
    height: 52px;
  }

  .dynamic-code-input>>>.el-input__inner {
    height: 52px;
    line-height: 52px;
    font-size: 22px;
  }
}

/* ElementUI 响应式工具类 */
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

/* 其他样式 */
.img-logo {
  max-width: 100%;
  max-height: 100%;
}

.scan-status {
  position: absolute;
  bottom: -30px;
  width: 100%;
  text-align: center;
  color: #67C23A;
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
  background: rgba(59, 130, 246, 0.3);
  border-radius: 3px;
}

.login-right::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.5);
}
</style>