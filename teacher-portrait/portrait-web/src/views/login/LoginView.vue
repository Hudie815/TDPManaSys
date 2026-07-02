<template>
  <div class="login-container" :class="{ 'mobile-layout': responsive.isMobile.value }">
    <!-- 桌面端/平板端品牌展示区 -->
    <div v-if="!responsive.isMobile.value" class="login-brand">
      <div class="brand-content">
        <div class="brand-emblem">
          <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="40" cy="40" r="36" stroke="rgba(200,164,92,0.4)" stroke-width="1.5"/>
            <circle cx="40" cy="40" r="24" stroke="rgba(200,164,92,0.6)" stroke-width="1"/>
            <circle cx="40" cy="40" r="12" fill="rgba(200,164,92,0.3)"/>
            <line x1="40" y1="4" x2="40" y2="76" stroke="rgba(200,164,92,0.2)" stroke-width="0.5"/>
            <line x1="4" y1="40" x2="76" y2="40" stroke="rgba(200,164,92,0.2)" stroke-width="0.5"/>
            <line x1="14.5" y1="14.5" x2="65.5" y2="65.5" stroke="rgba(200,164,92,0.15)" stroke-width="0.5"/>
            <line x1="65.5" y1="14.5" x2="14.5" y2="65.5" stroke="rgba(200,164,92,0.15)" stroke-width="0.5"/>
          </svg>
        </div>
        <h1 class="brand-title">教师数字画像</h1>
        <p class="brand-subtitle">Teacher Digital Portrait System</p>
        <div class="brand-divider"></div>
        <p class="brand-desc">多维度科研成果可视化分析平台</p>
        <p class="brand-desc-sub">服务于个人发展自评与学院统筹管理</p>
      </div>
      <div class="brand-decoration">
        <div class="deco-circle deco-circle-1"></div>
        <div class="deco-circle deco-circle-2"></div>
        <div class="deco-circle deco-circle-3"></div>
      </div>
    </div>

    <!-- 登录表单区域 -->
    <div class="login-form-side" :class="{ 'mobile-form': responsive.isMobile.value }">
      <!-- 移动端顶部品牌展示 -->
      <div v-if="responsive.isMobile.value" class="mobile-brand-header">
        <div class="mobile-emblem">
          <svg viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="30" cy="30" r="26" stroke="rgba(200,164,92,0.5)" stroke-width="1.2"/>
            <circle cx="30" cy="30" r="16" stroke="rgba(200,164,92,0.7)" stroke-width="1"/>
            <circle cx="30" cy="30" r="6" fill="rgba(200,164,92,0.4)"/>
          </svg>
        </div>
        <h1 class="mobile-brand-title">教师数字画像</h1>
      </div>

      <div class="login-card" :class="{ 'mobile-card': responsive.isMobile.value }">
        <h2 class="form-title" :class="{ 'mobile-title': responsive.isMobile.value }">欢迎回来</h2>
        <p class="form-subtitle">请输入您的账号信息登录系统</p>
        <el-form 
          ref="formRef" 
          :model="loginForm" 
          :rules="rules" 
          :size="responsive.isMobile.value ? 'default' : 'large'" 
          :label-position="responsive.isMobile.value ? 'top' : 'right'"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="workNo">
            <el-input v-model="loginForm.workNo" placeholder="请输入工号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="login-btn" 
              :class="{ 'mobile-btn': responsive.isMobile.value }"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 移动端底部版权信息 -->
      <div v-if="responsive.isMobile.value" class="mobile-footer">
        <p class="footer-text">Teacher Digital Portrait System</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'
import { useResponsive } from '../../composables/useResponsive'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const responsive = useResponsive()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  workNo: '',
  password: ''
})

const rules = {
  workNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========== 桌面端布局 ========== */
.login-container {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.login-brand {
  flex: 1;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 40px;
}

.brand-emblem {
  width: 80px;
  height: 80px;
  margin: 0 auto 28px;
  animation: emblem-float 6s ease-in-out infinite;
}

@keyframes emblem-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.brand-title {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 6px;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-family: var(--font-body);
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 32px;
}

.brand-divider {
  width: 40px;
  height: 1px;
  background: var(--color-accent);
  margin: 0 auto 28px;
  opacity: 0.6;
}

.brand-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.brand-desc-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 1px;
}

.brand-decoration {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(200, 164, 92, 0.08);
}

.deco-circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -200px;
}

.deco-circle-2 {
  width: 400px;
  height: 400px;
  bottom: -120px;
  left: -100px;
}

.deco-circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-color: rgba(200, 164, 92, 0.05);
}

.login-form-side {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-card);
  position: relative;
}

.login-form-side::before {
  content: '';
  position: absolute;
  left: 0;
  top: 15%;
  bottom: 15%;
  width: 1px;
  background: linear-gradient(to bottom, transparent, var(--color-accent), transparent);
  opacity: 0.3;
}

.login-card {
  width: 340px;
  padding: 0;
}

.form-title {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-bottom: 36px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
  border-radius: var(--radius-sm);
  margin-top: 8px;
}

:deep(.el-input__wrapper) {
  padding: 4px 12px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

/* ========== 移动端布局 ========== */
.mobile-layout {
  flex-direction: column;
}

.mobile-form {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--color-surface);
}

.mobile-form::before {
  display: none;
}

/* 移动端顶部品牌 */
.mobile-brand-header {
  text-align: center;
  margin-bottom: 24px;
}

.mobile-emblem {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
}

.mobile-brand-title {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 4px;
}

/* 移动端表单卡片 */
.mobile-card {
  width: 100%;
  max-width: 340px;
  padding: 24px;
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.mobile-title {
  font-size: 22px;
  text-align: center;
}

.mobile-btn {
  height: 48px;
  font-size: 16px;
}

/* 移动端底部版权 */
.mobile-footer {
  margin-top: 24px;
  text-align: center;
}

.footer-text {
  font-size: 12px;
  color: var(--color-text-light);
  letter-spacing: 1px;
}

/* ========== 平板端适配 ========== */
@media (max-width: 992px) and (min-width: 768px) {
  .login-brand {
    flex: 0.5;
  }
  
  .login-form-side {
    width: 50%;
  }
  
  .brand-title {
    font-size: 28px;
  }
  
  .login-card {
    width: 280px;
  }
}

/* ========== 移动端响应式断点 ========== */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-brand {
    display: none;
  }
  
  .login-form-side {
    width: 100%;
  }
}
</style>