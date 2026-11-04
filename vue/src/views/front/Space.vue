<!--个人空间（仿CSDN）：头部信息+统计，文章/粉丝/关注列表-->
<template>
  <div class="main-content">

    <!-- 头部卡片 -->
    <div class="card space-header" v-loading="loading">
      <img class="space-avatar" :src="user.avatar" alt="">
      <div class="space-info">
        <div class="space-name">
          {{ user.name || user.username }}
          <span class="space-username">@{{ user.username }}</span>
        </div>
        <div class="space-stats">
          <span class="stat-item">总访问量 <b>{{ totalReads }}</b></span>
          <span class="stat-item">原创 <b>{{ blogCount }}</b></span>
          <span class="stat-item" @click="activeTab='fans'">粉丝 <b>{{ fansCount }}</b></span>
          <span class="stat-item" @click="activeTab='following'">关注 <b>{{ followCount }}</b></span>
          <span class="stat-item">获赞 <b>{{ likesCount }}</b></span>
        </div>
        <div class="space-bio">个人简介：{{ user.info || '这个人很懒，什么都没有写' }}</div>
      </div>
      <div class="space-actions">
        <template v-if="!isSelf && user.id">
          <el-button round :type="isFollowed ? 'info' : 'primary'" @click="toggleFollow">
            {{ isFollowed ? '已关注' : '+ 关注' }}
          </el-button>
          <!-- 更多操作：提问 / 私信（CSDN式三点菜单） -->
          <el-dropdown trigger="click" @command="moreAction">
            <span class="more-btn"><i class="el-icon-more"></i></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="ask">提 问</el-dropdown-item>
              <el-dropdown-item command="message">私 信</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <template v-else-if="isSelf">
          <el-button round @click="$router.push('/front/person')">编辑资料</el-button>
          <el-button round type="primary" @click="$router.push('/front/newBlog')">写博客</el-button>
        </template>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="card" style="margin-top: 10px;min-height: 400px">
      <el-tabs v-model="activeTab">
        <!-- 文章 -->
        <el-tab-pane name="blogs">
          <span slot="label">文章 <span class="tab-count">{{ blogCount }}</span></span>
          <blog-list v-if="activeTab==='blogs'" type="space" :user-id="userId"/>
        </el-tab-pane>

        <!-- 粉丝 -->
        <el-tab-pane name="fans">
          <span slot="label">粉丝 <span class="tab-count">{{ fansCount }}</span></span>
          <div v-if="activeTab==='fans'" v-loading="listLoading">
            <div v-for="u in userList" :key="u.id" class="user-item">
              <img class="user-avatar" :src="u.avatar" @click="goSpace(u.id)" alt="">
              <div class="user-meta" @click="goSpace(u.id)">
                <div class="user-nickname">{{ u.name || u.username }}</div>
                <div class="user-bio">{{ u.info || '这个人很懒，什么都没有写' }}</div>
              </div>
              <el-button v-if="u.id !== me.id" size="mini" round
                         :type="u.isFollowed ? 'info' : 'primary'" @click="toggleFollowUser(u)">
                {{ u.isFollowed ? '已关注' : '关注' }}
              </el-button>
            </div>
            <div v-if="!userList.length" class="empty-tip">暂无粉丝，快去发布优质博客吸引关注吧~</div>
            <div style="text-align: center;margin-top: 10px" v-if="listTotal > listPageSize">
              <el-pagination background layout="prev, pager, next" :total="listTotal" :page-size="listPageSize"
                             :current-page.sync="listPageNum" @current-change="loadList"></el-pagination>
            </div>
          </div>
        </el-tab-pane>

        <!-- 关注 -->
        <el-tab-pane name="following">
          <span slot="label">关注 <span class="tab-count">{{ followCount }}</span></span>
          <div v-if="activeTab==='following'" v-loading="listLoading">
            <div v-for="u in userList" :key="u.id" class="user-item">
              <img class="user-avatar" :src="u.avatar" @click="goSpace(u.id)" alt="">
              <div class="user-meta" @click="goSpace(u.id)">
                <div class="user-nickname">{{ u.name || u.username }}</div>
                <div class="user-bio">{{ u.info || '这个人很懒，什么都没有写' }}</div>
              </div>
              <el-button v-if="u.id !== me.id" size="mini" round
                         :type="u.isFollowed ? 'info' : 'primary'" @click="toggleFollowUser(u)">
                {{ u.isFollowed ? '已关注' : '关注' }}
              </el-button>
            </div>
            <div v-if="!userList.length" class="empty-tip">暂无关注，去发现感兴趣的人吧~</div>
            <div style="text-align: center;margin-top: 10px" v-if="listTotal > listPageSize">
              <el-pagination background layout="prev, pager, next" :total="listTotal" :page-size="listPageSize"
                             :current-page.sync="listPageNum" @current-change="loadList"></el-pagination>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

  </div>
</template>

<script>
import BlogList from "@/components/BlogList.vue";

