<!--内容审核：用户发表的博客需审核通过后才公开-->
<template>
  <div>
    <div class="search">
      <el-radio-group v-model="status" @change="load(1)">
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="PUBLISHED">已通过</el-radio-button>
        <el-radio-button label="REJECTED">已驳回</el-radio-button>
      </el-radio-group>
      <el-input placeholder="请输入标题查询" style="width: 200px;margin-left: 15px" v-model="title"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" strip>
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="descr" label="简介" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userName" label="作者"></el-table-column>
        <el-table-column prop="date" label="提交时间" width="110"></el-table-column>
        <el-table-column label="可见性" width="80">
          <template v-slot="scope">
            <el-tag size="mini" :type="scope.row.visibility === 'PRIVATE' ? 'info' : 'success'">
              {{ scope.row.visibility === 'PRIVATE' ? '私人' : '公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template v-slot="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="驳回原因" show-overflow-tooltip>
          <template v-slot="scope">{{ scope.row.rejectReason || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="250">
          <template v-slot="scope">
            <div class="audit-actions">
              <el-button size="mini" type="primary" plain @click="viewContent(scope.row)">查看内容</el-button>
              <template v-if="scope.row.status === 'PENDING'">
                <el-button size="mini" type="success" plain @click="pass(scope.row)">通过</el-button>
                <el-button size="mini" type="danger" plain @click="reject(scope.row)">驳回</el-button>
              </template>
              <el-button v-else-if="scope.row.status === 'REJECTED'" size="mini" type="success" plain @click="pass(scope.row)">恢复通过</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background @current-change="handleCurrentChange"
                       :current-page="pageNum" :page-size="pageSize"
                       layout="total, prev, pager, next" :total="total"></el-pagination>
      </div>
    </div>

    <!-- 内容预览 -->
    <el-dialog title="博客内容预览" :visible.sync="contentVisible" width="60%">
      <div style="font-weight: bold;font-size: 18px;margin-bottom: 10px">{{ current.title }}</div>
      <div style="color: #999999;margin-bottom: 15px">作者：{{ current.userName }} · {{ current.date }}</div>
      <div class="w-e-text">
        <div v-html="current.content" style="max-height: 50vh;overflow: auto"></div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="contentVisible = false">关 闭</el-button>
        <el-button v-if="current.status === 'PENDING'" type="danger" @click="reject(current)">驳 回</el-button>
        <el-button v-if="current.status === 'PENDING' || current.status === 'REJECTED'" type="success" @click="pass(current)">通 过</el-button>
      </div>
    </el-dialog>

    <!-- 驳回原因 -->
    <el-dialog title="驳回（填写原因，将通知作者修改）" :visible.sync="rejectVisible" width="35%" :close-on-click-modal="false">
      <el-input type="textarea" v-model="rejectReason" :rows="3" placeholder="请填写驳回原因，例如：内容包含广告信息"
                maxlength="200"></el-input>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectVisible = false">取 消</el-button>
        <el-button type="primary" @click="doReject">确认驳回</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "Audit",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      status: 'PENDING',
      title: null,
      contentVisible: false,
      rejectVisible: false,
      rejectReason: '',
      rejectRow: null,
      current: {}
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/blog/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.status,
          title: this.title,
          scope: 'all'
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.title = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    viewContent(row) {
      this.current = row
      this.contentVisible = true
    },
    pass(row) {
      this.$confirm('确定通过该博客的审核并公开显示吗？', '确认', {type: 'warning'}).then(() => {
        this.$request.put('/blog/audit/' + row.id + '/true').then(res => {
          if (res.code === '200') {
            this.$message.success('已通过，作者将收到通知')
            this.contentVisible = false
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    },
    reject(row) {
      this.rejectRow = row
      this.rejectReason = ''
      this.rejectVisible = true
    },
    doReject() {
      this.$request.put('/blog/audit/' + this.rejectRow.id + '/false', { rejectReason: this.rejectReason }).then(res => {
        if (res.code === '200') {
          this.$message.success('已驳回，作者将收到通知')
          this.rejectVisible = false
          this.contentVisible = false
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    statusText(s) {
      const map = { DRAFT: '草稿', PENDING: '待审核', PUBLISHED: '已发布', REJECTED: '已驳回' }
      return map[s] || s
    },
    statusTagType(s) {
      const map = { DRAFT: 'info', PENDING: 'warning', PUBLISHED: 'success', REJECTED: 'danger' }
      return map[s] || ''
    }
  }
}
</script>

<style scoped>
/* 操作列按钮：固定一行居中排列，避免换行错位 */
.audit-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
}
.audit-actions /deep/ .el-button {
  margin-left: 0;
  margin-right: 8px;
}
.audit-actions /deep/ .el-button:last-child {
  margin-right: 0;
}
</style>
