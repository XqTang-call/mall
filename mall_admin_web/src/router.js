import Vue from 'vue'
import Router from 'vue-router'
import Login from './components/Login'

Vue.use(Router)

export default new Router({
  routes: [
    // 当输入的是 / 则重定向到 login，login 则又跳转到 Login 页面
    { path: '/', redirect: 'login' },
    // 基本的路由规则：当用户访问路径为 "/login"时，则跳转到 Login 页面
    { path: '/login', component: Login }
  ]
})
