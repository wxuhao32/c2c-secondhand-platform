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
        <h1 class="left-title">快速登录<br/>安全便捷</h1>
        <p class="left-desc">只需手机号和验证码，即可快速登录，无需记忆复杂密码</p>
        <div class="left-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>短信验证，安全可靠</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>一键登录，省时省力</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>新用户自动注册</span>
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
          <h2 class="login-title">短信登录</h2>
          <p class="login-subtitle">输入手机号快速登录</p>
        </div>

        <el-form
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
          class="login-form"
          @keyup.enter="handleSmsLogin"
        >
          <el-form-item prop="mobile">
            <el-input
              v-model="smsForm.mobile"
              placeholder="请输入手机号"
              size="large"
              prefix-icon="Phone"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="smsForm.captchaCode"
                placeholder="请输入图形验证码"
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

          <el-form-item prop="smsCode">
            <div class="sms-code-row">
              <el-input
                v-model="smsForm.smsCode"
                placeholder="请输入短信验证码"
                size="large"
                prefix-icon="Key"
                maxlength="6"
                style="flex: 1"
              />
              <el-button
                :disabled="smsCountdown > 0"
                class="sms-button"
                @click="handleSendSms"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="smsForm.rememberMe">记住我</el-checkbox>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleSmsLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <el-link type="primary" :underline="false" @click="goToAccountLogin">
            <el-icon><ArrowLeft /></el-icon>
            账号密码登录
          </el-link>
          <el-link type="primary" :underline="false" @click="goToRegister">立即注册</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, CircleCheck, Key, ArrowLeft, Shop } from '@element-plus/icons-vue'
import { sendSms, loginBySms, getCaptcha } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const smsFormRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const captchaError = ref(false)
const smsCountdown = ref(0)
let countdownTimer = null

const smsForm = reactive({
  mobile: '',
  captchaCode: '',
  smsCode: '',
  rememberMe: false
})

const smsRules = {
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { len: 5, message: '验证码长度为5位', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
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

const handleSendSms = async () => {
  if (!/^1[3-9]\d{9}$/.test(smsForm.mobile)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  if (!smsForm.captchaCode) {
    ElMessage.warning('请输入图形验证码')
    return
  }

  try {
    await sendSms({
      mobile: smsForm.mobile,
      captchaKey: captchaKey.value,
      captchaCode: smsForm.captchaCode
    })

    ElMessage.success('短信验证码已发送')

    smsCountdown.value = 60
    countdownTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
    refreshCaptcha()
  }
}

const handleSmsLogin = async () => {
  if (!smsFormRef.value) return

  await smsFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await loginBySms({
        mobile: smsForm.mobile,
        smsCode: smsForm.smsCode,
        rememberMe: smsForm.rememberMe
      })

      authStore.setToken(response.data.token)
      authStore.setUserInfo(response.data.userInfo)

      ElMessage.success(response.code === 201 ? '注册并登录成功' : '登录成功')

      router.push('/home')
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

const goToAccountLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

refreshCaptcha()
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
  .captcha-row,
  .sms-code-row {
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

  .sms-button {
    width: 120px;
    flex-shrink: 0;
    border-radius: $radius-md;
    font-weight: $font-weight-medium;
  }

  .form-options {
    display: flex;
    align-items: center;
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

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: $spacing-8;
}
</style>
