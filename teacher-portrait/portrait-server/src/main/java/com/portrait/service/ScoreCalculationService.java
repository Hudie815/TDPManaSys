package com.portrait.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portrait.entity.*;
import com.portrait.mapper.*;
import com.portrait.vo.PortraitDashboardVO;
import com.portrait.vo.PortraitDistributionVO;
import com.portrait.vo.PortraitRadarVO;
import com.portrait.vo.PortraitTrendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScoreCalculationService {

    @Resource
    private ScoreConfigMapper scoreConfigMapper;

    @Resource
    private VerticalProjectMapper verticalProjectMapper;

    @Resource
    private HorizontalProjectMapper horizontalProjectMapper;

    @Resource
    private PatentMapper patentMapper;

    @Resource
    private PatentTransferMapper patentTransferMapper;

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private volatile Map<String, BigDecimal> cachedGlobalMaxes;

    public void clearMaxCache() {
        cachedGlobalMaxes = null;
    }

    public PortraitRadarVO calculateRadar(Long userId) {
        AllUserData d = loadAllData();
        ensureCache(d);
        User user = d.userById.get(userId);
        Map<String, BigDecimal> raw = buildRawScores(userId, d);
        Map<String, BigDecimal> normalized = normalizeScores(raw);
        PortraitRadarVO vo = new PortraitRadarVO();
        vo.setUserId(userId);
        vo.setUserName(user != null ? user.getName() : "");
        vo.setCollege(user != null ? user.getCollege() : "");
        vo.setRawScores(raw);
        vo.setNormalizedScores(normalized);
        return vo;
    }

    public PortraitDashboardVO calculateDashboard(Long userId) {
        AllUserData d = loadAllData();
        ensureCache(d);
        return buildDashboard(userId, d);
    }

    private PortraitDashboardVO buildDashboard(Long userId, AllUserData d) {
        PortraitDashboardVO vo = new PortraitDashboardVO();

        List<VerticalProject> verticalList = d.verticalByUser.getOrDefault(userId, Collections.emptyList());
        List<HorizontalProject> horizontalList = d.horizontalByUser.getOrDefault(userId, Collections.emptyList());
        vo.setProjectTotalCount((long) (verticalList.size() + horizontalList.size()));

        BigDecimal totalFunding = BigDecimal.ZERO;
        for (VerticalProject vp : verticalList) {
            if (vp.getFunding() != null) totalFunding = totalFunding.add(vp.getFunding());
        }
        for (HorizontalProject hp : horizontalList) {
            if (hp.getContractAmount() != null) totalFunding = totalFunding.add(hp.getContractAmount());
        }
        vo.setTotalFunding(totalFunding);

        List<Paper> paperList = d.paperByUser.getOrDefault(userId, Collections.emptyList());
        vo.setPaperTotalCount((long) paperList.size());
        long aCount = 0, bCount = 0;
        for (Paper p : paperList) {
            String cls = d.paperClass.getOrDefault(p.getId(), "D");
            if ("A".equals(cls)) aCount++;
            else if ("B".equals(cls)) bCount++;
        }
        vo.setPaperACount(aCount);
        vo.setPaperBCount(bCount);

        long patentGranted = 0;
        for (Patent pt : d.patentByUser.getOrDefault(userId, Collections.emptyList())) {
            if ("已授权".equals(pt.getStatus())) patentGranted++;
        }
        vo.setPatentGrantedCount(patentGranted);

        vo.setSoftwareCount((long) d.swByUser.getOrDefault(userId, Collections.emptyList()).size());
        vo.setCompetitionAwardCount((long) d.compByUser.getOrDefault(userId, Collections.emptyList()).size());

        Map<String, BigDecimal> raw = buildRawScores(userId, d);
        vo.setRawScores(raw);
        vo.setNormalizedScores(normalizeScores(raw));

        return vo;
    }

    public PortraitDashboardVO calculateAggregatedDashboard() {
        AllUserData d = loadAllData();
        ensureCache(d);

        Map<Long, Map<String, BigDecimal>> allRawScores = new LinkedHashMap<>();
        for (Long uid : d.userById.keySet()) {
            allRawScores.put(uid, buildRawScores(uid, d));
        }

        PortraitDashboardVO vo = new PortraitDashboardVO();
        long totalProjects = 0;
        BigDecimal totalFunding = BigDecimal.ZERO;
        long totalPapers = 0, totalA = 0, totalB = 0;
        long totalPatents = 0, totalSoftware = 0, totalCompetitions = 0;

        Map<String, BigDecimal> aggregatedRaw = new LinkedHashMap<>();
        for (String dim : DIMS) { aggregatedRaw.put(dim, BigDecimal.ZERO); }

        for (Long uid : d.userById.keySet()) {
            Map<String, BigDecimal> userRaw = allRawScores.get(uid);
            for (Map.Entry<String, BigDecimal> e : userRaw.entrySet()) {
                aggregatedRaw.merge(e.getKey(), e.getValue(), BigDecimal::add);
            }

            List<VerticalProject> vl = d.verticalByUser.getOrDefault(uid, Collections.emptyList());
            List<HorizontalProject> hl = d.horizontalByUser.getOrDefault(uid, Collections.emptyList());
            totalProjects += vl.size() + hl.size();
            for (VerticalProject vp : vl) {
                if (vp.getFunding() != null) totalFunding = totalFunding.add(vp.getFunding());
            }
            for (HorizontalProject hp : hl) {
                if (hp.getContractAmount() != null) totalFunding = totalFunding.add(hp.getContractAmount());
            }

            List<Paper> pl = d.paperByUser.getOrDefault(uid, Collections.emptyList());
            totalPapers += pl.size();
            for (Paper p : pl) {
                String cls = d.paperClass.getOrDefault(p.getId(), "D");
                if ("A".equals(cls)) totalA++;
                else if ("B".equals(cls)) totalB++;
            }

            for (Patent pt : d.patentByUser.getOrDefault(uid, Collections.emptyList())) {
                if ("已授权".equals(pt.getStatus())) totalPatents++;
            }

            totalSoftware += d.swByUser.getOrDefault(uid, Collections.emptyList()).size();
            totalCompetitions += d.compByUser.getOrDefault(uid, Collections.emptyList()).size();
        }

        vo.setProjectTotalCount(totalProjects);
        vo.setTotalFunding(totalFunding);
        vo.setPaperTotalCount(totalPapers);
        vo.setPaperACount(totalA);
        vo.setPaperBCount(totalB);
        vo.setPatentGrantedCount(totalPatents);
        vo.setSoftwareCount(totalSoftware);
        vo.setCompetitionAwardCount(totalCompetitions);
        vo.setRawScores(aggregatedRaw);

        Map<String, BigDecimal> norm = new LinkedHashMap<>();
        Map<String, BigDecimal> maxes = cachedGlobalMaxes;
        for (String dim : DIMS) {
            BigDecimal r = aggregatedRaw.getOrDefault(dim, BigDecimal.ZERO);
            BigDecimal max = maxes.getOrDefault(dim, BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;
            BigDecimal n = r.multiply(BigDecimal.valueOf(100)).divide(max, 2, RoundingMode.HALF_UP);
            if (n.compareTo(BigDecimal.valueOf(100)) > 0) n = BigDecimal.valueOf(100);
            norm.put(dim, n);
        }
        vo.setNormalizedScores(norm);

        return vo;
    }

    public List<PortraitRadarVO> compareRadars(List<Long> userIds) {
        AllUserData d = loadAllData();
        List<PortraitRadarVO> result = new ArrayList<>();
        for (Long uid : userIds) {
            User user = d.userById.get(uid);
            Map<String, BigDecimal> raw = buildRawScores(uid, d);
            PortraitRadarVO vo = new PortraitRadarVO();
            vo.setUserId(uid);
            vo.setUserName(user != null ? user.getName() : "");
            vo.setCollege(user != null ? user.getCollege() : "");
            vo.setRawScores(raw);
            result.add(vo);
        }
        Map<String, BigDecimal> globalMaxes = new HashMap<>();
        for (PortraitRadarVO vo : result) {
            for (Map.Entry<String, BigDecimal> e : vo.getRawScores().entrySet()) {
                BigDecimal cur = globalMaxes.get(e.getKey());
                if (cur == null || e.getValue().compareTo(cur) > 0) {
                    globalMaxes.put(e.getKey(), e.getValue());
                }
            }
        }
        for (PortraitRadarVO vo : result) {
            Map<String, BigDecimal> norm = new LinkedHashMap<>();
            for (String dim : DIMS) {
                BigDecimal raw = vo.getRawScores().getOrDefault(dim, BigDecimal.ZERO);
                BigDecimal max = globalMaxes.getOrDefault(dim, BigDecimal.ONE);
                if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;
                BigDecimal n = raw.multiply(BigDecimal.valueOf(100)).divide(max, 2, RoundingMode.HALF_UP);
                if (n.compareTo(BigDecimal.valueOf(100)) > 0) n = BigDecimal.valueOf(100);
                norm.put(dim, n);
            }
            vo.setNormalizedScores(norm);
        }
        return result;
    }

    /**
     * 计算教师近5年的发展趋势数据
     * 优化：一次性查询所有数据，在内存中按年份分组统计，避免N次数据库查询
     * 性能提升：从40次查询降为6次查询，响应时间预计提升80%以上
     */
    public List<PortraitTrendVO> calculateTrend(Long userId) {
        List<PortraitTrendVO> list = new ArrayList<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        int endYear = now.getYear();
        int startYear = endYear - 4;

        // 一次性查询所有数据（避免循环查询）
        List<VerticalProject> allVertical = verticalProjectMapper.selectList(
                new LambdaQueryWrapper<VerticalProject>().eq(VerticalProject::getUserId, userId));
        List<HorizontalProject> allHorizontal = horizontalProjectMapper.selectList(
                new LambdaQueryWrapper<HorizontalProject>().eq(HorizontalProject::getUserId, userId));
        List<Patent> allPatents = patentMapper.selectList(
                new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId));
        List<SoftwareCopyright> allSoftware = softwareCopyrightMapper.selectList(
                new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, userId));
        List<Paper> allPapers = paperMapper.selectList(
                new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId));
        List<Competition> allCompetitions = competitionMapper.selectList(
                new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, userId));

        // 按年份分组统计
        Map<Integer, Long> verticalByYear = allVertical.stream()
                .collect(Collectors.groupingBy(v -> v.getStartDate().getYear(), Collectors.counting()));
        Map<Integer, Long> horizontalByYear = allHorizontal.stream()
                .collect(Collectors.groupingBy(h -> h.getSignDate().getYear(), Collectors.counting()));
        Map<Integer, BigDecimal> verticalFundingByYear = allVertical.stream()
                .collect(Collectors.groupingBy(v -> v.getStartDate().getYear(),
                        Collectors.reducing(BigDecimal.ZERO,
                                v -> v.getFunding() == null ? BigDecimal.ZERO : v.getFunding(),
                                BigDecimal::add)));
        Map<Integer, BigDecimal> horizontalFundingByYear = allHorizontal.stream()
                .collect(Collectors.groupingBy(h -> h.getSignDate().getYear(),
                        Collectors.reducing(BigDecimal.ZERO,
                                h -> h.getContractAmount() == null ? BigDecimal.ZERO : h.getContractAmount(),
                                BigDecimal::add)));
        Map<Integer, Long> patentByYear = allPatents.stream()
                .collect(Collectors.groupingBy(p -> p.getApplicationDate().getYear(), Collectors.counting()));
        Map<Integer, Long> softwareByYear = allSoftware.stream()
                .collect(Collectors.groupingBy(s -> s.getRegistrationDate().getYear(), Collectors.counting()));
        Map<Integer, Long> paperByYear = allPapers.stream()
                .collect(Collectors.groupingBy(p -> p.getPublishDate().getYear(), Collectors.counting()));
        Map<Integer, Long> competitionByYear = allCompetitions.stream()
                .collect(Collectors.groupingBy(c -> c.getCompetitionDate().getYear(), Collectors.counting()));

        // 构建结果列表
        for (int year = startYear; year <= endYear; year++) {
            PortraitTrendVO vo = new PortraitTrendVO();
            vo.setYear(String.valueOf(year));

            long vCount = verticalByYear.getOrDefault(year, 0L);
            long hCount = horizontalByYear.getOrDefault(year, 0L);
            vo.setProjectCount(vCount + hCount);

            BigDecimal vFunding = verticalFundingByYear.getOrDefault(year, BigDecimal.ZERO);
            BigDecimal hFunding = horizontalFundingByYear.getOrDefault(year, BigDecimal.ZERO);
            vo.setProjectFunding(vFunding.add(hFunding));

            vo.setPatentCount(patentByYear.getOrDefault(year, 0L));
            vo.setSoftwareCount(softwareByYear.getOrDefault(year, 0L));
            vo.setPaperCount(paperByYear.getOrDefault(year, 0L));
            vo.setCompetitionCount(competitionByYear.getOrDefault(year, 0L));

            list.add(vo);
        }
        return list;
    }

    public PortraitDistributionVO calculateDistribution(Long userId) {
        PortraitDistributionVO vo = new PortraitDistributionVO();

        List<VerticalProject> vList = verticalProjectMapper.selectList(
                new LambdaQueryWrapper<VerticalProject>().eq(VerticalProject::getUserId, userId));
        Map<String, Integer> vLevel = new LinkedHashMap<>();
        vLevel.put("国家级", 0); vLevel.put("省部级", 0); vLevel.put("市厅级", 0); vLevel.put("校级", 0);
        for (VerticalProject vp : vList) {
            if (vp.getLevel() != null) vLevel.merge(vp.getLevel(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : vLevel.entrySet()) {
            if (e.getValue() > 0) {
                Map<String, Object> item = new HashMap<>(); item.put("name", e.getKey()); item.put("value", e.getValue()); vo.getProjectLevel().add(item);
            }
        }

        List<Patent> pList = patentMapper.selectList(
                new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId));
        Map<String, Integer> pType = new LinkedHashMap<>();
        pType.put("发明专利", 0); pType.put("实用新型", 0); pType.put("外观设计", 0);
        for (Patent p : pList) {
            if (p.getType() != null) pType.merge(p.getType(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : pType.entrySet()) {
            if (e.getValue() > 0) {
                Map<String, Object> item = new HashMap<>(); item.put("name", e.getKey()); item.put("value", e.getValue()); vo.getPatentType().add(item);
            }
        }

        List<Paper> paperList = paperMapper.selectList(
                new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId));
        Map<Long, String> classMap = loadPaperClasses(paperList);
        Map<String, Integer> pClass = new LinkedHashMap<>();
        pClass.put("A类(SCI/SSCI)", 0); pClass.put("B类(EI/CSCD)", 0); pClass.put("C类(CSSCI/北大核心)", 0); pClass.put("D类(其他)", 0);
        for (Paper p : paperList) {
            String cls = classMap.getOrDefault(p.getId(), "D");
            switch (cls) {
                case "A": pClass.merge("A类(SCI/SSCI)", 1, Integer::sum); break;
                case "B": pClass.merge("B类(EI/CSCD)", 1, Integer::sum); break;
                case "C": pClass.merge("C类(CSSCI/北大核心)", 1, Integer::sum); break;
                default: pClass.merge("D类(其他)", 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> e : pClass.entrySet()) {
            if (e.getValue() > 0) {
                Map<String, Object> item = new HashMap<>(); item.put("name", e.getKey()); item.put("value", e.getValue()); vo.getPaperClass().add(item);
            }
        }

        List<Competition> cList = competitionMapper.selectList(
                new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, userId));
        Map<String, Integer> cLevel = new LinkedHashMap<>();
        cLevel.put("国家级", 0); cLevel.put("省级", 0); cLevel.put("校级", 0);
        for (Competition c : cList) {
            if (c.getAwardLevel() != null) cLevel.merge(c.getAwardLevel(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : cLevel.entrySet()) {
            if (e.getValue() > 0) {
                Map<String, Object> item = new HashMap<>(); item.put("name", e.getKey()); item.put("value", e.getValue()); vo.getCompetitionLevel().add(item);
            }
        }
        return vo;
    }

    private Map<String, BigDecimal> buildRawScores(Long userId, AllUserData d) {
        Map<String, BigDecimal> scores = new LinkedHashMap<>();
        scores.put("科研项目", calcProjectScore(userId, d));
        scores.put("专利成果", calcPatentScore(userId, d));
        scores.put("软件著作", calcSoftwareScore(userId, d));
        scores.put("学术论文", calcPaperScore(userId, d));
        scores.put("竞赛指导", calcCompetitionScore(userId, d));
        return scores;
    }

    private Map<String, BigDecimal> normalizeScores(Map<String, BigDecimal> raw) {
        Map<String, BigDecimal> globalMaxes = getGlobalMaxes();
        Map<String, BigDecimal> normalized = new LinkedHashMap<>();
        for (String dim : DIMS) {
            BigDecimal r = raw.getOrDefault(dim, BigDecimal.ZERO);
            BigDecimal max = globalMaxes.getOrDefault(dim, BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;
            BigDecimal norm = r.multiply(BigDecimal.valueOf(100)).divide(max, 2, RoundingMode.HALF_UP);
            if (norm.compareTo(BigDecimal.valueOf(100)) > 0) norm = BigDecimal.valueOf(100);
            normalized.put(dim, norm);
        }
        return normalized;
    }

    private Map<String, BigDecimal> getGlobalMaxes() {
        if (cachedGlobalMaxes != null) {
            return cachedGlobalMaxes;
        }
        synchronized (this) {
            if (cachedGlobalMaxes != null) {
                return cachedGlobalMaxes;
            }
            cachedGlobalMaxes = buildGlobalMaxes(loadAllData());
            return cachedGlobalMaxes;
        }
    }

    private BigDecimal calcProjectScore(Long userId, AllUserData d) {
        BigDecimal score = BigDecimal.ZERO;
        List<VerticalProject> vList = d.verticalByUser.getOrDefault(userId, Collections.emptyList());
        for (VerticalProject vp : vList) {
            BigDecimal base = BigDecimal.ZERO;
            switch (vp.getLevel() != null ? vp.getLevel() : "") {
                case "国家级": base = BigDecimal.valueOf(20); break;
                case "省部级": base = BigDecimal.valueOf(15); break;
                case "市厅级": base = BigDecimal.valueOf(10); break;
                case "校级": base = BigDecimal.valueOf(5); break;
            }
            if ("参与".equals(vp.getRole())) {
                base = base.multiply(BigDecimal.valueOf(0.5));
            }
            score = score.add(base);
        }
        List<HorizontalProject> hList = d.horizontalByUser.getOrDefault(userId, Collections.emptyList());
        for (HorizontalProject hp : hList) {
            BigDecimal base = BigDecimal.ZERO;
            if ("主持".equals(hp.getRole()) && hp.getContractAmount() != null) {
                base = hp.getContractAmount().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(2));
            } else if (hp.getContractAmount() != null) {
                base = hp.getContractAmount().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
            }
            score = score.add(base);
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcPatentScore(Long userId, AllUserData d) {
        BigDecimal score = BigDecimal.ZERO;
        List<Patent> pList = d.patentByUser.getOrDefault(userId, Collections.emptyList());
        for (Patent p : pList) {
            BigDecimal base = BigDecimal.ZERO;
            switch (p.getType() != null ? p.getType() : "") {
                case "发明专利": base = BigDecimal.valueOf(20); break;
                case "实用新型": base = BigDecimal.valueOf(10); break;
                case "外观设计": base = BigDecimal.valueOf(5); break;
            }
            if ("申请中".equals(p.getStatus())) {
                base = base.multiply(BigDecimal.valueOf(0.5));
            }
            score = score.add(base);
            Long transferCount = d.transferCountByPatent.getOrDefault(p.getId(), 0L);
            score = score.add(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(transferCount)));
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcSoftwareScore(Long userId, AllUserData d) {
        List<SoftwareCopyright> list = d.swByUser.getOrDefault(userId, Collections.emptyList());
        return BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(list.size())).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcPaperScore(Long userId, AllUserData d) {
        List<Paper> paperList = d.paperByUser.getOrDefault(userId, Collections.emptyList());
        BigDecimal score = BigDecimal.ZERO;
        for (Paper p : paperList) {
            String cls = d.paperClass.getOrDefault(p.getId(), "D");
            BigDecimal base = BigDecimal.ZERO;
            switch (cls) {
                case "A": base = BigDecimal.valueOf(25); break;
                case "B": base = BigDecimal.valueOf(15); break;
                case "C": base = BigDecimal.valueOf(10); break;
                case "D": base = BigDecimal.valueOf(5); break;
            }
            if (p.getAuthorOrder() != null && p.getAuthorOrder() != 1) {
                base = base.multiply(BigDecimal.valueOf(0.5));
            }
            score = score.add(base);
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcCompetitionScore(Long userId, AllUserData d) {
        List<Competition> cList = d.compByUser.getOrDefault(userId, Collections.emptyList());
        BigDecimal score = BigDecimal.ZERO;
        for (Competition c : cList) {
            BigDecimal base = BigDecimal.ZERO;
            String key = (c.getAwardLevel() != null ? c.getAwardLevel() : "") + "_"
                    + (c.getAwardGrade() != null ? c.getAwardGrade() : "");
            switch (key) {
                case "国家级_特等奖": base = BigDecimal.valueOf(30); break;
                case "国家级_一等奖": base = BigDecimal.valueOf(25); break;
                case "国家级_二等奖": base = BigDecimal.valueOf(20); break;
                case "国家级_三等奖": base = BigDecimal.valueOf(15); break;
                case "省级_特等奖": base = BigDecimal.valueOf(20); break;
                case "省级_一等奖": base = BigDecimal.valueOf(15); break;
                case "省级_二等奖": base = BigDecimal.valueOf(10); break;
                case "省级_三等奖": base = BigDecimal.valueOf(8); break;
                case "校级_特等奖": base = BigDecimal.valueOf(10); break;
                case "校级_一等奖": base = BigDecimal.valueOf(8); break;
                case "校级_二等奖": base = BigDecimal.valueOf(5); break;
                case "校级_三等奖": base = BigDecimal.valueOf(3); break;
                default: base = BigDecimal.ZERO;
            }
            if (c.getGuideRank() != null && c.getGuideRank() != 1) {
                base = base.multiply(BigDecimal.valueOf(0.7));
            }
            score = score.add(base);
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<Long, String> loadPaperClasses(List<Paper> paperList) {
        if (paperList.isEmpty()) return Collections.emptyMap();
        List<Long> paperIds = paperList.stream().map(Paper::getId).collect(Collectors.toList());
        List<PaperIndex> all = paperIndexMapper.selectList(
                new LambdaQueryWrapper<PaperIndex>().in(PaperIndex::getPaperId, paperIds));
        Map<Long, String> result = new HashMap<>();
        for (PaperIndex pi : all) {
            String cls = indexTypeToClass(pi.getIndexType());
            String existing = result.get(pi.getPaperId());
            if (existing == null || classPriority(cls) > classPriority(existing)) {
                result.put(pi.getPaperId(), cls);
            }
        }
        for (Paper p : paperList) {
            result.putIfAbsent(p.getId(), "D");
        }
        return result;
    }

    private String indexTypeToClass(String indexType) {
        if (indexType == null) return "D";
        switch (indexType) {
            case "SCI": case "SSCI": return "A";
            case "EI": case "CSCD": return "B";
            case "CSSCI": case "北大核心": return "C";
            default: return "D";
        }
    }

    private int classPriority(String cls) {
        switch (cls) {
            case "A": return 4;
            case "B": return 3;
            case "C": return 2;
            case "D": return 1;
            default: return 0;
        }
    }

    private static class AllUserData {
        final Map<Long, User> userById = new LinkedHashMap<>();
        final Map<Long, List<VerticalProject>> verticalByUser = new LinkedHashMap<>();
        final Map<Long, List<HorizontalProject>> horizontalByUser = new LinkedHashMap<>();
        final Map<Long, List<Paper>> paperByUser = new LinkedHashMap<>();
        final Map<Long, String> paperClass = new LinkedHashMap<>();
        final Map<Long, List<Patent>> patentByUser = new LinkedHashMap<>();
        final Map<Long, Long> transferCountByPatent = new LinkedHashMap<>();
        final Map<Long, List<SoftwareCopyright>> swByUser = new LinkedHashMap<>();
        final Map<Long, List<Competition>> compByUser = new LinkedHashMap<>();
    }

    private AllUserData loadAllData() {
        AllUserData d = new AllUserData();

        List<User> allUsers = userMapper.selectList(null);
        for (User u : allUsers) {
            d.userById.put(u.getId(), u);
        }

        for (VerticalProject vp : verticalProjectMapper.selectList(null)) {
            d.verticalByUser.computeIfAbsent(vp.getUserId(), k -> new ArrayList<>()).add(vp);
        }
        for (HorizontalProject hp : horizontalProjectMapper.selectList(null)) {
            d.horizontalByUser.computeIfAbsent(hp.getUserId(), k -> new ArrayList<>()).add(hp);
        }
        for (SoftwareCopyright sw : softwareCopyrightMapper.selectList(null)) {
            d.swByUser.computeIfAbsent(sw.getUserId(), k -> new ArrayList<>()).add(sw);
        }
        for (Competition c : competitionMapper.selectList(null)) {
            d.compByUser.computeIfAbsent(c.getUserId(), k -> new ArrayList<>()).add(c);
        }

        List<Paper> allPapers = paperMapper.selectList(null);
        for (Paper p : allPapers) {
            d.paperByUser.computeIfAbsent(p.getUserId(), k -> new ArrayList<>()).add(p);
        }
        if (!allPapers.isEmpty()) {
            List<Long> paperIds = allPapers.stream().map(Paper::getId).collect(Collectors.toList());
            List<PaperIndex> allIndexes = paperIndexMapper.selectList(
                    new LambdaQueryWrapper<PaperIndex>().in(PaperIndex::getPaperId, paperIds));
            for (PaperIndex pi : allIndexes) {
                String cls = indexTypeToClass(pi.getIndexType());
                String existing = d.paperClass.get(pi.getPaperId());
                if (existing == null || classPriority(cls) > classPriority(existing)) {
                    d.paperClass.put(pi.getPaperId(), cls);
                }
            }
            for (Paper p : allPapers) {
                d.paperClass.putIfAbsent(p.getId(), "D");
            }
        }

        for (Patent pt : patentMapper.selectList(null)) {
            d.patentByUser.computeIfAbsent(pt.getUserId(), k -> new ArrayList<>()).add(pt);
        }
        List<PatentTransfer> allTransfers = patentTransferMapper.selectList(null);
        for (PatentTransfer t : allTransfers) {
            d.transferCountByPatent.merge(t.getPatentId(), 1L, Long::sum);
        }

        return d;
    }

    private void ensureCache(AllUserData d) {
        if (cachedGlobalMaxes != null) return;
        synchronized (this) {
            if (cachedGlobalMaxes != null) return;
            cachedGlobalMaxes = buildGlobalMaxes(d);
        }
    }

    private Map<String, BigDecimal> buildGlobalMaxes(AllUserData d) {
        Map<String, BigDecimal> maxes = new LinkedHashMap<>();
        for (String dim : DIMS) { maxes.put(dim, BigDecimal.ZERO); }
        for (Long uid : d.userById.keySet()) {
            Map<String, BigDecimal> ur = buildRawScores(uid, d);
            for (Map.Entry<String, BigDecimal> e : ur.entrySet()) {
                if (e.getValue().compareTo(maxes.get(e.getKey())) > 0) {
                    maxes.put(e.getKey(), e.getValue());
                }
            }
        }
        return maxes;
    }

    private static final String[] DIMS = {"科研项目", "专利成果", "软件著作", "学术论文", "竞赛指导"};
}