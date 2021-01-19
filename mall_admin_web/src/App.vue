<template>
  <div>
    <!--路由占位符: 个人理解：页面先从 main 入口跳转到 app 由于这个路由占位符的存在，
        他的跳转则会符合路由规则，而根据路由规则，会跳转到 login。（那么路由规则中有多个怎么跳转）-->
    <router-view></router-view>
  </div>
</template>

<script>

export default {
}
</script>

<style>
</style>
