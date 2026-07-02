package com.portrait.view;

/**
 * JsonView 视图定义
 * 
 * 用于控制不同场景下返回的字段范围：
 * - BaseView：基础字段（ID、名称等核心信息）
 * - DetailView：详细字段（包含所有字段，用于详情页）
 * - MobileView：移动端精简字段（减少数据传输）
 * - ListView：列表页字段（中等详细程度）
 * 
 * 使用方式：
 * <code>
 * @JsonView(Views.BaseView.class)
 * public List<User> list() { ... }
 * 
 * @JsonView(Views.DetailView.class)
 * public User detail() { ... }
 * </code>
 * 
 * 视图继承关系：
 * BaseView -> ListView -> DetailView
 * MobileView 独立（精简字段）
 */
public class Views {
    
    /**
     * 基础视图 - 最核心的字段
     * 适用于：下拉选择、关联数据展示
     */
    public interface BaseView {}
    
    /**
     * 列表视图 - 包含基础字段 + 列表展示字段
     * 适用于：数据列表页
     * 继承 BaseView
     */
    public interface ListView extends BaseView {}
    
    /**
     * 详情视图 - 包含所有字段
     * 适用于：详情页、编辑页
     * 继承 ListView
     */
    public interface DetailView extends ListView {}
    
    /**
     * 移动端视图 - 精简字段（减少数据传输）
     * 适用于：移动端列表、快速预览
     * 独立视图，不继承其他视图
     */
    public interface MobileView {}
    
    /**
     * 移动端详情视图 - 比移动端列表更详细
     * 适用于：移动端详情页
     * 继承 MobileView
     */
    public interface MobileDetailView extends MobileView {}
    
    /**
     * 管理视图 - 包含管理相关字段
     * 适用于：管理员查看的数据
     */
    public interface AdminView extends DetailView {}
}