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
            <el-button :class="['tab-button', { 'active': !isPasswordLogin }]" @click="isPasswordLogin = false">
              <i class="el-icon-mobile-phone"></i>
              <span class="button-text">动态密码登录</span>
            </el-button>
            <el-button :class="['tab-button', { 'active': isPasswordLogin }]" @click="isPasswordLogin = true">
              <i class="el-icon-user"></i>
              <span class="button-text">账号密码登录</span>
            </el-button>

          </div>
        </div>

        <!-- 账号密码登录表单 -->
        <div v-if="isPasswordLogin" class="login-form">
          <el-form :model="loginForm" :rules="loginRules" ref="loginForm" @keyup.enter.native="handleLogin">

            <el-form-item prop="username" class="form-item">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user"
                class="login-input">
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <el-input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码"
                prefix-icon="el-icon-lock" class="login-input">
                <i slot="suffix" class="el-icon-view password-icon" @click="showPassword = !showPassword">
                </i>
              </el-input>
            </el-form-item>
          </el-form>

          <div class="form-options">
            <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
            <el-link type="primary" class="forgot-password">忘记密码？</el-link>
          </div>

          <el-button type="primary" class="login-button" @click="handleLogin" :loading="loading">
            登录
          </el-button>

          <!-- <div class="register-link">
            <span>还没有账号？</span>
            <el-link type="primary">立即注册</el-link>
          </div> -->
        </div>

        <!-- 扫码登录 -->
        <div v-else class="qrcode-login">
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
                ref="dynamicCodeInputs" class="dynamic-code-input" maxlength="1" @input="handleCodeInput(index, $event)"
                @keyup.delete.native="handleCodeDelete(index)"></el-input>
            </div>
            <p class="code-hint" v-if="codeError">{{ codeError }}</p>
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
  $route: {
    handler: function (route) {
      this.redirect = route.query && route.query.redirect;
    },
    immediate: true
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
      const password = localStorage.getItem("password");
      const rememberMe = localStorage.getItem('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : password,
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    async handleLogin() {
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

        // 登录成功处理
        this.$message.success('登录成功');
        this.$router.push({ path: this.redirect || "/" });

      } catch (error) {
        // 错误处理
        console.error('登录失败:', error);
        this.$message.error(error.message || '登录失败，请重试');
      } finally {
        this.loading = false;
      }
    },

    handleBack() {
      // 返回上一页或其他逻辑
      console.log('返回');
    },
    checkScreenSize() {
      this.showBackButton = window.innerWidth < 768;
    },
    // 动态密码输入处理
    handleCodeInput(index, value) {
      if (value && value.match(/^\d$/)) {
        this.dynamicCode[index] = value;
        if (index < 3) {
          this.$refs.dynamicCodeInputs[index + 1].focus();
        }
        if (index === 3) {
          this.verifyDynamicCode();
        }
      } else {
        this.dynamicCode[index] = '';
      }
    },

    handleCodeDelete(index) {

      // 确保 $refs 已正确引用
      if (!this.$refs.dynamicCodeInputs) {
        return;
      }

      // 当前框有内容时只清空当前框
      if (this.dynamicCode[index]) {
        this.dynamicCode[index] = '';
      }
      // 当前框为空且不是第一个框时
      else if (index > 0) {
        this.dynamicCode[index - 1] = '';
        // 添加 nextTick 确保DOM更新
        this.$nextTick(() => {
          this.$refs.dynamicCodeInputs[index - 1].focus();
        });
      }
    },

    // 验证动态密码
    verifyDynamicCode() {
      console.log('验证动态密码: ', "登录成功");
      const code = this.dynamicCode.join('');
      console.log('code: ', code);
      if (code.length === 4) {
        // this.loading = true;

        // 调用验证API (示例)
        wxLogin({ code }).then(res => {
          if (res.code === 200) {
            setToken(res.token);
            this.$message.success('登录成功');
            this.$router.push('/index');
          } else {
            this.codeError = '动态密码错误，请重新输入';
            // 清空输入框
            this.dynamicCode = ['', '', '', ''];
            this.$refs.dynamicCodeInputs[0].focus();
          }
        }).catch(err => {
          this.codeError = '动态密码错误，请重新输入';
          // 清空输入框
          this.dynamicCode = ['', '', '', ''];
          this.$refs.dynamicCodeInputs[0].focus();
        }).finally(() => {
          // this.loading = false;
        });
      }
    },
  }
}
</script>
<style scoped>
/* 基础样式 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 16px;
}

.login-box {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  flex-direction: column;
}

.login-left {
  width: 100%;
  height: 200px;
}

.login-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-right {
  width: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h2 {
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

/* 登录选项卡样式 */
.login-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.tab-buttons {
  display: flex;
  gap: 16px;
}

