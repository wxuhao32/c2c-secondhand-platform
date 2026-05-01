<template>
  <div class="security-container">
    <PageHeader title="账号安全" />

    <main class="main-content">
      <div class="security-card">
        <div class="security-item">
          <div class="item-left">
            <div class="item-icon" style="background: linear-gradient(135deg, #e0e7ff, #c7d2fe)">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="item-info">
              <h3 class="item-title">登录密码</h3>
              <p class="item-desc">定期更换密码有助于保护账号安全</p>
            </div>
          </div>
          <div class="item-right">
            <el-tag type="success" size="small">已设置</el-tag>
            <el-button type="primary" text @click="passwordDialogVisible = true">修改</el-button>
          </div>
        </div>

        <div class="security-item">
          <div class="item-left">
            <div class="item-icon" style="background: linear-gradient(135deg, #fce7f3, #fbcfe8)">
              <el-icon><Phone /></el-icon>
            </div>
            <div class="item-info">
              <h3 class="item-title">手机绑定</h3>
              <p class="item-desc">绑定手机号可用于登录和找回密码</p>
            </div>
          </div>
          <div class="item-right">
            <el-tag type="success" size="small">{{ formatMobile(userInfo?.mobile) }}</el-tag>
            <el-button type="primary" text>修改</el-button>
          </div>
        </div>

        <div class="security-item">
          <div class="item-left">
            <div class="item-icon" style="background: linear-gradient(135deg, #d1fae5, #a7f3d0)">
              <el-icon><Message /></el-icon>
            </div>
            <div class="item-info">
              <h3 class="item-title">邮箱绑定</h3>
              <p class="item-desc">绑定邮箱可用于接收通知和找回密码</p>
            </div>
          </div>
          <div class="item-right">
            <el-tag v-if="userInfo?.email" type="success" size="small">{{ userInfo.email }}</el-tag>
            <el-tag v-else type="info" size="small">未绑定</el-tag>
            <el-button type="primary" text>{{ userInfo?.email ? '修改' : '绑定' }}</el-button>
          </div>
        </div>

        <div class="security-item">
          <div class="item-left">
            <div class="item-icon" style="background: linear-gradient(135deg, #fef3c7, #fde68a)">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="item-info">
              <h3 class="item-title">微信绑定</h3>
              <p class="item-desc">绑定微信可使用微信快捷登录</p>
            </div>
          </div>
          <div class="item-right">
            <el-tag type="info" size="small">未绑定</el-tag>
            <el-button type="primary" text>绑定</el-button>
          </div>
        </div>
      </div>

      <div class="security-card">
        <div class="card-section-title">登录记录</div>
        <div class="login-records">
          <div v-for="record in loginRecords" :key="record.id" class="record-item">
            <div class="record-left">
              <el-icon :size="16" class="record-icon"><Monitor /></el-icon>
              <div class="record-info">
                <span class="record-device">{{ record.device }}</span>
                <span class="record-ip">IP: {{ record.ip }}</span>
              </div>
            </div>
            <div class="record-right">
              <span class="record-time">{{ record.time }}</span>
              <el-tag v-if="record.current" type="success" size="small">当前设备</el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="danger-zone">
        <h3 class="danger-title">危险操作</h3>
        <div class="danger-item">
          <div class="danger-info">
            <span class="danger-text">注销账号</span>
            <span class="danger-desc">注销后数据不可恢复，请谨慎操作</span>
          </div>
          <el-button type="danger" plain size="small">注销账号</el-button>
        </div>
      </div>
    </main>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="440px" :close-on-click-modal="false">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changing">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { Lock, Phone, Message, ChatDotRound, Monitor } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const authStore = useAuthStore()
const userInfo = ref(authStore.userInfo)
const passwordDialogVisible = ref(false)
const changing = ref(false)
const passwordFormRef = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const loginRecords = ref([
  { id: 1, device: 'Chrome · Windows 10', ip: '192.168.1.***', time: '2024-01-15 10:30', current: true },
  { id: 2, device: 'Safari · iPhone 15', ip: '10.0.0.***', time: '2024-01-14 18:22', current: false },
  { id: 3, device: 'Chrome · macOS', ip: '172.16.0.***', time: '2024-01-13 09:15', current: false }
])

const formatMobile = (mobile) => {
  if (!mobile) return '未绑定'
  return mobile.substring(0, 3) + '****' + mobile.substring(7)
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changing.value = true
      try {
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } finally {
        changing.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.security-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) { padding: $spacing-4; }
}

.security-card {
  @include card-base;
  padding: $spacing-2;
  margin-bottom: $spacing-5;
  animation: fadeInUp 0.5s ease-out;

  .card-section-title {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    padding: $spacing-3 $spacing-4;
    margin-bottom: $spacing-1;
  }
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4;
  border-radius: $radius-lg;
  transition: background $transition-fast;

  &:not(:last-child) {
    border-bottom: 1px solid $color-border-light;
  }

  &:hover {
    background: $neutral-50;
  }

  .item-left {
    display: flex;
    align-items: center;
    gap: $spacing-4;
    flex: 1;
  }

  .item-icon {
    @include flex-center;
    width: 44px;
    height: 44px;
    border-radius: $radius-lg;
    flex-shrink: 0;

    .el-icon { color: $color-primary; font-size: 20px; }
  }

  .item-info {
    .item-title {
      font-size: $font-size-md;
      font-weight: $font-weight-medium;
      color: $color-text-primary;
      margin: 0 0 2px;
    }

    .item-desc {
      font-size: $font-size-sm;
      color: $color-text-placeholder;
      margin: 0;
    }
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;
  }
}

.login-records {
  padding: 0 $spacing-2;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $spacing-2;
  border-bottom: 1px solid $color-border-light;

  &:last-child { border-bottom: none; }

  .record-left {
    display: flex;
    align-items: center;
    gap: $spacing-3;
  }

  .record-icon { color: $color-text-placeholder; }

  .record-info {
    @include flex-column-center;
    align-items: flex-start;
    gap: 2px;

    .record-device {
      font-size: $font-size-sm;
      color: $color-text-primary;
    }

    .record-ip {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }

  .record-right {
    display: flex;
    align-items: center;
    gap: $spacing-2;

    .record-time {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }
}

.danger-zone {
  @include card-base;
  padding: $spacing-5;
  border-color: $danger-100;
  animation: fadeInUp 0.5s ease-out 0.2s both;

  .danger-title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $danger-500;
    margin: 0 0 $spacing-4;
  }

  .danger-item {
    @include flex-between;

    .danger-info {
      .danger-text {
        font-size: $font-size-md;
        color: $color-text-primary;
        display: block;
      }

      .danger-desc {
        font-size: $font-size-xs;
        color: $color-text-placeholder;
        margin-top: 2px;
        display: block;
      }
    }
  }
}
</style>
