<!--数据仪表盘：全站核心数据可视化（登录后台默认页）-->
<template>
  <div>
    <!-- 欢迎横幅 -->
    <div class="card dash-banner">
      <div>
        <div style="font-size: 20px; font-weight: bold">您好，{{ user?.name || user?.username }}！欢迎使用 星栈始动</div>
        <div style="color: #909399; font-size: 13px; margin-top: 6px">今天是 {{ today }}，系统运行数据一目了然</div>
      </div>
      <i class="el-icon-data-line banner-icon"></i>
    </div>

    <!-- 统计卡片 8 个 -->
    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <div class="card stat-card" :class="{'stat-clickable': card.link}" @click="card.link && $router.push(card.link)">
          <div class="stat-icon" :style="{ background: card.color }">
            <i :class="card.icon"></i>
          </div>
          <div class="stat-body">
            <div class="stat-number">
              {{ card.value }}
              <span v-if="card.extra" class="stat-extra">{{ card.extra }}</span>
            </div>
            <div class="stat-title">
              {{ card.title }}
              <i v-if="card.link" class="el-icon-arrow-right"></i>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 2×2 -->
    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :span="12">
        <div class="card chart-card">
          <div class="chart-title">博客分类分布</div>
          <div ref="chartCategory" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card chart-card">
          <div class="chart-title">近 7 天发文与用户增长趋势</div>
          <div ref="chartTrend" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card chart-card">
          <div class="chart-title">博客状态分布</div>
          <div ref="chartStatus" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card chart-card">
          <div class="chart-title">热门博客 Top10（按浏览量）</div>
          <div ref="chartHot" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 底部：待办 + 公告 -->
    <el-row :gutter="10" style="margin-top: 10px">
      <el-col :span="8">
        <div class="card todo-card">
          <div class="chart-title">最新待审核博客</div>
          <div v-for="b in latestPendingBlogs" :key="b.id" class="todo-item" @click="$router.push('/audit')">
            <span class="todo-text line1">{{ b.title }}</span>
            <span class="todo-meta">{{ b.userName }} · {{ b.date }}</span>
          </div>
          <div v-if="!latestPendingBlogs.length" class="todo-empty">🎉 暂无待审核内容</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card todo-card">
          <div class="chart-title">最新待处理举报</div>
          <div v-for="r in latestPendingReports" :key="r.id" class="todo-item" @click="$router.push('/report')">
            <span class="todo-text line1">【{{ r.targetType === 'BLOG' ? '博客' : '活动' }}】{{ r.targetTitle }}</span>
            <span class="todo-meta">{{ r.reporterName }} · {{ r.time }}</span>
          </div>
          <div v-if="!latestPendingReports.length" class="todo-empty">🎉 暂无待处理举报</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card todo-card">
          <div class="chart-title">系统公告</div>
          <el-timeline reverse>
            <el-timeline-item v-for="item in notices" :key="item.id" :timestamp="item.time">
              <el-popover placement="right" width="200" trigger="hover" :content="item.content">
                <span slot="reference">{{ item.title }}</span>
              </el-popover>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'Home',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      today: new Date().toLocaleDateString('zh-CN'),
      notices: [],
      stats: {},
      latestPendingBlogs: [],
      latestPendingReports: [],
      charts: []   // 已初始化的 echarts 实例，用于 resize
    }
  },
  computed: {
    statCards() {
      const s = this.stats
      return [
        { title: '用户总数', value: s.totalUsers ?? 0, extra: s.newUsersToday ? `今日 +${s.newUsersToday}` : '', icon: 'el-icon-user-solid', color: 'linear-gradient(135deg,#b79cff,#8b5cf6)' },
        { title: '博客总数', value: s.totalBlogs ?? 0, icon: 'el-icon-document', color: 'linear-gradient(135deg,#6ee7b7,#10b981)' },
        { title: '总浏览量', value: s.totalReads ?? 0, icon: 'el-icon-view', color: 'linear-gradient(135deg,#93c5fd,#3b82f6)' },
        { title: '评论总数', value: s.totalComments ?? 0, icon: 'el-icon-chat-dot-square', color: 'linear-gradient(135deg,#fcd34d,#f59e0b)' },
        { title: '活动总数', value: s.totalActivities ?? 0, icon: 'el-icon-flag', color: 'linear-gradient(135deg,#f9a8d4,#ec4899)' },
        { title: '报名总数', value: s.totalSigns ?? 0, icon: 'el-icon-finished', color: 'linear-gradient(135deg,#5eead4,#14b8a6)' },
        { title: '待审核博客', value: s.pendingBlogs ?? 0, link: '/audit', icon: 'el-icon-s-check', color: 'linear-gradient(135deg,#fca5a5,#ef4444)' },
        { title: '待处理举报', value: s.pendingReports ?? 0, link: '/report', icon: 'el-icon-warning-outline', color: 'linear-gradient(135deg,#fdba74,#f97316)' }
      ]
    }
  },
  created() {
    this.loadData()
  },
  mounted() {
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
  },
  methods: {
    loadData() {
      this.$request.get('/dashboard/stats').then(res => {
        if (res.code !== '200') {
          this.$message.error(res.msg)
          return
        }
        this.stats = res.data || {}
        this.latestPendingBlogs = res.data?.latestPendingBlogs || []
        this.latestPendingReports = res.data?.latestPendingReports || []
        // 数据就绪后渲染图表（等 DOM 数字渲染完，容器尺寸稳定）
        this.$nextTick(() => this.initCharts())
      })

      this.$request.get('/notice/selectAll').then(res => {
        this.notices = res.data || []
      })
    },
    initCharts() {
      const s = this.stats
      const make = (ref, option) => {
        const el = this.$refs[ref]
        if (!el) return
        const chart = echarts.init(el)
        chart.setOption(option)
        this.charts.push(chart)
      }

      // 1. 分类博客分布（环形图）
      make('chartCategory', {
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, type: 'scroll' },
        series: [{
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '44%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: s.categoryDistribution || []
        }]
      })

      // 2. 近 7 天发文与用户增长（双折线）
      const trendX = (s.blogTrend || []).map(i => i.name)
      make('chartTrend', {
        tooltip: { trigger: 'axis' },
        legend: { data: ['发文量', '新增用户'], bottom: 0 },
        grid: { left: 40, right: 20, top: 30, bottom: 50 },
        xAxis: { type: 'category', data: trendX },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          { name: '发文量', type: 'line', smooth: true, data: (s.blogTrend || []).map(i => i.value), itemStyle: { color: '#8b5cf6' }, areaStyle: { opacity: 0.15 } },
          { name: '新增用户', type: 'line', smooth: true, data: (s.userTrend || []).map(i => i.value), itemStyle: { color: '#10b981' }, areaStyle: { opacity: 0.15 } }
        ]
      })

      // 3. 博客状态分布（饼图）
      make('chartStatus', {
        tooltip: { trigger: 'item', formatter: '{b}：{c} 篇（{d}%）' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie',
          radius: '62%',
          center: ['50%', '44%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          data: s.statusDistribution || []
        }]
      })

      // 4. 热门博客 Top10（横向条形图）
      const hot = (s.hotBlogs || []).slice().reverse()   // 条形图自下而上，反转让最高在顶部
      make('chartHot', {
        tooltip: { trigger: 'axis', formatter: p => {
          const d = p[0]
          return (d.data && d.data.fullName) || d.name + '：' + d.value + ' 次浏览'
        } },
        grid: { left: 8, right: 30, top: 10, bottom: 10, containLabel: true },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: hot.map(i => i.name), axisLabel: {
          formatter: v => (hot.find(i => i.name === v) ? (hot.find(i => i.name === v).short || v) : v),
          width: 110, overflow: 'truncate'
        } },
        series: [{
          type: 'bar',
          barWidth: 12,
          itemStyle: { borderRadius: [0, 6, 6, 0], color: '#8b5cf6' },
          data: hot.map(i => ({ value: i.value, fullName: i.name }))
        }]
      })

      this.resizeCharts()
    },
    resizeCharts() {
      this.charts.forEach(c => c.resize())
    }
  }
}
</script>

