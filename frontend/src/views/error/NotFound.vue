<template>
  <div class="not-found-container">
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="content">
      <div class="error-code">
        <span class="digit">4</span>
        <span class="digit zero">0</span>
        <span class="digit">4</span>
      </div>
      <h2 class="title">页面未找到</h2>
      <p class="description">抱歉，您访问的页面不存在或已被删除</p>
      <div class="actions">
        <el-button type="primary" size="large" round @click="goHome">
          返回首页
        </el-button>
        <el-button size="large" round @click="goBack">
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/home')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg-page;
  position: relative;
  overflow: hidden;
}

.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.5;
  }

  .shape-1 {
    width: 400px;
    height: 400px;
    top: -100px;
    right: -100px;
    background: linear-gradient(135deg, rgba($primary-200, 0.3), rgba($primary-100, 0.1));
    animation: float 8s ease-in-out infinite;
  }

  .shape-2 {
    width: 300px;
    height: 300px;
    bottom: -80px;
    left: -80px;
    background: linear-gradient(135deg, rgba(#c4b5fd, 0.3), rgba(#ddd6fe, 0.1));
    animation: float 10s ease-in-out infinite 2s;
  }

  .shape-3 {
    width: 200px;
    height: 200px;
    top: 40%;
    left: 15%;
    background: linear-gradient(135deg, rgba(#a7f3d0, 0.3), rgba(#d1fae5, 0.1));
    animation: float 6s ease-in-out infinite 1s;
  }
}

.content {
  text-align: center;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

.error-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  margin-bottom: $spacing-6;

  .digit {
    font-family: $font-family-display;
    font-size: 120px;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: 1;
    letter-spacing: -0.04em;

    @include respond-to(md) {
      font-size: 80px;
    }
  }

  .zero {
    color: $color-primary;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      inset: 15%;
      border-radius: 50%;
      border: 3px solid $primary-200;
      animation: pulse-soft 3s ease-in-out infinite;
    }
  }
}

.title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin: 0 0 $spacing-3;
}

.description {
  font-size: $font-size-md;
  color: $color-text-secondary;
  margin: 0 0 $spacing-8;
}

.actions {
  display: flex;
  justify-content: center;
  gap: $spacing-4;
}
</style>
