<template>
  <div class="container">
    <div class="login-card" style="width: 380px; padding:50px 30px;">
      <div style="text-align: center; font-size: 24px; margin-bottom: 30px; color: #6b5b95">欢迎注册 星栈始动 账号</div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input prefix-icon="el-icon-user" size="medium" placeholder="请输入账号（4-16位字母/数字/下划线）" v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input prefix-icon="el-icon-lock" size="medium" placeholder="请输入密码（8位以上，含字母和数字）" show-password  v-model="form.password"></el-input>
        </el-form-item>
        <!--密码强度提示-->
        <div v-if="form.password" class="strength">
          <span v-for="i in 3" :key="i" class="bar" :class="i <= strengthLevel ? 'bar-' + strengthLevel : ''"></span>
          <span class="strength-text" :class="'s' + strengthLevel">{{ strengthText }}</span>
        </div>
        <el-form-item prop="confirmPass">
          <el-input prefix-icon="el-icon-lock" size="medium"  placeholder="请确认密码" show-password  v-model="form.confirmPass"></el-input>
        </el-form-item>
        <!--验证码（服务端生成与校验）-->
        <el-form-item prop="code">
          <div style="display: flex; width: 100%">
            <el-input style="flex: 1" prefix-icon="el-icon-key" size="medium" placeholder="请输入验证码" v-model="code"></el-input>
            <img v-if="captcha.image" :src="captcha.image" alt="验证码" title="看不清？点击刷新"
                 style="width: 120px; height: 38px; margin-left: 10px; border-radius: 19px; cursor: pointer; border: 1px solid #e0dcef"
                 @click="refreshCaptcha" />
            <div v-else style="width: 120px; height: 38px; margin-left: 10px; border-radius: 19px; background: #f3f0fa"></div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button style="width: 100%; background-color: #2a60c9; border-color: #2a60c9; color: white" size="medium"  @click="register">注 册</el-button>
        </el-form-item>
        <div style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <div style="flex: 1; text-align: right">
            已有账号？请 <a href="/login">登录</a>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    // 验证码校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    // 密码强度校验
    const validateStrength = (rule, value, callback) => {
      if (!/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(value)) {
        callback(new Error('密码至少8位，且需同时包含字母和数字'))
      } else {
        callback()
      }
    }
    return {
      form: {role:'USER'},
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]{4,16}$/, message: '账号为4-16位字母、数字或下划线', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: validateStrength, trigger: 'blur' }
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ]
      },
      code: '',                        // 用户输入的验证码
      captcha: { key: '', image: '' }  // 服务端下发的验证码
    }
  },
  computed: {
    // 密码强度：1弱 2中 3强
    strengthLevel() {
      const p = this.form.password || ''
      if (!p) return 0
      let score = 0
      if (p.length >= 8) score++
      if (/[A-Za-z]/.test(p) && /\d/.test(p)) score++
      if (/[^A-Za-z0-9]/.test(p) || p.length >= 12) score++
      return score
    },
    strengthText() {
      return ['弱', '弱', '中', '强'][this.strengthLevel]
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
    register() {
      if (!this.code) {
        this.$message.warning('请输入验证码！')
        return
      }
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          const data = { ...this.form, captchaKey: this.captcha.key, captchaCode: this.code }
          this.$request.post('/register', data).then(res => {
            if (res.code === '200') {
              this.$router.push('/')  // 跳转登录页面
              this.$message.success('注册成功')
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
.strength {
  display: flex;
  align-items: center;
  margin: -14px 0 18px;
  padding: 0 6px;
}
.bar {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: #e8e2f2;
  margin-right: 6px;
  transition: background .2s;
}
.bar-1 { background: #f56c6c; }
.bar-2 { background: #e6a23c; }
.bar-3 { background: #67c23a; }
.strength-text { font-size: 12px; margin-left: 2px; }
.s1 { color: #f56c6c; }
.s2 { color: #e6a23c; }
.s3 { color: #67c23a; }
a {
  color: #e8619c;
}
</style>
