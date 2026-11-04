<!--敏感词管理：用户发布内容时自动拦截包含敏感词的文本，增删后即时生效-->
<template>
  <div>
    <div class="operation">
      <el-button type="primary" plain @click="addVisible = true">新增敏感词</el-button>
      <span style="margin-left: 15px;color: #999999;font-size: 13px">
        用户发表博客时，标题/简介/正文中包含以下任一词语将被自动拦截，增删即时生效
      </span>
    </div>

    <div class="table">
      <el-table :data="tableData" strip>
        <el-table-column prop="id" label="序号" width="100" align="center"></el-table-column>
        <el-table-column prop="word" label="敏感词"></el-table-column>
        <el-table-column label="操作" align="center" width="150">
          <template v-slot="scope">
            <el-popconfirm title="确定删除该敏感词吗？" @confirm="del(scope.row.id)">
              <el-button slot="reference" size="mini" type="danger" plain>删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background @current-change="handleCurrentChange"
                       :current-page="pageNum" :page-size="pageSize"
                       layout="total, prev, pager, next" :total="total"></el-pagination>
      </div>
    </div>

    <el-dialog title="新增敏感词" :visible.sync="addVisible" width="30%" :close-on-click-modal="false">
      <el-input v-model="word" placeholder="请输入敏感词" maxlength="50"></el-input>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addVisible = false">取 消</el-button>
        <el-button type="primary" @click="add">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "SensitiveWord",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      addVisible: false,
      word: ''
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/sensitiveWord/selectPage', {
        params: { pageNum: this.pageNum, pageSize: this.pageSize }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    add() {
      if (!this.word.trim()) {
        this.$message.warning('请输入敏感词')
        return
      }
      this.$request.post('/sensitiveWord/add', { word: this.word }).then(res => {
        if (res.code === '200') {
          this.$message.success('添加成功，已即时生效')
          this.word = ''
          this.addVisible = false
          this.load(1)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {
      this.$request.delete('/sensitiveWord/delete/' + id).then(res => {
        if (res.code === '200') {
          this.$message.success('删除成功')
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
