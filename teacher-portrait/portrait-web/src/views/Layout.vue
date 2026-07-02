<template>
  <el-container class="layout-container">
    <!-- 桌面端/平板端侧边栏 -->
    <el-aside 
      v-if="!responsive.isMobile.value" 
      :width="responsive.isTablet.value ? '64px' : (isCollapse ? '64px' : '230px')" 
      class="layout-aside"
    >
      <div class="logo" @click="router.push('/dashboard')">
        <div class="logo-icon">
          <svg viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="14" cy="14" r="12" stroke="var(--color-accent)" stroke-width="1.2"/>
            <circle cx="14" cy="14" r="6" stroke="var(--color-accent)" stroke-width="0.8" opacity="0.6"/>
            <circle cx="14" cy="14" r="2" fill="var(--color-accent)" opacity="0.5"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!isCollapse && !responsive.isTablet.value" class="logo-text">教师画像</span>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse || responsive.isTablet.value"
        :collapse-transition="false"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.55)"
        active-text-color="var(--color-accent-light)"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页仪表盘</span>
        </el-menu-item>

        <el-sub-menu index="/project">
          <template #title>
            <el-icon><FolderOpened /></el-icon>
            <span>科研项目</span>
          </template>
          <el-menu-item index="/project/vertical">纵向项目</el-menu-item>
          <el-menu-item index="/project/horizontal">横向项目</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/patent">
          <el-icon><Document /></el-icon>
          <span>专利管理</span>
        </el-menu-item>

        <el-menu-item index="/software">
          <el-icon><Monitor /></el-icon>
          <span>软著管理</span>
        </el-menu-item>

        <el-menu-item index="/paper">
          <el-icon><Reading /></el-icon>
          <span>论文管理</span>
        </el-menu-item>

        <el-menu-item index="/competition">
          <el-icon><Trophy /></el-icon>
          <span>竞赛指导</span>
        </el-menu-item>

        <el-menu-item :index="portraitPath">
          <el-icon><PieChart /></el-icon>
          <span>数字画像</span>
        </el-menu-item>

        <template v-if="userStore.role === 'ADMIN'">
          <div class="menu-divider"></div>
          <el-sub-menu index="/admin">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/score-config">权重配置</el-menu-item>
            <el-menu-item index="/admin/import-export">导入导出</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <!-- 移动端抽屉菜单 -->
    <el-drawer
      v-if="responsive.isMobile.value"
      v-model="mobileDrawerVisible"
      direction="ltr"
      :with-header="false"
      :size="230"
      :modal="true"
      class="mobile-menu-drawer"
    >
      <div class="mobile-menu-header">
        <div class="logo-mobile">
          <div class="logo-icon">
            <svg viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="14" cy="14" r="12" stroke="var(--color-accent)" stroke-width="1.2"/>
              <circle cx="14" cy="14" r="6" stroke="var(--color-accent)" stroke-width="0.8" opacity="0.6"/>
            </svg>
          </div>
          <span class="logo-text">教师画像</span>
        </div>
        <el-icon class="close-icon" @click="mobileDrawerVisible = false"><Close /></el-icon>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="var(--color-accent-light)"
        class="mobile-menu-list"
        @select="mobileDrawerVisible = false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/project/vertical">
          <el-icon><FolderOpened /></el-icon>
          <span>纵向项目</span>
        </el-menu-item>
        <el-menu-item index="/project/horizontal">
          <el-icon><FolderOpened /></el-icon>
          <span>横向项目</span>
        </el-menu-item>
        <el-menu-item index="/patent">
          <el-icon><Document /></el-icon>
          <span>专利管理</span>
        </el-menu-item>
        <el-menu-item index="/software">
          <el-icon><Monitor /></el-icon>
          <span>软著管理</span>
        </el-menu-item>
        <el-menu-item index="/paper">
          <el-icon><Reading /></el-icon>
          <span>论文管理</span>
        </el-menu-item>
        <el-menu-item index="/competition">
          <el-icon><Trophy /></el-icon>
          <span>竞赛指导</span>
        </el-menu-item>
        <el-menu-item :index="portraitPath">
          <el-icon><PieChart /></el-icon>
          <span>数字画像</span>
        </el-menu-item>
        <template v-if="userStore.role === 'ADMIN'">
          <div class="menu-divider"></div>
          <el-menu-item index="/admin/users">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-drawer>

    <el-container>
      <!-- 桌面端/平板端顶部导航栏 -->
      <el-header v-if="!responsive.isMobile.value" class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse && !responsive.isTablet.value" />
            <Expand v-else />
          </el-icon>
          <span class="page-breadcrumb">{{ route.meta.title || '' }}</span>
        </div>
        <div class="header-right">
          <div class="user-block">
            <div class="user-avatar">{{ avatarLetter }}</div>
            <div class="user-meta">
              <span class="user-name">{{ userStore.userName }}</span>
              <span class="user-role">{{ userStore.role === 'ADMIN' ? '管理员' : '教师' }}</span>
            </div>
          </div>
          <div class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
          </div>
        </div>
      </el-header>

      <!-- 移动端顶部导航栏 -->
      <el-header v-if="responsive.isMobile.value" class="mobile-header">
        <el-icon class="menu-toggle" @click="mobileDrawerVisible = true"><Expand /></el-icon>
        <span class="mobile-title">{{ route.meta.title || '教师画像' }}</span>
        <div class="mobile-header-right">
          <el-icon class="logout-btn-mobile" @click="handleLogout"><SwitchButton /></el-icon>
        </div>
      </el-header>

      <el-main class="layout-main" :class="{ 'mobile-main': responsive.isMobile.value }">
        <router-view />
      </el-main>

      <!-- 移动端底部 TabBar -->
      <div v-if="responsive.isMobile.value" class="mobile-tabbar">
        <div 
          v-for="tab in tabbarItems" 
          :key="tab.path" 
          class="tabbar-item" 
          :class="{ active: isActiveTab(tab.path) }"
          @click="handleTabClick(tab)"
        >
          <el-icon :size="20"><component :is="tab.icon" /></el-icon>
          <span class="tabbar-label">{{ tab.label }}</span>
        </div>
      </div>
    </el-container>

    <!-- 全局移动端预览抽屉组件 -->
    <MobilePreviewDrawer />
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { useResponsive } from '../composables/useResponsive'
import MobilePreviewDrawer from '../components/MobilePreviewDrawer.vue'
import {
  DataAnalysis, FolderOpened, Document, Monitor, Reading,
  Trophy, PieChart, Setting, Fold, Expand, SwitchButton, Close
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const responsive = useResponsive()

// ========== 侧边栏状态 ==========
const isCollapse = ref(false)
const mobileDrawerVisible = ref(false)

// ========== 响应式菜单处理 ==========
// 监听设备类型变化，自动调整侧边栏
watch(() => responsive.deviceType.value, (newType) => {
  if (newType === 'mobile') {
    isCollapse.value = true
  }
})

// ========== 计算属性 ==========
const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/project')) return '/project'
  if (path.startsWith('/admin')) return '/admin'
  return path
})

