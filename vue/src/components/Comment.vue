<template>
  <!--评论开始-->
  <div class="card">
    <h2 style="margin-bottom: 20px">评论 <span style="color: #4d4949">{{commentCount}}</span></h2>

    <div style="margin-bottom: 20px">
      <el-input type="textarea" :rows="3" placeholder="请输入评论内容" v-model="commentContent"></el-input>
      <div style="text-align: right;margin-top: 5px">
        <el-button type="primary" size="small" @click="addComment">评论</el-button>
      </div>
    </div>

    <div>
      <div style="display: flex;grid-gap: 20px;margin-bottom: 20px" v-for="item in commentList" :key="item.id">
        <img :src="item.userAvatar" alt="" style="width: 50px;height: 50px;border-radius: 50%">
        <div style="flex: 1">
          <!--第一级评论-->
          <div style="margin-bottom: 10px">
            <div style="color: #777888;margin-bottom: 10px">{{item.userName}}</div>
            <div style="color: #222226;margin-bottom: 10px">{{item.content}}</div>
            <div style="color: #777888;font-size: 13px;margin-bottom: 10px"><span style="margin-right: 20px">{{item.time}}</span>
              <span style="cursor: pointer" :class="{'comment-active' : item.showReplyInput}" @click="handleShowReplyInput(item)"><i class="el-icon-s-comment">评论</i></span>
              <span style="margin-left: 20px;cursor: pointer" @click="del(item.id)" v-if="item.userId===user.id"><i class="el-icon-delete"></i>删除</span>
            </div>
            <div v-if="item.showReplyInput">
              <el-input type="textarea" placeholder="请输入回复内容" v-model="item.replyContent"></el-input>
              <div style="text-align: right;margin-top: 5px">
                <el-button type="primary" @click="addReply(item)">回 复</el-button>
              </div>
            </div>
          </div>

          <!--这是回复-->
          <div style="display: flex;grid-gap: 20px;margin-bottom: 20px" v-for="sub in item.children" :key="sub.id">
            <img :src="sub.userAvatar" alt="" style="width: 50px;height: 50px;border-radius: 50%">
            <div style="flex: 1">
              <div style="color: #777888;margin-bottom: 10px">
                {{sub.userName}}
                <span style="color: #333333;margin-left: 10px" v-if="sub.replyUser!==item.userName">回复 {{sub.replyUser}}</span>
              </div>
              <div style="color: #222226;margin-bottom: 10px">{{sub.content}}</div>
              <div style="color: #777888;font-size: 13px;margin-bottom: 10px "><span style="margin-right: 20px">{{sub.time}}</span>
                <span style="cursor: pointer" :class="{'comment-active' : sub.showReplyInput}" @click="handleShowReplyInput(sub)"><i class="el-icon-s-comment">评论</i></span>
                <span style="margin-left: 20px;cursor: pointer" @click="del(sub.id)" v-if="sub.userId===user.id"><i class="el-icon-delete"></i>删除</span>
              </div>
              <div v-if="sub.showReplyInput">
                <el-input type="textarea" placeholder="请输入回复内容" v-model="sub.replyContent"></el-input>
                <div style="text-align: right;margin-top: 5px">
                  <el-button type="primary" @click="addReply(sub)">回 复</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--评论结束-->
  </div>

</template>

<script>
export default {
  name: "CommentComponent",
  props:{
    fid:null,
    module:null
  },
  data(){
    return{
      commentCount:0,
      commentContent:'',
      commentList:[],
      user:JSON.parse(localStorage.getItem("xm-user")|| '{}')
    }
  },
  created() {
    this.loadComment()
  },
  methods:{
    del(id) {   // 单个删除
      this.$confirm('您确定删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/comment/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('删除成功！')
            this.loadComment()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleShowReplyInput(item){
      this.$set(item,'showReplyInput',!item.showReplyInput)
    },
    checkUser(){   // 写操作前置检查：需用户身份（游客引导登录，管理员引导使用后台）
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      if (!user.id) {
        this.$message.warning('该操作需要登录，请先登录或注册')
        this.$router.push('/login')
        return false
      }
      if (user.role !== 'USER') {
        this.$message.warning('管理员请使用用户账号进行该操作')
        return false
      }
      return true
    },
    addReply(item){
      if (!this.checkUser()) return
      if (!item.replyContent || !item.replyContent.trim()) {
        this.$message.warning('回复内容不能为空')
        return
      }
      this.$request.post('/comment/add',{pid: item.id,rootId: item.rootId, content: item.replyContent, fid: this.fid,module:this.module}).then(res=>{
        if (res.code==='200'){
          this.$message.success('回复成功！')
          item.replyContent= ''
          this.loadComment() //重新加载数据
        }
      })
    },
    loadComment(){
      this.$request.get('/comment/selectForUser',{
        params:{fid:this.fid,module:this.module}}).then(res=>{
        this.commentList=res.data || []
      })

      this.$request.get('/comment/selectCount',{
        params:{fid:this.fid,module:this.module}
      }).then(res=>{
        this.commentCount=res.data || 0
      })
    },
    addComment(){
      if (!this.checkUser()) return
      if (!this.commentContent || !this.commentContent.trim()) {
        this.$message.warning('评论内容不能为空')
        return
      }
      this.$request.post('/comment/add',{content:this.commentContent,fid:this.fid,module:this.module}).then(res=>{
        if (res.code==='200'){
          this.$message.success('评论成功！')
          this.commentContent=''
          this.loadComment() //重新加载数据
        }
      })
    },
  },
}
</script>

<style scoped>

</style>