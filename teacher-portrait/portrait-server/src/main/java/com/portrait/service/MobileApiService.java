package com.portrait.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.portrait.common.BusinessException;
import com.portrait.common.PageQuery;
import com.portrait.dto.mobile.BatchOperationDTO;
import com.portrait.dto.mobile.BatchResultDTO;
import com.portrait.dto.mobile.ImageUploadDTO;
import com.portrait.entity.*;
import com.portrait.mapper.*;
import com.portrait.vo.mobile.MobileDashboardVO;
import com.portrait.vo.mobile.MobilePatentVO;
import com.portrait.vo.mobile.MobileProjectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 移动端 API 服务
 * 
 * 提供移动端专用的数据查询和操作接口
 */
@Slf4j
@Service
public class MobileApiService {
    
    @Resource
    private VerticalProjectMapper verticalProjectMapper;
    
    @Resource
    private HorizontalProjectMapper horizontalProjectMapper;
    
    @Resource
    private PatentMapper patentMapper;
    
    @Resource
    private PaperMapper paperMapper;
    
    @Resource
    private SoftwareCopyrightMapper softwareCopyrightMapper;
    
    @Resource
    private CompetitionMapper competitionMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ImageCompressService imageCompressService;
    
    @Resource
    private VerticalProjectService verticalProjectService;
    
    @Resource
    private HorizontalProjectService horizontalProjectService;
    
    @Resource
    private PatentService patentService;
    
    @Resource
    private PaperService paperService;
    
    @Resource
    private SoftwareCopyrightService softwareCopyrightService;
    
    @Resource
    private CompetitionService competitionService;

    @Resource
    private HttpServletRequest request;

    // ========== 仪表盘数据 ==========
    
    /**
     * 获取移动端仪表盘数据
     */
    public MobileDashboardVO getMobileDashboard() {
        // 获取当前用户 ID（从上下文或登录信息）
        Long userId = getCurrentUserId();
        return getMobileDashboard(userId);
    }
    
    /**
     * 获取指定用户的仪表盘数据
     */
    public MobileDashboardVO getMobileDashboard(Long userId) {
        MobileDashboardVO vo = new MobileDashboardVO();
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setTeacherName(user.getName());
            vo.setCollege(user.getCollege());
        }
        
        // 统计纵向项目
        LambdaQueryWrapper<VerticalProject> verticalWrapper = new LambdaQueryWrapper<>();
        verticalWrapper.eq(VerticalProject::getUserId, userId);
        Long verticalCount = verticalProjectMapper.selectCount(verticalWrapper);
        vo.setVerticalProjectCount(verticalCount.intValue());
        
        // 统计横向项目
        LambdaQueryWrapper<HorizontalProject> horizontalWrapper = new LambdaQueryWrapper<>();
        horizontalWrapper.eq(HorizontalProject::getUserId, userId);
        Long horizontalCount = horizontalProjectMapper.selectCount(horizontalWrapper);
        vo.setHorizontalProjectCount(horizontalCount.intValue());
        
        // 项目总数
        vo.setProjectTotal(verticalCount.intValue() + horizontalCount.intValue());
        
        // 项目总经费
        // 这里简化处理，实际应该使用聚合查询
        vo.setTotalFunding(java.math.BigDecimal.ZERO);
        
        // 统计专利
        LambdaQueryWrapper<Patent> patentWrapper = new LambdaQueryWrapper<>();
        patentWrapper.eq(Patent::getUserId, userId);
        patentWrapper.eq(Patent::getStatus, "已授权");
        Long patentGranted = patentMapper.selectCount(patentWrapper);
        vo.setPatentGrantedCount(patentGranted.intValue());
        
        patentWrapper.clear();
        patentWrapper.eq(Patent::getUserId, userId);
        patentWrapper.eq(Patent::getStatus, "申请中");
        Long patentPending = patentMapper.selectCount(patentWrapper);
        vo.setPatentPendingCount(patentPending.intValue());
        
        // 统计软著
        LambdaQueryWrapper<SoftwareCopyright> softwareWrapper = new LambdaQueryWrapper<>();
        softwareWrapper.eq(SoftwareCopyright::getUserId, userId);
        Long softwareCount = softwareCopyrightMapper.selectCount(softwareWrapper);
        vo.setSoftwareCount(softwareCount.intValue());
        
