<template>
  <div class="table-card-container">
    <!-- 卡片列表模式 -->
    <div v-if="useCardView" class="card-list">
      <div 
        v-for="item in data" 
        :key="item.id || item[rowKey]" 
        class="card-item"
        @click="$emit('row-click', item)"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <span class="card-title">{{ getItemTitle(item) }}</span>
          <span v-if="statusField" class="card-status" :class="getStatusClass(item)">
            {{ item[statusField] }}
          </span>
        </div>
        
        <!-- 卡片内容 -->
        <div class="card-body">
          <div 
            v-for="field in displayFields" 
            :key="field.prop" 
            class="card-row"
          >
            <span class="card-label">{{ field.label }}：</span>
            <span class="card-value" :class="field.valueClass">
              {{ formatValue(item[field.prop], field) }}
            </span>
          </div>
        </div>
        
        <!-- 卡片操作按钮 -->
        <div v-if="showActions" class="card-actions">
          <el-button 
            v-for="action in actions" 
            :key="action.label"
            :type="action.type || 'default'"
            :size="action.size || 'small'"
            :icon="action.icon"
            @click.stop="$emit(action.event || 'action', item)"
          >
            {{ action.label }}
          </el-button>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="data.length === 0" class="empty-state">
        <el-icon :size="48" color="var(--color-text-light)"><Document /></el-icon>
        <p class="empty-text">暂无数据</p>
      </div>
    </div>
    
    <!-- 表格模式（非移动端） -->
    <el-table
      v-else
      :data="data"
      :max-height="tableMaxHeight"
      :row-key="rowKey"
      stripe
      highlight-current-row
      @row-click="$emit('row-click', $event)"
    >
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :sortable="col.sortable"
        :fixed="col.fixed"
      >
        <template #default="{ row }">
          <template v-if="col.type === 'tag'">
            <el-tag :type="col.tagMap?.[row[col.prop]] || 'info'" size="small">
              {{ row[col.prop] }}
            </el-tag>
          </template>
          <template v-else-if="col.type === 'date'">
            {{ formatDate(row[col.prop]) }}
          </template>
          <template v-else>
            {{ row[col.prop] }}
          </template>
        </template>
      </el-table-column>
      
      <!-- 操作列 -->
      <el-table-column v-if="showActions" label="操作" :width="actionWidth" fixed="right">
        <template #default="{ row }">
          <el-button
            v-for="action in actions"
            :key="action.label"
            :type="action.type || 'primary'"
            :size="action.size || 'small'"
            :icon="action.icon"
            link
            @click="$emit(action.event || 'action', row)"
          >
            {{ action.label }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useResponsive } from '../composables/useResponsive'
import { Document } from '@element-plus/icons-vue'

// ========== Props 定义 ==========
const props = defineProps({
  // 数据源
  data: {
    type: Array,
    default: () => []
  },
  // 行唯一标识字段
  rowKey: {
    type: String,
    default: 'id'
  },
  // 表格列配置
  columns: {
    type: Array,
    default: () => []
  },
  // 卡片模式显示的字段（移动端）
  displayFields: {
    type: Array,
    default: () => []
  },
  // 卡片标题字段
  titleField: {
    type: String,
    default: 'name'
  },
  // 状态字段（用于显示状态标签）
  statusField: {
    type: String,
    default: ''
  },
  // 状态样式映射
  statusClassMap: {
    type: Object,
    default: () => ({})
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 操作按钮配置
  actions: {
    type: Array,
    default: () => [
      { label: '编辑', type: 'primary', event: 'edit', icon: null },
      { label: '删除', type: 'danger', event: 'delete', icon: null }
    ]
  },
  // 操作列宽度
  actionWidth: {
    type: Number,
    default: 150
  },
  // 强制使用卡片模式
  forceCardView: {
    type: Boolean,
    default: false
  }
})

// ========== Emits 定义 ==========
defineEmits(['row-click', 'edit', 'delete', 'action'])

// ========== 响应式配置 ==========
const responsive = useResponsive()

// 是否使用卡片视图
const useCardView = computed(() => props.forceCardView || responsive.isMobile.value)

// 表格最大高度
const tableMaxHeight = computed(() => {
  if (responsive.isMobile.value) return responsive.height.value * 0.4
  return responsive.height.value * 0.6
})

// ========== 辅助函数 ==========
// 获取卡片标题
const getItemTitle = (item) => {
  return item[props.titleField] || '未命名'
}

// 获取状态样式类
const getStatusClass = (item) => {
  const status = item[props.statusField]
  return props.statusClassMap[status] || ''
}

// 格式化字段值
const formatValue = (value, field) => {
  if (field.type === 'date' && value) {
    return formatDate(value)
  }
  if (field.formatter) {
    return field.formatter(value)
  }
  return value || '-'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return dateStr
  }
}
</script>

<style scoped>
.table-card-container {
  width: 100%;
}

/* ========== 卡片列表样式 ========== */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-item {
  background: var(--color-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 16px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.card-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.card-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text-muted);
}

.card-status.success {
  background: rgba(74, 158, 110, 0.1);
  color: var(--color-success);
}

.card-status.warning {
  background: rgba(200, 164, 92, 0.1);
  color: var(--color-warning);
}

.card-status.danger {
  background: rgba(196, 86, 78, 0.1);
  color: var(--color-danger);
}

.card-status.info {
  background: rgba(90, 138, 191, 0.1);
  color: var(--color-info);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-label {
  font-size: 13px;
  color: var(--color-text-muted);
  min-width: 80px;
}

.card-value {
  font-size: 13px;
  color: var(--color-text-primary);
  flex: 1;
  text-align: right;
}

.card-value.highlight {
  color: var(--color-accent);
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}

/* ========== 空状态样式 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: var(--color-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-light);
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-light);
  margin-top: 12px;
}

/* ========== 表格样式优化 ========== */
:deep(.el-table) {
  border-radius: var(--radius-md);
}

:deep(.el-table th.el-table__cell) {
  background: var(--color-surface-warm);
}

:deep(.el-table .el-table__row:hover > td) {
  background: var(--color-accent-glow);
}
</style>