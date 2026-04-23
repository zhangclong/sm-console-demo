package com.uh.system.service.impl;


import com.uh.common.config.AppHomeConfig;
import com.uh.common.config.ConsoleConfig;
import com.uh.common.config.DataSourceConfig;
import com.uh.common.utils.ClassResourceUtils;
import com.uh.common.utils.DateUtils;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.sql.ScriptRunner;
import com.uh.system.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@Service
public class DatabaseServiceImpl implements DatabaseService {

    protected final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);

    private static final String BACKUP_PREFIX = "export";

    private static final String SPLITER = "-";

    private static final String BACKUP_SUFFIX = ".sql.gz";

    /** 外部命令执行超时（秒），避免 mysqldump/mysql 进程卡住长期占用资源。 */
    private static final int EXTERNAL_COMMAND_TIMEOUT_SECONDS = 30 * 60;

    /** 匹配 {@code jdbc:mysql://host:port/dbName?params} 的 URL 片段。 */
    private static final Pattern MYSQL_URL_PATTERN =
            Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+).*", Pattern.CASE_INSENSITIVE);

    @Autowired
    private DataSource dataSource;

    @Resource
    private DataSourceConfig dataSourceConf;

    @Resource
    private ConsoleConfig consoleConfig;

    @Override
    public void executeSql(String classPathSql) {

        try(Connection conn = dataSource.getConnection();
            StringWriter outWriter = new StringWriter(); StringWriter errWriter = new StringWriter();
            InputStreamReader reader = new InputStreamReader(ClassResourceUtils.class.getClassLoader().getResourceAsStream(classPathSql))) {

            ScriptRunner runner = new ScriptRunner(conn, true, false);

            runner.setLogWriter(outWriter);
            runner.setErrorLogWriter(errWriter);

            runner.runScript(reader);

            String err = errWriter.toString();
            String out = outWriter.toString();

            if(StringUtils.isNotEmpty(out)) {
                logger.info( "executeSQL output(" + classPathSql + "):\n" + out);
            }

            if(StringUtils.isNotEmpty(err)) {
                logger.error( "executeSQL error(" + classPathSql + "):\n" + err);
                throw new RuntimeException("executeSQL Error!");
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public File backupDatabase() {
        File backDir = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH);
        if(!backDir.exists()) { backDir.mkdirs(); }

        String fileName =  BACKUP_PREFIX + SPLITER + DateUtils.dateTimeNow() + BACKUP_SUFFIX;
        File backFile = new File(backDir, fileName);

        MySqlConnectionInfo info = parseMysqlUrl(dataSourceConf.getUrl());

        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("--host=" + info.host);
        command.add("--port=" + info.port);
        command.add("--user=" + dataSourceConf.getUsername());
        command.add("--password=" + dataSourceConf.getPassword());
        command.add("--protocol=TCP");
        command.add("--single-transaction");
        command.add("--set-gtid-purged=OFF");
        command.add("--default-character-set=utf8mb4");
        command.add("--routines");
        command.add("--triggers");
        command.add("--events");
        command.add(info.database);

        try {
            runCommandWithGzipOutput(command, backFile);
        } catch (Exception e) {
            // 失败时清理半成品
            if (backFile.exists() && !backFile.delete()) {
                logger.warn("Failed to delete partial backup file: {}", backFile.getAbsolutePath());
            }
            throw new RuntimeException("Backup database error!", e);
        }

        logger.info("Database backup in file:" + backFile.getAbsolutePath());

        return backFile;
    }

    @Override
    public List<File> cleanExpiredBackup(int maxHistory) {
        File backDir = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH);
        File[] hisBackFiles = backDir.listFiles();
        List<File> deletedFiles = new ArrayList<>();
        if (hisBackFiles == null) {
            return deletedFiles;
        }
        for(File hisBackFile : hisBackFiles) {
            String hisFileName = hisBackFile.getName();
            // 仅识别 BACKUP_PREFIX 前缀、BACKUP_SUFFIX 后缀的文件
            if (!hisFileName.startsWith(BACKUP_PREFIX + SPLITER) || !hisFileName.endsWith(BACKUP_SUFFIX)) {
                continue;
            }
            String dateTimeStr = hisFileName.substring(
                    (BACKUP_PREFIX + SPLITER).length(),
                    hisFileName.length() - BACKUP_SUFFIX.length());
            try {
                Date exportDate = DateUtils.parseDate(dateTimeStr, DateUtils.YYYYMMDDHHMMSS);
                long range = System.currentTimeMillis() - exportDate.getTime();
                long maxHistoryRange = (long) maxHistory * 24 * 3600 * 1000;
                if(range > maxHistoryRange) {
                    if (hisBackFile.delete()) {
                        deletedFiles.add(hisBackFile);
                        logger.info("Expired database export file was deleted. File=" + hisBackFile.getAbsoluteFile());
                    } else {
                        logger.warn("Failed to delete expired database export file: {}", hisBackFile.getAbsolutePath());
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return deletedFiles;
    }


    @Override
    public void restoreDatabase(String fileName) {
        File importFile = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH, fileName);
        if(!importFile.exists()) {
            throw new RuntimeException("The import database dump file("
                    + importFile.getPath() + ") not exists, when restore database." );
        }

        MySqlConnectionInfo info = parseMysqlUrl(dataSourceConf.getUrl());

        List<String> command = new ArrayList<>();
        command.add("mysql");
        command.add("--host=" + info.host);
        command.add("--port=" + info.port);
        command.add("--user=" + dataSourceConf.getUsername());
        command.add("--password=" + dataSourceConf.getPassword());
        command.add("--protocol=TCP");
        command.add("--default-character-set=utf8mb4");
        command.add(info.database);

        try {
            runCommandWithInputStream(command, importFile);
            logger.info("Database was restored from: {}", importFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Restore database error!", e);
        }
    }

    // ---------- 内部辅助方法 ----------

    /**
     * 执行外部命令，将标准输出 gzip 压缩写入目标文件。错误输出通过日志记录。
     */
    private void runCommandWithGzipOutput(List<String> command, File outputFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);
        logger.info("Execute external command: {}", maskCommand(command));
        Process process = builder.start();

        // 单独线程抽取 stderr，避免缓冲区满阻塞进程
        Thread errThread = consumeErrorStream(process.getErrorStream(), "mysqldump");

        try (InputStream stdout = process.getInputStream();
             OutputStream out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            byte[] buffer = new byte[8 * 1024];
            int n;
            while ((n = stdout.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }

        waitForProcess(process, errThread, command.get(0));
    }

    /**
     * 执行外部命令，将指定文件内容（自动识别 gzip）作为其标准输入。
     */
    private void runCommandWithInputStream(List<String> command, File inputFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);
        logger.info("Execute external command: {}", maskCommand(command));
        Process process = builder.start();

        Thread errThread = consumeErrorStream(process.getErrorStream(), "mysql");

        try (InputStream source = openMaybeGzip(inputFile);
             OutputStream stdin = process.getOutputStream()) {
            byte[] buffer = new byte[8 * 1024];
            int n;
            while ((n = source.read(buffer)) != -1) {
                stdin.write(buffer, 0, n);
            }
            stdin.flush();
        }

        waitForProcess(process, errThread, command.get(0));
    }

    private InputStream openMaybeGzip(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".gz")) {
            return new GZIPInputStream(new FileInputStream(file));
        }
        return new FileInputStream(file);
    }

    private Thread consumeErrorStream(InputStream stderr, String tag) {
        Thread t = new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try (InputStream in = stderr) {
                byte[] buf = new byte[1024];
                int n;
                while ((n = in.read(buf)) != -1) {
                    sb.append(new String(buf, 0, n, java.nio.charset.StandardCharsets.UTF_8));
                }
            } catch (IOException ignored) {
                // stderr closed on process exit
            } finally {
                if (sb.length() > 0) {
                    logger.warn("[{} stderr] {}", tag, sb.toString().trim());
                }
            }
        }, tag + "-stderr-consumer");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void waitForProcess(Process process, Thread errThread, String commandName)
            throws InterruptedException {
        boolean finished = process.waitFor(EXTERNAL_COMMAND_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException(commandName + " timed out after " + EXTERNAL_COMMAND_TIMEOUT_SECONDS + "s");
        }
        errThread.join(5_000);
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException(commandName + " exited with non-zero code: " + exitCode);
        }
    }

    /** 用于日志输出时屏蔽 --password 参数。 */
    private static String maskCommand(List<String> command) {
        List<String> masked = new ArrayList<>(command.size());
        for (String arg : command) {
            if (arg != null && arg.startsWith("--password=")) {
                masked.add("--password=***");
            } else {
                masked.add(arg);
            }
        }
        return Arrays.toString(masked.toArray());
    }

    /** 从 MySQL JDBC URL 中解析出 host/port/database。 */
    static MySqlConnectionInfo parseMysqlUrl(String url) {
        if (url == null) {
            throw new IllegalStateException("datasource.url is not configured");
        }
        Matcher m = MYSQL_URL_PATTERN.matcher(url.trim());
        if (!m.matches()) {
            // 避免异常信息中回显包含用户名/密码参数的完整 JDBC URL
            throw new IllegalStateException("Unsupported MySQL JDBC url (credentials masked)");
        }
        MySqlConnectionInfo info = new MySqlConnectionInfo();
        info.host = m.group(1);
        info.port = m.group(2) != null ? m.group(2) : "3306";
        info.database = m.group(3);
        return info;
    }

    static class MySqlConnectionInfo {
        String host;
        String port;
        String database;
    }
}
