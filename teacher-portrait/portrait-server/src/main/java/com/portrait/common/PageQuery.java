package com.portrait.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询参数适配
 * 
 * 支持多端不同的分页大小配置：
 * - 桌面端默认 20 条
 * - 移动端默认 10 条
 * - 可选值：10 / 20 / 50 / 100
 * 
 * 使用方式：
 * <code>
 * @GetMapping("/list")
 * public Result<PageResult<VO>> list(PageQuery pageQuery, QueryDTO query) {
 *     return Result.success(PageResult.of(service.page(query, pageQuery.toPage())));
 * }
 * </code>
 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery {
    
    @Schema(description = "当前页码", defaultValue = "1")
    private Long page = 1L;
    
    @Schema(description = "每页条数（可选值：10/20/50/100）", defaultValue = "20")
    private Long size = 20L;
    
    @Schema(description = "客户端类型（desktop/mobile）", defaultValue = "desktop")
    private String client = "desktop";
    
    @Schema(description = "API 版本（v1/v2）", defaultValue = "v1")
    private String version = "v1";
    
    /**
     * 获取合法的分页大小
     * 限制可选值：10 / 20 / 50 / 100
     * 移动端默认 10，桌面端默认 20
     */
    public Long getValidSize() {
        // 根据客户端类型设置默认值
        if (size == null || size <= 0) {
            return isMobile() ? 10L : 20L;
        }
        
        // 限制可选值范围
        if (size <= 10) return 10L;
        if (size <= 20) return 20L;
        if (size <= 50) return 50L;
        return 100L;
    }
    
    /**
     * 判断是否为移动端请求
     */
    public boolean isMobile() {
        return "mobile".equalsIgnoreCase(client);
    }
    
    /**
     * 判断是否为 V2 版本 API
     */
    public boolean isV2() {
        return "v2".equalsIgnoreCase(version);
    }
    
    /**
     * 转换为 MyBatis-Plus 的 Page 对象
     */
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> toPage() {
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<T>(
            page != null && page > 0 ? page : 1L,
            getValidSize()
        );
    }
    
    /**
     * 创建移动端默认分页参数
     */
    public static PageQuery mobile() {
        PageQuery query = new PageQuery();
        query.setClient("mobile");
        query.setSize(10L);
        return query;
    }
    
    /**
     * 创建桌面端默认分页参数
     */
    public static PageQuery desktop() {
        PageQuery query = new PageQuery();
        query.setClient("desktop");
        query.setSize(20L);
        return query;
    }
}