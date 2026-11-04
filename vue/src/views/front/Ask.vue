<!--向作者提问（一问一答制）-->
<template>
  <div style="width: 60%; margin: 5px auto">
    <div class="card" style="margin-bottom: 100px" v-if="!guest">
      <div style="font-weight: bold; font-size: 24px; margin-bottom: 20px">向 TA 提问</div>

      <!-- 被提问作者信息卡 -->
      <div class="ask-author" v-if="author.id">
        <img :src="author.avatar" alt="" style="width: 46px; height: 46px; border-radius: 50%; object-fit: cover">
        <div>
          <div style="font-weight: bold">{{ author.name || author.username }}</div>
          <div style="font-size: 12px; color: #999999">@{{ author.username }}</div>
        </div>
      </div>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 20px"
                title="提问将发送给作者，作者回答后你会收到消息通知；请描述清楚问题背景，便于作者精准回答">
      </el-alert>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" style="padding-right: 30px">
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="form.title" placeholder="用一句话概括你的问题" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="问题描述" prop="content">
          <el-input type="textarea" :rows="8" v-model="form.content" placeholder="补充问题的详细描述：你在做什么、遇到了什么现象、期望的结果……（选填）"
                    maxlength="1000" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button type="primary" round style="width: 160px" @click="submit">提 交 问 题</el-button>
      </div>
    </div>

    <!-- 游客/管理员引导 -->
    <div v-if="guest" class="card" style="text-align: center; padding: 90px 20px;">
      <i :class="adminMode ? 'el-icon-s-tools' : 'el-icon-question'" style="font-size: 52px; color: #409EFF"></i>
      <p style="font-size: 16px; color: #666666; margin: 18px 0 26px">
        {{ adminMode ? '提问功能仅供用户账号使用' : '登录后就可以向作者提问啦' }}
      </p>
      <el-button v-if="adminMode" type="primary" @click="$router.push('/home')">去后台管理</el-button>
      <el-button v-else type="primary" @click="$router.push('/login')">去登录</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: "Ask",
  data() {
    return {
      userId: this.$route.query.userId ? Number(this.$route.query.userId) : null,   // 被提问作者
      author: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      guest: false,
      adminMode: false,
      form: { title: '', content: '' },
      rules: {
        title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    if (!this.user.id) {
      this.guest = true
      return
    }
    if (this.user.role !== 'USER') {
      this.guest = true
      this.adminMode = true
      return
    }
    if (!this.userId || this.userId === this.user.id) {
      this.$message.warning(this.userId === this.user.id ? '不能向自己提问' : '缺少被提问对象')
      this.$router.push('/front/home')
      return
    }
    this.loadAuthor()
  },
  methods: {
    loadAuthor() {
      this.$request.get('/space/info/' + this.userId).then(res => {
        if (res.code === '200' && res.data?.user) {
          this.author = res.data.user
        }
      })
    },
    submit() {
      this.$refs.formRef.validate((valid) => {
        if (!valid) return
        this.$request.post('/question/add', {
          authorId: this.userId,
          title: this.form.title,
          content: this.form.content
        }).then(res => {
          if (res.code === '200') {
            this.$message.success('提问已发送，作者回答后你会收到通知')
            setTimeout(() => {
              this.$router.push('/front/person')
            }, 1200)
          } else {
            this.$message.error(res.msg)
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.ask-author {
  display: inline-flex;
  align-items: center;
  grid-gap: 10px;
  background: #f7f3ff;
  border-radius: 12px;
  padding: 10px 18px;
  margin-bottom: 20px;
}
</style>
