<template>
  <div class="category-container">
    <PageHeader title="商品分类" />

    <main class="main-content">
      <div class="category-layout">
        <aside class="category-sidebar">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-menu-item"
            :class="{ active: activeCategory === cat.id }"
            @click="activeCategory = cat.id"
          >
            <el-icon :size="16"><component :is="cat.icon" /></el-icon>
            <span class="menu-text">{{ cat.name }}</span>
          </div>
        </aside>

        <section class="category-detail">
          <div v-if="currentCategory" class="detail-content">
            <h2 class="category-title">{{ currentCategory.name }}</h2>
            <div class="sub-categories">
              <div v-for="sub in currentCategory.children" :key="sub.id" class="sub-category-section">
                <h3 class="sub-title">{{ sub.name }}</h3>
                <div class="tag-list">
                  <span
                    v-for="tag in sub.children"
                    :key="tag.id"
                    class="category-tag"
                    @click="goToCategory(tag.id, tag.name)"
                  >{{ tag.name }}</span>
                </div>
              </div>
            </div>

            <div class="hot-products">
              <div class="section-header">
                <h3 class="section-title">热门推荐</h3>
                <el-button text size="small" @click="goToCategory(currentCategory.id, currentCategory.name)">查看更多</el-button>
              </div>
              <div class="products-grid">
                <ProductCard
                  v-for="product in hotProducts"
                  :key="product.id"
                  :product="product"
                  @click="router.push(`/product/${product.id}`)"
                  @toggle-fav="toggleFav"
                />
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Monitor, Clock, Football, Reading, ShoppingBag, MagicStick, House, Lollipop } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()
const activeCategory = ref(1)

const categories = [
  { id: 1, name: '手机数码', icon: Phone },
  { id: 2, name: '电脑办公', icon: Monitor },
  { id: 3, name: '智能手表', icon: Clock },
  { id: 4, name: '运动户外', icon: Football },
  { id: 5, name: '图书文具', icon: Reading },
  { id: 6, name: '服饰鞋包', icon: ShoppingBag },
  { id: 7, name: '美妆护肤', icon: MagicStick },
  { id: 8, name: '家居生活', icon: House },
  { id: 9, name: '母婴用品', icon: Lollipop }
]

const categoryData = {
  1: {
    name: '手机数码',
    children: [
      { id: 11, name: '手机', children: [
        { id: 111, name: 'iPhone' }, { id: 112, name: '华为' }, { id: 113, name: '小米' },
        { id: 114, name: 'OPPO' }, { id: 115, name: 'vivo' }, { id: 116, name: '三星' }
      ]},
      { id: 12, name: '平板', children: [
        { id: 121, name: 'iPad' }, { id: 122, name: '华为平板' }, { id: 123, name: '小米平板' }
      ]},
      { id: 13, name: '耳机音箱', children: [
        { id: 131, name: 'AirPods' }, { id: 132, name: '蓝牙耳机' }, { id: 133, name: '智能音箱' }
      ]},
      { id: 14, name: '配件', children: [
        { id: 141, name: '手机壳' }, { id: 142, name: '充电器' }, { id: 143, name: '数据线' }, { id: 144, name: '贴膜' }
      ]}
    ]
  },
  2: {
    name: '电脑办公',
    children: [
      { id: 21, name: '笔记本', children: [
        { id: 211, name: 'MacBook' }, { id: 212, name: 'ThinkPad' }, { id: 213, name: '游戏本' }
      ]},
      { id: 22, name: '台式机', children: [
        { id: 221, name: '一体机' }, { id: 222, name: '主机' }
      ]},
      { id: 23, name: '外设', children: [
        { id: 231, name: '键盘' }, { id: 232, name: '鼠标' }, { id: 233, name: '显示器' }
      ]}
    ]
  },
  3: {
    name: '智能手表',
    children: [
      { id: 31, name: 'Apple Watch', children: [{ id: 311, name: 'Series 9' }, { id: 312, name: 'Ultra' }, { id: 313, name: 'SE' }] },
      { id: 32, name: '华为手表', children: [{ id: 321, name: 'GT系列' }, { id: 322, name: 'Watch系列' }] },
      { id: 33, name: '其他品牌', children: [{ id: 331, name: '小米手表' }, { id: 332, name: '三星手表' }] }
    ]
  },
  4: {
    name: '运动户外',
    children: [
      { id: 41, name: '健身器材', children: [{ id: 411, name: '跑步机' }, { id: 412, name: '哑铃' }, { id: 413, name: '瑜伽垫' }] },
      { id: 42, name: '户外装备', children: [{ id: 421, name: '帐篷' }, { id: 422, name: '登山杖' }, { id: 423, name: '睡袋' }] }
    ]
  },
  5: {
    name: '图书文具',
    children: [
      { id: 51, name: '教材教辅', children: [{ id: 511, name: '考研' }, { id: 512, name: '公务员' }, { id: 513, name: '英语' }] },
      { id: 52, name: '文学小说', children: [{ id: 521, name: '国内文学' }, { id: 522, name: '外国文学' }] }
    ]
  },
  6: {
    name: '服饰鞋包',
    children: [
      { id: 61, name: '男装', children: [{ id: 611, name: '外套' }, { id: 612, name: 'T恤' }, { id: 613, name: '裤子' }] },
      { id: 62, name: '女装', children: [{ id: 621, name: '连衣裙' }, { id: 622, name: '上衣' }, { id: 623, name: '半裙' }] },
      { id: 63, name: '鞋靴', children: [{ id: 631, name: '运动鞋' }, { id: 632, name: '皮鞋' }, { id: 633, name: '凉鞋' }] },
      { id: 64, name: '箱包', children: [{ id: 641, name: '双肩包' }, { id: 642, name: '手提包' }, { id: 643, name: '行李箱' }] }
    ]
  },
  7: {
    name: '美妆护肤',
    children: [
      { id: 71, name: '护肤', children: [{ id: 711, name: '面霜' }, { id: 712, name: '精华' }, { id: 713, name: '防晒' }] },
      { id: 72, name: '彩妆', children: [{ id: 721, name: '口红' }, { id: 722, name: '粉底' }, { id: 723, name: '眼影' }] }
    ]
  },
  8: {
    name: '家居生活',
    children: [
      { id: 81, name: '家电', children: [{ id: 811, name: '小家电' }, { id: 812, name: '厨电' }] },
      { id: 82, name: '家具', children: [{ id: 821, name: '桌椅' }, { id: 822, name: '收纳' }] }
    ]
  },
  9: {
    name: '母婴用品',
    children: [
      { id: 91, name: '婴儿用品', children: [{ id: 911, name: '推车' }, { id: 912, name: '安全座椅' }] },
      { id: 92, name: '玩具', children: [{ id: 921, name: '积木' }, { id: 922, name: '益智玩具' }] }
    ]
  }
}

