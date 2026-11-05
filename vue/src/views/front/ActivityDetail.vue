<template>
  <div class="main-content narrow-page" style="width: 65%">
    <div class="card" style="margin-bottom: 10px">
      <div class="act-head" style="display: flex; grid-gap: 20px">
        <img :src="activity.cover" alt="" style="width: 30%; height: 250px; border-radius: 5px">

        <div style="flex: 1; position: relative">
          <div style="font-weight: bold; font-size: 24px; margin-bottom: 10px">{{ activity.name }}</div>
          <div style="color: #666; font-size: 13px; line-height: 22px; text-align: justify; margin-bottom: 10px">{{ activity.descr }}</div>
          <div style="color: #666; margin-bottom: 10px"><i class="el-icon-date"></i> 时间 : <span style="margin-left: 5px">{{ activity.start }} ~ {{ activity.end }}</span></div>
          <div style="color: #666; margin-bottom: 10px"><i class="el-icon-location"></i> 地址 :
            <a style="color: #2a60c9; margin-left: 5px" :href="activity.address" v-if="activity.form === '线上'">{{ activity.address }}</a>
            <span style="margin-left: 5px" v-else>{{ activity.address }}</span>
            <el-tag style="margin-left: 5px" type="primary" size="mini">{{ activity.form }}</el-tag>
          </div>

          <div class="act-actions" style="position: absolute;bottom: 0;width: 100%">
            <div class="act-btn-row" style="display: flex; align-items: center">
              <div style="flex: 1">
                <el-button type="primary" disabled v-if="activity.isEnd" key="已结束">已结束</el-button>
                <el-button type="success"  v-else-if="activity.isSign" :key="signText" @click="cancel"
                           @mouseenter.native="signText='取消报名'" @mouseleave.native="signText='已报名'">{{ signText }}</el-button>
                <el-button type="primary" v-else @click="sign">报 名</el-button>
              </div>

              <div style="text-align: right; flex: 1">
                <el-button type="danger" @click="like" v-if="activity.isLike">已点赞 {{ activity.likesCount }}</el-button>
                <el-button type="danger" @click="like" v-else>点赞 {{ activity.likesCount }}</el-button>

                <el-button type="warning" @click="collect" v-if="activity.isCollect">已收藏 {{ activity.collectCount }}</el-button>
                <el-button type="warning" @click="collect" v-else>收藏 {{ activity.collectCount }}</el-button>
                <el-button type="info">阅读量 {{ activity.readCount }}</el-button>
                <el-button type="info" plain icon="el-icon-warning-outline" @click="reportVisible = true">举报</el-button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

    <div class="card" style="margin-bottom: 10px">
      <div style="font-weight: bold; font-size: 24px; margin-bottom: 20px; padding-left: 10px; border-left: 5px solid #2a60c9">活动详情</div>
      <div class="w-e-text">
        <div v-html="activity.content"></div>
      </div>
    </div>

    <div>
      <Comment :fid="activityId" module="活动" />
    </div>

    <!--举报弹窗-->
    <report-dialog :visible.sync="reportVisible" target-type="ACTIVITY" :target-id="activityId" :target-title="activity.name"></report-dialog>

  </div>
</template>

<script>
import Comment from "@/components/Comment";
import ReportDialog from "@/components/ReportDialog";
export default {
  name: "ActivityDetail",
  components: {Comment, ReportDialog},
  data() {
    return {
      activity: {},
      activityId: this.$route.query.activityId,
      signText:'已报名',
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      reportVisible: false
    }
  },
  created() {
    this.load()

    this.$request.put('/activity/updateReadCount/' + this.activityId)
  },
  methods: {
    checkUser(){   // 写操作前置检查：需用户身份（游客引导登录，管理员引导使用后台）
      if (!this.user || !this.user.id) {
        this.$message.warning('该操作需要登录，请先登录或注册')
        this.$router.push('/login')
        return false
      }
      if (this.user.role !== 'USER') {
        this.$message.warning('管理员请使用用户账号进行该操作')
        return false
      }
      return true
    },
    cancel() {   // 活动取消报名
      if (!this.checkUser()) return
      this.$confirm('您确定取消报名吗？', '确认取消', {type: "warning"}).then(response => {
        this.$request.delete('/activitySign/delete/user/' + this.activityId+'/'+this.user.id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('取消成功')
            this.load()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    collect() {
      if (!this.checkUser()) return
      this.$request.post('/collect/set', { fid: this.activityId, module: '活动' }).then(res => {
        if (res.code === '200') {
          this.$message.success('操作成功')
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    like() {
      if (!this.checkUser()) return
      this.$request.post('/likes/set', { fid: this.activityId, module: '活动' }).then(res => {
        if (res.code === '200') {
          this.$message.success('操作成功')
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    sign() {
      if (!this.checkUser()) return
      this.$request.post('/activitySign/add', { activityId: this.activityId }).then(res => {
        if (res.code === '200') {
          this.$message.success('报名成功')
          this.load()  //刷新一下
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    load() {
      this.$request.get('/activity/selectById/' + this.activityId).then(res => {
        this.activity = res.data || {}
      })
    }
  }
}
</script>

<style scoped>

</style>