<template>
  <header class="page-header">
    <div class="header-inner">
      <div class="header-left">
        <el-button v-if="showBack" text class="back-btn" @click="handleBack">
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

const props = defineProps({
  title: { type: String, required: true },
  showBack: { type: Boolean, default: true }
})

const emit = defineEmits(['back'])

const router = useRouter()

const handleBack = () => {
  emit('back')
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/home')
  }
}
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