        // 统计论文
        LambdaQueryWrapper<Paper> paperWrapper = new LambdaQueryWrapper<>();
        paperWrapper.eq(Paper::getUserId, userId);
        Long paperTotal = paperMapper.selectCount(paperWrapper);
        vo.setPaperTotal(paperTotal.intValue());
        
        // 统计竞赛
        LambdaQueryWrapper<Competition> competitionWrapper = new LambdaQueryWrapper<>();
        competitionWrapper.eq(Competition::getUserId, userId);
        Long competitionCount = competitionMapper.selectCount(competitionWrapper);
        vo.setCompetitionAwardCount(competitionCount.intValue());
        
        vo.setUpdateTime(new Date().toString());
        return vo;
    }
    
    // ========== 项目列表（精简版）==========
    
    /**
     * 获取纵向项目列表（移动端）
     * 优化：批量查询用户信息，避免N+1查询问题
     */
    public Page<MobileProjectVO> listVerticalProjects(String keyword, String status, PageQuery pageQuery) {
        Page<VerticalProject> page = pageQuery.toPage();
        LambdaQueryWrapper<VerticalProject> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(VerticalProject::getName, keyword)
                   .or()
                   .like(VerticalProject::getProjectNo, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(VerticalProject::getStatus, status);
        }

        // 只查询当前用户的数据（或管理员查询所有）
        Long userId = getCurrentUserId();
        if (!isAdmin()) {
            wrapper.eq(VerticalProject::getUserId, userId);
        }

        wrapper.orderByDesc(VerticalProject::getCreateTime);

        Page<VerticalProject> entityPage = verticalProjectMapper.selectPage(page, wrapper);

        // 批量查询用户信息（优化N+1查询）
        List<Long> userIds = entityPage.getRecords().stream()
                .map(VerticalProject::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        // 转换为移动端 VO
        Page<MobileProjectVO> voPage = new Page<>();
        BeanUtils.copyProperties(entityPage, voPage, "records");
        voPage.setRecords(convertToMobileProjectVO(entityPage.getRecords(), userMap));

        return voPage;
    }
    
    /**
     * 获取纵向项目详情（移动端）
     */
    public MobileProjectVO getVerticalProjectDetail(Long id) {
        VerticalProject project = verticalProjectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return convertToMobileProjectVO(project);
    }
    
    /**
     * 获取横向项目列表（移动端）
     * 优化：批量查询用户信息，避免N+1查询问题
     */
    public Page<MobileProjectVO> listHorizontalProjects(String keyword, String status, PageQuery pageQuery) {
        Page<HorizontalProject> page = pageQuery.toPage();
        LambdaQueryWrapper<HorizontalProject> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(HorizontalProject::getName, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(HorizontalProject::getStatus, status);
        }

        Long userId = getCurrentUserId();
        if (!isAdmin()) {
            wrapper.eq(HorizontalProject::getUserId, userId);
        }

        wrapper.orderByDesc(HorizontalProject::getCreateTime);

        Page<HorizontalProject> entityPage = horizontalProjectMapper.selectPage(page, wrapper);

        // 批量查询用户信息（优化N+1查询）
        List<Long> userIds = entityPage.getRecords().stream()
                .map(HorizontalProject::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Page<MobileProjectVO> voPage = new Page<>();
        BeanUtils.copyProperties(entityPage, voPage, "records");
        voPage.setRecords(convertHorizontalToMobileVO(entityPage.getRecords(), userMap));

        return voPage;
    }
    
    /**
     * 获取横向项目详情（移动端）
     */
    public MobileProjectVO getHorizontalProjectDetail(Long id) {
        HorizontalProject project = horizontalProjectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return convertHorizontalToMobileVO(project);
    }
    
    // ========== 专利列表（精简版）==========
    
    /**
     * 获取专利列表（移动端）
     * 优化：批量查询用户信息，避免N+1查询问题
     */
    public Page<MobilePatentVO> listPatents(String keyword, String status, PageQuery pageQuery) {
        Page<Patent> page = pageQuery.toPage();
        LambdaQueryWrapper<Patent> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Patent::getName, keyword)
                   .or()
                   .like(Patent::getApplicationNo, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Patent::getStatus, status);
        }

        Long userId = getCurrentUserId();
        if (!isAdmin()) {
            wrapper.eq(Patent::getUserId, userId);
        }

        wrapper.orderByDesc(Patent::getCreateTime);

        Page<Patent> entityPage = patentMapper.selectPage(page, wrapper);

        // 批量查询用户信息（优化N+1查询）
        List<Long> userIds = entityPage.getRecords().stream()
                .map(Patent::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Page<MobilePatentVO> voPage = new Page<>();
        BeanUtils.copyProperties(entityPage, voPage, "records");
        voPage.setRecords(convertToMobilePatentVO(entityPage.getRecords(), userMap));

        return voPage;
    }
    
    /**
     * 获取专利详情（移动端）
     */
    public MobilePatentVO getPatentDetail(Long id) {
        Patent patent = patentMapper.selectById(id);
        if (patent == null) {
            throw new BusinessException("专利不存在");
        }
        return convertToMobilePatentVO(patent);
    }
    
    // ========== 批量操作 ==========
    
    /**
     * 执行批量操作
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO executeBatchOperation(BatchOperationDTO dto) {
        String entityType = dto.getEntityType();
        String operation = dto.getOperation();
        
        int successCount = 0;
        int failedCount = 0;
        List<BatchResultDTO.FailureDetail> failures = new ArrayList<>();
        
        try {
            switch (entityType) {
                case "vertical_project":
                    successCount = executeBatchForVerticalProject(operation, dto, failures);
                    break;
                case "horizontal_project":
                    successCount = executeBatchForHorizontalProject(operation, dto, failures);
                    break;
                case "patent":
                    successCount = executeBatchForPatent(operation, dto, failures);
                    break;
                case "paper":
                    successCount = executeBatchForPaper(operation, dto, failures);
                    break;
                case "software":
                    successCount = executeBatchForSoftware(operation, dto, failures);
                    break;
                case "competition":
                    successCount = executeBatchForCompetition(operation, dto, failures);
                    break;
                default:
                    throw new BusinessException("不支持的实体类型: " + entityType);
            }
        } catch (Exception e) {
            log.error("批量操作失败", e);
            throw new BusinessException("批量操作失败: " + e.getMessage());
        }
        
        failedCount = getExpectedCount(dto) - successCount;
        
        return BatchResultDTO.partialSuccess(operation, entityType, successCount, failedCount, failures);
    }
    
    // ========== 图片上传 ==========
    
    /**
     * 上传并压缩图片
     */
    public String uploadAndCompressImage(ImageUploadDTO dto) {
        return imageCompressService.compressAndUpload(dto);
    }
    
    // ========== 快速搜索 ==========
    
    /**
     * 快速搜索项目
     */
    public List<MobileProjectVO> quickSearchProjects(String keyword, int limit) {
        limit = Math.min(limit, 20); // 限制最大数量
        
        LambdaQueryWrapper<VerticalProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(VerticalProject::getName, keyword);
        wrapper.last("LIMIT " + limit);
        
        List<VerticalProject> projects = verticalProjectMapper.selectList(wrapper);
        return convertToMobileProjectVO(projects);
    }
    
    /**
     * 快速搜索专利
     */
    public List<MobilePatentVO> quickSearchPatents(String keyword, int limit) {
        limit = Math.min(limit, 20);
        
        LambdaQueryWrapper<Patent> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Patent::getName, keyword);
        wrapper.last("LIMIT " + limit);
        
        List<Patent> patents = patentMapper.selectList(wrapper);
        return convertToMobilePatentVO(patents);
    }
    
    /**
     * 快速搜索论文
     */
    public List<Map<String, Object>> quickSearchPapers(String keyword, int limit) {
        limit = Math.min(limit, 20);
        
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Paper::getTitle, keyword);
        wrapper.last("LIMIT " + limit);
        
        List<Paper> papers = paperMapper.selectList(wrapper);
        return papers.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("title", p.getTitle());
            map.put("journal", p.getJournalName());
            return map;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("projectCount", verticalProjectMapper.selectCount(
            new LambdaQueryWrapper<VerticalProject>().eq(VerticalProject::getUserId, userId)));
        stats.put("patentCount", patentMapper.selectCount(
            new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId)));
        stats.put("paperCount", paperMapper.selectCount(
            new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId)));
        return stats;
    }
    
    // ========== 私有方法 ==========

    /**
     * 批量转换纵向项目为移动端VO（优化：使用预加载的用户Map避免N+1查询）
     */
    private List<MobileProjectVO> convertToMobileProjectVO(List<VerticalProject> projects, Map<Long, User> userMap) {
        return projects.stream()
                .map(p -> convertToMobileProjectVO(p, userMap))
                .collect(Collectors.toList());
    }

    /**
     * 单个纵向项目转换（从userMap获取用户信息）
     */
    private MobileProjectVO convertToMobileProjectVO(VerticalProject project, Map<Long, User> userMap) {
        MobileProjectVO vo = new MobileProjectVO();
        BeanUtils.copyProperties(project, vo);

        User user = userMap.get(project.getUserId());
        if (user != null) {
            vo.setTeacherName(user.getName());
            vo.setTeacherCollege(user.getCollege());
        }
        return vo;
    }

    /**
     * 批量转换横向项目为移动端VO（优化：使用预加载的用户Map避免N+1查询）
     */
    private List<MobileProjectVO> convertHorizontalToMobileVO(List<HorizontalProject> projects, Map<Long, User> userMap) {
        return projects.stream()
                .map(p -> convertHorizontalToMobileVO(p, userMap))
                .collect(Collectors.toList());
    }

    /**
     * 单个横向项目转换（从userMap获取用户信息）
     */
    private MobileProjectVO convertHorizontalToMobileVO(HorizontalProject project, Map<Long, User> userMap) {
        MobileProjectVO vo = new MobileProjectVO();
        BeanUtils.copyProperties(project, vo);

        User user = userMap.get(project.getUserId());
        if (user != null) {
            vo.setTeacherName(user.getName());
            vo.setTeacherCollege(user.getCollege());
        }
        return vo;
    }

    /**
     * 批量转换专利为移动端VO（优化：使用预加载的用户Map避免N+1查询）
     */
    private List<MobilePatentVO> convertToMobilePatentVO(List<Patent> patents, Map<Long, User> userMap) {
        return patents.stream()
                .map(p -> convertToMobilePatentVO(p, userMap))
                .collect(Collectors.toList());
    }

    /**
     * 单个专利转换（从userMap获取用户信息）
     */
    private MobilePatentVO convertToMobilePatentVO(Patent patent, Map<Long, User> userMap) {
        MobilePatentVO vo = new MobilePatentVO();
        BeanUtils.copyProperties(patent, vo);

        User user = userMap.get(patent.getUserId());
        if (user != null) {
            vo.setTeacherName(user.getName());
        }
        return vo;
    }

    /**
     * 获取当前登录用户ID
     * 从JWT拦截器注入的request属性中获取
     */
    private Long getCurrentUserId() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return userId;
    }

    /**
     * 判断当前用户是否为管理员
     * 从JWT拦截器注入的request属性中获取角色信息
     */
    private boolean isAdmin() {
        String role = (String) request.getAttribute("role");
        return "ADMIN".equals(role);
    }
    
    private int getExpectedCount(BatchOperationDTO dto) {
        if ("batch_delete".equals(dto.getOperation())) {
            return dto.getIds() != null ? dto.getIds().size() : 0;
        } else {
            return dto.getItems() != null ? dto.getItems().size() : 0;
        }
    }
    
    // ========== 批量操作实现 ==========
    
    private int executeBatchForVerticalProject(String operation, BatchOperationDTO dto, 
            List<BatchResultDTO.FailureDetail> failures) {
        // TODO: 实现批量操作逻辑
        return 0;
    }
    
    private int executeBatchForHorizontalProject(String operation, BatchOperationDTO dto,
            List<BatchResultDTO.FailureDetail> failures) {
        return 0;
    }
    
    private int executeBatchForPatent(String operation, BatchOperationDTO dto,
            List<BatchResultDTO.FailureDetail> failures) {
        return 0;
    }
    
    private int executeBatchForPaper(String operation, BatchOperationDTO dto,
            List<BatchResultDTO.FailureDetail> failures) {
        return 0;
    }
    
    private int executeBatchForSoftware(String operation, BatchOperationDTO dto,
            List<BatchResultDTO.FailureDetail> failures) {
        return 0;
    }
    
    private int executeBatchForCompetition(String operation, BatchOperationDTO dto,
            List<BatchResultDTO.FailureDetail> failures) {
        return 0;
    }
}