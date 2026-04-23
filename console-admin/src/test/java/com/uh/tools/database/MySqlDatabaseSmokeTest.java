package com.uh.tools.database;

import com.uh.ConsoleApplication;
import com.uh.common.config.ApplicationRootConfig;
import com.uh.common.config.DataSourceConfig;
import com.uh.common.core.YamlConfigManager;
import com.uh.system.service.MySqlDatabaseInitializer;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL 数据库初始化冒烟测试。
 * <p>
 * 通过 {@link MySqlDatabaseInitializer} 连接配置中指定的 MySQL 实例；
 * 若目标库为全新库（无 {@code sys_user} 表），会自动执行
 * {@code sql/1.commons.sql}，并验证核心表/初始数据已就绪。
 * <p>
 * CI 中由 workflow 提供一个 MySQL 8.x service 容器，并确保
 * {@code consoleDb} 等连接信息与 {@code apphome/config/application.yml} 匹配。
 * <p>
 * 工作目录为 {@code apphome/}（Surefire 已配置）。当 {@code console-admin/sql/*.sql}
 * 发生变更时，该测试会在 CI 中自动触发。
 */
class MySqlDatabaseSmokeTest {

    private static DataSourceConfig dsConfig;
    private Connection connection;

    @BeforeAll
    static void ensureDatabaseInitialized() throws Exception {
        ApplicationRootConfig rootConfig = YamlConfigManager.loadConfig(ConsoleApplication.CONFIG_FILES);
        dsConfig = rootConfig.getDatasource();
        Class.forName(dsConfig.getDriverClassName());
        MySqlDatabaseInitializer initializer = new MySqlDatabaseInitializer(
                dsConfig.getUrl(),
                dsConfig.getUsername(),
                dsConfig.getPassword(),
                rootConfig.getConsole().getAdminPasswordExpireDays());
        initializer.initializeIfAbsent();
    }

    @BeforeEach
    void openConnection() throws Exception {
        connection = DriverManager.getConnection(dsConfig.getUrl(), dsConfig.getUsername(), dsConfig.getPassword());
    }

    @AfterEach
    void closeConnection() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ---- 核心系统表存在性验证 ----

    @Test
    void sysUserTableShouldExist() throws Exception {
        assertTableExists("sys_user");
    }

    @Test
    void sysRoleTableShouldExist() throws Exception {
        assertTableExists("sys_role");
    }

    @Test
    void sysMenuTableShouldNotExist() throws Exception {
        assertTableNotExists("sys_menu");
    }

    // ---- 初始数据完整性验证 ----

    @Test
    void adminUserShouldExist() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM sys_user WHERE user_name = 'admin'");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "admin 用户应存在");
        }
    }

    @Test
    void atLeastOneRoleShouldExist() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_role");
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "sys_role 表应有初始角色数据");
        }
    }

    @Test
    void adminRoleShouldExist() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM sys_role WHERE role_key = 'admin'");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "admin 角色应存在");
        }
    }

    @Test
    void userAndRoleAssociationShouldExist() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM sys_user_role WHERE user_id = 1 AND role_id = 1");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "admin 用户应与 admin 角色关联");
        }
    }

    @Test
    void sysRoleMenuTableShouldExistWithMenuCodeColumn() throws Exception {
        assertTableExists("sys_role_menu");
        // 验证 menu_code 列存在（安全管理员角色应有绑定数据）
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 5");
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "安全管理员角色(role_id=5)应有菜单编码绑定");
        }
    }

    // ---- 辅助方法 ----

    private void assertTableExists(String tableName) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        // MySQL 表名在 information_schema 中大小写敏感性取决于服务器配置；
        // 这里同时尝试小写名称以适配 Linux 上默认的 lower_case_table_names=0 配置。
        try (ResultSet rs = meta.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return;
            }
        }
        try (ResultSet rs = meta.getTables(connection.getCatalog(), null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            assertTrue(rs.next(), "表 " + tableName + " 应存在于数据库中");
        }
    }

    private void assertTableNotExists(String tableName) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet lower = meta.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"});
             ResultSet upper = meta.getTables(connection.getCatalog(), null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            assertFalse(lower.next() || upper.next(), "表 " + tableName + " 不应继续存在于数据库中");
        }
    }
}
