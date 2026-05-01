<template>
  <div class="home-container">
    <header class="header">
      <div class="header-inner">
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="22"><Shop /></el-icon>
          </div>
          <span class="logo-text">闲置好物</span>
        </div>

        <nav class="nav-links">
          <router-link class="nav-link active" to="/home">首页</router-link>
          <router-link class="nav-link" to="/category">分类</router-link>
          <router-link class="nav-link" to="/messages">聊天</router-link>
          <router-link class="nav-link" to="/notifications">消息</router-link>
          <router-link v-if="authStore.isAdmin" class="nav-link" to="/admin">管理</router-link>
        </nav>

        <div class="header-actions">
          <el-button type="primary" round @click="goToPublish">
            <el-icon class="btn-icon"><Plus /></el-icon>
            发布闲置
          </el-button>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-trigger">
              <el-avatar :size="34" :src="userInfo?.avatar">
                {{ userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userInfo?.username || '用户' }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="my-products">
                  <el-icon><Goods /></el-icon>
                  我的发布
                </el-dropdown-item>
                <el-dropdown-item command="my-orders">
                  <el-icon><List /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="messages">
                  <el-icon><ChatDotRound /></el-icon>
                  我的聊天
                </el-dropdown-item>
                <el-dropdown-item command="notifications">
                  <el-icon><Bell /></el-icon>
                  站内消息
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item v-if="authStore.isAdmin" divided command="admin">
                  <el-icon><Setting /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main-content">
      <section class="hero-section">
        <div class="hero-bg">
          <div class="hero-shape shape-1"></div>
          <div class="hero-shape shape-2"></div>
          <div class="hero-shape shape-3"></div>
        </div>
        <div class="hero-content">
          <h1 class="hero-title">发现身边的闲置好物</h1>
          <p class="hero-subtitle">让闲置流转，让生活更美好</p>
          <div class="search-wrapper">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索你想要的宝贝..."
              size="large"
              class="hero-search"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
              <template #append>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <div class="hot-tags">
              <span class="hot-label">热门：</span>
              <span class="hot-tag" v-for="tag in hotTags" :key="tag" @click="searchKeyword = tag; handleSearch()">{{ tag }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="categories-section">
        <div class="categories-grid">
          <div
            class="category-card"
            v-for="(item, index) in categories"
            :key="item.id"
            :style="{ animationDelay: `${index * 0.06}s` }"
            @click="item.id === 8 ? router.push('/category') : router.push({ path: '/category', query: { cat: item.id } })"
          >
            <div class="category-icon" :style="{ background: item.bg }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <span class="category-name">{{ item.name }}</span>
          </div>
        </div>
      </section>

      <section class="products-section">
        <div class="section-header">
          <div class="section-title-group">
            <h2 class="section-title">推荐好物</h2>
            <p class="section-desc">精选优质闲置，品质有保障</p>
          </div>
          <a class="view-more" @click="loadProducts">
            刷新
            <el-icon><ArrowRight /></el-icon>
          </a>
        </div>

        <div v-if="productsLoading" class="loading-state">
          <el-icon class="loading-spin" :size="24"><Loading /></el-icon>
          <span>正在加载商品，请稍候...</span>
        </div>
        <div v-else-if="productsError" class="loading-state error-state">
          <span>加载失败，服务器可能正在启动中</span>
          <el-button type="primary" size="small" @click="loadProducts" style="margin-top: 8px">点击重试</el-button>
        </div>
        <div v-else class="products-grid">
          <div v-if="productList.length === 0" style="grid-column: 1/-1; text-align: center; padding: 60px 0; color: #909399;">
            <el-empty description="暂无商品" />
          </div>
          <div
            class="product-card"
            v-for="(product, idx) in productList"
            :key="product.id"
            :style="{ animationDelay: `${idx * 0.05}s` }"
            @click="router.push(`/product/${product.id}`)"
          >
            <div class="product-image">
              <img v-if="product.images" :src="parseFirstImage(product.images)" alt="" class="real-image" @error="handleImgError" />
              <div v-else class="placeholder-image">
                <el-icon :size="32"><Goods /></el-icon>
                <span>商品图片</span>
              </div>
              <button class="fav-btn" @click.stop="handleFavorite(product)">
                <el-icon><Star /></el-icon>
              </button>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <div class="product-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ formatPrice(product.price) }}</span>
                <span v-if="product.originalPrice" class="price-original">¥{{ formatPrice(product.originalPrice) }}</span>
              </div>
              <div class="product-meta">
                <span class="meta-views">{{ product.viewCount || 0 }}浏览</span>
                <span class="meta-likes">{{ product.likeCount || 0 }}点赞</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <p>© 2024 闲置好物 - 让闲置流转，让生活更美好</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { Shop, Plus, ArrowDown, ArrowRight, User, Goods, List, SwitchButton, Search, Location, Phone, Monitor, Timer, Basketball, Document, Present, UserFilled, Star, ChatDotRound, Bell, Setting, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const authStore = useAuthStore()

const searchKeyword = ref('')
const productList = ref([])
const productsLoading = ref(false)
const productsError = ref(false)

const userInfo = ref(authStore.userInfo)

const loadProducts = async () => {
  productsLoading.value = true
  productsError.value = false
  try {
    const res = await request({ url: '/products', method: 'get' })
    productList.value = res.data || []
  } catch (e) {
    console.error('加载商品失败:', e)
    productsError.value = true
  } finally {
    productsLoading.value = false
  }
}

const parseFirstImage = (images) => {
  if (!images) return ''
  try {
    const arr = JSON.parse(images)
    return arr[0] || ''
  } catch {
    return images
  }
}

const handleImgError = (e) => {
  e.target.style.display = 'none'
  const placeholder = e.target.nextElementSibling
  if (placeholder) placeholder.style.display = 'flex'
}

const formatPrice = (price) => {
  if (!price) return '0'
  return Number(price).toLocaleString()
}

const handleFavorite = (product) => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  ElMessage.success('已收藏')
}

const hotTags = ref(['iPhone', 'MacBook', 'Switch', '戴森', 'Kindle'])

const categories = ref([
  { id: 1, name: '数码', icon: Monitor, bg: 'linear-gradient(135deg, #e0e7ff, #c7d2fe)' },
  { id: 2, name: '手机', icon: Phone, bg: 'linear-gradient(135deg, #fce7f3, #fbcfe8)' },
  { id: 3, name: '手表', icon: Timer, bg: 'linear-gradient(135deg, #d1fae5, #a7f3d0)' },
  { id: 4, name: '运动', icon: Basketball, bg: 'linear-gradient(135deg, #fef3c7, #fde68a)' },
  { id: 5, name: '图书', icon: Document, bg: 'linear-gradient(135deg, #e0f2fe, #bae6fd)' },
  { id: 6, name: '礼品', icon: Present, bg: 'linear-gradient(135deg, #fae8ff, #e9d5ff)' },
  { id: 7, name: '服饰', icon: UserFilled, bg: 'linear-gradient(135deg, #fee2e2, #fecaca)' },
  { id: 8, name: '更多', icon: Goods, bg: 'linear-gradient(135deg, #f1f5f9, #e2e8f0)' }
])

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { q: searchKeyword.value } })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'my-products':
      router.push('/my-products')
      break
    case 'my-orders':
      router.push('/my-orders')
      break
    case 'messages':
      router.push('/messages')
      break
    case 'notifications':
      router.push('/notifications')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'logout':
      authStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
      break
  }
}

