<template>
  <div class="trend-container" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端：下拉菜单形式 -->
    <el-select v-if="isMobile" v-model="activeTab" size="small" style="width: 100%; margin-bottom: 8px;" @change="refreshChart">
      <el-option label="项目数量" value="project" />
      <el-option label="专利" value="patent" />
      <el-option label="软著" value="software" />
      <el-option label="论文" value="paper" />
      <el-option label="竞赛" value="competition" />
    </el-select>
    
    <!-- PC端：按钮组形式 -->
    <el-radio-group v-else v-model="activeTab" size="small" @change="refreshChart">
      <el-radio-button label="project">项目数量</el-radio-button>
      <el-radio-button label="patent">专利</el-radio-button>
      <el-radio-button label="software">软著</el-radio-button>
      <el-radio-button label="paper">论文</el-radio-button>
      <el-radio-button label="competition">竞赛</el-radio-button>
    </el-radio-group>
    
    <div ref="chartRef" class="chart" :class="{ 'is-mobile': isMobile }"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ trendData: Array, isMobile: Boolean })
const activeTab = ref('project')
const chartRef = ref(null)
let chartInstance = null

function getField(tab) {
  const map = { project: 'projectCount', patent: 'patentCount', software: 'softwareCount', paper: 'paperCount', competition: 'competitionCount' }
  return map[tab] || 'projectCount'
}

function buildOption() {
  if (!props.trendData || props.trendData.length === 0) return {}
  const years = props.trendData.map(d => d.year)
  const field = getField(activeTab.value)
  const vals = props.trendData.map(d => Number(d[field] || 0))

  const series = [{
    name: '数量', type: 'bar', data: vals,
    itemStyle: { color: '#4a7ebf', borderRadius: [4, 4, 0, 0] },
    barWidth: props.isMobile ? 20 : 'auto'
  }]
  const yAxes = [{ type: 'value', name: '数量', minInterval: 1 }]

  if (activeTab.value === 'project') {
    const funds = props.trendData.map(d => Number(d.projectFunding || 0))
    series.push({
      name: '经费(万元)', type: 'line', yAxisIndex: 1,
      data: funds, lineStyle: { color: '#c4564e' }, itemStyle: { color: '#c4564e' }
    })
    yAxes.push({ type: 'value', name: '经费(万元)' })
  }

  // 移动端：调整 grid 边距
  const grid = props.isMobile 
    ? { left: 40, right: 40, top: 20, bottom: 40 }
    : { left: 50, right: 50, top: 20, bottom: 40 }

  return {
    tooltip: { trigger: 'axis' },
    legend: { 
      bottom: 0, 
      data: series.map(s => s.name),
      textStyle: { fontSize: props.isMobile ? 11 : 12 }
    },
    grid: grid,
    xAxis: { 
      type: 'category', 
      data: years,
      axisLabel: { fontSize: props.isMobile ? 11 : 12 }
    },
    yAxis: yAxes.map(y => ({ 
      ...y, 
      axisLabel: { fontSize: props.isMobile ? 11 : 12 }
    })),
    series
  }
}

function initChart() {
  if (!chartRef.value || !props.trendData) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(buildOption())
}

function refreshChart() { 
  if (chartInstance) {
    chartInstance.setOption(buildOption(), true)
    chartInstance.resize()
  }
}

// 监听窗口大小变化，触发 resize
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => props.trendData, () => nextTick(initChart), { deep: true })
watch(() => props.isMobile, () => {
  nextTick(() => {
    refreshChart()
  })
})

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  if (chartInstance) chartInstance.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.trend-container { 
  width: 100%; 
}

.trend-container.is-mobile {
  padding: 0;
}

.trend-container > .el-radio-group { 
  margin-bottom: 10px; 
}

.chart { 
  width: 100%; 
  height: 350px; 
}

.chart.is-mobile {
  height: 280px;
}
</style>
