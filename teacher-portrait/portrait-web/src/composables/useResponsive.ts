/**
 * 响应式布局组合式函数
 * 支持桌面端（1920x1080）、平板（768x1024）、手机（375x667）三端
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'

// ========== 响应式断点定义 ==========
export const BREAKPOINTS = {
  xs: 375,   // 手机端
  sm: 576,   // 小屏手机
  md: 768,   // 平板
  lg: 992,   // 小桌面
  xl: 1200,  // 桌面端
  xxl: 1920  // 大桌面
} as const

export type BreakpointKey = keyof typeof BREAKPOINTS

// ========== 设备类型枚举 ==========
export type DeviceType = 'mobile' | 'tablet' | 'desktop'

// ========== 响应式状态接口 ==========
export interface ResponsiveState {
  width: number
  height: number
  deviceType: DeviceType
  breakpoint: BreakpointKey
  isMobile: boolean
  isTablet: boolean
  isDesktop: boolean
  isLandscape: boolean
  orientation: 'portrait' | 'landscape'
}

/**
 * 响应式布局组合式函数
 * 
 * 使用方式：
 * ```ts
 * import { useResponsive } from '@/composables/useResponsive'
 * 
 * const responsive = useResponsive()
 * 
 * // 判断设备类型
 * if (responsive.isMobile.value) {
 *   // 移动端逻辑
 * }
 * ```
 */
export function useResponsive() {
  // 当前窗口宽度
  const width = ref(window.innerWidth)
  // 当前窗口高度
  const height = ref(window.innerHeight)
  
  // ========== 计算属性 ==========
  
  // 当前断点
  const breakpoint = computed<BreakpointKey>(() => {
    const w = width.value
    if (w < BREAKPOINTS.sm) return 'xs'
    if (w < BREAKPOINTS.md) return 'sm'
    if (w < BREAKPOINTS.lg) return 'md'
    if (w < BREAKPOINTS.xl) return 'lg'
    if (w < BREAKPOINTS.xxl) return 'xl'
    return 'xxl'
  })
  
  // 设备类型判断
  const deviceType = computed<DeviceType>(() => {
    const w = width.value
    if (w < BREAKPOINTS.md) return 'mobile'
    if (w < BREAKPOINTS.xl) return 'tablet'
    return 'desktop'
  })
  
  // 是否为手机端（xs, sm）
  const isMobile = computed(() => deviceType.value === 'mobile')
  
  // 是否为平板端（md, lg）
  const isTablet = computed(() => deviceType.value === 'tablet')
  
  // 是否为桌面端（xl, xxl）
  const isDesktop = computed(() => deviceType.value === 'desktop')
  
  // 是否为横屏
  const isLandscape = computed(() => width.value > height.value)
  
  // 屏幕方向
  const orientation = computed<'portrait' | 'landscape'>(() => 
    isLandscape.value ? 'landscape' : 'portrait'
  )
  
  // ========== 响应式栅格配置 ==========
  
  /**
   * 获取当前断点下的栅格配置
   * 用于 Element Plus 的 el-row / el-col
   */
  const gridConfig = computed(() => ({
    span: isMobile.value ? 24 : isTablet.value ? 12 : 6,
    offset: 0,
    gutter: isMobile.value ? 12 : isTablet.value ? 16 : 20
  }))
  
  /**
   * 根据断点返回不同的栅格 span 值
   * @param config - 各断点的 span 配置
   */
  const getSpan = (config: Partial<Record<BreakpointKey, number>>) => {
    const bp = breakpoint.value
    // 从当前断点开始向上查找
    const keys: BreakpointKey[] = ['xs', 'sm', 'md', 'lg', 'xl', 'xxl']
    const currentIndex = keys.indexOf(bp)
    
    for (let i = currentIndex; i >= 0; i--) {
      const key = keys[i]
      if (config[key] !== undefined) {
        return config[key]
      }
    }
    return 24 // 默认全宽
  }
  
  // ========== 尺寸计算工具 ==========
  
  /**
   * 根据断点计算合适的尺寸
   * @param sizes - 各断点的尺寸配置
   */
  const getSize = (sizes: Partial<Record<DeviceType, number>>) => {
    const dt = deviceType.value
    if (sizes[dt] !== undefined) return sizes[dt]
    // 默认值优先级：desktop > tablet > mobile
    return sizes.desktop ?? sizes.tablet ?? sizes.mobile ?? 0
  }
  
  /**
   * 计算 ECharts 图表尺寸
   * 根据容器宽度和设备类型自动调整
   */
  const getChartSize = (baseWidth: number, baseHeight: number) => {
    const scale = isMobile.value ? 0.6 : isTablet.value ? 0.8 : 1
    return {
      width: Math.floor(baseWidth * scale),
      height: Math.floor(baseHeight * scale)
    }
  }
  
  // ========== resize 监听 ==========
  
  const handleResize = () => {
    width.value = window.innerWidth
    height.value = window.innerHeight
  }
  
  // 使用 ResizeObserver 监听（可选）
  const setupResizeObserver = (element: HTMLElement, callback: () => void) => {
    const observer = new ResizeObserver(callback)
    observer.observe(element)
    return observer
  }
  
  // ========== 生命周期 ==========
  
  onMounted(() => {
    window.addEventListener('resize', handleResize)
    // 初始化时立即获取尺寸
    handleResize()
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
  })
  
  // ========== 返回响应式状态 ==========
  
  return {
    // 基础状态
    width,
    height,
    breakpoint,
    deviceType,
    orientation,
    
    // 设备类型判断
    isMobile,
    isTablet,
    isDesktop,
    isLandscape,
    
    // 工具函数
    gridConfig,
    getSpan,
    getSize,
    getChartSize,
    
    // ResizeObserver
    setupResizeObserver,
    
    // 断点常量
    BREAKPOINTS
  }
}

