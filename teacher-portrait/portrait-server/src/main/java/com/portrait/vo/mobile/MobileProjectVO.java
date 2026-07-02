package com.portrait.vo.mobile;

import com.fasterxml.jackson.annotation.JsonView;
import com.portrait.view.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 移动端项目 VO（精简版）
 * 
 * 使用 @JsonView 控制返回字段：
 * - MobileView：列表页精简字段
 * - MobileDetailView：详情页更多字段
 * 
 * 相比完整 VO，移动端 VO 减少了约 40% 的字段传输
 */
@Data
@Schema(description = "移动端项目数据（精简版）")
public class MobileProjectVO {
    
    // ========== 基础字段（MobileView 和 MobileDetailView 都返回）==========
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "项目ID")
    private Long id;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "项目名称")
    private String name;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "项目编号")
    private String projectNo;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "项目级别")
    private String level;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "项目状态")
    private String status;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "教师姓名")
    private String teacherName;
    
    // ========== 详情字段（仅 MobileDetailView 返回）==========
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "用户ID")
    private Long userId;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "来源单位")
    private String sourceUnit;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "计划结束日期")
    private LocalDate plannedEndDate;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "项目经费（万元）")
    private BigDecimal funding;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "承担角色")
    private String role;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "所属学院")
    private String teacherCollege;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "备注")
    private String remark;
}