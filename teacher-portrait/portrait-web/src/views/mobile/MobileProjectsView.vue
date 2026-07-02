<template>
  <div class="mobile-projects-page">
    <div class="page-header">
      <h2 class="page-title">项目中心</h2>
    </div>

    <!-- 项目分类网格 -->
    <div class="projects-grid">
      <!-- 科研项目（带二级导航） -->
      <div class="project-card research-card" @click="handleResearchClick">
        <div class="card-icon research-icon">
          <el-icon :size="32"><FolderOpened /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">科研项目</span>
          <span class="card-desc">纵向项目 · 横向项目</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 专利项目 -->
      <div class="project-card" @click="navigateTo('/patent')">
        <div class="card-icon patent-icon">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">专利项目</span>
          <span class="card-desc">发明专利 · 实用新型 · 外观设计</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 软著项目 -->
      <div class="project-card" @click="navigateTo('/software')">
        <div class="card-icon software-icon">
          <el-icon :size="32"><Monitor /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">软著项目</span>
          <span class="card-desc">软件著作权管理</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 论文管理 -->
      <div class="project-card" @click="navigateTo('/paper')">
        <div class="card-icon paper-icon">
          <el-icon :size="32"><Reading /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">论文管理</span>
          <span class="card-desc">学术论文发表记录</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 竞赛指导 -->
      <div class="project-card" @click="navigateTo('/competition')">
        <div class="card-icon competition-icon">
          <el-icon :size="32"><Trophy /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">竞赛指导</span>
          <span class="card-desc">学生竞赛获奖记录</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 快速统计卡片 -->
    <div class="stats-summary">
      <div class="stats-header">
        <span class="stats-title">我的成果概览</span>
        <el-button type="primary" size="small" link @click="navigateTo(portraitPath)">
          查看画像
        </el-button>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-value">{{ dashboardData.projectTotalCount || 0 }}</span>
          <span class="stat-label">科研项目</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ dashboardData.patentGrantedCount || 0 }}</span>
          <span class="stat-label">授权专利</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ dashboardData.softwareCount || 0 }}</span>
          <span class="stat-label">软件著作</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ dashboardData.paperTotalCount || 0 }}</span>
          <span class="stat-label">学术论文</span>
        </div>
      </div>
    </div>

    <!-- 科研项目二级导航抽屉 -->
    <el-drawer
      v-model="researchDrawerVisible"
      title="科研项目"
      direction="ltr"
      :size="280"
      :with-header="true"
      class="research-drawer"
    >
      <div class="drawer-content">
        <div class="drawer-item" @click="navigateTo('/project/vertical')">
          <div class="drawer-icon">
            <el-icon :size="24"><FolderOpened /></el-icon>
          </div>
          <div class="drawer-info">
            <span class="drawer-title">纵向项目</span>
            <span class="drawer-desc">国家级、省部级项目</span>
          </div>
          <el-icon class="drawer-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="drawer-divider"></div>
        <div class="drawer-item" @click="navigateTo('/project/horizontal')">
          <div class="drawer-icon">
            <el-icon :size="24"><FolderOpened /></el-icon>
          </div>
          <div class="drawer-info">
            <span class="drawer-title">横向项目</span>
            <span class="drawer-desc">企事业单位委托项目</span>
          </div>
          <el-icon class="drawer-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { FolderOpened, Document, Monitor, Reading, Trophy, ArrowRight } from '@element-plus/icons-vue'
import { getPortraitDashboard } from '../../api/portrait'

const router = useRouter()
const userStore = useUserStore()

// ========== 数据状态 ==========
const dashboardData = ref({})
const researchDrawerVisible = ref(false)

// ========== 路径计算 ==========
const portraitPath = computed(() => {
  return userStore.userInfo ? `/portrait/${userStore.userInfo.userId}` : '/portrait'
})

// ========== 事件处理 ==========
const handleResearchClick = () => {
  // 打开二级导航抽屉
  researchDrawerVisible.value = true
}

const navigateTo = (path) => {
  researchDrawerVisible.value = false
  router.push(path)
}

// ========== 数据加载 ==========
onMounted(async () => {
  try {
    if (userStore.userInfo?.userId) {
      const res = await getPortraitDashboard(userStore.userInfo.userId)
      if (res && res.code === 200) {
        dashboardData.value = res.data
      }
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.mobile-projects-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

/* ========== 项目分类网格 ========== */
.projects-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-base);
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.project-card:active {
  transform: translateY(0);
}

/* 科研项目卡片特殊样式 */
.research-card {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
}

.research-card .card-title,
.research-card .card-desc,
.research-card .card-arrow {
  color: #fff;
}

.card-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.research-icon {
  background: rgba(255, 255, 255, 0.15);
  color: var(--color-accent);
}

.patent-icon {
  background: rgba(196, 86, 78, 0.1);
  color: var(--color-danger);
}

.software-icon {
  background: rgba(90, 138, 191, 0.1);
  color: var(--color-info);
}

.paper-icon {
  background: rgba(191, 90, 110, 0.1);
  color: var(--color-rose);
}

.competition-icon {
  background: rgba(74, 158, 110, 0.1);
  color: var(--color-success);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.card-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.card-arrow {
  color: var(--color-text-light);
}

/* ========== 快速统计卡片 ========== */
.stats-summary {
  margin-top: 24px;
  padding: 16px;
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stats-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-accent);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ========== 科研项目抽屉 ========== */
.research-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  background: var(--color-primary);
  color: #fff;
}

.research-drawer :deep(.el-drawer__title) {
  color: #fff;
  font-weight: 600;
}

.research-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-content {
  padding: 8px;
}

.drawer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.drawer-item:hover {
  background: var(--color-surface);
}

.drawer-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(90, 138, 191, 0.1);
  border-radius: var(--radius-sm);
  color: var(--color-info);
}

.drawer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.drawer-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.drawer-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.drawer-arrow {
  color: var(--color-text-light);
}

.drawer-divider {
  height: 1px;
  background: var(--color-border-light);
  margin: 8px 16px;
}
</style>