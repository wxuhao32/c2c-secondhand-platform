<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="route.meta.transition || 'fade-up'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
