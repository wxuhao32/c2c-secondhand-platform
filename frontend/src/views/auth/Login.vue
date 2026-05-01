<template>
  <div class="login-page">
    <div class="login-left">
      <div class="left-content">
        <div class="brand">
          <div class="brand-icon">
            <el-icon :size="28"><Shop /></el-icon>
          </div>
          <span class="brand-name">闲置好物</span>
        </div>
        <h1 class="left-title">买卖闲置<br/>环保生活</h1>
        <p class="left-desc">让每一件闲置物品找到新的归宿，让可持续消费成为日常</p>
        <div class="left-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>品质验机，安心购买</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>极速发货，体验无忧</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>担保交易，资金安全</span>
          </div>
        </div>
      </div>
      <div class="left-decoration">
        <div class="deco-circle deco-1"></div>
        <div class="deco-circle deco-2"></div>
        <div class="deco-circle deco-3"></div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录你的账号继续探索</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="请输入用户名/手机/邮箱"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captchaCode"
                placeholder="请输入验证码"
                size="large"
                prefix-icon="CircleCheck"
                style="flex: 1"
              />
              <div class="captcha-image" @click="refreshCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                <span v-else class="captcha-loading">{{ captchaError ? '点击刷新' : '加载中...' }}</span>
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="divider">
          <span>其他登录方式</span>
        </div>

        <div class="social-login">
          <button class="social-btn wechat-btn" @click="handleWechatLogin">
            <svg class="social-icon" viewBox="0 0 24 24" width="22" height="22">
              <path fill="#07C160" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
            </svg>
          </button>
          <button class="social-btn sms-btn" @click="handleSmsLogin">
            <el-icon :size="22"><Message /></el-icon>
          </button>
        </div>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" :underline="false" @click="goToRegister">立即注册</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck, Message, Shop } from '@element-plus/icons-vue'
import { login, getCaptcha } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const captchaError = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
  captchaCode: '',
  rememberMe: false
})

const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 5, message: '验证码长度为5位', trigger: 'blur' }
  ]
}

const refreshCaptcha = async () => {
  try {
    captchaError.value = false
    const response = await getCaptcha()
    captchaImage.value = response.data.image
    captchaKey.value = response.data.key
  } catch (error) {
    captchaError.value = true
    captchaImage.value = ''
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await login({
        account: loginForm.account,
        password: loginForm.password,
        captchaKey: captchaKey.value,
        captchaCode: loginForm.captchaCode,
        rememberMe: loginForm.rememberMe
      })

      authStore.setToken(response.data.token)
      authStore.setUserInfo(response.data.userInfo)

      ElMessage.success('登录成功')

      const redirect = route.query.redirect || '/home'
      router.replace(redirect)
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}

const handleWechatLogin = () => {
  ElMessage.info('微信登录功能开发中')
}

const handleSmsLogin = () => {
  router.push('/login-sms')
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.login-page {
  min-height: 100vh;
  display: flex;
  background: $color-bg-page;
}

.login-left {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  @include gradient-hero;
  overflow: hidden;
  padding: $spacing-12;

  @include respond-to(lg) {
    display: none;
  }

  .left-content {
    position: relative;
    z-index: 2;
    max-width: 460px;
    animation: fadeInUp 0.6s ease-out;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    margin-bottom: $spacing-12;

    .brand-icon {
      @include flex-center;
      width: 44px;
      height: 44px;
      border-radius: $radius-lg;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(8px);
      color: #fff;
    }

    .brand-name {
      font-family: $font-family-display;
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: #fff;
    }
  }

  .left-title {
    font-size: 48px;
    font-weight: $font-weight-bold;
    color: #fff;
    line-height: 1.2;
    margin-bottom: $spacing-4;
    letter-spacing: -0.03em;
  }

  .left-desc {
    font-size: $font-size-lg;
    color: rgba(255, 255, 255, 0.8);
    line-height: $line-height-relaxed;
    margin-bottom: $spacing-10;
  }

  .left-features {
    display: flex;
    flex-direction: column;
    gap: $spacing-4;
  }

  .feature-item {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    color: rgba(255, 255, 255, 0.9);
    font-size: $font-size-md;

    .feature-dot {
      width: 8px;
      height: 8px;
      border-radius: $radius-full;
      background: rgba(255, 255, 255, 0.6);
      flex-shrink: 0;
    }
  }

  .left-decoration {
    position: absolute;
    inset: 0;
    pointer-events: none;

    .deco-circle {
      position: absolute;
      border-radius: 50%;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .deco-1 {
      width: 500px;
      height: 500px;
      bottom: -150px;
      right: -100px;
    }

    .deco-2 {
      width: 300px;
      height: 300px;
      top: -80px;
      right: 20%;
    }

    .deco-3 {
      width: 200px;
      height: 200px;
      bottom: 20%;
      left: -60px;
      background: rgba(255, 255, 255, 0.03);
    }
  }
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-8;
  min-height: 100vh;

  @include respond-to(lg) {
    flex: none;
    width: 100%;
  }

  @include respond-to(sm) {
    padding: $spacing-5;
  }
}

.login-box {
  width: 100%;
  max-width: 420px;
  animation: fadeInUp 0.5s ease-out;
}

.login-header {
  margin-bottom: $spacing-8;

  .login-title {
    font-size: $font-size-4xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 $spacing-2;
  }

  .login-subtitle {
    font-size: $font-size-md;
    color: $color-text-secondary;
    margin: 0;
  }
}

.login-form {
  .captcha-row {
    display: flex;
    gap: $spacing-3;
    width: 100%;
  }

  .captcha-image {
    width: 120px;
    height: 40px;
    border-radius: $radius-md;
    overflow: hidden;
    cursor: pointer;
    flex-shrink: 0;
    border: 1px solid $color-border;
    transition: border-color $transition-base;

    &:hover {
      border-color: $color-primary;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .captcha-loading {
      @include flex-center;
      width: 100%;
      height: 100%;
      background: $neutral-50;
      color: $color-text-placeholder;
      font-size: $font-size-xs;
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .login-button {
    width: 100%;
    height: 48px;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    border-radius: $radius-lg;
    letter-spacing: $letter-spacing-wide;
  }
}

.divider {
  display: flex;
  align-items: center;
  margin: $spacing-6 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: $color-border-light;
  }

  span {
    padding: 0 $spacing-4;
    color: $color-text-placeholder;
    font-size: $font-size-xs;
  }
}

.social-login {
  display: flex;
  justify-content: center;
  gap: $spacing-4;

  .social-btn {
    @include flex-center;
    width: 52px;
    height: 52px;
    border-radius: $radius-xl;
    border: 1px solid $color-border;
    background: $color-bg-card;
    cursor: pointer;
    transition: all $transition-base;

    &:hover {
      border-color: $color-primary;
      background: $primary-50;
      transform: translateY(-2px);
      box-shadow: $shadow-md;
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.login-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: $spacing-2;
  margin-top: $spacing-8;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}
</style>
