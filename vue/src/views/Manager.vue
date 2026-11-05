<template>
  <div class="manager-container">
    <!--  头部  -->
    <div class="manager-header">
      <div class="manager-header-left">
        <img src="@/assets/imgs/logo.png" />
        <div class="title">星栈始动 · 后台</div>
      </div>

      <!-- 手机/平板端汉堡按钮：打开侧边菜单抽屉（≤991px 显示） -->
      <div class="manager-burger" @click="mobileMenuVisible = true">
        <i class="el-icon-s-fold"></i>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <el-dropdown placement="bottom">
          <div class="avatar">
            <img :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <div>{{ user.name || user.username || '管理员' }}</div>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="goToPerson">个人信息</el-dropdown-item>
            <el-dropdown-item @click.native="$router.push('/password')">修改密码</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!--  主体  -->
    <div class="manager-main">
      <!--  侧边栏  -->
      <div class="manager-main-left">
        <el-menu :default-openeds="['info', 'user']" router style="border: none" :default-active="$route.path">
          <el-menu-item index="/home">
            <i class="el-icon-s-data"></i>
            <span slot="title">数据仪表盘</span>
          </el-menu-item>
          <el-submenu index="info">
            <template slot="title">
              <i class="el-icon-menu"></i><span>信息管理</span>
            </template>
            <el-menu-item index="/category">IT博客分类</el-menu-item>
            <el-menu-item index="/blog">IT博客信息</el-menu-item>
            <el-menu-item index="/activity">活动信息</el-menu-item>
            <el-menu-item index="/activitySign">活动报名信息</el-menu-item>
            <el-menu-item index="/comment">评论信息</el-menu-item>
            <el-menu-item index="/notice">公告信息</el-menu-item>
          </el-submenu>

          <el-submenu index="safe">
            <template slot="title">
              <i class="el-icon-s-check"></i><span>内容安全</span>
            </template>
            <el-menu-item index="/audit">内容审核</el-menu-item>
            <el-menu-item index="/report">举报处理</el-menu-item>
            <el-menu-item index="/sensitiveWord">敏感词管理</el-menu-item>
          </el-submenu>

          <el-submenu index="user">
            <template slot="title">
              <i class="el-icon-menu"></i><span>用户管理</span>
            </template>
            <el-menu-item index="/admin">管理员信息</el-menu-item>
            <el-menu-item index="/user">用户信息</el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <!--  数据表格  -->
      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>

    <!-- 手机/平板端侧边菜单抽屉（≤991px 替代固定侧栏） -->
    <el-drawer title="后台菜单" :visible.sync="mobileMenuVisible" size="220px"
               class="manager-menu-drawer" :append-to-body="true">
      <el-menu :default-openeds="['info', 'user']" router :default-active="$route.path"
               @select="mobileMenuVisible = false">
        <el-menu-item index="/home">
          <i class="el-icon-s-data"></i>
          <span slot="title">数据仪表盘</span>
        </el-menu-item>
        <el-submenu index="info">
          <template slot="title">
            <i class="el-icon-menu"></i><span>信息管理</span>
          </template>
          <el-menu-item index="/category">IT博客分类</el-menu-item>
          <el-menu-item index="/blog">IT博客信息</el-menu-item>
          <el-menu-item index="/activity">活动信息</el-menu-item>
          <el-menu-item index="/activitySign">活动报名信息</el-menu-item>
          <el-menu-item index="/comment">评论信息</el-menu-item>
          <el-menu-item index="/notice">公告信息</el-menu-item>
        </el-submenu>
        <el-submenu index="safe">
          <template slot="title">
            <i class="el-icon-s-check"></i><span>内容安全</span>
          </template>
          <el-menu-item index="/audit">内容审核</el-menu-item>
          <el-menu-item index="/report">举报处理</el-menu-item>
          <el-menu-item index="/sensitiveWord">敏感词管理</el-menu-item>
        </el-submenu>
        <el-submenu index="user">
          <template slot="title">
            <i class="el-icon-menu"></i><span>用户管理</span>
          </template>
          <el-menu-item index="/admin">管理员信息</el-menu-item>
          <el-menu-item index="/user">用户信息</el-menu-item>
        </el-submenu>
      </el-menu>
    </el-drawer>

  </div>
</template>

<script>
export default {
  name: "Manager",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      mobileMenuVisible: false,   // 手机/平板端侧边菜单抽屉（≤991px）
    }
  },
  created() {
    /*非管理员引导到前台首页（游客也可以浏览前台）*/
    if (!this.user.id||this.user.role!=='ADMIN') {
      this.$router.push('/front/home')
    }
  },
  methods: {
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // 重新获取下用户的最新信息
    },
    goToPerson() {
      if (this.user.role === 'ADMIN') {
        this.$router.push('/adminPerson')
      }
    },
    logout() {
      localStorage.removeItem('xm-user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/manager.css";
</style>