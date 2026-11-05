<template>
  <div>
    <!-- 萌系漂浮贴纸装饰（纯装饰，不阻挡任何操作） -->
    <div class="bg-decor">
      <span class="d1">☁️</span><span class="d2">💕</span><span class="d3">🪐</span><span class="d4">⭐</span>
      <span class="d5">🌸</span><span class="d6">✨</span><span class="d7">🎀</span><span class="d8">🫧</span>
    </div>
    <!-- 系统更新公告弹窗（登录后未读的最新公告弹一次；append-to-body 防止被父级层叠上下文遮挡） -->
    <el-dialog :title="'系统更新公告 · ' + (latestNotice ? latestNotice.title : '')"
               :visible.sync="noticeDialogVisible" width="42%"
               class="notice-dialog" :append-to-body="true">
      <div class="notice-dialog-body" v-if="latestNotice">{{ latestNotice.content }}</div>
      <div style="font-size: 12px; color: #bbbbbb; text-align: right" v-if="latestNotice">发布于 {{ latestNotice.time }}</div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" round @click="markNoticeRead">我知道了</el-button>
      </div>
    </el-dialog>

    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div style="line-height: 1.2; margin-top: 6px">
          <div class="title" style="line-height: 1.3">星栈始动</div>
          <div class="front-header-sub" style="font-size: 11px;color: rgba(255,255,255,.85);margin-top: 2px;letter-spacing: 1px;line-height: 1.2"> StarStack BootUp</div>
        </div>
      </div>

      <!-- 手机端汉堡导航按钮（≤767px 显示，桌面隐藏） -->
      <div class="front-burger" @click="mobileNavVisible = true">
        <i class="el-icon-s-fold"></i>
      </div>

      <div class="front-header-center">
        <div class="front-header-nav">
          <el-menu :default-active="$route.path" mode="horizontal" router>
						<el-menu-item index="/front/home">首页</el-menu-item>
            <el-menu-item index="/front/recommend">IT博客推荐</el-menu-item>
            <el-menu-item index="/front/activity">活动中心</el-menu-item>
            <el-menu-item index="/front/person">个人中心</el-menu-item>
          </el-menu>
        </div>
      </div>

      <!--搜索框-->
      <div class="front-header-search">
        <el-input placeholder="请输入关键字搜索" class="front-search-input" v-model="title" clearable @keyup.enter.native="goSearch"></el-input>
        <el-button type="success" icon="el-icon-search" @click="goSearch">搜 索</el-button>
      </div>

      <div class="front-header-right">
        <div v-if="!user.username">
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
        <div v-else style="display: flex;align-items: center">
          <!--消息通知铃铛-->
          <notification-bell></notification-bell>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img :src="$thumb(user.avatar)" alt="">
              <div style="margin-left: 10px;color: #ffffff">
                <span>{{ user.name || user.username }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
              </div>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>
                <div style="text-decoration: none" @click="$router.push('/front/space?userId=' + user.id)">个人空间</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div style="text-decoration: none" @click="$router.push('/front/person')">个人中心</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div style="text-decoration: none" @click="logout">退出</div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>
    <!-- 手机端导航抽屉（≤767px 时替代顶部水平菜单） -->
    <el-drawer title="星栈始动" :visible.sync="mobileNavVisible" size="240px"
               class="mobile-nav-drawer" :append-to-body="true">
      <div style="padding: 0 16px">
        <div class="mobile-nav-item" :class="{active: $route.path === '/front/home'}" @click="goMobileNav('/front/home')">
          <i class="el-icon-s-home"></i> 首页
        </div>
        <div class="mobile-nav-item" :class="{active: $route.path === '/front/recommend'}" @click="goMobileNav('/front/recommend')">
          <i class="el-icon-star-off"></i> IT博客推荐
        </div>
        <div class="mobile-nav-item" :class="{active: $route.path === '/front/activity'}" @click="goMobileNav('/front/activity')">
          <i class="el-icon-calendar"></i> 活动中心
        </div>
        <div class="mobile-nav-item" :class="{active: $route.path === '/front/person'}" @click="goMobileNav('/front/person')">
          <i class="el-icon-user"></i> 个人中心
        </div>
        <el-divider></el-divider>
        <el-input placeholder="请输入关键字搜索" v-model="title" clearable @keyup.enter.native="goSearch"></el-input>
        <el-button type="success" icon="el-icon-search" round style="width: 100%; margin-top: 10px" @click="goSearch">搜 索</el-button>
      </div>
    </el-drawer>
    <!--主体-->
    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" />
    </div>
  </div>

</template>

<script>
import NotificationBell from "@/components/NotificationBell.vue";

export default {
  name: "FrontLayout",
  components: { NotificationBell },

  data () {
    return {
      latestNotice: null,        // 最新一条公告（登录弹窗数据源）
      noticeDialogVisible: false,
      mobileNavVisible: false,   // 手机端导航抽屉（≤767px）
      user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
      title:this.$route.query.title
    }
  },

  watch: {
    user() {   // 登录态变化（如刚登录进来）时检查是否需要弹公告
      this.checkNoticeDialog()
    }
  },

  mounted() {
    this.loadNotice()
  },
  methods: {
    goSearch(){
      this.mobileNavVisible = false
      location.href='/front/search?title=' + this.title
    },
    // 手机端抽屉导航跳转：路由跳转后关闭抽屉
    goMobileNav(path){
      this.mobileNavVisible = false
      if (this.$route.path !== path) this.$router.push(path)
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notice = res.data || []
        if (this.notice.length) {
          this.latestNotice = this.notice[0]   // 最新一条：登录后弹窗告知
          this.checkNoticeDialog()
        }
      })
    },
    checkNoticeDialog() {   // 登录用户：最新公告未读过则弹窗告知一次
      const latest = this.latestNotice
      if (!latest || !this.user.id) return
      const readKey = 'notice-read-' + this.user.id
      if (String(localStorage.getItem(readKey)) !== String(latest.id)) {
        this.noticeDialogVisible = true
      }
    },
    markNoticeRead() {   // 点"我知道了"：按用户记录已读公告ID，之后不再弹
      const latest = this.latestNotice
      if (latest && this.user.id) {
        localStorage.setItem('notice-read-' + this.user.id, String(latest.id))
      }
      this.noticeDialogVisible = false
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem("xm-user") || '{}')   // 重新获取下用户的最新信息
    },
    // 退出登录
    logout() {
      localStorage.removeItem("xm-user");
      this.$router.push("/login");
    },
  }

}
</script>

<style scoped>
  @import "@/assets/css/front.css";
</style>