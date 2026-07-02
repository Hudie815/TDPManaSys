package com.portrait.vo.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 移动端成果概览 VO
 * 
 * 用于移动端首页仪表盘，展示关键统计数据
 * 数据精简，只保留核心指标
 */
@Data
@Schema(description = "移动端成果概览")
public class MobileDashboardVO {
    
    @Schema(description = "项目总数")
    private Integer projectTotal;
    
    @Schema(description = "纵向项目数量")
    private Integer verticalProjectCount;
    
    @Schema(description = "横向项目数量")
    private Integer horizontalProjectCount;
    
    @Schema(description = "项目总经费（万元）")
    private BigDecimal totalFunding;
    
    @Schema(description = "已授权专利数量")
    private Integer patentGrantedCount;
    
    @Schema(description = "专利申请中数量")
    private Integer patentPendingCount;
    
    @Schema(description = "软件著作权数量")
    private Integer softwareCount;
    
    @Schema(description = "学术论文总数")
    private Integer paperTotal;
    
    @Schema(description = "A类论文数量")
    private Integer paperACount;
    
    @Schema(description = "竞赛获奖数量")
    private Integer competitionAwardCount;
    
    @Schema(description = "教师姓名")
    private String teacherName;
    
    @Schema(description = "所属学院")
    private String college;
    
    /**
     * 计算综合得分（用于快速评估）
     */
    @Schema(description = "综合得分（百分制）")
    private BigDecimal overallScore;
    
    /**
     * 最近更新时间
     */
    @Schema(description = "数据更新时间")
    private String updateTime;
}