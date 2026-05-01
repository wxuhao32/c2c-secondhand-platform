<template>
  <div class="comment-item">
    <div class="comment-header">
      <el-avatar :size="36" :src="comment.avatar" class="commenter-avatar">
        {{ comment.username?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="commenter-info">
        <span class="commenter-name">{{ comment.username }}</span>
        <div class="comment-meta">
          <el-rate v-model="comment.rating" disabled :size="12" class="comment-rating" />
          <span class="comment-time">{{ comment.time }}</span>
        </div>
      </div>
    </div>
    <div class="comment-body">
      <p class="comment-content">{{ comment.content }}</p>
      <div v-if="comment.images?.length" class="comment-images">
        <img v-for="(img, i) in comment.images" :key="i" :src="img" class="comment-img" />
      </div>
    </div>
    <div class="comment-footer">
      <button class="action-btn" @click="$emit('like', comment)">
        <el-icon :size="14"><StarFilled /></el-icon>
        <span>{{ comment.likes || 0 }}</span>
      </button>
      <button class="action-btn" @click="$emit('reply', comment)">
        <el-icon :size="14"><ChatDotRound /></el-icon>
        <span>回复</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { StarFilled, ChatDotRound } from '@element-plus/icons-vue'

defineProps({
  comment: { type: Object, required: true }
})

defineEmits(['like', 'reply'])
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.comment-item {
  padding: $spacing-5 0;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.comment-header {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-3;
}

.commenter-avatar {
  flex-shrink: 0;
  border: 2px solid $color-border-light;
}

.commenter-info {
  flex: 1;
}

.commenter-name {
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  margin-top: 2px;
}

.comment-rating {
  :deep(.el-rate__icon) {
    font-size: 12px !important;
    margin-right: 1px;
  }
}

.comment-time {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

.comment-body {
  margin-left: 48px;
}

.comment-content {
  font-size: $font-size-md;
  color: $color-text-regular;
  line-height: $line-height-relaxed;
  margin: 0 0 $spacing-3;
}

.comment-images {
  display: flex;
  gap: $spacing-2;
  flex-wrap: wrap;
  margin-bottom: $spacing-3;
}

.comment-img {
  width: 80px;
  height: 80px;
  border-radius: $radius-md;
  object-fit: cover;
  border: 1px solid $color-border-light;
}

.comment-footer {
  display: flex;
  gap: $spacing-4;
  margin-left: 48px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-1 $spacing-2;
  border: none;
  background: transparent;
  color: $color-text-placeholder;
  font-size: $font-size-sm;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $neutral-50;
    color: $color-primary;
  }
}
</style>
