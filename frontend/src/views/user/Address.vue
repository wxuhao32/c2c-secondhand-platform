<template>
  <div class="address-container">
    <PageHeader title="收货地址">
      <template #actions>
        <el-button type="primary" @click="openAddressDialog()">
          <el-icon><Plus /></el-icon>
          新增地址
        </el-button>
      </template>
    </PageHeader>

    <main class="main-content">
      <div v-if="addressList.length" class="address-list">
        <div
          v-for="addr in addressList"
          :key="addr.id"
          class="address-card"
          :class="{ 'is-default': addr.isDefault }"
        >
          <div class="address-main">
            <div class="address-header">
              <span class="receiver-name">{{ addr.receiver }}</span>
              <span class="receiver-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" type="primary" size="small" effect="dark">默认</el-tag>
            </div>
            <p class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</p>
          </div>
          <div class="address-actions">
            <el-button text size="small" @click="openAddressDialog(addr)">编辑</el-button>
            <el-button v-if="!addr.isDefault" text size="small" @click="setDefault(addr.id)">设为默认</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(addr.id)">删除</el-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="暂无收货地址">
          <el-button type="primary" @click="openAddressDialog()">添加地址</el-button>
        </el-empty>
      </div>
    </main>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '新增地址'" width="500px" :close-on-click-modal="false">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="addressForm.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="省/市/区" prop="region">
          <el-input v-model="addressForm.region" placeholder="请输入省/市/区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const addressFormRef = ref(null)

const addressList = ref([
  { id: 1, receiver: '张三', phone: '138****1234', province: '北京市', city: '北京市', district: '朝阳区', detail: '望京SOHO T3 2801室', isDefault: true },
  { id: 2, receiver: '李四', phone: '139****5678', province: '上海市', city: '上海市', district: '浦东新区', detail: '张江高科技园区博云路2号', isDefault: false }
])

const addressForm = reactive({
  id: null,
  receiver: '',
  phone: '',
  region: '',
  detail: '',
  isDefault: false
})

const addressRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请输入省/市/区', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const openAddressDialog = (addr) => {
  isEdit.value = !!addr
  if (addr) {
    Object.assign(addressForm, {
      id: addr.id,
      receiver: addr.receiver,
      phone: addr.phone,
      region: `${addr.province}${addr.city}${addr.district}`,
      detail: addr.detail,
      isDefault: addr.isDefault
    })
  } else {
    Object.assign(addressForm, { id: null, receiver: '', phone: '', region: '', detail: '', isDefault: false })
  }
  dialogVisible.value = true
}

const handleSaveAddress = async () => {
  if (!addressFormRef.value) return
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        ElMessage.success(isEdit.value ? '地址修改成功' : '地址添加成功')
        dialogVisible.value = false
      } finally {
        saving.value = false
      }
    }
  })
}

const setDefault = (id) => {
  addressList.value.forEach(a => a.isDefault = a.id === id)
  ElMessage.success('已设为默认地址')
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', { type: 'warning' })
    addressList.value = addressList.value.filter(a => a.id !== id)
    ElMessage.success('地址已删除')
  } catch {}
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.address-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) { padding: $spacing-4; }
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.address-card {
  @include card-base;
  padding: $spacing-5;
  transition: all $transition-base;

  &.is-default {
    border-color: $primary-200;
    background: $primary-50;
  }

  &:hover {
    box-shadow: $shadow-md;
  }
}

.address-main {
  margin-bottom: $spacing-3;
}

.address-header {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-2;

  .receiver-name {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  .receiver-phone {
    font-size: $font-size-md;
    color: $color-text-secondary;
  }
}

.address-detail {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: $line-height-relaxed;
  margin: 0;
}

.address-actions {
  display: flex;
  gap: $spacing-2;
  padding-top: $spacing-3;
  border-top: 1px solid $color-border-light;
}

.empty-state {
  @include flex-center;
  min-height: 400px;
}
</style>
