package com.uh.system.service;

import com.uh.common.utils.ClassResourceUtils;
import com.uh.common.utils.sql.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 应用首次启动时的 MySQL 数据库初始化器。
 * <p>
 * 连接目标库后通过 {@code information_schema.tables} 判断核心表 {@code sys_user} 是否存在：
 * 不存在则认为是全新数据库，执行类路径下的 {@code sql/1.commons.sql}
 * 完成建表与初始化数据。存在则跳过（幂等）。
 */
public class MySqlDatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(MySqlDatabaseInitializer.class);

    private static final String SENTINEL_TABLE = "sys_user";

    private final String url;
    private final String username;
    private final String password;
    private final int adminPasswordExpireDays;

    public MySqlDatabaseInitializer(String url, String username, String password, int adminPasswordExpireDays) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.adminPasswordExpireDays = adminPasswordExpireDays;
    }

    /**
     * 若目标数据库中不存在 {@value #SENTINEL_TABLE} 表，则执行初始化并返回 true；否则返回 false。
     */
    public boolean initializeIfAbsent() throws Exception {
        logger.info("Checking MySQL database initialization state via {}", maskUrl(url));

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            if (sentinelTableExists(conn)) {
                logger.info("Database already initialized (table '{}' exists), skip initialization.", SENTINEL_TABLE);
                return false;
            }

            logger.info("Table '{}' not found, starting first-time database initialization.", SENTINEL_TABLE);
            conn.setAutoCommit(false);
            try {
                executeClasspathSql(conn, "sql/1.commons.sql");
                if (adminPasswordExpireDays > 0) {
                    updateAdminPasswordExpired(conn, adminPasswordExpireDays);
                }
                conn.commit();
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.warn("Rollback failed during initialization", rollbackEx);
                }
                throw e;
            }
        }

        logger.info("Database initialization completed successfully.");
        return true;
    }

    private boolean sentinelTableExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, SENTINEL_TABLE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void executeClasspathSql(Connection conn, String classpathResource) throws Exception {
        logger.info("Executing SQL resource: {}", classpathResource);
        try (InputStream inputStream = ClassResourceUtils.loadResourceAsStream(classpathResource)) {
            if (inputStream == null) {
                throw new IllegalStateException("SQL resource not found on classpath: " + classpathResource);
            }
            ScriptRunner runner = new ScriptRunner(conn, false, true);
            runner.setLogWriter(new StringWriter());
            runner.runScript(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
    }

    private void updateAdminPasswordExpired(Connection conn, int days) throws SQLException {
        String sql = "UPDATE sys_user SET password_expired = DATE_ADD(NOW(), INTERVAL ? DAY) WHERE user_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setString(2, "admin");
            int updated = ps.executeUpdate();
            if (updated != 1) {
                logger.warn("Expected to update 1 admin user, but updated {} rows", updated);
            }
            logger.info("Updated admin password_expired with days={}, affected rows={}", days, updated);
        }
    }

    /**
     * 从 JDBC URL 中屏蔽可能出现的用户名/密码参数，避免日志泄漏。
     */
    private static String maskUrl(String url) {
        if (url == null) {
            return "<null>";
        }
        // JDBC URL 中的 password/user 参数做简单打码
        return url.replaceAll("(?i)(password=)[^&;]*", "$1***")
                .replaceAll("(?i)(user=)[^&;]*", "$1***");
    }
}
