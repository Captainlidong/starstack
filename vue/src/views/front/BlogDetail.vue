<!--博客详情页-->

<template>
  <div class="main-content">

    <div class="detail-flex" style="display: flex; grid-gap: 10px">

      <!-- 左边 -->
      <div class="detail-body-pad" style="flex: 1;width: 0">
        <div class="card" style="padding: 30px;margin-bottom: 10px">
          <div style="font-weight: bold;font-size: 24px;margin-bottom: 20px">
            {{blog.title}}
          </div> 

          <div style="color:#666666;margin-bottom: 20px">
            <span style="margin-right: 20px;cursor: pointer" @click="goSpace(blog.userId)">
              <i class="el-icon-user"></i>
              <span style="border-bottom: 1px dashed #bbb">{{blog.userName}}</span>
            </span>
            <span style="margin-right: 20px">
              <i class="el-icon-date">
                {{blog.date}}
              </i>
            </span>
            <span style="margin-right: 20px">
              <i class="el-icon-eye">
                {{blog.readCount}}
              </i>
            </span>
            <span>
              <el-tag v-for="item in tagsArr" :key="item" type="primary" style="margin-right:5px">
                {{ item }}
              </el-tag>
            </span>
          </div>

          <div class="w-e-text">
            <div v-html="blog.content" style="width: 100%"></div>
          </div>

        </div>

        <!--评论区-->
        <div ref="commentArea">
          <Comment :fid="blogId" module="博客" />
        </div>

        <!--举报弹窗-->
        <report-dialog :visible.sync="reportVisible" target-type="BLOG" :target-id="blogId" :target-title="blog.title"></report-dialog>

      </div>

      <!--右侧悬浮工具栏（CSDN式）：不随内容滚动，滚动后出现回到顶部-->
      <div class="detail-toolbar">
        <div class="tool-item" @click="setLikes" title="点赞">
          <i class="el-icon-dianzan" :class="{'tool-active-like': blog.userLike}"></i>
          <span class="tool-badge" v-if="blog.likesCount">{{ blog.likesCount }}</span>
        </div>
        <div class="tool-item" @click="scrollToComment" title="评论">
          <i class="el-icon-chat-dot-square"></i>
          <span class="tool-badge" v-if="commentCount">{{ commentCount }}</span>
        </div>
        <div class="tool-item" @click="setCollect" title="收藏">
          <i class="el-icon-star-off" :class="{'tool-active-collect': blog.userCollect}"></i>
          <span class="tool-badge" v-if="blog.collectCount">{{ blog.collectCount }}</span>
        </div>
        <div class="tool-item" @click="share" title="分享">
          <i class="el-icon-share"></i>
        </div>
        <div class="tool-item" @click="reportVisible = true" title="举报">
          <i class="el-icon-warning-outline"></i>
        </div>
        <div class="tool-item" v-show="showTop" @click="backTop" title="回到顶部">
          <i class="el-icon-top"></i>
        </div>
      </div>

      <!--右边-->
      <div class="detail-aside" style="width: 260px">

        <!--右边 -- 上边卡片-->
        <div class="card" style="margin-bottom: 10px">

             <!--第一行-->
          <div style="display: flex;align-items: center;grid-gap: 10px;margin-bottom: 10px">
            <!--左边头像信息-->
            <img :src="blog.user?.avatar" alt="" style="width: 50px;height:50px;border-radius: 50%;cursor: pointer" @click="goSpace(blog.userId)">
            <!--右边作者信息-->
            <div style="flex: 1">
              <!--第一层：昵称 + 关注按钮（同一行）-->
              <div style="display: flex;align-items: center;justify-content: space-between;margin-bottom: 5px">
                <div style="font-weight: bold;cursor: pointer" @click="goSpace(blog.userId)">
                  {{blog.user?.name}}
                </div>
                <el-button v-if="!isSelf && blog.user?.id" size="mini" round
                           :class="['follow-btn', {'follow-btn-glow': glow}]"
                           :type="isFollowed ? 'info' : 'primary'"
                           @click="toggleFollow">
                  {{ isFollowed ? '已关注' : '+ 关注' }}
                </el-button>
                <el-button v-else-if="isSelf" size="mini" round class="follow-btn" type="info"
                           @click="$router.push('/front/person')">编辑资料</el-button>
              </div>
              <!--第二层作者简介信息-->
              <div style="color: #666666;font-size: 13px;" class="line2">
                {{blog.user?.info}}
              </div>
            </div>
          </div>

             <!--第二行-->
          <div style="display: flex">
            <div style="flex: 1;text-align: center">
              <!--文章数显示-->
              <div style="margin-bottom: 5px">文章</div>
              <div style="color: #4e4d4d">{{blog.user?.blogCount}}</div>
            </div>

            <div style="flex: 1;text-align: center">
              <!--点赞数显示-->
              <div style="margin-bottom: 5px">点赞</div>
              <div style="color: #4e4d4d">{{blog.user?.likesCount}}</div>
            </div>

            <div style="flex: 1;text-align: center">
              <!--收藏数显示-->
              <div style="margin-bottom: 5px">收藏</div>
              <div style="color: #4e4d4d">{{blog.user?.collectCount}}</div>
            </div>

            <div style="flex: 1;text-align: center">
              <!--粉丝数显示-->
              <div style="margin-bottom: 5px">粉丝</div>
              <div style="color: #4e4d4d">{{blog.user?.fansCount}}</div>
            </div>

          </div>

        </div>

        <!--右边 -- 下边卡片 -->
        <div class="card" style="margin-bottom: 10px">

          <div style="font-weight: bold;font-size: 20px;padding-bottom: 10px;border-bottom: 1px solid #dddddd;margin-bottom: 10px">
            相关技术文章推荐
          </div>

          <div>
            <div style="margin-bottom:15px" v-for="item in recommendList" :key="item.id">

              <a :href="'/front/blogDetail?blogId='+item.id" target="_blank">
                <div class="recommend-title line2">{{item.title}}</div>
              </a>

              <div style="color: #888">
                <span>阅读</span> <span>{{ item.readCount }}</span>
                <span style="margin-left: 10px">点赞</span> <span>{{ item.likesCount }}</span>
              </div>

            </div>
          </div>

        </div>

        <!--右边最下边--广告区-->
        <div class="card">
          <div style="display: flex;grid-gap: 10px">
            <div style="flex: 1;line-height: 25px">
              一起来学编程吧！！！
            </div>
            <img src="@/assets/imgs/广告.png" alt="" style="width: 80px;height: 80px;border-radius: 5px">
          </div>
        </div>

      </div>

    </div>
    <Footer />
  </div>
