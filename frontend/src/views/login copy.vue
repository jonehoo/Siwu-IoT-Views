<template>
  <div class="login" v-if="showStatus">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <div style='
     width: 100%;display: flex;
     justify-content: center;
     align-items: center;'>
        <img src="../../src/assets/logo/logo1.png" class="img-logo" alt="">
      </div>
      <h3 class="title">SiWU-IoT-VIEWS</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码"
          @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input v-model="loginForm.code" auto-complete="off" placeholder="验证码" style="width: 63%"
          @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img" />
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="medium" type="primary" style="width:100%;"
          @click.native.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;margin-top: 20px;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
      <el-divider content-position="center">获取演示账号</el-divider>
      <div style=" width: 100%; display: flex; justify-content: center; align-items: center; margin-top: 20px; "
        class="el-image">
        <img class="img-logo" :src="gzh">
      </div>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2024 All Rights Reserved.</span>
    </div>
  </div>

</template>
<style rel="stylesheet/scss" lang="scss">
.img-logo {
  width: 10vw;
  height: 20vh;
}

.login {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/loginbk.jpg");
  background-size: 100% 100%;
}

.title {
  margin: 2vh auto 4vh auto;
  text-align: center;
  color: #707070;
  font-size: 4vh;
  font-weight: bold;
}


.login-form {

  background: #ffffff;
  width: 25vw;
  height: 100vh;
  padding: 5vh 6vh 5vh 6vh;

  .el-input {
    height: 4vh;

    input {
      height: 4vh;
    }
  }

  .input-icon {
    height: 4vh;
    width: 1vh;
    margin-left: 2px;
  }
}

@media (max-width: 768px) {
  .login-form {
    width: 100VW;
    padding: 20px 10px;
    padding: 5vh 6vh 5vh 6vh;
  }

  .img-logo {
    width: 40vw;
    height: 20vh;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 33%;
  height: 30px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}

.el-login-footer {
  height: 4vh;
  line-height: 4vh;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 1vh;
  letter-spacing: 1px;
}
</style>

<script>
import { getCodeImg } from "@/api/login";

export default {
  name: "Login",
  data() {
    return {
      gzh: require('../assets/images/gzh.jpg'),
      log: require('../assets/images/肆物联云.svg'),
      showStatus: true,
      codeUrl: "",
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
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
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: true,
      redirect: undefined
    };
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
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
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            localStorage.setItem("username", this.loginForm.username, { expires: 30 });
            localStorage.setItem("password", this.loginForm.password, { expires: 30 });
            localStorage.setItem('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
             localStorage.removeItem("username");
             localStorage.removeItem("password");
             localStorage.removeItem('rememberMe');
          }
          this.loginForm.password = this.loginForm.password;
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(() => { });
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }
      });
    }
  }
};
</script>
