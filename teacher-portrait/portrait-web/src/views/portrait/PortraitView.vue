<template>
  <div class="portrait-page">
    <div class="page-header" :class="{ 'is-mobile': responsive.isMobile.value }">
      <div class="header-info">
        <h2 class="page-title">数字画像</h2>
        <p class="page-desc">多维度科研成果综合评估与可视化分析</p>
      </div>
      <div class="header-actions" :class="{ 'is-mobile': responsive.isMobile.value }">
        <el-select v-if="isAdmin" v-model="selectedUserId" placeholder="选择教师" filterable :style="{ width: responsive.isMobile.value ? '100%' : '240px' }" @change="onUserChange">
          <el-option v-for="u in teacherList" :key="u.id" :label="u.name + ' (' + u.college + ')'" :value="u.id" />
        </el-select>
        <span v-else class="current-user-badge">{{ userStore.userName || '当前用户' }}</span>
      </div>
    </div>

    <!-- 顶部数字指标卡片：移动端 2列×3行，PC端 6列×1行 -->
    <div class="summary-grid" :class="{ 'is-mobile': responsive.isMobile.value }">
      <div v-for="item in summaryItems" :key="item.label" class="summary-card" :style="{ '--card-accent': item.accent }">
        <div class="summary-value" :style="{ color: item.colorFn ? item.colorFn(item.value) : 'var(--color-text-primary)' }">{{ item.display }}</div>
        <div class="summary-label">{{ item.label }}</div>
      </div>
    </div>

    <!-- 雷达图与成果分布：移动端垂直堆叠，PC端并排 -->
    <el-row :gutter="responsive.isMobile.value ? 0 : 20" class="charts-row" :class="{ 'is-mobile': responsive.isMobile.value }">
      <el-col :span="responsive.isMobile.value ? 24 : 12" :class="{ 'mobile-col': responsive.isMobile.value }">
        <el-card shadow="never" :class="{ 'mobile-card': responsive.isMobile.value }">
          <template #header>
            <div class="section-header">
              <span class="section-title">综合能力雷达图</span>
            </div>
          </template>
          <RadarChart ref="radarRef" :radar-data="radarData" :compare-mode="compareMode" :compare-data="compareData" :teacher-list="teacherList" :is-mobile="responsive.isMobile.value" @update:compare-data="onCompareData" />
        </el-card>
      </el-col>
      <el-col :span="responsive.isMobile.value ? 24 : 12" :class="{ 'mobile-col': responsive.isMobile.value }">
        <el-card shadow="never" :class="{ 'mobile-card': responsive.isMobile.value }">
          <template #header>
            <div class="section-header">
              <span class="section-title">成果分布</span>
            </div>
          </template>
          <DistributionChart :user-id="selectedUserId" :is-mobile="responsive.isMobile.value" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 历年趋势 -->
    <el-row :gutter="responsive.isMobile.value ? 0 : 20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="never" :class="{ 'mobile-card': responsive.isMobile.value }">
          <template #header>
            <div class="section-header">
              <span class="section-title">历年趋势</span>
            </div>
          </template>
          <TrendChart :trend-data="trendData" :is-mobile="responsive.isMobile.value" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../store/user'
import { useResponsive } from '../../composables/useResponsive'
import { getPortraitRadar, getPortraitDashboard, getPortraitTrend, getPortraitTeachers } from '../../api/portrait'
import RadarChart from './RadarChart.vue'
import TrendChart from './TrendChart.vue'
import DistributionChart from './DistributionChart.vue'

const route = useRoute()
const userStore = useUserStore()
const responsive = useResponsive()
const isAdmin = computed(() => userStore.role === 'ADMIN')
const currentUserId = computed(() => userStore.userInfo?.userId)

const selectedUserId = ref(null)
const teacherList = ref([])
const radarData = ref(null)
const dashboard = ref({ totalFunding: 0, paperACount: 0, paperBCount: 0, patentGrantedCount: 0, softwareCount: 0, competitionAwardCount: 0, rawScores: {}, normalizedScores: {} })
const trendData = ref([])
const compareMode = ref(false)
const compareData = ref(null)
const radarRef = ref(null)

const avgScore = computed(() => {
  const ns = dashboard.value.normalizedScores || {}
  const dims = ['科研项目', '专利成果', '软件著作', '学术论文', '竞赛指导']
  const sum = dims.reduce((s, d) => s + Number(ns[d] || 0), 0)
  return dims.length > 0 ? sum / dims.length : 0
})

function scoreColor(v) { return v >= 60 ? 'var(--color-success)' : v >= 30 ? 'var(--color-warning)' : 'var(--color-danger)' }