</template>

<script>
import Footer from "@/components/Footer";
import Comment from "@/components/Comment";
import ReportDialog from "@/components/ReportDialog";
export default {
  name: "BlogDetail",
  computed: {
    comment() {
      return comment
    },
    isSelf() {
      return this.user.id && Number(this.user.id) === Number(this.blog.userId)
    }
  },
  components: {
    Comment,
    Footer,
    ReportDialog
  },
  data() {
    return {
      blogId: this.$route.query.blogId,
      blog: {},
      tagsArr:[],
      recommendList:[],
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      reportVisible: false,
      isFollowed: false,
      commentCount: 0,
      showTop: false,
      glow: false
    }
  },
  created() {
    this.load()
    this.loadFollowState()

    this.$request.put('blog/updateReadCount/'+this.blogId)
  },
  mounted() {
    window.addEventListener('scroll', this.onScroll)
    window.addEventListener('resize', this.alignToolbar)
    this.alignToolbar()
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.onScroll)
    window.removeEventListener('resize', this.alignToolbar)
  },
  methods: {
    alignToolbar() {   // 实测"相关技术文章推荐"卡顶部的文档位置，作为工具栏吸附点（任何缩放/分辨率下与卡片平齐）
      this.$nextTick(() => {
        const cards = document.querySelectorAll('.main-content > div > div:last-child > .card')
        const recommendCard = cards[1]   // 右栏第二张卡 = 相关技术文章推荐
        if (recommendCard) {
          const docTop = Math.round(recommendCard.getBoundingClientRect().top + window.scrollY)
          document.documentElement.style.setProperty('--toolbar-top', docTop + 'px')
        }
      })
    },
    onScroll() {   // 滚动超过 300px 显示"回到顶部"
      this.showTop = window.scrollY > 300
    },
    backTop() {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    scrollToComment() {   // 滚动到评论区
      const el = this.$refs.commentArea
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    goSpace(userId) {   // 跳转作者个人空间
      if (userId) this.$router.push('/front/space?userId=' + userId)
    },
    toggleFollow() {   // 详情页关注/取关作者（带光效）
      if (!this.checkUser()) return
      const url = '/follow/' + this.blog.userId
      const req = this.isFollowed ? this.$request.delete(url) : this.$request.post(url)
      req.then(res => {
        if (res.code === '200') {
          this.isFollowed = !this.isFollowed
          this.glow = true
          setTimeout(() => { this.glow = false }, 900)
          this.$message.success(this.isFollowed ? '关注成功' : '已取消关注')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadFollowState() {   // 当前用户是否已关注作者
      if (!this.user.id) return
      this.$request.get('/space/isFollowed/' + this.blog.userId).then(res => {
        if (res.code === '200') this.isFollowed = res.data === true
      })
    },
    share() {   // 复制链接分享
      const link = location.origin + '/front/blogDetail?blogId=' + this.blogId
      if (navigator.clipboard) {
        navigator.clipboard.writeText(link).then(() => {
          this.$message.success('链接已复制，快去分享吧~')
        }).catch(() => {
          this.$message.info('分享链接：' + link)
        })
      } else {
        this.$message.info('分享链接：' + link)
      }
    },
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
    setLikes(){
      if (!this.checkUser()) return
      this.$request.post('/likes/set',{fid:this.blogId,module:'博客'}).then(res => {
        if (res.code==='200'){
          this.load() //重新加载数据
        }
      })
    },
    setCollect(){
      if (!this.checkUser()) return
      this.$request.post('/collect/set',{fid:this.blogId,module:'博客'}).then(res => {
        if (res.code==='200'){
          this.load() //重新加载数据
        }
      })
    },
    load() {
      this.$request.get('/blog/selectById/' + this.blogId).then(res => {
        if (res.code !== '200') {   // 博客不存在/未公开（草稿、私人、被下架等）
          this.$alert(res.msg || '该博客不存在或未公开', '提示', {
            confirmButtonText: '返回首页',
            callback: () => { this.$router.push('/front/home') }
          })
          return
        }
        this.blog = res.data || {}
        this.tagsArr = JSON.parse(this.blog.tags || '[]')
        this.loadFollowState()
        // 正文内嵌图片原生懒加载：视口外的图滚动到才加载，降低详情页首屏带宽
        this.$nextTick(() => {
          this.$el.querySelectorAll('.blog-content img, [v-html] img, .main-content img').forEach(img => {
            img.setAttribute('loading', 'lazy')
          })
        })
      })

      // 评论数（悬浮工具栏角标）
      this.$request.get('/comment/selectCount', {
        params: { fid: this.blogId, module: '博客' }
      }).then(res => {
        this.commentCount = res.data || 0
      })

      // 博客推荐
      this.$request.get('/blog/selectRecommend/' + this.blogId).then(res => {
        this.recommendList = res.data || []
      })
    }
  }
}
</script>

<style>
/* blockquote 样式 */
blockquote {
  display: block;
  border-left: 8px solid #d0e5f2;
  padding: 20px 10px;
  margin: 10px 0;
  line-height: 1.4;
  font-size: 100%;
  background-color: #f1f1f1;
}

/* code 样式 */
code {
  display: inline-block;
  *display: inline;
  *zoom: 1;
  background-color: #f1f1f1;
  border-radius: 3px;
  padding: 3px 5px;
  margin: 0 3px;
}
pre code {
  display: block;
}
p {
  line-height: 30px
}
.active{
  color: orange !important;
}
.recommend-title{
  margin-bottom: 5px
}
.recommend-title:hover{
  color: #2a60c9;
}
.comment-active{
  color: #2a60c9;
}
pre{
  white-space: pre-wrap;			/* 保留空白符序列，并正常进行换行 */
  white-space: -moz-pre-wrap;		/* 兼容火狐浏览器 */
  white-space: pre-wrap;	/* 兼容谷歌浏览器 */
  white-space: -o-pre-wrap;			/* 兼容opera浏览器 */
  word-wrap: break-word;			/* 允许字母、url地址换行 */

}

/* ============ 悬浮工具栏（CSDN式）：夹在博客与作者卡之间，粘性定位不随滚动 ============ */
.detail-toolbar {
  position: sticky;
  /* 吸附时与右侧"相关技术文章推荐"卡片顶部平齐：--toolbar-top 由 mounted/resize 时动态实测写入 */
  top: var(--toolbar-top, 272px);
  align-self: flex-start;
  width: 56px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 30;
}
.tool-item {
  position: relative;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666666;
  font-size: 20px;
  margin-bottom: 14px;
  transition: all .2s;
}
.tool-item:hover {
  color: #b79cff;
  transform: translateY(-2px);
  box-shadow: 0 4px 14px rgba(160, 120, 220, 0.3);
}
.tool-item .tool-active-like {
  color: #e8619c;
}
.tool-item .tool-active-collect {
  color: #e6a23c;
}
.tool-badge {
  position: absolute;
  top: -5px;
  right: -8px;
  background: #b79cff;
  color: #ffffff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  padding: 0 4px;
  text-align: center;
}

/* ============ 作者卡关注按钮 ============ */
.follow-btn.el-button--primary {
  background: linear-gradient(135deg, #b79cff, #ff9ecf) !important;
  border: none !important;
  color: #ffffff !important;
}
.follow-btn.el-button--info {
  background: #f3e8ff !important;
  border: 1px solid #d8c9f5 !important;
  color: #8b5cf6 !important;
}
.follow-btn.el-button--info:hover {
  color: #f56c6c !important;
  background: #ffeaf5 !important;
}
.follow-btn-glow {
  animation: followGlow .8s ease;
}
@keyframes followGlow {
  0% { box-shadow: 0 0 0 0 rgba(183, 156, 255, 0.6); transform: scale(1); }
  50% { box-shadow: 0 0 0 8px rgba(183, 156, 255, 0.15); transform: scale(1.08); }
  100% { box-shadow: 0 0 0 0 rgba(183, 156, 255, 0); transform: scale(1); }
}
</style>