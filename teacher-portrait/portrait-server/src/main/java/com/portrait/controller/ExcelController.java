package com.portrait.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.portrait.common.BusinessException;
import com.portrait.common.Result;
import com.portrait.dto.ImportResultDTO;
import com.portrait.entity.*;
import com.portrait.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Resource
    private VerticalProjectMapper verticalProjectMapper;

    @Resource
    private HorizontalProjectMapper horizontalProjectMapper;

    @Resource
    private PatentMapper patentMapper;

    @Resource
    private SoftwareCopyrightMapper softwareCopyrightMapper;

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private PaperIndexMapper paperIndexMapper;

    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private HttpServletRequest request;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/template/{module}")
    public void downloadTemplate(@PathVariable String module, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode(module + "-导入模板.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        List<List<String>> head = getModuleHead(module);
        List<List<Object>> data = new ArrayList<>();
        data.add(getModuleExample(module));
        EasyExcel.write(response.getOutputStream()).head(head).sheet("Sheet1").doWrite(data);
    }

    /**
     * 导入Excel数据
     * 添加事务注解，确保导入失败时能回滚已插入的数据
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/import/{module}")
    public Result<ImportResultDTO> importData(@PathVariable String module, @RequestParam("file") MultipartFile file) {
        ImportResultDTO result = new ImportResultDTO();
        try {
            List<Map<Integer, String>> rows = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    rows.add(data);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {}
            }).sheet().doRead();

            List<String> expected = getFieldOrder(module);
            Long currentUserId = getCurrentUserId();
            User currentUser = userMapper.selectById(currentUserId);
            Map<String, Long> userMap = loadUserMap();
            int rowNum = 0;
            for (Map<Integer, String> row : rows) {
                rowNum++;
                try {
                    Map<String, String> named = new LinkedHashMap<>();
                    for (int i = 0; i < expected.size(); i++) {
                        named.put(expected.get(i), row.getOrDefault(i, ""));
                    }
                    String skipReason = importOneRow(module, named, userMap, currentUser);
                    if (skipReason != null) {
                        result.setSkipCount(result.getSkipCount() + 1);
                        result.getSkipReasons().add("第" + rowNum + "行: " + skipReason);
                    } else {
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    }
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    result.getFailReasons().add("第" + rowNum + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("文件解析失败: " + e.getMessage());
        }
        return Result.success(result);
    }

    @GetMapping("/export/{module}")
    public void exportData(@PathVariable String module,
                           @RequestParam(required = false) Long userId,
                           @RequestParam(required = false) Integer year,
                           HttpServletResponse response) throws IOException {
        String role = (String) request.getAttribute("role");
        Long currentUserId = getCurrentUserId();
        if ("TEACHER".equals(role)) {
            userId = currentUserId;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode(module + "-导出数据.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        List<List<String>> head = getModuleHead(module);
        List<List<Object>> data = exportModuleData(module, userId, year);
        EasyExcel.write(response.getOutputStream()).head(head).sheet("Sheet1").doWrite(data);
    }

    private String importOneRow(String module, Map<String, String> row, Map<String, Long> userMap, User currentUser) {
        String workNo = row.get("work_no");
        Long targetUserId = currentUser.getId();
        if (workNo != null && !workNo.isEmpty() && userMap.containsKey(workNo)) {
            targetUserId = userMap.get(workNo);
        }

        switch (module) {
            case "vertical-project": return importVerticalProject(row, targetUserId);
            case "horizontal-project": return importHorizontalProject(row, targetUserId);
            case "patent": return importPatent(row, targetUserId);
            case "software": return importSoftware(row, targetUserId);
            case "paper": return importPaper(row, targetUserId);
            case "competition": return importCompetition(row, targetUserId);
            default: return "未知模块";
        }
    }

    private String importVerticalProject(Map<String, String> row, Long userId) {
        String name = row.get("name"); if (isEmpty(name)) return "项目名称为空";
        String level = row.get("level"); if (isEmpty(level)) return "项目级别为空";
        if (!Arrays.asList("国家级","省部级","市厅级","校级").contains(level)) return "项目级别无效: " + level;
        String role = row.get("role"); if (!Arrays.asList("主持","参与").contains(role)) return "角色无效: " + role;
        String status = row.get("status"); if (!Arrays.asList("在研","已结题","延期").contains(status)) return "状态无效: " + status;

        VerticalProject entity = new VerticalProject();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setProjectNo(row.get("project_no"));
        entity.setLevel(level);
        entity.setSourceUnit(row.get("source_unit"));
        entity.setStartDate(parseDate(row.get("start_date")));
        entity.setPlannedEndDate(parseDate(row.get("planned_end_date")));
        entity.setFunding(parseBigDecimal(row.get("funding")));
        entity.setRole(role);
        entity.setStatus(status);
        entity.setRemark(row.get("remark"));

        if (entity.getProjectNo() != null && !entity.getProjectNo().isEmpty()) {
            Long cnt = verticalProjectMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VerticalProject>()
                            .eq(VerticalProject::getProjectNo, entity.getProjectNo()));
            if (cnt > 0) return "项目编号已存在: " + entity.getProjectNo();
        }
        verticalProjectMapper.insert(entity);
        return null;
    }

    private String importHorizontalProject(Map<String, String> row, Long userId) {
        String name = row.get("name"); if (isEmpty(name)) return "项目名称为空";
        String company = row.get("company_name"); if (isEmpty(company)) return "企业名称为空";
        String role = row.get("role"); if (!Arrays.asList("主持","参与").contains(role)) return "角色无效: " + role;

        HorizontalProject entity = new HorizontalProject();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setCompanyName(company);
        entity.setContractAmount(parseBigDecimal(row.get("contract_amount")));
        entity.setSignDate(parseDate(row.get("sign_date")));
        entity.setEndDate(parseDate(row.get("end_date")));
        entity.setRole(role);
        entity.setStatus(row.get("status"));
        entity.setRemark(row.get("remark"));

        if (entity.getContractAmount() == null || entity.getContractAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "合同金额必须大于0";
        }
        horizontalProjectMapper.insert(entity);
        return null;
    }

    private String importPatent(Map<String, String> row, Long userId) {
        String name = row.get("name"); if (isEmpty(name)) return "专利名称为空";
        String type = row.get("type"); if (!Arrays.asList("发明专利","实用新型","外观设计").contains(type)) return "专利类型无效: " + type;

        Patent entity = new Patent();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setType(type);
        entity.setApplicationNo(row.get("application_no"));
        entity.setGrantNo(row.get("grant_no"));
        entity.setApplicationDate(parseDate(row.get("application_date")));
        entity.setGrantDate(parseDate(row.get("grant_date")));
        entity.setStatus(row.get("status"));
        entity.setInventors(row.get("inventors"));
        entity.setPatentee(row.get("patentee"));
        entity.setIsCounted(1);
        entity.setRemark(row.get("remark"));
        patentMapper.insert(entity);
        return null;
    }

    private String importSoftware(Map<String, String> row, Long userId) {
        String name = row.get("name"); if (isEmpty(name)) return "软件名称为空";
        String regNo = row.get("registration_no"); if (isEmpty(regNo)) return "登记号为空";

        Long cnt = softwareCopyrightMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SoftwareCopyright>()
                        .eq(SoftwareCopyright::getRegistrationNo, regNo));
        if (cnt > 0) return "登记号已存在: " + regNo;

        SoftwareCopyright entity = new SoftwareCopyright();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setRegistrationNo(regNo);
        entity.setVersion(row.get("version"));
        entity.setDevCompletionDate(parseDate(row.get("dev_completion_date")));
        entity.setFirstPublishDate(parseDate(row.get("first_publish_date")));
        entity.setRegistrationDate(parseDate(row.get("registration_date")));
        entity.setCopyrightOwners(row.get("copyright_owners"));
        entity.setRemark(row.get("remark"));
        softwareCopyrightMapper.insert(entity);
        return null;
    }

    private String importPaper(Map<String, String> row, Long userId) {
        String title = row.get("title"); if (isEmpty(title)) return "论文标题为空";
        String journal = row.get("journal_name"); if (isEmpty(journal)) return "期刊名称为空";

        Paper entity = new Paper();
        entity.setUserId(userId);
        entity.setTitle(title);
        entity.setType(row.get("type"));
        entity.setJournalName(journal);
        entity.setVolume(row.get("volume"));
        entity.setIssue(row.get("issue"));
        entity.setPages(row.get("pages"));
        entity.setPublishDate(parseDate(row.get("publish_date")));
        entity.setAuthors(row.get("authors"));
        entity.setAuthorOrder(parseInt(row.get("author_order"), 1));
        entity.setDoi(row.get("doi"));
        entity.setRemark(row.get("remark"));
        paperMapper.insert(entity);

        String indexTypes = row.get("index_types");
        if (indexTypes != null && !indexTypes.isEmpty()) {
            for (String it : indexTypes.split("[,;，；]")) {
                String t = it.trim();
                if (!t.isEmpty()) {
                    PaperIndex pi = new PaperIndex();
                    pi.setPaperId(entity.getId());
                    pi.setIndexType(t);
                    paperIndexMapper.insert(pi);
                }
            }
        }
        return null;
    }

    private String importCompetition(Map<String, String> row, Long userId) {
        String name = row.get("name"); if (isEmpty(name)) return "竞赛名称为空";
        Competition entity = new Competition();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setOrganizer(row.get("organizer"));
        entity.setCompetitionDate(parseDate(row.get("competition_date")));
        entity.setStudentTeam(row.get("student_team"));
        entity.setAwardLevel(row.get("award_level"));
        entity.setAwardGrade(row.get("award_grade"));
        entity.setGuideRank(parseInt(row.get("guide_rank"), 1));
        entity.setCertificateNo(row.get("certificate_no"));
        entity.setRemark(row.get("remark"));
        competitionMapper.insert(entity);
        return null;
    }

    private List<List<String>> getModuleHead(String module) {
        List<String> fields = getFieldOrder(module);
        return fields.stream().map(Arrays::asList).collect(Collectors.toList());
    }

    private List<String> getFieldOrder(String module) {
        switch (module) {
            case "vertical-project":
                return Arrays.asList("work_no","name","project_no","level","source_unit","start_date","planned_end_date","funding","role","status","remark");
            case "horizontal-project":
                return Arrays.asList("work_no","name","company_name","contract_amount","sign_date","end_date","role","status","remark");
            case "patent":
                return Arrays.asList("work_no","name","type","application_no","grant_no","application_date","grant_date","status","inventors","patentee","remark");
            case "software":
                return Arrays.asList("work_no","name","registration_no","version","dev_completion_date","first_publish_date","registration_date","copyright_owners","remark");
            case "paper":
                return Arrays.asList("work_no","title","type","journal_name","volume","issue","pages","publish_date","authors","author_order","doi","index_types","remark");
            case "competition":
                return Arrays.asList("work_no","name","organizer","competition_date","student_team","award_level","award_grade","guide_rank","certificate_no","remark");
            default: return Collections.emptyList();
        }
    }

    private List<Object> getModuleExample(String module) {
        switch (module) {
            case "vertical-project":
                return Arrays.asList("","示例项目","","国家级","科技部","2025-01-01","2027-12-31","50.00","主持","在研","");
            case "horizontal-project":
                return Arrays.asList("","示例项目","XX公司","30.00","2025-01-01","2025-12-31","主持","在研","");
            case "patent":
                return Arrays.asList("","示例专利","发明专利","","","2025-01-01","","已授权","张三;李四","XX大学","");
            case "software":
                return Arrays.asList("","示例软件","SR-2025-001","V1.0","2025-01-01","2025-03-01","2025-06-01","张三","");
            case "paper":
                return Arrays.asList("","示例论文","期刊论文","XX学报","","","","2025-06-01","张三;李四","1","","SCI","");
            case "competition":
                return Arrays.asList("","示例竞赛","教育部","2025-07-01","张三;李四","国家级","一等奖","1","","");
            default: return Collections.emptyList();
        }
    }

    /**
     * 导出数据（优化：批量查询用户信息，避免N+1查询）
     */
    private List<List<Object>> exportModuleData(String module, Long userId, Integer year) {
        List<List<Object>> data = new ArrayList<>();
        List<String> fields = getFieldOrder(module);

        // 批量查询用户信息（优化：避免在循环中逐条查询）
        Map<Long, User> userMap;
        if (userId != null) {
            User user = userMapper.selectById(userId);
            userMap = user != null ? Collections.singletonMap(userId, user) : Collections.emptyMap();
        } else {
            // 管理员导出所有数据时，一次性查询所有用户
            userMap = userMapper.selectList(null).stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        }

        switch (module) {
            case "vertical-project": {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VerticalProject> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VerticalProject>()
                        .eq(userId != null, VerticalProject::getUserId, userId);
                if (year != null) {
                    String begin = year + "-01-01"; String end = year + "-12-31";
                    w.ge(VerticalProject::getStartDate, begin).le(VerticalProject::getStartDate, end);
                }
                verticalProjectMapper.selectList(w).forEach(e -> data.add(toRow(e, fields, userMap)));
                break;
            }
            case "horizontal-project": {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HorizontalProject> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HorizontalProject>()
                        .eq(userId != null, HorizontalProject::getUserId, userId);
                if (year != null) {
                    String begin = year + "-01-01"; String end = year + "-12-31";
                    w.ge(HorizontalProject::getSignDate, begin).le(HorizontalProject::getSignDate, end);
                }
                horizontalProjectMapper.selectList(w).forEach(e -> data.add(toRow(e, fields, userMap)));
                break;
            }
            case "patent": {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Patent> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Patent>()
                        .eq(userId != null, Patent::getUserId, userId);
                if (year != null) {
                    String begin = year + "-01-01"; String end = year + "-12-31";
                    w.ge(Patent::getApplicationDate, begin).le(Patent::getApplicationDate, end);
                }
                patentMapper.selectList(w).forEach(e -> data.add(toRow(e, fields, userMap)));
                break;
            }
            case "software": {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SoftwareCopyright> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SoftwareCopyright>()
                        .eq(userId != null, SoftwareCopyright::getUserId, userId);
                if (year != null) {
                    String begin = year + "-01-01"; String end = year + "-12-31";
                    w.ge(SoftwareCopyright::getRegistrationDate, begin).le(SoftwareCopyright::getRegistrationDate, end);
                }
                softwareCopyrightMapper.selectList(w).forEach(e -> data.add(toRow(e, fields, userMap)));
                break;
            }
            case "paper": {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Paper> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Paper>()
                        .eq(userId != null, Paper::getUserId, userId);
                if (year != null) {
                    String begin = year + "-01-01"; String end = year + "-12-31";
                    w.ge(Paper::getPublishDate, begin).le(Paper::getPublishDate, end);
                }
                paperMapper.selectList(w).forEach(e -> data.add(toRow(e, fields, userMap)));
                break;
            }
            case "competition": {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Competition> w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Competition>()
                        .eq(userId != null, Competition::getUserId, userId);
                if (year != null) {
                    String begin = year + "-01-01"; String end = year + "-12-31";
                    w.ge(Competition::getCompetitionDate, begin).le(Competition::getCompetitionDate, end);
                }
                competitionMapper.selectList(w).forEach(e -> data.add(toRow(e, fields, userMap)));
                break;
            }
        }
        return data;
    }

    /**
     * 将实体转换为行数据（优化：使用预加载的用户Map避免N+1查询）
     */
    private List<Object> toRow(Object entity, List<String> fields, Map<Long, User> userMap) {
        // 从实体中获取userId（通过反射）
        Long entityUserId = null;
        try {
            java.lang.reflect.Field userIdField = entity.getClass().getDeclaredField("userId");
            userIdField.setAccessible(true);
            entityUserId = (Long) userIdField.get(entity);
        } catch (Exception e) {
            // 忽略
        }

        User u = entityUserId != null ? userMap.get(entityUserId) : null;
        List<Object> row = new ArrayList<>();
        for (String f : fields) {
            row.add(getFieldValue(entity, f, u));
        }
        return row;
    }

    private Object getFieldValue(Object entity, String field, User u) {
        try {
            if ("work_no".equals(field)) return u != null ? u.getWorkNo() : "";
            java.lang.reflect.Field f = entity.getClass().getDeclaredField(toCamel(field));
            f.setAccessible(true);
            Object v = f.get(entity);
            return v != null ? v.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String toCamel(String underscore) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : underscore.toCharArray()) {
            if (c == '_') { upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }

    private Map<String, Long> loadUserMap() {
        return userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getWorkNo, User::getId, (a, b) -> a));
    }

    private Long getCurrentUserId() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(401, "未登录");
        return userId;
    }

    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }

    private LocalDate parseDate(String s) {
        if (isEmpty(s)) return null;
        try { return LocalDate.parse(s, DATE_FMT); } catch (Exception e) { return null; }
    }

    private BigDecimal parseBigDecimal(String s) {
        if (isEmpty(s)) return null;
        try { return new BigDecimal(s); } catch (Exception e) { return null; }
    }

    private int parseInt(String s, int def) {
        if (isEmpty(s)) return def;
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}