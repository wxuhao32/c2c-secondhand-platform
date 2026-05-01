<template>
  <div class="favorites-container">
    <PageHeader title="我的收藏" />

    <main class="main-content">
      <div v-if="favorites.length" class="favorites-grid">
        <ProductCard
          v-for="product in favorites"
          :key="product.id"
          :product="product"
          @click="router.push(`/product/${product.id}`)"
          @toggle-fav="toggleFav"
        />
      </div>

      <div v-else class="empty-state">
        <el-empty description="暂无收藏商品">
          <el-button type="primary" @click="router.push('/home')">去逛逛</el-button>
        </el-empty>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()

const favorites = ref([
  { id: 1, title: 'MacBook Air M2 8+256 星光色', price: '6800', originalPrice: '9499', condition: '几乎全新', image: '', isFavorite: true },
  { id: 2, title: 'AirPods Pro 2 USB-C版本', price: '1200', originalPrice: '1899', condition: '全新未拆', image: '', isFavorite: true },
  { id: 3, title: 'Nintendo Switch OLED 白色', price: '1800', originalPrice: '2599', condition: '9成新', image: '', isFavorite: true },
  { id: 4, title: 'Kindle Paperwhite 5 32GB', price: '650', originalPrice: '1068', condition: '8成新', image: '', isFavorite: true },
  { id: 5, title: 'Sony WH-1000XM5 降噪耳机', price: '1699', originalPrice: '2999', condition: '9成新', image: '', isFavorite: true },
  { id: 6, title: 'iPad Air 5 64G WiFi版', price: '2800', originalPrice: '4799', condition: '9成新', image: '', isFavorite: true }
])

const toggleFav = (product) => {
  product.isFavorite = !product.isFavorite
  if (!product.isFavorite) {
    favorites.value = favorites.value.filter(p => p.id !== product.id)
    ElMessage.success('已取消收藏')
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.favorites-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) { padding: $spacing-4; }
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: $spacing-4;

  @include respond-to(sm) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.empty-state {
  @include flex-center;
  min-height: 400px;
}
</style>
