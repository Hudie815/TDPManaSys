package com.portrait.controller.mobile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonView;
import com.portrait.annotation.RequireRole;
import com.portrait.common.PageQuery;
import com.portrait.common.PageResult;
import com.portrait.common.Result;
import com.portrait.dto.mobile.BatchOperationDTO;
import com.portrait.dto.mobile.BatchResultDTO;
import com.portrait.dto.mobile.ImageUploadDTO;
import com.portrait.entity.User;
import com.portrait.service.MobileApiService;
import com.portrait.service.UserService;
import com.portrait.view.Views;
import com.portrait.vo.mobile.MobileDashboardVO;
import com.portrait.vo.mobile.MobilePatentVO;
import com.portrait.vo.mobile.MobileProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端专用 API Controller
 * 
 * 特性：
 * 1. 路径前缀 /api/mobile/*
 * 2. 使用 @JsonView 控制返回字段精简
 * 3. 分页默认 10 条
 * 4. 支持批量操作减少请求次数
 * 
 * API 版本控制：
 * - Header: X-API-Version: v1/v2
 * - 或 Query: ?version=v1/v2
 */
@Slf4j
@Tag(name = "移动端API", description = "专为移动端优化的接口")
@RestController
@RequestMapping("/api/mobile")
public class MobileApiController {
    
    @Resource
    private MobileApiService mobileApiService;
    
    @Resource
    private UserService userService;
    
    // ========== 仪表盘接口 ==========
    
    @Operation(summary = "获取移动端首页概览")
    @GetMapping("/dashboard")
    public Result<MobileDashboardVO> getDashboard() {
        return Result.success(mobileApiService.getMobileDashboard());
    }
    
    @Operation(summary = "获取移动端首页概览（指定用户）")
    @GetMapping("/dashboard/{userId}")
    @RequireRole("ADMIN")
    public Result<MobileDashboardVO> getDashboardByUserId(@PathVariable Long userId) {
        return Result.success(mobileApiService.getMobileDashboard(userId));
    }
    
    // ========== 项目接口（精简版）==========
    
    @Operation(summary = "获取纵向项目列表（移动端精简）")
    @GetMapping("/vertical-projects")
    @JsonView(Views.MobileView.class)
    public Result<PageResult<MobileProjectVO>> listVerticalProjects(
            @Parameter(description = "查询关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Validated PageQuery pageQuery) {
        pageQuery.setClient("mobile"); // 强制移动端
        Page<MobileProjectVO> page = mobileApiService.listVerticalProjects(keyword, status, pageQuery);
        return Result.success(PageResult.of(page));
    }
    
    @Operation(summary = "获取纵向项目详情（移动端）")
    @GetMapping("/vertical-projects/{id}")
    @JsonView(Views.MobileDetailView.class)
    public Result<MobileProjectVO> getVerticalProjectDetail(@PathVariable Long id) {
        return Result.success(mobileApiService.getVerticalProjectDetail(id));
    }
    
    @Operation(summary = "获取横向项目列表（移动端精简）")
    @GetMapping("/horizontal-projects")
    @JsonView(Views.MobileView.class)
    public Result<PageResult<MobileProjectVO>> listHorizontalProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @Validated PageQuery pageQuery) {
        pageQuery.setClient("mobile");
        Page<MobileProjectVO> page = mobileApiService.listHorizontalProjects(keyword, status, pageQuery);
        return Result.success(PageResult.of(page));
    }
    
    @Operation(summary = "获取横向项目详情（移动端）")
    @GetMapping("/horizontal-projects/{id}")
    @JsonView(Views.MobileDetailView.class)
    public Result<MobileProjectVO> getHorizontalProjectDetail(@PathVariable Long id) {
        return Result.success(mobileApiService.getHorizontalProjectDetail(id));
    }
    
    // ========== 专利接口（精简版）==========
    
    @Operation(summary = "获取专利列表（移动端精简）")
    @GetMapping("/patents")
    @JsonView(Views.MobileView.class)
    public Result<PageResult<MobilePatentVO>> listPatents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @Validated PageQuery pageQuery) {
        pageQuery.setClient("mobile");
        Page<MobilePatentVO> page = mobileApiService.listPatents(keyword, status, pageQuery);
        return Result.success(PageResult.of(page));
    }
    
    @Operation(summary = "获取专利详情（移动端）")
    @GetMapping("/patents/{id}")
    @JsonView(Views.MobileDetailView.class)
    public Result<MobilePatentVO> getPatentDetail(@PathVariable Long id) {
        return Result.success(mobileApiService.getPatentDetail(id));
    }
    
    // ========== 批量操作接口 ==========
    
    @Operation(summary = "批量操作接口")
    @PostMapping("/batch")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result<BatchResultDTO> batchOperation(@Valid @RequestBody BatchOperationDTO dto) {
        long startTime = System.currentTimeMillis();
        BatchResultDTO result = mobileApiService.executeBatchOperation(dto);
        result.setProcessingTime(System.currentTimeMillis() - startTime);
        return Result.success(result);
    }
    
    @Operation(summary = "批量删除")
    @DeleteMapping("/batch/{entityType}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result<BatchResultDTO> batchDelete(
            @PathVariable String entityType,
            @RequestBody List<Long> ids) {
        BatchOperationDTO dto = BatchOperationDTO.delete(entityType, ids);
        return Result.success(mobileApiService.executeBatchOperation(dto));
    }
    
    // ========== 快速查询接口 ==========
    
    @Operation(summary = "获取我的成果统计")
    @GetMapping("/my-stats")
    public Result<Map<String, Object>> getMyStats(HttpServletRequest request) {
        // 从请求属性获取当前用户 ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        Map<String, Object> stats = mobileApiService.getUserStats(userId);
        return Result.success(stats);
    }
    
    @Operation(summary = "快速搜索（全局）")
    @GetMapping("/search")
    public Result<Map<String, Object>> quickSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> results = new HashMap<>();
        results.put("projects", mobileApiService.quickSearchProjects(keyword, limit));
        results.put("patents", mobileApiService.quickSearchPatents(keyword, limit));
        results.put("papers", mobileApiService.quickSearchPapers(keyword, limit));
        return Result.success(results);
    }
    
    // ========== 图片上传接口 ==========
    
    @Operation(summary = "上传图片（自动压缩）")
    @PostMapping("/upload/image")
    public Result<Map<String, String>> uploadImage(@Valid ImageUploadDTO dto) {
        String imageUrl = mobileApiService.uploadAndCompressImage(dto);
        Map<String, String> result = new HashMap<>();
        result.put("url", imageUrl);
        result.put("type", dto.getType());
        return Result.success(result);
    }
    
    @Operation(summary = "上传头像（自动裁剪压缩）")
    @PostMapping("/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpServletRequest request) {
        ImageUploadDTO dto = ImageUploadDTO.avatar(file);
        String imageUrl = mobileApiService.uploadAndCompressImage(dto);
        // 返回头像 URL（暂不更新用户表，因为 User 实体无 avatar 字段）
        Map<String, String> result = new HashMap<>();
        result.put("url", imageUrl);
        return Result.success(result);
    }
}