.tab-button {
  border: none;
  background: none;
  padding: 8px 12px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
}

.tab-button.active {
  color: #409eff;
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #409eff;
}

.button-text {
  margin-left: 4px;
}

/* 登录表单样式 - 合并优化 */
.login-form {
  width: 100%;
  margin-top: 20px;
}

.form-item {
  margin-bottom: 24px;
}

.login-input>>>.el-input__inner {
  height: 48px;
  line-height: 48px;
  background-color: #f9f9f9;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 0 45px;
  padding-right: 45px;
  /* 为眼睛图标留出空间 */
  transition: all 0.3s;
}

.login-input>>>.el-input__inner:hover {
  border-color: #c0c4cc;
}

.login-input>>>.el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.login-input>>>.el-input__prefix,
.login-input>>>.el-input__suffix {
  display: flex;
  align-items: center;
}

.password-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 18px;
  transition: color 0.2s;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

.password-icon:hover {
  color: #909399;
}

/* 表单选项样式 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
}

.forgot-password {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
}

.forgot-password:hover {
  color: #66b1ff;
}

/* 登录按钮样式 */
.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  margin-top: 10px;
  border-radius: 4px;
}

/* 注册链接样式 */
.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #606266;
}

.register-link .el-link {
  margin-left: 5px;
}

/* 二维码登录样式 */
.qrcode-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 16px 0;
}

.qrcode-placeholder {
  width: 180px;
  height: 180px;
  background-color: #f9f9f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #ddd;
  background-image: url('../assets/images/gzh.jpg');
  background-size: 100% 100%;
}

.qrcode-placeholder i {
  font-size: 48px;
  color: #999;
}

.qrcode-login p {
  color: #666;
  font-size: 14px;
}

/* 社交媒体登录样式 */
.social-login {
  margin-top: auto;
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background-color: #e5e7eb;
}

.divider-text {
  padding: 0 16px;
  color: #999;
  font-size: 14px;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.social-button {
  width: 44px;
  height: 44px;
  font-size: 18px;
  border: 1px solid #e5e7eb;
  background-color: #fff;
  color: #666;
  transition: all 0.2s ease;
}

.social-button:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
}

/* 移动端样式 */
.mobile-back {
  margin-bottom: 16px;
}

.mobile-back .el-button {
  padding: 8px;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .login-box {
    flex-direction: row;
    min-height: 600px;
  }

  .login-left {
    width: 50%;
    height: auto;
  }

  .login-right {
    width: 50%;
    padding: 40px;
  }

  .login-header h2 {
    font-size: 24px;
  }

  .tab-buttons {
    gap: 32px;
  }
}

@media (max-width: 767px) {
  .login-box {
    min-height: auto;
  }

  .login-left {
    height: 160px;
  }

  .login-right {
    padding: 20px;
  }

  .tab-button {
    padding: 6px 8px;
    font-size: 13px;
  }

  .button-text {
    display: none;
  }

  .qrcode-placeholder {
    width: 150px;
    height: 150px;
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

/* 二维码区域样式 */
.qrcode-placeholder {
  width: 180px;
  height: 180px;
  margin: 0 auto;
  position: relative;
  border: 1px solid #eee;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

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

/* 动态密码输入区域 */
.dynamic-code-container {
  margin-top: 14px;
  text-align: center;
}

.dynamic-code-title {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

.dynamic-code-inputs {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 8px;
}

.dynamic-code-input {
  width: 48px;
  height: 48px;
}

.dynamic-code-input>>>.el-input__inner {
  height: 48px;
  line-height: 48px;
  text-align: center;
  font-size: 20px;
  border: 1px solid #e7e7e7;
  border-radius: 4px;
  font-weight: 800;

}

.dynamic-code-input>>>.el-input__inner:focus {
  border-color: #e7e7e7;
  box-shadow: 0 0 0 2px rgba(112, 112, 112, 0.1);
}

.code-hint {
  color: #e7e7e7;
  font-size: 12px;
  height: 20px;
  margin-top: 4px;
}
</style>