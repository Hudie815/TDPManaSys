package com.portrait.vo.mobile;

import com.fasterxml.jackson.annotation.JsonView;
import com.portrait.view.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 移动端专利 VO（精简版）
 * 
 * 使用 @JsonView 控制返回字段，减少移动端数据传输
 */
@Data
@Schema(description = "移动端专利数据（精简版）")
public class MobilePatentVO {
    
    // ========== 基础字段 ==========
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "专利ID")
    private Long id;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "专利名称")
    private String name;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "专利类型")
    private String patentType;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "申请号")
    private String applicationNo;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "专利状态")
    private String status;
    
    @JsonView({Views.MobileView.class, Views.MobileDetailView.class})
    @Schema(description = "教师姓名")
    private String teacherName;
    
    // ========== 详情字段 ==========
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "用户ID")
    private Long userId;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "公开号")
    private String publicationNo;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "申请日期")
    private LocalDate applicationDate;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "授权日期")
    private LocalDate grantDate;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "第一发明人")
    private String firstInventor;
    
    @JsonView(Views.MobileDetailView.class)
    @Schema(description = "发明人列表")
    private String inventors;
}