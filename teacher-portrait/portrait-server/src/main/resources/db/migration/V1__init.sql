-- ============================================
-- 教师数字画像系统 - 数据库初始化脚本
-- ============================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `teacher_portrait` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `teacher_portrait`;

-- ============================================
-- 清理已有表（支持重复执行）
-- ============================================
-- 先删除有外键依赖的表，再删除被引用的表
DROP TABLE IF EXISTS `paper_index`;
DROP TABLE IF EXISTS `patent_transfer`;
DROP TABLE IF EXISTS `competition`;
DROP TABLE IF EXISTS `software_copyright`;
DROP TABLE IF EXISTS `paper`;
DROP TABLE IF EXISTS `patent`;
DROP TABLE IF EXISTS `horizontal_project`;
DROP TABLE IF EXISTS `vertical_project`;
DROP TABLE IF EXISTS `score_config`;
DROP TABLE IF EXISTS `user`;

-- 1. 用户表
CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `work_no` VARCHAR(20) NOT NULL UNIQUE COMMENT '工号',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `college` VARCHAR(100) COMMENT '学院',
  `role` VARCHAR(20) NOT NULL DEFAULT 'TEACHER' COMMENT '角色: TEACHER/ADMIN',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户表';

-- 2. 纵向项目表
CREATE TABLE `vertical_project` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '教师ID',
  `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `project_no` VARCHAR(100) UNIQUE COMMENT '项目编号',
  `level` VARCHAR(20) NOT NULL COMMENT '项目级别: 国家级/省部级/市厅级/校级',
  `source_unit` VARCHAR(200) COMMENT '来源单位',
  `start_date` DATE NOT NULL COMMENT '立项时间',
  `planned_end_date` DATE COMMENT '计划完成时间',
  `funding` DECIMAL(12,2) COMMENT '项目经费(万元)',
  `role` VARCHAR(20) NOT NULL COMMENT '教师参与角色: 主持/参与',
  `status` VARCHAR(20) DEFAULT '在研' COMMENT '项目状态: 在研/已结题/延期',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_level` (`level`),
  INDEX `idx_start_date` (`start_date`)
) COMMENT='纵向项目表';

-- 3. 横向项目表
CREATE TABLE `horizontal_project` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '教师ID',
  `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `company_name` VARCHAR(200) NOT NULL COMMENT '合作企业名称',
  `contract_amount` DECIMAL(12,2) NOT NULL COMMENT '合同金额(万元)',
  `sign_date` DATE NOT NULL COMMENT '签订日期',
  `end_date` DATE COMMENT '完成日期',
  `role` VARCHAR(20) NOT NULL COMMENT '教师参与角色: 主持/参与',
  `status` VARCHAR(20) DEFAULT '在研' COMMENT '项目状态: 在研/已结题',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  CHECK (`contract_amount` > 0)
) COMMENT='横向项目表';

-- 4. 专利表
CREATE TABLE `patent` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '教师ID',
  `name` VARCHAR(200) NOT NULL COMMENT '专利名称',
  `type` VARCHAR(20) NOT NULL COMMENT '专利类型: 发明专利/实用新型/外观设计',
  `application_no` VARCHAR(100) COMMENT '专利申请号',
  `grant_no` VARCHAR(100) COMMENT '专利授权号',
  `application_date` DATE NOT NULL COMMENT '申请日期',
  `grant_date` DATE COMMENT '授权日期',
  `status` VARCHAR(20) NOT NULL COMMENT '专利状态: 申请中/已授权',
  `inventors` VARCHAR(500) COMMENT '发明人列表(分号分隔)',
  `patentee` VARCHAR(200) COMMENT '专利权人',
  `is_counted` TINYINT(1) DEFAULT 1 COMMENT '是否计入考核(1是0否)',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`)
) COMMENT='专利表';

-- 5. 专利转让记录表
CREATE TABLE `patent_transfer` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `patent_id` BIGINT NOT NULL COMMENT '专利ID',
  `transfer_date` DATE NOT NULL COMMENT '转让日期',
  `transferee` VARCHAR(200) NOT NULL COMMENT '受让方名称',
  `amount` DECIMAL(12,2) COMMENT '转让金额(万元)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_patent_id` (`patent_id`),
  FOREIGN KEY (`patent_id`) REFERENCES `patent`(`id`) ON DELETE CASCADE
) COMMENT='专利转让记录表';

-- 6. 软著表
CREATE TABLE `software_copyright` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '教师ID',
  `name` VARCHAR(200) NOT NULL COMMENT '软件名称',
  `registration_no` VARCHAR(100) NOT NULL UNIQUE COMMENT '登记号',
  `version` VARCHAR(50) COMMENT '版本号',
  `dev_completion_date` DATE NOT NULL COMMENT '开发完成日期',
  `first_publish_date` DATE COMMENT '首次发表日期',
  `registration_date` DATE NOT NULL COMMENT '登记日期',
  `copyright_owners` VARCHAR(500) COMMENT '著作权人(分号分隔)',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_registration_date` (`registration_date`)
) COMMENT='软著表';

-- 7. 论文表
CREATE TABLE `paper` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '教师ID',
  `title` VARCHAR(300) NOT NULL COMMENT '论文题目',
  `type` VARCHAR(20) NOT NULL COMMENT '论文类型: 期刊论文/会议论文',
  `journal_name` VARCHAR(200) NOT NULL COMMENT '期刊/会议名称',
  `volume` VARCHAR(50) COMMENT '卷号',
  `issue` VARCHAR(50) COMMENT '期号',
  `pages` VARCHAR(50) COMMENT '页码',
  `publish_date` DATE NOT NULL COMMENT '发表时间',
  `authors` VARCHAR(500) COMMENT '作者列表(分号分隔)',
  `author_order` INT NOT NULL COMMENT '教师作者排序',
  `doi` VARCHAR(200) COMMENT 'DOI号',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_publish_date` (`publish_date`)
) COMMENT='论文表';

