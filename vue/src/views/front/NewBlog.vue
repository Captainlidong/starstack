<template>
  <div class="narrow-page" style="width: 50%;margin: 5px auto">
    <div class="card" style="margin-bottom: 100px" v-if="!guest">
      <div style="font-weight: bold;font-size: 24px;margin-bottom: 20px">发表新博客/编辑博客</div>
      <el-alert v-if="form.status==='REJECTED'" type="error" :closable="false" show-icon style="margin-bottom: 20px"
                :title="'该博客未通过审核：' + (form.rejectReason || '内容不符合平台规范')"
                description="请修改内容后重新提交审核">
      </el-alert>
      <el-form :model="form" label-width="100px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item label="简介" prop="descr">
          <el-input type="textarea" v-model="form.descr" placeholder="简介" maxlength="200"></el-input>
        </el-form-item>
        <el-form-item label="封面" prop="cover">
          <el-upload
              :action="$baseUrl + '/files/upload'"
              :headers="{ token: user.token }"
              list-type="picture"
              :on-success="handleCoverSuccess"
          >
            <el-button type="primary">上传封面</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="item in categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-select v-model="tagsArr" multiple filterable allow-create default-first-option style="width: 100%">
            <el-option value="后端"></el-option>
            <el-option value="Java"></el-option>
            <el-option value="面试"></el-option>
            <el-option value="Vue"></el-option>
            <el-option value="前端"></el-option>
            <el-option value="大数据"></el-option>
            <el-option value="算法"></el-option>
            <el-option value="程序员"></el-option>
            <el-option value="小白"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="可见性" prop="visibility">
          <el-radio-group v-model="form.visibility">
            <el-radio label="PUBLIC">公开</el-radio>
            <el-radio label="PRIVATE">私人（仅自己可见）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div id="editor"></div>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button type="primary" style="width: 130px" @click="save('PENDING')">提交审核</el-button>
        <el-button style="width: 130px;margin-left: 20px" @click="save('DRAFT')">存草稿</el-button>
      </div>
      <div style="text-align: center;font-size: 12px;color: #999999;margin-top: 10px">
        提交后进入审核队列，审核通过即可公开显示；草稿会保存在"个人中心-草稿箱"，可随时继续编辑
      </div>
    </div>

    <!-- 游客/管理员引导 -->
    <div v-if="guest" class="card" style="text-align: center; padding: 90px 20px;">
      <i :class="adminMode ? 'el-icon-s-tools' : 'el-icon-edit-outline'" style="font-size: 52px; color: #409EFF"></i>
      <p style="font-size: 16px; color: #666666; margin: 18px 0 26px">
        {{ adminMode ? '发表博客请使用用户账号登录，管理员请在后台管理系统内容' : '登录后就可以发表你的博客啦' }}
      </p>
      <el-button v-if="adminMode" type="primary" @click="$router.push('/home')">去后台管理</el-button>
      <el-button v-else type="primary" @click="$router.push('/login')">去登录</el-button>
      <el-button v-if="!adminMode" @click="$router.push('/register')">去注册</el-button>
    </div>
  </div>
</template>

<script>
import E from "wangeditor";
import hljs from "highlight.js";

export default {
  name: "NewBlog",
  data(){
    return{
      form: { visibility: 'PUBLIC' },
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
      },
      tagsArr:[],
      categoryList:[],
      editor:null,
      blogId:this.$route.query.blogId,
      guest: false,
      adminMode: false
    }
  },
  mounted() {
    if (!this.user || !this.user.id) {   // 游客：展示引导卡片而不是强制跳登录
      this.guest = true
      return
    }
    if (this.user.role !== 'USER') {   // 管理员：引导使用用户账号
      this.guest = true
      this.adminMode = true
      return
    }
    this.$request.get('/category/selectAll').then(res=>{
      this.categoryList=res.data || []
    })
    this.$request.get('/blog/selectById/'+this.blogId).then(res=>{
      this.form=res.data || {}
      if (!this.form.visibility) this.$set(this.form, 'visibility', 'PUBLIC')
      if(this.form.id){
        this.tagsArr=JSON.parse(this.form.tags||'[]')
        setTimeout(()=>{
          this.editor.txt.html(this.form.content)
        },0)
      }
    })

    this.setRichText()
  },
  methods:{
    save(status) {   // status: 'PENDING'提交审核 / 'DRAFT'存草稿
      if (!this.user || !this.user.id || this.user.role !== 'USER') {   // 仅用户账号可发表
        this.$message.warning('请使用用户账号发表博客')
        return
      }
      if (status !== 'DRAFT') {   // 发布前的基础校验
        if (!this.form.title || !this.form.title.trim()) {
          this.$message.warning('请输入标题')
          return
        }
        const plain = (this.editor.txt.text() || '').trim()
        if (!plain) {
          this.$message.warning('请输入正文内容')
          return
        }
      }
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.form.tags=JSON.stringify(this.tagsArr)//把json数组转化成json子字符串存到数据库
          this.form.content=this.editor.txt.html()//获取富文本编辑器中的html内容
          this.form.status = status
          this.$request({
            url: this.form.id ? '/blog/update' : '/blog/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // 表示成功保存
              this.$message.success(status === 'DRAFT' ? '已保存到草稿箱' : '已提交审核，审核通过后将公开显示');
              setTimeout(() => {
                this.$router.push('/front/person');
              }, 1000); // 1秒后跳转
            } else {
              this.$message.error(res.msg)  // 弹出错误的信息（如含违禁词）
            }
          })
        }
      })
    },
    handleCoverSuccess(res){
      this.form.cover=res.data
    },
    setRichText() {   /*设置富文本的函数*/
      this.$nextTick(() => {
        this.editor = new E(`#editor`)  /*创建一个富文本*/
        this.editor.highlight = hljs  /*设置代码高亮*/
        this.editor.config.uploadImgServer = this.$baseUrl + '/files/editor/upload'  /*设置图片的上传地址*/
        this.editor.config.uploadFileName = 'file'   /*图片上传的名称*/
        this.editor.config.uploadImgHeaders = {     /*图片上传的token*/
          token: this.user.token
        }
        this.editor.config.uploadImgParams = {
          type: 'img',
        }
        this.editor.config.zIndex = 0  //解决分类标签遮挡
        this.editor.create()  // 创建
      })
    },

  }
}
</script>

<style scoped>

</style>
