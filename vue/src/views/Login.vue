<template>
  <div class="container">
    <div class="login-card" style="width: 380px; padding: 45px;">
      <div style="text-align: center; font-size: 24px; margin-bottom: 6px; color: #6b5b95; font-weight: bold">星栈始动</div>
      <div style="text-align: center; font-size: 12px; margin-bottom: 26px; color: #b9a7e0; letter-spacing: 2px">StarStack BootUp · IT 技术交流社区</div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input size="medium" prefix-icon="el-icon-user" placeholder="请输入账号" v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="medium" prefix-icon="el-icon-lock" placeholder="请输入密码" show-password  v-model="form.password"></el-input>
        </el-form-item>

        <el-form-item prop="role">
          请选择登录角色：
          <el-radio-group v-model="form.role">
            <el-radio label="ADMIN">管理员</el-radio>
            <el-radio label="USER">用户</el-radio>
          </el-radio-group>
        </el-form-item>

         <!--验证码（服务端生成与校验）-->
        <el-form-item prop="code">
          <div style="display: flex; width: 100%">
            <el-input style="flex: 1" size="medium" prefix-icon="el-icon-key" placeholder="请输入验证码" v-model="code"></el-input>
            <img v-if="captcha.image" :src="captcha.image" alt="验证码" title="看不清？点击刷新"
                 style="width: 120px; height: 38px; margin-left: 10px; border-radius: 19px; cursor: pointer; border: 1px solid #e0dcef"
                 @click="refreshCaptcha" />
            <div v-else style="width: 120px; height: 38px; margin-left: 10px; border-radius: 19px; background: #f3f0fa"></div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button size="medium" style="width: 100%; background-color: #2a60c9; border-color: #2a60c9; color: white" @click="login">登 录</el-button>
        </el-form-item>
        <div style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <div style="flex: 1; text-align: right">
            还没有账号？请 <a href="/register">注册</a>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      form: { role: 'ADMIN' },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ]
      },
      code: '',                        // 用户输入的验证码
      captcha: { key: '', image: '' }  // 服务端下发的验证码
    }
  },
  mounted() {
    this.refreshCaptcha()
  },
  methods: {
    // 从服务端获取新验证码
    refreshCaptcha() {
      this.$request.get('/captcha').then(res => {
        this.captcha = res.data || { key: '', image: '' }
        this.code = ''
      })
    },
    login() {
      if (!this.code) {
        this.$message.warning('请输入验证码！')
        return
      }
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          const data = { ...this.form, captchaKey: this.captcha.key, captchaCode: this.code }
          this.$request.post('/login', data).then(res => {
            if (res.code === '200') {
              localStorage.setItem("xm-user", JSON.stringify(res.data))  // 存储用户数据

              this.$message.success('登录成功')
              setTimeout(() =>{
                if (res.data.role === 'ADMIN'){
                  location.href='/home'  // 跳转主页---管理员
                }else {
                  location.href='/front/home'  // 跳转主页---用户
                }
              },500)

            } else {
              // 服务端校验失败：验证码已消费，必须换一张新的
              this.refreshCaptcha()
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(160deg, #c9b8f9 0%, #e3c7f7 22%, #ffd7ee 48%, #ffeaf5 66%, #c8f3e2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}
.login-card {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 22px;
  border: 1.5px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 14px 40px rgba(160, 120, 220, 0.25);
}
.login-card :deep(.el-button--medium) {
  background: linear-gradient(135deg, #b79cff, #ff9ecf) !important;
  border: none !important;
  border-radius: 999px !important;
  font-weight: bold;
  box-shadow: 0 6px 16px rgba(255, 158, 207, 0.45);
}
.login-card :deep(.el-input__inner) {
  border-radius: 999px !important;
}
a {
  color: #e8619c;
}
</style>
