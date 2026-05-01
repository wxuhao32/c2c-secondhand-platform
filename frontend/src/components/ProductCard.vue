<template>
  <div class="product-card" @click="$emit('click', product)">
    <div class="product-image">
      <img v-if="product.image" :src="product.image" :alt="product.title" />
      <div v-else class="placeholder-image">
        <el-icon :size="28"><Goods /></el-icon>
      </div>
      <div v-if="product.condition" class="image-badge">{{ product.condition }}</div>
      <button class="fav-btn" :class="{ 'is-fav': product.isFavorite }" @click.stop="$emit('toggle-fav', product)">
        <el-icon><Star /></el-icon>
      </button>
    </div>
    <div class="product-info">
      <h3 class="product-title">{{ product.title }}</h3>
      <div v-if="product.tags?.length" class="product-tags">
        <span v-for="tag in product.tags" :key="tag" class="tag" :class="`tag-${tag.type}`">{{ tag.text }}</span>
      </div>
      <div class="product-price">
        <span class="price-symbol">¥</span>
        <span class="price-value">{{ product.price }}</span>
        <span v-if="product.originalPrice" class="price-original">¥{{ product.originalPrice }}</span>
      </div>
      <div class="product-meta">
        <span v-if="product.location" class="meta-location">
          <el-icon :size="12"><Location /></el-icon>
          {{ product.location }}
        </span>
        <span v-if="product.time" class="meta-time">{{ product.time }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Goods, Star, Location } from '@element-plus/icons-vue'

defineProps({
  product: { type: Object, required: true }
})

defineEmits(['click', 'toggle-fav'])
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.product-card {
  @include card-base;
  overflow: hidden;
  cursor: pointer;

  &:hover {
    box-shadow: $shadow-xl;
    transform: translateY(-4px);
    border-color: transparent;
  }
}

.product-image {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  background: $neutral-100;

  .placeholder-image {
    width: 100%;
    height: 100%;
    @include flex-center;
    flex-direction: column;
    gap: $spacing-2;
    background: linear-gradient(135deg, $neutral-50, $neutral-100);
    color: $neutral-400;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  }

  .image-badge {
    position: absolute;
    top: $spacing-3;
    left: $spacing-3;
    @include badge-tag;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(8px);
    color: $color-primary;
    font-weight: $font-weight-semibold;
  }

  .fav-btn {
    position: absolute;
    top: $spacing-3;
    right: $spacing-3;
    @include flex-center;
    width: 32px;
    height: 32px;
    border-radius: $radius-full;
    border: none;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(8px);
    color: $neutral-400;
    opacity: 0;
    transform: scale(0.8);
    transition: all $transition-base;
    cursor: pointer;

    &:hover, &.is-fav {
      color: $danger-500;
      background: #fff;
    }

    &.is-fav {
      opacity: 1;
      transform: scale(1);
    }
  }

  &:hover {
    img { transform: scale(1.05); }
    .fav-btn { opacity: 1; transform: scale(1); }
  }
}

.product-info {
  padding: $spacing-4;
}

.product-title {
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  margin: 0 0 $spacing-2;
  @include text-ellipsis(2);
  line-height: 1.4;
  min-height: 2.8em;
}

.product-tags {
  display: flex;
  gap: $spacing-1;
  margin-bottom: $spacing-2;

  .tag {
    @include badge-tag;
    font-size: 11px;
  }

  .tag-quality { background: $primary-50; color: $primary-600; }
  .tag-fast { background: $success-50; color: $success-600; }
  .tag-hot { background: $danger-50; color: $danger-600; }
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
  margin-bottom: $spacing-2;

  .price-symbol {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $danger-500;
  }

  .price-value {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $danger-500;
    letter-spacing: $letter-spacing-tight;
  }

  .price-original {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    text-decoration: line-through;
    margin-left: $spacing-2;
  }
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: $font-size-xs;
  color: $color-text-placeholder;

  .meta-location {
    display: flex;
    align-items: center;
    gap: 2px;
  }
}
</style>
