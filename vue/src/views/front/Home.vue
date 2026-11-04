<template>
  <div class="main-content">

    <!--横向分类导航条：单行滚动 + 左右箭头 + 展开/收起-->
    <div class="card category-bar">
      <div class="category-wrap">
        <div class="category-scroll" :class="{'category-scroll-expanded': expanded}" ref="scrollBox" @scroll="updateArrow">
          <div class="category-chip" :class="{'category-chip-active': item.name===current}" v-for="item in categoryList"
               :key="item.id" @click="selectCategory(item.name)"> {{ item.name }}
          </div>
        </div>
        <div class="category-fade left" v-show="!expanded && canLeft"></div>
        <div class="category-fade right" v-show="!expanded && canRight"></div>
        <div class="category-arrow left" v-show="!expanded && canLeft" @click="scrollCate(-1)"><i class="el-icon-arrow-left"></i></div>
        <div class="category-arrow right" v-show="!expanded && canRight" @click="scrollCate(1)"><i class="el-icon-arrow-right"></i></div>
      </div>
      <div class="category-toggle" v-show="overflow || expanded" @click="toggleExpand">
        {{ expanded ? '收起' : '展开' }}<i :class="expanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
      </div>
    </div>

    <div style="display: flex;align-items: flex-start;grid-gap: 10px">

      <!--中间博客展示模块-->
      <div style="flex: 1">

        <BlogList :categoryName="current"/>

        <Footer/>

      </div>

      <!--右边-->
      <div style="width: 260px">
        <div class="card" style="margin-bottom: 10px">
          <div style="font-size: 20px;font-weight: bold;margin-bottom: 10px">
            欢迎您！😊
          </div>
          <a href="/front/person">
            <div style="color: #666666">写下博客，记录美好的一天！</div>
          </a>
        </div>

        <!--榜单模块-->
        <div class="card" style="margin-bottom: 10px">
          <div style="display: flex;align-items:center;padding-bottom: 10px;border-bottom: 1px solid #dddddd">
            <div style="font-size: 20px;flex: 1">IT技术文章热榜</div>
            <div style="font-size: 12px;color: #666666;cursor: pointer;" @click="refreshTop"><i
                class="el-icon-refresh"></i>换一换
            </div>
          </div>

          <div>
            <div v-for="item in showList" :key="item.id" style="margin: 15px 0" class="line1">
              <a :href="'/front/blogDetail?blogId='+item.id" target="_blank">
              <span style="width: 18px;display:inline-block;text-align: right;margin-right: 10px">
                    <span style="color: orangered" v-if="item.index===1">{{ item.index }}</span>
                    <span style="color: goldenrod" v-else-if="item.index===2">{{ item.index }}</span>
                    <span style="color: dodgerblue" v-else-if="item.index===3">{{ item.index }}</span>
                    <span style="color: #87724d" v-else>{{ item.index }}</span>
              </span>

                <span style="color: #666666">{{ item.title }}</span>
              </a>
            </div>

          </div>

        </div>

        <div style="margin-bottom: 10px">
          <div v-for="item in topActivityList" :key="item.id" style="margin-bottom: 10px">
            <a :href="'/front/activityDetail?activityId='+item.id" target="_blank">
              <img :src="item.cover" alt="" style="width:100%;border-radius: 5px">
            </a>
          </div>
        </div>

        <div style="line-height: 30px; color: #666; padding: 0 10px">
          <div> 举报邮箱： feedback@outlook.io</div>
          <div> 座机电话： M78%$%@%%$%$*&</div>
          <div> 宇宙警ICP备888888©2025 Captain_dong</div>
        </div>

      </div>

    </div>
  </div>
</template>

<script>

import Footer from "@/components/Footer.vue";
import BlogList from "@/components/BlogList.vue";

export default {
  components: {
    BlogList,
    Footer
  },

  data() {
    return {
      current: '全部博客',
      categoryList: [],
      topList: [],
      showList: [],
      lastIndex: 0,
      topActivityList: [],
      expanded: false,   // 分类条是否展开为多行
      canLeft: false,    // 分类条可向左滚动（显示左箭头）
      canRight: false,   // 分类条可向右滚动（显示右箭头）
      overflow: false    // 分类是否超出一行（控制展开按钮显隐）
    }
  },
  mounted() {
    this.load()

    this.refreshTop()

    this.loadTopActivity()

    window.addEventListener('resize', this.updateArrow)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateArrow)
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadTopActivity() {
      this.$request.get('/activity/selectTop').then(res => {
        this.topActivityList = res.data || []
      })
    },
    refreshTop() {
      this.$request.get('/blog/selectTop').then(res => {
        this.topList = res.data || []
        this.topList = res.data || []
        let i = 1
        this.topList.forEach(item => item.index = i++)

        if (this.lastIndex === 20) {
          this.lastIndex = 0;
        }
        this.showList = this.topList.slice(this.lastIndex, this.lastIndex + 5)
        this.lastIndex += 5;
      })
    },
    //切换分类
    selectCategory(categoryName) {
      this.current = categoryName
    },
    // 根据滚动位置刷新左右箭头与展开按钮的显示
    updateArrow() {
      const box = this.$refs.scrollBox
      if (!box) return
      this.overflow = box.scrollWidth > box.clientWidth + 2
      this.canLeft = box.scrollLeft > 2
      this.canRight = box.scrollLeft + box.clientWidth < box.scrollWidth - 2
    },
    // 点击箭头滚动分类行
    scrollCate(dir) {
      const box = this.$refs.scrollBox
      if (box) box.scrollBy({ left: dir * 320, behavior: 'smooth' })
    },
    // 展开/收起分类行；收起时把选中的分类滚动回可视区
    toggleExpand() {
      this.expanded = !this.expanded
      this.$nextTick(() => {
        const box = this.$refs.scrollBox
        if (box && !this.expanded) {
          const active = box.querySelector('.category-chip-active')
          if (active) box.scrollTo({ left: active.offsetLeft - box.clientWidth / 2, behavior: 'smooth' })
        }
        this.updateArrow()
      })
    },
    load() {
      //请求分类的数据
      this.$request.get('/category/selectAll').then(res => {
        this.categoryList = res.data || []
        this.categoryList.unshift({name: '全部博客', id: 0})
        this.$nextTick(this.updateArrow)
      })
    },

  }
}
</script>
<style>
/* 分类胶囊样式已统一移至 front.css（.main-body .category-bar / .category-chip） */
</style>