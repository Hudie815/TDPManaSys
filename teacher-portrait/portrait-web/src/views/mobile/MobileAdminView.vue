<template>
  <div class="mobile-admin-page">
    <div class="page-header">
      <h2 class="page-title">系统管理</h2>
    </div>

    <!-- 管理功能网格 -->
    <div class="admin-grid">
      <!-- 用户管理 -->
      <div class="admin-card" @click="navigateTo('/admin/users')">
        <div class="card-icon user-icon">
          <el-icon :size="32"><User /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">用户管理</span>
          <span class="card-desc">教师账号增删改查</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 权重配置 -->
      <div class="admin-card" @click="navigateTo('/admin/score-config')">
        <div class="card-icon config-icon">
          <el-icon :size="32"><Setting /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">权重配置</span>
          <span class="card-desc">画像评分维度权重</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 导入导出 -->
      <div class="admin-card" @click="navigateTo('/admin/import-export')">
        <div class="card-icon data-icon">
          <el-icon :size="32"><Download /></el-icon>
        </div>
        <div class="card-content">
          <span class="card-title">导入导出</span>
          <span class="card-desc">Excel批量数据处理</span>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 系统状态概览 -->
    <div class="system-summary">
      <div class="summary-header">
        <span class="summary-title">系统状态概览</span>
      </div>
      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-value">{{ stats.userCount || 0 }}</span>
          <span class="summary-label">用户总数</span>
        </div>
        <div class="summary-item">
          <span class="summary-value">{{ stats.teacherCount || 0 }}</span>
          <span class="summary-label">教师数量</span>
        </div>
        <div class="summary-item">
          <span class="summary-value">{{ stats.adminCount || 0 }}</span>
          <span class="summary-label">管理员数量</span>
        </div>
        <div class="summary-item">
          <span class="summary-value">{{ stats.dataCount || 0 }}</span>
          <span class="summary-label">成果总数</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div class="actions-header">
        <span class="actions-title">快捷操作</span>
      </div>
      <div class="actions-list">
        <div class="action-item" @click="handleQuickAddUser">
          <el-icon :size="20"><Plus /></el-icon>
          <span class="action-label">新增用户</span>
        </div>
        <div class="action-item" @click="handleQuickExport">
          <el-icon :size="20"><Download /></el-icon>
          <span class="action-label">导出全部数据</span>
        </div>
        <div class="action-item" @click="handleRefreshStats">
          <el-icon :size="20"><Refresh /></el-icon>
          <span class="action-label">刷新统计</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Setting, Download, ArrowRight, Plus, Refresh } from '@element-plus/icons-vue'
import request from '../../api/request'

const router = useRouter()

// ========== 数据状态 ==========
const stats = ref({
  userCount: 0,
  teacherCount: 0,
  adminCount: 0,
  dataCount: 0
})

// ========== 导航处理 ==========
const navigateTo = (path) => {
  router.push(path)
}

const handleQuickAddUser = () => {
  router.push('/admin/users')
}

const handleQuickExport = () => {
  router.push('/admin/import-export')
}

const handleRefreshStats = async () => {
  await fetchStats()
  ElMessage.success('统计数据已刷新')
}

// ========== 数据加载 ==========
const fetchStats = async () => {
  try {
    // 获取用户统计
    const userRes = await request({ url: '/users', method: 'get', params: { page: 1, size: 1 } })
    if (userRes.code === 200) {
      stats.value.userCount = userRes.data.total || 0
    }

    // 计算成果总数（各模块数据汇总）
    const modules = ['vertical-project', 'horizontal-project', 'patent', 'software', 'paper', 'competition']
    let totalCount = 0
    
    // 并行请求各模块数据
    const promises = modules.map(m => 
      request({ url: `/${m.replace('-', '/')}`, method: 'get', params: { page: 1, size: 1 } })
        .catch(() => ({ data: { total: 0 } }))
    )
    
    const results = await Promise.all(promises)
    results.forEach(res => {
      if (res && res.data && res.data.total) {
        totalCount += res.data.total
      }
    })
    
    stats.value.dataCount = totalCount
    stats.value.teacherCount = stats.value.userCount - stats.value.adminCount
    
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.mobile-admin-page {
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

/* ========== 管理功能网格 ========== */
.admin-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-card {
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

.admin-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.admin-card:active {
  transform: translateY(0);
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

.user-icon {
  background: rgba(196, 86, 78, 0.1);
  color: var(--color-danger);
}

.config-icon {
  background: rgba(90, 138, 191, 0.1);
  color: var(--color-info);
}

.data-icon {
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

/* ========== 系统状态概览 ========== */
.system-summary {
  margin-top: 24px;
  padding: 16px;
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.summary-header {
  margin-bottom: 16px;
}

.summary-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-accent);
}

.summary-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ========== 快捷操作 ========== */
.quick-actions {
  margin-top: 24px;
  padding: 16px;
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.actions-header {
  margin-bottom: 12px;
}

.actions-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-item:hover {
  background: rgba(90, 138, 191, 0.08);
}

.action-item:active {
  background: rgba(90, 138, 191, 0.12);
}

.action-label {
  font-size: 14px;
  color: var(--color-text-primary);
}
</style>