const goToPublish = () => {
  router.push('/publish')
}

onMounted(() => {
  if (!userInfo.value) {
    authStore.initUserInfo()
    userInfo.value = authStore.userInfo
  }
  loadProducts()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.home-container {
  min-height: 100vh;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.header {
  @include glass-effect(0.92);
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  border-bottom: 1px solid rgba($neutral-200, 0.6);

  .header-inner {
    @include page-container;
    height: $header-height;
    display: flex;
    align-items: center;
    gap: $spacing-8;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;

    .logo-icon {
      @include flex-center;
      width: 36px;
      height: 36px;
      border-radius: $radius-lg;
      @include gradient-primary;
      color: #fff;
    }

    .logo-text {
      font-family: $font-family-display;
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      letter-spacing: $letter-spacing-tight;
    }
  }

  .nav-links {
    display: flex;
    gap: $spacing-2;
    flex: 1;

    @include respond-to(md) {
      display: none;
    }

    .nav-link {
      padding: $spacing-2 $spacing-4;
      font-size: $font-size-md;
      font-weight: $font-weight-medium;
      color: $color-text-secondary;
      border-radius: $radius-md;
      cursor: pointer;
      transition: all $transition-base;

      &:hover {
        color: $color-text-primary;
        background: $neutral-50;
      }

      &.active {
        color: $color-primary;
        background: $primary-50;
      }
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: $spacing-4;
    flex-shrink: 0;

    .btn-icon {
      margin-right: $spacing-1;
    }
  }

  .user-trigger {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    cursor: pointer;
    padding: $spacing-1 $spacing-2;
    border-radius: $radius-full;
    transition: all $transition-base;

    &:hover {
      background: $neutral-50;
    }

    .user-name {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $color-text-primary;

      @include respond-to(md) {
        display: none;
      }
    }

    .arrow-icon {
      font-size: 12px;
      color: $color-text-placeholder;
      transition: transform $transition-base;
    }

    &:hover .arrow-icon {
      transform: rotate(180deg);
    }
  }
}

.main-content {
  flex: 1;
}

.hero-section {
  position: relative;
  padding: $spacing-16 0 $spacing-10;
  overflow: hidden;

  @include respond-to(md) {
    padding: $spacing-10 0 $spacing-6;
  }

  .hero-bg {
    position: absolute;
    inset: 0;
    @include gradient-hero;
    clip-path: ellipse(80% 100% at 50% 0%);

    @include respond-to(md) {
      clip-path: ellipse(120% 100% at 50% 0%);
    }
  }

  .hero-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;

    &.shape-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -50px;
      background: #fff;
    }

    &.shape-2 {
      width: 250px;
      height: 250px;
      bottom: -50px;
      left: 10%;
      background: #fff;
    }

    &.shape-3 {
      width: 150px;
      height: 150px;
      top: 20%;
      left: 30%;
      background: #fff;
    }
  }

  .hero-content {
    @include page-container;
    position: relative;
    text-align: center;
    animation: fadeInUp 0.6s ease-out;
  }

  .hero-title {
    font-size: $font-size-6xl;
    font-weight: $font-weight-bold;
    color: #fff;
    margin-bottom: $spacing-3;
    letter-spacing: -0.03em;

    @include respond-to(md) {
      font-size: $font-size-4xl;
    }
  }

  .hero-subtitle {
    font-size: $font-size-lg;
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: $spacing-8;

    @include respond-to(md) {
      font-size: $font-size-md;
      margin-bottom: $spacing-6;
    }
  }

  .search-wrapper {
    max-width: 600px;
    margin: 0 auto;
  }

  .hero-search {
    :deep(.el-input__wrapper) {
      border-radius: $radius-full;
      padding: 4px 4px 4px 20px;
      box-shadow: $shadow-xl;
      border: none;
      height: 52px;
    }

    :deep(.el-input__inner) {
      font-size: $font-size-md;
    }

    :deep(.el-input-group__append) {
      border-radius: $radius-full;
      border: none;
      padding: 0;
      background: transparent;
      box-shadow: none;

      .el-button {
        height: 44px;
        padding: 0 $spacing-6;
        border-radius: $radius-full;
        font-weight: $font-weight-medium;
      }
    }

    .search-icon {
      color: $color-text-placeholder;
      font-size: 18px;
    }
  }

  .hot-tags {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-2;
    margin-top: $spacing-4;
    flex-wrap: wrap;

    .hot-label {
      font-size: $font-size-xs;
      color: rgba(255, 255, 255, 0.7);
    }

    .hot-tag {
      padding: $spacing-1 $spacing-3;
      font-size: $font-size-xs;
      color: rgba(255, 255, 255, 0.9);
      background: rgba(255, 255, 255, 0.15);
      border-radius: $radius-full;
      cursor: pointer;
      transition: all $transition-fast;
      backdrop-filter: blur(4px);

      &:hover {
        background: rgba(255, 255, 255, 0.25);
        color: #fff;
      }
    }
  }
}

.categories-section {
  @include page-container;
  margin-top: -$spacing-6;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out 0.1s both;

  .categories-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: $spacing-3;
    background: $color-bg-card;
    padding: $spacing-6 $spacing-5;
    border-radius: $radius-2xl;
    box-shadow: $shadow-md;
    border: 1px solid $color-border-light;

    @include respond-to(lg) {
      grid-template-columns: repeat(4, 1fr);
    }

    @include respond-to(sm) {
      grid-template-columns: repeat(4, 1fr);
      gap: $spacing-2;
      padding: $spacing-4 $spacing-3;
    }
  }

  .category-card {
    @include flex-column-center;
    gap: $spacing-2;
    padding: $spacing-4 $spacing-2;
    border-radius: $radius-lg;
    cursor: pointer;
    transition: all $transition-base;

    &:hover {
      background: $neutral-50;
      transform: translateY(-2px);
    }

    &:active {
      transform: translateY(0);
    }

    .category-icon {
      @include flex-center;
      width: 52px;
      height: 52px;
      border-radius: $radius-xl;
      transition: transform $transition-spring;

      .el-icon {
        color: $color-primary;
      }
    }

    &:hover .category-icon {
      transform: scale(1.08);
    }

    .category-name {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $color-text-regular;
    }
  }
}

.products-section {
  @include page-container;
  padding-top: $spacing-8;

  .section-header {
    @include flex-between;
    margin-bottom: $spacing-6;
  }

  .section-title-group {
    .section-title {
      @include section-title;
      margin-bottom: $spacing-1;
    }

    .section-desc {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      margin: 0;
    }
  }

  .view-more {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-primary;
    cursor: pointer;
    transition: all $transition-fast;

    &:hover {
      gap: $spacing-2;
    }
  }

  .products-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-5;

    @include respond-to(lg) {
      grid-template-columns: repeat(3, 1fr);
    }

    @include respond-to(md) {
      grid-template-columns: repeat(2, 1fr);
      gap: $spacing-3;
    }
  }

  .product-card {
    @include card-base;
    overflow: hidden;
    animation: fadeInUp 0.5s ease-out both;

    &:hover {
      box-shadow: $shadow-xl;
      transform: translateY(-4px);
      border-color: transparent;
    }

    .product-image {
      position: relative;
      aspect-ratio: 1;
      overflow: hidden;
      background: $neutral-100;

      .placeholder-image {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: $spacing-2;
        background: linear-gradient(135deg, $neutral-50, $neutral-100);
        color: $neutral-400;
        font-size: $font-size-sm;
      }

      img, .real-image {
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

        &:hover {
          color: $danger-500;
          background: #fff;
        }
      }

      &:hover {
        img {
          transform: scale(1.05);
        }

        .fav-btn {
          opacity: 1;
          transform: scale(1);
        }
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

      .tag-quality {
        background: $primary-50;
        color: $primary-600;
      }

      .tag-fast {
        background: $success-50;
        color: $success-600;
      }
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

      .meta-location, .meta-views, .meta-likes {
        display: flex;
        align-items: center;
        gap: 2px;
      }
    }
  }
}

.site-footer {
  margin-top: auto;
  padding: $spacing-8 0;
  border-top: 1px solid $color-border-light;
  background: $color-bg-card;

  .footer-inner {
    @include page-container;
    text-align: center;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: $color-text-secondary;
  gap: 12px;

  .loading-spin {
    animation: spin 1s linear infinite;
  }

  &.error-state {
    color: $color-text-placeholder;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
