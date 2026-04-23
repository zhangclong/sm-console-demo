package com.uh.system.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * menu.yml 中单个菜单节点的 Java 映射类。
 * <p>
 * 每个节点对应 YAML 中的一项菜单配置，{@code menuCode} 同时作为权限标识（perms）。
 */
public class MenuDefinition {

    /** 语义化菜单编码，同时作为权限标识（perms），如 "console:rdsservice:list" */
    private String menuCode;

    /** 菜单名称（直接使用中文） */
    private String menuName;

    /** 菜单类型：M=目录 C=菜单 F=按钮 */
    private String menuType;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 路由参数 */
    private String query;

    /** 在控制台内嵌（iframe）打开外链，默认 false（新窗口打开） */
    private boolean inFrame = false;

    /** 菜单图标 */
    private String icon;

    /**
     * 标签限制，多个用英文逗号分隔。
     * 可选值：dev
     */
    private String labels;

    /** 父节点的 menuCode（在 MenuRegistry 解析时设置，不在 YAML 中定义） */
    private String parentCode;

    /** 子菜单列表 */
    private List<MenuDefinition> children = new ArrayList<>();

    public MenuDefinition() {
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isInFrame() {
        return inFrame;
    }

    public void setInFrame(boolean inFrame) {
        this.inFrame = inFrame;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public List<MenuDefinition> getChildren() {
        return children;
    }

    public void setChildren(List<MenuDefinition> children) {
        this.children = children;
    }
}
