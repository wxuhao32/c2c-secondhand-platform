<template>
  <div class="orders-container">
    <PageHeader title="我的订单" />

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

      <div v-if="filteredOrders.length" class="orders-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card" @click="router.push(`/order/${order.id}`)">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status" :class="`status-${order.status}`">{{ statusMap[order.status] }}</span>
          </div>
          <div class="order-body">
            <div class="product-thumb">
              <div class="thumb-placeholder">
                <el-icon :size="20"><Goods /></el-icon>
              </div>
            </div>
            <div class="order-product-info">
              <h4 class="product-title">{{ order.productTitle }}</h4>
              <span class="product-condition">{{ order.condition }}</span>
            </div>
            <div class="order-price-info">
              <span class="order-price">¥{{ order.price }}</span>
              <span class="order-quantity">x{{ order.quantity || 1 }}</span>
            </div>
          </div>
          <div class="order-footer">
            <span class="order-time">{{ order.createdAt }}</span>
            <div class="order-actions" @click.stop>
              <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order)">立即付款</el-button>
              <el-button v-if="order.status === 0" size="small" @click="handleCancel(order)">取消订单</el-button>
              <el-button v-if="order.status === 1" size="small" type="primary" plain>提醒发货</el-button>
              <el-button v-if="order.status === 2" type="primary" size="small" @click="handleConfirmReceipt(order)">确认收货</el-button>
              <el-button v-if="order.status === 3" type="primary" size="small" @click="goToReview(order)">去评价</el-button>
              <el-button size="small" @click="router.push(`/order/${order.id}`)">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty :description="`暂无${tabs.find(t => t.value === activeTab)?.label}订单`">
          <el-button type="primary" @click="router.push('/home')">去逛逛</el-button>
        </el-empty>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getOrderList, cancelOrder, confirmReceipt } from '@/api/order'

const router = useRouter()
const activeTab = ref('all')

const statusMap = {
  0: '待付款',
  1: '待发货',
  2: '运输中',
  3: '已完成',
  4: '已取消'
}

const tabs = computed(() => [
  { label: '全部', value: 'all', count: orders.value.length },
  { label: '待付款', value: 0, count: orders.value.filter(o => o.status === 0).length },
  { label: '待发货', value: 1, count: orders.value.filter(o => o.status === 1).length },
  { label: '运输中', value: 2, count: orders.value.filter(o => o.status === 2).length },
  { label: '已完成', value: 3, count: orders.value.filter(o => o.status === 3).length }
])

const orders = ref([])

const loadOrders = async () => {
  try {
    const res = await getOrderList()
    orders.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadOrders()
})

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

const handlePay = (order) => {
  ElMessage.info('支付功能开发中')
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

const handleConfirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmReceipt(order.id)
    ElMessage.success('已确认收货')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('确认失败')
  }
}

const goToReview = (order) => {
  router.push(`/order/${order.id}?action=review`)
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.orders-container {
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
  overflow-x: auto;
}

.tab-item {
  flex: 1;
  @include flex-center;
  gap: $spacing-1;
  padding: $spacing-3 $spacing-3;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-base;
  white-space: nowrap;

  &:hover { color: $color-text-primary; background: $neutral-50; }

  &.active {
    @include gradient-primary;
    color: #fff;
    box-shadow: $shadow-colored;
  }

  .tab-count {
    font-size: 11px;
    padding: 0 5px;
    border-radius: $radius-full;
    background: rgba(0, 0, 0, 0.08);

    .active & { background: rgba(255, 255, 255, 0.2); }
  }
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.order-card {
  @include card-base;
  padding: $spacing-5;
  cursor: pointer;
  animation: fadeInUp 0.4s ease-out both;

  &:hover { box-shadow: $shadow-md; }
}

.order-header {
  @include flex-between;
  margin-bottom: $spacing-4;
  padding-bottom: $spacing-3;
  border-bottom: 1px solid $color-border-light;

  .order-no {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  .order-status {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;

    &.status-pending_pay { color: $warning-500; }
    &.status-pending_ship { color: $info-500; }
    &.status-shipped { color: $color-primary; }
    &.status-completed { color: $success-500; }
    &.status-cancelled { color: $color-text-placeholder; }
  }
}

.order-body {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  margin-bottom: $spacing-4;
}

.product-thumb {
  width: 80px;
  height: 80px;
  border-radius: $radius-lg;
  overflow: hidden;
  flex-shrink: 0;
  background: $neutral-100;

  .thumb-placeholder {
    width: 100%;
    height: 100%;
    @include flex-center;
    background: linear-gradient(135deg, $neutral-50, $neutral-100);
    color: $neutral-400;
  }
}

.order-product-info {
  flex: 1;
  min-width: 0;

  .product-title {
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin: 0 0 $spacing-1;
    @include text-ellipsis;
  }

  .product-condition {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

.order-price-info {
  text-align: right;
  flex-shrink: 0;

  .order-price {
    display: block;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $danger-500;
  }

  .order-quantity {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

.order-footer {
  @include flex-between;
  padding-top: $spacing-3;
  border-top: 1px solid $color-border-light;

  .order-time {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  .order-actions {
    display: flex;
    gap: $spacing-2;
  }
}

.empty-state {
  @include flex-center;
  min-height: 400px;
}
</style>
