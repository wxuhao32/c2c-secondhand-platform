<template>
  <header class="page-header">
    <div class="header-inner">
      <div class="header-left">
        <el-button text class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">{{ title }}</h1>
      </div>
      <div class="header-right">
        <slot name="actions" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, required: true }
})

const router = useRouter()
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.page-header {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  @include glass-effect(0.95);
  border-bottom: 1px solid $color-border-light;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $spacing-5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.back-btn {
  padding: $spacing-2;
  color: $color-text-secondary;

  &:hover { color: $color-primary; }
}

.page-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}
</style>
