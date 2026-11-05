<!--私信：左侧会话列表 + 右侧聊天窗（CSDN 消息中心式）-->
<template>
  <div class="main-content">
    <div class="card message-wrap" :class="{'mobile-chat': mobileChat}" v-if="!guest">

      <!-- 左：会话列表 -->
      <div class="conv-list">
        <div class="conv-head">私信</div>
        <div v-for="c in conversations" :key="c.otherId" class="conv-item"
             :class="{active: c.otherId === Number(activeId)}" @click="openConversation(c.otherId)">
          <div style="position: relative; flex-shrink: 0">
            <img class="conv-avatar" :src="c.otherAvatar" alt="">
            <span v-if="c.unreadCount > 0" class="conv-badge">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</span>
          </div>
          <div style="flex: 1; min-width: 0">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="conv-name">{{ c.otherName }}</span>
              <span class="conv-time">{{ c.lastTime ? c.lastTime.substring(5, 16) : '' }}</span>
            </div>
            <div class="conv-last line1">{{ c.lastContent }}</div>
          </div>
        </div>
        <div v-if="!conversations.length" class="conv-empty">
          暂无私信。<br>去别人的个人空间给他们发私信吧~
        </div>
      </div>

      <!-- 右：聊天窗 -->
      <div class="chat-panel">
        <template v-if="activeId">
          <div class="chat-head">
            <!-- 手机端返回会话列表（≤767px 显示） -->
            <span class="mobile-back" @click="mobileChat = false"><i class="el-icon-back"></i></span>
            {{ activeName }}
          </div>

          <!-- 消息气泡列表 -->
          <div class="chat-body" ref="chatBody">
            <div v-for="m in history" :key="m.id"
                 class="msg-row" :class="m.senderId === me.id ? 'mine' : 'other'">
              <div class="msg-bubble" :class="m.senderId === me.id ? 'bubble-mine' : 'bubble-other'">{{ m.content }}</div>
              <div class="msg-time">{{ m.time }}</div>
            </div>
            <div v-if="!history.length" style="text-align: center; color: #bbbbbb; padding: 40px 0">
              还没有消息，打个招呼吧~
            </div>
          </div>

          <!-- 输入区 -->
          <div class="chat-input-area">
            <el-input type="textarea" :rows="2" v-model="inputText" maxlength="500"
                      placeholder="输入私信内容…" @keydown.enter.native="onEnter"></el-input>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px">
              <span style="font-size: 12px; color: #bbbbbb">{{ inputText.length }}/500，按下 Enter 或点击发送</span>
              <el-button type="primary" size="small" round @click="send">发 送</el-button>
            </div>
          </div>
        </template>
        <div v-else class="chat-empty">
          <i class="el-icon-chat-dot-square" style="font-size: 60px; color: #d8c9f5"></i>
          <p style="color: #999999">选择左侧一个会话，开始聊天</p>
        </div>
      </div>
    </div>

    <!-- 游客/管理员引导 -->
    <div v-if="guest" class="card" style="text-align: center; padding: 90px 20px;">
      <i :class="adminMode ? 'el-icon-s-tools' : 'el-icon-chat-dot-square'" style="font-size: 52px; color: #409EFF"></i>
      <p style="font-size: 16px; color: #666666; margin: 18px 0 26px">
        {{ adminMode ? '私信仅供用户账号使用' : '登录后就可以和关注的人私信啦' }}
      </p>
      <el-button v-if="adminMode" type="primary" @click="$router.push('/home')">去后台管理</el-button>
      <el-button v-else type="primary" @click="$router.push('/login')">去登录</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: "Message",
  data() {
    return {
      me: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      guest: false,
      adminMode: false,
      conversations: [],     // 会话列表
      activeId: this.$route.query.userId ? Number(this.$route.query.userId) : null,  // 当前聊天对象
      mobileChat: false,     // 手机端切换态：true=显示聊天窗，false=显示会话列表（≤767px 生效）
      activeName: '',
      historyRaw: [],        // 聊天记录原始数据（接口倒序：最新在前）
      inputText: '',
      pollTimer: null
    }
  },
  computed: {
    // 渲染用：强制按 id 升序排列（时间早的在上、晚的在下，微信逻辑），对接口顺序免疫
    history() {
      return [...this.historyRaw].sort((a, b) => (a.id || 0) - (b.id || 0))
    }
  },
  mounted() {
    if (!this.me.id) {
      this.guest = true
      return
    }
    if (this.me.role !== 'USER') {
      this.guest = true
      this.adminMode = true
      return
    }
    this.loadConversations()
    // URL 指定了私信对象：自动打开该会话
    if (this.activeId) this.openConversation(this.activeId)
    // 监听全局 WebSocket 实时推送（NotificationBell 全局连接）
    this.$bus.$on('ws:new-message', this.onWsMessage)
    this.$bus.$on('ws:connected', this.onWsConnected)
  },
  beforeDestroy() {
    this.$bus.$off('ws:new-message', this.onWsMessage)
    this.$bus.$off('ws:connected', this.onWsConnected)
  },
  methods: {
    // WebSocket 实时推送到达：接收者是当前会话则直接追加气泡，否则仅更新会话列表
    onWsMessage(data) {
      const msg = data.message
      if (!msg) return
      if (msg.senderId === this.activeId) {
        // 正在看这个会话：追加气泡并标记已读
        if (!this.historyRaw.some(m => m.id === msg.id)) {
          this.historyRaw.push({
            id: msg.id, senderId: msg.senderId, receiverId: msg.receiverId,
            content: msg.content, isRead: 0, time: msg.time
          })
          this.$request.put('/message/read/' + this.activeId).then(() => {
            // 已读生效后同步清掉会话列表角标，并让铃铛未读数对齐
            const conv = this.conversations.find(c => c.otherId === this.activeId)
            if (conv) conv.unreadCount = 0
            this.$bus.$emit('msg:read')
          })
          this.$nextTick(() => {
            const body = this.$refs.chatBody
            if (body) body.scrollTop = body.scrollHeight
          })
        }
      }
      this.loadConversations(true)   // 更新会话列表最后一条/角标
    },
    // WebSocket 重连成功：补齐断线期间的消息
    onWsConnected() {
      if (this.activeId) this.loadHistory()
    },
    loadConversations(silent) {
      this.$request.get('/message/conversations').then(res => {
        if (res.code === '200') {
          this.conversations = res.data || []
          // 正在查看的会话必然已读：修正响应竞态导致的角标残留
          const conv = this.conversations.find(c => c.otherId === this.activeId)
          if (conv) conv.unreadCount = 0
        }
      })
    },
    // 从空间页直接私信：对方可能还没有会话记录，取对方昵称显示
    loadOtherInfo(userId) {
      this.$request.get('/space/info/' + userId).then(res => {
        if (res.code === '200' && res.data?.user) {
          const u = res.data.user
          this.activeName = u.name || u.username
        }
      })
    },
    openConversation(otherId) {
      this.activeId = otherId
      this.mobileChat = true   // 手机端：点开会话切换到聊天窗
      const conv = this.conversations.find(c => c.otherId === otherId)
      this.activeName = conv ? conv.otherName : ''
      // 会话列表里还没有对方（首次发私信）时，单独取对方昵称
      if (!conv) this.loadOtherInfo(otherId)
      this.loadHistory()
      // 标记已读并清掉列表角标
      this.$request.put('/message/read/' + otherId).then(() => {
        const conv = this.conversations.find(c => c.otherId === otherId)
        if (conv) conv.unreadCount = 0
      })
    },
    loadHistory() {
      this.$request.get('/message/history/' + this.activeId).then(res => {
        if (res.code === '200') {
          this.historyRaw = res.data || []   // 原始倒序存入，渲染由 computed 强制升序
          this.$nextTick(() => {
            const body = this.$refs.chatBody
            if (body) body.scrollTop = body.scrollHeight      // 定位到最新消息
          })
        }
      })
    },
    onEnter(e) {
      if (!e.shiftKey) {   // Enter 发送，Shift+Enter 换行
        e.preventDefault()
        this.send()
      }
    },
    send() {
      if (!this.activeId) return
      if (!this.inputText.trim()) {
        this.$message.warning('私信内容不能为空')
        return
      }
      this.$request.post('/message/send', {
        receiverId: this.activeId,
        content: this.inputText
      }).then(res => {
        if (res.code === '200') {
          this.inputText = ''
          // 实时消息由 WebSocket 推送到达；此处主动拉取一次确保多端一致
          this.loadHistory()
          this.loadConversations(true)
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>

<style scoped>
.message-wrap {
  display: flex;
  height: calc(100vh - 160px);
  min-height: 500px;
  padding: 0;
  overflow: hidden;
}
.conv-list {
  width: 270px;
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  flex-shrink: 0;
}
.conv-head {
  font-size: 16px;
  font-weight: bold;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.conv-item {
  display: flex;
  grid-gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f7f7f7;
  transition: background .2s;
}
.conv-item:hover, .conv-item.active {
  background: #f7f3ff;
}
.conv-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}
.conv-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  background: #f56c6c;
  color: #ffffff;
  font-size: 11px;
  min-width: 17px;
  height: 17px;
  line-height: 17px;
  border-radius: 9px;
  padding: 0 4px;
  text-align: center;
}
.conv-name {
  font-size: 14px;
  font-weight: bold;
  color: #333333;
}
.conv-time {
  font-size: 11px;
  color: #bbbbbb;
}
.conv-last {
  font-size: 12px;
  color: #999999;
  margin-top: 3px;
}
.conv-empty {
  padding: 40px 20px;
  text-align: center;
  color: #999999;
  font-size: 13px;
  line-height: 2;
}
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-head {
  font-size: 16px;
  font-weight: bold;
  padding: 14px 18px;
  border-bottom: 1px solid #f0f0f0;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
  background: #faf8ff;
}
.msg-row {
  margin-bottom: 14px;
}
.msg-row.mine {
  text-align: right;
}
.msg-bubble {
  display: inline-block;
  max-width: 65%;
  padding: 9px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  text-align: left;
  word-break: break-word;
}
.bubble-other {
  background: #ffffff;
  color: #333333;
  border-top-left-radius: 2px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, .06);
}
.bubble-mine {
  background: linear-gradient(135deg, #b79cff, #a78bfa);
  color: #ffffff;
  border-top-right-radius: 2px;
}
.msg-time {
  font-size: 11px;
  color: #bbbbbb;
  margin-top: 4px;
}
.chat-input-area {
  border-top: 1px solid #f0f0f0;
  padding: 12px 16px;
}
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
