/**
 * 移动端专用 API 接口
 * 
 * 特性：
 * 1. 分页默认 10 条
 * 2. 数据精简（返回字段减少）
 * 3. 支持批量操作
 * 4. 图片压缩上传
 */

import request from './request'

// ========== 仪表盘接口 ==========

/**
 * 获取移动端首页概览
 */
export function getMobileDashboard() {
  return request({
    url: '/api/mobile/dashboard',
    method: 'get'
  })
}

/**
 * 获取指定用户的仪表盘数据
 * @param {number} userId - 用户ID
 */
export function getMobileDashboardByUserId(userId) {
  return request({
    url: `/api/mobile/dashboard/${userId}`,
    method: 'get'
  })
}

// ========== 项目接口（精简版）==========

/**
 * 获取纵向项目列表（移动端精简）
 * @param {object} params - 查询参数
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.status - 状态筛选
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页条数（默认10）
 */
export function getMobileVerticalProjects(params) {
  return request({
    url: '/api/mobile/vertical-projects',
    method: 'get',
    params: {
      ...params,
      client: 'mobile',
      size: params.size || 10
    }
  })
}

/**
 * 获取纵向项目详情（移动端）
 * @param {number} id - 项目ID
 */
export function getMobileVerticalProjectDetail(id) {
  return request({
    url: `/api/mobile/vertical-projects/${id}`,
    method: 'get'
  })
}

/**
 * 获取横向项目列表（移动端精简）
 */
export function getMobileHorizontalProjects(params) {
  return request({
    url: '/api/mobile/horizontal-projects',
    method: 'get',
    params: {
      ...params,
      client: 'mobile',
      size: params.size || 10
    }
  })
}

/**
 * 获取横向项目详情（移动端）
 * @param {number} id - 项目ID
 */
export function getMobileHorizontalProjectDetail(id) {
  return request({
    url: `/api/mobile/horizontal-projects/${id}`,
    method: 'get'
  })
}

// ========== 专利接口（精简版）==========

/**
 * 获取专利列表（移动端精简）
 */
export function getMobilePatents(params) {
  return request({
    url: '/api/mobile/patents',
    method: 'get',
    params: {
      ...params,
      client: 'mobile',
      size: params.size || 10
    }
  })
}

/**
 * 获取专利详情（移动端）
 * @param {number} id - 专利ID
 */
export function getMobilePatentDetail(id) {
  return request({
    url: `/api/mobile/patents/${id}`,
    method: 'get'
  })
}

// ========== 批量操作接口 ==========

/**
 * 批量操作接口
 * @param {object} data - 操作数据
 * @param {string} data.operation - 操作类型（batch_create/batch_update/batch_delete）
 * @param {string} data.entityType - 实体类型
 * @param {array} data.items - 数据列表
 * @param {array} data.ids - ID列表（用于批量删除）
 */
export function batchOperation(data) {
  return request({
    url: '/api/mobile/batch',
    method: 'post',
    data
  })
}

/**
 * 批量删除
 * @param {string} entityType - 实体类型
 * @param {array} ids - ID列表
 */
export function batchDelete(entityType, ids) {
  return request({
    url: `/api/mobile/batch/${entityType}`,
    method: 'delete',
    data: ids
  })
}

// ========== 快速查询接口 ==========

/**
 * 获取我的成果统计
 */
export function getMyStats() {
  return request({
    url: '/api/mobile/my-stats',
    method: 'get'
  })
}

/**
 * 快速搜索（全局）
 * @param {string} keyword - 搜索关键词
 * @param {number} limit - 结果数量限制
 */
export function quickSearch(keyword, limit = 10) {
  return request({
    url: '/api/mobile/search',
    method: 'get',
    params: { keyword, limit }
  })
}

// ========== 图片上传接口 ==========

/**
 * 上传图片（自动压缩）
 * @param {File} file - 图片文件
 * @param {object} options - 上传选项
 * @param {string} options.type - 图片类型（avatar/document/photo）
 * @param {number} options.maxWidth - 最大宽度
 * @param {number} options.maxHeight - 最大高度
 * @param {number} options.quality - 图片质量（0.1-1.0）
 */
export function uploadImage(file, options = {}) {
  const formData = new FormData()
  formData.append('file', file)
  
  if (options.type) formData.append('type', options.type)
  if (options.maxWidth) formData.append('maxWidth', options.maxWidth)
  if (options.maxHeight) formData.append('maxHeight', options.maxHeight)
  if (options.quality) formData.append('quality', options.quality)
  if (options.outputFormat) formData.append('outputFormat', options.outputFormat)
  
  return request({
    url: '/api/mobile/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传头像（自动裁剪压缩）
 * @param {File} file - 图片文件
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/mobile/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default {
  getMobileDashboard,
  getMobileDashboardByUserId,
  getMobileVerticalProjects,
  getMobileVerticalProjectDetail,
  getMobileHorizontalProjects,
  getMobileHorizontalProjectDetail,
  getMobilePatents,
  getMobilePatentDetail,
  batchOperation,
  batchDelete,
  getMyStats,
  quickSearch,
  uploadImage,
  uploadAvatar
}