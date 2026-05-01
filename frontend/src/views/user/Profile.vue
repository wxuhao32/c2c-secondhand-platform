<template>
  <div class="profile-container">
    <PageHeader title="个人中心" />

    <main class="main-content">
      <div class="profile-hero">
        <div class="hero-bg"></div>
        <div class="user-card">
          <div class="user-info">
            <div class="avatar-wrapper" @click="showAvatarDialog = true">
              <el-avatar :size="72" :src="userInfo?.avatar" class="user-avatar">
                {{ userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-edit-badge">
                <el-icon :size="12"><Camera /></el-icon>
              </div>
            </div>
            <div class="user-detail">
              <h2 class="username">{{ userInfo?.nickname || userInfo?.username || '未设置昵称' }}</h2>
              <p class="user-id">ID: {{ userInfo?.userId }}</p>
              <p v-if="userInfo?.mobile" class="user-mobile">{{ formatMobile(userInfo.mobile) }}</p>
            </div>
            <el-button type="primary" text @click="editDialogVisible = true">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
          </div>
          <div class="user-stats">
            <div class="stat-item" @click="router.push('/my-products')">
              <span class="stat-value">{{ stats.published }}</span>
              <span class="stat-label">发布</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="router.push('/my-orders')">
              <span class="stat-value">{{ stats.onSale }}</span>
              <span class="stat-label">在售</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.sold }}</span>
              <span class="stat-label">已售</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="router.push('/favorites')">
              <span class="stat-value">{{ stats.favorites }}</span>
              <span class="stat-label">收藏</span>
            </div>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-header">
          <h3 class="card-title">基本信息</h3>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo?.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">昵称</span>
            <span class="info-value">{{ userInfo?.nickname || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ formatMobile(userInfo?.mobile) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo?.email || '未绑定' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(userInfo?.createTime) }}</span>
          </div>
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-section-title">交易管理</div>
        <div class="menu-list">
          <div class="menu-item" @click="router.push('/my-products')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #e0e7ff, #c7d2fe)">
              <el-icon><Goods /></el-icon>
            </div>
            <span class="menu-text">我的发布</span>
            <span class="menu-badge" v-if="stats.onSale">{{ stats.onSale }}件在售</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item" @click="router.push('/my-orders')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #fce7f3, #fbcfe8)">
              <el-icon><List /></el-icon>
            </div>
            <span class="menu-text">我的订单</span>
            <span class="menu-badge" v-if="stats.pendingOrders">{{ stats.pendingOrders }}笔待处理</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item" @click="router.push('/favorites')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #fef3c7, #fde68a)">
              <el-icon><Star /></el-icon>
            </div>
            <span class="menu-text">我的收藏</span>
            <span class="menu-badge">{{ stats.favorites }}件</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item" @click="router.push('/messages')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #ecfdf5, #a7f3d0)">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span class="menu-text">我的消息</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-section-title">账户设置</div>
        <div class="menu-list">
          <div class="menu-item" @click="router.push('/security')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #e0f2fe, #bae6fd)">
              <el-icon><Lock /></el-icon>
            </div>
            <span class="menu-text">账号安全</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item" @click="router.push('/address')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #d1fae5, #a7f3d0)">
              <el-icon><Location /></el-icon>
            </div>
            <span class="menu-text">收货地址</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item" @click="router.push('/settings')">
            <div class="menu-icon" style="background: linear-gradient(135deg, #f1f5f9, #e2e8f0)">
              <el-icon><Setting /></el-icon>
            </div>
            <span class="menu-text">系统设置</span>
            <el-icon class="menu-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <el-button type="danger" plain class="logout-button" @click="handleLogout">退出登录</el-button>
    </main>

    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="480px" :close-on-click-modal="false">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己吧" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAvatarDialog" title="修改头像" width="400px">
      <div class="avatar-upload-area">
        <div class="upload-preview">
          <el-avatar :size="100" :src="editForm.avatar || userInfo?.avatar">
            {{ userInfo?.username?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <el-upload
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
          :on-change="handleAvatarChange"
        >
          <el-button type="primary" plain>选择图片</el-button>
        </el-upload>
        <p class="upload-tip">支持 JPG、PNG 格式，大小不超过 2MB</p>
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAvatar" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { ArrowRight, Edit, Goods, List, Star, Location, Lock, Setting, Camera, ChatDotRound } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const authStore = useAuthStore()

const userInfo = ref(authStore.userInfo)
const editDialogVisible = ref(false)
const showAvatarDialog = ref(false)
const saving = ref(false)

const stats = reactive({
  published: 12,
  onSale: 8,
  sold: 4,
  favorites: 26,
  pendingOrders: 2
})

const editForm = reactive({
  nickname: '',
  email: '',
  bio: '',
  avatar: ''
})

const editFormRef = ref(null)

const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const formatMobile = (mobile) => {
  if (!mobile) return '未绑定'
  return mobile.substring(0, 3) + '****' + mobile.substring(7)
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        authStore.updateUserInfo(editForm)
        userInfo.value = authStore.userInfo
        editDialogVisible.value = false
        ElMessage.success('保存成功')
      } finally {
        saving.value = false
      }
    }
  })
}

const handleAvatarChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    editForm.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSaveAvatar = async () => {
  saving.value = true
  try {
    authStore.updateUserInfo({ avatar: editForm.avatar })
    userInfo.value = authStore.userInfo
    showAvatarDialog.value = false
    ElMessage.success('头像更新成功')
  } finally {
    saving.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {}
}

onMounted(() => {
  if (!userInfo.value) {
    authStore.initUserInfo()
    userInfo.value = authStore.userInfo
  }
  if (userInfo.value) {
    editForm.nickname = userInfo.value.nickname || userInfo.value.username || ''
    editForm.email = userInfo.value.email || ''
    editForm.bio = userInfo.value.bio || ''
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.profile-container {
  min-height: 100vh;
  background: $color-bg-page;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-6 $spacing-5;

  @include respond-to(md) {
    padding: $spacing-4;
  }
}

.profile-hero {
  position: relative;
  margin-bottom: $spacing-5;

  .hero-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100px;
    @include gradient-hero;
    border-radius: $radius-2xl $radius-2xl 0 0;
  }

  .user-card {
    position: relative;
    background: $color-bg-card;
    border-radius: $radius-2xl;
    box-shadow: $shadow-md;
    border: 1px solid $color-border-light;
    overflow: hidden;
    animation: fadeInUp 0.5s ease-out;
  }

  .user-info {
    padding: $spacing-8 $spacing-6 $spacing-5;
    display: flex;
    align-items: center;
    gap: $spacing-5;
  }

  .avatar-wrapper {
    position: relative;
    cursor: pointer;

    .user-avatar {
      border: 3px solid #fff;
      box-shadow: $shadow-md;
      font-size: $font-size-2xl;
    }

    .avatar-edit-badge {
      position: absolute;
      bottom: 0;
      right: 0;
      @include flex-center;
      width: 24px;
      height: 24px;
      border-radius: $radius-full;
      background: $color-primary;
      color: #fff;
      border: 2px solid #fff;
    }
  }

  .user-detail {
    flex: 1;

    .username {
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 $spacing-1;
    }

    .user-id, .user-mobile {
      font-size: $font-size-sm;
      color: $color-text-placeholder;
      margin: 0;
    }

    .user-mobile {
      margin-top: 2px;
    }
  }

  .user-stats {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: $spacing-5 $spacing-6;
    border-top: 1px solid $color-border-light;
  }

  .stat-item {
    @include flex-column-center;
    gap: $spacing-1;
    cursor: pointer;
    transition: all $transition-fast;

    &:hover .stat-value {
      color: $color-primary;
    }

    .stat-value {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      transition: color $transition-fast;
    }

    .stat-label {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }

  .stat-divider {
    width: 1px;
    height: 28px;
    background: $color-border-light;
  }
}

.info-card {
  @include card-base;
  padding: $spacing-6;
  margin-bottom: $spacing-5;
  animation: fadeInUp 0.5s ease-out 0.1s both;

  .card-header {
    @include flex-between;
    margin-bottom: $spacing-4;
  }

  .card-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0;
  }

  .info-list {
    .info-item {
      display: flex;
      padding: $spacing-3 0;
      border-bottom: 1px solid $color-border-light;

      &:last-child { border-bottom: none; }

      .info-label {
        width: 100px;
        color: $color-text-secondary;
        font-size: $font-size-sm;
        flex-shrink: 0;
      }

      .info-value {
        flex: 1;
        color: $color-text-primary;
        font-size: $font-size-sm;
      }
    }
  }
}

.menu-card {
  @include card-base;
  padding: $spacing-4 $spacing-3;
  margin-bottom: $spacing-5;
  animation: fadeInUp 0.5s ease-out 0.2s both;

  .menu-section-title {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    padding: $spacing-1 $spacing-3;
    margin-bottom: $spacing-2;
  }

  .menu-list {
    .menu-item {
      display: flex;
      align-items: center;
      padding: $spacing-3;
      cursor: pointer;
      border-radius: $radius-lg;
      transition: all $transition-base;

      &:not(:last-child) { margin-bottom: $spacing-1; }
      &:hover { background: $neutral-50; }
      &:active { background: $neutral-100; }

      .menu-icon {
        @include flex-center;
        width: 40px;
        height: 40px;
        border-radius: $radius-lg;
        margin-right: $spacing-3;
        flex-shrink: 0;

        .el-icon { color: $color-primary; font-size: 18px; }
      }

      .menu-text {
        flex: 1;
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
        color: $color-text-primary;
      }

      .menu-badge {
        font-size: $font-size-xs;
        color: $color-text-placeholder;
        margin-right: $spacing-2;
      }

      .menu-arrow {
        color: $color-text-placeholder;
        font-size: 14px;
      }
    }
  }
}

.logout-button {
  width: 100%;
  height: 48px;
  border-radius: $radius-lg;
  font-weight: $font-weight-medium;
  animation: fadeInUp 0.5s ease-out 0.3s both;
}

.avatar-upload-area {
  @include flex-column-center;
  gap: $spacing-4;
  padding: $spacing-6 0;
}

.upload-preview {
  .el-avatar {
    border: 3px solid $color-border-light;
    font-size: $font-size-3xl;
  }
}

.upload-tip {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  margin: 0;
}
</style>
