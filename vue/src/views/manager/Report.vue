<!--举报处理：查看用户举报的博客/活动，核实后选择"属实下架"或"无问题驳回"-->
<template>
  <div>
    <div class="search">
      <el-radio-group v-model="status" @change="load(1)">
        <el-radio-button label="PENDING">待处理</el-radio-button>
        <el-radio-button label="RESOLVED">举报属实</el-radio-button>
        <el-radio-button label="DISMISSED">无问题</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table">
      <el-table :data="tableData" strip>
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="reporterName" label="举报人" width="100"></el-table-column>
        <el-table-column label="类型" width="80">
          <template v-slot="scope">
            <el-tag size="mini">{{ scope.row.targetType === 'BLOG' ? '博客' : '活动' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetTitle" label="被举报内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="reason" label="举报理由" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="举报时间" width="160"></el-table-column>
        <el-table-column label="状态" width="90">
          <template v-slot="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理结果" show-overflow-tooltip>
          <template v-slot="scope">
            <span v-if="scope.row.status === 'PENDING'">-</span>
            <span v-else>{{ scope.row.result }}（{{ scope.row.adminName }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template v-slot="scope">
            <el-button v-if="scope.row.targetType === 'BLOG'" size="mini" type="primary" plain
                       @click="$router.push('/front/blogDetail?blogId=' + scope.row.targetId)">查看内容</el-button>
            <el-button v-if="scope.row.targetType === 'ACTIVITY'" size="mini" type="primary" plain
                       @click="$router.push('/front/activityDetail?activityId=' + scope.row.targetId)">查看内容</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="warning" plain @click="handle(scope.row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background @current-change="handleCurrentChange"
                       :current-page="pageNum" :page-size="pageSize"
                       layout="total, prev, pager, next" :total="total"></el-pagination>
      </div>
    </div>

    <!-- 处理弹窗 -->
    <el-dialog :title="'处理举报：' + (current.targetTitle || '')" :visible.sync="handleVisible" width="40%" :close-on-click-modal="false">
      <div style="margin-bottom: 15px;color: #666666">举报理由：{{ current.reason }}</div>
      <el-input type="textarea" v-model="result" :rows="3" placeholder="填写处理说明（将随通知发送给举报人和作者）" maxlength="200"></el-input>
      <div style="margin-top: 15px;text-align: center">
        <el-button type="danger" @click="doHandle('RESOLVED')">举报属实，下架内容</el-button>
        <el-button type="success" @click="doHandle('DISMISSED')">内容无问题，驳回举报</el-button>
      </div>
      <div style="margin-top: 10px;font-size: 12px;color: #999999;text-align: center">
        "属实下架"：博客将被驳回下架，同时通知作者与举报人；"无问题"：通知举报人核实结果
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "Report",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      status: 'PENDING',
      handleVisible: false,
      current: {},
      result: ''
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/report/selectPage', {
        params: { pageNum: this.pageNum, pageSize: this.pageSize, status: this.status }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handle(row) {
      this.current = row
      this.result = ''
      this.handleVisible = true
    },
    doHandle(status) {
      if (!this.result.trim()) {
        this.$message.warning('请填写处理说明')
        return
      }
      this.$request.put('/report/handle', {
        id: this.current.id,
        status: status,
        result: this.result
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('处理完成，相关用户将收到通知')
          this.handleVisible = false
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    statusText(s) {
      const map = { PENDING: '待处理', RESOLVED: '举报属实', DISMISSED: '无问题' }
      return map[s] || s
    },
    statusTagType(s) {
      const map = { PENDING: 'warning', RESOLVED: 'danger', DISMISSED: 'success' }
      return map[s] || ''
    }
  }
}
</script>

<style scoped>

</style>
