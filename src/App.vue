<template>
  <el-config-provider>
    <router-view v-slot="{ Component, route }">
      <keep-alive>
        <component :is="Component" v-if="route.meta?.keepAlive" />
      </keep-alive>
      <component :is="Component" v-if="!route.meta?.keepAlive" />
    </router-view>
  </el-config-provider>
</template>

<script setup>
/**
 * @cn-file
 * @file src/App.vue
 * @desc 根组件：应用顶层布局与路由出口
 */

</script>