const currentCategory = computed(() => categoryData[activeCategory.value])

const hotProducts = computed(() => [
  { id: 1, title: `${currentCategory.value?.name || ''}热门商品1`, price: '2999', originalPrice: '5999', condition: '9成新', image: '', isFavorite: false },
  { id: 2, title: `${currentCategory.value?.name || ''}热门商品2`, price: '1599', originalPrice: '3299', condition: '8成新', image: '', isFavorite: false },
  { id: 3, title: `${currentCategory.value?.name || ''}热门商品3`, price: '899', originalPrice: '1999', condition: '几乎全新', image: '', isFavorite: true },
  { id: 4, title: `${currentCategory.value?.name || ''}热门商品4`, price: '4599', originalPrice: '8999', condition: '9成新', image: '', isFavorite: false }
])

const goToCategory = (id, name) => {
  router.push({ path: '/search', query: { q: name, category: id } })
}

const toggleFav = (product) => {
  product.isFavorite = !product.isFavorite
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.category-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-5;
}

.category-layout {
  display: flex;
  gap: $spacing-5;
  min-height: calc(100vh - 140px);
}

.category-sidebar {
  width: 100px;
  flex-shrink: 0;
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $spacing-2;
  box-shadow: $shadow-sm;
  border: 1px solid $color-border-light;
  position: sticky;
  top: 80px;
  height: fit-content;

  @include respond-to(md) {
    width: 80px;
  }
}

.category-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-3 $spacing-2;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-fast;
  color: $color-text-secondary;

  &:hover { background: $neutral-50; color: $color-text-primary; }

  &.active {
    @include gradient-primary;
    color: #fff;
    box-shadow: $shadow-colored;
  }

  .menu-text {
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
    text-align: center;
    white-space: nowrap;
  }
}

.category-detail {
  flex: 1;
  min-width: 0;
}

.detail-content {
  animation: fadeInUp 0.4s ease-out;
}

.category-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin: 0 0 $spacing-5;
}

.sub-categories {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
  margin-bottom: $spacing-6;
}

.sub-category-section {
  .sub-title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 $spacing-3;
    padding-left: $spacing-2;
    border-left: 3px solid $color-primary;
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-2;
}

.category-tag {
  padding: $spacing-2 $spacing-4;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  background: $color-bg-card;
  border: 1px solid $color-border-light;
  border-radius: $radius-full;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    color: $color-primary;
    border-color: $primary-200;
    background: $primary-50;
  }
}

.hot-products {
  .section-header {
    @include flex-between;
    margin-bottom: $spacing-4;

    .section-title {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin: 0;
    }
  }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: $spacing-4;

  @include respond-to(sm) {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
