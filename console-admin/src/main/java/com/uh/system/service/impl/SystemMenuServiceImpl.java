package com.uh.system.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uh.common.constant.Constants;
import com.uh.common.constant.UserConstants;
import com.uh.common.core.domain.TreeSelect;
import com.uh.system.domain.MenuDefinition;
import com.uh.common.utils.SecurityUtils;
import com.uh.common.utils.StringUtils;
import com.uh.system.domain.vo.MetaVo;
import com.uh.system.domain.vo.RouterVo;
import com.uh.system.manage.MenuRegistry;
import com.uh.system.mapper.SysRoleMenuMapper;
import com.uh.system.service.SystemMenuService;

/**
 * 系统菜单只读业务层处理（数据来源为 MenuRegistry）
 *
 * @author XiaoZhangTongZhi
 */
@Service
public class SystemMenuServiceImpl implements SystemMenuService
{

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private MenuRegistry menuRegistry;

    /**
     * 根据用户ID查询权限（切换到 MenuRegistry + sys_role_menu.menu_code）
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId)
    {
        if (SecurityUtils.isAdmin(userId))
        {
            return menuRegistry.getAllPermCodes();
        }
        List<String> menuCodes = roleMenuMapper.selectMenuCodesByUserId(userId);
        return new HashSet<>(menuCodes);
    }

    /**
     * 根据角色ID查询权限（切换到 sys_role_menu.menu_code）
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId)
    {
        List<String> menuCodes = roleMenuMapper.selectMenuCodesByRoleId(roleId);
        return new HashSet<>(menuCodes);
    }

    /**
     * 根据用户ID查询菜单树（MenuDefinition 版本，过滤 F 按钮节点）
     *
     * @param userId 用户ID
     * @return 菜单定义列表（仅 M/C 类型）
     */
    @Override
    public List<MenuDefinition> selectMenuDefTreeByUserId(Long userId)
    {
        if (SecurityUtils.isAdmin(userId))
        {
            return filterButtonNodes(menuRegistry.getMenuTree());
        }
        Set<String> authorizedCodes = new HashSet<>(
            roleMenuMapper.selectMenuCodesByUserId(userId)
        );
        return pruneMenuTree(menuRegistry.getMenuTree(), authorizedCodes);
    }

    /**
     * 根据角色ID查询已授权的菜单编码列表
     *
     * @param roleId 角色ID
     * @return 菜单编码列表
     */
    @Override
    public List<String> selectMenuCodesByRoleId(Long roleId)
    {
        return roleMenuMapper.selectMenuCodesByRoleId(roleId);
    }

    /**
     * 构建前端路由所需要的菜单（MenuDefinition 版本）
     *
     * @param menus 菜单定义列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<MenuDefinition> menus)
    {
        List<RouterVo> routers = new LinkedList<>();
        for (MenuDefinition menu : menus)
        {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), false, menu.getPath()));
            List<MenuDefinition> cMenus = menu.getChildren();
            if (!cMenus.isEmpty() && UserConstants.TYPE_DIR.equals(menu.getMenuType()))
            {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            }
            else if (isMenuFrame(menu))
            {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(StringUtils.capitalize(menu.getPath()));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), false, menu.getPath()));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            else if (menu.getParentCode() == null && isInnerLink(menu))
            {
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(StringUtils.capitalize(routerPath));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要下拉树结构（MenuDefinition 版本）
     *
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect()
    {
        return menuRegistry.getMenuTree().stream()
            .map(TreeSelect::new)
            .collect(Collectors.toList());
    }

    // ---- MenuDefinition 路由构建辅助方法 ----

    /**
     * 获取路由名称
     */
    public String getRouteName(MenuDefinition menu)
    {
        String routerName = StringUtils.capitalize(menu.getPath());
        if (isMenuFrame(menu))
        {
            routerName = StringUtils.EMPTY;
        }
        return routerName;
    }

