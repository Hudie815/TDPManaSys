<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">横向项目管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增项目</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline :class="{ 'mobile-form': responsive.isMobile.value }">
        <el-form-item label="项目名称">
          <el-input v-model="queryForm.name" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input v-model="queryForm.companyName" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:110px">
            <el-option label="在研" value="在研" />
            <el-option label="已结题" value="已结题" />
          </el-select>
        </el-form-item>
        <!-- 移动端隐藏次要筛选条件 -->
        <el-form-item v-if="!responsive.isMobile.value" label="角色">
          <el-select v-model="queryForm.role" placeholder="全部" clearable style="width:110px">
            <el-option label="主持" value="主持" />
            <el-option label="参与" value="参与" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="签订年份">
          <el-date-picker v-model="queryForm.dateRange" type="yearrange" range-separator="至" start-placeholder="开始年" end-placeholder="结束年" value-format="YYYY" style="width:200px" />
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="金额(万元)">
          <el-input-number v-model="queryForm.amountMin" :min="0" placeholder="最低" controls-position="right" style="width:105px" />
          <span style="margin:0 4px">-</span>
          <el-input-number v-model="queryForm.amountMax" :min="0" placeholder="最高" controls-position="right" style="width:105px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <!-- PC端：表格布局 -->
      <el-table
        v-if="!responsive.isMobile.value"
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width:100%"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="companyName" label="合作企业" min-width="160" show-overflow-tooltip />
        <el-table-column prop="contractAmount" label="合同金额(万元)" width="130" align="right" />
        <el-table-column prop="role" label="角色" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '在研' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="110" align="center" />
        <el-table-column prop="endDate" label="完成日期" width="110" align="center" />
        <el-table-column prop="teacherName" label="教师" width="100" align="center" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端：卡片化布局 -->
      <div v-if="responsive.isMobile.value" class="mobile-card-list" v-loading="loading">
        <div
          v-for="row in tableData"
          :key="row.id"
          class="project-card"
        >
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="card-title-row">
              <span class="card-name" :title="row.name">{{ row.name }}</span>
              <div class="card-tags">
                <el-tag :type="row.status === '在研' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </div>
            </div>
          </div>

          <!-- 卡片内容区 -->
          <div class="card-body">
            <div class="card-row">
              <span class="card-label">合作企业：</span>
              <span class="card-value card-value-ellipsis" :title="row.companyName">{{ row.companyName || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">合同金额：</span>
              <span class="card-value">{{ row.contractAmount ? row.contractAmount + ' 万元' : '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">角色：</span>
              <span class="card-value">{{ row.role || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">签订日期：</span>
              <span class="card-value">{{ row.signDate || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">完成日期：</span>
              <span class="card-value">{{ row.endDate || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">教师：</span>
              <span class="card-value">{{ row.teacherName || '-' }}</span>
            </div>
          </div>

          <!-- 卡片底部：操作按钮 -->
          <div class="card-footer">
            <div class="card-actions">
              <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="tableData.length === 0 && !loading" class="card-empty">
          <el-icon :size="48" color="#b8bfcc"><Document /></el-icon>
          <p class="empty-text">暂无项目数据</p>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" :class="{ 'mobile-pagination': responsive.isMobile.value }">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :page-sizes="responsive.isMobile.value ? [10, 20] : [10, 20, 50]"
          :total="total"
          :layout="responsive.isMobile.value ? 'total, prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <HorizontalProjectFormDialog v-model:visible="dialogVisible" :edit-data="currentRow" @success="fetchData" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, RefreshRight, Edit, Delete, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageHorizontalProjects, deleteHorizontalProject } from '../../api/horizontalProject'
import { useResponsive } from '../../composables/useResponsive'
import HorizontalProjectFormDialog from './HorizontalProjectFormDialog.vue'

// 响应式布局检测
const responsive = useResponsive()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentRow = ref(null)

const queryForm = reactive({
  name: '',
  companyName: '',
  status: '',
  role: '',
  dateRange: null,
  amountMin: null,
  amountMax: null,
  page: 1,
  size: 10
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.dateRange && params.dateRange.length === 2) {
      params.signDateBegin = params.dateRange[0] + '-01-01'
      params.signDateEnd = params.dateRange[1] + '-01-01'
    }
    delete params.dateRange
    const res = await pageHorizontalProjects(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.page = 1
  fetchData()
}

function handleReset() {
  Object.assign(queryForm, {
    name: '',
    companyName: '',
    status: '',
    role: '',
    dateRange: null,
    amountMin: null,
    amountMax: null,
    page: 1,
    size: 10
  })
  fetchData()
}

function handleAdd() {
  currentRow.value = null
  dialogVisible.value = true
}

function handleEdit(row) {
  currentRow.value = { ...row }
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除项目「${row.name}」吗？此操作不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteHorizontalProject(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 20px; color: #303133; }
.search-card { padding: 4px 0; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* ========== 移动端搜索表单适配 ========== */
.mobile-form :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 12px;
  width: 100%;
}

.mobile-form :deep(.el-form-item__label) {
  width: 80px;
}

.mobile-form :deep(.el-input),
.mobile-form :deep(.el-select) {
  width: calc(100% - 80px) !important;
}

.mobile-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.mobile-form :deep(.el-button) {
  width: 48%;
}

/* ========== 移动端分页适配 ========== */
.mobile-pagination {
  justify-content: center;
}

.mobile-pagination :deep(.el-pagination) {
  flex-wrap: wrap;
}

/* ========== 移动端卡片化布局样式 ========== */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px 0;
}

.project-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.project-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 卡片头部 */
.card-header {
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eaed 100%);
  border-bottom: 1px solid #ebeef5;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

/* 卡片内容区 */
.card-body {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-row {
  display: flex;
  align-items: flex-start;
  gap: 4px;
}

.card-label {
  font-size: 13px;
  color: #909399;
  min-width: 70px;
  flex-shrink: 0;
}

.card-value {
  font-size: 13px;
  color: #606266;
  flex: 1;
}

.card-value-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 卡片底部 */
.card-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions :deep(.el-button) {
  padding: 5px 12px;
}

/* 空状态 */
.card-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: #f5f7fa;
  border-radius: 8px;
}

.empty-text {
  font-size: 14px;
  color: #909399;
  margin-top: 12px;
}

/* ========== 响应式媒体查询（备用） ========== */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .page-title {
    font-size: 18px;
  }

  .page-header :deep(.el-button) {
    width: 100%;
  }
}
</style>
