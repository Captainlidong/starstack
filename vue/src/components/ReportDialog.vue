<template>
  <el-dialog title="举报" :visible="visible" width="40%" :close-on-click-modal="false" @close="handleClose"
             :append-to-body="true" custom-class="pink-dialog">
    <el-form :model="form" label-width="80px">
      <el-form-item label="举报对象">
        <span style="color: #666666">{{ targetType === 'BLOG' ? '博客' : '活动' }}：{{ targetTitle }}</span>
      </el-form-item>
      <el-form-item label="举报理由">
        <el-radio-group v-model="form.reason">
          <el-radio v-for="r in reasons" :key="r" :label="r" style="display: block;margin-bottom: 10px">{{ r }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="补充说明">
        <el-input type="textarea" v-model="form.detail" placeholder="补充说明问题细节（选填）" maxlength="200"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="danger" @click="submit">提 交 举 报</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "ReportDialog",
  props: {
    visible: Boolean,
    targetType: String,   // BLOG / ACTIVITY
    targetId: null,
    targetTitle: String
  },
  data() {
    return {
      form: { reason: '', detail: '' },
      reasons: ['垃圾广告', '违法违规', '辱骂攻击', '泄露隐私', '侵权抄袭', '其他']
    }
  },
  computed: {
    visibleInner: {
      get() { return this.visible },
      set(v) { this.$emit('update:visible', v) }
    }
  },  methods: {
    submit() {
      if (!this.form.reason) {
        this.$message.warning('请选择举报理由')
        return
      }
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      if (!user.id) {
        this.$message.warning('请先登录后再举报')
        this.$router.push('/login')
        return
      }
      const reason = this.form.detail ? (this.form.reason + '：' + this.form.detail) : this.form.reason
      this.$request.post('/report/add', {
        targetType: this.targetType,
        targetId: this.targetId,
        reason: reason
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('举报已提交，我们会尽快核实处理')
          this.visibleInner = false
          this.form = { reason: '', detail: '' }
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleClose() {
      this.$emit('update:visible', false)
    }
  }
}
</script>
