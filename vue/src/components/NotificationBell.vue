<template>
  <div class="notice-bell" v-if="user.id">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <span class="bell-text" @click="open">消息</span>
    </el-badge>

    <!-- append-to-body：抽屉挂到 body 层，避免被头部毛玻璃效果的定位上下文影响导致页面假死 -->
    <el-drawer title="消息通知" :visible.sync="drawer" size="380px" :append-to-body="true">
      <div style="padding: 0 15px 20px">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
          <el-button type="text" size="mini" @click="$router.push('/front/message')">
            <i class="el-icon-chat-dot-square"></i> 查看私信
          </el-button>
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
      notifUnread: 0,        // 系统通知未读（30s 轮询）
      msgUnread: 0,          // 私信未读（WebSocket 实时维护）
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      timer: null,           // 系统通知未读兜底轮询
      ws: null,              // WebSocket 连接
      wsRetryTimer: null,    // 重连定时器
      wsRetry: 0,            // 重连次数
      wsManualClose: false   // 是否手动关闭（登出）
    }
  },
  computed: {
    isLogged() {
      return !!this.user.id
    }
  },
  watch: {
    // 登录/登出状态变化：登录则建立 WS 连接，登出则断开
    isLogged(logged) {
      if (logged) {
        this.connectWS()
        this.startPoll()
        this.loadUnread()
      } else {
        this.closeWS()
        this.stopPoll()
        this.unreadCount = 0
        this.notifUnread = 0
        this.msgUnread = 0
      }
    },
    '$route.path'() {   // 路由切换时若连接异常则重试
      if (this.isLogged && (!this.ws || this.ws.readyState > 1)) {
        this.connectWS()
      }
    }
  },
  mounted() {
    if (this.isLogged) {
      this.connectWS()
      this.startPoll()
      this.loadUnread()
    }
    // 聊天页标已读后：重新拉取私信未读数（避免正看会话时角标虚高）
    this.$bus.$on('msg:read', this.refreshMsgUnread)
  },
  beforeDestroy() {
    this.$bus.$off('msg:read', this.refreshMsgUnread)
    this.stopPoll()
    this.closeWS()
  },
  methods: {
    // ========== WebSocket 全局连接（私信实时推送） ==========
    connectWS() {
      if (this.ws && (this.ws.readyState === 0 || this.ws.readyState === 1)) return
      const proto = location.protocol === 'https:' ? 'wss://' : 'ws://'
      this.wsManualClose = false
      this.ws = new WebSocket(proto + location.host + '/ws/message?token=' + (this.user.token || ''))
      this.ws.onopen = () => {
        this.wsRetry = 0
        this.refreshMsgUnread()          // 重连后对齐私信未读数
        this.$bus.$emit('ws:connected')  // 通知聊天页补齐断线期间的消息
      }
      this.ws.onmessage = e => {
        let data = null
        try { data = JSON.parse(e.data) } catch (err) { return }
        if (!data || !data.type) return
        if (data.type === 'pong') return
        if (data.type === 'new-message') {
          this.msgUnread++
          this.unreadCount = this.notifUnread + this.msgUnread
          this.$bus.$emit('ws:new-message', data)   // 广播给聊天页（Message.vue）
        }
      }
      this.ws.onclose = () => {
        if (this.wsManualClose) return
        // 断线 5 秒后自动重连
        if (this.wsRetryTimer) clearTimeout(this.wsRetryTimer)
        this.wsRetryTimer = setTimeout(() => this.connectWS(), 5000)
      }
      this.ws.onerror = () => {}
    },
    closeWS() {
      this.wsManualClose = true
      if (this.ws) { this.ws.close(); this.ws = null }
      if (this.wsRetryTimer) { clearTimeout(this.wsRetryTimer); this.wsRetryTimer = null }
    },
    // ========== 未读数 ==========
    loadUnread() {
      if (!this.user.id) return
      // 系统通知未读（30s 兜底轮询）+ 私信未读（WS 实时增量）
      this.$request.get('/notification/unreadCount').then(res => {
        if (res.code === '200') {
          this.notifUnread = res.data || 0
          this.unreadCount = this.notifUnread + this.msgUnread
        }
      })
      this.refreshMsgUnread()
    },
    refreshMsgUnread() {
      if (!this.user.id) return
      this.$request.get('/message/unreadCount').then(res => {
        if (res.code === '200') {
          this.msgUnread = res.data || 0
          this.unreadCount = this.notifUnread + this.msgUnread
        }
      })
    },
    onWsNewMessage() {   // 收到实时私信推送：角标 +1
      this.msgUnread++
      this.unreadCount = this.notifUnread + this.msgUnread
    },
    onWsConnected() {   // WS 重连成功：对齐私信未读数
      this.refreshMsgUnread()
    },
    startPoll() {
      this.timer = setInterval(() => {
        if (this.user.id) this.loadUnread()
      }, 30000)
    },
    stopPoll() {
      if (this.timer) { clearInterval(this.timer); this.timer = null }
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