function fmtMoney(v) { return v ? Number(v).toFixed(1) : '0.0' }

const summaryItems = computed(() => {
  const d = dashboard.value
  return [
    { label: '项目总经费(万)', display: fmtMoney(d.totalFunding), accent: 'var(--color-teal)' },
    { label: 'A/B类论文', display: `${d.paperACount} / ${d.paperBCount}`, accent: 'var(--color-rose)' },
    { label: '已授权专利', display: d.patentGrantedCount, accent: 'var(--color-amber)' },
    { label: '软件著作', display: d.softwareCount, accent: 'var(--color-info)' },
    { label: '竞赛获奖', display: d.competitionAwardCount, accent: 'var(--color-success)' },
    { label: '综合均分', display: avgScore.value.toFixed(1), accent: 'var(--color-accent)', colorFn: scoreColor, value: avgScore.value },
  ]
})

async function loadTeachers() {
  try {
    const res = await getPortraitTeachers()
    teacherList.value = res.data || []
  } catch { /* ignore */ }
}

async function loadData(uid) {
  try {
    const [radarRes, dashRes, trendRes] = await Promise.all([
      getPortraitRadar(uid), getPortraitDashboard(uid), getPortraitTrend(uid)
    ])
    if (radarRes.code === 200) radarData.value = radarRes.data
    if (dashRes.code === 200) dashboard.value = dashRes.data
    if (trendRes.code === 200) trendData.value = trendRes.data
  } catch { /* ignore */ }
}

function onUserChange(uid) {
  compareMode.value = false
  compareData.value = null
  loadData(uid)
}

function onCompareData(data) {
  compareData.value = data
}

// 监听设备类型变化，触发图表 resize
watch(() => responsive.isMobile.value, () => {
  nextTick(() => {
    if (radarRef.value) radarRef.value.refreshChart()
    // 其他图表组件通过 window resize 事件自动触发
    window.dispatchEvent(new Event('resize'))
  })
})

onMounted(async () => {
  const routeUserId = route.params.userId
  if (isAdmin.value) {
    await loadTeachers()
    selectedUserId.value = routeUserId ? Number(routeUserId) : (teacherList.value[0]?.id || currentUserId.value)
  } else {
    selectedUserId.value = currentUserId.value
  }
  if (selectedUserId.value) loadData(selectedUserId.value)
})

watch(() => route.params.userId, (val) => {
  if (val) { selectedUserId.value = Number(val); loadData(Number(val)) }
})
</script>

<style scoped>
.portrait-page {
  max-width: 1200px;
}

/* ========== 页面头部适配 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-header.is-mobile {
  flex-direction: column;
  gap: 12px;
}

.header-info {
  flex: 1;
}

.header-actions {
  flex-shrink: 0;
}

.header-actions.is-mobile {
  width: 100%;
}

.page-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
}

.page-header.is-mobile .page-title {
  font-size: 18px;
}

.page-desc {
  font-size: 13px;
  color: var(--color-text-muted);
}

.page-header.is-mobile .page-desc {
  font-size: 12px;
}

.current-user-badge {
  font-size: 14px;
  color: var(--color-accent);
  font-weight: 600;
  padding: 6px 16px;
  background: var(--color-accent-glow);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(200, 164, 92, 0.2);
}

/* ========== 顶部数字指标卡片适配 ========== */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}

/* 移动端：2列 × 3行 */
.summary-grid.is-mobile {
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.summary-card {
  background: var(--color-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 20px 16px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-base);
}

.summary-grid.is-mobile .summary-card {
  padding: 16px 12px;
}

.summary-card::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 2px;
  background: var(--card-accent);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.summary-card:hover::after {
  opacity: 1;
}

/* 移动端禁用 hover 效果 */
@media (hover: none) {
  .summary-card:hover {
    transform: none;
    box-shadow: var(--shadow-sm);
  }
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.summary-grid.is-mobile .summary-value {
  font-size: 20px;
}

.summary-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 6px;
}

.summary-grid.is-mobile .summary-label {
  font-size: 11px;
  margin-top: 4px;
}

/* ========== 图表行适配 ========== */
.charts-row {
  margin-top: 20px;
}

.charts-row.is-mobile {
  margin-top: 16px;
}

.mobile-col {
  margin-bottom: 16px;
}

.mobile-card {
  border-radius: var(--radius-md);
}

.mobile-card :deep(.el-card__header) {
  padding: 12px 16px;
}

.mobile-card :deep(.el-card__body) {
  padding: 12px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.mobile-card .section-title {
  font-size: 14px;
}

/* ========== 响应式媒体查询 ========== */
@media (max-width: 768px) {
  .portrait-page {
    padding: 0;
  }
}
</style>