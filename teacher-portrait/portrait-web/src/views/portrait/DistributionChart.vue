<template>
  <div class="dist-container" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端：下拉菜单形式 -->
    <el-select v-if="isMobile" v-model="activeTab" size="small" style="width: 100%; margin-bottom: 8px;" @change="loadData">
      <el-option label="项目级别" value="projectLevel" />
      <el-option label="专利类型" value="patentType" />
      <el-option label="论文类别" value="paperClass" />
      <el-option label="竞赛级别" value="compLevel" />
    </el-select>
    
    <!-- PC端：按钮组形式 -->
    <el-radio-group v-else v-model="activeTab" size="small" @change="loadData">
      <el-radio-button label="projectLevel">项目级别</el-radio-button>
      <el-radio-button label="patentType">专利类型</el-radio-button>
      <el-radio-button label="paperClass">论文类别</el-radio-button>
      <el-radio-button label="compLevel">竞赛级别</el-radio-button>
    </el-radio-group>
    
    <div ref="chartRef" class="chart" :class="{ 'is-mobile': isMobile }"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

const props = defineProps({ userId: Number, isMobile: Boolean })
const activeTab = ref('projectLevel')
const chartRef = ref(null)
let chartInstance = null

const COLORS = ['#4a7ebf', '#4a9e6e', '#c8a45c', '#c4564e', '#5a8abf', '#bf8a3a', '#6a5abf', '#4a9e9e']

async function loadData() {
  if (!props.userId) return
  try {
    const res = await request({ url: `/portrait/${props.userId}/distribution`, method: 'get' })
    if (res.code !== 200) return
    const dist = res.data
    let data = []
    switch (activeTab.value) {
      case 'projectLevel': data = dist.projectLevel || []; break
      case 'patentType': data = dist.patentType || []; break
      case 'paperClass': data = dist.paperClass || []; break
      case 'compLevel': data = dist.competitionLevel || []; break
    }
    renderChart(data)
  } catch { }
}

function renderChart(data) {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  if (!data || data.length === 0) {
    chartInstance.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }
  
  // 移动端：调整饼图位置和大小
  const radius = props.isMobile ? ['35%', '60%'] : ['40%', '70%']
  const center = props.isMobile ? ['50%', '50%'] : ['40%', '50%']
  
  chartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { 
      orient: props.isMobile ? 'horizontal' : 'vertical', 
      right: props.isMobile ? 'auto' : 10, 
      bottom: props.isMobile ? 0 : 'center',
      top: props.isMobile ? 'auto' : 'center',
      textStyle: { fontSize: props.isMobile ? 11 : 12 }
    },
    series: [{
      type: 'pie',
      radius: radius,
      center: center,
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: props.isMobile ? 14 : 16, fontWeight: 'bold' } },
      data: data.map((d, i) => ({ ...d, itemStyle: { color: COLORS[i % COLORS.length] } }))
    }]
  })
}

// 监听窗口大小变化，触发 resize
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => props.userId, () => { nextTick(loadData) })
watch(() => props.isMobile, () => {
  nextTick(() => {
    loadData()
    if (chartInstance) chartInstance.resize()
  })
})

onMounted(() => {
  nextTick(loadData)
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  if (chartInstance) chartInstance.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dist-container { 
  width: 100%; 
}

.dist-container.is-mobile {
  padding: 0;
}

.dist-container > .el-radio-group { 
  margin-bottom: 10px; 
}

.chart { 
  width: 100%; 
  height: 320px; 
}

.chart.is-mobile {
  height: 260px;
}
</style>