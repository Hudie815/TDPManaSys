<template>
  <div class="radar-container" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端：下拉菜单选择模式 -->
    <div v-if="isMobile" class="mobile-controls">
      <el-select v-model="scoreMode" size="small" style="width: 100%; margin-bottom: 8px;" @change="refreshChart">
        <el-option label="归一化得分" value="normalized" />
        <el-option label="原始得分" value="raw" />
      </el-select>
      <div v-if="isAdmin && !compareMode" class="compare-row">
        <el-checkbox v-model="showCompare" size="small">对比模式</el-checkbox>
        <el-select v-if="showCompare" v-model="compareUserIds" multiple placeholder="选择对比教师(2-5人)" collapse-tags size="small" style="width: 100%; margin-top: 8px;" @change="onCompareChange">
          <el-option v-for="u in teacherList" :key="u.id" :label="u.name" :value="u.id" />
        </el-select>
      </div>
    </div>
    
    <!-- PC端：按钮组形式 -->
    <div v-else class="chart-controls">
      <el-radio-group v-model="scoreMode" size="small" @change="refreshChart">
        <el-radio-button label="normalized">归一化得分</el-radio-button>
        <el-radio-button label="raw">原始得分</el-radio-button>
      </el-radio-group>
      <el-checkbox v-if="isAdmin && !compareMode" v-model="showCompare" size="small" style="margin-left:12px">
        对比模式
      </el-checkbox>
      <el-select v-if="isAdmin && showCompare && !compareMode" v-model="compareUserIds" multiple placeholder="选择对比教师(2-5人)" collapse-tags style="width:340px;margin-left:12px" @change="onCompareChange">
        <el-option v-for="u in teacherList" :key="u.id" :label="u.name" :value="u.id" />
      </el-select>
    </div>
    
    <div ref="chartRef" class="chart" :class="{ 'is-mobile': isMobile }"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getPortraitCompare } from '../../api/portrait'
import { useUserStore } from '../../store/user'

const props = defineProps({ radarData: Object, compareMode: Boolean, compareData: Array, teacherList: Array, isMobile: Boolean })
const emit = defineEmits(['update:compareData'])
const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')

const scoreMode = ref('normalized')
const showCompare = ref(false)
const compareUserIds = ref([])
const chartRef = ref(null)
let chartInstance = null

const dims = ['科研项目', '专利成果', '软件著作', '学术论文', '竞赛指导']
const colors = ['#4a7ebf', '#4a9e6e', '#c8a45c', '#c4564e', '#6a5abf']

function buildOption() {
  const indicators = dims.map(d => ({ name: d, max: 100 }))
  const seriesData = []

  if (props.compareData && props.compareData.length > 0) {
    for (const item of props.compareData) {
      const vals = dims.map(d => Number(item[scoreMode.value === 'normalized' ? 'normalizedScores' : 'rawScores']?.[d] || 0))
      seriesData.push({ name: item.userName || ('ID:' + item.userId), value: vals })
    }
  } else if (props.radarData) {
    const vals = dims.map(d => Number(props.radarData[scoreMode.value === 'normalized' ? 'normalizedScores' : 'rawScores']?.[d] || 0))
    seriesData.push({ name: props.radarData.userName || '当前', value: vals })
  }

  // 移动端：调小半径，调整中心位置，增加边距
  const radius = props.isMobile ? '45%' : '60%'
  const center = props.isMobile ? ['50%', '55%'] : ['50%', '52%']
  const axisNameFontSize = props.isMobile ? 11 : 12

  return {
    tooltip: { trigger: 'item' },
    legend: { 
      bottom: 0, 
      data: seriesData.map(s => s.name),
      textStyle: { fontSize: props.isMobile ? 11 : 12 }
    },
    radar: {
      center: center,
      radius: radius,
      indicator: indicators,
      axisName: { color: '#5a6478', fontSize: axisNameFontSize }
    },
    series: [{
      type: 'radar',
      data: seriesData.map((s, i) => ({
        name: s.name,
        value: s.value,
        lineStyle: { color: colors[i % colors.length], width: 2 },
        areaStyle: { color: colors[i % colors.length], opacity: 0.1 },
        itemStyle: { color: colors[i % colors.length] }
      }))
    }]
  }
}

function initChart() {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(buildOption())
}

function refreshChart() { 
  if (chartInstance) {
    chartInstance.setOption(buildOption())
    chartInstance.resize()
  }
}

// 监听窗口大小变化，触发 resize
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => props.radarData, () => nextTick(initChart), { deep: true })
watch(() => props.compareData, () => nextTick(initChart), { deep: true })
watch(() => props.isMobile, () => {
  nextTick(() => {
    refreshChart()
  })
})

async function onCompareChange(val) {
  if (val && val.length >= 2) {
    try {
      const res = await getPortraitCompare(val.join(','))
      if (res.code === 200) {
        emit('update:compareData', res.data)
      }
    } catch { compareUserIds.value = [] }
  } else if (val && val.length < 2) {
    showCompare.value = false
    emit('update:compareData', null)
  }
}

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  if (chartInstance) chartInstance.dispose()
  window.removeEventListener('resize', handleResize)
})

defineExpose({ refreshChart })
</script>

<style scoped>
.radar-container { 
  width: 100%; 
}

.radar-container.is-mobile {
  padding: 0;
}

/* ========== 移动端控制面板 ========== */
.mobile-controls {
  margin-bottom: 12px;
}

.compare-row {
  margin-top: 8px;
}

/* ========== PC端控制面板 ========== */
.chart-controls { 
  margin-bottom: 10px; 
  display: flex; 
  align-items: center; 
  flex-wrap: wrap; 
  gap: 8px; 
}

/* ========== 图表容器 ========== */
.chart { 
  width: 100%; 
  height: 420px; 
}

.chart.is-mobile {
  height: 300px;
}
</style>
