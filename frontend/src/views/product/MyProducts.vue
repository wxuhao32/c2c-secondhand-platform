<template>
  <div class="my-products-container">
    <PageHeader title="我的发布">
      <template #actions>
        <el-button type="primary" @click="router.push('/publish')">
          <el-icon><Plus /></el-icon>
          发布闲置
        </el-button>
      </template>
    </PageHeader>

    <main class="main-content">
      <div class="tabs-bar">
        <div
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
          <span v-if="tab.count" class="tab-count">{{ tab.count }}</span>
        </div>
      </div>

      <div v-if="filteredProducts.length" class="products-list">
        <div v-for="product in filteredProducts" :key="product.id" class="product-item">
          <div class="product-image">
            <img v-if="product.image" :src="product.image" :alt="product.title" />
            <div v-else class="placeholder-image">
              <el-icon :size="24"><Goods /></el-icon>
            </div>
            <span class="status-badge" :class="`status-${product.status}`">{{ statusMap[product.status] || '未知' }}</span>
          </div>
          <div class="product-info">
            <h3 class="product-title">{{ product.title }}</h3>
            <div class="product-meta">
              <span class="product-price">¥{{ product.price }}</span>
              <span class="product-views">{{ product.viewCount || 0 }}次浏览</span>
              <span class="product-likes">{{ product.likeCount || 0 }}人想要</span>
            </div>
            <div class="product-time">发布于 {{ product.createdAt }}</div>
          </div>
          <div class="product-actions">
            <el-button v-if="product.status === 1" type="primary" text size="small" @click="handleEdit(product)">编辑</el-button>
            <el-button v-if="product.status === 1" text size="small" @click="toggleStatus(product, 3)">下架</el-button>
            <el-button v-if="product.status === 3" type="success" text size="small" @click="toggleStatus(product, 1)">重新上架</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(product)">删除</el-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty :description="activeTab === 'all' ? '暂无发布商品' : `暂无${tabs.find(t => t.value === activeTab)?.label}商品`">
          <el-button type="primary" @click="router.push('/publish')">发布闲置</el-button>
        </el-empty>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Goods } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getMyProducts, updateProductStatus, deleteProduct } from '@/api/product'

const router = useRouter()
const activeTab = ref('all')
const loading = ref(false)

const statusMap = {
  0: '待审核',
  1: '在售',
  2: '已售',
  3: '已下架'
}

const tabs = computed(() => [
  { label: '全部', value: 'all', count: myProducts.value.length },
  { label: '在售', value: 1, count: myProducts.value.filter(p => p.status === 1).length },
  { label: '已下架', value: 3, count: myProducts.value.filter(p => p.status === 3).length },
  { label: '已售出', value: 2, count: myProducts.value.filter(p => p.status === 2).length }
])

const myProducts = ref([])

const loadMyProducts = async () => {
  loading.value = true
  try {
    const res = await getMyProducts()
    myProducts.value = (res.data || []).map(p => ({
      ...p,
      image: p.images ? (JSON.parse(p.images)[0] || '') : ''
    }))
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMyProducts()
})

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') return myProducts.value
  return myProducts.value.filter(p => p.status === activeTab.value)
})

const handleEdit = (product) => {
  router.push(`/publish?id=${product.id}`)
}

const toggleStatus = async (product, newStatus) => {
  try {
    await updateProductStatus(product.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已重新上架' : '已下架')
    loadMyProducts()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (product) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复', '提示', { type: 'warning' })
    await deleteProduct(product.id)
    ElMessage.success('商品已删除')
    loadMyProducts()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.my-products-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) { padding: $spacing-4; }
}

.tabs-bar {
  display: flex;
  gap: $spacing-1;
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $spacing-1;
  margin-bottom: $spacing-5;
  box-shadow: $shadow-sm;
  border: 1px solid $color-border-light;
}

.tab-item {
  flex: 1;
  @include flex-center;
  gap: $spacing-1;
  padding: $spacing-3 $spacing-4;
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-base;

  &:hover { color: $color-text-primary; background: $neutral-50; }

  &.active {
    @include gradient-primary;
    color: #fff;
    box-shadow: $shadow-colored;
  }

  .tab-count {
    font-size: $font-size-xs;
    padding: 0 $spacing-1;
    border-radius: $radius-full;
    background: rgba(0, 0, 0, 0.08);

    .active & {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.product-item {
  @include card-base;
  display: flex;
  gap: $spacing-4;
  padding: $spacing-4;
  animation: fadeInUp 0.4s ease-out both;

  &:hover { box-shadow: $shadow-md; }

  @include respond-to(md) {
    flex-direction: column;
    gap: $spacing-3;
  }
}

.product-image {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: $radius-lg;
  overflow: hidden;
  flex-shrink: 0;
  background: $neutral-100;

  @include respond-to(md) {
    width: 100%;
    height: 200px;
  }

  .placeholder-image {
    width: 100%;
    height: 100%;
    @include flex-center;
    background: linear-gradient(135deg, $neutral-50, $neutral-100);
    color: $neutral-400;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .status-badge {
    position: absolute;
    top: $spacing-2;
    left: $spacing-2;
    @include badge-tag;
    font-size: 11px;

    &.status-on_sale { background: $success-50; color: $success-600; }
    &.status-off_sale { background: $neutral-100; color: $neutral-500; }
    &.status-sold { background: $primary-50; color: $primary-600; }
  }
}

.product-info {
  flex: 1;
  min-width: 0;

  .product-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin: 0 0 $spacing-2;
    @include text-ellipsis(2);
    line-height: 1.4;
  }

  .product-meta {
    display: flex;
    align-items: center;
    gap: $spacing-4;
    margin-bottom: $spacing-2;

    .product-price {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $danger-500;
    }

    .product-views, .product-likes {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }

  .product-time {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
  flex-shrink: 0;
  justify-content: center;

  @include respond-to(md) {
    flex-direction: row;
    justify-content: flex-end;
  }
}

.empty-state {
  @include flex-center;
  min-height: 400px;
}
</style>