const portraitPath = computed(() => {
  return userStore.userInfo ? `/portrait/${userStore.userInfo.userId}` : '/portrait'
})

const avatarLetter = computed(() => {
  const name = userStore.userName || ''
  return name.charAt(0).toUpperCase()
})

// ========== 底部 TabBar 配置 ==========
// 新结构：首页、项目（含二级导航）、画像、管理（含二级导航）
// 移除原有的"成果"栏目，将其合并到"项目"中
const tabbarItems = computed(() => [
  { label: '首页', path: '/dashboard', icon: DataAnalysis },
  { label: '项目', path: '/mobile/projects', icon: FolderOpened },
  { label: '画像', path: portraitPath.value, icon: PieChart },
  ...(userStore.role === 'ADMIN' ? [{ label: '管理', path: '/mobile/admin', icon: Setting }] : [])
])

const isActiveTab = (path) => {
  const currentPath = route.path
  // 项目栏目：包含所有项目相关路径
  if (path === '/mobile/projects' && (
    currentPath.startsWith('/project') || 
    currentPath.startsWith('/patent') || 
    currentPath.startsWith('/software') || 
    currentPath.startsWith('/paper') || 
    currentPath.startsWith('/competition') ||
    currentPath.startsWith('/mobile/projects') ||
    currentPath.startsWith('/mobile/project')
  )) return true
  // 管理栏目：包含所有管理相关路径
  if (path === '/mobile/admin' && (
    currentPath.startsWith('/admin') ||
    currentPath.startsWith('/mobile/admin')
  )) return true
  // 画像栏目
  if (path.startsWith('/portrait') && currentPath.startsWith('/portrait')) return true
  // 首页
  return currentPath === path || currentPath.startsWith(path + '/')
}

