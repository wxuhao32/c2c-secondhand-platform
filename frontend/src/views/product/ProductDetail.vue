<template>
  <div class="product-detail-container">
    <header class="detail-header">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </header>

    <main class="main-content">
      <div class="product-gallery">
        <div class="main-image">
          <img v-if="mainImage" :src="mainImage" alt="" class="real-main-image" @error="handleMainImgError" />
          <div v-else class="image-placeholder">
            <el-icon :size="48"><Goods /></el-icon>
            <span>商品图片</span>
          </div>
        </div>
        <div v-if="imageList.length > 1" class="thumb-list">
          <div v-for="(img, idx) in imageList" :key="idx" class="thumb-item" :class="{ active: idx === activeImageIndex }" @click="activeImageIndex = idx">
            <img :src="img" alt="" class="thumb-real" @error="handleThumbError($event)" />
          </div>
        </div>
      </div>

      <div class="product-info-section">
        <div class="price-row">
          <span class="current-price"><em>¥</em>{{ product.price }}</span>
          <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
        </div>
        <h1 class="product-title">{{ product.title }}</h1>
        <div class="product-tags">
          <el-tag v-if="product.categoryId" size="small" type="info" effect="plain">分类商品</el-tag>
          <el-tag size="small" type="warning" effect="plain">包邮</el-tag>
        </div>
        <div class="product-stats">
          <span>{{ product.viewCount || 0 }}次浏览</span>
          <span>{{ product.likeCount || 0 }}人想要</span>
          <span>{{ comments.length }}条评论</span>
        </div>
      </div>

      <div class="seller-card">
        <div class="seller-left">
          <el-avatar :size="44" class="seller-avatar">{{ product.sellerName?.charAt(0) || '卖' }}</el-avatar>
          <div class="seller-info">
            <span class="seller-name">{{ product.sellerName || '卖家' }}</span>
            <div class="seller-meta">
              <el-rate :model-value="4.8" disabled :size="12" />
            </div>
          </div>
        </div>
        <el-button type="primary" plain size="small" @click="contactSeller">联系卖家</el-button>
      </div>

      <div class="detail-card">
        <h3 class="card-title">商品描述</h3>
        <p class="description-text">{{ product.description }}</p>
      </div>

      <div class="detail-card">
        <h3 class="card-title">交易方式</h3>
        <div class="trade-methods">
          <div class="method-item">
            <el-icon><Van /></el-icon>
            <span>快递邮寄</span>
          </div>
          <div class="method-item">
            <el-icon><Location /></el-icon>
            <span>同城自提</span>
          </div>
        </div>
      </div>

      <div class="comments-section">
        <div class="section-header">
          <h3 class="card-title">用户评价 ({{ comments.length }})</h3>
          <el-rate :model-value="avgRating" disabled :size="14" show-score />
        </div>
        <div class="comment-list">
          <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            @like="handleLikeComment"
            @reply="handleReplyComment"
          />
        </div>
        <div v-if="comments.length > 3" class="more-comments">
          <el-button text type="primary">查看全部评价</el-button>
        </div>
      </div>
    </main>

    <div class="bottom-bar">
      <div class="bar-left">
        <button class="bar-icon-btn" @click="toggleFavorite">
          <el-icon :size="22" :class="{ 'is-fav': isFavorite }"><Star /></el-icon>
          <span>收藏</span>
        </button>
        <button class="bar-icon-btn" @click="contactSeller">
          <el-icon :size="22"><ChatDotRound /></el-icon>
          <span>私聊</span>
        </button>
      </div>
      <div class="bar-right">
        <el-button type="primary" size="large" @click="handleBuy">立即购买</el-button>
        <el-button type="danger" size="large" plain @click="handleAddCart">加入购物车</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Goods, Van, Location, Star, ChatDotRound } from '@element-plus/icons-vue'
import CommentItem from '@/components/CommentItem.vue'
import { getOrCreateConversation } from '@/api/chat'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const isFavorite = ref(false)
const loading = ref(true)

const product = ref({})

const comments = ref([])

const activeImageIndex = ref(0)

const imageList = computed(() => {
  if (!product.value.images) return []
  try {
    return JSON.parse(product.value.images)
  } catch {
    return product.value.images ? [product.value.images] : []
  }
})

const mainImage = computed(() => imageList.value[activeImageIndex.value] || '')

const handleMainImgError = () => { activeImageIndex.value = -1 }
const handleThumbError = (e) => { e.target.style.display = 'none' }

const loadProduct = async () => {
  try {
    loading.value = true
    const id = route.params.id
    const res = await request({ url: `/products/${id}`, method: 'get' })
    product.value = res.data || {}
  } catch (e) {
    console.error('加载商品失败:', e)
    ElMessage.error('商品不存在')
    router.back()
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const id = route.params.id
    const res = await request({ url: `/comments/product/${id}`, method: 'get' })
    comments.value = res.data || []
  } catch (e) {
    console.error('加载评论失败:', e)
  }
}

const avgRating = computed(() => {
  if (!comments.value.length) return 0
  return +(comments.value.reduce((sum, c) => sum + c.rating, 0) / comments.value.length).toFixed(1)
})

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已收藏' : '已取消收藏')
}

const handleBuy = () => { ElMessage.info('购买功能开发中') }
const handleAddCart = () => { ElMessage.success('已加入购物车') }

