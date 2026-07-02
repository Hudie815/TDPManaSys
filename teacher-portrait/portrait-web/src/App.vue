<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --color-primary: #1a2744;
  --color-primary-light: #243354;
  --color-primary-lighter: #2d3f66;
  --color-accent: #c8a45c;
  --color-accent-light: #dfc07a;
  --color-accent-glow: rgba(200, 164, 92, 0.15);
  --color-surface: #f7f6f3;
  --color-surface-warm: #faf8f4;
  --color-card: #ffffff;
  --color-card-hover: #fffdf8;
  --color-border: #e8e4db;
  --color-border-light: #f0ece3;
  --color-text-primary: #1a2744;
  --color-text-secondary: #5a6478;
  --color-text-muted: #8e96a6;
  --color-text-light: #b8bfcc;
  --color-success: #4a9e6e;
  --color-warning: #c8a45c;
  --color-danger: #c4564e;
  --color-info: #5a8abf;
  --color-blue: #4a7ebf;
  --color-teal: #4a9e9e;
  --color-amber: #bf8a3a;
  --color-rose: #bf5a6e;
  --color-indigo: #6a5abf;
  --font-display: 'Noto Serif SC', 'Songti SC', serif;
  --font-body: 'Inter', -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --shadow-sm: 0 1px 3px rgba(26, 39, 68, 0.04), 0 1px 2px rgba(26, 39, 68, 0.06);
  --shadow-md: 0 4px 12px rgba(26, 39, 68, 0.06), 0 2px 4px rgba(26, 39, 68, 0.04);
  --shadow-lg: 0 12px 40px rgba(26, 39, 68, 0.08), 0 4px 12px rgba(26, 39, 68, 0.04);
  --shadow-xl: 0 24px 64px rgba(26, 39, 68, 0.12), 0 8px 24px rgba(26, 39, 68, 0.06);
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-base: 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-smooth: 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  
  /* ========== 响应式断点 CSS 变量 ========== */
  --breakpoint-xs: 375px;
  --breakpoint-sm: 576px;
  --breakpoint-md: 768px;
  --breakpoint-lg: 992px;
  --breakpoint-xl: 1200px;
  --breakpoint-xxl: 1920px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: var(--font-body);
  color: var(--color-text-primary);
  background: var(--color-surface);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

::selection {
  background: var(--color-accent-glow);
  color: var(--color-primary);
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted);
}

.el-card {
  border: 1px solid var(--color-border-light) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-sm) !important;
  transition: all var(--transition-base) !important;
}
.el-card:hover {
  box-shadow: var(--shadow-md) !important;
}
.el-card__header {
  border-bottom: 1px solid var(--color-border-light) !important;
  padding: 14px 20px !important;
}

.el-button--primary {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}
.el-button--primary:hover {
  background: var(--color-primary-light) !important;
  border-color: var(--color-primary-light) !important;
}

.el-table th.el-table__cell {
  background: var(--color-surface-warm) !important;
  color: var(--color-text-secondary) !important;
  font-weight: 500 !important;
  font-size: 13px !important;
}

.el-table .el-table__row:hover > td {
  background: var(--color-accent-glow) !important;
}

.el-tag {
  border-radius: var(--radius-sm) !important;
}

.el-input__wrapper {
  border-radius: var(--radius-sm) !important;
}

.el-dialog {
  border-radius: var(--radius-lg) !important;
}

.el-message-box {
  border-radius: var(--radius-lg) !important;
}

/* ========== 响应式全局样式 ========== */

/* 移动端（xs: <576px） */
@media (max-width: 575.98px) {
  .el-card__header {
    padding: 12px 16px !important;
  }
  
  .el-card__body {
    padding: 16px !important;
  }
  
  .el-dialog {
    width: 90% !important;
    max-width: 320px !important;
  }
  
  .el-form-item__label {
    font-size: 13px !important;
  }
  
  .el-button {
    padding: 8px 16px !important;
  }
  
  .el-input__wrapper {
    padding: 1px 8px !important;
  }
  
  /* 移动端表格优化 */
  .el-table th.el-table__cell {
    font-size: 12px !important;
    padding: 8px 0 !important;
  }
  
  .el-table td.el-table__cell {
    font-size: 12px !important;
    padding: 8px 0 !important;
  }
}

/* 平板端（sm: 576px - 767px） */
@media (min-width: 576px) and (max-width: 767.98px) {
  .el-dialog {
    width: 80% !important;
    max-width: 480px !important;
  }
  
  .el-card__body {
    padding: 18px !important;
  }
}

/* 小桌面端（md: 768px - 991px） */
@media (min-width: 768px) and (max-width: 991.98px) {
  .el-dialog {
    width: 70% !important;
    max-width: 560px !important;
  }
}

/* 大桌面端（xl: >=1200px） */
@media (min-width: 1200px) {
  .el-dialog {
    width: 50% !important;
    max-width: 640px !important;
  }
}

/* ========== 移动端 Element Plus 组件优化 ========== */

/* 移动端 Dialog 弹窗 */
@media (max-width: 768px) {
  .el-dialog {
    border-radius: var(--radius-md) !important;
    margin-top: 10vh !important;
  }
  
  .el-dialog__header {
    padding: 12px 16px !important;
  }
  
  .el-dialog__body {
    padding: 16px !important;
  }
  
  .el-dialog__footer {
    padding: 12px 16px !important;
  }
  
  /* 移动端 Drawer 抽屉 */
  .el-drawer {
    border-radius: 0 !important;
  }
  
  .el-drawer__header {
    padding: 12px 16px !important;
  }
  
  .el-drawer__body {
    padding: 12px !important;
  }
  
  /* 移动端 Select 选择器 */
  .el-select-dropdown {
    max-width: 100vw !important;
  }
  
  /* 移动端 Menu 菜单 */
  .el-menu--collapse .el-menu-item span,
  .el-menu--collapse .el-sub-menu__title span {
    display: none !important;
  }
  
  /* 移动端 Pagination 分页 */
  .el-pagination {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .el-pagination .el-pagination__sizes,
  .el-pagination .el-pagination__jump {
    margin-left: 0 !important;
    margin-top: 8px;
  }
}

/* ========== 触摸设备优化 ========== */
@media (hover: none) and (pointer: coarse) {
  /* 增大触摸目标尺寸 */
  .el-button {
    min-height: 44px !important;
  }
  
  .el-input__inner {
    min-height: 44px !important;
  }
  
  .el-checkbox__inner {
    width: 20px !important;
    height: 20px !important;
  }
  
  .el-radio__inner {
    width: 20px !important;
    height: 20px !important;
  }
  
  /* 移除 hover 效果 */
  .el-card:hover {
    box-shadow: var(--shadow-sm) !important;
    transform: none !important;
  }
  
  .el-button:hover {
    transform: none !important;
  }
}
</style>