const handleTabClick = (tab) => {
  router.push(tab.path)
}

// ========== 退出登录 ==========
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* ========== 侧边栏样式 ========== */
.layout-aside {
  background: var(--color-primary);
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none;
  transition: width var(--transition-smooth);
}

.layout-aside::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 20px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  transition: all var(--transition-base);
}

.logo:hover {
  background: rgba(255, 255, 255, 0.03);
}

.logo-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.logo-text {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4px;
  white-space: nowrap;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.layout-aside :deep(.el-menu) {
  border-right: none;
  padding: 8px;
}

.layout-aside :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: var(--radius-sm);
  font-size: 14px;
  transition: all var(--transition-fast);
}

.layout-aside :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
}

.layout-aside :deep(.el-menu-item.is-active) {
  background: rgba(200, 164, 92, 0.12) !important;
  color: var(--color-accent-light) !important;
  font-weight: 500;
}

.layout-aside :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  border-radius: var(--radius-sm);
  margin: 2px 0;
  font-size: 14px;
}

.layout-aside :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
}

.layout-aside :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 52px !important;
  font-size: 13px;
  height: 40px;
  line-height: 40px;
}

.menu-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 12px 16px;
}

/* ========== 桌面端顶部导航栏 ========== */
.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border-light);
  padding: 0 24px;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-muted);
  transition: color var(--transition-fast);
  padding: 4px;
}

.collapse-btn:hover {
  color: var(--color-text-primary);
}

.page-breadcrumb {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-block {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.user-block:hover {
  background: var(--color-surface);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font-display);
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: var(--color-text-muted);
  line-height: 1.2;
}

.logout-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  background: rgba(196, 86, 78, 0.08);
  color: var(--color-danger);
}

/* ========== 移动端抽屉菜单 ========== */
.mobile-menu-drawer :deep(.el-drawer__body) {
  background: var(--color-primary);
  padding: 0;
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-mobile {
  display: flex;
  align-items: center;
  gap: 10px;
}

.close-icon {
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 20px;
}

.mobile-menu-list {
  border-right: none;
  padding: 8px;
}

.mobile-menu-list :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 0;
  border-radius: var(--radius-sm);
  font-size: 15px;
}

.mobile-menu-list :deep(.el-menu-item.is-active) {
  background: rgba(200, 164, 92, 0.15) !important;
  color: var(--color-accent-light) !important;
}

/* ========== 移动端顶部导航栏 ========== */
.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border-light);
  padding: 0 16px;
  height: 56px;
}

.menu-toggle {
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-primary);
}

.mobile-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.mobile-header-right {
  display: flex;
  align-items: center;
}

.logout-btn-mobile {
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-muted);
}

/* ========== 主内容区域 ========== */
.layout-main {
  background: var(--color-surface);
  overflow-y: auto;
  padding: 24px;
}

.mobile-main {
  padding: 16px;
  padding-bottom: 70px; /* 为底部 TabBar 留空间 */
}

/* ========== 移动端底部 TabBar ========== */
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--color-card);
  border-top: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 1000;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  cursor: pointer;
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
}

.tabbar-item.active {
  color: var(--color-accent);
}

.tabbar-item.active .tabbar-label {
  color: var(--color-accent);
  font-weight: 500;
}

.tabbar-label {
  font-size: 12px;
}

/* ========== 响应式断点适配 ========== */
/* 平板端适配 */
@media (max-width: 1200px) {
  .layout-aside :deep(.el-sub-menu .el-menu-item) {
    display: none; /* 平板折叠状态下隐藏二级菜单文字 */
  }
}

/* ========== 抽屉全局样式 ========== */
:deep(.mobile-menu-drawer) {
  background: var(--color-primary);
}
</style>