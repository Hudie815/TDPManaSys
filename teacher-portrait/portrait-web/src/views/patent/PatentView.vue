<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">专利管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增专利</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline :class="{ 'mobile-form': responsive.isMobile.value }">
        <el-form-item label="专利名称">
          <el-input v-model="queryForm.name" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="专利类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width:120px">
            <el-option label="发明专利" value="发明专利" />
            <el-option label="实用新型" value="实用新型" />
            <el-option label="外观设计" value="外观设计" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:110px">
            <el-option label="申请中" value="申请中" />
            <el-option label="已授权" value="已授权" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="申请年份">
          <el-date-picker v-model="queryForm.dateRange" type="yearrange" range-separator="至" start-placeholder="开始年" end-placeholder="结束年" value-format="YYYY" style="width:200px" />
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
        <el-table-column prop="name" label="专利名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="专利类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicationNo" label="申请号" width="140" show-overflow-tooltip />
        <el-table-column prop="grantNo" label="授权号" width="140" show-overflow-tooltip />
        <el-table-column prop="applicationDate" label="申请日期" width="110" align="center" />
        <el-table-column prop="grantDate" label="授权日期" width="110" align="center" />
        <el-table-column prop="inventors" label="发明人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="教师" width="100" align="center" />
        <el-table-column prop="isCounted" label="考核" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isCounted ? 'success' : 'info'" size="small">{{ row.isCounted ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === '已授权'" type="warning" link :icon="Sell" @click="handleTransfer(row)">转让</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端：卡片化布局 -->
      <div v-if="responsive.isMobile.value" class="mobile-card-list" v-loading="loading">
        <div 
          v-for="row in tableData" 
          :key="row.id" 
          class="patent-card"
        >
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="card-title-row">
              <span class="card-name" :title="row.name">{{ row.name }}</span>
              <div class="card-tags">
                <el-tag :type="typeColor(row.type)" size="small">{{ row.type }}</el-tag>
                <el-tag :type="statusColor(row.status)" size="small">{{ row.status }}</el-tag>
              </div>
            </div>
          </div>

          <!-- 卡片内容区 -->
          <div class="card-body">
            <div class="card-row">
              <span class="card-label">申请号：</span>
              <span class="card-value">{{ row.applicationNo || '-' }}</span>
            </div>
            <div v-if="row.grantNo" class="card-row">
              <span class="card-label">授权号：</span>
              <span class="card-value">{{ row.grantNo }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">申请日期：</span>
              <span class="card-value">{{ row.applicationDate || '-' }}</span>
            </div>
            <div v-if="row.grantDate" class="card-row">
              <span class="card-label">授权日期：</span>
              <span class="card-value">{{ row.grantDate }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">发明人：</span>
              <span class="card-value card-value-ellipsis" :title="row.inventors">{{ row.inventors || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">教师：</span>
              <span class="card-value">{{ row.teacherName || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">考核：</span>
              <el-tag :type="row.isCounted ? 'success' : 'info'" size="small">{{ row.isCounted ? '是' : '否' }}</el-tag>
            </div>
          </div>

          <!-- 卡片底部：操作按钮 -->
          <div class="card-footer">
            <div class="card-actions">
              <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.status === '已授权'" type="warning" size="small" :icon="Sell" @click="handleTransfer(row)">转让</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="tableData.length === 0 && !loading" class="card-empty">
          <el-icon :size="48" color="#b8bfcc"><Document /></el-icon>
          <p class="empty-text">暂无专利数据</p>
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

    <PatentFormDialog v-model:visible="dialogVisible" :edit-data="currentRow" @success="fetchData" />
    <TransferDialog v-model:visible="transferVisible" :patent-id="transferPatentId" :patent-name="transferPatentName" @success="fetchData" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, RefreshRight, Edit, Delete, Sell, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pagePatents, deletePatent } from '../../api/patent'
import { useResponsive } from '../../composables/useResponsive'
import PatentFormDialog from './PatentFormDialog.vue'
import TransferDialog from './TransferDialog.vue'

// 响应式布局检测
const responsive = useResponsive()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentRow = ref(null)
const transferVisible = ref(false)
const transferPatentId = ref(null)
const transferPatentName = ref('')

const queryForm = reactive({
  name: '', type: '', status: '', dateRange: null, page: 1, size: 10
})

onMounted(() => { fetchData() })

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.dateRange && params.dateRange.length === 2) {
      params.applicationDateBegin = params.dateRange[0] + '-01-01'
      params.applicationDateEnd = params.dateRange[1] + '-01-01'
    }
    delete params.dateRange
    const res = await pagePatents(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { queryForm.page = 1; fetchData() }

function handleReset() {
  Object.assign(queryForm, { name: '', type: '', status: '', dateRange: null, page: 1, size: 10 })
  fetchData()
}

function handleAdd() { currentRow.value = null; dialogVisible.value = true }
function handleEdit(row) { currentRow.value = { ...row }; dialogVisible.value = true }
function handleTransfer(row) { transferPatentId.value = row.id; transferPatentName.value = row.name; transferVisible.value = true }

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除专利「${row.name}」吗？转让记录将同步删除。`, '删除确认', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await deletePatent(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

function typeColor(type) {
  return { '发明专利': 'danger', '实用新型': 'warning', '外观设计': 'info' }[type] || 'info'
}

function statusColor(status) {
  return status === '已授权' ? 'success' : ''
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

.patent-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.patent-card:hover {
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