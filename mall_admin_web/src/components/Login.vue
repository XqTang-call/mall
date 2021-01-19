<template>
  <div>
    <el-card class="login-form-layout">
      <el-form autoComplete="on"
               :model="loginForm"
               :rules="loginRules"
               ref="loginForm"
               label-position="left">
        <div style="text-align: center">
          <svg-icon icon-class="login-mall" style="width: 56px;height: 56px;color: #409EFF"></svg-icon>
        </div>
        <h2 class="login-title color-main">mall-admin-web</h2>
        <el-form-item prop="username">
          <el-input name="username"
                    type="text"
                    v-model="loginForm.username"
                    autoComplete="on"
                    placeholder="请输入用户名">
          <span slot="prefix">
            <svg-icon icon-class="user" class="color-main"></svg-icon>
          </span>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input name="password"
                    :type="pwdType"
                    @keyup.enter.native="handleLogin"
                    v-model="loginForm.password"
                    autoComplete="on"
                    placeholder="请输入密码">
          <span slot="prefix">
            <svg-icon icon-class="password" class="color-main"></svg-icon>
          </span>
            <span slot="suffix" @click="showPwd">
            <svg-icon icon-class="eye" class="color-main"></svg-icon>
          </span>
          </el-input>
        </el-form-item>
        <el-form-item style="margin-bottom: 60px;text-align: center">
          <el-button style="width: 45%" type="primary" :loading="loading" @click.native.prevent="handleLogin">
            登录
          </el-button>
          <el-button style="width: 45%" type="primary" @click.native.prevent="handleTry">
            赞赏并支持他
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <img :src="login_center_bg" class="login-center-layout">
    <el-dialog
      title="赞赏码"
      :visible.sync="dialogVisible"
      :show-close="false"
      :center="true"
      width="30%">
      <div style="text-align: center">
        <img src="../assets/images/1611024940.jpg" width="200px" height="200px" style="margin-top: 10px">
      </div>
      <span slot="footer" class="dialog-footer">
    <el-button type="primary" @click="dialogConfirm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {isvalidUsername} from '../utils/validate'
import {setSupport, getSupport, setCookie, getCookie} from '../utils/support'
import login_center_bg from '../assets/images/login_center_bg.png'

  export default {
    name: 'login',
    data() {
      // 定义一个常数，通过 validate 文件的正则规则方法，判断获取的 value 是否合法
      const validateUsername = (rule, value, callback) => {
        if (!isvalidUsername(value)) {
          callback(new Error('请输入正确的用户名'))
        } else {
          callback()
        }
      };
      const validatePass = (rule, value, callback) => {
        if (value.length < 3) {
          callback(new Error('密码不能小于3位'))
        } else {
          callback()
        }
      };
      return {
        loginForm: {
          username: '',
          password: '',
        },
        // 验证规则对象：validator 对应的值是上方 const 的对象
        loginRules: {
          username: [{required: true, trigger: 'blur', validator: validateUsername}],
          password: [{required: true, trigger: 'blur', validator: validatePass}]
        },
        loading: false,
        pwdType: 'password',
        login_center_bg,
        dialogVisible:false,
        supportDialogVisible:false
      }
    },
    created() {
      // 获取 cookid 值自动输入到 username 框中
      this.loginForm.username = getCookie("username");
      this.loginForm.password = getCookie("password");
      // 当输入框中没有值的时候，username 中则加入 admin
      if(this.loginForm.username === undefined||this.loginForm.username==null||this.loginForm.username===''){
        this.loginForm.username =   'admin';
      }
      // 当输入框中没有值的时候，password 中则为空（效果就是，进入网站，则 username 输入框中有默认值）
      if(this.loginForm.password === undefined||this.loginForm.password==null){
        this.loginForm.password = '';
      }
    },
    methods: {
      showPwd() {
        if (this.pwdType === 'password') {
          this.pwdType = ''
        } else {
          this.pwdType = 'password'
        }
      },
      handleLogin() {
        this.$refs.loginForm.validate(valid => {
          if (valid) {
            // let isSupport = getSupport();
            // if(isSupport===undefined||isSupport==null){
            //   this.dialogVisible =true;
            //   return;
            // }
            this.loading = true;
            this.$store.dispatch('Login', this.loginForm).then(() => {
              this.loading = false;
              setCookie("username",this.loginForm.username,15);
              setCookie("password",this.loginForm.password,15);
              this.$router.push({path: '/'})
            }).catch(() => {
              this.loading = false
            })
          } else {
            console.log('参数验证不合法！');
            return false
          }
        })
      },
      handleTry(){
        this.dialogVisible =true
      },
      dialogConfirm(){
        this.dialogVisible =false;
        setSupport(true);
      },
      dialogCancel(){
        this.dialogVisible = false;
        setSupport(false);
      }
    }
  }

</script>

<!--此处需要安装插件less-loader less才能有效果-->
<style lang="less" scoped>
  .login-form-layout {
    position: absolute;
    left: 0;
    right: 0;
    width: 360px;
    margin: 140px auto;
    border-top: 10px solid #409EFF;
  }

  .login-title {
    text-align: center;
  }

  .login-center-layout {
    background: #409EFF;
    width: auto;
    height: auto;
    max-width: 100%;
    max-height: 100%;
    margin-top: 200px;
  }
</style>
