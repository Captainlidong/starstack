<template>
  <div class="main-content" style="width: 50%">
    <el-tabs v-if="!guest" v-model="activeName" @tab-click="clickTabs">
      <el-tab-pane label="我发表的博客" name="我发表的博客">
        <div class="card" style="padding: 5px"><el-button type="primary" @click="addBlog">发表新博客</el-button></div>
        <div style="margin-top: 10px">
          <blog-list type="user" :show-opt="true"/>
        </div>
      </el-tab-pane>

      <el-tab-pane label="草稿箱" name="草稿箱">
        <div class="card" style="padding: 5px">
          <el-button type="primary" @click="addBlog">写新博客</el-button>
          <span style="margin-left: 10px;color: #999999;font-size: 13px">草稿仅自己可见，编辑完成后点击"提交审核"即可发布</span>
        </div>
        <div style="margin-top: 10px">
          <blog-list type="user" status="DRAFT" :show-opt="true"/>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我报名的活动" name="我报名的活动">
        <activity-list type="user" :span="8"/>
      </el-tab-pane>

      <el-tab-pane label="我的提问" name="我的提问">
        <div class="card" style="padding: 5px">
          <span style="color: #999999; font-size: 13px">你向其他作者提出的问题，作者回答后会收到通知</span>
        </div>
        <div style="margin-top: 10px">
          <div class="card" v-for="q in myQuestions" :key="q.id" style="padding: 12px 18px; margin-bottom: 10px">
            <div style="display: flex; align-items: center; margin-bottom: 6px">
              <el-tag size="mini" :type="q.status === 'ANSWERED' ? 'success' : 'warning'" style="margin-right: 8px">
                {{ q.status === 'ANSWERED' ? '已回答' : '待回答' }}
              </el-tag>
              <span style="font-weight: bold">{{ q.title }}</span>
            </div>
            <div v-if="q.content" style="color: #666666; font-size: 13px; margin-bottom: 6px">问题描述：{{ q.content }}</div>
            <div v-if="q.answer" style="background: #f7f3ff; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin-bottom: 6px">
              <span style="color: #8b5cf6; font-weight: bold">{{ q.authorName }} 回答：</span>{{ q.answer }}
            </div>
            <div style="font-size: 12px; color: #bbbbbb">
              向 {{ q.authorName }} 提问 · {{ q.time }}<span v-if="q.answerTime"> · 回答于 {{ q.answerTime }}</span>
            </div>
          </div>
          <div class="card" v-if="!myQuestions.length" style="padding: 20px; text-align: center; color: #999999">
            还没有提问记录，去作者的个人空间向 TA 提问吧~
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收到的提问" name="收到的提问">
        <div class="card" style="padding: 5px">
          <span style="color: #999999; font-size: 13px">其他用户向你的提问，回答后会通知提问者</span>
        </div>
        <div style="margin-top: 10px">
          <div class="card" v-for="q in receivedQuestions" :key="q.id" style="padding: 12px 18px; margin-bottom: 10px">
            <div style="display: flex; align-items: center; margin-bottom: 6px">
              <img v-if="q.askerAvatar" :src="q.askerAvatar" style="width: 28px; height: 28px; border-radius: 50%; margin-right: 8px" />
              <span style="font-weight: bold">{{ q.askerName }}</span>
              <el-tag size="mini" :type="q.status === 'ANSWERED' ? 'success' : 'warning'" style="margin-left: 10px">
                {{ q.status === 'ANSWERED' ? '已回答' : '待回答' }}
              </el-tag>
            </div>
            <div style="font-weight: bold; margin-bottom: 4px">{{ q.title }}</div>
            <div v-if="q.content" style="color: #666666; font-size: 13px; margin-bottom: 6px">{{ q.content }}</div>
            <div v-if="q.answer" style="background: #f7f3ff; border-radius: 8px; padding: 10px 14px; font-size: 13px">
              <span style="color: #8b5cf6; font-weight: bold">我的回答：</span>{{ q.answer }}
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
              <span style="font-size: 12px; color: #bbbbbb">提问于 {{ q.time }}<span v-if="q.answerTime"> · 回答于 {{ q.answerTime }}</span></span>
              <el-button v-if="q.status === 'WAIT'" type="primary" size="mini" round @click="openAnswer(q)">回 答</el-button>
            </div>
          </div>
          <div class="card" v-if="!receivedQuestions.length" style="padding: 20px; text-align: center; color: #999999">
            暂无收到的提问
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的点赞" name="我的点赞">
        <div class="card" style="padding: 5px;display: flex">
          <div class="category-btn" :class="{'active':likeCurrent==='博客'}" @click="likeCurrent='博客'">博客</div>
          <div class="category-btn" :class="{'active':likeCurrent==='活动'}" @click="likeCurrent='活动'">活动</div>
        </div>
        <div style="margin-top: 10px">
          <blog-list v-if="likeCurrent==='博客'" type="like"></blog-list>
          <activity-list v-if="likeCurrent==='活动'" type="like" :span="8"></activity-list>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的收藏" name="我的收藏">
        <div class="card" style="padding: 5px;display: flex">
          <div class="category-btn" :class="{'active':collectCurrent==='博客'}" @click="collectCurrent='博客'">博客</div>
          <div class="category-btn" :class="{'active':collectCurrent==='活动'}" @click="collectCurrent='活动'">活动</div>
        </div>
        <div style="margin-top: 10px">
          <blog-list v-if="collectCurrent==='博客'" type="collect"></blog-list>
          <activity-list v-if="collectCurrent==='活动'" :span="8" type="collect"></activity-list>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的评论" name="我的评论">
        <div class="card" style="padding: 5px;display: flex">
          <div class="category-btn" :class="{'active':commentCurrent==='博客'}" @click="commentCurrent='博客'">博客</div>
          <div class="category-btn" :class="{'active':commentCurrent==='活动'}" @click="commentCurrent='活动'">活动</div>
        </div>
        <div style="margin-top: 10px">
          <blog-list v-if="commentCurrent==='博客'" type="comment"></blog-list>
          <activity-list v-if="commentCurrent==='活动'" :span="8" type="comment"></activity-list>
        </div>
      </el-tab-pane>

      <el-tab-pane label="个人信息" name="个人信息">
        <person-page @update:user="updateUser"/>
      </el-tab-pane>
    </el-tabs>

    <!-- 回答提问弹窗（append-to-body 防止被父级层叠上下文遮挡导致无法点击） -->
    <el-dialog :title="'回答提问：' + (answering ? answering.title : '')" :visible.sync="answerVisible" width="40%"
               :close-on-click-modal="false" :append-to-body="true" custom-class="pink-dialog">
      <el-input type="textarea" :rows="5" v-model="answerText" maxlength="2000" show-word-limit
                placeholder="写下你的回答…" v-if="answering"></el-input>
      <div slot="footer" class="dialog-footer">
        <el-button @click="answerVisible = false">取 消</el-button>
        <el-button type="primary" @click="doAnswer">提交回答</el-button>
      </div>
    </el-dialog>

    <!-- 游客/管理员引导 -->
    <div v-if="guest" class="card" style="text-align: center; padding: 90px 20px;">
      <i class="el-icon-user-solid" style="font-size: 52px; color: #409EFF"></i>
      <p style="font-size: 16px; color: #666666; margin: 18px 0 26px">
        {{ isAdmin ? '个人中心仅供用户账号使用，请在后台管理系统内容' : '登录后可管理你的博客、点赞、收藏和活动报名' }}
      </p>
      <el-button v-if="isAdmin" type="primary" @click="$router.push('/home')">去后台管理</el-button>
      <el-button v-else type="primary" @click="$router.push('/login')">去登录</el-button>
      <el-button v-if="!isAdmin" @click="$router.push('/register')">去注册</el-button>
    </div>

  <Footer/>
  </div>
