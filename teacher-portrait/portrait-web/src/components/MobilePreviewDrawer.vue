<template>
  <!-- 全局悬浮按钮 - 右下角固定定位（预览模式下隐藏） -->
  <div v-if="!isPreviewMode" class="mobile-preview-fab" @click="openDrawer">
    <el-icon :size="24">
      <Cellphone />
    </el-icon>
  </div>

  <!-- 抽屉组件 - 从右侧滑出 -->
  <el-drawer
    v-model="drawerVisible"
    title="移动端预览"
    direction="rtl"
    :size="drawerWidth"
    :with-header="true"
    :modal="true"
    :z-index="2000"
    class="mobile-preview-drawer"
    @opened="onDrawerOpened"
  >
    <!-- 控制面板 -->
    <div class="preview-control-panel">
      <div class="control-header">
        <span class="control-title">预览设置</span>
        <el-button type="primary" size="small" @click="refreshPreview">
          <el-icon><Refresh /></el-icon>
          刷新预览
        </el-button>
      </div>
      
      <div class="control-row">
        <span class="control-label">当前路由：</span>
        <span class="current-route">{{ currentPcPath }}</span>
      </div>
      
      <div class="control-row">
        <span class="control-label">设备选择：</span>
        <el-radio-group v-model="selectedDevice" size="small">
          <el-radio-button label="iphone">iPhone 14</el-radio-button>
          <el-radio-button label="android">Android</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="control-row">
        <span class="control-label">网络模拟：</span>
        <el-radio-group v-model="networkMode" size="small">
          <el-radio-button label="fast">4G 快速</el-radio-button>
          <el-radio-button label="slow">3G 慢速</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 拟真手机外壳 + Iframe 模拟器 -->
    <div class="phone-simulator-wrapper">
      <div class="phone-frame" :class="selectedDevice">
        <!-- 手机顶部状态栏 -->
        <div class="phone-notch">
          <div class="notch-speaker"></div>
        </div>
        <div class="phone-status-bar">
          <span class="status-time">{{ currentTime }}</span>
          <div class="status-icons">
            <el-icon :size="12"><Connection /></el-icon>
            <el-icon :size="12" v-if="networkMode === 'fast'"><Promotion /></el-icon>
            <span class="battery-icon">
              <span class="battery-level"></span>
            </span>
          </div>
        </div>
        
        <!-- Iframe 内容区域 -->
        <div class="phone-screen">
          <iframe
            ref="previewIframe"
            :src="mobilePreviewUrl"
            class="preview-iframe"
            frameborder="0"
            :style="iframeStyle"
            @load="onIframeLoad"
          ></iframe>
        </div>
        
        <!-- 手机底部导航栏 -->
        <div class="phone-bottom-bar">
          <div class="bottom-indicator"></div>
        </div>
      </div>
    </div>
    
    <!-- 预览提示 -->
    <div class="preview-tip">
      <el-icon><InfoFilled /></el-icon>
      <span>当前预览的是 PC 端路由的移动端适配效果。如需专属移动端路由，请在 getMobileUrl 函数中配置映射规则。</span>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Cellphone, Refresh, Connection, Promotion, InfoFilled } from '@element-plus/icons-vue'

// ========== 预览模式检测 ==========
/**
 * 检测当前页面是否在预览 iframe 中
 * 
 * 方案一：检测是否在 iframe 中（最可靠）
 * 使用 window.self !== window.top 判断当前页面是否被嵌入
 * 无论路由如何变化，只要在 iframe 中就不会显示预览按钮
 * 
 * 方案二：URL 参数检测（作为备用）
 * 通过 route.query.preview === 'true' 判断
 */
const isInIframe = ref(window.self !== window.top)

// 综合判断：在 iframe 中或 URL 带 preview 参数都视为预览模式
const isPreviewMode = computed(() => {
  return isInIframe.value || route.query.preview === 'true'
})

// ========== 响应式状态 ==========
const drawerVisible = ref(false)
const selectedDevice = ref('iphone')
const networkMode = ref('fast')
const previewIframe = ref(null)
const currentTime = ref('')
const drawerWidth = '45%'

// ========== 路由相关 ==========
const route = useRoute()

// 当前 PC 端路由路径
const currentPcPath = computed(() => route.fullPath)

// ========== 路由转换函数（核心） ==========
/**
 * 将 PC 端路由路径转换为移动端预览 URL
 * 
 * 当前策略：直接使用 PC 端路由路径作为移动端预览 URL
 * 并添加 preview=true 参数，标记为预览模式
 * 
 * TODO: 后续可在此函数中统一配置移动端专属路由映射规则
 * 示例映射规则：
 *   '/dashboard'         -> '/mobile/dashboard'
 *   '/project/vertical'  -> '/mobile/project/vertical'
 *   '/patent'            -> '/mobile/patent'
 *   '/portrait/:userId'  -> '/mobile/portrait/:userId'
 * 
 * @param {string} pcPath - 当前 PC 端路由路径
 * @returns {string} - 移动端预览 URL（带 preview 参数）
 */
