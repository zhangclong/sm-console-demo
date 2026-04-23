package com.uh.system.manage;

import com.uh.system.domain.MenuDefinition;
import com.uh.system.service.config.TestAppConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MenuRegistry 集成测试：验证 menu.yml 加载、索引、权限编码等核心功能。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestAppConfig.class)
class MenuRegistryTest {

    @Autowired
    private MenuRegistry menuRegistry;

    @Test
    void menuTree_shouldNotBeEmpty() {
        List<MenuDefinition> tree = menuRegistry.getMenuTree();
        assertNotNull(tree, "菜单树不应为 null");
        assertFalse(tree.isEmpty(), "菜单树不应为空");
    }

    @Test
    void allMenuCodes_shouldContainExpectedEntries() {
        Set<String> codes = menuRegistry.getAllMenuCodes();
        assertFalse(codes.isEmpty(), "菜单编码集合不应为空");
        assertTrue(codes.contains("system"), "应包含顶级目录 'system'");
        assertTrue(codes.contains("system:user"), "应包含菜单 'system:user'");
        assertTrue(codes.contains("system:user:query"), "应包含按钮权限 'system:user:query'");
    }

    @Test
    void getByCode_shouldReturnCorrectDefinition() {
        MenuDefinition userMenu = menuRegistry.getByCode("system:user");
        assertNotNull(userMenu, "应能按 menuCode 查到 'system:user'");
        assertEquals("用户管理", userMenu.getMenuName());
        assertEquals("C", userMenu.getMenuType());
        assertEquals("user", userMenu.getPath());
        assertEquals("system/user/index", userMenu.getComponent());
        assertEquals("system", userMenu.getParentCode());
    }

    @Test
    void getByCode_nonExistent_shouldReturnNull() {
        assertNull(menuRegistry.getByCode("nonexistent:menu:code"), "不存在的编码应返回 null");
    }

    @Test
    void allPermCodes_shouldOnlyContainFTypeMenus() {
        Set<String> permCodes = menuRegistry.getAllPermCodes();
        assertFalse(permCodes.isEmpty(), "权限编码集合不应为空");
        for (String code : permCodes) {
            MenuDefinition def = menuRegistry.getByCode(code);
            assertNotNull(def, "权限编码 " + code + " 应能在索引中找到");
            assertEquals("F", def.getMenuType(), "权限编码 " + code + " 应为 F 类型");
        }
    }

    @Test
    void topLevelNodes_shouldBeDirectories() {
        List<MenuDefinition> tree = menuRegistry.getMenuTree();
        for (MenuDefinition node : tree) {
            assertEquals("M", node.getMenuType(), "顶级节点 " + node.getMenuCode() + " 应为目录类型");
            assertNull(node.getParentCode(), "顶级节点 " + node.getMenuCode() + " 的 parentCode 应为 null");
        }
    }

    @Test
    void childNodes_shouldHaveCorrectParentCode() {
        MenuDefinition systemDir = menuRegistry.getByCode("system");
        assertNotNull(systemDir);
        assertFalse(systemDir.getChildren().isEmpty(), "system 目录应有子菜单");
        for (MenuDefinition child : systemDir.getChildren()) {
            assertEquals("system", child.getParentCode(), "system 的子节点 parentCode 应为 'system'");
        }
    }
}