</template>

<script>
import Footer from "@/components/Footer";
import PersonPage from "@/components/PersonPage";
import BlogList from "@/components/BlogList";
import ActivityList from "@/components/ActivityList";

export default {
  components:{
    Footer,
    BlogList,
    PersonPage,
    ActivityList,
  },

  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请确认密码'))
      } else if (value !== this.user.newPassword) {
        callback(new Error('确认密码错误'))
      } else {
        callback()
      }
    }
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      dialogVisible: false,

      rules: {
        password: [
          { required: true, message: '请输入原始密码', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      },
      activeName: this.$route.query.tab || '我发表的博客',   // 支持从通知跳转定位到指定标签页
      likeCurrent:'博客',
      collectCurrent:'博客',
      commentCurrent:'博客',
      guest: false,
      isAdmin: false,
      myQuestions: [],        // 我提出的问答
      receivedQuestions: [],  // 我收到的问答
      answerVisible: false,
      answerText: '',
      answering: null         // 当前正在回答的提问
    }
  },
  created() {
    const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
    if (!user.id) {   // 游客：展示引导卡片而不是强制跳登录
      this.guest = true
    } else if (user.role !== 'USER') {   // 管理员：个人中心仅供用户账号使用
      this.guest = true
      this.isAdmin = true
    } else {
      this.loadQuestions()
    }
  },
  watch: {
    // 已在本页时点击铃铛通知切换标签页
    '$route.query.tab'(tab) {
      if (tab) this.activeName = tab
    }
  },
  methods: {
    loadQuestions() {   // 加载问答（我提出的 + 我收到的）
      this.$request.get('/question/myAsk').then(res => {
        if (res.code === '200') this.myQuestions = res.data || []
      })
      this.$request.get('/question/myReceived').then(res => {
        if (res.code === '200') this.receivedQuestions = res.data || []
      })
    },
    openAnswer(q) {   // 打开回答弹窗
      this.answering = q
      this.answerText = ''
      this.answerVisible = true
    },
    doAnswer() {   // 提交回答
      if (!this.answerText.trim()) {
        this.$message.warning('回答内容不能为空')
        return
      }
      this.$request.put('/question/answer', { id: this.answering.id, answer: this.answerText }).then(res => {
        if (res.code === '200') {
          this.$message.success('回答成功，提问者将收到通知')
          this.answerVisible = false
          this.loadQuestions()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    updateUser(){
      // 触发父级的数据更新
      this.$emit('update:user')
    },
    addBlog(){
      window.open('/front/newBlog')
    },
    clickTabs(tab){
      console.log(tab)
    },
    update() {
      // 保存当前的用户信息到数据库
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          // 成功更新
          this.$message.success('保存成功')
          // 更新浏览器缓存里的用户信息
          localStorage.setItem('xm-user', JSON.stringify(this.user))

          // 触发父级的数据更新
          this.$emit('update:user')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleAvatarSuccess(response, file, fileList) {
      // 把user的头像属性换成上传的图片的链接
      this.$set(this.user, 'avatar', response.data)
    },
    // 修改密码
    updatePassword() {
      this.dialogVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.put('/updatePassword', this.user).then(res => {
            if (res.code === '200') {
              // 成功更新
              this.$message.success('修改密码成功,请重新登录~')
              this.$router.push('/login')
            } else {
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
/deep/.el-form-item__label {
  font-weight: bold;
}
/deep/.el-upload {
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border-radius: 50%;
}
.avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 50%;
}
.category-btn{
  width: fit-content;
  padding: 5px 10px;
  border-radius: 5px;
  cursor: pointer;
}
.active{
  background-color: #2a60c9 !important;
  color: white !important;
}
</style>