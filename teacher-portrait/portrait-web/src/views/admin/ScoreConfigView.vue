<template>
  <div class="page-container">
    <div class="page-header" :class="{ 'is-mobile': isMobile }">
      <h2 class="page-title">评分权重配置</h2>
      <el-button type="primary" :icon="Check" :disabled="totalWeight !== 100" @click="handleSave" :size="isMobile ? 'default' : 'default'">
        保存配置
      </el-button>
    </div>

    <el-alert type="info" :closable="false" show-icon :class="{ 'mobile-alert': isMobile }">
      权重总和：<strong :style="{ color: totalWeight === 100 ? '#67C23A' : '#F56C6C' }">{{ totalWeight }}%</strong>
      <template v-if="totalWeight !== 100">
        <el-tag type="danger" size="small" style="margin-left:8px">总和必须为 100%</el-tag>
      </template>
    </el-alert>

    <el-row :gutter="isMobile ? 8 : 16">
      <el-col v-for="item in configList" :key="item.id" :span="24" style="margin-bottom:16px">
        <el-card shadow="hover" :class="{ 'mobile-card': isMobile }">
          <template #header>
            <div class="config-header">
              <span class="config-title">{{ item.dimension }}</span>
              <span class="config-weight" :class="{ 'mobile-weight': isMobile }">{{ item.weight }}%</span>
            </div>
          </template>
          <div class="slider-row" :class="{ 'mobile-slider-row': isMobile }">
            <span class="slider-label">权重</span>
            <el-slider
              v-model="item.weight"
              :min="0"
              :max="100"
              :step="1"
              :show-input="!isMobile"
              :style="{ flex: 1, marginLeft: isMobile ? '8px' : '12px' }"
            />
            <el-input-number
              v-if="isMobile"
              v-model="item.weight"
              :min="0"
              :max="100"
              :step="1"
              size="small"
              style="width: 70px; margin-left: 8px;"
            />
          </div>
          <el-collapse :class="{ 'mobile-collapse': isMobile }">
            <el-collapse-item>
              <template #title>
                <span :class="{ 'mobile-collapse-title': isMobile }">计分规则</span>
              </template>
              <el-input
                v-model="item.scoringRules"
                type="textarea"
                :rows="isMobile ? 3 : 4"
                placeholder="JSON 格式计分规则"
              />
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()

const configList = reactive([])

const totalWeight = computed(() => {
  const sum = configList.reduce((s, item) => s + Number(item.weight || 0), 0)
  return Math.round(sum * 100) / 100
})

onMounted(async () => {
  try {
    const res = await request({ url: '/score-config', method: 'get' })
    if (res.code === 200 && res.data) {
      configList.splice(0, configList.length, ...res.data.map(item => ({
        ...item,
        weight: Number(item.weight)
      })))
    }
  } catch { }
})

async function handleSave() {
  try {
    const payload = configList.map(item => ({
      id: item.id,
      dimension: item.dimension,
      weight: item.weight,
      scoringRules: item.scoringRules
    }))
    await request({ url: '/score-config', method: 'put', data: payload })
    ElMessage.success('配置已更新，画像数据将重新计算')
  } catch { }
}
</script>

<style scoped>
.page-container { padding: 0; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header.is-mobile {
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}
.page-header.is-mobile .page-title {
  font-size: 18px;
}
.page-title { font-size: 20px; color: #303133; }
.config-header { display: flex; justify-content: space-between; align-items: center; }
.config-title { font-size: 15px; font-weight: bold; }
.config-weight { font-size: 24px; font-weight: bold; color: #409EFF; }
.config-weight.mobile-weight { font-size: 20px; }
.slider-row { display: flex; align-items: center; }
.slider-row.mobile-slider-row {
  flex-wrap: wrap;
}
.slider-label { width: 40px; color: #909399; }
.mobile-alert {
  font-size: 13px;
  padding: 8px 12px;
}
.mobile-card :deep(.el-card__header) {
  padding: 12px 16px;
}
.mobile-card :deep(.el-card__body) {
  padding: 12px 16px;
}
.mobile-collapse {
  margin-top: 8px;
}
.mobile-collapse-title {
  font-size: 13px;
}
.mobile-collapse :deep(.el-collapse-item__header) {
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}
.mobile-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 8px;
}
</style>
