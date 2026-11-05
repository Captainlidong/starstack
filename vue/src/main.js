import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '@/assets/css/global.css'
import '@/assets/css/theme/index.css'
import '@/assets/css/iconfont/iconfont.css'
import 'highlight.js/styles/monokai-sublime.css'

import request from "@/utils/request";

Vue.config.productionTip = false

Vue.prototype.$request = request
Vue.prototype.$baseUrl = process.env.VUE_APP_BASEURL
Vue.prototype.$bus = new Vue()   // 全局事件总线：WebSocket 私信实时推送跨组件通信

// 列表小图专用：把本站 /files/ 图片 URL 转为懒生成的 WebP 缩略图端点
// （外链 URL 原样返回；详情页/预览场景保持原图，不走此方法）
Vue.prototype.$thumb = function (url) {
  if (!url || typeof url !== 'string') return url
  const i = url.indexOf('/files/')
  if (i === -1) return url
  return url.substring(0, i) + '/files/thumb/' + url.substring(i + 7)
}

Vue.use(ElementUI, {size: "small"})

new Vue({
    router,
    render: h => h(App)
}).$mount('#app')
