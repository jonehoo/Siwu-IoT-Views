<template>
  <div class="login" v-if="showStatus">
    <!-- <div class='logo-img'>

      
      <img 
        style=" width: 200px; height: 200px;" 
        src="@/assets/images/肆物联云.svg" 
        class=" el-image__inner">
    </div> -->
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <div style='
     width: 100%;display: flex;
     justify-content: center;
     align-items: center;'>
        <img src="../../src/assets/logo/logo1.png" height="200px" width="200px" alt="">
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
      <div 
      style=" width: 100%; display: flex; justify-content: center; align-items: center; margin-top: 20px; " 
      class="el-image">
        <img 
        style=" width: 200px; height: 200px;" 
        :src="gzh" 
        class=" el-image__inner">
      </div>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2024 All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

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
    this.autoAuth();
  },
  methods: {
    autoAuth() {
      console.log('this.redirect: ', this.redirect);
      const inputString = this.redirect
      const regex = /[?&](clientId|clientSecret)=([^&]*)/g;

      let match;
      const params = {};

      // 使用正则表达式查找匹配的部分
      while ((match = regex.exec(inputString)) !== null) {
        params[match[1]] = match[2];
      }

      // 输出提取的参数
      console.log(params);

      // 如果包含 clientId 和 clientSecret，显示它们的值
      if (params.clientId && params.clientSecret) {
        console.log("clientId:", params.clientId);
        console.log("clientSecret:", params.clientSecret);
        this.showStatus = false;
        this.loginForm.username = params.clientId;
        this.loginForm.password = encrypt(params.clientSecret);
        this.loading = true;
        this.$store.dispatch("Login", this.loginForm).then(() => {
          this.$router.push({ path: this.redirect || "/" }).catch(() => { });
        }).catch(() => {
          this.loading = false;
        });
      } else {
        console.log("clientId 或 clientSecret 不存在");
      }
      // const clientId = this.$route.query.clientId;
      // const clientSecret = this.$route.query.clientSecret;
      // if (clientId && clientSecret) {
      //   this.showStatus = false;
      //   this.loginForm.username = clientId;
      //   this.loginForm.password = encrypt(clientSecret);
      //   this.loading = true;
      //   this.$store.dispatch("Login", this.loginForm).then(() => {
      //     this.$router.push({ path: this.redirect || "/" }).catch(() => { });
      //   }).catch(() => {
      //     this.loading = false;
      //   });
      // }
    },
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
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.loginForm.password = encrypt(this.loginForm.password);
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

<style rel="stylesheet/scss" lang="scss">
.logo-img{
position: absolute;
top: 1%;
left: 1%;
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
  margin: 10vh auto 30px auto;
  text-align: center;
  color: #707070;
  font-size: 30px;
  font-weight: bold;
}


.login-form {

  background: #ffffff;
  width: 480px;
  height: 100%;
  padding: 25px 50px 5px 50px;

  .el-input {
    height: 38px;

    input {
      height: 38px;
    }
  }

  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}

@media (max-width: 768px) {
  .login-form {
    width: 100VW;
    padding: 20px 10px;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 33%;
  height: 38px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-code-img {
  height: 38px;
}
</style>