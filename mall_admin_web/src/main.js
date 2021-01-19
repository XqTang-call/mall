import Vue from 'vue'
import router from './router'
import store from './store'
import './plugins/element.js'
import App from './App'
// 导入全局样式表
// import './assets/css/global.css'

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
