package com.uh.system.manage;

import com.uh.system.domain.MenuDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

/**
 * 菜单注册中心：启动时加载 menu.yml，对外提供内存菜单树和编码索引。
 * <p>
 * 支持通过标签（labels）过滤菜单：default=默认所有环境显示；dev=仅开发模式下显示。
 * YAML 缺失、格式非法、编码重复时直接抛异常中止启动。
 */
@Component
public class MenuRegistry {

    private static final Logger log = LoggerFactory.getLogger(MenuRegistry.class);

    @Value("classpath:menu.yml")
    private Resource menuYmlResource;

    /** 从 YAML 加载的原始菜单节点（未过滤） */
    private List<Map<String, Object>> rawMenuNodes = new ArrayList<>();

    /** 当前激活的菜单树（含目录、菜单、按钮） */
    private List<MenuDefinition> menuTree = new ArrayList<>();

    /** 当前激活的 menuCode -> MenuDefinition 扁平索引 */
    private Map<String, MenuDefinition> menuCodeIndex = new LinkedHashMap<>();

    /**
     * 启动时加载 YAML 原始数据，并以 "default" 标签初始化菜单树。
     */
    @PostConstruct
    public void init() {
        try (InputStream is = menuYmlResource.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);
            if (root == null || !root.containsKey("menus")) {
                throw new IllegalStateException("menu.yml 中未找到 'menus' 根节点");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> menuNodes = (List<Map<String, Object>>) root.get("menus");
            if (menuNodes == null || menuNodes.isEmpty()) {
                throw new IllegalStateException("menu.yml 中 'menus' 节点为空");
            }

            this.rawMenuNodes = menuNodes;
            // 默认只加载 default 标签菜单
            init(Collections.singleton("default"));
        } catch (Exception e) {
            throw new IllegalStateException("MenuRegistry 加载 menu.yml 失败", e);
        }
    }

    /**
     * 根据指定标签集合重新加载（过滤）菜单树。
     * <p>
     * 标签含义：default=默认所有环境显示；dev=仅开发模式下显示。
     * 无 labels 字段的节点按 "default" 处理（总是显示）。
     *
     * @param activeLabels 当前激活的标签集合，如 ["default"] 或 ["default", "dev"]
     */
    public synchronized void init(Collection<String> activeLabels) {
        Map<String, MenuDefinition> newIndex = new LinkedHashMap<>();
        List<MenuDefinition> newTree = parseMenuList(rawMenuNodes, null, activeLabels, newIndex);
        this.menuCodeIndex = newIndex;
        this.menuTree = newTree;
        log.info("MenuRegistry reloaded: {} menu codes active (labels={})", newIndex.size(), activeLabels);
    }

    /**
     * 递归解析 YAML 菜单节点列表，根据 activeLabels 过滤
     */
    @SuppressWarnings("unchecked")
    private List<MenuDefinition> parseMenuList(List<Map<String, Object>> nodes, String parentCode,
                                               Collection<String> activeLabels,
                                               Map<String, MenuDefinition> index) {
        List<MenuDefinition> result = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            // 标签过滤：不符合当前激活标签的节点跳过
            String labels = (String) node.get("labels");
            if (!shouldInclude(labels, activeLabels)) {
                continue;
            }

            MenuDefinition def = new MenuDefinition();

            String menuCode = (String) node.get("menuCode");
            if (menuCode == null || menuCode.isEmpty()) {
                throw new IllegalStateException("menu.yml 中存在缺少 menuCode 的菜单节点");
            }
            if (index.containsKey(menuCode)) {
                throw new IllegalStateException("menu.yml 中 menuCode 重复: " + menuCode);
            }

            def.setMenuCode(menuCode);
            def.setMenuName((String) node.get("menuName"));
            def.setMenuType((String) node.get("menuType"));
            def.setPath((String) node.get("path"));
            def.setComponent((String) node.get("component"));
            def.setQuery((String) node.get("query"));
            def.setIcon((String) node.get("icon"));
            def.setLabels(labels);
            def.setParentCode(parentCode);

            Object inFrameObj = node.get("inFrame");
            if (inFrameObj instanceof Boolean) {
                def.setInFrame((Boolean) inFrameObj);
            }

            // 校验必填字段
            String menuType = def.getMenuType();
            if (menuType == null || menuType.isEmpty()) {
                throw new IllegalStateException("menu.yml 中 menuCode=" + menuCode + " 缺少 menuType");
            }
            if (("M".equals(menuType) || "C".equals(menuType)) && (def.getPath() == null || def.getPath().isEmpty())) {
                throw new IllegalStateException("menu.yml 中 menuCode=" + menuCode + " (menuType=" + menuType + ") 缺少 path");
            }
            if ("C".equals(menuType) && (def.getComponent() == null || def.getComponent().isEmpty())) {
                throw new IllegalStateException("menu.yml 中 menuCode=" + menuCode + " (menuType=C) 缺少 component");
            }

            index.put(menuCode, def);

            // 递归解析子节点（子节点继承父节点通过过滤，但子节点本身也可有自己的标签）
            List<Map<String, Object>> childNodes = (List<Map<String, Object>>) node.get("children");
            if (childNodes != null && !childNodes.isEmpty()) {
                def.setChildren(parseMenuList(childNodes, menuCode, activeLabels, index));
            }

            result.add(def);
        }
        return result;
    }

    /**
     * 判断节点是否应包含在当前激活标签集合中。
     * 无标签的节点视为 "default"，总是包含。
     */
    private boolean shouldInclude(String labels, Collection<String> activeLabels) {
        if (labels == null || labels.trim().isEmpty()) {
            // 无标签，视为 default，总是显示
            return true;
        }
        for (String label : labels.split(",")) {
            if (activeLabels.contains(label.trim())) {
                return true;
            }
        }
        return false;
    }

    /** 获取当前激活的菜单树（含目录、菜单、按钮节点） */
    public List<MenuDefinition> getMenuTree() {
        return Collections.unmodifiableList(menuTree);
    }

    /** 根据 menuCode 查找菜单节点 */
    public MenuDefinition getByCode(String menuCode) {
        return menuCodeIndex.get(menuCode);
    }

    /** 获取所有激活的菜单编码（包含按钮权限编码） */
    public Set<String> getAllMenuCodes() {
        return Collections.unmodifiableSet(menuCodeIndex.keySet());
    }

    /** 获取所有激活的按钮权限编码（menuType=F 的节点） */
    public Set<String> getAllPermCodes() {
        Set<String> perms = new LinkedHashSet<>();
        for (MenuDefinition def : menuCodeIndex.values()) {
            if ("F".equals(def.getMenuType())) {
                perms.add(def.getMenuCode());
            }
        }
        return Collections.unmodifiableSet(perms);
    }
}
