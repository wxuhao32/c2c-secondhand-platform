<template>
  <div class="search-result-container">
    <header class="search-header">
      <div class="search-bar-wrapper">
        <SearchBar
          :default-keyword="keyword"
          :compact="true"
          @search="handleSearch"
        />
        <el-button text @click="router.back()" class="cancel-btn">取消</el-button>
      </div>
    </header>

    <main class="main-content">
      <div class="filter-bar">
        <div class="filter-tabs">
          <span
            v-for="filter in filters"
            :key="filter.value"
            class="filter-tab"
            :class="{ active: activeFilter === filter.value }"
            @click="activeFilter = filter.value"
          >{{ filter.label }}</span>
        </div>
        <div class="view-toggle">
          <el-icon :size="18" @click="viewMode = 'grid'" :class="{ active: viewMode === 'grid' }"><Grid /></el-icon>
          <el-icon :size="18" @click="viewMode = 'list'" :class="{ active: viewMode === 'list' }"><List /></el-icon>
        </div>
      </div>

      <div v-if="results.length" :class="['results-grid', viewMode]">
        <ProductCard
          v-for="product in results"
          :key="product.id"
          :product="product"
          :view-mode="viewMode"
          @click="router.push(`/product/${product.id}`)"
          @toggle-fav="toggleFav"
        />
      </div>

      <div v-else class="empty-state">
        <el-empty description="未找到相关商品">
          <template #image>
            <div class="empty-icon">
              <el-icon :size="48"><Search /></el-icon>
            </div>
          </template>
          <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
        </el-empty>
      </div>

      <div v-if="results.length" class="related-keywords">
        <span class="related-label">相关搜索：</span>
        <span
          v-for="kw in relatedKeywords"
          :key="kw"
          class="related-tag"
          @click="handleSearch(kw)"
        >{{ kw }}</span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Grid, List, Search } from '@element-plus/icons-vue'
import SearchBar from '@/components/SearchBar.vue'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()
const route = useRoute()
const keyword = ref(route.query.q || '')
const activeFilter = ref('all')
const viewMode = ref('grid')

const filters = [
  { label: '综合', value: 'all' },
  { label: '最新', value: 'newest' },
  { label: '价格', value: 'price' },
  { label: '销量', value: 'sales' }
]

const results = ref([
  { id: 1, title: 'iPhone 13 Pro 256G 远峰蓝 国行在保', price: '3999', originalPrice: '8999', condition: '9成新', image: '', isFavorite: false },
  { id: 2, title: 'iPhone 14 Pro Max 512G 暗紫色', price: '5999', originalPrice: '11999', condition: '几乎全新', image: '', isFavorite: true },
  { id: 3, title: 'iPhone 12 128G 白色 电池95%', price: '2199', originalPrice: '5599', condition: '8成新', image: '', isFavorite: false },
  { id: 4, title: 'iPhone 15 Pro 256G 原色钛金属', price: '6800', originalPrice: '8999', condition: '几乎全新', image: '', isFavorite: false },
  { id: 5, title: 'iPhone 13 mini 128G 星光色', price: '2499', originalPrice: '5199', condition: '9成新', image: '', isFavorite: false },
  { id: 6, title: 'iPhone 14 128G 午夜色 双卡', price: '3599', originalPrice: '5999', condition: '9成新', image: '', isFavorite: false }
])

const relatedKeywords = ref(['iPhone 14', 'iPhone手机壳', 'iPhone充电器', '二手苹果', 'AirPods'])

const handleSearch = (kw) => {
  keyword.value = kw
  router.replace({ path: '/search', query: { q: kw } })
}

const toggleFav = (product) => {
  product.isFavorite = !product.isFavorite
}

onMounted(() => {
  if (!keyword.value) {
    router.replace('/home')
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.search-result-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  @include glass-effect(0.95);
  padding: $spacing-3 $spacing-5;
  border-bottom: 1px solid $color-border-light;

  .search-bar-wrapper {
    max-width: 900px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    gap: $spacing-3;
  }

  .cancel-btn {
    flex-shrink: 0;
    font-size: $font-size-md;
    color: $color-text-secondary;
  }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-5;
}

.filter-bar {
  @include flex-between;
  margin-bottom: $spacing-5;
  padding: $spacing-3 $spacing-4;
  background: $color-bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
  border: 1px solid $color-border-light;
}

.filter-tabs {
  display: flex;
  gap: $spacing-1;
}

.filter-tab {
  padding: $spacing-2 $spacing-4;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover { color: $color-text-primary; background: $neutral-50; }

  &.active {
    @include gradient-primary;
    color: #fff;
  }
}

.view-toggle {
  display: flex;
  gap: $spacing-2;

  .el-icon {
    padding: $spacing-2;
    border-radius: $radius-md;
    cursor: pointer;
    color: $color-text-placeholder;
    transition: all $transition-fast;

    &:hover { color: $color-text-primary; background: $neutral-50; }
    &.active { color: $color-primary; background: $primary-50; }
  }
}

.results-grid {
  display: grid;
  gap: $spacing-4;

  &.grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));

    @include respond-to(sm) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  &.list {
    grid-template-columns: 1fr;
  }
}

.related-keywords {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $spacing-2;
  margin-top: $spacing-6;
  padding: $spacing-4;
  background: $color-bg-card;
  border-radius: $radius-xl;
  border: 1px solid $color-border-light;

  .related-label {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    flex-shrink: 0;
  }

  .related-tag {
    padding: $spacing-1 $spacing-3;
    font-size: $font-size-sm;
    color: $color-text-secondary;
    background: $neutral-50;
    border-radius: $radius-full;
    cursor: pointer;
    transition: all $transition-fast;

    &:hover {
      color: $color-primary;
      background: $primary-50;
    }
  }
}

.empty-state {
  @include flex-center;
  min-height: 400px;

  .empty-icon {
    color: $neutral-300;
  }
}
</style>
