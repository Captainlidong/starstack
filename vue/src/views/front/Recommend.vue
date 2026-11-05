<template>
  <div class="main-content" style="max-width: 1000px">
  <div class="card" style="min-height: 80vh;display: flex;flex-direction: column">

    <div v-if="loading" style="text-align: center; padding: 30px;">
      <el-spinner></el-spinner>
    </div>

    <!-- 游客引导 -->
    <div v-if="guest" style="text-align: center; padding: 90px 20px;">
      <i class="el-icon-user-solid" style="font-size: 52px; color: #409EFF"></i>
      <p style="font-size: 16px; color: #666666; margin: 18px 0 26px">登录后可根据你的浏览、点赞和收藏，生成专属个性化推荐</p>
      <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
      <el-button @click="$router.push('/register')">去注册</el-button>
    </div>

    <!-- 博客列表 -->
    <div v-for="item in tableData" :key="item.id" class="blog-box" v-show="!loading && !guest">
      <div style="flex: 1;width: 0">
        <!-- 标题 -->
        <a :href="'/front/blogDetail?blogId=' + item.id" target="_blank">
          <div class="line1 blog-title">{{ item.title }}</div>
        </a>
        <!-- 简介 -->
        <div class="line1" style="font-size: 13px;margin-top: 6px;margin-bottom: 10px;color: #515151;margin-right: 5px;line-height: 1.6">
          {{ item.descr }}
        </div>
        <!-- 用户信息 -->
        <div style="display: flex;align-items: center;margin-top: 4px">
          <div style="flex:1;font-size: 13px">
            <span style="color: #666666;margin-right: 10px">
              <i class="el-icon-user" style="margin-right: 3px"></i>{{ item.userName }}
            </span>
            <span style="color: #666666;margin-right: 10px">
              <i class="el-icon-eye" style="margin-right: 3px"></i>{{ item.readCount }}
            </span>
            <span style="color: #666666;margin-right: 10px">
              <i class="el-icon-dianzan" style="margin-right: 3px"></i>{{ item.likesCount }}
            </span>
          </div>
          <!-- 标签 -->
          <div style="width: fit-content">
            <el-tag v-for="tag in JSON.parse(item.tags || '[]')" :key="tag" type="primary" style="margin-right:5px">
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 封面图 -->
      <div class="blog-cover" style="width: 150px">
        <img :src="item.cover" alt="" style="width: 100%; height: 80px; border-radius: 5px;" />
      </div>

    </div>

    <!-- 暂无数据 -->
    <div v-if="total === 0 && !loading && !guest" style="padding: 20px; text-align: center; font-size: 16px; color: #666666">
      暂无推荐内容。
    </div>

    <!-- 分页（列表较短时固定在卡片底部左侧） -->
    <div v-if="total > 0" style="margin-top: auto;padding-top: 10px">
      <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="total">
      </el-pagination>
    </div>

    </div>
  </div>
</template>

<script>
export default {
  name: "Recommend",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      userId: null,
      guest: false
    };
  },
  created() {
    this.loadUserIdFromToken();
  },
  methods: {
    // 解析 token 获取用户ID
    loadUserIdFromToken() {
      const user = localStorage.getItem("xm-user"); // 登录后存储的用户对象
      if (!user) {
        this.guest = true;   // 游客：展示引导卡片，不再强制跳登录
        return;
      }

      try {
        const account = JSON.parse(user);
        this.userId = account.id;

        if (!this.userId) {
          throw new Error("token中没有用户ID");
        }

        this.loadRecommendations(this.pageNum);
      } catch (e) {
        console.error("token解析失败", e);
        this.guest = true;   // 登录态异常也按游客引导处理
      }
    },
    // 添加 parseJwt 方法
    parseJwt(token) {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '_').replace(/_/g, '/');
      return JSON.parse(atob(base64));
    },

    // 加载推荐数据
    loadRecommendations(pageNumber) {
      this.loading = true;
      this.pageNum = pageNumber;

      this.$request.get(`/recommend/user/${this.userId}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      }).then(res => {
        this.tableData = res.data?.list || [];
        this.total = res.data?.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.$message.error('加载推荐失败');
      });
    },

    // 分页切换
    handleCurrentChange(pageNum) {
      this.loadRecommendations(pageNum);
    }
  }
};
</script>

<style scoped>
.blog-box {
  display: flex;
  grid-gap: 15px;
  padding: 16px 6px;
  border-bottom: 1px solid #dddddd;
}

.blog-box:first-child {
  padding-top: 6px;
}

.blog-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  line-height: 1.5;
  cursor: pointer;
}

.blog-title:hover {
  color: #2a60c9;
}
</style>