    /**
     * 获取路由地址
     */
    public String getRouterPath(MenuDefinition menu)
    {
        String routerPath = menu.getPath();
        // 内链打开外网方式
        if (menu.getParentCode() != null && isInnerLink(menu))
        {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        if (menu.getParentCode() == null && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && !menu.isInFrame())
        {
            routerPath = "/" + menu.getPath();
        }
        // 非外链并且是一级目录（类型为菜单）
        else if (isMenuFrame(menu))
        {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     */
    public String getComponent(MenuDefinition menu)
    {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu))
        {
            component = menu.getComponent();
        }
        else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentCode() != null && isInnerLink(menu))
        {
            component = UserConstants.INNER_LINK;
        }
        else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu))
        {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为菜单内部跳转（一级菜单且非外链）
     */
    public boolean isMenuFrame(MenuDefinition menu)
    {
        return menu.getParentCode() == null && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && !menu.isInFrame();
    }

    /**
     * 是否为内链组件
     */
    public boolean isInnerLink(MenuDefinition menu)
    {
        return !menu.isInFrame() && StringUtils.ishttp(menu.getPath());
    }

    /**
     * 是否为parent_view组件
     */
    public boolean isParentView(MenuDefinition menu)
    {
        return menu.getParentCode() != null && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 过滤掉按钮节点（menuType=F），只保留目录和菜单
     */
    private List<MenuDefinition> filterButtonNodes(List<MenuDefinition> tree)
    {
        List<MenuDefinition> result = new ArrayList<>();
        for (MenuDefinition node : tree)
        {
            if (!"F".equals(node.getMenuType()))
            {
                MenuDefinition copy = shallowCopy(node);
                copy.setChildren(filterButtonNodes(node.getChildren()));
                result.add(copy);
            }
        }
        return result;
    }

    /**
     * 根据授权编码集合裁剪菜单树（仅保留授权的 M/C 节点，过滤 F 按钮节点）
     */
    private List<MenuDefinition> pruneMenuTree(List<MenuDefinition> tree, Set<String> authorizedCodes)
    {
        List<MenuDefinition> result = new ArrayList<>();
        for (MenuDefinition node : tree)
        {
            if ("F".equals(node.getMenuType()))
            {
                continue;
            }
            if ("M".equals(node.getMenuType()))
            {
                // 目录节点：递归子节点，只要有一个子节点被保留，目录就保留
                List<MenuDefinition> prunedChildren = pruneMenuTree(node.getChildren(), authorizedCodes);
                if (!prunedChildren.isEmpty())
                {
                    MenuDefinition copy = shallowCopy(node);
                    copy.setChildren(prunedChildren);
                    result.add(copy);
                }
            }
            else if ("C".equals(node.getMenuType()) && authorizedCodes.contains(node.getMenuCode()))
            {
                // 菜单节点：直接判断是否授权
                MenuDefinition copy = shallowCopy(node);
                copy.setChildren(new ArrayList<>());
                result.add(copy);
            }
        }
        return result;
    }

    /**
     * 浅拷贝 MenuDefinition（不复制 children）
     */
    private MenuDefinition shallowCopy(MenuDefinition src)
    {
        MenuDefinition copy = new MenuDefinition();
        copy.setMenuCode(src.getMenuCode());
        copy.setMenuName(src.getMenuName());
        copy.setMenuType(src.getMenuType());
        copy.setPath(src.getPath());
        copy.setComponent(src.getComponent());
        copy.setQuery(src.getQuery());
        copy.setInFrame(src.isInFrame());
        copy.setIcon(src.getIcon());
        copy.setLabels(src.getLabels());
        copy.setParentCode(src.getParentCode());
        return copy;
    }

    /**
     * 内链域名特殊字符替换
     *
     * @return 替换后的内部路由路径
     */
    public String innerLinkReplaceEach(String path)
    {
        return StringUtils.replaceEach(path, new String[] { Constants.HTTP, Constants.HTTPS, Constants.WWW, "." },
                new String[] { "", "", "", "/" });
    }
}

