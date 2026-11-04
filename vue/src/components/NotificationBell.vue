<template>
  <div class="notice-bell" v-if="user.id">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <span class="bell-text" @click="open">消息</span>
    </el-badge>

    <!-- append-to-body：抽屉挂到 body 层，避免被头部毛玻璃效果的定位上下文影响导致页面假死 -->
    <el-drawer title="消息通知" :visible.sync="drawer" size="380px" :append-to-body="true">
      <div style="padding: 0 15px 20px">
        <div style="text-align: right;margin-bottom: 8px">
          <el-button type="text" size="mini" @click="readAll" v-if="list.length">全部已读</el-button>
        </div>
        <div v-for="n in list" :key="n.id" class="notice-item" :class="{unread: !n.isRead}" @click="clickItem(n)">
          <div class="notice-title">
            <el-tag size="mini" :type="tagType(n.type)">{{ tagText(n.type) }}</el-tag>
            <span style="margin-left: 6px;font-weight: bold">{{ n.title }}</span>
          </div>
          <div class="notice-content">{{ n.content }}</div>
          <div class="notice-time">{{ n.time }}</div>
        </div>
        <div v-if="!list.length" style="text-align: center;color: #999999;padding: 30px">暂无通知</div>
        <div style="text-align: center;margin-top: 10px" v-if="total > pageSize">
          <el-pagination small layout="prev, pager, next" :total="total" :page-size="pageSize"
                         :current-page.sync="pageNum" @current-change="load"></el-pagination>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "NotificationBell",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      drawer: false,
      unreadCount: 0,
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      timer: null
    }
  },
  mounted() {
    this.loadUnread()
    // 每 30 秒轮询一次未读数
    this.timer = setInterval(this.loadUnread, 30000)
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    loadUnread() {
      if (!this.user.id) return
      // 铃铛角标 = 系统通知未读 + 私信未读
      this.$request.get('/notification/unreadCount').then(res => {
        if (res.code === '200') this.unreadCount = res.data || 0
      })
      this.$request.get('/message/unreadCount').then(res => {
        if (res.code === '200') this.unreadCount += res.data || 0
      })
    },
    open() {
      this.drawer = true
      this.pageNum = 1
      this.load()
    },
    load() {
      this.$request.get('/notification/selectPage', {
        params: { pageNum: this.pageNum, pageSize: this.pageSize }
      }).then(res => {
        if (res.code === '200') {
          this.list = res.data?.list || []
          this.total = res.data?.total || 0
        }
      })
    },
    readAll() {
      this.$request.put('/notification/readAll').then(res => {
        if (res.code === '200') {
          this.load()
          this.loadUnread()
        }
      })
    },
    clickItem(n) {
      if (!n.isRead) {
        this.$request.put('/notification/read/' + n.id).then(res => {
          if (res.code === '200') {
            n.isRead = 1
            this.loadUnread()
          }
        })
      }
      // 按类型跳转
      let url = null
      switch (n.type) {
        case 'LIKE':
        case 'COLLECT':
        case 'COMMENT':
          url = '/front/blogDetail?blogId=' + n.targetId; break
        case 'FOLLOW':
          url = '/front/space?userId=' + n.targetId; break
        case 'AUDIT':
        case 'REPORT':
          url = '/front/person'; break
        case 'ASK':
          url = '/front/person?tab=' + encodeURIComponent('收到的提问'); break
        case 'ANSWER':
          url = '/front/person?tab=' + encodeURIComponent('我的提问'); break
        case 'SYSTEM':
          url = n.targetId ? '/front/activityDetail?activityId=' + n.targetId : '/front/activity'; break
      }
      if (url) {
        this.drawer = false
        this.$router.push(url).catch(() => {})
      }
    },
    tagText(type) {
      const map = { LIKE: '点赞', COLLECT: '收藏', COMMENT: '评论', FOLLOW: '粉丝', AUDIT: '审核', REPORT: '举报', SYSTEM: '系统', ASK: '提问', ANSWER: '回答' }
      return map[type] || '通知'
    },
    tagType(type) {
      const map = { LIKE: 'danger', COLLECT: 'warning', COMMENT: 'primary', FOLLOW: 'success', AUDIT: 'warning', REPORT: 'info', SYSTEM: '', ASK: 'warning', ANSWER: 'success' }
      return map[type] || ''
    }
  }
}
</script>

<style scoped>
.notice-bell {
  display: inline-flex;
  align-items: center;
  margin: 0 28px;   /* 与搜索框、头像拉开间距 */
  flex-shrink: 0;   /* 头部空间不足时不被压缩 */
  white-space: nowrap;
  cursor: pointer;
  position: relative;
  top: 2px;
}
.bell-text {
  color: #ffffff;
  font-size: 15px;
  line-height: 1;
}
.notice-item {
  padding: 10px 8px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 6px;
  transition: background .2s;
}
.notice-item:hover {
  background: #f7f3ff;
}
.notice-item.unread {
  background: #fdf3ff;
}
.notice-title {
  font-size: 13px;
  color: #333333;
  margin-bottom: 4px;
}
.notice-content {
  font-size: 12px;
  color: #666666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.notice-time {
  font-size: 11px;
  color: #bbbbbb;
}
</style>
