<template>
  <div class="register-page">
    <div class="register-left">
      <div class="left-content">
        <div class="brand">
          <div class="brand-icon">
            <el-icon :size="28"><Shop /></el-icon>
          </div>
          <span class="brand-name">闲置好物</span>
        </div>
        <h1 class="left-title">加入我们<br/>开启闲置之旅</h1>
        <p class="left-desc">注册成为会员，享受安全便捷的二手交易体验</p>
        <div class="left-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>免费注册，即刻开始</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>实名认证，交易更安心</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>专属优惠，新人礼包</span>
          </div>
        </div>
      </div>
      <div class="left-decoration">
        <div class="deco-circle deco-1"></div>
        <div class="deco-circle deco-2"></div>
        <div class="deco-circle deco-3"></div>
      </div>
    </div>

    <div class="register-right">
      <div class="register-box">
        <div class="register-header">
          <h2 class="register-title">创建账号</h2>
          <p class="register-subtitle">填写以下信息完成注册</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（4-20个字符）"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="mobile">
            <el-input
              v-model="registerForm.mobile"
              placeholder="请输入手机号"
              size="large"
              prefix-icon="Phone"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱（选填）"
              size="large"
              prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（8-20位，包含大小写字母和数字）"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="registerForm.captchaCode"
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
            <el-checkbox v-model="registerForm.agreeTerms">
              我已阅读并同意
              <el-link type="primary" underline="never">《用户协议》</el-link>
              和
              <el-link type="primary" underline="never">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              :disabled="!registerForm.agreeTerms"
              class="register-button"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-link type="primary" underline="never" @click="goToLogin">立即登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Phone, Message, Lock, CircleCheck, Shop } from '@element-plus/icons-vue'
import { register, getCaptcha } from '@/api/auth'

const router = useRouter()

const registerFormRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const captchaError = ref(false)

const registerForm = reactive({
  username: '',
  mobile: '',
  email: '',
  password: '',
  confirmPassword: '',
  captchaCode: '',
  agreeTerms: false
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,20}$/.test(value)) {
    callback(new Error('密码必须包含大小写字母和数字'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度为4-20个字符', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
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

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!registerForm.agreeTerms) {
      ElMessage.warning('请阅读并同意用户协议和隐私政策')
      return
    }

    loading.value = true
    try {
      await register({
        username: registerForm.username,
        mobile: registerForm.mobile,
        email: registerForm.email,
        password: registerForm.password,
        captchaKey: captchaKey.value,
        captchaCode: registerForm.captchaCode
      })

      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      ElMessage.error(error.message || '注册失败')
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.register-page {
  min-height: 100vh;
  display: flex;
  background: $color-bg-page;
}

.register-left {
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

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-8;
  min-height: 100vh;
  overflow-y: auto;

  @include respond-to(lg) {
    flex: none;
    width: 100%;
  }

  @include respond-to(sm) {
    padding: $spacing-5;
  }
}

.register-box {
  width: 100%;
  max-width: 440px;
  animation: fadeInUp 0.5s ease-out;
}

.register-header {
  margin-bottom: $spacing-6;

  .register-title {
    font-size: $font-size-4xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 $spacing-2;
  }

  .register-subtitle {
    font-size: $font-size-md;
    color: $color-text-secondary;
    margin: 0;
  }
}

.register-form {
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

  .register-button {
    width: 100%;
    height: 48px;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    border-radius: $radius-lg;
    letter-spacing: $letter-spacing-wide;
  }
}

.register-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: $spacing-2;
  margin-top: $spacing-6;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}
</style>
