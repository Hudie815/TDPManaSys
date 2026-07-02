<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">数据导入导出</h2>
    </div>

    <el-tabs v-model="activeTab" :class="{ 'mobile-tabs': isMobile }">
      <el-tab-pane v-for="m in modules" :key="m.value" :label="isMobile ? getShortLabel(m.label) : m.label" :name="m.value">
        <el-row :gutter="isMobile ? 0 : 16">
          <el-col :span="isMobile ? 24 : 12">
            <el-card shadow="never">
              <template #header><span class="card-title">导入数据</span></template>
              <div class="step-row">
                <span class="step-label">1. 下载模板</span>
                <el-button type="success" :size="isMobile ? 'default' : 'small'" @click="downloadTemplate">下载模板</el-button>
              </div>
              <div class="step-row">
                <span class="step-label">2. 上传文件</span>
                <el-upload
                  ref="uploadRef"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="onUploadSuccess"
                  :on-error="onUploadError"
                  :before-upload="beforeUpload"
                  :show-file-list="false"
                  accept=".xlsx"
                >
                  <el-button type="primary" :size="isMobile ? 'default' : 'small'" :loading="uploading">选择文件并上传</el-button>
                </el-upload>
              </div>
              <div v-if="importResult" class="result-box">
                <el-alert :title="`成功 ${importResult.successCount} 条，跳过 ${importResult.skipCount} 条，失败 ${importResult.failCount} 条`"
                  :type="importResult.failCount > 0 ? 'warning' : 'success'" :closable="false" show-icon style="margin-bottom:8px" />
                <div v-if="importResult.skipReasons && importResult.skipReasons.length > 0" style="margin-top:8px">
                  <el-tag type="info" size="small" style="margin-bottom:4px">跳过原因</el-tag>
                  <div v-for="(r, i) in importResult.skipReasons" :key="'s'+i" class="reason-line">{{ r }}</div>
                </div>
                <div v-if="importResult.failReasons && importResult.failReasons.length > 0" style="margin-top:8px">
                  <el-tag type="danger" size="small" style="margin-bottom:4px">失败原因</el-tag>
                  <div v-for="(r, i) in importResult.failReasons" :key="'f'+i" class="reason-line">{{ r }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="isMobile ? 24 : 12" :class="{ 'mt-16': isMobile }">
            <el-card shadow="never">
              <template #header><span class="card-title">导出数据</span></template>
              <el-form :label-width="isMobile ? '100%' : '80px'" :label-position="isMobile ? 'top' : 'right'" size="small">
                <el-form-item v-if="isAdmin" label="选择教师">
                  <el-select v-model="exportUserId" placeholder="全部教师" clearable filterable :style="{ width: isMobile ? '100%' : '200px' }">
                    <el-option v-for="u in teacherList" :key="u.id" :label="u.name + ' (' + u.workNo + ')'" :value="u.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="年份">
                  <el-select v-model="exportYear" placeholder="全部年份" clearable :style="{ width: isMobile ? '100%' : '140px' }">
                    <el-option v-for="y in years" :key="y" :label="String(y)" :value="y" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :icon="Download" :size="isMobile ? 'default' : 'small'" @click="handleExport">导出 Excel</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'
import { useResponsive } from '../../composables/useResponsive'
import request from '../../api/request'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')
const token = userStore.token || ''

// 响应式布局
const { isMobile } = useResponsive()

const activeTab = ref('vertical-project')
const uploading = ref(false)
const importResult = ref(null)
const exportUserId = ref(null)
const exportYear = ref(null)
const teacherList = ref([])
const uploadRef = ref(null)

const modules = [
  { label: '纵向项目', value: 'vertical-project' },
  { label: '横向项目', value: 'horizontal-project' },
  { label: '专利', value: 'patent' },
  { label: '软著', value: 'software' },
  { label: '论文', value: 'paper' },
  { label: '竞赛', value: 'competition' }
]

// 移动端标签栏简化显示
function getShortLabel(label) {
  const map = {
    '纵向项目': '纵向',
    '横向项目': '横向',
    '专利': '专利',
    '软著': '软著',
    '论文': '论文',
    '竞赛': '竞赛'
  }
  return map[label] || label
}

const years = []
for (let y = new Date().getFullYear(); y >= new Date().getFullYear() - 10; y--) {
  years.push(y)
}

const uploadUrl = computed(() => `/api/excel/import/${activeTab.value}`)
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + token }))

onMounted(async () => {
  if (isAdmin.value) {
    try {
      const res = await request({ url: '/portrait/teachers', method: 'get' })
      teacherList.value = res.data || []
    } catch { }
  }
})

function downloadTemplate() {
  const a = document.createElement('a')
  a.href = `/api/excel/template/${activeTab.value}`
  a.download = `${activeTab.value}-模板.xlsx`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

function beforeUpload(file) {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('仅支持 .xlsx 文件')
    return false
  }
  uploading.value = true
  return true
}

function onUploadSuccess(res) {
  uploading.value = false
  if (res.code === 200 && res.data) {
    importResult.value = res.data
    ElMessage.success('导入完成')
  } else {
    ElMessage.error(res.message || '导入异常')
  }
}

function onUploadError() {
  uploading.value = false
  ElMessage.error('上传失败，请检查网络或文件格式')
}

async function handleExport() {
  const params = {}
  if (exportUserId.value) params.userId = exportUserId.value
  if (exportYear.value) params.year = exportYear.value
  const qs = Object.keys(params).map(k => k + '=' + encodeURIComponent(params[k])).join('&')
  try {
    const res = await request({
      url: `/excel/export/${activeTab.value}` + (qs ? '?' + qs : ''),
      method: 'get',
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    a.download = `${activeTab.value}-导出数据.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; color: #303133; }
.card-title { font-size: 15px; font-weight: bold; }
.step-row { display: flex; align-items: center; margin-bottom: 12px; gap: 12px; }
.step-label { width: 90px; color: #606266; font-size: 14px; }
.result-box { margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 6px; }
.reason-line { font-size: 12px; color: #909399; line-height: 1.6; }

/* 移动端适配 */
.mt-16 { margin-top: 16px; }

.mobile-tabs {
  margin: 0 -8px;
}

.mobile-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.mobile-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 8px;
}

.mobile-tabs :deep(.el-tabs__item) {
  padding: 0 10px;
  font-size: 13px;
}

.mobile-tabs :deep(.el-tabs__content) {
  padding: 0 8px;
}

/* 移动端按钮样式调整 */
@media (max-width: 768px) {
  .step-row {
    flex-wrap: wrap;
  }

  .step-label {
    width: 100%;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
  }

  .result-box {
    margin-top: 12px;
    padding: 10px;
  }

  .reason-line {
    word-break: break-all;
  }
}
</style>
