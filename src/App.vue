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
</script>