/**
 * 表格响应式配置
 * 用于 el-table 的 max-height 和列显示控制
 */
export function useTableResponsive() {
  const { isMobile, height, deviceType } = useResponsive()
  
  // 表格最大高度（根据设备类型调整）
  const tableMaxHeight = computed(() => {
    if (isMobile.value) return Math.floor(height.value * 0.4) // 手机端 40%
    return Math.floor(height.value * 0.6) // 其他设备 60%
  })
  
  // 是否显示完整表格（桌面端）
  const showFullTable = computed(() => deviceType.value === 'desktop')
  
  // 是否使用卡片列表视图（移动端）
  const useCardView = computed(() => isMobile.value)
  
  return {
    tableMaxHeight,
    showFullTable,
    useCardView,
    isMobile
  }
}

/**
 * 表单响应式配置
 * 用于 el-form 的布局控制
 */
export function useFormResponsive() {
  const { isMobile, isTablet, gridConfig } = useResponsive()
  
  // 表单布局：移动端单列，桌面端多列
  const formLayout = computed(() => ({
    labelPosition: isMobile.value ? 'top' : 'right',
    labelWidth: isMobile.value ? '100%' : '120px',
    inline: false,
    columns: isMobile.value ? 1 : isTablet.value ? 2 : 3
  }))
  
  // 表单项 span 配置
  const formItemSpan = computed(() => {
    if (isMobile.value) return 24
    if (isTablet.value) return 12
    return 8 // 桌面端三列
  })
  
  return {
    formLayout,
    formItemSpan,
    gridConfig,
    isMobile
  }
}

/**
 * 导航菜单响应式配置
 * 用于侧边栏和移动端菜单
 */
export function useMenuResponsive() {
  const { isMobile, isTablet, width } = useResponsive()
  
  // 侧边栏是否折叠
  const sidebarCollapsed = ref(isMobile.value || isTablet.value)
  
  // 是否使用抽屉菜单（移动端）
  const useDrawerMenu = computed(() => isMobile.value)
  
  // 是否使用底部 TabBar（移动端）
  const useBottomTabBar = computed(() => isMobile.value)
  
  // 侧边栏宽度
  const sidebarWidth = computed(() => {
    if (isMobile.value) return 0 // 移动端隐藏侧边栏
    if (isTablet.value) return 64 // 折叠状态
    return 230 // 桌面端展开
  })
  
  // 监听窗口变化自动调整侧边栏状态
  const handleDeviceChange = () => {
    sidebarCollapsed.value = isMobile.value || isTablet.value
  }
  
  return {
    sidebarCollapsed,
    useDrawerMenu,
    useBottomTabBar,
    sidebarWidth,
    handleDeviceChange,
    isMobile
  }
}

// ========== CSS 变量导出（用于样式） ==========
export const RESPONSIVE_CSS_VARS = `
  /* 响应式断点 CSS 变量 */
  :root {
    --breakpoint-xs: ${BREAKPOINTS.xs}px;
    --breakpoint-sm: ${BREAKPOINTS.sm}px;
    --breakpoint-md: ${BREAKPOINTS.md}px;
    --breakpoint-lg: ${BREAKPOINTS.lg}px;
    --breakpoint-xl: ${BREAKPOINTS.xl}px;
    --breakpoint-xxl: ${BREAKPOINTS.xxl}px;
  }
`

export default useResponsive