<style scoped>
.dash-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 25px;
}
.banner-icon {
  font-size: 46px;
  color: #8b5cf6;
  opacity: .5;
}
.stat-card {
  display: flex;
  align-items: center;
  grid-gap: 12px;
  padding: 18px 20px;
  margin-bottom: 10px;
}
.stat-clickable {
  cursor: pointer;
  transition: all .2s;
}
.stat-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, .1);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-body {
  flex: 1;
  min-width: 0;
}
.stat-number {
  font-size: 22px;
  font-weight: bold;
  color: #333333;
  line-height: 1.2;
}
.stat-extra {
  font-size: 12px;
  color: #10b981;
  font-weight: normal;
  margin-left: 4px;
}
.stat-title {
  font-size: 13px;
  color: #909399;
  margin-top: 3px;
}
.chart-card {
  margin-bottom: 10px;
}
.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}
.chart-box {
  height: 280px;
}
.todo-card {
  height: 100%;
  margin-bottom: 10px;
}
.todo-item {
  display: flex;
  flex-direction: column;
  padding: 8px 4px;
  border-bottom: 1px dashed #eeeeee;
  cursor: pointer;
  border-radius: 6px;
  transition: background .2s;
}
.todo-item:hover {
  background: #f7f3ff;
}
.todo-text {
  font-size: 13px;
  color: #333333;
  margin-bottom: 3px;
}
.todo-meta {
  font-size: 12px;
  color: #bbbbbb;
}
.todo-empty {
  text-align: center;
  color: #999999;
  padding: 25px 0;
  font-size: 13px;
}
.line1 {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