-- 8. 论文收录关联表
CREATE TABLE `paper_index` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `paper_id` BIGINT NOT NULL COMMENT '论文ID',
  `index_type` VARCHAR(20) NOT NULL COMMENT '收录类型: SCI/SSCI/EI/CSCD/CSSCI/北大核心/普通期刊/其他',
  INDEX `idx_paper_id` (`paper_id`),
  FOREIGN KEY (`paper_id`) REFERENCES `paper`(`id`) ON DELETE CASCADE
) COMMENT='论文收录关联表';

-- 9. 竞赛指导表
CREATE TABLE `competition` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '教师ID',
  `name` VARCHAR(200) NOT NULL COMMENT '竞赛名称',
  `organizer` VARCHAR(200) COMMENT '主办单位',
  `competition_date` DATE NOT NULL COMMENT '参赛时间',
  `student_team` VARCHAR(500) COMMENT '学生团队(分号分隔)',
  `award_level` VARCHAR(20) NOT NULL COMMENT '获奖级别: 国家级/省级/校级',
  `award_grade` VARCHAR(20) NOT NULL COMMENT '获奖等级: 特等奖/一等奖/二等奖/三等奖/优秀奖',
  `guide_rank` INT DEFAULT 1 COMMENT '指导排名',
  `certificate_no` VARCHAR(100) COMMENT '获奖证书编号',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_award_level` (`award_level`),
  INDEX `idx_competition_date` (`competition_date`)
) COMMENT='竞赛指导表';

-- 10. 评分权重配置表
CREATE TABLE `score_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `dimension` VARCHAR(20) NOT NULL UNIQUE COMMENT '维度名: 科研项目/专利成果/软件著作/学术论文/竞赛指导',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '权重值(百分比)',
  `scoring_rules` JSON COMMENT '计分规则JSON',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='评分权重配置表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认管理员: admin / 123456 (BCrypt)
INSERT INTO `user` (`work_no`, `name`, `college`, `role`, `password`) VALUES
('admin', '系统管理员', '信息中心', 'ADMIN', '$2b$10$K4hSd.4a/6mFOKq9sLtPReHri7Gc4TCtvgnjG3y0LtxOJNSt4pijy');

-- 默认教师: teacher / 123456 (BCrypt)
INSERT INTO `user` (`work_no`, `name`, `college`, `role`, `password`) VALUES
('T001', '测试教师', '计算机学院', 'TEACHER', '$2b$10$K4hSd.4a/6mFOKq9sLtPReHri7Gc4TCtvgnjG3y0LtxOJNSt4pijy');

SET @u0 = (SELECT id FROM `user` WHERE work_no = 'T001');

INSERT INTO vertical_project (user_id, name, project_no, level, source_unit, start_date, planned_end_date, funding, role, status, remark) VALUES
(@u0, '人工智能学习吃意大利面', 'VP-T001-001', '国家级', '国家自然科学基金委员会', '2020-01-01', '2023-12-31', 180.00, '主持', '已结题', NULL),
(@u0, '使用Trae开发全栈项目', 'VP-T001-002', '省部级', '科技部重点研发计划', '2022-06-01', '2025-05-31', 200.00, '主持', '在研', NULL),
(@u0, 'DeekseepV4-Pro太贵了', 'VP-T001-003', '市厅级', '国家自然科学基金委员会', '2021-01-01', '2024-12-31', 85.00, '参与', '在研', NULL);


-- 初始化默认权重
INSERT INTO `score_config` (`dimension`, `weight`, `scoring_rules`) VALUES
('科研项目', 20.00, '{"host_national":20,"host_provincial":15,"host_municipal":10,"host_school":5,"participate_factor":0.5,"horizontal_per_10w":2}'),
('专利成果', 20.00, '{"invention_auth":20,"utility_auth":10,"design_auth":5,"pending_factor":0.5,"transfer_extra":5}'),
('软件著作', 20.00, '{"per_item":10}'),
('学术论文', 20.00, '{"A":25,"B":15,"C":10,"D":5,"first_author_factor":1.0,"other_author_factor":0.5}'),
('竞赛指导', 20.00, '{"national_special":30,"national_first":25,"national_second":20,"national_third":15,"provincial_special":20,"provincial_first":15,"provincial_second":10,"provincial_third":8,"school_special":10,"school_first":8,"school_second":5,"school_third":3,"second_guide_factor":0.7}');