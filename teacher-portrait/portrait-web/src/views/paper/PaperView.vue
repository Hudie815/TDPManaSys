<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">论文管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增论文</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline :class="{ 'mobile-form': responsive.isMobile.value }">
        <el-form-item label="论文题目">
          <el-input v-model="queryForm.title" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="论文类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width:120px">
            <el-option label="期刊论文" value="期刊论文" />
            <el-option label="会议论文" value="会议论文" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="期刊/会议">
          <el-input v-model="queryForm.journalName" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="作者排序">
          <el-select v-model="queryForm.authorOrder" placeholder="全部" clearable style="width:100px">
            <el-option label="第1作者" :value="1" />
            <el-option label="第2作者" :value="2" />
            <el-option label="第3作者" :value="3" />
            <el-option label="第4+作者" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="收录标签">
          <el-select v-model="queryForm.indexTypes" multiple placeholder="全部" clearable collapse-tags collapse-tags-tooltip style="width:280px">
            <el-option label="SCI" value="SCI" />
            <el-option label="SSCI" value="SSCI" />
            <el-option label="EI" value="EI" />
            <el-option label="CSCD" value="CSCD" />
            <el-option label="CSSCI" value="CSSCI" />
            <el-option label="北大核心" value="北大核心" />
            <el-option label="普通期刊" value="普通期刊" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!responsive.isMobile.value" label="发表年份">
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
        <el-table-column prop="title" label="论文题目" min-width="220" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '期刊论文' ? '' : 'success'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="journalName" label="期刊/会议" min-width="160" show-overflow-tooltip />
        <el-table-column prop="publishDate" label="发表时间" width="110" align="center" />
        <el-table-column prop="authors" label="作者" min-width="120" show-overflow-tooltip />
        <el-table-column prop="authorOrder" label="排序" width="70" align="center" />
        <el-table-column label="收录标签" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.indexTypes" :key="tag" :type="indexColor(tag)" size="small" style="margin:2px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
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
          class="paper-card"
        >
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="card-title-row">
              <span class="card-title" :title="row.title">{{ row.title }}</span>
              <el-tag :type="row.type === '期刊论文' ? '' : 'success'" size="small">{{ row.type }}</el-tag>
            </div>
          </div>

          <!-- 卡片内容区 -->
          <div class="card-body">
            <div class="card-row">
              <span class="card-label">期刊/会议：</span>
              <span class="card-value card-value-ellipsis" :title="row.journalName">{{ row.journalName || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">发表时间：</span>
              <span class="card-value">{{ row.publishDate || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">作者：</span>
              <span class="card-value card-value-ellipsis" :title="row.authors">{{ row.authors || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">作者排序：</span>
              <span class="card-value">第{{ row.authorOrder }}作者</span>
            </div>
            <div class="card-row">
              <span class="card-label">收录标签：</span>
              <div class="card-tags">
                <el-tag v-for="tag in row.indexTypes" :key="tag" :type="indexColor(tag)" size="small" style="margin:2px">{{ tag }}</el-tag>
                <span v-if="!row.indexTypes || row.indexTypes.length === 0" class="card-value">-</span>
              </div>
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
          <p class="empty-text">暂无论文数据</p>
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

    <PaperFormDialog v-model:visible="dialogVisible" :edit-data="currentRow" @success="fetchData" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, RefreshRight, Edit, Delete, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pagePapers, deletePaper } from '../../api/paper'
import { useResponsive } from '../../composables/useResponsive'
import PaperFormDialog from './PaperFormDialog.vue'

// 响应式布局检测
const responsive = useResponsive()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentRow = ref(null)

const queryForm = reactive({
  title: '', type: '', journalName: '', authorOrder: null,
  indexTypes: [], dateRange: null, page: 1, size: 10
})

onMounted(() => { fetchData() })

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.indexTypes && params.indexTypes.length > 0) {
      params.indexTypes = params.indexTypes.join(',')
    } else {
      params.indexTypes = ''
    }
    if (params.dateRange && params.dateRange.length === 2) {
      params.publishDateBegin = params.dateRange[0] + '-01-01'
      params.publishDateEnd = params.dateRange[1] + '-01-01'
    }
    delete params.dateRange
    const res = await pagePapers(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { queryForm.page = 1; fetchData() }

function handleReset() {
  Object.assign(queryForm, {
    title: '', type: '', journalName: '', authorOrder: null,
    indexTypes: [], dateRange: null, page: 1, size: 10
  })
  fetchData()
}

function handleAdd() { currentRow.value = null; dialogVisible.value = true }
function handleEdit(row) { currentRow.value = { ...row }; dialogVisible.value = true }

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除论文「${row.title}」吗？收录标签将同步删除。`, '删除确认', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await deletePaper(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

function indexColor(type) {
  const map = { 'SCI': 'danger', 'SSCI': 'danger', 'EI': 'warning', 'CSCD': '', 'CSSCI': 'success', '北大核心': '', '普通期刊': 'info', '其他': 'info' }
  return map[type] || 'info'
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

.paper-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.paper-card:hover {
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

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
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

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  flex: 1;
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
