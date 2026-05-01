<template>
  <div class="order-detail-container">
    <PageHeader title="订单详情" />

    <main class="main-content">
      <div class="status-banner" :class="`status-${order.status}`">
        <div class="status-icon">
          <el-icon :size="32">
            <component :is="statusIcon" />
          </el-icon>
        </div>
        <div class="status-info">
          <h2 class="status-text">{{ statusMap[order.status] }}</h2>
          <p class="status-desc">{{ statusDescMap[order.status] }}</p>
        </div>
      </div>

      <div v-if="order.status === 'shipped' || order.status === 'completed'" class="logistics-card">
        <div class="card-title">
          <el-icon><Van /></el-icon>
          物流信息
        </div>
        <div class="logistics-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in logistics"
              :key="index"
              :timestamp="item.time"
              :type="index === 0 ? 'primary' : 'info'"
              :hollow="index !== 0"
            >
              {{ item.desc }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <div class="product-card">
        <div class="card-title">
          <el-icon><Goods /></el-icon>
          商品信息
        </div>
        <div class="product-detail">
          <div class="product-thumb">
            <div class="thumb-placeholder"><el-icon :size="24"><Goods /></el-icon></div>
          </div>
          <div class="product-info">
            <h4 class="product-title">{{ order.productTitle }}</h4>
            <span class="product-condition">{{ order.condition }}</span>
          </div>
          <div class="product-price">
            <span class="price">¥{{ order.price }}</span>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-title">
          <el-icon><Document /></el-icon>
          订单信息
        </div>
        <div class="info-list">
          <div class="info-row">
            <span class="info-label">订单编号</span>
            <span class="info-value">{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">下单时间</span>
            <span class="info-value">{{ order.createdAt }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">支付方式</span>
            <span class="info-value">{{ order.payMethod || '在线支付' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">商品金额</span>
            <span class="info-value price">¥{{ order.price }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">运费</span>
            <span class="info-value">{{ order.shippingFee === '0' ? '免运费' : '¥' + order.shippingFee }}</span>
          </div>
          <div class="info-row total">
            <span class="info-label">实付金额</span>
            <span class="info-value total-price">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>

      <div class="address-card">
        <div class="card-title">
          <el-icon><Location /></el-icon>
          收货信息
        </div>
        <div class="address-info">
          <div class="address-header">
            <span class="receiver">{{ order.receiver }}</span>
            <span class="phone">{{ order.receiverPhone }}</span>
          </div>
          <p class="address-detail">{{ order.receiverAddress }}</p>
        </div>
      </div>

      <div class="action-bar">
        <el-button v-if="order.status === 'pending_pay'" type="primary" size="large" @click="handlePay">立即付款</el-button>
        <el-button v-if="order.status === 'pending_pay'" size="large" @click="handleCancel">取消订单</el-button>
        <el-button v-if="order.status === 'shipped'" type="primary" size="large" @click="handleConfirmReceipt">确认收货</el-button>
        <el-button v-if="order.status === 'completed' && !order.hasReview" type="primary" size="large" @click="reviewDialogVisible = true">评价商品</el-button>
      </div>
    </main>

    <el-dialog v-model="reviewDialogVisible" title="评价商品" width="500px">
      <el-form :model="reviewForm" label-position="top">
        <el-form-item label="商品评分">
          <el-rate v-model="reviewForm.rating" :texts="['很差', '较差', '一般', '满意', '非常满意']" show-text />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="分享你的购买体验吧" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview" :loading="submittingReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods, Van, Location, Document, Clock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)

const statusMap = {
  pending_pay: '待付款',
  pending_ship: '待发货',
  shipped: '运输中',
  completed: '已完成',
  cancelled: '已取消'
}

const statusDescMap = {
  pending_pay: '请在30分钟内完成付款，超时订单将自动取消',
  pending_ship: '卖家正在准备发货，请耐心等待',
  shipped: '商品正在配送中，请注意查收',
  completed: '交易已完成，感谢您的购买',
  cancelled: '订单已取消'
}

const statusIcon = computed(() => {
  const map = { pending_pay: Clock, pending_ship: Van, shipped: Van, completed: CircleCheck, cancelled: CircleClose }
  return map[order.status] || Clock
})

const order = reactive({
  id: 1,
  orderNo: 'ORD20240115001',
  productTitle: '二手iPhone 13 Pro 256G 国行在保',
  condition: '9成新',
  price: '3999',
  totalAmount: '3999',
  shippingFee: '0',
  status: 'shipped',
  payMethod: '微信支付',
  createdAt: '2024-01-15 10:30:25',
  receiver: '张三',
  receiverPhone: '138****1234',
  receiverAddress: '北京市朝阳区望京SOHO T3 2801室',
  hasReview: false
})

const logistics = ref([
  { time: '2024-01-16 08:30', desc: '快递员正在派送中，请保持电话畅通' },
  { time: '2024-01-16 06:15', desc: '到达 北京朝阳望京营业部' },
  { time: '2024-01-15 22:00', desc: '已到达 北京转运中心' },
  { time: '2024-01-15 14:30', desc: '已从 深圳转运中心 发出' },
  { time: '2024-01-14 20:00', desc: '卖家已发货，顺丰快递 单号：SF1234567890' }
])

const handlePay = () => { ElMessage.info('支付功能开发中') }

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    order.status = 'cancelled'
    ElMessage.success('订单已取消')
  } catch {}
}

const handleConfirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？确认后款项将支付给卖家', '确认收货', { type: 'info' })
    order.status = 'completed'
    ElMessage.success('已确认收货')
  } catch {}
}

const handleSubmitReview = async () => {
  submittingReview.value = true
  try {
    order.hasReview = true
    reviewDialogVisible.value = false
    ElMessage.success('评价提交成功')
  } finally {
    submittingReview.value = false
  }
}

const reviewForm = reactive({
  rating: 5,
  content: ''
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.order-detail-container {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 100px;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) { padding: $spacing-4; }
}

.status-banner {
  display: flex;
  align-items: center;
  gap: $spacing-5;
  padding: $spacing-6;
  border-radius: $radius-2xl;
  margin-bottom: $spacing-5;
  color: #fff;
  animation: fadeInUp 0.5s ease-out;

  &.status-pending_pay { @include gradient-hero; }
  &.status-pending_ship { background: linear-gradient(135deg, $info-500, $info-600); }
  &.status-shipped { background: linear-gradient(135deg, $primary-500, $primary-700); }
  &.status-completed { background: linear-gradient(135deg, $success-500, $success-600); }
  &.status-cancelled { background: linear-gradient(135deg, $neutral-400, $neutral-500); }

  .status-icon {
    @include flex-center;
    width: 56px;
    height: 56px;
    border-radius: $radius-xl;
    background: rgba(255, 255, 255, 0.2);
    flex-shrink: 0;
  }

  .status-text {
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    margin: 0 0 $spacing-1;
  }

  .status-desc {
    font-size: $font-size-sm;
    opacity: 0.85;
    margin: 0;
  }
}

.logistics-card, .product-card, .info-card, .address-card {
  @include card-base;
  padding: $spacing-5;
  margin-bottom: $spacing-4;
  animation: fadeInUp 0.5s ease-out 0.1s both;

  .card-title {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $spacing-4;
    padding-bottom: $spacing-3;
    border-bottom: 1px solid $color-border-light;

    .el-icon { color: $color-primary; }
  }
}

.product-detail {
  display: flex;
  align-items: center;
  gap: $spacing-4;
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

.product-info {
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

.product-price .price {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $danger-500;
}

.info-list {
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: $spacing-2 0;

    &:not(:last-child) { border-bottom: 1px solid $color-border-light; }

    .info-label {
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }

    .info-value {
      color: $color-text-primary;
      font-size: $font-size-sm;

      &.price { color: $danger-500; }
    }

    &.total {
      padding-top: $spacing-3;

      .info-label { font-weight: $font-weight-semibold; color: $color-text-primary; }
      .total-price { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $danger-500; }
    }
  }
}

.address-info {
  .address-header {
    display: flex;
    gap: $spacing-3;
    margin-bottom: $spacing-2;

    .receiver { font-weight: $font-weight-semibold; color: $color-text-primary; }
    .phone { color: $color-text-secondary; }
  }

  .address-detail {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: $line-height-relaxed;
    margin: 0;
  }
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include glass-effect(0.95);
  padding: $spacing-4 $spacing-6;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
  border-top: 1px solid $color-border-light;
  z-index: $z-sticky;
}
</style>