export default {
  name: "Space",
  components: { BlogList },
  data() {
    return {
      userId: this.$route.query.userId,   // 空间主人
      me: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      user: {},
      loading: false,
      listLoading: false,
      isFollowed: false,
      blogCount: 0,
      totalReads: 0,
      likesCount: 0,
      collectCount: 0,
      fansCount: 0,
      followCount: 0,
      activeTab: 'blogs',
      userList: [],
      listPageNum: 1,
      listPageSize: 10,
      listTotal: 0
    }
  },
  computed: {
    isSelf() {
      return this.me.id && Number(this.me.id) === Number(this.userId)
    }
  },
  watch: {
    activeTab(tab) {
      if (tab === 'fans' || tab === 'following') this.loadList(1)
    },
    '$route.query.userId'() {   // 空间内跳转到其他用户空间
      this.userId = this.$route.query.userId
      this.load()
      if (this.activeTab !== 'blogs') this.loadList(1)
    }
  },
  mounted() {
    if (!this.userId) {
      if (this.me.id) {
        this.userId = this.me.id   // 不带参数时展示自己的空间
      } else {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
    }
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      this.$request.get('/space/info/' + this.userId).then(res => {
        if (res.code === '200') {
          this.user = res.data?.user || {}
          this.blogCount = res.data?.blogCount || 0
          this.totalReads = res.data?.totalReads || 0
          this.likesCount = res.data?.likesCount || 0
          this.collectCount = res.data?.collectCount || 0
          this.fansCount = res.data?.fansCount || 0
          this.followCount = res.data?.followCount || 0
        }
        this.loading = false
      }).catch(() => { this.loading = false })
      this.$request.get('/space/isFollowed/' + this.userId).then(res => {
        if (res.code === '200') this.isFollowed = res.data === true
      })
    },
    loadList(pageNum) {
      if (pageNum) this.listPageNum = pageNum
      this.listLoading = true
      const url = this.activeTab === 'fans' ? '/space/fans/' : '/space/following/'
      this.$request.get(url + this.userId, {
        params: { pageNum: this.listPageNum, pageSize: this.listPageSize }
      }).then(res => {
        if (res.code === '200') {
          this.userList = res.data?.list || []
          this.listTotal = res.data?.total || 0
        }
        this.listLoading = false
      }).catch(() => { this.listLoading = false })
    },
    toggleFollow() {   // 头部关注/取关
      if (!this.me.id) {
        this.$message.warning('请先登录')
        return this.$router.push('/login')
      }
      const url = '/follow/' + this.userId
      const req = this.isFollowed ? this.$request.delete(url) : this.$request.post(url)
      req.then(res => {
        if (res.code === '200') {
          this.isFollowed = !this.isFollowed
          this.fansCount += this.isFollowed ? 1 : -1
          this.$message.success(this.isFollowed ? '关注成功' : '已取消关注')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    toggleFollowUser(u) {   // 列表里的关注/取关
      const url = '/follow/' + u.id
      const req = u.isFollowed ? this.$request.delete(url) : this.$request.post(url)
      req.then(res => {
        if (res.code === '200') {
          u.isFollowed = !u.isFollowed
          // 如果操作的是当前空间的关注列表且空间主人是自己，联动统计
          this.$message.success(u.isFollowed ? '关注成功' : '已取消关注')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    goSpace(id) {
      if (id && Number(id) !== Number(this.userId)) {
        this.$router.push('/front/space?userId=' + id)
      }
    },
    moreAction(command) {   // 三点菜单：提问 / 私信
      if (!this.me.id) {
        this.$message.warning('请先登录')
        return this.$router.push('/login')
      }
      if (command === 'ask') {
        this.$router.push('/front/ask?userId=' + this.userId)
      } else if (command === 'message') {
        this.$router.push('/front/message?userId=' + this.userId)
      }
    }
  }
}
</script>

<style scoped>
.space-header {
  display: flex;
  align-items: center;
  grid-gap: 20px;
  padding: 25px 30px;
}
.space-avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 16px rgba(160, 120, 220, .25);
}
.space-info {
  flex: 1;
}
.space-name {
  font-size: 22px;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8px;
}
.space-username {
  font-size: 13px;
  color: #999999;
  font-weight: normal;
  margin-left: 8px;
}
.space-stats {
  font-size: 13px;
  color: #666666;
  margin-bottom: 8px;
}
.stat-item {
  margin-right: 20px;
  cursor: pointer;
}
.stat-item:hover {
  color: #2a60c9;
}
.stat-item b {
  font-size: 16px;
  color: #333333;
  margin-left: 2px;
}
.space-bio {
  font-size: 13px;
  color: #888888;
}
.space-actions {
  display: flex;
  align-items: center;
  grid-gap: 10px;
}
.more-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: all .2s;
}
.more-btn:hover {
  color: #8b5cf6;
  border-color: #b79cff;
  background: #f7f3ff;
}
.tab-count {
  font-size: 12px;
  color: #999999;
}
.user-item {
  display: flex;
  align-items: center;
  grid-gap: 12px;
  padding: 14px 6px;
  border-bottom: 1px solid #f0f0f0;
}
.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}
.user-meta {
  flex: 1;
  cursor: pointer;
}
.user-nickname {
  font-size: 15px;
  font-weight: bold;
  color: #333333;
  margin-bottom: 4px;
}
.user-nickname:hover {
  color: #2a60c9;
}
.user-bio {
  font-size: 12px;
  color: #999999;
}
.empty-tip {
  text-align: center;
  color: #999999;
  padding: 40px 0;
  font-size: 14px;
}
</style>
