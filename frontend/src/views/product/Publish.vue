<template>
  <div class="publish-container">
    <PageHeader :title="isEdit ? '编辑商品' : '发布闲置'" />

    <main class="main-content">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="publish-form">
        <div class="form-section">
          <h3 class="section-title">商品图片</h3>
          <p class="section-desc">最多上传9张图片，第一张为封面图</p>
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="9"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            accept="image/*"
          >
            <el-icon :size="24"><Plus /></el-icon>
          </el-upload>
        </div>

        <div class="form-section">
          <h3 class="section-title">商品信息</h3>
          <el-form-item label="商品标题" prop="title">
            <el-input v-model="form.title" placeholder="请描述商品品牌、型号、关键特征" maxlength="30" show-word-limit />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="商品分类" prop="category" class="form-col">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="cat in categoryOptions" :key="cat.value" :label="cat.label" :value="cat.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="成色" prop="condition" class="form-col">
              <el-select v-model="form.condition" placeholder="请选择成色" style="width: 100%">
                <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="售价" prop="price" class="form-col">
              <el-input v-model="form.price" placeholder="请输入售价" type="number">
                <template #prefix>¥</template>
              </el-input>
            </el-form-item>
            <el-form-item label="原价" class="form-col">
              <el-input v-model="form.originalPrice" placeholder="选填" type="number">
                <template #prefix>¥</template>
              </el-input>
            </el-form-item>
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">商品描述</h3>
          <el-form-item prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="6"
              placeholder="请详细描述商品的使用情况、购买时间、转手原因等信息，让买家更了解你的宝贝"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>

        <div class="form-section">
          <h3 class="section-title">交易方式</h3>
          <el-form-item prop="tradeMethods">
            <el-checkbox-group v-model="form.tradeMethods">
              <el-checkbox label="express">快递邮寄</el-checkbox>
              <el-checkbox label="self_pickup">同城自提</el-checkbox>
              <el-checkbox label="face_trade">当面交易</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item v-if="form.tradeMethods.includes('express')" label="运费">
            <el-radio-group v-model="form.shippingFee">
              <el-radio label="seller">卖家承担运费</el-radio>
              <el-radio label="buyer">买家承担运费</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '发布商品' }}
          </el-button>
        </div>
      </el-form>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)

const isEdit = computed(() => !!route.query.id)

const categoryOptions = [
  { label: '手机数码', value: 'digital' },
  { label: '电脑办公', value: 'computer' },
  { label: '智能手表', value: 'watch' },
  { label: '运动户外', value: 'sports' },
  { label: '图书文具', value: 'book' },
  { label: '服饰鞋包', value: 'clothing' },
  { label: '美妆护肤', value: 'beauty' },
  { label: '家居生活', value: 'home' },
  { label: '母婴用品', value: 'baby' },
  { label: '其他', value: 'other' }
]

const conditionOptions = [
  { label: '全新未拆', value: 'new' },
  { label: '几乎全新', value: 'like_new' },
  { label: '9成新', value: 'excellent' },
  { label: '8成新', value: 'good' },
  { label: '7成新及以下', value: 'fair' }
]

const form = reactive({
  title: '',
  category: '',
  condition: '',
  price: '',
  originalPrice: '',
  description: '',
  tradeMethods: ['express'],
  shippingFee: 'seller',
  images: []
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 5, max: 30, message: '标题长度为5-30个字符', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择商品成色', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ],
  tradeMethods: [{ type: 'array', required: true, message: '请选择交易方式', trigger: 'change' }]
}

const handleImageChange = (file) => {
  form.images.push(file)
}

const handleImageRemove = (file) => {
  form.images = form.images.filter(f => f.uid !== file.uid)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        ElMessage.success(isEdit.value ? '商品修改成功' : '商品发布成功')
        router.push('/my-products')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.publish-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 720px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) { padding: $spacing-4; }
}

.publish-form {
  :deep(.el-form-item__label) {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }
}

.form-section {
  @include card-base;
  padding: $spacing-6;
  margin-bottom: $spacing-5;
  animation: fadeInUp 0.5s ease-out both;

  &:nth-child(2) { animation-delay: 0.05s; }
  &:nth-child(3) { animation-delay: 0.1s; }
  &:nth-child(4) { animation-delay: 0.15s; }

  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 $spacing-1;
  }

  .section-desc {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    margin: 0 0 $spacing-4;
  }
}

.form-row {
  display: flex;
  gap: $spacing-4;

  @include respond-to(sm) {
    flex-direction: column;
    gap: 0;
  }
}

.form-col {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
  padding: $spacing-4 0;
}
</style>