const getMobileUrl = (pcPath) => {
  const baseUrl = window.location.origin
  
  // 添加 preview 参数，避免 iframe 内再显示预览按钮
  // 处理 URL 参数：如果已有参数用 &，否则用 ?
  const separator = pcPath.includes('?') ? '&' : '?'
  const previewUrl = `${baseUrl}${pcPath}${separator}preview=true`
  
  return previewUrl
}

// 计算移动端预览 URL
const mobilePreviewUrl = computed(() => {
  if (!drawerVisible.value) return ''
  return getMobileUrl(currentPcPath.value)
})

// ========== 样式控制 ==========
// 网络模拟样式（慢速时添加加载效果）
const iframeStyle = computed(() => ({
  opacity: networkMode.value === 'slow' ? 0.7 : 1,
  transition: 'opacity 0.3s ease'
}))

// ========== 事件处理 ==========
// 打开抽屉
const openDrawer = () => {
  drawerVisible.value = true
}

// 抽屉打开后的回调
const onDrawerOpened = () => {
  // 确保 Iframe src 被正确赋值
  if (previewIframe.value && mobilePreviewUrl.value) {
    previewIframe.value.src = mobilePreviewUrl.value
  }
  updateTime()
}

// 刷新预览
const refreshPreview = () => {
  if (previewIframe.value) {
    previewIframe.value.src = mobilePreviewUrl.value
  }
}

// Iframe 加载完成
const onIframeLoad = () => {
  console.log('[MobilePreview] Iframe 加载完成，URL:', mobilePreviewUrl.value)
}

// 更新时间显示
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// ========== 生命周期 ==========
let timeInterval = null

onMounted(() => {
  timeInterval = setInterval(updateTime, 1000)
  updateTime()
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})

// ========== 监听路由变化 ==========
watch(currentPcPath, (newPath) => {
  if (drawerVisible.value) {
    // 路由变化时自动更新预览
    refreshPreview()
  }
})
</script>

<style scoped>
/* ========== 悬浮按钮样式 ========== */
.mobile-preview-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 48px;
  height: 48px;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-accent);
  box-shadow: var(--shadow-lg);
  z-index: 3000;
  transition: all var(--transition-base);
}

.mobile-preview-fab:hover {
  background: var(--color-primary-light);
  transform: scale(1.08);
  box-shadow: var(--shadow-xl);
}

.mobile-preview-fab:active {
  transform: scale(0.95);
}

/* ========== 抽屉内容样式 ========== */
.preview-control-panel {
  background: var(--color-surface-warm);
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 16px;
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.control-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.control-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.control-row:last-child {
  margin-bottom: 0;
}

.control-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  min-width: 80px;
}

.current-route {
  font-size: 13px;
  color: var(--color-info);
  font-weight: 500;
  background: rgba(90, 138, 191, 0.1);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
}

/* ========== 手机模拟器容器 ========== */
.phone-simulator-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8eaed 100%);
  border-radius: var(--radius-lg);
}

/* ========== 拟真手机外壳 ========== */
.phone-frame {
  position: relative;
  background: #1a1a1a;
  border-radius: 40px;
  padding: 12px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    inset 0 2px 4px rgba(255, 255, 255, 0.1);
}

.phone-frame.iphone {
  width: 280px;
  height: 580px;
}

.phone-frame.android {
  width: 300px;
  height: 600px;
  border-radius: 30px;
}

/* ========== 手机顶部刘海/状态栏 ========== */
.phone-notch {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 28px;
  background: #1a1a1a;
  border-radius: 20px;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notch-speaker {
  width: 50px;
  height: 6px;
  background: #333;
  border-radius: 3px;
}

.phone-status-bar {
  position: absolute;
  top: 0;
  left: 12px;
  right: 12px;
  height: 44px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  z-index: 5;
}

.status-time {
  margin-left: 60px;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.battery-icon {
  width: 22px;
  height: 10px;
  border: 1px solid #fff;
  border-radius: 2px;
  position: relative;
  display: flex;
  align-items: center;
  padding: 1px;
}

.battery-icon::after {
  content: '';
  position: absolute;
  right: -3px;
  top: 2px;
  width: 2px;
  height: 4px;
  background: #fff;
  border-radius: 1px;
}

.battery-level {
  width: 80%;
  height: 100%;
  background: #4ade80;
  border-radius: 1px;
}

/* ========== 手机屏幕（Iframe 容器） ========== */
.phone-screen {
  position: absolute;
  top: 44px;
  left: 12px;
  right: 12px;
  bottom: 28px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}

/* ========== 手机底部导航指示器 ========== */
.phone-bottom-bar {
  position: absolute;
  bottom: 0;
  left: 12px;
  right: 12px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-indicator {
  width: 100px;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

/* ========== 预览提示 ========== */
.preview-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(200, 164, 92, 0.1);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-top: 16px;
}

/* ========== 抽屉全局样式覆盖 ========== */
:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

:deep(.el-drawer__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

:deep(.el-drawer__body) {
  padding: 20px;
  background: var(--color-surface);
}
</style>