const handleLikeComment = (comment) => {
  comment.likes = (comment.likes || 0) + 1
}

const handleReplyComment = (comment) => {
  ElMessage.info('回复功能开发中')
}

const contactSeller = async () => {
  try {
    const sellerId = product.value.sellerId
    if (!sellerId) {
      ElMessage.warning('卖家信息不可用')
      return
    }
    const res = await getOrCreateConversation(sellerId)
    const conv = res.data
    router.push({
      path: '/chat',
      query: {
        conversationId: conv.id,
        otherUserId: conv.otherUserId,
        otherUserName: conv.otherUserName || product.value.seller,
        otherUserAvatar: conv.otherUserAvatar || ''
      }
    })
  } catch (e) {
    console.error('创建会话失败', e)
    ElMessage.error('无法联系卖家，请稍后再试')
  }
}

onMounted(() => {
  loadProduct()
  loadComments()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.product-detail-container {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 80px;
}

.detail-header {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  @include glass-effect(0.95);
  padding: $spacing-3 $spacing-5;
  border-bottom: 1px solid $color-border-light;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 $spacing-5 $spacing-6;

  @include respond-to(md) { padding: 0 $spacing-4 $spacing-6; }
}

.product-gallery {
  margin-bottom: $spacing-5;

  .main-image {
    width: 100%;
    aspect-ratio: 1;
    border-radius: $radius-2xl;
    overflow: hidden;
    margin-bottom: $spacing-3;
    background: $neutral-100;

    .real-main-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      @include flex-center;
      flex-direction: column;
      gap: $spacing-2;
      background: linear-gradient(135deg, $neutral-50, $neutral-100);
      color: $neutral-400;
      font-size: $font-size-sm;
    }
  }

  .thumb-list {
    display: flex;
    gap: $spacing-2;
  }

  .thumb-item {
    width: 64px;
    height: 64px;
    border-radius: $radius-lg;
    overflow: hidden;
    border: 2px solid transparent;
    cursor: pointer;
    transition: all $transition-fast;
    background: $neutral-100;

    &.active { border-color: $color-primary; }

    .thumb-placeholder, .thumb-real {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .thumb-placeholder {
      @include flex-center;
      background: linear-gradient(135deg, $neutral-50, $neutral-100);
      color: $neutral-400;
    }
  }
}

.product-info-section {
  @include card-base;
  padding: $spacing-5;
  margin-bottom: $spacing-4;
  animation: fadeInUp 0.4s ease-out;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: $spacing-3;
  margin-bottom: $spacing-3;

  .current-price {
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $danger-500;

    em { font-style: normal; font-size: $font-size-xl; }
  }

  .original-price {
    font-size: $font-size-md;
    color: $color-text-placeholder;
    text-decoration: line-through;
  }

  .discount-badge {
    @include badge-tag;
    background: $danger-50;
    color: $danger-500;
  }
}

.product-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  line-height: $line-height-snug;
  margin: 0 0 $spacing-3;
}

.product-tags {
  display: flex;
  gap: $spacing-2;
  margin-bottom: $spacing-3;
}

.product-stats {
  display: flex;
  gap: $spacing-4;
  font-size: $font-size-sm;
  color: $color-text-placeholder;
}

.seller-card {
  @include card-base;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4 $spacing-5;
  margin-bottom: $spacing-4;

  .seller-left {
    display: flex;
    align-items: center;
    gap: $spacing-3;
  }

  .seller-avatar {
    background: linear-gradient(135deg, $primary-400, $primary-600);
    color: #fff;
    font-weight: $font-weight-semibold;
  }

  .seller-name {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    display: block;
  }

  .seller-meta {
    display: flex;
    align-items: center;
    gap: $spacing-2;

    .seller-stats {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }
}

.detail-card {
  @include card-base;
  padding: $spacing-5;
  margin-bottom: $spacing-4;
  animation: fadeInUp 0.4s ease-out 0.1s both;

  .card-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 $spacing-4;
    padding-bottom: $spacing-3;
    border-bottom: 1px solid $color-border-light;
  }
}

.description-text {
  font-size: $font-size-md;
  color: $color-text-secondary;
  line-height: $line-height-relaxed;
  margin: 0;
}

.trade-methods {
  display: flex;
  gap: $spacing-6;

  .method-item {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    color: $color-text-secondary;
    font-size: $font-size-md;

    .el-icon { color: $color-primary; }
  }
}

.comments-section {
  @include card-base;
  padding: $spacing-5;
  margin-bottom: $spacing-4;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-4;
    padding-bottom: $spacing-3;
    border-bottom: 1px solid $color-border-light;
  }

  .card-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0;
  }

  .comment-list {
    display: flex;
    flex-direction: column;
  }

  .more-comments {
    text-align: center;
    padding-top: $spacing-3;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include glass-effect(0.95);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $spacing-5;
  border-top: 1px solid $color-border-light;
  z-index: $z-sticky;

  .bar-left {
    display: flex;
    gap: $spacing-5;
  }

  .bar-icon-btn {
    @include flex-center;
    flex-direction: column;
    gap: 2px;
    background: none;
    border: none;
    cursor: pointer;
    color: $color-text-secondary;
    font-size: $font-size-xs;
    padding: 0;

    .is-fav { color: $warning-500; }
  }

  .bar-right {
    display: flex;
    gap: $spacing-3;
  }
}
</style>
